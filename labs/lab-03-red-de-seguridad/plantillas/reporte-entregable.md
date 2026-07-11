# Reporte entregable · Lab 03

**Nombre:** ______________________  **Fecha:** ____________

Vale el 20 %. Se lee entero.

## 1 · La bandeja en rojo

| Pregunta | Tu respuesta |
|---|---|
| ¿Cuántos tests llegaron en rojo? (transcribe el `Tests run: ...`) | |
| ¿Cuál compromiso te costó más entender leyéndolo? | |

## 2 · La triangulación

| Pregunta | Tu respuesta |
|---|---|
| Cuando hardcodeaste el validador a `return true`, ¿qué test pasó y cuáles fallaron? | |
| ¿Por qué un test suelto es un hilo y una suite es una red? | |

## 3 · Transcripción literal

Corre un test parametrizado que aún falle (por ejemplo con el validador a medias) y
**TRANSCRIBE LITERALMENTE** la línea del reporte de Surefire que muestra el caso exacto que
falló (el RUT concreto):

```
(pega aquí, p. ej.:  rutConDvFalso[2] ... expected: false but was: true)
```

## 4 · Lo que implementaste

| Pregunta | Tu respuesta |
|---|---|
| El algoritmo del módulo 11, en tus palabras | |
| ¿Qué campo nombra tu 400 cuando el tipo está en blanco? | |
| El shape exacto del 409 de transición ilegal | |

## 5 · Tus tests (TODO_4)

| Pregunta | Tu respuesta |
|---|---|
| ¿Qué mockeaste, y qué NO? ¿Por qué el DTO no se mockea? | |
| ¿Qué verificaste con `ArgumentCaptor` que `verify(save(any()))` no vería? | |

## 6 · Evidencia

```
(pega la salida de ./bin/90-validar.sh cuando llegó a 🏆)
```

- [ ] `./bin/90-validar.sh` → **🏆 LAB 03 APROBADO**

## 7 · Honestidad

| Pregunta | Tu respuesta |
|---|---|
| ¿Consultaste `solucion/`? (sí / no) | |
| Si sí: ¿en qué actividad, y por qué? | |

## 8 · La siembra

| Pregunta | Tu respuesta |
|---|---|
| Tus tests vigilan validación y errores. ¿Qué NO cubren todavía? | |
| ¿Qué guardián ya te advirtió sobre EAGER sin que lo notaras? (pista: AU-0_) | |
