# Reporte entregable · Lab 08

**Nombre:** ______________________  **Fecha:** ____________

Vale el 20 %. Se lee entero.

## 1 · El crimen (transcripción literal)

Pega la salida de `./bin/start-lab.sh --dir starter --teso-lento 30000`:

```
     GET /tramites -> HTTP ___ en ___ s
```

| Pregunta | Tu respuesta |
|---|---|
| ¿Por qué un endpoint que NO toca pagos (el listado) se cayó por culpa de TESO? | |

## 2 · El timeout

| Pregunta | Tu respuesta |
|---|---|
| ¿Qué números pusiste (connect / read) y por qué esos? | |
| Agrandar el pool "funciona". **¿Por qué no resuelve?** | |

## 3 · La mala noticia

| Pregunta | Tu respuesta |
|---|---|
| ¿Por qué responder 503 rápido es MEJOR servicio que intentarlo 30 s? | |
| ¿Por qué el trámite queda íntegro (no avanza) cuando TESO falla? | |

## 4 · El endurecimiento

| Pregunta | Tu respuesta |
|---|---|
| ¿Por qué CORS `*` es rendirse? | |
| ¿Por qué aquí SÍ es correcto deshabilitar CSRF? | |

## 5 · Honestidad

| Pregunta | Tu respuesta |
|---|---|
| ¿Usaste `95-recuperar.sh --todo`? | |
| ¿Qué parte te costó más? | |
