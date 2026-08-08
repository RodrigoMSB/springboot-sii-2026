#!/usr/bin/env bash
# =============================================================================
#  lib-sistema.sh — lo que comparten los scripts del Lab 14
# -----------------------------------------------------------------------------
#  Los trece labs anteriores hablaban con UNA aplicación. Este habla con seis
#  procesos, un registro y un portal, así que necesita vocabulario propio:
#  preguntar al registro quién está anotado, esperar a que el sistema llegue a un
#  estado ESTABLE (que no es lo mismo que «arrancado»), y contar qué instancia
#  atendió cada respuesta.
#
#  Reglas de la casa, heredadas de labs/lib/lib-comunes.sh: bash 3.2, sin ANSI,
#  sin Python en el bin/ del alumno, contadores dinámicos, y jamás un `sleep`
#  ciego donde pueda haber una condición.
# =============================================================================

# El compose vive en sistema/, no en el bin/.
sistema_dir() {
    printf '%s' "$DIR_LAB/sistema"
}

# dc <args...> — `docker compose` sobre el compose de este laboratorio, con la
# configuración (starter o solución) que toque.
dc() {
    ( cd "$(sistema_dir)" && DGT_CONFIG_REPO="${DGT_CONFIG_REPO:-./config-repo}" \
        DGT_RETARDO_MS="${DGT_RETARDO_MS:-0}" docker compose "$@" )
}

# asegurar_config_montada — que el Config Server esté sirviendo el config-repo
# que se pidió con `--dir`, y no el de la vez anterior.
#
# El volumen se decide al CREAR el contenedor, no al arrancarlo: un
# `docker compose restart` conserva el montaje viejo. Así que hay que comparar lo
# montado con lo pedido y recrear solo si difieren. Recrear siempre costaría
# treinta segundos en cada escenario, por nada.
asegurar_config_montada() {
    _pedido="$(cd "$(sistema_dir)" && cd "${DGT_CONFIG_REPO:-./config-repo}" 2>/dev/null && pwd)"
    [ -n "$_pedido" ] || return 0
    _id="$(dc ps -q dgt-config 2>/dev/null | head -n 1)"
    [ -n "$_id" ] || return 0
    _montado="$(docker inspect --format '{{range .Mounts}}{{if eq .Destination "/config-repo"}}{{.Source}}{{end}}{{end}}' "$_id" 2>/dev/null)"
    [ "$_montado" = "$_pedido" ] && return 0
    log_info "El Config Server tenía montado otro config-repo: lo recreo."
    dc up -d --force-recreate dgt-config >/dev/null 2>&1
    _i=0
    while [ "$_i" -lt 60 ]; do
        curl -sf -o /dev/null --max-time 5 "http://localhost:8888/actuator/health" 2>/dev/null && break
        _i=$((_i + 1)); sleep 1
    done
}

# -----------------------------------------------------------------------------
#  El registro
# -----------------------------------------------------------------------------

# piezas_anotadas — cuántas INSTANCIAS hay anotadas y arriba en el registro.
#
# Se pregunta por la API XML de Eureka y se cuentan los `<status>UP</status>`.
# Nada de `grep -c` sobre una tubería con contador: aquí el resultado se imprime,
# no se acumula (la trampa del subshell, D3 de la SPEC-004).
piezas_anotadas() {
    curl -s --max-time 10 "http://localhost:${PUERTO_REGISTRO}/eureka/apps" 2>/dev/null \
        | tr '<' '\n' | grep -c '^status>UP$'
}

# instancias_anotadas — una línea por INSTANCIA anotada, con su identificador.
#
# Se lee `<instanceId>`, no `<name>`. Parece un detalle y no lo es: dentro de
# cada instancia hay un `<dataCenterInfo><name>MyOwn</name></dataCenterInfo>`,
# así que un `sed` sobre `name>` devuelve seis «MyOwn» de propina. Y como la
# gracia de este laboratorio es contar piezas, un contador que cuenta de más es
# peor que ninguno.
instancias_anotadas() {
    curl -s --max-time 10 "http://localhost:${PUERTO_REGISTRO}/eureka/apps" 2>/dev/null \
        | tr '<' '\n' | sed -n 's/^instanceId>\(.*\)/\1/p' | sort
}

# -----------------------------------------------------------------------------
#  El portal
# -----------------------------------------------------------------------------

# tramite_1 — el cuerpo crudo de GET /api/v1/tramites/1 a través del portal.
tramite_1() {
    curl -s --max-time "${1:-30}" "http://localhost:${PUERTO_PORTAL}/api/v1/tramites/1" 2>/dev/null
}

# respuesta_completa <cuerpo> — 0 si la respuesta trae el nombre del
# contribuyente (el sistema está entero), 1 si viene degradada o es un error.
#
# La comprobación es deliberadamente literal: se busca el nombre de la semilla.
# Un `grep nombreContribuyente` daría verde también con `"nombreContribuyente":null`,
# que es justo el caso que queremos distinguir. El crimen de este laboratorio es
# que la respuesta degradada PARECE correcta; un validador que caiga en la misma
# trampa no vale nada.
respuesta_completa() {
    case "${1:-}" in
        *'"nombreContribuyente":"Valentina Rojas"'*) return 0 ;;
        *) return 1 ;;
    esac
}

# atendido_por <cuerpo> — qué instancia firmó la respuesta, o cadena vacía.
atendido_por() {
    _v="$(printf '%s' "${1:-}" | tr ',' '\n' | sed -n 's/.*"atendidoPor":"\([^"]*\)".*/\1/p' | head -n 1)"
    # Con salto de línea: quien llame a esto en un bucle va a canalizarlo hacia
    # `sort | uniq -c`, y sin el salto sale todo pegado en una sola línea
    # gigante que dice «1». Se descubrió contando instancias en el bloque 3.
    printf '%s\n' "${_v:-SIN-RESPUESTA}"
}

# -----------------------------------------------------------------------------
#  Esperas — todas con condición y tope, ninguna con `sleep N` a secas
# -----------------------------------------------------------------------------

# esperar_piezas <cuantas> <segundos> — hasta que el registro vea N instancias UP.
esperar_piezas() {
    _meta="$1"; _limite="${2:-180}"; _i=0
    while [ "$_i" -lt "$_limite" ]; do
        [ "$(piezas_anotadas)" -ge "$_meta" ] && return 0
        _i=$((_i + 1)); sleep 1
    done
    return 1
}

# esperar_sistema_estable <segundos>
#
# «Arrancado» y «estable» NO son lo mismo, y este laboratorio es donde se aprende
# la diferencia. Un contenedor puede estar `healthy` mientras su balanceador
# todavía no tiene la lista de instancias; y si en esa ventana entran peticiones,
# fallan, y el circuit breaker las cuenta y ABRE — con todo el sistema sano.
#
# Por eso no basta con una respuesta buena: se exigen TRES SEGUIDAS. Es la forma
# barata de comprobar que el circuito está cerrado y la lista propagada.
esperar_sistema_estable() {
    _limite="${1:-180}"; _i=0; _seguidas=0
    while [ "$_i" -lt "$_limite" ]; do
        if respuesta_completa "$(tramite_1 30)"; then
            _seguidas=$((_seguidas + 1))
            [ "$_seguidas" -ge 3 ] && return 0
        else
            _seguidas=0
        fi
        _i=$((_i + 1)); sleep 1
    done
    return 1
}

# instancias_atendiendo <muestras> — cuántas instancias DISTINTAS contestaron.
#
# No es lo mismo que `piezas_anotadas`. El registro puede tener dos instancias
# apuntadas y el balanceador seguir mandando el 100 % del tráfico a una sola,
# porque entre las dos hay DOS cachés (la del cliente de Eureka y la del propio
# LoadBalancer). Lo único que demuestra que el balanceo está vivo es contar
# quién firma las respuestas.
instancias_atendiendo() {
    _n="${1:-6}"; _i=0
    while [ "$_i" -lt "$_n" ]; do
        atendido_por "$(tramite_1)"
        _i=$((_i + 1))
    done | sort -u | grep -vc '^SIN-RESPUESTA$'
}

# esperar_balanceo <cuantas> <segundos> — hasta que N instancias distintas
# aparezcan atendiendo de verdad.
esperar_balanceo() {
    _meta="$1"; _limite="${2:-120}"; _i=0
    while [ "$_i" -lt "$_limite" ]; do
        [ "$(instancias_atendiendo 6)" -ge "$_meta" ] && return 0
        _i=$((_i + 1)); sleep 1
    done
    return 1
}

# -----------------------------------------------------------------------------
#  El circuito
# -----------------------------------------------------------------------------

# estado_circuito — CLOSED | OPEN | HALF_OPEN, o «?» si no se puede preguntar.
#
# Se pregunta por el puerto de GESTIÓN del contenedor de trámites, que es fijo
# (8081); el de la API es efímero y no lo sabe nadie.
estado_circuito() {
    _j="$(dc exec -T dgt-tramites curl -s --max-time 5 \
            http://localhost:8081/actuator/circuitbreakers 2>/dev/null)"
    case "$_j" in
        *'"state":"OPEN"'*)      printf 'OPEN' ;;
        *'"state":"HALF_OPEN"'*) printf 'HALF_OPEN' ;;
        *'"state":"CLOSED"'*)    printf 'CLOSED' ;;
        *)                       printf '?' ;;
    esac
}

# milis_de <orden...> — cuánto tarda una orden, en milisegundos enteros.
#
# Sin `date +%s%N`: el `date` de macOS no lo soporta y devolvería la letra N.
# `curl -w %{time_total}` lo da el propio curl, en segundos con decimales, y la
# conversión a enteros se hace con awk (que está en las tres plataformas).
milis_url() {
    _t="$(curl -s -o /dev/null -w '%{time_total}' --max-time "${2:-40}" "$1" 2>/dev/null)"
    printf '%s' "$_t" | awk '{printf "%d", $1 * 1000}'
}
