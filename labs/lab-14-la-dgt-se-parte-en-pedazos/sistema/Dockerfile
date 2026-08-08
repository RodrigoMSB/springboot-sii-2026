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

ARG MODULO
# Se fija como variable de entorno para que el log de arranque diga qué es esto.
ENV DGT_MODULO=${MODULO}

WORKDIR /app

# `curl` para los healthchecks del compose. Alpine no lo trae.
RUN apk add --no-cache curl

COPY ${MODULO}/target/${MODULO}-1.0.0.jar /app/servicio.jar

# Usuario sin privilegios: la misma regla del Lab 13 (nada corre como root, ni
# siquiera en un laboratorio).
RUN addgroup -S dgt && adduser -S dgt -G dgt && chown -R dgt:dgt /app
USER dgt

ENTRYPOINT ["java", \
            "-XX:MaxRAMPercentage=60.0", \
            "-XX:+UseSerialGC", \
            "-jar", "/app/servicio.jar"]
