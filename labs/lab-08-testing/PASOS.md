# Pasos · Lab 08 · Testing

Seis pasos. Se trabaja en `practica/`, en vivo, uno a la vez. Después de cada paso se corre
`./mvnw test` y se mira la consola antes de seguir.

```bash
cd practica
./mvnw test
```

Hoy **no se levanta el servidor** salvo para mirarlo una vez al principio. Los tests no necesitan
puerto.

El código de producción llega **completo**. Lo que llega vacío es esto:

```
src/test/java/cl/dgt/testing/     ←  hoy se llena entero
```

y se llena en este orden:

```
ProductoServiceTest  →  (romperlo)  →  assertThrows  →  ProductoServiceConDobleTest
     paso 1              paso 2         paso 3               paso 4

     →  ProductoControllerTest  →  ContextoDeSpringTest
              paso 5                     paso 6
```

---

## Paso 0 · Mirar lo que ya funciona

**Se explica:** el proyecto está entero. Hoy no se repara nada: se protege.

**Se corre:**

```bash
./mvnw spring-boot:run
```

**En consola:**

```
Tomcat started on port 8093 (http) with context path '/'
Started Lab08Application in 0.783 seconds
```

```bash
$ curl http://localhost:8093/productos/2
{"id":2,"nombre":"Tóner negro","precioNeto":68900}

$ curl -i http://localhost:8093/productos/99
HTTP/1.1 404
{"mensaje":"No existe el producto 99"}

$ curl http://localhost:8093/productos/valor-total
{"valorConIva":420891}
```

**Ctrl+C**, y ahora la pregunta con la que se abre el día:

> Todo esto funciona. **¿Cómo lo sabemos?** Porque lo acabamos de mirar. Mañana, cuando alguien
> toque el IVA, ¿quién lo mira?

Y la comprobación que cierra la pregunta:

```bash
$ ./mvnw test
[INFO] BUILD SUCCESS
```

**Verde. Con cero tests.** Un `BUILD SUCCESS` sobre una suite vacía no dice que el código esté
bien: dice que nadie preguntó.

---

## Paso 1 · El primer test

**Se explica:** el método más fácil de testear del proyecto es `precioConIva`: entra un número,
sale otro, no depende de nada. Un test es una clase normal, con métodos normales, y una anotación
que le dice a JUnit «esto es un test».

Tres tiempos, siempre los mismos:

1. **preparar** — construir lo que hace falta
2. **ejecutar** — llamar al método
3. **comprobar** — decir qué resultado se esperaba

Y el nombre del método **no es un identificador, es una frase**. Se va a leer en un informe de
fallos a las tres de la mañana: `test1` no sirve de nada.

**Se pega:** archivo **nuevo** `practica/src/test/java/cl/dgt/testing/ProductoServiceTest.java` — el archivo entero.

```java
package cl.dgt.testing;

import cl.dgt.testing.repositories.ProductoRepositoryLista;
import cl.dgt.testing.services.ProductoService;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ProductoServiceTest {

    private final ProductoService servicio = new ProductoService(new ProductoRepositoryLista());

    @Test
    void elPrecioConIvaSeRedondeaAlPesoMasCercano() {
        int conIva = servicio.precioConIva(4990);

        assertEquals(5938, conIva);
    }

}
```

Nótese lo que **no** hay: ni `@SpringBootTest`, ni anotaciones de Spring, ni contexto. El
servicio se construye con `new`, como cualquier objeto.

**Se corre:** `./mvnw test`

**En consola:**

```
[INFO] Running cl.dgt.testing.ProductoServiceTest
[INFO] Tests run: 1, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 0.005 s
[INFO] BUILD SUCCESS
```

**Cinco milésimas de segundo.** Ese número va a importar en el paso 6.

Y ahora dos más, del mismo tamaño, para tener con qué trabajar.

**Se pega:** en `practica/src/test/java/cl/dgt/testing/ProductoServiceTest.java`, **antes de la llave que cierra la clase**.

```java
    @Test
    void elCatalogoTraeLosCuatroProductos() {
        assertEquals(4, servicio.todos().size());
    }

    @Test
    void unIdQueExisteDevuelveElProducto() {
        assertEquals("Tóner negro", servicio.porId(2L).nombre());
    }
```

```
[INFO] Tests run: 3, Failures: 0, Errors: 0, Skipped: 0
```

---

## Paso 2 · El test que avisa

**Este es el momento del laboratorio.** Si de las tres horas hay que salvar cinco minutos, son
estos.

**Se explica:** el verde de arriba no ha demostrado todavía nada. Un test que nunca se ha puesto
rojo podría estar comprobando el aire. Así que se rompe el código de producción **a propósito**,
y se mira qué pasa.

**Se pega:** en `practica/src/main/java/cl/dgt/testing/services/ProductoService.java`,
**reemplazando la línea de `TASA_IVA`**. Una sola cifra.

<!-- pasos:intermedio · se deshace en cuanto se ve el rojo: la solución lleva 0.19 -->

```java
    private static final double TASA_IVA = 0.10;   // era 0.19
```

**Se corre:** `./mvnw test`

**En consola:**

```
[ERROR] Tests run: 3, Failures: 1, Errors: 0, Skipped: 0 <<< FAILURE!
[ERROR] cl.dgt.testing.ProductoServiceTest.elPrecioConIvaSeRedondeaAlPesoMasCercano <<< FAILURE!
org.opentest4j.AssertionFailedError: expected: <5938> but was: <5489>
	at cl.dgt.testing.ProductoServiceTest.elPrecioConIvaSeRedondeaAlPesoMasCercano(ProductoServiceTest.java:17)

[INFO] BUILD FAILURE
```

Se lee en voz alta, entera:

- **qué** falló: el nombre del test, que es una frase
- **qué esperaba**: `5938`
- **qué obtuvo**: `5489`
- **dónde**: archivo y línea

Nadie tuvo que abrir el navegador, ni levantar el servidor, ni acordarse de que el IVA es 19 %.

**Se deshace:** devolver `0.19`.

```
[INFO] Tests run: 3, Failures: 0, Errors: 0, Skipped: 0
[INFO] BUILD SUCCESS
```

**Lo que hay que notar:** el rojo llegó **cero segundos** después del cambio, en la máquina de
quien lo hizo. Sin test, ese `0.10` viaja a producción y lo descubre un contribuyente al que le
cobraron de menos.

> Un test no sirve cuando está verde. Sirve **el día que se pone rojo**.

---

## Paso 3 · El camino triste

**Se explica:** hasta aquí se ha probado que el código hace lo que debe cuando todo va bien. Falta
la otra mitad, que en producción es la que se rompe: **qué pasa cuando el dato no está**.

`servicio.porId(99L)` no devuelve `null` ni un `Optional` vacío: **lanza** una excepción. Un test
normal no puede comprobar eso —la excepción lo tumbaría a él también—, así que JUnit trae
`assertThrows`: recibe el tipo de excepción esperado y un trozo de código, lo ejecuta, y falla si
**no** explota.

**Se pega (1 de 2):** en `practica/src/test/java/cl/dgt/testing/ProductoServiceTest.java`, **arriba**, con los imports.

```java
import cl.dgt.testing.services.ProductoNoEncontradoException;

import static org.junit.jupiter.api.Assertions.assertThrows;
```

**Se pega (2 de 2):** en el mismo archivo, **antes de la llave que cierra la clase**.

```java
    @Test
    void unIdQueNoExisteLanzaProductoNoEncontrado() {
        ProductoNoEncontradoException e =
                assertThrows(ProductoNoEncontradoException.class, () -> servicio.porId(99L));

        assertEquals(99L, e.getId());
    }
```

`assertThrows` **devuelve** la excepción capturada, y por eso la segunda línea puede seguir
comprobando: no basta con que explote, tiene que explotar diciendo **cuál** es el producto que
falta.

**Se corre:** `./mvnw test`

**En consola:**

```
[INFO] Tests run: 4, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 0.034 s
```

**Lo que hay que notar:** este test también hay que verlo rojo para creerlo. Cámbiese `99L` por
`2L` —un id que sí existe— y JUnit dirá que esperaba una excepción y no hubo ninguna. Devolver
`99L` y seguir.

---

## Paso 4 · Aislar con Mockito

**Se explica:** los cuatro tests de arriba construyen el servicio con el repositorio **de verdad**:

```java
new ProductoService(new ProductoRepositoryLista())
```

Funciona, y hoy es barato porque el repositorio es una lista. Pero ahí se están probando **dos
piezas a la vez**, y eso trae dos problemas:

1. Si el test falla, no se sabe cuál de las dos tiene la culpa.
2. El día que el repositorio hable con una base de datos, este test necesitará una base de datos.

La salida es sustituir la dependencia por un **doble**: un objeto que cumple la interfaz
`ProductoRepository` y que **hace lo que el test le diga**. Mockito lo fabrica solo.

Tres verbos:

| | |
|---|---|
| `@Mock` | fabrica el doble |
| `when(...).thenReturn(...)` | le dice qué contestar |
| `verify(...)` | comprueba **que se le llamó** |

El tercero es distinto de los otros dos: no mira el resultado, mira **la conversación**.

**Se pega:** archivo **nuevo** `practica/src/test/java/cl/dgt/testing/ProductoServiceConDobleTest.java` — el archivo entero.

```java
package cl.dgt.testing;

import cl.dgt.testing.models.Producto;
import cl.dgt.testing.repositories.ProductoRepository;
import cl.dgt.testing.services.ProductoNoEncontradoException;
import cl.dgt.testing.services.ProductoService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProductoServiceConDobleTest {

    @Mock
    private ProductoRepository repositorio;

    @Test
    void elValorDelCatalogoSumaLosPreciosConIva() {
        when(repositorio.todos()).thenReturn(List.of(
                new Producto(1L, "Uno", 1000),
                new Producto(2L, "Dos", 2000)));

        ProductoService servicio = new ProductoService(repositorio);

        assertEquals(3570, servicio.valorDelCatalogo());
        verify(repositorio).todos();
    }

    @Test
    void siElRepositorioNoTraeNadaSeLanzaLaExcepcion() {
        when(repositorio.porId(7L)).thenReturn(Optional.empty());

        ProductoService servicio = new ProductoService(repositorio);

        assertThrows(ProductoNoEncontradoException.class, () -> servicio.porId(7L));
        verify(repositorio).porId(7L);
    }
}
```

**Se corre:** `./mvnw test`

**En consola:**

```
[INFO] Running cl.dgt.testing.ProductoServiceConDobleTest
[INFO] Tests run: 2, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 0.077 s
[INFO] Tests run: 6, Failures: 0, Errors: 0, Skipped: 0
```

**Lo que hay que notar:** sigue sin haber Spring en ninguna parte. Mockito es una librería normal.

---

## Paso 5 · Probar el endpoint sin levantar el servidor

**Se explica:** falta la capa de arriba. `/productos/99` tiene que devolver **404**, y eso no lo
decide el servicio: lo deciden el controller y el manejador de errores.

Se podría levantar la aplicación y llamar con `curl`. Pero eso necesita un puerto libre, un
proceso vivo, y una persona mirando. `@WebMvcTest` hace otra cosa: levanta **solo la capa web** —
el enrutado, la conversión a JSON, el manejador de errores— y nada más. Sin Tomcat y sin puerto.

Dos piezas nuevas:

| | |
|---|---|
| `MockMvc` | manda peticiones falsas y deja comprobar la respuesta |
| `@MockitoBean` | pone un doble de Mockito **dentro del contexto de Spring** |

El segundo hace falta porque `@WebMvcTest` **no** carga el servicio: solo la capa web. Si no se le
da un `ProductoService`, el controller no se puede construir.

> En Spring Boot 4, `@WebMvcTest` vive fuera de `spring-boot-starter-test`: hace falta la
> dependencia `spring-boot-webmvc-test`, y en `practica/` ya viene en el `pom.xml`.
> `@MockitoBean` reemplazó al viejo `@MockBean`.

**Se pega:** archivo **nuevo** `practica/src/test/java/cl/dgt/testing/ProductoControllerTest.java` — el archivo entero.

```java
package cl.dgt.testing;

import cl.dgt.testing.controllers.ProductoController;
import cl.dgt.testing.models.Producto;
import cl.dgt.testing.services.ProductoNoEncontradoException;
import cl.dgt.testing.services.ProductoService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ProductoController.class)
class ProductoControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ProductoService servicio;

    @Test
    void pedirUnProductoQueExisteDevuelve200YSuJson() throws Exception {
        when(servicio.porId(1L)).thenReturn(new Producto(1L, "Resma de papel carta", 4990));

        mockMvc.perform(get("/productos/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.nombre").value("Resma de papel carta"))
                .andExpect(jsonPath("$.precioNeto").value(4990));
    }

    @Test
    void pedirUnProductoQueNoExisteDevuelve404ConCuerpo() throws Exception {
        when(servicio.porId(99L)).thenThrow(new ProductoNoEncontradoException(99L));

        mockMvc.perform(get("/productos/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.mensaje").value("No existe el producto 99"));
    }
}
```

**Se corre:** `./mvnw test`

**En consola:**

```
[INFO] Running cl.dgt.testing.ProductoControllerTest
2026-08-18T00:26:08.908-04:00  INFO --- ProductoControllerTest : Started ProductoControllerTest in 0.245 seconds
[INFO] Tests run: 2, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 0.384 s
[INFO] Tests run: 8, Failures: 0, Errors: 0, Skipped: 0
```

**Lo que hay que notar:** apareció una línea que no había salido en todo el día — `Started
ProductoControllerTest in 0.245 seconds`. Spring arrancó. Esos 245 ms no estaban en los pasos 1 a
4, y ese es el tema del paso 6.

---

## Paso 6 · Cuándo levantar Spring entero

**Se explica:** queda una anotación más, la más conocida y la que se usa mal más a menudo.
`@SpringBootTest` levanta **el contexto completo**: todos los beans, todas las autoconfiguraciones,
todo lo que arrancaría la aplicación de verdad. Sirve para probar exactamente una cosa: **que el
cableado funciona**.

**Se pega:** archivo **nuevo** `practica/src/test/java/cl/dgt/testing/ContextoDeSpringTest.java` — el archivo entero.

```java
package cl.dgt.testing;

import cl.dgt.testing.controllers.ProductoController;
import cl.dgt.testing.services.ProductoService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;

import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest
class ContextoDeSpringTest {

    @Autowired
    private ApplicationContext contexto;

    @Test
    void elCableadoDeSpringEsCorrecto() {
        assertNotNull(contexto.getBean(ProductoService.class));
        assertNotNull(contexto.getBean(ProductoController.class));
    }
}
```

**Se corre:** `./mvnw test`

**En consola:**

```
[INFO] Tests run: 4, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 0.034 s -- in ProductoServiceTest
[INFO] Tests run: 2, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 0.077 s -- in ProductoServiceConDobleTest
[INFO] Tests run: 2, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 0.369 s -- in ProductoControllerTest
[INFO] Tests run: 1, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 1.242 s -- in ContextoDeSpringTest
[INFO] Tests run: 9, Failures: 0, Errors: 0, Skipped: 0
[INFO] BUILD SUCCESS
```

### Los números, y lo que de verdad dicen

Medido en la máquina donde se preparó el material (Mac Studio, JDK 25 de la maleta):

| | qué levanta | 4 tests / 2 / 2 / 1 |
|---|---|---|
| `ProductoServiceTest` | nada | **0,034 s** |
| `ProductoServiceConDobleTest` | Mockito | **0,077 s** |
| `ProductoControllerTest` (`@WebMvcTest`) | la capa web | **0,369 s** |
| `ContextoDeSpringTest` (`@SpringBootTest`) | el contexto entero | **1,242 s** |

Y aquí hay que decir **la verdad completa**, porque la tabla sola engaña. Corriendo la suite al
revés, con `@SpringBootTest` de último, los números cambian:

```
ProductoControllerTest   (@WebMvcTest)     1,033 s
ContextoDeSpringTest     (@SpringBootTest) 0,221 s
```

**Se dieron vuelta.** Lo caro no es la anotación: es **el primer arranque de Spring**, unos 0,7 s
que paga quien llegue primero. En esta aplicación —nueve clases, sin base de datos, sin
seguridad— el contexto completo no cuesta prácticamente nada más que la capa web.

Lo que **no** cambia con el orden es el salto de escalón:

> de **0,03 s** sin Spring a **0,7 s** con Spring. **Veinte veces.**

Y ese 0,7 s de hoy es el suelo. En una aplicación real, ese mismo contexto trae un pool de
conexiones, Hibernate leyendo las entidades, la seguridad, los cachés: son **segundos**, y se
pagan por cada configuración de contexto distinta que tenga la suite.

### La regla que se llevan

> **El 90 % de los tests no necesita levantar Spring.**
>
> Si lo que se prueba es una regla de negocio, se prueba con `new`. Si es la capa web, con
> `@WebMvcTest`. `@SpringBootTest` se reserva para cuando lo que se prueba **es precisamente el
> cableado** — y por eso en este proyecto hay uno, y uno solo.

Una suite de mil tests con `@SpringBootTest` en todos tarda veinte minutos, y una suite que tarda
veinte minutos **no se corre**. Un test que no se corre no protege nada.

### Un aviso sobre la consola

Al correr los tests con Mockito, la JVM escupe unas líneas que asustan y no son un problema:

```
Mockito is currently self-attaching to enable the inline-mock-maker...
WARNING: A Java agent has been loaded dynamically...
OpenJDK 64-Bit Server VM warning: Sharing is only supported for boot loader classes...
```

Es el JDK 25 avisando de cómo Mockito se instala para poder fabricar los dobles. Los tests pasan
igual. Lo que importa sigue siendo la línea de abajo: `Tests run: 9, Failures: 0`.

---

## Al terminar

`practica/` tiene los mismos cuatro archivos de test que `solucion/`, y `./mvnw test` da
**9 tests, 0 fallos** en los dos. Si algo no cuadra, `solucion/` está ahí para comparar archivo
por archivo.

Lo que hay que poder decir con las propias palabras:

> Un test es código que llama a mi código y comprueba el resultado. Sirve el día que se pone rojo.
> Se prueba una pieza a la vez, sustituyendo lo que hay debajo por un doble, y casi nunca hace
> falta levantar Spring para hacerlo.

### Lo que siembra este lab

Hasta hoy, cada laboratorio de este curso se comprobó **mirando**: el navegador, el `curl`, el
contador en pantalla, los veinte folios del Lab 07. Funciona mientras haya una persona mirando.

Lo que se siembra aquí es que esa persona ya no hace falta:

> **Todo lo que se comprobó a ojo en los seis labs anteriores se puede escribir como un test que
> lo comprueba solo, en milésimas de segundo, cada vez que alguien toca el código.**

El 404 con cuerpo del Lab 03, la implementación que Spring eligió en el Lab 02, el número de
consultas del Lab 06: los tres son `assertEquals`. De aquí en adelante, cada pieza nueva que se
construya puede llegar con su red debajo, y la pregunta al terminar un laboratorio deja de ser
«¿funcionó?» para ser **«¿qué test lo demuestra?»**.
