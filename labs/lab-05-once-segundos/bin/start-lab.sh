#!/usr/bin/env bash
# =============================================================================
#  start-lab.sh — levanta la DGT del Lab 05 (tu starter/, o la solución)
# -----------------------------------------------------------------------------
#    ./bin/start-lab.sh                    # levanta starter/ en el puerto 8099
#    ./bin/start-lab.sh --dir solucion     # levanta la solución
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
LOTES=0

es_numero() { printf '%s' "${1:-}" | grep -q '^[0-9][0-9]*$'; }

while [ $# -gt 0 ]; do
    case "$1" in
        --puerto)  PUERTO="${2:-}"; es_numero "$PUERTO" || { printf '[ERROR] --puerto necesita un número\n' >&2; exit 2; }; shift 2 ;;
        --puerto=*) PUERTO="${1#*=}"; es_numero "$PUERTO" || { printf '[ERROR] --puerto necesita un número\n' >&2; exit 2; }; shift ;;
        --dir)     OBJETIVO="${2:-}"; shift 2 ;;
        --dir=*)   OBJETIVO="${1#*=}"; shift ;;
        --lotes)   LOTES="${2:-}"; es_numero "$LOTES" || { printf '[ERROR] --lotes necesita un número\n' >&2; exit 2; }; shift 2 ;;
        --lotes=*) LOTES="${1#*=}"; es_numero "$LOTES" || { printf '[ERROR] --lotes necesita un número\n' >&2; exit 2; }; shift ;;
        -h|--help) printf 'Uso: %s [--dir starter|solucion] [--puerto N]\n' "$(basename "$0")"; exit 0 ;;
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

# --- Siembra masiva (--lotes): el escenario del N+1 --------------------------
if [ "$LOTES" -gt 0 ]; then
    if [ "$LOTES" -gt 4000 ]; then
        paso_warn "Con $LOTES lotes, el listado ingenuo hará miles de consultas." \
                  "Es el escenario de la Guía 02. Prepárate para contar los segundos."
    fi
    log_info "Sembrando $LOTES contribuyentes y trámites (puede tardar)…"
    # Se siembra por SQL dentro del contenedor: rápido, sin pasar por la app.
    if ( cd "$APP" && docker compose exec -T postgres psql -U dgt -d dgt -q -c "
        INSERT INTO contribuyente (rut, razon_social, puntaje_riesgo_interno)
          SELECT 'L' || g || '-0', 'Lote ' || g, 0 FROM generate_series(1, $LOTES) g;
        INSERT INTO tramite (contribuyente_id, tipo, estado)
          SELECT c.id, 'DECLARACION_F29', 'BORRADOR' FROM contribuyente c WHERE c.rut LIKE 'L%-0';
      " >/dev/null 2>&1 ); then
        paso_ok "Sembrados $LOTES lotes"
    else
        paso_warn "No pude sembrar (¿psql en el contenedor?)"
    fi
fi

printf '\n'
printf '[OK] Pruébalo tú mismo:\n\n'
printf '     curl http://localhost:%s/api/contribuyentes/11111111-1\n' "$PUERTO"
printf '     curl http://localhost:%s/api/tramites/1        # tu endpoint del TODO_4\n' "$PUERTO"
printf '     curl -i http://localhost:%s/api/tramites/999   # ¿404 con ProblemDetail?\n\n' "$PUERTO"
log_info "Cuando termines:  ./bin/99-destruir.sh"
printf '\n'
