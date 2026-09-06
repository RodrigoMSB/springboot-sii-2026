#!/usr/bin/env bash
# =============================================================================
#  levantar.sh — los seis, en orden, esperando a que cada uno esté listo
# -----------------------------------------------------------------------------
#  POR QUÉ HAY UN SCRIPT AQUÍ Y NO EN LA DEMOSTRACIÓN CON DOCKER
#
#  Aquélla envuelve `docker compose up` en nada, y a propósito: lo que tenía que
#  enseñar ERA ese comando. Aquí el protagonista es otro —el registro y el Config
#  Server— y el arranque es solo el peaje para llegar a él. Seis terminales
#  abiertas en el orden correcto no enseñan nada que el README no diga mejor en
#  cuatro líneas, y sí son seis oportunidades de equivocarse delante de la sala.
#
#  El orden NO es una preferencia, y esto se midió (ver el README, «Y si se
#  arranca al revés»):
#
#    1. config    — los cuatro servicios MUEREN al arrancar si no lo encuentran
#    2. registro  — sin él arrancan, pero no se ven entre ellos
#    3. los cuatro — entre sí, el orden ya no importa
#
#  Sí, el config arranca antes que el registro y además se registra en él. Eso
#  significa que el config se inscribe unos segundos tarde, cuando el registro
#  aparece, y no pasa nada: el cliente de Eureka reintenta solo. Es la primera
#  cosa que la demostración enseña sobre un registro — que nadie tiene que estar
#  arriba en el momento exacto.
#
#  ---------------------------------------------------------------------------
#  Uso:
#      ./levantar.sh                  los seis, y espera a que el sistema SIRVA
#      ./levantar.sh contribuyentes   uno solo (el bloque 1 de la demostración)
#      ./levantar.sh --sin-config     SIN el Config Server, para enseñar que los
#                                     cuatro servicios ni siquiera arrancan
#      ./apagar.sh                    los mata a todos
#
#  Los logs quedan en `.estado/<servicio>.log`, uno por servicio, y los PID en
#  `.estado/<servicio>.pid`. Para mirar uno en vivo:
#      tail -f .estado/tramites.log
# =============================================================================
set -euo pipefail
cd "$(dirname "$0")"

RAIZ="$(pwd)"
ESTADO="$RAIZ/.estado"
mkdir -p "$ESTADO"

# El JDK del curso, el mismo que usa `./mvnw`. Sin esto, `java` sería el que
# tenga la máquina — que puede ser 17, 21 o ninguno.
JDK="$RAIZ/../../tools/jdk/runtime/macos-aarch64"
if [ -d "$JDK" ]; then
  VERSION="$(ls "$JDK" | head -1)"
  export JAVA_HOME="$JDK/$VERSION/Contents/Home"
  export PATH="$JAVA_HOME/bin:$PATH"
fi

if ! command -v java >/dev/null 2>&1; then
  echo "[ERROR] no hay java. Corre ./construir.sh una vez: ensambla el JDK del curso." >&2
  exit 1
fi

# El Config Server necesita la ruta ABSOLUTA de config-repo: si se le pasa una
# relativa, depende del directorio desde el que se arrancó, y eso es exactamente
# la fragilidad que no se quiere en una clase.
export DGT_CONFIG_REPO="file://$RAIZ/config-repo"

SIN_CONFIG=""
UNO=""
case "${1:-}" in
  --sin-config) SIN_CONFIG="si" ;;
  "")           ;;
  *)            UNO="$1" ;;      # `./levantar.sh contribuyentes` levanta uno solo
esac

# ===========================================================================
#  EL TECHO DE MEMORIA, y por qué está aquí
# ===========================================================================
#  Sin `-Xmx`, cada JVM dimensiona su heap contra la RAM de la MÁQUINA: un cuarto
#  de la memoria física, por defecto. En el Mac de 64 GB donde se preparó esto,
#  los seis procesos juntos pedían 3,1 GB de RSS; en un portátil de 16 GB pedirían
#  bastante menos. O sea: «cuánta memoria necesita esta demostración» pasaba a
#  depender de quién preguntara, que es la peor respuesta posible para un material
#  que se proyecta en una máquina que no es ésta.
#
#  Con el techo puesto, la cifra es la misma en todas partes. Es exactamente la
#  misma decisión que el `mem_limit` de la demostración con Docker, y por la misma
#  razón — sólo que allí lo pone el orquestador y aquí hay que ponerlo a mano.
#  Otra pieza más de trabajo de operación que, sin plataforma, hace una persona.
# ===========================================================================
techo() {
  case "$1" in
    contribuyentes|tramites|auditoria) echo "-Xmx320m" ;;   # llevan JPA, Flyway y el pool
    *)                                 echo "-Xmx256m" ;;   # registro, config y gateway
  esac
}

# ---------------------------------------------------------------------------
#  puerto_de <servicio>
# ---------------------------------------------------------------------------
#  El puerto NO se escribe aquí: se LEE de `config-repo/<servicio>.yml`, que es
#  donde vive de verdad. Si estuviera en las dos partes, el día que se cambie en
#  el Config Server —que es justo lo que hace el bloque 2— este script seguiría
#  esperando en el puerto viejo y diría que el servicio no arrancó.
#
#  El registro y el config no están en `config-repo/` (son los que arrancan
#  antes que nadie), así que sus puertos sí van aquí.
# ---------------------------------------------------------------------------
puerto_de() {
  case "$1" in
    registro) echo 8761 ;;
    config)   echo 8888 ;;
    *)        sed -n 's/^  port: \([0-9][0-9]*\).*/\1/p' "$RAIZ/config-repo/$1.yml" | head -1 ;;
  esac
}

# ---------------------------------------------------------------------------
#  arrancar <servicio> <ruta-de-salud>
# ---------------------------------------------------------------------------
arrancar() {
  SERVICIO="$1"; SALUD="$2"
  PUERTO="$(puerto_de "$SERVICIO")"
  if [ -z "$PUERTO" ]; then
    echo "[ERROR] no encuentro el puerto de $SERVICIO en config-repo/$SERVICIO.yml" >&2
    exit 1
  fi
  JAR="$(ls "$RAIZ/sistema/$SERVICIO/target/"*.jar 2>/dev/null | head -1)"

  if [ -z "$JAR" ]; then
    echo "[ERROR] falta el jar de $SERVICIO. Corre ./construir.sh primero." >&2
    exit 1
  fi

  if lsof -ti "tcp:$PUERTO" >/dev/null 2>&1; then
    echo "[ERROR] el puerto $PUERTO ya está ocupado (¿otra copia corriendo? ¿./apagar.sh?)" >&2
    exit 1
  fi

  printf '  %-15s :%-5s ' "$SERVICIO" "$PUERTO"

  # CADA UNO ARRANCA DESDE SU PROPIO DIRECTORIO, y no es cosmético: los tres
  # servicios con base levantan un PostgreSQL embebido cuyo directorio de datos
  # es `.datos-pg` RELATIVO al directorio de trabajo. Arrancándolos todos desde
  # aquí, los tres pelean por el mismo `.datos-pg/epg-lock` y el segundo muere
  # con el mensaje de candado tomado. En el laboratorio no pasa porque cada
  # terminal hace su propio `cd`; aquí hay que hacerlo a mano.
  ( cd "$RAIZ/sistema/$SERVICIO" \
      && exec nohup java $(techo "$SERVICIO") -jar "$JAR" > "$ESTADO/$SERVICIO.log" 2>&1 ) &
  echo $! > "$ESTADO/$SERVICIO.pid"

  # Se espera a que RESPONDA, no un `sleep`. Un cronómetro funciona en la
  # máquina de quien lo escribió y falla en la del vecino — es la misma lección
  # que `condition: service_healthy` en la demostración con Docker, hecha a mano
  # porque aquí no hay orquestador que la haga por nosotros.
  INICIO="$(date +%s)"
  for _ in $(seq 1 120); do
    if curl -fsS -o /dev/null --max-time 2 "http://localhost:$PUERTO$SALUD" 2>/dev/null; then
      echo "listo en $(( $(date +%s) - INICIO ))s"
      return 0
    fi
    if ! kill -0 "$(cat "$ESTADO/$SERVICIO.pid")" 2>/dev/null; then
      echo "MURIÓ"
      echo
      echo "[ERROR] $SERVICIO no llegó a arrancar. Las últimas líneas de su log:" >&2
      tail -20 "$ESTADO/$SERVICIO.log" | sed 's/^/        /' >&2
      exit 1
    fi
    sleep 0.5
  done
  echo "SIN RESPUESTA tras 60s"
  exit 1
}

# ---------------------------------------------------------------------------
#  Uno solo. Es lo que pide el BLOQUE 1 de la demostración: arrancar un servicio
#  con el panel de Eureka proyectado al lado y verlo aparecer.
# ---------------------------------------------------------------------------
if [ -n "$UNO" ]; then
  case "$UNO" in
    config)         arrancar config         /actuator/health ;;
    registro)       arrancar registro       /actuator/health ;;
    gateway)        arrancar gateway        /salud ;;
    contribuyentes) arrancar contribuyentes /salud ;;
    tramites)       arrancar tramites       /salud ;;
    auditoria)      arrancar auditoria      /salud ;;
    *) echo "[ERROR] no conozco el servicio '$UNO'." >&2
       echo "        Son: config registro gateway contribuyentes tramites auditoria" >&2
       exit 1 ;;
  esac
  exit 0
fi

ARRANQUE="$(date +%s)"
echo
echo "Levantando el sistema. El orden importa y está explicado en el README."
echo

if [ -z "$SIN_CONFIG" ]; then
  arrancar config /actuator/health
else
  echo "  (sin el Config Server, a propósito: los cuatro servicios van a MORIR)"
fi

arrancar registro       /actuator/health
arrancar gateway        /salud
arrancar contribuyentes /salud
arrancar tramites       /salud
arrancar auditoria      /salud

SEIS_ARRIBA=$(( $(date +%s) - ARRANQUE ))
echo
echo "  Los seis procesos responden (${SEIS_ARRIBA}s)."

# ===========================================================================
#  LA ESPERA QUE DE VERDAD IMPORTA — y que hay que explicar, porque es la
#  diferencia entre esta demostración y un accidente en directo
# ===========================================================================
#  Que los seis contesten a /salud NO significa que el sistema sirva. Cada
#  servicio guarda su propia COPIA del registro y la refresca cada
#  `registry-fetch-interval-seconds`. El gateway arrancó antes que trámites, así
#  que en su copia trámites todavía no está, y hasta el siguiente refresco
#  contesta:
#
#      WARN GATEWAY - No servers available for service: tramites
#
#  Con los 30 s de fábrica eso son 24 segundos de HTTP 503 con todo el sistema
#  sano — medido, y está en el informe de la SPEC-048. Por eso
#  `config-repo/application.yml` los baja a 5, y por eso este script no dice
#  «listo» hasta que una petición REAL cruza los cuatro servicios.
#
#  Es lo mismo que enseñó el Lab 11 con `liveness` contra `readiness`: estar
#  vivo y estar listo para recibir tráfico no son la misma cosa. Aquí se paga en
#  segundos de pantalla en rojo si se confunden.
# ===========================================================================
PUERTA="$(puerto_de gateway)"

printf '  Esperando a que los seis se VEAN entre ellos '
TOKEN="$(curl -s --max-time 5 -X POST "http://localhost:$PUERTA/auth/login" \
  -H 'Content-Type: application/json' \
  -d '{"usuario":"carolina","clave":"dgt2026"}' | sed 's/.*"token":"\([^"]*\)".*/\1/')"

LISTO=""
for _ in $(seq 1 120); do
  COD="$(curl -s -o /dev/null -w '%{http_code}' --max-time 3 \
         -H "Authorization: Bearer $TOKEN" "http://localhost:$PUERTA/tramites/1" || true)"
  if [ "$COD" = "200" ]; then LISTO="si"; break; fi
  printf '.'
  sleep 0.5
done
echo

if [ -z "$LISTO" ]; then
  echo
  echo "[ERROR] los seis procesos están vivos pero el sistema no sirve una petición." >&2
  echo "        Mira quién falta en el panel: http://localhost:8761" >&2
  exit 1
fi

echo
echo "  Sistema LISTO en $(( $(date +%s) - ARRANQUE ))s (procesos arriba: ${SEIS_ARRIBA}s)."
echo
echo "  panel del registro   http://localhost:8761"
echo "  config server        http://localhost:8888/tramites/default"
echo "  la puerta            http://localhost:$PUERTA"
echo
