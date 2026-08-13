# INFORME-SPEC-024 · El Java que viaja en la maleta

**SPEC:** SPEC-024 · **Ejecuta:** mocito · **Fecha:** 13 de agosto de 2026
**Rama:** `spec-024-jdk-embebido` · **Tag al cierre:** `material-v0.4.0`
**Máquina:** Mac Studio del PO — Darwin 25.5.0, `arm64`

---

## 1 · Veredicto en una línea

**EL CÍRCULO ESTÁ CERRADO, Y VOLADO** — el JDK 25 viaja partido en el repositorio, el shim lo
ensambla verificando su firma y lo usa **ignorando cualquier Java de la máquina**. Verificado en
el vuelo 4 (§9): Lab 00 más los siete labs, con el cable desenchufado **y con un `JAVA_HOME`
hostil activo todo el vuelo**. La lista de prerrequisitos del curso quedó en una palabra:
**Git**.

---

## 2 · Qué viaja y de dónde salió

| | |
|---|---|
| Distribución | Eclipse Temurin (Adoptium) |
| Versión | **`jdk-25.0.4+7`** — la última GA de la línea 25 |
| Plataformas | `windows-x64` (alumnos) · `macos-aarch64` (preparación del material) |
| Licencia | GPLv2 + Classpath Exception — redistribución permitida |
| Origen | `github.com/adoptium/temurin25-binaries/releases/tag/jdk-25.0.4+7` |

**Decisión de versión, declarada.** La SPEC pedía «la última 25.x GA» y sugería como candidata
la `25+36` que ya corría en el Mac. Consultada la API de Adoptium, la última GA es `25.0.4+7` —
**cuatro niveles de parche por delante**. Se tomó esa, por la instrucción principal y porque
entregar a una institución del Estado un JDK cuatro parches atrasado no se defiende. Coste: hubo
que revalidar las suites contra ella, y se hizo (V3).

### V1 · sha256 contra el publicado por Temurin

```
--- windows-x64 ---
  publicado: 7caab7db43bf4b94a2e6252c699e70d90084f9aa7c943cd3414761fd540937ae
  local    : 7caab7db43bf4b94a2e6252c699e70d90084f9aa7c943cd3414761fd540937ae
  IDENTICOS
--- macos-aarch64 ---
  publicado: 5a101c54abf5a9f16c0f70d8c38ba99e6567c1ba213378f0bb04497284f051bd
  local    : 5a101c54abf5a9f16c0f70d8c38ba99e6567c1ba213378f0bb04497284f051bd
  IDENTICOS
```

Y los trozos reconstruyen el original **bit a bit**, comprobado antes de commitear nada:

```
windows: 7caab7db…37ae  ==  jdk.zip.sha256      RECONSTRUYE BIEN
macos:   5a101c54…51bd  ==  jdk.tar.gz.sha256   RECONSTRUYE BIEN
```

**Empaquetado (D-024-2):** `split -b 80m -d`. Cuatro trozos en total, el mayor de 80,0 MB —
holgado bajo el techo de 95. `tools/jdk/LEEME.md` documenta qué es, versión, origen, licencia,
los dos sha256 y cómo se actualiza.

---

## 3 · Cómo funciona el shim

Antes de invocar Maven, `mvnw`:

1. **Detecta plataforma.** `Darwin`+`arm64` → `macos-aarch64`; `MINGW/MSYS/CYGWIN` →
   `windows-x64`. Cualquier otra (Linux, Mac Intel) → **no hay paquete y se cae al Java del
   sistema**. No es un descuido: es lo que mantiene verde el CI de GitHub, que corre en Ubuntu.
2. **¿Hay que ensamblar?** Solo si falta el sello o el sello no es de este paquete — así un
   `git pull` que traiga una versión nueva del JDK re-ensambla solo.
3. **Junta, verifica, extrae, sella.** Si el sha256 no cuadra: **aborta**, borra lo a medias y
   explica qué mirar. Un JDK que no se puede verificar no se usa.
4. **Exporta `JAVA_HOME` y `PATH` solo para su propio proceso.** Ni `.bashrc`, ni `.zshrc`, ni
   variables de usuario de Windows. El entorno del alumno queda exactamente como estaba.

Detalles de portabilidad que la SPEC exigía y quedaron resueltos:

- **`tar -xf` también para el `.zip`** — Git Bash no garantiza `unzip`, pero su `tar` es bsdtar
  y abre los dos formatos. En Windows 10+ el `tar` nativo hace lo mismo.
- **`shasum -a 256` vs `sha256sum`** — detectados por `command -v`, porque macOS base no trae el
  segundo.
- **`Contents/Home`** — el `tar.gz` de Temurin para macOS trae un bundle, no un JDK plano. El
  shim añade ese sufijo solo en esa plataforma.
- **bash 3.2, sin ANSI**, y `shellcheck` limpio.

---

## 4 · Tabla de verificación

| # | Prueba | Resultado |
|---|---|---|
| **V1** | sha256 ensamblado vs. Temurin | ✅ Idénticos en ambas plataformas, §2 |
| **V2** | Ensamblado limpio | ✅ 3,5 s |
| **V3** | **El Java hostil** | ✅ 46 tests verdes usando el 25 embebido |
| **V4** | Corrupción simulada | ✅ Aborta, no deja basura, se recupera |
| **V5** | Idempotencia | ✅ 0,5 s en la segunda corrida, cero re-ensamblado |
| **V6** | Lab 00 completo | ✅ 5/5 `ESTACIÓN LISTA`, sin red |
| **V7** | Guard 95 MB y tamaños | ✅ Mayor archivo: 80,0 MB |
| **V8** | Revisión estática del `.cmd` | ✅ Con una reserva declarada, §5 |

### V2 · Ensamblado limpio, desde cero

```
$ rm -rf tools/jdk/runtime && ./mvnw -version
[INFO]  Primera vez: ensamblando el JDK jdk-25.0.4+7 del repositorio…
        (solo ocurre una vez; después arranca directo)
[INFO]  JDK jdk-25.0.4+7 listo en tools/jdk/runtime/
Apache Maven 3.9.11
Maven home: …/tools/maven
Java version: 25.0.4, vendor: Eclipse Adoptium, runtime: …/tools/jdk/runtime/macos-aarch64/jdk-25.0.4+7/Contents/Home
real 3,5 s
```

### V3 · La prueba del Java hostil — la que da sentido a la SPEC

`JAVA_HOME` apuntando al GraalVM 21 de la máquina, y ese Java **primero en el PATH**:

```
--- lo que la maquina dice tener ---
JAVA_HOME=/Users/rodrigosilva/.sdkman/candidates/java/21.0.1-graalce
openjdk version "21.0.1" 2023-10-17

--- y lo que el build usa de verdad ---
Java version: 25.0.4, vendor: Eclipse Adoptium, runtime: …/runtime/macos-aarch64/jdk-25.0.4+7/Contents/Home

--- la suite completa, con el 21 hostil activo ---
[INFO] Tests run: 46, Failures: 0, Errors: 0, Skipped: 0
[INFO] BUILD SUCCESS
```

**El problema «N alumnos con N Javas distintos» se convierte en cero problemas.**

### V4 · Corrupción simulada

Seis bytes de basura añadidos a `jdk.tar.gz.part-01`, `runtime/` borrado:

```
[INFO]  Primera vez: ensamblando el JDK jdk-25.0.4+7 del repositorio…
[ERROR] El JDK ensamblado NO coincide con su firma.
          esperado: 5a101c54abf5a9f16c0f70d8c38ba99e6567c1ba213378f0bb04497284f051bd
          obtenido: b23b9888706b64f9f3897a3a82b958daccf0ba1c4fe1704b53c8572052307683

        No voy a usar un JDK que no puedo verificar. Lo más probable es un clon
        incompleto o un antivirus que tocó los archivos.
        Solución: clon fresco del repositorio.

runtime/ tras el aborto: limpio (bien)
```

Restaurado el trozo, **recupera solo**: `[INFO] JDK jdk-25.0.4+7 listo` · `Java version: 25.0.4`.

Este es el mensaje que el guion de sala usa como síntoma del antivirus corporativo (§3 del
guion). No es hipotético: es la salida literal.

### V5 · Idempotencia

```
sello antes:  14:24:57
./mvnw -version   real 0,5 s
sello despues:14:24:57      (mismo timestamp = no re-ensambló)
mensajes de ensamblado en la 2a corrida: 0
```

### V6 · El Lab 00 nuevo, dentro del sandbox sin red

```
$ sandbox-exec … curl https://repo1.maven.org/   →  exit=7 (sin red)

  Estación Base de la DGT — verificación de tu máquina
  Plataforma detectada: macos

[OK]    git está en el PATH
[INFO]  Tu Git: git version 2.50.1 (Apple Git-155)
[OK]    El clon tiene todas sus piezas (Maven, dependencias, JDK y los labs)
[INFO]  Ensamblando el JDK embebido si hace falta (la primera vez tarda)…
[OK]    JDK embebido listo: openjdk version "25.0.4" 2026-07-21 LTS
[INFO]  Java del sistema: openjdk version "21.0.1" 2023-10-17  — el curso NO lo usa
[OK]    ./mvnw usa el Maven del repositorio: Apache Maven 3.9.11
[OK]    Espacio libre: 37 GB

  5/5 verificaciones
  ESTACIÓN LISTA
EXIT=0
```

Esas dos líneas juntas —*«JDK embebido listo: 25.0.4»* y *«Java del sistema: 21.0.1 — el curso
NO lo usa»*— son la SPEC entera en dos renglones.

### V7 · Tamaños

| | |
|---|---|
| `tools/jdk/` (los cuatro trozos) | **559 MB** |
| `repo-maven/` | 230 MB |
| `tools/maven/` | 10 MB |
| `.git` | **494 MB** |
| Clon que recibe el alumno | ~1,2 GB |
| `tools/jdk/runtime/` (extraído, **no viaja**) | ~294 MB por plataforma |

Archivo más pesado que viaja: **80,0 MB** (`jdk.zip.part-00`). `[OK] Ningún archivo supera los
95 MB.`

**El guard aprendió algo.** Marcó en rojo el `lib/modules` del JDK extraído — 138 MB. Tenía
razón en verlo y no en contarlo: `tools/jdk/runtime/` está gitignorado y es derivable, igual que
`target/`. Se añadió a su lista de podados, con el porqué escrito en el propio script.

### V8 · El `.cmd`, revisado sin poder ejecutarlo

| Punto | Comprobado |
|---|---|
| Finales de línea que recibirá Windows | `eol: crlf` vía `.gitattributes` |
| Paréntesis de bloque | `abre 15 · cierra 15 · BALANCEADO` |
| Etiquetas y saltos | 5 etiquetas; los 5 `goto` resuelven |
| Orden de los trozos en el `for` | `part-00`, `part-01` — alfabético = correcto |
| Concatenación binaria | `copy /b !LISTA! "%JDK_TMP%"` |
| Extracción del hash de `certutil` | `for /f "skip=1"` toma la 2ª línea y le quita los espacios |
| Contención del entorno | `setlocal` en la línea 26: ni `JAVA_HOME` ni `PATH` sobreviven al cierre |

**La reserva, declarada:** los archivos que el `.cmd` lee con `set /p` (`VERSION` y el
`.sha256`) están marcados `binary`, así que llegan a Windows con **finales LF**. `set /p` corta
en el primer CR o LF, de modo que debería leerlos limpios — pero eso es **razonado, no medido**:
no hay Windows aquí. Es el punto que la prueba en Parallels del PO tiene que mirar primero. Si
fallara, el síntoma sería inconfundible: el shim diría que el sha no coincide con un valor que a
simple vista es idéntico.

---

## 5 · El Lab 00 y el guion de sala

**`00-verificar.sh` reescrito.** Fuera: Docker, su demonio, Docker Hub, Maven Central, el modo
`--sin-docker` y el requisito duro de Java. Dentro: Git, integridad del clon, **ensamblado real
del JDK embebido** citando su versión, y que `./mvnw` resuelva al Maven del repositorio. El Java
del sistema baja a informativo. El script **no toca la red**, y es a propósito: tiene que dar
verde con el cable desenchufado.

El espacio requerido baja de 10 GB a 3: ya no hay imágenes que descargar.

`--sin-docker` no se borra en silencio — responde explicando que el curso entero corre sin
Docker. Un alumno con apuntes de la sesión 1 no merece un «argumento no reconocido».

**`docs/guion-reinicio-de-sala.md`** — el protocolo de diez minutos: clon fresco y no `git pull`
(con el porqué en una línea), cómo deshacer el parche de `PATH` de la sesión 1 sin tocar nada
más, la secuencia de arranque con la salida esperada, y el plan B con los tres sospechosos de
las máquinas corporativas: Git ausente, clon dentro de OneDrive —incluido el aviso de que
`Documents` **es** OneDrive en muchas máquinas sin que se note— y el antivirus reteniendo el
ensamblado, con la salida literal de V4 como síntoma.

---

## 6 · Vuelo 4 — en pista, sin lanzar

`tools/vuelo-4-modo-avion.sh`. Mismo protocolo probado: espera el corte, vigila
recontaminación, caja negra con timestamps (`/tmp/caja-negra-vuelo4.log`), veredicto
(`/tmp/veredicto-vuelo4.txt`), restaura `~/.m2` y **no relanza Docker**.

**La diferencia con el vuelo 3:** todo el vuelo corre con un **`JAVA_HOME` hostil** apuntando al
GraalVM 21 y ese Java primero en el `PATH`. Ya no basta con que el material vuele sin red: tiene
que volar **con la máquina en contra**, que es el escenario real de la sala.

Cubre: el **Lab 00** completo (que abre el vuelo), N1–N5 en avión de los labs 01–07, §4.1 y §4.2,
y **V10-ter** — el simulacro del alumno con **todas** las cachés frías: la de Zonky borrada y el
JDK sin ensamblar, porque el clon es nuevo.

**Duración real: 9 minutos exactos** (14:43:09 → 14:52:09), contra los 14–18 estimados. La
estimación volvió a ser conservadora: el ensamblado del JDK cuesta 3–4 s, no el sobrecoste por
suite que temía.

```bash
nohup tools/vuelo-4-modo-avion.sh > /tmp/vuelo4.out 2>&1 &
```

---

## 9 · Vuelo 4 — el material con la máquina en contra

Ejecutado el 13 de agosto, **14:43:09 → 14:52:09: nueve minutos exactos**, contra los 14–18
estimados. Despegó solo 108 s después de lanzarse y aterrizó con la red todavía cortada. Caja
negra: `/tmp/caja-negra-vuelo4.log`.

### Aislamiento — con un matiz que hay que mirar de frente

```
--- ping -c1 github.com ---    ping: cannot resolve github.com: Unknown host   exit=68
--- curl repo1.maven.org ---   curl: (6) Could not resolve host    http=000    exit=6
--- IPs no-loopback ---        bridge100 10.211.55.2
                               bridge101 10.37.129.2
--- ruta por defecto ---       (sin ruta)
--- el .m2 del usuario ---     apartado
--- docker ---                 sin daemon
```

**Aparecieron dos IPs que en los vuelos anteriores no estaban.** No son un escape: son las redes
virtuales de **Parallels Desktop**, levantadas porque el PO tenía la VM Windows corriendo.
Verificado tras aterrizar: sus miembros son `vmenet0` y `vmenet2`, y las creó
`prl_disp_service`.

El aislamiento se sostiene, y por tres hechos independientes: **no hay ruta por defecto** —sin
ella no se sale de la máquina—, el **DNS está muerto** (NXDOMAIN), y las quince suites
registraron **cero descargas intentadas**. Esos puentes solo alcanzan a la VM Windows, y nada
del build le habla a la VM. Se declara porque un informe que dijera «(ninguna)» cuando la
pantalla decía otra cosa sería exactamente la clase de mentira que la A-04 persigue.

### El Java hostil, activo desde antes de despegar

```
--- java HOSTIL de la maquina (el que el material debe ignorar) ---
JAVA_HOME=/Users/rodrigosilva/.sdkman/candidates/java/21.0.1-graalce
openjdk version "21.0.1" 2023-10-17
--- y el que el shim va a usar de verdad ---
Java version: 25.0.4, vendor: Eclipse Adoptium, runtime: /private/tmp/dgt-vuelo4/tools/jdk/runtime/…
```

Ese `runtime:` apunta al clon del vuelo, no al del repositorio: **el JDK se ensambló dentro del
avión**, en tres segundos (14:43:14 → 14:43:17), sin red. La prueba de que el ensamblado no
necesita internet no es un razonamiento: es una marca de tiempo.

### El Lab 00, primero en la lista

```
[OK]    git está en el PATH
[OK]    El clon tiene todas sus piezas (Maven, dependencias, JDK y los labs)
[OK]    JDK embebido listo: openjdk version "25.0.4" 2026-07-21 LTS
[INFO]  Java del sistema: openjdk version "21.0.1" 2023-10-17  — el curso NO lo usa
[OK]    ./mvnw usa el Maven del repositorio: Apache Maven 3.9.11
[OK]    Espacio libre: 36 GB
  5/5 verificaciones · ESTACIÓN LISTA          EXIT=0
```

### Los siete labs

| Lab | `solucion` | descargas | `start-lab` | huérfanos | `90-validar` sol / starter |
|---|---|---|---|---|---|
| 01 | `EXIT=0` · 15 s | **0** | `health=200` | 0 | APROBADO / NO APROBADO |
| 02 | `EXIT=0` · 15 s | **0** | `health=200` | 0 | APROBADO / NO APROBADO |
| 03 | `EXIT=0` · 15 s | **0** | `health=200` | 0 | APROBADO / NO APROBADO |
| 04 | `EXIT=0` · 14 s | **0** | `health=200` | 0 | APROBADO / NO APROBADO |
| 05 | `EXIT=0` · 21 s | **0** | `health=200` | 0 | APROBADO / NO APROBADO |
| 06 | `EXIT=0` · 17 s | **0** | `health=200` | 0 | APROBADO / NO APROBADO |
| 07 | `EXIT=0` · 19 s | **0** | `health=200` | 0 | APROBADO / NO APROBADO |

**Cero descargas en las quince suites**, y los labs 04 y 05 ya se anuncian con su propio número
—el arreglo de la SPEC-FIX-05, confirmado en vuelo.

§4.1 y §4.2, intactos sin red y con el Java hostil: `13L to be less than or equal to 3L`, el
contador de la solución en verde, y los cuatro tests de la carrera del Lab 06 pasando.

### V10-ter · el simulacro, ahora de verdad frío

```
cache de binarios de Zonky borrada
el clon del alumno se hace de cero, asi que su JDK tampoco esta ensamblado
  git clone                   5s      (1.0G)
  Lab 06 verify (frio)       25s      EXIT=0 · descargas=0
  Lab 07 start-lab            8s      EXIT=0 · health=200
  ------------------------------
  TOTAL                      38s
```

**38 segundos** desde el `git clone` hasta la aplicación sirviendo, con **todo** frío: sin
`target/`, sin caché de Zonky, sin JDK ensamblado y sin una sola conexión de red. Son 4 s más
que el vuelo 3 (34 s) y esos 4 s son exactamente el ensamblado del JDK. El clon pasó de 486 MB a
1,0 GB.

### Veredicto

```
VUELO 4 CON FALLAS EN: lab-03-red-de-seguridad/starter-FALLOS-AJENOS
                       lab-05-once-segundos/starter-FALLOS-AJENOS
```

**Las dos son las mismas falsas alarmas del arnés que el vuelo 3 ya diagnosticó**, y siguen
siendo correctas: `TramiteServiceTest` y `ListadoIntegracionTest` son los huecos donde el alumno
escribe SUS tests, declarados en su `derivacion-starter.txt` y lanzando
`UnsupportedOperationException`. Deben fallar en el `starter`. Ninguna otra prueba falló.

---

## 10 · A2.1 · Lo que la prueba en Windows real destapó

**Mi supuesto era falso, y solo una máquina real podía decirlo.** El informe afirmaba que
`tar -xf` abre el ZIP «en Git Bash y en Windows 10+, porque es bsdtar». La primera mitad es
mentira:

```
tar: This does not look like a tar archive
```

El `tar` que **Git Bash** pone en el PATH es **GNU tar**, y GNU tar no abre ZIP. El bsdtar que
sí lo abre existe en toda máquina Windows 10+, pero vive en `System32` y queda **detrás** en el
PATH de Git Bash. Confundí «hay un bsdtar en Windows» con «el `tar` que se invoca es bsdtar».

### Lo que sí validó Windows real

| | |
|---|---|
| Detección de plataforma (`MINGW*`) | ✅ |
| Concatenación de los trozos | ✅ |
| Verificación del sha256 | ✅ |
| Mensajes de error del shim | ✅ comportándose como se diseñaron |
| Extracción | ❌ — el hallazgo |

Que el sha pasara **antes** de fallar la extracción no es un detalle menor: significa que los
trozos viajan bien por Git, que `copy`/`cat` los reconstruye bit a bit en Windows, y que la
detección de `shasum`/`sha256sum` funciona en Git Bash. El fallo estaba en el último paso, y
solo en él.

### El arreglo

En la rama Windows del shim, la extracción va **por ruta explícita**:

```bash
TAR_WIN="${SYSTEMROOT:-C:/Windows}"
TAR_WIN="${TAR_WIN//\\//}/System32/tar.exe"     # SYSTEMROOT viene con \ de Windows
if [ -x "$TAR_WIN" ]; then "$TAR_WIN" -xf "$1"; return $?; fi
if command -v unzip >/dev/null 2>&1; then unzip -q "$1"; return $?; fi
# …y si no hay ninguno, un [ERROR] que explica por qué el tar de Git Bash no sirve
```

La sustitución de backslashes se comprobó en **bash 3.2.57** (`C:\Windows` →
`C:/Windows/System32/tar.exe`), y también su valor por defecto cuando `SYSTEMROOT` no existe.
El archivo se le pasa por **nombre relativo** con el `cwd` ya puesto, así no hay que traducir
rutas POSIX para un binario nativo de Windows.

**El `.cmd` recibió el mismo tratamiento aunque allí `tar` suele resolver bien.** «Suele» no es
garantía: basta que el alumno tenga un GNU tar por delante —Git, MSYS, chocolatey— para caer en
el mismo agujero. Nombrar el binario cuesta cero.

**Revisados todos los demás usos de `tar` del repositorio:** 41 sitios, todos
`tar cf - | tar xf -` sobre directorios en formato tar. GNU tar los maneja perfecto. **Ninguno
más tocaba un ZIP.**

**Regresión en macOS:** ensamblado limpio y 46 tests verdes tras el cambio.

### Lo que sigue sin probarse

La reserva de V8 **sigue abierta**: el PO probó Git Bash, es decir el `mvnw`. El `mvnw.cmd` no
se ha ejecutado en ninguna parte, y con él sigue sin medirse si `set /p` lee bien los archivos
`VERSION` y `.sha256`, que llegan con finales LF. Es lo que hay que mirar en la re-prueba.

---

## 7 · Sorpresas y desviaciones

**7.1 · Se tomó `25.0.4+7`, no la `25+36` sugerida.** Detallado en §2. La instrucción principal
mandaba sobre la candidata, y cuatro niveles de parche de seguridad no son un detalle.

**7.2 · El guard de 95 MB mordió al JDK extraído.** Y hacía bien en verlo: `lib/modules` pesa
138 MB. Lo que no hacía bien era contarlo, porque `runtime/` está gitignorado y es derivable. Se
añadió al podado con su razón. Es exactamente el mismo caso que `target/`.

**7.3 · Linux y Mac Intel se caen al Java del sistema, y es deliberado.** No se empaquetan (la
SPEC fija dos plataformas). Si el shim exigiera el JDK embebido, **el CI de GitHub —que corre en
Ubuntu— se pondría rojo al instante**. Con el fallback, CI sigue verde y el alumno del SII sigue
cubierto.

**7.4 · El `.cmd` no se pudo ejecutar.** Está en V8 con su reserva, y **sigue sin ejecutarse**:
la prueba del PO en Parallels usó Git Bash, o sea el `mvnw`. Ver §10.

**7.4-bis · Y la prueba en Windows real encontró un fallo que aquí era invisible** — mi supuesto
sobre `tar` y el ZIP. Está en §10 con su arreglo. Es la mejor demostración de por qué esa prueba
no se puede sustituir por razonamiento: el shim se comportó exactamente como estaba diseñado en
todo lo demás, y falló en el único punto donde yo había supuesto en vez de medir.

**7.5 · Deuda heredada de la FIX-05, que consta aquí como se pidió.** Los `[OK]` corregidos de
los labs **08–14 siguen sin ejecución real** — necesitan Docker. Quedaron linteados y revisados,
pero nadie los ha visto correr. Es deuda explícita de la Fase 2, no de esta SPEC.

---

## 8 · Lo que queda

**El vuelo 4 ya voló** — resultados en §9. Queda **la prueba del `.cmd` en Parallels**, que es
la única verificación de esta SPEC que no se pudo hacer aquí.

**Fase 2:**

1. **Labs 08 a 11** — WireMock en contenedor → in-process, y capturar ese jar.
2. **Ejecución real de los fixes de scripts de los labs 08–14** (§7.5). Van juntas: cuando esos
   labs corran sin Docker, sus ciclos completos verifican de paso los `[OK]` corregidos.
3. **Lab 13** — Jib, y qué significa un lab de contenedores en un curso sin Docker. Decisión del
   Arquitecto antes que técnica.
4. **Labs 12 y 14** — el 12 comparte los contenedores singleton del 08; el 14 son cinco
   proyectos Spring Cloud. Decisión pendiente sobre su alcance.
5. **Rediseño de los `TODO_1`/`TODO_2`** del perfil `dev`, aún marcados `PROVISORIO SPEC-022`.
6. **Los 30 `maven-wrapper.properties`** apuntando a `repo.maven.apache.org`. Nadie los lee ya;
   se limpian cuando el shim llegue a los catorce labs.
7. **El JDK para los labs 08–14** ya está: el shim es el mismo en los 16 proyectos migrados, y
   copiarlo a los que falten es parte de su migración.
