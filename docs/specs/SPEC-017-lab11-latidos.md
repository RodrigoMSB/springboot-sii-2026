# SPEC-017 · Lab 11 «Latidos»

| Campo | Valor |
|---|---|
| ID | SPEC-017 |
| Título | Undécimo laboratorio: el reloj con problema de identidad — scheduling, hilos virtuales y eventos (M10) |
| Autor | Arquitecto |
| Aprueba | PO (Rodrigo) — **APROBADA** |
| Depende de | SPEC-016 (Lab 10) |
| Estado | LISTA PARA EJECUCIÓN |

> **Instrucción de ejecución (mocito):** guardar este archivo íntegro en
> `docs/specs/SPEC-017-lab11-latidos.md` y commitearlo en rama antes de ejecutar.
> Parte desde `main`. Protocolo de dos etapas vigente.

---

## §0 · Resoluciones previas

1. **El bug cosmético del Lab 09** (tu hallazgo 1 de la SPEC-016: su `90-validar.sh`
   busca `T[0-9]_` pero sus tests se llaman `E1_…E4_`, así que la pista dirigida nunca
   se imprime): **arréglalo en esta pasada.** Es una línea, y una pista que no se
   imprime es material muerto. Declara el cambio.
2. **`spec/007` sigue viva** por la regla de no tocar ramas no mergeadas. Su contenido
   ya está en `main` vía #15 y solo conserva un commit de merge huérfano. **Bórrala
   ahora**, con esta autorización explícita del arquitecto, y cítalo.
3. **PRs abiertos:** #16 (Lab 10) espera la Prueba del PO; #17 (manifiesto) puede
   mergearse cuando el PO quiera. **No los mergees tú.** Parte esta rama desde `main`
   tal como está y declara en el reporte si tu trabajo choca con alguno en `ESTADO.md`
   (el ajuste trivial que ya anticipaste).

## §1 · Objetivo

Que exista `labs/lab-11-latidos/`: la sesión del **Módulo 10 oficial** (Procesamiento
asíncrono, tareas programadas y eventos). El alumno sale con: una tarea programada que
corre **una sola vez** aunque haya varias instancias, procesamiento asíncrono sobre
hilos virtuales de Java 25, y efectos secundarios desacoplados con eventos
transaccionales. Encadenamiento: `starter/` = `solucion/` del Lab 10 + el cierre
nocturno ingenuo + los huecos.

## §2 · El crimen

El `starter/` trae el **cierre nocturno**: una tarea `@Scheduled` que consolida los
trámites del día y notifica. Con una instancia, perfecta. Guion del relator (10 min):

1. `./bin/start-lab.sh --instancias 2` (bandera P-04: levanta **dos instancias** de la
   app contra la misma base, en puertos distintos — mecanismo a tu juicio; el criterio
   es que el efecto sea visible y reproducible).
2. En pantalla: el cierre se ejecuta **dos veces**. Los totales quedan duplicados, y
   las notificaciones salieron dos veces al mismo contribuyente.
3. Carolina: *"El viernes el cierre corrió dos veces y a doscientos contribuyentes les
   llegó el mismo aviso duplicado. Dos servidores, y los dos se creyeron el único. El
   reloj no tiene la culpa: la culpa es de quien programó una tarea sin preguntarse
   cuántos la iban a escuchar."*

Lección estructural: **`@Scheduled` es local a la JVM; en un mundo de muchas instancias,
"una vez al día" no significa nada hasta que alguien lo garantice.**

## §3 · Los tres actos

- **Acto 1 · Choque:** el cierre duplicado con dos instancias.
- **Acto 2 · El parche bruto que FUNCIONA:** una bandera en `application.yml`
  (`cierre.habilitado: true`) activada **solo en una instancia**. ¡Funciona! El cierre
  corre una vez. La guía confronta: ¿qué pasa cuando esa instancia justo está caída esa
  noche? (no corre nadie) ¿y cuando escalas a 5 réplicas idénticas en Kubernetes, donde
  todas tienen la misma config? El parche cambia un problema visible por uno silencioso
  — y el silencioso es peor.
- **Acto 3 · La forma correcta:** el candado vive **en un recurso compartido**, no en la
  configuración. Bloqueo distribuido sobre la base de datos (misma familia que el
  contador de folios del Lab 06 — la teoría lo conecta explícitamente): el que toma el
  lock ejecuta, los demás se van a dormir; si el que ejecutaba muere, el lock expira.

## §4 · Los TODOs (4 × ~15 min)

1. **TODO_1 — `fixedRate` vs `fixedDelay` y el cron que engaña:** corregir la tarea que
   está mal declarada (el andamio trae un `fixedRate` donde correspondía `fixedDelay`,
   con el efecto de solapamiento cuando la ejecución dura más que el intervalo). Test
   del enunciado: dos ejecuciones **no se solapan** (verificado con marcas de tiempo o
   un contador de concurrencia, **sin `Thread.sleep`** — AU-05 vigila; usa Awaitility).
   Incluye la zona horaria en el cron: `America/Santiago` explícita, y por qué omitirla
   es un error que solo se nota en marzo y septiembre.
2. **TODO_2 — El candado distribuido:** tabla técnica de lock (migración Flyway nueva) +
   la lógica de "tomo el lock o me voy" con expiración. Test del enunciado: **N hilos
   compitiendo, una sola ejecución** (determinista, sin sleeps). El invariante: el
   trabajo se hace exactamente una vez.
3. **TODO_3 — Asincronía sobre hilos virtuales (Java 25):** las notificaciones salen del
   hilo de la petición con `@Async` sobre un executor de **hilos virtuales**. Tests: la
   petición responde sin esperar la notificación (cota de tiempo), la notificación
   ocurre igual, y **la trampa del proxy**: la autoinvocación no es asíncrona (mismo
   límite de `@Transactional` y del AOP del Lab 09 — el arco se cierra). El andamio
   trae el executor configurado; el alumno escribe el uso y el manejo de excepciones
   asíncronas (que **no** se propagan al llamador: eso es la lección).
4. **TODO_4 — El evento transaccional:** el efecto secundario (notificar) se desacopla
   con `ApplicationEventPublisher` + `@TransactionalEventListener(AFTER_COMMIT)`. Test
   estrella: si la transacción **revierte**, la notificación **no sale**. El contraste
   con `@EventListener` normal —que notifica igual y miente— es el contenido: *avisar de
   algo que no ocurrió es peor que no avisar.*

## §5 · Anatomía

La de SPEC-000 §7.6 completa. `TEORIA.md`: M10 completo (`@EnableScheduling` y
`@Scheduled`; cron con zona horaria; `fixedRate` vs `fixedDelay` con el diagrama del
solapamiento; el problema de las múltiples instancias y el bloqueo distribuido,
conectado con el contador bloqueado del Lab 06; hilos virtuales de Java 25 — qué cargas
se benefician de verdad y qué cambia en el dimensionamiento de pools, structured
concurrency como vista conceptual; `@Async`, sus trampas y el manejo de excepciones;
`CompletableFuture` para componer; eventos de aplicación y `@TransactionalEventListener`).
**Siembra del Lab 12 «Amortiguadores»:** *"el cierre ya corre una vez y las
notificaciones no bloquean a nadie. Pero anoche el servicio de notificaciones estuvo
caído dos horas, y esos avisos no existen: se perdieron en el aire. La próxima semana,
lo que se envía se guarda hasta que alguien lo reciba."* Plantillas con trampa
registrada y la transcripción natural: **la doble ejecución del cierre en el log, con
los dos `traceId` distintos** (usa lo del Lab 09 — pregunta de criterio: *"¿cómo supiste
que eran dos ejecuciones y no una que se registró dos veces?"*).

## §6 · Verificación del ejecutor (etapa 1)

Citado, sobre estado limpio: (1) `91-e2e` ×2 — starter falla nombrando los 4 TODOs,
solución 100 %; (2) el crimen vivido: `--instancias 2` sobre el starter con la doble
ejecución citada (totales duplicados y/o doble notificación), y sobre la solución con
**una sola** ejecución citada; (3) acto 2 medido: la bandera en una instancia funciona
(citado) y la guía documenta sus dos fallos; (4) el test del candado es determinista
(×3 corridas citadas — si flakea, se rediseña, no se reintenta); (5) `@Async`: la
petición responde antes de que termine la notificación (tiempos citados) y la
autoinvocación **no** es asíncrona (test citado); (6) el evento transaccional: rollback
→ sin notificación (citado), y el contraste con `@EventListener` normal medido en copia;
(7) AU-05 verde (sin `Thread.sleep` en ningún test nuevo); (8) manifiesto discrimina;
(9) `deriva` y `siembra` (audita L10→L11) verdes en el runner; (10) CI verde, run
citado; (11) `ESTADO.md` al día; §0.1 y §0.2 resueltos y citados; estimación honesta por
TODO.

## §7 · La Prueba del PO (etapa 2 — cierra la SPEC)

Invitación literal, resultados esperados escritos, tres pasos: (1) `--instancias 2` en
el starter → **ver el cierre ejecutarse dos veces** y los totales duplicados; (2) lo
mismo en la solución → una sola ejecución, con el log mostrando a la otra instancia
diciendo "no tomé el lock, me voy a dormir"; (3) `90 --dir solucion` → aprobado.
Declara Java/Docker por paso.

## §8 · Criterios de aceptación

- [ ] §0 resuelto (pista del Lab 09 corregida, `spec/007` borrada, PRs no tocados).
- [ ] SPEC commiteada antes del material; rama + PR desde main.
- [ ] Lab 11 completo (las 5 piezas); evidencia §6 íntegra, actos medidos.
- [ ] El test del candado distribuido determinista (×3) y sin sleeps.
- [ ] El evento transaccional demostrado en ambos sentidos (commit / rollback).
- [ ] TODOs ≤ ~15 min c/u (o recorte aplicado y declarado).
- [ ] Siembra L10→L11 auditada; Lab 11 siembra el 12 con el gancho de los avisos
      perdidos.
- [ ] `ESTADO.md` y bitácora al día.
- [ ] **Prueba del PO reportada** — cierra la SPEC.
- [ ] Commits `SPEC-017:`; checks verdes citados.

## §9 · Reporte

Evidencia de §6, tiempos por TODO, decisiones declaradas (mecanismo de `--instancias`,
diseño del lock y su expiración, configuración del executor virtual), URL del run,
`git log --oneline`, discrepancias y hallazgos — sin tocarlos. Cierra con la invitación
del §7.

---

## Nota del ejecutor (anexada durante la ejecución, no altera el contrato)

La SPEC declara `Depende de SPEC-016` y a la vez ordena partir desde `main`. Al ejecutar,
`main` **no** contenía el Lab 10 (vivía solo en la rama del PR #16, abierto a la espera de
la Prueba del PO), de modo que no había `solucion/` del Lab 10 de la que derivar el
`starter/` que exige el §1, ni eslabón `L10 → L11` que el job `deriva` del §6.9 pudiera
auditar. Las dos instrucciones eran incompatibles con el estado real del repositorio.

Consultado el PO, resolvió: **error del arquitecto**; se apila esta rama sobre
`spec/016-lab10-observabilidad` y, al mergear, se reapunta la base a `main` **antes** del
merge, verificando el destino (la disciplina que corrigió el incidente del PR #7).
