#!/usr/bin/env bash
# =============================================================================
#  start-lab.sh — levanta la DGT del Lab 07 (tu starter/, o la solución)
# -----------------------------------------------------------------------------
#    ./bin/start-lab.sh                    # levanta starter/ en el puerto 8099
#    ./bin/start-lab.sh --dir solucion     # levanta la solución
#    ./bin/start-lab.sh --crimen           # …y demuestra el crimen (puerta abierta)
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
CRIMEN=0

es_numero() { printf '%s' "${1:-}" | grep -q '^[0-9][0-9]*$'; }

while [ $# -gt 0 ]; do
    case "$1" in
        --puerto)  PUERTO="${2:-}"; es_numero "$PUERTO" || { printf '[ERROR] --puerto necesita un número\n' >&2; exit 2; }; shift 2 ;;
        --puerto=*) PUERTO="${1#*=}"; es_numero "$PUERTO" || { printf '[ERROR] --puerto necesita un número\n' >&2; exit 2; }; shift ;;
        --dir)     OBJETIVO="${2:-}"; shift 2 ;;
        --dir=*)   OBJETIVO="${1#*=}"; shift ;;
        --crimen)  CRIMEN=1; shift ;;
        -h|--help) printf 'Uso: %s [--dir starter|solucion] [--crimen] [--puerto N]\n' "$(basename "$0")"; exit 0 ;;
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

# -----------------------------------------------------------------------------
#  El crimen (--crimen): ¿la puerta existe?
# -----------------------------------------------------------------------------
#  Dos golpes: (1) emitir un folio SIN ser nadie; (2) emitir con un "token"
#  fabricado a mano —base64(rut:rol)— sin firma. En el starter, los dos entran.
#  En la solución, los dos mueren con 401.
codigo() { curl -s -o /dev/null -w '%{http_code}' "$@"; }

if [ "$CRIMEN" -eq 1 ]; then
    printf '\n  --- El crimen: la puerta que no existía ---\n\n'

    ANON="$(codigo -X POST "$BASE/1/folio")"
    printf '     1) curl anónimo (sin token) POST /tramites/1/folio   -> HTTP %s\n' "$ANON"

    FORJADO="$(printf '%s' 'ladron:FUNCIONARIO' | base64)"
    FALSO="$(codigo -X POST "$BASE/1/folio" -H "Authorization: Bearer $FORJADO")"
    printf '     2) curl con token FABRICADO a mano (base64, sin firma) -> HTTP %s\n' "$FALSO"
    printf '        (el token se armó con:  echo -n '\''ladron:FUNCIONARIO'\'' | base64)\n'

    printf '\n'
    if [ "$ANON" = "401" ] && [ "$FALSO" = "401" ]; then
        paso_ok "La puerta existe: los dos golpes reciben 401" \
                "Sin credencial válida y FIRMADA, no se entra. Esto es la solución."
    else
        paso_warn "La puerta NO existe (o es de cartón): alguien entró sin firma" \
                  "Un token sin firma es una opinión. Codificar no es cifrar, y cifrar no es firmar."
    fi
fi

printf '\n'
printf '[OK] Pruébalo tú mismo:\n\n'
printf '     # 1) el crimen: emitir sin ser nadie\n'
printf '     curl -i -X POST %s/1/folio\n\n' "$BASE"
printf '     # 2) login real (solo la solución) y emitir con el token\n'
# Estas líneas IMPRIMEN comandos de ejemplo: $TOKEN y $(...) salen literales a propósito,
# para que el alumno los copie. Por eso van en comillas simples.
# shellcheck disable=SC2016
printf '     TOKEN=$(curl -s -X POST http://localhost:%s/api/v1/auth/login \\\n' "$PUERTO"
printf '        -H "Content-Type: application/json" \\\n'
printf '        -d '\''{"rut":"9876543-2","clave":"dgt-2026"}'\'' | sed '\''s/.*"token":"//; s/".*//'\'')\n'
# shellcheck disable=SC2016
printf '     curl -i -X POST %s/2/folio -H "Authorization: Bearer $TOKEN"\n\n' "$BASE"
log_info "Cuando termines:  ./bin/99-destruir.sh"
printf '\n'
