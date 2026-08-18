# Lab 11 · Tareas y asincronía

Trabajo que ocurre **sin que nadie lo pida**, y trabajo que ocurre **sin que nadie lo espere**.

Son dos cosas distintas y hoy se ven las dos: una tarea que se ejecuta sola cada cierto tiempo, y
un trabajo lento que deja de bloquear al usuario que lo disparó. Al final, el problema que
aparece cuando la aplicación deja de ser una sola.

## Los números del laboratorio

| | el usuario espera |
|---|---|
| Crear un trámite con 3 avisos **síncronos** | **3,03 s** |
| El mismo trámite con 3 avisos **asíncronos** | **0,004 s** |

Ochocientas veces más rápido, y el trabajo se hace igual — sólo que después, y en paralelo.

Y el número del paso 5, con dos instancias arriba:

```
12:58:55  [CIERRE] instancia-8104 · vuelta 6
12:58:55  [CIERRE] instancia-8114 · vuelta 1     ← el mismo segundo
12:59:01  [CIERRE] instancia-8104 · vuelta 7
12:59:01  [CIERRE] instancia-8114 · vuelta 2     ← otra vez
```

**El cierre nocturno se ejecutó dos veces.** Dos servidores, dos cierres, totales duplicados.

## Qué se aprende

- `@Scheduled` con intervalo fijo, y la diferencia entre **`fixedRate` y `fixedDelay`** —que no es
  un detalle: uno de los dos puede solapar ejecuciones.
- El **cron de seis campos** de Spring (no el de cinco de Unix) y por qué la **zona horaria** va
  escrita.
- `@Async`: devolver la respuesta antes de terminar el trabajo, con la medición al lado.
- Los **hilos virtuales** de Java 25: qué cambian, cómo se encienden, y **cuándo no sirven de
  nada**.
- El problema que nadie ve venir: **dos instancias ejecutan la misma tarea programada.** Se
  reproduce en vivo.

## Las tres carpetas

| | |
|---|---|
| **`practica/`** | Donde trabajas. Sin tareas programadas, sin asincronía, con hilos de plataforma |
| **`solucion/`** | Las dos tareas, el `@Async` y los hilos virtuales encendidos |
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

**En el paso 5 se levantan los dos a la vez.** Ese es el punto.

## Los endpoints

```
POST /tramites/sincrono     3 avisos, uno detrás de otro
POST /tramites/asincrono    los mismos 3, sin esperar
GET  /tramites/quien        qué instancia responde y en qué clase de hilo
```

## Sin base de datos, y eso importa para el paso 5

El problema de las dos instancias **se ve** hoy, y **no se resuelve** hoy: la solución es un
candado distribuido, y un candado necesita un sitio compartido donde ponerlo — una tabla, un
Redis. Meter una base de datos aquí para resolverlo convertiría el lab en otro lab.

Así que el paso 5 **enseña a reconocer el problema y nombra la solución**, sin implementarla. Es
deliberado, y está dicho también en el guion: es la clase de fallo que en producción se descubre
cuando ya duplicó los totales de un mes.

## Lo que no vimos hoy

- **Candado distribuido de verdad** (una tabla con expiración, ShedLock, Redis): la solución al
  paso 5.
- **Colas de mensajes** (RabbitMQ, Kafka): cuando el trabajo asíncrono tiene que sobrevivir a que
  el proceso se caiga. `@Async` vive en memoria; si la aplicación muere, el aviso se pierde.
- **Planificadores distribuidos** (Quartz en clúster, el cron del orquestador): sacar la
  programación fuera de la aplicación.

## El guion

`PASOS.md` — los cinco pasos de la sesión.
