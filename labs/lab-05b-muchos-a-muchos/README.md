# Lab 05b · Muchos a muchos

La tercera forma de relación, y la tabla que aparece sin que nadie la escriba.

En el Lab 05 la relación era **una columna**: `tramite.contribuyente_id`. Hoy no cabe en una
columna. Un trámite pide **varios documentos** —cédula, escritura, poder— y el mismo tipo de
documento sirve para **varios trámites**. Eso necesita una tabla aparte, y JPA la crea, la llena y
la vacía sin que exista una clase para ella.

Lo que se aprende no es la anotación —son dos líneas—, sino **qué le pasa a esa tabla intermedia
con cada cosa que se hace en Java**. Y hay un número: el mismo cambio cuesta **1 sentencia con
`Set` y 6 con `List`**.

## Qué se aprende

- Que un muchos-a-muchos en la base es **una tabla de pares**, y en Java se declara con
  `@ManyToMany` y `@JoinTable`. Nadie escribe esa tabla, y nadie escribe sus `insert`.
- Que **agregar y quitar un documento** de la colección de Java es un `insert` y un `delete` en la
  intermedia, y que quitar un documento **no borra el documento**.
- Que `@ManyToMany(mappedBy = ...)` es el **lado espejo**, exactamente igual que el `@OneToMany`
  del Lab 05: no guarda nada, sirve para navegar.
- **Por qué la colección va en `Set` y no en `List`**, contando las sentencias que cuesta el mismo
  cambio con cada una. Es el paso del laboratorio.
- Que el nombre de un método de repositorio puede **atravesar la tabla intermedia**, y Spring
  escribe los dos `join`.
- **Cuándo `@ManyToMany` deja de servir** y en qué se convierte: en cuanto la relación necesita un
  dato propio, pasa a ser una entidad con dos `@ManyToOne`.

## El modelo

Dos clases. Y una tabla que no es ninguna de las dos.

```
Tramite                     tramite_documento                  Documento
  id                          tramite_id    ──┐                  id
  tipo                        documento_id  ──┼──               codigo
  rut                         (PK: las dos)   │                 nombre
  fecha                                       │                 tramites  ←── mappedBy
  documentos  ── @JoinTable ──────────────────┘
```

El lado con `@JoinTable` es **el dueño**: es el que escribe en la tabla del medio. El otro es un
espejo.

**La tabla del medio no tiene clase, no tiene `@Entity` y no tiene repositorio.** Existe, tiene
filas y se puede consultar con SQL — pero en Java solo existe como el nombre dentro de una
anotación.

## Los dos directorios

| | |
|---|---|
| **`practica/`** | Donde trabajas. `Documento` viene dado sin su lado espejo; a `Tramite` le falta la relación entera, y las seis demos llegan vacías. Los dos instrumentos de `soporte/` —el contador y el mirador de la intermedia— vienen hechos: son el arnés, no el ejercicio. |
| **`solucion/`** | El mismo proyecto, terminado. |

Los dos son proyectos completos y arrancan solos.

> **Este lab va entre el 05 y el 06, y por eso se llama `05b`.** Nació después, de una pregunta en
> clase dictando el 05. No se renumeró nada: el Lab 06 sigue siendo el Lab 06.

## Cómo se corre

```bash
cd practica          # o solucion
./mvnw spring-boot:run
```

No hace falta instalar nada: PostgreSQL viaja dentro del proyecto, como en el Lab 04 y el 05.

El programa corre las demos, imprime… y **se queda corriendo**. **Se apaga con Ctrl+C.** La base
**no se borra al apagar**: vive en `.datos-pg/`, dentro del propio proyecto.

En `practica/` el `CommandLineRunner` de `Lab05bApplication` llega **vacío**. Cada paso agrega su
llamada —el guion trae la línea exacta—.

## Mirar la base por fuera

Con el programa corriendo, desde DBeaver, pgAdmin o el cliente SQL que uses:

| | `practica/` | `solucion/` |
|---|---|---|
| host | `localhost` | `localhost` |
| puerto | **55447** | **55448** |
| base / usuario / contraseña | `postgres` | `postgres` |

Sus puertos HTTP también son distintos (**8110** y **8111**), así que pueden correr los dos a la
vez.

```sql
select t.tipo, d.codigo
from tramite t
join tramite_documento td on td.tramite_id = t.id
join documento d          on d.id          = td.documento_id
order by t.id, d.id;
```

**Dos `join` para una consulta.** Ésa es la diferencia con el Lab 05, donde bastaba uno: en el
medio hay una tabla más.

## El número del laboratorio

El paso 4 se mide, no se cuenta de memoria. **El mismo cambio** —adjuntar un documento a un
trámite que ya lleva cuatro, y después quitarlo— con los dos tipos de colección:

| | adjuntar 1 documento | quitar 1 documento |
|---|---|---|
| `Set<Documento>` | **1** sentencia | **1** sentencia |
| `List<Documento>` | **6** sentencias | **5** sentencias |

Con `List`, Hibernate emite `delete from tramite_documento where tramite_id=?` —**sin**
`documento_id`, o sea la relación entera del trámite— y después la reinserta completa. Con `Set`,
cada cambio es la sentencia que le toca.

Con cuatro documentos son cinco sentencias de más. Con treinta son treinta.

## El guion

`PASOS.md` — los seis pasos de la sesión, con qué escribir en cada uno y qué debe aparecer en la
consola.
