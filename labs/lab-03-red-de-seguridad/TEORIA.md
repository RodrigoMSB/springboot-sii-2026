# Teoría · Módulo 3 (resto) + Módulo 4

## Índice

1. [Un test es un contrato: se lee al revés](#1-un-test-es-un-contrato-se-lee-al-revés)
2. [Validación declarativa](#2-validación-declarativa)
3. [Tu propio validador: `@RutValido`](#3-tu-propio-validador-rutvalido)
4. [Mensajes internacionalizables](#4-mensajes-internacionalizables)
5. [El error con contrato](#5-el-error-con-contrato)
6. [JUnit 6: qué cambió, y qué te delata si googleas](#6-junit-6-qué-cambió-y-qué-te-delata-si-googleas)
7. [Mockito: mock, stub, spy — y qué NO se mockea](#7-mockito-mock-stub-spy--y-qué-no-se-mockea)
8. [Por qué una suite es una red y un test es un hilo](#8-por-qué-una-suite-es-una-red-y-un-test-es-un-hilo)
9. [AU-05: los tests no duermen](#9-au-05-los-tests-no-duermen)
10. [Tabla DO / DON'T · Glosario](#10-tabla-do--dont--glosario)
11. [Conclusiones y siembra del Módulo 5](#11-conclusiones-y-siembra-del-módulo-5)

---

## 1. Un test es un contrato: se lee al revés

La mayoría lee un test de arriba abajo y se pierde en el setup. Léelo **al revés**: primero
el `assert` (qué se promete), luego el `act` (qué se ejecuta), al final el `arrange` (qué se
prepara). El nombre del test —su `@DisplayName`— es el titular.

```java
@DisplayName("una transición ilegal responde 409 con tipo, origen y destino")
```

Ese nombre es la especificación. El cuerpo solo la hace ejecutable. El que implementa sin
leer el test implementa otra cosa, y su código pasa por casualidad o no pasa.

---

## 2. Validación declarativa

Las reglas de un request no van en un montón de `if` al principio del método. Van en
**anotaciones** sobre el record:

```java
public record CrearTramiteRequest(
        @NotBlank @RutValido String rutContribuyente,
        @NotBlank @Pattern(regexp = "...") String tipo) {}
```

Con `@Valid` en el controlador, Spring rechaza un request inválido **antes** de que llegue a
tu lógica. Tu lógica confía: si la ejecutan, los datos ya son válidos. Y el rechazo es un
400 `ProblemDetail` que **nombra los campos** — un cliente corrige sin adivinar.

---

## 3. Tu propio validador: `@RutValido`

Bean Validation no sabe qué es un RUT: eso es regla chilena. Se escribe una anotación
(`@RutValido`) y un `ConstraintValidator` con el **módulo 11**:

> Multiplica cada dígito del cuerpo, de derecha a izquierda, por la serie cíclica
> 2,3,4,5,6,7; suma; `11 - (suma % 11)` da el dígito verificador (10 → 'K', 11 → '0').

No es difícil. Lo difícil es que **no se puede hardcodear**: los tests parametrizados te dan
seis RUTs distintos. Volvemos a eso en §8.

---

## 4. Mensajes internacionalizables

El mensaje de error no se escribe a mano en la anotación: se referencia por clave.

```java
String message() default "{cl.dgt.rut.invalido}";
```

Y la clave se resuelve en `ValidationMessages.properties`. Mañana hay un
`ValidationMessages_en.properties` y el mismo código habla inglés. Un texto hardcodeado es
una traducción que nunca ocurrirá.

---

## 5. El error con contrato

El dominio lanza `TransicionIlegalException` cuando alguien intenta `BORRADOR → FOLIADO`. Sin
un handler, eso es un 500 con una traza. Un error de negocio merece un **contrato**:

```json
{"type":"https://dgt.cl/errores/transicion-ilegal","status":409,
 "origen":"BORRADOR","destino":"FOLIADO"}
```

El `409 Conflict` dice "tu petición es válida, pero choca con el estado actual". El `type` es
una URL estable que un cliente puede reconocer. El camino triste también es API.

---

## 6. JUnit 6: qué cambió, y qué te delata si googleas

Spring Boot 4 trae **JUnit 6** (Jupiter). Si buscas tutoriales, la mayoría son de JUnit 4 o
5. Señales de que estás leyendo algo viejo:

| Si ves… | Es… | Usa… |
|---|---|---|
| `import org.junit.Test;` (sin `jupiter`) | JUnit 4 | `org.junit.jupiter.api.Test` |
| `@RunWith(...)` | JUnit 4 | `@ExtendWith(...)` |
| `@Before`, `@After` | JUnit 4 | `@BeforeEach`, `@AfterEach` |
| `assertEquals(esperado, real)` a secas | JUnit viejo | AssertJ: `assertThat(real).isEqualTo(esperado)` |

De JUnit 5 a 6 el cambio para ti es mínimo (mismo Jupiter, versión mayor del BOM). Lo que
importa es no copiar código de JUnit 4.

**AAA:** todo test es Arrange–Act–Assert. Y el nombre narra: no `test1()`, sino
`crearConRutInexistenteNoPersiste()`.

---

## 7. Mockito: mock, stub, spy — y qué NO se mockea

- **Mock:** un doble vacío. Verificas cómo se usó (`verify`).
- **Stub:** un mock al que le programas respuestas (`given(...).willReturn(...)`).
- **Spy:** un objeto real al que le espías (o alteras) algún método. Úsalo poco.

**`ArgumentCaptor`** captura lo que le pasaste a un mock, para inspeccionarlo: no solo *que*
llamaste a `save`, sino *qué* guardaste.

**Qué NO se mockea:**
- **Lo que estás probando** (el `TramiteService`): es el sujeto, no un doble.
- **Un dato** (un `record`, un DTO): no tiene comportamiento que simular. Mockear un dato es
  señal de que no entendiste qué es un mock. Se mockean **fronteras** (el repositorio: lento,
  con estado, externo), no valores.

Por eso la inyección por constructor importa: `@InjectMocks` construye el servicio con los
mocks porque el constructor los pide. Un bean inyectado por campo no se deja construir así.

---

## 8. Por qué una suite es una red y un test es un hilo

Pon el validador de RUT a `return true`. El test del caso feliz pasa. ¿Terminaste?

Dos tests más abajo, el parametrizado con `11111111-2` (DV falso) espera `false`, y tu
`return true` lo desmiente. Y el de basura de formato también. **Los tests triangulan**:
engañar a uno es trivial; engañar al conjunto exige implementar de verdad.

Un test suelto es un hilo: se rompe y no sujeta nada. Una suite es una red: cada test cubre
lo que los otros dejan pasar. Por eso te entregamos catorce, no uno.

---

## 9. AU-05: los tests no duermen

Hay un guardián nuevo: `AU-05` falla si cualquier test llama a `Thread.sleep`. Un `sleep` en
un test es una apuesta: pasa en tu máquina rápida y falla en el CI lento, o al revés. Es la
causa nº1 de los tests "flaky" (que pasan a veces).

¿La alternativa? Esperar una **condición**, no un número. Awaitility ya está en el classpath:
`await().until(() -> algoPasó)`. Su uso real llega con los labs asíncronos (Módulo 12); por
ahora, basta saber que `Thread.sleep` en un test está prohibido, y hay un guardián que lo
prueba mordiendo a una clase que duerme a propósito.

---

## 10. Tabla DO / DON'T · Glosario

| ✅ DO | ❌ DON'T |
|---|---|
| Leer el test antes de implementar | Implementar y después mirar si pasa |
| Validación declarativa (anotaciones) | Un muro de `if` al inicio del método |
| Mensaje por clave i18n | Texto hardcodeado en la anotación |
| 409 con contrato para el error de negocio | Dejar salir un 500 con traza |
| `ArgumentCaptor` para verificar *qué* se guardó | `verify(repo).save(any())` y confiar |
| Mockear la frontera (el repositorio) | Mockear un DTO (es un dato) |
| Esperar una condición (Awaitility) | `Thread.sleep` en un test |

- **ConstraintValidator** — la clase que implementa una anotación de validación.
- **Módulo 11** — el algoritmo del dígito verificador del RUT.
- **ProblemDetail** — formato estándar (RFC 9457) de errores de una API.
- **Triangular** — varios tests que, juntos, fijan el comportamiento que uno solo no puede.
- **Flaky** — un test que pasa a veces. Casi siempre por tiempo (`sleep`) o por orden.

---

## 11. Conclusiones y siembra del Módulo 5

Hoy invertiste tu relación con los tests: dejaron de ser algo que escribes *después* para
convertirse en el **enunciado** que lees *antes*. Y escribiste los tuyos, con Mockito.

La red ya existe. Valida, atrapa errores, verifica comportamiento.

🌱 **Siembra del Módulo 5 — "Persistencia con Spring Data JPA e Hibernate 7".**

Y aquí está lo que no ves venir. La próxima semana vas a trabajar con la base de datos de
verdad: entidades, relaciones, consultas. La app vendrá configurada **"como siempre
funcionó"**: cada relación cargándose junto con su dueño, `FetchType.EAGER` por todas partes,
porque así el practicante nunca vio un `LazyInitializationException`.

Y va a funcionar. Los tests van a pasar. Con los datos de prueba —tres trámites, dos
contribuyentes— nadie notará nada.

**Ese es exactamente el punto.** Hay un guardián que ya te lo advirtió sin que lo notaras:
`AU-04` exige que cada relación declare `LAZY` a mano. Todavía no entiendes por qué. La
próxima semana, con la base llena, lo vas a entender de golpe.

El laboratorio del Módulo 5 se llama *«El árbol de trámites»*. Trae paciencia: va a cargar más de lo que
pediste.
