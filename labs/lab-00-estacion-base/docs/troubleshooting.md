# Troubleshooting · Lab 00

Cada fila tiene número. **Cítalo cuando pidas ayuda** (`"me pasa la T-03"`): nos ahorramos
veinte mensajes averiguando de qué hablas.

| # | Síntoma | Qué pasa de verdad | Qué haces |
|---|---|---|---|
| **T-01** | `El puerto 8099 ya está ocupado por: java (PID 1234)` | Algo escucha en el puerto del curso. Es raro —por eso elegimos el 8099 y no el 8080, que ocupa medio mundo— pero puede pasar. | No mates el proceso a ciegas: puede ser tu trabajo. Levanta la DGT en otro puerto: `./bin/start-lab.sh --puerto 8100`. Todo funciona igual; solo cambia el número en tus `curl`. |
| **T-02** | `Java 21 es muy antiguo; el curso pide 25` | Tienes otra versión de Java primero en el `PATH`. Muy común si hiciste otro curso este año. | Instala Temurin 25 (guía 01) y **comprueba** con `java -version`. Si usas `sdkman`: `sdk use java 25-tem`. Si usas `nvm`-style para Java en Windows, ver `entorno-alumno.md`. |
| **T-03** | `docker está instalado, pero su demonio NO responde` | Docker Desktop está cerrado. Es el fallo nº 1 del primer día. | Abre Docker Desktop y **espera** a que el icono de la ballena deje de animarse. Luego repite `./bin/00-verificar.sh`. |
| **T-04** | `No encuentro ni 'docker' ni 'podman'` | No está instalado, o tu institución no lo autoriza. | Si puedes instalarlo: guía 01. Si **no** te dejan: `./bin/00-verificar.sh --sin-docker` y avisa al instructor. No estás fuera del curso; hay un plan para ti. |
| **T-05** | `No llego a Maven Central` / `No llego a Docker Hub` | Un proxy o firewall corporativo bloquea la salida. | Pide a tu área de TI el host y puerto del proxy y configúralo en `~/.m2/settings.xml`. **Empieza esta gestión el martes**, no el viernes: suele tardar días. |
| **T-06** | `La DGT no respondió en 120 segundos` | El arranque murió, casi siempre por la base de datos. | `tail -n 40 labs/lab-00-estacion-base/.estado/dgt.log`. Si dice `port is already allocated`, tienes otro PostgreSQL ocupando el puerto: bájalo, o mira T-07. |
| **T-07** | `docker compose up ... port is already allocated` | Ya tienes un PostgreSQL escuchando. | El `compose.yaml` del curso publica un **puerto efímero** justo para evitar esto. Si aun así choca, comprueba qué contenedores corren: `docker ps`. No apagues los que no sean tuyos. |
| **T-08** | `Solo 4 GB libres; el curso necesita ~10 GB` | Entre imágenes de Docker y `~/.m2`, el curso ocupa varios gigas. | Libera espacio. `docker system prune -a` borra imágenes que no usas (¡ojo: **todas**, no solo las del curso). |
| **T-09** | `./mvnw existe pero no corre` | `JAVA_HOME` apunta a una Java distinta de la del `PATH`. | `echo $JAVA_HOME` y `java -version` deben coincidir. En macOS: `export JAVA_HOME=$(/usr/libexec/java_home -v 25)`. |
| **T-11** | Tras `./mvnw verify`, `docker ps` muestra contenedores raros: `testcontainers-ryuk-…`, o uno con nombre absurdo (`keen_babbage`) | Son de **Testcontainers**: la librería que levanta un PostgreSQL de verdad para los tests de integración. `ryuk` es su basurero; normalmente los borra solo al terminar. Si el build se interrumpió, quedan. | `99-destruir.sh` **no los toca**: no los levantó el laboratorio, y un script que apaga contenedores ajenos no se vuelve a correr. Míralos y bórralos tú: `docker ps --filter label=org.testcontainers` y luego `docker rm -f $(docker ps -q --filter label=org.testcontainers)`. |
| **T-10** | El script se queda colgado al hacer `./bin/start-lab.sh \| tee registro.txt` | Antes pasaba: la app heredaba la salida del script y el `tee` nunca terminaba. | **Ya está arreglado.** Si te vuelve a ocurrir, es un bug nuestro: repórtalo con la línea exacta que escribiste. |

---

## Si nada de esto te sirve

Manda al instructor, en un solo mensaje:

1. La salida **completa** de `./bin/00-verificar.sh` (pegada, no una captura).
2. Tu sistema operativo y versión.
3. Qué esperabas que pasara y qué pasó.

Con eso basta casi siempre. Sin eso, no basta casi nunca.
