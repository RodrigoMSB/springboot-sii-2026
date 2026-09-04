# INFORME-SPEC-048 · Comentarios rancios en el Lab 09 y BCrypt huérfano en el gateway

**Ejecuta:** mocito · **Fecha:** 3 de septiembre de 2026 · **Origen:** SPEC-048 del PO.

Dos frentes con urgencias distintas, así que fueron **dos ramas y dos PR**. El frente 1 tenía fecha
—el Lab 09 se dicta el viernes 4 a las 08.00— y no tenía por qué esperar a la validación larga del
lab de microservicios.

| frente | rama | PR | estado |
|---|---|---|---|
| 1 · comentarios del Lab 09 | `spec-048-frente-1-comentarios-lab-09` | **#65** | **mergeado el 3 de septiembre a las 22:04** |
| 2 · Argon2id en el gateway | `spec-048-frente-2-gateway-argon2` | **#67** | este informe |

---

# FRENTE 1 · El Lab 09 decía Argon2 en el código y BCrypt en los comentarios

**Mergeado a las 22:04 del 3 de septiembre**, con diez horas de margen sobre el plazo.

## 1.1 · El diff

Dos comentarios en `solucion/`, y **nada más**:

```diff
 // entities/Usuario.java
-    // Nunca la contraseña: siempre su hash BCrypt. 60 caracteres, siempre.
+    // Nunca la contraseña: siempre su hash Argon2id. El largo no lo pone la contraseña, lo ponen
+    // los parámetros del codificador: con los de `defaultsForSpringSecurity_v5_8()` son 97.

 // controllers/AuthController.java
-            // Aquí es donde BCrypt compara: la clave que llega contra el hash de la tabla.
+            // Aquí no se compara nada. `authenticate` se lo pasa al DaoAuthenticationProvider que
+            // hay detrás del AuthenticationManager, y ese es quien llama al PasswordEncoder
+            // —Argon2id— con la clave que llega y el hash de la tabla.
```

`git show --stat`: **2 archivos, 5 líneas añadidas, 2 quitadas.**

## 1.2 · El número, medido

La spec decía 97. **Es 97**, comprobado corriendo el propio codificador del lab contra su classpath
offline en lugar de contarlo a mano:

```
clave de  7 caracteres  ->  hash de 97
clave de  1 caracteres  ->  hash de 97
clave de 63 caracteres  ->  hash de 97

ejemplo: $argon2id$v=19$m=16384,t=2,p=1$+CWj3bcqPtGjUpXLeIz8vg$arXlJXUfz4AxDncl6ZkH4Iz7i0R0WRBAMj1ZTfIhKKg
```

Y **fijo para estos parámetros**: sube la memoria y sube el hash. Por eso el comentario nuevo no
suelta un número a secas, lo ata a la fábrica que lo produce. Es la D-048-2 aplicada a sí misma:
prometer «97» sin decir de qué depende sería el mismo defecto con otro número.

## 1.3 · Que fue sólo comentarios, medido

`instructor/` es copia de `solucion/` con más documentación, así que existe una comprobación exacta:
**quitar comentarios y líneas en blanco de los dos y comparar**. El despojador de la SPEC-041 §4.1
vivía en el scratchpad de aquella sesión y ya no está; **se reescribió** (respeta literales de
cadena, `char` y bloques `"""`), y se corrió antes y después:

```
IDENTICO · 13 archivos .java, 0 divergencias de código
```

Compila offline: `BUILD SUCCESS`. Y los tres verificadores que tocan este lab, en verde:
`pasos-copiables` (14 guiones), `instructor` (18 XML · 173 .java), `guion-vs-practica` (84 promesas).

## 1.4 · El `grep` que pedía la spec

```
PASOS.md:244,246,248,346                    ← las cuatro didácticas
solucion/…/config/SeguridadConfig.java:50   ← la comparativa a propósito
```

**Exactamente lo que la spec anticipaba**, ni una de más.

## 1.5 · Lo que se encontró de paso en `instructor/`

La revisión que la spec pedía en `instructor/` **encontró dos defectos**, los dos heredados de cómo
se editó la SPEC-036:

**a · `Usuario.java` tenía el bloque partido por la mitad.** Un `**` suelto, una frase cortada y una
conclusión que ya no era verdad:

```
//   ...no es fijo como el de BCrypt, que medía
//   exactamente 60
//   caracteres**, siempre, sea cual sea el largo de la contraseña. ... el hash sigue
//   midiendo 60.
```

Ese «el hash sigue midiendo 60» habla de BCrypt pero **se lee como si hablara del código de al
lado**, que es Argon2id. Reescrito entero, separando lo que depende de los parámetros de lo que no.

**b · La migración decía «unos 95 caracteres».** Estimado y falso. Ahora dice 97, medido.

**Nada de esto viaja en el PR** (D-031-2). Respaldado con `tools/instructor-respaldo.sh respaldar`,
y comprobado que llegó.

---

# FRENTE 2 · El gateway del lab de microservicios seguía en BCrypt

## 2.1 · El diff

```diff
-import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
+import org.springframework.security.crypto.argon2.Argon2PasswordEncoder;

+    // El mismo codificador del Lab 09 y del proyecto final: Argon2id, con los parámetros de la
+    // fábrica de Spring Security. El arco entero usa uno solo.
     @Bean
     PasswordEncoder codificadorDeClaves() {
-        return new BCryptPasswordEncoder();
+        return Argon2PasswordEncoder.defaultsForSpringSecurity_v5_8();
     }
```

En **`solucion/`, `practica/`, `instructor/` y `demos-instructor/`**, más el bloque de BouncyCastle
en los cuatro `pom.xml` del gateway — el mismo bloque y el mismo comentario del Lab 09, palabra por
palabra:

```xml
<bouncycastle.version>1.82</bouncycastle.version>
…
<!-- Argon2 lo implementa BouncyCastle: `Argon2PasswordEncoder` es una fachada de Spring
     Security sobre él, y sin esta dependencia el arranque muere con
     NoClassDefFoundError: org/bouncycastle/crypto/params/Argon2Parameters$Builder.
     Spring Security la declara `optional`, así que hay que pedirla a mano. -->
```

**No entra ningún jar nuevo a la maleta**: `repo-maven/org/bouncycastle/bcprov-jdk18on/1.82` ya
estaba desde la SPEC-036. Comprobado antes de tocar el pom, y confirmado por la compilación offline.

## 2.2 · El hallazgo de la siembra — que era el riesgo

La spec mandaba mirar esto antes de tocar nada, y **declararlo se haya encontrado algo o no**.

**No hay ningún hash precalculado en el lab de microservicios.** El gateway siembra así:

```java
return new InMemoryUserDetailsManager(
        User.withUsername("carolina").password(codificador.encode("dgt2026"))…
```

Las claves pasan por **este mismo codificador en el arranque**, así que cambiarlo no deja ningún
hash viejo detrás: al siguiente arranque los hashes se recalculan con Argon2id y validan. Se
comprobó además que no hubiera hashes escritos a mano en ninguna parte:

```
grep -rn '\$2[aby]\$' labs/lab-microservicios demos-instructor/microservicios-docker
(ninguno)
```

Las cuatro migraciones que sí existen son de contribuyentes, trámites y auditoría — **ninguna toca
usuarios**. No hubo que regenerar nada ni pasar la siembra por el codificador: ya lo estaba.

> **Y el contraste vale la pena decirlo**, porque el mismo riesgo sí se materializó a dos metros de
> aquí: el **proyecto final** siembra con los hashes escritos en la migración, y por eso la SPEC-049
> —hecha esta misma noche— tuvo que regenerarlos. Misma familia de cambio, dos resultados distintos
> según dónde nazca el hash.

## 2.3 · Validación

**Los cuatro servicios compilan offline** y producen sus jars:

```
gateway         BUILD SUCCESS   microservicios-gateway-solucion-0.1.0.jar
contribuyentes  BUILD SUCCESS   microservicios-contribuyentes-solucion-0.1.0.jar
tramites        BUILD SUCCESS   microservicios-tramites-solucion-0.1.0.jar
auditoria       BUILD SUCCESS   microservicios-auditoria-solucion-0.1.0.jar
```

**Los cuatro arrancan:**

```
Started GatewayApplication in 1.86 seconds
Started ContribuyentesApplication in 2.744 seconds
Started TramitesApplication in 2.786 seconds
Started AuditoriaApplication in 2.572 seconds
```

**El login contra el gateway, ya con Argon2id** — que es lo que había que probar:

```
POST /auth/login  carolina / dgt2026     ->  200
POST /auth/login  carolina / lo-que-sea  ->  401
GET  /tramites/1  sin token              ->  401
GET  /tramites/1  con token              ->  200
{"id":1,…,"nombreContribuyente":"Carolina Fuentes Aravena","estadoDelNombre":"OK"}
```

La última línea es el cruce completo: gateway → trámites → contribuyentes, con el nombre resuelto.

**La comparación byte a byte con `demos-instructor/`** y los cinco verificadores:

```
demo-docker       32 idénticos · 11 con diferencia declarada · 9 retirados
pasos-copiables   [OK] 14 guion(es)
guion-vs-practica [OK] 84 promesas
instructor        [OK] al día con solucion/
temario           VEREDICTO: las 5 verificaciones PASAN
generar-guias     [OK] 78 bloques · 0 líneas que la solución no tiene
```

## 2.4 · El `grep` final

`grep -rn BCrypt labs proyecto-final` devuelve **24 líneas, todas comparativas y ninguna código
vivo**: el `PASOS.md` del Lab 09 (4), los bloques `POR QUÉ` de `instructor/` (16), la migración
comentada (2) y el comentario de la fábrica en el proyecto final (3 copias).

La comprobación que de verdad cierra la D-048-1:

```
grep -rn "new BCryptPasswordEncoder\|crypto.bcrypt" labs proyecto-final demos-instructor
→ una sola línea, y es un comentario que cuenta lo que decía ANTES de esta SPEC
```

**Cero BCrypt como código vivo en todo el arco.**

---

## 3 · Los puntos de la spec que resultaron falsos al medir

### 3.1 · «32 idénticos y 4 diferencias declaradas»

Son **32 idénticos y 11 declaradas**, no 4. Y no lo cambió esta SPEC: se midió **antes** de tocar
nada, guardando el trabajo con `git stash`:

```
antes:    32 idénticos · 11 con diferencia declarada · 9 retirados
después:  32 idénticos · 11 con diferencia declarada · 9 retirados
```

El número del enunciado venía de un estado anterior del repositorio. **Lo que la spec quería
proteger —«ni una más»— se cumple**: la cifra es idéntica antes y después. Las 11 son las que el
propio verificador lista y explica (pom sin Zonky, yml con nombres de servicio, los tres
`Application.java` que ya no levantan su base embebida, y las de cada servicio).

### 3.2 · Los sitios estaban en otras líneas

La spec daba líneas 13 y 59 para `solucion/` y 12 y 51 para `practica/`. Las de `practica/` eran
12 y 51 ✓; las de `solucion/` eran **13 y 59** ✓. Sin novedad — se localizaron por contenido, no
por número, que es lo que aguanta que el archivo se mueva.

---

## 4 · Anotado, no tocado

**1 · Quedan 22 «Lab 14» con espacio en el lab de microservicios.** Es un hueco de la verificación
de la **SPEC-039**, cuyo `grep` buscaba `lab-14\|lab14` y por eso no los vio:

```xml
<description>Lab 14 — Microservicios · gateway — la solución</description>
```

Están en los `<description>` de los ocho poms del lab, los cuatro de la demo, el `LEEME.md` de
`instructor/` y algún encabezado. **El lab ya no tiene número**, así que dicen algo que dejó de ser
verdad. No se tocaron porque son de otra SPEC; el arreglo es mecánico y está listo para cuando el PO
lo mande.

**2 · Un cambio ajeno en el árbol de trabajo.** `labs/lab-08-testing/solucion/…/ProductoService.java`
tiene dos comentarios re-partidos en dos líneas, de las 20:33 de hoy. **No es de esta SPEC y no se
commiteó**; se deja tal cual estaba, como se hizo en la SPEC-041 con el trabajo del PO.

**3 · El respaldo privado tiene tres carpetas huérfanas** de la SPEC-039: `labs/lab-12-tareas`,
`labs/lab-13-empaquetado` y `labs/lab-14-microservicios`, con los nombres viejos. El material vivo
se respaldó bien —244 archivos en 15 carpetas—, pero conviene limpiarlas. **No se borró nada**: es
el repositorio privado del PO y esta SPEC no lo autoriza.

**4 · Cuatro instancias del lab quedaban corriendo desde las 19:35** —de la validación de la
SPEC-039, en esta misma sesión— y bloqueaban los puertos 8210-8213. Se cerraron. Al terminar este
frente **no queda ningún puerto ocupado**.

---

## 5 · Las decisiones, aplicadas

**D-048-1 · Argon2id es el único codificador del arco.** Cumplida y comprobable con un `grep`: cero
`new BCryptPasswordEncoder()` en `labs`, `proyecto-final` y `demos-instructor`. BCrypt sólo aparece
como término de comparación en texto.

**D-048-2 · Un comentario que contradice al código es un defecto.** Aplicada a los dos sitios que la
spec listaba y a los dos que aparecieron en `instructor/` al revisar — que es donde estaba el
comentario más caro de los cuatro, porque afirmaba un largo de hash que el código no produce.
