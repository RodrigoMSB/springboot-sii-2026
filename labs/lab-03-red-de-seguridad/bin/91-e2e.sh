#!/usr/bin/env bash
# =============================================================================
#  91-e2e.sh — el encadenamiento, verificado
# -----------------------------------------------------------------------------
#  Demuestra DOS cosas de una sola pasada:
#
#    1. El `starter/` está GENUINAMENTE INCOMPLETO: `90-validar.sh` sale 1.
#    2. El `starter/` TIENE SOLUCIÓN: aplicándola, `90-validar.sh` sale 0.
#
#  Sin (1), el lab no enseña nada: el alumno "aprueba" sin escribir una línea.
#  Sin (2), el enunciado es imposible y el validador, un capricho.
#
#  Trabaja sobre una COPIA desechable (.e2e/). Jamás toca tu `starter/`: puedes
#  correrlo en mitad del laboratorio sin perder tu trabajo.
# =============================================================================
set -uo pipefail

DIR_BIN="$(cd "$(dirname "$0")" && pwd)"
# shellcheck source-path=SCRIPTDIR
# shellcheck source=../../lib/lib-comunes.sh
. "$DIR_BIN/../../lib/lib-comunes.sh"

DIR_LAB="$(cd "$DIR_BIN/.." && pwd)"
BANCO="$DIR_LAB/.e2e"

limpiar() { rm -rf "$BANCO" "$DIR_LAB"/.respaldo-*; }
trap limpiar EXIT

printf '\n  Lab 03 · encadenamiento starter -> solucion\n\n'

limpiar
mkdir -p "$BANCO"
( cd "$DIR_LAB/starter" && tar cf - --exclude='./target' --exclude='./.estado' . ) | ( cd "$BANCO" && tar xf - )
log_info "Copia desechable del starter en .e2e/ (tu starter/ no se toca)"

# --- 1 · El starter virgen debe FALLAR ---------------------------------------
printf '\n  --- 1/2 · el starter virgen debe fallar ---\n'
if "$DIR_BIN/90-validar.sh" --dir "$BANCO" >/dev/null 2>&1; then
    paso_fail "El starter virgen APRUEBA el validador" \
              "El lab no enseña nada: el alumno pasaría sin escribir una línea."
else
    paso_ok "El starter virgen falla el validador (exit 1), como debe"
fi

# --- 2 · Aplicada la solución, debe PASAR ------------------------------------
printf '\n  --- 2/2 · con la solución aplicada debe pasar ---\n'
"$DIR_BIN/95-recuperar.sh" --todo --dir "$BANCO" >/dev/null 2>&1
if "$DIR_BIN/90-validar.sh" --dir "$BANCO" >/dev/null 2>&1; then
    paso_ok "Con la solución aplicada, el validador aprueba (exit 0)"
else
    paso_fail "Ni siquiera la solución pasa el validador" \
              "El validador está roto, o la solución lo está. Arréglalo antes de dar clase."
fi

printf '\n'
resumen_final "ENCADENAMIENTO CORRECTO — el starter está incompleto y tiene solución" \
              "ENCADENAMIENTO ROTO"
exit $?
