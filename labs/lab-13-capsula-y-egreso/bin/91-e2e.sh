#!/usr/bin/env bash
# =============================================================================
#  91-e2e.sh — el examen tiene algo que hacer, y la referencia lo hace  (Lab 13)
# -----------------------------------------------------------------------------
#  En los doce labs anteriores el 91 demostraba que el `starter/` estaba
#  genuinamente incompleto y que tenía solución. Aquí demuestra lo mismo con
#  otras palabras, porque no hay TODOs que contar:
#
#    1. El `starter/` —la app entera del Lab 12— NO aprueba el boletín: le falta
#       el consolidado. Si aprobara, el examen no pediría nada.
#    2. La `solucion-referencia/` SÍ lo aprueba. Si no, la referencia está rota
#       o el boletín es un capricho.
#    3. LA SUITE NO ES FLAKY: se corre TRES veces y el resultado debe ser el
#       mismo. Un test que a veces pasa no es una prueba, es una moneda — y el
#       eje OFICIO lo castiga.
#
#  El paso 3 es el caro (tres suites completas con contenedores) y es el que
#  más gente omite. Por eso está aquí y no como una nota al pie: sin él, "sin
#  flaky" sería un criterio que ningún mecanismo verifica (A-02).
#
#  Trabaja sobre una COPIA desechable (.e2e/): jamás toca tu entrega.
# =============================================================================
set -uo pipefail

DIR_BIN="$(cd "$(dirname "$0")" && pwd)"
# shellcheck source-path=SCRIPTDIR
# shellcheck source=../../lib/lib-comunes.sh
. "$DIR_BIN/../../lib/lib-comunes.sh"

DIR_LAB="$(cd "$DIR_BIN/.." && pwd)"
BANCO="$DIR_LAB/.e2e"
CORRIDAS=3

limpiar() { borrar_seguro "$BANCO"; for _r in "$DIR_LAB"/.respaldo-*; do [ -e "$_r" ] && borrar_seguro "$_r"; done; }
trap limpiar EXIT

printf '\n  Lab 13 · el examen pide algo, y la referencia lo entrega\n\n'

limpiar
mkdir -p "$BANCO"
( cd "$DIR_LAB/starter" && tar cf - --exclude='./target' --exclude='./.estado' . ) | ( cd "$BANCO" && tar xf - )
log_info "Copia desechable del starter en .e2e/ (tu entrega no se toca)"

# --- 1 · El starter NO debe aprobar ------------------------------------------
printf '\n  --- 1/3 · el starter (app del Lab 12) NO debe aprobar el boletín ---\n'
if "$DIR_BIN/90-validar.sh" --dir "$BANCO" --sin-imagen >/dev/null 2>&1; then
    paso_fail "El starter APRUEBA el boletín tal cual" \
              "Entonces el examen no pide nada: el consolidado del brief ya estaría."
else
    paso_ok "El starter no aprueba: falta lo que pide el brief"
fi

# --- 2 · La referencia SÍ debe aprobar ---------------------------------------
printf '\n  --- 2/3 · la solución de referencia SÍ debe aprobarlo ---\n'
if "$DIR_BIN/90-validar.sh" --dir solucion-referencia --sin-imagen >/dev/null 2>&1; then
    paso_ok "La referencia aprueba el núcleo del boletín"
else
    paso_fail "Ni la referencia aprueba el boletín" \
              "El boletín está roto, o la referencia lo está. Arréglalo antes de examinar a nadie."
fi

# --- 3 · Sin flaky: tres corridas, mismo resultado ----------------------------
printf '\n  --- 3/3 · la suite NO es flaky (%s corridas) ---\n' "$CORRIDAS"
RESULTADOS=""
_i=1
while [ "$_i" -le "$CORRIDAS" ]; do
    if ( cd "$DIR_LAB/solucion-referencia" && ./mvnw -B -q verify >/dev/null 2>&1 ); then
        RESULTADOS="$RESULTADOS verde"
        printf '     corrida %s/%s -> verde\n' "$_i" "$CORRIDAS"
    else
        RESULTADOS="$RESULTADOS rojo"
        printf '     corrida %s/%s -> ROJO\n' "$_i" "$CORRIDAS"
    fi
    _i=$((_i + 1))
done

DISTINTOS="$(printf '%s' "$RESULTADOS" | tr ' ' '\n' | grep -c . )"
IGUALES="$(printf '%s' "$RESULTADOS" | tr ' ' '\n' | grep -v '^$' | sort -u | wc -l | tr -d ' ')"
if [ "${IGUALES:-0}" -eq 1 ]; then
    case "$RESULTADOS" in
        *rojo*) paso_fail "Las $DISTINTOS corridas dan ROJO de forma consistente" \
                          "No es flaky: está roto. Arréglalo." ;;
        *)      paso_ok "Las $DISTINTOS corridas dan el mismo resultado: la suite es determinista" ;;
    esac
else
    paso_fail "SUITE FLAKY: las corridas NO dieron el mismo resultado ($RESULTADOS)" \
              "Un test que a veces pasa no es una prueba, es una moneda. El eje OFICIO lo castiga."
fi

printf '\n'
resumen_final "EL EXAMEN ESTÁ EN PIE — pide algo, tiene referencia, y su suite es determinista" \
              "EL EXAMEN NO ESTÁ EN PIE"
exit $?
