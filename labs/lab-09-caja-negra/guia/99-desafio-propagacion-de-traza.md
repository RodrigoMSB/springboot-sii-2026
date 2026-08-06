# Desafío 99 · Propagar el traceId a TESO (opcional)

> Opcional (P-15). Si no lo hacés, no perdés nada del lab. Si lo hacés, marcalo con honestidad.

## El reto

El `traceId` muere en tu API. Hacé que CRUCE: cuando el cliente de TESO (Lab 08) llame a
Tesorería, que le mande el `traceId` en una cabecera `X-Trace-Id`. Así una operación se sigue a
través de los dos servicios.

Ideas:
1. Un interceptor del `RestClient` de TESO que lea `MDC.get("traceId")` y lo ponga como header.
2. Verificalo: hacé una operación que llame a TESO, y confirmá que el `traceId` de tu log
   coincide con el que TESO recibió (mirá los logs de WireMock: `docker compose logs teso`).

## Las preguntas

1. ¿Qué pasa con el MDC cuando el trabajo salta a OTRO hilo (un `@Async`, un pool)? ¿El
   `traceId` viaja solo? (pista: no; hay que propagarlo a mano o con un decorador de tareas)
2. Esto es el primer paso de la **traza distribuida** (Lab 14). ¿Qué agrega una herramienta como
   Micrometer Tracing / OpenTelemetry sobre lo que hiciste a mano?

No hay validador para esto. Resumí tu conclusión en el reporte.
