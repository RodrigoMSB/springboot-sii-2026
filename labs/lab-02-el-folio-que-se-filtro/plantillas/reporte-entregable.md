# Reporte entregable · Lab 02

**Nombre:** ______________________  **Fecha:** ____________

El validador mide tu código. Este reporte mide tu cabeza. Vale el 20 %.

## 1 · El crimen

| Pregunta | Tu respuesta |
|---|---|
| Transcribe el JSON que devolvía la ficha del starter (curl completo) | |
| ¿Qué campo jamás debió salir, y por qué es peligroso? | |
| ¿Qué clase se estaba serializando? | |

## 2 · El parche del Acto 2

| Pregunta | Tu respuesta |
|---|---|
| Con `@JsonIgnore` sobre el puntaje, ¿qué devolvía el curl? | |
| ¿Qué otro campo se te escapó igual? | |
| **¿Por qué `@JsonIgnore` era insuficiente si el JSON salía limpio?** | |

## 3 · La lista blanca y las capas

| Pregunta | Tu respuesta |
|---|---|
| ¿Qué campos expone tu `FichaContribuyenteDTO`? | |
| Si mañana agregan `saldoDeuda` a la entidad, ¿sale por tu ficha? ¿Por qué? | |
| ¿Por qué `FichaService` es una interfaz y no una clase suelta? | |

## 4 · Los guardianes (transcripción literal)

Instalaste AU-02. Para probar que muerde, tu `T3` la enfrenta a un fixture.
**TRANSCRIBE LITERALMENTE** el mensaje de ArchUnit cuando AU-02 caza al violador (lo ves si
corres el fixture, o si dejas el crimen sin arreglar). Cópialo entero, no lo resumas:

```
(pega aquí el "Architecture Violation ... with type argument depending on ...")
```

| Pregunta | Tu respuesta |
|---|---|
| ¿Por qué NO se escribe AU-02 con `haveRawReturnType`? | |
| ¿Qué parte del mensaje demuestra que cazó el genérico, no el tipo crudo? | |
| Un guardián sin fixture, ¿qué prueba? | |

## 5 · El contrato

| Pregunta | Tu respuesta |
|---|---|
| ¿Qué muestra Swagger UI del esquema de la ficha? | |
| ¿Por qué `/api/v1/`? | |

## 6 · Evidencia

```
(pega la salida de ./bin/90-validar.sh)
```

- [ ] `./bin/90-validar.sh` → **🏆 LAB 02 APROBADO**

## 7 · Honestidad

Mirar la solución no está prohibido: está registrado. Se evalúa la honestidad, no la pureza.

| Pregunta | Tu respuesta |
|---|---|
| ¿Consultaste `solucion/`? (sí / no) | |
| Si sí: ¿en qué actividad, y por qué? | |

## 8 · La siembra

| Pregunta | Tu respuesta |
|---|---|
| Tus guardianes vigilan la **estructura**. ¿Qué NO pueden ver? | |
| Da un ejemplo de código con las capas perfectas que aun así hace algo incorrecto | |
