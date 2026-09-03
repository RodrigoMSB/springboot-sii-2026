# Pasos · Lab 11 · Observabilidad

Tres pasos y el paso 0. Se trabaja en `practica/`, en vivo.

```bash
cd practica
./mvnw spring-boot:run
```

Escucha en el **8101** (`solucion/`, en el 8102). La base va en el **55442**.

Lo que llega hecho: los trámites y la base. Lo que se escribe hoy:

```
pom.xml                        →  paso 1 (una dependencia)
application.yml                →  pasos 1, 2 y 3
infra/FiltroDeCorrelacion      →  paso 2
infra/SaludDeLaBase            →  paso 3
```

---

## Paso 0 · El sistema mudo

**Se explica:** la aplicación emite trámites y funciona. Se le pide uno y contesta.

**Se corre:**

```bash
curl -X POST http://localhost:8101/tramites \
     -H 'Content-Type: application/json' -d '{"tipo":"F29","rut":"11.111.111-1"}'
```

**En consola:**

```
{"id":1,"tipo":"F29"}

2026-09-03T14:00:41  INFO  c.d.o.controllers.TramiteController : Emitiendo trámite tipo=F29 rut=11.111.111-1
```

Y ahora las preguntas del día, que no se pueden contestar:

- ¿Está sana la aplicación **ahora mismo**? — «Parece que sí, acabo de llamarla.»
- ¿Y la base de datos que hay debajo? — «Si la aplicación contesta, supongo que sí.»
- De las cuarenta líneas que hay en el log, ¿cuáles son **de esta petición**? — «Las que están
  cerca, supongo.»

```
$ curl -o /dev/null -w "%{http_code}\n" http://localhost:8101/actuator/health
404
```

**No hay por dónde preguntar.**

---

## Paso 1 · Actuator, y qué NO se expone

**Se explica:** Actuator añade endpoints de diagnóstico. Con la dependencia basta, pero **lo
importante del paso es lo que se deja fuera**.

**Se pega:** en `practica/pom.xml`, **dentro de `<dependencies>`**.

```xml
    <dependency>
      <groupId>org.springframework.boot</groupId>
      <artifactId>spring-boot-starter-actuator</artifactId>
    </dependency>
```

y en `practica/src/main/resources/application.yml`, donde dice `# escribe aquí`.

> **Ojo con el YAML de este lab:** los pasos 1 y 3 pegan cada uno un trozo bajo `management:`.
> **Es una sola clave**, no dos: el segundo trozo se **funde** con lo que ya escribiste, no se
> pega debajo. Un `application.yml` con `management:` repetido dos veces no es válido y la
> aplicación no arranca.

```yaml
management:
  endpoints:
    web:
      exposure:
        include: health,info
```

**Se corre:** `curl http://localhost:8101/actuator`

**En consola:**

```
  expuesto: health  health-path  info  self
```

Y lo que **no** está:

```
  /actuator/env          -> 404
  /actuator/beans        -> 404
  /actuator/heapdump     -> 404
  /actuator/threaddump   -> 404
  /actuator/loggers      -> 404
  /actuator/mappings     -> 404
  /actuator/configprops  -> 404
  /actuator/metrics      -> 404
```

**Lo que hay que notar, y es todo el paso:** la lista es **nominal**. No es `include: "*"`, y hay
que decir por qué mirando los que quedaron fuera:

| endpoint | qué regala |
|---|---|
| `/env` y `/configprops` | **toda** la configuración: cadenas de conexión, usuarios, y el secreto de firma del Lab 09 |
| `/heapdump` | **la memoria del proceso en un archivo.** Todo lo que la aplicación tenga en RAM: tokens, datos de contribuyentes, contraseñas en tránsito |
| `/beans` y `/mappings` | el mapa del sistema por dentro: qué hay, cómo se llama y qué rutas existen — incluidas las que nadie publicó |
| `/loggers` | permite **cambiar el nivel de log en caliente**, sin reiniciar. Cómodo, y también una forma de apagar la auditoría |

> Con `include: "*"` se publican todos. Es una línea más corta de escribir y **es una filtración
> esperando fecha**.

La regla: **se enumera lo que se expone, uno por uno.** Y en producción, además, esto va detrás de
autenticación o en un puerto que no sale a internet.

**Y por qué `metrics` tampoco está**, que alguien lo va a preguntar: porque una métrica que nadie
recoge es un número que se pierde al reiniciar. Encender el endpoint es la parte fácil; lo que le
da sentido —un Prometheus preguntando cada quince segundos y un tablero dibujando la curva— está
en «lo que no vimos hoy».

---

## Paso 2 · Seguir UNA petición entre miles

**Se explica:** el log de arriba sirve con una petición. Con cuarenta simultáneas, las líneas de
una se mezclan con las de las otras y **no hay forma de saber cuáles van juntas**. En producción no
son cuarenta: son miles por minuto.

La solución es vieja y funciona: darle a cada petición un **identificador**, y que **todas** sus
líneas lo lleven. El sitio donde se guarda se llama **MDC** (*Mapped Diagnostic Context*): un mapa
atado al **hilo** que atiende la petición, que el sistema de logs sabe leer.

Lo bueno: se pone **una vez**, en un filtro, y a partir de ahí no hay que pasarlo por parámetro a
ningún sitio.

**Se pega:** archivo **nuevo**
`practica/src/main/java/cl/dgt/observabilidad/infra/FiltroDeCorrelacion.java` — el
archivo entero.

```java
package cl.dgt.observabilidad.infra;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.UUID;

@Component
public class FiltroDeCorrelacion extends OncePerRequestFilter {

    public static final String CABECERA = "X-Trace-Id";
    public static final String CLAVE = "traceId";

    @Override
    protected void doFilterInternal(HttpServletRequest peticion, HttpServletResponse respuesta,
                                    FilterChain cadena) throws ServletException, IOException {
        String id = peticion.getHeader(CABECERA);
        if (id == null || id.isBlank()) {
            id = UUID.randomUUID().toString().substring(0, 8);
        }
        MDC.put(CLAVE, id);
        respuesta.setHeader(CABECERA, id);
        try {
            cadena.doFilter(peticion, respuesta);
        } finally {
            MDC.remove(CLAVE);
        }
    }
}
```

y en `application.yml`, el patrón que lo imprime:

```yaml
logging:
  pattern:
    console: "%d{HH:mm:ss.SSS} %-5level [%X{traceId:-........}] %logger{25} - %msg%n"
```

`%X{traceId}` es «saca del MDC la clave traceId». El `:-........` es el valor por defecto cuando no
hay ninguno — las líneas de arranque, que no pertenecen a ninguna petición.

**Se corre:** dos peticiones seguidas.

**En consola:**

```
16:54:36.245 INFO  [b3189d84] c.d.o.c.TramiteController - Emitiendo trámite tipo=F29 rut=11.111.111-1
16:54:36.317 INFO  [b3189d84] c.d.o.c.TramiteController - Trámite 1 emitido
16:54:36.331 INFO  [ceccad1e] c.d.o.c.TramiteController - Emitiendo trámite tipo=F22 rut=22.222.222-2
16:54:36.338 INFO  [ceccad1e] c.d.o.c.TramiteController - Trámite 2 emitido
```

**Dos peticiones, dos identificadores.** Con esto, buscar `b3189d84` en el log de producción
devuelve **la historia completa de esa petición y nada más**. Sin esto, la pregunta «¿qué pasó con
el trámite del señor Pérez a las 14:03?» no tiene respuesta.

**Y hay que fijarse en dónde NO está el traceId:** en `TramiteController` no aparece por ninguna
parte. No se pasa por parámetro, no se menciona. Lo pone el filtro en el MDC y el patrón de log lo
imprime. Ése es todo el argumento del MDC.

**Tres detalles que valen la sesión:**

1. **El id vuelve en la cabecera de la respuesta.** Así, cuando un usuario reclama, puede decir su
   número:

```
$ curl -D- -o /dev/null -X POST http://localhost:8101/tramites ...
X-Trace-Id: 92110101
```

2. **Si el que llama ya trae un id, se respeta.** Ese `if` de tres líneas es lo que hace que el id
   **cruce de un servicio a otro** y se pueda seguir una operación completa entre varias
   aplicaciones:

```
$ curl -H "X-Trace-Id: MI-ID-123" http://localhost:8101/tramites
16:54:36.352 INFO  [MI-ID-123] c.d.o.c.TramiteController - Listando trámites
```

3. **Y el `finally` no es opcional.** El hilo que atendió esta petición **se reutiliza** para la
   siguiente. Sin el `MDC.remove`, la petición que viene hereda el id de la anterior, y el log
   miente — que es peor que no tenerlo.

---

## Paso 3 · Un health que dice QUÉ, y a quién le importa

**Se explica:** `/actuator/health` ya contesta, y contesta poco:

```json
{"status":"UP"}
```

Sirve para un semáforo y para nada más. Si un día dice `DOWN`, la pregunta siguiente —**¿qué se
cayó?**— no tiene respuesta. Este paso tiene dos mitades: primero se hace que el health **diga
qué**, y después se decide **a quién le importa cada cosa**.

### Primera mitad · que diga qué

**Se pega:** en `practica/src/main/resources/application.yml`, dentro del `management:` que ya
escribiste en el paso 1.

```yaml
management:
  endpoint:
    health:
      show-details: always
```

y el archivo **nuevo** `practica/src/main/java/cl/dgt/observabilidad/infra/SaludDeLaBase.java`
— el archivo entero:

```java
package cl.dgt.observabilidad.infra;

import org.springframework.boot.health.contributor.Health;
import org.springframework.boot.health.contributor.HealthIndicator;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

@Component("baseDeDatos")
public class SaludDeLaBase implements HealthIndicator {

    private final DataSource dataSource;

    public SaludDeLaBase(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Override
    public Health health() {
        long inicio = System.currentTimeMillis();
        try (Connection conexion = dataSource.getConnection();
             Statement sentencia = conexion.createStatement()) {

            sentencia.execute("SELECT count(*) FROM tramite");

            return Health.up()
                    .withDetail("consulta", "SELECT count(*) FROM tramite")
                    .withDetail("milisegundos", System.currentTimeMillis() - inicio)
                    .build();

        } catch (SQLException e) {
            return Health.down()
                    .withDetail("causa", "la base de datos no responde")
                    .withDetail("detalle", e.getMessage().lines().findFirst().orElse(""))
                    .withDetail("milisegundos", System.currentTimeMillis() - inicio)
                    .build();
        }
    }
}
```

**Se corre:** `curl http://localhost:8101/actuator/health`

**En consola:**

```json
{"status":"UP","components":{
  "baseDeDatos":{"status":"UP","details":{"consulta":"SELECT count(*) FROM tramite","milisegundos":5}},
  "db":{"status":"UP","details":{"database":"PostgreSQL","validationQuery":"isValid()"}},
  "diskSpace":{"status":"UP", ...},
  "livenessState":{"status":"UP"},
  "readinessState":{"status":"UP"},
  "ping":{"status":"UP"}, "ssl":{"status":"UP", ...}}}
```

**Lo que hay que notar:**

- **El nombre del bean es la clave del JSON.** `@Component("baseDeDatos")` produce
  `"baseDeDatos": {...}`.
- Aparece **también** un `db` que nadie escribió: es el indicador que Spring Boot pone solo al ver
  un `DataSource`. Hace un `isValid()`, que comprueba que la conexión vive. El nuestro va más allá:
  **ejecuta una consulta real contra una tabla real**. La diferencia importa el día que la conexión
  esté viva y la tabla no exista.
- **`milisegundos` no es decorativo:** una base que responde en 3.000 ms está técnicamente `UP` y
  en la práctica inservible. Ese número es lo que permite verlo venir.
- Y el `catch` **nombra la causa**. Es la diferencia entre «algo pasa» y «la base no responde», que
  es la diferencia entre buscar media hora y no buscar.

### Segunda mitad · liveness no es readiness

**Se explica:** y ahora la pregunta que de verdad importa, y que casi todo el mundo contesta mal.

Un orquestador (Kubernetes, o el que sea) le hace **dos** preguntas distintas a la aplicación:

| | pregunta | qué hace si la respuesta es NO |
|---|---|---|
| **liveness** | ¿estás vivo, o hay que **reiniciarte**? | mata el proceso y arranca otro |
| **readiness** | ¿puedes **atender** peticiones ahora? | te saca de rotación, sin matarte |

Y ahora la pregunta para la sala, antes de tocar nada:

> **Se cayó la base de datos. ¿Qué debe responder cada una?**

La respuesta —y hay que dejar que se equivoquen primero:

- **liveness: UP.** El proceso está perfectamente. **Reiniciarlo no arregla la base**, y encima
  tira las peticiones que estaba atendiendo. Si liveness bajara, el orquestador entraría en un
  bucle de reinicios mientras la base sigue caída: la aplicación pasaría de «degradada» a
  «apagada».
- **readiness: DOWN.** No puede atender. Que dejen de mandarle tráfico hasta que la base vuelva.

**Se pega:** en `practica/src/main/resources/application.yml`, en el mismo `management:`.

```yaml
management:
  endpoint:
    health:
      probes:
        enabled: true
      group:
        readiness:
          include: readinessState,baseDeDatos
        liveness:
          include: livenessState
```

**Esas cuatro últimas líneas son la decisión del laboratorio.** Por defecto, el health propio
**no** entra en ninguna sonda: hay que decir explícitamente que la base pertenece a readiness y no
a liveness.

**Se corre — con la base arriba:**

```
liveness  -> 200 {"components":{"livenessState":{"status":"UP"}},"status":"UP"}
readiness -> 200 {"components":{"baseDeDatos":{"details":{"consulta":"SELECT count(*) FROM tramite",
                 "milisegundos":4},"status":"UP"},"readinessState":{"status":"UP"}},"status":"UP"}
```

**Los dos dan 200, y no dicen lo mismo.** Liveness mira un solo estado —el del proceso—; readiness
mira además la base. Ahí ya se ve la diferencia: **son dos preguntas, no dos formas de preguntar
lo mismo.**

### Y ahora lo que pasaría con la base caída

**Esto no se corre. Se proyecta y se explica**, porque tirar la base en vivo cuesta media hora de
andamiaje que no enseña nada. Lo que respondería la aplicación:

```
liveness  -> 200 {"status":"UP"}      ← el proceso está sano. NO lo reinicies

readiness -> 503 {"status":"DOWN","components":{
                   "baseDeDatos":{"status":"DOWN","details":{
                     "causa":"la base de datos no responde",
                     "detalle":"Connection to localhost:55442 refused.",
                     "milisegundos":2003}},
                   "readinessState":{"status":"UP"}}}
```

**Aquí se para y se señalan los dos números.** 200 y 503, **al mismo tiempo**, contestando dos
preguntas distintas sobre la misma aplicación. Y el 503 viene con el nombre de lo que falló y con
el mensaje exacto del error.

Y cuando la base vuelve, readiness pasa a 200 **sin reiniciar nada**: el pool se reconecta, el
health vuelve a consultar y la aplicación vuelve a rotación sola.

> **La regla que se llevan:** liveness responde «¿sirve reiniciarme?». Readiness responde «¿puedo
> trabajar ahora?».
>
> **Ninguna dependencia externa va en liveness. Nunca.** Ni la base, ni Tesorería, ni el disco de
> red. Meter una dependencia externa en liveness es firmar un bucle de reinicios el día que esa
> dependencia falle — y el día que falle, un bucle de reinicios es exactamente lo último que hace
> falta.

---

## Lo que no vimos hoy

- **Métricas.** Actuator trae decenas —memoria, hilos, peticiones HTTP, pool de conexiones— y la
  aplicación puede declarar las suyas: un `Counter` de trámites emitidos, un `Timer` de cuánto
  tarda cada emisión. Se declara en tres líneas con Micrometer. Lo que no cabe en la sesión es lo
  que les da sentido: **alguien que las recoja**. Una métrica sin recolector es un número que se
  pierde al reiniciar.
- **Prometheus y un tablero.** Un proceso que pregunta `/actuator/prometheus` cada quince segundos,
  guarda la serie y dibuja la curva. Porque lo que importa casi nunca es el valor de ahora: es que
  los trámites emitidos cayeron a la mitad esta mañana.
- **Logs centralizados.** Los de todas las instancias en un solo sitio, buscables. Spring Boot 4
  trae logging estructurado nativo: `logging.structured.format.console: ecs` y cada línea sale
  como un JSON, sin añadir una sola dependencia.
- **Trazas distribuidas** (OpenTelemetry): el `traceId` de hoy, pero cruzando automáticamente de un
  servicio a otro, con el tiempo que se pasó en cada uno. Lo del paso 2 es la versión a mano de
  esa idea, y por eso se hace a mano: para entender qué está automatizando la herramienta.
- **Alertas.** Que alguien se entere a las tres de la mañana sin estar mirando.

---

## Al terminar

`practica/` responde lo mismo que `solucion/`. Lo que hay que poder decir con las propias palabras:

> Actuator se expone por lista nominal, nunca con asterisco, porque `/env` y `/heapdump` regalan
> el sistema entero. Un id de correlación por petición convierte un log ilegible en una historia
> que se puede seguir, y no cuesta ni una línea en el código de negocio. Y un health tiene que
> decir **qué** se cayó: liveness para reiniciar, readiness para sacar de rotación, y una
> dependencia externa sólo afecta a la segunda.

### Lo que siembra este lab

Con esto, la aplicación ya cuenta lo que le pasa. Y aparece la pregunta que no se veía antes:
**contarlo, ¿a quién?**

Hoy los datos se miraron con `curl`, a mano, uno por uno. Eso funciona con una aplicación, un
laboratorio y una persona mirando. En producción hay varias instancias, nadie mirando, y lo que
importa no es el valor de ahora: es **la curva** — que el health estuvo `DOWN` cuatro minutos de
madrugada, que las emisiones cayeron a la mitad esta mañana.

> **Lo que queda planteado:** un log que nadie centraliza es un archivo dentro de una máquina que
> mañana no existe, y un health que nadie consulta es un endpoint que nadie llama.

Eso es lo que está en «lo que no vimos hoy». El trabajo de este laboratorio es lo que lo hace
posible: **nada de eso se puede montar sobre una aplicación que no dice nada.**

Y queda una segunda cosa, más cercana. Todo lo de hoy pasa **mientras alguien llama**. Pero hay
trabajo que la aplicación tiene que hacer **sin que nadie llame** —un cierre a medianoche, un
correo que no debe hacer esperar al usuario— y eso todavía no sabemos hacerlo.

> **La pregunta que abre el Lab 12** — ¿cómo se hace que la aplicación haga algo **sola**, sin que
> haya una petición esperando al otro lado?
