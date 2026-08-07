#!/usr/bin/env bash
# =============================================================================
#  start-lab.sh — levanta la DGT del Lab 12 (tu starter/, o la solución)
# -----------------------------------------------------------------------------
#    ./bin/start-lab.sh                     # normal, puerto 8099
#    ./bin/start-lab.sh --dir solucion      # la solución
#    ./bin/start-lab.sh --avisos-caidos     # EL CRIMEN, en dos fases
#    ./bin/start-lab.sh --puerto 8100
#
#  ---------------------------------------------------------------------------
#  MECANISMO DE `--avisos-caidos` (declarado, SPEC-018 §2)
#  ---------------------------------------------------------------------------
#  «El servicio de avisos» son dos cosas que caen juntas: el CONSUMIDOR que toma
#  los mensajes y el DESTINO al que entrega. Tumbar solo una no reproduce nada:
#
#   · Si tumbaras solo el destino, en la solución el consumidor seguiría tomando
#     mensajes, fallando y mandándolos a la DLQ. Correcto, pero no es la escena.
#   · Si tumbaras solo el consumidor, en el starter no cambiaría nada: ahí el
#     aviso ni siquiera pasa por la cola.
#
#  Así que se apagan las dos:
#     spring.rabbitmq.listener.simple.auto-startup=false   (el consumidor no arranca)
#     dgt.avisos.destino-caido=true                        (el destino no contesta)
#
#  Y el guion tiene DOS FASES, porque el crimen solo se ve en el contraste:
#
#   FASE 1 — con el servicio caído, se emiten N folios. La API responde 201 a
#            todos. Cero errores en pantalla.
#   FASE 2 — se REINICIA la aplicación sin las banderas. Eso es «levantar el
#            servicio de avisos». Y ahí se ve la diferencia:
#              · starter  → no llega nada, y no hay forma de saber qué se perdió.
#              · solución → los N mensajes seguían en la cola (durable: sobrevivieron
#                al reinicio) y se entregan solos.
#
#  El reinicio no es un detalle del guion: es lo que demuestra que la cola es
#  DURABLE. En el Lab 11 los avisos vivían en memoria y un reinicio se los
#  llevaba enteros.
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
AVISOS_CAIDOS=0
FOLIOS=3

es_numero() { printf '%s' "${1:-}" | grep -q '^[0-9][0-9]*$'; }

while [ $# -gt 0 ]; do
    case "$1" in
        --puerto)         PUERTO="${2:-}"; es_numero "$PUERTO" || { printf '[ERROR] --puerto necesita un número\n' >&2; exit 2; }; shift 2 ;;
        --puerto=*)       PUERTO="${1#*=}"; es_numero "$PUERTO" || { printf '[ERROR] --puerto necesita un número\n' >&2; exit 2; }; shift ;;
        --dir)            OBJETIVO="${2:-}"; shift 2 ;;
        --dir=*)          OBJETIVO="${1#*=}"; shift ;;
        --avisos-caidos)  AVISOS_CAIDOS=1; shift ;;
        -h|--help) printf 'Uso: %s [--dir starter|solucion] [--avisos-caidos] [--puerto N]\n' "$(basename "$0")"; exit 0 ;;
        *) printf '[ERROR] Argumento no reconocido: %s\n' "$1" >&2; exit 2 ;;
    esac
done

APP="$DIR_LAB/$OBJETIVO"
[ -f "$APP/pom.xml" ] || { printf '[ERROR] No es un proyecto Maven: %s\n' "$APP" >&2; exit 2; }

mkdir -p "$ESTADO"
printf '\n  Levantando la DGT (%s, puerto %s)\n\n' "$OBJETIVO" "$PUERTO"

if ! docker info >/dev/null 2>&1; then
    paso_fail "El demonio de Docker no responde" "Abre Docker Desktop y espera a que arranque (T-03 del Lab 00)."
    printf '\n'; exit 1
fi

if puerto_ocupado "$PUERTO"; then
    CULPABLE="$(quien_ocupa_puerto "$PUERTO")"
    paso_fail "El puerto $PUERTO ya está ocupado${CULPABLE:+ por: $CULPABLE}" \
              "Usa otro:  ./bin/start-lab.sh --puerto $DGT_PUERTO_SUGERIDO   (ver T-01 del Lab 00)"
    printf '\n'; exit 1
fi

# arrancar <archivo-log> <argumentos-extra>
arrancar() {
    _log="$1"; shift
    ( cd "$APP" && exec nohup ./mvnw -q spring-boot:run \
          -Dspring-boot.run.arguments="--server.port=$PUERTO $*" \
          </dev/null >"$_log" 2>&1 ) &
    echo $! > "$PID"
    esperar_url "http://localhost:$PUERTO/actuator/health" 240
}

detener() {
    [ -f "$PID" ] || return 0
    _n="$(cat "$PID")"
    if kill -0 "$_n" 2>/dev/null; then
        pkill -P "$_n" 2>/dev/null; kill "$_n" 2>/dev/null
        _e=0; while kill -0 "$_n" 2>/dev/null && [ "$_e" -lt 20 ]; do _e=$((_e + 1)); sleep 1; done
    fi
    rm -f "$PID"
}

consulta_rabbit() {
    ( cd "$APP" && docker compose exec -T rabbitmq rabbitmqctl list_queues name messages 2>/dev/null | grep "^$1" )
}

# OJO con `grep -c`: imprime 0 Y SALE CON CÓDIGO 1 cuando no hay coincidencias. Un
# `|| printf '0'` detrás añade un segundo cero y el resultado ("0\n0") revienta cualquier
# comparación numérica. Se captura la salida y se usa tal cual.
avisos_entregados() {
    _c="$(grep -c 'Notificación enviada' "$1" 2>/dev/null)"
    printf '%s' "${_c:-0}"
}

emitir_folios() {
    _tok="$(curl -s --max-time 30 -X POST "http://localhost:$PUERTO/api/v1/auth/login" \
            -H 'Content-Type: application/json' -d '{"rut":"9876543-2","clave":"dgt-2026"}' \
            | sed 's/.*"token":"//; s/".*//')"
    [ -n "$_tok" ] || { paso_warn "No pude autenticarme"; return 1; }
    _i=0
    while [ "$_i" -lt "$FOLIOS" ]; do
        _tr="$(curl -s --max-time 30 -X POST "http://localhost:$PUERTO/api/v1/tramites" \
               -H "Authorization: Bearer $_tok" -H 'Content-Type: application/json' \
               -d '{"rutContribuyente":"11111111-1","tipo":"DECLARACION_F29"}' \
               | sed 's/.*"id"://; s/[,}].*//')"
        _cod="$(curl -s -o /dev/null -w '%{http_code}' --max-time 30 -X POST \
                "http://localhost:$PUERTO/api/v1/tramites/$_tr/folio" -H "Authorization: Bearer $_tok")"
        printf '     POST /api/v1/tramites/%s/folio  ->  HTTP %s\n' "$_tr" "$_cod"
        _i=$((_i + 1))
    done
}

# -----------------------------------------------------------------------------
#  Camino normal
# -----------------------------------------------------------------------------
if [ "$AVISOS_CAIDOS" -eq 0 ]; then
    log_info "Arrancando… (log en $LOG; RabbitMQ tarda unos segundos más que Postgres)"
    if arrancar "$LOG"; then
        paso_ok "La DGT está viva en el puerto $PUERTO"
    else
        paso_fail "La DGT no respondió en 240 segundos" "Mira el log:  tail -n 40 $LOG"
        printf '\n'; exit 1
    fi
    printf '\n'
    log_info "La consola de RabbitMQ está en http://localhost:15672  (dgt / dgt-dev)"
    log_info "Cuando termines:  ./bin/99-destruir.sh"
    printf '\n'
    exit 0
fi

# -----------------------------------------------------------------------------
#  EL CRIMEN — fase 1: el servicio de avisos está caído
# -----------------------------------------------------------------------------
LOG1="$ESTADO/dgt-fase1.log"
log_info "FASE 1 · arrancando con el servicio de avisos CAÍDO… (log en $LOG1)"
# Se desactiva el latido del cierre nocturno (Lab 11) durante el guion: también notifica, y sus
# avisos contaminarían el conteo de los avisos de FOLIO, que es lo que este crimen mide.
# Ojo: NADA de pasar el cron por aquí. `spring-boot.run.arguments` separa por ESPACIOS, y una
# expresión cron los lleva dentro: "0 0 3 * * *" llegaría partida y la app no arrancaría
# ("Cron expression must consist of 6 fields (found 1)"). El default de application.yml ya apunta
# a las 3 AM, así que no va a saltar durante el guion.
SIN_CIERRE="--dgt.cierre.retardo-inicial-ms=86400000 --dgt.cierre.intervalo-ms=86400000"
if ! arrancar "$LOG1" "--spring.rabbitmq.listener.simple.auto-startup=false --dgt.avisos.destino-caido=true $SIN_CIERRE"; then
    paso_fail "La DGT no respondió en 240 segundos" "Mira el log:  tail -n 40 $LOG1"
    printf '\n'; exit 1
fi
paso_ok "La DGT está viva. El servicio de avisos, NO."

printf '\n  --- Se emiten %s folios con el servicio de avisos caído ---\n\n' "$FOLIOS"
emitir_folios
sleep 3

printf '\n  Estado tras emitir:\n\n'
printf '     avisos entregados            ->  %s\n' "$(avisos_entregados "$LOG1")"
COLA="$(consulta_rabbit 'dgt.avisos.q' | awk '{print $2}')"
printf '     mensajes esperando en la cola ->  %s\n\n' "${COLA:-0}"

if [ "${COLA:-0}" -ge 1 ]; then
    paso_ok "Los avisos están GUARDADOS en la cola, esperando" \
            "Nada se perdió: cuando el servicio vuelva, ahí siguen."
else
    paso_warn "Cero avisos entregados y cero en la cola: se evaporaron" \
              "Ese es el crimen. Y lo peor: no hay forma de saber CUÁLES eran."
fi

# -----------------------------------------------------------------------------
#  Fase 2: se levanta el servicio de avisos (reinicio SIN las banderas)
# -----------------------------------------------------------------------------
printf '\n  --- Se levanta el servicio de avisos (la app se reinicia) ---\n\n'
detener
LOG2="$ESTADO/dgt-fase2.log"
log_info "FASE 2 · arrancando con el servicio de avisos ARRIBA… (log en $LOG2)"
if ! arrancar "$LOG2" "$SIN_CIERRE"; then
    paso_fail "La DGT no respondió en 240 segundos" "Mira el log:  tail -n 40 $LOG2"
    printf '\n'; exit 1
fi

_i=0
while [ "$_i" -lt 30 ]; do
    [ "$(avisos_entregados "$LOG2")" -ge "$FOLIOS" ] && break
    _i=$((_i + 1)); sleep 1
done

ENTREGADOS="$(avisos_entregados "$LOG2")"
COLA_FIN="$(consulta_rabbit 'dgt.avisos.q' | awk '{print $2}')"

printf '\n  Estado tras levantar el servicio:\n\n'
printf '     avisos entregados            ->  %s de %s\n' "$ENTREGADOS" "$FOLIOS"
printf '     mensajes esperando en la cola ->  %s\n\n' "${COLA_FIN:-0}"

if [ "$ENTREGADOS" -ge "$FOLIOS" ]; then
    paso_ok "Llegaron TODOS los avisos que esperaban, y sin reenviar nada a mano" \
            "La cola es durable: sobrevivió incluso al reinicio de la aplicación."
else
    paso_warn "No llegó ninguno. Esos avisos no existen en ninguna parte." \
              "Se evaporaron mientras el servicio estuvo caído. Empieza por guia/02-el-aviso-que-se-evaporo.md"
fi

printf '\n'
log_info "Mira las colas por dentro:  http://localhost:15672  (dgt / dgt-dev)"
log_info "Cuando termines:  ./bin/99-destruir.sh"
printf '\n'
