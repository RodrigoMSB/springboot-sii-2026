#!/usr/bin/env bash
# =============================================================================
#  lib-aceptacion.sh — la ACEPTACIÓN del examen, desde fuera
# -----------------------------------------------------------------------------
#    lib-aceptacion.sh <ruta-al-proyecto> <nombre-de-la-imagen>
#
#  Levanta la aplicación DESDE SU IMAGEN OCI y comprueba el brief contra ella,
#  por HTTP. No mira el código y no corre los tests del alumno.
#
#  ¿Por qué desde fuera y no con un test dentro del proyecto?
#
#   · Porque el alumno decide qué prueba, y eso es materia de evaluación. Un
#     test del examinador dentro de su árbol sería un enunciado encubierto: el
#     alumno codificaría para pasarlo, que es justo lo contrario de lo que este
#     examen mide.
#   · Porque un `verify` verde puede comprarse con aserciones tautológicas. La
#     aceptación no le pregunta a sus tests: le pregunta a la aplicación.
#   · Y porque el brief exige la entrega EMPAQUETADA (§3.3). Comprobarla contra
#     la imagen y no contra `spring-boot:run` es comprobar lo que se entrega.
#
#  Las dependencias (PostgreSQL, RabbitMQ, TESO) las levanta el propio compose
#  del proyecto, y el contenedor se une a esa red para hablarles por nombre.
# =============================================================================
set -uo pipefail

DIR_BIN="$(cd "$(dirname "$0")" && pwd)"
# shellcheck source-path=SCRIPTDIR
# shellcheck source=../../lib/lib-comunes.sh
. "$DIR_BIN/../../lib/lib-comunes.sh"

PROYECTO="${1:-}"
IMAGEN="${2:-}"
if [ -z "$PROYECTO" ] || [ -z "$IMAGEN" ]; then
    printf '[ERROR] Uso: %s <proyecto> <imagen>\n' "$(basename "$0")" >&2
    exit 2
fi

PUERTO_ACEPTACION=8123
CONTENEDOR="dgt-aceptacion-$$"

# Se invoca desde el `trap` de abajo, nunca por nombre, y shellcheck no puede verlo.
# Los dos códigos son el MISMO aviso en versiones distintas: SC2329 en la local,
# SC2317 en la del runner de CI. Se declaran ambos.
# shellcheck disable=SC2317,SC2329
limpiar() {
    docker rm -f "$CONTENEDOR" >/dev/null 2>&1 || true
    ( cd "$PROYECTO" && docker compose down -v >/dev/null 2>&1 ) || true
}
trap limpiar EXIT

# --- Las dependencias, con el compose del propio proyecto ---------------------
( cd "$PROYECTO" && docker compose up -d >/dev/null 2>&1 ) || {
    printf '[ERROR] No pude levantar las dependencias (docker compose up)\n'; exit 1; }

# Las comillas simples son OBLIGATORIAS: lo de dentro es una plantilla de Go que
# interpreta `docker inspect`, no una expansión de shell. Si se expandiera aquí,
# `$k` y `$v` llegarían vacíos y el formato no devolvería nada.
# shellcheck disable=SC2016
RED="$( cd "$PROYECTO" && docker compose ps --format '{{.Name}}' 2>/dev/null | head -1 \
        | xargs -I{} docker inspect {} --format '{{range $k,$v := .NetworkSettings.Networks}}{{$k}}{{end}}' 2>/dev/null )"
[ -n "$RED" ] || { printf '[ERROR] No pude descubrir la red del compose\n'; exit 1; }

# --- La aplicación, desde su imagen ------------------------------------------
# Se le pasa todo por VARIABLE DE ENTORNO: ni una credencial dentro de la imagen.
# Es la doctrina del Lab 01 aplicada al empaquetado (M14).
docker run -d --name "$CONTENEDOR" --network "$RED" \
    -p "$PUERTO_ACEPTACION:$PUERTO_ACEPTACION" \
    -e SERVER_PORT="$PUERTO_ACEPTACION" \
    -e SPRING_DOCKER_COMPOSE_ENABLED=false \
    -e SPRING_DATASOURCE_URL=jdbc:postgresql://postgres:5432/dgt \
    -e SPRING_DATASOURCE_USERNAME=dgt \
    -e SPRING_DATASOURCE_PASSWORD=dgt-dev \
    -e SPRING_RABBITMQ_HOST=rabbitmq \
    -e SPRING_RABBITMQ_USERNAME=dgt \
    -e SPRING_RABBITMQ_PASSWORD=dgt-dev \
    -e DGT_TESO_URL=http://teso:8080 \
    -e DGT_CIERRE_RETARDO_MS=86400000 \
    "$IMAGEN" >/dev/null 2>&1 || {
        printf '[ERROR] La imagen no arrancó (docker run falló)\n'; exit 1; }

BASE="http://localhost:$PUERTO_ACEPTACION"
if ! esperar_url "$BASE/actuator/health" 180; then
    printf '[ERROR] La app no respondió en 180 s desde el contenedor\n'
    docker logs --tail 20 "$CONTENEDOR" 2>&1 | sed 's/^/          /'
    exit 1
fi

SALUD="$(curl -s --max-time 20 "$BASE/actuator/health")"
case "$SALUD" in
    *'"status":"UP"'*) printf '          health desde el contenedor -> %s\n' "$SALUD" ;;
    *) printf '[ERROR] /actuator/health no está UP: %s\n' "$SALUD"; exit 1 ;;
esac

# --- El brief, comprobado por HTTP -------------------------------------------
token_de() {
    curl -s --max-time 20 -X POST "$BASE/api/v1/auth/login" \
        -H 'Content-Type: application/json' \
        -d "{\"rut\":\"$1\",\"clave\":\"dgt-2026\"}" | sed 's/.*"token":"//; s/".*//'
}

FISCALIZADOR="$(token_de '8765432-1')"
CONTRIBUYENTE="$(token_de '11111111-1')"
[ -n "$FISCALIZADOR" ] || { printf '[ERROR] No pude autenticarme como FISCALIZADOR\n'; exit 1; }

RUTA="/api/v1/contribuyentes/12345678-5/consolidado?periodo=2026-05"

# 1 · El consolidado responde, y trae lo que pidió Carolina.
CUERPO="$(curl -s --max-time 20 -H "Authorization: Bearer $FISCALIZADOR" "$BASE$RUTA")"
COD="$(curl -s -o /dev/null -w '%{http_code}' --max-time 20 -H "Authorization: Bearer $FISCALIZADOR" "$BASE$RUTA")"
if [ "$COD" != "200" ]; then
    printf '[ERROR] El consolidado no responde 200 (fue %s)\n' "$COD"
    printf '        %s\n' "$CUERPO"
    exit 1
fi
printf '          GET %s -> HTTP %s\n' "$RUTA" "$COD"
printf '          %s\n' "$(printf '%s' "$CUERPO" | cut -c1-160)"

FALTA=""
case "$CUERPO" in *tramites*) ;; *) FALTA="$FALTA tramites" ;; esac
case "$CUERPO" in *estado*)   ;; *) FALTA="$FALTA estado" ;; esac
case "$CUERPO" in *otal*)     ;; *) FALTA="$FALTA total" ;; esac
if [ -n "$FALTA" ]; then
    printf '[ERROR] El consolidado no trae lo que pide el brief. Falta:%s\n' "$FALTA"
    exit 1
fi

# 2 · Es de FISCALIZADORES. Un contribuyente autenticado no pasa.
if [ -n "$CONTRIBUYENTE" ]; then
    COD_CONTRIB="$(curl -s -o /dev/null -w '%{http_code}' --max-time 20 \
        -H "Authorization: Bearer $CONTRIBUYENTE" "$BASE$RUTA")"
    printf '          mismo GET con rol CONTRIBUYENTE -> HTTP %s\n' "$COD_CONTRIB"
    if [ "$COD_CONTRIB" != "403" ]; then
        printf '[ERROR] El brief dice "para los fiscalizadores": se esperaba 403, fue %s\n' "$COD_CONTRIB"
        exit 1
    fi
fi

# 3 · Sin credencial, cerrado por defecto.
COD_ANON="$(curl -s -o /dev/null -w '%{http_code}' --max-time 20 "$BASE$RUTA")"
printf '          mismo GET sin credencial -> HTTP %s\n' "$COD_ANON"
if [ "$COD_ANON" != "401" ]; then
    printf '[ERROR] Sin credencial se esperaba 401, fue %s\n' "$COD_ANON"
    exit 1
fi

exit 0
