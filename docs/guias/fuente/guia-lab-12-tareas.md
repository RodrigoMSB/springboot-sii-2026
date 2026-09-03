---
title: "Lab 12 · El trabajo que ocurre solo"
subtitle: "Curso de Spring Boot · Servicio de Impuestos Internos · 2026"
date: "60 minutos · Spring Boot 4.1.0 · Java 25 (Temurin)"
abstract-title: "Lo que se demuestra"
abstract: |
  Que hay trabajo que debe ocurrir sin que nadie lo pida, y trabajo que no debe hacer esperar a
  quien está en la ventanilla: **3,02 s el envío síncrono, 0,004 s el asíncrono**. Y el problema
  que nadie ve venir cuando la aplicación se despliega dos veces.
lang: es
---

# Antes de empezar

## Qué vas a lograr

Todo lo que tu aplicación hace, lo hace **porque alguien se lo pidió**. Hoy empieza a trabajar
sola.

Vas a programar una tarea que corre cada pocos segundos, otra que corre a una hora exacta, vas a
sacar del camino del usuario un trabajo lento —y a medir la diferencia—, y vas a cambiar el modelo
de hilos del servidor con **una línea de configuración**. Y al final vas a ver el problema que
tienen todas las tareas programadas y que sólo aparece **cuando la aplicación se despliega dos
veces**.

## Qué necesitas tener listo

| Requisito | Cómo lo compruebas | Qué tiene que salir |
|---|---|---|
| Los labs 01 y 02 hechos | Sabes crear endpoints y qué es un bean | — |
| Estar en la carpeta del lab | `cd labs/lab-12-tareas/practica` | El `cd` no da error |
| **Ninguna base de datos** | Este lab no la usa | — |

## Cómo copiar el código de esta guía

**Al copiar de un PDF se pierden los espacios del principio de línea, y a veces una línea larga se
parte en dos.** Con Java no importa. El código completo está en `labs/lab-12-tareas/solucion/`.

## La puesta a punto

``` bash
cd labs/lab-12-tareas/practica
./mvnw spring-boot:run
```

Escucha en el **8103**. **Déjala corriendo un rato**: en este lab lo interesante pasa **sin que
pidas nada**, así que hay que mirar la consola en silencio.

# El caso

La oficina de la DGT cierra a las ocho. Pero hay trabajo que se hace **cuando no hay público**: el
cierre del día, los recordatorios, las notificaciones que nadie está esperando en el mostrador.

## El conserje de noche, que es la metáfora de este laboratorio

::: metafora
**Hay trabajo que se hace con la oficina cerrada, y trabajo que no debe hacerse en el mostrador.**

**El cierre nocturno.** Todos los días, sin que nadie lo pida, alguien cuadra las cuentas. Y hay
una decisión sobre cómo se programa que parece un detalle y no lo es:

- *«empieza cada 5 minutos»* — si un día el cierre tarda 6, **empieza el siguiente con el anterior
  a medias**. Dos personas cuadrando las mismas cuentas a la vez.
- *«empieza 5 minutos después de terminar el anterior»* — nunca se solapan. Es lo que se usa aquí.

**Y el correo del mostrador.** Cuando emites un trámite hay que avisar por correo. El correo tarda
un segundo. **Multiplicado por tres destinatarios, el ciudadano espera tres segundos delante de la
ventanilla** mirando cómo mandas correos. No hace falta: se apunta el encargo y se le atiende.

Eso último tiene un precio que hay que decir: **si el correo falla, el ciudadano ya se fue**.
:::

# Los pasos

## Paso 1 · Una tarea que corre sola

### Qué vamos a hacer

Encender la programación de tareas y escribir una que corre cada pocos segundos.

### Para entenderlo mejor

El cierre nocturno. Nadie lo pide: ocurre.

### El problema

Todo lo que has escrito hasta ahora se dispara con una petición HTTP. Un cierre diario no tiene
petición: tiene **un reloj**.

### La alternativa, y por qué no

- **Un `cron` del sistema operativo** que llame a un endpoint: funciona, y reparte la lógica entre
  el sistema y la aplicación — el día que alguien mire sólo el código, no verá que eso existe.
- **Un `ScheduledExecutorService` a mano**: control total, y hay que gestionar su ciclo de vida.
- **`@Scheduled`**, que es lo de aquí: la tarea vive junto al código que la hace, y el
  planificador lo pone Spring.
- **Quartz**, cuando haga falta persistir el plan o coordinar varias instancias. Es la respuesta al
  problema del paso 5, y es otra pieza que operar.

**Y la decisión que da nombre al paso:** `fixedDelay` y no `fixedRate`.

- `fixedRate = 5000` cuenta 5 s **desde que empieza** una hasta que empieza la siguiente. Si la
  tarea tarda más que el intervalo, **se solapan**.
- `fixedDelay = 5000` cuenta 5 s **desde que TERMINA** una. Nunca se solapan.

El día que el cierre tarde más de lo normal —porque hay más trámites que de costumbre, justo el día
que más importa— `fixedRate` arranca otro encima. **`fixedDelay` es el valor por defecto correcto.**

### Se pega

En `practica/src/main/java/cl/dgt/tareas/Lab12Application.java`, **arriba**:

``` java
import org.springframework.scheduling.annotation.EnableScheduling;
```

y la anotación **sobre la clase**, junto a `@SpringBootApplication`:

``` java
@EnableScheduling
```

Archivo **nuevo** `practica/src/main/java/cl/dgt/tareas/programadas/CierreNocturno.java` — entero:

{{codigo lab=lab-12-tareas archivo=src/main/java/cl/dgt/tareas/programadas/CierreNocturno.java modo=entero lenguaje=java}}

### Lo que vas a ver

**Sin llamar a nada**, sólo esperando:

``` text
[CIERRE] vuelta 1 · 01:16:15 · hilo Thread[#63,scheduling-1,5,main]
[CIERRE] vuelta 2 · 01:16:21 · hilo Thread[#63,scheduling-1,5,main]
[CIERRE] vuelta 3 · 01:16:27 · hilo Thread[#63,scheduling-1,5,main]
[CIERRE] vuelta 4 · 01:16:33 · hilo Thread[#63,scheduling-1,5,main]
[CIERRE] vuelta 5 · 01:16:39 · hilo Thread[#63,scheduling-1,5,main]
```

**Mira los segundos: 15, 21, 27, 33, 39.** Entre vuelta y vuelta pasan **6 segundos**, no 5.

Y ahí está el paso entero: la tarea duerme 1 segundo, y `fixedDelay` cuenta desde que **termina**.
5 + 1 = 6. Con `fixedRate` habrían salido cada 5, y el día que la tarea tardara 6, dos a la vez.

::: vasbien
Salen líneas `[CIERRE]` sin que hayas pedido nada, y entre una y otra pasan **6 segundos**.
:::

::: atasco
**1 · No sale ninguna línea `[CIERRE]`.**

Falta `@EnableScheduling` sobre la clase de arranque. **Sin ella, `@Scheduled` no hace nada y no
avisa** — es el fallo más silencioso de este lab.

**2 · Sale una sola vez y no se repite.**

Escribiste `initialDelay` donde iba `fixedDelay`, o la tarea lanzó una excepción: si una ejecución
falla, el planificador puede dejar de repetirla.

**3 · `Port 8103 was already in use`**

La tienes arrancada en otra terminal:

``` bash
lsof -ti:8103 | xargs kill -9
```
:::

## Paso 2 · Que el usuario no espere

### Qué vamos a hacer

Sacar del camino del usuario un trabajo lento, y **medir** la diferencia.

### Para entenderlo mejor

Apuntar el encargo y atender al siguiente. El correo se manda igual; lo que no se hace es **mandarlo
con el ciudadano delante**.

### El problema

Emitir un trámite manda tres correos, y cada uno tarda un segundo. El ciudadano espera tres
segundos mirando cómo mandas correos que no le aportan nada.

### La alternativa, y por qué no

- **Hilos a mano** o `CompletableFuture.runAsync`: funciona, y hay que gestionar el pool.
- **`@Async`**, que es lo de aquí: una anotación, y el método vuelve al instante.
- **Una cola de verdad** (RabbitMQ, Kafka): es lo correcto **en cuanto el trabajo no se pueda
  perder**. `@Async` vive en memoria: si el proceso se cae, lo que estuviera en vuelo desaparece.
  Un correo perdido se aguanta; un pago, no.

**Y la trampa que hay que decir en voz alta:** `@Async` funciona **por un proxy**. Llamar a un
método `@Async` **desde otro método de la misma clase** no pasa por el proxy y **se ejecuta de
forma síncrona, sin avisar**. Es el fallo número uno de `@Async` — y es la misma regla de
`@Transactional`, por el mismo motivo. Por eso el trabajo vive en **otro bean**.

### Se pega

**Arriba**, en la clase de arranque:

``` java
import org.springframework.scheduling.annotation.EnableAsync;
```

``` java
@EnableScheduling
@EnableAsync
@SpringBootApplication
```

En `practica/src/main/java/cl/dgt/tareas/services/NotificadorService.java`, el import **arriba**:

``` java
import org.springframework.scheduling.annotation.Async;
```

y el método **dentro de la clase**, debajo de `notificarSincrono`:

{{codigo lab=lab-12-tareas archivo=src/main/java/cl/dgt/tareas/services/NotificadorService.java modo=metodo nombre=notificarAsincrono lenguaje=java}}

Y en `practica/src/main/java/cl/dgt/tareas/controllers/TramiteController.java`, donde dice
`// escribe aquí`:

{{codigo lab=lab-12-tareas archivo=src/main/java/cl/dgt/tareas/controllers/TramiteController.java modo=metodo nombre=asincrono lenguaje=java}}

### Se corre

``` bash
curl -s -o /dev/null -w '%{time_total}s\n' -X POST localhost:8103/tramites/sincrono
curl -s -o /dev/null -w '%{time_total}s\n' -X POST localhost:8103/tramites/asincrono
```

### Lo que vas a ver

``` text
3.018813s     <- síncrono
0.004011s     <- asíncrono
```

**Tres segundos contra tres milésimas.** Mil veces.

Y **el trabajo se hace igual**: las líneas del notificador siguen saliendo en la consola después de
que el `curl` haya vuelto.

:::  nota
**Los dos endpoints se dejan puestos a propósito.** El síncrono no sobra: es la mitad de la
comparación, y sin él el número queda en una diapositiva y deja de poder comprobarse.
:::

::: vasbien
El asíncrono responde en milésimas, **y** en la consola siguen apareciendo después las líneas del
notificador.
:::

::: atasco
**1 · El asíncrono también tarda 3 segundos.**

Tres causas, por frecuencia: falta `@EnableAsync`; el método `@Async` se llama **desde la misma
clase** —el problema del proxy—; o `@Async` está sobre un método privado, donde el proxy no llega.

**2 · La respuesta vuelve rápido pero no se manda nada.**

Mira si hubo una excepción en el hilo de fondo. Con `@Async` y `void`, **la excepción no vuelve a
quien llamó**: si nadie la registra, desaparece.
:::

## Paso 3 · Hilos virtuales

### Qué vamos a hacer

Cambiar el modelo de hilos del servidor con **una línea de configuración**, y comprobarlo.

### Para entenderlo mejor

Los funcionarios de la oficina dejan de ser plantilla fija y pasan a ser gente que aparece cuando
hace falta y se va cuando no. La oficina aguanta muchos más ciudadanos a la vez sin contratar a
nadie.

### El problema

Un hilo de plataforma pesa: un megabyte de pila y un recurso del sistema operativo. Con miles de
peticiones concurrentes que se pasan el rato **esperando** —a una base, a otro servicio— tienes
miles de hilos caros sin hacer nada.

### La alternativa, y por qué no

- **Programación reactiva** (WebFlux): resuelve lo mismo y **cambia cómo se escribe todo el
  código** — nada de bloquear, todo en cadenas de operadores. Es un cambio de modelo mental.
- **Hilos virtuales**, que es lo de aquí: el código sigue siendo el mismo código bloqueante de
  siempre, y **una línea** cambia quién lo ejecuta.

### Se edita — aquí no se pega nada

En `practica/src/main/resources/application.yml` ya está esto:

``` yaml
spring:
  threads:
    virtual:
      enabled: false
```

**Cambia `false` por `true`.** Es todo.

### Lo que vas a ver

``` bash
curl -s localhost:8103/tramites/quien
```

``` text
{"vueltasDelCierre":4,
 "hiloQueAtiende":"VirtualThread[#68,tomcat-handler-0]/runnable@ForkJoinPool-1-worker-2",
 "esVirtual":true}
```

**Antes decía `Thread[#58,http-nio-8103-exec-1,5,main]` y `esVirtual: false`.** Una línea de
configuración, y Tomcat entero pasó a hilos virtuales. **No se tocó una línea de código.**

::: vasbien
`esVirtual` es `true` y el nombre del hilo empieza por `VirtualThread`.
:::

::: atasco
**1 · Sigue diciendo `esVirtual: false`.**

No reiniciaste, o la línea quedó mal indentada y la propiedad no se leyó. `virtual` va bajo
`threads`, y `threads` bajo `spring`.
:::

# Lo que aprendiste

**1 · `fixedDelay` y `fixedRate` no son lo mismo, y el defecto correcto es `fixedDelay`.**

Lo viste en los segundos: 15, 21, 27 — seis, no cinco. Con `fixedRate`, el día que la tarea tarde
más que el intervalo, se solapan.

**2 · `@Async` saca el trabajo del camino del usuario — y funciona por un proxy.**

3,02 s contra 0,004 s. Y llamarlo desde la misma clase lo convierte en síncrono **sin avisar**.

**3 · Los hilos virtuales son una línea de YAML, y sirven para esperar.**

`esVirtual` pasó de `false` a `true` sin tocar una línea de código. Para trabajo que calcula no
cambian nada: los núcleos siguen siendo los que son.

# Lo que no vimos hoy

**El `cron`.** `fixedDelay` sirve para «cada tanto»; para «todos los días a las 3 de la mañana»
hace falta `@Scheduled(cron = "0 0 3 * * *", zone = "America/Santiago")`. Dos advertencias sobre
esa línea:

- **El cron de Spring tiene SEIS campos, no cinco** — segundo, minuto, hora, día del mes, mes, día
  de la semana. Casi todo lo que encuentres en internet es el cron de Unix, de cinco, y al pegarlo
  aquí **no da error**: se corre un campo y la tarea se ejecuta a una hora que no es.
- **La zona se escribe siempre.** Sin ella la decide el servidor, que en producción suele estar en
  UTC. Un cierre «a las 3» se ejecutaría a las 23:00 o a la medianoche según la época del año,
  porque Chile cambia la hora y UTC no.

**El problema de las dos instancias.** La DGT abre una segunda sede para atender más público. Las
dos tienen el mismo manual, y el manual dice *«a las 2 de la mañana se hace el cierre»*.

> **A las 2 de la mañana se hace el cierre dos veces.**

`@Scheduled` dispara **en cada instancia**: cada JVM tiene su planificador y su reloj, y ninguna
sabe que la otra existe. Es exactamente lo que quieres para escalar el tráfico HTTP y exactamente
lo que no quieres para un cierre. Y no aparece en desarrollo, donde siempre corre una sola
instancia: aparece el día del segundo despliegue.

Las salidas, y ninguna es gratis:

- **Que sólo una instancia tenga las tareas activas** (un perfil): simple, y esa instancia pasa a
  ser un punto único de fallo — si se cae, no hay cierre.
- **Un candado en la base**, como el del Lab 07: la instancia que lo consigue ejecuta; las demás
  pasan de largo. Barato, y ya tienes la base. **Ojo**: aquel candado del Lab 07 funciona entre
  procesos precisamente porque vive en la base. Lo que no funciona entre procesos es
  `synchronized`, que es memoria de una sola JVM.
- **Un planificador con estado compartido** (Quartz en clúster, ShedLock): resuelve esto
  exactamente, y es otra pieza que operar.

Lo importante no es cuál elijas: es **saber que hace falta elegir**.

Y dos cosas más que quedan fuera:

- **Colas de mensajes** (RabbitMQ, Kafka), para cuando el trabajo asíncrono tenga que sobrevivir a
  que el proceso se caiga. `@Async` vive en memoria: si la aplicación muere, el aviso se pierde.
- **Planificadores fuera de la aplicación**: el cron del orquestador llamando a un endpoint.

# Para profundizar

- **Cambia `fixedDelay` por `fixedRate`** y sube el `Thread.sleep` a 7 segundos. Mira los
  timestamps: verás dos ejecuciones solapadas.
- **Llama a `notificarAsincrono` desde otro método de la misma clase** y mide. Vas a ver los 3
  segundos otra vez: es el proxy.
- **Haz que el método `@Async` lance una excepción** y mira si te enteras. Después cámbialo para
  que devuelva `CompletableFuture` y prueba otra vez.
- **Arranca práctica y solución a la vez** y mira los dos `[CIERRE]` en el mismo segundo. Es el
  problema de las dos instancias, en tu máquina.

# Antes de cerrar

**Párala con `Ctrl+C`.**

``` bash
./mvnw clean
```

**Lo que te llevas:**

> `@Scheduled` para el trabajo que ocurre solo, con `fixedDelay` por defecto. `@Async` para el
> trabajo que no debe hacer esperar — y ojo con el proxy. Los hilos virtuales, una línea de YAML.
> Y todo esto se dispara en cada instancia.

**Lo que queda pendiente, y abre el Lab 13:** tu aplicación funciona **en tu máquina**. Para que
funcione en otra hay que llevarla entera: el código, sus dependencias, su Java y su configuración.
En el Lab 13 se empaqueta en una imagen — sin Docker.
