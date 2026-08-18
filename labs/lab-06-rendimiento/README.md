# Lab 06 · Rendimiento — el N+1

Por qué la pantalla tarda, dicho con un número.

El Lab 05 terminó con una pregunta: si traer un trámite dispara un SELECT extra por su
contribuyente, ¿qué pasa con doscientos? Hoy se responde midiendo — y después se arregla tres
veces, de tres formas distintas.

## Qué se aprende

- A **medir antes de optimizar**. Un contador de consultas en pantalla convierte «va lento» en
  un dato.
- Qué es el **N+1**: una consulta para la lista, y una más por cada elemento.
- Tres formas de arreglarlo — **`JOIN FETCH`**, **`@EntityGraph`** y **proyección a un DTO**— y
  cuándo se prefiere cada una.
- Que **el arreglo va en la consulta, no en el mapeo**. Y por qué poner `EAGER` no solo no lo
  arregla: lo empeora.

## Los datos

La base se siembra sola al arrancar: **200 contribuyentes con 5 trámites cada uno** — 1.000
trámites. Ese tamaño es a propósito. Con tres filas el N+1 no se nota, y ese es justamente el
motivo por el que llega a producción.

Solo se siembra si la base está vacía, así que los datos **persisten entre arranques** y las
mediciones se repiten iguales.

## Los dos directorios

| | |
|---|---|
| **`practica/`** | Donde trabajas. Las entidades, el cargador de datos y el contador vienen **dados**: hoy no se escriben entidades. Faltan los métodos del repositorio y el cuerpo de las cinco demos. |
| **`solucion/`** | El mismo proyecto, terminado. |

## Cómo se corre

```bash
cd practica          # o solucion
./mvnw spring-boot:run
```

**Se queda corriendo. Se apaga con Ctrl+C.**

| | HTTP | Postgres |
|---|---|---|
| `practica/` | 8089 | **55436** |
| `solucion/` | 8090 | **55437** |

En `practica/` las cinco demos están **comentadas** en `Lab06Application`. Cada paso descomenta la
suya.

## Hoy el SQL está apagado

En `application.yml`, `show-sql: false`. No es un olvido: la demo 1 dispara **201 consultas**, y
con el SQL encendido serían mil líneas por pantalla y el número se perdería entre ellas.

Leer el SQL fue el Lab 05. **Hoy se cuenta.** Lo que sí está encendido es
`hibernate.generate_statistics`, que es de donde sale el contador.

## Los números del laboratorio

Todos medidos sobre estos 200 contribuyentes y 1.000 trámites. La pantalla que se arma es la
misma en las cinco demos:

| | consultas | tiempo |
|---|---|---|
| **1 · el crimen** (`findAll` + tocar la relación) | **201** | 79 ms |
| **2 · `JOIN FETCH`** | **1** | 19 ms |
| **3 · `@EntityGraph`** | **1** | 20 ms |
| **4 · proyección a un `record`** | **1** | 12 ms |
| 5 · una pantalla que no usa los trámites | 1 | 2 ms |

Y la trampa del paso 5, poner `EAGER` en la entidad, medida en las mismas dos pantallas:

| | LAZY | EAGER |
|---|---|---|
| demo 1 (el crimen) | 201 · 79 ms | **201 · 145 ms** |
| demo 5 (no pide trámites) | 1 · 2 ms | **201 · 58 ms** |

Léase despacio: **`EAGER` no arregla el N+1** —la demo 1 sigue en 201, y encima tarda casi el
doble— **y rompe la pantalla que estaba bien**, que pasa de 1 consulta a 201. Es estrictamente
peor. Ese es el paso 5 entero.

## El guion

`PASOS.md` — los cinco pasos de la sesión, con qué escribir en cada uno y qué número debe
aparecer.
