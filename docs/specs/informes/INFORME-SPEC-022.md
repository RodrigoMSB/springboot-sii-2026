# INFORME-SPEC-022 · El material autocontenido — modo avión

**SPEC:** SPEC-022 · **Ejecuta:** mocito · **Fecha:** 12–13 de agosto de 2026
**Rama:** `spec-022-material-autocontenido`
**Máquina de ejecución:** Mac Studio del PO — Darwin 25.5.0, `arm64` (Apple Silicon)
**Sustituye la estrategia de:** SPEC-021 (ver `INFORME-SPEC-021.md`)

---

## 1 · Veredicto en una línea

**MATERIAL AUTOCONTENIDO VIABLE** — el Lab 01 corre entero, suite completa verde, con el
cable de red desenchufado y sin Docker. El Lab 02 está verde en modo `--offline` y su
verificación en avión (V6) se traslada al vuelo 3 por no haber despegado el vuelo 2.

---

## 2 · Precondiciones

Las tres, verificadas antes de tocar nada.

**P0.1 · Java 25** — ✅

```
$ sdk env && java -version
openjdk version "25" 2025-09-16 LTS
OpenJDK Runtime Environment Temurin-25+36 (build 25+36-LTS)
OpenJDK 64-Bit Server VM Temurin-25+36 (build 25+36-LTS, mixed mode, sharing)
```

Nota heredada de la SPEC-021: sin `sdk env` la sesión entrega **Java 21 (GraalVM)**. Todo
lo que sigue se ejecutó con `JAVA_HOME` apuntando explícitamente a `25-tem`.

**P0.2 · Internet para la captura** — ✅ `curl` a Maven Central devolvió `200`. La captura
se hace de este lado del muro y el alumno no la repite jamás.

**P0.3 · Docker para V9** — ⚠️ **Disponible al empezar (28.5.1) pero no al medir.** Ver §6:
V9 quedó parcialmente SKIP y está explicado ahí.

---

## 3 · Trabajo · Parte A — Migración Zonky del Lab 01

Aplicado a `starter/` **y** `solucion/`.

| Archivo | Qué cambió | Por qué |
|---|---|---|
| `pom.xml` | Fuera `spring-boot-docker-compose`, `spring-boot-testcontainers`, `testcontainers-postgresql`, `testcontainers-junit-jupiter`. Entra `embedded-postgres` **2.2.2** (A1.5), el BOM `embedded-postgres-binaries-bom` **16.14.0** y los binarios `windows-amd64` + `darwin-arm64v8` | Docker sale del lab. Los dos binarios porque el alumno va en Windows y el material se prepara en Apple Silicon |
| `config/BaseDeDatosEmbebida.java` | **Nueva.** `@Configuration @Profile("dev")` que publica `EmbeddedPostgres` y su `DataSource` | Reemplaza al `compose.yaml`. El alumno sigue sin escribir una cadena de conexión — el punto pedagógico intacto |
| `compose.yaml` | **Eliminado** | Ya no hay Docker que orquestar |
| `application-dev.yml` | La NOTA DEL PECADO ORIGINAL migra completa (A1.7), con una coda nueva | Ver abajo |
| `application-test.yml`, `application-prod.yml` | `spring.docker.compose.enabled` → `dgt.base-embebida.enabled` (test) y bloque eliminado (prod). Marcados `PROVISORIO SPEC-022` (A1.6) | Eran propiedades muertas: el módulo que las leía ya no está |
| `SemillaCoherenteIT`, `ContratoRn03IT` (×2) | `@TestConfiguration`+`PostgreSQLContainer`+`@ServiceConnection` → singleton `PostgresEmbebido` + `@DynamicPropertySource` | Espejo del patrón que el Lab 12 ya usa y explica |
| `PostgresEmbebido.java` | **Nueva** (test). Singleton por JVM | Una base por JVM: el contexto de Spring se cachea entre clases y una base por-clase dejaría al contexto apuntando a un puerto muerto |
| `bin/start-lab.sh` | Guard `docker info` eliminado | Un lab sin Docker que exige Docker Desktop es un bug del material |
| `bin/99-destruir.sh` | Toda la lógica `docker compose down -v` → verificación de procesos | Ver la nota sobre matar procesos, abajo |

**Scope de la dependencia, decisión documentada en el propio pom:** `embedded-postgres` va
en scope **compile + `optional`**, no `test`. La clase que arranca la base vive en `src/main`
(es el reemplazo del compose para el perfil `dev`), así que hay que compilar contra Zonky.
`optional` evita que se propague a cualquiera que dependa del artefacto: es andamiaje de
desarrollo, no parte de la API.

**Dónde quedó la nota pedagógica (A1.7):** la «NOTA DEL PECADO ORIGINAL» migró íntegra de
`compose.yaml` a `application-dev.yml`, donde el alumno la sigue leyendo. Se le añadió una
coda honesta, porque el mundo cambió debajo de ella:

> *Nota de 2026: hoy este perfil ya no tiene NINGUNA contraseña que versionar, ni buena ni
> mala. La base embebida no la necesita. El pecado original se quedó sin cuerpo del delito —
> pero la distinción que enseñaba sigue siendo la que separa el Lab 01 de un incidente de
> seguridad real.*

**Cómo se apagan los procesos (ley post-ALCHEMIA).** El PostgreSQL de Zonky es proceso hijo
del JVM y muere con él cuando el bean se cierra. `99-destruir.sh` **verifica** que se fue; si
alguno sobrevive, lo **reporta sin matarlo**:

```bash
HUERFANOS="$(pgrep -f 'embedded-pg/PG-.*/bin/postgres' 2>/dev/null | wc -l | tr -d ' ')"
...
    log_info "No los mato por nombre: podrían ser de otro proyecto tuyo."
```

Un `pkill postgres` mataría la base del trabajo del alumno. Se miran, no se matan — la misma
regla que ya se aplicaba a los contenedores ajenos.

**Las 7 reglas ArchUnit siguen mordiendo:** `ArquitecturaTest` 8/8 y
`MordidaDeLosGuardianesTest` 7/7 en verde. La clase nueva vive en `config`, que ninguna regla
prohíbe. No hubo que debilitar nada.

---

## 4 · Trabajo · Parte B — `repo-maven/` y el shim

**`tools/maven/`** — Apache Maven 3.9.11, 10 MB, commiteado. Procedencia: la caché del
wrapper (`~/.m2/wrapper/dists/`), que a su vez vino de `repo.maven.apache.org` según el
`distributionUrl` de los wrappers.

**El shim (D-022-2).** `mvnw` y `mvnw.cmd` dejan de descargar:

```bash
RAIZ="$DIR_PROYECTO"
while [ ! -x "$RAIZ/tools/maven/bin/mvn" ]; do
    PADRE="$(dirname "$RAIZ")"
    [ "$PADRE" = "$RAIZ" ] && { printf '[ERROR] No encuentro tools/maven/...'; exit 1; }
    RAIZ="$PADRE"
done
[ "${DGT_ONLINE:-0}" = "1" ] && exec "$MVN" "$@"
exec "$MVN" --offline "-Dmaven.repo.local=$RAIZ/repo-maven" "$@"
```

Marcador elegido: `tools/maven/bin/mvn` en vez de `repo-maven/`. Es lo que el shim necesita
para ejecutar, y existe siempre — incluido el momento de la captura, cuando `repo-maven/`
todavía no está. Sin git, sin rutas absolutas, sin variables de entorno.

**Captura, por ejecución real.** Con `DGT_ONLINE=1` y `-Dmaven.repo.local=<raíz>/repo-maven`:
`verify` en los cuatro proyectos, `spring-boot:run` con arranque y apagado limpio (para
capturar los plugins de runtime que usan los `bin/`), y los goals sueltos que invocan los
scripts (`dependency:tree`, `help:effective-settings`, `test-compile`).

**Zonky, presencia física verificada** (§5.2 de la SPEC pedía citar rutas):

```
repo-maven/io/zonky/test/embedded-postgres/2.2.2/embedded-postgres-2.2.2.jar
repo-maven/io/zonky/test/postgres/embedded-postgres-binaries-bom/16.14.0/...-bom-16.14.0.pom
repo-maven/io/zonky/test/postgres/embedded-postgres-binaries-windows-amd64/16.14.0/...jar
repo-maven/io/zonky/test/postgres/embedded-postgres-binaries-darwin-arm64v8/16.14.0/...jar
repo-maven/io/zonky/test/postgres/embedded-postgres-binaries-darwin-amd64/16.14.0/...jar
repo-maven/io/zonky/test/postgres/embedded-postgres-binaries-linux-amd64/16.14.0/...jar
repo-maven/io/zonky/test/postgres/embedded-postgres-binaries-linux-amd64-alpine/16.14.0/...jar
```

El alumno de Windows resuelve desde el mismo repositorio, sin tocar la red. **Regalo no
pedido:** cayeron también los binarios de Linux e Intel Mac (los arrastra
`embedded-postgres` por defecto). 56 MB extra a cambio de que el material funcione en
cualquier plataforma que aparezca. Se dejan.

**Higiene (§5.3).** Se borraron **558** `_remote.repositories` y 0 `*.lastUpdated` (la captura
salió limpia). No es cosmética, es lo que hace posible el modo offline: ese archivo marca de
qué repositorio vino cada artefacto, y Maven **se niega a usar la caché** cuando el
repositorio de origen no está disponible con ese mismo id. Es exactamente el muro contra el
que chocó la SPEC-021:

```
[INFO] Artifact ... is present in the local repository, but cached from a remote repository ID
       that is unavailable in current build context, verifying that is downloadable from [...]
```

Se conservan los `.sha1`: son la verificación de integridad y pesan nada.

**Dos trampas encontradas que habrían roto el clon del alumno:**

1. **`.gitignore` tenía `*.jar`.** Habría vaciado `repo-maven/` entera: el alumno clonaría un
   esqueleto sin una sola dependencia. Se añadieron excepciones `!repo-maven/**` y
   `!tools/maven/**`. Verificado: **262 jars efectivamente en el índice** en el momento del
   primer commit (274 tras el Lab 02).
2. **`.gitattributes` tenía `* text=auto eol=lf`.** Habría normalizado los finales de línea de
   cada `.pom`, cambiando su contenido y **rompiendo su `.sha1`** — fallo de integridad en la
   máquina del alumno, sin red para recuperarse. Se añadieron `repo-maven/** binary` y
   `tools/maven/** binary`, **al final del archivo** porque en `.gitattributes` gana la última
   coincidencia.

**Guard de 95 MB (D-022-5).** `tools/verificar-tamanos.sh`, en verde. Ver §6/V8.

---

## 5 · Trabajo · Parte C — La réplica en el Lab 02

**Tiempo de la réplica: ~3 minutos** de reloj, de la primera inspección al `verify` verde.
El dato pedido para dimensionar la Fase 1 — con la salvedad honesta de que mide **ejecución
mecánica**, no el criterio para decidir qué se toca.

Qué se pudo copiar tal cual (la derivación lo exige idéntico a `lab-01/solucion`): `mvnw`,
`mvnw.cmd`, `BaseDeDatosEmbebida`, `PostgresEmbebido`, los tres `application-*.yml`,
`SemillaCoherenteIT` y `ContratoRn03IT`.

**Qué difirió del Lab 01 — cuatro cosas, y son las que hay que buscar en cada lab de la Fase 1:**

1. **El pom no era idéntico.** El Lab 02 añade `springdoc-openapi-starter-webmvc-ui` 3.0.3.
   Se partió del pom nuevo del Lab 01 y se le devolvió esa dependencia.
2. **Dos IT propios del enunciado con su propio contenedor.** `T1_FichaSinDatosSensiblesIT` y
   `T4_ContratoOpenApiIT` llevaban su bloque `@TestConfiguration` + `PostgreSQLContainer`.
   Migrados al mismo singleton.
3. **Por eso hubo que regenerar `manifiesto-tests.sha256`** — T1 y T4 cambiaron de hash; T2 y
   T3 conservan el suyo, verificable en el diff.
4. **`90-validar.sh` tenía un gate de Docker propio** que abortaba la validación entera, más
   una cabecera que decía *«Este lab NECESITA DOCKER»*. Ambos fuera. El Lab 01 no tenía nada
   de esto: **cada lab puede traer su propia sorpresa en `bin/`**, y hay que leerlos uno a uno.

Derivación y manifiesto, verificados tras la conversión:

```
[OK] solucion en sincronía con su base (solucion)   ← lab-01/solucion → lab-02/solucion
[OK] starter en sincronía con su base (solucion)    ← lab-02/solucion → lab-02/starter
manifiesto starter EXIT=0 · manifiesto solucion EXIT=0
```

---

## 6 · Tabla de verificación

Las V1–V5 se corrieron sobre **clon fresco** (`git clone` a un directorio limpio), con
**`~/.m2` renombrado** y con la **red físicamente desconectada** — cable de Ethernet fuera y
Wi-Fi apagado por el PO — y Docker detenido. Caja negra completa con timestamps en
`/tmp/caja-negra-spec022.log`.

### Evidencia de aislamiento, citada antes de empezar

```
--- ping -c1 github.com ---     ping: cannot resolve github.com: Unknown host   (exit=68)
--- curl repo1.maven.org ---    curl: (6) Could not resolve host    http=000    (exit=6)
--- IPs no-loopback ---         (ninguna IP externa)
--- ruta por defecto ---        (sin ruta por defecto)
--- ~/.m2 ---                   no existe: apartado
--- docker ---                  no responde
```

Al aterrizar: `red aun cortada`. **Ningún tramo del vuelo quedó contaminado.**

### Resultados

| # | Prueba | Resultado | Salida citada |
|---|---|---|---|
| **V1** | Lab 01 `starter` · `./mvnw test` | ✅ | `EXIT=1 · 8s` · `Tests run: 46, Failures: 5, Errors: 2` · `fallos fuera de enunciado/: 0` · `descargas intentadas: 0` |
| **V2** | Lab 01 `solucion` · `./mvnw verify` | ✅ | `EXIT=0 · 13s` · `Tests run: 46, Failures: 0` + `Tests run: 7, Failures: 0` · `BUILD SUCCESS` · `descargas: 0` |
| **V3** | `start-lab.sh` + curls + `99-destruir.sh` | ✅ | `start-lab EXIT=0 · 5s` · `curls con 200: 3/3` · `99-destruir EXIT=0` |
| **V4** | `90-validar.sh` en ambos estados | ✅ | starter `4/5 · LAB 01 NO APROBADO` (único fallo: *«Faltan TODOs por resolver»*) · solución `5/5 · 🏆 LAB 01 APROBADO` |
| **V5** | `ps` por ruta exacta tras V3 | ✅ | antes: `app: 1 · postgres: 1` → después: `app: 0 · postgres: 0` |
| **V6** | Lab 02 en modo avión | ⏭️ DIFERIDA | El vuelo 2 quedó en pista y nunca despegó: no hubo corte de red esa noche. Se traslada al **vuelo 3** (SPEC-023 §6). Verde con red disponible pero `--offline`: `42 + 11 tests · BUILD SUCCESS · descargas: 0` |
| **V7** | `java -version` junto a V1 | ✅ | `openjdk version "25" · Temurin-25+36`, citado dentro del vuelo |
| **V8** | Tamaños + guard 95 MB | ✅ | Ver abajo |
| **V9** | Comparativa de tiempos | ⚠️ SKIP parcial | Ver §7 |
| **V10** | Simulacro del alumno | ⏭️ DIFERIDA | Ídem: al vuelo 3, como V10-bis |

**Sobre V1 y el criterio «Verde»:** la tabla de la SPEC pide *«Verde»* para el `starter`, y eso
no es alcanzable — un starter con los TODO sin resolver **debe** fallar sus tests del
enunciado; es el ejercicio. El criterio que sí se puede verificar, y se verificó, es que fallen
**solo** los de `enunciado/`: `fallos fuera de enunciado/: 0`. Discrepancia de redacción, no de
material.

**Los tres `curl` de V3**, con la app viva sin red y sin Docker:

```
api/contribuyentes/11111111-1 -> {"rut":"11111111-1","razonSocial":"Valentina Rojas"}
api/tramites/1                -> {"id":1,"tipo":"DECLARACION_F29","estado":"BORRADOR",...}
actuator/health               -> "db":{"details":{"database":"PostgreSQL",...},"status":"UP"}
```

Ese `"database":"PostgreSQL"` en el health es la firma que importa: **no es un H2 disfrazado**.
El log de arranque lo confirma: `starting PostgreSQL 16.14 on x86_64-apple-darwin24.6.0`.

### V8 · Tamaños

```
224M    repo-maven      (1 784 archivos · 274 jars)
 10M    tools/maven
225M    .git
518M    clon completo, tal como le llega al alumno
```

Top-10 (salida del guard):

| MB | Artefacto |
|---:|---|
| 28,7 | `embedded-postgres-binaries-darwin-amd64-16.14.0.jar` |
| 28,7 | `embedded-postgres-binaries-darwin-arm64v8-16.14.0.jar` |
| 21,9 | `embedded-postgres-binaries-windows-amd64-16.14.0.jar` |
| 14,6 | `hibernate-core-7.4.1.Final.jar` |
| 14,1 | `embedded-postgres-binaries-linux-amd64-16.14.0.jar` |
| 13,8 | `embedded-postgres-binaries-linux-amd64-alpine-16.14.0.jar` |
| 7,1 | `zstd-jni-1.5.7-6.jar` |
| 4,4 | `byte-buddy-1.18.10.jar` |
| 4,3 | `archunit-1.4.2.jar` |
| 3,4 | `tomcat-embed-core-11.0.22.jar` |

```
[OK] Ningún archivo supera los 95 MB.
```

Con dos labs convertidos vamos en 224 MB. La estimación del PO (400–700 MB para el curso
entero) se sostiene: los labs que faltan comparten casi todas las dependencias, y lo que se
sume serán artefactos nuevos concretos (WireMock, Spring Cloud, Resilience4j) y no otra copia
de lo mismo.

---

## 7 · Comparativa de tiempos

**Lado Zonky, medido:**

| Escenario | Tiempo |
|---|---|
| Lab 01 `solucion` · `verify` · **modo avión, clon fresco** | **13 s** |
| Lab 01 `starter` · `test` · modo avión | 8 s |
| Lab 01 · `start-lab.sh` hasta app viva · modo avión | 5 s |
| Lab 01 `solucion` · `verify` · offline con caché caliente | 14,7 s |
| Lab 02 `solucion` · `verify` · offline | 14,6 s |
| Lab 01 `solucion` · `verify` · **captura online** (poblando `repo-maven` desde Central) | 25,3 s |

**Lado Testcontainers: SKIP.** El demonio de Docker no volvió a levantar tras detenerlo para
el vuelo — `docker info` devuelve solo la sección `Client`, sin `Server`, y el script de
medición esperó su ciclo completo sin obtener versión de servidor. Se relanzó Docker Desktop
dos veces. Sin daemon no hay línea base que medir, y P0.3 prevé exactamente este caso.

Lo que sí se puede afirmar sin medirlo, porque no es una cuestión de segundos: en las máquinas
del SII el lado Testcontainers **no tiene tiempo**, tiene un error. No hay demonio que arrancar
ni permisos para instalarlo. La comparativa honesta no es «13 s contra N s», es **«13 s contra
no se puede»**.

---

## 8 · Sorpresas y desviaciones

**8.1 · Zonky funciona con Java 25 y Spring Boot 4.1.0.** Era la incógnita de fondo heredada
de la SPEC-021 y se responde con evidencia: `Detected a Darwin aarch64 system`, Flyway aplicando
sus dos migraciones y `PostgreSQL 16.14` sirviendo. Ni un parche, ni una exclusión, ni un flag.

**8.2 · El primer intento de modo avión fue evidencia falsa, y se descartó.** Apagué el Wi-Fi
(`en1`) y la máquina siguió conectada: el Mac Studio sale por **Ethernet** (`en0`). El
`ping github.com` respondió en 52 ms. La corrida se tiró a la basura entera.

**8.3 · El segundo intento —`sandbox-exec`— produjo fallos que NO eran del material.** El
sandbox de macOS deniega al JVM hasta la conexión a `127.0.0.1`, mientras que a `python3` se la
permite. Reproducido con un programa de cinco líneas:

```
java loopback: java.net.SocketException: Operation not permitted
```

V2 y V3 «fallaron» por eso. Se descartó el método completo. **Lección registrada:** una
herramienta de aislamiento que altera el comportamiento del sujeto medido no aísla, contamina.

**8.4 · Regla nueva de la casa, a petición del PO: el ejecutor jamás corre `sudo`.** Bajar las
interfaces requería privilegios; en vez de buscar rodeos, el PO desconectó el cable con la
mano. Se adoptó además un **protocolo de vuelo autónomo**: el script espera en pista hasta
detectar que `ping` falla, corre solo, y vigila entre tramos que la red no haya vuelto (si
vuelve, marca el resto como no-evidencia). Nada pide input durante el vuelo.

**8.5 · Bug preexistente destapado en `borrar_seguro` (`labs/lib/lib-comunes.sh:199`).**
Durante V3, con el clon en `/tmp`:

```
[ERROR] borrar_seguro: /tmp/dgt-modo-avion/.../.estado cae fuera del repo (/private/tmp/dgt-modo-avion) — abortado
[OK]    Archivos temporales del lab borrados        ← pero no los borró
  3/3 verificaciones · Todo quedó como estaba       ← tampoco
```

Causa: en macOS `/tmp` es symlink a `/private/tmp`. `raiz_repo` devuelve la ruta resuelta y la
ruta a borrar llega sin resolver, así que no coinciden y el guard aborta — **correctamente**,
es su trabajo dudar. El defecto son las dos líneas siguientes: el script **declara `[OK]` y
`3/3` sobre un borrado que no ocurrió**.

No afecta al alumno (clona en su carpeta de usuario, sin symlinks). **No se tocó**:
`lib-comunes.sh` lo comparten los catorce labs y §10 deja los labs 03–14 fuera de alcance.
Queda como **SPEC-FIX-05** por decisión del PO. Arreglo propuesto: resolver ambas rutas con
`pwd -P` antes de comparar, y que el `[OK]` dependa del código de retorno de `borrar_seguro`
en vez de darse por hecho.

**8.6 · La ruta relativa de `.mvn/maven.config` (A1.8, ahora derogada) resolvía contra el
propio archivo, no contra el directorio de trabajo.** Hacían falta **cinco** `../`, no tres.
Se midió antes de derogarla y queda anotado por si la Fase 1 vuelve a considerar ese
mecanismo.

**8.7 · El comentario sobre coordenadas de Testcontainers 2.x se reescribió.** `ContratoRn03IT`
tenía un párrafo pedagógico sobre el renombre `org.testcontainers:postgresql`. La SPEC-021 §5.3
mandaba no tocar los comentarios pedagógicos — pero ese explicaba una dependencia que ya no
existe en el lab, y un comentario que miente es peor que uno ausente. Se sustituyó por la nota
equivalente sobre la base embebida. Los `@DisplayName` y los mensajes `as(...)` **intactos**.

**8.8 · El `settings-sii.xml` rescatado describía el mecanismo derogado.** Su encabezado decía
que el Lab 01 lo aplica solo vía `.mvn/maven.config`. Reescrito para su papel real de plan B,
incluyendo por qué `apus` dejó de estar en el camino crítico. Sigue sin credenciales
(verificado: `grep 'password' → 0`).

---

## 9 · Lo que queda para la Fase 1

**V6 y V10 se trasladan al vuelo 3** (SPEC-023 §6). El script del vuelo 2 quedó en pista
esperando el corte de red y nunca despegó — la noche avanzó y el cable siguió puesto. No es un
fallo del material: ambas pruebas están verdes con red disponible y `--offline`, con cero
descargas; lo que falta es la evidencia de red apagada que §10 exige. Como la SPEC-023 pide un
vuelo 3 de todos modos, se agrupan ahí en vez de pedir dos cortes de cable al PO.

**El grueso — los labs 03 a 14**, con la receta ya probada. Lo que la réplica del Lab 02
enseña sobre el costo real: la parte mecánica es rápida, y el trabajo de verdad es leer el
`bin/` de cada lab (cada uno trae su propia sorpresa de Docker) y decidir sobre sus tests del
enunciado, que arrastran manifiesto.

**Los diferidos que ya están anotados:**

1. **Rediseño de los `TODO_1`/`TODO_2` del perfil `dev`** (A1.6). Hoy están adaptados al
   mínimo y marcados `PROVISORIO SPEC-022`. Es contenido evaluado del curso: decisión del
   Arquitecto.
2. **WireMock in-process** del Lab 08 en adelante — hoy es un contenedor.
3. **Jib** en el Lab 13, y qué hacer con un lab de contenedores en un curso sin Docker.
4. **Manifiestos globales y reconciliación de la derivación** completa.
5. **V9 pendiente**: la línea base de Testcontainers, cuando el demonio de Docker vuelva.
6. **SPEC-FIX-05**: el `borrar_seguro` de §8.5.
7. **Los 30 `maven-wrapper.properties`** siguen apuntando a `repo.maven.apache.org`. Ya no
   los lee nadie en los labs 01 y 02 (el shim los ignora), pero son 28 archivos que dicen algo
   falso. Limpieza cuando el shim llegue a todos los labs.
8. **Verificar el shim en Git Bash sobre Windows**, en sala. La lógica es POSIX simple y
   `mvnw.cmd` tiene su equivalente para `cmd.exe`, pero no está probado en la plataforma real.
