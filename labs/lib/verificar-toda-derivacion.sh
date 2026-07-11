#!/usr/bin/env bash
# =============================================================================
#  verificar-toda-derivacion.sh — el job `deriva`, sobre todos los labs
# -----------------------------------------------------------------------------
#  Recorre la CADENA de derivacion y verifica que nada divergio en silencio:
#    tronco -> Lab01 solucion -> Lab02 solucion -> ...   (encadenamiento)
#    cada solucion -> su propio starter                  (los huecos + el crimen)
#
#  Cada eslabon declara sus divergencias intencionales en un allowlist. Correr
#  desde la raiz del repo.
# =============================================================================
set -uo pipefail

DIR_LIB="$(cd "$(dirname "$0")" && pwd)"
RAIZ="$(cd "$DIR_LIB/../.." && pwd)"
VERIF="$DIR_LIB/verificar-derivacion.sh"
cd "$RAIZ" || exit 2

FALLOS=0
chequear() {
    if "$VERIF" "$1" "$2" "$3"; then :; else FALLOS=$((FALLOS + 1)); fi
}

TRONCO="dgt-tramites-api"
ANTERIOR="$TRONCO"

# La cadena de solucion/, en orden. Añade un lab aquí cuando nazca.
for LAB in labs/lab-01-del-otro-lado-del-boton labs/lab-02-el-folio-que-se-filtro labs/lab-03-red-de-seguridad labs/lab-04-el-arbol-de-tramites labs/lab-05-once-segundos labs/lab-06-dos-folios-un-numero; do
    [ -d "$LAB/solucion" ] || continue
    chequear "$ANTERIOR"      "$LAB/solucion" "$LAB/derivacion-solucion.txt"
    chequear "$LAB/solucion"  "$LAB/starter"  "$LAB/derivacion-starter.txt"
    # P-16: una segunda solución (el "antes" con N+1) que tambien deriva de solucion/.
    if [ -d "$LAB/solucion-con-n1" ]; then
        chequear "$LAB/solucion" "$LAB/solucion-con-n1" "$LAB/derivacion-con-n1.txt"
    fi
    ANTERIOR="$LAB/solucion"
done

echo
if [ "$FALLOS" -gt 0 ]; then
    echo "[ERROR] $FALLOS eslabon(es) con deriva silenciosa."
    exit 1
fi
echo "[OK] La cadena de derivacion esta en sincronia."
