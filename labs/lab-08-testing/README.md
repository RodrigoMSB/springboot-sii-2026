# Lab 08 · Testing

Hoy no se arregla nada. Hoy se **protege** lo que ya funciona.

El proyecto de este laboratorio llega **entero y andando**: un catálogo de productos con su
repositorio, su servicio y su controller, sus tres endpoints respondiendo. No hay ningún error
que buscar. Lo que no hay es **ni un solo test**.

Doce de los dieciocho alumnos de este curso nunca escribió un test automatizado. Este lab
existe para eso.

## Qué se aprende

- Que un test es **código normal** que llama a tu código y comprueba el resultado. Nada más.
- Que un test sirve el día que se pone **rojo**: avisa antes que el usuario.
- **Qué merece un test y qué no**: se prueban reglas y **bordes**, no que una lista tenga cuatro
  elementos.
- Un test **parametrizado** (`@CsvSource`): cuatro casos de la misma regla en una tabla.
- Que probar el fallo (`assertThrows`) vale tanto como probar el éxito.
- Cómo **aislar** una pieza con un doble (Mockito) — y **cuándo un doble estorba**.
- Cómo probar un endpoint **sin levantar el servidor** (`@WebMvcTest` + `MockMvc`).
- Cómo se lee **la consola de un test rojo**, que es donde está la información.

## Las tres carpetas

Este laboratorio estrena la estructura que rige de aquí en adelante:

| | |
|---|---|
| **`practica/`** | Donde trabajas. El código de producción está completo; los tres archivos de test llegan **declarados y vacíos**, y `ContextoDeSpringTest` llega resuelto |
| **`solucion/`** | El mismo proyecto con los cuatro archivos de test escritos, comentados al mínimo |
| **`instructor/`** | Los mismos archivos, explicados línea por línea. **No viaja en el repositorio** |

`practica/` y `solucion/` son proyectos completos y arrancan solos. `instructor/` **no es un
proyecto**: no tiene `mvnw` ni se compila. Son los archivos para leer mientras se enseña, y por
eso no está versionada — la genera quien dicta la sesión.

> **`models/` y `entities/` no son lo mismo, y por eso no se llaman igual.** Lo que hay en
> `models/` es un objeto que vive **en memoria**: nace, se usa y se muere con el proceso. No hay
> tabla detrás ni JPA de por medio. Desde el Lab 04, cuando aparezca una base de datos de verdad,
> el paquete se llamará `entities/` — y ese cambio de nombre es la señal de que la clase ya no es
> un objeto suelto, sino **una fila**.

## Cómo se corre

```bash
cd practica          # o solucion
./mvnw test              # correr los tests
./mvnw spring-boot:run   # levantar la aplicación
```

**La aplicación se queda corriendo. Se apaga con Ctrl+C.** Los tests no: terminan y devuelven la
consola.

| | puerto |
|---|---|
| `practica/` | **8093** |
| `solucion/` | **8094** |

## Los endpoints, que ya funcionan

```
GET /productos                      el catálogo
GET /productos/{id}                 uno, o 404 con cuerpo
GET /productos/{id}/total?cantidad= la cotización con descuento por volumen
```

```bash
curl http://localhost:8093/productos/2
{"id":2,"nombre":"Tóner negro","precioNeto":68900}

curl -i http://localhost:8093/productos/99
HTTP/1.1 404
{"mensaje":"No existe el producto 99"}

curl "http://localhost:8093/productos/1/total?cantidad=3"
{"total":16033}
```

## La regla que se protege hoy

Cabe en una línea, y es lo único del proyecto que puede romperse de forma interesante:

> **Descuento por volumen: 3 o más unidades, 10 %. 10 o más, 20 %.**

Cuatro cosas pueden salir mal ahí —los bordes, el orden de los tramos, el redondeo y la cantidad
inválida— y de eso van los cuatro casos del primer test.

## Sin base de datos, a propósito

El repositorio es una lista en memoria, como en el Lab 02. Meter JPA aquí obligaría a hablar de
`@DataJpaTest` y de transacciones de test, y hoy se aprende **a testear**, no a testear
persistencia.

## El paso que hay que llegar a hacer

El **paso 1**, su segunda mitad. Ahí se cambia el IVA de `0.19` a `0.10` a propósito y se corre la
suite otra vez. Tres de los cuatro casos se ponen **rojos** —el de cero unidades no, porque no
depende del IVA— y el informe de Surefire dice, en dos líneas:

```
org.opentest4j.AssertionFailedError: expected: <5938> but was: <5489>
	at cl.dgt.testing.ProductoServiceTest...(ProductoServiceTest.java:30)
```

**Dos líneas, no sesenta.** Eso se lo debe el laboratorio a `<trimStackTrace>true</trimStackTrace>`
en el `pom.xml`: sin él, ese mismo fallo entierra la información útil bajo la pila entera de JUnit
y Spring.

Después se deshace y vuelve el verde. Ese ida y vuelta es el laboratorio entero: **un test que
nunca se ha puesto rojo no ha demostrado nada.**

## Lo que no vimos hoy

- **Tests de persistencia** (`@DataJpaTest`, bases de datos de test, rollback por transacción).
  Se apoya en el Lab 04 y da para una sesión propia.
- **Cobertura** (JaCoCo, el porcentaje de líneas ejecutadas por la suite) — y por qué un 90 % de
  cobertura no significa que el código esté probado.
- **TDD**: escribir el test antes que el código. Es una forma de trabajar, no una herramienta, y
  aprenderla exige más tiempo del que hay hoy.
- **`verify` y los tests de interacción**: comprobar que algo se *llamó*, no lo que devolvió. Tiene
  su sitio —un correo que se envía, un pago que se registra— y no es ninguno de los cuatro tests
  de hoy. Se nombra en el paso 2 y se explica por qué no está.

## El guion

`PASOS.md` — el paso 0 y los tres pasos de la sesión, con qué escribir en cada uno y qué debe
salir en la consola.
