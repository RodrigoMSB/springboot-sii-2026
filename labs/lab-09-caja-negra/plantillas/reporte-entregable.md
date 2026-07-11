# Reporte entregable · Lab 09

**Nombre:** ______________________  **Fecha:** ____________

Vale el 20 %. Se lee entero.

## 1 · El crimen (transcripción literal)

Pega dos líneas de log JSON de la MISMA petición (mismo traceId), de la solución:

```
{ ... "traceId":"____________" ... }
{ ... "traceId":"____________" ... }
```

| Pregunta | Tu respuesta |
|---|---|
| Antes tenías 200 líneas y ninguna respuesta. **¿Qué cambió, además del formato?** | |

## 2 · El hilo y el JSON

| Pregunta | Tu respuesta |
|---|---|
| ¿Por qué el `MDC.remove` en el `finally` es obligatorio? | |
| ¿Por qué un agregador prefiere JSON a texto plano? | |

## 3 · El auditor

| Pregunta | Tu respuesta |
|---|---|
| ¿Por qué un aspecto y no un `log.info` en cada método? | |
| El aspecto NO auditó la autoinvocación. **¿Por qué?** | |
| ¿Cómo evitaste que la clave llegara al log? | |

## 4 · Los archivos

| Pregunta | Tu respuesta |
|---|---|
| ¿Por qué juzgar por los magic bytes y no por la extensión? | |
| ¿Cómo neutralizaste el `../../etc/passwd`? | |

## 5 · Honestidad

| Pregunta | Tu respuesta |
|---|---|
| ¿Usaste `95-recuperar.sh --todo`? | |
| ¿Qué parte te costó más? | |
