# Teoría · Módulo 5 (primera parte)

## Índice

1. [Mapear una relación: quién es el dueño](#1-mapear-una-relación-quién-es-el-dueño)
2. [Unidireccional vs bidireccional](#2-unidireccional-vs-bidireccional)
3. [La tabla de defaults de fetch (proyéctala)](#3-la-tabla-de-defaults-de-fetch-proyéctala)
4. [Por qué LAZY, siempre, y declarado](#4-por-qué-lazy-siempre-y-declarado)
5. [Cascade y orphanRemoval](#5-cascade-y-orphanremoval)
6. [Repositorios y consultas derivadas](#6-repositorios-y-consultas-derivadas)
7. [JPQL vs SQL nativo](#7-jpql-vs-sql-nativo)
8. [`JdbcClient`: cuando el ORM sobra](#8-jdbcclient-cuando-el-orm-sobra)
9. [Tabla DO / DON'T · Glosario](#9-tabla-do--dont--glosario)
10. [El caveat honesto: LAZY no siempre es LAZY](#10-el-caveat-honesto-lazy-no-siempre-es-lazy)
11. [Conclusiones y siembra del Módulo 5](#11-conclusiones-y-siembra-del-módulo-5)

---

## 1. Mapear una relación: quién es el dueño

En una relación JPA, un lado es el **dueño** (tiene la clave foránea, la columna
`@JoinColumn`) y el otro es el **inverso** (`mappedBy`). El dueño decide qué se guarda en la
base; el inverso es un espejo para navegar.

En `Tramite`, el dueño de la relación con `Contribuyente` es `Tramite` (tiene
`contribuyente_id`). En `Contribuyente`, la lista `tramites` es el inverso (`mappedBy`).
Confundirlos hace que Hibernate guarde `null` en la FK o haga UPDATEs de más.

---

## 2. Unidireccional vs bidireccional

Una relación es **bidireccional** si puedes navegarla en los dos sentidos
(`tramite.getContribuyente()` y `contribuyente.getTramites()`). Es cómodo, y cuesta: hay que
mantener los dos lados sincronizados, y el inverso invita a cargar colecciones enteras sin
querer.

Regla práctica: haz bidireccional **solo** lo que de verdad navegas en ambos sentidos. Cada
lado que agregas es una puerta más por la que la base puede filtrarse a tu memoria.

---

## 3. La tabla de defaults de fetch (proyéctala)

Cuando no declaras `fetch`, JPA aplica un default. Y **el default es distinto por
anotación**:

| Anotación | Default si no declaras `fetch` |
|---|---|
| `@ManyToOne` | **EAGER** |
| `@OneToOne` | **EAGER** |
| `@OneToMany` | LAZY |
| `@ManyToMany` | LAZY |

Léela dos veces. Un `@ManyToOne` sin `fetch` carga a su dueño **siempre**, aunque no lo
pidas. Un `@OneToMany` sin `fetch`, no. Así que "quitar el fetch para que decida el
framework" no es neutral: es delegar a esta tabla, que casi nadie recuerda, y obtener
comportamientos opuestos según la anotación.

**El crimen del starter** era EAGER declarado. El "parche" de quitarlo es EAGER por default
para los `@ManyToOne` — el mismo muro, ahora invisible.

---

## 4. Por qué LAZY, siempre, y declarado

**LAZY:** la relación no se carga hasta que la tocas. Pides un trámite, recibes un trámite —
no su contribuyente, ni su F29, ni sus adjuntos. Si necesitas el contribuyente, lo accedes y
se carga entonces (una consulta más, cuando la decides tú).

**Declarado:** aunque el default coincida con lo que quieres, escríbelo. `fetch = LAZY`
explícito es una declaración de intención que se lee, se audita, y no depende de que quien te
lea recuerde la tabla del §3.

Hay un guardián, **AU-04**, que falla si una relación `@ManyToOne`/`@OneToOne` queda sin
`fetch` explícito. Hoy lo instalas tú.

> **DON'T:** confiar en el default de fetch. **DO:** declararlo aunque coincida.

---

## 5. Cascade y orphanRemoval

`cascade` propaga operaciones del dueño a lo dependiente. `Formulario29` con
`cascade = ALL` sobre sus `lineas`: guardas el F29, se guardan sus líneas; lo borras, se
borran. `orphanRemoval = true` va más lejos: si sacas una línea de la lista, se borra de la
base — porque una línea sin su F29 no tiene sentido.

Úsalo solo donde la vida de lo dependiente **pertenece** al dueño. Un `Tramite` no cascadea a
su `Contribuyente`: el contribuyente existe por su cuenta. (En este lab, el cascade correcto
viene ya escrito y comentado — es lectura, no tarea.)

---

## 6. Repositorios y consultas derivadas

Spring Data escribe consultas a partir del **nombre del método**:

```java
List<Tramite> findByContribuyenteRut(String rut);   // WHERE contribuyente.rut = ?
long countByEstado(EstadoTramite estado);            // SELECT count(*) WHERE estado = ?
boolean existsByContribuyenteRutAndEstado(...);      // SELECT exists ...
```

No escribes SQL: escribes una firma legible y Spring la traduce. Con `Pageable` pides
páginas. (La paginación profunda y las proyecciones son del Lab 05.)

---

## 7. JPQL vs SQL nativo

Cuando el nombre del método no alcanza, `@Query` con **JPQL**: consultas sobre *entidades*,
no sobre tablas. `SELECT t FROM Tramite t JOIN t.formulario29 f WHERE f.periodo = :periodo`.
Portátil entre motores, con parámetros nombrados.

`nativeQuery = true` te da SQL crudo cuando necesitas algo específico del motor. Menos
portable, más poder. Elígelo con criterio.

---

## 8. `JdbcClient`: cuando el ORM sobra

Un total por período —sumar columnas, agrupar— es una pregunta de SQL, no de objetos. Cargar
entidades para eso sería traer árboles que nadie va a navegar. `JdbcClient` (Spring 6.1+) lee
filas y las mapea a un `record`, directo:

```java
jdbc.sql("SELECT f.periodo, SUM(l.monto) AS total FROM ... GROUP BY f.periodo")
    .query(TotalPorPeriodo.class).list();
```

No todo lo que lee la base merece el peaje del ORM. Para escribir y navegar el dominio, JPA.
Para reportar, a veces, SQL directo.

---

## 9. Tabla DO / DON'T · Glosario

| ✅ DO | ❌ DON'T |
|---|---|
| `fetch = LAZY` explícito en cada relación | Confiar en el default (distinto por anotación) |
| Declarar el fetch aunque coincida | "Quitarlo para que decida el framework" |
| Bidireccional solo lo que navegas | Bidireccional por comodidad |
| `JdbcClient` para reportes agregados | Cargar entidades para sumar columnas |
| JPQL con parámetros nombrados | Concatenar strings en la consulta |

- **Dueño / inverso** — el lado con la FK / el `mappedBy`.
- **EAGER / LAZY** — cargar la relación siempre / al tocarla.
- **Cascade** — propagar operaciones del dueño a lo dependiente.
- **JPQL** — lenguaje de consulta sobre entidades (no tablas).
- **`JdbcClient`** — API fluida de Spring para SQL directo.

---

## 10. El caveat honesto: LAZY no siempre es LAZY

Una advertencia que otros cursos te ocultan: `fetch = LAZY` en un `@OneToOne` **del lado
inverso** (`mappedBy`) Hibernate suele **ignorarlo**. Para devolver `null` o un objeto,
Hibernate necesita saber si la fila existe — y eso ya requiere una consulta. Sin *bytecode
enhancement*, ese `@OneToOne` inverso se carga igual.

No te vendemos que "LAZY resuelve todo". LAZY resuelve los `@ManyToOne` y las colecciones,
que es la mayoría. El `@OneToOne` inverso tiene su letra chica, y saber que existe te ahorra
una tarde de confusión. (El curso no finge que un dialecto o una anotación es magia: eso se
desmonta con evidencia, no con fe.)

---

## 11. Conclusiones y siembra del Módulo 5

Hoy modelaste bien. Corregiste las relaciones a LAZY, instalaste AU-04, y aprendiste que "no
declarar" no es neutral: es delegar a una tabla que casi nadie recuerda.

Hiciste **lo correcto**.

🌱 **Siembra del Módulo 5 (segunda parte) — "Rendimiento, N+1 y cómo se mide".**

Y aquí está lo que no ves venir. Ese mismo `LAZY` que declaraste —lo correcto, lo que el
guardián exige— tiene un precio, y todavía no lo has pagado.

La próxima semana, un listado inocente va a recorrer una lista de trámites y, por cada uno,
va a tocar su contribuyente. Con LAZY, cada acceso es **una consulta más**. Con tres trámites
de prueba, nadie lo nota. Con **cincuenta mil lotes**, ese listado va a tardar **once
segundos**, y va a disparar mil ochocientas consultas para responder una página.

Lo correcto también se mide. LAZY no es gratis; EAGER tampoco. La ingeniería no es elegir el
default bueno — es saber **qué compras** con cada decisión, y **medirlo** antes de que un
usuario lo mida por ti.

El Módulo 5 se llama, en su segunda mitad, *«Once segundos»*. **Trae cronómetro.**
