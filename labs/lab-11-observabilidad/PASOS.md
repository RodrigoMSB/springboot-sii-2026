# Pasos · Lab 11 · Observabilidad

Cinco pasos. Se trabaja en `practica/`, en vivo.

```bash
cd practica
./mvnw spring-boot:run
```

Escucha en el **8101** (`solucion/`, en el 8102). La base va en el **55442**.

Lo que llega hecho: los trámites, la base y el mando a distancia. Lo que se escribe hoy:

```
pom.xml                        →  paso 1 (una dependencia)
application.yml                →  pasos 1, 2, 4 y 5
infra/                         →  pasos 2 y 4 (FiltroDeCorrelacion y SaludDeLaBase;
                                   el resto de `infra/` llega dado)
controllers/TramiteController  →  paso 3
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

2026-08-18T14:00:41  INFO  c.d.o.controllers.TramiteController : Emitiendo trámite tipo=F29 rut=11.111.111-1
```

Y ahora las preguntas del día, que no se pueden contestar:

- ¿Está sana la aplicación **ahora mismo**? — «Parece que sí, acabo de llamarla.»
- ¿Cuántos trámites lleva emitidos hoy? — «Ni idea.»
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

> **Ojo con el YAML de este lab:** los pasos 1, 4 y 5 pegan cada uno un trozo bajo `management:`.
> **Es una sola clave**, no tres: el segundo trozo y el tercero se **funden** con lo que ya
> escribiste, no se pegan debajo. Un `application.yml` con `management:` repetido tres veces no
> es válido y la aplicación no arranca.

```yaml
management:
  endpoints:
    web:
      exposure:
        include: health,info,metrics
```

**Se corre:** `curl http://localhost:8101/actuator`

**En consola:**

```
  expuesto: health health-path info metrics metrics-requiredMetricName self
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
```

**Lo que hay que notar, y es todo el paso:** la lista es **nominal**. No es `include: "*"`, y hay
que decir por qué mirando los cuatro que quedaron fuera:

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
14:00:25.895 INFO  [823c094e] c.d.o.c.TramiteController - Emitiendo trámite tipo=F29 rut=11.111.111-1
14:00:25.926 INFO  [823c094e] c.d.o.c.TramiteController - Trámite 1 emitido
14:00:25.941 INFO  [49598873] c.d.o.c.TramiteController - Emitiendo trámite tipo=F22 rut=22.222.222-2
14:00:25.942 INFO  [49598873] c.d.o.c.TramiteController - Trámite 2 emitido
```

**Dos peticiones, dos identificadores.** Con esto, buscar `823c094e` en el log de producción
devuelve **la historia completa de esa petición y nada más**. Sin esto, la pregunta «¿qué pasó con
el trámite del señor Pérez a las 14:03?» no tiene respuesta.

**Dos detalles que valen la sesión:**

1. **El id vuelve en la cabecera de la respuesta.** Así, cuando un usuario reclama, puede decir su
   número:

```
$ curl -D- -o /dev/null -X POST http://localhost:8101/tramites ...
X-Trace-Id: 49153662
```

2. **Si el que llama ya trae un id, se respeta.** Ese `if` de tres líneas es lo que hace que el id
   **cruce de un servicio a otro** y se pueda seguir una operación completa entre varias
   aplicaciones:

```
$ curl -H "X-Trace-Id: MI-ID-123" http://localhost:8101/tramites
14:00:25.978 INFO  [MI-ID-123] c.d.o.c.TramiteController - Listando trámites
```

3. **Y el `finally` no es opcional.** El hilo que atendió esta petición **se reutiliza** para la
   siguiente. Sin el `MDC.remove`, la petición que viene hereda el id de la anterior, y el log
   miente — que es peor que no tenerlo.

---

## Paso 3 · Una métrica que le importa al negocio

**Se explica:** Actuator ya trae decenas de métricas técnicas —memoria, hilos, peticiones HTTP,
pool de conexiones—. Lo que no puede traer es **cuántos trámites se emitieron**, porque eso sólo lo
sabe esta aplicación.

**Se pega:** en `practica/src/main/java/cl/dgt/observabilidad/controllers/TramiteController.java`,
**arriba, con los demás `import`**:

```java
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
```

**Se pega:** en el mismo archivo, el campo **debajo** de `private final TramiteRepository
repositorio;`, y el constructor **reemplazando el que hay** — le entra un parámetro más.

```java
    private final Counter emitidos;

    public TramiteController(TramiteRepository repositorio, MeterRegistry registro) {
        this.repositorio = repositorio;
        this.emitidos = Counter.builder("dgt.tramites.emitidos")
                .description("Trámites emitidos desde que arrancó la aplicación")
                .register(registro);
    }
```

```java
        emitidos.increment();
```

**Se corre:**

```bash
curl http://localhost:8101/actuator/metrics/dgt.tramites.emitidos
```

**En consola:**

```json
{"name":"dgt.tramites.emitidos",
 "description":"Trámites emitidos desde que arrancó la aplicación",
 "measurements":[{"statistic":"COUNT","value":3.0}]}
```

Se emiten dos más y se vuelve a mirar: **5.0**.

**Lo que hay que notar:**

- El `MeterRegistry` **se pide por constructor**, como cualquier dependencia. Lo pone Actuator.
- El contador **se declara una vez** y se guarda. Crearlo dentro del método también funciona
  —Micrometer devuelve el mismo si el nombre coincide— pero es trabajo por petición para nada.
- El nombre es jerárquico y con puntos: `dgt.tramites.emitidos`. La convención importa porque el
  día que haya un tablero, `dgt.*` es lo que se agrupa.
- Un `Counter` **sólo sube**. Para algo que sube y baja (trámites en curso) hay `Gauge`; para
  medir duraciones, `Timer`.

Y la pregunta que conviene dejar caer: *¿por qué esto y no un `SELECT count(*)`?* Porque la métrica
está **siempre disponible, es barata y no toca la base** — y porque un tablero la va a preguntar
cada quince segundos.

---

## Paso 4 · Un health que dice QUÉ se rompió

**Se explica:** `/actuator/health` ya contesta, y contesta poco:

```json
{"status":"UP"}
```

Sirve para un semáforo y para nada más. Si un día dice `DOWN`, la pregunta siguiente —**¿qué se
cayó?**— no tiene respuesta.

**Se pega:** en `practica/src/main/resources/application.yml`.

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
  "baseDeDatos":{"status":"UP","details":{"consulta":"SELECT count(*) FROM tramite","milisegundos":0}},
  "db":{"status":"UP","details":{"database":"PostgreSQL","validationQuery":"isValid()"}},
  "diskSpace":{"status":"UP", ...}}}
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

---

## Paso 5 · Liveness no es readiness

**Se explica:** ahora la pregunta que de verdad importa, y que casi todo el mundo contesta mal.

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

**Se pega:** en `practica/src/main/resources/application.yml`.

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
readiness -> 200 {"components":{"baseDeDatos":{...,"status":"UP"}},"status":"UP"}
```

**Y ahora se tira la base**, con la aplicación viva:

```bash
curl -X POST http://localhost:8101/simulador/base-caida
```

**En consola:**

```
liveness  -> 200 {"status":"UP"}      ← el proceso está sano. NO lo reinicies
readiness -> 503
             readiness: DOWN
             causa: la base de datos no responde
```

**Aquí se para y se señalan los dos números.** 200 y 503, **al mismo tiempo**, contestando dos
preguntas distintas sobre la misma aplicación. Y el 503 viene con el nombre de lo que falló.

**Se levanta otra vez:**

```bash
curl -X POST http://localhost:8101/simulador/base-sana
```

```
readiness -> 200 UP
```

**Sin reiniciar nada.** El pool se reconecta, el health vuelve a consultar y la aplicación vuelve a
rotación sola.

> **La regla que se llevan:** liveness responde «¿sirve reiniciarme?». Readiness responde «¿puedo
> trabajar ahora?». Meter una dependencia externa en liveness es firmar un bucle de reinicios el
> día que esa dependencia falle.

---

## Al terminar

`practica/` responde lo mismo que `solucion/`. Lo que hay que poder decir con las propias palabras:

> Actuator se expone por lista nominal, nunca con asterisco, porque `/env` y `/heapdump` regalan
> el sistema entero. Un id de correlación por petición convierte un log ilegible en una historia
> que se puede seguir. Una métrica de negocio la declara la aplicación, porque nadie más la sabe.
> Y un health tiene que decir **qué** se cayó: liveness para reiniciar, readiness para sacar de
> rotación, y una base caída sólo afecta a la segunda.

### Lo que siembra este lab

Con esto, la aplicación ya cuenta lo que le pasa. Y aparece la pregunta que no se veía antes:
**contarlo, ¿a quién?**

Hoy los datos se miraron con `curl`, a mano, uno por uno. Eso funciona con una aplicación, un
laboratorio y una persona mirando. En producción hay varias instancias, nadie mirando, y lo que
importa no es el valor de ahora: es **la curva** — que los trámites emitidos cayeron a la mitad
esta mañana, que el health estuvo `DOWN` cuatro minutos de madrugada.

> **Lo que queda planteado:** una métrica que nadie recoge es un número que se pierde al reiniciar,
> y un log que nadie centraliza es un archivo dentro de una máquina que mañana no existe.

Eso —Prometheus recogiendo cada quince segundos, un tablero dibujando la curva, los logs de todas
las instancias en un solo sitio, y el `traceId` cruzando de un servicio a otro— es lo que está en
«lo que no vimos hoy». El trabajo de este laboratorio es lo que lo hace posible: **nada de eso se
puede montar sobre una aplicación que no dice nada.**
