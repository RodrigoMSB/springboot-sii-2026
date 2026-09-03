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
- Cómo se arregla con un **turno con nombre** de PostgreSQL (`pg_advisory_xact_lock`), y por qué
  `synchronized` no sirve.
- Que la **restricción en la base** es una segunda defensa, y por qué hacen falta las dos.

## La regla que hay que cumplir

Cabe en una línea:

> Dentro de un mismo año, no puede haber dos folios con el mismo número.

Todo el laboratorio consiste en descubrir lo difícil que es cumplirla.

## Los dos directorios

| | |
|---|---|
| **`practica/`** | Donde trabajas. Las entidades vienen dadas, y también el andamiaje que lanza los hilos y cuenta repetidos: hoy no se aprende a lanzar hilos, se aprende qué pasa cuando compiten. Falta el turno y el cuerpo de las tres demos. |
| **`solucion/`** | El mismo proyecto, terminado, con el turno y la restricción. |

> **`entities/` y `models/` no son lo mismo, y por eso no se llaman igual.** Cada clase de
> `entities/` está **mapeada a una tabla**: lo que se le hace al objeto termina en la base. Los
> `models/` de los labs 02, 03 y 08 son lo contrario — objetos que viven en memoria, sin tabla
> detrás. El nombre distinto es deliberado: dice de un vistazo si hay una fila al otro lado.

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

Emitiendo folios sobre el año 2026, que arranca con el folio `2026-0001`. En `practica/`, donde la
restricción única todavía no existe:

| | folios en la tabla | números distintos | repetidos |
|---|---|---|---|
| **1 · diez, de una en una** | 11 | 11 | **ninguno** |
| **2 · veinte a la vez, sin protección** | 21 | **9** | **8 números repetidos** |
| **3 · veinte a la vez, con turno** | 21 | **21** | **ninguno** |

La fila del medio es el laboratorio. Con el mismo método que funcionó perfecto en la primera fila:

```
REPETIDOS : [2026-0002 (x4), 2026-0003 (x3), 2026-0004 (x2), 2026-0005 (x2),
             2026-0006 (x2), 2026-0007 (x2), 2026-0008 (x2), 2026-0009 (x3)]
```

**`2026-0002` se emitió cuatro veces.** Cuatro contribuyentes con el mismo folio.

Los números exactos varían de una corrida a otra —es una carrera, y las carreras no se repiten
igual—, pero **siempre hay repetidos sin turno y nunca los hay con él**.

## En `solucion/`, la demo 2 se ve distinta — y no es un error

En `solucion/` la restricción única existe **desde el arranque**: la trae la migración V2, que en
`practica/` se escribe recién en el paso 5. Así que la demo 2 no llega a dejar repetidos en la
tabla. Los rechaza la base:

```
=== 2 · EL CRIMEN · 20 emisiones a la vez, sin protección ===
  folios en la tabla : 11
  números distintos  : 11
  REPETIDOS          : ninguno
  rechazados por la base : 10
  y los rechazó diciendo : ERROR: duplicate key value violates unique constraint "folio_anio_numero_unico"
```

**Diez de las veinte emisiones fallaron.** Ya no hay folios repetidos — ahora hay peticiones que
revientan. Es el mismo problema de debajo, con otro síntoma: la carrera sigue ocurriendo, y lo que
la evita es el turno del paso 4. La restricción sólo impide que el daño llegue a la tabla.

> Si lo que se quiere es **ver los repetidos en vivo**, hay que correr `practica/` sin haber hecho
> el paso 5 — o seguir el anexo destructivo del final del guion.

## El guion

`PASOS.md` — el paso 0 (la píldora de hilos y transacciones, ~15 min) y los cinco pasos de la
sesión.

## Lo que queda fuera

Tres defensas más para el mismo invariante, que se nombran en el cierre del guion y no se
implementan:

- **`@Lock(PESSIMISTIC_WRITE)`** sobre una fila, cuando lo que se protege **es** esa fila — el
  saldo de una cuenta, el stock de un producto. Aquí lo que se protege es un cálculo sobre toda la
  tabla, y no hay una fila natural que lo represente.
- **`@Version`** (bloqueo optimista), cuando los choques son raros: no hace esperar a nadie y el
  segundo que llega reintenta.
- **Secuencias** de PostgreSQL (`nextval`), cuando se toleran **huecos** en la numeración. Son
  atómicas y no hacen cola; su precio es que una transacción que aborta se lleva su número.
