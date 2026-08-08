#!/usr/bin/env bash
# =============================================================================
#  95-recuperar.sh — la red de seguridad del Lab 14
# -----------------------------------------------------------------------------
#    ./bin/95-recuperar.sh                    # copia la configuración de referencia
#    ./bin/95-recuperar.sh --solo-velocidad   # devuelve el proveedor a velocidad normal
#    ./bin/95-recuperar.sh --solo-instancias  # vuelve a dejar dos instancias vivas
#
#  RESPALDA ANTES DE SOBRESCRIBIR, siempre. Lo que escribiste no se pierde: se
#  guarda con fecha en .respaldo-AAAAMMDD-HHMMSS/, y el script te dice dónde.
#  Nadie tiene por qué elegir entre pedir ayuda y conservar su trabajo.
# =============================================================================
set -uo pipefail

DIR_BIN="$(cd "$(dirname "$0")" && pwd)"
# shellcheck source-path=SCRIPTDIR
# shellcheck source=../../lib/lib-comunes.sh
. "$DIR_BIN/../../lib/lib-comunes.sh"

DIR_LAB="$(cd "$DIR_BIN/.." && pwd)"
# shellcheck source-path=SCRIPTDIR
# shellcheck source=lib-sistema.sh
. "$DIR_BIN/lib-sistema.sh"

PUERTO_PORTAL="$DGT_PUERTO_DEFECTO"
PUERTO_REGISTRO=8761
MODO="configuracion"

while [ $# -gt 0 ]; do
    case "$1" in
        --solo-velocidad)  MODO="velocidad"; shift ;;
        --solo-instancias) MODO="instancias"; shift ;;
        -h|--help)
            printf 'Uso: %s [--solo-velocidad | --solo-instancias]\n' "$(basename "$0")"; exit 0 ;;
        *) printf '[ERROR] Argumento no reconocido: %s\n' "$1" >&2; exit 2 ;;
    esac
done

SIS="$(sistema_dir)"

# -----------------------------------------------------------------------------
#  Devolver el proveedor a su velocidad normal
# -----------------------------------------------------------------------------
if [ "$MODO" = "velocidad" ]; then
    printf '\n  Devolviendo dgt-contribuyentes a velocidad normal\n\n'
    if ! docker info >/dev/null 2>&1; then
        paso_fail "Docker no responde" "Abre Docker Desktop."
        printf '\n'; exit 1
    fi
    DGT_RETARDO_MS=0 dc up -d --force-recreate --scale dgt-contribuyentes=2 dgt-contribuyentes >/dev/null 2>&1
    dc restart dgt-tramites >/dev/null 2>&1
    if esperar_sistema_estable 180; then
        paso_ok "El proveedor responde rápido otra vez y el sistema está estable"
    else
        paso_warn "Recreado, pero el sistema aún no da respuestas completas" \
                  "Dale unos segundos y prueba:  ./bin/90-validar.sh"
    fi
    printf '\n'; exit 0
fi

# -----------------------------------------------------------------------------
#  Volver a dos instancias vivas
# -----------------------------------------------------------------------------
if [ "$MODO" = "instancias" ]; then
    printf '\n  Devolviendo el sistema a DOS instancias de dgt-contribuyentes\n\n'
    if ! docker info >/dev/null 2>&1; then
        paso_fail "Docker no responde" "Abre Docker Desktop."
        printf '\n'; exit 1
    fi
    dc up -d --scale dgt-contribuyentes=2 dgt-contribuyentes >/dev/null 2>&1
    if esperar_piezas 6 240; then
        paso_ok "Seis piezas anotadas otra vez"
    else
        paso_warn "El registro solo ve $(piezas_anotadas)" "Mira:  cd sistema && docker compose ps -a"
    fi
    printf '\n'; exit 0
fi

# -----------------------------------------------------------------------------
#  Restaurar la configuración de referencia
# -----------------------------------------------------------------------------
printf '\n  Restaurando la configuración de referencia\n\n'

ORIGEN="$SIS/config-repo-solucion"
DESTINO="$SIS/config-repo"

[ -d "$ORIGEN" ] || { paso_fail "No encuentro $ORIGEN" "¿Se borró? Recupéralo con git."; printf '\n'; exit 1; }

SELLO="$(date +%Y%m%d-%H%M%S)"
RESPALDO="$DIR_LAB/.respaldo-$SELLO"
mkdir -p "$RESPALDO"
cp -R "$DESTINO" "$RESPALDO/" 2>/dev/null
paso_ok "Tu configuración quedó respaldada en .respaldo-$SELLO/"

cp "$ORIGEN"/*.yml "$DESTINO"/ 2>/dev/null
paso_ok "config-repo/ ahora tiene la configuración de referencia"

printf '\n'
log_info "Lo tuyo NO se ha perdido. Está en:"
log_info "  $RESPALDO/config-repo/dgt-tramites.yml"
printf '\n'
log_info "Y el paso que importa: ABRE LOS DOS Y COMPÁRALOS."
log_info "La casilla del reporte que pregunta si consultaste la solución no es una"
log_info "trampa — mirarla no está prohibido, está registrado. Se evalúa la"
log_info "honestidad, no la pureza."
printf '\n'
log_info "Para que el sistema tome la configuración nueva:"
log_info "  ./bin/start-lab.sh --reiniciar-tramites"
printf '\n'
