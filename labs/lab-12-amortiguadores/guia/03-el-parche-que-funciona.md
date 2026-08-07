# 03 · El parche que funciona (y por qué no basta)

## El arreglo obvio

Si el aviso se pierde porque el destino no contestó, reinténtalo. Spring Framework 7 lo trae de
fábrica, sin librerías:

```java
@Retryable(maxAttempts = 3, delay = 500)
public void notificar(String rut, String mensaje) { ... }
```

Pruébalo. **Funciona**, y no es un juguete: el fallo transitorio —una desconexión, un pico de
latencia, un bloqueo momentáneo— se salva casi siempre. Mídelo tú: con el destino cayéndose de vez
en cuando, la mayoría de los avisos llegan.

Es un progreso real, y es donde se detiene mucha gente.

## Las tres preguntas que lo desarman

**1. ¿Y si la caída dura dos horas?**

Tres intentos con medio segundo de espera cubren **segundo y medio**. La caída de Carolina duró
**7.200 segundos**.

¿Reintentas 7.200 veces? ¿Con qué espera entre medias? Y mientras tanto, ¿dónde vive esa lista de
pendientes? En la memoria del proceso. Que es exactamente el problema que veníamos a resolver.

**2. ¿Y si despliegas a mitad de camino?**

Los reintentos viven en memoria. Un despliegue, un pod reciclado, un `OutOfMemory` — y se lleva la
lista entera. No solo pierdes los avisos: pierdes **saber cuáles** estabas reintentando.

Reintentar no es persistir. Son dos cosas distintas y solo una sobrevive a un reinicio.

**3. Y la peor: ¿a quién le estás pegando?**

Cada reintento **golpea a un servicio que ya está en el suelo**. Multiplica: doscientos avisos × tres
intentos = seiscientas peticiones contra un sistema que intenta levantarse.

Ese es el patrón que convierte una caída de dos minutos en una de veinte. Tu reintento bienintencionado
es parte del problema.

## Lo que el parche revela

`@Retryable` responde a «el otro falló **ahora mismo**». No responde a «el otro no está, y no sé
cuándo volverá».

> El aviso necesita vivir **fuera de tu proceso**, en un sitio que sobreviva a que tú te reinicies y
> a que el otro no esté. Eso no es un reintento: es una **cola**.

## La forma correcta

El aviso se **entrega a una cola** y ahí se acaba tu responsabilidad:

```java
@TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
public void alEmitirseUnFolio(FolioEmitido evento) {
    publicador.publicar(AvisoDeFolio.de(evento));
}
```

El `AFTER_COMMIT` **no se toca**, y ahora importa más que en el Lab 11: publicar antes del commit
dejaría un mensaje **durable**, sobreviviente a reinicios, hablando de un folio que la base
revirtió. El error dejaría de evaporarse y pasaría a quedar escrito en disco esperando a que alguien
lo procese.

Y con eso vienen tres cosas que hay que decidir, no heredar:

- **TODO_2 · El duplicado.** Vas a recibir el mismo mensaje dos veces. No es una posibilidad remota:
  «exactly once» no existe. Hazlo idempotente — y ya sabes cómo, es RN-05 del Lab 06 con otro
  transporte.
- **TODO_3 · El envenenado.** Un mensaje que fallará siempre no se cura reintentando. Necesita una
  salida, y sobre todo **no puede atascar a los buenos que van detrás**.
- **TODO_4 · Lo que sigue siendo síncrono.** A TESO se le sigue llamando en el momento. Ahí el
  arreglo no es una cola: es dejar de golpear al que está caído.

## Dos avisos antes de que empieces

**El `default` que muerde.** `default-requeue-rejected` viene en `true`. Con ese valor, el mensaje
rechazado vuelve al principio de la cola y empieza otra vez — para siempre — y la cola se atasca
detrás de él. Puedes tener la DLQ perfectamente declarada y que no se dispare nunca.

**La clave de idempotencia.** Elige la que identifica el **hecho** («se emitió el folio 4471»), no
la entrega. Un UUID por mensaje hace único cada reintento y tu deduplicación queda de adorno: pasa
los tests que tú escribas y falla el día que el broker reentregue de verdad.

→ Cuando termines: `./bin/90-validar.sh --dir starter`
