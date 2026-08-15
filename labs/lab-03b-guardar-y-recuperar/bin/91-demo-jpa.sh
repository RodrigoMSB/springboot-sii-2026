#!/usr/bin/env bash
# =============================================================================
#  91-demo-jpa.sh — guardar y recuperar, en vivo
# -----------------------------------------------------------------------------
#    ./bin/91-demo-jpa.sh                  # contra lo que esté levantado
#    ./bin/91-demo-jpa.sh --puerto 8100
#
#  Dos peticiones. Un objeto Java entra a la base y vuelve:
#
#    POST /api/internal/observaciones      -> guardar (INSERT, sin escribir SQL)
#    GET  /api/internal/observaciones?rut= -> recuperar (SELECT, sin escribir SQL)
#
#  Si la aplicación se levantó con `--ver-sql`, al final se muestran las líneas
#  de SQL que Hibernate generó — que es lo que de verdad hay que mirar.
#
#  Este script NO levanta ni baja la aplicación: la quiere ya arriba.
#      ./bin/start-lab.sh                 (y al terminar, ./bin/99-destruir.sh)
# =============================================================================
set -uo pipefail

DIR_BIN="$(cd "$(dirname "$0")" && pwd)"
# shellcheck source-path=SCRIPTDIR
# shellcheck source=../../lib/lib-comunes.sh
. "$DIR_BIN/../../lib/lib-comunes.sh"

DIR_LAB="$(cd "$DIR_BIN/.." && pwd)"
LOG="$DIR_LAB/.estado/dgt.log"
PUERTO="$DGT_PUERTO_DEFECTO"
RUT="11111111-1"

es_numero() { printf '%s' "${1:-}" | grep -q '^[0-9][0-9]*$'; }

while [ $# -gt 0 ]; do
    case "$1" in
        --puerto)   PUERTO="${2:-}"; es_numero "$PUERTO" || { printf '[ERROR] --puerto necesita un número\n' >&2; exit 2; }; shift 2 ;;
        --puerto=*) PUERTO="${1#*=}"; es_numero "$PUERTO" || { printf '[ERROR] --puerto necesita un número\n' >&2; exit 2; }; shift ;;
        --rut)      RUT="${2:-}"; shift 2 ;;
        --rut=*)    RUT="${1#*=}"; shift ;;
        -h|--help)  printf 'Uso: %s [--puerto N] [--rut RUT]\n' "$(basename "$0")"; exit 0 ;;
        *) printf '[ERROR] Argumento no reconocido: %s\n' "$1" >&2; exit 2 ;;
    esac
done

BASE="http://localhost:$PUERTO/api/internal/observaciones"
TEXTO="Observación de prueba escrita por la demo."

printf '\n  Guardar y recuperar · JPA en vivo\n\n'

if ! curl -sS -m 10 -o /dev/null "http://localhost:$PUERTO/actuator/health" 2>/dev/null; then
    paso_fail "La DGT no responde en el puerto $PUERTO" \
              "Levántala primero:  ./bin/start-lab.sh"
    printf '\n'; exit 1
fi

# Cuántas líneas tenía el log antes: así al final se muestran SOLO las de esta demo.
LINEAS_ANTES=0
[ -f "$LOG" ] && LINEAS_ANTES="$(wc -l < "$LOG" | tr -d ' ')"

printf '  ── 1 · Guardar un objeto ────────────────────────────────────────────\n\n'
printf '     POST %s\n' "$BASE"
printf '     {"rut":"%s","texto":"%s","autor":"Carolina Espinoza"}\n\n' "$RUT" "$TEXTO"

CUERPO="$(printf '{"rut":"%s","texto":"%s","autor":"Carolina Espinoza"}' "$RUT" "$TEXTO")"
R1="$(curl -sS -m 20 -w '\n%{http_code}' -X POST "$BASE" \
        -H 'Content-Type: application/json' -d "$CUERPO")"
COD1="$(printf '%s' "$R1" | tail -1)"
printf '%s\n' "$(printf '%s' "$R1" | sed '$d')" | sed 's/^/     /'
printf '\n     -> HTTP %s\n\n' "$COD1"

if [ "$COD1" != "201" ]; then
    paso_warn "El guardado respondió HTTP $COD1" \
              "Si estás en el starter, es lo esperado hasta que completes los TODOs. Mira el log: tail -n 30 $LOG"
    printf '\n'; exit 0
fi

printf '  ── 2 · Recuperarlo ──────────────────────────────────────────────────\n\n'
printf '     GET %s?rut=%s\n\n' "$BASE" "$RUT"
R2="$(curl -sS -m 20 -G "$BASE" --data-urlencode "rut=$RUT")"
printf '%s\n' "$R2" | sed 's/},{/},\n     {/g' | sed 's/^/     /'
CUANTAS="$(printf '%s' "$R2" | grep -o '"texto"' | wc -l | tr -d ' ')"
printf '\n     -> %s observación(es) para %s\n\n' "$CUANTAS" "$RUT"

printf '  ────────────────────────────────────────────────────────────────────\n'
if printf '%s' "$R2" | grep -q "$TEXTO"; then
    paso_ok "El objeto que guardaste volvió de la base" \
            "Entró como objeto Java, se guardó como fila, y volvió como objeto."
else
    paso_warn "No encontré en la respuesta lo que se acababa de guardar" \
              "Revisa el log:  tail -n 30 $LOG"
fi

# ---------------------------------------------------------------------------
#  El SQL, si la app se levantó con --ver-sql
# ---------------------------------------------------------------------------
if [ -f "$LOG" ]; then
    # Hibernate escribe la sentencia en las lineas SIGUIENTES a su marca de log, y
    # formateada. Se captura desde cada marca hasta la proxima linea de log (las
    # que empiezan por una fecha), que es como queda legible el JOIN.
    SQL="$(tail -n "+$((LINEAS_ANTES + 1))" "$LOG" 2>/dev/null | awk '
        /org\.hibernate\.SQL/ { dentro = 1; sentencias++; next }
        dentro && /^[0-9]{4}-[0-9]{2}-[0-9]{2}T/ { dentro = 0 }
        dentro && sentencias <= 2 && NF { print }
    ')"
    if [ -n "$SQL" ]; then
        printf '\n  ── 3 · Y este es el SQL que tú no escribiste ────────────────────────\n\n'
        printf '%s\n' "$SQL" | sed 's/^/     /'
        printf '\n     Salió de tu entidad y de tu repositorio. Cero líneas de SQL tuyas.\n'
    else
        printf '\n'
        log_info "¿Quieres ver el SQL? Levanta así:  ./bin/99-destruir.sh && ./bin/start-lab.sh --ver-sql"
    fi
fi
printf '\n'
