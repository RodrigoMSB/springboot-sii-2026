# INFORME-SPEC-025 · La diplomacia viaja en la maleta (Labs 08–11)

**SPEC:** SPEC-025 · **Ejecuta:** mocito · **Fecha:** 15 de agosto de 2026
**Rama:** `spec-025-fase-2-labs-08-11` · **Tag al cierre:** `material-v0.5.0`
**Máquina:** Mac Studio del PO — Darwin 25.5.0, `arm64`
**Base:** `main` en `material-v0.4.0` (`b9bd2e4`)

---

## 1 · Veredicto en una línea

**LOS LABS 08 A 11 CORREN SIN DOCKER, Y EL CI VUELVE A ESTAR EN SINCRONÍA HASTA EL LAB 11** — TESO
dejó de ser un contenedor para ser una librería in-process, los cuatro labs pasaron sus N1–N6, los
dos demos en vivo que de verdad usaban Docker se conservan enteros y medidos, y el único rojo que
queda es la frontera 11→12, que es la que la SPEC autoriza a dejar roja. Con esto, **del Lab 00 al
Lab 11 el curso entero corre sin demonio, sin administrador y sin red**.

Dos cosas que no estaban en la SPEC y hay que leer: **A2.4 tiene media respuesta y el sospechoso
principal quedó refutado** (§6), y apareció **A3.1, un defecto preexistente de la SPEC-024** que
toca lo que aquella prometió (§7).

---

## 2 · Qué se hizo, por lab

Los cuatro siguieron la misma receta. La primera vuelta (Lab 08) fue la cara: es donde se
descubrieron los tres problemas de §3. Las tres siguientes fueron propagación.

| Lab | Archivos tocados | Suite de la solución | Tiempo | Lo propio de este lab |
|---|---|---|---|---|
| **08** Diplomacia con Tesorería | 55 | 40 unitarios + **27 IT** | 20,8 s | TESO nace aquí: `BaseResilienciaIT` y `TesoSimuladoDev` |
| **09** Caja negra | 56 | 40 unitarios + **34 IT** | 22,2 s | `BaseObservabilidadIT` (captura de logs con appender en memoria) |
| **10** Observabilidad | 58 | 40 unitarios + **47 IT** | 34,5 s | `E1_HealthQueNoMienteIT`, que **mata su propia base** |
| **11** Latidos | 61 | 40 unitarios + **57 IT** | 41,5 s | `BaseLatidosIT`; y el demo de dos instancias |

**Dimensionando lo que queda:** el trabajo por lab es sorprendentemente plano —entre 55 y 61
archivos— porque **la inmensa mayoría es propagación mecánica por la cadena de derivación**: cada
lab hereda del anterior todo salvo lo suyo. Lo que cuesta de verdad son los archivos propios: uno
o dos por lab, más su `pom.xml`. Para el Lab 12 la cuenta será parecida en volumen y **mayor en
dificultad**, porque su pieza propia es RabbitMQ y ahí no hay una librería in-process equivalente
a WireMock esperando: esa es una decisión de alcance, no una migración.

### Cómo funciona ahora TESO

- **En los tests (D-025-1).** Cada base de IT levanta un `WireMockServer` en **puerto dinámico**
  (`port(0)`), lo publica por `@DynamicPropertySource` y lo gobierna por **API Java directa**. Lo
  que antes era un POST de JSON contra `/__admin/mappings` ahora es `TESO.stubFor(get(...))`: si
  te equivocas en el nombre de un método, no compila.
- **En dev (D-025-2).** `TesoSimuladoDev`, una `@Configuration @Profile("dev")`, lo levanta en el
  **8089 fijo** con los mappings cargados del classpath. El 8089 lo nombran las guías, los `curl`
  del alumno y el `--teso-lento`; la API de administración sigue viva ahí, así que **el material
  escrito sigue siendo verdad palabra por palabra**.
- **Todo atado a `127.0.0.1`.** Y esto no solo mantiene A2.3: **reduce** la exposición. El
  `compose.yaml` publicaba `"8089:8080"`, es decir, TESO en **todas** las interfaces de la
  máquina. Medido tras la migración:

```
lsof -nP -iTCP:8099 -sTCP:LISTEN   ->  java  TCP 127.0.0.1:8099 (LISTEN)
lsof -nP -iTCP:8089 -sTCP:LISTEN   ->  java  TCP 127.0.0.1:8089 (LISTEN)
```

---

## 3 · Las tres cosas que hubo que medir, porque razonarlas fallaba

Ninguna se podía deducir del changelog. Las tres aparecieron corriendo.

### 3.1 · `wiremock` a secas no arranca sobre Boot 4.1

```
Jetty 11 is not present and no suitable HttpServerFactory extension was found.
```

El artefacto de base trae el servidor HTTP para Jetty 11; Boot 4.1 va en Jetty 12. Es
`org.wiremock:wiremock-jetty12`.

### 3.2 · Con eso, el classpath quedaba PARTIDO entre dos Jetty

```
NoSuchMethodError: org.eclipse.jetty.util.component.Environment.ensure(String)
```

Boot 4.1.0 gestiona `org.eclipse.jetty:*` en **12.1.10**, pero **no** gestiona
`org.eclipse.jetty.ee10:*`, que llegaba en **12.0.16** desde el BOM de WireMock. Así que
`jetty-ee10-servlet-12.0.16` llamaba a un método que `jetty-util-12.1.10` ya no tiene.

Se alinea **a la baja**, a la versión que WireMock declara y contra la que está probado, y **por
BOM, no por la propiedad `jetty.version`** de Boot: bajar esa propiedad hace que el BOM de Boot
intente importar un `jetty-ee11-bom:12.0.16` que **no existe** (ee11 nace con Jetty 12.1), y el
POM ni se lee. También medido.

### 3.3 · La peor, porque no rompía: MENTÍA

`E1_TimeoutIT` es el test que da sentido al Lab 08 — mide que el pago falla RÁPIDO cuando TESO
cuelga. Tras la migración empezó a fallar con un número extraño: **2637 ms** contra un presupuesto
de 2000, cuando el timeout es de 800.

Se midió, en este orden:

1. WireMock in-process + `HttpURLConnection` con `readTimeout=800` → corta a los **809 ms**. El
   mecanismo funciona.
2. El cliente exacto de la app contra ese TESO → **802–823 ms**, tres veces seguidas. También.
3. Instrumentando el adaptador: **se invocaba DOS veces**, en dos hilos distintos de Tomcat,
   separadas 1,84 s. No era un reintento del cliente de TESO: eran **dos peticiones a la
   aplicación**.
4. Línea base con contenedores, extraída de `main` y ejecutada con Docker: **una sola
   invocación**, test verde. La regresión era nuestra.
5. Classpath: WireMock arrastra **Apache HttpClient 5** y `jetty-client`, que antes no estaban.

La causa: **Spring elige el transporte de `RestTestClient` por lo que encuentra en el classpath**
cuando nadie se lo dice, y prefiere Apache HttpClient 5 sobre el del JDK. Y la estrategia de
reintentos por defecto de Apache **reintenta ante un 503, esperando un segundo**. El 503 es
exactamente lo que este lab enseña a devolver cuando TESO no contesta: el cliente de test se
tragaba la degradación elegante, esperaba y repetía el pago entero.

```
transporte autodetectado (Apache):  2 llamadas a TESO · 2660 ms · E1_TimeoutIT FALLA
transporte fijado (JDK):            1 llamada  a TESO ·  864 ms · E1_TimeoutIT pasa
```

**Excluir Apache no es una opción**: WireMock construye su `ApacheHttpClientFactory` en el propio
constructor de `WireMockServer`. Se intentó y el resultado fue `NoClassDefFoundError:
org/apache/hc/client5/http/AuthenticationStrategy`. Así que el arreglo va donde tiene que ir: el
transporte se **fija** en `BaseResilienciaIT`, que es el único sitio donde se afirma un 503 y
además está fuera de la cadena de derivación.

**La lección vale más que el arreglo, y quedó escrita en el propio archivo:** un test que mide
tiempos o cuenta llamadas no puede dejar su transporte a la autodetección por classpath. Una
dependencia nueva, en otra capa, se lo cambia sin avisar. **Y esto es una trampa activa para el
Lab 12**, cuyo circuit breaker devuelve 503.

---

## 4 · Preservación pedagógica · los dos demos que de verdad usaban Docker

Es lo que la SPEC §4 pedía demostrar, y es lo único que un `verify` verde no prueba.

### Lab 08 · los escenarios de resiliencia

`--teso-lento 30000` configura TESO por su API de administración —el mismo `curl` de siempre,
contra el mismo 8089— lanza 12 pagos en paralelo y golpea el listado mientras cuelgan:

```
[INFO]  TESO configurado para tardar 30000 ms en cada confirmación.
[INFO]  Disparando 12 confirmaciones de pago EN PARALELO (cada una cuelga en TESO)…
     GET /tramites -> HTTP 200 en 0.859924 s
[OK]    La API sigue VIVA (respondió rápido) — el timeout liberó los hilos
```

Y los mappings versionados se cargan igual, ahora desde el classpath:

```
GET /__admin/mappings -> HTTP 200      1 mapping(s):  /pagos/.* -> 200
POST /tramites/2/pago -> HTTP 200
```

### Lab 10 · el tablero que no miente, con la base MUERTA de verdad

El `--db-caida` mataba el contenedor. Ahora **anota los PostgreSQL embebidos que había ANTES de
arrancar y mata por PID el que apareció después**, con `SIGKILL`: se quiere una base que muere, no
una que se despide. Nunca `pkill postgres` — eso se llevaría la base del trabajo del alumno y la
de cualquier otro curso.

```
ANTES:    baseDeDatos {"motor":"PostgreSQL","sonda":"SELECT 1","tardanzaMs":11,"status":"UP"}

[INFO]  Tumbando el PostgreSQL embebido de esta app (PID 49810)…
[OK]    PostgreSQL detenido. La aplicación sigue corriendo (PID 49760).

DESPUÉS:  GET /api/v1/tramites            ->  HTTP 500   <- el negocio está caído
          readiness  ->  DOWN · baseDeDatos DOWN · causa "ConnectException: Connection refused"
          liveness   ->  UP
[OK]    El tablero dice la VERDAD: readiness cayó y nombra el componente; liveness sigue UP
```

El mismo test lo hace en la suite (`E1_HealthQueNoMienteIT`): levanta un **motor embebido propio**
—no el compartido, que dejaría sin base a toda la suite— y lo cierra a media prueba. La idea no
cambió ni un milímetro; lo único que ya no hace falta es Docker para matarla.

### Lab 11 · dos servidores, un candado — y una sola base

Este era el más difícil: las instancias 2..N se colgaban de la base del compose. Ahora la
instancia 1 levanta la suya y **Zonky publica el puerto en su propia línea de comandos**
(`-p 49973`), que es de donde se lee; las demás se conectan ahí y no levantan infraestructura
propia — ni base ni TESO, porque el 8089 es fijo y la segunda moría con `Address already in use`.
En el compose también había **un** TESO para todos: se reproduce igual.

```
[OK]    Instancia 1 viva en el puerto 8099
[INFO]  La base de la instancia 1 está en localhost:49973 — la instancia 2 se conecta ahí
[OK]    Instancia 2 viva en el puerto 8100 (misma base que la 1)

  --- El crimen: dos servidores, y los dos se creyeron el único ---
     id=1  instancia=dgt-2  tramites=5  total=14555000
[OK]    El cierre corrió UNA sola vez, con 2 instancias latiendo a la vez
```

Y el crimen sigue siendo visible en el `starter`:

```
[WARN]  El cierre corrió 2 veces en el mismo latido. Los totales están duplicados.
```

**Un obstáculo que hubo que resolver: Zonky NO empaqueta `psql`.** Su `bin/` trae `initdb`,
`pg_ctl` y `postgres`, y nada más — comprobado, no supuesto. Como el guion necesita preguntarle a
la base cuántos cierres hubo, la consulta va por **JDBC**, con las dos únicas cosas que ya viajan
en el repositorio: el JDK embebido y el driver de PostgreSQL, como programa de un solo archivo
(`bin/ConsultaSql.java`). Cero dependencias nuevas.

### ArchUnit

Los ocho proyectos pasan sus guardianes sin que se tocara ninguna regla. `TesoSimuladoDev` vive en
`config/`, que es donde vive `BaseDeDatosEmbebida`, y AU-07 no se inmutó: no hubo que negociar
nada con los guardianes.

---

## 5 · Verificación · N1–N6

Todo lo de abajo se corrió **con red** (etapa 1). El vuelo 5 queda en pista, §9.

### N1 · el starter falla SOLO en lo declarado como hueco

| Lab | Falla en | ¿Algo fuera del enunciado? |
|---|---|---|
| 08 | `E1_TimeoutIT`, `E2_DegradacionEleganteIT`, `E4_EndurecimientoIT` | **no** |
| 09 | `E1_TrazabilidadIT`, `E2_LogJsonIT`, `E3_AuditoriaIT`, `E4_AdjuntosIT` | **no** |
| 10 | `E1_HealthQueNoMiente`, `E2_Metricas`, `E3_Exposicion`, `E4_Cache` | **no** |
| 11 | `E1_ElRelojBienDeclarado`, `E2_CandadoDistribuido`, `E3_AsincroniaVirtual`, `E4_EventoTransaccional` | **no** |

En el Lab 08 el crimen se ve sin Docker con toda claridad: con TESO devolviendo 200 tras tres
segundos, el pago del `starter` **espera y responde 200** donde la solución responde 503. El rehén
sigue siendo visible.

### N2 · la solución, verde

Las cuatro. Cifras y tiempos en la tabla de §2. Y también **OFFLINE**, que es la prueba que
importa:

```
$ ./mvnw -B verify          (lab-08, contra repo-maven, sin red)
  Tests run: 40 · Tests run: 27   BUILD SUCCESS
  descargas intentadas: 0
```

### N3 · ciclo start-lab + destrucción

Los cuatro arrancan, sirven y se desmontan. El `--teso-lento` del Lab 08 y los dos demos del 10 y
el 11 están citados en §4.

### N4 · `90-validar` en los dos estados

```
lab-08  solucion LAB 08 APROBADO (2/2)   starter LAB 08 NO APROBADO (1/2)
lab-09  solucion LAB 09 APROBADO (2/2)   starter LAB 09 NO APROBADO (1/2)
lab-10  solucion LAB 10 APROBADO (2/2)   starter LAB 10 NO APROBADO (1/2)
lab-11  solucion LAB 11 APROBADO (2/2)   starter LAB 11 NO APROBADO (1/2)
```

Los manifiestos de tests se regeneraron: `BaseResilienciaIT` es un test del enunciado, y al
migrarlo el validador dijo *«alguien modificó un test del enunciado»*. **Dijo la verdad** — lo
modificamos nosotros. Comprobado además que el enunciado es idéntico entre `starter` y `solucion`
en los cuatro labs.

### N5 · cero huérfanos, ahora de DOS familias

```
postgres embebidos vivos : 0
LISTEN en 8099           : 0
LISTEN en 8089 (TESO)    : 0
LISTEN en 8100           : 0
```

El `99-destruir.sh` de los cuatro labs pasó de bajar contenedores a **mirar** lo que quedó: los
postgres por su ruta exacta y el 8089 de TESO por su puerto. Se miran, no se matan. Sube de 3/3 a
**4/4 verificaciones** porque TESO añadió la suya.

### N6 · los escenarios de resiliencia

En §4. Los tres estados de TESO —sano, lento y caído— se comportan idéntico.

### Transversales

| | |
|---|---|
| Guard de 95 MB | `[OK] Ningún archivo supera los 95 MB.` |
| `repo-maven/` | 230 MB → **254 MB** (+24 MB: WireMock, Jetty 12.0.16 y sus transitivas) |
| `.git` | 514 MB |
| Derivación | en sincronía **del tronco al Lab 11**; frontera roja en 11→12 |
| shellcheck · `bash -n` | limpios en los 81 scripts |
| Regresión labs 01–07 | labs 01, 05, 06 y 07 en verde. **Cero archivos tocados** en labs 01–07 y en el tronco, verificado con `git diff --name-only` |

---

## 6 · A2.4 · media respuesta, y el sospechoso principal REFUTADO

La SPEC pedía identificar qué se ata fuera de loopback durante los IT. Se midió, vigilando los
sockets en `LISTEN` cada medio segundo mientras corría la suite completa.

**En el Lab 01 —uno de los que el PO corrió en Windows— durante `verify`:**

```
java      127.0.0.1:59663        <- el Tomcat de un IT con RANDOM_PORT
postgres  127.0.0.1:59613
postgres  [::1]:59613
--- de esos, FUERA de loopback: (ninguno) ---
```

**En el Lab 08 ya migrado**, la lista de procesos escuchando fuera de loopback durante toda la
suite no contiene **ni uno solo** del laboratorio: son el Centro de Control, Dropbox, Logitech,
Transmission, Docker Desktop y `rapportd`, todos del PO y ajenos al curso.

### Lo que esto cierra y lo que no

**Cierra:** la hipótesis del Tomcat de `RANDOM_PORT` queda **refutada**. Se ata a `127.0.0.1`, y
la razón es que **los IT corren bajo el perfil `dev`** —`spring.profiles.active` tiene default
`dev`— así que `server.address: localhost` de A2.3 **sí les llega**. La lectura de que «A2.3 no
alcanza a los tests» era razonable y es falsa.

**No cierra:** por qué aparece el cartel en Windows. En macOS no hay nada que atar fuera de
loopback, así que el fenómeno no se reproduce aquí. El sospechoso que queda es el `postgres.exe`
de Zonky, cuya dirección de escucha podría elegirse distinto en Windows.

### Por qué NO se aplicó el candado

La SPEC pedía aplicarlo. Se decidió **no hacerlo**, y por dos razones que conviene tener escritas:

1. **No se toca a ciegas lo que no se puede verificar aquí.** Es el mismo criterio con el que la
   A2.2 dejó en suspenso el pariente del `pgrep`, y acertó.
2. **El candado dejaría el material asimétrico.** Fijar `listen_addresses` de Zonky se hace en
   `PostgresEmbebido` y `BaseDeDatosEmbebida`, dos archivos que **comparten el tronco y los labs
   01 a 11**. Aplicarlo solo a los migrados los haría divergir de su base sin motivo pedagógico;
   aplicarlo a los dieciséis proyectos es tocar todo el material por una hipótesis no confirmada.

**Lo que falta es una sola pregunta, de un minuto, en la re-prueba Windows del PO: qué ejecutable
nombra el cartel.** Si dice `postgres.exe`, el candado es de una línea y se aplica uniformemente
en una SPEC-FIX. Si dice `java.exe`, hay que volver a mirar, porque aquí el java se ata a
loopback.

**Lo que sí mejoró sin proponérselo:** el `compose.yaml` publicaba TESO en todas las interfaces.
Al pasar a in-process, TESO se ata a `127.0.0.1`. Sea cual sea la causa del cartel, **esta SPEC
quita superficie en vez de añadirla**.

---

## 7 · A3.1 · el JVM que Maven bifurca no usa el JDK embebido

**No es de esta SPEC, y es más grave que A2.4.** Apareció al intentar el ciclo `start-lab` del
Lab 08 y se declara aquí porque toca el corazón de lo que la SPEC-024 prometió.

### El síntoma

```
$ ./bin/start-lab.sh --dir solucion
[ERROR] La DGT no respondió en 120 segundos

$ tail .estado/dgt.log
UnsupportedClassVersionError: ... has been compiled by a more recent version of the Java
Runtime (class file version 69.0), this version of the Java Runtime only recognizes class
file versions up to 65.0
```

Class file 69 es Java 25; 65 es Java 21. Las clases se compilan con el JDK embebido y **el JVM que
Maven bifurca arranca con el Java de la máquina**. Pasa igual con `surefire` y `failsafe`: los
labs 05, 06 y 07 mueren en `verify` con el mismo error.

### Que NO es de la SPEC-025, medido

Se reproduce **idéntico en el Lab 07 de `main`**, que esta SPEC no tocó ni un byte. Y desaparece
en cuanto `JAVA_HOME` apunta al JDK embebido:

```
JAVA_HOME=<el de la máquina, un Java 21>   ->  labs 05/06/07: BUILD FAILURE
JAVA_HOME=<tools/jdk/runtime/.../Home>     ->  labs 05/06/07: BUILD SUCCESS (10, 11 y 22 tests)
```

### Lo que NO se pudo cerrar, y se declara

El mecanismo. Hay una observación que no encaja y que no conviene esconder: `./mvnw -version`
informa `Java version: 25.0.4 … runtime: tools/jdk/runtime/…` —o sea, Maven corriendo sobre el JDK
embebido— mientras que `./mvnw -X -o validate`, en la misma carpeta y en el mismo shell, muestra
un banner que dice `Java version: 21.0.1, vendor: GraalVM Community`. Se comprobó que el shim
exporta bien: un hijo suyo ve `JAVA_HOME` y el `java` del PATH apuntando al JDK embebido. No hay
`~/.mavenrc`, ni `MAVEN_OPTS`, ni `.mvn/jvm.config`, y el `toolchains.xml` es la plantilla vacía de
Maven. **No tengo explicación, y prefiero decirlo a inventarla.**

**Y hay una contradicción con el registro:** el vuelo 4 de la SPEC-024 corrió con un `JAVA_HOME`
hostil apuntando a ese mismo GraalVM 21 y dio los siete labs en verde. Hoy, con esa misma
configuración, fallan. **Resolver esa contradicción es lo primero**, porque una de las dos
evidencias está mal y necesitamos saber cuál.

### Qué se hizo mientras tanto

Todas las verificaciones de esta SPEC que arrancan un JVM se corrieron con `JAVA_HOME` apuntando
al **JDK embebido**, y queda declarado aquí en vez de escondido en una nota. El vuelo 5 no lo
asume en ningún sentido: **lo mide en una sonda, antes de despegar** (§9).

---

## 8 · Estado del CI

Corrido sobre la rama, **no supuesto**. Siete de ocho en verde:

| Job | Resultado | |
|---|---|---|
| `app · dgt-tramites-api (verify)` | ✅ | el tronco no se tocó |
| `deriva · labs en sincronía con su base` | ❌ **justificado** | ver abajo |
| `grpc · la demo del Lab 08 compila y responde` | ✅ | `demo-grpc/` no se tocó |
| `lab14 · el sistema de microservicios` | ✅ | prohibido tocarlo, y no se tocó |
| `labs-sh · andamiaje (ubuntu-latest)` | ✅ | shellcheck limpio en los 81 scripts |
| `labs-sh · andamiaje (windows-latest)` | ✅ | ídem |
| `siembra · toda TEORIA.md con sucesor` | ✅ | no se tocó teoría |
| `temario · coherencia .md ↔ .docx` | ✅ | no se tocó el temario |

El job `deriva` cuenta ahora **26 eslabones en sincronía** —eran 17 antes de esta SPEC— y falla en
uno solo. El propio log dice cuál:

```
[ERROR] 20 archivo(s) divergieron sin declararse.
        Si el cambio es intencional, decláralo en labs/lab-12-amortiguadores/derivacion-solucion.txt
[ERROR] 1 eslabon(es) con deriva silenciosa.
```

**El rojo de `deriva` es el mismo de siempre, y se movió a donde tenía que moverse.** Venía del PR
#27 porque el Lab 08 iba atrasado respecto del 07. Ahora la cadena está en sincronía **del tronco
al Lab 11**, y falla en la frontera **11→12** porque el Lab 12 no está migrado. Es el guard
diciendo la verdad, y la SPEC §0 lo autoriza expresamente: se apaga migrando el 12, no declarando
divergencias que no lo son.

Divergencias declaradas nuevas: **una sola**, el `pom.xml` del Lab 08 —que es la convención ya
usada por los labs 09, 10 y 12— con su porqué escrito al lado.

---

## 9 · Vuelo 5 — en pista, sin lanzar

`tools/vuelo-5-modo-avion.sh`. Mismo protocolo probado: espera el corte, vigila recontaminación,
caja negra con timestamps, veredicto, restaura `~/.m2` y **no relanza Docker**.

Cubre: la **sonda A3.1** antes de despegar (§7), el Lab 06 como regresión, N1–N5 de los cuatro
labs migrados, **los dos demos en vivo** —el `--db-caida` del 10 y el `--instancias 2` del 11, en
sus dos caras— y el simulacro del alumno con las cachés frías.

**Duración estimada: ~30 minutos**, con los tiempos ya medidos de cada pieza.

```bash
nohup tools/vuelo-5-modo-avion.sh > /tmp/vuelo5.out 2>&1 &
```

---

## 10 · Deudas heredadas que esta SPEC salda, y lo que queda

### Saldado

**FIX-05 en ejecución real (labs 08–11).** Los `[OK]` corregidos de esos scripts estaban en deuda
—solo se habían linteado, porque correrlos necesitaba Docker—. Ahora corrieron enteros: los ciclos
completos de §5 (N3, N4, N5) los ejercitan, y el `[OK] API detenida` del `99-destruir.sh` dice la
verdad en los cuatro. La deuda de los labs **12 a 14 sigue declarada**.

### Lo que queda

1. **A3.1** (§7) — la más urgente, porque es preexistente y toca la promesa de la SPEC-024.
   Empezar por la contradicción con el vuelo 4.
2. **A2.4** (§6) — media respuesta; falta la pregunta de un minuto en Windows.
3. **Lab 12** — RabbitMQ amputado. Su circuit breaker devuelve 503: **cuidado con D-025-3**, la
   trampa del transporte que reintenta.
4. **Lab 13** — Jib, y qué significa un lab de contenedores en un curso sin Docker. Decisión del
   Arquitecto antes que técnica.
5. **Lab 14** — congelado, sin módulo contractual.
6. **Rediseño de los `TODO_1`/`TODO_2`** del perfil `dev`, aún marcados `PROVISORIO`.
7. **Guion de fin de curso** con la limpieza de la caché de Zonky.
8. **Una observación menor**, sin arreglar por no tocar un demo que funciona: en el
   `--instancias 2` del Lab 11, cuando el cierre corre dos veces, el script anuncia «corrió 2
   veces» pero alcanza a listar una sola fila — la segunda aterriza después de la consulta. El
   número es correcto; el listado va un instante por detrás.
9. **Un comentario obsoleto que se propaga:** varios `ContratoRn03IT` conservan un párrafo de
   javadoc sobre «Testcontainers 2.x» heredado desde el Lab 01. Es falso desde la SPEC-022.
   Corregirlo obliga a tocar los labs 01–11 a la vez para no romper la derivación, así que se
   anota para la SPEC de pulido.

---

## 11 · Cierre

Cuatro labs migrados, ocho proyectos, 779 archivos y siete commits. Del Lab 00 al Lab 11, el curso
corre sin Docker, sin administrador y sin red. Los dos demos que hacían de Docker un requisito
—matar una base y levantar dos servidores contra una sola— siguen funcionando, y siguen
enseñando lo mismo.

Queda una anotación abierta con media respuesta (A2.4) y una nueva que no es nuestra pero es peor
(A3.1).

En posición de merge y tag `material-v0.5.0` cuando el PO lo autorice, y con el vuelo 5 esperando
en pista.
