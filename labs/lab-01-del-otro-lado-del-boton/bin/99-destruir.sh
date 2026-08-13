#!/usr/bin/env bash
# =============================================================================
#  99-destruir.sh — deja tu máquina como estaba antes del Lab 01
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
PID="$ESTADO/dgt.pid"

printf '\n  Desmontando el Lab 01\n\n'

TENIAMOS_PID=0
if [ -f "$PID" ]; then
    TENIAMOS_PID=1
    NUM_PID="$(cat "$PID")"
    if kill -0 "$NUM_PID" 2>/dev/null; then
        pkill -P "$NUM_PID" 2>/dev/null
        kill "$NUM_PID" 2>/dev/null
        espera=0
        while kill -0 "$NUM_PID" 2>/dev/null && [ "$espera" -lt 15 ]; do espera=$((espera + 1)); sleep 1; done
        paso_ok "API detenida (PID $NUM_PID)"
    else
        paso_ok "La API ya no estaba corriendo"
    fi
    rm -f "$PID"
else
    paso_ok "La API no estaba corriendo"
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

# El PostgreSQL embebido es proceso HIJO del JVM de la app: cuando el contexto
# de Spring se cierra, el bean lo apaga y el proceso se va con él. Aquí no hay
# nada que bajar — solo que comprobar que efectivamente se fue.
#
# Y ojo con la tentación de `pkill postgres`: eso mataría la base del proyecto
# del trabajo, la de otro curso y cualquier PostgreSQL que el alumno tenga
# levantado. Se MIRAN, no se matan. Es la misma regla que aplicábamos a los
# contenedores ajenos, y la razón por la que existe.
HUERFANOS="$(pgrep -f 'embedded-pg/PG-.*/bin/postgres' 2>/dev/null | wc -l | tr -d ' ')"
if [ "${HUERFANOS:-0}" -eq 0 ]; then
    paso_ok "PostgreSQL embebido detenido: no quedó ningún proceso"
else
    paso_warn "Veo $HUERFANOS proceso(s) de PostgreSQL embebido todavía vivos"
    log_info "No los mato por nombre: podrían ser de otro proyecto tuyo."
    log_info "Míralos con:   pgrep -fl 'embedded-pg/PG-'"
    log_info "Si son de este lab, ciérralos por PID:   kill <pid>"
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
