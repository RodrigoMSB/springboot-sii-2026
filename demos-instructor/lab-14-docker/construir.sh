#!/usr/bin/env bash
# =============================================================================
#  construir.sh — los cuatro jar, con el Maven del curso y sin red
# -----------------------------------------------------------------------------
#  Se corre UNA VEZ antes de `docker compose up`, y otra vez cada vez que se
#  toque el código. La imagen de Docker no compila nada: solo copia el jar.
#
#  Por qué está separado de `docker compose up` y no metido dentro: porque lo que
#  la demostración tiene que enseñar es `docker compose up` a pelo, sin envoltura.
#  Si un script lo tapara, el protagonista sería el script.
#
#  Usa `./mvnw`, el shim del curso: el JDK y el Maven del repositorio, en modo
#  offline contra `repo-maven/`. Aquí NO se descarga nada — lo único de esta
#  demostración que necesita red son las dos imágenes base de Docker, y eso pasa
#  una sola vez (ver el README, «Lo que necesita»).
# =============================================================================
set -euo pipefail
cd "$(dirname "$0")/sistema"

for MODULO in gateway contribuyentes tramites auditoria; do
  echo "==> $MODULO"
  ( cd "$MODULO" && ../mvnw -q -o -DskipTests package )
done

echo
echo "Los cuatro jar:"
ls -lh */target/*.jar | awk '{print "  " $9 "   " $5}'
