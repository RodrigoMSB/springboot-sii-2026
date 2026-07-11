#!/usr/bin/env bash
# =============================================================================
#  90-validar.sh — ¿está tu trabajo listo?  (Lab 06)
# -----------------------------------------------------------------------------
#    ./bin/90-validar.sh                  # valida starter/ (tu trabajo)
#    ./bin/90-validar.sh --dir solucion   # valida la solución de referencia
#
#  EL MISMO CRITERIO JUZGA A AMBOS. No hay dos verdades.
#
#  Este lab NECESITA DOCKER: dos de sus pruebas (la ficha contra base real y el
#  contrato OpenAPI) levantan un PostgreSQL con Testcontainers. Corre `verify`,
#  no `test`. Si no tienes Docker, te lo dice claro y no finge un veredicto.
#
#  De SOLO LECTURA y SIN `set -e`. En Java el criterio se verifica con TESTS
#  COMPILADOS y ArchUnit; este script orquesta, jamás inspecciona tu código con
#  expresiones regulares (anti-herencia A-01).
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

[ -d "$PROYECTO" ]         || { printf '[ERROR] No existe: %s\n' "$PROYECTO" >&2; exit 2; }
[ -f "$PROYECTO/pom.xml" ] || { printf '[ERROR] No es un proyecto Maven: %s\n' "$PROYECTO" >&2; exit 2; }

printf '\n  Lab 06 · validando %s\n\n' "$OBJETIVO"

# -----------------------------------------------------------------------------
#  0 · Docker vivo (lo necesitan T1 y T4)
# -----------------------------------------------------------------------------
if ! docker info >/dev/null 2>&1; then
    paso_fail "El demonio de Docker no responde" \
              "Este lab prueba la ficha contra una base real. Abre Docker Desktop (T-03 del Lab 00)."
    resumen_final "no verificado" "no verificado" >/dev/null
    printf '\n[ERROR] No puedo validar sin Docker.\n\n'
    exit 1
fi

# -----------------------------------------------------------------------------
#  1 · El enunciado no se toca (manifiesto) — antes de compilar, es barato
# -----------------------------------------------------------------------------
MANIFIESTO="$DIR_LAB/manifiesto-tests.sha256"
if [ ! -f "$MANIFIESTO" ]; then
    paso_skip "No hay manifiesto de tests que verificar"
elif ( cd "$PROYECTO" && "$DIR_BIN/../../lib/verificar-manifiesto.sh" "$MANIFIESTO" >/dev/null 2>&1 ); then
    paso_ok "Los tests del enunciado están intactos"
else
    paso_fail "Alguien modificó un test del enunciado" \
              "Son el enunciado, no tu trabajo. Restáuralos:  ./bin/95-recuperar.sh --solo-enunciado"
fi

# -----------------------------------------------------------------------------
#  2 · `verify` — una sola vez (compila + tests + integración con Docker)
# -----------------------------------------------------------------------------
SALIDA="$(mktemp)"
trap 'rm -f "$SALIDA"' EXIT
log_info "Compilando y corriendo la suite completa (incluye integración; toma su tiempo)…"
( cd "$PROYECTO" && ./mvnw -B -q verify > "$SALIDA" 2>&1 )
VERIFY_EXIT=$?

if grep -q 'BUILD SUCCESS' "$SALIDA" 2>/dev/null || [ "$VERIFY_EXIT" -eq 0 ]; then
    paso_ok "Compila, y la suite completa está en verde (los 4 TODOs, arquitectura y todo lo demás)"
else
    # ¿Compiló siquiera?
    if grep -qE 'COMPILATION ERROR|BUILD FAILURE.*compile' "$SALIDA"; then
        paso_fail "El proyecto no compila" "Mira el error:  cd $OBJETIVO && ./mvnw test-compile"
    else
        paso_fail "Hay pruebas en rojo" "Estas son las que fallan:"
        grep -E '^\[ERROR\]   [A-Za-z0-9_]+\.[a-zA-Z]' "$SALIDA" | sed 's/^\[ERROR\]   /          · /' | head -12
        printf '\n'
        # Pista dirigida: ¿enunciado o arquitectura?
        if grep -qE '^\[ERROR\]   T[0-9]_' "$SALIDA"; then
            log_info "Los T#_ son tus TODOs. El número te dice cuál falta."
        fi
        if grep -qiE 'arquitectura|Architecture Violation' "$SALIDA"; then
            log_info "Hay una regla de arquitectura roja: su mensaje nombra el crimen."
        fi
    fi
fi

resumen_final "🏆 LAB 06 APROBADO — Carolina aprueba. Por ahora." \
              "LAB 06 NO APROBADO — vuelve a la guía que menciona cada error"
VEREDICTO=$?

printf '\n'
[ "$VEREDICTO" -eq 0 ] && log_info "Siguiente: completa plantillas/reporte-entregable.md y entrégalo."
printf '\n'
exit "$VEREDICTO"
