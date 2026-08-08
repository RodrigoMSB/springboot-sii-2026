#!/usr/bin/env bash
# =============================================================================
#  91-e2e.sh — el laboratorio se prueba a sí mismo
# -----------------------------------------------------------------------------
#  Lo corre quien CONSTRUYE el material, no el alumno. Comprueba dos cosas que
#  nadie más comprueba:
#
#    1. EL PASO CANÓNICO (SPEC-000 §7.6): el starter está genuinamente
#       incompleto Y tiene solución.
#
#           90 --dir starter   -> exit 1     (el enunciado tiene un hueco real)
#           90 --dir solucion  -> exit 0     (ese hueco se puede llenar)
#
#       Un starter que ya pasa el validador es un enunciado sin ejercicio. Una
#       solución que no lo pasa es un enunciado sin salida. Las dos averías son
#       invisibles hasta que hay dieciocho personas mirando.
#
#    2. QUE LOS TESTS NO SEAN VOLUBLES: la suite corre TRES veces y se compara el
#       veredicto. Si difiere entre corridas, el gate no vale nada — es
#       exactamente lo que la rúbrica del Lab 13 castiga como «pipeline
#       deshonesto», y no vamos a cometerlo nosotros en el lab siguiente.
#
#  No levanta contenedores: el paso canónico se juega entero en los tests, que
#  leen los archivos de configuración de verdad.
# =============================================================================
set -uo pipefail

DIR_BIN="$(cd "$(dirname "$0")" && pwd)"
# shellcheck source-path=SCRIPTDIR
# shellcheck source=../../lib/lib-comunes.sh
. "$DIR_BIN/../../lib/lib-comunes.sh"

DIR_LAB="$(cd "$DIR_BIN/.." && pwd)"
SIS="$DIR_LAB/sistema"

printf '\n  E2E del Lab 14 — el laboratorio contra sí mismo\n\n'

# -----------------------------------------------------------------------------
#  Paso canónico
# -----------------------------------------------------------------------------
printf '  --- el paso canónico ---\n\n'

log_info "1/2 · el starter debe FALLAR (si pasa, no hay ejercicio)"
if "$DIR_BIN/90-validar.sh" --dir starter >/dev/null 2>&1; then
    paso_fail "El starter APRUEBA el validador" \
              "El enunciado no tiene hueco: alguien dejó los umbrales puestos en config-repo/."
else
    paso_ok "El starter falla, como debe: el hueco es real"
fi

log_info "2/2 · la solución debe PASAR (si falla, el hueco no tiene salida)"
if "$DIR_BIN/90-validar.sh" --dir solucion >/dev/null 2>&1; then
    paso_ok "La solución pasa: el hueco tiene salida"
else
    paso_fail "La solución NO pasa el validador" \
              "El enunciado no tiene salida. Corre:  ./bin/90-validar.sh --dir solucion"
fi

# -----------------------------------------------------------------------------
#  Determinismo
# -----------------------------------------------------------------------------
printf '\n  --- los tests no son volubles ---\n\n'

CORRIDAS=3
VEREDICTOS=""
_i=1
while [ "$_i" -le "$CORRIDAS" ]; do
    if ( cd "$SIS" && ./mvnw -B -q test -pl dgt-tramites \
            -Ddgt.config-repo=../config-repo-solucion >/dev/null 2>&1 ); then
        VEREDICTOS="$VEREDICTOS verde"
    else
        VEREDICTOS="$VEREDICTOS rojo"
    fi
    _i=$((_i + 1))
done

DISTINTOS="$(printf '%s' "$VEREDICTOS" | tr ' ' '\n' | grep -v '^$' | sort -u | wc -l | tr -d ' ')"
if [ "$DISTINTOS" -eq 1 ]; then
    paso_ok "Tres corridas, el mismo veredicto:$VEREDICTOS"
else
    paso_fail "Las tres corridas NO coinciden:$VEREDICTOS" \
              "Un test voluble es peor que no tener test: da confianza falsa."
fi

printf '\n'
resumen_final "EL LABORATORIO SE SOSTIENE" "EL LABORATORIO TIENE UN AGUJERO"
VEREDICTO=$?
printf '\n'
exit "$VEREDICTO"
