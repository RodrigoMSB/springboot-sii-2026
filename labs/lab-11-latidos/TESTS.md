# Tests · Lab 11

Los del **enunciado** (`src/test/java/cl/dgt/tramites/enunciado/`) no se tocan: los protege
`manifiesto-tests.sha256`. `E<n>` corresponde a `TODO_<n>`. Todo lo demás que quieras escribir es
territorio libre.

| Test | Qué prueba | Verifica |
|---|---|---|
| `E1_ElRelojBienDeclaradoIT` | La tarea que Spring **registró** es de tipo `FixedDelayTask`, no `FixedRateTask`. Y el cron de producción declara `zone = America/Santiago`. | TODO_1 |
| `E2_CandadoDistribuidoIT` | Ocho hilos piden el candado a la vez y **exactamente uno** lo consigue. Ocho latidos simultáneos producen **un** cierre, **una** fila y `maximoSimultaneas == 1`. Y un candado **vencido** se puede arrebatar, pero uno vigente no. | TODO_2 |
| `E3_AsincroniaVirtualIT` | `notificar()` vuelve enseguida y el aviso llega después, **en un hilo virtual**. Emitir un folio responde sin esperar al correo. Y la **autoinvocación no es asíncrona**. | TODO_3 |
| `E4_EventoTransaccionalIT` | Con **commit**, el aviso sale. Con **rollback**, el aviso **no** sale: ese folio nunca existió. | TODO_4 |

## Cómo se mide, y por qué así

**El candado se prueba con concurrencia real, no con un doble.** Ocho hilos virtuales se bloquean
en una barrera (`CountDownLatch`) y salen a la vez. Lo que se afirma —«exactamente un ganador»— es
cierto para *cualquier* orden de ejecución, no para uno afortunado. Y contra un PostgreSQL de
verdad: lo que se está probando es que **el motor serializa**, y eso un mock no lo puede
demostrar.

**Sin dormir, nunca.** AU-05 prohíbe `Thread.sleep` en todo el proyecto, y no es una manía: un
test que duerme afirma «en mi máquina tardó menos que esto», que no es una afirmación sobre el
sistema. Se espera con Awaitility sobre una condición, o con barreras deterministas.

**La expiración se prueba sin esperar.** Tomar el candado con una vigencia ya vencida
(`Duration.ofSeconds(-1)`) simula «el dueño murió» de forma instantánea y determinista. Esperar a
que expire de verdad convertiría el test en una siesta.

**El solapamiento se afirma por declaración.** Para ver dos ejecuciones pisándose habría que
alargar el trabajo artificialmente, y hacerlo sin `Thread.sleep` exigiría meter una traba en el
código de producción solo para el test. La propiedad que importa es binaria y vive en la
declaración: `fixedDelay` mide de fin a inicio y **por construcción** no puede solaparse. `E1`
afirma eso; el invariante de ejecución —«el trabajo ocurre una sola vez»— lo mide `E2` con
concurrencia real.

**La afirmación negativa necesita una ventana.** «El aviso NO sale» no se puede comprobar en un
instante: si fuera a salir, saldría un momento después. Por eso `E4` usa `.during(2s)` — sostiene
la condición durante un rato en vez de mirarla una sola vez.

## Lo que sigue corriendo de los labs anteriores

| Paquete | De dónde viene | Qué sigue vigilando |
|---|---|---|
| `tablero/` | Lab 10 | Health real, métricas de negocio, exposición, caché medido |
| `trazabilidad/` | Lab 09 | El `traceId`, el log JSON, el aspecto de auditoría, los adjuntos |
| `resiliencia/` | Lab 08 | Timeout de TESO, degradación elegante, endurecimiento |
| `seguridad/` | Lab 07 | Puerta cerrada, login, firma del token, roles |
| `concurrencia/` | Lab 06 | Folio único bajo concurrencia, idempotencia, rollback |
| `arquitectura/` | Lab 02 en adelante | AU-01…AU-07 y sus siete mordidas |

Cuando el enunciado de un lab se aprueba, deja de ser enunciado y pasa a ser **regresión**. Es lo
que avisa si el trabajo de hoy rompió lo de ayer — meter asincronía en la emisión de folios no
puede costarte la idempotencia del Lab 06 ni el hilo de la traza del Lab 09.
