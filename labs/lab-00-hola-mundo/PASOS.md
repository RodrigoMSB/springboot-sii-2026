# Pasos · Lab 00 · Hola mundo

Cuatro pasos, quince minutos. Se trabaja en `practica/`, en vivo, uno a la vez. Después de cada
paso se corre el programa y se mira la consola antes de seguir.

```bash
cd practica
./mvnw spring-boot:run
```

El programa **arranca, imprime y termina solo**. No hay que apagarlo con nada.

Solo se toca un archivo Java en todo el laboratorio:
`practica/src/main/java/cl/dgt/hola/HolaMundoApplication.java`.

---

## Paso 1 · Correr algo que todavía no es tuyo

**Se explica:** que el proyecto ya arranca antes de que el alumno escriba una sola línea. Lo que
se corre no es un `main` pelado: `SpringApplication.run(...)` levanta un **contenedor** —un
programa que sabe encontrar clases, construirlas y conectarlas— y después lo apaga. Hoy ese
contenedor no tiene nada dentro; el resto del curso consiste en irle metiendo cosas.

**Se escribe:** nada.

**Se agrega al runner:** nada.

**En consola:** el `./mvnw spring-boot:run` tal cual viene:

```
  .   ____          _            __ _ _
 /\\ / ___'_ __ _ _(_)_ __  __ _ \ \ \ \
( ( )\___ | '_ | '_| | '_ \/ _` | \ \ \ \
 \\/  ___)| |_)| | | | | || (_| |  ) ) ) )
  '  |____| .__|_| |_|_| |_\__, | / / / /
 =========|_|==============|___/=/_/_/_/

 :: Spring Boot ::                (v4.1.0)

... INFO --- [mi-primera-app] [  main] cl.dgt.hola.HolaMundoApplication : Starting HolaMundoApplication using Java 25.0.4 ...
... INFO --- [mi-primera-app] [  main] cl.dgt.hola.HolaMundoApplication : Started HolaMundoApplication in 0.425 seconds
```

Tres cosas que vale la pena señalar en voz alta:

1. El **banner**. Si sale, Java compiló y Maven encontró todo.
2. `using Java 25.0.4` — ese Java **viaja dentro del repositorio**. No es el que tenga instalado
   el alumno, y por eso a todo el mundo le dice lo mismo.
3. `Started ... in 0.425 seconds` — arrancó y **terminó**. Sin errores rojos.

> **Las cuatro líneas `WARNING: ... sun.misc.Unsafe ...` de más arriba no son un problema.** Las
> escribe Maven, antes de que Spring exista. Aparecen siempre, en todos los labs, y se ignoran.

---

## Paso 2 · Que imprima algo tuyo

**Se explica:** dentro de la clase hay un método `run()` anotado con `@Bean`. Devuelve un
`CommandLineRunner`, que es la forma que tiene Spring de decir «esto se ejecuta una vez, cuando
la aplicación ya está lista». **Nadie lo llama desde el `main`.** Spring lo encuentra porque
está anotado, y lo ejecuta él. Esa es, en pequeño, la idea de todo el framework.

**Se pega:** en `practica/src/main/java/cl/dgt/hola/HolaMundoApplication.java`, **reemplazando
las dos líneas de comentario** que hay dentro de `return args -> {`.

```java
            System.out.println();
            System.out.println("  Hola, mundo. Esto lo escribí yo.");
            System.out.println();
```

**En consola:** el mismo arranque de antes, y al final:

```
... INFO --- [mi-primera-app] [  main] cl.dgt.hola.HolaMundoApplication : Started HolaMundoApplication in 0.424 seconds

  Hola, mundo. Esto lo escribí yo.
```

El mensaje sale **después** de `Started`, no antes. Ese orden no es casualidad: el
`CommandLineRunner` corre cuando la aplicación ya está en pie.

**La pregunta del paso:** en el `main` no aparece ninguna llamada a `run()`. ¿Quién lo llamó?

---

## Paso 3 · Cambiar algo sin recompilar a mano

**Se explica:** no todo se cambia tocando Java. `src/main/resources/application.yml` es el
archivo de configuración, y Spring lo lee al arrancar.

**Se pega:** en `practica/src/main/resources/application.yml`, **reemplazando el bloque
`spring:` que ya está** (son sus tres primeras líneas).

```yaml
spring:
  application:
    name: la-app-de-carolina      # el nombre de quien esté al teclado
```

**En consola:** volver a correr y mirar **el corchete de cada línea de log**, que antes decía
`[mi-primera-app]`:

```
... INFO --- [la-app-de-carolina] [  main] cl.dgt.hola.HolaMundoApplication : Starting HolaMundoApplication ...
... INFO --- [la-app-de-carolina] [  main] cl.dgt.hola.HolaMundoApplication : Started HolaMundoApplication in 0.4 seconds
```

Ahí está el nombre. No se tocó una línea de Java.

> **Ojo con lo que NO cambió:** sigue diciendo `Starting HolaMundoApplication`. Eso es el nombre
> de la **clase**; lo del corchete es el nombre de la **aplicación**. Son dos cosas distintas y
> se ven juntas.

Debajo, en el mismo archivo, está `logging.level.root: WARN`. Es lo que mantiene la consola
corta: Spring tiene muchísimo que contar y hoy no toca.

---

## Paso 4 · El mapa: qué hay en el `pom.xml`

**Se explica:** el `pom.xml` es la lista de la compra del proyecto. Se abre y se leen tres cosas,
sin profundizar — hoy es el mapa, no el territorio:

| En el pom | Qué es |
|---|---|
| `<parent>` … `spring-boot-starter-parent` | Alguien ya eligió y probó las versiones de todo. Por eso las dependencias de abajo no llevan `<version>`. |
| `<dependency>` … `spring-boot-starter` | Lo que el proyecto necesita para existir. Hoy hay **una sola**. |
| `<plugin>` … `spring-boot-maven-plugin` | Lo que aporta el comando `spring-boot:run` que se viene usando. |

**Se escribe:** nada.

**En consola:** nada. Este paso es de leer.

**La pregunta del paso:** si mañana la aplicación tiene que responder por HTTP, ¿dónde se pide
eso? (Respuesta: una línea más en `<dependencies>`. Es literalmente el paso 0 del Lab 01.)

---

## Al terminar

`practica/` imprime exactamente lo mismo que `solucion/` — con el nombre de aplicación que cada
uno haya puesto en el paso 3. Si algo no cuadra, `solucion/` está ahí para comparar.

Lo que hay que poder decir con las propias palabras:

> Una aplicación Spring Boot es una clase con `@SpringBootApplication` y un `main`. Al arrancar
> levanta un contenedor, ese contenedor encuentra lo que está anotado, y lo ejecuta.

### Lo que siembra este lab

Hoy la aplicación arrancó, imprimió y se murió. Duró medio segundo.

Eso no le sirve a nadie: un programa que atiende a otros tiene que **quedarse esperando**. Y
para esperar hay que escuchar por algún lado — un puerto, una URL, alguien que pregunta.

> **La pregunta que abre el Lab 01** — ¿qué hay que agregar para que, en vez de terminar, se
> quede arriba y conteste?

En el Lab 01 se agrega esa línea al `pom.xml`, la aplicación deja de terminar, y el primer
método que escriba el alumno responde desde un navegador.
