# Teoría · Módulo 13 (Mensajería y Resiliencia)

## Índice

1. [El aviso que se evaporó](#1-el-aviso-que-se-evaporó)
2. [La cola es un amortiguador](#2-la-cola-es-un-amortiguador)
3. [RabbitMQ vs Kafka: el criterio](#3-rabbitmq-vs-kafka-el-criterio)
4. [Exchanges, colas y bindings](#4-exchanges-colas-y-bindings)
5. [Ack, reintentos y el mensaje envenenado](#5-ack-reintentos-y-el-mensaje-envenenado)
6. [La DLQ](#6-la-dlq)
7. [Idempotencia: por qué «exactly once» no existe](#7-idempotencia-por-qué-exactly-once-no-existe)
8. [Qué dependencias entran en readiness (y por qué el broker no)](#8-qué-dependencias-entran-en-readiness-y-por-qué-el-broker-no)
9. [Patrones de resiliencia](#9-patrones-de-resiliencia)
10. [Primitivas nativas de Framework 7 vs Resilience4j](#10-primitivas-nativas-de-framework-7-vs-resilience4j)
11. [DO/DON'T · Glosario · Siembra del Lab 13](#11-dodont--glosario--siembra-del-lab-13)

---

## 1. El aviso que se evaporó

El Lab 11 dejó las notificaciones asíncronas: la petición ya no espera al correo. Progreso real.
Pero el aviso se manda **al aire**.

Anoche el servicio de avisos estuvo caído dos horas. Doscientos contribuyentes tienen su folio y
ninguno lo sabe. Y la API respondió **201 a todo**, sin un solo error en pantalla.

> *«Y lo peor: nadie puede decirme cuáles doscientos, porque esos avisos no quedaron en ninguna
> parte. Un aviso que se pierde en silencio es peor que un error: el error, al menos, se ve.»*

Fíjate en que `@Async` no tiene la culpa. Hizo justo lo que promete: mover el trabajo a otro hilo.
**Mover no es guardar.** Un hilo distinto sigue siendo memoria del mismo proceso, y la memoria de un
proceso se va con el proceso.

## 2. La cola es un amortiguador

La frase que resume el módulo:

> Una llamada directa exige que el otro esté vivo **en el mismo instante**. Una cola solo exige que
> **exista**.

Dos sistemas rara vez tienen el mismo ritmo: la DGT emite folios a ráfagas y el servidor de correo
procesa a su paso. Una llamada directa obliga a que los dos ritmos coincidan; una cola los separa y
absorbe la diferencia. Eso es un amortiguador.

Lo que se gana, en concreto:

| | Llamada directa | Cola |
|---|---|---|
| El destino está caído | Se pierde | Espera |
| El destino es lento | Te frena | Le da igual |
| Hay una ráfaga | Lo saturas | Se encola |
| ¿Quién escucha? | Uno, y lo conoces | Los que se suscriban |
| Reinicias tu app | Se pierde lo pendiente | Sigue ahí (cola *durable*) |

⚠️ **Lo que la cola NO resuelve.** Si el broker está caído cuando publicas, el aviso se pierde
igual. Has movido el punto de fallo a un sitio mucho más fiable —un broker con disco y réplicas—
pero no lo has eliminado. La garantía completa se llama **outbox**: escribir el mensaje en la
*misma transacción* que el folio y publicarlo después, desde una tarea que lee esa tabla. Se nombra
hoy y no se teclea; ahora que hiciste el Lab 11, ya sabrías cómo.

## 3. RabbitMQ vs Kafka: el criterio

No es «cuál es mejor». Son cosas distintas.

| | **RabbitMQ** — cola de trabajo | **Kafka** — registro de eventos |
|---|---|---|
| Metáfora | Una bandeja de tareas | Un diario que se va escribiendo |
| El mensaje | Se consume y **desaparece** | Se **queda**; el lector guarda por dónde va |
| ¿Releer? | No | Sí, desde donde quieras |
| Reparto | Compite: uno lo toma | Cada consumidor lleva su propio avance |
| Enrutamiento | Rico (exchanges, bindings) | Simple (particiones) |
| Encaja en | «Haz esto»: avisar, generar, cobrar | «Esto pasó»: analítica, auditoría, replay |

Para la DGT, el aviso es **una tarea**: avísale a este contribuyente, una vez, y ya. Eso es
RabbitMQ. Si quisiéramos guardar todo lo que pasó para que tres equipos lo leyeran a su ritmo y
pudieran reprocesar el histórico, sería Kafka.

> **No lo instalamos** (D-005). Levantar Kafka cuesta media sesión de RAM y de tiempo, y lo que hay
> que aprender es el criterio de elección, no su `docker-compose`.

## 4. Exchanges, colas y bindings

**El productor nunca le habla a una cola.** Le habla a un **exchange**, que es un clasificador de
correo: recibe el mensaje con una **routing key** y, según los **bindings** declarados, lo deja en
cero, una o varias colas.

```
  EmisionService ──publica──> [exchange dgt.avisos] ──binding "folio.emitido"──> [cola dgt.avisos.q]
                                                                                        │
                                                                                   consumidor
```

Esa indirección es lo que permite agregar mañana un segundo consumidor —un tablero, un archivo para
el fiscalizador— **sin tocar una línea del productor**. Es el desacople del evento de aplicación del
Lab 11, ahora cruzando el límite del proceso.

**Durable** es la palabra que importa: la cola sobrevive a un reinicio del broker, y los mensajes
al reinicio de tu aplicación.

**JSON en el cable, no serialización de Java.** La nativa ataría el mensaje a la *clase*: mismo
`record`, mismo paquete, misma versión en los dos lados. Con JSON el contrato es la *forma* del
dato. Lo que cruza un límite de proceso se negocia por datos, no por tipos.

## 5. Ack, reintentos y el mensaje envenenado

El consumidor **confirma** (*ack*) cuando terminó. Si no confirma —porque falló o se cayó—, el
broker vuelve a entregar el mensaje. Es lo correcto: el broker no puede saber si alcanzaste a
trabajar.

Ahora bien, hay dos clases de fallo y confundirlas cuesta caro:

- **Transitorio** — una desconexión, un bloqueo momentáneo. Reintentar **funciona**.
- **Envenenado** — un JSON corrupto, un contribuyente que ya no existe, un campo que cambió de tipo
  entre dos versiones del productor. Reintentar **nunca** va a funcionar. Fallará las mil veces.

Por eso los reintentos van **acotados**. Y agotados, hay que decidir qué se hace con el mensaje:

```yaml
spring.rabbitmq.listener.simple:
  retry: { enabled: true, max-attempts: 3 }
  default-requeue-rejected: false     # <- esta línea es la que importa
```

Con `true` (**el default**), el mensaje rechazado vuelve al principio de la cola y empieza otra vez.
Bucle infinito — y, peor, **la cola se atasca detrás de él**: los mensajes buenos que van detrás no
se procesan nunca. Un solo mensaje malo dejando sin correo a doscientos contribuyentes.

## 6. La DLQ

Con `default-requeue-rejected: false`, el mensaje agotado se rechaza. ¿Y adónde va? A donde diga el
`x-dead-letter-exchange` de la cola. Si no dice nada, **se descarta en silencio** — perdiste el
aviso y no lo sabes, que es exactamente el crimen de este laboratorio.

```java
QueueBuilder.durable("dgt.avisos.q")
        .deadLetterExchange("dgt.avisos.dlx")
        .deadLetterRoutingKey("folio.emitido")
        .build();
```

La DLQ es una **bandeja de trabajo**, no un basurero, y lo que la convierte en bandeja es que el
mensaje llega **con su causa**: el broker adjunta cabeceras `x-death` con cuántas veces falló y por
qué. Alguien la abre, lee, y decide: reprocesar, corregir el origen, o descartar a conciencia.

Tres propiedades, y las tres importan:

1. El mensaje **no se pierde**.
2. La cola principal **sigue fluyendo**.
3. Queda **rastro** de qué falló y por qué.

## 7. Idempotencia: por qué «exactly once» no existe

Ningún sistema de mensajería garantiza «exactamente una vez». Los que dicen garantizarlo están
describiendo otra cosa —normalmente, una transacción dentro de su propio universo—.

Lo que existe es **at least once**: al menos una vez, y por tanto a veces dos. Y llega por razones
normalísimas, ninguna un fallo: procesas el aviso, te caes **antes** de confirmar, y el broker se lo
entrega a otro. El broker hizo lo correcto. El contribuyente recibió dos correos.

> La respuesta no es pedirle al broker una garantía que no puede dar. Es hacer que **recibir dos
> veces dé lo mismo que recibir una**.

Y esto **ya lo sabes hacer**. Es RN-05 del Lab 06 —reintentar la emisión devuelve el MISMO folio en
vez de crear otro— con otro transporte. La idempotencia no es un truco de mensajería: es cómo se
sobrevive a un mundo donde los reintentos existen.

```sql
INSERT INTO aviso_procesado (clave, procesado_en)
VALUES (:clave, now())
ON CONFLICT (clave) DO NOTHING
```

**Mirar y marcar en una sola sentencia**, igual que el candado del Lab 11: escrito como «¿existe?
entonces inserta», entre las dos líneas caben dos entregas concurrentes.

**La clave identifica el HECHO, no la entrega.** `folio-4471`, no un UUID por mensaje: un
identificador aleatorio por envío haría único cada reintento y la deduplicación no serviría de nada.
Elegir mal la clave es la forma más común de tener una idempotencia decorativa.

## 8. Qué dependencias entran en readiness (y por qué el broker no)

En el Lab 10 pusimos una regla: **readiness responde «¿puedo ATENDER?», no «¿está todo perfecto?»**.
Solo entra lo que, si falta, hace inútil a esta instancia. La base entró; TESO no, porque el Lab 08
nos había enseñado a seguir sirviendo sin él.

Ahora llega una dependencia nueva, y la regla se aplica igual:

```yaml
management.health.rabbit.enabled: false
```

**Con el broker caído, la DGT sigue atendiendo.** Se listan trámites, se consultan fichas, se emiten
folios. Lo único que no sale es el aviso — y desacoplarnos de eso es, literalmente, el trabajo de
esta sesión. Marcar la instancia como no-apta la sacaría de rotación y **tumbaría el servicio entero
por la dependencia de la que acabamos de aprender a no depender**.

Míralo desde el absurdo: pasarías tres horas construyendo un amortiguador para que el aviso
sobreviva a que el broker no esté… y luego apagarías la aplicación cuando el broker no está. El
laboratorio se contradiría a sí mismo.

**Lo que esto NO significa.** El broker caído sí es un problema y hay que verlo. Se ve donde
corresponde: en las **métricas** del Lab 10 y en la **DLQ**. Sacar la instancia de servicio no es
observabilidad — es una rabieta.

> La pregunta que hay que hacerse con cada dependencia nueva: *si esto se cae, ¿mi instancia queda
> inútil, o queda disminuida?* Inútil → readiness. Disminuida → métrica y alerta.

## 9. Patrones de resiliencia

| Patrón | Contra qué | En una frase |
|---|---|---|
| **Retry** | Fallos transitorios | Inténtalo otra vez, pocas veces |
| **Circuit breaker** | Un servicio caído | Deja de intentarlo un rato |
| **Rate limiter** | Saturar al de enfrente | No más de N por segundo |
| **Bulkhead** | El contagio | Que un servicio lento no se lleve todos tus hilos |
| **Time limiter** | La espera infinita | Ríndete a los N ms |

### El circuit breaker, en detalle

El timeout del Lab 08 impide que **una** llamada espere para siempre: es un presupuesto por
petición. No impide que **mil** llamadas sigan golpeando a un TESO que lleva veinte minutos caído,
gastando 800 ms cada una y estorbando a quien intenta levantarse. Eso es lo que convierte una caída
de dos minutos en una de veinte.

| Estado | Qué pasa | Para qué sirve |
|---|---|---|
| **CLOSED** | Todo pasa; se cuentan los fallos | Operación normal |
| **OPEN** | Nada pasa; se falla al instante, **sin tocar la red** | Protege **al otro** |
| **HALF_OPEN** | Pasan unas pocas de prueba | Se recupera **solo** |

Ese `HALF_OPEN` es la pieza que mucha gente olvida: sin él, un circuito abierto se quedaría abierto
para siempre y alguien tendría que reiniciar algo a mano. Habrías cambiado una caída por otra.

Y fíjate en el diseño: el *fallback* es la **misma** `TesoreriaNoDisponibleException` que el Lab 08
ya sabe degradar en un 503 con el trámite intacto. El circuito cambió *cuánto tarda* la mala
noticia, no *cuál* es. La capa de arriba no tuvo que aprender nada nuevo.

## 10. Primitivas nativas de Framework 7 vs Resilience4j

Spring Framework 7 trae **`@Retryable`** y **`@ConcurrencyLimit`** de fábrica
(`org.springframework.resilience.annotation`, con `@EnableResilientMethods`). Para muchos casos
**bastan**, y meter una librería sería sobrediseño.

| Necesito… | Con qué |
|---|---|
| Reintentar lo transitorio | `@Retryable` **nativo** |
| No saturar al de enfrente | `@ConcurrencyLimit` **nativo** |
| **Dejar de intentar** y recuperarme solo | **Resilience4j** (no hay circuit breaker nativo) |
| Rate limiter, bulkhead, time limiter, y métricas de todo eso | **Resilience4j** |

El criterio: **usa lo nativo mientras te alcance**. Una dependencia menos es una versión menos que
alinear, y ya viste en este curso cuántas veces un renombre silencioso rompe un tutorial viejo.

> **Nota de este laboratorio:** usamos el **núcleo** de Resilience4j, no su starter de Spring Boot.
> El starter es `resilience4j-spring-boot3` — de **Boot 3**—, y su autoconfiguración apunta a APIs
> que Boot 4 reorganizó. El núcleo no depende de Spring en absoluto. Y declararlo a mano tiene un
> premio: los estados quedan a la vista y el test puede afirmarlos, igual que con el
> `HealthIndicator` del Lab 10.

⚠️ **Y el reintento en memoria, para que quede claro por qué no bastaba** (es el acto 2 de la guía):
`@Retryable` con 3 intentos salva el fallo transitorio y **nada más**. Muere con el proceso, no
sirve para una caída de dos horas —¿reintentas 7.200 veces?—, y cada reintento **golpea a un
servicio que ya está en el suelo**. Reintentar no es persistir.

## 11. DO/DON'T · Glosario · Siembra del Lab 13

| ✅ DO | ❌ DON'T |
|---|---|
| Entregar el aviso a una cola durable | Llamar directo y rezar |
| Publicar **después** del commit | Publicar dentro de la transacción |
| Reintentos **acotados** | Reintentar para siempre |
| `default-requeue-rejected: false` + DLQ | Dejar el default y atascar la cola |
| Clave de idempotencia = el **hecho** | Un UUID por mensaje |
| Asumir que llegará dos veces | Confiar en «exactly once» |
| Circuit breaker sobre lo síncrono | Golpear a quien está caído |
| Lo nativo mientras alcance | Una librería por costumbre |
| Broker fuera de readiness | Tumbar la app porque el broker no está |

- **Exchange / routing key / binding** — a quién le hablas, con qué etiqueta, y a qué cola llega.
- **Durable** — sobrevive a reinicios.
- **Ack** — la confirmación de que terminaste.
- **Mensaje envenenado** — el que fallará siempre, reintentes lo que reintentes.
- **DLQ** — donde va lo que nadie pudo procesar, **con su causa**.
- **At least once** — al menos una vez; por tanto, a veces dos.
- **Idempotencia** — hacerlo dos veces da lo mismo que hacerlo una.
- **Outbox** — escribir el mensaje en la misma transacción que el dato, y publicarlo después.
- **CLOSED / OPEN / HALF_OPEN** — los tres estados del circuito.

---

Hoy el aviso dejó de perderse: espera en una cola que sobrevive a los reinicios, se entrega aunque
llegue dos veces, y lo que nadie puede procesar queda apartado con su causa en vez de atascar a los
demás. Y sobre lo que sigue siendo síncrono, la DGT dejó de golpear al que está en el suelo.

Tu sistema ya aguanta que los demás se caigan.

🌱 **Siembra del Lab 13 — el egreso.**

La próxima semana **no hay crimen**.

Hay un *brief* de Carolina —un problema de negocio, escrito como lo escribiría ella, sin una sola
instrucción técnica—, un repositorio casi vacío, y tres horas.

Nadie te va a decir qué hacer. Ni qué capas, ni qué patrón, ni qué probar primero. Esa es la
evaluación: no si recuerdas la sintaxis —la máquina la escribe mejor que tú y eso ya no se
discute—, sino si frente a un problema que no has visto **reconoces cuál es el problema** y sabes
qué pedir.

Doce sesiones para llegar aquí. La última la escribes tú.
