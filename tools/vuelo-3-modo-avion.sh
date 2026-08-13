#!/usr/bin/env bash
# =============================================================================
#  VUELO 3 · SPEC-023 — la primera mitad del curso en modo avión
# -----------------------------------------------------------------------------
#  Autónomo y desatendido. Se lanza con la red viva y espera en pista; despega
#  solo cuando detecta que el cable se fue.
#
#  Cubre:  · N1-N5 en avión de los SIETE labs, del 01 al 07
#          · el 01 y el 02 se revalidan enteros: los toqué después de su vuelo 1
#            (aislamiento por contexto, unificación, manifiestos), así que su
#            evidencia anterior está vencida. Eso cierra además V6 y V10 de la 022
#          · V10-bis: simulacro del alumno cronometrado, con caché fría
#
#  NO relanza Docker al aterrizar: quedó mañoso tras los relanzamientos
#  programáticos de la noche anterior. Lo levanta el PO a mano si lo necesita.
#
#  Caja negra: /tmp/caja-negra-vuelo3.log   ·   Veredicto: /tmp/veredicto-vuelo3.txt
# =============================================================================
set -uo pipefail

CLON=/tmp/dgt-vuelo3
ALUMNO=/tmp/dgt-alumno3
RAMA=spec-023-fase-1-labs-03-07
CAJA=/tmp/caja-negra-vuelo3.log
VEREDICTO=/tmp/veredicto-vuelo3.txt
ESPERA_MAX=3600
export JAVA_HOME="$HOME/.sdkman/candidates/java/25-tem"
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

# El clon se hace AQUI, en pista: es local, no necesita red, y asi el vuelo
# solo cronometra verificacion. Docker se cierra ahora, no despues.
rm -rf "$CLON"
if git clone -q --branch "$RAMA" /Users/rodrigosilva/SOFTWARE/SII/XPERTIS/springboot-sii-2026 "$CLON"; then
    printf 'clon fresco: %s\n' "$(du -sh "$CLON" | cut -f1)"
else
    printf 'VUELO 3 ABORTADO — no pude clonar\n' | tee "$VEREDICTO"; exit 2
fi
osascript -e 'quit app "Docker"' >/dev/null 2>&1

ESPERADO=0
while hay_red; do
    sleep 3; ESPERADO=$((ESPERADO + 3))
    [ $((ESPERADO % 120)) -eq 0 ] && printf 'esperando... %ss\n' "$ESPERADO"
    if [ "$ESPERADO" -ge "$ESPERA_MAX" ]; then
        printf 'VUELO 3 ABORTADO — la red nunca se corto (%ss)\n' "$ESPERADO" | tee "$VEREDICTO"; exit 2
    fi
done
printf '\n>>> DESPEGUE tras %ss <<<\n' "$ESPERADO"
# Se aparta AHORA y no antes: si el vuelo nunca despega, el entorno del PO
# queda como estaba. Lo aprendimos con el vuelo 2, que se quedo en pista.
[ -d "$HOME/.m2" ] && mv "$HOME/.m2" "$HOME/.m2-guardado-spec023"
sleep 5

titulo "EVIDENCIA DE AISLAMIENTO"
printf -- '--- ping -c1 github.com ---\n'; ping -c1 -t8 github.com 2>&1 | tail -2; printf 'exit=%s\n' "$?"
printf -- '--- curl repo1.maven.org ---\n'; curl -sS -m 10 -o /dev/null -w 'http=%{http_code}\n' https://repo1.maven.org/maven2/ 2>&1; printf 'exit=%s\n' "$?"
printf -- '--- IPs no-loopback ---\n'
ifconfig 2>/dev/null | awk '/^[a-z0-9]+:/{i=substr($1,1,length($1)-1)} /^\t*inet /{print i, $2}' | grep -v '127.0.0.1' || printf '  (ninguna)\n'
printf -- '--- ruta por defecto ---\n'; route -n get default 2>&1 | grep -E 'interface|gateway' || printf '  (sin ruta)\n'
printf -- '--- el .m2 del usuario ---\n'; [ -d "$HOME/.m2" ] && { printf 'EXISTE (mal)\n'; falla "M2-PRESENTE"; } || printf 'apartado\n'
printf -- '--- docker ---\n'; timeout 12 docker info 2>/dev/null | grep -q 'Server Version' && { printf 'responde (mal)\n'; falla "DOCKER-VIVO"; } || printf 'sin daemon\n'
printf -- '--- java ---\n'; java -version 2>&1 | head -1

# ------------------------------------------------------------------ labs ---
suite() {  # suite <lab> <proyecto> <verde|rojo>
    local lab="$1" proy="$2" esp="$3" d="$CLON/labs/$1/$2"
    cd "$d" || { falla "$lab/$proy-NO-EXISTE"; return; }
    local t0 t1 e f="/tmp/w3-$lab-$proy.log"
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
    ./bin/start-lab.sh --dir solucion > "/tmp/w3-$lab-start.log" 2>&1; local e=$?
    printf '  start-lab EXIT=%s · health=%s\n' "$e" "$(curl -sS -m 12 -o /dev/null -w '%{http_code}' http://localhost:8099/actuator/health 2>&1)"
    [ "$e" -eq 0 ] || falla "$lab-ARRANQUE"
    ./bin/99-destruir.sh > "/tmp/w3-$lab-destruir.log" 2>&1
    sleep 2
    local hp; hp=$(pgrep -f 'embedded-pg/PG-.*/bin/postgres' | wc -l | tr -d ' ')
    printf '  --- N5 · postgres huerfanos: %s\n' "$hp"
    [ "$hp" -eq 0 ] || falla "$lab-HUERFANOS($hp)"
    printf '  --- N4 · 90-validar ---\n'
    ./bin/90-validar.sh --dir solucion > "/tmp/w3-$lab-v-sol.log" 2>&1
    printf '    solucion: %s\n' "$(grep -oE 'LAB [0-9]+ (NO )?APROBADO' "/tmp/w3-$lab-v-sol.log" | head -1)"
    grep -q 'NO APROBADO' "/tmp/w3-$lab-v-sol.log" && falla "$lab-VALIDAR-SOLUCION"
    ./bin/90-validar.sh --dir starter > "/tmp/w3-$lab-v-sta.log" 2>&1
    printf '    starter:  %s\n' "$(grep -oE 'LAB [0-9]+ (NO )?APROBADO' "/tmp/w3-$lab-v-sta.log" | head -1)"
    grep -q 'NO APROBADO' "/tmp/w3-$lab-v-sta.log" || falla "$lab-STARTER-NO-DEBERIA-APROBAR"
}

# El 01 y el 02 entran aunque ya volaron: los toco DESPUES de su vuelo 1 —el
# aislamiento por contexto, la unificacion cosmetica y sus manifiestos
# regenerados—, asi que aquella evidencia esta vencida. Se revalidan enteros.
for LAB in lab-01-del-otro-lado-del-boton lab-02-el-folio-que-se-filtro \
           lab-03-red-de-seguridad lab-04-el-arbol-de-tramites \
           lab-05-once-segundos lab-06-dos-folios-un-numero lab-07-el-portero; do
    titulo "$LAB"
    suite "$LAB" solucion verde
    suite "$LAB" starter  rojo
    [ "$LAB" = lab-05-once-segundos ] && suite "$LAB" solucion-con-n1 rojo-por-diseno
    ciclo "$LAB"
    vigilar_red "$LAB"
done

titulo "§4.1 · el N+1 con el cable fuera"
grep -A6 'elListadoPaginadoNoDisparaNMasUno' /tmp/w3-lab-05-once-segundos-solucion-con-n1.log \
    | grep -E 'Expecting|[0-9]+L|less than' | head -4
printf 'contador en solucion (debe pasar): %s\n' \
    "$(grep 'E1_ContadorDeConsultasIT' /tmp/w3-lab-05-once-segundos-solucion.log | grep -o 'Tests run: [0-9]*, Failures: [0-9]*, Errors: [0-9]*' | head -1)"

titulo "§4.2 · la carrera del Lab 06 con el cable fuera"
grep -E 'E[1-4]_(EmisionConcurrente|Idempotencia|CheckMontoCero|Rollback)' \
    /tmp/w3-lab-06-dos-folios-un-numero-solucion.log | grep 'Tests run'

# --------------------------------------------------------------- V10-bis ---
titulo "V10-bis · SIMULACRO DEL ALUMNO (todo frio, sin red)"
rm -rf "${TMPDIR:-/tmp}/embedded-pg" && printf 'cache de binarios de Zonky borrada: arranque frio de verdad\n'
rm -rf "$ALUMNO"
T0=$(date +%s); git clone -q --branch "$RAMA" /Users/rodrigosilva/SOFTWARE/SII/XPERTIS/springboot-sii-2026 "$ALUMNO"; T1=$(date +%s); CLONE=$((T1-T0))
printf 'git clone: %ss · %s\n' "$CLONE" "$(du -sh "$ALUMNO" | cut -f1)"

cd "$ALUMNO/labs/lab-06-dos-folios-un-numero/solucion" || exit 1
T0=$(date +%s); ./mvnw -B verify > /tmp/w3-alumno-06.log 2>&1; E=$?; T1=$(date +%s); SUITE=$((T1-T0))
printf 'Lab 06 · ./mvnw verify: EXIT=%s · %ss · descargas=%s\n' "$E" "$SUITE" "$(grep -c 'Downloading from' /tmp/w3-alumno-06.log)"
[ "$E" -eq 0 ] || falla "V10bis-SUITE"

cd "$ALUMNO/labs/lab-07-el-portero" || exit 1
T0=$(date +%s); ./bin/start-lab.sh --dir solucion > /tmp/w3-alumno-07.log 2>&1; E=$?; T1=$(date +%s); START=$((T1-T0))
printf 'Lab 07 · start-lab: EXIT=%s · %ss · health=%s\n' "$E" "$START" \
    "$(curl -sS -m 12 -o /dev/null -w '%{http_code}' http://localhost:8099/actuator/health 2>&1)"
[ "$E" -eq 0 ] || falla "V10bis-ARRANQUE"
./bin/99-destruir.sh > /dev/null 2>&1

printf '\n--- CRONOMETRO DEL ALUMNO ---\n'
printf '  git clone                %4ss\n' "$CLONE"
printf '  Lab 06 verify (frio)     %4ss\n' "$SUITE"
printf '  Lab 07 start-lab         %4ss\n' "$START"
printf '  ------------------------------\n'
printf '  TOTAL                    %4ss\n' "$((CLONE+SUITE+START))"

# ------------------------------------------------------------- aterrizaje ---
titulo "ATERRIZAJE"
hay_red && printf 'red YA RESTAURADA\n' || printf 'red aun cortada\n'
printf 'postgres al final: %s\n' "$(pgrep -f 'embedded-pg/PG-.*/bin/postgres' | wc -l | tr -d ' ')"
[ -d "$HOME/.m2-guardado-spec023" ] && mv "$HOME/.m2-guardado-spec023" "$HOME/.m2" && printf 'HOME/.m2 restaurado\n'
printf 'Docker NO se relanza: lo levanta el PO si lo necesita.\n'

if [ -z "$FALLAS" ]; then
    printf '\nVUELO 3 OK — tronco y labs 02-07 en modo avion\n' | tee "$VEREDICTO"
else
    printf '\nVUELO 3 CON FALLAS EN:%s\n' "$FALLAS" | tee "$VEREDICTO"
fi
[ "$CONTAMINADO" -eq 1 ] && printf 'ATENCION: hubo red viva a mitad del vuelo.\n' | tee -a "$VEREDICTO"
printf '\nFIN DEL VUELO 3\n'
