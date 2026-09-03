# Lab 12 · Tareas y asincronía

Trabajo que ocurre **sin que nadie lo pida**, y trabajo que ocurre **sin que nadie lo espere**.

Son dos cosas distintas y hoy se ven las dos: una tarea que se ejecuta sola cada cierto tiempo, y
un trabajo lento que deja de bloquear al usuario que lo disparó.

## Los números del laboratorio

| | el usuario espera |
|---|---|
| Crear un trámite con 3 avisos **síncronos** | **3,02 s** |
| El mismo trámite con 3 avisos **asíncronos** | **0,004 s** |

Setecientas veces más rápido, y el trabajo se hace igual — sólo que después, y en paralelo.

Y el otro número, el del paso 3:

```
antes:   "hiloQueAtiende": "Thread[#58,http-nio-8103-exec-1,5,main]"   "esVirtual": false
después: "hiloQueAtiende": "VirtualThread[#68,tomcat-handler-0]/..."   "esVirtual": true
```

Una línea de YAML, y Tomcat entero pasó a hilos virtuales sin tocar el código.

## Qué se aprende

- `@Scheduled` con intervalo fijo, y la diferencia entre **`fixedRate` y `fixedDelay`** —que no es
  un detalle: uno de los dos puede solapar ejecuciones.
- `@Async`: devolver la respuesta antes de terminar el trabajo, con la medición al lado, y **sus
  tres trampas**.
- Los **hilos virtuales** de Java 25: qué cambian, cómo se encienden, y **cuándo no sirven de
  nada**.
- Y en el cierre, el problema que nadie ve venir: **dos instancias ejecutan la misma tarea
  programada.**

## Las tres carpetas

| | |
|---|---|
| **`practica/`** | Donde trabajas. Sin tareas programadas, sin asincronía, con hilos de plataforma |
| **`solucion/`** | La tarea programada, el `@Async` y los hilos virtuales encendidos |
| **`instructor/`** | Los mismos archivos explicados línea por línea. **No viaja en el repositorio** |

## Cómo se corre

```bash
cd practica          # o solucion
./mvnw spring-boot:run
```

| | puerto |
|---|---|
| `practica/` | **8103** |
| `solucion/` | **8104** |

## Los endpoints

```
POST /tramites/sincrono     3 avisos, uno detrás de otro
POST /tramites/asincrono    los mismos 3, sin esperar
GET  /tramites/quien        cuántas vueltas lleva el cierre y en qué clase de hilo se atiende
```

## Sin base de datos, y eso importa para el cierre

El problema de las dos instancias **se cuenta** hoy y **no se resuelve** hoy: la solución es un
candado distribuido, y un candado necesita un sitio compartido donde ponerlo — una tabla, un
Redis. Meter una base de datos aquí para resolverlo convertiría el lab en otro lab.

Así que el cierre de la sesión **enseña a reconocer el problema y nombra la solución**, sin
implementarla. Es deliberado: es la clase de fallo que en producción se descubre cuando ya duplicó
los totales de un mes.

## Lo que no vimos hoy

- **El cron de seis campos** de Spring (no el de cinco de Unix), y por qué la **zona horaria** va
  siempre escrita: sin ella se usa la del servidor, que en producción suele ser UTC.
- **El problema de las dos instancias**, y su solución: un **candado distribuido** —una tabla con
  expiración, ShedLock, Redis—. Se cuenta entero en el cierre del guion.
- **Colas de mensajes** (RabbitMQ, Kafka): cuando el trabajo asíncrono tiene que sobrevivir a que
  el proceso se caiga. `@Async` vive en memoria; si la aplicación muere, el aviso se pierde.
- **Planificadores distribuidos** (Quartz en clúster, el cron del orquestador): sacar la
  programación fuera de la aplicación.

## El guion

`PASOS.md` — el paso 0 y los tres pasos de la sesión.
