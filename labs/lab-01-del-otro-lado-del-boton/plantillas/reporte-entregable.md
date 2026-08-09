# Reporte entregable · Lab 01

**Nombre:** ______________________  **Fecha:** ____________

El validador mide el estado de tu código. Este reporte mide tu cabeza. Vale el 20 % del
curso, y se lee entero.

---

## 1 · El crimen

| Pregunta | Tu respuesta |
|---|---|
| Las tres líneas exactas que sacaste de `application.yml` | |
| ¿Está la contraseña en el `application.yml` de hoy? | |
| ¿Sigue estando en el repositorio? Explica en una frase. | |
| ¿Por qué borrarla no arregló nada? | |

## 2 · El parche del Acto 2

| Pregunta | Tu respuesta |
|---|---|
| Cuando moviste la clave a `application-dev.yml`, ¿arrancó la aplicación? | |
| ¿Qué cambió realmente respecto al viernes? | |
| ¿Qué te dijo `T1_SinCredencialesEnElRepoTest`? | |

## 3 · Fallar rápido, y fallar claro

**TRANSCRIBE LITERALMENTE** el mensaje de error al arrancar en `prod` sin las variables de
entorno. No lo resumas, no lo interpretes: cópialo tal cual, con sus corchetes.

```
(pega aquí el mensaje exacto)
```

| Pregunta | Tu respuesta |
|---|---|
| ¿Cuál era el mensaje **antes** de tu `BeanFactoryPostProcessor`? (transcríbelo también) | |
| ¿Por qué un `BeanFactoryPostProcessor` y no un `@Component` normal? | |
| ¿Qué habría pasado con `${DGT_DB_PASSWORD:cambiame}`? | |

## 4 · Rotar

| Pregunta | Tu respuesta |
|---|---|
| Comando exacto con que rotaste la clave | |
| Después de rotar, ¿la credencial vieja abre algo? | |
| ¿Por qué borrarla del archivo no bastaba? | |

## 5 · Tipos y contrato

| Pregunta | Tu respuesta |
|---|---|
| Los tres agujeros de `@Value`, en tus palabras | |
| ¿Qué pasa al arrancar con `dgt.folio.largo: 3`? Transcribe el error. | |
| ¿Qué campos viajan en `GET /api/tramites/1`? | |
| `Content-Type` del 404 | |
| ¿Intentaste devolver la entidad? ¿Qué te dijo AU-02? | |

## 6 · Evidencia

```
(pega aquí la salida completa de ./bin/90-validar.sh)
```

- [ ] `./bin/90-validar.sh` → **🏆 LAB 01 APROBADO**

---

## 7 · Honestidad

Mirar la solución **no está prohibido**. Está registrado. Se evalúa tu honestidad, no tu
pureza: un alumno que dice "me atasqué en el TODO_2 y comparé" aprende más que uno que se
queda tres horas mirando la pared.

| Pregunta | Tu respuesta |
|---|---|
| ¿Consultaste `solucion/`? (sí / no) | |
| Si sí: ¿en qué actividad, y por qué? | |
| ¿Qué entendiste después de mirarla que no entendías antes? | |

## 8 · La siembra

| Pregunta | Tu respuesta |
|---|---|
| Tu endpoint devuelve un DTO. ¿Qué pasaría si devolviera la entidad? | |
| ¿Qué campo de `Contribuyente` jamás debe salir por la API? | |
