# Reporte entregable · Lab 06

**Nombre:** ______________________  **Fecha:** ____________

Vale el 20 %. Se lee entero.

## 1 · El crimen (transcripción literal)

Pega **tal cual** la salida del sabotaje sobre el starter (`--concurrencia`):

```
     trámite __  -> HTTP ___  folio ___
     trámite __  -> HTTP ___  ______________
```

| Pregunta | Tu respuesta |
|---|---|
| ¿Qué pasó: un HTTP 500, dos folios con el mismo número, o ambos? | |
| ¿Por qué la PK del folio te "salvó" y aun así hubo un problema? | |

## 2 · Los dos parches del acto 2

| Pregunta | Tu respuesta |
|---|---|
| `synchronized` pasó el test. **¿Por qué igual está mal?** (piensa en el Lab 10) | |
| `REQUIRES_NEW` puso `E4` en rojo. ¿Qué número quedó "gastado" y por qué? | |

## 3 · La forma correcta y el contrato

| Pregunta | Tu respuesta |
|---|---|
| ¿Por qué el bloqueo pesimista debe ir en la MISMA transacción que persiste el folio? | |
| ¿Por qué la idempotencia (RN-05) necesita además el `UNIQUE (tramite_id)`? | |
| El `CHECK` de la V3 es `monto <> 0`, no `monto >= 0`. **¿Por qué?** | |
| Si mañana validas el monto en Java, ¿el `CHECK` sobra? **¿Por qué no?** | |

## 4 · La siembra

| Pregunta | Tu respuesta |
|---|---|
| ¿Cuál es el "único problema grande" que queda con los folios? (pista: la puerta) | |

## 5 · Honestidad

| Pregunta | Tu respuesta |
|---|---|
| ¿Usaste `95-recuperar.sh --todo` en algún momento? (un "sí" honesto vale) | |
| ¿Qué parte te costó más entender? | |
