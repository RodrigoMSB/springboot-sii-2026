#!/usr/bin/env bash
# =============================================================================
#  start-lab.sh — levanta la DGT en tu máquina
# -----------------------------------------------------------------------------
#    ./bin/start-lab.sh                 # puerto 8099, el del curso
#    ./bin/start-lab.sh --puerto 8100   # si el 8099 estuviera ocupado
#
#  El puerto por defecto (8099) vive en lib-comunes.sh, no aquí: los doce labs
#  usan el mismo. No es 8080 a propósito — ese lo ocupa medio mundo.
#
#  Levanta PostgreSQL (docker compose), arranca la API en SEGUNDO PLANO con su
#  log en un archivo, y espera a que responda. Segundo plano y no primer plano
#  porque el alumno necesita su terminal libre para hacer `curl` y ver el
#  resultado con sus propios ojos — la victoria se comprueba, no se narra.
#
#  Para detenerlo todo:  ./bin/99-destruir.sh
# =============================================================================
set -uo pipefail

DIR_BIN="$(cd "$(dirname "$0")" && pwd)"
# shellcheck source-path=SCRIPTDIR
# shellcheck source=../../lib/lib-comunes.sh
. "$DIR_BIN/../../lib/lib-comunes.sh"

DIR_LAB="$(cd "$DIR_BIN/.." && pwd)"
RAIZ="$(raiz_repo)"
APP="$RAIZ/dgt-tramites-api"
ESTADO="$DIR_LAB/.estado"
LOG="$ESTADO/dgt.log"
PID="$ESTADO/dgt.pid"
PUERTO="$DGT_PUERTO_DEFECTO"   # de lib-comunes.sh: el mismo en los 12 labs

es_numero() {
    printf '%s' "${1:-}" | grep -q '^[0-9][0-9]*$'
}

while [ $# -gt 0 ]; do
    case "$1" in
        --puerto)
            PUERTO="${2:-}"
            if ! es_numero "$PUERTO"; then
                printf '[ERROR] --puerto necesita un número. Ej: --puerto %s\n' "$DGT_PUERTO_SUGERIDO" >&2
                exit 2
            fi
            shift 2 ;;
        --puerto=*)
            PUERTO="${1#*=}"
            if ! es_numero "$PUERTO"; then
                printf '[ERROR] --puerto necesita un número. Ej: --puerto=%s\n' "$DGT_PUERTO_SUGERIDO" >&2
                exit 2
            fi
            shift ;;
        -h|--help)
            printf 'Uso: %s [--puerto N]\n' "$(basename "$0")"; exit 0 ;;
        *)
            printf '[ERROR] Argumento no reconocido: %s  (usa --puerto N)\n' "$1" >&2
            exit 2 ;;
    esac
done

mkdir -p "$ESTADO"

printf '\n  Levantando la DGT (puerto %s)\n\n' "$PUERTO"

# -----------------------------------------------------------------------------
#  1 · ¿Ya la levantamos antes?
# -----------------------------------------------------------------------------
if [ -f "$PID" ] && kill -0 "$(cat "$PID")" 2>/dev/null; then
    paso_warn "La DGT ya está corriendo (PID $(cat "$PID"))" \
              "Si quieres empezar de cero:  ./bin/99-destruir.sh"
    exit 0
fi

# -----------------------------------------------------------------------------
#  2 · ¿Está el puerto libre? El alumno merece un nombre y apellido.
# -----------------------------------------------------------------------------
if puerto_ocupado "$PUERTO"; then
    CULPABLE="$(quien_ocupa_puerto "$PUERTO")"
    if [ -n "$CULPABLE" ]; then
        paso_fail "El puerto $PUERTO ya está ocupado por: $CULPABLE" \
                  "No es un error tuyo. Levanta la DGT en otro puerto:  ./bin/start-lab.sh --puerto $DGT_PUERTO_SUGERIDO   (ver T-01)"
    else
        paso_fail "El puerto $PUERTO ya está ocupado por otro programa" \
                  "Levanta la DGT en otro puerto:  ./bin/start-lab.sh --puerto $DGT_PUERTO_SUGERIDO   (ver T-01)"
    fi
    printf '\n'
    exit 1
fi

# -----------------------------------------------------------------------------
#  3 · Docker vivo (sin él no hay base de datos)
# -----------------------------------------------------------------------------
if ! docker info >/dev/null 2>&1; then
    paso_fail "El demonio de Docker no responde" \
              "Abre Docker Desktop y espera a que arranque. Luego repite (ver T-03)."
    printf '\n'
    exit 1
fi

# -----------------------------------------------------------------------------
#  4 · Arrancar. Boot levanta el compose solo (spring-boot-docker-compose).
# -----------------------------------------------------------------------------
log_info "Arrancando la API… (el primer arranque baja dependencias: paciencia)"
log_info "Log completo en: $LOG"

# Los tres descriptores se desprenden: stdin de /dev/null, stdout y stderr al log.
# Sin `</dev/null` y sin `exec`, la JVM hereda el stdout de este script y lo
# mantiene abierto: `./start-lab.sh | tee registro.txt` se colgaría para siempre
# esperando un EOF que nunca llega. Verificado en la SPEC-006.
( cd "$APP" && exec nohup ./mvnw -q spring-boot:run \
      -Dspring-boot.run.arguments="--server.port=$PUERTO" \
      </dev/null >"$LOG" 2>&1 ) &
echo $! > "$PID"

# -----------------------------------------------------------------------------
#  5 · Esperar el health. Bucle con timeout, jamás un `sleep` ciego.
# -----------------------------------------------------------------------------
if esperar_url "http://localhost:$PUERTO/actuator/health" 120; then
    paso_ok "La DGT respondió en el puerto $PUERTO"
else
    paso_fail "La DGT no respondió en 120 segundos" \
              "Mira las últimas líneas del log:  tail -n 40 $LOG"
    printf '\n'
    exit 1
fi

# -----------------------------------------------------------------------------
#  6 · La victoria del alumno: que la compruebe él, con sus manos.
# -----------------------------------------------------------------------------
printf '\n'
printf '[OK] La DGT está viva. Pruébalo tú mismo:\n'
printf '\n'
printf '     curl http://localhost:%s/api/contribuyentes/11111111-1\n' "$PUERTO"
printf '\n'
printf '     -> Valentina Rojas te va a responder.\n'
printf '\n'
log_info "Cuando termines:  ./bin/99-destruir.sh"
printf '\n'
