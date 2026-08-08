# 99 · Desafío (opcional) — el cierre que no se repite

> Lo opcional **nunca** baja el veredicto. Si no lo haces, el `90` no se entera. Si lo haces y
> queda a medias, tampoco: se cuenta aparte.

El candado garantiza que **dos instancias no ejecuten a la vez**. No garantiza que el cierre de un
día se haga **una sola vez en la historia**.

Piensa en esta secuencia, toda legítima:

1. 03:00 — la instancia 1 toma el candado y hace el cierre del día 6.
2. 03:00:04 — termina y libera.
3. 03:10 — alguien reinicia la flota (un despliegue). Las instancias arrancan.
4. 03:10:08 — el `initialDelay` dispara el latido. El candado está libre. Se hace **otro** cierre
   del día 6.

Dos filas, un solo día, y el candado funcionó perfectamente en todo momento.

## El criterio de aceptación

Haz que el cierre de una fecha dada ocurra **exactamente una vez**, aunque el latido se dispare
muchas veces y aunque la flota se reinicie entre medias.

Se aprueba si:

1. Llamar a `latido()` diez veces seguidas, con reinicios entre medias, deja **una** fila en
   `cierre_diario` para esa fecha.
2. El día siguiente sí produce su propia fila (no rompiste el caso normal).
3. Dos instancias compitiendo siguen produciendo una sola.

Sin pistas de implementación: esa es la gracia del `99`.

## Tres trampas, que no son pistas

- **Un `UNIQUE (fecha)` no es la respuesta**, o no toda. Piensa en qué momento explota y qué ya
  ocurrió para entonces. (Vuelve al comentario de la migración `V4`: está escrito ahí.)
- **Consultar «¿ya hay cierre de hoy?» antes de trabajar** te devuelve a la ventana de carrera del
  TODO_2, con más pasos.
- **Cuidado con la fecha.** `LocalDate.now()` usa la zona del servidor. Ya sabes cómo termina eso.

## La palabra que estás buscando

Se llama **idempotencia**: una operación que se puede ejecutar muchas veces con el mismo efecto que
ejecutarla una. Ya la implementaste sin nombrarla así — RN-05, en el Lab 06: reintentar la emisión
de un folio devuelve el mismo folio en vez de crear otro.

Guárdala. La semana que viene, cuando un mensaje se pueda entregar dos veces porque la red no da
garantías, va a ser la palabra central de la sesión.

## Para pensar (va en el reporte, aunque no lo implementes)

Si el cierre hiciera un **cargo** en vez de un resumen, ¿te bastaría con el candado del TODO_2?
Responde en términos de qué pasa entre el paso 2 y el 3 de la secuencia de arriba.
