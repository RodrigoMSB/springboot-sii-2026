# Guía del instructor · Lab 12 — amortiguadores

## Antes de la sesión

1. `./bin/91-e2e.sh` — el starter falla los 4 TODOs, la solución pasa. Si falla, no des la clase.
2. **Docker corriendo, y con margen.** Este lab levanta **dos** contenedores por suite (PostgreSQL
   y RabbitMQ). La primera pasada baja imágenes: hazla el día antes, no delante de la sala.
3. Ensaya las dos pasadas del crimen — cada una arranca la app **dos veces**, así que ten paciencia:
   `./bin/start-lab.sh --avisos-caidos` y `--dir solucion --avisos-caidos`.
4. Ten abierta la consola del broker: <http://localhost:15672> (`dgt` / `dgt-dev`). Vas a
   necesitarla.

## ORDEN PARA CLASE (180 min)

### 🔪 00:00 – 00:10 · La escena del crimen (en vivo)

```bash
./bin/start-lab.sh --avisos-caidos
```

**Fase 1.** Con el servicio de avisos caído, se emiten tres folios:

```
     POST /api/v1/tramites/6/folio  ->  HTTP 201
     POST /api/v1/tramites/7/folio  ->  HTTP 201
     POST /api/v1/tramites/8/folio  ->  HTTP 201

     avisos entregados             ->  0
     mensajes esperando en la cola ->  0
```

Detente ahí y pregunta a la sala: *«¿qué falló?»*. La respuesta correcta es **nada**. Cero errores,
tres 201. Ese es el punto: **el fallo silencioso**.

**Fase 2.** El script reinicia la app —eso es «levantar el servicio de avisos»— y no llega nada.

> *«Un aviso que se pierde en silencio es peor que un error: el error, al menos, se ve.»*

Y la frase que remata: *«nadie puede decirme CUÁLES doscientos»*. No es solo que se perdieran — es
que no quedó lista.

### 📚 00:10 – 00:50 · Teoría

`TEORIA.md`. Imprescindibles:

- **§2, el amortiguador.** «Una llamada directa exige que el otro esté vivo en el mismo instante;
  una cola solo exige que exista.» Si se llevan una frase, que sea esta.
- **§3, RabbitMQ vs Kafka.** Cinco minutos, con criterio. No instales Kafka (D-005).
- **§5 y §6, envenenado y DLQ.** El `default-requeue-rejected` merece su minuto: el **default** es
  el que atasca la cola.
- **§7, «exactly once» no existe.** Amárralo a RN-05 del Lab 06: *«esto ya lo hicieron ustedes»*.
- **§8, por qué el broker NO entra en readiness.** Es el puente con el Lab 10 y una de las mejores
  preguntas de criterio del curso. Úsala como pregunta abierta antes de dar la respuesta.
- **§10, nativo vs Resilience4j.** El temario lo exige y además es buen criterio profesional: una
  dependencia menos es una versión menos que alinear.

### ☕ 00:50 – 01:00 · Descanso

### 🔧 01:00 – 02:50 · Laboratorio

**El error que cometerá la sala — por orden de probabilidad:**

1. **Elegir mal la clave de idempotencia.** Un UUID por mensaje, o el timestamp. Hace único cada
   reintento y la deduplicación queda decorativa: `E2` la caza. Pregunta: *«¿tu clave identifica el
   hecho o la entrega?»*
2. **Dedupe en dos pasos.** `if (yaExiste()) return; else insertar();` — la carrera del Lab 06, van
   tres veces. Con un solo consumidor no falla casi nunca, y en producción falla.
3. **Poner la DLQ pero dejar `default-requeue-rejected` en `true`.** El `x-dead-letter-exchange`
   nunca se dispara porque el mensaje se re-encola antes. Todo «configurado» y nada funcionando.
4. **Publicar antes del commit.** Ahora es peor que en el Lab 11: el mensaje es *durable*, así que
   el error deja de evaporarse y queda escrito en disco.
5. **Meter el broker en readiness.** Después del §8 casi nadie lo hace — pero si alguien lo intenta,
   es una conversación de oro: *«acabas de apagar la aplicación por la dependencia de la que
   pasaste tres horas desacoplándote»*.

**Momento de pizarra recomendado (~01:40):** abre la consola del broker con la sala mirando, manda
un envenenado y ve las tres cosas a la vez: el contador de reintentos, el mensaje apareciendo en la
DLQ, y la cola principal vaciándose igual. Verlo en una pantalla vale más que el párrafo del §6.

### ✅ 02:50 – 03:00 · Cierre — LA SIEMBRA DEL EGRESO

Corre `90`, pide el reporte, y siembra el Lab 13 (`TEORIA §11`):

> *«La próxima semana no hay crimen. Hay un brief de Carolina, un repositorio casi vacío, y tres
> horas. Nadie te va a decir qué hacer.»*

Dilo despacio, y conecta con el manifiesto: no se evalúa si recuerdan la sintaxis —la máquina la
escribe mejor— sino si frente a un problema que no vieron **reconocen cuál es el problema**.

## Qué revisar en los reportes

1. **§1, el aviso evaporado.** ¿Entendió que el problema no fue el error, sino la **ausencia** de
   error? Y la segunda mitad: ¿por qué no había forma de saber cuáles se perdieron?
2. **§2, la clave de idempotencia.** La pregunta que separa: *¿identifica el hecho o la entrega?*
3. **§3, la DLQ.** *«¿Por qué es mejor caer a la DLQ que seguir reintentando para siempre?»* Si
   responde solo «para no perderlo», falta la mitad: **la cola sigue fluyendo**.
4. **§4, el circuito.** ¿Sabe decir su número — cuántos ms cerrado, cuántos abierto? ¿Y qué hace
   `HALF_OPEN`, y qué pasaría sin él?
5. **§5, readiness.** ¿Supo defender por qué el broker **no** entra? Es la pregunta más de criterio
   del lab.
6. **§6, honestidad.** Nunca penalices un «usé `--todo`».
