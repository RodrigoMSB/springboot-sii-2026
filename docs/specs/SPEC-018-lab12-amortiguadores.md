# SPEC-018 · Lab 12 «Amortiguadores»

| Campo | Valor |
|---|---|
| ID | SPEC-018 |
| Título | Duodécimo laboratorio: lo que se envía no se pierde — mensajería y resiliencia (M11) |
| Autor | Arquitecto |
| Aprueba | PO (Rodrigo) — **APROBADA** |
| Depende de | SPEC-017 (Lab 11) |
| Estado | LISTA PARA EJECUCIÓN |

> **Instrucción de ejecución (mocito):** guardar este archivo íntegro en
> `docs/specs/SPEC-018-lab12-amortiguadores.md` y commitearlo en rama antes de ejecutar.
> **Base:** apila sobre `spec/017` (el Lab 12 deriva del Lab 11; no escribas "parte desde
> main" — esa contradicción fue error del arquitecto en la SPEC-017 y no se repite).
> Protocolo de dos etapas vigente.

---

## §1 · Objetivo

Que exista `labs/lab-12-amortiguadores/`: la sesión del **Módulo 11 oficial** (Mensajería
y Resiliencia). El alumno sale con: los avisos que ya no se pierden cuando el
destinatario está caído, un consumidor idempotente que tolera la entrega duplicada, una
cola de mensajes muertos para lo que nadie pudo procesar, y un circuit breaker que deja
de golpear al que está en el suelo. Encadenamiento: `starter/` = `solucion/` del Lab 11
+ el aviso que se evapora + los huecos.

## §2 · El crimen

El Lab 11 dejó las notificaciones asíncronas — pero se envían **al aire**: si el
servicio de avisos no está, el mensaje se pierde y nadie se entera. El `starter/` trae
ese envío directo. Guion del relator (10 min):

1. `./bin/start-lab.sh --avisos-caidos` (bandera P-04: levanta todo con el servicio de
   avisos apagado — mecanismo a tu juicio; WireMock o el consumidor detenido, declara
   cuál).
2. Se emiten N folios. La API responde 201 a todos, feliz. **Cero errores en pantalla.**
3. Se levanta el servicio de avisos. No pasa nada: **esos avisos no existen.** Se
   evaporaron mientras estuvo caído, y no hay rastro de cuáles fueron.
4. Carolina: *"Anoche el servicio de avisos estuvo caído dos horas. Doscientos
   contribuyentes tienen su folio y ninguno lo sabe. Y lo peor: nadie puede decirme
   **cuáles** doscientos, porque esos avisos no quedaron en ninguna parte. Un aviso que
   se pierde en silencio es peor que un error: el error, al menos, se ve."*

Lección estructural: **una llamada directa exige que el otro esté vivo en el mismo
instante; una cola solo exige que exista. La cola es el amortiguador entre dos sistemas
con ritmos distintos.**

## §3 · Los tres actos

- **Acto 1 · Choque:** los avisos evaporados, sin error visible.
- **Acto 2 · El parche bruto que FUNCIONA:** reintentos en memoria (`@Retryable` con 3
  intentos). ¡Ya no se pierden tantos! La guía lo mide y lo desmonta: los reintentos en
  memoria mueren con el proceso (un despliegue a mitad de camino se lleva la lista), no
  sirven para una caída de dos horas (¿reintentas 7.200 veces?), y **cada reintento
  golpea a un servicio que ya está en el suelo** — el patrón que convierte una caída en
  una caída más larga. Reintentar no es persistir.
- **Acto 3 · La forma correcta:** el aviso se **entrega a una cola** (RabbitMQ, D-005) y
  se acabó la responsabilidad del emisor; el consumidor lo toma cuando puede, es
  idempotente (porque va a recibir duplicados, es un hecho, no una posibilidad), y lo
  que falla repetidamente cae a una **DLQ** donde alguien lo puede ver y decidir. Sobre
  las llamadas que sí deben ser síncronas, el circuit breaker corta el golpeteo.

## §4 · Los TODOs (4 × ~15 min)

1. **TODO_1 — El aviso va a la cola:** productor AMQP; el servicio de emisión publica el
   evento en vez de llamar al aviso directo (se conecta con el evento transaccional del
   Lab 11: se publica **después del commit**, no antes). Test del enunciado: con el
   consumidor **apagado**, el mensaje **existe en la cola** (no se perdió) y la API
   responde 201 igual de rápido.
2. **TODO_2 — El consumidor idempotente:** `@RabbitListener` que procesa el aviso. Test
   estrella: **el mismo mensaje entregado dos veces produce un solo aviso** (clave de
   idempotencia declarada — se conecta con RN-05 del Lab 06: la idempotencia ya no es
   teoría nueva, es el mismo principio en otro transporte). La lección: *"exactly once"
   no existe; existe "at least once" + idempotencia.*
3. **TODO_3 — La cola de los muertos (DLQ):** configurar reintentos acotados y el
   enrutamiento a DLQ del mensaje envenenado. Test: un mensaje que siempre falla termina
   en la DLQ **con su causa**, y la cola principal **sigue fluyendo** (no se atasca
   detrás del muerto — ese es el punto: un mensaje malo no puede bloquear a los buenos).
4. **TODO_4 — El circuit breaker:** sobre la llamada síncrona que queda (TESO, del
   Lab 08), Resilience4j: umbral, apertura, medio-abierto, y **fallback**. Test: tras N
   fallos el circuito abre y las llamadas siguientes **fallan rápido sin tocar la red**
   (medido en tiempo, citado); tras la ventana, el circuito prueba y se recupera. La
   teoría distingue las **primitivas nativas de Framework 7** (`@Retryable`,
   `@ConcurrencyLimit`) de lo que aporta Resilience4j, con criterio de cuándo basta cada
   una — el temario lo exige.

## §5 · Anatomía

La de SPEC-000 §7.6 completa. El compose suma **RabbitMQ** con tag fijado (verifica el
patch vigente; el de referencia era `rabbitmq:4.2.4-management`) — declara la divergencia
en el allowlist. **Kafka no se instala**: se compara conceptualmente (D-005) y la teoría
explica el criterio de elección (cola de trabajo vs registro de eventos), sin costar
media sesión en RAM. `TEORIA.md`: M11 completo (mensajería asíncrona y el amortiguador;
RabbitMQ vs Kafka con criterio; exchanges, colas, bindings; productor y consumidor; ack,
reintentos y el mensaje envenenado; DLQ; idempotencia del consumidor y por qué
"exactly once" es una promesa que nadie cumple; patrones de resiliencia: retry, circuit
breaker, rate limiter, bulkhead, time limiter; primitivas nativas vs Resilience4j).
**Siembra del Lab 13 (egreso):** *"tu sistema ya aguanta que los demás se caigan. La
próxima semana no hay crimen: hay un brief de Carolina, un repositorio casi vacío, y
tres horas. Nadie te va a decir qué hacer."* Plantillas con trampa registrada y la
transcripción natural: **el mensaje en la DLQ con su causa, pegado tal cual** (pregunta
de criterio: *"el mensaje falló 3 veces y quedó en la DLQ. ¿Por qué es mejor eso que
seguir reintentando para siempre?"*).

## §6 · Verificación del ejecutor (etapa 1)

Citado, sobre estado limpio: (1) `91-e2e` ×2 — starter falla nombrando los 4 TODOs,
solución 100 %; (2) el crimen vivido: `--avisos-caidos` en el starter → 201 en todo, cero
errores, y **los avisos no existen** al levantar el servicio (citado); en la solución →
los mensajes esperan en la cola y se procesan al volver (citado, con el conteo);
(3) acto 2 medido: `@Retryable` en copia → cita cuántos se salvan y cuántos no, y el
efecto sobre el servicio caído; (4) idempotencia: mismo mensaje ×2 → un solo aviso
(citado); (5) DLQ: el envenenado cae con su causa **y la cola principal sigue
fluyendo** (ambos citados); (6) circuit breaker: tiempos antes/después de abrir
(citados — la diferencia debe ser evidente), y la recuperación; (7) los tests de
mensajería son **deterministas** (×3 corridas citadas; Awaitility, sin `Thread.sleep` —
AU-05 vigila); (8) manifiesto discrimina; (9) `deriva` y `siembra` (audita L11→L12)
verdes en el runner; (10) CI verde con RabbitMQ en el job `app` — si el contenedor
complica el runner, **repórtalo, no lo escondas**; (11) `ESTADO.md` al día; estimación
honesta por TODO.

## §7 · La Prueba del PO (etapa 2 — cierra la SPEC)

Invitación literal, resultados esperados escritos, tres pasos: (1) `--avisos-caidos` en
el starter, emitir folios, levantar el servicio → **ver que no llega nada, y que no hay
forma de saber cuáles se perdieron**; (2) lo mismo en la solución → los avisos esperan
en la cola y **llegan todos** cuando el servicio vuelve; (3) `90 --dir solucion` →
aprobado. Declara Java/Docker por paso y el tiempo de arranque de RabbitMQ.

## §8 · Criterios de aceptación

- [ ] SPEC commiteada antes del material; rama apilada sobre `spec/017`, PR abierto.
- [ ] Lab 12 completo (las 5 piezas); evidencia §6 íntegra, actos medidos.
- [ ] Idempotencia del consumidor demostrada (mismo mensaje ×2 → un efecto).
- [ ] DLQ demostrada **y** la cola principal fluyendo detrás del muerto.
- [ ] Circuit breaker con la diferencia de tiempo citada (abierto = falla rápido).
- [ ] Tests deterministas ×3, sin `Thread.sleep`.
- [ ] RabbitMQ con tag fijado; Kafka solo conceptual (D-005 respetada).
- [ ] TODOs ≤ ~15 min c/u (o recorte aplicado y declarado, como el TODO_2 del Lab 11).
- [ ] Siembra L11→L12 auditada; Lab 12 siembra el 13 con el gancho del brief.
- [ ] `ESTADO.md` y bitácora al día.
- [ ] **Prueba del PO reportada** — cierra la SPEC.
- [ ] Commits `SPEC-018:`; checks verdes citados.

## §9 · Reporte

Evidencia de §6, tiempos por TODO, decisiones declaradas (mecanismo de
`--avisos-caidos`, clave de idempotencia elegida, parámetros del circuit breaker y su
porqué, cómo se comporta RabbitMQ en CI), URL del run, `git log --oneline`,
discrepancias y hallazgos — sin tocarlos. Cierra con la invitación del §7.
