# Tests · Lab 12

Los del **enunciado** (`src/test/java/cl/dgt/tramites/enunciado/`) no se tocan: los protege
`manifiesto-tests.sha256`. `E<n>` corresponde a `TODO_<n>`.

| Test | Qué prueba | Verifica |
|---|---|---|
| `E1_ElAvisoNoSePierdeIT` | Con el consumidor **apagado**, la API responde 201 igual de rápido y el mensaje **espera en la cola**. Al encender el consumidor, los avisos acumulados se procesan solos y la cola queda vacía. | TODO_1 |
| `E2_ConsumidorIdempotenteIT` | El **mismo** mensaje publicado dos veces produce **un** envío — sostenido en el tiempo, no mirado una vez. Y dos mensajes **distintos** sí producen dos: la deduplicación no se pasa de lista. | TODO_2 |
| `E3_LaColaDeLosMuertosIT` | El envenenado agota sus reintentos y cae a la DLQ **con su causa** (cabeceras `x-death`). Y —lo que importa— con el envenenado **primero**, los dos avisos buenos de detrás se entregan igual. | TODO_3 |
| `E4_CircuitBreakerIT` | Arranca `CLOSED`; tras 4 fallos pasa a `OPEN`; abierto, la llamada falla **en menos de 50 ms** sin tocar la red; pasada la ventana llega a `HALF_OPEN` y se recupera solo. | TODO_4 |

## Cómo se mide, y por qué así

**Con un broker de verdad, no con un doble.** Lo que este lab afirma no es «mi código llama a un
método»: es que **RabbitMQ se comporta** de cierta manera — que guarda el mensaje mientras nadie lo
consume, que reintenta un número acotado de veces, que enruta el rechazado a la DLQ. Un mock
demostraría lo que yo *creo* que hace RabbitMQ, que es justamente lo que hay que comprobar.

**El circuit breaker se mide en milisegundos.** La diferencia entre cerrado y abierto no está en el
resultado —las dos veces falla— sino en *cuánto tarda en fallar*. Por eso `E4` cronometra y compara
la **relación**, no un umbral absoluto: rechazar sin salir a la red tiene que ser claramente más
barato que intentarlo.

**Las afirmaciones negativas necesitan una ventana.** «No llega un segundo aviso» no se comprueba en
un instante: si fuera a llegar, llegaría un momento después. `E2` usa `.during(3s)` para sostener la
condición en vez de mirarla una vez.

**Sin dormir, nunca.** AU-05 lo prohíbe en todo el proyecto. Se espera con Awaitility sobre una
condición. Y la recuperación del circuito se espera igual: la condición es `HALF_OPEN`, no «2.000
milisegundos».

**El consumidor se apaga sin apagar el broker.** `E1` para el `MessageListenerContainer` desde el
registro de listeners: así el «servicio de avisos caído» convive con una cola que sigue aceptando
mensajes, que es exactamente el escenario del crimen.

## Una regresión que este lab provocó, y cómo se resolvió

Al mover el aviso a la cola, dos tests heredados del **Lab 11** se pusieron en rojo: esperaban la
notificación que ahora viaja por el broker. Y dos de los **Labs 07 y 10** también, porque Actuator
registra solo un indicador `rabbit` y sus contextos no tienen broker → `/actuator/health` en 503.

Se resolvió como manda el precedente del Lab 07 («romper hacia atrás»): los tests del Lab 11
**ganaron** un contenedor de RabbitMQ —siguen afirmando lo mismo, solo que ahora el aviso pasa por
otro sitio—, y el indicador `rabbit` se dejó **fuera del health** por criterio, no por comodidad. El
porqué está en `TEORIA.md §8`, y es contenido: si el broker tumbara readiness, el laboratorio se
contradiría a sí mismo.

## Lo que sigue corriendo de los labs anteriores

| Paquete | De dónde viene | Qué sigue vigilando |
|---|---|---|
| `latidos/` | Lab 11 | Scheduling, candado distribuido, `@Async`, evento transaccional |
| `tablero/` | Lab 10 | Health real, métricas, exposición, caché |
| `trazabilidad/` | Lab 09 | `traceId`, log JSON, auditoría, adjuntos |
| `resiliencia/` | Lab 08 | Timeout de TESO, degradación elegante |
| `seguridad/` | Lab 07 | Puerta cerrada, login, firma, roles |
| `concurrencia/` | Lab 06 | Folio único, idempotencia, rollback |
| `arquitectura/` | Lab 02 en adelante | AU-01…AU-07 y sus siete mordidas |
