#!/usr/bin/env bash
# =============================================================================
#  95-recuperar.sh — la red de seguridad
# -----------------------------------------------------------------------------
#    ./bin/95-recuperar.sh --solo-enunciado   # restaura los tests del enunciado
#    ./bin/95-recuperar.sh --todo             # copia la solución ENTERA sobre tu trabajo
#    ./bin/95-recuperar.sh --todo --dir <ruta>  # (uso interno del 91-e2e)
#
#  RESPALDA ANTES DE SOBRESCRIBIR. Siempre. Tu trabajo se guarda en
#  `.respaldo-<fecha>/` y el script te dice dónde quedó.
#
#  `--todo` existe para cuando te atascas y para el 91. No es hacer trampa: es
#  comparar. Pero si lo usas, anótalo en tu reporte — la casilla existe, y
#  responderla con honestidad vale más que un lab perfecto.
# =============================================================================
set -uo pipefail

DIR_BIN="$(cd "$(dirname "$0")" && pwd)"
# shellcheck source-path=SCRIPTDIR
# shellcheck source=../../lib/lib-comunes.sh
. "$DIR_BIN/../../lib/lib-comunes.sh"

DIR_LAB="$(cd "$DIR_BIN/.." && pwd)"
SOLUCION="$DIR_LAB/solucion"
DESTINO="$DIR_LAB/starter"
MODO=""

while [ $# -gt 0 ]; do
    case "$1" in
        --solo-enunciado) MODO="enunciado"; shift ;;
        --todo)           MODO="todo"; shift ;;
        --dir)            DESTINO="${2:-}"; shift 2 ;;
        --dir=*)          DESTINO="${1#*=}"; shift ;;
        -h|--help) printf 'Uso: %s [--solo-enunciado|--todo] [--dir <ruta>]\n' "$(basename "$0")"; exit 0 ;;
        *) printf '[ERROR] Argumento no reconocido: %s\n' "$1" >&2; exit 2 ;;
    esac
done

case "$DESTINO" in /*) ;; *) DESTINO="$DIR_LAB/$DESTINO" ;; esac

[ -n "$MODO" ] || { printf '[ERROR] Elige --solo-enunciado o --todo\n' >&2; exit 2; }
[ -d "$SOLUCION" ] || { printf '[ERROR] No encuentro solucion/\n' >&2; exit 2; }
[ -d "$DESTINO" ]  || { printf '[ERROR] No encuentro el destino: %s\n' "$DESTINO" >&2; exit 2; }

printf '\n  Recuperando (%s)\n\n' "$MODO"

# --- Respaldo, siempre, antes de tocar nada -----------------------------------
SELLO="$(git -C "$DIR_LAB" log -1 --format=%h 2>/dev/null || printf 'sin-git')"
RESPALDO="$DESTINO/../.respaldo-$(basename "$DESTINO")-$SELLO-$$"
mkdir -p "$RESPALDO"
if ( cd "$DESTINO" && tar cf - --exclude='./target' --exclude='./.estado' . ) | ( cd "$RESPALDO" && tar xf - ); then
    paso_ok "Tu trabajo quedó respaldado en $(basename "$RESPALDO")/"
else
    paso_fail "No pude respaldar tu trabajo" \
              "No sigo: recuperar sin respaldo es perderlo. Revisa permisos en $(dirname "$RESPALDO")/"
    resumen_final "Recuperación completa" "La recuperación falló"
    exit 1
fi

ENUNCIADO="src/test/java/cl/dgt/tramites/enunciado"

if [ "$MODO" = "enunciado" ]; then
    borrar_seguro "$DESTINO/$ENUNCIADO"
    mkdir -p "$DESTINO/$ENUNCIADO"
    if cp "$SOLUCION/$ENUNCIADO"/*.java "$DESTINO/$ENUNCIADO/"; then
        borrar_seguro "$DESTINO/target"
        paso_ok "Tests del enunciado restaurados"
    else
        paso_fail "No pude restaurar los tests del enunciado" \
                  "Comprueba que existe $SOLUCION/$ENUNCIADO/"
    fi
else
    if ( cd "$SOLUCION" && tar cf - --exclude='./target' --exclude='./.estado' . ) | ( cd "$DESTINO" && tar xf - ); then
        # tar preserva fechas: un .class viejo puede tapar la fuente nueva. Limpia el build.
        borrar_seguro "$DESTINO/target"
        paso_ok "Solución aplicada sobre $(basename "$DESTINO")/"
    else
        paso_fail "No pude aplicar la solución" "Comprueba que existe $SOLUCION/"
    fi
fi

resumen_final "Recuperación completa" "La recuperación falló"
VEREDICTO=$?
printf '\n'
log_info "Tu versión anterior sigue ahí: $RESPALDO"
printf '\n'
exit "$VEREDICTO"
