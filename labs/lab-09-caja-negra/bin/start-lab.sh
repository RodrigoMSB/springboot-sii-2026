#!/usr/bin/env bash
# =============================================================================
#  start-lab.sh — levanta la DGT del Lab 09 (tu starter/, o la solución)
# -----------------------------------------------------------------------------
#    ./bin/start-lab.sh                     # levanta starter/ en el puerto 8099
#    ./bin/start-lab.sh --dir solucion      # levanta la solución
#    ./bin/start-lab.sh --caos              # …y genera el muro de logs: el crimen
#    ./bin/start-lab.sh --puerto 8100
# =============================================================================
set -uo pipefail

DIR_BIN="$(cd "$(dirname "$0")" && pwd)"
# shellcheck source-path=SCRIPTDIR
# shellcheck source=../../lib/lib-comunes.sh
. "$DIR_BIN/../../lib/lib-comunes.sh"

DIR_LAB="$(cd "$DIR_BIN/.." && pwd)"
ESTADO="$DIR_LAB/.estado"
LOG="$ESTADO/dgt.log"
PID="$ESTADO/dgt.pid"
PUERTO="$DGT_PUERTO_DEFECTO"
OBJETIVO="starter"
CAOS=0

es_numero() { printf '%s' "${1:-}" | grep -q '^[0-9][0-9]*$'; }

while [ $# -gt 0 ]; do
    case "$1" in
        --puerto)  PUERTO="${2:-}"; es_numero "$PUERTO" || { printf '[ERROR] --puerto necesita un número\n' >&2; exit 2; }; shift 2 ;;
        --puerto=*) PUERTO="${1#*=}"; es_numero "$PUERTO" || { printf '[ERROR] --puerto necesita un número\n' >&2; exit 2; }; shift ;;
        --dir)     OBJETIVO="${2:-}"; shift 2 ;;
        --dir=*)   OBJETIVO="${1#*=}"; shift ;;
        --caos)    CAOS=1; shift ;;
        -h|--help) printf 'Uso: %s [--dir starter|solucion] [--caos] [--puerto N]\n' "$(basename "$0")"; exit 0 ;;
        *) printf '[ERROR] Argumento no reconocido: %s\n' "$1" >&2; exit 2 ;;
    esac
done

APP="$DIR_LAB/$OBJETIVO"
[ -f "$APP/pom.xml" ] || { printf '[ERROR] No es un proyecto Maven: %s\n' "$APP" >&2; exit 2; }

mkdir -p "$ESTADO"
printf '\n  Levantando la DGT (%s, puerto %s)\n\n' "$OBJETIVO" "$PUERTO"

if [ -f "$PID" ] && kill -0 "$(cat "$PID")" 2>/dev/null; then
    paso_warn "La DGT ya está corriendo (PID $(cat "$PID"))" "Para empezar de cero:  ./bin/99-destruir.sh"
    exit 0
fi

if puerto_ocupado "$PUERTO"; then
    CULPABLE="$(quien_ocupa_puerto "$PUERTO")"
    paso_fail "El puerto $PUERTO ya está ocupado${CULPABLE:+ por: $CULPABLE}" \
              "No es un error tuyo. Usa otro:  ./bin/start-lab.sh --puerto $DGT_PUERTO_SUGERIDO   (ver T-01 del Lab 00)"
    printf '\n'; exit 1
fi

if ! docker info >/dev/null 2>&1; then
    paso_fail "El demonio de Docker no responde" "Abre Docker Desktop y espera a que arranque (T-03 del Lab 00)."
    printf '\n'; exit 1
fi

log_info "Arrancando… (log en $LOG)"
( cd "$APP" && exec nohup ./mvnw -q spring-boot:run \
      -Dspring-boot.run.arguments="--server.port=$PUERTO" \
      </dev/null >"$LOG" 2>&1 ) &
echo $! > "$PID"

if esperar_url "http://localhost:$PUERTO/actuator/health" 120; then
    paso_ok "La DGT está viva en el puerto $PUERTO"
else
    paso_fail "La DGT no respondió en 120 segundos" "Mira el log:  tail -n 40 $LOG"
    printf '\n'; exit 1
fi

BASE="http://localhost:$PUERTO/api/v1/tramites"
LOGIN="http://localhost:$PUERTO/api/v1/auth/login"

# -----------------------------------------------------------------------------
#  El crimen (--caos): 30 peticiones a la vez, y a buscar la aguja
# -----------------------------------------------------------------------------
token_de_carolina() {
    curl -s -X POST "$LOGIN" -H 'Content-Type: application/json' \
        -d '{"rut":"9876543-2","clave":"dgt-2026"}' | sed 's/.*"token":"//; s/".*//'
}

if [ "$CAOS" -eq 1 ]; then
    printf '\n  --- El crimen: 30 peticiones concurrentes, y una operación que encontrar ---\n\n'
    TOKEN="$(token_de_carolina)"
    if [ -z "$TOKEN" ]; then paso_warn "No pude autenticarme para el caos"; else
        log_info "Disparando 30 peticiones a la vez…"
        PIDS=""
        _i=0
        while [ "$_i" -lt 30 ]; do
            ( curl -s -o /dev/null --max-time 15 "$BASE" -H "Authorization: Bearer $TOKEN" ) &
            PIDS="$PIDS $!"
            ( curl -s -o /dev/null -X POST "$BASE" -H "Authorization: Bearer $TOKEN" \
                -H 'Content-Type: application/json' --max-time 15 -d '{"rutContribuyente":"11111111-1","tipo":"DECLARACION_F29"}' ) &
            PIDS="$PIDS $!"
            _i=$((_i + 1))
        done
        # wait SOLO por los curls (no por el proceso de la app, que nunca termina).
        # shellcheck disable=SC2086
        wait $PIDS
        sleep 1   # dejar que el log termine de escribirse

        printf '\n'
        TOTAL="$(wc -l < "$LOG" | tr -d ' ')"
        log_info "El log de la jornada: $TOTAL líneas."

        # ¿Hay traceId (un UUID) en el log? En la solución sí; en el starter, no.
        TRAZA="$(grep -oE '[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}' "$LOG" | head -1)"
        if [ -n "$TRAZA" ]; then
            CUANTAS="$(grep -c "$TRAZA" "$LOG")"
            printf '\n  Filtrando por UN traceId (%s):\n' "$TRAZA"
            printf '     grep %s dgt.log  ->  %s líneas: UNA operación, aislada y legible.\n\n' "$TRAZA" "$CUANTAS"
            paso_ok "La aguja encontrada: el traceId reconstruye la operación completa" \
                    "Esto es la solución. El log JSON se filtra por traceId."
        else
            REGS="$(grep -c 'peticion' "$LOG" 2>/dev/null || printf '0')"
            printf '\n  Buscando a mano en el muro (texto plano, sin traceId):\n'
            printf '     grep peticion dgt.log  ->  %s líneas entrelazadas de 60 peticiones.\n' "$REGS"
            printf '     Sin traceId, no hay forma de saber cuáles son de la MISMA operación.\n\n'
            paso_warn "El muro entrelazado: buscar a mano es imposible" \
                      "Ese es el crimen. No hay hilo que seguir."
        fi
    fi
fi

printf '\n'
printf '[OK] Pruébalo tú mismo:\n\n'
printf '     tail -n 20 %s\n' "$LOG"
printf '     grep <un-traceId> %s     # (solo la solución tiene traceId)\n\n' "$LOG"
log_info "Cuando termines:  ./bin/99-destruir.sh"
printf '\n'
