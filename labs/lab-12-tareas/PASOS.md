# Pasos · Lab 12 · Tareas y asincronía

Cinco pasos. Se trabaja en `practica/`, en vivo. El último necesita **dos aplicaciones corriendo
a la vez**.

```bash
cd practica
./mvnw spring-boot:run
```

Escucha en el **8103** (`solucion/`, en el 8104).

Hoy **no hay dependencias nuevas**: `@Scheduled`, `@Async` y los hilos virtuales vienen dentro de
lo que ya hay. Lo que se escribe:

```
Lab12Application.java        →  pasos 1 y 3 (las dos anotaciones que lo encienden)
tareas/                      →  pasos 1 y 2 (llega vacía)
services/NotificadorService  →  paso 3
application.yml              →  paso 4 (una línea)
```

---

## Paso 0 · Lo que hay

**Se corre:** `./mvnw spring-boot:run`, y

```bash
curl http://localhost:8103/tramites/quien
```

**En consola:**

```json
{"instancia":"instancia-8103",
 "hiloQueAtiende":"Thread[#58,http-nio-8103-exec-1,5,main]",
 "esVirtual":false}
```

Guárdese esa línea del hilo: en el paso 4 va a cambiar.

---

## Paso 1 · Una tarea que corre sola

**Se explica:** hasta ahora, todo lo que hizo la aplicación lo pidió alguien por HTTP. Hay trabajo
que no: el cierre nocturno, la limpieza de temporales, el reintento de los avisos que fallaron.
Nadie los pide; ocurren.

**Se escribe:** en `Lab12Application.java`, la anotación que lo enciende:

```java
@EnableScheduling
@SpringBootApplication
public class Lab12Application {
```

y `tareas/CierreNocturno.java`:

```java
@Component
public class CierreNocturno {

    private static final Logger log = LoggerFactory.getLogger(CierreNocturno.class);

    private final Instancia instancia;
    private final AtomicInteger vueltas = new AtomicInteger();

    public CierreNocturno(Instancia instancia) {
        this.instancia = instancia;
    }

    @Scheduled(fixedDelay = 5000, initialDelay = 3000)
    public void ejecutar() throws InterruptedException {
        int n = vueltas.incrementAndGet();
        log.info("[CIERRE] {} · vuelta {} · {} · hilo {}",
                instancia.nombre(), n, LocalTime.now().withNano(0), Thread.currentThread());
        Thread.sleep(1000);
    }

    public int vueltas() {
        return vueltas.get();
    }
}
```

**En consola** — sin llamar a nada, solo esperando:

```
[CIERRE] instancia-8103 · vuelta 1 · 13:00:07
[CIERRE] instancia-8103 · vuelta 2 · 13:00:13
[CIERRE] instancia-8103 · vuelta 3 · 13:00:19
```

**Lo que hay que notar:** entre vuelta y vuelta pasan **6 segundos**, no 5. Y ahí está el
contenido del paso:

| | qué mide | qué pasa si la tarea tarda más que el intervalo |
|---|---|---|
| `fixedRate = 5000` | cada 5 s **desde que empieza** una y empieza la siguiente | **se solapan**: arranca otra con la anterior a medio hacer |
| `fixedDelay = 5000` | 5 s **desde que TERMINA** una hasta que empieza la siguiente | nunca se solapan |

La tarea duerme 1 segundo, así que con `fixedDelay` el ciclo real es 5 + 1 = **6**. Los timestamps
de la consola lo confirman.

> **`fixedDelay` es el valor por defecto correcto.** Con `fixedRate`, el día que el cierre tarde
> más de lo normal —porque hay más trámites que de costumbre, justo el día que más importa— se
> ejecutan dos a la vez sobre los mismos datos.

`initialDelay` existe para no ejecutar la tarea en mitad del arranque, cuando la aplicación
todavía está levantándose.

---

## Paso 2 · Cron, y cómo se lee

**Se explica:** `fixedDelay` sirve para «cada tanto». Para «todos los días a las 3 de la mañana»
hace falta un cron.

**Se escribe:** `tareas/Recordatorio.java`

```java
@Component
public class Recordatorio {

    private static final Logger log = LoggerFactory.getLogger(Recordatorio.class);

    @Scheduled(cron = "*/10 * * * * *", zone = "America/Santiago")
    public void avisar() {
        log.info("[CRON] recordatorio · {}", LocalTime.now().withNano(0));
    }
}
```

**Se explica la expresión, campo por campo, de izquierda a derecha:**

```
 *  /10   *      *      *      *       *
 └─ segundo  minuto  hora  día-mes  mes  día-semana
```

> **El cron de Spring tiene SEIS campos, no cinco.** El de Unix empieza en los minutos; Spring
> antepone los segundos. Copiar un cron de cinco campos de internet y pegarlo aquí no da error:
> **se corre todo un campo** y la tarea se ejecuta a una hora que no es. Es el error número uno
> con `@Scheduled`, y no avisa.

Algunos ejemplos que conviene leer en voz alta:

| expresión | cuándo |
|---|---|
| `*/10 * * * * *` | cada 10 segundos |
| `0 0 3 * * *` | todos los días a las 03:00:00 |
| `0 30 8 * * MON-FRI` | de lunes a viernes a las 08:30 |
| `0 0 0 1 * *` | el día 1 de cada mes, a medianoche |

**Y la zona horaria, que no es decorativa:**

> `zone = "America/Santiago"`. Sin esto se usa la zona **del servidor**, y el servidor de
> producción casi siempre está en UTC. Un cierre programado «a las 3 de la mañana» se ejecutaría a
> las 23:00 o a la medianoche según la época del año — porque Chile cambia la hora y UTC no.

**En consola:**

```
[CRON] recordatorio · 13:00:10
[CRON] recordatorio · 13:00:20
[CRON] recordatorio · 13:00:30
```

Segundos exactos 10, 20, 30: el cron se ancla al reloj, no al arranque. Es la otra diferencia con
`fixedDelay`.

---

## Paso 3 · Que el usuario no espere

**Se explica:** crear un trámite manda tres avisos por correo. Cada uno tarda un segundo. Hoy el
usuario espera los tres, y la pregunta es por qué: **el aviso no es parte de crear el trámite.**
El trámite ya está creado; los avisos son consecuencia.

**Se corre primero, para tener el número de antes:**

```bash
curl -X POST -w "  (%{time_total}s)\n" http://localhost:8103/tramites/sincrono
```

```
{"tramite":"creado","modo":"SINCRONO"}  (3.020019s)
```

**Tres segundos** por algo que el usuario no necesita ver.

**Se escribe:** en `Lab12Application.java`, la segunda anotación:

```java
@EnableScheduling
@EnableAsync
@SpringBootApplication
```

en `NotificadorService`, el método asíncrono:

```java
    @Async
    public void notificarAsincrono(String destinatario) {
        trabajar(destinatario, "ASINCRONO");
    }
```

y en `TramiteController`, el endpoint que lo usa:

```java
    @PostMapping("/asincrono")
    public Map<String, Object> asincrono() {
        List.of("ana@sii.cl", "luis@sii.cl", "sofia@sii.cl").forEach(notificador::notificarAsincrono);
        return Map.of("tramite", "creado", "modo", "ASINCRONO");
    }
```

**Se corre:**

```bash
curl -X POST -w "  (%{time_total}s)\n" http://localhost:8103/tramites/asincrono
```

**En consola:**

```
{"tramite":"creado","modo":"ASINCRONO"}  (0.004253s)
```

y un segundo después, cuando el usuario ya se fue:

```
[ASINCRONO] aviso enviado a ana@sii.cl   · hilo VirtualThread[#73,task-1]
[ASINCRONO] aviso enviado a luis@sii.cl  · hilo VirtualThread[#74,task-2]
[ASINCRONO] aviso enviado a sofia@sii.cl · hilo VirtualThread[#75,task-3]
```

**De 3,03 s a 0,004 s.** Y dos cosas que notar en esas tres líneas:

1. **Tres hilos distintos** (`task-1`, `task-2`, `task-3`): los avisos no sólo dejaron de bloquear,
   además se hicieron en paralelo. Tres segundos de trabajo en uno.
2. **Los tres tienen la misma marca de tiempo.** Empezaron a la vez.

### Las tres trampas de `@Async`, que hay que decir ahora

1. **La llamada tiene que venir de FUERA del objeto.** `@Async` funciona con un proxy: si un
   método de esta misma clase llamara a `notificarAsincrono()`, la llamada no pasaría por el proxy
   y se ejecutaría **síncrona, sin avisar**. Es el mismo mecanismo —y la misma trampa— de
   `@Transactional`.
2. **Devuelve `void` o `CompletableFuture`, nunca un valor normal.** Un `String` devuelto por un
   método `@Async` llega siempre vacío o nulo: quien llama ya siguió su camino.
3. **Una excepción dentro de un `@Async void` no llega a nadie.** No hay a quién lanzarla: el que
   llamó ya se fue. Se pierde salvo que se configure un manejador. Es la razón por la que el
   trabajo asíncrono **necesita** su propio registro de errores.

---

## Paso 4 · Hilos virtuales

**Se explica:** un hilo de Java, de los de toda la vida, es un hilo del sistema operativo: pesa
alrededor de **1 MB de pila** y cambiar de uno a otro lo hace el núcleo. Por eso un servidor tiene
un *pool* de 200 y no de 200.000, y por eso una petición que se queda esperando a un servicio
lento es tan cara: **ocupa un recurso escaso sin hacer nada.** (Es justo lo que se vio en el Lab
09.)

Un **hilo virtual** lo gestiona la JVM, no el sistema operativo. Pesa unos pocos cientos de bytes,
y cuando se bloquea esperando entrada/salida, la JVM lo **aparta** y usa el hilo real para otra
cosa.

**Se escribe:** una línea en `application.yml`.

```yaml
spring:
  threads:
    virtual:
      enabled: true
```

**Se corre:** `curl http://localhost:8103/tramites/quien`

**En consola** — compárese con el paso 0:

```
antes:   "hiloQueAtiende": "Thread[#58,http-nio-8103-exec-1,5,main]"    "esVirtual": false
después: "hiloQueAtiende": "VirtualThread[#68,tomcat-handler-0]/..."     "esVirtual": true
```

**Una línea de configuración, y Tomcat entero pasó a hilos virtuales.** No se tocó una sola línea
de código de la aplicación.

### Cuándo importa, y cuándo no — que es la mitad del paso

> Los hilos virtuales sirven para trabajo que **espera**: llamadas HTTP, consultas a la base,
> lectura de archivos. Para trabajo que **calcula**, no hacen nada: el procesador sigue teniendo
> los núcleos que tiene.

Con hilos de plataforma, 200 peticiones esperando a Tesorería agotan el *pool* y la aplicación
deja de atender. Con hilos virtuales caben decenas de miles esperando a la vez.

Y la advertencia que evita un mal recuerdo: **el código no cambia.** Se sigue escribiendo con
`Thread.sleep`, llamadas que bloquean y `for` normales. Esa es toda la gracia: la programación
reactiva resolvía este mismo problema obligando a reescribir la aplicación entera en un estilo
distinto; los hilos virtuales lo resuelven con una línea de YAML.

---

## Paso 5 · El problema que nadie ve venir

**Se explica:** todo lo de hoy funciona. Y ahora la aplicación tiene éxito, y hay que poner un
segundo servidor detrás de un balanceador. Antes de correr nada, la pregunta y una mano alzada:

> Hay **dos instancias** de esta aplicación corriendo. ¿Cuántas veces se ejecuta el cierre
> nocturno?

**Se corre:** se deja `practica/` levantada y **se levanta `solucion/` en otra terminal**.

```bash
# terminal 1 — ya está corriendo
cd practica && ./mvnw spring-boot:run

# terminal 2
cd solucion && ./mvnw spring-boot:run
```

Y se miran las dos consolas a la vez.

**En consola:**

```
terminal 1:  [CIERRE] instancia-8103 · vuelta 6 · 12:58:55
terminal 2:  [CIERRE] instancia-8104 · vuelta 1 · 12:58:55     ← el mismo segundo

terminal 1:  [CIERRE] instancia-8103 · vuelta 7 · 12:59:01
terminal 2:  [CIERRE] instancia-8104 · vuelta 2 · 12:59:01     ← otra vez

terminal 1:  [CIERRE] instancia-8103 · vuelta 8 · 12:59:07
terminal 2:  [CIERRE] instancia-8104 · vuelta 3 · 12:59:07     ← y otra
```

**Dos servidores, dos cierres, en el mismo segundo.**

**Lo que hay que notar, y hay que decirlo despacio:**

`@Scheduled` es una anotación **local**. Cada instancia tiene su propio reloj y no sabe que la
otra existe. Nadie hizo nada mal: el código es correcto, y el fallo aparece **el día que se
duplica el servidor**, que es un día de éxito y en el que nadie está mirando los cierres.

Las consecuencias, con nombre y apellido:

- El total del cierre sale **duplicado**.
- El contribuyente recibe **dos veces** el mismo aviso.
- Y si la tarea genera folios, se generan dos series — que es el problema del Lab 07, otra vez, y
  ahora entre procesos distintos.

### La solución, nombrada y no implementada

> Hace falta un **candado distribuido**: un sitio compartido por las dos instancias donde la
> primera que llega deja una marca —«el cierre del 18 de agosto es mío»— y la segunda, al no poder
> dejarla, se salta la ejecución.

Ese sitio compartido puede ser una fila en una tabla (con `INSERT` sobre una clave única, que es
atómico), una clave en Redis, o una librería que lo haga por uno (ShedLock). Y el candado necesita
**expiración**: si la instancia que lo tomó se cae a mitad, alguien tiene que poder retomarlo.

**No se implementa hoy, y es a propósito:** requiere un almacén compartido, y montarlo convertiría
este laboratorio en otro. Lo que hay que llevarse es **reconocer el problema**, que es la parte
difícil:

> Toda tarea programada en una aplicación que puede tener más de una instancia necesita un candado
> distribuido. **Y casi ninguna lo tiene**, porque en desarrollo siempre hay una sola instancia y
> el fallo no se puede ver.

---

## Al terminar

`practica/` hace lo mismo que `solucion/`: dos tareas corriendo solas, el endpoint asíncrono en
milésimas, y los hilos virtuales encendidos.

| | |
|---|---|
| síncrono | 3,03 s |
| asíncrono | 0,004 s |
| cierre con dos instancias | **2 ejecuciones** |

Lo que hay que poder decir con las propias palabras:

> `fixedDelay` cuenta desde que termina la anterior, y por eso no se solapan. El cron de Spring
> tiene seis campos y la zona se escribe siempre. `@Async` devuelve el control al instante, y sus
> excepciones no llegan a nadie. Los hilos virtuales sirven para esperar, no para calcular. Y una
> tarea programada en dos instancias se ejecuta dos veces.

### Lo que siembra este lab

De los cinco pasos, cuatro salieron bien y **uno quedó abierto a propósito**: el cierre duplicado
sigue duplicándose al terminar la sesión.

No es un descuido del guion. Es la forma honesta de dejar planteado lo que viene: hay una clase de
problemas que **no se pueden resolver dentro de un proceso**. El candado del Lab 07 funcionaba
porque los dos hilos vivían en la misma JVM y compartían memoria. Aquí no hay memoria compartida:
hay dos procesos que ni siquiera saben el uno del otro.

> **Lo que se lleva planteado:** en cuanto una aplicación tiene más de una instancia, todo lo que
> dependa de «yo soy el único» deja de ser cierto — el reloj, el contador en memoria, el caché,
> el candado. Y ninguna de esas cosas avisa cuando deja de serlo.

Y hay un corolario práctico para el laboratorio siguiente, el del empaquetado: si la aplicación se
va a desplegar en varias copias, **la copia tiene que ser exactamente la misma** y la diferencia
entre una y otra tiene que estar fuera del artefacto. Eso es lo que se construye mañana.
