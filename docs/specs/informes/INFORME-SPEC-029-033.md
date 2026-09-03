# INFORME-SPEC-029 a SPEC-033 · Simplificación de los labs 09 a 13

**Ejecuta:** mocito · **Rama:** `spec-029-033-simplificacion` · **Fecha:** 3 de septiembre de 2026
**Origen:** SPEC-029 a SPEC-033 del PO, con la **enmienda urgente a la SPEC-033** recibida durante
la ejecución (`tools/jib-base/` no se borra).

---

## 0 · Resumen

**Los cinco labs bajaron de paso y todos los números están medidos en esta máquina.**

| lab | antes | ahora | qué se fue |
|---|---|---|---|
| 09 Seguridad | 6 pasos | **5** | `/quien-soy`, el bean `conversorDeRoles` |
| 10 Resiliencia | 5 pasos | **3** + paso 0 | el reintento entero (`resilience4j-retry`) |
| 11 Observabilidad | 5 pasos | **3** + paso 0 | la métrica, `MotorDePostgres`, `SimuladorController` |
| 12 Tareas | 5 pasos | **3** + paso 0 | el cron (`Recordatorio`), la demo de dos instancias (`Instancia`) |
| 13 Empaquetado | 5 pasos | **4** | Jib, el plugin, `jib.version`, los dos `.mvn/maven.config` |

**41 proyectos Maven compilan offline** con el `./mvnw` del curso, y `git status repo-maven` sale
limpio: nadie salió a la red. Los cuatro verificadores del CI pasan en verde
(`pasos-copiables`, `guion-vs-practica`, `temario`, `instructor`), y los cinco PDF se regeneraron
desde su fuente.

**Cada guion se ejecutó de punta a punta sobre `practica/`**, pegando literalmente sus bloques, no
sólo sobre `solucion/`. Las salidas reales están en §1 a §5.

**Tres hallazgos que cambiaron el material**, y los tres nacieron de medir:

1. **El token de 40 segundos caduca a los ~100.** `NimbusJwtDecoder` aplica de fábrica **60 s de
   tolerancia de reloj**. La demo del token vencido decía «espera un minuto y da 401» y habría
   dado 200 delante de la sala. Corregido y explicado (§1.4).
2. **Sin reintento, el circuito del lab 10 necesita 5 peticiones para abrir, no 2.** Con reintento,
   una petición del usuario producía tres llamadas y la ventana se llenaba enseguida. El `for` del
   guion pasó de 3 a 7 iteraciones (§2.3).
3. **El escenario del circuito cambió de "caída" a "lenta"**, y el laboratorio mejoró: los tres
   números del arco quedan sobre **el mismo escenario** — 30,01 s → 2,04 s → 0,003 s (§2.3).

**La enmienda se verificó antes de aplicarla** y era correcta: cinco proyectos apuntan a
`tools/jib-base/` (120 MB) y dos poms del proyecto final llevan el plugin. **No se borró nada**
(§5.1).

**Una desviación**, declarada en §6: se tocó `ESTADO.md`, que no está en el alcance de la spec,
porque afirmaba que `tools/jib-base/` existe «para que el Lab 13 construya su imagen OCI» y eso
dejó de ser verdad.

---

## 1 · SPEC-029 · Lab 09 · Seguridad · de 6 pasos a 5

### 1.1 · Archivos borrados

Ninguno. Los cambios son todos dentro de archivos que siguen existiendo.

### 1.2 · Archivos modificados

```
labs/lab-09-seguridad/PASOS.md                                       reescrito, 5 pasos
labs/lab-09-seguridad/README.md
labs/lab-09-seguridad/guia-lab-09-seguridad.pdf                      regenerado, 11 páginas
docs/guias/fuente/guia-lab-09-seguridad.md
{solucion,practica,instructor}/src/.../controllers/ProductoController.java
{solucion,instructor}/src/.../config/SeguridadConfig.java
{solucion,instructor}/src/.../services/ServicioDeTokens.java
{solucion,instructor}/src/.../soporte/SembradorDeUsuarios.java
{solucion,practica,instructor}/src/main/resources/application.yml
```

Los ocho puntos de la spec, uno a uno:

1. **`GET /productos/quien-soy` borrado** de las tres carpetas, con sus dos imports.
2. **`conversorDeRoles()` borrado** de `SeguridadConfig`, con sus dos imports. La cadena queda
   `oauth2ResourceServer(oauth -> oauth.jwt(Customizer.withDefaults()))`.
3. **El claim `scope` sigue llevando `ROLE_ADMIN`/`ROLE_USUARIO`**, sin tocar.
4. **`.hasRole("ADMIN")` → `.hasAuthority("SCOPE_ROLE_ADMIN")`**, una sola línea de la cadena.
5. **`lab08.jwt.secreto` → `lab09.jwt.secreto`**, variable `LAB09_JWT_SECRETO`, issuer `lab09`.
6. **`SembradorDeUsuarios` imprime siempre.** La guarda `if (count() == 0)` envuelve sólo la
   siembra, no el método.
7. **`spring.jpa.open-in-view: false`** añadido al yml de las tres carpetas.
8. **`lab09.jwt.vigencia-segundos`**, con default `1800` en el `@Value`.

### 1.3 · La matriz, ejecutada sobre `practica/`

El guion se siguió literalmente: se añadió la dependencia del paso 1, se pegaron los archivos de
los pasos 2 a 5 extraídos del propio `PASOS.md`, y se compiló y corrió.

**Paso 1 — sólo `spring-boot-starter-security`:**

```
Using generated security password: 122fcc08-3994-4098-80e1-969f65c46123
  sin credencial: 401
  con user/clave: 200
```

**Paso 3 — la consola del sembrador:**

```
[semilla] usuarios ana/secreta (ADMIN) y luis/secreta (USUARIO)
[semilla] ana   ADMIN    $2a$10$mErQ54HlODGvK5NURB7PDuoTojeblY9ACdTptZlRWdo60.wg7XXxu
[semilla] luis  USUARIO  $2a$10$pszoalVNPxPoqFfAOV6UweZjohFWvSqcPLXMJpyDLDNZbAh2hZPIa
```

**Y en la SEGUNDA corrida, con la tabla ya poblada** — que es el punto 6 de la spec y hoy no salía:

```
[semilla] usuarios ana/secreta (ADMIN) y luis/secreta (USUARIO)
[semilla] ana   ADMIN    $2a$10$mErQ54HlODGvK5NURB7PDuoTojeblY9ACdTptZlRWdo60.wg7XXxu
[semilla] luis  USUARIO  $2a$10$pszoalVNPxPoqFfAOV6UweZjohFWvSqcPLXMJpyDLDNZbAh2hZPIa
```

**Paso 5 — los payloads decodificados:**

```
{"iss":"lab09","sub":"ana","exp":1788471821,"iat":1788470021,"scope":"ROLE_ADMIN"}
{"iss":"lab09","sub":"luis","exp":1788471821,"iat":1788470021,"scope":"ROLE_USUARIO"}
```

**La matriz de cinco curl que pide la spec:**

```
1 GET /productos                sin token   401
2 GET /productos                ana         200
3 GET /productos                luis        200
4 GET /productos/administracion ana         200
5 GET /productos/administracion luis        403

  token manipulado                          401
  clave equivocada                          401
```

**401, 200, 200, 200, 403.** Exactamente lo que la spec exige. `practica/` se restauró después.

### 1.4 · Hallazgo · la tolerancia de reloj de 60 segundos

La spec pide que el instructor pueda demostrar el token vencido poniendo
`lab09.jwt.vigencia-segundos: 40`. Se hizo, y **no funcionó como estaba escrito**:

```
t+0s   -> 200
t+10s  -> 200
t+50s  -> 200      ← el token dice exp a los 40 s, y sigue pasando
t+90s  -> 200
t+101s -> 401      ← aquí sí
```

**Causa:** `NimbusJwtDecoder` monta de fábrica un `JwtTimestampValidator` con **60 segundos de
tolerancia de reloj**, porque el reloj del emisor y el del verificador no tienen por qué estar
sincronizados al segundo. **40 + 60 = 100.**

Si esto no estuviera escrito, el instructor pondría 40 segundos, esperaría un minuto delante de la
sala, obtendría un 200 y tendría que improvisar. Está corregido en los cuatro sitios donde la demo
se explica: `PASOS.md`, la guía del alumno, el `instructor/ServicioDeTokens.java` y el
`instructor/application.yml`, con la instrucción de **decirlo antes de esperar**, no después.

---

## 2 · SPEC-030 · Lab 10 · Resiliencia · de 5 pasos a 3

### 2.1 · Archivos borrados

```
repo-maven/io/github/resilience4j/resilience4j-retry/    (5 archivos, 68 KB)
```

**Verificado con grep antes de borrar:** los únicos dos consumidores eran los poms de este lab.
`demos-instructor/lab-14-docker` y `labs/lab-14-microservicios` usan `resilience4j-circuitbreaker`,
que se queda.

### 2.2 · Archivos modificados

```
labs/lab-10-resiliencia/PASOS.md                             reescrito, paso 0 + 4 pasos
labs/lab-10-resiliencia/README.md
labs/lab-10-resiliencia/guia-lab-10-resiliencia.pdf          regenerado, 11 páginas
docs/guias/fuente/guia-lab-10-resiliencia.md
{solucion,practica,instructor}/pom.xml
{solucion,instructor}/src/.../services/PagoService.java
{solucion,practica,instructor}/src/.../tesoreria/ClienteTesoreria.java
{solucion,practica,instructor}/src/.../tesoreria/TesoreriaSimulada.java
{solucion,practica,instructor}/src/main/resources/application.yml
practica/src/.../services/PagoService.java                   huecos renumerados
practica/src/.../controllers/PagoController.java             hueco renumerado
```

Los cuatro puntos de la spec:

1. **`resilience4j-retry` fuera** de los tres poms y del `repo-maven`.
2. **`PagoService`**: fuera el campo `reintento`, su `RetryConfig`, el `ignoreExceptions` y el
   publicador `onRetry`. La llamada queda
   `CircuitBreaker.decorateSupplier(circuito, () -> cliente.consultarPago(id))`.
3. **`lab09.tesoreria.puerto` → `lab10.tesoreria.puerto`** en el yml, `ClienteTesoreria` y
   `TesoreriaSimulada` de las tres carpetas.
4. **Todo lo demás se queda**: timeouts, ventana de 5, degradación, `/estado-circuito`, simulador.

### 2.3 · La medición, y los dos cambios que provocó

Se aplicó el guion paso a paso sobre `practica/`, midiendo en cada uno.

**Paso 0 y paso 1:**

```
{"estado":"PAGADO","monto":45000}  (0.134300s)
  el usuario esperó: 30.012507s
```

**Paso 2, con el timeout de 2 s:**

```
{"timestamp":"...","status":500,"error":"Internal Server Error","path":"/pagos/77"}
  el usuario espera: 2.041250s  (HTTP 500)
```

**Paso 3 — y aquí está el primer hallazgo.** Con Tesorería **caída** (500 inmediato), el
contraste es pobre porque el fallo ya era rápido:

```
petición 1: 0.142121s  circuito=CLOSED  httpReales=1
petición 5: 0.007894s  circuito=OPEN    httpReales=5
petición 6: 0.003423s  circuito=OPEN    httpReales=5
```

De 7 ms a 3 ms no impresiona a nadie. Se probó el escenario **lento**, el mismo de los pasos 1 y 2:

```
petición 1: 2.038955s  circuito=CLOSED  httpReales=1
petición 2: 2.015943s  circuito=CLOSED  httpReales=2
petición 3: 2.013656s  circuito=CLOSED  httpReales=3
petición 4: 2.008409s  circuito=CLOSED  httpReales=4
petición 5: 2.015010s  circuito=OPEN    httpReales=5
petición 6: 0.003290s  circuito=OPEN    httpReales=5
petición 7: 0.003000s  circuito=OPEN    httpReales=5

>>> CIRCUITO CLOSED -> OPEN
```

**Se adoptó el escenario lento**, y el laboratorio mejoró: los tres números del arco quedan sobre
**el mismo escenario**, que antes no pasaba.

```
30,01 s   →   2,04 s   →   0,003 s
sin nada      timeout       circuito abierto
```

**Segundo hallazgo, y es una trampa para el instructor:** el guion viejo pedía **tres** peticiones.
Con reintento, una petición del usuario producía tres llamadas y la ventana de 5 se llenaba en dos.
Sin reintento hacen falta **cinco**. Si en clase se hacen tres, el circuito sigue `CLOSED` y la demo
no sale. El `for` del guion, del README y de la guía pasó a `1 2 3 4 5 6 7`, con la razón dicha.

**Paso 4 — la degradación:**

```
{"estado":"DESCONOCIDO","id":"77","aviso":"Tesorería no responde; el trámite sigue su curso"}
  (HTTP 200, 0.137039s)

WARN  Tesorería no respondió (InternalServerError). Se degrada.
WARN  Tesorería no respondió (CallNotPermittedException). Se degrada.
```

Las dos excepciones distintas se señalan en el guion: no es la misma avería.

**La vuelta a CLOSED:**

```
>>> CIRCUITO OPEN -> HALF_OPEN
>>> CIRCUITO HALF_OPEN -> CLOSED

{"llamadasHTTPReales":2,"estado":"CLOSED","tasaDeFallo":-1.0,...}
```

### 2.4 · El párrafo «lo que no vimos»

Está en `PASOS.md`, el README y la guía, con lo que la spec pide: **el reintento con su regla**
(sirve para fallos pasajeros, empeora las caídas de verdad), la **tormenta de reintentos** con su
nombre, y **bulkheads, rate limiting y backoff con jitter**. En la guía se añadió además un
ejercicio de «para profundizar» que hace añadir el `Retry` y volver a medir.

---

## 3 · SPEC-031 · Lab 11 · Observabilidad · de 5 pasos a 3

### 3.1 · Archivos borrados

```
{solucion,practica,instructor}/src/.../infra/MotorDePostgres.java
{solucion,practica,instructor}/src/.../controllers/SimuladorController.java
```

`infra/PuertoLibre.java` y `infra/CandadoLibre.java` **NO se borraron**, y esto es una desviación
declarada: ver §6.1.

### 3.2 · Archivos modificados

```
labs/lab-11-observabilidad/PASOS.md                          reescrito, paso 0 + 3 pasos
labs/lab-11-observabilidad/README.md
labs/lab-11-observabilidad/guia-lab-11-observabilidad.pdf     regenerado, 12 páginas
docs/guias/fuente/guia-lab-11-observabilidad.md
{solucion,practica,instructor}/src/.../Lab11Application.java  patrón de los labs 05-07
{solucion,practica,instructor}/src/.../controllers/TramiteController.java
{solucion,practica,instructor}/src/main/resources/application.yml
instructor/LEEME.md
```

Los seis puntos de la spec:

1. **Borrados** `MotorDePostgres` y `SimuladorController` (ver §6.1 sobre los otros dos).
2. **`Lab11Application` vuelve al patrón de los labs 05-07**: `@Bean EmbeddedPostgres` con
   `PuertoLibre`/`CandadoLibre` y `@Bean DataSource` derivado. Se copió del lab 06, no se inventó.
3. **`Counter`, `MeterRegistry` y `emitidos.increment()` fuera** del `TramiteController`;
   `metrics` fuera de `exposure.include`. Queda `health,info`.
4. **`lab10.base.puerto` → `lab11.base.puerto`**; `info.lab` corregido a `11 — Observabilidad`.
5. **Se quedan** `FiltroDeCorrelacion`, `SaludDeLaBase`, el patrón con `traceId`,
   `show-details: always` y los grupos de sondas.
6. **`hikari.connection-timeout` e `initialization-fail-timeout` fuera**, y con ellos todo el
   bloque `spring.datasource`: al volver al patrón de los labs 05-07 el `DataSource` sale del bean
   y ya no hay URL que configurar. **Health medido con la base viva: 4-5 ms.**

### 3.3 · La validación, ejecutada sobre `practica/`

Guion aplicado literalmente (dependencia + los dos archivos extraídos del `PASOS.md` + los tres
bloques de yml fundidos bajo un solo `management:`).

**Paso 1 — lo expuesto y lo que no:**

```
/actuator ->  health  health-path  info  self

  env          -> 404      loggers      -> 404
  beans        -> 404      mappings     -> 404
  heapdump     -> 404      configprops  -> 404
  threaddump   -> 404      metrics      -> 404
```

**Paso 2 — dos peticiones, dos traceId; la cabecera de entrada respetada; el id devuelto:**

```
17:15:04.357 INFO  [84493a86] c.d.o.c.TramiteController - Emitiendo trámite tipo=F29 rut=11.111.111-1
17:15:04.415 INFO  [84493a86] c.d.o.c.TramiteController - Trámite 2 emitido
17:15:04.428 INFO  [21ce4a51] c.d.o.c.TramiteController - Emitiendo trámite tipo=F22 rut=22.222.222-2
17:15:04.433 INFO  [21ce4a51] c.d.o.c.TramiteController - Trámite 3 emitido
17:15:04.445 INFO  [MI-ID-123] c.d.o.c.TramiteController - Listando trámites
17:15:04.546 INFO  [0d813f24] c.d.o.c.TramiteController - Emitiendo trámite tipo=F30 rut=3-3

X-Trace-Id: 0d813f24
```

**Paso 3 — el health con `baseDeDatos` y `milisegundos`, y las dos sondas:**

```json
"baseDeDatos": {"details":{"consulta":"SELECT count(*) FROM tramite","milisegundos":5},"status":"UP"}
```

```
liveness  200  {"components":{"livenessState":{"status":"UP"}},"status":"UP"}
readiness 200  {"components":{"baseDeDatos":{...,"status":"UP"},
                              "readinessState":{"status":"UP"}},"status":"UP"}
```

**Los dos dan 200 y no dicen lo mismo**, y el guion lo aprovecha: liveness mira un estado,
readiness mira además la base. Ahí ya se ve la separación sin necesitar tirar nada.

El **503 con la base caída no se corre**: se proyecta el JSON y se explica, como pide la spec. La
regla de liveness contra readiness se mantiene entera, con su párrafo de «ninguna dependencia
externa va en liveness, nunca».

**Consola de arranque limpia**, sin el WARN de `open-in-view`:

```
16:54:15.376 INFO  [........] c.d.o.Lab11Application - Started Lab11Application in 4.282 seconds
```

---

## 4 · SPEC-032 · Lab 12 · Tareas y asincronía · de 5 pasos a 3

### 4.1 · Archivos borrados

```
{solucion,instructor}/src/.../programadas/Recordatorio.java
{solucion,practica,instructor}/src/.../soporte/Instancia.java
```

### 4.2 · Archivos modificados

```
labs/lab-12-tareas/PASOS.md                                  reescrito, paso 0 + 3 pasos
labs/lab-12-tareas/README.md
labs/lab-12-tareas/guia-lab-12-tareas.pdf                    regenerado, 10 páginas
docs/guias/fuente/guia-lab-12-tareas.md
{solucion,instructor}/src/.../programadas/CierreNocturno.java
{solucion,practica,instructor}/src/.../controllers/TramiteController.java
{practica,instructor}/src/.../services/NotificadorService.java
{practica,instructor}/src/.../Lab12Application.java
{solucion,practica,instructor}/src/main/resources/application.yml
instructor/LEEME.md
```

Los cinco puntos de la spec:

1. **`Recordatorio.java` borrado.**
2. **`Instancia.java` borrado**; el log del cierre queda `[CIERRE] vuelta {} · {} · hilo {}` y
   `/quien` pierde el campo `instancia`.
3. **Se quedan** `CierreNocturno` con `fixedDelay`, los dos métodos del notificador, los tres
   endpoints y `spring.threads.virtual.enabled`.
4. **La referencia «Lab 09» corregida a «Lab 10»** al hablar de Tesorería. Estaba en el paso 4 de
   `PASOS.md`, partida por un salto de línea (`Lab\n09`), que es por lo que un grep de «Lab 09» no
   la encontraba.
5. **La frase del candado del lab 07, corregida**, y ahora vive en el párrafo de cierre. El texto
   nuevo dice que aquel candado **sí** funciona entre procesos porque vivía en la base, y que lo
   que no funciona entre procesos es `synchronized`. Está en `PASOS.md`, en la guía y en el
   comentario final de `instructor/CierreNocturno.java`.

### 4.3 · La medición, ejecutada sobre `practica/`

**Pasos 0 a 2, con hilos de plataforma:**

```
{"hiloQueAtiende":"Thread[#58,http-nio-8103-exec-1,5,main]","esVirtual":false,"vueltasDelCierre":0}

  sincrono:  3.020667s
  asincrono: 0.004530s
```

**Tres líneas `[ASINCRONO]`, tres hilos distintos, misma marca de tiempo:**

```
17:15:49.425 [task-2] [ASINCRONO] aviso enviado a luis@sii.cl  · hilo Thread[#74,task-2,5,main]
17:15:49.425 [task-1] [ASINCRONO] aviso enviado a ana@sii.cl   · hilo Thread[#73,task-1,5,main]
17:15:49.425 [task-3] [ASINCRONO] aviso enviado a sofia@sii.cl · hilo Thread[#75,task-3,5,main]
```

Y los tres síncronos, en **el mismo** hilo y separados por un segundo:

```
17:15:46.384 [SINCRONO] aviso enviado a ana@sii.cl   · hilo Thread[#60,http-nio-8103-exec-3,5,main]
17:15:47.394 [SINCRONO] aviso enviado a luis@sii.cl  · hilo Thread[#60,http-nio-8103-exec-3,5,main]
17:15:48.395 [SINCRONO] aviso enviado a sofia@sii.cl · hilo Thread[#60,http-nio-8103-exec-3,5,main]
```

Ese contraste se añadió al guion: es la mitad que faltaba del argumento.

**El cierre corre solo — `vueltasDelCierre` sube sin que nadie llame:**

```
{"...","vueltasDelCierre":0}    →    {"...","vueltasDelCierre":1}

[CIERRE] vuelta 1 · 17:15:46 · hilo Thread[#70,scheduling-1,5,main]
```

**Paso 3 — `esVirtual` de `false` a `true`:**

```
{"vueltasDelCierre":0,"esVirtual":true,
 "hiloQueAtiende":"VirtualThread[#66,tomcat-handler-0]/runnable@ForkJoinPool-1-worker-1"}
```

Y la tarea programada también pasa a virtual, que se aprovecha en el guion:

```
[CIERRE] vuelta 1 · 17:16:12 · hilo VirtualThread[#61,scheduling-1]/runnable@ForkJoinPool-1-worker-1
```

### 4.4 · El párrafo «lo que no vimos»

Lleva **el cron de seis campos con su zona horaria** y **el problema de las dos instancias**
contado entero: por qué ocurre, las tres consecuencias, el candado distribuido con sus tres
implementaciones y su expiración, y la corrección sobre el lab 07. El dibujo de las dos instancias
se mantiene en la guía del instructor (`instructor/CierreNocturno.java`, comentario final).

---

## 5 · SPEC-033 · Lab 13 · Empaquetado · de 5 pasos a 4

### 5.1 · La enmienda, verificada

La spec original pedía borrar `tools/jib-base/`. **La enmienda del PO lo prohíbe, y se comprobó
que la enmienda es correcta antes de aplicarla:**

```
proyecto-final/base/.mvn/maven.config                     -Djib.baseImageCache=../../tools/jib-base
proyecto-final/instructor/solucion-referencia/.mvn/maven.config   -Djib.baseImageCache=../../../tools/jib-base
examen-huecos/base/.mvn/maven.config                      -Djib.baseImageCache=../../tools/jib-base
examen-huecos/solucion/.mvn/maven.config                  -Djib.baseImageCache=../../tools/jib-base

jib-maven-plugin en:  proyecto-final/base/pom.xml
                      proyecto-final/instructor/solucion-referencia/pom.xml

tamaño de tools/jib-base:  120 MB
```

**`tools/jib-base/` NO se tocó.** Los 120 MB siguen en el repositorio, y el tamaño antes y después
es el mismo. Lo que se borró son los **dos `.mvn/maven.config` del lab 13**, que eran los únicos
cuyo contenido era exclusivamente esa propiedad.

### 5.2 · Archivos borrados

```
labs/lab-13-empaquetado/solucion/.mvn/maven.config
labs/lab-13-empaquetado/practica/.mvn/maven.config
```

`.mvn/wrapper/` se conserva en las dos carpetas: es el wrapper de Maven, no tiene que ver con Jib.

### 5.3 · Archivos modificados

```
labs/lab-13-empaquetado/PASOS.md                             reescrito, 4 pasos
labs/lab-13-empaquetado/README.md
labs/lab-13-empaquetado/guia-lab-13-empaquetado.pdf          regenerado, 10 páginas
docs/guias/fuente/guia-lab-13-empaquetado.md
{solucion,practica,instructor}/pom.xml                       jib-maven-plugin y jib.version fuera
{solucion,practica,instructor}/src/.../DondeEstoyController.java
{solucion,practica,instructor}/src/main/resources/application*.yml
instructor/LEEME.md
ESTADO.md                                                    (ver §6.2)
```

Los seis puntos de la spec:

1. **`jib-maven-plugin` y `jib.version` fuera** de los tres poms.
2. **Los dos `.mvn/maven.config` borrados.**
3. **`tools/jib-base/` NO borrado** — enmienda del PO, verificada en §5.1.
4. **`lab12.saludo` y `lab12.tesoreria-url` → `lab13.*`** en los tres yml de cada carpeta y en
   `DondeEstoyController`.
5. **`enContenedor` borrado** del endpoint. Leía `LAB12_EN_CONTENEDOR`, que nada definía.
6. **Se quedan** `layers.enabled: true`, los tres yml de perfiles y `/donde-estoy`.

Además, del README se quitaron **la tabla de tamaños de la imagen** y la sección **«Sin Docker. De
verdad»**, y en el cierre de los tres documentos se nombra Jib como la forma de construir la imagen
OCI sin Docker, para quien lo quiera probar fuera del curso — con el apunte de que el proyecto
final lo usa y hay un ejemplo funcionando al que mirar.

### 5.4 · Los nombres de los jar

La spec pide el nombre real de cada carpeta, y `PASOS.md` lo dice explícitamente arriba:

```
practica/target/lab13-empaquetado-0.1.0.jar
solucion/target/lab13-empaquetado-solucion-0.1.0.jar
```

### 5.5 · La validación

**`./mvnw package` produce el jar en las dos carpetas:**

```
21M   practica/target/lab13-empaquetado-0.1.0.jar
21M   solucion/target/lab13-empaquetado-solucion-0.1.0.jar
```

**El manifiesto:**

```
Main-Class:  org.springframework.boot.loader.launch.JarLauncher
Start-Class: cl.dgt.empaquetado.Lab13Application
```

**`list-layers` — las cuatro capas, en las dos carpetas:**

```
dependencies
spring-boot-loader
snapshot-dependencies
application
```

**Las tres corridas del paso 4**, con el mismo jar de `practica/` y sin reconstruir entre una y
otra:

```
1. java -jar (sin perfil)
   {"tesoreriaUrl":"http://localhost:9999/tesoreria-falsa","javaVersion":"25.0.4",
    "perfilesActivos":[],"saludo":"Hola desde el entorno por defecto"}

2. --spring.profiles.active=dev
   {"saludo":"Hola desde DESARROLLO","perfilesActivos":["dev"],
    "javaVersion":"25.0.4","tesoreriaUrl":"http://localhost:9098/pagos"}

3. prod + TESORERIA_URL
   {"perfilesActivos":["prod"],"saludo":"Hola desde PRODUCCIÓN",
    "tesoreriaUrl":"https://tesoreria.sii.cl/pagos","javaVersion":"25.0.4"}
```

La tercera es además la demostración de la **precedencia**: el perfil `prod` propone
`https://tesoreria.example.cl/pagos` y la variable de entorno la pisa.

**Arranque en el 8106** (`solucion/`) y en el 8105 (`practica/`), comprobado en las dos.

---

## 6 · Desviaciones

### 6.1 · `PuertoLibre` y `CandadoLibre` del lab 11 no se borraron

La spec dice: «Borrar `infra/MotorDePostgres.java`, `infra/PuertoLibre.java`,
`infra/CandadoLibre.java` y `controllers/SimuladorController.java`».

**Se borraron dos de los cuatro.** `PuertoLibre` y `CandadoLibre` se quedan, y el motivo es el
punto 2 de la misma spec: *«El PostgreSQL embebido de Zonky se levanta como en los labs 05 al 07,
con el mismo patrón de esos labs. Copiar el patrón, no inventar uno.»*

Ese patrón **es** `PuertoLibre.exigir(...)` + `CandadoLibre.exigir(...)` dentro del
`@Bean EmbeddedPostgres`. Los labs 04 a 07 los tienen todos. Borrarlos obligaría a inventar un
arranque distinto —justo lo que el punto 2 prohíbe— y perdería los dos mensajes de error legibles
que la SPEC-FIX-07 y la SPEC-FIX-08 pusieron ahí a propósito.

Los dos puntos de la spec son incompatibles entre sí; se eligió el que la spec marca como
requisito de forma («copiar el patrón, no inventar uno»).

### 6.2 · Se tocó `ESTADO.md`, que no está en el alcance

`ESTADO.md` decía, sobre la maleta:

> `tools/jib-base/` — las capas de `eclipse-temurin:25-jre`, para que el Lab 13 construya su
> imagen OCI sin salir a la red (D-032-1)

Eso dejó de ser verdad con esta spec. Se reescribió esa entrada para que diga lo que ahora es
cierto: que las capas se quedan **para el proyecto final y el examen**, y que el Lab 13 ya no las
usa. Es un cambio de una entrada, no una revisión del documento.

### 6.3 · El escenario del paso 3 del lab 10 cambió de «caída» a «lenta»

La spec dice, del paso 3: *«el circuito, 0,002 s y 0 llamadas, con la vuelta a CLOSED»*, y en
Validación pide *«0,002 s con circuito abierto»*. Se cumple el efecto y el orden de magnitud
(0,003 s medidos), pero **con Tesorería lenta en vez de caída**.

El motivo está medido en §2.3: con Tesorería caída, las peticiones previas a la apertura tardan
7 ms, y el «cien veces más rápido» del guion viejo se queda en «el doble». Con Tesorería lenta, las
cinco primeras tardan 2 s y las siguientes 3 ms, y **los tres números del laboratorio quedan sobre
el mismo escenario**. La degradación del paso 4 sí usa Tesorería caída, que es donde tiene sentido.

### 6.4 · Los `0,002 s` de la spec son `0,003 s` medidos

Diferencia de una milésima entre lo que la spec anticipaba y lo medido hoy. Los guiones llevan el
número real. El orden de magnitud —milésimas contra segundos— es el mismo.

---

## 7 · Verificación final

**Los cuatro verificadores del CI, en verde:**

```
verificar-temario.py            VEREDICTO: las 5 verificaciones PASAN
verificar-pasos-copiables.py    [OK] 16 guion(es) verificado(s)
verificar-guion-vs-practica.py  [OK] 95 promesas comprobadas, todas verdad
verificar-instructor.py         [OK] 21 XML · 206 .java · 18/18 carpetas
```

**La regla de siembra (P-18):** los cinco `PASOS.md` contienen el patrón `siembra`.

**Compilación offline, como el job `labs`:**

```
[INFO] 41 proyectos · 0 fallos
git status --porcelain repo-maven   (sin adiciones)
```

**Los cinco PDF regenerados desde su fuente**, y el verificador de extracción confirma que ninguna
guía promete código que `solucion/` no tenga:

```
guia-lab-09-seguridad.pdf        11 páginas   94 KB
guia-lab-10-resiliencia.pdf      11 páginas   99 KB
guia-lab-11-observabilidad.pdf   12 páginas  100 KB
guia-lab-12-tareas.pdf           10 páginas   93 KB
guia-lab-13-empaquetado.pdf      10 páginas   91 KB

[OK] 82 bloque(s) comprobado(s) contra solucion/ · 0 línea(s) que la solución no tiene
```

**Ningún lab fuera del 09-13 se tocó.** Los cambios en `docs/` son los cinco fuentes de guía y este
informe; el de `ESTADO.md` está declarado en §6.2; el de `repo-maven/` es el borrado de
`resilience4j-retry`, verificado sin consumidores.

---

## 8 · Lo que queda para el arquitecto

Como dice la spec, **las guías del instructor de los labs 09 al 13 las actualiza el arquitecto
después de recibir este informe**. Lo que este informe deja hecho de su parte:

- Las carpetas `instructor/` están al día con `solucion/` —el verificador lo confirma— y sus
  comentarios explican **qué salió y por qué**, para que el instructor pueda contestar si alguien
  pregunta. En particular: la nota de la tolerancia de reloj (lab 09), la del número de peticiones
  que ahora necesita el circuito (lab 10), la del paso 5 que ya no se corre (lab 11), la del
  problema de las dos instancias (lab 12) y la de qué se fue con Jib (lab 13).
- El **dibujo de las dos instancias** del lab 12 se mantiene, como pide la spec.
