# Guía 02 · El hilo y el JSON (TODO_1 y TODO_2)

## TODO_1 — El hilo de Ariadna (MDC)

Un filtro que pone un `traceId` en el MDC al entrar cada petición, y lo limpia al salir:

```java
@Component @Order(1)
public class FiltroDeTraza extends OncePerRequestFilter {
    protected void doFilterInternal(req, res, chain) {
        String traceId = respetarOGenerar(req.getHeader("X-Trace-Id"));
        MDC.put("traceId", traceId);
        res.setHeader("X-Trace-Id", traceId);   // devolvelo, para que quien reporte sepa qué buscar
        try { chain.doFilter(req, res); }
        finally { MDC.remove("traceId"); }       // el hilo vuelve al pool LIMPIO
    }
}
```

El `finally` no es opcional: el hilo se reutiliza para otra petición, y un `traceId` olvidado
contaminaría la siguiente. Si el cliente trae un `X-Trace-Id`, se respeta (la traza cruza
servicios); si no, se genera.

## TODO_2 — Log estructurado en JSON

Una línea en `application.yml`:

```yaml
logging:
  structured:
    format:
      console: ecs
```

Y cada línea de log sale como un objeto JSON con `timestamp`, `level`, `logger`, `message`, y el
`traceId` del MDC. Un agregador (ELK, Loki) lo indexa; un humano lo filtra por `traceId`. El
test `E1` verifica que dos peticiones tienen `traceId` distintos y que el MDC los lleva; `E2`
verifica que la línea parsea como JSON y contiene el `traceId`.

> **La pregunta de criterio:** antes tenías 200 líneas y ninguna respuesta; ¿qué cambió, además
> del formato? El `traceId`. El JSON lo hace consultable; el `traceId`, correlacionable.

Sigue con [`03-el-auditor-y-los-archivos.md`](03-el-auditor-y-los-archivos.md).
