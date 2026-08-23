# Lab 05 · Relaciones

Dos tablas que se apuntan, declaradas en la clase. Y lo que cuesta traerlas.

En el Lab 04 una clase era una tabla. Hoy son dos, y hay una flecha entre ellas: un **trámite**
pertenece a un **contribuyente**. Lo que se aprende no es la anotación —son dos líneas—, sino
**cuántos SELECT dispara cada forma de escribirla**.

## Qué se aprende

- Que la relación se declara con `@ManyToOne` y **vive en la tabla que tiene la columna**. Quien
  tiene la clave foránea es quien manda.
- Que `@OneToMany(mappedBy = ...)` es el **lado espejo**: no guarda nada, sirve para navegar.
- Qué es **LAZY**: la relación no viaja con el objeto; se va a buscar cuando la tocas, y eso es
  un SELECT más que se ve en la consola.
- Por qué **EAGER se paga aunque no lo uses**, contando los SELECT antes y después.
- Qué es la **`LazyInitializationException`** — el error que todo el mundo se encuentra— y por
  qué no es un fallo de JPA.
- Que el nombre de un método de repositorio puede **cruzar la relación** y Spring escribe el JOIN.

## El modelo

Dos clases. Nada más.

```
Contribuyente                        Tramite
  id                                   id
  rut                                  tipo
  razonSocial                          estado
  tramites  ←── mappedBy ────┐         fecha
                             └──────   contribuyente  ── @JoinColumn(contribuyente_id)
```

La flecha de la derecha es la que existe en la base. La de la izquierda es un espejo.

## Los dos directorios

| | |
|---|---|
| **`practica/`** | Donde trabajas. `Contribuyente` viene dado (es repaso del Lab 04); a `Tramite` le falta la relación, y las seis demos llegan vacías. |
| **`solucion/`** | El mismo proyecto, terminado. |

Los dos son proyectos completos y arrancan solos.

> **`entities/` y `models/` no son lo mismo, y por eso no se llaman igual.** Cada clase de
> `entities/` está **mapeada a una tabla**: lo que se le hace al objeto termina en la base. Los
> `models/` de los labs 02, 03 y 08 son lo contrario — objetos que viven en memoria, sin tabla
> detrás. El nombre distinto es deliberado: dice de un vistazo si hay una fila al otro lado.

## Cómo se corre

```bash
cd practica          # o solucion
./mvnw spring-boot:run
```

No hace falta instalar nada: PostgreSQL viaja dentro del proyecto, como en el Lab 04.

El programa corre las demos, imprime… y **se queda corriendo**. **Se apaga con Ctrl+C.** La base
**no se borra al apagar**: vive en `.datos-pg/`, dentro del propio proyecto.

En `practica/` el `CommandLineRunner` de `Lab05Application` llega **vacío**. Cada paso agrega su llamada —el guion trae la línea exacta—.

## Mirar la base por fuera

Con el programa corriendo, desde DBeaver, pgAdmin o el cliente SQL que uses:

| | `practica/` | `solucion/` |
|---|---|---|
| host | `localhost` | `localhost` |
| puerto | **55434** | **55435** |
| base / usuario / contraseña | `postgres` | `postgres` |

Sus puertos HTTP también son distintos (**8087** y **8088**), así que pueden correr los dos a la
vez.

```sql
select t.id, t.tipo, c.razon_social
from tramite t join contribuyente c on c.id = t.contribuyente_id;
```

Esa columna `contribuyente_id` es la relación. No hay una tercera tabla: **una relación
uno-a-muchos es una columna**.

## El número del laboratorio

El paso 4 se mide, no se cuenta. Trayendo los 6 trámites **sin tocar** su contribuyente:

| | SELECT |
|---|---|
| `@ManyToOne(fetch = FetchType.LAZY)` | **1** |
| `@ManyToOne(fetch = FetchType.EAGER)` | **4** |

Tres SELECT de más por no pedirle nada a nadie. Con 6 trámites da igual; el Lab 06 lo hace con
mil.

## El guion

`PASOS.md` — los seis pasos de la sesión, con qué escribir en cada uno y qué debe aparecer en la
consola.
