# Teoría · Módulo 15 (Contenedores, Arranque Acelerado y Proyecto Final) + cierre del curso

## Índice

1. [El jar ya no es el entregable](#1-el-jar-ya-no-es-el-entregable)
2. [El jar por capas](#2-el-jar-por-capas)
3. [Buildpacks vs Dockerfile](#3-buildpacks-vs-dockerfile)
4. [La imagen OCI](#4-la-imagen-oci)
5. [Arranque acelerado: Leyden y GraalVM](#5-arranque-acelerado-leyden-y-graalvm)
6. [Secretos: nunca dentro de la imagen](#6-secretos-nunca-dentro-de-la-imagen)
7. [Apagado elegante](#7-apagado-elegante)
8. [Las sondas, otra vez](#8-las-sondas-otra-vez)
9. [Qué construiste en trece sesiones](#9-qué-construiste-en-trece-sesiones)
10. [La despedida de Carolina](#10-la-despedida-de-carolina)

---

## 1. El jar ya no es el entregable

Durante trece sesiones el artefacto fue un `.jar`. En producción, el artefacto es una **imagen**: el
jar más el JDK, más el sistema de archivos, más cómo arranca y con qué usuario. «Funciona en mi
máquina» deja de ser una excusa cuando la máquina viaja con el código.

Lo que cambia de verdad no es el formato: es **quién es responsable del entorno**. Con un jar, el que
despliega tiene que acertar con la versión de Java. Con una imagen, esa decisión viaja dentro y deja
de ser negociable.

## 2. El jar por capas

Un jar de Spring Boot pesa decenas de megas, y el 95 % son dependencias que **casi nunca cambian**.
Si la imagen mete todo en una capa, cada despliegue sube todo otra vez.

El jar por capas lo separa en cuatro, de lo más estable a lo más volátil:

```
dependencies          ← cambian cuando tocas el pom (poco)
spring-boot-loader    ← cambian con la versión de Boot (menos)
snapshot-dependencies
application           ← cambia cada vez que compilas (siempre)
```

Docker cachea por capa, así que un despliegue normal sube **solo la última**: unos cientos de
kilobytes en vez de sesenta megas. En un pipeline que despliega diez veces al día, eso es la
diferencia entre dos minutos y veinte segundos.

## 3. Buildpacks vs Dockerfile

```bash
./mvnw spring-boot:build-image
```

Sin escribir un `Dockerfile`. Los **Buildpacks** de Cloud Native detectan que es una app Java, eligen
el JDK, aplican el troceado por capas, ponen un usuario no-root y fijan puntos de entrada sensatos.

| | **Buildpacks** | **Dockerfile** |
|---|---|---|
| Escribes | nada | el archivo entero |
| Capas óptimas | de fábrica | si te acuerdas |
| Usuario no-root | de fábrica | si te acuerdas |
| Actualizar el JDK | recompilar | editar y no olvidarte |
| Control fino | limitado | total |
| Auditoría de seguridad | «confía en el buildpack» | ves cada línea |

**El criterio:** empieza con Buildpacks. Cambia a Dockerfile cuando tengas una razón concreta —una
imagen base corporativa obligatoria, un binario nativo que instalar, una auditoría que exige ver cada
capa—. «Prefiero controlarlo todo» no es una razón concreta: es una preferencia que se paga cada vez
que sale un parche de seguridad del JDK.

## 4. La imagen OCI

**OCI** (Open Container Initiative) es el estándar del formato. Que la imagen sea OCI significa que la
corre Docker, Podman, containerd o Kubernetes indistintamente: no estás atado a una herramienta.

```bash
docker run -p 8099:8099 \
  -e SPRING_DATASOURCE_URL=... \
  -e DGT_JWT_SECRET=... \
  dgt-tramites-api:0.1.0
```

Fíjate en lo que **no** va ahí dentro: ninguna credencial. Ver §6.

## 5. Arranque acelerado: Leyden y GraalVM

Una app Spring Boot arranca en 2–4 segundos. Suena poco hasta que escalas de 3 a 30 réplicas ante un
pico, o pagas por milisegundo en *serverless*.

| | **JVM normal** | **Caché AOT (Leyden)** | **GraalVM nativo** |
|---|---|---|---|
| Arranque | 2–4 s | ~1–2 s | **decenas de ms** |
| Memoria | la de siempre | la de siempre | mucha menos |
| Build | segundos | segundos | **minutos** |
| Reflexión / proxies | todo funciona | todo funciona | **hay que declararlos** |
| Pico sostenido | el mejor (JIT) | el mejor | algo peor |

**El criterio, que es lo que hay que llevarse:** el nativo es espectacular arrancando y **peor en
régimen** — el JIT de la JVM optimiza con lo que observa en caliente, y el nativo no puede. Si tu app
arranca una vez y corre días, el nativo te da poco y te cuesta un build de minutos y una lista de
declaraciones que mantener. Si arranca mil veces al día, cambia el negocio.

> Hoy **no se instalan** ni Leyden ni GraalVM. Se nombran, se comparan y se decide con criterio, que
> es lo que se va a evaluar. Instalarlos costaría media sesión y no enseñaría nada que no esté en esa
> tabla.

## 6. Secretos: nunca dentro de la imagen

La imagen es un artefacto **inmutable y compartible**. Todo lo que metas dentro viaja a un registro,
lo descarga quien tenga acceso, y queda en el historial de capas **aunque lo borres en la siguiente**.

Es el crimen del Lab 01, en su versión final: allí la contraseña quedaba en el historial de git; aquí
queda en el historial de capas de una imagen que además está publicada.

Los secretos entran **en ejecución**, por variable de entorno o por un gestor de secretos. Y el perfil
de producción los exige **sin default** (`${DGT_DB_PASSWORD}`, sin `:`): si falta, la aplicación no
arranca y dice cuál falta. Fallar rápido y fuerte es una decisión de diseño — un default silencioso
arranca contra la base equivocada y el error aparece un martes a las tres de la mañana.

## 7. Apagado elegante

Cuando el orquestador manda `SIGTERM`, la app tiene dos opciones: cortar en seco —y con ella las
peticiones en vuelo— o **terminar lo que empezó**.

```yaml
server.shutdown: graceful
spring.lifecycle.timeout-per-shutdown-phase: 20s
```

Deja de aceptar peticiones nuevas, termina las que tiene, y entonces muere. Sin esto, cada despliegue
—y despliegas a menudo— tira un puñado de peticiones a la basura. El usuario ve un error que nadie
registró como incidente porque «fue solo el despliegue».

## 8. Las sondas, otra vez

El Lab 10 lo dejó dicho y aquí se cobra: **liveness** («¿reinicio?») y **readiness** («¿le mando
tráfico?») responden preguntas cuyas acciones son opuestas.

En el despliegue eso se traduce en algo muy concreto: el orquestador no manda tráfico a una réplica
nueva hasta que su readiness diga que sí. Sin sondas, manda tráfico a una app que todavía está
arrancando y el despliegue produce errores que nadie entiende.

Y el criterio del Lab 12: **solo entra en readiness lo que, si falta, hace inútil a esta instancia**.
La base sí. El broker no.

---

## 9. Qué construiste en trece sesiones

Mira hacia atrás una vez, porque hoy entregas todo esto junto:

| | Lo que aprendiste a ver |
|---|---|
| **01** | Una contraseña en el historial no se borra: se **rota** |
| **02** | La entidad no sale por la API. Y un guardián lo hace imposible de olvidar |
| **03** | Los tests **son** el enunciado |
| **04** | Lo correcto también tiene un costo, y se mide |
| **05** | «Va lento» no es un diagnóstico. **1.847 consultas** sí lo es |
| **06** | El candado va en el **dato**, no en el código |
| **07** | Codificar no es cifrar, y cifrar no es **firmar** |
| **08** | Un timeout es un presupuesto de espera. Sin él, tu app es rehén de la ajena |
| **09** | El log no es para ti: es para quien llega a las 3 AM |
| **10** | Un semáforo siempre en verde no es un semáforo: es un adorno |
| **11** | «Una vez al día» no significa nada hasta que alguien lo garantice |
| **12** | Mover no es guardar. Y «exactly once» no existe |
| **13** | Nadie te va a decir qué hacer |

Trece crímenes. Ninguno era sobre sintaxis.

## 10. La despedida de Carolina

> *«Cuando llegaste, te dije que había un botón y que del otro lado había un sistema. Trece semanas
> después ese sistema aguanta que se le caiga la base, que se le caiga Tesorería, que se le caiga el
> correo, y que alguien lo escale a cinco réplicas sin avisar.*
>
> *Pero no es eso lo que me llevo.*
>
> *Lo que me llevo es que hoy te di un requerimiento a medias y no me preguntaste qué hacer. Miraste
> dónde estaban los huecos, decidiste, y viniste a explicarme por qué. Eso no te lo enseñó ninguna
> lámina — eso lo aprendiste equivocándote once segundos, dos folios y un cierre nocturno duplicado.*
>
> *Los frameworks van a cambiar. Las anotaciones que memorizaste este mes van a tener otro nombre en
> dos años, y la máquina que hoy te escribe el código va a parecerte torpe en cinco.*
>
> *Lo que no caduca es mirar un sistema y saber dónde va a doler.*
>
> *Nos vemos en producción.»*
>
> — **Carolina Espinoza**

---

*Este laboratorio no siembra el siguiente: es el último. El arco cierra aquí.*
