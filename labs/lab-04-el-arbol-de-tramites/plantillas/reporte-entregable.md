# Reporte entregable · Lab 04

**Nombre:** ______________________  **Fecha:** ____________

Vale el 20 %. Se lee entero.

## 1 · El muro de JOINs (transcripción literal)

Pega **tal cual** el primer `select` que Hibernate disparó al pedir un trámite en el starter
(el del muro):

```
(pega aquí el SELECT con sus JOINs)
```

| Pregunta | Tu respuesta |
|---|---|
| ¿Cuántas tablas viajaron para responder una ficha? Cuéntalas. | |
| ¿Cuántos `select` en total se dispararon? | |

## 2 · El parche del default

| Pregunta | Tu respuesta |
|---|---|
| Sin declarar `fetch`, ¿qué default aplicó tu `@ManyToOne`? ¿Y un `@OneToMany`? | |
| ¿Por qué "no declarar" no es neutral? | |

## 3 · La corrección

| Pregunta | Tu respuesta |
|---|---|
| Después de LAZY, ¿cuántos JOINs tiene el primer `select`? (transcribe) | |
| ¿Qué te dijo AU-04 si dejaste una relación en EAGER? | |

## 4 · Las consultas

| Pregunta | Tu respuesta |
|---|---|
| El nombre de un método derivado que escribiste, y qué WHERE genera | |
| Tu JPQL de `presentadosDelPeriodo`, ¿por qué NO usa `JOIN FETCH`? | |
| ¿Por qué el reporte usa `JdbcClient` y no el repositorio JPA? | |

## 5 · Evidencia

```
(pega la salida de ./bin/90-validar.sh cuando llegó a 🏆)
```

- [ ] `./bin/90-validar.sh` → **🏆 LAB 04 APROBADO**

## 6 · Honestidad

| Pregunta | Tu respuesta |
|---|---|
| ¿Consultaste `solucion/`? (sí / no) | |
| Si sí: ¿en qué actividad, y por qué? | |

## 7 · La siembra

| Pregunta | Tu respuesta |
|---|---|
| Hiciste lo correcto (LAZY). ¿Qué te dijo Carolina que ese LAZY costará la próxima semana? | |
| ¿Qué crees que va a pasar cuando un listado recorra 50.000 trámites tocando cada contribuyente? | |
