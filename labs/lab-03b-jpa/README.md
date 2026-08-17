# Lab 3b · JPA

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
programa.

El programa corre las demos, imprime… y **se queda corriendo**. Es a propósito: con la
aplicación viva puedes mirar la base con un cliente SQL y llamar a los endpoints desde Postman.
**Se apaga con Ctrl+C.**

Y la base **no se borra al apagar**. Lo que guardaste sigue ahí la próxima vez que arranques:
vive en `.datos-pg/`, dentro del propio proyecto. Eso es lo que hace que este lab se llame
guardar y recuperar y no solo guardar.

En `practica/` las ocho demos están **comentadas** en `Lab03bApplication`. Cada paso descomenta
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

## Mirar la base por fuera

Con el programa corriendo, conéctate con DBeaver, pgAdmin o el cliente SQL que uses:

| | `practica/` | `solucion/` |
|---|---|---|
| host | `localhost` | `localhost` |
| puerto | **55432** | **55433** |
| base de datos | `postgres` | `postgres` |
| usuario | `postgres` | `postgres` |
| contraseña | `postgres` | `postgres` |

Cada proyecto tiene su propia base y su propio puerto, así que **pueden correr los dos a la vez**
sin pisarse (sus puertos HTTP también son distintos: 8099 y 8100).

Una vez conectado:

```sql
select * from observacion;
```

Deberías ver las mismas filas que imprimió la consola. Y ahí está la diferencia que enseña este
laboratorio: **ver el objeto en la consola es ver la memoria; ver la fila en la tabla es ver la
persistencia.**

## Los endpoints

Con la aplicación corriendo, lo mismo que viste en consola, ahora por HTTP:

```
GET  /api/observaciones                 todas
GET  /api/observaciones?autor=Carolina  filtradas por autor
GET  /api/observaciones/{id}            una (404 si no está)
POST /api/observaciones                 crea una, responde 201
```

En `solucion/` van en el puerto 8100; en `practica/`, en el 8099.

## El guion

`PASOS.md` — los diez pasos de la sesión, con qué escribir en cada uno y qué debe aparecer en
la consola.
