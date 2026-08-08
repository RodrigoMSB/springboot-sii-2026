# Troubleshooting · Lab 12

Filas citables: si algo te pasa, busca su número y menciónalo en el reporte o al instructor.

| # | Síntoma | Qué pasa | Qué hacer |
|---|---|---|---|
| **T12-01** | Con `--avisos-caidos`, cero avisos y cero en la cola | **No es un bug: es el crimen.** El aviso se manda al aire, así que no queda en ninguna parte. | TODO_1. Empieza por `guia/02-el-aviso-que-se-evaporo.md`. |
| **T12-02** | La app tarda mucho más que en labs anteriores en arrancar | Ahora son **dos** contenedores: PostgreSQL y RabbitMQ. El broker tarda unos segundos más. | Paciencia en la primera pasada. `./bin/start-lab.sh` espera hasta 240 s. |
| **T12-03** | `Connection refused` contra el puerto 5672 | La app arrancó antes que RabbitMQ, o el compose no lo levantó. | `docker compose ps` en `starter/`. El compose tiene healthcheck; si el contenedor no está *healthy*, mira sus logs. |
| **T12-04** | No entro a <http://localhost:15672> | La consola solo existe en la imagen `-management`, y su puerto es fijo. | Comprueba el tag en `compose.yaml`. Usuario `dgt`, clave `dgt-dev`. |
| **T12-05** | `E1` falla: la cola sigue en 0 tras emitir un folio | Nadie publicó: el aviso se fue por la llamada directa. | TODO_1. |
| **T12-06** | `E2` falla: llegan dos avisos del mismo mensaje | Falta la deduplicación, o la **clave está mal elegida**. | TODO_2. Si usaste un id aleatorio por mensaje, cada reintento es «nuevo»: la clave debe identificar el **hecho**. |
| **T12-07** | `E3` falla: la DLQ está vacía | La cola no declara `x-dead-letter-exchange`: el mensaje rechazado se **descarta en silencio**. | TODO_3. |
| **T12-08** | La DLQ sigue vacía **y** la cola principal crece sin parar | `default-requeue-rejected` está en `true` (el default): el mensaje vuelve al principio para siempre y atasca la cola. El `x-dead-letter-exchange` no llega a dispararse nunca. | Ponlo en `false`. Es la línea que hace funcionar la DLQ. |
| **T12-09** | Un mensaje malo dejó sin procesar a todos los de detrás | Lo mismo que T12-08, visto desde el negocio. | Es el punto del TODO_3: un mensaje malo no puede bloquear a los buenos. |
| **T12-10** | `E4` falla: el circuito nunca llega a `OPEN` | La llamada no pasa por el circuito: está inyectado y sin usar. | TODO_4. `circuito.executeCallable(() -> ...)`. |
| **T12-11** | `E4` falla: abierto tarda casi lo mismo que cerrado | Estás midiendo con TESO respondiendo (WireMock arriba), así que no hay fallos que contar. | El test del enunciado corre **sin** TESO a propósito: la ausencia ES el escenario. |
| **T12-12** | El circuito se queda abierto para siempre | Falta la transición a `HALF_OPEN`, o la ventana es enorme. | Sin `HALF_OPEN` cambiaste una caída por otra: nadie vuelve a probar. |
| **T12-13** | `/actuator/health` responde **503** y todo lo demás funciona | Actuator registra solo un indicador `rabbit` y el broker no está. | En este lab ese indicador va **apagado a propósito**, y el porqué es contenido: `TEORIA.md §8`. Si lo encendiste, léelo antes de decidir. |
| **T12-14** | Los avisos llegan **duplicados** en producción y en los tests no | Con un solo consumidor el duplicado casi no aparece; con varios, sí. La idempotencia deja de ser precaución y pasa a ser requisito. | Revisa que mirar y marcar sean **una sola** sentencia atómica. |
| **T12-15** | Tras el reinicio, la cola aparece vacía | ¿La declaraste `durable`? Una cola no durable muere con el broker. | `QueueBuilder.durable(...)`. |
| **T12-16** | El mensaje sale por la cola aunque la transacción reviertió | Estás publicando dentro de la transacción, no en `AFTER_COMMIT`. Ahora es peor que en el Lab 11: el mensaje es durable y queda escrito en disco. | Lab 11, TODO_4. La regla se refuerza: lo que sale del sistema solo sale cuando el dato ya es cierto. |
| **T12-17** | Al compilar sale un `WARNING` de `sun.misc.Unsafe` / Guice | Ruido del wrapper de Maven sobre JDK 25. | Nada. Ver `T-12` del Lab 00. |
