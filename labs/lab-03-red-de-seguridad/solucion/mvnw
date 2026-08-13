#!/usr/bin/env bash
# =============================================================================
#  mvnw — el Maven Y el Java de este curso (SPEC-022 · D-022-2, SPEC-024 · D-024-3)
# -----------------------------------------------------------------------------
#  Esto YA NO es el Maven Wrapper de Apache. El wrapper original era un
#  DESCARGADOR: al primer uso se bajaba una distribución de Maven de internet.
#  En las máquinas del SII no hay internet, así que se quedaba colgado en el
#  primer comando del primer día.
#
#  Este shim no descarga nada. Usa:
#
#      · el Maven que viaja en el repositorio  (tools/maven)
#      · las dependencias que viajan con él    (repo-maven)
#      · y el JDK 25 que también viaja         (tools/jdk)
#
#  Ese último es el que te ahorra la pelea: da igual que tu máquina tenga Java
#  17, Java 21, un JAVA_HOME apuntando a una carpeta que ya no existe, o ningún
#  Java. Este shim usa el suyo y no le pregunta a nadie.
#
#  Para ti no cambia nada. Sigue siendo:
#
#      ./mvnw test
#      ./mvnw spring-boot:run
#
#  La PRIMERA vez tarda unos segundos más: tiene que ensamblar el JDK desde los
#  trozos en que viaja (GitHub no acepta archivos de más de 100 MB) y verificar
#  su firma. Después, el costo es comprobar que un archivo existe.
#
#  ---------------------------------------------------------------------------
#  DGT_ONLINE=1 · solo para quien PREPARA el material
#
#      DGT_ONLINE=1 ./mvnw verify
#
#  Omite el modo offline y el repositorio embebido, y sale a internet como un
#  Maven normal. El alumno no lo necesita nunca.
# =============================================================================
set -uo pipefail

# --- La raíz del repositorio, subiendo hasta dar con el marcador -------------
# Sin git, sin rutas absolutas y sin variables de entorno: así el material sigue
# funcionando si el alumno lo clona donde se le antoje.
DIR_PROYECTO="$(cd "$(dirname "$0")" && pwd)"
RAIZ="$DIR_PROYECTO"
while [ ! -x "$RAIZ/tools/maven/bin/mvn" ]; do
    PADRE="$(dirname "$RAIZ")"
    if [ "$PADRE" = "$RAIZ" ]; then
        printf '[ERROR] No encuentro tools/maven/ subiendo desde:\n'                     >&2
        printf '        %s\n'                                    "$DIR_PROYECTO"         >&2
        printf '\n'                                                                       >&2
        printf '        Este proyecto tiene que vivir DENTRO del repositorio del\n'      >&2
        printf '        curso. Si copiaste la carpeta a otro sitio, vuelve a\n'          >&2
        printf '        trabajar sobre el clon original.\n'                              >&2
        exit 1
    fi
    RAIZ="$PADRE"
done

# =============================================================================
#  El JDK que viaja en la maleta
# =============================================================================
# Qué plataforma es esta, y por tanto qué paquete le toca.
JDK_PLATAFORMA=""
JDK_ARCHIVO=""
JDK_SUBDIR=""
case "$(uname -s)" in
    Darwin)
        # Solo se empaqueta Apple Silicon: es la máquina donde se prepara el
        # material. En un Mac Intel se cae al Java del sistema (ver abajo).
        if [ "$(uname -m)" = "arm64" ]; then
            JDK_PLATAFORMA="macos-aarch64"
            JDK_ARCHIVO="jdk.tar.gz"
            # El tar.gz de Temurin para macOS trae el JDK dentro de un bundle.
            JDK_SUBDIR="/Contents/Home"
        fi
        ;;
    MINGW*|MSYS*|CYGWIN*)
        JDK_PLATAFORMA="windows-x64"
        JDK_ARCHIVO="jdk.zip"
        ;;
esac

# extraer_jdk <archivo-en-el-directorio-actual>
#
# En macOS el .tar.gz lo abre cualquier tar. En Windows es OTRA historia, y la
# aprendimos en una máquina real (SPEC-024 · A2.1):
#
#   El `tar` que Git Bash pone en el PATH es GNU tar, y GNU tar NO abre ZIP:
#       tar: This does not look like a tar archive
#   El que sí lo abre es el bsdtar que Windows trae en System32 — existe en toda
#   máquina Windows 10+, pero queda DETRÁS en el PATH de Git Bash. Así que se
#   invoca por su ruta completa, sin depender de a quién resuelva `tar`.
#
# Se le pasa el archivo por su nombre relativo, con el cwd ya puesto: así no hay
# que traducir rutas POSIX a rutas de Windows para un binario nativo.
extraer_jdk() {
    if [ "$JDK_PLATAFORMA" != "windows-x64" ]; then
        tar -xf "$1"
        return $?
    fi

    TAR_WIN="${SYSTEMROOT:-C:/Windows}"
    TAR_WIN="${TAR_WIN//\\//}/System32/tar.exe"     # SYSTEMROOT viene con \ de Windows
    if [ -x "$TAR_WIN" ]; then
        "$TAR_WIN" -xf "$1"
        return $?
    fi

    if command -v unzip >/dev/null 2>&1; then
        unzip -q "$1"
        return $?
    fi

    printf '[ERROR] No encuentro con qué descomprimir el JDK en esta máquina.\n'      >&2
    printf '        Busqué, en este orden:\n'                                          >&2
    printf '          1. %s\n' "$TAR_WIN"                                              >&2
    printf '          2. unzip en el PATH\n'                                           >&2
    printf '\n'                                                                        >&2
    printf '        El tar de Git Bash no sirve: es GNU tar y no abre archivos\n'     >&2
    printf '        ZIP. El de Windows sí, y vive en System32 — si no está ahí,\n'    >&2
    printf '        esta máquina es más rara de lo que el curso puede manejar.\n'     >&2
    printf '        Avísale al instructor con esta pantalla.\n'                        >&2
    return 1
}

# sha256: cada sistema lo llama de otra forma. macOS base no trae sha256sum.
sha256_de() {
    if command -v shasum >/dev/null 2>&1; then
        shasum -a 256 "$1" | awk '{print $1}'
    elif command -v sha256sum >/dev/null 2>&1; then
        sha256sum "$1" | awk '{print $1}'
    else
        printf 'sin-herramienta-sha256'
    fi
}

JDK_DIR="$RAIZ/tools/jdk/$JDK_PLATAFORMA"

if [ -n "$JDK_PLATAFORMA" ] && [ -d "$JDK_DIR" ]; then
    JDK_VERSION="$(cat "$JDK_DIR/VERSION" 2>/dev/null)"
    JDK_ESPERADO="$(cat "$JDK_DIR/$JDK_ARCHIVO.sha256" 2>/dev/null)"
    JDK_RUNTIME="$RAIZ/tools/jdk/runtime/$JDK_PLATAFORMA"
    JDK_SELLO="$JDK_RUNTIME/.listo"
    JDK_HOME="$JDK_RUNTIME/$JDK_VERSION$JDK_SUBDIR"

    # ¿Hay que ensamblar? Solo si no hay sello, o el sello no es de ESTE paquete
    # (por ejemplo, tras un `git pull` que trajo una versión nueva del JDK).
    if [ ! -f "$JDK_SELLO" ] || [ "$(cat "$JDK_SELLO" 2>/dev/null)" != "$JDK_ESPERADO" ]; then
        printf '[INFO]  Primera vez: ensamblando el JDK %s del repositorio…\n' "$JDK_VERSION"
        printf '        (solo ocurre una vez; después arranca directo)\n'

        rm -rf "$JDK_RUNTIME"
        mkdir -p "$JDK_RUNTIME"
        JDK_TMP="$JDK_RUNTIME/$JDK_ARCHIVO"

        if ! cat "$JDK_DIR/$JDK_ARCHIVO".part-* > "$JDK_TMP" 2>/dev/null; then
            printf '[ERROR] No pude juntar los trozos del JDK en %s\n' "$JDK_DIR" >&2
            printf '        ¿Está el clon completo? Prueba con un clon fresco.\n'  >&2
            rm -rf "$JDK_RUNTIME"; exit 1
        fi

        JDK_REAL="$(sha256_de "$JDK_TMP")"
        if [ "$JDK_REAL" != "$JDK_ESPERADO" ]; then
            printf '[ERROR] El JDK ensamblado NO coincide con su firma.\n'            >&2
            printf '          esperado: %s\n' "$JDK_ESPERADO"                          >&2
            printf '          obtenido: %s\n' "$JDK_REAL"                              >&2
            printf '\n'                                                                >&2
            printf '        No voy a usar un JDK que no puedo verificar. Lo más\n'    >&2
            printf '        probable es un clon incompleto o un antivirus que tocó\n' >&2
            printf '        los archivos. Solución: clon fresco del repositorio.\n'   >&2
            rm -rf "$JDK_RUNTIME"; exit 1
        fi

        if ! ( cd "$JDK_RUNTIME" && extraer_jdk "$JDK_ARCHIVO" ); then
            printf '[ERROR] No pude extraer el JDK en %s\n' "$JDK_RUNTIME" >&2
            rm -rf "$JDK_RUNTIME"; exit 1
        fi
        rm -f "$JDK_TMP"

        if [ ! -x "$JDK_HOME/bin/java" ] && [ ! -f "$JDK_HOME/bin/java.exe" ]; then
            printf '[ERROR] El JDK se extrajo pero no encuentro su java en:\n'   >&2
            printf '        %s/bin\n' "$JDK_HOME"                                 >&2
            rm -rf "$JDK_RUNTIME"; exit 1
        fi

        printf '%s' "$JDK_ESPERADO" > "$JDK_SELLO"
        printf '[INFO]  JDK %s listo en tools/jdk/runtime/\n' "$JDK_VERSION"
    fi

    # Solo para ESTE proceso. No se toca el entorno del alumno, ni su .bashrc,
    # ni su JAVA_HOME: lo que tenga instalado sigue exactamente como estaba.
    export JAVA_HOME="$JDK_HOME"
    PATH="$JAVA_HOME/bin:$PATH"
    export PATH
fi

MVN="$RAIZ/tools/maven/bin/mvn"

if [ "${DGT_ONLINE:-0}" = "1" ]; then
    exec "$MVN" "$@"
fi

exec "$MVN" --offline "-Dmaven.repo.local=$RAIZ/repo-maven" "$@"
