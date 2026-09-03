# Pasos · Lab 08 · Testing

Tres pasos y el cierre. Se trabaja en `practica/`, en vivo, uno a la vez. Después de cada paso se
corre `./mvnw test` y se mira la consola antes de seguir.

```bash
cd practica
./mvnw test
```

Hoy **no se levanta el servidor** salvo para mirarlo una vez al principio. Los tests no necesitan
puerto.

El código de producción llega **completo**. Lo que se escribe hoy son tres archivos de test:

```
ProductoServiceTest         →  paso 1   la regla de negocio, y el rojo en vivo
ProductoServiceConDobleTest →  paso 2   el mismo método con un doble
ProductoControllerTest      →  paso 3   la capa web, sin servidor
```

`ContextoDeSpringTest` **llega resuelto**: es el único que ya está escrito, y se mira en el cierre.

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

$ curl "http://localhost:8093/productos/1/total?cantidad=3"
{"total":16033}
```

**Esa última línea es la del día.** Tres resmas de papel a 4990 el neto: 5938 con IVA, por tres,
menos un 10 % por volumen. **16.033 pesos.**

**Ctrl+C**, y ahora la pregunta con la que se abre el día:

> Todo esto funciona. **¿Cómo lo sabemos?** Porque lo acabamos de mirar. Mañana, cuando alguien
> toque el IVA o mueva un tramo del descuento, ¿quién lo mira?

Y la comprobación que cierra la pregunta:

```bash
$ ./mvnw test
[INFO] Tests run: 1, Failures: 0, Errors: 0, Skipped: 0
[INFO] BUILD SUCCESS
```

**Verde, y el único test que corre es el que comprueba que Spring arranca.** De la regla del
descuento no hay nadie preguntando.

---

## Paso 1 · El primer test, y las tres franjas

**Se explica:** el método que vale la pena proteger es `totalConDescuento`, porque es el único que
tiene una **regla** que alguien puede romper:

```java
    /** Descuento por volumen. 3 o más unidades, 10 %. 10 o más, 20 %. */
    public int totalConDescuento(Long id, int cantidad)
```

Y ahí hay cuatro cosas que pueden salir mal, no una:

| | |
|---|---|
| los **bordes** | ¿3 unidades entran en el 10 %, o hace falta 4? |
| el **orden** de los tramos | si se pregunta por `>= 3` antes que por `>= 10`, diez unidades se llevan el 10 % y nadie se entera |
| el **redondeo** | ¿se redondea por unidad o sobre el total? |
| la **entrada inválida** | ¿qué pasa con cero unidades? |

Un test por cada caso sería el mismo cuerpo cuatro veces. Se escribe **uno solo, parametrizado**.

**Se pega:** en `practica/src/test/java/cl/dgt/testing/ProductoServiceTest.java`, **arriba**, con
los imports.

```java
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
```

**Se pega:** en el mismo archivo, **dentro de la clase**, donde dice `// escribe aquí`.

```java
    @ParameterizedTest(name = "{0} unidades -> {1}")
    @CsvSource({
            " 1,  5938",     // sin descuento
            " 3, 16033",     // 10 %
            "10, 47504",     // 20 %
            " 0,     0"      // cantidad inválida: lanza
    })
    void elTotalAplicaElDescuentoPorVolumen(int cantidad, int esperado) {
        if (cantidad <= 0) {
            assertThrows(IllegalArgumentException.class, () -> servicio.totalConDescuento(1L, cantidad));
            return;
        }

        assertEquals(esperado, servicio.totalConDescuento(1L, cantidad));
    }
```

**Se corre:** `./mvnw test`

**En consola:**

```
[INFO] Tests run: 4, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 0.089 s -- in cl.dgt.testing.ProductoServiceTest
[INFO] BUILD SUCCESS
```

**Un método, cuatro ejecuciones.** JUnit las cuenta por separado: si se rompe la del 10 %, las
otras tres siguen verdes y se sabe exactamente cuál falló.

**Lo que hay que notar:**

- **Se usa el repositorio de verdad**, no un doble. `ProductoRepositoryLista` es una lista en
  memoria: no hay base, no hay red, no hay nada lento. Un mock aquí no ahorraría un milisegundo y
  añadiría tres líneas que no prueban nada. El doble llega en el paso 2, cuando tenga algo que
  demostrar.
- **Los cuatro casos son bordes**, no números al azar. `3` es el primer valor del 10 % y `10` el
  primero del 20 %: si alguien escribe `>` en vez de `>=`, esos dos casos se ponen rojos y ninguno
  más. Probar 5 unidades no habría añadido nada — cae en medio de una franja.
- **Los números se calculan de cabeza**, y conviene hacerlo en la pizarra: `4990 × 1,19 = 5938`;
  `5938 × 3 = 17.814`, menos 10 % = `16.033`.

### Y ahora se rompe a propósito

**Se cambia** en `practica/src/main/java/cl/dgt/testing/services/ProductoService.java`:

```java
    private static final double TASA_IVA = 0.10;
```

**Se corre:** `./mvnw test`

**En consola:**

```
[ERROR] Tests run: 4, Failures: 3, Errors: 0, Skipped: 0 <<< FAILURE! -- in cl.dgt.testing.ProductoServiceTest
[ERROR] elTotalAplicaElDescuentoPorVolumen(int, int)[1] <<< FAILURE!
[ERROR] elTotalAplicaElDescuentoPorVolumen(int, int)[2] <<< FAILURE!
[ERROR] elTotalAplicaElDescuentoPorVolumen(int, int)[3] <<< FAILURE!
```

**Tres de cuatro.** El cuarto —el de cero unidades— sigue verde, porque no depende del IVA.

**Y aquí está lo que hay que proyectar.** El detalle no se lee en esa lista: se lee en el archivo
que Surefire deja escrito.

```bash
cat target/surefire-reports/cl.dgt.testing.ProductoServiceTest.txt
```

```
cl.dgt.testing.ProductoServiceTest.elTotalAplicaElDescuentoPorVolumen(int, int)[1] <<< FAILURE!
org.opentest4j.AssertionFailedError: expected: <5938> but was: <5489>
	at cl.dgt.testing.ProductoServiceTest.elTotalAplicaElDescuentoPorVolumen(ProductoServiceTest.java:30)
```

**Dos líneas.** El test no dice «algo se rompió»: dice **qué número esperaba y cuál llegó**, y en
qué línea. Eso se lo debe el laboratorio a una línea del `pom.xml`:

```xml
    <trimStackTrace>true</trimStackTrace>
```

Sin ella, ese mismo fallo imprime **sesenta líneas** de `org.junit.platform`,
`org.springframework` y `sun.reflect`, y la única que importa queda enterrada.

**Se deja el IVA en `0.19` otra vez** y se comprueba que vuelve el verde.

> **La frase del paso:** un test no sirve por estar verde. Sirve por **ponerse rojo cuando alguien
> rompe algo**, y por decir qué.

---

## Paso 2 · El doble, y cuándo estorba

**Se explica:** antes de escribir nada, se mira otra vez el método real:

```java
        int bruto = precioConIva(porId(id).precioNeto()) * cantidad;
```

Ahí hay una dependencia: `porId` va al repositorio. En el paso 1 eso no molestaba —la lista en
memoria devuelve 4990 y ya está—, pero tiene un precio: **los números esperados del test son
5938, 16033 y 47504**, que nadie puede verificar de cabeza.

Con un doble se elige el precio. Se dice «este producto vale 1000» y la cuenta sale en la pizarra.

**Se pega:** en `practica/src/test/java/cl/dgt/testing/ProductoServiceConDobleTest.java`,
**arriba**, con los imports.

```java
import cl.dgt.testing.models.Producto;
import cl.dgt.testing.services.ProductoService;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;
```

**Se pega:** en el mismo archivo, **dentro de la clase**, donde dice `// escribe aquí`.

```java
    @Test
    void elDescuentoSeCalculaSobreLoQueDevuelveElRepositorio() {
        when(repositorio.porId(1L)).thenReturn(Optional.of(new Producto(1L, "Inventado", 1000)));

        ProductoService servicio = new ProductoService(repositorio);

        // 1000 neto -> 1190 con IVA -> x3 = 3570 -> 10 % menos = 3213
        assertEquals(3213, servicio.totalConDescuento(1L, 3));
    }
```

**Se corre:** `./mvnw test`

**En consola:**

```
[INFO] Tests run: 1, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 0.073 s -- in cl.dgt.testing.ProductoServiceConDobleTest
[INFO] Tests run: 6, Failures: 0, Errors: 0, Skipped: 0
```

**Lo que hay que notar, y es toda la lección del paso:**

> Un doble **no** se usa «porque hay una dependencia». Se usa cuando la dependencia real es
> **lenta**, **frágil**, o **no se puede controlar**.

Aquí el motivo es el tercero: hacía falta fijar el precio para que el número esperado se pudiera
comprobar a mano. En el paso 1 no se daba ninguno de los tres, y por eso allí **no hay doble**.
La comparación entre los dos archivos es el contenido.

Y dos detalles:

- **`new ProductoService(repositorio)`**, construido a mano. Eso sólo se puede hacer porque la
  dependencia entra por el **constructor**. Es el Lab 02 cobrando: con un `@Autowired` sobre un
  campo, habría que levantar Spring para poder probarlo.
- **Ni un `verify`.** Lo que importa es el número que sale, no si el método llamó al repositorio
  una vez o dos. `verify` ata el test a **cómo** está escrito el método, y el día que alguien lo
  reorganice sin cambiar su comportamiento, el test se pone rojo sin que nada esté mal. Tiene su
  sitio —comprobar que un correo se envió, que un pago se registró: efectos que no devuelven
  nada— y no es éste.

---

## Paso 3 · El endpoint, sin levantar el servidor

**Se explica:** falta la capa de arriba. Y lo interesante no es que pedir un producto devuelva su
JSON —eso lo hace Spring—, sino **el camino triste**: que una excepción del servicio se convierta
en un 404 con cuerpo.

Ese código no está en el controller. Está en el `@RestControllerAdvice` del **Lab 03**, cuatro
sesiones atrás. Este test comprueba que aquello sigue funcionando.

**Se pega:** en `practica/src/test/java/cl/dgt/testing/ProductoControllerTest.java`, **arriba**,
con los imports.

```java
import cl.dgt.testing.exceptions.ProductoNoEncontradoException;
import org.junit.jupiter.api.Test;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
```

**Se pega:** en el mismo archivo, **dentro de la clase**, donde dice `// escribe aquí`.

```java
    @Test
    void pedirUnProductoQueNoExisteDevuelve404ConCuerpo() throws Exception {
        when(servicio.porId(99L)).thenThrow(new ProductoNoEncontradoException(99L));

        mockMvc.perform(get("/productos/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.mensaje").value("No existe el producto 99"));
    }
```

**Se corre:** `./mvnw test`

**En consola:**

```
[INFO] Tests run: 1, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 0.359 s -- in cl.dgt.testing.ProductoControllerTest
[INFO] Tests run: 7, Failures: 0, Errors: 0, Skipped: 0
[INFO] BUILD SUCCESS
```

**Sin puerto, sin Tomcat, sin `curl`.** `MockMvc` recorre la cadena de Spring MVC en memoria; por
eso tarda cuatro décimas y no varios segundos.

**Las dos comprobaciones son el paso, y se señalan por separado:**

| | qué prueba |
|---|---|
| `status().isNotFound()` | el **404**: que la excepción no se escapó como un 500 |
| `jsonPath("$.mensaje")` | el **cuerpo**: que el error trae algo legible dentro |

**Ninguna de las dos la escribió nadie en el controller.** Las produce el manejador del Lab 03. Y
eso es lo que hace una suite: **avisar cuando algo viejo se rompe**.

> **Dos renombres de Boot 4 que muerden aquí**, y que ya venían escritos en el archivo:
> `@WebMvcTest` cambió de paquete (ahora `org.springframework.boot.webmvc.test.autoconfigure`), y
> `@MockBean` **ya no existe**: es `@MockitoBean`, en `org.springframework.test.context.bean.override.mockito`.
> Cualquier ejemplo de internet trae los viejos, y el síntoma es un `cannot find symbol`.

---

## Al terminar

Cuatro archivos, cuatro métodos de test, y `./mvnw test` en verde:

```
[INFO] Tests run: 4, Failures: 0, ... -- in cl.dgt.testing.ProductoServiceTest
[INFO] Tests run: 1, Failures: 0, ... -- in cl.dgt.testing.ContextoDeSpringTest
[INFO] Tests run: 1, Failures: 0, ... -- in cl.dgt.testing.ProductoServiceConDobleTest
[INFO] Tests run: 1, Failures: 0, ... -- in cl.dgt.testing.ProductoControllerTest
[INFO] Tests run: 7, Failures: 0, Errors: 0, Skipped: 0
```

(Siete ejecuciones y cuatro métodos: el parametrizado cuenta sus cuatro casos.)

**Y el cuarto archivo, `ContextoDeSpringTest`, llegó resuelto.** Es el único que levanta el
contexto entero, tarda más que los otros tres juntos, y su trabajo cabe en una frase:

> Comprueba que **Spring arranca** y que los beans se cablean. No prueba ninguna regla: prueba que
> el proyecto es un proyecto. Por eso hay **uno**, y por eso es el más caro.

Lo que hay que poder decir con las propias palabras:

> Un test no sirve por estar verde: sirve por ponerse rojo cuando alguien rompe algo, y por decir
> qué esperaba y qué llegó. Se prueban reglas y bordes, no que una lista tenga cuatro elementos.
> Un doble se usa cuando la dependencia real es lenta, frágil o no se puede controlar — no
> siempre. Y `verify` ata el test a cómo está escrito el método, no a lo que hace.

### Lo que siembra este lab

Hoy los tests corrieron **porque alguien escribió `./mvnw test`**. Esa persona se puede olvidar, o
puede tener prisa, o puede estar de vacaciones el día que llegue el cambio que rompe el IVA.

> **Lo que queda planteado:** una suite que hay que acordarse de correr protege exactamente igual
> que una que no existe.

Lo que falta es que corra **sola**: en cada cambio, antes de que nadie pueda mezclarlo. Eso es
integración continua, y este repositorio tiene una —`.github/workflows/material-ci.yml`— que hace
justo eso con los cuarenta y un proyectos del curso.

Y hay una segunda cosa. Los cuatro tests de hoy comprueban que el código **hace lo que dice**.
Ninguno comprueba que **siga en pie cuando lo usan veinte a la vez**, ni qué pasa cuando el
servicio de al lado no contesta, ni si alguien que no debería puede llamarlo. Eso son los
laboratorios que vienen.
