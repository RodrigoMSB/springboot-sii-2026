#!/usr/bin/env bash
# =============================================================================
#  start-lab.sh — levanta la DGT partida en pedazos, y la rompe a petición
# -----------------------------------------------------------------------------
#    ./bin/start-lab.sh                        # las seis piezas, todo sano
#    ./bin/start-lab.sh --dir solucion         # con los umbrales de la solución
#    ./bin/start-lab.sh --matar-contribuyentes # ⭐ EL CRIMEN (bloque 2)
#    ./bin/start-lab.sh --contribuyentes-lento # el circuito abriéndose (bloque 2)
#    ./bin/start-lab.sh --escalar              # el balanceo (bloque 3)
#    ./bin/start-lab.sh --matar-registro       # la demo del relator (bloque 4)
#    ./bin/start-lab.sh --reiniciar-tramites   # tras editar dgt-tramites.yml
#
#  ---------------------------------------------------------------------------
#  POR QUÉ ESTE SCRIPT ESPERA TANTO, Y QUÉ ESPERA
#  ---------------------------------------------------------------------------
#  Levantar seis procesos no es levantar uno seis veces. Hay tres estados, y solo
#  el tercero sirve:
#
#    1. contenedor ARRANCADO   — el proceso existe
#    2. contenedor SANO        — su /actuator/health dice UP
#    3. sistema ESTABLE        — el registro tiene a todos Y los balanceadores
#                                de cada pieza ya se bajaron esa lista
#
#  Entre el 2 y el 3 hay unos segundos en los que el sistema está entero y aun
#  así falla: el balanceador de trámites todavía no sabe que contribuyentes
#  existe. Si en esa ventana entran peticiones, fallan de verdad, y el circuit
#  breaker —haciendo bien su trabajo— las cuenta y abre.
#
#  Por eso aquí no hay un solo `sleep N`: hay condiciones. Y la última condición
#  no es «responde», es «responde COMPLETO tres veces seguidas».
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

PUERTO_PORTAL="$DGT_PUERTO_DEFECTO"     # 8099, el del curso
PUERTO_REGISTRO=8761
OBJETIVO="starter"
ESCENARIO="normal"
RETARDO_LENTO=5000
INSTANCIAS=2

while [ $# -gt 0 ]; do
    case "$1" in
        --dir)                     OBJETIVO="${2:-}"; shift 2 ;;
        --dir=*)                   OBJETIVO="${1#*=}"; shift ;;
        --matar-contribuyentes)    ESCENARIO="matar-proveedor"; shift ;;
        --contribuyentes-lento)    ESCENARIO="proveedor-lento"; shift ;;
        --escalar)                 ESCENARIO="escalar"; shift ;;
        --matar-registro)          ESCENARIO="matar-registro"; shift ;;
        --reiniciar-tramites)      ESCENARIO="reiniciar-tramites"; shift ;;
        -h|--help)
            printf 'Uso: %s [--dir starter|solucion] [escenario]\n\n' "$(basename "$0")"
            printf '  --matar-contribuyentes   el crimen: una pieza menos, el portal sigue\n'
            printf '  --contribuyentes-lento   el proveedor tarda; mira abrirse el circuito\n'
            printf '  --escalar                dos instancias, el balanceo, y matar una\n'
            printf '  --matar-registro         apaga la guía telefónica (demo del relator)\n'
            printf '  --reiniciar-tramites     recarga tu dgt-tramites.yml sin bajar el sistema\n'
            exit 0 ;;
        *) printf '[ERROR] Argumento no reconocido: %s\n' "$1" >&2; exit 2 ;;
    esac
done

case "$OBJETIVO" in
    starter)  DGT_CONFIG_REPO='./config-repo' ;;
    solucion) DGT_CONFIG_REPO='./config-repo-solucion' ;;
    *) printf '[ERROR] --dir acepta starter o solucion, no "%s"\n' "$OBJETIVO" >&2; exit 2 ;;
esac
export DGT_CONFIG_REPO
export DGT_RETARDO_MS=0

[ -f "$(sistema_dir)/compose.yaml" ] || {
    printf '[ERROR] No encuentro %s/compose.yaml\n' "$(sistema_dir)" >&2; exit 2; }

if ! docker info >/dev/null 2>&1; then
    paso_fail "El demonio de Docker no responde" \
              "Abre Docker Desktop y espera a que arranque (T-03 del Lab 00)."
    printf '\n'; exit 1
fi

# -----------------------------------------------------------------------------
#  Escenarios que operan sobre un sistema YA levantado
# -----------------------------------------------------------------------------
sistema_arriba() {
    [ "$(dc ps --format '{{.Service}}' 2>/dev/null | grep -c 'dgt-')" -ge 4 ]
}

exigir_sistema_arriba() {
    sistema_arriba && return 0
    paso_fail "El sistema no está levantado" "Primero:  ./bin/start-lab.sh"
    printf '\n'; exit 1
}

# -----------------------------------------------------------------------------
#  Compilar y levantar
# -----------------------------------------------------------------------------
levantar() {
    printf '\n  Levantando la DGT partida en pedazos (%s)\n\n' "$OBJETIVO"

    if puerto_ocupado "$PUERTO_PORTAL"; then
        CULPABLE="$(quien_ocupa_puerto "$PUERTO_PORTAL")"
        paso_fail "El puerto $PUERTO_PORTAL ya está ocupado${CULPABLE:+ por: $CULPABLE}" \
                  "Bájalo, o usa ./bin/99-destruir.sh si es de un lab anterior (T-01 del Lab 00)."
        printf '\n'; exit 1
    fi

    log_info "Compilando los cinco proyectos… (la primera vez tarda; después va con caché)"
    if ! ( cd "$(sistema_dir)" && ./mvnw -B -q -DskipTests package ); then
        paso_fail "La compilación falló" "Mira la salida de arriba: el error está ahí, entero."
        printf '\n'; exit 1
    fi
    paso_ok "Los cinco jar están construidos"

    log_info "Construyendo las imágenes…"
    if ! dc build >/dev/null 2>&1; then
        paso_fail "No pude construir las imágenes" "Reintenta con:  cd sistema && docker compose build"
        printf '\n'; exit 1
    fi
    paso_ok "Cinco imágenes listas"

    log_info "Arrancando las seis piezas, en orden (postgres, registro, config, el resto)…"
    if ! dc up -d --scale "dgt-contribuyentes=$INSTANCIAS" >/dev/null 2>&1; then
        paso_fail "El arranque falló" "Mira los logs:  cd sistema && docker compose logs"
        printf '\n'; exit 1
    fi

    if esperar_piezas 6 240; then
        paso_ok "Las SEIS piezas están anotadas en el registro"
    else
        paso_fail "Solo $(piezas_anotadas) pieza(s) llegaron al registro en 240 s" \
                  "Mira quién falta:  cd sistema && docker compose ps -a"
        printf '\n'; exit 1
    fi

    log_info "Esperando a que el sistema esté ESTABLE (no solo arrancado)…"
    if esperar_sistema_estable 180; then
        paso_ok "El portal devuelve respuestas completas de forma sostenida"
    else
        paso_warn "El sistema responde, pero degradado" \
                  "Mira el circuito:  ./bin/90-validar.sh   ·   y docs/troubleshooting.md T-6"
    fi
}

mostrar_mapa() {
    printf '\n  Anotados en el registro (%s piezas):\n\n' "$(piezas_anotadas)"
    instancias_anotadas | sed 's/^/     · /'
    printf '\n'
    log_info "El panel del registro:  http://localhost:$PUERTO_REGISTRO"
    log_info "La única puerta:        http://localhost:$PUERTO_PORTAL/api/v1/tramites"
    log_info "Cuando termines:        ./bin/99-destruir.sh"
    printf '\n'
}

# =============================================================================
#  ESCENARIO · normal
# =============================================================================
if [ "$ESCENARIO" = "normal" ]; then
    levantar
    mostrar_mapa
    exit 0
fi

# =============================================================================
#  ESCENARIO · reiniciar solo el consumidor (tras editar dgt-tramites.yml)
# =============================================================================
if [ "$ESCENARIO" = "reiniciar-tramites" ]; then
    exigir_sistema_arriba
    printf '\n  Recargando tu configuración de dgt-tramites\n\n'
    log_info "El Config Server lee config-repo/ en cada petición, así que tu archivo"
    log_info "ya está servido. Lo que hay que reiniciar es quien lo consume."
    dc restart dgt-tramites >/dev/null 2>&1
    if esperar_sistema_estable 180; then
        paso_ok "dgt-tramites releyó la configuración y el sistema está estable"
    else
        paso_warn "dgt-tramites reinició, pero el sistema no llega a estable" \
                  "¿Sangría mal en el YAML? El servicio arranca igual e ignora lo que no entiende: T-7."
    fi
    printf '\n     estado del circuito ahora:  %s\n\n' "$(estado_circuito)"
    exit 0
fi

# =============================================================================
#  ESCENARIO · el crimen: se apaga el proveedor
# =============================================================================
if [ "$ESCENARIO" = "matar-proveedor" ]; then
    sistema_arriba || levantar
    asegurar_config_montada
    esperar_sistema_estable 180 >/dev/null 2>&1

    printf '\n  ----------  ANTES: el sistema entero  ----------\n\n'
    ANTES="$(tramite_1)"
    printf '     %s\n' "$ANTES"

    printf '\n  ----------  «Ahora apaga el servicio de contribuyentes»  ----------\n\n'
    dc stop dgt-contribuyentes >/dev/null 2>&1
    paso_ok "dgt-contribuyentes apagado (las dos instancias)"

    printf '\n  ----------  DESPUÉS  ----------\n\n'
    _i=0
    while [ "$_i" -lt 4 ]; do
        _ms="$(milis_url "http://localhost:$PUERTO_PORTAL/api/v1/tramites/1")"
        printf '     [%s ms]  %s\n' "$_ms" "$(tramite_1)"
        _i=$((_i + 1))
    done

    printf '\n'
    DESPUES="$(tramite_1)"
    if respuesta_completa "$DESPUES"; then
        paso_warn "El portal sigue devolviendo el nombre" \
                  "¿Se apagó de verdad?  cd sistema && docker compose ps -a"
    else
        paso_ok "El portal SIGUE RESPONDIENDO. HTTP 200. JSON bien formado."
        printf '\n'
        printf '     Y ahora la pregunta del laboratorio:\n\n'
        printf '       ¿qué hay en el campo "nombreContribuyente"?\n'
        printf '       ¿qué línea roja apareció en tu pantalla para avisarte?\n\n'
        printf '     Un monolito caído es una pantalla en blanco: lo ves.\n'
        printf '     Esto funciona a medias, y nadie sabe qué mitad.\n'
    fi
    printf '\n'
    printf '     estado del circuito:  %s\n' "$(estado_circuito)"
    printf '\n'
    log_info "Para revivirlo:  cd sistema && docker compose start dgt-contribuyentes"
    log_info "Sigue en:        guia/02-matar-al-proveedor.md"
    printf '\n'
    exit 0
fi

# =============================================================================
#  ESCENARIO · el proveedor lento: mira abrirse el circuito
# =============================================================================
if [ "$ESCENARIO" = "proveedor-lento" ]; then
    sistema_arriba || levantar
    asegurar_config_montada

    printf '\n  Poniendo LENTO al proveedor (%s ms por respuesta)\n\n' "$RETARDO_LENTO"
    log_info "Un servicio lento es peor que uno caído: el caído te contesta «no» en"
    log_info "un milisegundo; el lento se queda con tu hilo, y con el del que te llamó."
    printf '\n'

    DGT_RETARDO_MS="$RETARDO_LENTO" dc up -d --force-recreate \
        --scale "dgt-contribuyentes=$INSTANCIAS" dgt-contribuyentes >/dev/null 2>&1
    dc restart dgt-tramites >/dev/null 2>&1
    esperar_piezas 6 240 >/dev/null 2>&1

    # Se espera a que el portal enrute otra vez, sin exigir respuesta completa:
    # aquí lo normal es que venga degradada. Basta con que sea del servicio.
    _i=0
    while [ "$_i" -lt 120 ]; do
        case "$(tramite_1 40)" in *rutContribuyente*) break ;; esac
        _i=$((_i + 1)); sleep 1
    done

    printf '  ----------  peticiones, una a una  ----------\n\n'
    printf '     %-4s %-10s %-11s %s\n' '#' 'tiempo' 'circuito' 'nombre del contribuyente'
    _i=1
    while [ "$_i" -le 8 ]; do
        _ms="$(milis_url "http://localhost:$PUERTO_PORTAL/api/v1/tramites/1")"
        _cuerpo="$(tramite_1)"
        if respuesta_completa "$_cuerpo"; then _nom='(llegó)'; else _nom='AUSENTE'; fi
        printf '     #%-3s %6s ms  %-11s %s\n' "$_i" "$_ms" "$(estado_circuito)" "$_nom"
        _i=$((_i + 1))
    done

    printf '\n'
    if [ "$(estado_circuito)" = "OPEN" ]; then
        paso_ok "El circuito ABRIÓ, y mira los tiempos: dejó de esperar al que no contesta"
        printf '\n     Eso es «fallar rápido». No arregla nada — el nombre sigue sin llegar —\n'
        printf '     pero deja de gastar hilos, deja de castigar al vecino caído, y le da\n'
        printf '     tiempo a levantarse.\n'
    else
        paso_warn "El circuito sigue $(estado_circuito) después de 8 peticiones" \
                  "¿Declaraste los umbrales? Mira sistema/config-repo/dgt-tramites.yml"
        printf '\n     Con los valores por defecto hacen falta CIEN llamadas para que este\n'
        printf '     circuito llegue a opinar. En esta clase no hay cien llamadas.\n'
        printf '     Ese es el TODO del laboratorio.\n'
    fi
    printf '\n'
    log_info "Para devolverlo a la normalidad:  ./bin/95-recuperar.sh --solo-velocidad"
    printf '\n'
    exit 0
fi

# =============================================================================
#  ESCENARIO · escalar: el balanceo, y matar una instancia
# =============================================================================
if [ "$ESCENARIO" = "escalar" ]; then
    sistema_arriba || levantar
    asegurar_config_montada

    printf '\n  ----------  paso 1 · UNA sola instancia  ----------\n\n'
    dc up -d --scale dgt-contribuyentes=1 dgt-contribuyentes >/dev/null 2>&1
    esperar_sistema_estable 180 >/dev/null 2>&1
    _i=0
    while [ "$_i" -lt 10 ]; do atendido_por "$(tramite_1)"; _i=$((_i + 1)); done \
        | sort | uniq -c | sed 's/^/     /'

    printf '\n  ----------  paso 2 · se levanta la SEGUNDA  ----------\n\n'
    dc up -d --scale dgt-contribuyentes=2 dgt-contribuyentes >/dev/null 2>&1
    if esperar_piezas 6 240; then
        paso_ok "Seis piezas anotadas: la nueva instancia entró sola en el registro"
        printf '\n     Nadie tocó una configuración. Nadie reinició el portal. La instancia\n'
        printf '     arrancó, se anotó en la guía, y el balanceador se enteró solo.\n\n'
    fi
    esperar_sistema_estable 180 >/dev/null 2>&1
    if esperar_balanceo 2 120; then
        paso_ok "El balanceador ya reparte entre las DOS"
    else
        paso_warn "Pasados 120 s el balanceador sigue usando una sola instancia" \
                  "Mira docs/troubleshooting.md, T-9 (las dos cachés)."
    fi
    printf '\n     20 peticiones, quién atendió cada una:\n\n'
    _i=0
    while [ "$_i" -lt 20 ]; do atendido_por "$(tramite_1)"; _i=$((_i + 1)); done \
        | sort | uniq -c | sed 's/^/       /'

    printf '\n  ----------  paso 3 · se mata UNA de las dos  ----------\n\n'
    VICTIMA="$(dc ps -q dgt-contribuyentes | head -n 1)"
    docker stop "$VICTIMA" >/dev/null 2>&1
    paso_ok "Instancia detenida. Contando cuánto tarda el sistema en no notarlo."
    printf '\n'
    printf '     Ojo a lo que viene: NO es instantáneo, y ahí está la lección.\n\n'

    _i=0; _seguidas=0; _t=0
    while [ "$_i" -lt 60 ]; do
        if respuesta_completa "$(tramite_1)"; then
            _seguidas=$((_seguidas + 1))
            [ "$_seguidas" -eq 1 ] && _t="$_i"
        else
            _seguidas=0
        fi
        [ "$_seguidas" -ge 5 ] && break
        _i=$((_i + 1)); sleep 1
    done

    if [ "$_seguidas" -ge 5 ]; then
        paso_ok "El sistema se recuperó SOLO, a los ~${_i} s. Nadie tocó nada."
        printf '\n     Los segundos que acabas de ver son la guía telefónica mintiendo: el\n'
        printf '     registro seguía anunciando una instancia muerta, el balanceador le\n'
        printf '     seguía mandando tráfico, y el circuit breaker —al contar esos fallos—\n'
        printf '     acabó castigando también a la instancia SANA.\n\n'
        printf '     «El sistema no se entera» es una verdad a medias. Se entera, sufre un\n'
        printf '     rato, y se cura sin ayuda. Eso último es lo que un monolito no hace.\n'
    else
        paso_warn "El sistema no se recuperó en 60 s" "Mira docs/troubleshooting.md, T-8."
    fi
    printf '\n'
    log_info "Para dejarlo como estaba:  ./bin/95-recuperar.sh --solo-instancias"
    printf '\n'
    exit 0
fi

# =============================================================================
#  ESCENARIO · matar el registro (demo del relator, bloque 4)
# =============================================================================
if [ "$ESCENARIO" = "matar-registro" ]; then
    exigir_sistema_arriba
    esperar_sistema_estable 180 >/dev/null 2>&1

    printf '\n  ----------  se apaga LA GUÍA TELEFÓNICA  ----------\n\n'
    log_info "Intuición de la sala: sin registro, nadie encuentra a nadie y todo cae."
    log_info "Vamos a medirlo."
    printf '\n'
    dc stop dgt-registro >/dev/null 2>&1
    paso_ok "dgt-registro apagado"
    printf '\n'

    _i=0; _primer_fallo=-1
    while [ "$_i" -lt 90 ]; do
        if respuesta_completa "$(tramite_1)"; then
            [ $((_i % 10)) -eq 0 ] && printf '     t+%-3ss  sigue entero\n' "$_i"
        else
            _primer_fallo="$_i"
            printf '     t+%-3ss  DEGRADADO  <-- aquí se acabó el caché\n' "$_i"
            break
        fi
        _i=$((_i + 1)); sleep 1
    done

    printf '\n'
    if [ "$_primer_fallo" -lt 0 ]; then
        paso_ok "$_i segundos sin registro y el sistema NO se ha enterado"
    else
        paso_ok "Aguantó ${_primer_fallo} s sin registro antes de degradarse"
    fi
    printf '\n'
    printf '     La pieza más crítica del sistema no falla como esperabas.\n\n'
    printf '     Cada cliente se había bajado una COPIA de la guía. Mientras esa copia\n'
    printf '     siga siendo cierta, el registro puede estar muerto y a nadie le importa.\n'
    printf '\n'

    # -------------------------------------------------------------------------
    #  Segundo acto: y ahora, ¿qué SÍ se rompió?
    # -------------------------------------------------------------------------
    #  Sin esta parte, la demo enseña la mitad tranquilizadora y deja a la sala
    #  con la idea de que el registro es prescindible. No lo es: lo que se perdió
    #  no es la comunicación, es la capacidad de enterarse de los CAMBIOS. Y eso
    #  no se ve hasta que algo cambia.
    printf '  ----------  segundo acto: ahora matamos una instancia  ----------\n\n'
    log_info "Con el registro vivo, esto se curaba solo en unos segundos."
    log_info "Sin registro que la tache de la lista, nadie va a enterarse de que murió."
    printf '\n'

    VICTIMA="$(dc ps -q dgt-contribuyentes | head -n 1)"
    docker stop "$VICTIMA" >/dev/null 2>&1
    paso_ok "Una instancia de dgt-contribuyentes, muerta"
    printf '\n'

    _i=0; _fallos=0; _total=0
    while [ "$_i" -lt 30 ]; do
        respuesta_completa "$(tramite_1)" || _fallos=$((_fallos + 1))
        _total=$((_total + 1))
        [ $((_i % 10)) -eq 0 ] && printf '     t+%-3ss  degradadas: %s de %s\n' "$_i" "$_fallos" "$_total"
        _i=$((_i + 1)); sleep 1
    done

    printf '\n     30 segundos después: %s de %s peticiones degradadas.\n\n' "$_fallos" "$_total"
    if [ "$_fallos" -gt 0 ]; then
        paso_ok "Y NO se cura. Esta vez no hay recuperación automática."
        printf '\n     El balanceador sigue con la lista de antes, en la que esa instancia\n'
        printf '     está viva. Nadie va a corregirle esa lista, porque quien la corrige\n'
        printf '     está muerto. El sistema se quedó congelado en la última foto que\n'
        printf '     alguien le sacó.\n\n'
    else
        paso_warn "No se degradó" "Quizá la lista ya había caducado. Reinicia el escenario."
    fi
    printf '     Ahora sí está dicha la lección entera:\n\n'
    printf '       · El registro NO es un punto único de fallo para el TRÁFICO.\n'
    printf '       · SÍ lo es para el CAMBIO: nadie nuevo entra, nadie muerto sale.\n\n'
    printf '     Un caché convierte una caída instantánea en una degradación lenta.\n'
    printf '     Eso compra tiempo, que en una guardia es lo único que se necesita.\n'
    printf '     Pero el reloj corre igual, y hay que levantar el registro.\n'
    printf '\n'
    log_info "Para dejarlo todo como estaba:"
    log_info "  cd sistema && docker compose start dgt-registro"
    log_info "  ./bin/95-recuperar.sh --solo-instancias"
    printf '\n'
    exit 0
fi
