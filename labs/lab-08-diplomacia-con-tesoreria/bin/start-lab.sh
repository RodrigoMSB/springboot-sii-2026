#!/usr/bin/env bash
# =============================================================================
#  start-lab.sh — levanta la DGT del Lab 08 (tu starter/, o la solución)
# -----------------------------------------------------------------------------
#    ./bin/start-lab.sh                        # levanta starter/ en el puerto 8099
#    ./bin/start-lab.sh --dir solucion         # levanta la solución
#    ./bin/start-lab.sh --teso-lento 30000     # …y pone a TESO a 30 s: el crimen
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
TESO_LENTO=0
TESO_PUERTO=8089

es_numero() { printf '%s' "${1:-}" | grep -q '^[0-9][0-9]*$'; }

while [ $# -gt 0 ]; do
    case "$1" in
        --puerto)  PUERTO="${2:-}"; es_numero "$PUERTO" || { printf '[ERROR] --puerto necesita un número\n' >&2; exit 2; }; shift 2 ;;
        --puerto=*) PUERTO="${1#*=}"; es_numero "$PUERTO" || { printf '[ERROR] --puerto necesita un número\n' >&2; exit 2; }; shift ;;
        --dir)     OBJETIVO="${2:-}"; shift 2 ;;
        --dir=*)   OBJETIVO="${1#*=}"; shift ;;
        --teso-lento)   TESO_LENTO="${2:-}"; es_numero "$TESO_LENTO" || { printf '[ERROR] --teso-lento necesita milisegundos\n' >&2; exit 2; }; shift 2 ;;
        --teso-lento=*) TESO_LENTO="${1#*=}"; es_numero "$TESO_LENTO" || { printf '[ERROR] --teso-lento necesita milisegundos\n' >&2; exit 2; }; shift ;;
        -h|--help) printf 'Uso: %s [--dir starter|solucion] [--teso-lento MS] [--puerto N]\n' "$(basename "$0")"; exit 0 ;;
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

# El WARN condicional: TESO lento y sin timeout es el escenario de la Guía 02.
if [ "$TESO_LENTO" -gt 800 ]; then
    paso_warn "Con TESO a $TESO_LENTO ms y sin timeout, el pool se agota." \
              "Es el escenario de la Guía 02. En el starter, la API entera se cuelga."
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
TESO_ADMIN="http://localhost:$TESO_PUERTO/__admin/mappings"

# -----------------------------------------------------------------------------
#  El crimen (--teso-lento): TESO se cuelga y se lleva a la API entera
# -----------------------------------------------------------------------------
token_de_carolina() {
    curl -s -X POST "$LOGIN" -H 'Content-Type: application/json' \
        -d '{"rut":"9876543-2","clave":"dgt-2026"}' | sed 's/.*"token":"//; s/".*//'
}
crear_presentado() {
    _t="$1"
    _id="$(curl -s -X POST "$BASE" -H "Authorization: Bearer $_t" -H 'Content-Type: application/json' \
        -d '{"rutContribuyente":"11111111-1","tipo":"DECLARACION_F29"}' | grep -oE '"id":[0-9]+' | grep -oE '[0-9]+')"
    [ -n "$_id" ] && curl -s -o /dev/null -X POST "$BASE/$_id/avanzar?a=PRESENTADO" -H "Authorization: Bearer $_t"
    printf '%s' "$_id"
}

if [ "$TESO_LENTO" -gt 0 ]; then
    printf '\n  --- El crimen: TESO a %s ms ---\n\n' "$TESO_LENTO"

    # 1) Poner a TESO lento (prioridad alta para ganarle al mapping por defecto sin retraso).
    curl -s -o /dev/null -X POST "$TESO_ADMIN" -H 'Content-Type: application/json' \
        -d "{\"priority\":1,\"request\":{\"method\":\"GET\",\"urlPathPattern\":\"/pagos/.*\"},\"response\":{\"status\":200,\"jsonBody\":{\"confirmado\":true},\"fixedDelayMilliseconds\":$TESO_LENTO}}"
    log_info "TESO configurado para tardar $TESO_LENTO ms en cada confirmación."

    TOKEN="$(token_de_carolina)"
    if [ -z "$TOKEN" ]; then paso_warn "No pude autenticarme para el sabotaje"; else
        # Primero creamos los trámites (rápido: crear no toca a TESO), y RECIÉN entonces
        # disparamos todos los pagos juntos, para que cuelguen a la vez y llenen el pool.
        log_info "Creando 12 trámites…"
        IDS=""
        _i=0
        while [ "$_i" -lt 12 ]; do
            _id="$(crear_presentado "$TOKEN")"
            [ -n "$_id" ] && IDS="$IDS $_id"
            _i=$((_i + 1))
        done
        log_info "Disparando 12 confirmaciones de pago EN PARALELO (cada una cuelga en TESO)…"
        for _id in $IDS; do
            ( curl -s -o /dev/null --max-time 60 -X POST "$BASE/$_id/pago" -H "Authorization: Bearer $TOKEN" ) &
        done

        # 2) El golpe: un endpoint que NO tiene nada que ver con pagos.
        printf '\n  Golpeando GET /tramites (no toca pagos) mientras los pagos cuelgan…\n'
        RES="$(curl -s -o /dev/null -w '%{http_code} %{time_total}' --max-time 8 "$BASE" -H "Authorization: Bearer $TOKEN")"
        CODE="$(printf '%s' "$RES" | cut -d' ' -f1)"
        TIME="$(printf '%s' "$RES" | cut -d' ' -f2)"
        printf '     GET /tramites -> HTTP %s en %s s\n\n' "$CODE" "$TIME"

        if [ "$CODE" = "200" ]; then
            paso_ok "La API sigue VIVA (respondió rápido) — el timeout liberó los hilos" \
                    "Esto es la solución: TESO se cae, pero nosotros no."
        else
            paso_warn "La API está SECUESTRADA: /tramites no responde (pool lleno de rehenes)" \
                      "Un servicio ajeno tumbó a un endpoint que ni toca pagos. Ese es el crimen."
        fi
        wait
    fi
fi

printf '\n'
printf '[OK] Pruébalo tú mismo:\n\n'
printf '     # login (Carolina) y confirmar un pago\n'
# shellcheck disable=SC2016
printf '     TOKEN=$(curl -s -X POST %s -H "Content-Type: application/json" \\\n' "$LOGIN"
printf '        -d '\''{"rut":"9876543-2","clave":"dgt-2026"}'\'' | sed '\''s/.*"token":"//; s/".*//'\'')\n'
# shellcheck disable=SC2016
printf '     curl -i -X POST %s/2/pago -H "Authorization: Bearer $TOKEN"\n\n' "$BASE"
log_info "Cuando termines:  ./bin/99-destruir.sh"
printf '\n'
