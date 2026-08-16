# Pasos · Lab 01 · Web

Seis pasos. Se construyen en `practica/`, en vivo, uno a la vez. Después de cada paso se reinicia
la aplicación y se prueba el endpoint antes de seguir.

```bash
cd practica
./mvnw spring-boot:run
```

**La aplicación se queda corriendo**: se apaga con **Ctrl+C** y se vuelve a arrancar después de
cada cambio. Escucha en el puerto **8081** (`solucion/`, en el 8082).

Todo lo que se escribe hoy va en dos carpetas que hoy llegan vacías:

- `practica/src/main/java/cl/dgt/web/controllers/`
- `practica/src/main/java/cl/dgt/web/dto/`

---

## Paso 0 · Antes de escribir nada

Arrancar el proyecto tal como viene y pedir `http://localhost:8081/hola` en el navegador.

```
HTTP/1.1 404
{"timestamp":"...","status":404,"error":"Not Found","path":"/hola"}
```

**404 es la respuesta correcta**: esa ruta no existe todavía. Lo importante es lo otro — la
aplicación **no terminó**. Está ahí, esperando. Comparado con el Lab 00, lo único que cambió es
una línea del `pom.xml`:

```xml
<dependency>
  <groupId>org.springframework.boot</groupId>
  <artifactId>spring-boot-starter-web</artifactId>
</dependency>
```

Esa línea trajo un servidor, el mecanismo de rutas y el conversor a JSON. Las tres cosas del
laboratorio de hoy.

---

## Paso 1 · El primer endpoint

**Se explica:** un endpoint es un método. `@RestController` marca la clase como «esta atiende
peticiones»; `@GetMapping("/hola")` marca el método como «yo respondo a esa ruta». Spring lee
las anotaciones al arrancar y arma la tabla de rutas. **Nadie registra nada a mano.**

**Se escribe:** `practica/src/main/java/cl/dgt/web/controllers/HolaController.java`

```java
package cl.dgt.web.controllers;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HolaController {

    @GetMapping("/hola")
    public String hola() {
        return "Hola, mundo.";
    }
}
```

**En consola / navegador:** reiniciar y pedir `http://localhost:8081/hola`

```
HTTP/1.1 200
Content-Type: text/plain;charset=UTF-8

Hola, mundo.
```

Ese texto salió de un método escrito hace treinta segundos. **El código del alumno respondió por
HTTP.**

**La pregunta del paso:** ¿quién decidió que este método atendiera `/hola`? Nadie escribió una
tabla de rutas: la escribió Spring leyendo la anotación.

---

## Paso 2 · El dato viene en la URL

**Se explica:** las llaves en la ruta marcan un hueco. `@PathVariable` dice «lo que venga en ese
hueco, mételo en este parámetro». Se usa cuando la ruta **identifica una cosa concreta**.

**Se escribe:** en el mismo controller, un método más:

```java
@GetMapping("/hola/{nombre}")
public String holaANombre(@PathVariable String nombre) {
    return "Hola, " + nombre + ".";
}
```

**En navegador:** `http://localhost:8081/hola/Carolina` y después `.../hola/Ignacio`

```
Hola, Carolina.
Hola, Ignacio.
```

Dos respuestas distintas, un método escrito una sola vez.

---

## Paso 3 · El dato viene después del `?`

**Se explica:** la otra forma. `@RequestParam` lee lo que va tras el `?` en la URL. Y aquí va la
duda clásica del día, que conviene dejar dicha en voz alta:

| | Cuándo se usa |
|---|---|
| `@PathVariable` — `/productos/7` | La ruta **identifica** algo. El 7 *es* el recurso. |
| `@RequestParam` — `/productos?orden=precio` | Modifica **cómo** se pide. El recurso es el mismo. |

**Se escribe:**

```java
@GetMapping("/saludo")
public String saludo(@RequestParam String nombre,
                     @RequestParam(defaultValue = "false") boolean formal) {
    return formal ? "Buenos días, " + nombre + "." : "Hola, " + nombre + ".";
}
```

**En navegador:**

```
http://localhost:8081/saludo?nombre=Carolina                -> Hola, Carolina.
http://localhost:8081/saludo?nombre=Carolina&formal=true    -> Buenos días, Carolina.
```

El `defaultValue` es lo que permite omitir `formal`. Sin él, pedir la URL sin ese parámetro
daría un **400**: vale la pena probarlo quitándolo un momento.

---

## Paso 4 · Devolver un objeto, no un texto

**Se explica:** hasta ahora se devolvían frases. Una API real devuelve **datos con estructura**.
Se escribe un `record` —una clase de datos de una línea— y se devuelve tal cual: nadie arma el
JSON a mano.

**Se escribe:** `practica/src/main/java/cl/dgt/web/dto/SaludoDto.java`

```java
package cl.dgt.web.dto;

public record SaludoDto(String mensaje, String para, boolean formal) {
}
```

y en el controller:

```java
@GetMapping("/saludos/{nombre}")
public SaludoDto saludoDe(@PathVariable String nombre) {
    return new SaludoDto("Hola, " + nombre + ".", nombre, false);
}
```

**En navegador:** `http://localhost:8081/saludos/Carolina`

```
HTTP/1.1 200
Content-Type: application/json

{"mensaje":"Hola, Carolina.","para":"Carolina","formal":false}
```

Dos cosas que señalar:

1. El `Content-Type` cambió solo, de `text/plain` a `application/json`.
2. Los nombres de los campos del JSON son **los del record**, tal cual. Si se le cambia el
   nombre a un campo en Java, cambia la API.

**Quién lo convirtió:** una librería llamada Jackson, que vino dentro de `starter-web` sin que
nadie la pidiera por su nombre.

---

## Paso 5 · El dato viene en el cuerpo

**Se explica:** tercera y última forma de recibir datos, y la que se usa cuando hay que mandar
algo más grande que una palabra. `@RequestBody` es el camino inverso del paso 4: el JSON que
manda el cliente se convierte en objeto Java **antes** de que empiece el método.

**Se escribe:** `practica/src/main/java/cl/dgt/web/dto/SolicitudSaludoDto.java`

```java
package cl.dgt.web.dto;

public record SolicitudSaludoDto(String nombre, boolean formal) {
}
```

y en el controller:

```java
@PostMapping("/saludos")
public SaludoDto crearSaludo(@RequestBody SolicitudSaludoDto solicitud) {
    String texto = solicitud.formal()
            ? "Buenos días, " + solicitud.nombre() + "."
            : "Hola, " + solicitud.nombre() + ".";
    return new SaludoDto(texto, solicitud.nombre(), solicitud.formal());
}
```

**Se prueba con Postman** (`POST`, cuerpo *raw · JSON*), o con `curl`:

```bash
curl -i -X POST http://localhost:8081/saludos \
     -H 'Content-Type: application/json' \
     -d '{"nombre":"Carolina","formal":true}'
```

```
HTTP/1.1 200
Content-Type: application/json

{"mensaje":"Buenos días, Carolina.","para":"Carolina","formal":true}
```

**La pregunta del paso:** ¿por qué esto no se puede probar escribiendo la URL en el navegador?
Porque la barra del navegador solo sabe hacer `GET`, y no tiene dónde escribir un cuerpo.

---

## Paso 6 · El código de estado también es la respuesta

**Se explica:** hasta ahora todo respondió **200**, incluso el POST que crea algo. Está mal, y no
es una sutileza: quien llama a la API decide qué hacer **mirando el código**, antes de abrir el
cuerpo. `ResponseEntity` es el sobre que envuelve la respuesta y lleva el código escrito por
fuera.

**Se escribe:** se cambian los dos métodos del paso 4 y del paso 5.

El GET, que además ahora puede decir «no está»:

```java
private static final List<String> CONOCIDOS = List.of("mundo", "Carolina", "Ignacio");

@GetMapping("/saludos/{nombre}")
public ResponseEntity<SaludoDto> saludoDe(@PathVariable String nombre) {
    if (!CONOCIDOS.contains(nombre)) {
        return ResponseEntity.notFound().build();
    }
    return ResponseEntity.ok(new SaludoDto("Hola, " + nombre + ".", nombre, false));
}
```

El POST, que crea:

```java
return ResponseEntity.status(HttpStatus.CREATED).body(saludo);
```

**En consola:** los tres códigos, uno detrás de otro:

```
$ curl -i http://localhost:8081/saludos/Carolina
HTTP/1.1 200
{"mensaje":"Hola, Carolina.","para":"Carolina","formal":false}

$ curl -i http://localhost:8081/saludos/Pedro
HTTP/1.1 404
Content-Length: 0

$ curl -i -X POST http://localhost:8081/saludos -H 'Content-Type: application/json' -d '{"nombre":"Carolina","formal":true}'
HTTP/1.1 201
{"mensaje":"Buenos días, Carolina.","para":"Carolina","formal":true}
```

**La pregunta del paso, y hay que dejarla en el aire:** mirar bien el 404.

```
HTTP/1.1 404
Content-Length: 0
```

**Cero bytes.** Dice que no está, pero no dice qué se pidió, ni por qué no está, ni qué hacer.
¿Le sirve así a quien lo recibe?

---

## Al terminar

`practica/` responde exactamente lo mismo que `solucion/` en los cinco endpoints. Si algo no
cuadra, `solucion/` está ahí para comparar archivo por archivo.

Lo que hay que poder decir con las propias palabras:

> Un endpoint es un método anotado. Los datos entran por la ruta, por parámetros o por el
> cuerpo. Lo que se devuelve se convierte a JSON solo, y el código de estado se controla con
> `ResponseEntity`.

### Lo que siembra este lab

Hoy el controller hizo **todo**: recibió la petición, decidió, armó la respuesta y hasta guardó
la agenda de nombres conocidos en una lista dentro de sí mismo.

Eso aguanta cinco endpoints. No aguanta cincuenta.

> **La pregunta que abre el Lab 02** — si los datos no van a vivir dentro del controller, alguien
> tiene que dárselos. ¿Quién construye a ese alguien, y cómo llega hasta aquí?

En el Lab 02 la lista sale del controller, se convierte en una pieza aparte, y aparece el
mecanismo que las conecta sin que nadie haga `new`. Y el 404 vacío del paso 6 tiene su propio
laboratorio: el 03.
