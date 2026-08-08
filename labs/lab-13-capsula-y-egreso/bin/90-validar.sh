#!/usr/bin/env bash
# =============================================================================
#  90-validar.sh — EL BOLETÍN DE EGRESO  (Lab 13)
# -----------------------------------------------------------------------------
#    ./bin/90-validar.sh                            # evalúa starter/ (tu entrega)
#    ./bin/90-validar.sh --dir solucion-referencia  # la solución de referencia
#    ./bin/90-validar.sh --sin-imagen               # omite el empaquetado OCI
#
#  ESTE VALIDADOR NO ES COMO LOS DOCE ANTERIORES.
#
#  No cuenta TODOs, porque no hay TODOs. Emite un BOLETÍN de tres ejes, y cada
#  eje declara QUIÉN lo mide. Esa declaración no es cortesía: un boletín que
#  afirma medir criterios que ningún mecanismo verifica es exactamente el
#  "pipeline deshonesto" que este mismo examen castiga (anti-herencia A-02).
#
#     · CORRECTITUD  automático     — ¿funciona, y funciona de verdad?
#     · OFICIO       semi-automático — ¿está bien hecho por dentro?
#     · CRITERIO     HUMANO          — ¿sabe por qué lo hizo así?
#
#  Y por eso este script NO PUEDE APROBAR A NADIE. Lo más que puede decir es
#  "el núcleo está verde"; el veredicto necesita la defensa oral, con la
#  rúbrica de rubrica/rubrica-evaluacion.md en la mano.
#
#  Los niveles finos (Competente / Destacado) tampoco los pone una máquina:
#  distinguirlos exige mirar QUÉ probó el alumno y por qué, no solo que pase.
#  El script separa lo que sabe de lo que no, y lo dice.
#
#  De SOLO LECTURA y sin `set -e`: acumula todo y lo reporta junto.
# =============================================================================
set -uo pipefail

DIR_BIN="$(cd "$(dirname "$0")" && pwd)"
# shellcheck source-path=SCRIPTDIR
# shellcheck source=../../lib/lib-comunes.sh
. "$DIR_BIN/../../lib/lib-comunes.sh"

DIR_LAB="$(cd "$DIR_BIN/.." && pwd)"
OBJETIVO="starter"
CON_IMAGEN=1

while [ $# -gt 0 ]; do
    case "$1" in
        --dir)        OBJETIVO="${2:-}"; shift 2 ;;
        --dir=*)      OBJETIVO="${1#*=}"; shift ;;
        --sin-imagen) CON_IMAGEN=0; shift ;;
        -h|--help) printf 'Uso: %s [--dir starter|solucion-referencia] [--sin-imagen]\n' "$(basename "$0")"; exit 0 ;;
        *) printf '[ERROR] Argumento no reconocido: %s\n' "$1" >&2; exit 2 ;;
    esac
done

case "$OBJETIVO" in
    /*) PROYECTO="$OBJETIVO" ;;
    *)  PROYECTO="$DIR_LAB/$OBJETIVO" ;;
esac

[ -d "$PROYECTO" ]         || { printf '[ERROR] No existe: %s\n' "$PROYECTO" >&2; exit 2; }
[ -f "$PROYECTO/pom.xml" ] || { printf '[ERROR] No es un proyecto Maven: %s\n' "$PROYECTO" >&2; exit 2; }

# Contadores por eje. Se llevan aparte de los de lib-comunes porque el boletín
# no da un "X/Y": da un nivel por eje.
FALLOS_CORRECTITUD=0
FALLOS_OFICIO=0
CORRECTITUD_INCOMPLETA=0

eje_correctitud_falla() { FALLOS_CORRECTITUD=$((FALLOS_CORRECTITUD + 1)); paso_fail "$1" "${2:-}"; }
eje_oficio_falla()      { FALLOS_OFICIO=$((FALLOS_OFICIO + 1)); paso_fail "$1" "${2:-}"; }

printf '\n'
printf '===========================================================\n'
printf '  BOLETÍN DE EGRESO · %s\n' "$OBJETIVO"
printf '===========================================================\n\n'

if ! docker info >/dev/null 2>&1; then
    paso_fail "El demonio de Docker no responde" \
              "Este examen necesita Docker: la suite usa contenedores y la entrega se empaqueta como imagen OCI."
    printf '\n[ERROR] No puedo emitir el boletín sin Docker.\n\n'
    exit 1
fi

# =============================================================================
#  EJE 1 · CORRECTITUD          (automático)
# =============================================================================
printf -- '--- EJE 1 · CORRECTITUD  [automático] -------------------\n\n'

SALIDA="$(mktemp)"
IMG_LOG="$(mktemp)"
trap 'rm -f "$SALIDA" "$IMG_LOG"' EXIT

log_info "Compilando y corriendo la suite completa…"
( cd "$PROYECTO" && ./mvnw -B -q verify > "$SALIDA" 2>&1 )
VERIFY_EXIT=$?

if [ "$VERIFY_EXIT" -eq 0 ] || grep -q 'BUILD SUCCESS' "$SALIDA" 2>/dev/null; then
    paso_ok "Compila y la suite completa pasa"
else
    if grep -qE 'COMPILATION ERROR' "$SALIDA"; then
        eje_correctitud_falla "El proyecto NO COMPILA" "cd $OBJETIVO && ./mvnw test-compile"
    else
        eje_correctitud_falla "Hay pruebas en rojo" "Las que fallan:"
        grep -E '^\[ERROR\]   [A-Za-z0-9_]+\.[a-zA-Z]' "$SALIDA" | sed 's/^\[ERROR\]   /          · /' | head -10
    fi
fi

# --- El pipeline deshonesto -------------------------------------------------
# Un `verify` verde no vale nada si el verde se compró apagando pruebas. Esto no
# es desconfianza: es la misma regla que el curso enseña desde el Lab 03.
if grep -rql '@Disabled' "$PROYECTO/src/test" 2>/dev/null; then
    eje_correctitud_falla "Hay tests DESACTIVADOS (@Disabled)" \
        "Un verde comprado apagando pruebas es un pipeline deshonesto. Bórralo o arréglalo: $(grep -rl '@Disabled' "$PROYECTO/src/test" 2>/dev/null | head -3 | tr '\n' ' ')"
else
    paso_ok "Ningún test está desactivado"
fi

# `catch` que traga: heurística DECLARADA. Caza la forma canónica del antipatrón
# del Lab 09 (`catch (...) {}` sin cuerpo). No pretende ser exhaustiva —detectar
# "un catch que no hace nada útil" es indecidible— y por eso el boletín la
# declara como heurística en vez de venderla como garantía.
if grep -rEl 'catch[[:space:]]*\([^)]*\)[[:space:]]*\{[[:space:]]*\}' "$PROYECTO/src" 2>/dev/null | grep -q .; then
    eje_correctitud_falla "Hay al menos un catch VACÍO (heurística)" \
        "El catch que traga convierte un error en un silencio (Lab 09). Registra y re-lanza."
else
    paso_ok "Sin catch vacíos (heurística: no detecta todas las formas)"
fi

# --- La aceptación: el examinador prueba el endpoint, no los tests del alumno --
if [ "$CON_IMAGEN" -eq 1 ]; then
    log_info "Empaquetando como imagen OCI (Buildpacks). La primera vez tarda varios minutos…"
    IMAGEN="dgt-egreso-$(basename "$OBJETIVO"):boletin"
    if ( cd "$PROYECTO" && ./mvnw -B -q spring-boot:build-image \
            -Dspring-boot.build-image.imageName="$IMAGEN" > "$IMG_LOG" 2>&1 ); then
        paso_ok "La imagen OCI se construye: $IMAGEN"
        if "$DIR_BIN/lib-aceptacion.sh" "$PROYECTO" "$IMAGEN"; then
            paso_ok "La app arranca desde el contenedor y el consolidado responde lo pedido"
        else
            eje_correctitud_falla "La app empaquetada no supera la aceptación" \
                "Detalle arriba. El brief está en brief/requerimientos-dgt.md"
        fi
    else
        eje_correctitud_falla "La imagen OCI NO se construye" \
            "Mira el error:  cd $OBJETIVO && ./mvnw spring-boot:build-image"
        tail -5 "$IMG_LOG" | sed 's/^/          /'
    fi
else
    # --sin-imagen no es un atajo inocente: se salta el empaquetado Y LA ACEPTACIÓN, que es
    # LA ÚNICA comprobación de que el consolidado del brief existe. Sin ella, una entrega que
    # no implementó nada saldría con el núcleo verde — el eje mediría "la suite del Lab 12
    # sigue pasando", que no es lo que se pidió.
    #
    # Por eso el eje queda INCOMPLETO y el boletín NO puede decir "núcleo verde". Sirve para
    # iterar rápido durante el examen; no sirve para evaluar.
    CORRECTITUD_INCOMPLETA=1
    paso_skip "Empaquetado OCI y aceptación omitidos (--sin-imagen)" \
              "El eje CORRECTITUD queda INCOMPLETO: sin la aceptación nadie comprobó el consolidado."
fi

# =============================================================================
#  EJE 2 · OFICIO               (semi-automático)
# =============================================================================
printf '\n'
printf -- '--- EJE 2 · OFICIO  [semi-automático] -------------------\n\n'

# Los 7 guardianes: no basta el verde, hay que ver que CORRIERON (P-05).
GUARDIANES_OK=1
for suite in ArquitecturaTest MordidaDeLosGuardianesTest; do
    informe="$PROYECTO/target/surefire-reports/cl.dgt.tramites.arquitectura.$suite.txt"
    if [ ! -f "$informe" ]; then
        eje_oficio_falla "No se ejecutó $suite" "Un verify que no corre a los guardianes es un gate decorativo."
        GUARDIANES_OK=0
    elif ! grep -q 'Failures: 0, Errors: 0' "$informe"; then
        eje_oficio_falla "$suite no está limpio" "Una regla de arquitectura está rota: su mensaje nombra el crimen."
        GUARDIANES_OK=0
    fi
done
[ "$GUARDIANES_OK" -eq 1 ] && paso_ok "Los 7 guardianes vigilan y los 7 muerden"

# Credenciales en archivos versionados. El crimen del Lab 01, que no vuelve.
SOSPECHOSAS="$(git -C "$DIR_LAB" ls-files "$(basename "$PROYECTO")" 2>/dev/null \
    | grep -E '\.(yml|yaml|properties|env)$' \
    | grep -v 'compose.yaml' || true)"
FILTRADAS=0
if [ -n "$SOSPECHOSAS" ]; then
    while IFS= read -r archivo; do
        [ -n "$archivo" ] || continue
        ruta="$DIR_LAB/$archivo"
        [ -f "$ruta" ] || continue
        # Una contraseña literal es `password: algo`. Un placeholder `${VAR}` no lo es:
        # esa es justamente la forma correcta que enseña el Lab 01.
        if grep -nE '^[[:space:]]*(password|secret)[[:space:]]*:[[:space:]]*[^$[:space:]]' "$ruta" >/dev/null 2>&1; then
            eje_oficio_falla "Credencial literal en un archivo versionado: $archivo" \
                             "Fuera del repo, por variable de entorno (Lab 01, D-012)."
            FILTRADAS=1
        fi
    done <<EOF
$SOSPECHOSAS
EOF
fi
[ "$FILTRADAS" -eq 0 ] && paso_ok "Sin credenciales literales en archivos versionados"

# Migraciones: presentes, numeradas y sin huecos.
MIGRACIONES="$PROYECTO/src/main/resources/db/migration"
if [ -d "$MIGRACIONES" ]; then
    CUANTAS="$(find "$MIGRACIONES" -name 'V*__*.sql' | wc -l | tr -d ' ')"
    MAYOR="$(find "$MIGRACIONES" -name 'V*__*.sql' -exec basename {} \; | sed 's/^V\([0-9]*\)__.*/\1/' | sort -n | tail -1)"
    if [ "${CUANTAS:-0}" -gt 0 ] && [ "${CUANTAS:-0}" -eq "${MAYOR:-0}" ]; then
        paso_ok "Migraciones versionadas y sin huecos ($CUANTAS)"
    else
        eje_oficio_falla "Las migraciones tienen huecos o numeración inconsistente ($CUANTAS archivos, mayor V$MAYOR)" \
                         "El esquema se reconstruye aplicándolas en orden: un hueco rompe esa promesa."
    fi
else
    eje_oficio_falla "No hay migraciones" "El esquema se versiona, no se improvisa."
fi

log_info "La detección de FLAKY vive en el 91: ./bin/91-e2e.sh corre la suite 3 veces."

# =============================================================================
#  EJE 3 · CRITERIO             (HUMANO — este script no lo mide)
# =============================================================================
printf '\n'
printf -- '--- EJE 3 · CRITERIO  [HUMANO] --------------------------\n\n'
printf '  Este script NO evalúa este eje, y no va a fingir que lo hace.\n\n'
printf '  Se evalúa con la defensa oral y el reporte de egreso:\n'
printf '     · plantillas/reporte-egreso.md   (lo que entrega el alumno)\n'
printf '     · rubrica/guia-instructor.md     (las preguntas y su calibración)\n'
printf '     · rubrica/rubrica-evaluacion.md  (los cuatro niveles por eje)\n\n'

# =============================================================================
#  EL BOLETÍN
# =============================================================================
printf '\n'
printf '===========================================================\n'
printf '  BOLETÍN\n'
printf -- '-----------------------------------------------------------\n'

if [ "$FALLOS_CORRECTITUD" -eq 0 ] && [ "$CORRECTITUD_INCOMPLETA" -eq 1 ]; then
    printf '  CORRECTITUD  ·  INCOMPLETO         [automático]\n'
    printf '                  Se omitió la aceptación (--sin-imagen): nadie\n'
    printf '                  comprobó que el consolidado exista. No evaluable.\n'
elif [ "$FALLOS_CORRECTITUD" -eq 0 ]; then
    printf '  CORRECTITUD  ·  SUFICIENTE o más   [automático]\n'
    printf '                  El salto a Competente/Destacado lo pone la\n'
    printf '                  rúbrica: mira QUÉ probó, no solo que pase.\n'
else
    printf '  CORRECTITUD  ·  INSUFICIENTE       [automático]  (%s problema(s))\n' "$FALLOS_CORRECTITUD"
fi

if [ "$FALLOS_OFICIO" -eq 0 ]; then
    printf '  OFICIO       ·  SUFICIENTE o más   [semi-automático]\n'
else
    printf '  OFICIO       ·  INSUFICIENTE       [semi-automático]  (%s problema(s))\n' "$FALLOS_OFICIO"
fi

printf '  CRITERIO     ·  NO MEDIDO AQUÍ     [humano]\n'
printf -- '-----------------------------------------------------------\n'

NUCLEO=$((FALLOS_CORRECTITUD + FALLOS_OFICIO))
if [ "$NUCLEO" -eq 0 ] && [ "$CORRECTITUD_INCOMPLETA" -eq 1 ]; then
    printf '  SIN VEREDICTO — la aceptación no se ejecutó.\n\n'
    printf '  Corre el boletín completo (sin --sin-imagen) antes de\n'
    printf '  evaluar a nadie. Un núcleo "verde" que no comprobó lo que\n'
    printf '  pedía el brief sería exactamente el pipeline deshonesto que\n'
    printf '  este examen califica Insuficiente.\n'
    VEREDICTO=1
elif [ "$NUCLEO" -eq 0 ]; then
    printf '  NÚCLEO VERDE.\n\n'
    printf '  Y eso NO es la aprobación. El umbral del curso es:\n'
    printf '     núcleo verde  Y  criterio >= Suficiente.\n\n'
    printf '  Un alumno con todo verde y sin criterio no aprueba: es\n'
    printf '  exactamente lo que este curso no quiere formar.\n'
    printf '  Falta la defensa. Llama al relator.\n'
    VEREDICTO=0
else
    printf '  NÚCLEO EN ROJO — %s problema(s) que resolver.\n\n' "$NUCLEO"
    printf '  Revisa los [ERROR] de arriba: cada uno trae su "->".\n'
    printf '  El núcleo se arregla; el criterio se defiende.\n'
    VEREDICTO=1
fi
printf '===========================================================\n\n'

exit "$VEREDICTO"
