#!/usr/bin/env bash
# =============================================================================
#  start-lab.sh — levanta la DGT del Lab 10 (tu starter/, o la solución)
# -----------------------------------------------------------------------------
#    ./bin/start-lab.sh                     # levanta starter/ en el puerto 8099
#    ./bin/start-lab.sh --dir solucion      # levanta la solución
#    ./bin/start-lab.sh --db-caida          # …y le tumba la base: EL CRIMEN
#    ./bin/start-lab.sh --puerto 8100
#
#  ---------------------------------------------------------------------------
#  MECANISMO DE `--db-caida` (declarado, SPEC-016 §5)
#  ---------------------------------------------------------------------------
#  La app arranca NORMAL —con su PostgreSQL arriba, migraciones aplicadas y todo
#  en orden— y solo DESPUÉS se mata el proceso de la base:
#
#      kill -9 <el postgres embebido de ESTA app>
#
#  Se hace en ese orden a propósito, y el orden ES la lección:
#
#   · Arrancar sin base no serviría: Flyway y la validación de Hibernate corren al
#     inicio, así que la app ni siquiera llegaría a levantar. Un proceso que no
#     arranca no engaña a nadie — no hay crimen que ver.
#   · Lo que este lab quiere mostrar es un proceso VIVO, sano según el tablero, y
#     absolutamente incapaz de hacer su trabajo. Eso solo se consigue matando la
#     dependencia con la aplicación ya en pie. Es, además, lo que pasa de verdad:
#     nadie despliega sin base; la base se cae un martes a las tres de la tarde.
#
#  ¿Y cómo se sabe cuál matar? Se anotan los PostgreSQL embebidos que había ANTES
#  de arrancar, y se mata el que apareció después. Nunca `pkill postgres`: eso se
#  llevaría por delante la base del trabajo del alumno y la de cualquier otro
#  curso, sin avisar. Se mata por PID o no se mata (post-ALCHEMIA).
#
#  Para repetir el experimento: `./bin/99-destruir.sh` y volver a arrancar. La
#  base embebida se siembra sola en cada arranque, así que no hay nada que
#  restaurar a mano.
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
DB_CAIDA=0

es_numero() { printf '%s' "${1:-}" | grep -q '^[0-9][0-9]*$'; }

while [ $# -gt 0 ]; do
    case "$1" in
        --puerto)   PUERTO="${2:-}"; es_numero "$PUERTO" || { printf '[ERROR] --puerto necesita un número\n' >&2; exit 2; }; shift 2 ;;
        --puerto=*) PUERTO="${1#*=}"; es_numero "$PUERTO" || { printf '[ERROR] --puerto necesita un número\n' >&2; exit 2; }; shift ;;
        --dir)      OBJETIVO="${2:-}"; shift 2 ;;
        --dir=*)    OBJETIVO="${1#*=}"; shift ;;
        --db-caida) DB_CAIDA=1; shift ;;
        -h|--help)  printf 'Uso: %s [--dir starter|solucion] [--db-caida] [--puerto N]\n' "$(basename "$0")"; exit 0 ;;
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

# Aquí vivía el guard de Docker. Ya no hace falta nada de eso: PostgreSQL y TESO
# viajan dentro del proyecto y arrancan con la aplicación (SPEC-022 y SPEC-025).

# Los PostgreSQL embebidos que YA estaban corriendo antes de que arrancáramos
# nada. Se guardan para poder distinguir DESPUÉS cuál es el nuestro: es lo que
# permite que `--db-caida` mate exactamente una base y no la de nadie más.
PG_PATRON='embedded-pg/PG-.*/bin/postgres'
PG_ANTES="$(pgrep -f "$PG_PATRON" 2>/dev/null | sort | tr '\n' ' ')"

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

SALUD="http://localhost:$PUERTO/actuator/health"
TRAMITES="http://localhost:$PUERTO/api/v1/tramites"
LOGIN="http://localhost:$PUERTO/api/v1/auth/login"

# Un curl que devuelve SOLO el código de estado. Sirve para mostrar el contraste sin ruido.
codigo_de()      { curl -s -o /dev/null -w '%{http_code}' --max-time 30 "$1" 2>/dev/null; }
cuerpo_de()      { curl -s --max-time 30 "$1" 2>/dev/null; }
codigo_con_token() {
    curl -s -o /dev/null -w '%{http_code}' --max-time 30 -H "Authorization: Bearer $2" "$1" 2>/dev/null
}

# -----------------------------------------------------------------------------
#  El crimen (--db-caida): la app viva, la base muerta, el tablero en verde
# -----------------------------------------------------------------------------
if [ "$DB_CAIDA" -eq 1 ]; then
    printf '\n  --- El crimen: la base se cae, y el tablero no se entera ---\n\n'

    # El token se toma AHORA, con la base viva, y es deliberado: el login consulta la tabla
    # `usuario`, así que con PostgreSQL muerto ya no se podría autenticar nadie. Sin token, la API
    # respondería 401 —"no te conozco"— y taparía justo lo que queremos ver: un 500, que significa
    # "te conozco, pero no puedo trabajar". El JWT es sin estado: sigue valiendo sin base.
    TOKEN="$(curl -s --max-time 30 -X POST "$LOGIN" -H 'Content-Type: application/json' \
             -d '{"rut":"9876543-2","clave":"dgt-2026"}' | sed 's/.*"token":"//; s/".*//')"
    if [ -z "$TOKEN" ]; then
        paso_warn "No pude autenticarme para montar el crimen" "Revisa el log: tail -n 40 $LOG"
    fi

    printf '  ANTES (todo en orden):\n'
    printf '     GET /api/v1/tramites   ->  HTTP %s\n' "$(codigo_con_token "$TRAMITES" "$TOKEN")"
    printf '     GET /actuator/health   ->  %s\n\n' "$(cuerpo_de "$SALUD")"

    # Cuál de los PostgreSQL embebidos es el NUESTRO: el que no estaba antes.
    # Se mata por PID, nunca por nombre. `pkill postgres` se llevaría por delante
    # la base del trabajo del alumno, la de otro curso y la de este laboratorio,
    # todas a la vez — y el alumno no tendría ni cómo saberlo.
    PG_NUESTROS=""
    for _pid in $(pgrep -f "$PG_PATRON" 2>/dev/null); do
        case " $PG_ANTES " in
            *" $_pid "*) : ;;                       # ya estaba: no es nuestro
            *) PG_NUESTROS="$PG_NUESTROS $_pid" ;;
        esac
    done

    if [ -z "$PG_NUESTROS" ]; then
        paso_warn "No encontré el PostgreSQL embebido de esta aplicación" \
                  "Sin él no puedo montar el crimen. Míralos con:  pgrep -fl 'embedded-pg/PG-'"
    else
        log_info "Tumbando el PostgreSQL embebido de esta app (PID$PG_NUESTROS)…"
        # SIGKILL y no SIGTERM: se quiere una base que MUERE, no una que se
        # despide ordenadamente. Es el martes a las tres de la tarde.
        for _pid in $PG_NUESTROS; do kill -9 "$_pid" 2>/dev/null; done
        paso_ok "PostgreSQL detenido. La aplicación sigue corriendo (PID $(cat "$PID"))."
    fi

    printf '\n  DESPUÉS (la base está muerta):\n\n'

    ESTADO_TRAMITES="$(codigo_con_token "$TRAMITES" "$TOKEN")"
    CUERPO_READY="$(cuerpo_de "$SALUD/readiness")"

    printf '     GET /api/v1/tramites            ->  HTTP %s   <- el negocio está caído\n' "$ESTADO_TRAMITES"
    printf '     GET /actuator/health            ->  %s\n' "$(cuerpo_de "$SALUD")"
    printf '     GET /actuator/health/liveness   ->  %s\n'  "$(cuerpo_de "$SALUD/liveness")"
    printf '     GET /actuator/health/readiness  ->  %s\n\n' "$CUERPO_READY"

    # El veredicto se decide por lo que DICE readiness, no por si la ruta existe: los grupos
    # liveness/readiness vienen activados por defecto en Boot 4.1, así que el starter TAMBIÉN los
    # publica — y eso es peor, no mejor. No es que falte la sonda: es que la sonda existe, la
    # consultan, y responde UP con la base muerta.
    case "$CUERPO_READY" in
        *DOWN*)
            paso_ok "El tablero dice la VERDAD: readiness cayó y nombra el componente; liveness sigue UP" \
                    "Eso es la solución: no hay que reiniciar la app — hay que levantar la base."
            ;;
        *)
            paso_warn "El tablero MIENTE: readiness dice UP con la base muerta y la API devolviendo $ESTADO_TRAMITES" \
                      "Ese es el crimen. Empieza por guia/02-el-tablero-que-mentia.md"
            ;;
    esac

    printf '\n'
    log_info "Para repetir el experimento:  ./bin/99-destruir.sh && ./bin/start-lab.sh --db-caida"
fi

printf '\n'
printf '[OK] Pruébalo tú mismo:\n\n'
printf '     curl -s localhost:%s/actuator/health\n' "$PUERTO"
printf '     curl -s localhost:%s/actuator/health/readiness    # (solo la solución las separa)\n' "$PUERTO"
printf '     curl -s localhost:%s/actuator/health/liveness\n\n' "$PUERTO"
log_info "Cuando termines:  ./bin/99-destruir.sh"
printf '\n'
