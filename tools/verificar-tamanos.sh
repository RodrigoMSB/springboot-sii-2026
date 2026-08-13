#!/usr/bin/env bash
# =============================================================================
#  verificar-tamanos.sh — el guard de los 95 MB (SPEC-022 · D-022-5)
# -----------------------------------------------------------------------------
#  GitHub rechaza de plano cualquier archivo de más de 100 MB. Con el material
#  autocontenido metemos jars de verdad en el repositorio, así que el día que
#  alguien capture una dependencia gorda queremos enterarnos AQUÍ y no en el
#  push que no entra.
#
#  El umbral es 95 MB, no 100: el margen es para que la advertencia llegue antes
#  del rechazo, no junto con él.
#
#  Uso:  ./tools/verificar-tamanos.sh
# =============================================================================
set -uo pipefail

UMBRAL_MB=95

DIR_RAIZ="$(cd "$(dirname "$0")/.." && pwd)"
cd "$DIR_RAIZ" || exit 2

printf '\n  Guard de tamaños (umbral: %s MB)\n\n' "$UMBRAL_MB"

# Solo lo que de verdad viaja en el repositorio: `target/` y `.git` no cuentan
# —el primero es basura de compilación que el .gitignore ya descarta—.
podar() {
    find . \( -name .git -o -name target -o -name node_modules \) -prune -o "$@"
}

GRANDES="$(podar -type f -size +${UMBRAL_MB}M -print 2>/dev/null)"

printf '  Los 10 archivos más pesados del repositorio:\n\n'
podar -type f -exec stat -f '%z %N' {} + 2>/dev/null \
    | sort -rn \
    | head -10 \
    | awk '{ printf "    %7.1f MB  %s\n", $1/1048576, substr($0, index($0,$2)) }'
printf '\n'

if [ -n "$GRANDES" ]; then
    CUANTOS="$(printf '%s\n' "$GRANDES" | wc -l | tr -d ' ')"
    printf '[ERROR] %s archivo(s) superan los %s MB:\n' "$CUANTOS" "$UMBRAL_MB"
    printf '%s\n' "$GRANDES" | sed 's|^|        |'
    printf '\n'
    printf '        GitHub rechaza los archivos de más de 100 MB. Hay que sacarlos\n'
    printf '        del repositorio ANTES de hacer push.\n'
    printf '\n'
    printf '        Y no, Git LFS no es la salida: D-022-4 lo prohíbe. LFS guarda\n'
    printf '        los objetos en endpoints que nadie ha probado desde el SII, que\n'
    printf '        es justo la incógnita que este diseño vino a eliminar.\n\n'
    exit 1
fi

printf '[OK] Ningún archivo supera los %s MB.\n\n' "$UMBRAL_MB"
exit 0
