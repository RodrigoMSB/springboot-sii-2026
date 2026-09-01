# =============================================================================
#  Dockerfile — UNA imagen para los cuatro servicios
# -----------------------------------------------------------------------------
#  El argumento MODULO decide qué jar entra. Cuatro servicios, un solo
#  Dockerfile: si mañana hay que cambiar la JVM base o los flags de memoria, se
#  cambia aquí y vale para los cuatro.
#
#  El jar tiene que estar construido ANTES (`./construir.sh`); esta imagen no
#  compila nada. Es a propósito, y aquí hay un motivo que el lab viejo no tenía:
#  las dependencias del curso viven en `repo-maven/`, DENTRO del repositorio, y
#  compilar dentro de Docker significaría meter 230 MB en el contexto de
#  construcción o renunciar al modo offline. Se compila fuera, con el `./mvnw`
#  del curso, y aquí solo se copia el resultado.
#
#  Heredado del lab viejo (`material-v0.8.0`), y no es estilo: es el ORDEN de las
#  capas. Docker comparte las capas idénticas entre imágenes, así que todo lo que
#  NO depende del módulo va ANTES del `ARG MODULO`. Si va después, el ARG ya ha
#  ensuciado el contexto y Docker considera distintas unas capas que son iguales:
#  cuatro copias de lo mismo en disco. Aquello se midió y daba 626 MB de
#  diferencia con cinco servicios.
# =============================================================================
FROM eclipse-temurin:25-jre-alpine

# `curl` es para los healthchecks del compose: Alpine no lo trae. Va aquí arriba,
# antes del ARG, para que la capa se comparta entre las cuatro imágenes.
RUN apk add --no-cache curl

# Usuario sin privilegios: la misma regla del Lab 13. Nada corre como root, ni
# siquiera en una demostración.
RUN addgroup -S dgt && adduser -S dgt -G dgt

WORKDIR /app

# ---------------------------------------------------------------------------
#  A partir de AQUÍ empieza lo propio de cada servicio.
# ---------------------------------------------------------------------------
ARG MODULO
# Se fija como variable de entorno para que el contenedor sepa decir qué es.
ENV DGT_MODULO=${MODULO}

COPY --chown=dgt:dgt sistema/${MODULO}/target/lab14-${MODULO}-docker-0.1.0.jar /app/servicio.jar

USER dgt

EXPOSE 8080

# -XX:MaxRAMPercentage se calcula sobre la memoria DEL CONTENEDOR, y por eso el
# compose le pone `mem_limit` a cada uno: sin el techo, cada JVM mira la RAM de
# la máquina entera —23 GB en la del PO— y dimensiona su heap contra ese número.
# Con siete contenedores, eso deja de caber en cualquier sitio.
ENTRYPOINT ["java", \
            "-XX:MaxRAMPercentage=60.0", \
            "-XX:+UseSerialGC", \
            "-jar", "/app/servicio.jar"]
