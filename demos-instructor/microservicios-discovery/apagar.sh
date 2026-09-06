#!/usr/bin/env bash
# =============================================================================
#  apagar.sh — los seis abajo, y sin dejar rastro
# -----------------------------------------------------------------------------
#  Mata por PID lo que `levantar.sh` arrancó. Si algo quedó suelto de una corrida
#  anterior, `--a-lo-bruto` busca por puerto y también lo mata.
# =============================================================================
set -uo pipefail
cd "$(dirname "$0")"
ESTADO="$(pwd)/.estado"

# `./apagar.sh tramites` apaga uno solo; sin argumento, los seis.
# `./apagar.sh registro --a-lo-bruto` NO: los modificadores van siempre solos.
SERVICIOS="auditoria tramites contribuyentes gateway registro config"
case "${1:-}" in
  ""|--a-lo-bruto) ;;
  *)               SERVICIOS="$1" ;;
esac

for SERVICIO in $SERVICIOS; do
  PID_FILE="$ESTADO/$SERVICIO.pid"
  if [ -f "$PID_FILE" ] && kill -0 "$(cat "$PID_FILE")" 2>/dev/null; then
    printf '  %-15s ' "$SERVICIO"
    kill "$(cat "$PID_FILE")" 2>/dev/null && echo "abajo"
    rm -f "$PID_FILE"
  fi
done

if [ "${1:-}" = "--a-lo-bruto" ]; then
  # Los cuatro puertos de servicio se leen de `config-repo/`, que es donde viven:
  # si el bloque 2 movió uno, `--a-lo-bruto` tiene que ir a buscarlo donde está
  # AHORA, no donde estaba cuando se escribió este script.
  PUERTOS="8888 8761 55480 55481 55482"
  for S in gateway contribuyentes tramites auditoria; do
    P="$(sed -n 's/^  port: \([0-9][0-9]*\).*/\1/p' "$(dirname "$0")/config-repo/$S.yml" | head -1)"
    [ -n "$P" ] && PUERTOS="$PUERTOS $P"
  done

  for PUERTO in $PUERTOS; do
    PIDS="$(lsof -ti "tcp:$PUERTO" 2>/dev/null || true)"
    [ -n "$PIDS" ] && { echo "  puerto $PUERTO: matando $PIDS"; echo "$PIDS" | xargs kill -9 2>/dev/null; }
  done
fi

echo "Sistema abajo."
