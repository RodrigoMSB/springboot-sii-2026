# SPEC-013 · Lab 07 «El portero»

| Campo | Valor |
|---|---|
| ID | SPEC-013 |
| Título | Séptimo laboratorio: la puerta que no existía — Spring Security 7 y JWT (S07 · M9 3,0 h) |
| Autor | Arquitecto |
| Aprueba | PO (Rodrigo) — **APROBADA** |
| Depende de | SPEC-012 |
| Estado | LISTA PARA EJECUCIÓN |

> **Instrucción de ejecución (mocito):** guardar este archivo íntegro en
> `docs/specs/SPEC-013-lab07-el-portero.md` y commitearlo en rama antes de ejecutar.
> Apila sobre la pila y decláralo. Protocolo de dos etapas vigente.

---

## §1 · Objetivo

Que exista `labs/lab-07-el-portero/`: la sesión completa de seguridad. El alumno sale
con: la API cerrada por defecto, login real contra la tabla de usuarios (BCrypt de la
semilla), JWT **entendido por dentro** (firmado, no creído) y validado en modo
productivo con OAuth2 Resource Server, y autorización por rol sobre la emisión de
folios. Encadenamiento: `starter/` = `solucion/` del Lab 06 + la puerta falsa + los
huecos.

## §2 · El crimen

Dos capas, en escalera:

1. **La puerta no existe.** Guion del relator (10 min): Carolina, con su celular en
   pantalla compartida, emite un folio con un `curl` pelado — sin usuario, sin clave,
   sin nada. *"Acabo de emitir un folio desde la micro. Yo ni siquiera trabajo en
   emisión. El Lab 06 hizo los folios únicos e idempotentes — perfectos. Perfectamente
   disponibles para cualquier ser humano con internet."*
2. **La puerta falsa del practicante.** El `starter/` trae un "login" ya hecho: entrega
   un "token" que es `base64(rut:rol)`, y un filtro que lo lee y deja pasar. Parece
   seguridad. Es un disfraz.

## §3 · Los tres actos

- **Acto 1 · Choque:** el curl anónimo emitiendo folios.
- **Acto 2 · El parche bruto que FUNCIONA:** el token base64 del practicante. El filtro
  lo acepta, los tests manuales pasan, hay "login". La guía lo demuele **en vivo**: el
  alumno fabrica su propio token con `echo 'cualquiera:FUNCIONARIO' | base64` y entra
  como funcionario. La lección en una frase (va en la teoría, proyectable): **un token
  sin firma es una opinión; codificar no es cifrar, y cifrar no es firmar.**
- **Acto 3 · La forma correcta:** JWT firmado — primero **a mano** (header.payload.
  signature, HMAC, ver la firma romperse al tocar un byte del payload: didáctico),
  después **en modo productivo**: OAuth2 Resource Server con `JwtDecoder`, validación
  declarativa, roles como authorities. Se entiende por dentro, se opera por fuera.

## §4 · Los TODOs (4 × ~15 min)

1. **TODO_1 — Cerrar la puerta:** `SecurityFilterChain` con **denegar por defecto**;
   público solo lo justo (health, login, Swagger si aplica — declara). Tests del
   enunciado: sin token → 401 en `/api/v1/tramites`; health → 200. La regla del pulgar
   en el Javadoc: *la lista blanca es de puertas abiertas, nunca de cerradas.*
2. **TODO_2 — El login real:** `UserDetailsService` contra la tabla `usuario` +
   `PasswordEncoder` BCrypt (la semilla del tronco ya trae hashes: cobra esa siembra de
   la SPEC-005); el endpoint de login emite el JWT (el servicio emisor viene en andamio
   con Javadoc — claims, expiración, firma HMAC — y el alumno completa lo esencial).
   Tests: credenciales buenas → token bien formado; malas → 401 **sin distinguir** si
   falló el usuario o la clave (la razón, en el Javadoc).
3. **TODO_3 — El validador que no cree:** Resource Server validando el JWT
   declarativamente; roles → authorities. Test estrella del enunciado: un token
   **adulterado** (payload editado, firma vieja) → 401. La firma detecta la mentira —
   el contraste directo con el base64 del acto 2.
4. **TODO_4 — Cada rol a su puerta:** `@PreAuthorize` — emitir folio solo FUNCIONARIO;
   CONTRIBUYENTE consulta lo suyo pero al emitir → **403** (y la teoría distingue 401
   de 403 con la analogía del edificio: no tener credencial vs tener credencial de
   visita e intentar entrar a la bóveda). Tests con los tres personajes de la semilla:
   Carolina emite, Valentina no, Ignacio (FISCALIZADOR) lee todo.

**La clave de firma y D-012:** ninguna clave real en archivos trackeados. Resolución
dentro de la doctrina del Lab 01: variable de entorno (`DGT_JWT_SECRET`) con un default
**solo en perfil dev**, marcado como utilería con el comentario que remite al pecado
original del compose; el perfil prod exige la variable (el `VerificadorDeSecretosProd`
del Lab 01 se extiende — otra siembra que se cobra). Declara la implementación exacta.

## §5 · Anatomía

La de SPEC-000 §7.6 completa. `TEORIA.md`: M9 núcleo (la cadena de filtros contada como
la fila de porteros; `SecurityFilterChain` y los defaults de Security 7; BCrypt/Argon2 y
por qué jamás MD5 ni texto plano — con el dato de que la semilla del curso nació con
BCrypt el primer día; anatomía del JWT con el diagrama de las tres partes; simétrica vs
asimétrica —cuándo cada una, la asimétrica se nombra, no se instala—; refresh y
expiración a nivel de criterio; passkeys/WebAuthn como panorama de dos párrafos). CORS,
CSRF y cabeceras **no van aquí**: son la hora de M9 del Lab 08 (déjalo dicho en la
teoría para que nadie los eche de menos). **Siembra del Lab 08:** *"la puerta ya tiene
portero. Mañana la DGT necesita hablar con Tesorería para confirmar pagos… y TESO se
demora treinta segundos en contestar cuando anda de buenas. La próxima semana, toda la
API se muere esperando un pago. Trae paciencia. O mejor: no la traigas."* Plantillas
con trampa registrada y la transcripción natural: **el 401 del token adulterado** (la
pregunta de criterio: *"el token adulterado tenía un payload perfectamente válido —
¿qué exactamente detectó el servidor?"*).

## §6 · Verificación del ejecutor (etapa 1)

Citado, sobre estado limpio: (1) `91-e2e` ×2 — starter falla nombrando los 4 TODOs,
solución 100 %; (2) el crimen visible: el curl anónimo emitiendo folio en el starter
(citado con el 201) y el mismo curl en la solución → 401; (3) acto 2 medido: el token
base64 fabricado a mano entra en el starter (citado) — y contra la solución, muere;
(4) el test del token adulterado: edita el payload de un JWT válido y cita el 401;
(5) los tres personajes: Carolina 201, Valentina 403 al emitir, Ignacio 200 al leer —
los tres citados; (6) manifiesto discrimina; (7) `SemillaCoherenteIT` y los guardianes
del tronco siguen verdes bajo la seguridad nueva (los tests viejos del enunciado de
labs previos que ahora chocarían con el 401 son parte de la derivación: declara cómo lo
resolviste — es la primera vez que un lab **rompe hacia atrás** los supuestos de sus
antecesores y el mecanismo sienta precedente); (8) `deriva` y `siembra` (audita L6→L7)
verdes en el runner; (9) CI verde, run citado; (10) `ESTADO.md` al día; estimación
honesta por TODO.

## §7 · La Prueba del PO (etapa 2 — cierra la SPEC)

Invitación literal, resultados esperados escritos, tres pasos: (1) el curl anónimo en
el starter — **emitir un folio sin ser nadie** (el crimen en tus manos); (2) fabricar
tu propio token con `echo ... | base64` y entrar como funcionario falso — sentir lo
fácil; (3) la solución: mismo curl anónimo → 401; login real de Carolina → token →
emisión 201; y `90 --dir solucion` → aprobado. Declara Java/Docker por paso. Recuerda
la fila acumulada del PO (Labs 00–06).

## §8 · Criterios de aceptación

- [ ] SPEC commiteada antes del material; rama + PR apilado y declarado.
- [ ] Lab 07 completo (las 5 piezas); evidencia §6 íntegra, actos medidos.
- [ ] Denegar por defecto verificado (una ruta nueva sin regla nace cerrada — cítalo).
- [ ] Ninguna clave real trackeada; la resolución D-012 declarada; prod exige la
      variable.
- [ ] El mecanismo de "romper hacia atrás" (§6.7) declarado — sienta precedente.
- [ ] TODOs ≤ ~15 min c/u (o recorte aplicado y declarado).
- [ ] Siembra L6→L7 auditada; Lab 07 siembra el 08 con el gancho de TESO.
- [ ] `ESTADO.md` y bitácora al día.
- [ ] **Prueba del PO reportada** — cierra la SPEC.
- [ ] Commits `SPEC-013:`; checks verdes citados.

## §9 · Reporte

Evidencia de §6, tiempos por TODO, decisiones declaradas (forma exacta del secreto dev,
mecanismo de compatibilidad hacia atrás, qué quedó público en la lista blanca y por
qué), URL del run, `git log --oneline`, discrepancias y hallazgos — sin tocarlos.
Cierra con la invitación del §7.
