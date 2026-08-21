# Lab 07 · Concurrencia

Dos peticiones, el mismo folio.

Todo lo que se ha hecho hasta aquí se ha probado **de uno en uno**: una demo detrás de otra, un
hilo, un usuario imaginario. Un sistema de verdad no funciona así. Hoy se emite un folio veinte
veces **a la vez** con un código que está bien escrito, y salen números repetidos.

## Qué se aprende

- Qué es un **hilo** y qué es una **transacción**, desde cero. Va en el paso 0 del guion, y no se
  da por sabido.
- Que un código **correcto en secuencia puede ser incorrecto en paralelo**, y que la diferencia no
  se ve leyéndolo.
- Qué es una **condición de carrera**, vista en una tabla con los duplicados marcados.
- Cómo se arregla con un **bloqueo pesimista** (`@Lock(PESSIMISTIC_WRITE)`), y por qué
  `synchronized` no sirve.
- Que la **restricción en la base** es una segunda defensa, y por qué hacen falta las dos.

## La regla que hay que cumplir

Cabe en una línea:

> Dentro de un mismo año, no puede haber dos folios con el mismo número.

Todo el laboratorio consiste en descubrir lo difícil que es cumplirla.

## Los dos directorios

| | |
|---|---|
| **`practica/`** | Donde trabajas. Las entidades vienen dadas, y también el andamiaje que lanza los hilos y cuenta repetidos: hoy no se aprende a lanzar hilos, se aprende qué pasa cuando compiten. Falta el candado y el cuerpo de las tres demos. |
| **`solucion/`** | El mismo proyecto, terminado, con el candado y la restricción. |

## Cómo se corre

```bash
cd practica          # o solucion
./mvnw spring-boot:run
```

**Se queda corriendo. Se apaga con Ctrl+C.**

| | HTTP | Postgres |
|---|---|---|
| `practica/` | 8091 | **55438** |
| `solucion/` | 8092 | **55439** |

En `practica/` el `CommandLineRunner` de `Lab07Application` llega **vacío**. Cada paso agrega su llamada —el guion trae la línea exacta—.

## Los números del laboratorio

Emitiendo folios sobre el año 2026, que arranca con el folio de apertura `2026-0001`:

| | folios en la tabla | números distintos | repetidos |
|---|---|---|---|
| **1 · diez, de una en una** | 11 | 11 | **ninguno** |
| **2 · veinte a la vez, sin candado** | 21 | **9** | **8 números repetidos** |
| **3 · veinte a la vez, con candado** | 21 | **21** | **ninguno** |

La fila del medio es el laboratorio. Con el mismo método que funcionó perfecto en la primera fila:

```
REPETIDOS : [2026-0002 (x4), 2026-0003 (x3), 2026-0004 (x2), 2026-0005 (x2),
             2026-0006 (x2), 2026-0007 (x2), 2026-0008 (x2), 2026-0009 (x3)]
```

**`2026-0002` se emitió cuatro veces.** Cuatro contribuyentes con el mismo folio.

Los números exactos varían de una corrida a otra —es una carrera, y las carreras no se repiten
igual—, pero **siempre hay repetidos sin candado y nunca los hay con él**. Se comprobó cuatro
veces seguidas antes de escribir esto.

## Y cuando existe la restricción

Después del paso 5, la base rechaza los duplicados y la demo 2 cambia de síntoma:

```
números distintos      : 11
REPETIDOS              : ninguno
rechazados por la base : 10
```

**Diez de las veinte emisiones fallaron.** Ya no hay folios repetidos — ahora hay peticiones que
revientan. Sigue estando el mismo problema debajo: el candado del paso 4 es lo que lo arregla. La
restricción solo impide que el daño llegue a la tabla.

## El guion

`PASOS.md` — el paso 0 (la píldora de hilos y transacciones, ~15 min) y los cinco pasos de la
sesión.
