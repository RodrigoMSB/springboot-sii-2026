#!/usr/bin/env bash
# =============================================================================
#  VUELO 5 · SPEC-025 — los labs 08-11 en modo avion, sin Docker y sin red
# -----------------------------------------------------------------------------
#  Autonomo y desatendido. Se lanza con la red viva y espera en pista; despega
#  solo cuando detecta que el cable se fue.
#
#  Cubre:  · N1-N5 de los labs 08, 09, 10 y 11 — los cuatro recien migrados
#          · los DOS demos en vivo, que son los que de verdad usaban Docker:
#              - Lab 10 `--db-caida`     (matar la base y ver si el tablero miente)
#              - Lab 11 `--instancias 2` (dos servidores, un candado)
#          · el Lab 06 como REGRESION: la maleta no se rompe por atras
#          · simulacro del alumno cronometrado, con todas las cachas frias
#
#  ---------------------------------------------------------------------------
#  LEE ESTO ANTES DE LANZARLO — la sonda de ARQUITECTURA
#  ---------------------------------------------------------------------------
#  Durante la SPEC-025 se perdieron horas persiguiendo un fantasma: las suites
#  morian con "class file version 69.0 ... only recognizes up to 65.0" y parecia
#  que los JVM bifurcados ignoraban el JDK embebido. No era eso. El arnes de
#  medicion envolvia `./mvnw` en `timeout`, y el `timeout` de esta maquina es el
#  binario x86_64 del Homebrew de Intel (/usr/local/bin): corre bajo Rosetta y
#  sus hijos heredan la personalidad x86_64, asi que `uname -m` devuelve
#  x86_64 en vez de arm64. El shim entonces cree estar en un Mac Intel, no
#  detecta plataforma y se cae al Java del sistema — que es su comportamiento
#  DISENADO (SPEC-024 §7.3).
#
#  Por eso este vuelo hace dos cosas: (1) NO envuelve `./mvnw` en `timeout`, y
#  (2) comprueba antes de despegar que la arquitectura efectiva es la que el shim
#  espera. Si esa sonda sale mal, todo lo que siga mide otra maquina.
#
#  NO relanza Docker al aterrizar. El cable lo corta el PO.
#
#  Caja negra: /tmp/caja-negra-vuelo5.log   ·   Veredicto: /tmp/veredicto-vuelo5.txt
# =============================================================================
set -uo pipefail

RAIZ=/Users/rodrigosilva/SOFTWARE/SII/XPERTIS/springboot-sii-2026
RAMA=spec-025-fase-2-labs-08-11
CLON=/tmp/dgt-vuelo5
ALUMNO=/tmp/dgt-alumno5
CAJA=/tmp/caja-negra-vuelo5.log
VEREDICTO=/tmp/veredicto-vuelo5.txt
ESPERA_MAX=3600

# JAVA_HOME HOSTIL a proposito: el 21 de la maquina, y primero en el PATH.
JAVA_HOSTIL="$HOME/.sdkman/candidates/java/21.0.1-graalce"
export JAVA_HOME="$JAVA_HOSTIL"
export PATH="$JAVA_HOME/bin:$PATH"

FALLAS=""; CONTAMINADO=0
: > "$CAJA"
exec > >(while IFS= read -r L; do printf '[%s] %s\n' "$(date '+%H:%M:%S')" "$L"; done >> "$CAJA") 2>&1

falla()  { FALLAS="$FALLAS $1"; }
titulo() { printf '\n############ %s ############\n' "$1"; }
hay_red() { ping -c1 -t3 github.com >/dev/null 2>&1; }
vigilar_red() {
    if hay_red; then
        CONTAMINADO=1
        printf '\n!!!!!! RED VIVA EN PLENO VUELO — lo que siga NO CUENTA !!!!!!\n\n'
        falla "CONTAMINADO(tras:$1)"
    fi
}

# ------------------------------------------------------------------ pista ---
titulo "EN PISTA · esperando el corte de red"
printf 'PID: %s\n' "$$"
printf 'Red al lanzar: %s\n' "$(hay_red && echo 'VIVA (correcto)' || echo 'YA CORTADA')"

rm -rf "$CLON"
if git clone -q --branch "$RAMA" "$RAIZ" "$CLON"; then
    printf 'clon fresco: %s\n' "$(du -sh "$CLON" | cut -f1)"
else
    printf 'VUELO 5 ABORTADO — no pude clonar\n' | tee "$VEREDICTO"; exit 2
fi
osascript -e 'quit app "Docker"' >/dev/null 2>&1

ESPERADO=0
while hay_red; do
    sleep 3; ESPERADO=$((ESPERADO + 3))
    [ $((ESPERADO % 120)) -eq 0 ] && printf 'esperando... %ss\n' "$ESPERADO"
    if [ "$ESPERADO" -ge "$ESPERA_MAX" ]; then
        printf 'VUELO 5 ABORTADO — la red nunca se corto (%ss)\n' "$ESPERADO" | tee "$VEREDICTO"; exit 2
    fi
done
printf '\n>>> DESPEGUE tras %ss <<<\n' "$ESPERADO"
[ -d "$HOME/.m2" ] && mv "$HOME/.m2" "$HOME/.m2-guardado-spec025"
sleep 5

titulo "EVIDENCIA DE AISLAMIENTO"
printf -- '--- ping -c1 github.com ---\n'; ping -c1 -t8 github.com 2>&1 | tail -2; printf 'exit=%s\n' "$?"
printf -- '--- curl repo1.maven.org ---\n'; curl -sS -m 10 -o /dev/null -w 'http=%{http_code}\n' https://repo1.maven.org/maven2/ 2>&1; printf 'exit=%s\n' "$?"
printf -- '--- ruta por defecto ---\n'; route -n get default 2>&1 | grep -E 'interface|gateway' || printf '  (sin ruta)\n'
printf -- '--- el .m2 del usuario ---\n'; [ -d "$HOME/.m2" ] && { printf 'EXISTE (mal)\n'; falla "M2-PRESENTE"; } || printf 'apartado\n'
printf -- '--- docker ---\n'; timeout 12 docker info 2>/dev/null | grep -q 'Server Version' && { printf 'responde (mal)\n'; falla "DOCKER-VIVO"; } || printf 'sin daemon\n'
printf -- '--- java HOSTIL de la maquina ---\n'; printf 'JAVA_HOME=%s\n' "$JAVA_HOME"; java -version 2>&1 | head -1
printf -- '--- y el que el shim dice que va a usar ---\n'
( cd "$CLON/labs/lab-08-diplomacia-con-tesoreria/solucion" && ./mvnw -version 2>&1 | grep -E 'Java version' )

# ------------------------------------------------------- sonda de arquitectura ---
# Barata y va primero: si la arquitectura efectiva no es la que el shim espera,
# el shim se cae al Java del sistema por diseño y TODO lo que siga mide otra cosa.
titulo "SONDA DE ARQUITECTURA · ¿ve el shim la maquina que es?"
printf 'uname -s = %s   uname -m = %s\n' "$(uname -s)" "$(uname -m)"
if [ "$(uname -m)" != "arm64" ]; then
    printf 'ABORTADO: uname -m dice "%s". En un Mac Apple Silicon eso significa que\n' "$(uname -m)"
    printf 'algo de este arnes corre bajo Rosetta (un binario x86_64 en la cadena).\n'
    printf 'El shim se caeria al Java del sistema y el vuelo no mediria nada util.\n'
    printf 'VUELO 5 ABORTADO — arquitectura efectiva x86_64\n' | tee "$VEREDICTO"
    exit 2
fi
printf 'arquitectura correcta: el shim va a usar el JDK embebido.\n'

# Y se cita el java EFECTIVO de un fork, que es lo que de verdad importa: con el
# JAVA_HOME hostil puesto, ¿sobre que arranca el JVM de los tests?
cd "$CLON/labs/lab-06-dos-folios-un-numero/solucion" || exit 1
( for _ in $(seq 1 200); do
    pgrep -fl 'bin/java' 2>/dev/null | grep -oE '/[^ ]*/bin/java' >> /tmp/w5-forks.txt
    sleep 0.3
  done ) & VIG=$!
: > /tmp/w5-forks.txt
./mvnw -B verify > /tmp/w5-sonda.log 2>&1; E_SONDA=$?
kill "$VIG" 2>/dev/null
printf 'JAVA_HOME hostil : %s\n' "$JAVA_HOME"
printf 'Maven corre sobre: %s\n' "$(./mvnw -version 2>&1 | grep -m1 -oE 'Java version: [0-9.]+')"
printf 'java EFECTIVO de los forks:\n'; sort -u /tmp/w5-forks.txt | sed 's/^/    /'
printf 'suite del Lab 06 : EXIT=%s\n' "$E_SONDA"
[ "$E_SONDA" -eq 0 ] || falla "SONDA-LAB06"
sort -u /tmp/w5-forks.txt | grep -q 'tools/jdk/runtime' || falla "FORK-NO-USA-EL-JDK-EMBEBIDO"
vigilar_red sonda-arquitectura

# ------------------------------------------------------------------ labs ---
suite() {  # suite <lab> <proyecto> <verde|rojo>
    local lab="$1" proy="$2" esp="$3" d="$CLON/labs/$1/$2"
    cd "$d" || { falla "$lab/$proy-NO-EXISTE"; return; }
    local t0 t1 e f="/tmp/w5-$lab-$proy.log"
    t0=$(date +%s); ./mvnw -B verify > "$f" 2>&1; e=$?; t1=$(date +%s)
    local desc; desc=$(grep -c 'Downloading from' "$f")
    printf '  %-16s EXIT=%s  %ss  descargas=%s  %s\n' "$proy" "$e" "$((t1-t0))" "$desc" \
        "$(grep -E '^\[(INFO|ERROR)\] Tests run: [0-9]+, Failures' "$f" | tail -1 | sed 's/\[INFO\] //;s/\[ERROR\] //')"
    [ "$desc" -eq 0 ] || falla "$lab/$proy-DESCARGO"
    if [ "$esp" = verde ] && [ "$e" -ne 0 ]; then
        falla "$lab/$proy-ROJO"
        grep -E '<<< (FAILURE|ERROR)!' "$f" | head -4 | sed 's/^/      /'
    fi
    if [ "$esp" = rojo ]; then
        local aj; aj=$(grep -oE '\] +[A-Za-z0-9_.]+(Test|IT)\.[A-Za-z0-9_]+' "$f" | grep -vE 'T[0-9]_|E[0-9]_' | sort -u | wc -l | tr -d ' ')
        printf '      fallos fuera de enunciado/: %s\n' "$aj"
        [ "${aj:-1}" -eq 0 ] || falla "$lab/$proy-FALLOS-AJENOS"
    fi
}

ciclo() {  # ciclo <lab>
    local lab="$1" d="$CLON/labs/$1"
    cd "$d" || return
    printf '  --- N3 · start-lab + destruir ---\n'
    ./bin/start-lab.sh --dir solucion > "/tmp/w5-$lab-start.log" 2>&1; local e=$?
    printf '  start-lab EXIT=%s · health=%s\n' "$e" "$(curl -sS -m 12 -o /dev/null -w '%{http_code}' http://localhost:8099/actuator/health 2>&1)"
    [ "$e" -eq 0 ] || falla "$lab-ARRANQUE"
    # TESO in-process: su API de administracion tiene que responder en el 8089.
    printf '  TESO simulado (8089) admin=%s\n' \
        "$(curl -sS -m 10 -o /dev/null -w '%{http_code}' http://localhost:8089/__admin/mappings 2>&1)"
    ./bin/99-destruir.sh > "/tmp/w5-$lab-destruir.log" 2>&1
    sleep 2
    printf '  --- N5 · huerfanos ---\n'
    local hp hw
    hp=$(pgrep -f 'embedded-pg/PG-.*/bin/postgres' | wc -l | tr -d ' ')
    hw=$(lsof -nP -iTCP:8089 -sTCP:LISTEN 2>/dev/null | grep -c LISTEN)
    printf '  postgres=%s  TESO-8089-escuchando=%s\n' "$hp" "$hw"
    [ "$hp" -eq 0 ] || falla "$lab-HUERFANOS-PG($hp)"
    [ "$hw" -eq 0 ] || falla "$lab-HUERFANOS-TESO($hw)"
    printf '  --- N4 · 90-validar ---\n'
    ./bin/90-validar.sh --dir solucion > "/tmp/w5-$lab-v-sol.log" 2>&1
    printf '    solucion: %s\n' "$(grep -oE 'LAB [0-9]+ (NO )?APROBADO' "/tmp/w5-$lab-v-sol.log" | head -1)"
    grep -q 'NO APROBADO' "/tmp/w5-$lab-v-sol.log" && falla "$lab-VALIDAR-SOLUCION"
    ./bin/90-validar.sh --dir starter > "/tmp/w5-$lab-v-sta.log" 2>&1
    printf '    starter:  %s\n' "$(grep -oE 'LAB [0-9]+ (NO )?APROBADO' "/tmp/w5-$lab-v-sta.log" | head -1)"
    grep -q 'NO APROBADO' "/tmp/w5-$lab-v-sta.log" || falla "$lab-STARTER-NO-DEBERIA-APROBAR"
}

titulo "REGRESION · Lab 06, que esta SPEC no toco"
suite lab-06-dos-folios-un-numero solucion verde
vigilar_red regresion-06

for LAB in lab-08-diplomacia-con-tesoreria lab-09-caja-negra \
           lab-10-observabilidad lab-11-latidos; do
    titulo "$LAB"
    suite "$LAB" solucion verde
    suite "$LAB" starter  rojo
    ciclo "$LAB"
    vigilar_red "$LAB"
done

# ------------------------------------------------------- los demos en vivo ---
# Son los que de verdad usaban Docker. Si algo de esta SPEC se rompe sin red,
# se rompe aqui.
titulo "§4 · DEMO EN VIVO · Lab 10 --db-caida (el tablero que no miente)"
cd "$CLON/labs/lab-10-observabilidad" || exit 1
./bin/start-lab.sh --dir solucion --db-caida > /tmp/w5-demo-10.log 2>&1
grep -E 'Tumbando|PostgreSQL detenido|readiness|El tablero' /tmp/w5-demo-10.log | head -6
grep -q 'El tablero dice la VERDAD' /tmp/w5-demo-10.log || falla "DEMO10-TABLERO"
./bin/99-destruir.sh > /dev/null 2>&1
vigilar_red demo-10

titulo "§4 · DEMO EN VIVO · Lab 11 --instancias 2 (dos servidores, un candado)"
cd "$CLON/labs/lab-11-latidos" || exit 1
./bin/start-lab.sh --dir solucion --instancias 2 > /tmp/w5-demo-11-sol.log 2>&1
grep -E 'La base de la instancia 1|Instancia 2 viva|Cierres ejecutados|corrio UNA|corrió UNA|El cierre' /tmp/w5-demo-11-sol.log | head -6
grep -q 'El cierre corrió UNA sola vez' /tmp/w5-demo-11-sol.log || falla "DEMO11-SOLUCION"
./bin/99-destruir.sh > /dev/null 2>&1
# Y el crimen, que tiene que seguir siendo visible.
./bin/start-lab.sh --dir starter --instancias 2 > /tmp/w5-demo-11-sta.log 2>&1
grep -E 'El cierre corrió [0-9]+ veces|duplicados' /tmp/w5-demo-11-sta.log | head -3
grep -q 'veces en el mismo latido' /tmp/w5-demo-11-sta.log || falla "DEMO11-CRIMEN-INVISIBLE"
./bin/99-destruir.sh > /dev/null 2>&1
vigilar_red demo-11

# ---------------------------------------------------------------- simulacro ---
titulo "SIMULACRO DEL ALUMNO (todo frio, sin red)"
rm -rf "${TMPDIR:-/tmp}/embedded-pg" && printf 'cache de binarios de Zonky borrada\n'
rm -rf "$ALUMNO"
T0=$(date +%s); git clone -q --branch "$RAMA" "$RAIZ" "$ALUMNO"; T1=$(date +%s); CLONE=$((T1-T0))
printf 'git clone: %ss · %s\n' "$CLONE" "$(du -sh "$ALUMNO" | cut -f1)"

cd "$ALUMNO/labs/lab-08-diplomacia-con-tesoreria/solucion" || exit 1
T0=$(date +%s); ./mvnw -B verify > /tmp/w5-alumno-08.log 2>&1; E=$?; T1=$(date +%s); SUITE=$((T1-T0))
printf 'Lab 08 · ./mvnw verify: EXIT=%s · %ss · descargas=%s\n' "$E" "$SUITE" "$(grep -c 'Downloading from' /tmp/w5-alumno-08.log)"
[ "$E" -eq 0 ] || falla "SIMULACRO-SUITE"

cd "$ALUMNO/labs/lab-11-latidos" || exit 1
T0=$(date +%s); ./bin/start-lab.sh --dir solucion > /tmp/w5-alumno-11.log 2>&1; E=$?; T1=$(date +%s); START=$((T1-T0))
printf 'Lab 11 · start-lab: EXIT=%s · %ss · health=%s\n' "$E" "$START" \
    "$(curl -sS -m 12 -o /dev/null -w '%{http_code}' http://localhost:8099/actuator/health 2>&1)"
[ "$E" -eq 0 ] || falla "SIMULACRO-ARRANQUE"
./bin/99-destruir.sh > /dev/null 2>&1

printf '\n--- CRONOMETRO DEL ALUMNO ---\n'
printf '  git clone                %4ss\n' "$CLONE"
printf '  Lab 08 verify (frio)     %4ss\n' "$SUITE"
printf '  Lab 11 start-lab         %4ss\n' "$START"
printf '  ------------------------------\n'
printf '  TOTAL                    %4ss\n' "$((CLONE+SUITE+START))"

# ------------------------------------------------------------- aterrizaje ---
titulo "ATERRIZAJE VUELO 5"
hay_red && printf 'red YA RESTAURADA\n' || printf 'red aun cortada\n'
printf 'postgres al final: %s\n' "$(pgrep -f 'embedded-pg/PG-.*/bin/postgres' | wc -l | tr -d ' ')"
printf 'escuchando en 8089/8099/8100: %s\n' "$(lsof -nP -iTCP:8089 -iTCP:8099 -iTCP:8100 -sTCP:LISTEN 2>/dev/null | grep -c LISTEN)"
[ -d "$HOME/.m2-guardado-spec025" ] && mv "$HOME/.m2-guardado-spec025" "$HOME/.m2" && printf 'HOME/.m2 restaurado\n'
printf 'Docker NO se relanza: lo levanta el PO si lo necesita.\n'

if [ -z "$FALLAS" ]; then
    printf '\nVUELO 5 OK — labs 08-11 en avion, sin Docker, con los dos demos en vivo\n' | tee "$VEREDICTO"
else
    printf '\nVUELO 5 CON FALLAS EN:%s\n' "$FALLAS" | tee "$VEREDICTO"
fi
[ "$CONTAMINADO" -eq 1 ] && printf 'ATENCION: hubo red viva a mitad del vuelo.\n' | tee -a "$VEREDICTO"
printf '\nFIN DEL VUELO 5\n'
