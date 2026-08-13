#!/usr/bin/env bash
# =============================================================================
#  99-destruir.sh — deja tu máquina como estaba antes del Lab 14
# -----------------------------------------------------------------------------
#  Este laboratorio levanta SIETE contenedores y construye CINCO imágenes. Es el
#  que más huella deja de todo el curso, así que el desmontaje importa más que
#  nunca — y aun así solo toca lo que ESTE laboratorio creó.
#
#  Lo que NO toca, jamás: contenedores de otros proyectos, imágenes que no
#  construyó él, y los volúmenes de nadie más.
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
BORRAR_IMAGENES=0

while [ $# -gt 0 ]; do
    case "$1" in
        --imagenes) BORRAR_IMAGENES=1; shift ;;
        -h|--help)
            printf 'Uso: %s [--imagenes]\n\n' "$(basename "$0")"
            printf '  --imagenes   borra también las cinco imágenes del lab (~1 GB)\n'
            printf '               Sin esta bandera se conservan: el próximo arranque\n'
            printf '               tarda segundos en vez de minutos.\n'
            exit 0 ;;
        *) printf '[ERROR] Argumento no reconocido: %s\n' "$1" >&2; exit 2 ;;
    esac
done

printf '\n  Desmontando el Lab 14\n\n'

if docker info >/dev/null 2>&1; then
    # `down -v` baja los siete contenedores, la red del compose y los volúmenes
    # que él mismo creó. Acotado al proyecto de compose de este laboratorio: no
    # existe forma de que alcance a un contenedor de otro proyecto.
    if [ -f "$(sistema_dir)/compose.yaml" ]; then
        dc down -v --remove-orphans >/dev/null 2>&1
        paso_ok "Las seis piezas, PostgreSQL, la red y los volúmenes del lab: abajo"
    else
        paso_skip "No encuentro el compose del laboratorio"
    fi

    if [ "$BORRAR_IMAGENES" -eq 1 ]; then
        BORRADAS=0
        for IMG in sistema-dgt-registro sistema-dgt-config sistema-dgt-portal \
                   sistema-dgt-contribuyentes sistema-dgt-tramites; do
            if docker image inspect "$IMG" >/dev/null 2>&1; then
                docker image rm -f "$IMG" >/dev/null 2>&1 && BORRADAS=$((BORRADAS + 1))
            fi
        done
        paso_ok "Imágenes del laboratorio borradas ($BORRADAS)"
        log_info "La imagen base eclipse-temurin:25-jre-alpine se queda: no la creó este lab"
        log_info "y otros la usan. Si la quieres fuera:  docker image rm eclipse-temurin:25-jre-alpine"
    else
        paso_ok "Las cinco imágenes del lab se conservan (arranque rápido la próxima vez)"
        log_info "¿Recuperar ese ~1 GB?  ./bin/99-destruir.sh --imagenes"
    fi
else
    paso_skip "Docker no responde: no hay contenedores del lab que bajar"
fi

# Los `target/` de los cinco módulos.
LIMPIADOS=0
FALLIDOS=0
for M in dgt-registro dgt-config dgt-portal dgt-contribuyentes dgt-tramites; do
    if [ -d "$(sistema_dir)/$M/target" ]; then
        if borrar_seguro "$(sistema_dir)/$M/target"; then
            LIMPIADOS=$((LIMPIADOS + 1))
        else
            FALLIDOS=$((FALLIDOS + 1))
        fi
    fi
done
if [ "$FALLIDOS" -eq 0 ]; then
    paso_ok "Artefactos de compilación borrados ($LIMPIADOS módulos)"
else
    paso_fail "$FALLIDOS módulo(s) conservan su target/" \
              "Mira los [ERROR] de arriba: el cinturón de borrar_seguro se negó."
fi

for _r in "$DIR_LAB"/.respaldo-*; do
    [ -e "$_r" ] || continue
    log_info "Conservo tu respaldo: $(basename "$_r")  (tu configuración está ahí dentro)"
done

rm -f "$(sistema_dir)/.tests-90.log" "$(sistema_dir)/.arbol-dependencias.txt" 2>/dev/null

resumen_final "Todo quedó como estaba" "Quedó algo a medio desmontar"
VEREDICTO=$?
printf '\n'
log_info "«Todo quedó como estaba» se refiere a lo que ESTE laboratorio creó."
log_info "Si ves contenedores de otros proyectos, son tuyos y siguen ahí a propósito."
printf '\n'
exit "$VEREDICTO"
