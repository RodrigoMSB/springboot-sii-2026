# Troubleshooting · Lab 09

## L9-01 · `E1` rojo: no hay traceId / dos peticiones dan el mismo
Falta el `FiltroDeTraza`, o no genera un traceId nuevo por petición, o no limpia el MDC en el `finally`
(el hilo reutiliza el traceId anterior).

## L9-02 · `E2` rojo: la línea no parsea como JSON
Falta `logging.structured.format.console: ecs` en `application.yml`. En texto plano, no es JSON.

## L9-03 · `E3` rojo: no hay auditoría
Falta el `@Aspect`, o `aspectjweaver` no está en el pom (Boot 4 no trae `spring-boot-starter-aop`; usa
`org.aspectj:aspectjweaver`), o el pointcut no matchea el paquete de servicios.

## L9-04 · `E3` rojo: auditó la autoinvocación
El aspecto vio un `this.otro()` que no debía. Ese es el LÍMITE del proxy: una autoinvocación no pasa por
él. Si tu test falla al revés (esperabas que NO auditara y auditó), revisá que la llamada sea de verdad
`this.metodo()` y no a través del bean.

## L9-05 · `E4` rojo: el .exe disfrazado se acepta
Estás confiando en `archivo.getContentType()` o la extensión. Leé los magic bytes del contenido.

## L9-06 · `E4` rojo: el path traversal no se neutraliza
El nombre se guarda crudo. Quédate con el último segmento (tras la última `/` o `\`).

## L9-07 · Docker no responde
Este lab necesita Docker: PostgreSQL con Testcontainers. Ver T-03 del Lab 00.
