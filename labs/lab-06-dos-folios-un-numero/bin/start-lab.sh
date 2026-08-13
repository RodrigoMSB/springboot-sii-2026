#!/usr/bin/env bash
# =============================================================================
#  start-lab.sh — levanta la DGT del Lab 06 (tu starter/, o la solución)
# -----------------------------------------------------------------------------
#    ./bin/start-lab.sh                       # levanta starter/ en el puerto 8099
#    ./bin/start-lab.sh --dir solucion        # levanta la solución
#    ./bin/start-lab.sh --concurrencia 2      # …y dispara el sabotaje de concurrencia
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
CONCURRENCIA=0

es_numero() { printf '%s' "${1:-}" | grep -q '^[0-9][0-9]*$'; }

while [ $# -gt 0 ]; do
    case "$1" in
        --puerto)  PUERTO="${2:-}"; es_numero "$PUERTO" || { printf '[ERROR] --puerto necesita un número\n' >&2; exit 2; }; shift 2 ;;
        --puerto=*) PUERTO="${1#*=}"; es_numero "$PUERTO" || { printf '[ERROR] --puerto necesita un número\n' >&2; exit 2; }; shift ;;
        --dir)     OBJETIVO="${2:-}"; shift 2 ;;
        --dir=*)   OBJETIVO="${1#*=}"; shift ;;
        --concurrencia)   CONCURRENCIA="${2:-}"; es_numero "$CONCURRENCIA" || { printf '[ERROR] --concurrencia necesita un número\n' >&2; exit 2; }; shift 2 ;;
        --concurrencia=*) CONCURRENCIA="${1#*=}"; es_numero "$CONCURRENCIA" || { printf '[ERROR] --concurrencia necesita un número\n' >&2; exit 2; }; shift ;;
        -h|--help) printf 'Uso: %s [--dir starter|solucion] [--concurrencia N] [--puerto N]\n' "$(basename "$0")"; exit 0 ;;
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
#  El sabotaje de concurrencia (--concurrencia N)
# -----------------------------------------------------------------------------
#  Dispara N emisiones de folio EN PARALELO, cada una sobre un trámite distinto.
#  El contador es el único recurso compartido: si la emisión no lo bloquea, dos
#  peticiones se llevan el mismo número. La PK del folio impide que el duplicado
#  se GUARDE —así que verás una emisión caerse con HTTP 500 (violación de clave)—:
#  la base te salvó del duplicado real, a costa de tumbar una emisión VÁLIDA.
#
#  La carrera es una carrera: puede no salir a la primera. Reintentamos.
crear_tramite() {
    curl -s -X POST "$BASE" -H 'Content-Type: application/json' \
        -d '{"rutContribuyente":"11111111-1","tipo":"DECLARACION_F29"}' \
        | grep -oE '"id":[0-9]+' | head -1 | grep -oE '[0-9]+'
}

sabotear() {
    n="$1"
    ids=""
    i=0
    while [ "$i" -lt "$n" ]; do
        id="$(crear_tramite)"
        [ -n "$id" ] || { paso_warn "No pude crear un trámite para el sabotaje"; return 1; }
        ids="$ids $id"
        i=$((i + 1))
    done

    tmp="$(mktemp -d)"
    : > "$tmp/numeros"
    i=0
    for id in $ids; do
        i=$((i + 1))
        ( curl -s -o "$tmp/body.$i" -w '%{http_code}' -X POST "$BASE/$id/folio" > "$tmp/code.$i" 2>/dev/null ) &
    done
    wait

    printf '\n'
    hubo_500=0
    i=0
    for id in $ids; do
        i=$((i + 1))
        code="$(cat "$tmp/code.$i" 2>/dev/null)"
        body="$(cat "$tmp/body.$i" 2>/dev/null)"
        num="$(printf '%s' "$body" | grep -oE '"numero":[0-9]+' | grep -oE '[0-9]+')"
        if [ "$code" = "201" ] || [ "$code" = "200" ]; then
            printf '     trámite %-4s -> HTTP %s   folio %s\n' "$id" "$code" "${num:-?}"
            [ -n "$num" ] && printf '%s\n' "$num" >> "$tmp/numeros"
        else
            printf '     trámite %-4s -> HTTP %s   ¡EMISIÓN CAÍDA! (la carrera la reventó)\n' "$id" "$code"
            hubo_500=1
        fi
    done

    # ¿dos emisiones se llevaron el mismo número?
    dup="$(sort "$tmp/numeros" | uniq -d)"
    rm -rf "$tmp"
    if [ "$hubo_500" -eq 1 ] || [ -n "$dup" ]; then
        return 0   # el crimen apareció
    fi
    return 1       # esta vez la carrera no chocó
}

if [ "$CONCURRENCIA" -gt 0 ]; then
    printf '\n  --- Sabotaje de concurrencia: %s emisiones a la vez ---\n' "$CONCURRENCIA"
    intento=0
    exito=1
    while [ "$intento" -lt 6 ]; do
        intento=$((intento + 1))
        if sabotear "$CONCURRENCIA"; then
            exito=0
            break
        fi
        log_info "Intento $intento: la carrera no chocó; reintentando…"
    done
    printf '\n'
    if [ "$exito" -eq 0 ]; then
        paso_warn "El crimen apareció (intento $intento)" \
                  "Dos emisiones se llevaron el mismo número, o una se cayó. Eso NO puede pasar en un libro foliado."
        log_info "En la solución esto no ocurre: el contador se toma con bloqueo, dentro de la transacción."
    else
        paso_ok "Tras 6 intentos, esta corrida no chocó (la carrera es una carrera)" \
                "Vuelve a intentar, o sube --concurrencia. En el starter, el choque es cuestión de tiempo."
    fi
fi

printf '\n'
printf '[OK] Pruébalo tú mismo:\n\n'
printf '     curl -i -X POST %s/1/folio      # emite un folio (201)\n' "$BASE"
printf '     curl -i -X POST %s/1/folio      # reintenta: ¿200 con el MISMO folio? (RN-05)\n' "$BASE"
printf '     ./bin/start-lab.sh --dir %s --concurrencia 2   # el sabotaje\n\n' "$OBJETIVO"
log_info "Cuando termines:  ./bin/99-destruir.sh"
printf '\n'
