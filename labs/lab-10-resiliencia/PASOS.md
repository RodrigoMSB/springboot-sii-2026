# Pasos · Lab 10 · Resiliencia

Cinco pasos. Se trabaja en `practica/`, en vivo. Cada paso cambia un número que se mide en
pantalla.

```bash
cd practica
./mvnw spring-boot:run
```

Escucha en el **8097** (`solucion/`, en el 8098). Tesorería simulada, en el 9097.

Lo que llega hecho: Tesorería simulada, el mando a distancia, el controller. Lo que se escribe
hoy son dos archivos:

```
tesoreria/ClienteTesoreria.java  →  paso 2 (el timeout)
services/PagoService.java        →  pasos 3, 4 y 5
```

**Un aviso antes de empezar:** los tiempos que salen abajo son los medidos al preparar el
material. Los de la sala variarán en las centésimas; lo que no varía son los órdenes de magnitud,
que es lo que enseña el laboratorio.

---

## Paso 0 · Todo bien

**Se explica:** la aplicación consulta pagos en Tesorería, que es otro servicio. Hoy Tesorería es
WireMock dentro de este mismo proceso — no hay contenedor ni segundo terminal— y viene con mando a
distancia para ponerla lenta o tirarla abajo.

**Se corre:**

```bash
curl -X POST http://localhost:8097/simulador/sana
curl -w "  (%{time_total}s)\n" http://localhost:8097/pagos/77
```

**En consola:**

```
{"estado":"PAGADO","monto":45000}  (0.197819s)
```

Doscientas milésimas. Todo bien, y así se va a quedar exactamente un minuto.

---

## Paso 1 · La espera infinita

**Se explica:** Tesorería no se cae. Se pone **lenta**, que es peor, y es lo que de verdad pasa en
producción: la base tiene un bloqueo, el disco se llena, alguien lanzó una consulta que no
termina. El servicio sigue vivo, acepta la conexión, y no contesta.

Antes de correr nada, se hace la pregunta y se pide una mano alzada:

> Tesorería va a tardar **30 segundos** en responder. ¿Cuánto va a esperar nuestro usuario?

**Se corre:**

```bash
curl -X POST "http://localhost:8097/simulador/lenta?segundos=30"
curl -w "  (%{time_total}s)\n" http://localhost:8097/pagos/77
```

**En consola** — y aquí conviene callarse y dejar que pase el tiempo, los treinta segundos
enteros, mirando la terminal:

```
  el usuario esperó: 30.006042s
```

**Treinta segundos y seis milésimas.** Nuestra aplicación esperó todo lo que Tesorería quiso.

**Lo que hay que notar, y es la idea del paso:**

> El valor por defecto de un cliente HTTP **no es un timeout largo. Es ningún timeout.**

Si Tesorería hubiera tardado diez minutos, habríamos esperado diez minutos. Y no es un usuario
esperando: es **un hilo de Tomcat bloqueado**. Con doscientos hilos y doscientas peticiones a un
servicio colgado, la aplicación entera deja de atender a nadie — incluidos los endpoints que no
tienen nada que ver con Tesorería.

> Un servicio lento no te pone lento. **Te tumba.**

---

## Paso 2 · Fallar rápido

**Se explica:** la primera medida no es reintentar ni nada sofisticado: es **decidir cuánto se
está dispuesto a esperar**. Dos timeouts, que son cosas distintas:

- **de conexión** — cuánto se espera a que el otro extremo acepte la conexión
- **de lectura** — cuánto se espera a que conteste, una vez conectado

El paso 1 murió en el segundo: la conexión se aceptó al instante y la respuesta no llegaba.

**Se pega:** en `practica/src/main/java/cl/dgt/resiliencia/tesoreria/ClienteTesoreria.java`,
**reemplazando el constructor entero**, y sus imports **arriba** si te faltan.

```java
    private static final Duration TIMEOUT_CONEXION = Duration.ofSeconds(2);
    private static final Duration TIMEOUT_LECTURA = Duration.ofSeconds(2);

    public ClienteTesoreria(@Value("${lab09.tesoreria.puerto}") int puerto) {
        JdkClientHttpRequestFactory fabrica = new JdkClientHttpRequestFactory(
                HttpClient.newBuilder().connectTimeout(TIMEOUT_CONEXION).build());
        fabrica.setReadTimeout(TIMEOUT_LECTURA);

        this.http = RestClient.builder()
                .baseUrl("http://localhost:" + puerto)
                .requestFactory(fabrica)
                .build();
    }
```

> **Por qué se fija el transporte a mano** (`JdkClientHttpRequestFactory`) en vez de dejar que
> Spring elija: WireMock arrastra Apache HttpClient 5, Spring prefiere Apache si lo encuentra en
> el classpath, y **Apache reintenta solo**. Este laboratorio cuenta llamadas y mide tiempos: un
> reintento invisible del transporte falsearía los dos. Es una lección cara del material
> (D-025-3), y aquí se aplica de entrada.

**Se corre:** mismo escenario, Tesorería a 30 segundos.

**En consola:**

```
  el usuario espera: 2.040460s

{"timestamp":"...","status":500,"error":"Internal Server Error","path":"/pagos/77"}
```

**De 30 segundos a 2.** El hilo se liberó y la aplicación sigue atendiendo a todos los demás.

**Y ahora la mitad incómoda del paso**, que es la que no hay que saltarse:

> Falla **rápido**. Pero falla.

El usuario ya no espera medio minuto: ahora recibe un 500 en dos segundos. Es mejor —muchísimo
mejor para el sistema— y sigue siendo un error en la cara de alguien que quería consultar su pago.
Un timeout es la primera medida, no la última. Los tres pasos que quedan son sobre eso.

---

## Paso 3 · Reintentar, y cuándo no

**Se explica:** muchos fallos de red son **transitorios**: un paquete perdido, un reinicio de un
segundo, el momento exacto en que el otro lado cerró la conexión. Reintentar cuesta poco y salva
esos casos.

**Se pega:** en `practica/src/main/java/cl/dgt/resiliencia/services/PagoService.java`, el
reintento y su uso. Los imports van **arriba**; lo demás, **dentro de la clase**.

```java
        this.reintento = Retry.of("tesoreria", RetryConfig.custom()
                .maxAttempts(3)
                .waitDuration(Duration.ofMillis(200))
                .build());

        reintento.getEventPublisher().onRetry(e ->
                log.info(">>> REINTENTO n.º {}", e.getNumberOfRetryAttempts()));
```

<!-- pasos:intermedio · el paso 4 envuelve esta llamada en el circuito -->

```java
    public Map<String, Object> consultar(String id) {
        return Retry.decorateSupplier(reintento, () -> cliente.consultarPago(id)).get();
    }
```

**Se corre:** otra vez Tesorería a 30 segundos.

**En consola:**

```
>>> REINTENTO n.º 1
>>> REINTENTO n.º 2
  el usuario espera: 6.438910s      ← llamadas HTTP reales: 3
```

**Y aquí se para**, porque el número acaba de ir en la dirección equivocada:

| | usuario espera | llamadas a Tesorería |
|---|---|---|
| paso 2, sólo timeout | 2,04 s | 1 |
| paso 3, con 3 intentos | **6,44 s** | **3** |

> **Reintentar un servicio caído lo empeora dos veces.** El usuario espera el triple, y el
> servicio que se está muriendo recibe el triple de tráfico justo cuando menos puede con él.

Eso tiene nombre —**tormenta de reintentos**— y es una de las formas más comunes de convertir una
caída parcial en una caída total: mil clientes reintentando tres veces son tres mil peticiones
sobre un servicio que ya no daba abasto con mil.

La conclusión no es «no reintentar». Es:

> Un reintento sirve cuando el fallo es **transitorio**. Contra un servicio que está caído de
> verdad, hace falta algo que **deje de intentarlo**.

Y eso es el paso 4.

---

## Paso 4 · El circuito

**Se explica:** el nombre viene del automático de la casa. Cuando hay un cortocircuito, el
automático **salta y corta la corriente**: no porque el problema se arregle solo, sino para que no
se queme la instalación mientras alguien lo arregla.

Un circuit breaker de software hace lo mismo con las llamadas. Tres estados:

| estado | qué hace |
|---|---|
| **CLOSED** | todo normal, las llamadas pasan. Va contando fallos |
| **OPEN** | demasiados fallos: **corta**. Las llamadas fallan al instante, sin tocar la red |
| **HALF_OPEN** | pasado un rato, deja pasar unas pocas de prueba. Si van bien, cierra; si no, vuelve a abrir |

**Se pega:** en `practica/src/main/java/cl/dgt/resiliencia/services/PagoService.java` — el
circuito y sus umbrales. Los imports **arriba**, el campo y el constructor **dentro de la clase**.

```java
        CircuitBreakerConfig configuracion = CircuitBreakerConfig.custom()
                .slidingWindowType(CircuitBreakerConfig.SlidingWindowType.COUNT_BASED)
                .slidingWindowSize(5)
                .minimumNumberOfCalls(5)
                .failureRateThreshold(50)
                .waitDurationInOpenState(Duration.ofSeconds(10))
                .permittedNumberOfCallsInHalfOpenState(2)
                .build();

        this.circuito = CircuitBreaker.of("tesoreria", configuracion);

        circuito.getEventPublisher().onStateTransition(e ->
                log.info(">>> CIRCUITO {} -> {}",
                        e.getStateTransition().getFromState(), e.getStateTransition().getToState()));
```

y la llamada queda envuelta por los dos, con el reintento por fuera:

```java
        Supplier<Map<String, Object>> protegida =
                Retry.decorateSupplier(reintento,
                        CircuitBreaker.decorateSupplier(circuito, () -> cliente.consultarPago(id)));
```

Y una línea en la configuración del reintento que importa más de lo que parece:

```java
                .ignoreExceptions(CallNotPermittedException.class)
```

> Sin ella, cuando el circuito está abierto el reintento **reintenta el rechazo**: espera 400 ms
> para volver a recibir un «no» instantáneo. Con ella, un circuito abierto responde en
> milésimas — que es todo el sentido de abrirlo.

**Los valores no son los de por defecto, y hay que decirlo:** de fábrica, el circuito pide **100
llamadas** antes de opinar. En una sesión de tres horas no abriría nunca. Aquí se le pide una
ventana de 5 llamadas y un 50 % de fallos.

**Se corre:** Tesorería caída, y se pide varias veces mirando el contador.

```bash
curl -X POST http://localhost:8097/simulador/caida
for i in 1 2 3; do curl -s -o /dev/null -w "%{time_total}s " http://localhost:8097/pagos/77;
                   curl -s http://localhost:8097/pagos/estado-circuito; echo; done
```

**En consola:**

```
>>> CIRCUITO CLOSED -> OPEN

petición 1: 0.215079s  circuito=OPEN   httpReales=1
petición 2: 0.002241s  circuito=OPEN   httpReales=1
petición 3: 0.002180s  circuito=OPEN   httpReales=1
```

**Aquí se para y se señala el contador con el dedo.** `httpReales` es cuántas veces se salió de
verdad a la red:

> **No se mueve.** Se hicieron tres peticiones y sólo una tocó Tesorería. Las otras dos se
> resolvieron aquí dentro, en dos milésimas — **cien veces más rápido** — y sin poner un gramo más
> de peso sobre un servicio que está intentando levantarse.

Ese es el doble regalo del circuito: protege a quien llama **y** a quien es llamado.

### Y ahora la vuelta

**Se corre:** se repara Tesorería y se espera.

```bash
curl -X POST http://localhost:8097/simulador/sana
# el circuito sigue OPEN: todavía no lo sabe
sleep 11
curl http://localhost:8097/pagos/77
curl http://localhost:8097/pagos/77
```

**En consola:**

```
>>> CIRCUITO OPEN -> HALF_OPEN
>>> CIRCUITO HALF_OPEN -> CLOSED
```

**Lo que hay que notar:** nadie avisó al circuito de que Tesorería volvió. Pasados los 10 segundos
dejó pasar dos llamadas de prueba, salieron bien, y cerró solo. **Se recupera sin intervención
humana**, que es justo lo que se quiere de un sistema a las tres de la mañana.

(Detalle fino, por si alguien mira el estado justo después del `sleep`: sigue diciendo `OPEN`
hasta que llega la siguiente llamada. La transición a `HALF_OPEN` ocurre al intentar, no por reloj.)

---

## Paso 5 · Qué se responde cuando no hay respuesta

**Se explica:** queda la última pieza, y **no es técnica**. El circuito ya protege el sistema, pero
el usuario sigue recibiendo un error. La pregunta del paso es de negocio:

> Tesorería no contesta. ¿Qué le decimos al contribuyente?

Y tiene tres respuestas posibles, según el caso:

1. **Un error honesto** — cuando sin ese dato no se puede hacer nada.
2. **Un dato de reserva** — el último valor conocido, el de un caché, un valor por defecto.
3. **Una respuesta parcial** — todo lo que sí se sabe, diciendo claramente qué falta.

Aquí se elige la tercera, porque el trámite **no depende** de la confirmación del pago para
seguir su curso.

**Se pega:** en `practica/src/main/java/cl/dgt/resiliencia/services/PagoService.java`,
**reemplazando el método entero**: aquí es donde se decide qué responder cuando no hay respuesta.

```java
    public Map<String, Object> consultar(String id) {
        Supplier<Map<String, Object>> protegida =
                Retry.decorateSupplier(reintento,
                        CircuitBreaker.decorateSupplier(circuito, () -> cliente.consultarPago(id)));
        try {
            return protegida.get();
        } catch (Exception e) {
            log.warn("Tesorería no respondió ({}). Se degrada.", e.getClass().getSimpleName());
            return Map.of("estado", "DESCONOCIDO", "id", id,
                    "aviso", "Tesorería no responde; el trámite sigue su curso");
        }
    }
```

**Se corre:** con Tesorería caída.

**En consola:**

```json
{"estado":"DESCONOCIDO","id":"77","aviso":"Tesorería no responde; el trámite sigue su curso"}
```

Y en el log del servidor:

```
WARN  Tesorería no respondió (CallNotPermittedException). Se degrada.
```

**Lo que hay que notar, y es el cierre del laboratorio:**

- El usuario recibe un **200**, no un 500. La aplicación **funciona** con Tesorería caída.
- Y no miente: `DESCONOCIDO` no es `PAGADO`. Dice exactamente lo que sabe y lo que no.
- El problema **sí** queda registrado, en el log, donde lo verá quien tenga que arreglarlo.

> Degradar no es esconder el fallo. Es **decidir de antemano** qué parte del servicio puede seguir
> funcionando sin la pieza que se cayó — y no dejar que esa decisión la tome un stacktrace.

---

## Al terminar

`practica/` da los mismos números que `solucion/`:

| situación | espera | llamadas HTTP |
|---|---|---|
| sana | 0,15 s | 1 |
| lenta, 3 intentos | 6,44 s | 3 |
| caída, primera | 0,22 s | 1 |
| caída, circuito abierto | **0,002 s** | **0** |

Lo que hay que poder decir con las propias palabras:

> Un cliente HTTP sin timeout espera para siempre, y eso no es un fallo del otro: es mi
> configuración. Un timeout convierte una espera infinita en un error rápido. Un reintento sirve
> para fallos pasajeros y empeora las caídas de verdad. Y un circuito abierto es la forma de
> dejar de golpear a alguien que ya está en el suelo.

### Lo que siembra este lab

Hoy pasó algo que conviene mirar de frente antes de irse.

El circuito abrió, la aplicación se degradó, el usuario recibió una respuesta rara pero razonable,
y **todo funcionó**. Nadie se enteró de nada. En producción, esa misma tarde, alguien pregunta:

- ¿Cuántas veces se degradó el servicio hoy?
- ¿Cuánto tiempo estuvo el circuito abierto?
- ¿Cuántos contribuyentes recibieron `DESCONOCIDO` en vez de su estado de pago?

Y la respuesta honesta es que no se sabe. Salió por la consola, y la consola de producción es un
río que nadie mira. Lo mismo que quedó pendiente del Lab 09 —los 401 y 403 que nadie registró—
vuelve hoy multiplicado: **el sistema aguanta, y sigue siendo mudo.**

> **La pregunta que queda abierta** — un sistema resiliente que no cuenta lo que le pasa esconde
> sus propios problemas mejor que uno frágil. ¿Cómo se hace para que la aplicación **diga** lo
> que está viviendo?

Esa es la materia del laboratorio de observabilidad: métricas, health checks y un log que se
pueda seguir. Con lo de hoy, además, ya hay algo concreto que medir.
