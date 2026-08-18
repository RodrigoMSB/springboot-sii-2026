# Pasos · Lab 04 · JPA

Diez pasos. Se construyen en `practica/`, en vivo, uno a la vez. Después de cada paso se corre
el programa y se mira la consola antes de seguir.

```bash
cd practica
./mvnw spring-boot:run
```

**La aplicación se queda corriendo** después de imprimir las demos: se apaga con **Ctrl+C**. Es
así a propósito — con el programa vivo se puede mirar la base con un cliente SQL (paso 9) y
llamar a los endpoints (paso 10).

Los pasos 1 y 2 crean dos archivos. Los pasos 3 a 8 llenan los métodos de `DemosJpa` y
descomentan su llamada en `Lab04Application`. Los pasos 4 y 8 llevan dos demos cada uno porque
son pares que se explican juntos: los ocho métodos quedan cubiertos. Los pasos 9 y 10 no escriben
demos: comprueban que lo guardado quedó, primero apagando el programa y después por HTTP.

---

## Paso 1 · La entidad

**Se explica:** una clase Java y una tabla pueden ser la misma cosa. Las anotaciones son el mapa
que las une, y con ese mapa Hibernate escribe el SQL de todo lo demás.

**Se escribe:** `practica/src/main/java/cl/dgt/jpa/entities/Observacion.java`

Una clase con cuatro campos —`id` (Long), `texto` (String), `autor` (String), `fecha`
(LocalDate)— y estas anotaciones:

- `@Entity` y `@Table(name = "observacion")` sobre la clase.
- `@Id` y `@GeneratedValue(strategy = GenerationType.IDENTITY)` sobre `id`.
- `@Column(nullable = false, ...)` en los otros tres.
- Constructor sin argumentos `protected` (lo exige JPA), constructor público con los tres
  valores, getters, y un `toString()` para poder imprimirla.

La tabla ya existe: está en `db/migration/V1__observacion.sql`. Vale la pena abrirla y poner las
dos cosas lado a lado.

**Se descomenta:** nada todavía.

**En consola:** el programa arranca y termina sin imprimir demos. Lo que importa es que
**arranque**: `spring.jpa.hibernate.ddl-auto: validate` compara tu clase con la tabla, así que
si te falta una columna o le pusiste otro nombre, falla aquí y lo dice.

---

## Paso 2 · El repositorio

**Se explica:** no hace falta escribir la clase que guarda y busca. Se declara una interfaz y
Spring Data genera la implementación al arrancar.

**Se escribe:** `practica/src/main/java/cl/dgt/jpa/repositories/ObservacionRepository.java`

```java
public interface ObservacionRepository extends JpaRepository<Observacion, Long> {
}
```

Eso solo ya trae `save`, `findById`, `findAll`, `deleteById` y `count`. Los métodos propios
llegan en el paso 5.

**Se descomenta:** nada todavía.

**En consola:** otra vez, arranca y termina. Sin errores.

---

## Paso 3 · Guardar

**Se explica:** `save` toma un objeto sin id, lo inserta, y le escribe el id que generó la base.
El objeto entra sin id y sale con id.

**Se escribe:** el cuerpo de `guardar()` en `DemosJpa`. Antes hay que **recibir el repositorio**:
un campo `private final ObservacionRepository repositorio;` y un constructor que lo reciba.

Crear una `Observacion`, imprimir su id (será `null`), guardarla, e imprimir el id de vuelta.
Guardar dos más, que harán falta después. Y guardar el id de la primera en un campo
(`private Long primerId;`): los pasos 4 y 7 lo usan.

Los tres valores, para que la consola coincida con la de `solucion/`:

| texto | autor | fecha |
|---|---|---|
| `Revisión anual sin hallazgos.` | `Carolina` | `2026-03-10` |
| `Solicita certificado de situación.` | `Carolina` | `2026-08-01` |
| `Diferencias en el F29 de julio.` | `Ignacio` | `2026-07-15` |

Dos del mismo autor con fechas distintas y una de otro: eso es lo que hace interesantes los
pasos 5 y 6.

**Se descomenta:** `demos.guardar();`

**En consola:**

```
=== 1 · GUARDAR · save() ===
  antes de guardar -> id = null
Hibernate:
    insert
    into
        observacion
        (autor, fecha, texto)
    values
        (?, ?, ?)
  después de guardar -> id = 1
```

Dos cosas para señalar: el `id` pasó de `null` a `1`, y el `INSERT` **no menciona la columna
`id`** — la pone la base.

---

## Paso 4 · Buscar por id, y listar

**Se explica:** `findById` devuelve `Optional` porque preguntar por algo que no está es normal,
no es un error. `findAll` trae la tabla entera, que es cómodo y peligroso a la vez.

**Se escribe:** los cuerpos de `buscarPorId()` y `listarTodas()`.

En el primero, buscar el id guardado en el paso 3 y también un id que no existe (9999), para ver
las dos caras del `Optional`. En el segundo, `findAll()` y recorrer imprimiendo.

**Se descomenta:** `demos.buscarPorId();` y `demos.listarTodas();`

**En consola:**

```
=== 2 · BUSCAR POR ID · findById() ===
Hibernate:
    select ... from observacion o1_0 where o1_0.id=?
  id 1 -> Observacion{id=1, texto='...', autor='Carolina', fecha=2026-03-10}
  id 9999 -> no existe

=== 3 · LISTAR TODAS · findAll() ===
  3 observaciones:
    Observacion{id=1, ...}
```

El `select` de `findAll` **no tiene `where`**. Ahí conviene preguntar en voz alta qué pasaría con
esa línea si la tabla tuviera 500.000 filas.

---

## Paso 5 · Buscar por autor

**Se explica:** aquí está la idea que sorprende. Se declara un método en la interfaz y **el
nombre del método es la consulta**: Spring Data lo lee, comprueba contra la entidad que existe
una propiedad `autor`, y genera el SQL.

**Se escribe:** en `ObservacionRepository`, la línea

```java
List<Observacion> findByAutor(String autor);
```

y el cuerpo de `buscarPorAutor()`, que la llama e imprime lo que vuelve.

**Se descomenta:** `demos.buscarPorAutor();`

**En consola:**

```
=== 4 · BUSCAR POR AUTOR · findByAutor() ===
Hibernate:
    select ... from observacion o1_0 where o1_0.autor=?
  autor = Carolina -> 2
```

Buen momento para el experimento: cambiar el nombre a `findByAutorr` y volver a arrancar. **La
aplicación no arranca**, y el error dice qué propiedad no encontró. Es un error de arranque, no
de producción — el mejor error posible.

---

## Paso 6 · Dos condiciones

**Se explica:** el vocabulario de los nombres da para bastante más: `And`, `Or`, `After`,
`Before`, `Between`, `LessThan`, `OrderBy`.

**Se escribe:** en el repositorio,

```java
List<Observacion> findByAutorAndFechaAfter(String autor, LocalDate fecha);
```

y el cuerpo de `buscarConDosCondiciones()`, con `LocalDate.of(2026, 6, 1)` como fecha de corte:
queda entre las dos observaciones de Carolina, así que solo debe volver una.

**Se descomenta:** `demos.buscarConDosCondiciones();`

**En consola:**

```
=== 5 · DOS CONDICIONES · findByAutorAndFechaAfter() ===
Hibernate:
    select ... where o1_0.autor=? and o1_0.fecha>?
  autor = Carolina y fecha > 2026-06-01 -> 1
```

Y la pregunta que abre el tema siguiente: ¿hasta qué largo de nombre sigue siendo esto legible?
Cuando deja de leerse, se escribe la consulta a mano — pero eso es del Lab 05.

---

## Paso 7 · Actualizar sin `save()`

**Se explica:** este es el momento raro del laboratorio, y conviene decirlo antes: vamos a
cambiar un dato en la base **sin llamar a `save`**. Dentro de una transacción, el objeto que
cargaste queda vigilado; al cerrar, Hibernate compara y lanza el `UPDATE` solo.

**Se escribe:** el cuerpo de `actualizar()`, con `@Transactional` sobre el método. Cargar la
observación del paso 3 por su id, imprimir su texto, cambiarlo, imprimirlo otra vez… y no llamar
a `save`.

Hace falta un setter en la entidad —`setTexto`— que hasta ahora no existía: es la primera vez que
se modifica una observación. El texto nuevo, para que la consola coincida:
`Revisión anual: se detecta diferencia menor.`

**Se descomenta:** `demos.actualizar();`

**En consola:**

```
=== 6 · ACTUALIZAR SIN save() · dirty checking ===
  antes:  Revisión anual sin hallazgos.
  después: Revisión anual: se detecta diferencia menor.
  NO llamamos a save(). El UPDATE aparece justo aquí abajo,
  cuando esta transacción se cierre:
Hibernate:
    update
        observacion
    set
        autor=?,
        fecha=?,
        texto=?
    where
        id=?
```

**Ahí hay que parar.** El `UPDATE` apareció después del último `println`, cuando el método
terminó y la transacción se cerró. Nadie lo pidió. Se llama *dirty checking*, y es la respuesta
a por qué en un servicio real casi nunca se ve un `save` en las modificaciones.

Y de vuelta: entonces, ¿para qué sirve `save`? Para lo del paso 3 — objetos **nuevos**.

---

## Paso 8 · Borrar y contar

**Se explica:** `deleteById` borra, y se cuenta antes y después para no creerle al método.
`count()` y `findAll().size()` responden lo mismo de dos maneras muy distintas.

**Se escribe:** los cuerpos de `borrar()` y `contar()`. En el primero, contar, borrar por id,
contar otra vez. En el segundo, imprimir `count()`, `findAll().size()`, y de paso
`countByAutor("Carolina")` — que hay que declarar en el repositorio:

```java
long countByAutor(String autor);
```

**Se descomenta:** `demos.borrar();` y `demos.contar();`

**En consola:**

```
=== 7 · BORRAR · deleteById() ===
  filas antes:  3
Hibernate:
    select ... where o1_0.id=?
Hibernate:
    delete from observacion where id=?
  filas después: 2

=== 8 · CONTAR · count() vs findAll().size() ===
Hibernate:
    select count(*) from observacion o1_0
  count()          -> 2
  findAll().size() -> 2
  countByAutor("Carolina") -> 1
```

Y de paso: `countByAutor("Carolina")` da **1**, no 2 — la que se borró en el paso anterior era
de ella. Vale la pena que alguien lo note antes de que lo diga el instructor.

Dos cosas para cerrar. En el borrado, Hibernate hace un `SELECT` **antes** del `DELETE`: necesita
el objeto para saber qué está borrando. Y en el conteo, las dos líneas dan `2`, pero el `count()`
le preguntó a la base y el `findAll().size()` se trajo las filas enteras a memoria para
contarlas. Con dos filas da igual; con quinientas mil, no.

---

## Paso 9 · La prueba de que quedó guardado

**Se explica:** hasta aquí las observaciones se vieron en la consola, que es la memoria del
programa. Ahora se apaga todo y se vuelve a levantar: si siguen ahí, quedaron en disco de verdad.

**Se hace:**

1. Con el programa corriendo, **Ctrl+C**.
2. En `Lab04Application`, comentar todas las llamadas **menos** `demos.listarTodas();`.
3. Volver a arrancar.

**En consola:**

```
=== 3 · LISTAR TODAS · findAll() ===
  3 observaciones:
    Observacion{id=2, texto='Solicita certificado de situación.', autor='Carolina', ...}
    Observacion{id=3, texto='Diferencias en el F29 de julio.', autor='Ignacio', ...}
```

Las mismas filas, sin haber vuelto a guardar nada. Y arriba, Flyway lo confirma a su manera:

```
Schema "public" is up to date. No migration necessary.
```

La base vive en `.datos-pg/`, dentro del proyecto. Si alguna vez se quiere empezar de cero,
se borra ese directorio y el siguiente arranque lo crea otra vez.

> Y de paso, con el programa corriendo: abrir DBeaver o pgAdmin contra `localhost:55432`
> (usuario y clave `postgres`) y hacer `select * from observacion`. Ver el objeto en la consola
> es ver la memoria; ver la fila en la tabla es ver la persistencia. Los datos de conexión
> completos están en el README.

Antes de seguir al paso 10, volver a descomentar las ocho llamadas.

---

## Paso 10 · Lo mismo, por HTTP

**Se explica:** el repositorio no es solo para las demos. Un controller llama exactamente a los
mismos métodos, y así se ve desde fuera lo que hasta ahora se veía en la consola.

**Se escribe:** los tres métodos de `web/ObservacionController.java`, que llega declarado y
vacío. Igual que en `DemosJpa`, primero hay que recibir el repositorio por constructor. Y hay que
cambiar los tipos comodín (`List<?>`, `ResponseEntity<?>`, `Map`) por `Observacion`: están así
para que el proyecto compilara antes de que la entidad existiera.

- `listar(autor)` → `findAll()`, o `findByAutor(autor)` si viene el parámetro.
- `porId(id)` → `findById(id)`, y **404** si no está. Ahí se ve para qué servía el `Optional`.
- `crear(nueva)` → `save(nueva)` y responder **201** con lo creado.

**Se prueba** (Postman, o estos `curl`; en `practica/` el puerto es 8099):

```bash
curl localhost:8099/api/observaciones
curl "localhost:8099/api/observaciones?autor=Carolina"
curl localhost:8099/api/observaciones/2
curl -i localhost:8099/api/observaciones/9999

curl -i -X POST localhost:8099/api/observaciones \
  -H 'Content-Type: application/json' \
  -d '{"texto":"Creada desde Postman.","autor":"Rodrigo","fecha":"2026-08-15"}'
```

**Lo que debe verse:**

```
[{"texto":"Solicita certificado de situación.","autor":"Carolina","fecha":"2026-08-01","id":2}, …]

HTTP/1.1 404                      ← el id 9999 no existe
HTTP/1.1 201                      ← y el POST devuelve la creada, ya con su id
{"texto":"Creada desde Postman.","autor":"Rodrigo","fecha":"2026-08-15","id":4}
```

**Y el cierre del laboratorio:** después del POST, Ctrl+C, volver a arrancar, y pedir otra vez
`GET /api/observaciones`. La observación creada desde Postman sigue ahí. Se guardó por HTTP, se
apagó el programa entero, y el dato sobrevivió.

---

## Al terminar

`practica/` imprime exactamente lo mismo que `solucion/`. Si algo no cuadra, `solucion/` está
ahí para comparar archivo por archivo.

### Lo que siembra este lab

Hoy quedaron dos preguntas sin responder, y las dos son el Lab 05.

La primera salió en el paso 4: `findAll` no lleva `where`, y con quinientas mil filas eso deja
de ser gratis. La segunda es más honda y todavía no se ha visto: **¿qué pasa cuando una entidad
apunta a otra?** Pedir una observación podría traer también a su autor, y con él lo suyo, y así
hacia abajo.

> **¿Y si trae de más?** — Traer de más no da error. Da lentitud. Y la lentitud no aparece con
> tres filas de prueba: aparece en producción.

El Lab 05 convierte esa pregunta en un número: un contador de consultas y un presupuesto que no
se puede cruzar.
