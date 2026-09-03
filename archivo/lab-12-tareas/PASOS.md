# Pasos · Lab 12 · Tareas y asincronía

Tres pasos y el paso 0. Se trabaja en `practica/`, en vivo.

```bash
cd practica
./mvnw spring-boot:run
```

Escucha en el **8103** (`solucion/`, en el 8104).

Hoy **no hay dependencias nuevas**: `@Scheduled`, `@Async` y los hilos virtuales vienen dentro de
lo que ya hay. Lo que se escribe:

```
Lab12Application.java        →  pasos 1 y 2 (las dos anotaciones que lo encienden)
programadas/                 →  paso 1 (llega vacía)
services/NotificadorService  →  paso 2
application.yml              →  paso 3 (una línea)
```

---

## Paso 0 · Lo que hay

**Se corre:** `./mvnw spring-boot:run`, y

```bash
curl http://localhost:8103/tramites/quien
```

**En consola:**

```json
{"hiloQueAtiende":"Thread[#58,http-nio-8103-exec-1,5,main]",
 "esVirtual":false}
```

Guárdese esa línea del hilo: en el paso 3 va a cambiar.

---

## Paso 1 · Una tarea que corre sola

**Se explica:** hasta ahora, todo lo que hizo la aplicación lo pidió alguien por HTTP. Hay trabajo
que no: el cierre nocturno, la limpieza de temporales, el reintento de los avisos que fallaron.
Nadie los pide; ocurren.

**Se pega:** en `practica/src/main/java/cl/dgt/tareas/Lab12Application.java` — el import
**arriba** y la anotación **sobre la clase**, junto a `@SpringBootApplication`.

```java
import org.springframework.scheduling.annotation.EnableScheduling;
```

```java
@EnableScheduling
@SpringBootApplication
public class Lab12Application {
```

y el archivo **nuevo** `practica/src/main/java/cl/dgt/tareas/programadas/CierreNocturno.java`
— el archivo entero:

```java
package cl.dgt.tareas.programadas;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalTime;
import java.util.concurrent.atomic.AtomicInteger;

@Component
public class CierreNocturno {

    private static final Logger log = LoggerFactory.getLogger(CierreNocturno.class);

    private final AtomicInteger vueltas = new AtomicInteger();

    @Scheduled(fixedDelay = 5000, initialDelay = 3000)
    public void ejecutar() throws InterruptedException {
        int n = vueltas.incrementAndGet();
        log.info("[CIERRE] vuelta {} · {} · hilo {}",
                n, LocalTime.now().withNano(0), Thread.currentThread());
        Thread.sleep(1000);
    }

    public int vueltas() {
        return vueltas.get();
    }
}
```

**Se pega:** y para poder contar las vueltas desde fuera, en
`practica/src/main/java/cl/dgt/tareas/controllers/TramiteController.java`: el import **arriba**,

```java
import cl.dgt.tareas.programadas.CierreNocturno;
```

el campo **debajo** de `private final NotificadorService notificador;` y el constructor
**reemplazando el que hay** — le entra un parámetro más:

```java
    private final CierreNocturno cierre;
```

```java
    public TramiteController(NotificadorService notificador, CierreNocturno cierre) {
        this.notificador = notificador;
        this.cierre = cierre;
    }
```

y una línea más en lo que devuelve `/quien`:

```java
                "vueltasDelCierre", cierre.vueltas(),
```

**En consola** — sin llamar a nada, solo esperando:

```
[CIERRE] vuelta 1 · 13:00:07 · hilo Thread[#63,scheduling-1,5,main]
[CIERRE] vuelta 2 · 13:00:13 · hilo Thread[#63,scheduling-1,5,main]
[CIERRE] vuelta 3 · 13:00:19 · hilo Thread[#63,scheduling-1,5,main]
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

Y el nombre del hilo, `scheduling-1`, dice otra cosa que conviene señalar: **la tarea no corre en
el hilo de Tomcat.** Tiene el suyo, y ahí no hay ninguna petición esperando al otro lado.

**Se corre** para confirmarlo desde fuera:

```bash
curl http://localhost:8103/tramites/quien
# ...esperar seis segundos...
curl http://localhost:8103/tramites/quien
```

`vueltasDelCierre` subió sin que nadie llamara a nada. Eso es todo el paso.

---

## Paso 2 · Que el usuario no espere

**Se explica:** crear un trámite manda tres avisos por correo. Cada uno tarda un segundo. Hoy el
usuario espera los tres, y la pregunta es por qué: **el aviso no es parte de crear el trámite.**
El trámite ya está creado; los avisos son consecuencia.

**Se corre primero, para tener el número de antes:**

```bash
curl -X POST -w "  (%{time_total}s)\n" http://localhost:8103/tramites/sincrono
```

```
{"tramite":"creado","modo":"SINCRONO"}  (3.018813s)
```

**Tres segundos** por algo que el usuario no necesita ver.

**Se pega:** en `practica/src/main/java/cl/dgt/tareas/Lab12Application.java` — el import
**arriba** y la anotación **sobre la clase**.

```java
import org.springframework.scheduling.annotation.EnableAsync;
```

```java
@EnableScheduling
@EnableAsync
@SpringBootApplication
```

en `practica/src/main/java/cl/dgt/tareas/services/NotificadorService.java`, el import **arriba**:

```java
import org.springframework.scheduling.annotation.Async;
```

y el método asíncrono, **dentro de la clase**, debajo de `notificarSincrono`:

```java
    @Async
    public void notificarAsincrono(String destinatario) {
        trabajar(destinatario, "ASINCRONO");
    }
```

y en `practica/src/main/java/cl/dgt/tareas/controllers/TramiteController.java`, el endpoint que
lo usa, donde dice `// escribe aquí`:

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
{"tramite":"creado","modo":"ASINCRONO"}  (0.004011s)
```

y un segundo después, cuando el usuario ya se fue:

```
17:00:17.272  [ASINCRONO] aviso enviado a ana@sii.cl   · hilo VirtualThread[#73,task-1]
17:00:17.272  [ASINCRONO] aviso enviado a sofia@sii.cl · hilo VirtualThread[#76,task-3]
17:00:17.273  [ASINCRONO] aviso enviado a luis@sii.cl  · hilo VirtualThread[#74,task-2]
```

**De 3,02 s a 0,004 s.** Y dos cosas que notar en esas tres líneas:

1. **Tres hilos distintos** (`task-1`, `task-2`, `task-3`): los avisos no sólo dejaron de bloquear,
   además se hicieron en paralelo. Tres segundos de trabajo en uno.
2. **Los tres tienen prácticamente la misma marca de tiempo.** Empezaron a la vez.

Compárese con los tres síncronos, todos en el **mismo** hilo y separados por un segundo exacto:

```
17:00:14.236  [SINCRONO] aviso enviado a ana@sii.cl   · hilo VirtualThread[#70,tomcat-handler-2]
17:00:15.241  [SINCRONO] aviso enviado a luis@sii.cl  · hilo VirtualThread[#70,tomcat-handler-2]
17:00:16.248  [SINCRONO] aviso enviado a sofia@sii.cl · hilo VirtualThread[#70,tomcat-handler-2]
```

Y ese hilo es **el que atiende al usuario**: mientras manda correos, no atiende a nadie más.

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

Y hay una cuarta cosa, que no es trampa sino precio: **el asíncrono devuelve antes porque no sabe
si el trabajo salió bien.** Los tres segundos del síncrono compraban una certeza. Es aceptable
para un aviso; no lo sería para un cobro.

---

## Paso 3 · Hilos virtuales

**Se explica:** un hilo de Java, de los de toda la vida, es un hilo del sistema operativo: pesa
alrededor de **1 MB de pila** y cambiar de uno a otro lo hace el núcleo. Por eso un servidor tiene
un *pool* de 200 y no de 200.000, y por eso una petición que se queda esperando a un servicio
lento es tan cara: **ocupa un recurso escaso sin hacer nada.** (Es justo lo que se vio en el
Lab 10, con Tesorería tardando treinta segundos.)

Un **hilo virtual** lo gestiona la JVM, no el sistema operativo. Pesa unos pocos cientos de bytes,
y cuando se bloquea esperando entrada/salida, la JVM lo **aparta** y usa el hilo real para otra
cosa.

**Se pega:** en `practica/src/main/resources/application.yml` — una línea.

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
de código de la aplicación. Hasta el nombre del hilo cambia: `http-nio-...-exec-N` era el pool
clásico de Tomcat; `tomcat-handler-N` es el modo de hilos virtuales.

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

## Lo que no vimos hoy

**El cron.** `fixedDelay` sirve para «cada tanto». Para «todos los días a las 3 de la mañana» hace
falta `@Scheduled(cron = "0 0 3 * * *", zone = "America/Santiago")`, y hay dos cosas que decir de
esa línea:

- **El cron de Spring tiene SEIS campos, no cinco** — segundo, minuto, hora, día del mes, mes, día
  de la semana. El de Unix empieza en los minutos. Copiar un cron de cinco campos de internet y
  pegarlo aquí **no da error**: se corre todo un campo y la tarea se ejecuta a una hora que no es.
- **La zona horaria se escribe siempre.** Sin ella se usa la del servidor, que en producción casi
  siempre está en UTC. Un cierre «a las 3 de la mañana» se ejecutaría a las 23:00 o a la
  medianoche según la época del año, porque Chile cambia la hora y UTC no.

**El problema de las dos instancias**, que es el más importante de los dos y hay que contarlo
entero:

> La aplicación tiene éxito y se levanta un segundo servidor detrás de un balanceador.
> **¿Cuántas veces se ejecuta el cierre nocturno?**

Dos. `@Scheduled` es una anotación **local**: cada instancia tiene su propio planificador, su
propio reloj y ninguna idea de que la otra existe. Las dos consolas escriben `[CIERRE]` en el
mismo segundo. Y nadie hizo nada mal — el código es correcto, y el fallo aparece **el día que se
duplica el servidor**, que es un día de éxito y en el que nadie está mirando los cierres.

Las consecuencias, con nombre y apellido: el total del cierre sale duplicado, el contribuyente
recibe dos veces el mismo aviso, y si la tarea genera folios se generan dos series.

La solución es un **candado distribuido**: un sitio compartido donde la primera instancia que
llega deja una marca —«el cierre del 18 de agosto es mío»— y la segunda, al no poder dejarla, se
salta la ejecución. Ese sitio puede ser una fila en una tabla con `INSERT` sobre clave única, una
clave en Redis, o ShedLock, que es una librería que hace exactamente esto. Y el candado necesita
**expiración**: si la instancia que lo tomó se cae a mitad, alguien tiene que poder retomarlo.

**Y aquí hay que tener cuidado con lo que se dice**, porque es fácil sacar la conclusión
equivocada: el candado del **Lab 07 sí funciona entre procesos**. Aquel candado vivía en la
base —un bloqueo sobre la fila del folio— y la base es precisamente el sitio compartido que hace
falta. Lo que **no** funciona entre procesos es `synchronized`, que es memoria de una sola JVM. Y
`@Scheduled`, por la misma razón: cada JVM tiene su planificador.

Y hay más cosas que quedan fuera:

- **Colas de mensajes** (RabbitMQ, Kafka): cuando el trabajo asíncrono tiene que sobrevivir a que
  el proceso se caiga. `@Async` vive en memoria; si la aplicación muere, el aviso se pierde.
- **Planificadores distribuidos** (Quartz en clúster, el cron del orquestador): sacar la
  programación fuera de la aplicación.

---

## Al terminar

`practica/` hace lo mismo que `solucion/`: la tarea corriendo sola, el endpoint asíncrono en
milésimas, y los hilos virtuales encendidos.

| | |
|---|---|
| síncrono | 3,02 s |
| asíncrono | 0,004 s |
| `esVirtual` | de `false` a `true` |

Lo que hay que poder decir con las propias palabras:

> `fixedDelay` cuenta desde que termina la anterior, y por eso no se solapan. `@Async` devuelve el
> control al instante, no pasa por el proxy si se llama desde dentro, y sus excepciones no llegan
> a nadie. Los hilos virtuales sirven para esperar, no para calcular, y se encienden con una línea
> de YAML sin tocar el código.

### Lo que siembra este lab

Hay algo que quedó dicho y **no resuelto**: el cierre nocturno se ejecuta dos veces en cuanto haya
dos instancias, y al terminar la sesión sigue siendo así.

No es un descuido. Es la forma honesta de dejar planteado lo que viene: hay una clase de problemas
que **no se pueden resolver dentro de un proceso**. Dos procesos no comparten memoria, ni reloj,
ni contadores — y ninguna de esas cosas avisa cuando deja de ser cierta.

> **Lo que se lleva planteado:** en cuanto una aplicación tiene más de una instancia, todo lo que
> dependa de «yo soy el único» deja de valer. Lo que quede compartido tiene que estar **fuera** del
> proceso: en la base, en Redis, en algún sitio que las dos instancias puedan ver.

Y hay un corolario práctico para el laboratorio siguiente, el del empaquetado: si la aplicación se
va a desplegar en varias copias, **la copia tiene que ser exactamente la misma** y la diferencia
entre una y otra tiene que estar fuera del artefacto. Eso es lo que se construye mañana.
