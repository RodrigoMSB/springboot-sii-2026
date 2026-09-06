#!/usr/bin/env bash
# =============================================================================
#  construir.sh — los seis jar ejecutables
# -----------------------------------------------------------------------------
#  ⚠️  ESTE ES EL ÚNICO PASO DE LA DEMOSTRACIÓN QUE NECESITA RED, y solo la
#      primera vez. Hay que correrlo ANTES de la clase.
#
#  Por qué necesita red, dicho sin rodeos:
#
#  El resto del curso compila OFFLINE contra `repo-maven/`, el repositorio Maven
#  que viaja dentro del repositorio (D-022-3). Eureka y el Config Server NO están
#  ahí, y no se metieron a propósito: son megas de artefactos de Spring Cloud que
#  acabarían en el clon de cada uno de los dieciocho alumnos, para un material que
#  ningún alumno ejecuta. `demos-instructor/` es, por definición, lo que no viaja
#  en la maleta — y ésta es la primera vez que esa regla tiene consecuencias para
#  Maven y no solo para Docker.
#
#  Así que se usa `DGT_ONLINE=1`, el modo que el propio shim del curso trae para
#  quien PREPARA el material: sale a internet como un Maven normal y deja lo que
#  baje en el `~/.m2` de esta máquina. Después ya está todo en la caché.
#
#  Es el mismo trato que la demostración con Docker: allí «baja las dos imágenes
#  antes de la clase», aquí «compila una vez antes de la clase».
#
#  ---------------------------------------------------------------------------
#  ./construir.sh --sin-red
#
#  Añade `-o` a Maven y falla si algo no está en la caché. Sirve para comprobar,
#  el día antes, que la clase no va a necesitar wifi.
# =============================================================================
set -euo pipefail
cd "$(dirname "$0")/sistema"

SIN_RED=""
if [ "${1:-}" = "--sin-red" ]; then
  SIN_RED="-o"
  echo "==> modo --sin-red: si falta algo en la caché, esto falla (y eso es lo que se quiere saber)"
  echo
fi

# El orden no importa para compilar; es el orden de arranque, y así se lee igual
# aquí que en `levantar.sh` y que en el README.
MODULOS="registro config gateway contribuyentes tramites auditoria"

for MODULO in $MODULOS; do
  printf '==> %s\n' "$MODULO"
  ( cd "$MODULO" && DGT_ONLINE=1 ../mvnw -q -B $SIN_RED -DskipTests package )
done

echo
echo "Los seis jar:"
ls -lh */target/*.jar 2>/dev/null | awk '{print "  " $9 "   " $5}'
