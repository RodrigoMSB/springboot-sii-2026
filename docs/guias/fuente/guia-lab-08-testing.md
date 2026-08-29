---
title: "Lab 08 · La inspección antes de abrir"
subtitle: "Curso de Spring Boot · Servicio de Impuestos Internos · 2026"
date: "90 minutos · Spring Boot 4.1.0 · Java 25 (Temurin)"
abstract-title: "Lo que se demuestra"
abstract: |
  Que un test que nunca se ha puesto rojo no demuestra nada — se rompe la producción a propósito
  para verlo—, y que probar con Spring cuesta: **0,040 s los cuatro tests sin Spring, 1,453 s uno
  solo con el contexto entero**.
lang: es
---

# Antes de empezar

## Qué vas a lograr

Todo lo que has comprobado hasta ahora lo has comprobado **mirando la consola**. Eso no se puede
repetir, no avisa cuando alguien rompe algo, y no se puede correr mil veces.

Hoy escribes tu primer test. Vas a verlo pasar, **vas a romper el código de producción a propósito
para verlo fallar**, y vas a aprender los cuatro niveles a los que se puede probar una aplicación
Spring — con el precio de cada uno medido en segundos.

## Qué necesitas tener listo

| Requisito | Cómo lo compruebas | Qué tiene que salir |
|---|---|---|
| Los labs 02 y 03 hechos | Sabes qué es inyección por constructor | Imprescindible en el paso 4 |
| Estar en la carpeta del lab | `cd labs/lab-08-testing/practica` | El `cd` no da error |
| **Ninguna base de datos** | Este lab no la usa | — |

## Cómo copiar el código de esta guía

**Al copiar de un PDF se pierden los espacios del principio de línea, y a veces una línea larga se
parte en dos.** Con Java no importa. El código completo está en `labs/lab-08-testing/solucion/`.

## La puesta a punto

``` bash
cd labs/lab-08-testing/practica
./mvnw test
```

**Este lab no se arranca: se testea.** El comando de hoy es `./mvnw test`, y no
`spring-boot:run`. No hay puertos que liberar ni procesos que matar.

# El caso

La oficina de la DGT va a abrir. Antes de dejar entrar al público, **hay que inspeccionarla**.

## La inspección, que es la metáfora de este laboratorio

::: metafora
**Se puede inspeccionar a cuatro niveles, y cada uno cuesta distinto.**

1. **Una mesa suelta.** Coges la calculadora del mostrador, le das dos números y compruebas el
   resultado. No hace falta abrir la oficina, ni encender nada, ni que haya nadie. Es un test
   **unitario**, y tarda milésimas.

2. **La mesa con un proveedor de mentira.** Quieres probar al jefe de compras, pero no quieres
   depender de lo que el proveedor real tenga hoy en el almacén. Le pones **un proveedor figurante**
   que entrega exactamente lo que tú digas. Eso es un **doble**, y así los números del test los
   eliges tú.

3. **La ventanilla, sin abrir la puerta de la calle.** Quieres comprobar que la ventanilla atiende
   bien: que responde al número correcto, que sella el papel como debe. Simulas a alguien pidiendo
   por la ventanilla **sin abrir el edificio al público**. Eso es probar la capa web sin levantar
   el servidor.

4. **Abrir la oficina entera.** Todo montado, todas las salas, el conserje haciendo su ronda. Es la
   única forma de comprobar que **el edificio se sostiene** — y es la más cara con diferencia.

La regla que sale de aquí: **inspecciona al nivel más barato que responda tu pregunta.** Hoy vas a
ver los cuatro y cuánto cuesta cada uno, con el reloj delante.
:::

# Los pasos

## Paso 1 · El primer test

### Qué vamos a hacer

Escribir un test de una clase, sin Spring por ninguna parte.

### Para entenderlo mejor

La mesa suelta: la calculadora del IVA, dos números, un resultado.

### El problema

Comprobar un cálculo arrancando la aplicación y mirando la consola tarda segundos, hay que hacerlo
a mano, y **no queda constancia**. Mañana nadie sabe si se comprobó.

### La alternativa, y por qué no

- **Un `main` que imprima el resultado** y mirarlo. Es lo que se hace sin saber que existen los
  tests, y no falla solo: hay que estar delante.
- **Un test**, que es lo de aquí: se corre solo, dice sí o no, y **se vuelve a correr gratis** cada
  vez que alguien toca el código.

Y una decisión de forma que conviene entender: **esto es Java y JUnit, sin Spring**. Mucha gente
cree que testear una aplicación Spring exige levantar Spring. No: la clase que vas a probar es una
clase normal con un constructor, y se prueba como cualquier otra.

### Se pega

Archivo **nuevo** `practica/src/test/java/cl/dgt/testing/ProductoServiceTest.java` — fíjate en que
va bajo **`src/test/java`**, no bajo `src/main/java`:

{{codigo lab=lab-08-testing archivo=src/test/java/cl/dgt/testing/ProductoServiceTest.java modo=metodo nombre=elPrecioConIvaSeRedondeaAlPesoMasCercano lenguaje=java}}

`assertEquals(esperado, obtenido)`: **el primero es lo que esperas**. Al revés, el mensaje de fallo
miente.

### Se corre

``` bash
./mvnw test
```

### Lo que vas a ver

``` text
[INFO] Running cl.dgt.testing.ProductoServiceTest
[INFO] Tests run: 4, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 0.040 s
[INFO] BUILD SUCCESS
```

**0,040 segundos.** Guarda ese número: vuelve en el paso 5.

::: vasbien
`BUILD SUCCESS` y una línea `Tests run:` con `Failures: 0`.
:::

::: atasco
**1 · `No tests were executed!`**

El archivo está en `src/main/java` en vez de `src/test/java`, o el nombre de la clase no acaba en
`Test`. Maven busca por convención.

**2 · `cannot find symbol: class Test`**

Falta `import org.junit.jupiter.api.Test;`. Ojo: es `jupiter`, que es el nombre real de JUnit 5.

**3 · El test pasa pero no comprueba nada.**

Si escribiste el `assertEquals` con los dos argumentos iguales, siempre pasará. El paso 2 está
justo para descubrir eso.
:::

## Paso 2 · El test que avisa — el momento del laboratorio

### Qué vamos a hacer

**Romper el código de producción a propósito** y mirar qué dice el test.

### Para entenderlo mejor

Un detector de humo que nunca ha sonado no está probado: está **sin probar**. La única forma de
saber que funciona es echarle humo.

### El problema

Un test en verde no demuestra que el código esté bien. Demuestra que **el test pasó**. Y un test
mal escrito —que compara algo consigo mismo, o que no llega a ejecutar lo que cree— pasa siempre y
no protege de nada.

**Un test que nunca se ha puesto rojo podría estar comprobando el aire.**

### Se hace

Abre `practica/src/main/java/cl/dgt/testing/services/ProductoService.java` y **cambia la tasa del
IVA** de `0.19` a `0.10`. Es una mentira evidente, y ésa es la gracia.

``` bash
./mvnw test
```

### Lo que vas a ver

``` text
[ERROR] Tests run: 4, Failures: 1, Errors: 0, Skipped: 0 <<< FAILURE!
[ERROR] cl.dgt.testing.ProductoServiceTest.elPrecioConIvaSeRedondeaAlPesoMasCercano <<< FAILURE!
org.opentest4j.AssertionFailedError: expected: <5938> but was: <5489>
	at cl.dgt.testing.ProductoServiceTest.elPrecioConIvaSeRedondeaAlPesoMasCercano(ProductoServiceTest.java:20)
```

**Lee el mensaje entero, porque un buen fallo es media reparación:**

- **Qué esperaba** (`5938`) y **qué salió** (`5489`).
- **Qué test** falló, por su nombre.
- **En qué línea**.

Nadie tuvo que arrancar nada ni mirar ninguna consola. **Ahora vuelve a poner `0.19`** y corre otra
vez: verde. El test está probado.

::: vasbien
Viste el rojo con `expected: <5938> but was: <5489>`, y al deshacer el cambio volvió a verde.
:::

::: atasco
**1 · Cambias el IVA y el test sigue verde.**

**Éste es el hallazgo importante, no un problema tuyo:** significa que tu test no comprueba lo que
crees. Míralo otra vez — probablemente no llama al método que cambiaste.

**2 · Falla más de un test.**

Es normal: el del paso 4 también usa el IVA. Los dos te están avisando de lo mismo.
:::

## Paso 3 · El camino triste

### Qué vamos a hacer

Comprobar que, cuando algo **no** existe, se lanza la excepción que toca.

### Para entenderlo mejor

Inspeccionar qué pasa cuando alguien pide un expediente que no existe. Una oficina bien montada no
se limita a funcionar cuando todo va bien.

### El problema

Casi todo el mundo prueba sólo el camino feliz. Los errores en producción **están casi siempre en
el otro**: el dato que falta, la lista vacía, el id que no está.

### La alternativa, y por qué no

- **Un `try/catch` con `fail()`** en la línea siguiente: seis líneas, y una trampa clásica — si te
  olvidas del `fail()`, el test **pasa cuando NO se lanza la excepción**, justo al revés de lo que
  querías.
- **`assertThrows`**, que es lo de aquí: dice lo mismo en una línea y **devuelve la excepción**,
  así que puedes seguir comprobando lo que lleva dentro.

### Se pega

{{codigo lab=lab-08-testing archivo=src/test/java/cl/dgt/testing/ProductoServiceTest.java modo=metodo nombre=unIdQueNoExisteLanzaProductoNoEncontrado lenguaje=java}}

::: vasbien
El test pasa, y comprueba **además** que el id que viaja dentro de la excepción es el que pediste.
:::

::: atasco
**1 · `Expected ... to be thrown, but nothing was thrown`**

El método no lanza la excepción: probablemente devuelve `null` o un `Optional` vacío. El test te
está diciendo la verdad.
:::

## Paso 4 · Aislar con un doble

### Qué vamos a hacer

Probar el servicio **sin el repositorio real**, poniéndole uno de mentira que devuelve lo que tú
digas.

### Para entenderlo mejor

El proveedor figurante. Quieres comprobar que el jefe de compras **suma bien**, y para eso
necesitas saber exactamente qué hay en el almacén. Si dependes del almacén real, el día que alguien
añada un producto tu inspección deja de cuadrar.

### El problema

Hasta ahora el test usaba el repositorio de verdad, y pasaba **por los datos que ese repositorio
trae dentro**. Si mañana alguien añade un producto a la lista, el test se pone rojo — y no porque
el cálculo esté mal.

**Un test que se rompe cuando el comportamiento no ha cambiado es un impuesto, no una red.**

### La alternativa, y por qué no

- **La implementación real**: gratis y estable **si** los datos no cambian. Aquí cambian.
- **Un doble escrito a mano** (una clase de test que implementa la interfaz): funciona igual de
  bien, y son treinta líneas más que mantener.
- **Mockito con `@Mock`**, que es lo de aquí: el doble se declara en dos líneas y **los datos los
  pone el test**, así que el número que afirmas es el número que elegiste.

Y la extensión: **`@ExtendWith(MockitoExtension.class)` y no `@SpringBootTest`**. Aquí no hay nada
que Spring aporte —se prueba una clase con un constructor—, y el contexto de Spring cuesta lo que
vas a ver en el paso 5. De regalo, la extensión avisa si preparas una respuesta que el código nunca
llega a usar, que es la forma más común de que un test verde no esté probando nada.

### Se pega

Archivo **nuevo** `practica/src/test/java/cl/dgt/testing/ProductoServiceConDobleTest.java`:

{{codigo lab=lab-08-testing archivo=src/test/java/cl/dgt/testing/ProductoServiceConDobleTest.java modo=metodo nombre=elValorDelCatalogoSumaLosPreciosConIva lenguaje=java}}

**Fíjate en que el test elige los datos.** El `3570` que afirma sale de los productos que él mismo
puso, no de lo que hubiera en el repositorio.

### Lo que vas a ver

``` text
[INFO] Running cl.dgt.testing.ProductoServiceConDobleTest
[INFO] Tests run: 2, Failures: 0, Errors: 0, Time elapsed: 0.082 s
```

::: vasbien
El test pasa en menos de una décima, y los productos con los que trabaja están escritos en el
propio test.
:::

::: atasco
**1 · `UnnecessaryStubbingException`**

Preparaste un `when(...)` que el código no llega a llamar. **Es un aviso útil**: o el test no
prueba lo que crees, o sobra esa preparación.

**2 · `NullPointerException` dentro del servicio.**

El doble devuelve `null` por defecto para todo lo que no hayas preparado. Te falta un `when(...)`.

**3 · `@Mock` no crea nada y todo es `null`.**

Falta `@ExtendWith(MockitoExtension.class)` sobre la clase.
:::

## Paso 5 · Los cuatro niveles, y lo que cuesta cada uno

### Qué vamos a hacer

Mirar los cuatro tipos de test corriendo juntos y **comparar los tiempos**.

### Para entenderlo mejor

La inspección de la mesa suelta tarda un segundo. Abrir la oficina entera con todas las salas tarda
mucho más — y a veces es lo único que responde a la pregunta.

### El problema

«Probar más» no es gratis. Una suite que tarda diez minutos **no se corre**, y una suite que no se
corre no protege de nada. Por eso hay que elegir el nivel.

### La alternativa, y por qué no

| Nivel | Qué prueba | Qué **no** ve |
|---|---|---|
| Sin Spring | la lógica de una clase | nada del cableado ni de HTTP |
| Con doble | la lógica, con datos elegidos | lo mismo |
| Capa web (`@WebMvcTest`) | ruta, estado, JSON, manejo de errores | no pasa por un servidor real: ni HTTPS, ni cabeceras del contenedor |
| Todo (`@SpringBootTest`) | **que la aplicación arranca** | tarda un orden de magnitud más |

El de la capa web es el que más gente se salta, y es el que prueba lo que **sólo** Spring MVC hace:
un test unitario del controlador llama a un método y recibe un objeto — **el 404 no aparece por
ninguna parte**.

### Se corre

``` bash
./mvnw test
```

### Lo que vas a ver

``` text
[INFO] Running cl.dgt.testing.ProductoServiceTest
[INFO] Tests run: 4 ... Time elapsed: 0.040 s

[INFO] Running cl.dgt.testing.ContextoDeSpringTest
[INFO] Tests run: 1 ... Time elapsed: 1.453 s

[INFO] Running cl.dgt.testing.ProductoServiceConDobleTest
[INFO] Tests run: 2 ... Time elapsed: 0.082 s

[INFO] Running cl.dgt.testing.ProductoControllerTest
[INFO] Tests run: 2 ... Time elapsed: 0.388 s

[INFO] Tests run: 9, Failures: 0, Errors: 0, Skipped: 0
```

**Míralo bien: cuatro tests sin Spring tardan 0,040 s. UNO con el contexto entero tarda 1,453 s.**
Treinta y seis veces más, por un test.

**Y aun así el caro hace falta**, porque es el único que responde a «¿arranca esto?». Si falta un
bean, si dos se pelean por el mismo nombre, si una propiedad no resuelve — ninguno de los rápidos
se entera.

:::  nota
**Los tiempos van a ser distintos en tu máquina.** Lo que no cambia es la proporción: el contexto
de Spring cuesta un orden de magnitud más que no levantarlo.
:::

::: vasbien
`Tests run: 9, Failures: 0` y puedes señalar en la salida cuál de los cuatro tarda mucho más que
los otros tres juntos.
:::

::: atasco
**1 · El test del contexto falla con `Failed to load ApplicationContext`.**

**Es exactamente para lo que está.** Lee la causa: falta un bean, o hay dos candidatos —el problema
del Lab 02—, o una propiedad no resuelve.

**2 · La suite entera tarda muchísimo.**

Spring reutiliza el contexto entre clases de test que compartan configuración, así que el coste se
paga una vez. Pero basta un `@MockitoBean` distinto para que sea otro contexto y otro pago.
:::

# Lo que aprendiste

**1 · Un test que nunca se ha puesto rojo no demuestra nada.**

Romper la producción a propósito es la única forma de saber que el test mira donde crees. Cinco
minutos, y es lo que separa una red de seguridad de un adorno.

**2 · Probar una aplicación Spring no exige levantar Spring.**

La mayoría de lo que escribes son clases normales con un constructor. Se prueban como cualquier
clase Java, y tardan milésimas.

**3 · Un doble sirve para elegir los datos.**

No es «evitar la base de datos»: es que el número que afirmas sea el que tú pusiste, y no el que
hubiera. Así el test no se rompe cuando alguien añade una fila.

**4 · Cada nivel cuesta, y hay que elegirlo.**

0,040 s los cuatro sin Spring; 1,453 s uno solo con el contexto entero. Se prueba al nivel más
barato que responda la pregunta — y se paga el caro sólo donde hace falta.

# Para profundizar

- **Escribe un `@ParameterizedTest`** para el IVA con cinco importes de borde: 0, 1, y un número
  muy grande.
- **Rompe el controlador** —cambia la ruta— y mira cuál de los cuatro tipos de test se entera.
- **Añade un `@MockitoBean` distinto** a un segundo test con `@SpringBootTest` y mira si la suite
  tarda el doble. Ahí se ve la caché de contextos.
- **Quita el `@ExtendWith`** del test con doble y mira qué error da.

# Antes de cerrar

Este lab **no deja nada corriendo**: no hay servidor ni base que apagar.

``` bash
./mvnw clean
```

**Lo que te llevas:**

> Un test se escribe, se ve fallar a propósito, y se corre solo. Se prueba al nivel más barato que
> responda la pregunta, y el contexto entero se levanta sólo para comprobar que la aplicación
> arranca.

**Lo que queda pendiente, y abre el Lab 09:** todos los endpoints que llevas escritos **los puede
llamar cualquiera**. En el Lab 09 se cierra la puerta, y se aprende la diferencia entre «no te
conozco» y «te conozco, pero esto no es para ti».
