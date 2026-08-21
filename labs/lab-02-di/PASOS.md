# Pasos · Lab 02 · Inyección de dependencias

Seis pasos. Se construyen en `practica/`, en vivo, uno a la vez. Después de cada paso se reinicia
la aplicación y se prueba antes de seguir.

```bash
cd practica
./mvnw spring-boot:run
```

**La aplicación se queda corriendo**: se apaga con **Ctrl+C**. Escucha en el **8083**
(`solucion/`, en el 8084).

Las cuatro carpetas de hoy llegan vacías, y se llenan en este orden:

```
models/         →  repositories/  →  controllers/  →  services/
   paso 1              paso 1-2          paso 2          paso 6
```

---

## Paso 1 · El dato y el contrato

**Se explica:** dos archivos que no hacen nada, y por eso se pueden escribir de un tirón. Un
`record` para el dato, y una **interfaz** para decir qué se le puede pedir a un almacén de
productos. La interfaz es la pieza clave del día: dice **qué** se puede pedir, nunca **cómo** se
hace.

**Se pega (1 de 2):** archivo **nuevo** `practica/src/main/java/cl/dgt/di/models/Producto.java`
— el archivo entero.

```java
package cl.dgt.di.models;

public record Producto(Long id, String nombre, int precio) {
}
```

**Se pega (2 de 2):** archivo **nuevo**
`practica/src/main/java/cl/dgt/di/repositories/ProductoRepository.java` — el archivo entero.

```java
package cl.dgt.di.repositories;

import cl.dgt.di.models.Producto;

import java.util.List;
import java.util.Optional;

public interface ProductoRepository {

    List<Producto> todos();

    Optional<Producto> porId(Long id);
}
```

**En consola:** arranca y no pasa nada. Todavía no hay quien cumpla el contrato.

---

## Paso 2 · Una implementación, y un controller que la usa

**Se explica:** ahora sí, la clase que hace el trabajo. Lleva `@Repository`, que en la práctica
significa **«Spring, esta clase te interesa»**: al arrancar, Spring la encuentra, la construye
una vez, y se la guarda.

Y luego el controller. Mírese bien su constructor, porque ahí está todo el laboratorio.

**Se pega (1 de 2):** archivo **nuevo**
`practica/src/main/java/cl/dgt/di/repositories/ProductoRepositoryLista.java` — el archivo entero.

<!-- pasos:intermedio · el paso 5a le añade @Primary -->

```java
package cl.dgt.di.repositories;

import cl.dgt.di.models.Producto;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class ProductoRepositoryLista implements ProductoRepository {

    private static final List<Producto> DATOS = List.of(
            new Producto(1L, "Resma de papel carta", 4990),
            new Producto(2L, "Tóner negro", 68900),
            new Producto(3L, "Silla ergonómica", 129900),
            new Producto(4L, "Monitor 24 pulgadas", 149900));

    @Override
    public List<Producto> todos() {
        return DATOS;
    }

    @Override
    public Optional<Producto> porId(Long id) {
        return DATOS.stream().filter(p -> p.id().equals(id)).findFirst();
    }
}
```

**Se pega (2 de 2):** archivo **nuevo**
`practica/src/main/java/cl/dgt/di/controllers/ProductoController.java` — el archivo entero.
Mírese bien su constructor, porque ahí está todo el laboratorio.

<!-- pasos:intermedio · el paso 6 lo reescribe para pedir el servicio en vez del repositorio -->

```java
package cl.dgt.di.controllers;

import cl.dgt.di.models.Producto;
import cl.dgt.di.repositories.ProductoRepository;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/productos")
public class ProductoController {

    private final ProductoRepository repositorio;

    public ProductoController(ProductoRepository repositorio) {
        this.repositorio = repositorio;
    }

    @GetMapping
    public List<Producto> listar() {
        return repositorio.todos();
    }

    @GetMapping("/quien")
    public String quien() {
        return repositorio.getClass().getSimpleName();
    }
}
```

**En navegador:** `http://localhost:8083/productos`

```
[{"id":1,"nombre":"Resma de papel carta","precio":4990}, ... ]
```

Y `http://localhost:8083/productos/quien`

```
ProductoRepositoryLista
```

---

## Paso 3 · La pregunta del laboratorio

**Se explica:** aquí se para la sesión y se hace la pregunta, en voz alta, antes de contestarla:

> El controller usa un `ProductoRepositoryLista`. **¿Quién lo construyó?**

Se busca en el proyecto. **No hay ni un `new` en ninguna parte.** No hay tampoco un archivo que
diga «cuando alguien pida un `ProductoRepository`, dale un `ProductoRepositoryLista`».

Lo que pasó, en tres movimientos:

1. Al arrancar, Spring recorrió las clases del proyecto y se fijó en las que llevan anotación
   (`@Repository`, `@RestController`, `@Service`…). Encontró `ProductoRepositoryLista`.
2. La construyó **una sola vez** y se la guardó. A ese objeto guardado se le llama un **bean**, y
   al sitio donde viven, el **contenedor**.
3. Al construir el controller, vio que su constructor pedía un `ProductoRepository`. Buscó entre
   lo que tenía, encontró uno que cumple, y **se lo pasó por el parámetro**.

Eso —el punto 3— es la **inyección de dependencias**: tú declaras qué necesitas, y alguien te lo
entrega ya construido.

**Se escribe:** nada. Este paso es de mirar el código que ya está.

**La comprobación:** el endpoint `/productos/quien` devuelve el nombre de una clase que **el
controller nunca menciona**. Solo menciona la interfaz.

---

## Paso 4 · Dos candidatos: la aplicación deja de arrancar

**Se explica:** hasta aquí sonaba a magia cómoda. Ahora se rompe a propósito, porque el momento
en que falla enseña más que el momento en que funciona.

Una segunda clase que cumple el mismo contrato, con datos que se reconocen a simple vista.

**Se pega:** archivo **nuevo**
`practica/src/main/java/cl/dgt/di/repositories/ProductoRepositoryFalso.java` — el archivo entero.

```java
package cl.dgt.di.repositories;

import cl.dgt.di.models.Producto;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class ProductoRepositoryFalso implements ProductoRepository {

    private static final List<Producto> DATOS = List.of(
            new Producto(1L, "PRODUCTO DE PRUEBA UNO", 1),
            new Producto(2L, "PRODUCTO DE PRUEBA DOS", 2));

    @Override
    public List<Producto> todos() {
        return DATOS;
    }

    @Override
    public Optional<Producto> porId(Long id) {
        return DATOS.stream().filter(p -> p.id().equals(id)).findFirst();
    }
}
```

**En consola:** arrancar. **No arranca.** Y hay que leerlo entero:

```
***************************
APPLICATION FAILED TO START
***************************

Description:

Parameter 0 of constructor in cl.dgt.di.controllers.ProductoController required a single bean, but 2 were found:
	- productoRepositoryFalso: defined in file [.../ProductoRepositoryFalso.class]
	- productoRepositoryLista: defined in file [.../ProductoRepositoryLista.class]

This may be due to missing parameter name information

Action:

Consider marking one of the beans as @Primary, updating the consumer to accept multiple beans,
or using @Qualifier to identify the bean that should be consumed
```

Tres cosas que decir sobre este bloque:

1. **Dice exactamente qué pasa** (dos candidatos), **dónde** (el parámetro 0 de ese constructor)
   y **qué se puede hacer** (tres opciones). No hay que adivinar nada.
2. **Falló al arrancar, no atendiendo a un usuario.** Esa es la mitad del valor de todo este
   mecanismo: los errores de cableado aparecen en el segundo cero, no un martes a las tres de la
   tarde.
3. El bloque termina con un consejo sobre el flag `-parameters` del compilador. **Aquí no
   aplica**: este proyecto ya compila con ese flag —es lo que hace funcionar los
   `@PathVariable` del Lab 01— y Spring lo sugiere siempre, por si acaso. Se ignora.

---

## Paso 5 · Las dos formas de resolverlo

**Se explica:** de las tres opciones que ofrece el error, se prueban las dos que se usan a
diario. Y el punto no es cuál elegir: es **cuánto código hay que cambiar** para que la
aplicación haga otra cosa.

### 5a · `@Primary` — «cuando dudes, esta»

Se le añade `@Primary` a `ProductoRepositoryLista` — una anotación y su import. Como el archivo
es corto, se reemplaza entero: así no hay que acertar dónde va cada línea.

**Se pega:** `practica/src/main/java/cl/dgt/di/repositories/ProductoRepositoryLista.java` — el
archivo entero, **borrando lo que había**.

```java
package cl.dgt.di.repositories;

import cl.dgt.di.models.Producto;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
@Primary
public class ProductoRepositoryLista implements ProductoRepository {

    private static final List<Producto> DATOS = List.of(
            new Producto(1L, "Resma de papel carta", 4990),
            new Producto(2L, "Tóner negro", 68900),
            new Producto(3L, "Silla ergonómica", 129900),
            new Producto(4L, "Monitor 24 pulgadas", 149900));

    @Override
    public List<Producto> todos() {
        return DATOS;
    }

    @Override
    public Optional<Producto> porId(Long id) {
        return DATOS.stream().filter(p -> p.id().equals(id)).findFirst();
    }
}
```

**En navegador:** arranca. `http://localhost:8083/productos/quien`

```
ProductoRepositoryLista
```

### 5b · `@Qualifier` — «esta, y da igual quién sea la primaria»

**Se pega (1 de 2):** en `controllers/ProductoController.java`, **arriba**, con los imports.

<!-- pasos:intermedio · este import se va con el archivo que reescribe el paso 6 -->

```java
import org.springframework.beans.factory.annotation.Qualifier;
```

**Se pega (2 de 2):** en el mismo archivo, **reemplazando el constructor entero**, sin quitar el
`@Primary` de antes.

<!-- pasos:intermedio · el paso 5c lo deja como estaba -->

```java
    public ProductoController(@Qualifier("productoRepositoryFalso") ProductoRepository repositorio) {
        this.repositorio = repositorio;
    }
```

(El nombre entre comillas es el de la clase con la primera letra en minúscula: así llama Spring a
los beans cuando nadie les pone nombre. Es el mismo que salía en el error del paso 4.)

**En navegador:** reiniciar y pedir las dos cosas:

```
http://localhost:8083/productos/quien   ->  ProductoRepositoryFalso
http://localhost:8083/productos         ->  [{"id":1,"nombre":"PRODUCTO DE PRUEBA UNO","precio":1}, ...]
```

**Aquí es donde se para y se dice en voz alta:** la aplicación devuelve datos completamente
distintos. Se cambió **una anotación**. No se tocó el controller —salvo esa anotación—, ni la
interfaz, ni el modelo, ni el `pom.xml`, ni un archivo de configuración.

### 5c · Volver atrás

**Se pega:** en `controllers/ProductoController.java`, **reemplazando el constructor entero**
otra vez, para dejarlo como estaba. El `@Primary` se queda.

<!-- pasos:intermedio · el paso 6 lo reescribe para pedir el servicio -->

```java
    public ProductoController(ProductoRepository repositorio) {
        this.repositorio = repositorio;
    }
```

(El `import` del `@Qualifier` puede quedarse: no molesta, y el paso 6 reescribe el archivo entero.)

**En navegador:** `/productos/quien` vuelve a decir `ProductoRepositoryLista`.

---

## Paso 6 · La capa del medio

**Se explica:** hoy el controller le pide directo al repositorio. Funciona, y con dos endpoints
no molesta. El problema llega con la primera regla que no es ni HTTP ni base de datos —un
descuento, un permiso, un cálculo—: no tiene dónde vivir. En el controller se mezcla con lo de
HTTP; en el repositorio, con lo de los datos.

Por eso se reserva el sitio **antes** de necesitarlo: `controller → service → repository`.

**Se pega (1 de 2):** archivo **nuevo**
`practica/src/main/java/cl/dgt/di/services/ProductoService.java` — el archivo entero.

```java
package cl.dgt.di.services;

import cl.dgt.di.models.Producto;
import cl.dgt.di.repositories.ProductoRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class ProductoService {

    private final ProductoRepository repositorio;

    public ProductoService(ProductoRepository repositorio) {
        this.repositorio = repositorio;
    }

    public List<Producto> catalogo() {
        return repositorio.todos();
    }

    public Optional<Producto> porId(Long id) {
        return repositorio.porId(id);
    }

    public String quienMeAtiende() {
        return repositorio.getClass().getSimpleName();
    }
}
```

**Se pega (2 de 2):** `practica/src/main/java/cl/dgt/di/controllers/ProductoController.java` — el
archivo entero, **borrando lo que había**. Cambian el campo, el constructor y los tres métodos, así
que rehacerlo es más rápido y no deja restos. Y de paso entra el endpoint `/{id}`, que hasta ahora
no estaba.

```java
package cl.dgt.di.controllers;

import cl.dgt.di.models.Producto;
import cl.dgt.di.services.ProductoService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/productos")
public class ProductoController {

    private final ProductoService servicio;

    public ProductoController(ProductoService servicio) {
        this.servicio = servicio;
    }

    @GetMapping
    public List<Producto> listar() {
        return servicio.catalogo();
    }

    @GetMapping("/quien")
    public String quien() {
        return servicio.quienMeAtiende();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Producto> porId(@PathVariable Long id) {
        return servicio.porId(id)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }
}
```

**En navegador / consola:**

```
$ curl http://localhost:8083/productos/quien
ProductoRepositoryLista

$ curl -i http://localhost:8083/productos/2
HTTP/1.1 200
{"id":2,"nombre":"Tóner negro","precio":68900}

$ curl -i http://localhost:8083/productos/99
HTTP/1.1 404
```

**Lo que hay que notar:** apareció una pieza nueva entre las dos, y **nadie tuvo que reconectar
nada**. El servicio declaró que necesita un repositorio, el controller declaró que necesita un
servicio, y Spring armó la cadena. Tres clases, cero `new`.

---

## Al terminar

`practica/` responde exactamente lo mismo que `solucion/`. Si algo no cuadra, `solucion/` está
ahí para comparar archivo por archivo.

Lo que hay que poder decir con las propias palabras —y esto es lo que 17 de 18 no sabían
explicar al empezar el curso:

> Spring es un contenedor. Al arrancar busca las clases anotadas, las construye, y se las entrega
> a quien las declare en su constructor. Por eso se puede cambiar una pieza sin tocar las demás.

### Lo que siembra este lab

Hoy quedó una cosa a medias, y se vio en el paso 6.

`/productos/99` devolvió esto:

```
HTTP/1.1 404
Content-Length: 0
```

**Cero bytes.** El sistema sabía perfectamente qué pasó —le pidieron un producto que no está—
y no lo contó. Quien llama a la API recibe un número y nada más.

Y hay algo peor, que todavía no se ha visto: cuando el error **no** está previsto —una división
por cero, un dato nulo, una conexión caída—, lo que sale por la API es el reverso exacto: la
traza entera, el nombre de la clase que falló, y el mapa del sistema por dentro.

> **La pregunta que abre el Lab 03** — un 404 vacío no dice nada, y un stacktrace dice demasiado.
> ¿Cuál es la respuesta correcta?

El Lab 03 le da forma al camino triste: un error con cuerpo, con código, y sin contar de más.
