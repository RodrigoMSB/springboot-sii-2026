# Reporte entregable · Lab 10

**Nombre:** ______________________  **Fecha:** ____________

Vale el 20 %. Se lee entero.

## 1 · El crimen (transcripción literal)

Con la base caída en el **starter**, pega las dos respuestas tal cual, sin retocarlas:

```
$ curl -s localhost:8099/api/v1/tramites
HTTP ______   __________________________________________

$ curl -s localhost:8099/actuator/health
__________________________________________________________
```

Y ahora lo mismo en la **solución**:

```
$ curl -s localhost:8099/actuator/health/readiness
__________________________________________________________

$ curl -s localhost:8099/actuator/health/liveness
__________________________________________________________
```

| Pregunta | Tu respuesta |
|---|---|
| El proceso estaba vivo y respondía `UP`. **¿Por qué eso era una mentira?** | |

## 2 · Liveness y readiness

| Pregunta | Tu respuesta |
|---|---|
| Si `liveness` dice DOWN, ¿qué hay que hacer? ¿Y si lo dice `readiness`? | |
| ¿Qué pasaría si metieras la base en `liveness`? Piénsalo hasta el final. | |
| TESO **no** entra en readiness. ¿Por qué no? (pista: Lab 08) | |
| Tu readiness nombra el componente caído. ¿Por qué no basta con decir `DOWN`? | |

## 3 · Exposición con criterio

| Pregunta | Tu respuesta |
|---|---|
| `/actuator/env` responde **404** con un token válido de funcionario. ¿Por qué 404 y no 401? | |
| ¿Qué se llevaría exactamente alguien que consiguiera `/actuator/env`? ¿Y `/heapdump`? | |
| `/actuator/prometheus` sí existe, pero pide token. ¿Por qué esa asimetría? | |

## 4 · El caché

| Pregunta | Tu respuesta |
|---|---|
| Tu hit-rate medido tras dos llamadas: aciertos ____ / fallos ____ | |
| Las tres condiciones para cachear algo. ¿Las cumple este reporte? | |
| Si olvidaras el `@CacheEvict`, **¿qué se rompe?** Cuidado con la respuesta fácil. | |
| ¿Por qué el `@CacheEvict` está en otro bean y no junto al `@Cacheable`? | |
| El TTL es de 5 minutos. ¿Para qué sirve si ya invalidas explícitamente? | |

## 5 · Honestidad

| Pregunta | Tu respuesta |
|---|---|
| ¿Consultaste `solucion/`? ¿En qué actividad y por qué? | |
| ¿Usaste `95-recuperar.sh --todo`? | |
| ¿Qué parte te costó más? | |
