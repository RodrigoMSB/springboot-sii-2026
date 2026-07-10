#!/usr/bin/env bash
# =============================================================================
#  lib-comunes.sh — la caja de herramientas de los laboratorios de la DGT
# -----------------------------------------------------------------------------
#  Se escribe UNA vez, bien, y los 12 labs la consumen. Reglas de la casa:
#
#   · bash 3.2 compatible. El macOS de fábrica trae bash 3.2.57 y el alumno de
#     Windows usa Git Bash. Nada de `mapfile`, arrays asociativos ni `${var,,}`.
#   · Sin ANSI. Los prefijos son [OK] [ERROR] [SKIP] [WARN] [INFO]: se leen igual
#     en una terminal, en un log de CI y pegados en un correo al instructor.
#   · Contadores DINÁMICOS. El "7/7" se calcula; jamás se escribe a mano.
#   · Sin Python en el bin/ del alumno (portabilidad).
#
#  Trampa que este archivo evita a propósito (D3 de la SPEC-004): un
#  `... | while read` corre en subshell y se traga los contadores; el script
#  saldría verde siempre. Aquí nunca se acumula tras una tubería.
#
#  Los scripts `90-validar.sh` corren en modo solo-lectura y SIN `set -e`: deben
#  acumular todas las fallas y decirlas juntas. Por eso `paso_fail` no aborta.
#
#  Uso:
#     . "$(dirname "$0")/../../lib/lib-comunes.sh"
#     paso_ok "Java 25 presente"
#     paso_fail "Falta Docker" "Instálalo desde https://docker.com"
#     resumen_final "ESTACIÓN LISTA"
# =============================================================================

# Contadores. Globales a propósito: son el estado del validador.
DGT_OK=0
DGT_FALLOS=0
DGT_SKIP=0
DGT_WARN=0

# -----------------------------------------------------------------------------
#  Mensajería
# -----------------------------------------------------------------------------

# Informativo: no cuenta para el veredicto.
log_info() {
    printf '[INFO]  %s\n' "$1"
}

# Una verificación pasó.
paso_ok() {
    DGT_OK=$((DGT_OK + 1))
    printf '[OK]    %s\n' "$1"
}

# Una verificación falló. $2 (opcional) es la ACCIÓN: qué hacer al respecto.
# No aborta: el validador acumula y reporta todo junto.
paso_fail() {
    DGT_FALLOS=$((DGT_FALLOS + 1))
    printf '[ERROR] %s\n' "$1"
    if [ -n "${2:-}" ]; then
        printf '        -> %s\n' "$2"
    fi
}

# Una verificación no aplica en este escenario. No baja el veredicto (P-15).
paso_skip() {
    DGT_SKIP=$((DGT_SKIP + 1))
    printf '[SKIP]  %s\n' "$1"
    if [ -n "${2:-}" ]; then
        printf '        -> %s\n' "$2"
    fi
}

# Algo digno de mirar, que no rompe nada.
paso_warn() {
    DGT_WARN=$((DGT_WARN + 1))
    printf '[WARN]  %s\n' "$1"
    if [ -n "${2:-}" ]; then
        printf '        -> %s\n' "$2"
    fi
}

# -----------------------------------------------------------------------------
#  Veredicto
# -----------------------------------------------------------------------------

# resumen_final <titulo-del-exito> [titulo-del-fracaso]
#
# Imprime "X/Y verificaciones" con Y CALCULADO (ok + fallos), no escrito a mano.
# Los SKIP no entran en el denominador: lo opcional nunca baja el veredicto.
# Fija el exit code: 0 solo si no hubo fallas.
resumen_final() {
    titulo_ok="${1:-TODO EN ORDEN}"
    titulo_mal="${2:-HAY TRABAJO POR HACER}"
    total=$((DGT_OK + DGT_FALLOS))

    printf '\n'
    printf -- '-----------------------------------------------------------\n'
    printf '  %s/%s verificaciones' "$DGT_OK" "$total"
    [ "$DGT_SKIP" -gt 0 ] && printf '  ·  %s omitidas' "$DGT_SKIP"
    [ "$DGT_WARN" -gt 0 ] && printf '  ·  %s avisos' "$DGT_WARN"
    printf '\n'

    if [ "$DGT_FALLOS" -eq 0 ]; then
        printf '  %s\n' "$titulo_ok"
        printf -- '-----------------------------------------------------------\n'
        return 0
    fi

    printf '  %s\n' "$titulo_mal"
    printf '  Revisa los [ERROR] de arriba: cada uno trae su "->" con qué hacer.\n'
    printf -- '-----------------------------------------------------------\n'
    return 1
}

# -----------------------------------------------------------------------------
#  Utilidades
# -----------------------------------------------------------------------------

# requiere_comando <cmd> <mensaje-humano-accionable>
# Existencia, no versión. Devuelve 0 si está, 1 si no.
requiere_comando() {
    cmd="$1"
    ayuda="${2:-Instálalo y vuelve a intentar.}"
    if command -v "$cmd" >/dev/null 2>&1; then
        paso_ok "$cmd está en el PATH"
        return 0
    fi
    paso_fail "No encuentro '$cmd' en el PATH" "$ayuda"
    return 1
}

# Devuelve: macos | linux | gitbash | desconocido
# Sirve para que los mensajes hablen del sistema que el alumno tiene delante.
detectar_plataforma() {
    case "$(uname -s)" in
        Darwin)               printf 'macos' ;;
        Linux)                printf 'linux' ;;
        MINGW*|MSYS*|CYGWIN*) printf 'gitbash' ;;
        *)                    printf 'desconocido' ;;
    esac
}

# esperar_url <url> <segundos> — bucle con timeout, jamás un `sleep` ciego.
# Un sleep fijo es una apuesta: pasa en tu máquina y falla en la del alumno.
esperar_url() {
    url="$1"
    limite="${2:-60}"
    intento=0
    while [ "$intento" -lt "$limite" ]; do
        if curl -sf -o /dev/null "$url" 2>/dev/null; then
            return 0
        fi
        intento=$((intento + 1))
        sleep 1
    done
    return 1
}

# puerto_ocupado <puerto> — 0 si algo escucha ahí.
puerto_ocupado() {
    puerto="$1"
    if command -v lsof >/dev/null 2>&1; then
        lsof -nP -iTCP:"$puerto" -sTCP:LISTEN >/dev/null 2>&1
        return $?
    fi
    # Sin lsof (Git Bash), preguntamos con curl: si algo responde, está ocupado.
    curl -sf -o /dev/null --max-time 1 "http://localhost:$puerto" 2>/dev/null
    return $?
}

# quien_ocupa_puerto <puerto> — nombre del proceso, o cadena vacía.
# El alumno merece un nombre y apellido, no un "puerto ocupado".
quien_ocupa_puerto() {
    puerto="$1"
    if command -v lsof >/dev/null 2>&1; then
        lsof -nP -iTCP:"$puerto" -sTCP:LISTEN 2>/dev/null | awk 'NR==2 {print $1" (PID "$2")"}'
    fi
}

# raiz_repo — ruta absoluta a la raíz del repositorio, desde donde sea.
raiz_repo() {
    if command -v git >/dev/null 2>&1 && git rev-parse --show-toplevel >/dev/null 2>&1; then
        git rev-parse --show-toplevel
        return 0
    fi
    # Sin git: subimos desde este archivo (labs/lib/ -> raíz).
    ( cd "$(dirname "${BASH_SOURCE[0]}")/../.." && pwd )
}
