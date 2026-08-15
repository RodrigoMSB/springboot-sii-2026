# Lab 3.5 · JPA

Guardar objetos Java en una base de datos y recuperarlos, sin escribir SQL.

Es la pieza que faltaba: vienes usando entidades desde el Lab 01 sin que nadie te haya dicho
qué son. Hoy las construyes tú, un paso a la vez, junto al instructor.

## Qué se aprende

- Que una clase Java y una tabla pueden ser **la misma cosa**, y cómo se declara eso.
- Guardar (`save`), buscar por id, listar, y buscar por criterios — sin una línea de SQL.
- Que el **nombre de un método** puede ser la consulta.
- Que un objeto cargado dentro de una transacción se actualiza solo, sin llamar a `save`.
- A **leer el SQL** que Hibernate escribe por ti. Sale en la consola, en cada demo.

## Los dos directorios

| | |
|---|---|
| **`practica/`** | Donde trabajas. Está incompleto a propósito: faltan la entidad, el repositorio y el cuerpo de las ocho demos. |
| **`solucion/`** | El mismo proyecto, terminado. Para comparar cuando algo no salga, o para mirarlo después con calma. |

Los dos son proyectos completos y arrancan solos.

## Cómo se corre

```bash
cd practica          # o solucion
./mvnw spring-boot:run
```

No hace falta instalar nada: PostgreSQL viaja dentro del proyecto y arranca como parte del
programa. El programa corre las demos, imprime, y termina.

En `practica/` las ocho demos están **comentadas** en `Lab35Application`. Cada paso descomenta
la suya, así que el programa crece contigo: si algo se rompe, sabes qué línea lo rompió.

## El SQL sale en la consola

Está encendido en `application.yml` (`show-sql: true`), y es lo más importante que vas a mirar
hoy:

```
Hibernate:
    insert
    into
        observacion
        (autor, fecha, texto)
    values
        (?, ?, ?)
```

Ese `INSERT` no lo escribió nadie. Salió de la clase que escribiste tú.

## El guion

`PASOS.md` — los ocho pasos de la sesión, con qué escribir en cada uno y qué debe aparecer en
la consola.
