#!/usr/bin/env bash
# =============================================================================
#  99-destruir.sh — deja tu máquina como estaba antes del Lab 11
# -----------------------------------------------------------------------------
#  Solo toca lo que ESTE laboratorio levantó. Tus otros contenedores y procesos
#  —el proyecto del trabajo, otra base de datos— ni los mira.
# =============================================================================
set -uo pipefail

DIR_BIN="$(cd "$(dirname "$0")" && pwd)"
# shellcheck source-path=SCRIPTDIR
# shellcheck source=../../lib/lib-comunes.sh
. "$DIR_BIN/../../lib/lib-comunes.sh"

DIR_LAB="$(cd "$DIR_BIN/.." && pwd)"
ESTADO="$DIR_LAB/.estado"
# Este lab levanta VARIAS instancias (--instancias N), cada una con su PID.
PIDS_GLOB="$ESTADO/dgt-*.pid"

printf '\n  Desmontando el Lab 11\n\n'

TENIAMOS_PID=0
DETENIDAS=0
for PID_FILE in $PIDS_GLOB; do
    [ -e "$PID_FILE" ] || continue
    TENIAMOS_PID=1
    NUM_PID="$(cat "$PID_FILE")"
    if kill -0 "$NUM_PID" 2>/dev/null; then
        pkill -P "$NUM_PID" 2>/dev/null
        kill "$NUM_PID" 2>/dev/null
        espera=0
        while kill -0 "$NUM_PID" 2>/dev/null && [ "$espera" -lt 15 ]; do espera=$((espera + 1)); sleep 1; done
        DETENIDAS=$((DETENIDAS + 1))
    fi
    rm -f "$PID_FILE"
done
if [ "$TENIAMOS_PID" -eq 1 ]; then
    paso_ok "Instancias detenidas ($DETENIDAS)"
else
    paso_ok "No había instancias corriendo"
fi

# Acotado a las apps de ESTE lab: nunca `pkill -f spring-boot:run` a secas, que
# cazaría cualquier app Spring Boot de la máquina (lección de la SPEC-006).
for PROYECTO in starter solucion; do
    PATRON="multiModuleProjectDirectory=$DIR_LAB/$PROYECTO"
    if pgrep -f "$PATRON" >/dev/null 2>&1; then
        pkill -f "$PATRON" 2>/dev/null
        sleep 1
        [ "$TENIAMOS_PID" -eq 1 ] || paso_warn "Había una DGT huérfana en $PROYECTO/; la detuve"
    fi
done

if docker info >/dev/null 2>&1; then
    BAJADOS=0
    for PROYECTO in starter solucion; do
        if [ -f "$DIR_LAB/$PROYECTO/compose.yaml" ]; then
            ( cd "$DIR_LAB/$PROYECTO" && docker compose down -v >/dev/null 2>&1 ) && BAJADOS=$((BAJADOS + 1))
        fi
    done
    paso_ok "PostgreSQL del laboratorio detenido ($BAJADOS compose)"

    SOBRANTES="$(docker ps -q --filter label=org.testcontainers 2>/dev/null | wc -l | tr -d ' ')"
    if [ "${SOBRANTES:-0}" -gt 0 ]; then
        log_info "Veo $SOBRANTES contenedor(es) de Testcontainers, de un './mvnw verify'."
        log_info "No los toco: no los levantó este laboratorio. Cómo limpiarlos: ver T-11 del Lab 00."
    fi
else
    paso_skip "Docker no responde: no hay contenedores del lab que bajar"
fi

borrar_seguro "$ESTADO"
    borrar_seguro "$DIR_LAB/.e2e"
    for _r in "$DIR_LAB"/.respaldo-*; do [ -e "$_r" ] && borrar_seguro "$_r"; done
paso_ok "Archivos temporales del lab borrados"

resumen_final "Todo quedó como estaba" "Quedó algo a medio desmontar"
VEREDICTO=$?
printf '\n'
log_info "«Todo quedó como estaba» se refiere a lo que ESTE laboratorio creó."
printf '\n'
exit "$VEREDICTO"
