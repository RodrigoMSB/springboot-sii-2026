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
for LAB in labs/lab-01-del-otro-lado-del-boton labs/lab-02-el-folio-que-se-filtro labs/lab-03-red-de-seguridad labs/lab-04-el-arbol-de-tramites labs/lab-05-once-segundos labs/lab-06-dos-folios-un-numero labs/lab-07-el-portero labs/lab-08-diplomacia-con-tesoreria labs/lab-09-caja-negra labs/lab-10-observabilidad labs/lab-11-latidos labs/lab-12-amortiguadores labs/lab-13-capsula-y-egreso labs/lab-14-la-dgt-se-parte-en-pedazos; do

    # ------------------------------------------------------------------------
    #  TERCERA FORMA de lab: el SISTEMA que no deriva de nadie (Lab 14).
    #
    #  Detectada por estructura —tiene `sistema/` y no tiene `solucion*/`—, no
    #  por nombre: la nota de abajo ya avisaba de que cablear un caso especial
    #  al nombre del lab 13 sería una bomba para el 14, y no vamos a poner la
    #  bomba nosotros con el 15.
    #
    #  Este lab NO es un eslabón de la cadena y no puede serlo: no es la app
    #  canónica con un hueco más, son CINCO proyectos Maven nuevos, con otro
    #  dominio reducido y otro `pom.xml`. Compararlo byte a byte contra el Lab 13
    #  daría una divergencia del 100 %, y un allowlist que declare «todo es
    #  distinto» no declara nada.
    #
    #  Lo que sí importa declarar es que la cadena SIGUE VIVA hasta el Lab 13 y
    #  que este lab queda FUERA a propósito. Por eso se dice en voz alta —una
    #  línea en el log del CI— en vez de dejar que el `continue` lo salte en
    #  silencio, que es justo la deriva invisible que este job existe para
    #  impedir.
    # ------------------------------------------------------------------------
    if [ -d "$LAB/sistema" ] && [ ! -d "$LAB/solucion" ] && [ ! -d "$LAB/solucion-referencia" ]; then
        echo "[INFO] $LAB es un SISTEMA propio (5 proyectos Maven nuevos): fuera de la"
        echo "       cadena de derivacion a proposito. Ver docs/specs/SPEC-020 §7."
        if [ -f "$LAB/derivacion-sistema.txt" ]; then
            echo "[OK]   $LAB declara su exclusion en derivacion-sistema.txt"
        else
            echo "[ERROR] $LAB no declara por que esta fuera de la cadena."
            echo "        Falta $LAB/derivacion-sistema.txt"
            FALLOS=$((FALLOS + 1))
        fi
        # ANTERIOR no avanza: la cadena termina en el ultimo lab que si deriva.
        continue
    fi

    # ------------------------------------------------------------------------
    #  Dos FORMAS de lab, y la diferencia se detecta por estructura, no por
    #  nombre. Nada de `if [ "$LAB" = "labs/lab-13-..." ]`: un caso especial
    #  cableado al nombre es una bomba para el lab 14.
    #
    #  · Lab con TODOs (01..12):   anterior -> solucion -> starter
    #      El starter es la solucion con huecos, asi que DERIVA de ella.
    #
    #  · Lab de EXAMEN (13):       anterior -> starter -> solucion-referencia
    #      Aqui el starter es la app entera del lab previo (no tiene huecos) y
    #      la referencia es UNA solucion posible construida ENCIMA. El sentido
    #      de la derivacion se invierte, y por eso la cadena continua por la
    #      referencia: es la que lleva el trabajo nuevo.
    # ------------------------------------------------------------------------
    if [ -d "$LAB/solucion-referencia" ]; then
        chequear "$ANTERIOR"     "$LAB/starter"              "$LAB/derivacion-starter.txt"
        chequear "$LAB/starter"  "$LAB/solucion-referencia"  "$LAB/derivacion-referencia.txt"
        ANTERIOR="$LAB/solucion-referencia"
        continue
    fi

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
