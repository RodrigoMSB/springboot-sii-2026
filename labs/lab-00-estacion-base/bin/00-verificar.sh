#!/usr/bin/env bash
# =============================================================================
#  00-verificar.sh — ¿está lista tu máquina para el curso?
# -----------------------------------------------------------------------------
#  Córrelo ANTES de la sesión 1. Cada [ERROR] trae una flecha "->" con qué hacer.
#  Si algo falla, no es culpa tuya: es información.
#
#    ./bin/00-verificar.sh                # verificación completa
#    ./bin/00-verificar.sh --sin-docker   # si tu institución no autorizó Docker
#
#  Sin `set -e`: este script es de SOLO LECTURA y acumula todas las fallas para
#  decírtelas juntas. Un validador que se detiene en el primer error te obliga a
#  correrlo siete veces.
# =============================================================================
set -uo pipefail

DIR_BIN="$(cd "$(dirname "$0")" && pwd)"
# La lib se resuelve desde la ubicación del script, no desde el cwd: el alumno
# puede invocarlo desde donde quiera.
# shellcheck source-path=SCRIPTDIR
# shellcheck source=../../lib/lib-comunes.sh
. "$DIR_BIN/../../lib/lib-comunes.sh"

RAIZ="$(raiz_repo)"
APP="$RAIZ/dgt-tramites-api"
SIN_DOCKER=0
JAVA_MINIMO=25

while [ $# -gt 0 ]; do
    case "$1" in
        --sin-docker) SIN_DOCKER=1; shift ;;
        -h|--help)
            printf 'Uso: %s [--sin-docker]\n' "$(basename "$0")"
            exit 0 ;;
        *)
            printf '[ERROR] Argumento no reconocido: %s  (usa --sin-docker)\n' "$1" >&2
            exit 2 ;;
    esac
done

PLATAFORMA="$(detectar_plataforma)"

printf '\n'
printf '  Estación Base de la DGT — verificación de tu máquina\n'
printf '  Plataforma detectada: %s\n' "$PLATAFORMA"
[ "$SIN_DOCKER" -eq 1 ] && printf '  Modo: SIN DOCKER (capacidades reducidas)\n'
printf '\n'

# -----------------------------------------------------------------------------
#  1 · Java 25
# -----------------------------------------------------------------------------
#  `java -version` escribe en stderr, y el formato varía entre distribuciones.
#  Tomamos el primer número: "25.0.3" -> 25, "1.8.0_401" -> 1 (y falla, bien).
if command -v java >/dev/null 2>&1; then
    VERSION_CRUDA="$(java -version 2>&1 | head -n 1)"
    VERSION_MAYOR="$(printf '%s' "$VERSION_CRUDA" | sed -n 's/.*version "\([0-9][0-9]*\).*/\1/p')"
    if [ -z "$VERSION_MAYOR" ]; then
        paso_fail "No pude leer la versión de Java (dijo: $VERSION_CRUDA)" \
                  "Reporta esta línea al instructor: es un formato que no conocíamos."
    elif [ "$VERSION_MAYOR" -ge "$JAVA_MINIMO" ]; then
        paso_ok "Java $VERSION_MAYOR (el curso pide $JAVA_MINIMO)"
    else
        paso_fail "Java $VERSION_MAYOR es muy antiguo; el curso pide $JAVA_MINIMO" \
                  "Instala Temurin $JAVA_MINIMO desde https://adoptium.net y revisa tu JAVA_HOME."
    fi
else
    paso_fail "No encuentro 'java' en el PATH" \
              "Instala Temurin $JAVA_MINIMO desde https://adoptium.net (guía 01 del lab)."
fi

# -----------------------------------------------------------------------------
#  2 · Git
# -----------------------------------------------------------------------------
requiere_comando git "Instala Git desde https://git-scm.com (en Windows trae Git Bash, que es tu terminal)."

# -----------------------------------------------------------------------------
#  3 · Docker (o Podman) con el DEMONIO VIVO
# -----------------------------------------------------------------------------
#  Instalado no es lo mismo que corriendo. El 90 % de los "no me funciona" del
#  primer día es Docker Desktop cerrado.
if [ "$SIN_DOCKER" -eq 1 ]; then
    paso_skip "Docker (modo --sin-docker)" \
              "Sin Docker no correrás Testcontainers ni la imagen OCI: esos temas serán demo del relator."
else
    MOTOR=""
    command -v docker >/dev/null 2>&1 && MOTOR="docker"
    [ -z "$MOTOR" ] && command -v podman >/dev/null 2>&1 && MOTOR="podman"

    if [ -z "$MOTOR" ]; then
        paso_fail "No encuentro ni 'docker' ni 'podman'" \
                  "Instala Docker Desktop (https://docker.com). Si tu institución no lo autoriza, corre: ./bin/00-verificar.sh --sin-docker (ver T-04)."
    elif "$MOTOR" info >/dev/null 2>&1; then
        paso_ok "$MOTOR está instalado y su demonio responde"
    else
        paso_fail "$MOTOR está instalado, pero su demonio NO responde" \
                  "Abre Docker Desktop y espera a que la ballena deje de moverse. Luego repite (ver T-03)."
    fi
fi

# -----------------------------------------------------------------------------
#  4 · El proyecto y su wrapper
# -----------------------------------------------------------------------------
if [ -x "$APP/mvnw" ]; then
    if ( cd "$APP" && ./mvnw -q -version >/dev/null 2>&1 ); then
        paso_ok "El Maven Wrapper funciona en dgt-tramites-api/"
    else
        paso_fail "./mvnw existe pero no corre" \
                  "Suele ser JAVA_HOME apuntando a otra versión. Comprueba: java -version (ver T-02)."
    fi
else
    paso_fail "No encuentro dgt-tramites-api/mvnw" \
              "¿Clonaste el repo completo? Corre este script desde labs/lab-00-estacion-base/."
fi

# -----------------------------------------------------------------------------
#  5 · Conectividad: Maven Central
# -----------------------------------------------------------------------------
if curl -sf -o /dev/null --max-time 10 https://repo1.maven.org/maven2/ 2>/dev/null; then
    paso_ok "Llegas a Maven Central"
else
    paso_fail "No llego a Maven Central (repo1.maven.org)" \
              "Si estás tras un proxy corporativo, configúralo en ~/.m2/settings.xml (ver T-05)."
fi

# -----------------------------------------------------------------------------
#  6 · Conectividad: Docker Hub
# -----------------------------------------------------------------------------
if [ "$SIN_DOCKER" -eq 1 ]; then
    paso_skip "Docker Hub (modo --sin-docker)" "No descargarás imágenes."
elif curl -sf -o /dev/null --max-time 10 https://hub.docker.com/ 2>/dev/null; then
    paso_ok "Llegas a Docker Hub"
else
    paso_fail "No llego a Docker Hub" \
              "Sin esto no podrás bajar PostgreSQL. Revisa proxy/firewall (ver T-05)."
fi

# -----------------------------------------------------------------------------
#  7 · Espacio en disco (el curso baja ~3 GB entre imágenes y dependencias)
# -----------------------------------------------------------------------------
LIBRES_GB="$(df -g "$RAIZ" 2>/dev/null | awk 'NR==2 {print $4}')"
if [ -z "$LIBRES_GB" ]; then
    LIBRES_GB="$(df -k "$RAIZ" 2>/dev/null | awk 'NR==2 {print int($4/1048576)}')"
fi
if [ -z "$LIBRES_GB" ]; then
    paso_warn "No pude medir el espacio libre en disco" "Asegúrate de tener al menos 10 GB."
elif [ "$LIBRES_GB" -ge 10 ]; then
    paso_ok "Espacio libre: ${LIBRES_GB} GB"
else
    paso_fail "Solo ${LIBRES_GB} GB libres; el curso necesita ~10 GB" \
              "Libera espacio: las imágenes de Docker y ~/.m2 pesan."
fi

# -----------------------------------------------------------------------------
#  Veredicto
# -----------------------------------------------------------------------------
if [ "$SIN_DOCKER" -eq 1 ]; then
    resumen_final "ESTACIÓN LISTA (MODO SIN DOCKER: capacidades reducidas)" \
                  "ESTACIÓN INCOMPLETA"
else
    resumen_final "ESTACIÓN LISTA" "ESTACIÓN INCOMPLETA"
fi
VEREDICTO=$?

printf '\n'
if [ "$VEREDICTO" -eq 0 ] && [ "$SIN_DOCKER" -eq 1 ]; then
    # Sin Docker no hay PostgreSQL, y sin PostgreSQL la API no arranca. Decirlo
    # ahora es honesto; dejar que el alumno choque contra start-lab.sh, no.
    log_info "Tu estación sirve para leer, compilar y correr los tests unitarios."
    log_info "NO podrás levantar la app (necesita PostgreSQL en un contenedor):"
    log_info "eso lo verás como demo del relator. Habla con el instructor."
elif [ "$VEREDICTO" -eq 0 ]; then
    log_info "Siguiente paso:  ./bin/start-lab.sh"
else
    log_info "Arregla los [ERROR] y vuelve a correr este script. Si te atascas,"
    log_info "mira docs/troubleshooting.md o mándale esta salida completa al instructor."
fi
printf '\n'

exit "$VEREDICTO"
