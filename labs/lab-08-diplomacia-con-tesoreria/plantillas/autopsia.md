# Autopsia · Lab 08

Para pensar mientras depuras. No se entrega.

## El timeout
- ¿`E1` rojo (el pago tarda demasiado)? ¿Le puse `requestFactory` con timeouts al RestClient?
- ¿El read timeout es más corto que el retraso de TESO en el test (3 s)?

## La mala noticia
- ¿`E2` rojo (no es 503)? ¿El adaptador traduce `RestClientException` a `TesoreriaNoDisponibleException`?
- ¿El `ManejadorDeErrores` tiene el `@ExceptionHandler` del 503? ¿El trámite quedó en PRESENTADO?

## El refactor
- Migré a `@HttpExchange` y algo se rompió: ¿el `RestClient` de respaldo tiene los MISMOS timeouts?
- ¿El adaptador sigue traduciendo la excepción?

## El endurecimiento
- ¿`E4` rojo? ¿`.cors(...)` + el bean `CorsConfigurationSource` con el origen nominal?
- ¿La cabecera CSP? ¿`.headers(h -> h.contentSecurityPolicy(...))`?
