#!/usr/bin/env bash
# =============================================================================
#  90-validar.sh — ¿está tu trabajo listo?
# -----------------------------------------------------------------------------
#    ./bin/90-validar.sh                  # valida starter/ (tu trabajo)
#    ./bin/90-validar.sh --dir solucion   # valida la solución de referencia
#
#  EL MISMO CRITERIO JUZGA A AMBOS. No hay dos verdades: si la solución no pasara
#  este validador, el validador estaría roto, y el CI lo descubriría antes que tú.
#
#  De SOLO LECTURA y SIN `set -e`: acumula todas las fallas y te las dice juntas.
#  Un validador que se detiene en el primer error te obliga a correrlo siete veces.
#
#  En Java, el criterio se verifica con TESTS COMPILADOS y ArchUnit. Este script
#  orquesta: jamás inspecciona tu código con expresiones regulares (anti-herencia
#  A-01 del proyecto: un grep confunde la forma del texto con la propiedad).
# =============================================================================
set -uo pipefail

DIR_BIN="$(cd "$(dirname "$0")" && pwd)"
# shellcheck source-path=SCRIPTDIR
# shellcheck source=../../lib/lib-comunes.sh
. "$DIR_BIN/../../lib/lib-comunes.sh"

DIR_LAB="$(cd "$DIR_BIN/.." && pwd)"
OBJETIVO="starter"

while [ $# -gt 0 ]; do
    case "$1" in
        --dir)    OBJETIVO="${2:-}"; shift 2 ;;
        --dir=*)  OBJETIVO="${1#*=}"; shift ;;
        -h|--help) printf 'Uso: %s [--dir starter|solucion]\n' "$(basename "$0")"; exit 0 ;;
        *) printf '[ERROR] Argumento no reconocido: %s\n' "$1" >&2; exit 2 ;;
    esac
done

case "$OBJETIVO" in
    /*) PROYECTO="$OBJETIVO" ;;
    *)  PROYECTO="$DIR_LAB/$OBJETIVO" ;;
esac

[ -d "$PROYECTO" ]        || { printf '[ERROR] No existe: %s\n' "$PROYECTO" >&2; exit 2; }
[ -f "$PROYECTO/pom.xml" ] || { printf '[ERROR] No es un proyecto Maven: %s\n' "$PROYECTO" >&2; exit 2; }

SALIDA="$(mktemp)"
trap 'rm -f "$SALIDA"' EXIT

printf '\n  Lab 3.5 · validando %s\n\n' "$OBJETIVO"

# -----------------------------------------------------------------------------
#  1 · Compila
# -----------------------------------------------------------------------------
if ( cd "$PROYECTO" && ./mvnw -B -q test-compile > "$SALIDA" 2>&1 ); then
    paso_ok "El proyecto compila (código y tests)"
    COMPILA=1
else
    paso_fail "El proyecto no compila" "Mira el error:  cd $OBJETIVO && ./mvnw test-compile"
    COMPILA=0
fi

# -----------------------------------------------------------------------------
#  2 · El enunciado no se toca (manifiesto)
# -----------------------------------------------------------------------------
#  Protege SOLO src/test/java/**/enunciado/**. Los tests que escribas tú, en
#  cualquier otro paquete, son territorio libre: el manifiesto jamás castiga al
#  alumno bueno.
MANIFIESTO="$DIR_LAB/manifiesto-tests.sha256"
if [ ! -f "$MANIFIESTO" ]; then
    paso_skip "No hay manifiesto de tests que verificar"
elif ( cd "$PROYECTO" && "$DIR_BIN/../../lib/verificar-manifiesto.sh" "$MANIFIESTO" >/dev/null 2>&1 ); then
    paso_ok "Los tests del enunciado están intactos"
else
    paso_fail "Alguien modificó un test del enunciado" \
              "Los tests de src/test/java/**/enunciado/** son el enunciado, no tu trabajo. Restáuralos:  ./bin/95-recuperar.sh --solo-enunciado"
fi

# -----------------------------------------------------------------------------
#  3 · Los cuatro TODOs (tests del enunciado)
# -----------------------------------------------------------------------------
if [ "$COMPILA" -eq 1 ]; then
    # Patrón de RUTA, no de paquete (un patrón que no casa corre CERO tests y "pasa":
    # gate decorativo). Sin failIfNoSpecifiedTests=false: si deja de casar, que grite.
    # `verify` y no `test`: tres de los cuatro compromisos de este lab son de
    # INTEGRACIÓN (*IT) y los corre failsafe, no surefire. Se apuntan los dos
    # plugins, cada uno con su patrón de RUTA, y sin `failIfNoSpecifiedTests=false`:
    # si algún patrón deja de casar, que grite en vez de pasar en verde con cero.
    if ( cd "$PROYECTO" && ./mvnw -B -q verify \
            -Dtest='**/enunciado/*Test.java' -Dit.test='**/enunciado/*IT.java' \
            > "$SALIDA" 2>&1 ); then
        paso_ok "Los compromisos del enunciado están todos en verde"
    else
        # El conteo exacto (Tests run / Failures / Errors), sumado sobre las clases.
        RESUMEN="$(grep -hE 'Tests run: [0-9]+, Failures' "$SALIDA" | tail -1 | sed 's/.*Tests run/Tests run/')"
        paso_fail "La suite llega en ROJO — cada test es un compromiso que aún no cumples" \
                  "${RESUMEN:-hay tests en rojo}"
        # Los COMPROMISOS, en español, leídos de los propios tests: la suite ES la spec.
        printf '\n          Los compromisos de esta sesión (cada @DisplayName es un test):\n'
        find "$PROYECTO/src/test/java" -path '*/enunciado/*.java' -exec \
            grep -hoE '@DisplayName\("([^"]+)"\)' {} + 2>/dev/null \
            | sed 's/@DisplayName("//; s/")$//; s/^/            · /' | head -20
        printf '\n          Ábrelos y léelos: el que implementa sin leer el test, implementa otra cosa.\n'
    fi
else
    paso_skip "Tests del enunciado (el proyecto no compila)"
fi

# -----------------------------------------------------------------------------
#  4 · Las reglas de la casa siguen en pie
# -----------------------------------------------------------------------------
if [ "$COMPILA" -eq 1 ]; then
    if ( cd "$PROYECTO" && ./mvnw -B -q test -Dtest='**/arquitectura/*Test.java' > "$SALIDA" 2>&1 ); then
        paso_ok "Las 7 reglas de la casa siguen verdes (y sus 7 mordidas)"
    else
        paso_fail "Rompiste una regla de arquitectura" \
                  "El mensaje del test nombra el crimen. Léelo:  cd $OBJETIVO && ./mvnw test -Dtest='*arquitectura*'"
    fi
else
    paso_skip "Reglas de arquitectura (el proyecto no compila)"
fi

# -----------------------------------------------------------------------------
#  5 · Nada de lo que ya funcionaba se rompió
# -----------------------------------------------------------------------------
if [ "$COMPILA" -eq 1 ]; then
    # Se EXCLUYE el enunciado: sus fallos ya los reporta el paso 3. Aquí se mide otra
    # cosa distinta —¿rompiste algo que ya funcionaba?— y mezclarlas haría que el
    # alumno leyera "hay una regresión" cuando solo le faltan TODOs por hacer.
    if ( cd "$PROYECTO" && ./mvnw -B -q verify \
            -Dtest='!**/enunciado/*' -Dit.test='!**/enunciado/*' \
            > "$SALIDA" 2>&1 ); then
        paso_ok "No rompiste nada de lo que ya funcionaba"
    else
        paso_fail "Algo que antes funcionaba dejó de funcionar" \
                  "Corre la suite entera y mira qué:  cd $OBJETIVO && ./mvnw test"
    fi
else
    paso_skip "Suite completa (el proyecto no compila)"
fi

resumen_final "🏆 LAB 3.5 APROBADO — el apóstrofe dejó de ser código" \
              "LAB 3.5 NO APROBADO — cada [ERROR] de arriba trae su \"->\" con qué hacer"
VEREDICTO=$?

printf '\n'
if [ "$VEREDICTO" -eq 0 ]; then
    log_info "Siguiente: completa plantillas/reporte-entregable.md y entrégalo."
fi
printf '\n'
exit "$VEREDICTO"
