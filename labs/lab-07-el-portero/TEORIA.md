# Teoría · Módulo 9 (seguridad: autenticación y autorización)

## Índice

1. [La puerta que no existía](#1-la-puerta-que-no-existía)
2. [La cadena de filtros: la fila de porteros](#2-la-cadena-de-filtros-la-fila-de-porteros)
3. [Denegar por defecto](#3-denegar-por-defecto)
4. [BCrypt y por qué jamás MD5 ni texto plano](#4-bcrypt-y-por-qué-jamás-md5-ni-texto-plano)
5. [Anatomía del JWT: las tres partes](#5-anatomía-del-jwt-las-tres-partes)
6. [Un token sin firma es una opinión](#6-un-token-sin-firma-es-una-opinión)
7. [Firma simétrica vs asimétrica](#7-firma-simétrica-vs-asimétrica)
8. [Autenticación vs autorización: 401 vs 403](#8-autenticación-vs-autorización-401-vs-403)
9. [Expiración, refresh y el secreto fuera del repo](#9-expiración-refresh-y-el-secreto-fuera-del-repo)
10. [Panorama: passkeys / WebAuthn](#10-panorama-passkeys--webauthn)
11. [Tabla DO / DON'T · Glosario](#11-tabla-do--dont--glosario)
12. [Conclusiones y siembra del Módulo 9](#12-conclusiones-y-siembra-del-módulo-9)

> **Lo que NO va hoy:** CORS, CSRF y las cabeceras de seguridad. No es olvido: son la hora
> de M9 del Lab 08, junto con la resiliencia. Aquí cerramos la puerta y ponemos portero;
> allá blindamos el resto del edificio. En esta API deshabilitamos CSRF a propósito —es sin
> estado, la credencial viaja en el header, no en una cookie de sesión que un tercero pueda
> montar—.

---

## 1. La puerta que no existía

El Lab 06 dejó los folios perfectos: únicos, secuenciales, idempotentes. Y abiertos a
cualquiera con `curl`. Peor: el "login" del practicante entrega un `base64(rut:rol)` y un
filtro que lo lee y le cree. Eso no es seguridad; es un disfraz. Cualquiera fabrica su
credencial:

```bash
echo -n 'ladron:FUNCIONARIO' | base64      # y ya "eres" funcionario
```

Este lab desarma las dos capas: cierra la puerta por defecto, y reemplaza el disfraz por un
JWT **firmado** que el servidor valida, no cree.

---

## 2. La cadena de filtros: la fila de porteros

Spring Security es una **cadena de filtros** delante de tu aplicación. Cada petición pasa
por una fila de porteros antes de tocar un controlador: uno mira si traes token, otro lo
valida, otro decide si tu rol te deja pasar a ESA puerta. Si alguno dice que no, la petición
nunca llega a tu código.

En Spring Security 7 esa fila se arma con un `SecurityFilterChain` (un `@Bean`): ahí declaras
qué es público, cómo se valida el token, y qué exige cada ruta. No hay `WebSecurityConfigurerAdapter`
(murió en Security 6): todo es configuración basada en componentes.

---

## 3. Denegar por defecto

La regla de oro: **la lista blanca es de puertas ABIERTAS, nunca de cerradas.**

```java
.authorizeHttpRequests(reglas -> reglas
    .requestMatchers("/actuator/health", "/api/v1/auth/login").permitAll()
    .requestMatchers("/swagger-ui/**", "/v3/api-docs/**").permitAll()
    .anyRequest().authenticated())   // <- todo lo demás, cerrado
```

¿Por qué así y no al revés? Porque los errores deben caer hacia lo seguro. Si mañana agregas
un endpoint y olvidas su regla, con "denegar por defecto" nace **cerrado** (un 401 molesto,
pero seguro). Con "permitir por defecto" nacería **abierto** — una filtración esperando. El
test `E1` lo prueba: una ruta sin regla (`/api/contribuyentes`) responde 401 sin que nadie la
haya cerrado a mano.

---

## 4. BCrypt y por qué jamás MD5 ni texto plano

Una contraseña **no se guarda**: se guarda su *hash*, y no cualquiera.

- **Texto plano** — si te roban la base, tienen todas las claves. Inexcusable.
- **MD5 / SHA-1** — rápidos, y eso es el problema: una GPU prueba miles de millones por
  segundo. Diseñados para velocidad, no para resistir un ataque offline.
- **BCrypt / Argon2** — *lentos a propósito*, con un factor de costo ajustable y una *sal*
  por hash. Verificar una clave tarda milisegundos (no lo notas); romper millones tarda años.

La semilla de este curso nació con **BCrypt (cost 10)** el primer día —los hashes viven en la
`V2`, versionados a propósito, porque un hash no es un secreto—. Hoy, por fin, sirven: el
login carga el hash y deja que el `PasswordEncoder` (BCrypt) **compare**. Tu código nunca ve
la clave en claro más allá del instante de compararla.

---

## 5. Anatomía del JWT: las tres partes

Un JWT son tres bloques en base64url, separados por puntos:

```
   eyJhbGciOiJIUzI1NiJ9  .  eyJzdWIiOiI5ODc2NTQzLTIiLCJyb2xlcyI6WyJGVU5DSU9OQVJJTyJdfQ  .  3mF7...firma
        HEADER                              PAYLOAD (claims)                                    SIGNATURE
     (alg: HS256)              (sub, exp, iat, roles: [FUNCIONARIO])                       (HMAC del resto)
```

- **Header** — el algoritmo de firma (aquí HS256).
- **Payload** — los *claims*: quién eres (`sub`), hasta cuándo vale (`exp`), tus roles.
- **Signature** — un HMAC calculado sobre `header.payload` con un secreto. **Es lo único que
  no puedes falsificar sin el secreto.**

El header y el payload son base64 — legibles por cualquiera, NO cifrados. No pongas secretos
ahí. Lo que protege el token no es que esté escondido: es que está **firmado**.

---

## 6. Un token sin firma es una opinión

Aquí está el corazón del lab, en una frase proyectable:

> **Codificar no es cifrar, y cifrar no es firmar.**

El base64 del practicante *codifica* (cualquiera lo decodifica y lo re-arma). Un JWT no se
esconde: se *firma*. Cuando el Resource Server recibe el token, **recalcula** el HMAC sobre
el header y el payload con su secreto, y lo compara con la firma que viene. Si cambiaste un
solo byte del payload —te ascendiste a FUNCIONARIO—, el HMAC ya no cuadra: **401**.

El test `E3` lo demuestra: toma un token válido, le edita el payload, deja la firma vieja, y
el servidor lo rechaza. El payload era perfectamente válido como JSON. Lo que el servidor
detectó no fue un dato malo: fue una **firma que no correspondía**. Esa es la diferencia
entre creer y verificar.

---

## 7. Firma simétrica vs asimétrica

- **Simétrica (HS256)** — un mismo secreto firma y valida. Simple, rápida. Sirve cuando quien
  emite y quien valida son **el mismo servicio** (nuestro caso: la DGT firma y la DGT valida).
- **Asimétrica (RS256/ES256)** — una clave *privada* firma, una *pública* valida. El validador
  no puede firmar: puedes repartir la clave pública a mil servicios y ninguno puede fabricar
  tokens. Es lo correcto cuando emisor y validadores son distintos (un proveedor de identidad
  central, muchas APIs). La nombramos; no la instalamos hoy.

La regla: ¿el que valida necesita también firmar? Simétrica. ¿Muchos validan lo que uno
firma? Asimétrica.

---

## 8. Autenticación vs autorización: 401 vs 403

Dos preguntas distintas:

- **Autenticación** — *¿quién eres?* Si no traes credencial válida: **401 Unauthorized**.
- **Autorización** — *¿puedes hacer ESTO?* Si eres alguien válido pero sin permiso: **403 Forbidden**.

La analogía del edificio: **401** es no tener credencial para entrar. **403** es tener
credencial de visita e intentar entrar a la bóveda. Valentina (CONTRIBUYENTE) está
perfectamente autenticada; cuando intenta emitir un folio recibe **403**, no 401: su
credencial es real, pero no abre esa puerta. La autorización por rol se declara con
`@PreAuthorize("hasRole('FUNCIONARIO')")` sobre el método — y `E4` la prueba con los tres
personajes de la semilla.

---

## 9. Expiración, refresh y el secreto fuera del repo

- **Expiración (`exp`)** — un token vale poco tiempo (aquí, una hora). Un token robado caduca
  solo. Cuanto más corto, más seguro y más incómodo: es un balance, no un absoluto.
- **Refresh** — para no re-loguear cada hora, se emite un *refresh token* de vida larga y
  guardado con más cuidado, que canjea nuevos *access tokens* cortos. (Criterio de M9; no lo
  implementamos hoy.)
- **El secreto** — la clave de firma es un secreto de producción: **jamás en el repo**. Llega
  por `DGT_JWT_SECRET`, con un default de utilería SOLO en dev (abre juguetes, no firma nada
  real), y en prod obligatoria: `VerificadorDeSecretosProd` no deja arrancar sin ella. Misma
  doctrina del Lab 01: el que un `application.yml` no tenga la clave no es suerte, es diseño.

---

## 10. Panorama: passkeys / WebAuthn

La contraseña es el eslabón débil: se reutiliza, se filtra, se adivina. Las **passkeys**
(WebAuthn) la reemplazan por un par de claves criptográficas ligado a tu dispositivo y a un
gesto biométrico: no hay secreto compartido que robar del servidor, y el phishing clásico deja
de funcionar porque la credencial está atada al dominio real. Es hacia donde va la industria.
No lo implementamos hoy; conviene saber que existe y por qué gana terreno.

---

## 11. Tabla DO / DON'T · Glosario

| ✅ DO | ❌ DON'T |
|---|---|
| Denegar por defecto; lista blanca de puertas abiertas | Permitir por defecto y cerrar a mano |
| Validar la FIRMA del token | Decodificar el token y creerle (base64) |
| BCrypt/Argon2 con sal | MD5, SHA-1, o texto plano |
| 401 para "no sé quién eres", 403 para "no puedes esto" | Confundirlos (o devolver 200 y filtrar por dentro) |
| El secreto de firma por variable de entorno | La clave en un `application.yml` |
| Token corto + (si hace falta) refresh | Un token eterno |

- **Autenticación** — probar quién eres (login). Falla → 401.
- **Autorización** — decidir qué puedes hacer (rol). Falla → 403.
- **JWT** — token de tres partes: header, payload, firma.
- **HMAC** — firma simétrica: un secreto firma y valida.
- **Resource Server** — el que recibe el token y valida su firma antes de dejar pasar.
- **BCrypt** — hash de contraseñas lento a propósito, con sal.

---

## 12. Conclusiones y siembra del Módulo 9

Hoy la DGT dejó de ser una casa sin puerta. Cierra por defecto, sabe quién entra (login real
contra BCrypt), no cree en tokens sin firma (JWT verificado), y manda a cada rol a su puerta
(401 vs 403). Y el secreto que todo lo firma vive fuera del repo, como debe.

🌱 **Siembra del Módulo 9 (que abre el M10) — "Diplomacia con Tesorería".**

La puerta ya tiene portero. Pero mañana la DGT necesita hablar con **Tesorería** (TESO) para
confirmar pagos… y TESO se demora **treinta segundos** en contestar cuando anda de buenas. Y
cuando anda de malas, no contesta. La próxima semana, toda tu API se queda colgada esperando
un pago que no llega: un hilo tras otro, hasta que no queda ninguno libre y la DGT entera se
muere — por culpa de un servicio ajeno.

Trae paciencia. O mejor: **no la traigas** — aprende a no esperar para siempre. Timeouts,
reintentos, y un cortafuegos que se abre cuando el vecino se cae.

El Módulo 10 se llama *«Diplomacia con Tesorería»*.
