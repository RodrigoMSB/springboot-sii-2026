#!/usr/bin/env bash
# =============================================================================
#  90-validar.sh — el veredicto del Lab 14
# -----------------------------------------------------------------------------
#    ./bin/90-validar.sh                  # juzga TU configuración
#    ./bin/90-validar.sh --dir solucion   # juzga la de referencia
#
#  Solo lectura, sin `set -e`: acumula todas las fallas y las dice juntas. Nadie
#  quiere arreglar de una en una.
#
#  ---------------------------------------------------------------------------
#  QUÉ SE JUZGA AQUÍ, Y QUÉ NO
#  ---------------------------------------------------------------------------
#  Este validador NO necesita el sistema levantado, y eso es deliberado: seis
#  contenedores tardan minutos y un veredicto no puede depender de que tu Docker
#  esté de buen humor. Lo que se mide aquí es lo que se puede medir sin red:
#
#    · los tests compilados de dgt-tramites, que leen TU archivo de
#      configuración y comprueban si el circuito abriría en el escenario del lab;
#    · la estructura del sistema (que las piezas y su configuración existan);
#    · que ninguna dependencia arrastre Hystrix, Ribbon ni Zuul.
#
#  Lo que se mide con el sistema arriba —los milisegundos cayendo, el balanceo
#  repartiendo, cuánto aguanta el sistema sin registro— lo enseña
#  `start-lab.sh` con sus banderas. Ahí el alumno lo VE; aquí se CERTIFICA.
#  Son dos cosas distintas y confundirlas hace un validador lento y frágil.
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
OBJETIVO="starter"

while [ $# -gt 0 ]; do
    case "$1" in
        --dir)   OBJETIVO="${2:-}"; shift 2 ;;
        --dir=*) OBJETIVO="${1#*=}"; shift ;;
        -h|--help) printf 'Uso: %s [--dir starter|solucion]\n' "$(basename "$0")"; exit 0 ;;
        *) printf '[ERROR] Argumento no reconocido: %s\n' "$1" >&2; exit 2 ;;
    esac
done

case "$OBJETIVO" in
    starter)  REPO_CONFIG="config-repo" ;;
    solucion) REPO_CONFIG="config-repo-solucion" ;;
    *) printf '[ERROR] --dir acepta starter o solucion, no "%s"\n' "$OBJETIVO" >&2; exit 2 ;;
esac

SIS="$(sistema_dir)"

printf '\n  Validando el Lab 14 (%s)\n\n' "$OBJETIVO"

# -----------------------------------------------------------------------------
#  1 · La estructura: las cinco piezas y su configuración
# -----------------------------------------------------------------------------
printf '  --- el sistema está completo ---\n\n'

for M in dgt-registro dgt-config dgt-portal dgt-contribuyentes dgt-tramites; do
    if [ -f "$SIS/$M/pom.xml" ]; then
        paso_ok "$M presente"
    else
        paso_fail "Falta el proyecto $M" "Recupéralo con:  ./bin/95-recuperar.sh"
    fi
done

for F in application.yml dgt-portal.yml dgt-contribuyentes.yml dgt-tramites.yml; do
    if [ -f "$SIS/$REPO_CONFIG/$F" ]; then
        paso_ok "$REPO_CONFIG/$F presente"
    else
        paso_fail "Falta $REPO_CONFIG/$F" "El Config Server no tendría qué servir. ./bin/95-recuperar.sh"
    fi
done

# -----------------------------------------------------------------------------
#  2 · El modelo antiguo NO se ha colado
# -----------------------------------------------------------------------------
printf '\n  --- nada de material caducado ---\n\n'

# `bootstrap.yml` es la marca inconfundible de un tutorial de hace cinco años.
# Se busca por NOMBRE DE ARCHIVO, no inspeccionando código con regex (A-01).
SOBRA="$(find "$SIS" -name 'bootstrap.yml' -o -name 'bootstrap.properties' 2>/dev/null | head -n 3)"
if [ -z "$SOBRA" ]; then
    paso_ok "Sin bootstrap.yml: los clientes usan spring.config.import (el modelo de hoy)"
else
    paso_fail "Hay un bootstrap.yml en el sistema" \
              "La fase bootstrap ya no está en el camino por defecto de Boot: usa spring.config.import."
fi

# -----------------------------------------------------------------------------
#  3 · Ninguna dependencia arrastra a los muertos
# -----------------------------------------------------------------------------
#  De todo el stack Netflix histórico solo Eureka sigue vivo. Que Hystrix, Ribbon
#  o Zuul aparezcan en el árbol significa que alguien copió un tutorial viejo, y
#  entonces la mitad de lo que enseña este laboratorio deja de ser cierto.
if command -v java >/dev/null 2>&1 && [ -f "$SIS/mvnw" ]; then
    ARBOL="$SIS/.arbol-dependencias.txt"
    if ( cd "$SIS" && ./mvnw -B -q dependency:tree -DoutputFile="$ARBOL" -DappendOutput=true >/dev/null 2>&1 ); then
        ZOMBIS="$(grep -icE 'hystrix|:ribbon|zuul|archaius' "$ARBOL" 2>/dev/null)"
        if [ "${ZOMBIS:-0}" -eq 0 ]; then
            paso_ok "Sin Hystrix, Ribbon, Zuul ni Archaius en el árbol de dependencias"
        else
            paso_fail "El árbol arrastra $ZOMBIS artefacto(s) muerto(s) del stack Netflix" \
                      "Mira $ARBOL. Sustitutos vivos: Resilience4j, Spring Cloud LoadBalancer, Spring Cloud Gateway."
        fi
        rm -f "$ARBOL"
    else
        paso_skip "No pude calcular el árbol de dependencias" "Sin red, Maven no puede resolverlo. No baja el veredicto."
    fi
else
    paso_skip "Sin Java o sin el wrapper: no se revisa el árbol de dependencias"
fi

# -----------------------------------------------------------------------------
#  4 · EL CRITERIO DE ACEPTACIÓN: los tests
# -----------------------------------------------------------------------------
printf '\n  --- el criterio de aceptación ---\n\n'

# La integridad del enunciado, ANTES de correr nada. El criterio de este lab es
# que el circuito abra; borrar esa aserción es más fácil que entender la ventana
# deslizante, y un enunciado editable deja de ser un enunciado para convertirse en
# un espejo. Protege SOLO src/test/java/**/enunciado/**: lo que el alumno escriba
# por su cuenta en otro paquete es territorio libre.
MANIFIESTO="manifiesto-tests.sha256"
if [ ! -f "$SIS/$MANIFIESTO" ]; then
    paso_skip "Sin manifiesto de tests: no se comprueba la integridad del enunciado"
elif ( cd "$SIS" && "$DIR_BIN/../../lib/verificar-manifiesto.sh" "$MANIFIESTO" >/dev/null 2>&1 ); then
    paso_ok "Los tests del enunciado están intactos"
else
    paso_fail "Los tests del enunciado fueron modificados o falta alguno" \
              "Recupéralos con git, o pídeselos al instructor. Ver el detalle:"
    ( cd "$SIS" && "$DIR_BIN/../../lib/verificar-manifiesto.sh" "$MANIFIESTO" 2>&1 | sed 's/^/        /' )
fi

log_info "Corriendo los tests de dgt-tramites contra $REPO_CONFIG/…"
SALIDA_TESTS="$SIS/.tests-90.log"
if ( cd "$SIS" && ./mvnw -B test -pl dgt-tramites \
        "-Ddgt.config-repo=../$REPO_CONFIG" > "$SALIDA_TESTS" 2>&1 ); then
    RESUMEN="$(grep -E '^\[INFO\] Tests run: [0-9]+, Fail' "$SALIDA_TESTS" | tail -n 1)"
    paso_ok "Los tests pasan${RESUMEN:+  ·  $RESUMEN}"
    rm -f "$SALIDA_TESTS"
else
    RESUMEN="$(grep -E 'Tests run: [0-9]+, Fail' "$SALIDA_TESTS" | tail -n 1)"
    paso_fail "Los tests NO pasan${RESUMEN:+  ·  $RESUMEN}" \
              "El mensaje de cada fallo dice exactamente qué falta. Léelo:  $SALIDA_TESTS"
    printf '\n'
    # Se muestra el motivo aquí mismo: obligar a abrir otro archivo para saber
    # qué pasó es una crueldad innecesaria.
    grep -A 14 'AssertionFailedError' "$SALIDA_TESTS" 2>/dev/null | head -n 22 | sed 's/^/        /'
    printf '\n'
fi

# -----------------------------------------------------------------------------
#  5 · Si además el sistema está levantado, se mira — pero no se exige
# -----------------------------------------------------------------------------
printf '\n  --- el sistema en vivo (opcional) ---\n\n'

if ! docker info >/dev/null 2>&1; then
    paso_skip "Docker no responde: no se mira el sistema en vivo" \
              "Lo obligatorio ya se juzgó arriba, sin contenedores."
elif [ "$(piezas_anotadas)" -eq 0 ] 2>/dev/null; then
    paso_skip "El sistema no está levantado: no se mira en vivo" \
              "Para verlo:  ./bin/start-lab.sh"
else
    ANOTADAS="$(piezas_anotadas)"
    if [ "$ANOTADAS" -ge 6 ]; then
        paso_ok "El registro ve $ANOTADAS piezas anotadas"
    else
        paso_warn "El registro solo ve $ANOTADAS pieza(s) de 6" \
                  "¿Mataste alguna a propósito? Si no:  cd sistema && docker compose ps -a"
    fi

    CUERPO="$(tramite_1)"
    case "$CUERPO" in
        *rutContribuyente*) paso_ok "El portal enruta por nombre lógico y responde" ;;
        *) paso_warn "El portal no devolvió un trámite" "Mira: docs/troubleshooting.md, T-5 y T-6" ;;
    esac

    if respuesta_completa "$CUERPO"; then
        paso_ok "La respuesta llega COMPLETA: el sistema está entero"
    else
        paso_warn "La respuesta llega DEGRADADA (sin nombre de contribuyente)" \
                  "Normal si acabas de matar al proveedor. Si no:  estado del circuito -> $(estado_circuito)"
    fi
fi

# -----------------------------------------------------------------------------
printf '\n'
resumen_final "EL LAB 14 ESTÁ APROBADO" "TODAVÍA NO"
VEREDICTO=$?
printf '\n'
if [ "$VEREDICTO" -eq 0 ]; then
    log_info "Ahora la parte que ningún script puede juzgar: tu reporte."
    log_info "Está en plantillas/reporte-entregable.md"
fi
printf '\n'
exit "$VEREDICTO"
