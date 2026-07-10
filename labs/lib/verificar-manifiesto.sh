#!/usr/bin/env bash
# =============================================================================
#  verificar-manifiesto.sh — ¿siguen intactos los tests del enunciado?
# -----------------------------------------------------------------------------
#    verificar-manifiesto.sh <ruta-al-manifiesto>     (desde la raíz del proyecto)
#
#  El manifiesto protege SOLO `src/test/java/**/enunciado/**`. Los tests que el
#  alumno escriba por iniciativa propia, en cualquier otro paquete, son territorio
#  libre: el manifiesto jamás castiga al alumno bueno.
#
#  ¿Por qué existe? Porque un enunciado que se puede editar no es un enunciado.
#  Si el alumno borra la aserción que le molesta, el validador se vuelve un espejo.
#
#  Formato: una línea por archivo, `<sha256>  <ruta-relativa>`.
#  Portabilidad: macOS trae `shasum`, Linux y Git Bash traen `sha256sum`.
# =============================================================================
set -uo pipefail

MANIFIESTO="${1:-}"
[ -f "$MANIFIESTO" ] || { printf '[ERROR] No existe el manifiesto: %s\n' "$MANIFIESTO" >&2; exit 2; }

sha_de() {
    if command -v sha256sum >/dev/null 2>&1; then
        sha256sum "$1" | awk '{print $1}'
    else
        shasum -a 256 "$1" | awk '{print $1}'
    fi
}

FALLOS=0
while IFS= read -r LINEA; do
    [ -z "$LINEA" ] && continue
    case "$LINEA" in \#*) continue ;; esac

    ESPERADO="${LINEA%% *}"
    ARCHIVO="${LINEA#* }"
    ARCHIVO="${ARCHIVO# }"

    if [ ! -f "$ARCHIVO" ]; then
        printf '[ERROR] Falta un test del enunciado: %s\n' "$ARCHIVO"
        FALLOS=$((FALLOS + 1))
        continue
    fi

    REAL="$(sha_de "$ARCHIVO")"
    if [ "$REAL" != "$ESPERADO" ]; then
        printf '[ERROR] Test del enunciado modificado: %s\n' "$ARCHIVO"
        FALLOS=$((FALLOS + 1))
    fi
done < "$MANIFIESTO"

[ "$FALLOS" -eq 0 ] || exit 1
exit 0
