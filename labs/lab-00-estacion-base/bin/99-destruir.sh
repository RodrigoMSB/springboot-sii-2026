#!/usr/bin/env bash
# =============================================================================
#  99-destruir.sh — deja tu máquina como estaba antes del laboratorio
# -----------------------------------------------------------------------------
#  Detiene la API, baja el PostgreSQL del laboratorio y borra su volumen.
#
#  IMPORTANTE: solo toca lo que ESTE laboratorio levantó. Usa el `compose.yaml`
#  del proyecto, así que si tienes otros contenedores corriendo (otro PostgreSQL,
#  el proyecto del trabajo, lo que sea), ni los mira. Un script de curso que
#  apaga contenedores ajenos es un script que nadie vuelve a correr.
# =============================================================================
set -uo pipefail

DIR_BIN="$(cd "$(dirname "$0")" && pwd)"
# shellcheck source-path=SCRIPTDIR
# shellcheck source=../../lib/lib-comunes.sh
. "$DIR_BIN/../../lib/lib-comunes.sh"

DIR_LAB="$(cd "$DIR_BIN/.." && pwd)"
RAIZ="$(raiz_repo)"
APP="$RAIZ/dgt-tramites-api"
ESTADO="$DIR_LAB/.estado"
PID="$ESTADO/dgt.pid"

# -----------------------------------------------------------------------------
#  El patrón que identifica A NUESTRA app y a ninguna otra.
# -----------------------------------------------------------------------------
#  Maven pone `-Dmaven.multiModuleProjectDirectory=<ruta>` en la línea de comandos
#  de todo proceso que lanza. Esa ruta es única de este proyecto.
#
#  ¿Por qué no `pkill -f "spring-boot:run"`, que es lo obvio? Porque cazaría
#  CUALQUIER app Spring Boot del alumno: su proyecto del trabajo, la demo de otro
#  curso, lo que sea. Lo comprobamos en carne propia durante la SPEC-006: mató un
#  proyecto ajeno que corría en la misma máquina. Un script de curso que apaga
#  procesos que no levantó es un script que nadie vuelve a correr.
PATRON_APP="multiModuleProjectDirectory=$APP"

printf '\n  Desmontando la Estación Base\n\n'

# -----------------------------------------------------------------------------
#  1 · La API
# -----------------------------------------------------------------------------
TENIAMOS_PID=0
if [ -f "$PID" ]; then
    TENIAMOS_PID=1
    NUM_PID="$(cat "$PID")"
    if kill -0 "$NUM_PID" 2>/dev/null; then
        # El `spring-boot:run` de Maven arranca la JVM de la app como proceso
        # hijo: hay que matar el árbol, no solo la raíz, o la JVM queda
        # escuchando el puerto y el siguiente start-lab.sh choca contra sí mismo.
        pkill -P "$NUM_PID" 2>/dev/null
        kill "$NUM_PID" 2>/dev/null
        esperar_muerte=0
        while kill -0 "$NUM_PID" 2>/dev/null && [ "$esperar_muerte" -lt 15 ]; do
            esperar_muerte=$((esperar_muerte + 1))
            sleep 1
        done
        # El [OK] depende de que el proceso se haya ido DE VERDAD, no de que se lo
        # hayamos pedido: si aguanta los 15 s, decirlo es mentir (SPEC-FIX-05).
        if kill -0 "$NUM_PID" 2>/dev/null; then
            paso_fail "La API (PID $NUM_PID) sigue viva tras 15 s" \
                      "Ciérrala a mano:  kill -9 $NUM_PID"
        else
            paso_ok "API detenida (PID $NUM_PID)"
        fi
    else
        paso_ok "La API ya no estaba corriendo"
    fi
    rm -f "$PID"
else
    paso_ok "La API no estaba corriendo"
fi

# Barrido final: el árbol de Maven deja nietos que `pkill -P` no alcanza.
# Acotado a NUESTRO proyecto por la ruta (ver PATRON_APP): jamás toca otra app.
#
# Si veníamos de un PID nuestro, esto es parte del desmontaje normal y NO es un
# aviso: un WARN que salta siempre enseña a ignorar los WARN. Solo avisamos si
# aparece un proceso del curso que nadie había registrado (arranque interrumpido).
if pgrep -f "$PATRON_APP" >/dev/null 2>&1; then
    pkill -f "$PATRON_APP" 2>/dev/null
    sleep 1
    if [ "$TENIAMOS_PID" -eq 1 ]; then
        log_info "Barrido de procesos hijos de Maven completado"
    else
        paso_warn "Había una DGT huérfana de un arranque anterior; la detuve"
    fi
fi

# -----------------------------------------------------------------------------
#  2 · La base de datos del laboratorio (y SOLO la del laboratorio)
# -----------------------------------------------------------------------------
if docker info >/dev/null 2>&1; then
    if ( cd "$APP" && docker compose down -v >/dev/null 2>&1 ); then
        paso_ok "PostgreSQL del laboratorio detenido y su volumen borrado"
    else
        paso_warn "No pude bajar el compose del laboratorio" \
                  "Compruébalo a mano:  cd dgt-tramites-api && docker compose ps"
    fi
else
    paso_skip "Docker no responde: no hay contenedores del lab que bajar"
fi

# -----------------------------------------------------------------------------
#  3 · Rastro local
# -----------------------------------------------------------------------------
if [ -d "$ESTADO" ]; then
    # Condicionado a que el borrado OCURRA, no a que el directorio existiera
    # (SPEC-FIX-05): borrar_seguro puede negarse, y entonces no hay nada que celebrar.
    if borrar_seguro "$ESTADO"; then
        paso_ok "Archivos temporales del lab borrados (.estado/)"
    else
        paso_fail "No pude borrar $ESTADO" \
                  "El cinturón de borrar_seguro se negó; mira el [ERROR] de arriba."
    fi
fi

# -----------------------------------------------------------------------------
#  4 · Testcontainers: los vemos, no los tocamos.
# -----------------------------------------------------------------------------
#  `./mvnw verify` levanta contenedores propios (y un vigilante, `ryuk`). Los
#  creó Maven, no este laboratorio, así que este script NO los apaga: hacerlo
#  sería volver al pecado de matar procesos ajenos. Pero callarlos tampoco vale:
#  el alumno vería basura y creería que "Todo quedó como estaba" le mintió.
if docker info >/dev/null 2>&1; then
    SOBRANTES="$(docker ps -q --filter label=org.testcontainers 2>/dev/null | wc -l | tr -d ' ')"
    if [ "${SOBRANTES:-0}" -gt 0 ]; then
        log_info "Veo $SOBRANTES contenedor(es) de Testcontainers, de un './mvnw verify'."
        log_info "No los toco: no los levantó este laboratorio. Cómo limpiarlos: ver T-11."
    fi
fi

resumen_final "Todo quedó como estaba" "Quedó algo a medio desmontar"
VEREDICTO=$?

printf '\n'
log_info "«Todo quedó como estaba» se refiere a lo que ESTE laboratorio creó."
log_info "Tus otros contenedores y procesos siguen intactos."
printf '\n'

exit "$VEREDICTO"
