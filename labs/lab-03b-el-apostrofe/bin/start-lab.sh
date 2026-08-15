#!/usr/bin/env bash
# =============================================================================
#  start-lab.sh — levanta la DGT del Lab 3.5 (tu starter/, o la solución)
# -----------------------------------------------------------------------------
#    ./bin/start-lab.sh                    # levanta starter/ en el puerto 8099
#    ./bin/start-lab.sh --dir solucion     # levanta la solución
#    ./bin/start-lab.sh --puerto 8100
#    ./bin/start-lab.sh --ver-sql          # …y muestra el SQL que Hibernate genera
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
VER_SQL=0

es_numero() { printf '%s' "${1:-}" | grep -q '^[0-9][0-9]*$'; }

while [ $# -gt 0 ]; do
    case "$1" in
        --puerto)  PUERTO="${2:-}"; es_numero "$PUERTO" || { printf '[ERROR] --puerto necesita un número\n' >&2; exit 2; }; shift 2 ;;
        --puerto=*) PUERTO="${1#*=}"; es_numero "$PUERTO" || { printf '[ERROR] --puerto necesita un número\n' >&2; exit 2; }; shift ;;
        --dir)     OBJETIVO="${2:-}"; shift 2 ;;
        --dir=*)   OBJETIVO="${1#*=}"; shift ;;
        --ver-sql) VER_SQL=1; shift ;;
        -h|--help) printf 'Uso: %s [--dir starter|solucion] [--puerto N] [--ver-sql]\n' "$(basename "$0")"; exit 0 ;;
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

# Ya no hay guard de Docker: este lab no lo usa. PostgreSQL viaja como
# dependencia Maven y arranca como proceso hijo de la app. La primera vez tarda
# unos segundos más, porque tiene que extraer los binarios del motor.
# --ver-sql · el TODO_3 pide MIRAR el SQL que Hibernate genera por ti. Se enciende
# aquí, con propiedades, y no editando un application.yml: así el log ruidoso dura
# lo que dure este arranque y no se queda encendido para los labs siguientes.
ARGS="--server.port=$PUERTO"
if [ "$VER_SQL" -eq 1 ]; then
    ARGS="$ARGS --spring.jpa.show-sql=true"
    ARGS="$ARGS --spring.jpa.properties.hibernate.format_sql=true"
    ARGS="$ARGS --logging.level.org.hibernate.SQL=DEBUG"
    log_info "SQL a la vista: cada consulta que Hibernate genere aparecerá en $LOG"
fi

log_info "Arrancando… (log en $LOG)"
( cd "$APP" && exec nohup ./mvnw -q spring-boot:run \
      -Dspring-boot.run.arguments="$ARGS" \
      </dev/null >"$LOG" 2>&1 ) &
echo $! > "$PID"

if esperar_url "http://localhost:$PUERTO/actuator/health" 120; then
    paso_ok "La DGT está viva en el puerto $PUERTO"
else
    paso_fail "La DGT no respondió en 120 segundos" "Mira el log:  tail -n 40 $LOG"
    printf '\n'; exit 1
fi

printf '\n'
printf '[OK] Pruébalo tú mismo:\n\n'
printf '     curl http://localhost:%s/api/contribuyentes/11111111-1\n' "$PUERTO"
printf '     curl http://localhost:%s/api/tramites/1        # tu endpoint del TODO_4\n' "$PUERTO"
printf '     curl -i http://localhost:%s/api/tramites/999   # ¿404 con ProblemDetail?\n\n' "$PUERTO"
log_info "Cuando termines:  ./bin/99-destruir.sh"
printf '\n'
