# Autopsia · Lab 09

Para pensar mientras depuras. No se entrega.

## El traceId
- ¿`E1` rojo? ¿El filtro pone el traceId en el MDC? ¿Devuelve el header `X-Trace-Id`?
- ¿Dos peticiones dan el mismo traceId? Olvidaste generar uno nuevo, o no limpiaste el MDC.

## El JSON
- ¿`E2` rojo (no parsea)? ¿Está `logging.structured.format.console: ecs` en el yml?

## El aspecto
- ¿`E3` rojo (no audita)? ¿El pointcut matchea (`..application..*Service`)? ¿Está aspectjweaver en el pom?
- ¿Auditó la autoinvocación? Entonces algo pasó por el proxy que no debía — revisá el ensayo.
- ¿El RUT completo en el log? Falta enmascarar.

## Los archivos
- ¿`E4` rojo (acepta el exe)? ¿Leés los magic bytes o confiás en el Content-Type?
- ¿El nombre con `../`? Falta sanear al último segmento.
