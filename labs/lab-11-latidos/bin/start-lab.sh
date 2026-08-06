#!/usr/bin/env bash
# =============================================================================
#  start-lab.sh — levanta la DGT del Lab 11 (tu starter/, o la solución)
# -----------------------------------------------------------------------------
#    ./bin/start-lab.sh                     # una instancia, puerto 8099
#    ./bin/start-lab.sh --dir solucion      # la solución
#    ./bin/start-lab.sh --instancias 2      # DOS instancias: EL CRIMEN
#    ./bin/start-lab.sh --puerto 8100
#
#  ---------------------------------------------------------------------------
#  MECANISMO DE `--instancias 2` (declarado, SPEC-017 §2)
#  ---------------------------------------------------------------------------
#  Dos JVM de la MISMA aplicación, en dos puertos, contra UNA sola base:
#
#   1. La instancia 1 arranca normal. Boot levanta el compose y siembra la base.
#   2. Se descubre a qué puerto de host quedó PostgreSQL:
#          docker compose port postgres 5432
#      (el compose publica un puerto EFÍMERO a propósito, desde el Lab 01: un
#      5432 fijo choca con el PostgreSQL que medio mundo ya tiene instalado).
#   3. La instancia 2 arranca con el compose DESACTIVADO y la URL de esa misma
#      base pasada a mano. Sin este paso, Boot intentaría levantar un segundo
#      compose y cada instancia hablaría con su propia base — no habría crimen
#      que ver, porque el problema es justamente COMPARTIR el estado.
#   4. A las dos se les da la misma identidad de tarea pero distinto nombre
#      (`dgt-1`, `dgt-2`), y un latido corto para no esperar a la medianoche.
#
#  Es la simulación honesta de dos réplicas detrás de un balanceador: mismo
#  código, misma configuración, misma base, distinto proceso. Que es exactamente
#  lo que Kubernetes hace cuando escalas a 2.
# =============================================================================
set -uo pipefail

DIR_BIN="$(cd "$(dirname "$0")" && pwd)"
# shellcheck source-path=SCRIPTDIR
# shellcheck source=../../lib/lib-comunes.sh
. "$DIR_BIN/../../lib/lib-comunes.sh"

DIR_LAB="$(cd "$DIR_BIN/.." && pwd)"
ESTADO="$DIR_LAB/.estado"
PUERTO="$DGT_PUERTO_DEFECTO"
OBJETIVO="starter"
INSTANCIAS=1

# ---------------------------------------------------------------------------
#  Por qué la demo late por CRON y no por fixedDelay
# ---------------------------------------------------------------------------
#  `initialDelay` cuenta desde que arranca CADA JVM, y la instancia 2 arranca
#  después de la 1 (hay que esperar a que la 1 levante el compose y siembre). Con
#  ese desfase los dos latidos nunca coinciden: la 1 ejecuta, suelta el candado, y
#  diez segundos más tarde la 2 lo encuentra libre y ejecuta también. Se verían dos
#  cierres incluso en la solución — y no porque el candado falle, sino porque
#  jamás llegó a disputarse.
#
#  El cron es ABSOLUTO: `0 * * * * *` dispara en el segundo 0 de cada minuto en las
#  dos instancias, arrancaran cuando arrancaran. Eso es lo que reproduce el crimen
#  de verdad —dos servidores despertando a la MISMA hora— y lo que pone al candado
#  a hacer su trabajo.
#
#  El latido por fixedDelay se desactiva en la demo (retardo altísimo) para que no
#  se mezcle con el del cron.
RETARDO_MS=86400000
INTERVALO_MS=86400000
CRON_DEMO="0 * * * * *" 

es_numero() { printf '%s' "${1:-}" | grep -q '^[0-9][0-9]*$'; }

while [ $# -gt 0 ]; do
    case "$1" in
        --puerto)      PUERTO="${2:-}"; es_numero "$PUERTO" || { printf '[ERROR] --puerto necesita un número\n' >&2; exit 2; }; shift 2 ;;
        --puerto=*)    PUERTO="${1#*=}"; es_numero "$PUERTO" || { printf '[ERROR] --puerto necesita un número\n' >&2; exit 2; }; shift ;;
        --dir)         OBJETIVO="${2:-}"; shift 2 ;;
        --dir=*)       OBJETIVO="${1#*=}"; shift ;;
        --instancias)  INSTANCIAS="${2:-}"; es_numero "$INSTANCIAS" || { printf '[ERROR] --instancias necesita un número\n' >&2; exit 2; }; shift 2 ;;
        --instancias=*) INSTANCIAS="${1#*=}"; es_numero "$INSTANCIAS" || { printf '[ERROR] --instancias necesita un número\n' >&2; exit 2; }; shift ;;
        -h|--help) printf 'Uso: %s [--dir starter|solucion] [--instancias N] [--puerto N]\n' "$(basename "$0")"; exit 0 ;;
        *) printf '[ERROR] Argumento no reconocido: %s\n' "$1" >&2; exit 2 ;;
    esac
done

APP="$DIR_LAB/$OBJETIVO"
[ -f "$APP/pom.xml" ] || { printf '[ERROR] No es un proyecto Maven: %s\n' "$APP" >&2; exit 2; }
[ "$INSTANCIAS" -ge 1 ] || { printf '[ERROR] --instancias debe ser al menos 1\n' >&2; exit 2; }

mkdir -p "$ESTADO"
printf '\n  Levantando la DGT (%s, %s instancia(s))\n\n' "$OBJETIVO" "$INSTANCIAS"

if ! docker info >/dev/null 2>&1; then
    paso_fail "El demonio de Docker no responde" "Abre Docker Desktop y espera a que arranque (T-03 del Lab 00)."
    printf '\n'; exit 1
fi

# --- Instancia 1: arranca normal, levanta el compose y siembra ----------------
PUERTO1="$PUERTO"
if puerto_ocupado "$PUERTO1"; then
    CULPABLE="$(quien_ocupa_puerto "$PUERTO1")"
    paso_fail "El puerto $PUERTO1 ya está ocupado${CULPABLE:+ por: $CULPABLE}" \
              "Usa otro:  ./bin/start-lab.sh --puerto $DGT_PUERTO_SUGERIDO   (ver T-01 del Lab 00)"
    printf '\n'; exit 1
fi

LOG1="$ESTADO/dgt-1.log"
log_info "Instancia 1 (dgt-1) en el puerto ${PUERTO1}… (log en $LOG1)"
( cd "$APP" && DGT_INSTANCIA=dgt-1 \
    DGT_CIERRE_RETARDO_MS="$RETARDO_MS" DGT_CIERRE_INTERVALO_MS="$INTERVALO_MS" \
    DGT_CIERRE_CRON="$CRON_DEMO" \
    exec nohup ./mvnw -q spring-boot:run \
      -Dspring-boot.run.arguments="--server.port=$PUERTO1" \
      </dev/null >"$LOG1" 2>&1 ) &
echo $! > "$ESTADO/dgt-1.pid"

if esperar_url "http://localhost:$PUERTO1/actuator/health" 180; then
    paso_ok "Instancia 1 viva en el puerto $PUERTO1"
else
    paso_fail "La instancia 1 no respondió en 180 segundos" "Mira el log:  tail -n 40 $LOG1"
    printf '\n'; exit 1
fi

# --- Instancias 2..N: misma base, compose apagado ----------------------------
_n=2
while [ "$_n" -le "$INSTANCIAS" ]; do
    PUERTO_N=$((PUERTO1 + _n - 1))

    # ¿A qué puerto de host quedó PostgreSQL? El compose publica uno efímero.
    MAPEO="$( cd "$APP" && docker compose port postgres 5432 2>/dev/null )"
    PUERTO_DB="${MAPEO##*:}"
    if [ -z "$PUERTO_DB" ]; then
        paso_fail "No pude descubrir el puerto de PostgreSQL" \
                  "Comprueba:  ( cd $OBJETIVO && docker compose port postgres 5432 )"
        printf '\n'; exit 1
    fi
    [ "$_n" -eq 2 ] && log_info "PostgreSQL del compose está en localhost:$PUERTO_DB — la instancia 2 se conecta ahí"

    LOG_N="$ESTADO/dgt-$_n.log"
    log_info "Instancia $_n (dgt-$_n) en el puerto ${PUERTO_N}… (log en $LOG_N)"
    ( cd "$APP" && DGT_INSTANCIA="dgt-$_n" \
        DGT_CIERRE_RETARDO_MS="$RETARDO_MS" DGT_CIERRE_INTERVALO_MS="$INTERVALO_MS" \
        DGT_CIERRE_CRON="$CRON_DEMO" \
        exec nohup ./mvnw -q spring-boot:run \
          -Dspring-boot.run.arguments="--server.port=$PUERTO_N --spring.docker.compose.enabled=false --spring.datasource.url=jdbc:postgresql://localhost:$PUERTO_DB/dgt --spring.datasource.username=dgt --spring.datasource.password=dgt-dev --spring.flyway.enabled=false" \
          </dev/null >"$LOG_N" 2>&1 ) &
    echo $! > "$ESTADO/dgt-$_n.pid"

    if esperar_url "http://localhost:$PUERTO_N/actuator/health" 180; then
        paso_ok "Instancia $_n viva en el puerto $PUERTO_N (misma base que la 1)"
    else
        paso_fail "La instancia $_n no respondió en 180 segundos" "Mira el log:  tail -n 40 $LOG_N"
        printf '\n'; exit 1
    fi
    _n=$((_n + 1))
done

# -----------------------------------------------------------------------------
#  El crimen (--instancias >= 2): esperar el latido y contar los cierres
# -----------------------------------------------------------------------------
if [ "$INSTANCIAS" -ge 2 ]; then
    printf '\n  --- El crimen: dos servidores, y los dos se creyeron el único ---\n\n'
    log_info "Esperando el latido del cierre nocturno…"

    consulta() { ( cd "$APP" && docker compose exec -T postgres psql -U dgt -d dgt -tAc "$1" 2>/dev/null ); }

    # MARCA: el último id de cierre ANTES de empezar a contar. Solo se cuentan los posteriores.
    # Sin esta marca, un cierre disparado por la instancia 1 mientras la 2 todavía arrancaba
    # contaría como "doble ejecución" sin serlo.
    #
    # Se marca por ID y no por marca de tiempo A PROPÓSITO: `ejecutado_en` lo escribe la JVM (hora
    # de Santiago) y `now()` lo evalúa el contenedor de PostgreSQL (UTC). Compararlos daba cero
    # siempre. Es exactamente el error del TODO_1 —dos relojes que no son el mismo— mordiendo en el
    # guion del propio laboratorio. El id es monótono y no sabe de husos.
    MARCA="$(consulta 'SELECT COALESCE(MAX(id), 0) FROM cierre_diario;' | tr -d ' \r')"
    log_info "Esperando el latido (cron $CRON_DEMO: las dos instancias laten en el mismo segundo)…"

    # Espera ACTIVA sobre la condición, no un sleep ciego.
    _i=0
    while [ "$_i" -lt 90 ]; do
        CUANTOS="$(consulta "SELECT COUNT(*) FROM cierre_diario WHERE id > $MARCA;" | tr -d ' \r')"
        [ "${CUANTOS:-0}" -ge 1 ] && break
        _i=$((_i + 1)); sleep 1
    done
    # Margen para que la OTRA instancia también lata: late en el mismo segundo, así que
    # unos pocos segundos bastan y sobran.
    _i=0; while [ "$_i" -lt 6 ]; do _i=$((_i + 1)); sleep 1; done

    printf '  Cierres ejecutados desde que las %s instancias están vivas:\n\n' "$INSTANCIAS"
    consulta "SELECT '     id=' || id || '  instancia=' || instancia || '  tramites=' || tramites || '  total=' || total_declarado FROM cierre_diario WHERE id > $MARCA ORDER BY id;"
    printf '\n'

    CUANTOS="$(consulta "SELECT COUNT(*) FROM cierre_diario WHERE id > $MARCA;" | tr -d ' \r')"
    if [ "${CUANTOS:-0}" -eq 0 ]; then
        paso_fail "El latido no llegó a dispararse en 90 segundos" \
                  "No es el crimen ni la solución: es que la tarea no corrió. Mira: tail -n 30 $ESTADO/dgt-1.log"
    elif [ "${CUANTOS:-0}" -ge 2 ]; then
        paso_warn "El cierre corrió $CUANTOS veces en el mismo latido. Los totales están duplicados." \
                  "Ese es el crimen. Empieza por guia/02-dos-servidores-un-reloj.md"
        printf '\n'
        printf '     Y en el log de cada instancia verás la MISMA tarea ejecutándose:\n'
        printf '        grep "consolidado" %s\n'   "$ESTADO/dgt-1.log"
        printf '        grep "consolidado" %s\n\n' "$ESTADO/dgt-2.log"
    else
        paso_ok "El cierre corrió UNA sola vez, con $INSTANCIAS instancias latiendo a la vez" \
                "Eso es la solución: el candado vive en la base, no en la configuración."
        printf '\n'
        printf '     Y la instancia que no ganó lo dice en su log:\n'
        for _l in "$ESTADO"/dgt-*.log; do printf '        grep "no tomé el candado" %s\n' "$_l"; done
        printf '\n'
    fi
fi

printf '\n'
log_info "Cuando termines:  ./bin/99-destruir.sh"
printf '\n'
