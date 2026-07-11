# Guía 02 · El timeout y la mala noticia (TODO_1 y TODO_2)

## Acto 2 — el parche que no resuelve

La tentación: agrandar el pool. Con más hilos/conexiones, aguantas más pagos colgados. Y
funciona… hasta el pago N+1. Solo compraste tiempo pagando memoria; el rehén sigue siendo
rehén. Posponer no es resolver.

## TODO_1 — El timeout dirigido

El candado va en el cliente de TESO, corto y por separado:

```java
SimpleClientHttpRequestFactory fabrica = new SimpleClientHttpRequestFactory();
fabrica.setConnectTimeout(Duration.ofMillis(500));   // abrir conexión es instantáneo si TESO está sano
fabrica.setReadTimeout(Duration.ofMillis(800));      // TESO confirma en decenas de ms; 800 es de sobra
RestClient restClient = RestClient.builder().baseUrl(baseUrl).requestFactory(fabrica).build();
```

Cortos **a propósito**: mejor un "no pude" en un segundo que un "espera…" de treinta. Y
**dirigidos**: el timeout es de TESO, no global — cada dependencia con su presupuesto.

## TODO_2 — La mala noticia elegante

Cuando TESO no responde, el adaptador traduce el fallo de transporte a una excepción de
DOMINIO, y la web la convierte en un **503** honesto:

```java
// TesoreriaAdapter
catch (RestClientException e) {
    throw new TesoreriaNoDisponibleException("Tesorería no confirmó a tiempo...", e);
}
```

```java
// ManejadorDeErrores
@ExceptionHandler(TesoreriaNoDisponibleException.class)
public ProblemDetail tesoreriaNoDisponible(...) {
    ProblemDetail p = ProblemDetail.forStatusAndDetail(HttpStatus.SERVICE_UNAVAILABLE, "...reintenta...");
    p.setProperty("reintentarEnSegundos", 5);
    return p;
}
```

Sin stacktrace, con sugerencia de reintento. Y el trámite queda ÍNTEGRO: como el timeout
corta ANTES de tocarlo, no avanza de estado. El test `E1` mide que falla rápido Y la API
sigue viva; `E2` mide el 503 y que el trámite no cambió.

> **La pregunta de criterio:** ¿por qué un 503 rápido es mejor servicio que intentarlo 30 s?
> Porque a los 30 s el usuario ya se fue y el hilo ya secuestró a otros. La respuesta es la
> misma —no se pudo—; darla rápido ES el servicio.

Sigue con [`03-el-cliente-declarativo-y-cors.md`](03-el-cliente-declarativo-y-cors.md).
