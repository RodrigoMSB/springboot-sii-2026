#!/usr/bin/env bash
# =============================================================================
#  00-verificar.sh — ¿está lista tu máquina para el curso?
# -----------------------------------------------------------------------------
#  Córrelo ANTES de la sesión 1. Cada [ERROR] trae una flecha "->" con qué hacer.
#  Si algo falla, no es culpa tuya: es información.
#
#    ./bin/00-verificar.sh
#
#  La lista de requisitos de este curso cabe en una línea: GIT. Nada más.
#
#  No hace falta Docker (PostgreSQL viaja como dependencia y corre como proceso
#  hijo), no hace falta Maven (viaja en tools/maven), no hace falta internet (las
#  dependencias viajan en repo-maven) y —desde la SPEC-024— tampoco hace falta
#  Java: el JDK 25 viaja partido en tools/jdk y el propio ./mvnw lo ensambla la
#  primera vez. Da igual el Java que tengas instalado, o si no tienes ninguno.
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
LAB01="$RAIZ/labs/lab-01-del-otro-lado-del-boton/starter"

while [ $# -gt 0 ]; do
    case "$1" in
        -h|--help)
            printf 'Uso: %s\n' "$(basename "$0")"
            exit 0 ;;
        --sin-docker)
            printf '[INFO]  --sin-docker ya no existe: el curso entero corre sin Docker.\n'
            printf '        Corre el script sin argumentos.\n'
            exit 0 ;;
        *)
            printf '[ERROR] Argumento no reconocido: %s\n' "$1" >&2
            exit 2 ;;
    esac
done

PLATAFORMA="$(detectar_plataforma)"

printf '\n'
printf '  Estación Base de la DGT — verificación de tu máquina\n'
printf '  Plataforma detectada: %s\n' "$PLATAFORMA"
printf '\n'

# -----------------------------------------------------------------------------
#  1 · Git — el único requisito de verdad
# -----------------------------------------------------------------------------
if requiere_comando git "Instala Git desde https://git-scm.com (en Windows trae Git Bash, que es tu terminal)."; then
    log_info "Tu Git: $(git --version 2>/dev/null)"
fi

# -----------------------------------------------------------------------------
#  2 · El clon está completo
# -----------------------------------------------------------------------------
#  Un clon a medias (antivirus, disco lleno, Ctrl-C a destiempo) da errores
#  rarísimos más adelante. Se comprueba aquí, que es barato, en vez de dejar que
#  reviente en el Lab 03.
FALTAN=""
for PIEZA in "tools/maven/bin/mvn" "repo-maven" "tools/jdk" "$LAB01/pom.xml"; do
    case "$PIEZA" in
        /*) RUTA="$PIEZA" ;;
        *)  RUTA="$RAIZ/$PIEZA" ;;
    esac
    [ -e "$RUTA" ] || FALTAN="$FALTAN $PIEZA"
done
if [ -z "$FALTAN" ]; then
    paso_ok "El clon tiene todas sus piezas (Maven, dependencias, JDK y los labs)"
else
    paso_fail "Al clon le faltan piezas:$FALTAN" \
              "Clona otra vez, entero. No uses 'Download ZIP' de GitHub."
fi

# -----------------------------------------------------------------------------
#  3 · El JDK embebido se ensambla y responde
# -----------------------------------------------------------------------------
#  Este es el paso que reemplazó al viejo "¿tienes Java 25 instalado?". Ya no se
#  le pregunta nada a tu máquina: se ensambla el JDK del repositorio y se le
#  pregunta a ÉL. La primera vez tarda unos segundos.
case "$PLATAFORMA" in
    macos)   PLAT_JDK="macos-aarch64"; SUB="/Contents/Home" ;;
    gitbash) PLAT_JDK="windows-x64";   SUB="" ;;
    *)       PLAT_JDK="";              SUB="" ;;
esac

if [ -z "$PLAT_JDK" ]; then
    paso_skip "JDK embebido: no se empaqueta para $PLATAFORMA" \
              "En esta plataforma ./mvnw usará el Java que tengas instalado."
elif [ ! -d "$RAIZ/tools/jdk/$PLAT_JDK" ]; then
    paso_fail "No encuentro tools/jdk/$PLAT_JDK en el clon" \
              "Clona otra vez, entero."
else
    log_info "Ensamblando el JDK embebido si hace falta (la primera vez tarda)…"
    if ( cd "$LAB01" && ./mvnw -q -version >/dev/null 2>&1 ); then
        JDK_VER="$(cat "$RAIZ/tools/jdk/$PLAT_JDK/VERSION" 2>/dev/null)"
        JAVA_EMB="$RAIZ/tools/jdk/runtime/$PLAT_JDK/$JDK_VER$SUB/bin/java"
        if [ -x "$JAVA_EMB" ] || [ -f "$JAVA_EMB.exe" ]; then
            paso_ok "JDK embebido listo: $("$JAVA_EMB" -version 2>&1 | head -n 1)"
        else
            paso_fail "El JDK no quedó donde se esperaba ($JAVA_EMB)" \
                      "Borra tools/jdk/runtime/ y vuelve a correr este script."
        fi
    else
        paso_fail "No pude ensamblar o usar el JDK embebido" \
                  "Corre  cd labs/lab-01-del-otro-lado-del-boton/starter && ./mvnw -version  y mándale la salida al instructor."
    fi
fi

# -----------------------------------------------------------------------------
#  4 · El Java del sistema — informativo, no requisito
# -----------------------------------------------------------------------------
#  Se muestra solo para que nadie se asuste: tu Java puede ser el 8, el 17 o
#  ninguno. El curso no lo usa ni lo toca.
if command -v java >/dev/null 2>&1; then
    log_info "Java del sistema: $(java -version 2>&1 | head -n 1)  — el curso NO lo usa"
else
    log_info "No tienes Java instalado — y no lo necesitas: el curso trae el suyo"
fi

# -----------------------------------------------------------------------------
#  5 · El wrapper resuelve al Maven del repositorio
# -----------------------------------------------------------------------------
if [ -x "$LAB01/mvnw" ]; then
    SALIDA_MVN="$( cd "$LAB01" && ./mvnw -version 2>&1 )"

    # Se compara el FINAL de la ruta, no la ruta entera, y con los separadores
    # normalizados. Es lo que este chequeo quiere saber de verdad: ¿el Maven que
    # corrió es el del repositorio, o uno del sistema?
    #
    # Comparar la ruta completa contra $RAIZ era un falso negativo garantizado en
    # Windows, y nos lo encontramos en una máquina real (SPEC-024 · A2.2): Git Bash
    # conoce el repo como `/c/SPRINGBOOT/…` y Maven imprime `C:\SPRINGBOOT\…`. No es
    # solo el separador: es que la misma carpeta se escribe de dos formas distintas,
    # y ninguna contiene a la otra. El chequeo daba [ERROR] con todo funcionando.
    #
    # La normalización de separadores va por expansión de parámetros y no por
    # `tr '\\' '/'`: es el mismo idioma que ya usa el shim (`${TAR_WIN//\\//}`),
    # y el `tr` disparaba SC1003 en shellcheck, que el CI trata como fallo.
    HOME_MVN="$(printf '%s' "$SALIDA_MVN" | grep -m1 '^Maven home:' \
                | sed 's/^Maven home: *//' | tr -d '\r')"
    HOME_MVN="${HOME_MVN//\\//}"
    case "$HOME_MVN" in
        */tools/maven|*/tools/maven/)
            paso_ok "./mvnw usa el Maven del repositorio: $(printf '%s' "$SALIDA_MVN" | grep -m1 '^Apache Maven')"
            ;;
        "")
            paso_fail "./mvnw no imprimió su 'Maven home'" \
                      "Mándale al instructor la salida de:  cd labs/lab-01-del-otro-lado-del-boton/starter && ./mvnw -version"
            ;;
        *)
            paso_fail "./mvnw está usando otro Maven: $HOME_MVN" \
                      "Debería ser el de tools/maven del repositorio. Mándale esta línea al instructor."
            ;;
    esac
else
    paso_fail "No encuentro $LAB01/mvnw" \
              "¿Clonaste el repo completo? Corre este script desde labs/lab-00-estacion-base/."
fi

# -----------------------------------------------------------------------------
#  6 · Espacio en disco
# -----------------------------------------------------------------------------
#  Bajó de 10 GB a 3: ya no hay imágenes de Docker que descargar. Lo que ocupa
#  es el clon (~1,2 GB) más el JDK que se extrae al vuelo (~300 MB por plataforma).
LIBRES_GB="$(df -g "$RAIZ" 2>/dev/null | awk 'NR==2 {print $4}')"
if [ -z "$LIBRES_GB" ]; then
    LIBRES_GB="$(df -k "$RAIZ" 2>/dev/null | awk 'NR==2 {print int($4/1048576)}')"
fi
if [ -z "$LIBRES_GB" ]; then
    paso_warn "No pude medir el espacio libre en disco" "Asegúrate de tener al menos 3 GB."
elif [ "$LIBRES_GB" -ge 3 ]; then
    paso_ok "Espacio libre: ${LIBRES_GB} GB"
else
    paso_fail "Solo ${LIBRES_GB} GB libres; el curso necesita ~3 GB" \
              "Libera espacio: el clon y el JDK extraído ocupan lo suyo."
fi

# -----------------------------------------------------------------------------
#  Lo que YA NO se verifica, y por qué
# -----------------------------------------------------------------------------
#  · Docker y su demonio: el curso no lo usa (SPEC-022/023). PostgreSQL viaja
#    como dependencia Maven y corre como proceso hijo del JVM.
#  · Conectividad a Maven Central y a Docker Hub: no se descarga nada. Todo
#    viaja en el repositorio. Este script no toca la red, y es a propósito —
#    puedes correrlo con el cable desenchufado y tiene que dar verde igual.
#  · "¿Tienes Java 25?": ya no se le pregunta a tu máquina (paso 3).

# -----------------------------------------------------------------------------
#  Veredicto
# -----------------------------------------------------------------------------
resumen_final "ESTACIÓN LISTA" "ESTACIÓN INCOMPLETA"
VEREDICTO=$?

printf '\n'
if [ "$VEREDICTO" -eq 0 ]; then
    log_info "Siguiente paso:  ./bin/start-lab.sh"
else
    log_info "Arregla los [ERROR] y vuelve a correr este script. Si te atascas,"
    log_info "mira docs/troubleshooting.md o mándale esta salida completa al instructor."
fi
printf '\n'

exit "$VEREDICTO"
