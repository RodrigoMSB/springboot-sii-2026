# Reporte entregable · Lab 12

**Nombre:** ______________________  **Fecha:** ____________

Vale el 20 %. Se lee entero.

## 1 · El crimen (transcripción literal)

Fase 1 en el **starter**, con el servicio de avisos caído. Pega lo que salió, sin retocarlo:

```
     POST /api/v1/tramites/__/folio  ->  HTTP ____
     POST /api/v1/tramites/__/folio  ->  HTTP ____
     POST /api/v1/tramites/__/folio  ->  HTTP ____

     avisos entregados             ->  ____
     mensajes esperando en la cola ->  ____
```

| Pregunta | Tu respuesta |
|---|---|
| **¿Qué falló?** Míralo bien antes de responder. | |
| Carolina no pidió solo que no se perdieran. ¿Qué más pidió, y por qué un reintento no se lo da? | |

## 2 · El mensaje muerto (transcripción literal)

Pega el mensaje que quedó en la DLQ, **con su causa**:

```
______________________________________________________________________
______________________________________________________________________
```

| Pregunta | Tu respuesta |
|---|---|
| Falló 3 veces y quedó en la DLQ. **¿Por qué es mejor eso que seguir reintentando para siempre?** (hay dos razones; una es la obvia) | |
| ¿Qué habría pasado con los avisos buenos que iban detrás? | |

## 3 · La idempotencia

| Pregunta | Tu respuesta |
|---|---|
| Tu clave de idempotencia es ______________. ¿Identifica el **hecho** o la **entrega**? | |
| ¿Qué pasaría si usaras un UUID nuevo por mensaje? | |
| «Exactly once» no existe. ¿Por qué llega un duplicado sin que nada haya fallado? | |
| ¿En qué se parece esto a RN-05 del Lab 06? | |

## 4 · El circuito

| Pregunta | Tu respuesta |
|---|---|
| Tiempo de una llamada fallida: **cerrado** ____ ms · **abierto** ____ ms | |
| El timeout del Lab 08 ya existía. ¿Qué añade el circuito que el timeout no daba? | |
| ¿Qué hace `HALF_OPEN`? ¿Qué pasaría **sin** ese estado? | |
| ¿A quién protege un circuito abierto: a ti o al otro? Justifica. | |

## 5 · Criterio: readiness

| Pregunta | Tu respuesta |
|---|---|
| El broker **no** entra en el health de la aplicación. **¿Por qué?** | |
| Si entrara, ¿qué pasaría el día que RabbitMQ se caiga? | |
| ¿Dónde SÍ se debería ver que el broker está caído? | |

## 6 · Honestidad

| Pregunta | Tu respuesta |
|---|---|
| ¿Consultaste `solucion/`? ¿En qué actividad y por qué? | |
| ¿Usaste `95-recuperar.sh --todo`? | |
| ¿Qué parte te costó más? | |
