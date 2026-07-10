#!/usr/bin/env bash
# =============================================================================
#  verificar-derivacion.sh — ¿siguen en sincronía los labs con su base?
# -----------------------------------------------------------------------------
#    verificar-derivacion.sh <base> <derivado> <allowlist>
#
#  El riesgo que ataca (hallazgo 1 de la SPEC-007): el día que el tronco cambie,
#  los `starter/` y `solucion/` de los labs quedan desactualizados EN SILENCIO.
#  Es el mismo problema fuente ⇄ build del temario, y la misma medicina.
#
#  Cómo: un `starter/`/`solucion/` se DERIVA de una base (el tronco, o la
#  `solucion/` del lab anterior). Todo archivo que exista en AMBOS debe ser
#  idéntico byte a byte — salvo los que el lab cambia a propósito (el crimen, los
#  huecos, sus dependencias nuevas). Esos NO son excepciones ad-hoc del diff:
#  están DECLARADOS en un allowlist, que es parte del contrato de derivación.
#
#  · Archivo en el derivado y no en la base  -> adición del lab. OK.
#  · Archivo en ambos, idéntico              -> heredado sin tocar. OK.
#  · Archivo en ambos, distinto, EN allowlist -> divergencia declarada. OK.
#  · Archivo en ambos, distinto, NO en allowlist -> DERIVA SILENCIOSA. Rojo.
#
#  `src/test/**/enunciado/**` se excluye: es, por definición, propio de cada lab.
# =============================================================================
set -uo pipefail

BASE="${1:-}"
DERIVADO="${2:-}"
ALLOWLIST="${3:-}"

[ -d "$BASE" ]     || { printf '[ERROR] No existe la base: %s\n' "$BASE" >&2; exit 2; }
[ -d "$DERIVADO" ] || { printf '[ERROR] No existe el derivado: %s\n' "$DERIVADO" >&2; exit 2; }

# El allowlist es opcional: sin él, se exige identidad total de lo compartido.
permitido() {
    [ -f "$ALLOWLIST" ] || return 1
    grep -qxF "$1" "$ALLOWLIST"
}

DIVERGENCIAS=0

# Recorremos los archivos del DERIVADO (los que el lab entrega).
while IFS= read -r ARCHIVO; do
    REL="${ARCHIVO#"$DERIVADO"/}"

    case "$REL" in
        target/*|*/target/*|.estado/*|*/.estado/*|.git/*|*/.git/*) continue ;;
        */enunciado/*) continue ;;
    esac

    BASE_ARCHIVO="$BASE/$REL"
    [ -f "$BASE_ARCHIVO" ] || continue   # adición del lab: no es deriva

    if ! cmp -s "$ARCHIVO" "$BASE_ARCHIVO"; then
        if permitido "$REL"; then
            :   # divergencia declarada
        else
            printf '[ERROR] Deriva silenciosa: %s difiere de la base y no está declarado\n' "$REL"
            DIVERGENCIAS=$((DIVERGENCIAS + 1))
        fi
    fi
done < <(find "$DERIVADO" -type f)

if [ "$DIVERGENCIAS" -gt 0 ]; then
    printf '[ERROR] %s archivo(s) divergieron sin declararse.\n' "$DIVERGENCIAS"
    printf '        Si el cambio es intencional, decláralo en %s\n' "${ALLOWLIST:-el allowlist del lab}"
    printf '        Si no, re-deriva desde la base con labs/lib/derivar-desde-tronco.sh\n'
    exit 1
fi

printf '[OK] %s en sincronía con su base (%s)\n' "$(basename "$DERIVADO")" "$(basename "$BASE")"
exit 0
