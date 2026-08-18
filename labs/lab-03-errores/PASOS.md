# Pasos · Lab 03 · Errores con forma

Cinco pasos. Se trabaja en `practica/`, en vivo, uno a la vez. Después de cada paso se reinicia
la aplicación y se prueba antes de seguir.

```bash
cd practica
./mvnw spring-boot:run
```

**La aplicación se queda corriendo**: se apaga con **Ctrl+C**. Escucha en el **8085**
(`solucion/`, en el 8086).

Hoy hay una diferencia con los labs anteriores: **el controller ya está escrito y funciona**. En
toda la sesión se le tocan dos líneas. Lo que se escribe va en una carpeta que llega vacía,
`exceptions/`, y en `dto/`.

---

## Paso 1 · Mirar lo que sale hoy

**Se explica:** nada todavía. Primero se mira.

**Se escribe:** nada.

**En consola:**

```bash
curl -s http://localhost:8085/productos/99
```

```json
{"timestamp":"...","status":500,"error":"Internal Server Error",
 "exception":"java.util.NoSuchElementException",
 "trace":"java.util.NoSuchElementException: No value present\n\tat java.base/java.util.Optional.orElseThrow(Optional.java:377)\n\tat cl.dgt.errores.controllers.ProductoController.porId(ProductoController.java:59)\n\tat ...
 (siguen unas cuarenta líneas más)
 ...\n\tat java.base/java.lang.Thread.run(Thread.java:1474)\n",
 "message":"No value present","path":"/productos/99"}
```

Hay que leerlo entero, aunque dé pereza, porque **todo lo que hay ahí está mal**:

| Lo que dice | Por qué está mal |
|---|---|
| `500 Internal Server Error` | **No es un error del servidor.** Preguntaron por un producto que no existe: eso es normal, y el sistema funcionó perfectamente. |
| `"message":"No value present"` | Eso no le dice nada a quien llama. Ni siquiera menciona el id 99, ni la palabra «producto». |
| `"exception":"java.util.NoSuchElementException"` | Es un detalle de cómo está hecho el sistema por dentro. A quien consume la API no le incumbe. |
| `"trace": ...` (40 líneas) | **Es el mapa del sistema.** Versión de Spring, versión de Tomcat, nombres de clases, rutas de archivo, números de línea. |

Y la línea que lo provoca es una sola, en `ProductoController`:

```java
.orElseThrow();          // ← sin argumentos: lanza NoSuchElementException
```

> **La filtración está encendida a propósito.** En `application.yml` hay tres ajustes bajo
> `spring.web.error` puestos para que esto se vea. Sin ellos, el cuerpo sería solo
> `{"status":500,"error":"Internal Server Error"}` y la traza estaría en la consola del servidor.
> **Se quitan en el paso 5.**
>
> Y una nota para quien busque esto después: en Spring Boot 4 estas propiedades viven bajo
> `spring.web.error`. Si se escriben como `server.error.include-stacktrace` —que es como se
> escribían antes— **la aplicación arranca igual y no pasa nada**. No avisa. Simplemente se
> ignoran.

---

## Paso 2 · Una excepción que dice lo que pasó

**Se explica:** el primer arreglo no es de HTTP: es de vocabulario. «No hay valor» es lo que le
pasó a un `Optional`. Lo que le pasó al **sistema** es que no existe el producto 99. Se escribe
una excepción que diga eso.

Fíjate en lo que **no** lleva: ninguna anotación de HTTP, ningún 404. Esta clase no sabe que
existe la web, y por eso serviría igual en un programa de consola.

**Se escribe:** `practica/src/main/java/cl/dgt/errores/exceptions/ProductoNoEncontradoException.java`

```java
package cl.dgt.errores.exceptions;

public class ProductoNoEncontradoException extends RuntimeException {

    public ProductoNoEncontradoException(Long id) {
        super("No existe el producto con id " + id + ".");
    }
}
```

y en `ProductoController`, la línea del `orElseThrow()`:

```java
.orElseThrow(() -> new ProductoNoEncontradoException(id));
```

**En consola:** volver a pedir `curl -s http://localhost:8085/productos/99`

```json
{"timestamp":"...","status":500,"error":"Internal Server Error",
 "exception":"cl.dgt.errores.exceptions.ProductoNoEncontradoException",
 "trace":"cl.dgt.errores.exceptions.ProductoNoEncontradoException: No existe el producto con id 99. ...",
 "message":"No existe el producto con id 99.","path":"/productos/99"}
```

**Sigue siendo un 500 y sigue saliendo la traza.** El mensaje mejoró, y nada más. Y eso es
importante decirlo en voz alta: **una excepción bien nombrada no arregla la respuesta**. Falta
quien la traduzca.

---

## Paso 3 · El traductor

**Se explica:** aquí está la pieza del laboratorio. Un `@RestControllerAdvice` es una clase que
Spring consulta **cuando un controller lanza algo**: busca dentro un `@ExceptionHandler` que
acepte esa excepción, y usa lo que devuelva como respuesta.

No hay que registrarla en ninguna parte. No hay que llamarla desde ningún sitio. Y vale para
**todos** los controllers de la aplicación, presentes y futuros.

**Se escribe:** primero la forma que van a tener todos los errores,
`practica/src/main/java/cl/dgt/errores/dto/ErrorRespuesta.java`

```java
package cl.dgt.errores.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.time.Instant;
import java.util.Map;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record ErrorRespuesta(String mensaje, int codigo, Instant timestamp, Map<String, String> campos) {

    public static ErrorRespuesta de(String mensaje, int codigo) {
        return new ErrorRespuesta(mensaje, codigo, Instant.now(), null);
    }
}
```

(`campos` se usa en el paso 4. `@JsonInclude(NON_NULL)` hace que, mientras esté vacío, ni
aparezca en el JSON.)

y el traductor, `practica/src/main/java/cl/dgt/errores/exceptions/ManejadorDeErrores.java`

```java
package cl.dgt.errores.exceptions;

import cl.dgt.errores.dto.ErrorRespuesta;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class ManejadorDeErrores {

    @ExceptionHandler(ProductoNoEncontradoException.class)
    public ResponseEntity<ErrorRespuesta> noEncontrado(ProductoNoEncontradoException e) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ErrorRespuesta.de(e.getMessage(), 404));
    }
}
```

**En consola:**

```bash
$ curl -i -s http://localhost:8085/productos/99
HTTP/1.1 404
Content-Type: application/json

{"mensaje":"No existe el producto con id 99.","codigo":404,"timestamp":"2026-08-15T21:41:45.861780Z"}
```

**404, con cuerpo, sin una línea de traza.** Y compárese con el 404 del Lab 01, que traía
`Content-Length: 0`.

**Lo que hay que notar:** el controller no menciona el número 404 en ningún sitio. Lanza lo que
pasó; la traducción a HTTP vive en un solo archivo. Cambiar el formato de todos los errores de la
API es cambiar ese archivo.

---

## Paso 4 · Lo que manda mal quien llama

**Se explica:** hasta ahora el error lo provocaba pedir algo que no está. Ahora el otro caso:
mandar algo inválido. Hoy el POST acepta cualquier cosa — se puede probar antes de arreglarlo:

```bash
$ curl -i -s -X POST http://localhost:8085/productos \
       -H 'Content-Type: application/json' -d '{"nombre":"","precio":-5}'
HTTP/1.1 201
{"id":4,"nombre":"","precio":-5}
```

Un producto sin nombre y con precio negativo, creado tan tranquilamente.

**Se escribe:** las anotaciones en `dto/ProductoNuevoDto.java`

```java
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;

public record ProductoNuevoDto(
        @NotBlank(message = "el nombre es obligatorio") String nombre,
        @Positive(message = "el precio debe ser mayor que cero") int precio) {
}
```

un `@Valid` en el controller —**la segunda y última línea que se toca ahí**—:

```java
public ResponseEntity<Producto> crear(@Valid @RequestBody ProductoNuevoDto nuevo) {
```

y un segundo handler en `ManejadorDeErrores`:

```java
@ExceptionHandler(MethodArgumentNotValidException.class)
public ResponseEntity<ErrorRespuesta> validacion(MethodArgumentNotValidException e) {
    Map<String, String> campos = new HashMap<>();
    e.getBindingResult().getFieldErrors()
            .forEach(error -> campos.put(error.getField(), error.getDefaultMessage()));

    ErrorRespuesta cuerpo = new ErrorRespuesta(
            "Hay datos inválidos en la petición.", 400, Instant.now(), campos);
    return ResponseEntity.badRequest().body(cuerpo);
}
```

**En consola:** el mismo POST de antes:

```
HTTP/1.1 400
{"mensaje":"Hay datos inválidos en la petición.","codigo":400,"timestamp":"...",
 "campos":{"precio":"el precio debe ser mayor que cero","nombre":"el nombre es obligatorio"}}
```

Tres cosas que señalar:

1. **400, no 500.** El sistema no falló: le mandaron mal los datos.
2. Vienen **los dos campos malos a la vez**, no solo el primero. Quien llama arregla todo de una
   pasada en vez de descubrir los errores de uno en uno.
3. El método `crear()` **no llegó a ejecutarse**. `@Valid` corta antes.

---

## Paso 5 · La red de seguridad, y lo que se calla

**Se explica:** faltan los errores que nadie previó. Para verlos hay un endpoint que divide el
precio en cuotas:

```bash
$ curl -s http://localhost:8085/productos/1/cuota?cuotas=0
```

Sale otra vez la traza completa, ahora de una `ArithmeticException: / by zero`. **Y aquí está la
diferencia con el paso 1:** ese error no se puede prever uno por uno, porque son infinitos. Lo
que se puede hacer es no dejar salir ninguno.

**Se escribe:** el tercer handler, con dos ideas dentro:

```java
private static final Logger log = LoggerFactory.getLogger(ManejadorDeErrores.class);

@ExceptionHandler(Exception.class)
public ResponseEntity<ErrorRespuesta> todoLoDemas(Exception e) {
    log.error("Error no previsto atendiendo una petición", e);       // ← todo, para la casa
    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
            .body(ErrorRespuesta.de("Ocurrió un error inesperado. Inténtalo más tarde.", 500));
}
```

y se **quitan** del `application.yml` los tres ajustes de `spring.web.error`, que ya no pintan
nada:

```yaml
  web:                          # ← se borra este bloque entero
    error:
      include-message: always
      include-exception: true
      include-stacktrace: always
```

**En consola:**

```
$ curl -i -s http://localhost:8085/productos/1/cuota?cuotas=0
HTTP/1.1 500
{"mensaje":"Ocurrió un error inesperado. Inténtalo más tarde.","codigo":500,"timestamp":"..."}
```

y **en la consola del servidor**, al mismo tiempo:

```
ERROR ... c.d.e.exceptions.ManejadorDeErrores : Error no previsto atendiendo una petición
java.lang.ArithmeticException: / by zero
	at cl.dgt.errores.controllers.ProductoController.cuota(ProductoController.java:...)
	...
```

**La información no se perdió: cambió de destinatario.** Quien llama recibe lo justo para saber
que reintente. Quien mantiene el sistema recibe todo, en el log, que es de la casa.

### Y el efecto colateral, que hay que probar

`@ExceptionHandler(Exception.class)` atrapa **todo**. También lo que no se quería. Pídase una
dirección que no existe:

```bash
$ curl -i -s http://localhost:8085/noexiste
HTTP/1.1 500
{"mensaje":"Ocurrió un error inesperado. Inténtalo más tarde.","codigo":500,...}
```

**500 por escribir mal una URL.** Spring lanza una `NoResourceFoundException` para decir «esa
ruta no corresponde a nada», y el handler general se la tragó como si fuera un fallo del sistema.

**Se escribe:** un handler más, antes del general:

```java
import org.springframework.web.servlet.resource.NoResourceFoundException;

@ExceptionHandler(NoResourceFoundException.class)
public ResponseEntity<ErrorRespuesta> rutaNoExiste(NoResourceFoundException e) {
    return ResponseEntity.status(HttpStatus.NOT_FOUND)
            .body(ErrorRespuesta.de("La ruta pedida no existe.", 404));
}
```

```
$ curl -i -s http://localhost:8085/noexiste
HTTP/1.1 404
{"mensaje":"La ruta pedida no existe.","codigo":404,"timestamp":"..."}
```

**La moraleja del paso, y es la más útil del día:** una red que atrapa todo atrapa también lo que
no debía. Después de poner un handler general hay que **volver a probar los caminos que ya
funcionaban**, porque puede habérselos comido.

---

## Al terminar

`practica/` responde exactamente lo mismo que `solucion/` en las cuatro situaciones. Si algo no
cuadra, `solucion/` está ahí para comparar archivo por archivo.

Lo que hay que poder decir con las propias palabras:

> Un error previsto se traduce a su código y se explica. Uno no previsto se registra entero
> dentro y se cuenta de forma genérica fuera. Y todos tienen la misma forma, porque quien consume
> la API tiene que escribir el código que lee errores una sola vez.

### Lo que siembra este lab

Con estos cuatro laboratorios la aplicación ya arranca, responde, se conecta sola por dentro y
falla con educación. Lo que no hace es **acordarse de nada**: cada vez que se apaga, el producto
creado con el POST desaparece. La lista vive en memoria, dentro de un objeto.

> **La pregunta que abre el Lab 04** — ¿dónde se guarda lo que tiene que seguir ahí mañana?

El Lab 04 mete una base de datos de verdad debajo, y esa lista de tres productos escrita a mano
en el controller se convierte en una tabla. La misma pieza que hoy es un `ArrayList` pasa a ser
un repositorio — y el nombre no es casualidad: es el mismo del Lab 02.
