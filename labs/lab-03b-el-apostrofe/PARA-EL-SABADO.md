# Para el sábado · Lab 3.5

*Lo que no cabe en tres horas. Se lee después, sin prisa y con el código al lado.*

---

## 1 · Cuarenta líneas para leer una tabla: el mundo antes del ORM

Abre `ReporteInternoLegacyDao.java` y cuenta lo que hace para responder una pregunta simple —
*¿qué observaciones tiene este contribuyente?*

1. Pedir una conexión.
2. Escribir el SQL a mano, con su `JOIN`.
3. Crear un `Statement`.
4. Ejecutarlo.
5. Recorrer el `ResultSet` fila por fila.
6. Leer cada columna por su número de orden y convertirla al tipo Java.
7. Construir el objeto.
8. Cerrar el `ResultSet`, el `Statement` y la conexión, en ese orden.
9. Capturar `SQLException`, que es una excepción *checked* y hay que atrapar sí o sí.

Nueve pasos. Ocho de ellos son plomería: no dicen nada del negocio de la DGT. Y hay que
repetirlos en cada consulta de la aplicación, cambiando solo el paso 2.

Eso era programar contra una base de datos en 1998, y así se escribieron millones de líneas que
todavía corren. El DAO del lab no es una caricatura: es cómo se hacía.

**El problema no es que sea largo. Es que cada repetición es una oportunidad de equivocarse**, y
los cuatro pecados de la sección siguiente son cuatro maneras de equivocarse que caben en esas
cuarenta líneas.

---

## 2 · Los cuatro pecados del DAO

### Pecado 1 · El SQL concatenado

```java
String sql = "SELECT c.rut, o.texto, o.autor, o.creada_en "
           + "FROM observacion_interna o "
           + "JOIN contribuyente c ON c.id = o.contribuyente_id "
           + "WHERE c.rut = '" + rut + "'";
```

El `rut` viene del request. Viene de fuera. Y se pega dentro de la instrucción que el motor va a
ejecutar. Sección 3.

### Pecado 2 · El mapeo a mano

```java
resultado.add(new ObservacionInternaVista(
        rs.getString(1),
        rs.getString(2),
        rs.getString(3),
        rs.getTimestamp(4).toLocalDateTime()));
```

Las columnas se leen **por número de orden**. El día que alguien agregue una columna al `SELECT`
o cambie dos de sitio, esto compila igual, corre igual, y devuelve el autor donde va el texto.
Ningún compilador te va a avisar: para Java, `getString(2)` es válido siempre.

### Pecado 3 · El recurso filtrado

```java
rs.close();
st.close();
cx.close();
```

Están al final del camino feliz. Si `executeQuery` lanza, o si el mapeo revienta con un `null`
inesperado, esas tres líneas **no se ejecutan** y la conexión se queda tomada. El pool tiene un
número finito de conexiones; unas cuantas fugas y la aplicación deja de responder — no con un
error claro, sino colgada esperando una conexión que nunca vuelve.

No hay `finally`. No hay *try-with-resources*. La corrección existía desde Java 7 y este código
es posterior.

### Pecado 4 · El `catch` que traga

```java
} catch (SQLException e) {
}
```

La base puede estar caída. La consulta puede ser inválida. La tabla puede no existir. En los tres
casos, quien llama recibe una lista vacía, **exactamente igual** que si el contribuyente no
tuviera observaciones.

Un error que no se distingue de un resultado legítimo es peor que un error: es una mentira que
nadie va a investigar, porque nadie sabe que ocurrió.

---

## 3 · El apóstrofe: por qué concatenar convierte datos en código

El RUT honesto:

```sql
WHERE c.rut = '11111111-1'
```

El RUT con un apóstrofe:

```
rut = 11111111-1' OR '1'='1
```

Se pega igual, sin pensar, y queda:

```sql
WHERE c.rut = '11111111-1' OR '1'='1'
```

Lee esa línea como la lee el motor. La comilla que el atacante escribió **cerró** la cadena. Lo
que venía después dejó de ser parte del RUT y pasó a ser parte de la instrucción: un `OR` con una
condición que es verdadera siempre. El `WHERE` completo se vuelve verdadero para todas las filas.

**Esa es toda la idea de la inyección SQL.** No hay un exploit sofisticado: hay una frontera —la
que separa *lo que la aplicación dice* de *lo que el usuario escribe*— y concatenar la borra.

El caso del laboratorio es de solo lectura, y ya es grave: se filtraron observaciones de una
fiscalización en curso. Con la misma técnica se escribe `'; DROP TABLE ...` o se altera un
`UPDATE`. La diferencia entre leer y destruir es qué escribió el atacante, no qué permitía el
código.

**La regla, en una línea:** si un valor que viene de fuera termina dentro del texto de una
consulta, ya perdiste. No importa cuánto lo revises antes.

> ¿Y validar el RUT antes? Ayuda, y hay que hacerlo (Lab 03). Pero es una defensa por lista
> negra: protege de lo que se te ocurrió prohibir. La solución de esta sección protege de lo que
> no se te ocurrió.

---

## 4 · La trampa: `PreparedStatement` arregla uno de cuatro

Casi todo el mundo, al ver el pecado 1, propone esto:

```java
String sql = "SELECT ... WHERE c.rut = ?";
PreparedStatement ps = cx.prepareStatement(sql);
ps.setString(1, rut);
ResultSet rs = ps.executeQuery();
```

Y **es correcto**: el `?` es un parámetro. El motor recibe la instrucción y el dato por separado,
compila la instrucción primero, y el valor ya no puede cambiar su forma. El apóstrofe muere.

El problema es lo que *no* arregla:

| Pecado | ¿Lo arregla `PreparedStatement`? |
|---|---|
| 1 · SQL concatenado | **Sí** |
| 2 · Mapeo a mano por número de columna | No. Sigue igual de frágil. |
| 3 · Recursos filtrados | No. Sigue sin `finally`. |
| 4 · `catch` vacío | No. El error sigue sin existir. |

Y quedan las cuarenta líneas, y la próxima consulta las vuelve a pedir.

Por eso el laboratorio no toma ese camino. `PreparedStatement` es la respuesta correcta a **una**
pregunta; el ORM es la respuesta a las cuatro y además borra la plomería.

**Dicho esto:** cuando escribas SQL a propósito —y a veces hay que hacerlo, el Lab 04 lo hace con
`JdbcClient`— usa parámetros. Siempre. La lección del `?` no caduca.

---

## 5 · El mapa: tu clase ES la tabla

La idea del mapeo objeto-relacional cabe en una frase: **en vez de escribir el código que traduce
entre filas y objetos, se declara la correspondencia y alguien más escribe ese código.**

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

Cuatro anotaciones y ya no hay `ResultSet`, ni números de columna, ni conversiones a mano.

Dos que conviene entender bien:

- **`@Column(name = ...)`** solo hace falta cuando el nombre Java y el de la columna no coinciden.
  `creadaEn` ≠ `creada_en`, así que se declara. `texto` coincide, y se anota igual para declarar
  `nullable` y `length` — el mapeo también documenta.
- **`@ManyToOne`** es una relación, y su `fetch` decide **cuándo** se trae el otro lado. Sección 9.

---

## 6 · Quién trabaja por ti: Hibernate, EntityManager, y dónde encaja Spring Data

Tres nombres que se confunden todo el tiempo:

- **JPA** es la *especificación*: las anotaciones (`@Entity`, `@Id`, `@ManyToOne`) y el contrato
  del `EntityManager`. Es un estándar de Java, no una librería.
- **Hibernate** es la *implementación*: quien de verdad lee tus anotaciones, genera el SQL, lo
  ejecuta y arma los objetos. Es lo que Spring Boot pone por defecto.
- **Spring Data JPA** es la *comodidad* encima: los repositorios. No reemplaza a Hibernate; le
  ahorra a Hibernate el código repetitivo que tú tendrías que escribir.

El `EntityManager` es la pieza central de JPA: quien guarda, busca y borra entidades. Existe
debajo de todo lo que hiciste hoy, aunque no lo hayas escrito — cuando llamas a
`findByContribuyenteRut`, Spring Data termina llamándolo por ti.

Y tiene una **sesión de persistencia**: mientras está abierta, las entidades que cargó están
"vivas" y pueden completarse solas. Cuando se cierra, no. De ahí sale la sección 9.

---

## 7 · El repositorio: la consulta derivada

```java
public interface ObservacionInternaRepository extends JpaRepository<ObservacionInterna, Long> {
    List<ObservacionInterna> findByContribuyenteRut(String rut);
}
```

Una interfaz. Sin implementación. **El nombre del método ES la consulta**: Spring Data lo parte
en `findBy` + `Contribuyente` + `Rut`, comprueba contra tu entidad que esas propiedades existen,
sigue la relación, y genera el `SELECT` con su `JOIN` y su parámetro.

Dos consecuencias que valen más de lo que parecen:

1. **Si el nombre no cuadra con la entidad, la aplicación no arranca.** No falla en producción un
   martes: falla al arrancar, con un mensaje que dice qué propiedad no encontró. Un error de
   arranque es el mejor error posible.
2. **No hay dónde concatenar.** Tú no escribes la consulta. La superficie donde vivía el pecado 1
   dejó de existir.

De `JpaRepository` heredas gratis `findAll`, `findById`, `save`, `delete`, paginación y orden.
Cuando el nombre derivado se vuelva ilegible —y pasa, con cuatro o cinco condiciones— existe
`@Query` con JPQL. El Lab 04 lo usa.

---

## 8 · Leer el SQL generado: tu primer SELECT, el `?`, y tu primer JOIN

```bash
./bin/start-lab.sh --ver-sql
```

Pide las observaciones y busca en `.estado/dgt.log` algo parecido a esto:

```sql
select oi1_0.id, oi1_0.autor, oi1_0.contribuyente_id, oi1_0.creada_en, oi1_0.texto
from observacion_interna oi1_0
join contribuyente c1_0 on c1_0.id = oi1_0.contribuyente_id
where c1_0.rut = ?
```

Tres cosas para mirar:

- **El `JOIN` está ahí y tú no lo escribiste.** Salió de `findBy` + `Contribuyente` + `Rut`: la
  relación de tu entidad le dijo a Hibernate por dónde unir las tablas.
- **El `?` al final.** Ese es el parámetro. El RUT no aparece en el texto de la consulta: viaja
  aparte. Compara esa línea con el `WHERE c.rut = '" + rut + "'` del DAO — es exactamente la
  diferencia entre dato y código.
- **Los alias raros** (`oi1_0`) los genera Hibernate. No son para ti; son para que dos tablas con
  columnas del mismo nombre no choquen.

**Acostúmbrate a mirar este log.** Es la única forma de saber qué está haciendo de verdad tu
aplicación contra la base, y en el Lab 04 vas a contar consultas — no suponerlas.

---

## 9 · Lo que JPA no resuelve solo

El ORM quita la plomería. No quita la necesidad de saber qué se está trayendo.

**`FetchType`.** Tu `@ManyToOne` declara `LAZY`, y no por casualidad: el valor por defecto de
`@ManyToOne` es **`EAGER`**. Con EAGER, pedir una observación traería también al contribuyente
entero, aunque solo quisieras el texto. Y si el contribuyente trae sus trámites, y cada trámite
lo suyo, una consulta inocente termina siendo veinte.

**`LazyInitializationException`.** Es la otra cara, y probablemente la viste hoy: con LAZY, el
contribuyente se trae *cuando se pide*; si para entonces la sesión de persistencia ya se cerró,
salta la excepción. Por eso el servicio lleva `@Transactional(readOnly = true)`. No es un conjuro:
es «mantén la sesión abierta mientras dure este método».

Las dos cosas son el mismo tema visto de los dos lados, y ese tema es el Lab 04 entero:

> **«¿Y si trae de más?»** — Traer de más no da error. Da lentitud. Y la lentitud no aparece en
> los tests con tres filas de semilla: aparece en producción, con treinta mil.

El Lab 04 lo convierte en número: un contador de consultas, un presupuesto, y la diferencia
medida entre hacerlo bien y hacerlo mal.

---

## 10 · DO / DON'T · glosario mínimo

**DO**

- Declarar `fetch = FetchType.LAZY` explícito en todo `@ManyToOne` y `@OneToOne`.
- Dejar que el nombre del método sea la consulta mientras se lea bien.
- Mirar el SQL generado cuando algo va lento o raro.
- Usar parámetros el día que escribas SQL a mano a propósito.
- `@Transactional(readOnly = true)` en lecturas que recorren relaciones LAZY.

**DON'T**

- Concatenar en una consulta algo que venga de fuera. Nunca. Ni "validado".
- Leer columnas por número de orden.
- Cerrar recursos solo en el camino feliz.
- Atrapar una excepción para no hacer nada con ella.
- Suponer qué consultas se ejecutan. Mirarlas.

**Glosario**

| Término | En una línea |
|---|---|
| **JPA** | La especificación: las anotaciones y el contrato. |
| **Hibernate** | La implementación que genera y ejecuta el SQL. |
| **Spring Data JPA** | Los repositorios: te ahorra escribir la implementación. |
| **Entidad** | Clase mapeada a una tabla. |
| **EntityManager** | Quien guarda, busca y borra entidades. |
| **Sesión de persistencia** | La ventana en la que una entidad está "viva" y puede completarse. |
| **Consulta derivada** | La que Spring Data genera leyendo el nombre del método. |
| **LAZY / EAGER** | Cuándo se trae el otro lado de una relación: al pedirlo, o siempre. |
| **Inyección SQL** | Cuando un dato de fuera pasa a formar parte de la instrucción. |
