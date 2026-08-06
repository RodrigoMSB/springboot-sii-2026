#!/usr/bin/env bash
# =============================================================================
#  derivar-desde-tronco.sh — materializa el `starter/` o la `solucion/` de un lab
# -----------------------------------------------------------------------------
#  Uso:  labs/lib/derivar-desde-tronco.sh <origen> <destino>
#
#    <origen>   dgt-tramites-api                       (el tronco, SPEC-005)
#               labs/lab-01-*/solucion                 (encadenamiento: N -> N+1)
#    <destino>  labs/lab-01-*/starter
#
#  DECISIÓN DE DISEÑO (SPEC-007 §5): la derivación es una COPIA de archivos, no un
#  submódulo ni un subtree.
#
#   · Copia   -> el alumno clona el repo y abre `starter/` en su IDE. Un `pom.xml`
#               ahí mismo, sin ceremonias. Cuesta duplicación en el repo.
#   · Subtree -> ahorra bytes y crea un acoplamiento que el alumno no entiende:
#               al primer `git pull` con conflicto, perdimos la sesión.
#   · Symlink -> Git Bash en Windows los trata como archivos de texto. Muerte.
#
#  El precio de la copia es real: 12 labs × 2 proyectos. Se paga con creces la
#  primera vez que un alumno abre una carpeta y compila sin preguntar nada.
#
#  Encadenamiento (compromiso de la SPEC-000 §7.6): la `solucion/` del lab N es el
#  `starter/` del lab N+1 más los huecos nuevos. Este script materializa ese paso.
#
#  Lo que NO se copia: `target/`, `.estado/`, y los fixtures de arquitectura viven
#  igual porque son parte del tronco vigilado.
# =============================================================================
set -uo pipefail

DIR_LIB="$(cd "$(dirname "$0")" && pwd)"
# shellcheck source-path=SCRIPTDIR
# shellcheck source=lib-comunes.sh
. "$DIR_LIB/lib-comunes.sh"

if [ $# -ne 2 ]; then
    printf '[ERROR] Uso: %s <origen> <destino>\n' "$(basename "$0")" >&2
    exit 2
fi

ORIGEN="$1"
DESTINO="$2"

[ -d "$ORIGEN" ] || { paso_fail "No existe el origen: $ORIGEN"; exit 1; }
[ -f "$ORIGEN/pom.xml" ] || { paso_fail "El origen no parece un proyecto Maven: $ORIGEN"; exit 1; }

mkdir -p "$DESTINO"

# `rsync` no está garantizado en Git Bash; `tar` sí, y preserva permisos (mvnw).
( cd "$ORIGEN" && tar cf - \
      --exclude='./target' \
      --exclude='./.estado' \
      --exclude='./.git' \
      . ) | ( cd "$DESTINO" && tar xf - )

# Invalidar cualquier build previo del destino. `tar` PRESERVA las fechas, así que
# un `.class` viejo puede quedar más nuevo que la fuente recién copiada y Maven NO
# recompila: correría bytecode fantasma. Quien reemplaza fuentes limpia el target.
borrar_seguro "$DESTINO/target"

paso_ok "Derivado: $ORIGEN -> $DESTINO"
resumen_final "Derivación completa" "La derivación falló"
