# Para el sábado · Lab 3.5

*Lo que no cabe en tres horas. Se lee después, sin prisa y con el código al lado.*

---

## 1 · Qué hace Hibernate cuando tú no escribes SQL

Escribiste esto:

```java
observaciones.save(new ObservacionInterna(contribuyente, "Presenta dentro de plazo.", autor));
```

Y en la base apareció una fila. En el medio pasó lo siguiente, y conviene saberlo porque
explica casi todo lo demás:

1. **Hibernate leyó tu clase.** No en ese momento: al arrancar la aplicación. Vio `@Entity`,
   `@Table(name = "observacion_interna")`, `@Id`, cada `@Column` y el `@ManyToOne`, y construyó
   un **mapa** interno: qué propiedad va a qué columna, de qué tipo, y por dónde se une con
   `contribuyente`.
2. **Con ese mapa generó el SQL.** No lo tenía escrito de antes: lo armó a partir de tu
   declaración. Por eso el `INSERT` que ves en el log nombra exactamente tus columnas.
3. **Pasó los valores como parámetros.** Los `?` del log no son adorno: la instrucción viaja
   por un lado y los datos por otro.
4. **Te devolvió el `id`.** La columna es `BIGSERIAL`, la genera el motor, y Hibernate lo lee
   de vuelta y lo escribe en tu objeto. Por eso `guardada.getId()` ya no es `null`.

El trato, en una frase: **declaras la correspondencia una vez, y Hibernate escribe el SQL de
todas las operaciones, para siempre.**

---

## 2 · El mapa, anotación por anotación

```java
@Entity                                    // esta clase es una tabla
@Table(name = "observacion_interna")       // …esta
public class ObservacionInterna {

    @Id                                    // esta propiedad es la clave primaria
    @GeneratedValue(strategy = IDENTITY)   // …y la genera el motor (BIGSERIAL)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)     // muchas observaciones, un contribuyente
    @JoinColumn(name = "contribuyente_id") // por esta columna
    private Contribuyente contribuyente;

    @Column(nullable = false, length = 500)
    private String texto;

    @Column(name = "creada_en", nullable = false)   // el nombre Java ≠ el de la columna
    private LocalDateTime creadaEn;
}
```

Tres detalles que se preguntan siempre:

- **`@Column(name = ...)` solo hace falta cuando los nombres no coinciden.** `creadaEn` ≠
  `creada_en`, así que se declara. `texto` coincide y se anota igual, para declarar
  `nullable` y `length`: el mapeo también documenta.
- **El constructor sin argumentos es obligatorio** y por eso está `protected`. Hibernate
  necesita poder crear el objeto vacío antes de rellenarlo; nadie más debería usarlo.
- **`@GeneratedValue(IDENTITY)`** significa «el id lo pone la base». Hay otras estrategias
  (secuencias, tablas); ésta es la que corresponde a un `BIGSERIAL` de PostgreSQL.

---

## 3 · El ciclo de vida de una entidad

Un objeto que representa una fila no está siempre en el mismo estado, y casi todas las
sorpresas de JPA salen de ahí.

| Estado | Qué significa |
|---|---|
| **Transitorio** | Un objeto Java recién construido con `new`. La base no sabe que existe. |
| **Gestionado** | Hibernate lo está vigilando dentro de una sesión. Si le cambias un campo, se entera. |
| **Separado** | Existió en una sesión que ya se cerró. Sigue en memoria, pero nadie lo vigila. |
| **Eliminado** | Marcado para borrarse cuando la sesión se sincronice. |

`save()` toma un objeto **transitorio** y lo convierte en **gestionado**: le hace el `INSERT` y
lo deja bajo vigilancia mientras dure la transacción.

Lo importante de «gestionado»: si dentro de la misma transacción le cambias un campo a una
entidad que cargaste, **no hace falta llamar a `save`**. Hibernate compara el estado al cerrar
y lanza el `UPDATE` solo. Se llama *dirty checking*, y sorprende la primera vez.

Y de ahí sale la excepción que probablemente viste hoy:

> **`LazyInitializationException`** — pediste algo LAZY (el contribuyente) sobre un objeto que
> ya está **separado**, porque la sesión se cerró. No es un bug de Hibernate: es que le pediste
> a la base algo cuando ya no había línea abierta con la base.

Por eso el servicio lleva `@Transactional`. No es un conjuro: es «mantén la sesión abierta
mientras dure este método».

---

## 4 · La consulta derivada, y cuándo se queda corta

```java
List<ObservacionInterna> findByContribuyenteRut(String rut);
```

Spring Data parte el nombre en `findBy` + `Contribuyente` + `Rut`, comprueba contra tu entidad
que esas propiedades existen, sigue la relación y genera el `SELECT` con su `JOIN`.

Dos consecuencias que valen más de lo que parecen:

1. **Si el nombre no cuadra con la entidad, la aplicación no arranca.** No falla en producción
   un martes: falla al arrancar, diciendo qué propiedad no encontró.
2. **No hay dónde equivocarse escribiendo SQL**, porque no escribes SQL.

El vocabulario es amplio: `findBy...And...`, `...Or...`, `...Between`, `...LessThan`,
`...OrderBy...Desc`, `countBy...`, `existsBy...`, `deleteBy...`.

**¿Y cuándo se queda corta?** Cuando el nombre deja de leerse. Esto es real y es horrible:

```java
List<Tramite> findByContribuyenteRutAndEstadoAndFechaBetweenOrderByFechaDesc(...);
```

En ese punto se pasa a `@Query` con JPQL —una consulta escrita a mano, pero sobre *entidades*,
no sobre tablas— y el método recupera un nombre corto. El Lab 04 lo usa.

Regla práctica: **si el nombre del método no se puede leer en voz alta de un tirón, escribe la
consulta.**

---

## 5 · Cómo era esto antes de JPA

Una página, para que valores lo que te ahorra. Así se leía una tabla en 1998, y así siguen
corriendo millones de líneas:

```java
String sql = "SELECT c.rut, o.texto, o.autor, o.creada_en "
           + "FROM observacion_interna o "
           + "JOIN contribuyente c ON c.id = o.contribuyente_id "
           + "WHERE c.rut = '" + rut + "'";

Connection cx = dataSource.getConnection();
Statement st = cx.createStatement();
ResultSet rs = st.executeQuery(sql);
while (rs.next()) {
    resultado.add(new ObservacionInternaVista(
            rs.getString(1), rs.getString(2), rs.getString(3),
            rs.getTimestamp(4).toLocalDateTime()));
}
rs.close(); st.close(); cx.close();
```

Cuarenta líneas para responder *«¿qué observaciones tiene este contribuyente?»*, y de ellas
solo una —el `SELECT`— habla del negocio. El resto es plomería, y hay que repetirla en cada
consulta de la aplicación.

Cuatro problemas viven ahí dentro, y los cuatro desaparecen con lo que hiciste hoy:

| | El problema | Por qué desaparece |
|---|---|---|
| 1 | **El `rut` se pega al texto de la consulta.** Si alguien escribe un apóstrofe en ese campo, cierra la comilla y lo que siga deja de ser un dato: pasa a ser parte de la instrucción. Eso se llama **inyección SQL**. | Tú no escribes la consulta, así que no hay dónde concatenar. El RUT viaja como parámetro: es el `?` del log. |
| 2 | **Las columnas se leen por número de orden.** El día que alguien agregue una columna al `SELECT`, esto compila igual y devuelve el autor donde va el texto. | El mapeo es por nombre y está declarado una sola vez. |
| 3 | **Los `close()` están en el camino feliz.** Si algo lanza antes, la conexión se queda tomada. Unas cuantas fugas y la aplicación deja de responder. | No abres ni cierras nada: la sesión la gestiona Spring con `@Transactional`. |
| 4 | **El `catch (SQLException e) {}` se traga el error.** Base caída y «no tiene observaciones» se vuelven indistinguibles. | Las excepciones de JPA no son *checked*: si la base falla, sube. |

Sobre el (1), la corrección clásica es `PreparedStatement` con `?`, y **funciona**: mata la
inyección. Pero deja intactos el 2, el 3 y el 4, y las cuarenta líneas siguen ahí. Por eso el
camino del curso va al ORM.

> Y que quede dicho para el día que escribas SQL a propósito —a veces hay que hacerlo, el Lab
> 04 lo hace con `JdbcClient`—: **usa parámetros, siempre.** La lección del `?` no caduca.

---

## 6 · Lo que JPA no resuelve solo

El ORM quita la plomería. No quita la necesidad de saber qué se está trayendo.

**`FetchType`.** Tu `@ManyToOne` declara `LAZY`, y no por casualidad: el valor por defecto de
`@ManyToOne` es **`EAGER`**. Con EAGER, pedir una observación traería también al contribuyente
entero, aunque solo quisieras el texto. Y si el contribuyente trae sus trámites, y cada trámite
sus folios, una consulta inocente termina siendo veinte.

### La siembra del Lab 04

> **«¿Y si trae de más?»** — Traer de más no da error. Da lentitud. Y la lentitud no aparece en
> los tests con tres filas: aparece en producción, con treinta mil.

Quédate con esa pregunta, porque el módulo siguiente empieza ahí. El Lab 04 la convierte en
número: un contador de consultas, un presupuesto que no se puede cruzar, y la diferencia
**medida** entre dos versiones del mismo código que hacen lo mismo y cuestan distinto.

Hoy escribiste `fetch = FetchType.LAZY` porque el TODO_1 te lo pidió. En el Lab 04 vas a ver
qué pasa cuando no está.

---

## 7 · DO / DON'T · glosario mínimo

**DO**

- Declarar `fetch = FetchType.LAZY` explícito en todo `@ManyToOne` y `@OneToOne`.
- Dejar que el nombre del método sea la consulta mientras se lea bien.
- `@Transactional(readOnly = true)` en lecturas que recorren relaciones LAZY.
- Mirar el SQL generado cuando algo va lento o raro.
- Usar parámetros el día que escribas SQL a mano.

**DON'T**

- Concatenar en una consulta algo que venga de fuera. Nunca.
- Suponer qué consultas se ejecutan. Mirarlas.
- Llamar a `save` sobre una entidad ya gestionada «por si acaso»: el *dirty checking* ya lo hizo.
- Dejar que el nombre de un método derivado crezca hasta ser ilegible.

**Glosario**

| Término | En una línea |
|---|---|
| **JPA** | La especificación: las anotaciones y el contrato. |
| **Hibernate** | La implementación que lee tu mapeo y genera el SQL. |
| **Spring Data JPA** | Los repositorios: te ahorra escribir la implementación. |
| **Entidad** | Clase mapeada a una tabla. |
| **EntityManager** | Quien guarda, busca y borra entidades. |
| **Sesión de persistencia** | La ventana en la que una entidad está «viva» y puede completarse. |
| **Dirty checking** | Hibernate detecta los cambios de una entidad gestionada y lanza el `UPDATE` solo. |
| **Consulta derivada** | La que Spring Data genera leyendo el nombre del método. |
| **JPQL** | Consultas escritas a mano, pero sobre entidades y no sobre tablas. |
| **LAZY / EAGER** | Cuándo se trae el otro lado de una relación: al pedirlo, o siempre. |
