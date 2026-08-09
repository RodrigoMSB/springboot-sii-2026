# Teoría · Módulo 12 (Asincronía con Hilos Virtuales, Scheduling y Eventos)

## Índice

1. [El reloj con problema de identidad](#1-el-reloj-con-problema-de-identidad)
2. [`@EnableScheduling` y `@Scheduled`](#2-enablescheduling-y-scheduled)
3. [`fixedRate` vs `fixedDelay`](#3-fixedrate-vs-fixeddelay)
4. [El cron y su zona horaria](#4-el-cron-y-su-zona-horaria)
5. [Muchas instancias: el problema de verdad](#5-muchas-instancias-el-problema-de-verdad)
6. [El bloqueo distribuido](#6-el-bloqueo-distribuido)
7. [Hilos virtuales (Java 25)](#7-hilos-virtuales-java-25)
8. [`@Async` y sus trampas](#8-async-y-sus-trampas)
9. [Eventos y `@TransactionalEventListener`](#9-eventos-y-transactionaleventlistener)
10. [DO/DON'T · Glosario · Siembra del Lab 12](#10-dodont--glosario--siembra-del-lab-12)

---

## 1. El reloj con problema de identidad

El Lab 10 dejó el tablero midiendo. Y midiendo apareció esto: **el contador de cierres marcaba 2**.

La DGT corre en dos servidores para aguantar la carga. A medianoche, los dos dispararon la misma
tarea programada. Los dos se creyeron el único.

> El reloj no tiene la culpa. La culpa es de quien programó una tarea sin preguntarse cuántos la
> iban a escuchar.

El resultado: totales duplicados, y doscientos contribuyentes con el mismo aviso dos veces. Y lo
peor —lo que hace este bug tan caro— es que **con una instancia el código es perfecto**. Funcionó
meses. Se rompió el día que alguien escaló a 2, y ese alguien no tocó una línea de este código.

## 2. `@EnableScheduling` y `@Scheduled`

```java
@Configuration
@EnableScheduling
public class AsyncConfig { }

@Scheduled(fixedDelayString = "${dgt.cierre.intervalo-ms}")
public void latido() { ... }
```

Sin `@EnableScheduling`, la anotación `@Scheduled` es un **comentario decorativo**: no falla, no
avisa, y no hace nada. Es el mismo silencio del `@EnableCaching` olvidado del Lab 10 y del
`@EnableAsync` que veremos en el §8. Spring no grita cuando le pides algo que no habilitaste.

**El planificador tiene un pool, y su default es UNO.** Ese uno esconde bugs: con un solo hilo, las
tareas hacen fila y muchos problemas de solapamiento no aparecen. El día que alguien agrega una
segunda tarea programada y sube el pool —que es lo que pasa siempre—, los problemas salen todos
juntos y nadie los relaciona con la configuración vieja.

## 3. `fixedRate` vs `fixedDelay`

Suenan igual. No lo son.

```
fixedRate = 10s   (mide de INICIO a INICIO)
  |--trabajo (4s)--|      espera      |--trabajo--|
  0s              4s                 10s
  ... pero si el trabajo dura 14s:
  |--------trabajo (14s)--------|
                 |--------trabajo (14s)--------|     <- SE SOLAPAN
  0s            10s

fixedDelay = 10s  (mide de FIN a INICIO)
  |--------trabajo (14s)--------|    espera 10s    |--trabajo--|
  0s                          14s                 24s          <- jamás se solapan
```

| | Mide | Se puede solapar | Para qué |
|---|---|---|---|
| `fixedRate` | inicio → inicio | **Sí** (si el pool > 1) | Tareas cortas e idempotentes |
| `fixedDelay` | fin → inicio | No | Tareas que escriben, o cuya duración crece |

La regla: **si escribe, o si su duración depende del volumen de datos, `fixedDelay`**. El cierre
nocturno crece con los años de declaraciones. Elegir `fixedRate` «porque suena a cada minuto» es el
bug que aparece el día que la base creció.

Y ojo con `initialDelay`: sin él, la tarea arranca en cuanto el contexto está listo — es decir, en
mitad del despliegue, cuando la mitad de la flota todavía está arrancando.

## 4. El cron y su zona horaria

```java
@Scheduled(cron = "0 0 3 * * *", zone = "America/Santiago")
```

Seis campos: `segundo minuto hora día mes día-de-semana`. (Ojo: el cron de Spring tiene **seis**,
el de Unix tiene cinco — el de los segundos es de Spring.)

**Sin `zone`, se usa la del sistema.** Y el sistema es el servidor, que en la nube suele estar en
UTC aunque la DGT esté en Santiago: el «cierre de las 3 AM» corre a medianoche.

Peor todavía: **Chile mueve la hora en marzo y septiembre**. Un cierre que funcionó todo el verano
se corre una hora un domingo cualquiera. Nadie relaciona el reporte raro del lunes con un cambio
de horario de dos días antes. Declarar la zona cuesta doce caracteres y ahorra ese día.

## 5. Muchas instancias: el problema de verdad

`@Scheduled` es **local a la JVM**. Cada instancia tiene su propio planificador, y ninguno sabe de
los otros. Con N instancias, la tarea corre N veces.

### El parche que funciona (acto 2)

```yaml
dgt.cierre.habilitado: true    # …y en la instancia 2, false
```

Funciona. El cierre corre una vez. Y tiene dos agujeros:

1. **¿Qué pasa la noche que esa instancia está caída?** No corre **nadie**. Cambiaste un problema
   ruidoso (dos cierres, que se ven) por uno silencioso (ningún cierre, que no se ve hasta que
   alguien pregunta). El silencioso es peor.
2. **¿Y cuando escalas a 5 réplicas en Kubernetes?** Todas tienen la **misma** configuración: esa
   es la definición de réplica. No hay dónde poner el `false`.

El parche funciona porque hoy sabes cuántas instancias hay y cómo se llaman. Deja de funcionar en
cuanto eso lo decide un orquestador.

## 6. El bloqueo distribuido

Si el candado no puede vivir en el código ni en la configuración, tiene que vivir donde **todas las
instancias miran**: la base de datos.

Es **la misma lección del Lab 06**. Allí, dos emisiones concurrentes se llevaban el mismo folio, y
la solución no fue `synchronized` —que solo sabe de su JVM— sino poner el candado en el **dato**,
con `SELECT ... FOR UPDATE`. Aquí es idéntico, con otro disfraz: entonces eran dos hilos, ahora son
dos servidores. La escala cambió; el razonamiento, no.

```sql
INSERT INTO candado_tarea (nombre, tomado_por, expira_en)
VALUES (:nombre, :quien, now() + make_interval(secs => :segundos))
ON CONFLICT (nombre) DO UPDATE
   SET tomado_por = EXCLUDED.tomado_por, expira_en = EXCLUDED.expira_en
 WHERE candado_tarea.expira_en < now()
```

Tres decisiones, y las tres son la lección:

- **Atomicidad.** Mirar y tomar son la **misma** sentencia. Escrito como `if (libre) tomar()`, entre
  las dos líneas cabe la otra instancia entera.
- **Expiración.** Si el que lo tomó muere a mitad del cierre, sin expiración el candado queda tomado
  por un muerto y **el cierre no vuelve a correr jamás**. Nadie se entera hasta que Carolina
  pregunta por el resumen del martes.
- **El reloj es el de la base.** Dos servidores con dos minutos de desfase —normalísimo— no se
  ponen de acuerdo sobre si algo expiró. El único reloj que ambos comparten es el del motor.

⚠️ **El compromiso que hay que declarar:** si el trabajo tarda **más** que la expiración, dos
instancias pueden solaparse. El TTL se elige con holgura sobre la duración real, y esa elección se
documenta. No hay bloqueo distribuido sin esta decisión — quien te venda uno «sin compromisos» no
te ha contado esta parte.

> En producción se usan librerías (ShedLock, o el `LockRegistry` de Spring Integration). Se escribe
> a mano una vez para entender qué hacen — y para poder juzgar si la librería que elijas resuelve
> de verdad estos tres puntos.

## 7. Hilos virtuales (Java 25)

Un hilo de plataforma es un hilo del sistema operativo: ~1 MB de pila, caro de crear, y **bloqueado
es un desperdicio total** — el sistema operativo lo tiene reservado sin hacer nada.

Un **hilo virtual** lo gestiona la JVM: kilobytes, baratísimo de crear, y cuando se bloquea
esperando E/S **se desmonta** de su hilo portador y devuelve la máquina.

| | Se benefician | No cambia nada |
|---|---|---|
| **Carga** | Esperar E/S: HTTP, base de datos, correo, archivos | Cálculo puro (CPU) |
| **Por qué** | El hilo está bloqueado el 99 % del tiempo | Un hilo virtual no te da un núcleo más |

**Qué cambia en el dimensionamiento.** Antes, el pool era la palanca: pocos hilos y las tareas
hacen fila; muchos y la máquina se ahoga. Con virtuales, uno por tarea y se acabó la apuesta.

**Qué NO cambia.** El servicio del otro lado sigue teniendo su límite. Diez mil hilos virtuales
golpeando un servidor de correo que aguanta cincuenta no es paralelismo: es una denegación de
servicio con tu firma. Los hilos virtuales quitan el cuello de botella de **tu** lado, no del ajeno.
Si hay que limitar, se limita en la salida (`setConcurrencyLimit`), no en el pool.

> **Structured concurrency** (vista conceptual): agrupar varias tareas concurrentes en un ámbito que
> las trata como una unidad — si una falla, se cancelan las hermanas; el ámbito no se cierra hasta
> que todas terminan. Resuelve el clásico «lancé cinco tareas y perdí la pista de dos». No se teclea
> hoy; se nombra para que sepas que existe cuando lo necesites.

## 8. `@Async` y sus trampas

```java
@Async("ejecutorVirtual")
public void notificar(String rut, String mensaje) { ... }
```

Sin `@EnableAsync`, no hace nada. Sin avisar. (Van tres.)

**Trampa 1 — las excepciones.** Un `@Async` que devuelve `void` y revienta: nadie se entera. El
llamador ya siguió su camino; no hay a quién propagarle nada. Se resuelve con un
`AsyncUncaughtExceptionHandler`, o cambiando la firma a `CompletableFuture<T>` — ahí la excepción
viaja dentro del futuro y quien haga `join()` la recibe.

**Trampa 2 — el proxy. Por tercera vez en el curso.** Una llamada entre métodos de la misma clase
**no pasa por el proxy**: `@Async` no se aplica y corre síncrono. Es el mismo límite de
`@Transactional` (Lab 06) y del aspecto de auditoría (Lab 09). A estas alturas ya no es mala
suerte: **es cómo funciona Spring**. Las anotaciones no son magia del compilador — son un objeto
envolviendo a otro, y desde dentro del objeto envuelto el envoltorio no existe.

**Trampa 3 — el contexto no viaja solo.** El hilo nuevo no hereda la transacción, ni el
`SecurityContext`, ni el MDC del Lab 09. Si tu log asíncrono perdió el `traceId`, es esto.

**`CompletableFuture` para componer:** `thenApply`, `thenCombine`, `allOf`. Cuando necesitas
lanzar tres cosas a la vez y esperar a las tres, o encadenar el resultado de una en la siguiente.

## 9. Eventos y `@TransactionalEventListener`

```java
eventos.publishEvent(new FolioEmitido(id, numero, rut));   // publico un HECHO
```

Un evento es un **hecho del pasado**, no una orden. Si `EmisionService` llamara al notificador
estaría *mandando*; publicando un hecho dice «esto ocurrió» y se desentiende de quién se entera.
Mañana se agrega un listener que actualiza un tablero, y otro que archiva para el fiscalizador, sin
tocar `EmisionService`.

**Y ahora la parte cara.** Un `@EventListener` normal reacciona **cuando el evento se publica**, es
decir *dentro* de la transacción, cuando el folio todavía no es un hecho. Si esa transacción
revierte después, el folio nunca existió… y el correo ya salió.

> **Avisar de algo que no ocurrió es peor que no avisar.** El que no recibe nada, pregunta. El que
> recibe un folio inexistente lo anota, lo declara, lo cita ante un fiscalizador — y el problema
> aparece meses después, cuando ya es de otro.

```java
@TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
```

Mueve el listener al otro lado de la frontera: se ejecuta solo si la transacción llegó a puerto.

**El precio, que hay que saber:** ahí ya **no hay transacción**. Si el listener escribiera, tendría
que abrir la suya (`REQUIRES_NEW`), y si fallara, lo ya confirmado no se desharía. Después del
commit el mundo vuelve a ser un lugar sin garantías. Por eso lo que va ahí son efectos **externos**
—avisar, publicar, archivar—, no correcciones al dato.

Las otras fases: `BEFORE_COMMIT` (aún se puede abortar), `AFTER_ROLLBACK`, `AFTER_COMPLETION`.

**Dos desacoples encadenados, y cada uno resuelve algo distinto:** el **evento** desacopla *quién*
se entera; el **`@Async`** desacopla *cuándo* se hace. Confundirlos lleva a creer que con `@Async`
bastaba — no: sin el evento, `EmisionService` seguiría teniendo que saber que hay que notificar.

## 10. DO/DON'T · Glosario · Siembra del Lab 12

| ✅ DO | ❌ DON'T |
|---|---|
| `fixedDelay` para trabajo que escribe | `fixedRate` «porque suena a cada minuto» |
| `zone` explícita en el cron | Confiar en la hora del servidor |
| Candado en la base, visible por todos | Una bandera en `application.yml` |
| Mirar y tomar en una sentencia atómica | `if (libre) tomar()` |
| Expiración en el candado | Un candado eterno tomado por un muerto |
| El reloj de la base | El reloj de cada máquina |
| Hilos virtuales para esperar E/S | Hilos virtuales para quemar CPU |
| Limitar en la salida si el destino es frágil | Diez mil hilos contra un servicio que aguanta cincuenta |
| `AFTER_COMMIT` para efectos externos | `@EventListener` normal para avisar |
| Manejar la excepción del `@Async void` | Dejar que se pierda en el aire |

- **`@Scheduled`** — tarea programada, **local a la JVM**.
- **`fixedRate` / `fixedDelay`** — inicio→inicio / fin→inicio.
- **Bloqueo distribuido** — candado en un recurso compartido, con expiración.
- **Hilo virtual** — hilo gestionado por la JVM; se desmonta al bloquearse.
- **`@Async`** — ejecuta en otro hilo. No devuelve nada útil salvo con `CompletableFuture`.
- **Evento de aplicación** — un hecho publicado, no una orden.
- **`AFTER_COMMIT`** — el listener corre solo si la transacción confirmó.

---

Hoy el cierre corre **una vez**, aunque haya diez instancias; el reloj sabe en qué país vive; las
notificaciones no hacen esperar a nadie; y nadie recibe el aviso de un folio que no existe.

🌱 **Siembra del Lab 12 — «Amortiguadores».**

El cierre ya corre una vez y las notificaciones no bloquean a nadie. Pero anoche el servicio de
notificaciones estuvo caído dos horas.

Y esos avisos **no existen**. Se perdieron en el aire.

Míralo otra vez: nuestro notificador guarda lo enviado en una cola **en memoria**. Si el proceso se
reinicia —un despliegue, un pod que se recicla, un `OutOfMemory`—, ahí no queda nada. Ni los
enviados ni, sobre todo, los que faltaban por enviar. `@Async` mueve el trabajo a otro hilo; no lo
hace sobrevivir a nada.

La próxima semana: **lo que se envía se guarda hasta que alguien lo reciba**. Colas, reintentos,
mensajes que se pueden entregar dos veces sin hacer daño, y qué se hace con el que no hay forma de
entregar.
