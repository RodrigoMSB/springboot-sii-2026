# INFORME-SPEC-024 · El Java que viaja en la maleta

**SPEC:** SPEC-024 · **Ejecuta:** mocito · **Fecha:** 13 de agosto de 2026 · **Cierre:** 14 de agosto de 2026
**Rama:** `spec-024-jdk-embebido` · **Tag al cierre:** `material-v0.4.0`
**Máquinas:** Mac Studio del PO (Darwin 25.5.0, `arm64`) · Windows 11 ARM en Parallels ·
Windows 11 x64 corporativo (VM de Netec)

---

## 1 · Veredicto en una línea

**EL CÍRCULO ESTÁ CERRADO, VOLADO Y PROBADO EN TRES PLATAFORMAS** — el JDK 25 viaja partido en el
repositorio, el shim lo ensambla verificando su firma y lo usa **ignorando cualquier Java de la
máquina**. Verificado en el vuelo 4 (§9) —Lab 00 más los siete labs, con el cable desenchufado y
un `JAVA_HOME` hostil activo—, en **cuatro vueltas de prueba en una máquina Windows real**
(§10–§13), que encontraron tres defectos invisibles desde macOS y los cerraron todos, y en una
**quinta validación sobre Windows 11 x64 corporativo** (§14), el pariente más cercano a las
máquinas del SII, donde todo pasó a la primera. La lista de prerrequisitos del curso quedó en una
palabra: **Git**.

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
| **V8** | El `.cmd` en Windows real | ✅ **CERRADA Y MEDIDA** — ver §12 |

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

*(Superado por §12: la segunda vuelta del PO cerró esta reserva.)*

---

## 11 · A2.2 · El validador que dio ERROR con todo funcionando

**Re-prueba en Windows real, segunda vuelta.** El arreglo del `tar` funcionó: el JDK ensambla y
verifica. Pero el Lab 00 bajó a **4/5** por un chequeo que mentía al revés:

```
[ERROR] ./mvnw no está resolviendo al Maven de tools/
```

Y a mano, en esa misma máquina:

```
Maven home: C:\SPRINGBOOT\prueba-windows\tools\maven
```

Resolvía perfecto. **El falso negativo era mío.** El chequeo comparaba la salida de Maven contra
`$RAIZ/tools/maven`, y en Git Bash `$RAIZ` vale `/c/SPRINGBOOT/prueba-windows`. No es solo el
separador: **la misma carpeta se escribe de dos formas y ninguna contiene a la otra**, así que
el `grep` no podía acertar jamás en Windows.

### El arreglo

Se compara **el final de la ruta, con los separadores normalizados** — que es lo que el chequeo
quiere saber de verdad: ¿el Maven que corrió es el del repositorio o uno del sistema?

```bash
HOME_MVN="$(printf '%s' "$SALIDA_MVN" | grep -m1 '^Maven home:' \
            | sed 's/^Maven home: *//' | tr -d '\r')"
HOME_MVN="${HOME_MVN//\\//}"
case "$HOME_MVN" in
    */tools/maven|*/tools/maven/)  paso_ok  … ;;
    "")                            paso_fail "no imprimió su 'Maven home'" … ;;
    *)                             paso_fail "está usando otro Maven: $HOME_MVN" … ;;
esac
```

**Corrección posterior, en el cierre (§16).** La primera versión de este arreglo normalizaba con
`tr '\\' '/'` dentro del pipe, y eso **puso el CI en rojo**: shellcheck lo marca `SC1003` y el
job `labs-sh` trata cualquier salida no limpia como fallo. Se cambió a la expansión de parámetros
de arriba —el mismo idioma que ya usaba el shim (`${TAR_WIN//\\//}`)— que es shellcheck-limpia y
se comporta igual. La tabla de abajo se volvió a pasar contra la versión corregida.

Probado contra las formas reales:

| Entrada | Veredicto |
|---|---|
| `Maven home: /Users/rodrigo/repo/tools/maven` | ACEPTA |
| `Maven home: C:\SPRINGBOOT\prueba-windows\tools\maven` | **ACEPTA** |
| `Maven home: /opt/homebrew/Cellar/maven/3.9.11/libexec` | RECHAZA |
| `Maven home: C:\Program Files\apache-maven-3.9.9` | RECHAZA |
| Salida sin línea `Maven home:` | «no imprimió su Maven home» |

El caso de fallo también mejora: antes decía «no está resolviendo al Maven de tools/» sin más;
ahora **nombra el Maven que sí encontró**, que es la información que hace falta para arreglarlo.

### Revisión de parientes

**Comparaciones de ruta contra la salida de un programa: había exactamente una en todo el
repositorio**, la corregida. Ninguna otra.

**Pero apareció un pariente que NO se puede verificar desde macOS y queda declarado como
sospecha, no como hallazgo.** Los catorce `99-destruir.sh` localizan la app del lab así:

```bash
PATRON="multiModuleProjectDirectory=$DIR_LAB/$PROYECTO"
pgrep -f "$PATRON"
```

Ese patrón lleva una ruta POSIX (`/c/…`) dentro, y la línea de comandos del proceso en Windows
la llevará en formato Windows (`C:\…`). **Es la misma clase de fallo que A2.2.** Y hay una
segunda incógnita encima: `pgrep` y `pkill` **no vienen de fábrica en Git Bash**, así que puede
que esos bloques ni siquiera lleguen a ejecutarse.

No se toca a ciegas, por dos razones: no se puede verificar aquí, y cualquier cambio ahí roza la
ley post-ALCHEMIA de no matar procesos por nombre genérico. **Va a la re-prueba como pregunta
concreta:** en Windows, tras `./bin/start-lab.sh`, ¿qué dice `./bin/99-destruir.sh`? Si aparece
`pgrep: command not found` o si dice que detuvo cosas sin haberlo hecho, está confirmado.

### Gobernanza

Es el **segundo validador mentiroso de la semana**, y por eso se registró — pero como **coda de
A-04, no como anti-herencia nueva**: inventar una A-05 por cada instancia las diluye, y esta es
la misma familia vista desde el otro lado. A-02 prohíbe declarar sin medir; A-04, medir sin
mostrar; la coda cierra el triángulo: **un validador solo puede fallar por la razón que dice**.
Con su regla práctica: *nunca compares rutas absolutas entre plataformas*.

Que los dos casos aparecieran en la misma semana no es casualidad: **los dos salieron al correr
el material en una máquina que no era la de su autor.**

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

**Nada de esta SPEC.** El vuelo 4 voló (§9), las cuatro vueltas de Windows cerraron sus tres
defectos (§10–§13) y la quinta validación en Windows x64 corporativo pasó a la primera (§14). La
prueba del `.cmd`, que era la última reserva, quedó medida en la segunda vuelta (§12).

**Anotación abierta para la próxima SPEC — A2.4 · el cartel del Firewall durante `verify`.**
Detallada en §14: es **cosmética y no bloqueante**, con evidencia de dos máquinas Windows de que
el build termina verde aunque el permiso se deniegue. No entra en esta SPEC; su investigación y su
candado son trabajo de la siguiente.

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


---

## 12 · Segunda vuelta en Windows — lo que quedó medido

### V8, cerrada

`mvnw.cmd` **se ejecutó en `cmd.exe`**. Resolvió el Maven y el Java embebidos a la primera, y
`mvnw.cmd test` dio **46/46 · BUILD SUCCESS en 40 s**.

Con eso cae la reserva que arrastraba el informe desde §4: **`set /p` lee bien los archivos
`VERSION` y `.sha256` aunque lleguen con finales LF.** Era razonado; ahora está medido. Y con
ello el `.cmd` deja de ser territorio virgen: la ruta explícita a `System32\tar.exe`, el
`copy /b`, el `certutil` y el sellado funcionan en Windows real.

### Los acentos: el problema es de la consola, no del material

Los mensajes salen **correctos en `cmd.exe`** —«Defínelas», «jamás»—. Los `▒` que se veían son
de la **consola de Git Bash**, no de los scripts ni de los `.yml`.

Eso reduce el alcance de ese pulido a un problema de terminal, no de material. Queda **fuera**
de esta SPEC: no se toca nada por ahora, y si molesta en sala se ataca donde está el defecto
—la configuración de la consola—, no reescribiendo textos que ya son correctos.

### El pariente del `pgrep`: media respuesta

En Git Bash de Windows, `99-destruir.sh` corrió **sin `pgrep: command not found`** y reportó:

```
[OK] API detenida (PID 1660)
  3/3 verificaciones
```

Dos cosas se aprenden y una queda pendiente:

1. **`pgrep`/`pkill` existen** en el Git Bash de esa máquina. La incógnita mayor, despejada.
2. Ese `[OK]` **ya no es de los que regalan**: tras la SPEC-FIX-05 depende de un `kill -0`
   posterior a la espera. Que lo diga significa que, para Git Bash, ese PID ya no existía.
3. **Lo que falta:** que `kill -0` no lo vea no prueba que el JVM nativo muriera. Git Bash y
   Windows no cuentan los procesos igual, y el riesgo real es que muera el shell del wrapper y
   sobreviva el Java escuchando el 8099.

**Verificación ejecutada en la cuarta vuelta — el pariente quedó ABSUELTO. Ver §13.**

El plan B del guion de sala (§4.d) se conserva igual: aunque el desmontaje funcione, un puerto
tomado por cualquier otra razón sigue mereciendo su receta.

### A2.3 · `server.address: localhost`

⚠️ **El texto de esta anotación no llegó al ejecutor: solo su título.** Se implementó bajo la
lectura evidente, y se declara para que el Arquitecto la ratifique o la corrija.

**Lo que se hizo:** `server.address: localhost` en el perfil `dev` de los 16 proyectos, con el
porqué escrito al lado. Sin esto Tomcat se ata a `0.0.0.0` y Windows saca el cartel de
«Windows Defender ha bloqueado algunas características…», que pide administrador — un permiso
que el alumno de una máquina corporativa no puede dar, en el primer minuto de clase.

**Verificado en el Mac:**

```
lsof -nP -iTCP:8099 -sTCP:LISTEN
  java -> 127.0.0.1:8099          <-- loopback, no *:8099
curl localhost:8099  -> 200
curl 127.0.0.1:8099  -> 200
```

Suites verdes tras el cambio: Lab 01 `46 + 7` y Lab 07 `40`, ambas `BUILD SUCCESS`. Derivación
intacta — el `application-dev.yml` sigue idéntico donde debe serlo.

**Y en el guion de sala** (§3): qué cartel *no* debería aparecer, qué hacer si aparece igual
(Cancelar, y comprobar que la app responde de todos modos), y cuándo eso sí sería un caso nuevo.

**Si A2.3 pedía otra cosa** —otro alcance, otra propiedad, el `.yml` de otro perfil—, es un
cambio de minutos: está en un solo bloque, en un solo archivo por proyecto.

---

## 13 · Cuarta vuelta — el veredicto de Windows

### A2.3, verificado en la máquina real

`./bin/start-lab.sh` levantó la aplicación **sin que apareciera el cartel del Firewall**, a la
primera. `server.address: localhost` hace lo que prometía: Tomcat se ata al loopback y Windows
no tiene nada que preguntar.

Es el defecto que **habría parado la sala entera** — dieciocho máquinas corporativas, dieciocho
diálogos pidiendo un administrador que ningún alumno tiene — y no llegó a existir.

### El pariente del `pgrep`: ABSUELTO, con evidencia

La sospecha de §11 era razonable y resultó infundada. Tras `./bin/start-lab.sh` y
`./bin/99-destruir.sh`, en Git Bash de Windows:

```
netstat -ano | grep 8099     ->  solo un TIME_WAIT del curl cliente; CERO en LISTENING
tasklist | grep -i java      ->  vacío
```

**El JVM nativo muere, no solo el envoltorio.** Los dos hechos juntos lo cierran: nada escuchando
en el 8099 y ningún proceso `java` en la lista de tareas de Windows — que es la lista real, no
la que ve Git Bash.

Un matiz para quien lea esto y se asuste con el `TIME_WAIT`: es el **lado cliente** del `curl`
que se usó para probar la app, un socket que el sistema retiene unos segundos tras cerrarse. No
es un servidor vivo. Lo que importaba era la ausencia de `LISTENING`, y no hay ninguno.

Con esto, el `[OK] API detenida (PID …)` de los `99-destruir.sh` **dice la verdad también en
Windows**. El arreglo de la SPEC-FIX-05 —hacerlo depender de un `kill -0` posterior a la espera—
resulta ser correcto en las dos plataformas, y ahora está medido en ambas.

### Las cuatro vueltas, en una tabla

| Vuelta | Qué encontró | Estado |
|---|---|---|
| **1ª** | `tar -xf` no abre ZIP en Git Bash: es GNU tar, no bsdtar (§10 · A2.1) | ✅ Arreglado por ruta explícita a `System32\tar.exe` |
| **2ª** | El chequeo del Lab 00 comparaba rutas absolutas entre plataformas y daba `[ERROR]` con todo funcionando (§11 · A2.2) | ✅ Arreglado comparando el sufijo normalizado |
| **2ª** | `mvnw.cmd` en `cmd.exe`: **46/46 · BUILD SUCCESS en 40 s** (§12) | ✅ V8 cerrada y medida |
| **3ª/4ª** | `server.address: localhost` — sin cartel del Firewall, a la primera (§13 · A2.3) | ✅ Verificado |
| **4ª** | El desmontaje mata el JVM nativo, no solo el wrapper (§13) | ✅ Absuelto con evidencia |

**Los tres defectos que encontraron esas vueltas eran invisibles desde macOS**, y los tres
habrían costado tiempo de sala. Ninguno se podía haber cazado razonando: el `tar` porque
confundí «existe un bsdtar en Windows» con «el `tar` que se invoca es bsdtar»; el del Lab 00
porque en una sola plataforma la comparación funciona; el del firewall porque en macOS no hay
cartel que aparecer.

Es el argumento entero a favor de probar en la máquina del alumno, y la razón de que esta SPEC
cierre con cuatro vueltas en vez de con una.

### Estado final de las cuatro vueltas

**Nada pendiente de esta SPEC.** Las ocho verificaciones cerradas, las tres anotaciones A2.x
implementadas y verificadas en Windows real, el vuelo 4 volado y el guion de sala listo para
proyectar.

Lo que faltaba no era una prueba pendiente sino una **plataforma**: las cuatro vueltas corrieron
sobre Windows ARM en Parallels, y las máquinas del SII son x64 nativas. Eso es lo que cierra §14.

---

## 14 · Quinta validación · Netec, Windows x64 corporativo

**Fuente:** evidencia del PO, transcrita por el Arquitecto. Ejecutada el 14 de agosto de 2026.

La tercera plataforma del tour, y la que más se parece a la sala: **VM corporativa de Netec,
Windows 11 x64 NATIVO** (no ARM, no emulación), consola **MINGW64**, **Temurin 17.0.20 instalado
como Java de sistema**, clon en `C:\PRUEBA`. Máquina con antivirus corporativo y sin privilegios
de administrador — el retrato del alumno del SII.

### Resultados

| Prueba | Resultado |
|---|---|
| `git clone` de la rama (475 MB) | **21 s** a 62 MB/s — sin síntoma de antivirus en git |
| Lab 00 `00-verificar.sh` | **5/5 ESTACIÓN LISTA a la primera** — JDK embebido ensamblado en x64 nativo, Java 17 del sistema declarado irrelevante |
| `./mvnw verify` (frío) | **BUILD SUCCESS · 46 + 7/7 IT · 1m28s** — PostgreSQL 16.14 nativo, Flyway 2 migraciones, initdb 14 s (vs 8 en Parallels: ahí se nota el antivirus, irrelevante por loopback) |
| Cartel del firewall durante `verify` | **Apareció** (en `SemillaCoherenteIT`) y el PO le dio **Cancel a propósito** → el build terminó verde igual. El alumno sin admin sobrevive completo |
| `start-lab.sh` | App viva **SIN cartel** — **A2.3 validado en fierro corporativo** |
| health / destruir / netstat / tasklist | UP con Postgres → 3/3 honesto → netstat solo TIME_WAIT del curl cliente, **cero LISTENING** → tasklist limpio. Muerte del JVM nativo confirmada en segunda plataforma Windows |

### Qué queda demostrado con esto

**Cero defectos nuevos.** Es la primera plataforma del tour que no encontró nada que arreglar, y
eso importa más que un verde cualquiera: significa que los tres arreglos de las vueltas 1–4 —el
`tar` por ruta explícita, el sufijo normalizado del Lab 00 y el `server.address: localhost`— no
eran parches de una máquina, sino correcciones reales que sobreviven al cambio de arquitectura.

**El x64 nativo estaba sin probar, y era justamente el de la sala.** Todo lo anterior en Windows
corrió sobre ARM emulado en Parallels. Aquí el JDK embebido `windows-x64` se ensambla y corre en
el procesador para el que fue empaquetado, que es el caso real y no el difícil.

**Tres Javas hostiles derrotados**, uno por plataforma: GraalVM 21 (Mac), Temurin 17.0.13
(Parallels) y **Temurin 17.0.20 (Netec)**. La tesis de la SPEC —el material ignora el Java de la
máquina— ya no descansa en una sola observación.

**El antivirus se dejó ver donde no molesta.** `initdb` tardó 14 s contra 8 en Parallels: es el
antivirus inspeccionando los binarios de PostgreSQL al extraerse. Es un costo de arranque, una
vez, y no toca el camino crítico de la clase. En `git clone` —los 475 MB que más miedo daban— no
apareció síntoma alguno: 21 s a 62 MB/s.

**A2.3 validado en fierro corporativo.** `start-lab.sh` levantó la app **sin cartel del
Firewall**, igual que en Parallels. Es el defecto que habría parado dieciocho máquinas a la vez, y
ahora está descartado en las dos plataformas Windows.

**El desmontaje, confirmado en segunda plataforma Windows.** `netstat` sin un solo `LISTENING` en
el 8099 y `tasklist` sin `java`: el JVM nativo muere, no solo el envoltorio. Lo que en §13 era una
medición, aquí es una reproducción.

### El tour de validación, completo

| Plataforma | Vueltas | Java hostil derrotado | Resultado |
|---|---|---|---|
| **Mac Studio** (Darwin, arm64) | 4 vuelos en modo avión | GraalVM 21 | ✅ Sin red, sin Docker, 15 suites |
| **Windows ARM** (Parallels) | 4 vueltas | Temurin 17.0.13 | ✅ Tres defectos hallados y cerrados |
| **Windows 11 x64** (Netec, corporativa) | 1 vuelta | Temurin 17.0.20 | ✅ Todo a la primera, cero defectos |

**Cero incógnitas.** No queda plataforma, Java ni escenario de esta SPEC sin medir en fierro real.

---

## 15 · A2.4 · El cartel del Firewall durante `verify` — anotación abierta

**Clasificación: cosmético, NO bloqueante. No se toca en esta SPEC.**

### El hecho

El cartel de «Windows Defender ha bloqueado algunas características…» **reaparece durante
`./mvnw verify`, en la fase de tests de integración**. Se vio en las **dos** máquinas Windows —
Parallels y Netec—, en Netec concretamente al correr `SemillaCoherenteIT`.

**A2.3 cubrió la aplicación en perfil `dev`, no el contexto de los IT.** Eso no es una sospecha:
está medido en el propio repositorio. `address: localhost` aparece en **exactamente 16 archivos, y
los 16 son `application-dev.yml`**. Ni un solo `application-test.yml` lo lleva, y ese es el perfil
con el que corren los tests. `start-lab.sh` —que sí usa `dev`— levanta la app sin cartel en las
dos máquinas Windows; `verify` no tiene de dónde heredar la propiedad.

**Lo que sí está verificado sobre los sospechosos, desde el código:**

- **`ContratoRn03IT` y `E2_ListadoFuncionalIT` arrancan Tomcat de verdad** —
  `@SpringBootTest(webEnvironment = RANDOM_PORT)`— y sin `server.address` se atan a todas las
  interfaces. Es el candidato más directo.
- **Pero `SemillaCoherenteIT`, donde el PO vio el cartel, NO levanta Tomcat**: es
  `@SpringBootTest(properties = "dgt.base-embebida.enabled=false")`, sin `webEnvironment`, o sea
  entorno `MOCK`. Ahí no hay servidor web que atar.

**Esa contradicción es el corazón de la anotación, y se declara sin resolver.** Caben dos lecturas
y desde macOS no se puede elegir entre ellas: o el cartel lo dispara **otro proceso** —el
`postgres.exe` nativo que Zonky extrae y arranca, que también abre un socket y es un binario
desconocido para el Firewall—, o el diálogo apareció mientras la consola mostraba
`SemillaCoherenteIT` pero lo disparó otra IT del mismo fork. **Atribuir el cartel a una clase por
lo que había en pantalla es justamente el error que la coda de A-04 prohíbe**, así que aquí queda
como observación, no como diagnóstico.

### Por qué no bloquea el merge

**Porque está medido con el permiso denegado.** El PO le dio **Cancel a propósito** al cartel en
Netec y **el build terminó verde igual: `BUILD SUCCESS · 46 + 7/7 IT`.** El alumno sin
administrador —que es el alumno real del SII— completa la suite entera cancelando el diálogo. La
molestia es visual; la funcionalidad no depende del permiso.

Y el guion de sala **ya cubre el caso**: su §3 dice qué hacer si el cartel aparece igual —Cancelar
y comprobar que el material sigue— sin prometer que no aparecerá nunca.

### Lo que la próxima SPEC tiene que hacer

1. **Identificar el proceso, no la clase.** En la máquina Windows, mirar qué ejecutable nombra el
   cartel —¿`java.exe` o `postgres.exe`?— y qué está en `LISTENING` fuera del loopback mientras
   corre `verify`. Esa sola pregunta separa las dos lecturas de arriba en un minuto.
2. **Poner el candado donde esté el defecto**, no donde sea cómodo: si es el Tomcat de
   `RANDOM_PORT`, `server.address` va al perfil de test; si es el PostgreSQL de Zonky, se le pasa
   su propia dirección de escucha al arrancarlo. Son arreglos distintos y solo uno es el correcto.
3. **Verificar en Windows real** — este es exactamente el tipo de defecto que macOS no puede ver,
   como los tres de §10–§13.

Queda anotado también en `ESTADO.md`.

---

## 16 · El CI, mirado de frente antes del merge

Nada de esto estaba en el informe hasta el momento del merge, y **debía haber estado**: la SPEC se
declaró «en posición de merge» sin citar el estado del CI. Se corrige aquí, con los dos hechos
separados y medidos.

### El rojo que era nuestro — corregido

El job `labs-sh · andamiaje (ubuntu-latest)` fallaba **en los ocho commits de la rama**, y el
culpable era el propio arreglo de A2.2:

```
In labs/lab-00-estacion-base/bin/00-verificar.sh line 146:
                | sed 's/^Maven home: *//' | tr -d '\r' | tr '\\' '/')"
                                                               ^-- SC1003 (info)
[ERROR] labs/lab-00-estacion-base/bin/00-verificar.sh
[ERROR] 1 script(s) con problemas
```

Es doblemente incómodo porque §3 de este informe presumía de «shellcheck limpio». Lo era antes de
A2.2 y dejó de serlo con él. Arreglado con la expansión de parámetros (§11), y verificado como lo
verifica el CI:

```
$ shellcheck -x labs/lab-00-estacion-base/bin/00-verificar.sh   →  limpio, exit=0
$ for f in $(find labs -name '*.sh' | sort); do shellcheck -x "$f"; done
  scripts con problemas: 0
```

La conducta no cambió — las seis entradas de la tabla de §11 dan el mismo veredicto sobre bash
3.2.57, incluida una con finales CRLF — y el Lab 00 completo vuelve a dar `5/5 · ESTACIÓN LISTA ·
EXIT=0` en el Mac.

### El rojo que no es nuestro — declarado, no silenciado

El job `deriva · labs en sincronía con su base` falla, **y también falla en `main`**:

```
main @ 191b008 (merge del PR #29)     deriva: failure      (los otros 7 jobs: success)
rama @ d9ae8d2                        deriva: failure
```

Es rojo desde el **PR #27** (SPEC-022); el tag `material-v0.3.1` está puesto sobre un `main` rojo.
La causa es estructural y conocida: `lab-08` deriva de `lab-07`, y las SPEC-022/023/024 migraron
el `lab-07` —`mvnw`, `mvnw.cmd`, `pom.xml`, los `application-*.yml`, las IT de Zonky— sin migrar
el `lab-08`, que es Fase 2. El guard cuenta 13 archivos divergiendo sin declararse.

**Y tiene razón.** Se decidió **no** declararlos en `derivacion-solucion.txt`, que era el atajo
disponible: esos archivos no divergen *a propósito*, divergen **porque el lab-08 va atrasado**.
Declararlos convertiría a un guard que dice la verdad en uno que calla — exactamente el defecto
que la SPEC-FIX-05 pasó una semana desmontando. El rojo es honesto y se apaga migrando el lab-08,
no editando su declaración.

**El merge de esta SPEC no empeora `main`:** entra con el mismo único job rojo que `main` ya
tenía, y devuelve `labs-sh` al verde. Queda anotado en `ESTADO.md` como deuda de Fase 2.

---

## 17 · Cierre

Ocho verificaciones cerradas. Tres anotaciones A2.x implementadas y verificadas en fierro. Un
vuelo en modo avión, cuatro vueltas en Windows ARM y una validación limpia en Windows x64
corporativo. Una anotación abierta —A2.4— clasificada con evidencia de dos máquinas como cosmética
y entregada a la próxima SPEC.

En posición de merge y tag `material-v0.4.0`.