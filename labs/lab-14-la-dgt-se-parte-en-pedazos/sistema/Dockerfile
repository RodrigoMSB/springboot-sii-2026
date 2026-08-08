# =============================================================================
#  Dockerfile — UNA imagen para las cinco piezas
# -----------------------------------------------------------------------------
#  El argumento MODULO decide qué jar entra. Cinco servicios, un solo Dockerfile:
#  si mañana hay que cambiar la JVM base o los flags de memoria, se cambia aquí y
#  vale para todos.
#
#  El jar tiene que estar construido ANTES (`./mvnw package`); esta imagen no
#  compila nada. Es a propósito: compilar dentro de Docker sin caché de Maven
#  descarga medio Maven Central cinco veces y convierte un `up` de dos minutos en
#  uno de veinte. `bin/start-lab.sh` se encarga de compilar primero.
#
#  ⚠️  -XX:MaxRAMPercentage: sin este flag, cada JVM mira la RAM del CONTENEDOR y
#  se reserva una fracción; con seis JVM en un portátil, eso se va de las manos
#  rápido. Fijarlo es la diferencia entre que el lab quepa en 8 GB o no quepa.
#  El dato medido está en el README, §«Lo que le pide a tu máquina».
# =============================================================================
FROM eclipse-temurin:25-jre-alpine

# ---------------------------------------------------------------------------
#  TODO LO QUE NO DEPENDE DEL MÓDULO VA PRIMERO. Y esto no es estilo: son
#  cientos de megas.
# ---------------------------------------------------------------------------
#  Docker comparte las capas idénticas entre imágenes. Estas dos —instalar curl
#  y crear el usuario— son idénticas en las cinco, así que si van ANTES del
#  `ARG MODULO` se construyen una vez y se comparten. Si van después, el `ARG`
#  ya ha ensuciado el contexto y Docker las considera distintas: cinco copias de
#  lo mismo en disco.
#
#  Medido en este laboratorio, y por eso está escrito así:
#
#      orden malo (ARG arriba) ...... 1,85 GB en disco  (232 MB compartidos)
#      este orden ................... 1,22 GB en disco  (232 MB compartidos)
#
#  El mismo contenido, las mismas cinco imágenes, y 626 MB de diferencia por el
#  orden de cuatro líneas. Se comprobó con `docker system df -v`, que separa lo
#  compartido de lo único; `docker images` no vale para esto, porque suma la capa
#  base cinco veces y da 2,2 GB.
#
#  `curl` es para los healthchecks del compose: Alpine no lo trae.
RUN apk add --no-cache curl

# Usuario sin privilegios: la misma regla del Lab 13 (nada corre como root, ni
# siquiera en un laboratorio).
RUN addgroup -S dgt && adduser -S dgt -G dgt

WORKDIR /app

# A partir de AQUÍ empieza lo propio de cada servicio.
ARG MODULO
# Se fija como variable de entorno para que el log de arranque diga qué es esto.
ENV DGT_MODULO=${MODULO}

COPY --chown=dgt:dgt ${MODULO}/target/${MODULO}-1.0.0.jar /app/servicio.jar

USER dgt

ENTRYPOINT ["java", \
            "-XX:MaxRAMPercentage=60.0", \
            "-XX:+UseSerialGC", \
            "-jar", "/app/servicio.jar"]
