# INFORME-SPEC-034 a SPEC-037 · Correcciones de labs y demo Docker

**Ejecuta:** mocito · **Rama:** `spec-034-037-correcciones` · **Fecha:** 3 de septiembre de 2026
**Origen:** SPEC-034 a SPEC-037 del PO.

---

## 0 · Resumen

| spec | lab | qué cambió |
|---|---|---|
| **034** | 07 Concurrencia | el candado sobre la fila del folio 1 → **`pg_advisory_xact_lock(anio)`** |
| **035** | 08 Testing | de **9 tests a 4**, sobre una regla de negocio real; consola de Maven legible |
| **036** | 09 Seguridad | BCrypt → **Argon2id**; el token de 40 s vence **a los 40** |
| **037** | demo Docker | bloque nuevo: **el traceId cruzando cuatro contenedores** |

**Todo lo medible está medido, con una excepción declarada:** el `grep DEMO-1` de la SPEC-037 no
se pudo ejecutar porque la imagen base de Docker (233 MB) no llegó a bajar en este entorno. Lo que
sí se verificó de ese bloque —el reenvío de la cabecera en los tres clientes, que es de lo que
depende— está en §4.1, y el detalle de lo que falta, en §4.4. Las demás salidas reales van en §1
a §3.

**Cuatro puntos de la spec resultaron falsos o incompletos al medir**, y los cuatro están
corregidos y declarados en §5:

1. **El anexo del lab 07 «se mantiene» — no existía.** Se escribió.
2. **La demo 2 del lab 07 ensuciaba la consola con 30 líneas de WARN.** La spec pedía consola
   limpia *y* que se viera el mensaje `duplicate key`: se resolvieron las dos.
3. **`./mvnw test` con «4 verdes» son 7 ejecuciones.** Son 4 métodos en 4 archivos; el
   parametrizado cuenta sus 4 casos.
4. **Argon2 necesita BouncyCastle**, que no estaba en la maleta. Capturado (8,1 MB).

**Y un punto de la spec que resultó verdadero y conviene dejar dicho:** el aviso de la SPEC-034
sobre si Spring Data aceptaría la consulta nativa devolviendo `Object`. **La acepta.** No hizo
falta la alternativa con `EntityManager`.

**El tag.** La cabecera pide cerrar con `material-v0.6.1`. **Ese tag ya existe**, igual que pasó
con `material-v0.6.0` en la spec anterior: pertenece a la serie v0.4.0–v0.8.0, que es el archivo
del arco antiguo. Se cierra con **`material-v1.11.1`**, siguiendo el criterio que el PO aprobó al
cerrar la SPEC-029-033. Ver §7.

---

## 1 · SPEC-034 · Lab 07 · El turno con nombre

### 1.1 · Archivos modificados

```
labs/lab-07-concurrencia/PASOS.md                      paso 4 reescrito + anexo nuevo
labs/lab-07-concurrencia/README.md
labs/lab-07-concurrencia/guia-lab-07-concurrencia.pdf  regenerado, 10 páginas
docs/guias/fuente/guia-lab-07-concurrencia.md
{solucion,practica,instructor}/…/repositories/FolioRepository.java
{solucion,practica,instructor}/…/services/EmisorDeFolios.java
{solucion,practica,instructor}/…/demos/DemosConcurrencia.java
{solucion,practica,instructor}/…/Lab07Application.java
{solucion,practica,instructor}/src/main/resources/application.yml
{solucion,instructor}/…/db/migration/V2__folio_unico_por_anio.sql   (sólo comentarios)
instructor/LEEME.md
```

### 1.2 · Los seis puntos de la spec

1. **`bloquearLaApertura` borrado**, y la consulta nativa puesta **tal como la spec la escribió**:

   ```java
   @Query(value = "select pg_advisory_xact_lock(:anio)", nativeQuery = true)
   Object tomarElTurnoDelAnio(@Param("anio") long anio);
   ```

   **Spring Data la acepta así.** No hizo falta el `EntityManager`. Se conservan los dos comentarios
   que la spec pedía: por qué `Object` y no `void`, y —añadido— por qué `long` y no `int`
   (`pg_advisory_xact_lock` tiene una forma que toma un `bigint` y otra que toma dos `int`).

2. **`emitirConCandado` → `emitirConTurno`**, con `folios.tomarElTurnoDelAnio(anio)` en lugar del
   `orElseThrow`. Las dos líneas siguientes intactas. `DemosConcurrencia` y `Lab07Application`
   actualizados.

3. **`prepararElAnio` sigue sembrando el folio 1**, y su comentario ya no dice que sea el punto de
   encuentro: ahora dice que está para que los números del guion cuadren (1 + 20 = 21).

4. **El comentario del repositorio** está, con lo que la spec pide: un lock con nombre dentro de la
   transacción, PostgreSQL se lo da al primero y deja esperando a los demás, no hay fila que
   bloquear, el nombre es el número del año.

5. **`show-sql: false` se mantiene** en `solucion/` (en `practica/` sigue `true`, que es donde el
   paso 4 muestra el SQL). Y la línea `[TURNO]`, una vez por proceso con `AtomicBoolean`.

6. **La restricción única y su V2 no cambian.** El SQL está intacto; sí se ajustaron sus
   *comentarios*, que decían «el candado del paso 4» — ver §5.5.

### 1.3 · La validación

**Demo 3 — lo que la spec exige: 21 de 21, rechazados 0, `[TURNO]` una sola vez.**

```
=== 3 · CON TURNO · 20 a la vez, con un lock con nombre ===
  año 2026 reiniciado: solo el folio 2026-0001
  [TURNO] pg_advisory_xact_lock(2026) · el turno vive en la base, no en Java
  folios en la tabla : 21
  números distintos  : 21
  REPETIDOS          : ninguno
  rechazados por la base : 0
  emitidos: [2026-0001, 2026-0002, ..., 2026-0021]
```

**Demo 2 — rechazados mayor que cero, con su mensaje:**

```
=== 2 · EL CRIMEN · 20 emisiones a la vez, sin protección ===
  folios en la tabla : 11
  números distintos  : 11
  REPETIDOS          : ninguno
  rechazados por la base : 10
  y los rechazó diciendo : ERROR: duplicate key value violates unique constraint "folio_anio_numero_unico"
```

**El SQL, medido en `practica/` con `show-sql: true`:**

```
Hibernate:
    select
        pg_advisory_xact_lock(?)
```

**Cero apariciones de `for update` o `for no key update`** en toda la corrida (`grep -c` = 0), que
es exactamente lo que la spec pedía comprobar.

---

## 2 · SPEC-035 · Lab 08 · Cuatro tests que valgan la pena

### 2.1 · Archivos modificados

```
labs/lab-08-testing/PASOS.md                        reescrito: paso 0 + 3 pasos
labs/lab-08-testing/README.md
labs/lab-08-testing/guia-lab-08-testing.pdf         regenerado, 11 páginas
docs/guias/fuente/guia-lab-08-testing.md            de 5 pasos a 4
{solucion,practica,instructor}/…/services/ProductoService.java
{solucion,practica,instructor}/…/controllers/ProductoController.java
{solucion,practica,instructor}/src/test/…           los cuatro archivos
{solucion,practica,instructor}/pom.xml              Surefire
{solucion,practica}/.mvn/jvm.config                 nuevo
examen-huecos/base/pom.xml  ·  examen-huecos/base/.mvn/jvm.config
instructor/LEEME.md
```

### 2.2 · El método nuevo

`valorDelCatalogo()` salió; entró:

```java
    /** Descuento por volumen. 3 o más unidades, 10 %. 10 o más, 20 %. */
    public int totalConDescuento(Long id, int cantidad)
```

con `IllegalArgumentException` para `cantidad <= 0` y `Math.round` al peso, sobre el total.
`ProductoController` cambió `/productos/valor-total` por `/productos/{id}/total?cantidad=`.

**Por qué el método viejo no valía un test**, y está escrito en el `instructor/`: era un
`stream().sum()`. Su test comprobaba que sumar funciona. El descuento sí tiene bordes, orden de
tramos, redondeo y entrada inválida.

### 2.3 · Los cuatro tests

| archivo | qué prueba |
|---|---|
| `ProductoServiceTest` | `@ParameterizedTest` con `@CsvSource`: 1, 3, 10 y 0 unidades. Repositorio **real** |
| `ProductoServiceConDobleTest` | el mismo método con `@Mock`, **un solo `when`, sin `verify`** |
| `ProductoControllerTest` | `GET /productos/99` → 404 con cuerpo |
| `ContextoDeSpringTest` | sin cambios; en `practica/` llega **resuelto** |

Los seis tests que salieron están nombrados con su motivo en la cabecera de cada archivo del
`instructor/`.

### 2.4 · La validación

**`./mvnw test` en verde:**

```
[INFO] Tests run: 4, Failures: 0, ... -- in cl.dgt.testing.ProductoServiceTest
[INFO] Tests run: 1, Failures: 0, ... -- in cl.dgt.testing.ContextoDeSpringTest
[INFO] Tests run: 1, Failures: 0, ... -- in cl.dgt.testing.ProductoServiceConDobleTest
[INFO] Tests run: 1, Failures: 0, ... -- in cl.dgt.testing.ProductoControllerTest
[INFO] Tests run: 7, Failures: 0, Errors: 0, Skipped: 0
[INFO] BUILD SUCCESS
```

**Son 4 métodos en 4 archivos; Surefire cuenta 7 ejecuciones** porque el parametrizado corre sus
cuatro casos. La spec decía «4 verdes» — ver §5.3.

**Con el IVA a 0.10, dos archivos rojos**, tal como la spec anticipaba:

```
[ERROR] Tests run: 4, Failures: 3, ... <<< FAILURE! -- in cl.dgt.testing.ProductoServiceTest
[ERROR] Tests run: 1, Failures: 1, ... <<< FAILURE! -- in cl.dgt.testing.ProductoServiceConDobleTest
```

**Y la traza recortada, que es lo que se proyecta:**

```
$ cat target/surefire-reports/cl.dgt.testing.ProductoServiceTest.txt

cl.dgt.testing.ProductoServiceTest.elTotalAplicaElDescuentoPorVolumen(int, int)[1] <<< FAILURE!
org.opentest4j.AssertionFailedError: expected: <5938> but was: <5489>
	at cl.dgt.testing.ProductoServiceTest.elTotalAplicaElDescuentoPorVolumen(ProductoServiceTest.java:30)
```

**Dos líneas.** Sin `trimStackTrace`, ese mismo fallo imprime la pila entera de JUnit y Spring.

### 2.5 · La consola de Maven · **el shim SÍ lee `.mvn/jvm.config`**

La spec pedía verificarlo antes de decidir dónde va la opción. **Verificado y medido:**

```
                                       WARNINGs en `./mvnw test`
  antes                                        8
  con .mvn/jvm.config                          4   ← los de Unsafe desaparecen
  + argLine en Surefire                        0
```

El shim `./mvnw` ejecuta `tools/maven/bin/mvn`, que hace
`MAVEN_OPTS="$(concat_lines $MAVEN_PROJECTBASEDIR/.mvn/jvm.config) $MAVEN_OPTS"`. **La opción va en
`.mvn/jvm.config`, por proyecto**, y no hizo falta tocar el shim.

Los cuatro WARNING que quedaban **no eran de Unsafe**: eran del agente dinámico de ByteBuddy que
carga Mockito, y son de la JVM *forked* de Surefire, no de la de Maven. Se apagan con
`-XX:+EnableDynamicAgentLoading` en el `argLine`. La spec no los pedía; se quitaron por la regla
general de consola limpia.

**Aplicado también a `examen-huecos/base`**, donde los doce tests rojos a propósito siguen rojos y
la consola queda limpia:

```
WARNINGs: 0
[ERROR] Tests run: 12, Failures: 2, Errors: 10, Skipped: 0
```

---

## 3 · SPEC-036 · Lab 09 · Argon2 y el token que vence cuando se dice

### 3.1 · Archivos modificados

```
labs/lab-09-seguridad/PASOS.md  ·  README.md  ·  guia-lab-09-seguridad.pdf (11 páginas)
docs/guias/fuente/guia-lab-09-seguridad.md
{solucion,instructor}/…/config/SeguridadConfig.java
{solucion,practica,instructor}/pom.xml                    bcprov-jdk18on
{solucion,practica,instructor}/…/db/migration/V1__usuario.sql   60 -> 120
instructor/…/entities/Usuario.java · controllers/AuthController.java
instructor/…/soporte/SembradorDeUsuarios.java · LEEME.md
repo-maven/org/bouncycastle/                              NUEVO, 8,1 MB
```

### 3.2 · Los cinco puntos

1. **`Argon2PasswordEncoder.defaultsForSpringSecurity_v5_8()`.** Se verificó la API real con
   `javap` sobre el jar de la maleta: los únicos métodos de fábrica son
   `defaultsForSpringSecurity_v5_2()` y `defaultsForSpringSecurity_v5_8()`. **El v5_8 es el más
   reciente disponible en Spring Security 7.1.0**, tal como la spec suponía.

2. **BouncyCastle NO estaba en la maleta**, y hace falta — ver §5.4. Capturado con el procedimiento
   de la SPEC-023 y declarado en los tres poms.

3. **Los hashes empiezan por `$argon2id$`** y siguen siendo dos distintos para la misma clave.

4. **`clave_hash` de 60 a 120** en las tres V1. `ddl-auto: validate` sigue pasando (la aplicación
   arranca y siembra sin error).

5. **Tolerancia de reloj a cero**, con el código que la spec escribió. Comentado en `SeguridadConfig`
   y en el `instructor/` que **en producción se deja puesta**, porque los relojes de dos servidores
   nunca coinciden. **Los cuatro sitios que decían «100 segundos» vuelven a decir 40.**

### 3.3 · La validación

**Los dos hashes, con la misma clave:**

```
[semilla] usuarios ana/secreta (ADMIN) y luis/secreta (USUARIO)
[semilla] ana   ADMIN    $argon2id$v=19$m=16384,t=2,p=1$6pRDZ7pRwU3jaaV9oNK7Ag$EtTVmBMJb4eWLzEg3NvxJqDad+X7GbuBHBpFJiTBD/A
[semilla] luis  USUARIO  $argon2id$v=19$m=16384,t=2,p=1$DiTWa388g9rj7QMybwv78A$irfjVwPs8vDvX75eHbmMeeWH6WbtpD2SSyInaxrsYeU
```

**Login de los dos: 200.** Y la matriz completa, idéntica a la de la SPEC-029:

```
1 GET /productos                sin token   401
2 GET /productos                ana         200
3 GET /productos                luis        200
4 GET /productos/administracion ana         200
5 GET /productos/administracion luis        403
  token manipulado                          401
  clave equivocada                          401
```

**El token de 40 segundos vence a los 40:**

```
  t+0s  -> 200
  t+8s  -> 200
  t+16s -> 200
  t+24s -> 200
  t+32s -> 200
  t+40s -> 401      ← con la tolerancia de reloj a cero
```

Comparado con lo que midió la SPEC-029 sin ese ajuste, donde el 401 llegaba a los **101 s**.

---

## 4 · SPEC-037 · Demo Docker · el traceId entre cuatro contenedores

### 4.1 · Lo que se verificó ANTES de escribir el bloque

La spec pedía comprobar que los clientes reenvían la cabecera, y advertía que si alguno no lo
hacía era un defecto del lab 14 que había que corregir en dos sitios.

**Los tres lo hacen, y no hubo defecto que corregir:**

```
tramites/…/clientes/ClienteContribuyentes.java:86
    .header(FiltroDeCorrelacion.CABECERA, MDC.get(FiltroDeCorrelacion.CLAVE))

tramites/…/clientes/ClienteAuditoria.java:38-45
    String traceId = MDC.get(FiltroDeCorrelacion.CLAVE);   // copiado ANTES de saltar de hilo
    ...  .header(FiltroDeCorrelacion.CABECERA, traceId)

gateway/…/enrutado/Enrutador.java:96
    .header(FiltroDeCorrelacion.CABECERA, MDC.get(FiltroDeCorrelacion.CLAVE));
```

Y los tres archivos son **idénticos byte a byte** entre `labs/lab-14-microservicios/solucion/` y
`demos-instructor/lab-14-docker/sistema/`; el job `demo-docker` del CI sigue verde.

`ClienteAuditoria` además copia el id **antes** de saltar al hilo asíncrono, que es justo la trampa
del `@Async` del Lab 12. Se aprovecha en el bloque.

### 4.2 · Los cuatro puntos

1. **Bloque 6 nuevo**, «Seguir una petición por cuatro contenedores», antes del cierre, en el
   README de la demo y en la guía del instructor.
2. **Verificación previa hecha** (§4.1). Sin cambios en el lab 14.
3. **El cierre pasó a ser el bloque 7.**
4. **La nota de arquitectura** en «Antes de que entre nadie»: las imágenes se construyen para la
   arquitectura de la máquina donde corre `docker compose build`; si se proyecta desde otra, hay
   que reconstruir allí.

### 4.3 · La guía

`guia-demo-lab-14-docker.pdf` regenerada desde su fuente: **11 páginas**, con el bloque 6 y una
entrada nueva en «Lo que aprendiste».

### 4.4 · La validación que NO se pudo ejecutar

La spec pide: *«El grep DEMO-1 devuelve al menos una línea de cada uno de los cuatro servicios,
pegada en el informe.»*

**No se pudo ejecutar, y el bloque va al material sin esa comprobación.** Qué se intentó y dónde se
paró:

```
docker info                        → Docker disponible
./construir.sh                     → OK, los cuatro jar construidos
docker compose up -d               → se queda esperando la imagen base
docker pull eclipse-temurin:25-jre-alpine
                                   → ~20 minutos, log vacío, sin progreso.
                                     Abortado.
docker compose down -v             → sin residuos
```

La imagen base (233 MB) no llegó a bajar en este entorno. Sin ella los cuatro contenedores de
aplicación no arrancan, así que el `grep` no tiene sobre qué correr.

**Lo que SÍ está verificado del bloque, y no es poco:**

- **Los tres clientes reenvían la cabecera** — comprobado leyendo el código, con línea y archivo
  (§4.1). Es la condición que la spec pedía verificar *antes* de escribir el bloque, y es de donde
  depende que el `grep` encuentre las cuatro líneas.
- **`ClienteAuditoria` copia el `traceId` del MDC antes de saltar de hilo**, que es el detalle fino
  que el bloque explica.
- **Los tres archivos son idénticos** entre `labs/lab-14-microservicios/solucion/` y
  `demos-instructor/lab-14-docker/sistema/`, y el job `demo-docker` del CI lo confirma.
- **Los cuatro jar de la demo se construyen** con `./construir.sh`, offline.

**Lo que queda por comprobar en una máquina con la imagen ya bajada** —el propio README dice que
hay que bajarla antes de la clase— es la **salida literal** del `grep`: el bloque la muestra con el
formato de `docker compose logs` (`servicio-1 | ... [DEMO-1] ...`) y los nombres de logger reales de
cada servicio. Es lo primero que hay que correr al preparar la demostración; si algún nombre de
logger no cuadra exactamente, se ajusta el bloque de salida esperada del README y de la guía.

---

## 5 · Los puntos de la spec que resultaron falsos, y qué se hizo

### 5.1 · El anexo del lab 07 «se mantiene» — no existía

La SPEC-034 dice: *«El anexo para ver repetidos en vivo, quitar V2 y borrar .datos-pg, se mantiene
y marcado como destructivo.»*

**No había tal anexo** en `PASOS.md` ni en el README. Se escribió, al final del guion, marcado
como destructivo y opcional, con los tres comandos (`rm` de la V2, `rm -rf .datos-pg`, arrancar) y
la salida que produce. Se añadió también cómo volver atrás.

### 5.2 · La demo 2 ensuciaba la consola con treinta líneas de WARN

La spec pide dos cosas que chocaban: **consola limpia** (regla general) y que la demo 2 muestre
*«el mensaje `duplicate key value violates unique constraint`»*.

Medido: Hibernate avisa de **cada** choque **dos veces por hilo**. Con 13 rechazos, treinta líneas
de WARN tapando el informe, que es lo único que hay que leer.

**Se resolvieron las dos:** el logger `org.hibernate.orm.jdbc.error` se silencia en el yml, y
`DemosConcurrencia` recupera el mensaje **de la excepción** y lo imprime **una vez**:

```
  rechazados por la base : 10
  y los rechazó diciendo : ERROR: duplicate key value violates unique constraint "folio_anio_numero_unico"
```

### 5.3 · «`./mvnw test` con 4 verdes» son 7 ejecuciones

La SPEC-035 pide cuatro tests en cuatro archivos **y** un `@ParameterizedTest` de cuatro casos. Las
dos cosas a la vez dan **4 métodos** y **7 ejecuciones**, porque Surefire cuenta cada caso del
parametrizado por separado.

No es un defecto —es lo que hace útil el parametrizado, que el rojo del IVA afecte a 3 de 4 casos y
no a un test entero— pero el número de la validación no es el que la spec anticipaba. Los
documentos dicen ahora «cuatro métodos, siete ejecuciones» y explican por qué.

### 5.4 · Argon2 necesita BouncyCastle, y no estaba en la maleta

La spec pedía verificarlo. **No estaba**, y sin él la aplicación **compila y revienta al arrancar**:

```
java.lang.NoClassDefFoundError: org/bouncycastle/crypto/params/Argon2Parameters$Builder
Caused by: java.lang.ClassNotFoundException: org.bouncycastle.crypto.params.Argon2Parameters$Builder
```

Spring Security declara BouncyCastle como `optional`, así que hay que pedirlo a mano. Declarado en
los tres poms (`org.bouncycastle:bcprov-jdk18on:1.82`) y capturado en `repo-maven/` — **8,1 MB**.

**Detalle del procedimiento, para la próxima vez:** `DGT_ONLINE=1 ./mvnw` **no** captura en la
maleta, porque el shim en modo online omite el `-Dmaven.repo.local`. Hay que pasarlo a mano:

```bash
DGT_ONLINE=1 ./mvnw compile -Dmaven.repo.local=<raíz>/repo-maven
```

Después, los 41 proyectos compilan offline y `git status repo-maven` sólo muestra BouncyCastle.

### 5.5 · Los comentarios de la V2 decían «el candado del paso 4»

La SPEC-034 dice que la V2 «no cambia». El **SQL** no cambió. Pero sus comentarios —y los del
`instructor/`— hablaban del «candado del paso 4», que ya no existe. Se ajustó **sólo el
vocabulario** de los comentarios: es exactamente el tipo de mentira que esta spec viene a corregir
(«y la guía dice la verdad»). Por lo mismo, el título de la demo 2 pasó de «sin candado» a «sin
protección», y eso sí cambia una línea de salida que aparece en los tres documentos.

---

## 6 · Verificación final

**Los cinco verificadores, en verde:**

```
verificar-temario.py            VEREDICTO: las 5 verificaciones PASAN
verificar-pasos-copiables.py    [OK] 16 guion(es) verificado(s)
verificar-guion-vs-practica.py  [OK] Todo lo que los guiones prometen es verdad
verificar-instructor.py         [OK] 21 XML · 206 .java · 18/18 carpetas
verificar-demo-docker.py        [OK] la demostración dice el mismo código que el laboratorio
```

**Regla de siembra (P-18):** los dieciséis `PASOS.md` la cumplen.

**Compilación offline, como el job `labs`:**

```
[INFO] 41 proyectos · 0 fallos
git status --porcelain repo-maven  →  sólo org/bouncycastle/ (capturado a propósito)
```

**PDFs regenerados desde su fuente:**

```
guia-lab-07-concurrencia.pdf     10 páginas   83 KB
guia-lab-08-testing.pdf          11 páginas   92 KB
guia-lab-09-seguridad.pdf        11 páginas   96 KB
guia-demo-lab-14-docker.pdf      11 páginas  106 KB
```

**Ningún lab fuera del 07, 08 y 09 se tocó**, salvo `examen-huecos/base` —que la SPEC-035 pide
explícitamente— y la demo del 14.

---

## 7 · El tag

La cabecera pide cerrar con **`material-v0.6.1`**. Ese tag **ya existe**, y es el mismo caso que
`material-v0.6.0` en la spec anterior: pertenece a la serie **v0.4.0–v0.8.0**, el archivo del arco
antiguo que `ESTADO.md` y `docs/CONTEXTO-MOCITO.md` citan para recuperar material retirado.
Moverlo exigiría un push forzado y dejaría esas referencias apuntando a otra cosa.

Se cierra con **`material-v1.11.1`** — correcciones sobre `material-v1.11.0`, que fue el cierre de
la SPEC-029-033. Es el criterio que el PO aprobó entonces y que quedó escrito en el §9 de aquel
informe:

> La serie v0.x está congelada y es el archivo del arco antiguo; los cierres nuevos van en v1.x.

**Sigue en pie la recomendación de aquel informe:** contrastar el número de tag de la cabecera con
`git tag -l 'material-v*' --sort=-v:refname | head -1` antes de empezar.
