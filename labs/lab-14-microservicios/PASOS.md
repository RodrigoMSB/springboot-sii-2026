# Pasos · Lab 14 · Microservicios

Ocho pasos, y los dos primeros **no se teclean**: son infraestructura y una apuesta. Se trabaja en
`practica/`, con cuatro terminales abiertas.

```bash
# terminal 1                        # terminal 2
cd practica/contribuyentes          cd practica/tramites
./mvnw spring-boot:run              ./mvnw spring-boot:run

# terminal 3                        # terminal 4
cd practica/auditoria               cd practica/gateway
./mvnw spring-boot:run              ./mvnw spring-boot:run
```

En ese orden: **contribuyentes → trámites → auditoría → gateway**.

Lo que llega hecho: **contribuyentes y auditoría, enteros** — son las hojas del sistema y hoy no se
tocan. Lo que se escribe está todo en los otros dos:

```
tramites/clientes/ClienteContribuyentes.java  →  pasos 3, 5 y 7
tramites/clientes/ClienteAuditoria.java       →  paso 8
tramites/services/TramiteService.java         →  pasos 3, 5 y 8
tramites/controllers/TramiteController.java   →  paso 5
gateway/enrutado/TablaDeRutas.java            →  paso 6
gateway/config/SeguridadConfig.java        →  paso 6
gateway/enrutado/Enrutador.java               →  paso 7
```

**Un aviso antes de empezar:** los tiempos de abajo son los medidos al preparar el material, en un
Mac. Los de la sala variarán en las centésimas. Lo que no varía es lo que enseña cada paso: un
código de estado, un contador que se congela, un id que aparece tres veces.

---

## Paso 0 · El monolito que teníamos · 10 min, sin teclado

**Se explica:** durante trece sesiones esto fue **una** aplicación. Una base de datos, un
`./mvnw spring-boot:run`, un log, un `pom.xml`. Traer el nombre del contribuyente dentro de un
trámite era esto:

```java
Contribuyente c = contribuyenteRepository.findByRut(tramite.getRut());
```

Nanosegundos. Siempre disponible. Dentro de la misma transacción. Imposible que fallara sin que
fallara todo lo demás.

Hoy la misma frase son **cuatro procesos y tres bases de datos**. Antes de arrancar nada, la
pregunta del día, y conviene pedir manos alzadas y anotar la apuesta en la pizarra:

> Vamos a partir esto en cuatro. **¿Qué creen que ganamos, y qué creen que pagamos?**

No se corrige a nadie. La lista se queda escrita y se vuelve a ella en el cierre.

---

## Paso 1 · Levantar el sistema

**Se explica:** esto es infraestructura, no código. Antes de tocar nada hay que verlo en pie, y hay
que ver **que son cuatro cosas de verdad**: cuatro terminales, cuatro banners de Spring, cuatro
puertos.

**Se corre:** los cuatro comandos de arriba, en su orden, cada uno en su terminal. Y después, desde
una quinta:

```bash
curl http://localhost:8201/salud
curl http://localhost:8202/salud
curl http://localhost:8203/salud
curl http://localhost:8200/salud
```

**En consola:**

```
{"servicio":"contribuyentes","estado":"vivo"}
{"servicio":"tramites","estado":"vivo"}
{"servicio":"auditoria","estado":"vivo"}
{"servicio":"gateway","estado":"vivo"}
```

**Lo que hay que notar:** cada terminal levantó **su propio PostgreSQL**. En el log de tres de
ellas sale su base y su puerto, y son tres puertos distintos:

```
CONTRIBUYENTES - Database: jdbc:postgresql://localhost:55450/postgres (PostgreSQL 16.14)
TRAMITES       - Database: jdbc:postgresql://localhost:55451/postgres (PostgreSQL 16.14)
AUDITORIA      - Database: jdbc:postgresql://localhost:55452/postgres (PostgreSQL 16.14)
```

Tres motores, tres directorios de datos, tres esquemas. El gateway no tiene ninguno: **no es dueño
de ningún dato**, solo reparte.

> Con Docker Compose esto habría sido `docker compose up` y una sola línea de salida. Se vería
> menos, y esa es exactamente la razón de hacerlo a mano: hoy los procesos, sus puertos y sus bases
> **se ven**.

---

## Paso 2 · Una base por servicio

**Se explica:** el trámite número 1 está en la base de trámites. Vamos a pedirlo.

**Se corre:**

```bash
curl -s http://localhost:8202/tramites/1
```

**En consola:**

```json
{"id":1,"tipo":"DECLARACION_F29","estado":"EN_PROCESO",
 "rutContribuyente":"11111111-1","nombreContribuyente":null,
 "estadoDelNombre":"NO_CONSULTADO","creadoEn":"..."}
```

**Falta el nombre.** Y no es un `null` de olvido: mira la tabla que hay debajo, en
`tramites/src/main/resources/db/migration/V1__tramite.sql`.

```sql
CREATE TABLE tramite (
    id                BIGSERIAL PRIMARY KEY,
    rut_contribuyente VARCHAR(20) NOT NULL,     -- el RUT, y nada más
    tipo              VARCHAR(40) NOT NULL,
    estado            VARCHAR(30) NOT NULL,
    creado_en         TIMESTAMPTZ NOT NULL
);
```

El nombre no está porque **no le pertenece a este servicio**. Y aquí está el momento del paso: la
pregunta obvia es «bueno, pues un JOIN». Antes de discutirlo, se mira qué hay de verdad en cada
base. Los tres servicios traen un endpoint que lista **las tablas de su propia base**:

```bash
curl -s http://localhost:8201/mi-base
curl -s http://localhost:8202/mi-base
curl -s http://localhost:8203/mi-base
```

**En consola:**

```json
{"servicio":"contribuyentes","tablas":["contribuyente","flyway_schema_history"]}
{"servicio":"tramites",      "tablas":["flyway_schema_history","tramite"]}
{"servicio":"auditoria",     "tablas":["flyway_schema_history","registro_de_auditoria"]}
```

Se lee la segunda línea otra vez, despacio. **En la base de trámites hay una tabla: `tramite`.** No
hay ninguna `contribuyente`. El JOIN que alguien iba a proponer no es que esté prohibido: es que
`FROM tramite t JOIN contribuyente c ...` no tiene con qué juntarse, y el motor contestaría
`relation "contribuyente" does not exist`.

**Lo que hay que notar, y es la idea del laboratorio entero:**

> No es que el JOIN esté prohibido por convención. Es que **no hay tabla que juntar**. La frontera
> no es una regla de estilo escrita en un README: es física.

El nombre existe, y está a un puerto de distancia:

```bash
curl -s http://localhost:8201/contribuyentes/11111111-1
```

```json
{"rut":"11111111-1","nombre":"Carolina Fuentes Aravena","segmento":"PERSONA_NATURAL"}
```

Ahí está. En otra base, en otro proceso. Para traerlo hay que **preguntar**, y preguntar es el
paso 3.

---

## Paso 3 · La llamada entre servicios

**Se explica:** lo que era `findByRut()` pasa a ser una llamada HTTP. Con dos consecuencias que hay
que decir en voz alta antes de escribir nada: puede **tardar**, y puede **fallar**. Un `findByRut()`
no hacía ninguna de las dos cosas por su cuenta.

**Se pega:** archivo **nuevo**
`practica/tramites/src/main/java/cl/dgt/tramites/clientes/ClienteContribuyentes.java` — el
archivo entero.

<!-- pasos:intermedio · el paso 5 lo reescribe con el circuito y el 7 le añade la cabecera -->

```java
package cl.dgt.tramites.clientes;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.net.http.HttpClient;
import java.time.Duration;

@Component
public class ClienteContribuyentes {

    private static final Logger log = LoggerFactory.getLogger(ClienteContribuyentes.class);

    private static final Duration TIMEOUT = Duration.ofSeconds(2);

    private final RestClient http;

    public record FichaDto(String rut, String nombre, String segmento) {
    }

    public ClienteContribuyentes(@Value("${lab14.contribuyentes.url}") String url) {
        JdkClientHttpRequestFactory fabrica =
                new JdkClientHttpRequestFactory(HttpClient.newBuilder().connectTimeout(TIMEOUT).build());
        fabrica.setReadTimeout(TIMEOUT);
        this.http = RestClient.builder().baseUrl(url).requestFactory(fabrica).build();
    }

    public FichaDto ficha(String rut) {
        log.info("[TRAMITES] pido la ficha de {} a contribuyentes", rut);
        return http.get().uri("/contribuyentes/{rut}", rut).retrieve().body(FichaDto.class);
    }
}
```

Dos cosas que merecen una frase cada una:

- **El timeout va desde el primer minuto.** Es la lección del Lab 10 —«el valor por defecto de un
  cliente HTTP no es un timeout largo, es ninguno»— aplicada a un vecino que ahora está al otro
  lado de la red.
- **`FichaDto` está declarado aquí, otra vez.** Contribuyentes tiene el suyo. Duplicarlo parece
  sucio y es deliberado: si los dos servicios compartieran un módulo con los DTO, cambiar un campo
  obligaría a desplegar los dos a la vez — y ahí se perdió la independencia por la que se partió el
  sistema.

**Se pega:** en `practica/tramites/src/main/java/cl/dgt/tramites/services/TramiteService.java` —
recibir el cliente y usarlo.

<!-- pasos:intermedio · el paso 5 lo reescribe con el circuito, y el 7 le anade la cabecera del id -->

```java
    private final TramiteRepository repositorio;
    private final ClienteContribuyentes contribuyentes;

    public TramiteService(TramiteRepository repositorio, ClienteContribuyentes contribuyentes) {
        this.repositorio = repositorio;
        this.contribuyentes = contribuyentes;
    }
```

y el método que cose los dos datos:

<!-- pasos:intermedio · el paso 5 lo cambia para tratar la ausencia como caso normal -->

```java
    private TramiteDto conNombre(Tramite tramite) {
        ClienteContribuyentes.FichaDto ficha = contribuyentes.ficha(tramite.getRutContribuyente());
        return new TramiteDto(tramite.getId(), tramite.getTipo(), tramite.getEstado(),
                tramite.getRutContribuyente(), ficha.nombre(), "OK", tramite.getCreadoEn());
    }
```

**Se corre:** Ctrl+C en la terminal de trámites, `./mvnw spring-boot:run` otra vez, y:

```bash
curl -s http://localhost:8202/tramites/1
```

**En consola:**

```json
{"id":1,"tipo":"DECLARACION_F29","estado":"EN_PROCESO",
 "rutContribuyente":"11111111-1","nombreContribuyente":"Carolina Fuentes Aravena",
 "estadoDelNombre":"OK","creadoEn":"..."}
```

**Y ahora lo que de verdad hay que mirar, que no está en el JSON.** Se ponen las dos terminales
lado a lado:

```
terminal de trámites:
14:16:55.294 INFO [2cd88c14] TRAMITES - [TRAMITES] pido la ficha de 11111111-1 a contribuyentes

terminal de contribuyentes:
14:16:55.333 INFO [1efa2ebb] CONTRIBUYENTES - [CONTRIBUYENTES] me piden la ficha de 11111111-1
```

**Dos programas distintos, treinta y nueve milésimas de diferencia.** Ese salto entre las dos
pantallas es todo lo que separa un monolito de un sistema repartido — y todo lo que viene después
del paso 4 existe por culpa de ese salto.

---

## Paso 4 · Matar un servicio ← el momento fuerte

**Se explica:** hasta aquí, aplausos. Ahora la pregunta, otra vez con manos alzadas, **antes** de
correr nada:

> Voy a apagar contribuyentes. El trámite número 1 sigue entero en la base de trámites: su id, su
> tipo, su estado, su RUT. Lo único que no está es el nombre. **¿Qué va a devolver
> `GET /tramites/1`?**

Casi siempre alguien dice «el trámite sin el nombre». Es lo razonable. No es lo que pasa.

**Se corre:** Ctrl+C en la terminal de **contribuyentes**. Y desde la quinta:

```bash
curl -s -o /dev/null -w "HTTP %{http_code}  en %{time_total}s\n" http://localhost:8202/tramites/1
curl -s -o /dev/null -w "HTTP %{http_code}  en %{time_total}s\n" http://localhost:8202/tramites/1
curl -s -o /dev/null -w "HTTP %{http_code}  en %{time_total}s\n" http://localhost:8202/tramites/1
```

**En consola:**

```
HTTP 500  en 0.018616s
HTTP 500  en 0.007050s
HTTP 500  en 0.006007s
```

Y el cuerpo que recibe el usuario:

```json
{"timestamp":"2026-08-20T18:17:14.987Z","status":500,
 "error":"Internal Server Error","path":"/tramites/1"}
```

Y en el log de trámites:

```
ERROR TRAMITES - ... threw exception [Request processing failed:
  org.springframework.web.client.ResourceAccessException:
  I/O error on GET request for "http://localhost:8201/contribuyentes/11111111-1": null]
  with root cause java.nio.channels.ClosedChannelException
```

**Lo que hay que notar, y hay que decirlo despacio:**

> **El servicio de trámites está perfectamente sano.** Su base está viva, su tabla está entera, el
> trámite número 1 está ahí. Y no puede entregarlo, porque **otro** servicio está caído.

Eso es el **fallo en cascada**: un servicio que se cae se lleva por delante a todos los que lo
llaman, aunque lo que le estuvieran pidiendo no fuera lo importante.

Y conviene medir bien lo que se acaba de medir, sin adornar:

- El fallo es **rápido** —cinco milésimas— porque un proceso muerto **rechaza** la conexión al
  instante. Aquí no hay espera.
- El caso caro es el otro: el vecino que **no se cae** y se pone lento. Ese se midió en el Lab 10 y
  costaba **30,01 segundos** por petición, con un hilo de Tomcat bloqueado en cada una. Los dos
  casos son reales; hoy tocó el rápido.
- Lo que sí es igual de grave en los dos: **el usuario no recibe su trámite**, y trámites sigue
  llamando a la puerta de un muerto, una vez por cada petición, indefinidamente.

---

## Paso 5 · Circuit breaker y degradación

**Se explica:** hay dos problemas y son distintos.

1. **La respuesta.** Falta un campo accesorio y se cae la petición entera. Eso hay que decidirlo,
   no sufrirlo.
2. **La insistencia.** Trámites sigue golpeando a un servicio caído. Cuando el caído está
   intentando levantarse, esa insistencia es parte del problema.

El **circuit breaker** es un diferencial eléctrico: no arregla el cortocircuito, corta la corriente
para que no arda el edificio. Tres estados:

| Estado | Qué hace | Cuándo cambia |
|---|---|---|
| **CERRADO** | deja pasar todo y cuenta fallos | supera el umbral → ABIERTO |
| **ABIERTO** | rechaza sin llamar (*falla rápido*) | pasa el tiempo → MEDIO ABIERTO |
| **MEDIO ABIERTO** | deja pasar unas pocas de prueba | van bien → CERRADO · van mal → ABIERTO |

**Se pega:** en `practica/tramites/src/main/java/cl/dgt/tramites/clientes/ClienteContribuyentes.java`.
Primero el circuito, **dentro del constructor**:

<!-- pasos:intermedio · el paso 5 lo reescribe -->

```java
    private final CircuitBreaker circuito;
    private final AtomicInteger llamadasReales = new AtomicInteger();

    // ...dentro del constructor, después del RestClient:
    this.circuito = CircuitBreaker.of("contribuyentes", CircuitBreakerConfig.custom()
            .slidingWindowType(CircuitBreakerConfig.SlidingWindowType.COUNT_BASED)
            .slidingWindowSize(4)
            .minimumNumberOfCalls(3)
            .failureRateThreshold(50)
            .waitDurationInOpenState(Duration.ofSeconds(10))
            .permittedNumberOfCallsInHalfOpenState(1)
            .ignoreExceptions(HttpClientErrorException.class)
            .build());

    circuito.getEventPublisher().onStateTransition(e ->
            log.warn("[CIRCUITO] {} -> {}",
                    e.getStateTransition().getFromState(), e.getStateTransition().getToState()));
```

> **Los umbrales no son decorativos, y hay que explicar por qué están ahí.** Sin declararlos, manda
> el valor por defecto de Resilience4j: `minimumNumberOfCalls: 100`. **Cien llamadas** antes de que
> el circuito llegue siquiera a *opinar*. En esta sesión no hay cien llamadas; en un servicio
> interno con poco tráfico, tampoco. Eso es un **circuit breaker decorativo**: sale en el árbol de
> dependencias, sale en el diagrama de arquitectura, y no se va a abrir jamás.
>
> Un patrón de resiliencia mal configurado es **peor** que no tenerlo: da la tranquilidad sin dar
> la protección.

Y la línea que parece un detalle y no lo es — `ignoreExceptions(HttpClientErrorException.class)`:

> Un circuito mide **la salud del otro servicio**. Un **404** no dice nada malo del otro servicio:
> dice que **tú** pediste algo que no existe.

Sin esa línea, pedir dos veces un trámite cuyo RUT no está en el padrón abre el circuito — y a
partir de ahí **todos** los trámites salen degradados, con contribuyentes vivo y contestando 200 a
todo el mundo. Se midió al preparar el material, y salía exactamente así:

```
:8211/contribuyentes/11111111-1  ->  HTTP 200      (contribuyentes, perfectamente sano)
:8202/tramites/estado-circuito   ->  "circuito":"OPEN"
```

La regla, y sirve para cualquier circuito que escribas: **los 5xx y los fallos de red cuentan; los
4xx no.**

Después, `ficha()` cambia de forma: en vez de lanzar, devuelve `Optional`.

```java
    public Optional<FichaDto> ficha(String rut) {
        try {
            return Optional.ofNullable(circuito.executeCallable(() -> pedir(rut)));
        } catch (CallNotPermittedException e) {
            log.warn("[TRAMITES] circuito ABIERTO: no llamo a contribuyentes por {}", rut);
            return Optional.empty();
        } catch (Exception e) {
            log.warn("[TRAMITES] contribuyentes no contestó por {}: {}", rut, e.getClass().getSimpleName());
            return Optional.empty();
        }
    }

    private FichaDto pedir(String rut) {
        llamadasReales.incrementAndGet();
        log.info("[TRAMITES] pido la ficha de {} a contribuyentes", rut);
        return http.get().uri("/contribuyentes/{rut}", rut).retrieve().body(FichaDto.class);
    }
```

Y el tablero, para poder mirar el circuito por dentro:

<!-- pasos:intermedio · el paso 7 le anade la cabecera del id de correlacion -->

```java
    public Map<String, Object> estadoDelCircuito() {
        CircuitBreaker.Metrics m = circuito.getMetrics();
        return Map.of(
                "circuito", circuito.getState().name(),
                "llamadasHttpReales", llamadasReales.get(),
                "llamadasEnLaVentana", m.getNumberOfBufferedCalls(),
                "fallidas", m.getNumberOfFailedCalls(),
                "tasaDeFallo", m.getFailureRate());
    }
```

**Ahora la mitad que no es técnica**, y es la que se lleva el alumno. En `TramiteService`:

```java
    private TramiteDto conNombre(Tramite tramite) {
        return contribuyentes.ficha(tramite.getRutContribuyente())
                .map(f -> nuevoDto(tramite, f.nombre(), "OK"))
                .orElseGet(() -> nuevoDto(tramite, null, "NO_DISPONIBLE"));
    }
```

Ese `"NO_DISPONIBLE"` **lo escribió alguien**. Es una decisión de negocio disfrazada de línea de
código, y hay cuatro opciones defendibles:

| Opción | Qué le dice al usuario | Cuándo es correcta |
|---|---|---|
| Datos parciales en silencio (`null` y nada más) | «aquí tienes» — y miente por omisión | cuando el campo es accesorio de verdad |
| Datos parciales **marcados** ← la de hoy | «esto está incompleto» | casi siempre |
| HTTP 503 honesto | «ahora no puedo» | cuando el dato es esencial |
| Último valor conocido (caché) | «esto es de hace un rato» | si lo viejo sirve |

> Lo indefendible no es elegir mal. Es **elegir sin darte cuenta de que estás eligiendo** — y el
> valor por defecto siempre es «datos parciales en silencio», porque es lo que sale de escribir el
> fallback sin pensarlo.

Falta el endpoint del tablero, en `TramiteController.java`:

```java
    @GetMapping("/estado-circuito")
    public Map<String, Object> estadoDelCircuito() {
        return contribuyentes.estadoDelCircuito();
    }
```

**Se corre:** Ctrl+C y arrancar trámites otra vez. **Contribuyentes sigue apagado** — este es el
mismo escenario exacto del paso 4.

```bash
for i in 1 2 3 4 5 6; do
  curl -s -o /dev/null -w "HTTP %{http_code} en %{time_total}s  " http://localhost:8202/tramites/1
  curl -s http://localhost:8202/tramites/estado-circuito; echo
done
```

**En consola:**

```
HTTP 200 en 0.106026s  {"circuito":"CLOSED","llamadasHttpReales":1,"fallidas":1,...}
HTTP 200 en 0.006448s  {"circuito":"CLOSED","llamadasHttpReales":2,"fallidas":2,...}
HTTP 200 en 0.007348s  {"circuito":"OPEN",  "llamadasHttpReales":3,"fallidas":3,"tasaDeFallo":100.0}
HTTP 200 en 0.004526s  {"circuito":"OPEN",  "llamadasHttpReales":3,"fallidas":3,"tasaDeFallo":100.0}
HTTP 200 en 0.004316s  {"circuito":"OPEN",  "llamadasHttpReales":3,"fallidas":3,"tasaDeFallo":100.0}
HTTP 200 en 0.006239s  {"circuito":"OPEN",  "llamadasHttpReales":3,"fallidas":3,"tasaDeFallo":100.0}
```

y el cuerpo:

```json
{"id":1,"tipo":"DECLARACION_F29","estado":"EN_PROCESO","rutContribuyente":"11111111-1",
 "nombreContribuyente":null,"estadoDelNombre":"NO_DISPONIBLE","creadoEn":"..."}
```

**Los dos comportamientos, uno al lado del otro** — el mismo servicio caído, la misma petición:

| | paso 4 | paso 5 |
|---|---|---|
| Lo que recibe el usuario | **HTTP 500**, cuerpo vacío | **HTTP 200** con el trámite y el aviso |
| Llamadas a un servicio muerto | **1 por petición, siempre** | **3 en total**, y luego cero |

*(La primera petición tarda cien milésimas y las demás cinco: es el circuito cargándose por
primera vez. Pasa una sola vez por arranque y no tiene nada que ver con el patrón — pero sale en
pantalla, así que mejor nombrarlo que dejar que alguien lo interprete.)*

**Y ahora las dos cosas que hay que mirar dos veces:**

1. **`llamadasHttpReales` se queda en 3.** De la cuarta petición en adelante, trámites **no toca la
   red**. Sabe que contribuyentes está caído y deja de molestarlo. Cuando el caído está intentando
   arrancar, eso es la diferencia entre ayudarle y estorbarle.
2. **El tiempo casi no se movió** —de unas 6 milésimas a unas 5— y eso es honesto: un proceso muerto rechaza
   la conexión al instante, así que aquí no había espera que ahorrar. El circuito hoy no compró
   velocidad: compró **una respuesta útil** y **dejar en paz al vecino**. La velocidad la compra
   cuando el vecino está lento en vez de muerto, y eso ya se midió en el Lab 10.

**Y el tercer estado, que se ve gratis.** Se vuelve a arrancar contribuyentes y se espera un poco:

```
13:55:22.844 WARN [CIRCUITO] CLOSED -> OPEN
13:55:40.808 WARN [CIRCUITO] OPEN -> HALF_OPEN
13:55:40.831 WARN [CIRCUITO] HALF_OPEN -> CLOSED
```

Los tres estados, en tres líneas de log. **Nadie lo arregló a mano**: pasaron los diez segundos,
el circuito dejó pasar una petición de prueba, salió bien, y se cerró solo.

---

## Paso 6 · El gateway

**Se explica:** hasta ahora el alumno ha usado tres puertos distintos. Un cliente de verdad no
puede: no tiene por qué saber que los trámites están en el 8202 y los contribuyentes en el 8201, ni
enterarse el día que eso cambie. Y hay un problema peor: **¿quién valida el token?** Si lo valida
cada servicio, la misma configuración de seguridad está copiada tres veces y basta olvidarla en una
para dejar la puerta abierta.

Un **API gateway** es la recepción del edificio: una sola dirección pública, y dentro, quien haga
falta.

**Se pega:** en `practica/gateway/src/main/java/cl/dgt/gateway/enrutado/TablaDeRutas.java`,
**sustituyendo la línea del `TODO`**:

```java
        this.rutas = List.of(
                new Ruta("/contribuyentes", contribuyentes, "contribuyentes"),
                new Ruta("/tramites", tramites, "tramites"),
                new Ruta("/auditoria", auditoria, "auditoria"));
```

Tres líneas. **Eso es todo el enrutado**, y las tres URL vienen de `application.yml`, así que
cambiar un puerto es editar texto, no recompilar.

> Aquí es donde vivirían **Eureka** y el **Config Server**, y hoy no están: son dos servicios de
> infraestructura más, y en tres horas no caben. Lo que hacen se explica en el README. Lo que hay
> que saber ya es que un registro de servicios **siempre miente un poco** —entre que una instancia
> muere y alguien la tacha pasan segundos—, así que quien llama tiene que estar preparado para que
> la dirección que le dieron no conteste. El circuit breaker del paso 5 no está por si acaso: está
> porque la lista es falible por diseño.

**Se pega:** en `practica/gateway/src/main/java/cl/dgt/gateway/config/SeguridadConfig.java`,
**sustituyendo las dos líneas del otro `TODO`**:

```java
                .authorizeHttpRequests(rutas -> rutas
                        .requestMatchers("/auth/login", "/salud").permitAll()
                        .anyRequest().authenticated())
                .oauth2ResourceServer(oauth -> oauth.jwt(Customizer.withDefaults()))
```

(hace falta añadir `import org.springframework.security.config.Customizer;`)

**Se corre:** Ctrl+C y arrancar el gateway otra vez.

```bash
curl -s -o /dev/null -w "sin token -> HTTP %{http_code}\n" http://localhost:8200/tramites/1

TOKEN=$(curl -s -X POST http://localhost:8200/auth/login \
  -H 'Content-Type: application/json' \
  -d '{"usuario":"carolina","clave":"dgt2026"}' | sed 's/.*"token":"\([^"]*\)".*/\1/')

curl -s -H "Authorization: Bearer $TOKEN" http://localhost:8200/tramites/1
curl -s -H "Authorization: Bearer $TOKEN" http://localhost:8200/contribuyentes/11111111-1
```

**En consola:**

```
sin token -> HTTP 401

{"id":1,"tipo":"DECLARACION_F29","estado":"EN_PROCESO","rutContribuyente":"11111111-1",
 "nombreContribuyente":"Carolina Fuentes Aravena","estadoDelNombre":"OK","creadoEn":"..."}

{"rut":"11111111-1","nombre":"Carolina Fuentes Aravena","segmento":"PERSONA_NATURAL"}
```

**Un solo puerto.** Dos servicios distintos detrás, y el cliente no se enteró de cuál era cuál.

**Y ahora la pregunta incómoda**, que hay que hacer en clase porque el alumno la va a descubrir
solo en cuanto se le ocurra:

```bash
curl -s http://localhost:8202/tramites/1        # sin token, saltándose el gateway
```

Y sale el trámite. **El portero solo sirve si no hay otra puerta.** Hoy los tres servicios de atrás
no llevan Spring Security y confían en que nadie les habla directamente, lo cual es cierto mientras
estén atados a `localhost` en la máquina del alumno — y deja de serlo el día que se despliegan de
verdad. Las dos salidas reales son la red (que solo el gateway pueda alcanzarlos) o repetir la
validación en cada servicio. Lo que **no** es una salida es suponer que nadie lo va a intentar.

---

## Paso 7 · Seguir una petición por tres servicios

**Se explica:** el paso 6 dejó una petición que atraviesa gateway → trámites → contribuyentes. Si
mañana algo falla ahí dentro, hay que mirar **tres logs**, y en producción cada uno tiene miles de
líneas por minuto de miles de peticiones mezcladas. ¿Cómo se sabe **qué línea de allá** corresponde
a **esta línea de aquí**?

Con un **id de correlación**: un identificador que nace con la petición y viaja con ella.

Los cuatro servicios ya traen el filtro del Lab 11 (`infra/FiltroDeCorrelacion.java`): pone un id
en el MDC, y a partir de ahí toda línea de log lo lleva sin que nadie lo pase por parámetro. Y ya
respeta el que venga de fuera:

```java
        String id = peticion.getHeader(CABECERA);
        if (id == null || id.isBlank()) {
            id = UUID.randomUUID().toString().substring(0, 8);
        }
```

**Lo que falta es lo único que no puede ser automático: mandarlo en cada salto.** Un servicio no
puede adivinar el id del que le llamó; hay que ponérselo en la cabecera. Y hasta que no se ponga,
esto es lo que hay — las dos líneas del paso 3, miradas otra vez:

```
14:16:55.294 INFO [2cd88c14] TRAMITES       - [TRAMITES] pido la ficha de 11111111-1 a contribuyentes
14:16:55.333 INFO [1efa2ebb] CONTRIBUYENTES - [CONTRIBUYENTES] me piden la ficha de 11111111-1
```

**Es la misma petición y los ids son distintos.** Cada servicio se inventó el suyo, porque nadie le
mandó ninguno. Con dos líneas y cuarenta milésimas de diferencia todavía se adivina; con cuatro
servicios y mil peticiones por minuto, no.

**Se pega:** dos líneas, en dos archivos.

En `gateway/enrutado/Enrutador.java`, en el método `reenviar`:

```java
        RestClient.RequestBodySpec salida = http
                .method(HttpMethod.valueOf(peticion.getMethod()))
                .uri(uri)
                .header(FiltroDeCorrelacion.CABECERA, MDC.get(FiltroDeCorrelacion.CLAVE));
```

En `tramites/clientes/ClienteContribuyentes.java`, en `pedir`:

```java
        return http.get()
                .uri("/contribuyentes/{rut}", rut)
                .header(FiltroDeCorrelacion.CABECERA, MDC.get(FiltroDeCorrelacion.CLAVE))
                .retrieve()
                .body(FichaDto.class);
```

(y sus dos `import`: `cl.dgt.*.infra.FiltroDeCorrelacion` y `org.slf4j.MDC`)

**Se corre:** Ctrl+C y arrancar el gateway y trámites otra vez. Una sola petición:

```bash
curl -s -D - -o /dev/null -H "Authorization: Bearer $TOKEN" http://localhost:8200/tramites/1 | grep -i trace
```

```
X-Trace-Id: f1881d07
```

Y ahora se buscan esos ocho caracteres **en las tres terminales**:

```
14:18:48.470 INFO [f1881d07] GATEWAY        - [GATEWAY] GET /tramites/1 -> tramites
14:18:48.580 INFO [f1881d07] TRAMITES       - [TRAMITES] pido la ficha de 11111111-1 a contribuyentes
14:18:48.619 INFO [f1881d07] CONTRIBUYENTES - [CONTRIBUYENTES] me piden la ficha de 11111111-1
```

**Lo que hay que notar:**

> **Tres procesos, tres logs, un id.** Y en orden: la petición entró por la puerta a las .470, salió
> hacia contribuyentes a las .580, y llegó a las .619.

Sin esto, un sistema repartido **no es depurable**: es una caja negra con cuatro compartimentos que
no se pueden relacionar entre sí. Con esto, un `grep` reconstruye el viaje entero.

Y la advertencia que conviene dejar dicha: **la propagación es manual en cada salto**. La cabecera
que se te olvide poner es exactamente el eslabón que no vas a poder cruzar el día que importe.
Herramientas como OpenTelemetry lo hacen solas —y además miden cuánto tardó cada tramo—, pero el
mecanismo es este.

---

## Paso 8 · Consistencia eventual

**Se explica:** falta el cuarto servicio. Cada trámite nuevo tiene que quedar registrado en
auditoría. En el monolito esto era una línea más dentro del mismo `@Transactional`: o se guardaban
las dos cosas, o no se guardaba ninguna.

Repartido **no hay tal cosa**. No existe transacción que abarque dos bases de datos en dos
procesos. Hay que elegir, y las dos opciones se pagan:

| Si trámites **espera** a auditoría | Si trámites **no espera** |
|---|---|
| El usuario paga la lentitud de auditoría | El usuario no se entera de nada |
| Auditoría caída = **no se puede crear un trámite** | Auditoría caída = el trámite se crea y **el evento se pierde** |

Hoy se elige la segunda, que es lo que casi siempre se elige para una auditoría: registrar es
importante, pero no tanto como poder trabajar.

**Se pega:** archivo **nuevo**
`practica/tramites/src/main/java/cl/dgt/tramites/clientes/ClienteAuditoria.java` — lo importante son las
tres líneas del `Thread.ofVirtual()`:

```java
    public void avisarDeUnTramiteNuevo(Tramite tramite) {
        String traceId = MDC.get(FiltroDeCorrelacion.CLAVE);

        Thread.ofVirtual().name("aviso-auditoria").start(() -> {
            MDC.put(FiltroDeCorrelacion.CLAVE, traceId);
            try {
                http.post()
                        .uri("/auditoria/eventos")
                        .header(FiltroDeCorrelacion.CABECERA, traceId)
                        .body(Map.of("evento", "TRAMITE_CREADO",
                                "tramiteId", tramite.getId(),
                                "rutContribuyente", tramite.getRutContribuyente()))
                        .retrieve()
                        .toBodilessEntity();
                log.info("[TRAMITES] auditoría acusó recibo del trámite {}", tramite.getId());
            } catch (Exception e) {
                log.warn("[TRAMITES] auditoría no recibió el aviso del trámite {}: {}. "
                        + "El trámite queda creado igual.", tramite.getId(), e.getClass().getSimpleName());
            } finally {
                MDC.remove(FiltroDeCorrelacion.CLAVE);
            }
        });
    }
```

> **`String traceId = MDC.get(...)` va FUERA del hilo nuevo, y no es un detalle de estilo.** El MDC
> vive en el hilo de la petición; en el hilo nuevo está vacío. Copiarlo dentro daría siempre `null`
> y auditoría aparecería con un id distinto — o sea, el paso 7 roto sin que nadie se entere.

Y en `TramiteService.crear`, una línea, **después** de guardar:

```java
        auditoria.avisarDeUnTramiteNuevo(tramite);
        return conNombre(tramite);
```

**Se corre:** Ctrl+C, arrancar trámites, y crear un trámite.

```bash
curl -s -w "\n  el usuario esperó: %{time_total}s\n" -X POST http://localhost:8200/tramites \
  -H "Authorization: Bearer $TOKEN" -H 'Content-Type: application/json' \
  -d '{"rutContribuyente":"11111111-1","tipo":"DECLARACION_F29"}'
```

**En consola:**

```
{"id":3,...,"estado":"EN_PROCESO",...}
  el usuario esperó: 0.046069s
```

Y las tres líneas de log, mirando las dos terminales:

```
14:19:00.134 INFO [697ea585] TRAMITES  - [TRAMITES] trámite 3 creado para 11111111-1
14:19:00.156 INFO [697ea585] AUDITORIA - [AUDITORIA] llega el evento TRAMITE_CREADO del trámite 3 — procesando...
14:19:01.734 INFO [697ea585] AUDITORIA - [AUDITORIA] REGISTRADO id=1 del trámite 3
```

**Lo que hay que notar:** el POST entero le costó al usuario **0,046 segundos**. Trámites dio el
trámite por creado a las **00.134**, y auditoría terminó de registrarlo a las **01.734**: **un
segundo y medio más tarde**, con el usuario hace rato en otra pantalla.

> Durante ese segundo y medio, el trámite **existe** y su registro de auditoría **no**. El sistema
> estuvo inconsistente, a propósito, y nadie se rompió. Eso es **consistencia eventual**: no es que
> los datos cuadren siempre, es que **acaban** cuadrando.

*(La demora de auditoría es deliberada, para que se vea en clase: un `Thread.sleep(1500)` en su
controller, con su comentario. Sin ella el desfase serían milésimas y no se distinguiría.)*

**Y ahora la mitad incómoda.** Ctrl+C en la terminal de **auditoría**, y otro trámite:

```bash
curl -s -w "\n  el usuario esperó: %{time_total}s\n" -X POST http://localhost:8200/tramites \
  -H "Authorization: Bearer $TOKEN" -H 'Content-Type: application/json' \
  -d '{"rutContribuyente":"22222222-2","tipo":"TERMINO_GIRO"}'
```

```
{"id":4,"tipo":"TERMINO_GIRO","estado":"EN_PROCESO",...}
  el usuario esperó: 0.014389s
```

El trámite **se creó**, en catorce milésimas, con auditoría muerta. Y en el log de trámites:

```
WARN [3656b3d1] TRAMITES - [TRAMITES] auditoría no recibió el aviso del trámite 4:
                            ResourceAccessException. El trámite queda creado igual.
```

> **Ese evento se perdió.** No hay reintento, no hay cola, no hay nada que lo recupere. Ese trámite
> existe y no tiene registro de auditoría, y la única prueba de que eso pasó es una línea `WARN` en
> un log que nadie está mirando.

Se pregunta a la sala: **¿está bien?** Depende de qué sea la auditoría. Si es una estadística
interna, perfectamente. Si es el registro legal que un fiscalizador puede exigir, es un problema
grave, y la solución no es «reintentar tres veces»: es una **cola** —un servidor de mensajería que
guarda el evento hasta que auditoría pueda leerlo—, que necesita una pieza de infraestructura que
hoy no está.

Lo que no se puede es elegir esto sin saber que se está eligiendo.

---

## Cierre · volver a la pizarra

Se vuelve a la lista del paso 0, la que quedó escrita antes de arrancar nada, y se completa con lo
que se vio hoy.

**Lo que se gana:**

- **Equipos independientes.** El equipo de contribuyentes despliega sin avisar al de trámites. Con
  varios equipos pisándose en un mismo despliegue, esto es la ventaja entera — y es *organizativa*,
  no técnica.
- **Fallar por partes.** Auditoría se cayó y la DGT siguió emitiendo trámites. En el monolito, un
  fallo en el módulo de auditoría se llevaba el proceso entero.
- **Escalar por partes.** Si el 90 % de la carga es consultar el padrón, se levantan tres
  contribuyentes y uno de todo lo demás.
- **Fronteras que se sostienen solas.** Nadie va a colar un `JOIN` «temporal» entre trámites y
  contribuyentes, porque no hay tabla que juntar.

**Lo que se paga, y hoy se pagó de verdad:**

- **Latencia.** Un `findByRut()` de nanosegundos pasó a ser una llamada de red — treinta y nueve
  milésimas, y eso en `localhost`.
- **El fallo en cascada.** Un servicio caído devolvió **500** en otro que estaba sano, y hubo que
  escribir un circuit breaker para arreglar un problema que en el monolito **no existía**.
- **La consistencia.** Se acabaron las transacciones que abarcan todo. Hoy un evento **se perdió**.
- **Depurar.** Cuatro logs en vez de uno, y hubo que inventar un id para poder cruzarlos.
- **Operar.** Cuatro terminales, cuatro puertos, tres bases, un orden de arranque. Y esto son
  **cuatro** servicios: la lista de la compra de un sistema con cuarenta no cabe en esta pizarra.

**Y la parte que hay que decir aunque el tema haya sido el más votado del curso:**

> Todo lo que se escribió hoy —el cliente HTTP, el circuit breaker, la degradación marcada, el id de
> correlación, el aviso asíncrono— es código que **existe únicamente porque partimos el sistema**.
> No le entrega ni una función nueva al contribuyente. Es el peaje.
>
> Si tu equipo cabe en una mesa, si no sabes todavía dónde están las costuras, o si la razón es que
> «es lo moderno», **estás pagando ese peaje sin cobrar el viaje**. Y si además pones dos servicios
> contra una misma base de datos, tienes un **monolito distribuido**: todos los costos de hoy y
> ninguna de las ventajas.
>
> El consejo de casi todo el que ha hecho las dos cosas: **empieza por un monolito bien
> modularizado.** Es más fácil partir un monolito ordenado que juntar diez microservicios mal
> partidos. Los trece labs anteriores construyeron exactamente eso.

### Lo que siembra este lab — y cierra el curso

Este es el último laboratorio: lo que siembra no lo recoge otra sesión, lo recoge quien vuelva el
lunes a su trabajo.

En catorce sesiones se armó una aplicación que arranca, expone endpoints, guarda en una base de
datos, no se cae bajo concurrencia, está probada, está cerrada con llave, sobrevive a que el vecino
falle, hace su trabajo a tiempo, sale de la máquina donde nació — y hoy se partió en cuatro para
poder mirar de frente lo que eso cuesta.

Lo que queda por delante, y hay que nombrarlo para que nadie se vaya creyendo que el mapa está
completo: **el registro de servicios, la configuración centralizada, las colas, las sagas, las
trazas distribuidas y el orquestador que arranca todo esto sin cuatro terminales.** Están en la
sección «lo que no vimos hoy» del README, cada uno con la razón de por qué no cabía.

> **Lo que se lleva:** la arquitectura no es elegir la opción moderna. Es saber **qué estás pagando
> y a cambio de qué** — y ser capaz de decir que no.

Y la pregunta que conviene llevarse puesta, la misma de todo el curso, ahora con cuatro procesos
donde antes había uno:

> **¿Y esto qué pasa cuando se cae?** — preguntado **antes** de que se caiga.

---

## Si no caben los ocho pasos

Se recorta **por el final**, y se dice en voz alta lo que se está dejando fuera:

1. Primero el **paso 8** (consistencia eventual). Auditoría se queda arrancada y sin usar, y el
   cierre menciona el problema sin demostrarlo.
2. Después el **paso 7** (correlación). Se pierde poco: el mecanismo ya se enseñó en el Lab 11 y
   aquí solo se propaga.

**Los pasos 4 y 5 no se recortan nunca.** Sin esos dos, el laboratorio no enseña nada que no se
pueda leer en un blog.
