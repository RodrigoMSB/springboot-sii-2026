# Reporte entregable · Lab 11

**Nombre:** ______________________  **Fecha:** ____________

Vale el 20 %. Se lee entero.

## 1 · El crimen (transcripción literal)

Con `--instancias 2` en el **starter**, pega las filas tal cual salieron:

```
Cierres registrados HOY en la base:
     id=___  instancia=________  tramites=____  total=__________
     id=___  instancia=________  tramites=____  total=__________
```

Y dos líneas del log, una de cada instancia, con su `traceId`:

```
____________________________________________________________________
____________________________________________________________________
```

| Pregunta | Tu respuesta |
|---|---|
| **¿Cómo supiste que eran dos ejecuciones y no una que se registró dos veces?** | |
| Ninguna instancia dio error. ¿Por qué eso empeora el problema en vez de mejorarlo? | |

## 2 · El reloj

| Pregunta | Tu respuesta |
|---|---|
| `fixedRate` vs `fixedDelay`: ¿cuál elegiste y por qué **este** trabajo lo pide? | |
| El pool del planificador es de 4 hilos. ¿Qué tiene que ver con la pregunta anterior? | |
| Le pusiste `zone` al cron. ¿Qué falla exactamente sin ella, y **cuándo** se nota? | |

## 3 · El candado

| Pregunta | Tu respuesta |
|---|---|
| ¿Por qué mirar y tomar tienen que ser **una sola** sentencia? | |
| ¿Qué pasa si el que tomó el candado muere a mitad del cierre? ¿Y sin expiración? | |
| El `now()` de tu SQL: **¿el reloj de quién es?** ¿Por qué importa? | |
| Tu TTL es de ______. ¿Qué pasa si el cierre llegara a tardar más que eso? | |
| El parche de la bandera en `application.yml` **funciona**. Nombra sus **dos** fallos. | |

## 4 · Asincronía y eventos

| Pregunta | Tu respuesta |
|---|---|
| ¿Qué gana el contribuyente con que la notificación sea asíncrona? | |
| Tu `@Async` devuelve `void`. Si lanza una excepción, **¿quién se entera?** | |
| ¿Por qué hilos virtuales para esto y no para un cálculo pesado? | |
| La autoinvocación no fue asíncrona. **¿Por qué?** (van tres veces en el curso) | |
| Con `@EventListener` normal, el rollback igual mandaba el aviso. ¿Por qué es peor que no avisar? | |
| Al cruzar a `AFTER_COMMIT`, **¿qué pierdes?** | |

## 5 · Honestidad

| Pregunta | Tu respuesta |
|---|---|
| ¿Consultaste `solucion/`? ¿En qué actividad y por qué? | |
| ¿Usaste `95-recuperar.sh --todo`? | |
| ¿Qué parte te costó más? | |
