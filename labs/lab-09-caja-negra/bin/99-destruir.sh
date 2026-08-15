#!/usr/bin/env bash
# =============================================================================
#  99-destruir.sh — deja tu máquina como estaba antes del Lab 09
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

printf '\n  Desmontando el Lab 09\n\n'

TENIAMOS_PID=0
if [ -f "$PID" ]; then
    TENIAMOS_PID=1
    NUM_PID="$(cat "$PID")"
    if kill -0 "$NUM_PID" 2>/dev/null; then
        pkill -P "$NUM_PID" 2>/dev/null
        kill "$NUM_PID" 2>/dev/null
        espera=0
        while kill -0 "$NUM_PID" 2>/dev/null && [ "$espera" -lt 15 ]; do espera=$((espera + 1)); sleep 1; done
        # El [OK] depende de que el proceso se haya ido DE VERDAD, no de que se lo
        # hayamos pedido: si aguanta los 15 s, decirlo es mentir (SPEC-FIX-05).
        if kill -0 "$NUM_PID" 2>/dev/null; then
            paso_fail "La API (PID $NUM_PID) sigue viva tras 15 s" \
                      "Ciérrala a mano:  kill -9 $NUM_PID"
        else
            paso_ok "API detenida (PID $NUM_PID)"
        fi
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

# Las DOS piezas embebidas de este lab viven dentro del JVM de la app, y por eso
# aquí no hay nada que bajar: solo que comprobar que se fueron.
#
#   · PostgreSQL (Zonky) es proceso HIJO del JVM: el bean lo apaga al cerrarse el
#     contexto de Spring y el proceso se va con él.
#   · TESO (WireMock) ni siquiera es un proceso: corre DENTRO del JVM, así que
#     muere con él por definición. Lo que se comprueba de TESO es su puerto.
#
# Y ojo con la tentación de `pkill postgres` o `pkill java`: eso mataría la base
# del proyecto del trabajo, la de otro curso y cualquier cosa que el alumno tenga
# levantada. Se MIRAN, no se matan. Es la misma regla que aplicábamos a los
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

# TESO: lo que importa es que su puerto quede libre para el próximo arranque.
if command -v lsof >/dev/null 2>&1 && lsof -nP -iTCP:8089 -sTCP:LISTEN >/dev/null 2>&1; then
    paso_warn "El puerto 8089 (TESO simulado) sigue ocupado"
    log_info "Míralo con:   lsof -nP -iTCP:8089 -sTCP:LISTEN"
else
    paso_ok "TESO simulado detenido: el puerto 8089 quedó libre"
fi

# Cada borrado responde si pudo o no; el veredicto suma esas respuestas en vez de
# darlas por buenas. Aqui vivia el bug que da nombre a la SPEC-FIX-05: el cinturon
# de borrar_seguro abortaba y el script felicitaba igual.
BORRADOS_OK=1
borrar_seguro "$ESTADO"       || BORRADOS_OK=0
borrar_seguro "$DIR_LAB/.e2e" || BORRADOS_OK=0
for _r in "$DIR_LAB"/.respaldo-*; do
    [ -e "$_r" ] && { borrar_seguro "$_r" || BORRADOS_OK=0; }
done
if [ "$BORRADOS_OK" -eq 1 ]; then
    paso_ok "Archivos temporales del lab borrados"
else
    paso_fail "Quedó algún temporal sin borrar" \
              "Mira los [ERROR] de arriba: el cinturón de borrar_seguro se negó, y por algo será."
fi

resumen_final "Todo quedó como estaba" "Quedó algo a medio desmontar"
VEREDICTO=$?
printf '\n'
log_info "«Todo quedó como estaba» se refiere a lo que ESTE laboratorio creó."
printf '\n'
exit "$VEREDICTO"
