# Teoría · Módulo 14 (Observabilidad sobre OpenTelemetry, Métricas y Caché)

## Índice

1. [El tablero adorno](#1-el-tablero-adorno)
2. [Actuator: qué publica y qué NO deberías publicar](#2-actuator-qué-publica-y-qué-no-deberías-publicar)
3. [Liveness vs readiness: el semáforo y el hospital](#3-liveness-vs-readiness-el-semáforo-y-el-hospital)
4. [Health indicators propios](#4-health-indicators-propios)
5. [Qué entra en readiness, y qué no](#5-qué-entra-en-readiness-y-qué-no)
6. [Micrometer: contadores, timers, gauges](#6-micrometer-contadores-timers-gauges)
7. [Métricas de NEGOCIO](#7-métricas-de-negocio)
8. [Prometheus y Grafana](#8-prometheus-y-grafana)
9. [Caché: la abstracción de Spring y Caffeine](#9-caché-la-abstracción-de-spring-y-caffeine)
10. [Invalidación, TTL y consistencia](#10-invalidación-ttl-y-consistencia)
11. [Los tres pilares · Tabla DO/DON'T · Glosario](#11-los-tres-pilares--tabla-dodont--glosario)
12. [Conclusiones y siembra del Lab 11](#12-conclusiones-y-siembra-del-lab-11)

---

## 1. El tablero adorno

El Lab 09 terminó con un sistema que sabe **contar lo que hizo**. Hoy la pregunta es otra:
¿cómo **está**?

El practicante dejó Actuator configurado. `/actuator/health` responde `UP`. El monitoreo está en
verde. Y con PostgreSQL muerto, sigue diciendo `UP` mientras `/api/v1/tramites` devuelve 500 a
todos los contribuyentes.

No está roto: está **respondiendo a otra pregunta**. Confirma que el proceso Java existe. Nadie
le preguntó eso. La pregunta era si puede hacer su trabajo.

> Un semáforo que siempre está en verde no es un semáforo: es un adorno. Y un adorno en la pared
> de la sala de operaciones es peor que no tener nada, porque la gente le cree.

La línea que lo causa está escrita, con su justificación y todo:

```yaml
management:
  health:
    db:
      enabled: false    # "tiraba DOWN cuando Postgres tardaba y nos llenaba de alertas"
```

Y el practicante tenía razón: se acabaron las alertas. También se acabaron las verdaderas.

## 2. Actuator: qué publica y qué NO deberías publicar

Actuator es la puerta de servicio de la aplicación: endpoints que cuentan su estado interno.

| Endpoint | Qué da | ¿En producción? |
|---|---|---|
| `/health` | El semáforo | **Sí** — lo consulta el orquestador |
| `/info` | Versión, build | Sí |
| `/metrics` | Las métricas, una a una | Sí, cerrado con llave |
| `/prometheus` | Todas las series, en formato de scrape | Sí, cerrado con llave |
| `/env` | **Toda** la configuración resuelta, variables de entorno incluidas | **JAMÁS** |
| `/configprops` | Las propiedades enlazadas | No |
| `/beans` | El mapa interno de la aplicación | No |
| `/threaddump` | Volcado de hilos | No |
| `/heapdump` | **La memoria del proceso**, descargable | **JAMÁS** |

`/env` publica el secreto de firma del JWT y la contraseña de la base. `/heapdump` publica todo
lo demás. Es el crimen del Lab 01 —credenciales al alcance de quien pase— por otra puerta.

Por eso la exposición se declara como **lista blanca nominal**, nunca `*`:

```yaml
management:
  endpoints:
    web:
      exposure:
        include: health,info,metrics,prometheus
```

### 404 no es 401 — y la diferencia importa

| Respuesta | Significa | Quién lo decide |
|---|---|---|
| **404** | El endpoint NO EXISTE. No hay nada que forzar ni adivinar. | La lista blanca de Actuator (M12) |
| **401 / 403** | EXISTE, y con la credencial correcta se abre. | La cadena de filtros (M9, Lab 07) |

Se usan **las dos**, en ese orden: lo que no necesita estar publicado no se publica; lo que sí,
se cierra con llave. Defensa en profundidad. Confundirlas es lo que lleva a dejar `/env`
«protegido por seguridad» y descubrir el día del incidente que un rol de más lo abría.

## 3. Liveness vs readiness: el semáforo y el hospital

Son dos preguntas distintas y la respuesta a cada una desencadena una acción **opuesta**.

| | Pregunta | Si dice DOWN, ¿qué hago? |
|---|---|---|
| **liveness** | ¿Está **vivo** el proceso? | **Reiniciarlo.** Está colgado, sin salida. |
| **readiness** | ¿Puede **atender**? | **No mandarle tráfico** — y esperar. |

La analogía del hospital: *liveness* es «¿el médico respira?». *readiness* es «¿puede recibir
pacientes?». Un médico perfectamente vivo puede estar sin quirófano disponible: respira, y no
debe recibir pacientes todavía. Reanimarlo no le conseguiría un quirófano.

**Mezclarlas es el error caro.** Si la base entra en *liveness*, una caída de PostgreSQL hace que
Kubernetes reinicie todas las instancias. Reiniciar no levanta PostgreSQL. Resultado: una caída
de la base **más** un `CrashLoopBackOff` de la aplicación, y ahora hay dos incidentes que
diagnosticar en vez de uno. La aplicación no estaba rota: estaba sin base.

```yaml
management:
  endpoint:
    health:
      probes:
        enabled: true          # habilita /actuator/health/liveness y /readiness
      group:
        liveness:
          include: livenessState
        readiness:
          include: readinessState,baseDeDatos
```

## 4. Health indicators propios

Un `HealthIndicator` es una pregunta con respuesta `UP`/`DOWN` y detalles.

```java
@Component("baseDeDatos")     // el nombre del bean es la CLAVE que verá el operador
public class BaseDeDatosHealthIndicator implements HealthIndicator {
    @Override
    public Health health() {
        try {
            jdbc.sql("SELECT 1").query(Integer.class).single();
            return Health.up().withDetail("motor", "PostgreSQL").build();
        } catch (Exception fallo) {
            return Health.down().withDetail("causa", causaRaiz(fallo)).build();
        }
    }
}
```

Tres cosas que no son decorado:

1. **El nombre del bean es la clave de la respuesta.** `baseDeDatos` es lo que le dice al
   operador *qué* se cayó. «Algo se cayó» no es información.
2. **La pregunta es `SELECT 1`, no «¿existe el objeto DataSource?»**. El objeto siempre existe:
   lo creó Spring al arrancar y seguirá ahí aunque el motor lleve una hora muerto. Preguntar por
   el objeto es preguntarle al enfermo si respira mirándole la fotografía.
3. **El detalle lleva la causa raíz.** Nombrar el componente sin decir qué le pasa deja al que
   llega a las 3 AM en la misma duda.

> Boot ya trae un indicador para el `DataSource`. Se escribe a mano una vez para ver el contrato
> por dentro — porque el día que haya que chequear algo que Boot no conoce (TESO, una carpeta de
> adjuntos, una licencia por vencer), ese contrato es el único que hay.

### El health que llega tarde

Este chequeo tarda, como mucho, lo que Hikari tarde en rendirse pidiendo conexión
(`spring.datasource.hikari.connection-timeout`, **30 s por defecto**). Un health que tarda medio
minuto en admitir que está caído es casi tan inútil como uno que miente: para cuando responde, el
balanceador ya te mandó tráfico. En este lab baja a 5 s.

## 5. Qué entra en readiness, y qué no

La base **sí**: sin ella esta instancia no sirve un solo trámite.

**TESO no** — y es una decisión, no un olvido. El Lab 08 le puso *timeout*, *circuit breaker* y
degradación elegante: con TESO caído la API **sigue atendiendo** (los pagos esperan; el resto
funciona). Meter TESO en readiness sacaría de rotación a **toda la flota** por un servicio del
que ya aprendimos a no depender. Sería convertir una degradación en una caída total, por
prudencia mal entendida.

La regla: **readiness es «¿puedo atender?», no «¿está todo perfecto?»**. Solo entra lo que, si
falta, hace inútil a esta instancia. Y ojo con el efecto dominó: si tu readiness depende de un
servicio cuyo readiness depende del tuyo, un parpadeo tumba a los dos y ninguno vuelve.

## 6. Micrometer: contadores, timers, gauges

Micrometer es la fachada de métricas (lo que SLF4J es a los logs). Viaja dentro de
`spring-boot-starter-actuator`.

| Instrumento | Para qué | Ejemplo |
|---|---|---|
| **Counter** | Algo que solo sube | Folios emitidos |
| **Timer** | Cuántas veces y cuánto tardó | Tiempo de emisión |
| **Gauge** | Un valor que sube y baja **ahora** | Trámites en cola |

Las **etiquetas** (*tags*) son lo que convierte un número en una pregunta respondible:

```java
Counter.builder("dgt.folios.emitidos").tag("resultado", "nuevo").register(metricas);
```

Sin la etiqueta `resultado`, un pico en el contador no distingue «hoy se declaró mucho» (buena
noticia) de «un cliente reintenta en bucle» (incidencia). Con ella, la métrica conserva una
distinción que el negocio ya hacía (RN-05).

⚠️ **Cuidado con la cardinalidad.** Una etiqueta con el RUT del contribuyente crea una serie
temporal por contribuyente. Un millón de contribuyentes, un millón de series: eso no es
observabilidad, es una denegación de servicio contra tu propio Prometheus. Las etiquetas son para
valores de **baja cardinalidad**: estados, tipos, resultados. Nunca identificadores.

### Regístralas al arrancar, no al primer uso

Una serie **ausente** no se distingue de «el scrape falló». Una serie registrada y en **cero**
dice «vivo, sin emitir» — y eso sí es una alerta accionable. La ausencia es una duda; el cero es
un dato.

## 7. Métricas de NEGOCIO

El CPU, la memoria, los hilos y la latencia HTTP los publica Actuator solo. Ninguna de esas sabe
**cuántos folios emitió la DGT hoy**.

> Un servidor al 3 % de carga puede llevar dos horas sin emitir un solo folio porque un validador
> rechaza todo. La máquina: perfecta. El negocio: detenido. El contador en cero lo grita; el CPU,
> no.

Ese es el salto del módulo: de métricas que le importan al que mantiene la máquina, a métricas
que le importan a **Carolina**. Son las que permiten *ver venir* el problema en vez de reaccionar
cuando ya llamó el ministerio.

## 8. Prometheus y Grafana

**Prometheus** no recibe: **va a buscar** (*scrape*). Cada cierto tiempo pide
`/actuator/prometheus` y guarda las series con su marca de tiempo.

```
# HELP dgt_folios_emitidos_total Folios emitidos por la DGT
# TYPE dgt_folios_emitidos_total counter
dgt_folios_emitidos_total{application="dgt-tramites-api",resultado="nuevo"} 47.0
dgt_folios_emitidos_total{application="dgt-tramites-api",resultado="reusado"} 3.0
```

**Grafana** dibuja esas series y dispara alertas. Y aquí se cierra el arco que abrió el Lab 09:
el `traceId` del log y la métrica del tablero son **la misma operación vista de dos maneras**. El
tablero te dice *que* algo pasa; la traza te dice *qué* pasó exactamente y *a quién*. Se ve en la
demo del relator; no se teclea hoy.

## 9. Caché: la abstracción de Spring y Caffeine

Spring da la **abstracción**; el proveedor da la implementación.

| Anotación | Qué hace |
|---|---|
| `@Cacheable` | Si está en caché, lo devuelve. Si no, ejecuta y guarda. |
| `@CachePut` | **Siempre** ejecuta, y refresca la entrada. |
| `@CacheEvict` | Borra la entrada (o todas, con `allEntries = true`). |

Sin proveedor, Boot cae en un `ConcurrentMapCacheManager`: un `HashMap` que nunca expira, nunca
se acota y nunca cuenta nada. **Caffeine** trae lo que este lab necesita: TTL, tope de tamaño y
**estadísticas**.

```java
Caffeine.newBuilder()
        .expireAfterWrite(Duration.ofMinutes(5))
        .maximumSize(500)
        .recordStats();          // sin esto, no hay hit-rate: el caché es un acto de fe
```

### Las tres condiciones antes de cachear nada

1. **Es cara.** (Aquí: `JOIN` + `GROUP BY` sobre todo el histórico.)
2. **Se lee mucho más de lo que cambia.**
3. **Tolera estar un poco desactualizada** — y ese «un poco» se **declara**, no se supone.

Si falla cualquiera de las tres, cachear es cambiar un problema de rendimiento por uno de
**correctitud**. Y los de correctitud se pagan más caros: el lento se nota, el incorrecto no.

### Mídelo o bórralo

Un caché con 2 % de aciertos no es un caché: es una tabla de despiste que además puede servir
datos rancios. Medir el hit-rate es lo que te da derecho a mantenerlo — o el argumento para
quitarlo.

⚠️ **La trampa del proxy, otra vez.** `@Cacheable` y `@CacheEvict` los aplica un proxy, igual que
`@Transactional` (Lab 06) y que el aspecto de auditoría (Lab 09). Una llamada entre métodos de la
misma clase **no pasa por el proxy**: la anotación no se dispara y nadie avisa. Es la misma
trampa por tercera vez; a estas alturas ya no es mala suerte, es cómo funciona Spring.

## 10. Invalidación, TTL y consistencia

> **Un caché sin invalidación es un mentiroso con buena memoria:** responde rápido, con
> seguridad, y con el dato de antes.

La regla: **quien ESCRIBE el dato es el responsable de invalidar su copia.** Si esa
responsabilidad se deja en el lector, no hay forma de saber cuándo invalidar — y el bug no se ve,
porque el sistema no falla: solo miente.

**El TTL no es la invalidación: es la red por si la invalidación falla.** Acota cuánto puede
durar una mentira si alguien olvidó el `@CacheEvict`, si otra instancia escribió, o si un proceso
batch tocó la base por fuera de la aplicación.

**Granularidad.** Nuestro caché guarda la lista completa de períodos en **una sola entrada**, así
que una línea de cualquier período la invalida entera (`allEntries = true`). Cuando la
granularidad del caché y la de la escritura no coinciden, se tira todo: más caro y correcto, en
ese orden.

**Redis** (conceptual, no se instala hoy). Con varias instancias, cada una tiene su copia local y
empiezan a contradecirse: la instancia A invalidó, la B no se enteró. Una caché **distribuida**
es una sola copia compartida. Es la respuesta correcta cuando el problema aparece — y el problema
aparece cuando hay más de un servidor. Recuerda esta frase la semana que viene.

## 11. Los tres pilares · Tabla DO/DON'T · Glosario

| Pilar | Responde | Dónde se vio |
|---|---|---|
| **Logs** | ¿Qué pasó exactamente, y a quién? | Lab 09 |
| **Métricas** | ¿Cómo va el sistema, en agregado? | **Hoy** |
| **Trazas** | ¿Por dónde pasó una operación? | Lab 09 (`traceId`) → distribuida, conceptual |

| ✅ DO | ❌ DON'T |
|---|---|
| Health que consulta la dependencia (`SELECT 1`) | Health que confirma que el proceso existe |
| Liveness y readiness **separados** | Un solo `/health` para todo |
| Nombrar el componente caído y su causa | Un `DOWN` anónimo |
| Lista blanca nominal de endpoints | `include: "*"` |
| Etiquetas de baja cardinalidad | Etiquetar por RUT o por id |
| Registrar las métricas al arrancar | Crearlas al primer uso |
| Métricas de negocio | Solo CPU y memoria |
| Cachear lo caro, estable y tolerante | Cachear «porque sí» |
| Invalidar donde se escribe | Confiar solo en el TTL |
| Medir el hit-rate | Suponer que el caché ayuda |

- **Actuator** — los endpoints de estado interno de la aplicación.
- **liveness** — ¿está vivo el proceso? Si no: reiniciar.
- **readiness** — ¿puede atender? Si no: no mandarle tráfico.
- **HealthIndicator** — una comprobación con respuesta `UP`/`DOWN` y detalles.
- **Micrometer** — la fachada de métricas; Counter, Timer, Gauge.
- **Cardinalidad** — cuántas series distintas genera una etiqueta.
- **Scrape** — Prometheus yendo a buscar las métricas.
- **Hit-rate** — proporción de peticiones servidas desde el caché.
- **TTL** — cuánto vive una entrada; la red bajo la invalidación.

---

## 12. Conclusiones y siembra del Lab 11

Hoy el tablero dejó de ser un adorno. Readiness dice la verdad y **nombra** lo que se cayó;
liveness no arrastra a la aplicación a un bucle de reinicios por culpa de una dependencia; las
métricas cuentan lo que al negocio le importa; y la consulta cara se sirve de memoria, medida y
con invalidación explícita.

Carolina ya no tiene que elegir entre creerle al monitoreo o a los contribuyentes.

🌱 **Siembra del Lab 11 — «Latidos».**

Ya sabes si tu sistema está sano y puedes medir lo que hace. Pero al mirar el tablero nuevo,
Carolina encontró algo raro: el **cierre nocturno del viernes se ejecutó dos veces**. El contador
de cierres marcaba 2 donde debía marcar 1.

La DGT corre en **dos servidores** para aguantar la carga. A medianoche, los dos dispararon la
misma tarea programada: los dos se creyeron el único. Dos cierres, dos veces los mismos correos,
dos veces los mismos cálculos.

Y fíjate: es el mismo problema que dejamos anotado en el §10 de hoy, cuando dijimos que cada
instancia tiene su propia copia del caché y ninguna sabe de las otras. Un sistema de un servidor
y uno de dos no son el mismo sistema con más máquinas: son dos problemas distintos.

La próxima semana, **el reloj tiene un problema de identidad**: tareas programadas, y cómo hacer
que en un mundo de dos (o diez) servidores, una tarea que debe correr UNA vez, corra una sola vez.
