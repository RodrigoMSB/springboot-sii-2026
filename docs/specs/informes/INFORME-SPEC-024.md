# INFORME-SPEC-024 · El Java que viaja en la maleta

**SPEC:** SPEC-024 · **Ejecuta:** mocito · **Fecha:** 13 de agosto de 2026
**Rama:** `spec-024-jdk-embebido` · **Tag al cierre:** `material-v0.4.0`
**Máquina:** Mac Studio del PO — Darwin 25.5.0, `arm64`

---

## 1 · Veredicto en una línea

**EL CÍRCULO ESTÁ CERRADO** — el JDK 25 viaja partido en el repositorio, el shim lo ensambla
verificando su firma y lo usa **ignorando cualquier Java de la máquina**; el Lab 00 da
`ESTACIÓN LISTA` sin red y sin Docker, y la lista de prerrequisitos del curso quedó en una
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

**Duración estimada: 14 a 18 minutos** desde el corte. Son los 8m28s del vuelo 3 más el Lab 00,
más el ensamblado del JDK en dos clones distintos (~4 s cada uno) y el sobrecoste de arrancar
cada suite con un JDK recién extraído y sin caché de clases.

```bash
nohup tools/vuelo-4-modo-avion.sh > /tmp/vuelo4.out 2>&1 &
```

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

**7.4 · El `.cmd` no se pudo ejecutar.** Está en V8 con su reserva. No se disimula: la prueba
real es la del PO en Parallels.

**7.5 · Deuda heredada de la FIX-05, que consta aquí como se pidió.** Los `[OK]` corregidos de
los labs **08–14 siguen sin ejecución real** — necesitan Docker. Quedaron linteados y revisados,
pero nadie los ha visto correr. Es deuda explícita de la Fase 2, no de esta SPEC.

---

## 8 · Lo que queda

**Inmediato:** lanzar el vuelo 4 (§6) y la prueba del `.cmd` en Parallels.

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
