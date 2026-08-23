# Lab 14 · Microservicios

Durante trece laboratorios hubo **una** aplicación: un puerto, una base, un log, un `pom.xml`.

Hoy la misma DGT son **cuatro programas distintos**, en cuatro terminales, con **tres bases de
datos separadas**. La pregunta del día no es cómo se parte —eso lo hace el framework— sino **qué
se gana y qué se paga** al partirlo.

Un aviso que va en serio, y que es la mitad del laboratorio:

> Un sistema distribuido no es una aplicación mejor. Es una aplicación **distinta**, con problemas
> que la anterior no tenía y que no se resuelven programando mejor.

---

## El mapa

```
                       POSTMAN / curl
                            │ :8200
                            ▼
                  ┌──────────────────┐
                  │     GATEWAY      │   enruta · valida el JWT · circuit breaker
                  │      :8200       │   (sin base de datos: no es dueño de nada)
                  └───┬──────────┬───┘
               :8201  │          │  :8202
             ┌────────▼─────┐  ┌─▼──────────────┐
             │CONTRIBUYENTES│◄─┤    TRÁMITES    │   la flecha es HTTP, no un JOIN
             │  PG :55450   │  │   PG :55451    │
             └──────────────┘  └───────┬────────┘
                                       │ evento (no espera respuesta)
                               ┌───────▼────────┐
                               │   AUDITORÍA    │  :8203
                               │   PG :55452    │
                               └────────────────┘
```

**Tres bases distintas, una por servicio, y ese es el punto.** Trámites guarda el RUT del
contribuyente y nada más. El nombre vive en la otra base, en otro proceso, en otro puerto. Trámites
**no puede** hacer un JOIN para traerlo: tiene que preguntarle por HTTP. Esa imposibilidad es la
lección del día.

---

## Los dos números del laboratorio

Contribuyentes se apaga, y se pide un trámite que **está entero en la base de trámites**:

| | respuesta al usuario | llamadas HTTP a un servicio muerto |
|---|---|---|
| **Paso 4** — sin protección | **HTTP 500**, cuerpo vacío, en 0,006–0,019 s | 1 por cada petición, para siempre |
| **Paso 5** — con circuit breaker | **HTTP 200** con el trámite y `"estadoDelNombre": "NO_DISPONIBLE"` | 3, y después **el contador se congela** |

Las dos filas son el arco del día, y ninguna de las dos es sobre velocidad:

- **500 → 200**: un dato ausente dejó de tumbar la petición entera. El trámite —que estaba ahí,
  intacto, en su propia base— vuelve a llegarle al usuario.
- **el contador congelado**: a partir de la cuarta petición, trámites **deja de llamar** a un
  servicio que está caído. No le ayuda a levantarse tirándole peticiones encima.

> Un proceso muerto rechaza la conexión al instante, así que aquí el circuito no ahorra tiempo:
> ahorra **errores en la cara del usuario** y **presión sobre el que está caído**. El caso caro —el
> vecino que no se cae sino que se pone **lento**— se midió en el Lab 10: 30,01 s → 0,002 s. Los
> dos son reales y son distintos.

---

## Qué se aprende

- Que **una base por servicio** no es una convención de estilo: es lo que hace imposible el JOIN y
  obligatoria la llamada de red.
- Que un servicio caído **se lleva por delante** al que lo llama, aunque el dato pedido no fuera
  suyo — el fallo en cascada, medido.
- Que un **circuit breaker** convierte «no puedo responder» en «respondo con menos», y que **qué
  responder cuando no hay respuesta es una decisión de negocio**, no un detalle técnico.
- Qué hace de verdad un **API gateway**: una sola puerta, el enrutado por ruta y el token validado
  una vez en vez de tres.
- Que sin un **id de correlación** un sistema repartido no es depurable, y que la propagación es
  **manual en cada salto**: la cabecera que no pones es el log que no vas a poder cruzar.
- Que entre dos servicios **no hay transacción**: hay consistencia eventual, y hay que elegir qué
  pasa cuando el segundo no contesta.
- Y **cuándo NO** partir un sistema, que es probablemente lo más útil que te llevas.

---

## Cuatro terminales · el orden de arranque

Esta es la mayor fricción del laboratorio, y no se puede esconder: **son cuatro terminales
abiertas a la vez**, cada una con su servicio, su puerto y su base.

```bash
# terminal 1                        # terminal 2
cd practica/contribuyentes          cd practica/tramites
./mvnw spring-boot:run              ./mvnw spring-boot:run

# terminal 3                        # terminal 4
cd practica/auditoria               cd practica/gateway
./mvnw spring-boot:run              ./mvnw spring-boot:run
```

**El orden es contribuyentes → trámites → auditoría → gateway**: de dentro hacia fuera, los
proveedores antes que sus clientes.

### ¿Y si se arranca al revés?

Nada explota, y conviene decirlo con precisión porque la respuesta enseña algo. Los cuatro
servicios **arrancan igual**: ninguno busca a los demás al encender, porque las direcciones se
resuelven en cada petición, no en el arranque.

Lo que pasa es esto, medido:

```
13:55:22.844  WARN  [CIRCUITO] CLOSED -> OPEN         <- tres fallos mientras el otro arrancaba
13:55:40.808  WARN  [CIRCUITO] OPEN -> HALF_OPEN
13:55:40.831  WARN  [CIRCUITO] HALF_OPEN -> CLOSED    <- se curó solo, 18 s después
```

Las primeras peticiones salen degradadas, el circuito se abre **por fallos que solo ocurrieron
durante el arranque**, y se cierra solo en cuanto vuelve a probar. Aquí dura 18 segundos y no pasa
nada.

**En producción esos 18 segundos son el problema entero**: un sistema completamente sano, recién
desplegado, respondiendo degradado a todo el mundo porque aceptó tráfico antes de que sus vecinos
estuvieran listos. La solución no es tocar el circuit breaker —el circuit breaker hizo justo lo que
se le pidió— sino **no aceptar tráfico hasta estar listo**: es la diferencia entre *liveness* y
*readiness* del Lab 11, ahora con consecuencias.

### Y las guardas de puerto

Los tres servicios con base traen las guardas de los labs anteriores (SPEC-FIX-07 y -08), y **aquí
es donde más falta hacen**: con cuatro terminales, arrancar el mismo servicio dos veces por
equivocación es lo más fácil del mundo. Si pasa, el programa lo dice con el nombre del servicio, el
puerto y el comando exacto para arreglarlo, en vez de morir bajo cinco excepciones anidadas.

---

## Los puertos

| servicio | `practica/` | `solucion/` | base de datos (práctica / solución) |
|---|---|---|---|
| **gateway** | **8200** | 8210 | — |
| contribuyentes | 8201 | 8211 | 55450 / 55460 |
| trámites | 8202 | 8212 | 55451 / 55461 |
| auditoría | 8203 | 8213 | 55452 / 55462 |

`practica/` y `solucion/` pueden correr a la vez — pero son ocho procesos, así que probablemente no
quieras.

---

## Los endpoints

Todo entra por el **8200**. El cliente no sabe que hay tres servicios detrás:

```
POST /auth/login                    usuario y clave -> token JWT   (la única ruta abierta)
GET  /salud                         ¿está viva la puerta?

GET  /tramites                      los trámites, con el nombre traído por HTTP
GET  /tramites/{id}                 uno
POST /tramites                      crea uno, y avisa a auditoría sin esperarla
GET  /tramites/estado-circuito      el tablero del paso 5

GET  /contribuyentes                el padrón
GET  /contribuyentes/{rut}          una ficha

GET  /auditoria/eventos             lo que auditoría alcanzó a registrar
```

Y cada servicio con base sabe decir qué hay dentro de la suya — es lo que se mira en el paso 2, y
va contra su propio puerto, no contra el gateway:

```
GET  :8201/mi-base    ->  {"servicio":"contribuyentes","tablas":["contribuyente", ...]}
GET  :8202/mi-base    ->  {"servicio":"tramites",      "tablas":["tramite", ...]}
GET  :8203/mi-base    ->  {"servicio":"auditoria",     "tablas":["registro_de_auditoria", ...]}
```

Los dos usuarios: `carolina` / `dgt2026` (FUNCIONARIO) y `jefatura` / `dgt2026` (ADMIN).

```bash
TOKEN=$(curl -s -X POST http://localhost:8200/auth/login \
  -H 'Content-Type: application/json' \
  -d '{"usuario":"carolina","clave":"dgt2026"}' | sed 's/.*"token":"\([^"]*\)".*/\1/')

curl -H "Authorization: Bearer $TOKEN" http://localhost:8200/tramites/1
```

---

## Las tres carpetas

| | |
|---|---|
| **`practica/`** | Donde trabajas. Contribuyentes y auditoría llegan enteros; **trámites no sabe llamar a nadie** y el **gateway no lleva a ninguna parte** |
| **`solucion/`** | Los cuatro completos: el cliente HTTP, el circuito, la tabla de rutas, el JWT en la puerta, la correlación y el aviso a auditoría |
| **`instructor/`** | Los mismos archivos explicados línea por línea. **No viaja en el repositorio** |

---

> **`entities/` y `models/` no son lo mismo, y por eso no se llaman igual.** Cada clase de
> `entities/` está **mapeada a una tabla**: lo que se le hace al objeto termina en la base. Los
> `models/` de los labs 02, 03 y 08 son lo contrario — objetos que viven en memoria, sin tabla
> detrás. El nombre distinto es deliberado: dice de un vistazo si hay una fila al otro lado.

## Lo que este laboratorio NO trae, y por qué

Esta sección importa tanto como el código: si te vas creyendo que ya viste microservicios, el
laboratorio te hizo un flaco favor.

- **Eureka (service discovery) y Config Server.** Son **dos servicios de infraestructura más** que
  habría que arrancar, configurar y explicar en una sesión que ya tiene cuatro procesos. Aquí las
  direcciones son tres URLs en `application.yml` — que además es lo que hacen muchísimos sistemas
  reales. Lo que se pierde: cuando las instancias son N y cambian solas, una lista escrita a mano
  deja de servir. **Y hay una trampa que conviene saber ya**: un registro siempre **miente** un
  poco —entre que una instancia muere y alguien la tacha pasan segundos— así que todo lo que llame
  a otro servicio tiene que estar preparado para que la dirección que le dieron no conteste. El
  circuit breaker no está por si acaso: está porque la lista *es* falible por diseño.
- **Spring Cloud Gateway.** El gateway de este laboratorio está **escrito a mano**, y son unas 90
  líneas que puedes leer enteras en `enrutado/Enrutador.java`. Se hizo así por dos razones: el tren
  de Spring Cloud vigente está compilado contra una versión de Spring Boot distinta de la del curso,
  y traerlo significaba meter una dependencia grande en la maleta para esconder detrás de
  configuración justo lo que hoy queremos mirar. En un proyecto de verdad se usa el producto
  —trae filtros, reintentos, límites de tasa y mucho más—, y el artefacto que hay que buscar se
  llama `spring-cloud-starter-gateway-server-webmvc` (el nombre viejo, `spring-cloud-starter-gateway`,
  murió; con él no arranca ni una ruta y **no da ningún error**: todo 404).
- **gRPC.** El temario lo promete y aquí no está: su compilador de `.proto` no viaja en la maleta y
  no funciona sin red. Queda declarado como brecha en `docs/temario/MAPA-LAB-MODULO.md`.
- **Mensajería (colas).** Cuando auditoría está caída, el evento de hoy **se pierde** — y lo verás
  pasar en el paso 8. Que no se pierda es exactamente lo que resuelve una cola, y una cola necesita
  un servidor que la sala del SII no puede instalar.
- **Sagas y compensaciones.** Aquí solo hay dos servicios y el segundo es opcional. Cuando los dos
  son obligatorios —«emitir el folio **y** cobrar»— hace falta una saga: pasos que se deshacen a
  mano, porque no hay `rollback` que cruce dos bases.
- **Trazas distribuidas de verdad** (OpenTelemetry): el `X-Trace-Id` del paso 7 es el 20 % que da
  el 80 %. Lo que falta es el árbol de tiempos que dice **cuál** de los tres saltos fue el lento.
- **Balanceo entre N instancias**, **cliente declarativo** (`@HttpExchange`, Feign) y **tests de
  contrato** entre servicios.

---

## Cuándo NO partir un sistema

El cierre del laboratorio, y no es una nota al pie:

- **Si tu equipo cabe en una mesa.** La ventaja de microservicios es *organizativa*: equipos que
  despliegan sin pedirse permiso. Con seis personas no hay permiso que pedir, y te quedas con todos
  los costos y ninguna ventaja.
- **Si no sabes dónde están las costuras.** Cada llamada que cruza una frontera mal puesta es una
  llamada de red que debería haber sido un método.
- **Si necesitas transacciones entre las partes.** Trivial en un monolito; repartido son sagas,
  compensaciones y estados intermedios visibles.
- **Si no tienes observabilidad.** Sin logs correlacionados, cuatro procesos son una caja negra con
  cuatro compartimentos. Si vas a partir, **primero** la observabilidad.
- **Si la razón es «es lo moderno».** Es la peor de todas, y la más frecuente.

Y el consejo de casi todo el que ha hecho las dos cosas: **empieza por un monolito bien
modularizado.** Es más fácil partir un monolito ordenado que juntar diez microservicios mal
partidos. Los trece labs anteriores construyeron justo eso.

---

## El guion

`PASOS.md` — los ocho pasos de la sesión, con lo que sale en cada consola.
