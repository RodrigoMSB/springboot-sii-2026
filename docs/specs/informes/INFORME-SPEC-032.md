# INFORME-SPEC-032 · Cuatro de los cinco labs que faltaban

**SPEC:** SPEC-032 · **Ejecuta:** mocito · **Fecha:** 18 de agosto de 2026
**Rama:** `spec-032-labs-08-12` · **Tag al cierre:** `material-v0.8.0`
**Máquina:** Mac Studio del PO (Darwin 25.5.0, `arm64`) · JDK 25.0.4 y Maven 3.9.11 de la maleta

---

## 1 · Veredicto en una línea

**NACEN CUATRO LABS —08, 09, 11 y 12— Y EL ARCO NUEVO LLEGA HASTA EL EMPAQUETADO.** Los cuatro
arrancan, sus números están medidos y citados, seguir `PASOS.md` sobre `practica/` llega al
resultado de `solucion/` en los cuatro, y `instructor/` existe en los cuatro sin entrar al
repositorio. **El Lab 10 no se hizo, por decisión del PO** (§7.a): su nombre choca *exactamente*
con el del arco viejo y no se inventó uno provisional. **El aviso de la SPEC sobre Jib se
activó** (§7.b): construir la imagen sin red exige 122 MB de imagen base en el repositorio, y el
PO autorizó commitearlos con el criterio del JDK embebido. Hay además **una desviación de
protocolo propia**, declarada en §7.e: la SPEC se commiteó después de ejecutar, no antes.

---

## 2 · Qué nace, y con qué forma

| Lab | Pasos | Duración estimada | Puertos | Base de datos | Tamaño en limpio |
|---|---|---|---|---|---|
| `lab-08-seguridad` | 6 | **2 h 50** | 8095 / 8096 · PG 55440 / 55441 | sí, para usuarios | 272 KB |
| `lab-09-resiliencia` | 5 | **2 h 30** | 8097 / 8098 · WireMock 9097 / 9098 | no | 200 KB |
| `lab-11-tareas` | 5 | **2 h 20** | 8103 / 8104 | no | 180 KB |
| `lab-12-empaquetado` | 5 | **2 h 45** | 8105 / 8106 | no | 168 KB |

Los cuatro **por debajo de las tres horas** y **muy por debajo de 1 MB**, como exigen las
transversales de §7 de la SPEC.

### Lo que quedó en «lo que no vimos hoy»

| Lab | Fuera, y nombrado |
|---|---|
| 08 | OAuth2 / OpenID Connect · refresh tokens · seguridad a nivel de método (`@PreAuthorize`) |
| 09 | Bulkheads · rate limiting · backoff exponencial con jitter |
| 11 | Candado distribuido de verdad · colas de mensajes · planificadores distribuidos |
| 12 | Kubernetes · registries · CI/CD |

### La estructura de tres carpetas, medida

| | `practica/` | `solucion/` | `instructor/` |
|---|---|---|---|
| Lab 08 | 0 líneas de documentación en `src/` | 11 | ~640 |
| Lab 09 | 0 | 14 | ~600 |
| Lab 11 | 0 | 9 | ~530 |
| Lab 12 | 0 | 8 | ~490 |

En los cuatro, `instructor/` contiene **todos** los archivos de `solucion/` con la misma
estructura de carpetas, y **ninguno** contiene `mvnw`, `.mvn` ni `target`.

---

## 3 · La cadena de preguntas

Los cuatro labs encadenan, y cada uno abre el siguiente con un problema que él mismo creó:

1. **Lab 08** cierra la API. Y al cerrarla la vuelve **dependiente de la base**: cada login la
   consulta. *¿Qué pasa cuando lo que está debajo no contesta?*
2. **Lab 09** sobrevive al vecino caído: degrada, abre el circuito, responde igual. Y entonces
   **nadie se entera de nada** — el circuito abrió y se fue por la consola. *¿Cómo se hace para
   que el sistema cuente lo que le pasa?* (**Esa pregunta queda abierta: es el Lab 10, §7.a.**)
3. **Lab 11** hace trabajo solo y sin bloquear, y descubre que **con dos instancias el trabajo se
   hace dos veces**. *Si va a haber varias copias, ¿qué tiene que ser idéntico entre ellas?*
4. **Lab 12** construye esa copia: un artefacto, todos los ambientes. Y cierra el curso.

El hueco del 10 **rompe la cadena en un punto** y está declarado como tal en el guion del 09: su
cierre plantea la pregunta de observabilidad y no promete la sesión siguiente.

---

## 4 · Tabla de verificación

Las ocho pruebas de la SPEC, por lab:

| | V1 arranca | V2 funciona | V3 los números | V4 PASOS→solución | V5 instructor | V6 git | V7 offline | V8 ls |
|---|---|---|---|---|---|---|---|---|
| **08** | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ |
| **09** | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ |
| **11** | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ |
| **12** | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ (§7.b) | ✅ |

### Lab 08 · V1, V2 y V3

`practica/` en su estado de entrega arranca **con la API abierta**, que es el punto de partida:

```
Tomcat started on port 8095 (http) with context path '/'
GET /productos   -> 200
```

**El paso 1**, con la dependencia añadida:

```
Using generated security password: 899fd35b-1dc5-4031-be00-3c284b88c6de
GET /productos                              -> 401
GET /productos (user:899fd35b-...)          -> 200
```

**El paso 3** — la semilla, con las dos claves iguales:

```
[semilla] ana   ADMIN    $2a$10$z2RuZ6YymqMEOa9haqcN2.m1B31q1pL1oGfPzUUaYNbi43Lor3Lsy
[semilla] luis  USUARIO  $2a$10$RBxoDtr9qH5oevKTWzwRaeKxD0Oc2pXrQtT07ayvBXO2h09HtqiN2
```

**El paso 4** — el cuerpo del token, decodificado sin clave:

```json
{"iss":"lab08","sub":"ana","exp":1787071711,"iat":1787069911,"scope":"ROLE_ADMIN"}
```

**V3 · la matriz completa**, medida sobre `solucion/` y reproducida sobre `practica/`:

```
GET /productos                  sin token      401
GET /productos                  ana ADMIN      200
GET /productos                  luis USUARIO   200
GET /productos/administracion   ana ADMIN      200
GET /productos/administracion   luis USUARIO   403     ← 401 y 403 son cosas distintas
GET /productos                  firma falsa    401
POST /auth/login                clave mala     401
```

### Lab 09 · V3, el arco de números

Todos medidos sobre `solucion/`, y reproducidos sobre `practica/`:

| situación | el usuario espera | llamadas HTTP reales |
|---|---|---|
| Tesorería sana | 0,15 s | 1 |
| **paso 1** · a 30 s, cliente ingenuo | **30,006 s** | 1 |
| **paso 2** · a 30 s, timeout de 2 s | 2,040 s — y un 500 | 1 |
| **paso 3** · a 30 s, timeout y 3 intentos | **6,439 s** | **3** |
| **paso 4** · caída, primera petición | 0,215 s | 1 |
| **paso 4** · caída, circuito abierto | **0,002 s** | **0** |

El paso 3 es el que va «en la dirección equivocada» a propósito: reintentar **empeora** una caída
real, y el guion lo usa para justificar el circuito.

**Las transiciones, en consola:**

```
>>> CIRCUITO CLOSED -> OPEN
>>> CIRCUITO OPEN -> HALF_OPEN
>>> CIRCUITO HALF_OPEN -> CLOSED
```

Y el ciclo completo de recuperación, sin intervención humana:

```
Tesorería reparada. Circuito ahora mismo: OPEN   <- todavía no lo sabe
(11 s)
  llamada de prueba 1: 0.007s -> HALF_OPEN
  llamada de prueba 2: 0.005s -> CLOSED
  y la siguiente ya es normal: {"estado":"PAGADO","monto":45000}
```

**El paso 5**, lo que recibe el usuario con Tesorería caída — un 200, no un 500:

```json
{"estado":"DESCONOCIDO","id":"77","aviso":"Tesorería no responde; el trámite sigue su curso"}
```

### Lab 11 · V3, el duplicado

```
POST /tramites/sincrono   -> el usuario espera 3.027928s
POST /tramites/asincrono  -> el usuario espera 0.003595s
```

Y los tres avisos asíncronos, en tres hilos y con la misma marca de tiempo:

```
12:58:27.502  [ASINCRONO] aviso enviado a sofia@sii.cl · hilo VirtualThread[#75,task-3]
12:58:27.502  [ASINCRONO] aviso enviado a luis@sii.cl  · hilo VirtualThread[#74,task-2]
12:58:27.502  [ASINCRONO] aviso enviado a ana@sii.cl   · hilo VirtualThread[#73,task-1]
```

**Los hilos virtuales**, antes y después de la línea de YAML:

```
false → "Thread[#58,http-nio-8103-exec-1,5,main]"
true  → "VirtualThread[#68,tomcat-handler-0]/runnable@ForkJoinPool-1-worker-1"
```

**Y el paso 5**, con dos instancias arriba — **el número de este lab**:

```
12:58:55  [CIERRE] instancia-8104 · vuelta 6
12:58:55  [CIERRE] instancia-8114 · vuelta 1     ← el mismo segundo
12:59:01  [CIERRE] instancia-8104 · vuelta 7
12:59:01  [CIERRE] instancia-8114 · vuelta 2
12:59:07  [CIERRE] instancia-8104 · vuelta 8
12:59:07  [CIERRE] instancia-8114 · vuelta 3
```

También se comprobó el ciclo de `fixedDelay`: 13:00:07 → 13:00:13 → 13:00:19, o sea **6 segundos**
(5 de espera + 1 de trabajo), que es lo que el guion afirma.

### Lab 12 · V3, la imagen

```
jar ejecutable: 20.9 MB · 181 entradas
Main-Class:  org.springframework.boot.loader.launch.JarLauncher
Start-Class: cl.dgt.empaquetado.Lab12Application

capas: dependencies · spring-boot-loader · snapshot-dependencies · application

[INFO] Built image tarball at target/jib-image.tar
imagen: 138.9 MB
```

**Abierta con `tar`, sin Docker:**

```
  capa 1:    39.6 MB  ┐
  capa 3:    20.4 MB  ├─ la base: sistema mínimo + JRE 25   (120,1 MB)
  capa 4:    60.1 MB  ┘
  capa 7:    18.6 MB  ─── las dependencias
  capas 2,5,6,8,9,10: < 0.1 MB cada una  ─── nuestro código

  arquitectura : amd64 / linux
  Entrypoint   : java -cp @/app/jib-classpath-file cl.dgt.empaquetado.Lab12Application
  puertos      : ['8106/tcp']
  entorno      : ['SPRING_PROFILES_ACTIVE=prod']
```

**El paso 5** — el mismo jar, tres arranques, sin recompilar:

```
sin perfil : {"perfilesActivos":[],       "saludo":"Hola desde el entorno por defecto"}
--dev      : {"perfilesActivos":["dev"],  "saludo":"Hola desde DESARROLLO"}
prod + var : {"perfilesActivos":["prod"], "tesoreriaUrl":"https://tesoreria.sii.cl/api/pagos"}
```

### V4 · seguir `PASOS.md` sobre `practica/`

En los cuatro labs se ejecutó el guion completo sobre `practica/`, paso a paso, y se restauró el
estado de entrega al terminar. Lo que se comprobó, más allá del resultado final:

- **Lab 08** · el estado intermedio del paso 2 (usuario en memoria, `httpBasic`) da exactamente lo
  que el guion cita: `200` en la ruta pública, `401` sin credenciales, `200` con `ana:secreta`, y
  la línea de la contraseña generada **desaparece** (0 apariciones) al haber un
  `UserDetailsService` propio. La matriz final coincide con la de `solucion/`.
- **Lab 09** · se midieron los estados intermedios que el guion cita y que **no** existen en
  `solucion/`: el paso 1 sin timeout (**30,006 s**) y el paso 2 con timeout y sin reintento
  (**2,040 s** + un 500). Los finales coinciden con `solucion/`.
- **Lab 11** · tras los cinco pasos, `practica/` da 3,03 s / 0,004 s y las tareas corriendo.
- **Lab 12** · `practica/` construye el jar por capas y la imagen OCI de 138,9 MB.

### V7 · offline

Los cuatro labs compilan y corren con el shim, que fuerza `--offline`. `repo-maven/` creció
**+3 MB**, y sólo con lo que el Lab 09 necesita:

```
io/github/resilience4j/{core,circuitbreaker,retry}   276 KB
org/jetbrains/kotlin/kotlin-stdlib (transitivo)      1.9 MB
org/jetbrains/annotations · commons-io               620 KB
```

Y el **plugin de Jib con sus dependencias**: 129 archivos, **17,7 MB** (Guava, las librerías de
Google Cloud Tools, HttpComponents y unas piezas internas de Maven). Sin ellos, un clon nuevo no
podría construir la imagen: faltaría el plugin, y `--offline` abortaría antes de llegar a mirar la
caché de la imagen base. Se detectaron al revisar `git status` antes de cerrar; **estaban en el
disco de trabajo y no versionados**, que es la forma clásica de que algo «funcione aquí» y no allá.

La captura se hizo **compilando el proyecto real**, no con `dependency:go-offline` — ver §6.2.

**La prueba de que no falta nada**, con `target/` borrado en los ocho proyectos y compilando los
cuatro labs con el shim (que fuerza `--offline`):

```
  lab-08-seguridad     practica  OK        lab-08-seguridad     solucion  OK
  lab-09-resiliencia   practica  OK        lab-09-resiliencia   solucion  OK
  lab-11-tareas        practica  OK        lab-11-tareas        solucion  OK
  lab-12-empaquetado   practica  OK        lab-12-empaquetado   solucion  OK

archivos nuevos en repo-maven tras compilar los ocho proyectos: 0
```

Y lo mismo para la imagen del Lab 12, reconstruida desde cero:

```
[INFO] Built image tarball at .../target/jib-image.tar
[INFO] BUILD SUCCESS
archivos nuevos tras la construcción: 0
```

**Cero archivos nuevos** es la prueba fuerte: todo lo que estos labs necesitan está versionado. Si
faltara algo, o habría aparecido ahí o el build habría muerto.

---

## 5 · Transversales

| Gate | Resultado |
|---|---|
| `siembra` (lógica del CI, local) | ✅ `FALLOS=0`; los cuatro `PASOS.md` siembran |
| CRLF | ✅ los ocho `mvnw` nuevos en LF |
| Guard de 95 MB | ✅ `[OK] Ningún archivo supera los 95 MB` — el mayor del `jib-base` son **60,1 MB** |
| `du -sh labs/*` | ✅ reportado en §2; ninguno de los cuatro pasa de **272 KB** |
| `instructor/` fuera de git | ✅ los cuatro existen en disco y `git status` ve **0** archivos de ellos |
| Puertos | ✅ 8095–8106 según la tabla de la SPEC; ninguno colisiona con los labs existentes |
| Prohibiciones | ✅ sin Docker en ningún lab (incluido el 12), sin narrativa DGT, sin ArchUnit, sin `bin/`, sin validadores, sin derivación, sin colas ni AOP |

**No se tocó ningún lab existente.** Los cambios fuera de `labs/lab-{08,09,11,12}-*` son:
`.gitattributes`, `.gitignore` (sin cambios — ya cubría `instructor/`), `README.md`, `ESTADO.md`,
`docs/decisiones.md`, `docs/specs/` y `repo-maven/` + `tools/jib-base/`.

> **Nota sobre el árbol de trabajo:** siguen presentes los cambios locales sin commitear del PO en
> `lab-00-hola-mundo` y `lab-01-web`. **Se dejaron intactos y fuera de todos los commits.**

---

## 6 · Decisiones tomadas al ejecutar

Cinco filas nuevas en `docs/decisiones.md` (**D-032-1** a **D-032-5**), y dos que conviene
explicar aquí:

### 6.1 · Resilience4j núcleo, no su starter — y con el orden de capas corregido

Se aplicó **SPEC-018 §9**, que ya estaba decidida: el starter `resilience4j-spring-boot3` está
escrito contra APIs que Boot 4 reorganizó, y el núcleo es Java puro. Además es mejor
pedagógicamente: el circuito se declara a mano y sus estados se imprimen.

Al medir apareció algo que la decisión previa no cubría. Con el reintento por fuera del circuito
—el orden que recomienda Resilience4j— un circuito **abierto** rechaza al instante y el reintento
**reintenta el rechazo**, esperando 2 × 200 ms para nada:

```
antes de arreglarlo:  0,41 s por petición con el circuito abierto
después:              0,002 s
```

Y lo grave no era el tiempo: **borraba la lección del paso 4**, que existe precisamente para
mostrar que un circuito abierto responde al instante. Se resolvió con
`ignoreExceptions(CallNotPermittedException.class)`. Registrado como **D-032-4**.

### 6.2 · La captura en `repo-maven/` se hace compilando, no con `dependency:go-offline`

El primer intento usó `dependency:go-offline` y arrastró **299 archivos**: `avalon-framework`,
`commons-chain`, `commons-digester`, cuatro versiones viejas de `commons-collections`… todo
resolución de plugins del ciclo de vida completo, nada que el lab use. Se revirtió por entero
(`git clean` + `git checkout` sobre `repo-maven/`) y se rehízo compilando el proyecto real: **8
archivos**, todos en el árbol de dependencias del pom.

Es la doctrina de D-022-1 —«se puebla por captura real, nunca a mano»— con una precisión que ahora
queda escrita: *captura real* significa **construir el proyecto**, no pedirle a Maven que se
prepare para todo.

### 6.3 · Cinco decisiones de construcción menores

1. **Lab 08** · los productos siguen en memoria y sólo los usuarios van a la base. La SPEC dice
   «base de datos sólo donde el tema la exige»; el paso 3 exige mirar hashes en una tabla, los
   productos no exigen nada.
2. **Lab 08** · la entidad `Usuario`, su repositorio y la migración **llegan hechas**. Son materia
   del Lab 03b, y escribirlas en clase se comería el tiempo de la seguridad.
3. **Lab 09** · Jetty alineado a la versión de WireMock por BOM. Sin eso la aplicación **no
   arranca**: `Environment.ensure` no existe en el Jetty que gestiona el BOM de Boot 4. Es el
   mismo arreglo que ya llevaba el lab-08 del arco antiguo.
4. **Lab 11** · el lab **no** resuelve la tarea duplicada, por instrucción de la SPEC. Queda
   declarado en el guion para que no se lea como un descuido (**D-032-5**).
5. **Lab 12** · es el único pom del arco que **enciende** el `repackage`. En los demás está apagado
   porque el fat jar no se usa; aquí el fat jar es el contenido del paso 1.

---

## 7 · Sorpresas y desviaciones

### 7.a · El Lab 10 no se hizo — la premisa de la SPEC no se cumplía

La SPEC §0 pedía verificar las colisiones de nombre y afirmaba que no había ninguna. Medido:

```
libre:            labs/lab-08-seguridad
libre:            labs/lab-09-resiliencia
COLISIÓN EXACTA:  labs/lab-10-observabilidad
libre:            labs/lab-11-tareas
libre:            labs/lab-12-empaquetado
```

Cuatro de cinco eran como decía la SPEC. El 10 no: el lab del arco antiguo se llama **literalmente**
`lab-10-observabilidad`, y §9 prohíbe tocarlo.

**Consultado el PO con el hallazgo**, su decisión: **no se inventa un nombre por un choque
temporal.** El lab nuevo se llamará `lab-10-observabilidad` cuando la SPEC de reempaquetado retire
el arco viejo, y se construirá entonces. Anotado en la SPEC (§10.1), en el `README.md` de la raíz
y en `ESTADO.md`.

**Consecuencia sobre el material, dicha en voz alta:** el arco vigente tiene un hueco entre el 09 y
el 11, y la cadena de siembra se rompe ahí. El cierre del Lab 09 plantea la pregunta de
observabilidad **sin prometer** que la conteste la sesión siguiente; el del Lab 08 se reescribió
para que apunte al 09 (resiliencia) y no al 10, que era su destino natural.

### 7.b · Jib: el aviso de la SPEC se activó, y costó 122 MB

§6 avisaba: «si no hay forma de que funcione offline, detenerse y reportar». Se midió antes de
construir nada:

| | resultado |
|---|---|
| Jib 3.4.6 con bytecode de Java 25 | **falla**: `Unsupported class file major version 69` |
| Jib 3.5.2, con red | construye |
| Jib 3.5.2, `--offline`, caché **vacía** | **falla**: `Cannot run Jib in offline mode; eclipse-temurin:25-jre not found in local Jib cache` |
| Jib 3.5.2, `--offline`, caché **poblada** | **construye** — 138,9 MB |

Ese tercer renglón es la prueba de que el offline es real y no un accidente: Jib respeta el
`--offline` de Maven y falla explícitamente si la base no está cacheada.

**Consultado el PO**, autorizó commitear la caché con el criterio del JDK embebido. Está en
`tools/jib-base/` (**122 MB**, 8 archivos, blob mayor de 60,1 MB, bajo el guard), y **hubo que
marcarla `binary` en `.gitattributes`**: el `* text=auto eol=lf` de la raíz habría reescrito los
blobs y Jib los habría rechazado, porque su nombre **es** su digest sha256.

**Cómo se regenera** (con internet, sólo quien prepara el material):

```bash
rm -rf tools/jib-base
cd labs/lab-12-empaquetado/solucion
DGT_ONLINE=1 ./mvnw package jib:buildTar
```

### 7.c · `jib.baseImageCache` en `<properties>` se ignora, y en silencio

Primer intento: la propiedad en el `<properties>` del pom, con `${project.basedir}/../../../…`.
El build **pasó en verde** y la caché acabó en `~/Library/Caches/Google/Jib` — la carpeta personal
de quien construye. En la máquina del preparador «funciona»; en la del alumno, el lab se cae en la
primera orden.

Jib lee ese ajuste de las propiedades **de usuario** (`-D`), no de las del proyecto. La solución
es `.mvn/maven.config`, cuyo contenido Maven añade a la línea de comandos en cada invocación:

```
-Djib.baseImageCache=../../../tools/jib-base
```

Registrado como **D-032-2**. Es exactamente la clase de verde engañoso que el material persigue:
pasó, y no habría funcionado donde importa.

### 7.d · Dos hallazgos menores que quedaron en el material

- **Spring Security 7 añade autoridades técnicas.** El token del Lab 08 salía con
  `"scope": "ROLE_ADMIN FACTOR_PASSWORD"`. Funciona igual, pero el paso 4 proyecta el contenido del
  token y se lee en voz alta: un `FACTOR_PASSWORD` que nadie ha explicado se lleva cinco minutos de
  la sesión. Se filtra a los `ROLE_`.
- **`NimbusJwtEncoder` hay que construirlo con `withSecretKey`.** Con el constructor genérico
  —que es lo que aparece en casi todos los tutoriales— intenta firmar con RS256 sobre una clave
  HMAC y revienta **en ejecución**, en el primer login:
  `Failed to select a JWK signing key`. Avisado en `PASOS.md` y en `instructor/`.

### 7.e · Una desviación de protocolo propia: la SPEC se commiteó tarde

El `README.md` de la raíz dice: **«Ninguna ejecución comienza antes de que su SPEC esté
commiteada.»** No se cumplió: `docs/specs/SPEC-032-labs-08-12.md` entró al repositorio **después**
de construir los cuatro labs, en el commit `ca7326f`, no antes del primero.

No tuvo consecuencia material —la SPEC se ejecutó tal como llegó, y sus dos ajustes quedaron
anotados dentro de ella según el protocolo— pero es una regla de la casa y se incumplió. Queda
declarada aquí en vez de pasar inadvertida.

---

## 8 · Lo que queda

**Del PO, y son las tres del cierre del arco:**

1. **El Lab 10 (observabilidad).** Es el pendiente explícito de esta SPEC. Va con la SPEC de
   reempaquetado, cuando el nombre quede libre. Su contenido está especificado en la SPEC-032 §4 y
   no hace falta volver a decidirlo: Actuator, MDC con id de correlación, una métrica de negocio,
   un health indicator propio, y liveness contra readiness tirando la base abajo.
2. **La retirada del arco viejo y la renumeración.** Hoy conviven dos `lab-08-*`, dos `lab-09-*`,
   dos `lab-11-*` y dos `lab-12-*` con nombres distintos, más el `lab-07` duplicado que ya venía de
   la SPEC-031. Funciona, y no se sostiene mucho más.
3. **La migración de los labs 00 a 07 a la estructura de tres carpetas.** El 07 la estrenó y del 08
   al 12 nacieron con ella; **del 00 al 06 siguen con los bloques explicativos largos** en
   `practica/`. El arco es asimétrico: el alumno pasa de un `practica/` que le explica todo a uno
   que no le explica nada, justo a mitad del curso.

**Del material, ya cubierto pero conviene tenerlo escrito:**

- **`instructor/` no está respaldada por Git**, por diseño (D-031-2). Las cuatro carpetas nuevas
  viven sólo en el disco donde se generaron; el informe deja constancia de qué contenían y el
  `LEEME.md` de cada una dice cómo regenerarlas desde `solucion/`.
- **`tools/jib-base/` sí está versionada**, y es lo único de esta SPEC que engorda el clon del
  alumno: **+122 MB**, un ~13 % sobre lo que ya pesaba. Con eso, el Lab 12 construye una imagen OCI
  real en una máquina sin Docker y sin internet.
- **El mapa `docs/temario/MAPA-LAB-MODULO.md` no se tocó** — está fuera del alcance de esta SPEC — y
  los cuatro labs nuevos todavía no aparecen en él.

**Nada bloquea el merge.** Las ocho verificaciones están en verde para los cuatro labs, con su
salida citada; las tres desviaciones (§7.a, §7.b, §7.e) están resueltas y declaradas, y el único
pendiente de alcance —el Lab 10— es una decisión tomada, no un trabajo a medias.
