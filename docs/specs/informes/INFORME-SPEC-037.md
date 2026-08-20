# INFORME-SPEC-037 · Lab 14 · Microservicios — el sistema repartido

**Ejecuta:** mocito · **Rama:** `spec-037-lab-14-microservicios` · **Fecha:** 20 de agosto de 2026
**Estado:** ejecutado y verificado por el ejecutor. Pendiente la fila de aceptación del PO.

---

## 0 · Resumen en diez líneas

El lab existe: `labs/lab-14-microservicios/`, con `README.md`, `PASOS.md` y las tres carpetas.
**Cuatro servicios, cuatro procesos, tres bases de datos, cero Docker.** Los ocho pasos de la SPEC
están, en su orden, y los dos que la SPEC llama el corazón —el 4 y el 5— están medidos con su
salida citada.

Se recuperó la teoría del `lab-14-la-dgt-se-parte-en-pedazos` desde `material-v0.8.0` y **se leyó
entera antes de diseñar nada**, como pedía la SPEC. No fue un trámite: de ahí salen el monolito
distribuido, la tabla de las cuatro degradaciones posibles, la advertencia de que un registro
siempre miente, el circuit breaker decorativo de las cien llamadas, y el envenenamiento de la
ventana en frío. Los cinco están en el material nuevo.

**El lab no añadió ni un artefacto a `repo-maven/`.** Cero descargas, y pesa **644 KB** en el clon.

**Dos desviaciones, las dos declaradas abajo:** el gateway está escrito a mano en vez de usar
Spring Cloud Gateway (§2), y el paso 2 llega al mismo sitio por otro camino (§3). gRPC **no
entra** (§4).

---

## 1 · La arquitectura final — sin cambios respecto a la SPEC

El diagrama cerrado de la SPEC §1 se implementó tal cual. Cuatro servicios, tres bases, las mismas
flechas y los mismos puertos.

```
                       POSTMAN / curl
                            │ :8200
                            ▼
                  ┌──────────────────┐
                  │     GATEWAY      │   enruta · valida el JWT · circuit breaker
                  │      :8200       │   (sin base de datos)
                  └───┬──────────┬───┘
               :8201  │          │  :8202
             ┌────────▼─────┐  ┌─▼──────────────┐
             │CONTRIBUYENTES│◄─┤    TRÁMITES    │   HTTP, no un JOIN
             │  PG :55450   │  │   PG :55451    │
             └──────────────┘  └───────┬────────┘
                                       │ evento (no espera respuesta)
                               ┌───────▼────────┐
                               │   AUDITORÍA    │  :8203
                               │   PG :55452    │
                               └────────────────┘
```

`solucion/` usa 8210-8213 y 55460-55462 para poder convivir con `practica/`.

**No se fundió auditoría dentro de trámites.** La SPEC §2 pedía reportarlo si arrancar cuatro
procesos resultaba demasiado. Se midió y no lo es: **8,6 s** para los cuatro en frío. Los cuatro se
quedan (§6, V8).

---

## 2 · Desviación 1 · el gateway está escrito a mano

**Qué dice la SPEC:** el gateway enruta, valida el JWT y lleva circuit breaker. No nombra
tecnología.

**Qué se hizo:** un `@RestController` con `@RequestMapping("/**")` que mira la ruta, elige el
destino en una tabla de tres líneas y reenvía los bytes. Noventa líneas, en
`gateway/enrutado/Enrutador.java`. **No se usó Spring Cloud Gateway**, y las razones se midieron
antes de decidir:

1. **Compatibilidad.** El curso está sobre **Spring Boot 4.1.0**. El tren vigente de Spring Cloud
   —2025.1.3, con `spring-cloud-starter-gateway-server-webmvc` 5.0.3— declara
   `<spring-boot.version>4.0.8</spring-boot.version>`. Es un desfase de versión menor sobre el
   framework entero del curso, y es **exactamente la clase de riesgo que este material ya rechazó
   una vez**: la SPEC-018 §9 descartó el starter de Resilience4j por lo mismo y se quedó con el
   núcleo. La decisión de hoy es la misma decisión.
2. **La maleta.** Habría que capturar el tren entero en `repo-maven/`. Tal como quedó, el lab suma
   **cero artefactos**.
3. **Pedagogía.** Con el producto, la tabla de rutas es un bloque de YAML y el mecanismo queda
   escondido. Escrito a mano, el alumno abre un archivo y lo lee entero. La SPEC §1 justifica dejar
   fuera Eureka y Config Server porque el alumno «solo los configuraría sin entender»; el mismo
   argumento aplica.

**Qué se pierde, y está dicho en el README y en `instructor/`:** filtros, reintentos, límites de
tasa y el resto del producto. Y se conserva explícitamente la trampa de nombres que traía la teoría
vieja, porque es cara y sigue vigente: el artefacto **ya no se llama** `spring-cloud-starter-gateway`
—ese nombre murió en la serie 4.3.x—, y con el prefijo de propiedades antiguo **el gateway arranca
sin ninguna ruta y sin dar ningún error**: todo 404.

---

## 3 · Desviación 2 · el paso 2 llega al mismo sitio por otro camino

**Qué dice la SPEC:** «Pedir un trámite y ver que trae el nombre del contribuyente. Mostrar en el
código que no hay JOIN: hay una llamada HTTP. Y en las bases, que la tabla del otro no existe.»

**El problema:** el paso 3 es *escribir* esa llamada HTTP. Si en el paso 2 el nombre ya viene, el
paso 3 no tiene nada que resolver.

**Qué se hizo:** el paso 2 muestra la frontera **por ausencia**. Se pide el trámite y el nombre
**no está** (`"estadoDelNombre": "NO_CONSULTADO"`); alguien propone el JOIN —y es la respuesta
correcta después de trece sesiones de monolito—; y ahí se enseña que no hay tabla que juntar. El
paso 3 escribe la llamada y el nombre aparece. La lección de la SPEC —«esa imposibilidad es la
lección»— queda intacta, y además queda con el crimen antes que la solución, que es como el
manifiesto pide construir un paso.

**Un hallazgo del terreno que obligó a resolverlo bien:** la primera versión del paso 2 pedía
correr un `SELECT ... JOIN` contra la base de trámites para ver el
`relation "contribuyente" does not exist`. **No es ejecutable**: el PostgreSQL embebido de Zonky
extrae solo `initdb`, `pg_ctl` y `postgres` — **no trae `psql`**. El alumno no tiene cliente SQL. Se
comprobó y se sustituyó por un endpoint que sí viaja en la maleta:

```
GET :8201/mi-base  ->  {"servicio":"contribuyentes","tablas":["contribuyente","flyway_schema_history"]}
GET :8202/mi-base  ->  {"servicio":"tramites",      "tablas":["flyway_schema_history","tramite"]}
GET :8203/mi-base  ->  {"servicio":"auditoria",     "tablas":["flyway_schema_history","registro_de_auditoria"]}
```

Veinte líneas por servicio, leyendo `information_schema.tables`. La frontera deja de ser una
afirmación del relator y pasa a ser una salida en pantalla.

---

## 4 · gRPC · NO entra, y por qué

La SPEC §4 lo condicionaba a dos cosas: que el mocito lo hubiera recomendado en la SPEC-036, y que
el tooling de protobuf funcionara offline.

**Sobre lo primero: no hay SPEC-036 en el repositorio** (`docs/specs/` va de la SPEC-035 a las
SPEC-FIX) ni informe suyo, así que no hay tal recomendación que honrar. Se decidió por la segunda
condición, que es medible.

**Sobre lo segundo: no funciona offline.** Medido en `repo-maven/`:

```
io/grpc/          ->  solo grpc-bom (un .pom) y grpc-context 1.27.2
                      (un residuo transitivo de opencensus, no una dependencia de nadie)
com/google/protobuf/ -> solo protobuf-bom
protoc            ->  0 archivos. No hay compilador.
```

No hay runtime de gRPC, no hay `protobuf-maven-plugin`, y sobre todo **no hay `protoc`**, que viaja
como binario **por plataforma** (`protoc-osx-aarch_64.exe`, `protoc-windows-x86_64.exe`). Traerlo
significaría capturar la pila completa más dos ejecutables nativos, para un paso que además no cabe
en tres horas.

**Queda declarado como brecha** en `docs/temario/MAPA-LAB-MODULO.md` (tema XVII, M10), donde ya
estaba, con la razón actualizada.

---

## 5 · Los pasos y su duración

Estimación del ejecutor sobre el tecleo real medido y el tiempo de explicación. **Suma 2 h 55 min**;
el guion del recorte está al final de `PASOS.md`.

| paso | qué | teclea | min |
|---|---|---|---|
| 0 | El monolito que teníamos · la apuesta en la pizarra | no | 10 |
| 1 | Levantar el sistema · cuatro terminales | no | 15 |
| 2 | Una base por servicio · el JOIN imposible | no | 20 |
| 3 | La llamada entre servicios | **~35 líneas** | 30 |
| **4** | **Matar un servicio · el fallo en cascada** | no | **15** |
| **5** | **Circuit breaker y degradación** | **~35 líneas** | **35** |
| 6 | El gateway · una puerta, un token | ~10 líneas | 20 |
| 7 | Correlation ID por tres servicios | ~6 líneas | 15 |
| 8 | Consistencia eventual | ~25 líneas | 20 |
| — | Cierre · volver a la pizarra del paso 0 | no | 15 |

**~110 líneas de tecleo en total**, casi todo en dos archivos de trámites. Contribuyentes y
auditoría llegan enteros y no se tocan: es lo que hace que quepa.

**Riesgo de tiempo, y hay que decirlo:** el paso 1 son cuatro terminales y es la mayor fricción
operativa del lab. Los 15 minutos asumen que el relator las tiene abiertas y colocadas **antes** de
que entre la gente (está en `instructor/LEEME.md`). Si se abren en vivo, se van 10 minutos y hay que
recortar el paso 8.

---

## 6 · Verificación

Todo lo de abajo salió de **una sola sesión de verificación** sobre el código final, en un Mac
Apple Silicon. La bitácora completa está en el cuerpo de este informe; los comandos son los de
`PASOS.md`.

### V1 · los cuatro de `practica/` arrancan en su estado de entrega — **CUMPLE**

En frío, sin `target/` y sin bases creadas:

```
ARRANQUE EN FRIO de los cuatro: 8.6 s
:8200 -> {"servicio":"gateway","estado":"vivo"}
:8201 -> {"estado":"vivo","servicio":"contribuyentes"}
:8202 -> {"estado":"vivo","servicio":"tramites"}
:8203 -> {"servicio":"auditoria","estado":"vivo"}

CONTRIBUYENTES - Database: jdbc:postgresql://localhost:55450/postgres (PostgreSQL 16.14)
AUDITORIA      - Database: jdbc:postgresql://localhost:55452/postgres (PostgreSQL 16.14)
TRAMITES       - Database: jdbc:postgresql://localhost:55451/postgres (PostgreSQL 16.14)
```

Tres motores, tres puertos, tres esquemas.

### V2 · el sistema completo, flujo de punta a punta por el gateway — **CUMPLE**

```
GET :8200/tramites/1 SIN token            -> HTTP 401
POST :8200/auth/login                     -> token emitido (286 caracteres)
GET :8200/tramites/1 CON token            -> {"id":1,"tipo":"DECLARACION_F29","estado":"EN_PROCESO",
   "rutContribuyente":"11111111-1","nombreContribuyente":"Carolina Fuentes Aravena",
   "estadoDelNombre":"OK","creadoEn":"..."}
GET :8200/contribuyentes/11111111-1       -> {"rut":"11111111-1","nombre":"Carolina Fuentes Aravena",
   "segmento":"PERSONA_NATURAL"}
```

Un solo puerto de entrada; el nombre del contribuyente llegó desde otro proceso y otra base.

### V3 · paso 4 · el fallo en cascada, medido — **CUMPLE** ★

Contribuyentes apagado. Trámites **sano**, con el trámite entero en su propia base:

```
comprobacion: :8201/salud -> HTTP 000  (contribuyentes apagado)
peticion 1: HTTP 500  en 0.018616s
peticion 2: HTTP 500  en 0.007050s
peticion 3: HTTP 500  en 0.006007s

cuerpo: {"timestamp":"2026-08-20T18:17:14.987Z","status":500,
         "error":"Internal Server Error","path":"/tramites/1"}

log:    ERROR TRAMITES - ... threw exception [Request processing failed:
        org.springframework.web.client.ResourceAccessException ...]
```

### V4 · paso 5 · el mismo caso con circuit breaker — **CUMPLE** ★

Mismo escenario exacto, contribuyentes sigue apagado:

```
peticion 1: HTTP 200 en 0.106026s  {"circuito":"CLOSED","llamadasHttpReales":1,"fallidas":1}
peticion 2: HTTP 200 en 0.006448s  {"circuito":"CLOSED","llamadasHttpReales":2,"fallidas":2}
peticion 3: HTTP 200 en 0.007348s  {"circuito":"OPEN",  "llamadasHttpReales":3,"fallidas":3,"tasaDeFallo":100.0}
peticion 4: HTTP 200 en 0.004526s  {"circuito":"OPEN",  "llamadasHttpReales":3,...}
peticion 5: HTTP 200 en 0.004316s  {"circuito":"OPEN",  "llamadasHttpReales":3,...}
peticion 6: HTTP 200 en 0.006239s  {"circuito":"OPEN",  "llamadasHttpReales":3,...}

cuerpo: {"id":1,...,"nombreContribuyente":null,"estadoDelNombre":"NO_DISPONIBLE",...}
```

**Los dos comportamientos, citados juntos:**

| | V3 · sin protección | V4 · con circuit breaker |
|---|---|---|
| Lo que recibe el usuario | **HTTP 500**, cuerpo vacío | **HTTP 200** con el trámite y `NO_DISPONIBLE` |
| Llamadas a un servicio muerto | **1 por petición, indefinidamente** | **3 en total**, y luego **cero** |

**Y la honestidad que exige la regla A-02, dicha en el README, en `PASOS.md` y en `instructor/`:**
el tiempo **casi no se movió** (de ~6 ms a ~5 ms). No es un fallo del lab: un proceso muerto rechaza
la conexión al instante, así que **aquí no había espera que ahorrar**. Lo que compró el circuito son
dos cosas distintas y medibles: una respuesta útil en vez de un error, y **un contador de llamadas
que deja de subir**. El caso donde el circuito sí compra tiempo —el vecino **lento** en vez de
muerto— ya está medido en el Lab 10 (**30,01 s → 0,002 s**) y se cita allí. No se infló ningún
número para que la tabla quedara más bonita.

### V5 · paso 7 · el mismo correlation id en los tres logs — **CUMPLE**

```
X-Trace-Id devuelto por el gateway: f1881d07

14:18:48.470 INFO [f1881d07] GATEWAY        - [GATEWAY] GET /tramites/1 -> tramites
14:18:48.580 INFO [f1881d07] TRAMITES       - [TRAMITES] pido la ficha de 11111111-1 a contribuyentes
14:18:48.619 INFO [f1881d07] CONTRIBUYENTES - [CONTRIBUYENTES] me piden la ficha de 11111111-1
```

Tres procesos, un id, y en orden causal. **Y el «antes» también quedó medido** y está en `PASOS.md`:
sin la propagación, la misma petición sale con `2cd88c14` en trámites y `1efa2ebb` en
contribuyentes.

### V6 · paso 8 · trámite creado con auditoría caída — **CUMPLE**

Primero, con auditoría **viva**, el desfase:

```
POST :8200/tramites -> {"id":34,...}
  el usuario espero: 0.046069s

14:19:00.134 INFO [697ea585] TRAMITES  - trámite 34 creado para 11111111-1
14:19:00.156 INFO [697ea585] AUDITORIA - llega el evento TRAMITE_CREADO del trámite 34 — procesando...
14:19:01.734 INFO [697ea585] AUDITORIA - REGISTRADO id=1 del trámite 34

base de auditoría: [{"id":1,"evento":"TRAMITE_CREADO","tramiteId":34,
                    "traceId":"697ea585","recibidoEn":"2026-08-20T18:19:01.668055Z"}]
```

**1,53 segundos** entre que el trámite existe y que auditoría lo registra — con el usuario hace rato
en otra pantalla. *(La demora es deliberada: `Thread.sleep(1500)` comentado como tal en las tres
carpetas. Sin ella el desfase serían milésimas y no se vería en clase.)*

Y con auditoría **apagada**:

```
comprobacion: :8203/salud -> HTTP 000
POST :8200/tramites -> {"id":35,"tipo":"TERMINO_GIRO","estado":"EN_PROCESO",...}
  el usuario espero: 0.014389s

14:19:25.051 WARN [3656b3d1] TRAMITES - auditoría no recibió el aviso del trámite 35:
                                        ResourceAccessException. El trámite queda creado igual.

releído de la base: {"id":35,"tipo":"TERMINO_GIRO","estado":"EN_PROCESO",...}
```

El trámite se creó en 14 ms y **el evento se perdió**. Está dicho así, con esas palabras, en
`PASOS.md`.

### V7 · seguir `PASOS.md` completo sobre `practica/` — **CUMPLE**

Se aplicaron los pasos 3 a 8 sobre `practica/` copiando **exactamente** los bloques de código de
`PASOS.md`, y se comparó contra `solucion/` archivo por archivo (sin comentarios, sin líneas en
blanco, normalizando puertos y sufijos):

```
archivos IDENTICOS: 48     archivos que DIFIEREN: 0
```

Siguiendo el guion, `practica/` llega **exactamente** al sistema de `solucion/`.

> Esto verifica que el guion es **completo y correcto**. Lo que sigue sin verificar, por definición,
> es si se **entiende**: eso es la fila de aceptación del PO.

### V8 · memoria y tiempo con los cuatro arriba — **REPORTADO · viable**

```
ARRANQUE EN FRIO de los cuatro: 8,6 s

  4 proc · JVM de la aplicacion (4 servicios)           954 MB
  4 proc · JVM de Maven (el ./mvnw que los lanza)      1301 MB
  3 proc · PostgreSQL: los 3 postmaster                  91 MB
 45 proc · PostgreSQL: procesos auxiliares              349 MB
 -------------------------------------------------------------
     SUMA DE RSS con el sistema completo arriba:      2695 MB
```

**Método y su límite, dicho:** es suma de RSS. Los 45 procesos auxiliares de PostgreSQL comparten
páginas entre sí, así que esos 349 MB **están sobreestimados**; el consumo real del sistema está
por debajo de la suma. Se intentó medirlo por diferencia de memoria del sistema (`vm_stat` antes y
después) y **se descartó por ruidoso**: en una máquina en uso, el ruido de fondo era del mismo orden
que lo que se quería medir. Se reporta el método que sí es reproducible.

**Veredicto: es viable en una máquina de alumno**, y con un matiz que conviene que el PO tenga:

> **Los cuatro procesos de Maven pesan MÁS que las cuatro aplicaciones** — 1301 MB contra 954 MB.
> `./mvnw spring-boot:run` mantiene vivo un JVM de Maven por servicio, solo para haber lanzado el
> otro. En un lab de un servicio eso es invisible; con cuatro es el mayor consumidor del laboratorio.

En una máquina justa de memoria ese es el primer sitio donde mirar, y la salida ya la enseña el Lab
13: construir el jar y arrancar con `java -jar`. **No se midió esa alternativa** —los `pom.xml` del
arco desactivan `repackage`, igual que en los otros trece labs, así que habría que cambiarlos— y por
eso se deja como observación para el PO y no como recomendación verificada.

### V9 · offline · `instructor/` invisible · tamaño — **CUMPLE**

```
[OK] practica/auditoria      [OK] solucion/auditoria
[OK] practica/contribuyentes [OK] solucion/contribuyentes
[OK] practica/gateway        [OK] solucion/gateway
[OK] practica/tramites       [OK] solucion/tramites
proyectos: 8

archivos en repo-maven: antes=2995  despues=2995  DESCARGAS=0
```

**Cero descargas y `repo-maven/` sin tocar.** El lab no añadió ni un artefacto: todo lo que usa
—`web`, `data-jpa`, `flyway`, `postgresql`, Zonky, `security`, `oauth2-resource-server` y
`resilience4j-circuitbreaker`— ya estaba desde los labs 09, 10 y 11.

`instructor/` invisible para git:

```
$ git check-ignore -v labs/lab-14-microservicios/instructor/LEEME.md
.gitignore:59:labs/*/instructor/	labs/lab-14-microservicios/instructor/LEEME.md
```

Tamaño:

```
316K  practica/       328K  solucion/       (260K  instructor/, que no viaja)
lo que entra al repositorio: 644 KB en 136 archivos
```

De esos 136, **24 son los `mvnw`, `mvnw.cmd` y `maven-wrapper.properties`** de los ocho proyectos:
el precio de que cada servicio sea un proyecto independiente, que es justamente lo que se está
enseñando.

**Nota sobre el CI:** los cuatro `pom.xml` de `instructor/` dan error si se compilan, porque
`instructor/` **no es un proyecto** (no tiene `mvnw` ni `.mvn`, por diseño y por regla de la casa).
El job `labs` ya los excluye con `-not -path '*/instructor/*'`, así que no hace falta tocarlo. Se
verificó con el filtro exacto del CI: **8 proyectos, 8 OK**.

---

## 7 · Un defecto real encontrado y corregido durante la construcción

Merece su sección porque no salió de razonar sino de probar, y porque en clase habría sido un
desastre silencioso.

**El síntoma.** Se crea un trámite con un RUT que no está en el padrón, se pide dos veces, y:

```
:8211/contribuyentes/11111111-1  ->  HTTP 200          (contribuyentes, perfectamente sano)
:8212/tramites/estado-circuito   ->  {"circuito":"OPEN","fallidas":2,...}
```

**El circuito abierto contra un servicio vivo.** Y con el circuito abierto, **todos** los trámites
salen degradados, incluidos los de RUT válido.

**La causa.** `retrieve()` lanza ante un 4xx. El 404 de «ese RUT no existe» llegaba al circuito como
un fallo del otro servicio, y dos de esos bastaban para abrirlo.

**El arreglo.** Una línea, `ignoreExceptions(HttpClientErrorException.class)`, y su explicación en
`PASOS.md` y en `instructor/`:

> Un circuito mide **la salud del otro servicio**. Un 404 no dice nada malo del otro servicio: dice
> que **tú** pediste algo que no existe. Los 5xx y los fallos de red cuentan; los 4xx no.

**Verificado después del arreglo**, en los dos sentidos: con RUT inexistente el circuito se queda en
`CLOSED` con `fallidas: 0`, y con contribuyentes apagado sigue abriéndose y congelando el contador
(V4 arriba se midió **después** de este arreglo).

El defecto pasó a ser material: es de los mejores ejemplos del lab de «un patrón de resiliencia mal
configurado es peor que no tenerlo».

---

## 8 · Lo que quedó en «lo que no vimos hoy»

Cada uno con su razón, en el README del lab:

| | por qué no |
|---|---|
| **Eureka** (service discovery) | dos servicios de infraestructura más; no caben en tres horas. Se explica en teoría, incluida la parte que importa: **un registro siempre miente un poco**, y por eso el circuit breaker no es opcional |
| **Config Server** | ídem. Las direcciones son tres URLs en `application.yml`, que es lo que hacen muchos sistemas reales |
| **Spring Cloud Gateway** | §2 de este informe. Se conserva la trampa del nombre del artefacto |
| **gRPC** | §4. No hay `protoc` en la maleta |
| **Mensajería (colas)** | el evento perdido del paso 8 se ve pasar; que no se pierda necesita un servidor que el SII no puede instalar. Brecha ya declarada en el mapa |
| **Sagas y compensaciones** | hoy el segundo servicio es opcional; cuando los dos son obligatorios hace falta una saga |
| **Trazas distribuidas** (OpenTelemetry) | el `X-Trace-Id` del paso 7 es el 20 % que da el 80 %; falta el árbol de tiempos |
| **Balanceo entre N instancias** | sin registro no hay lista que balancear |
| **Cliente declarativo** (`@HttpExchange`, Feign) y **tests de contrato** | no caben |

Y la sección que el material considera parte del temario y no un apéndice: **cuándo NO partir un
sistema** —equipo que cabe en una mesa, costuras desconocidas, transacciones necesarias, falta de
observabilidad, «es lo moderno»— y el **monolito distribuido** como el resultado más común de
«vamos a migrar a microservicios». El cierre del lab **no vende microservicios**: cuenta el peaje y
dice que a veces no compensa (prohibición §7 de la SPEC).

---

## 9 · Otros archivos tocados

- **`docs/temario/MAPA-LAB-MODULO.md`** — el Lab 14 entra en el mapa y cierra brechas. Detalle en el
  propio documento.
- **`ESTADO.md`** — §1 (el arco pasa a quince labs), §2 y §3 al día.
- **`labs/lab-13-empaquetado/PASOS.md`** — su cierre decía «este es el último laboratorio», que
  dejó de ser cierto. Ahora siembra el Lab 14. *(El job `siembra` del CI no se rompía —busca el
  patrón `siembra`, que ya estaba— pero el texto habría quedado mintiendo.)*
- **`docs/specs/SPEC-037-lab-14-microservicios.md`** — la SPEC al repositorio, como las anteriores.

---

## 10 · Lo que este informe NO puede afirmar

- **Que el guion se entienda.** V7 demuestra que es completo y correcto: siguiéndolo al pie de la
  letra se llega a `solucion/`. Que un alumno lo siga **sin abrir `solucion/`** es la fila de
  aceptación del PO, y quien escribió el guion no puede juzgarla.
- **Que las tres horas se cumplan en sala.** La suma de §5 es una estimación del ejecutor. El paso 1
  —cuatro terminales— es el que más puede desbordarse.
- **Que funcione en Windows.** Todo lo de aquí se midió en macOS Apple Silicon. El material es
  portable por construcción (mismo `mvnw`, mismas guardas con su rama de `os.name`), pero **los tres
  defectos de la SPEC-024 eran invisibles desde macOS**, y este es el primer lab que abre **cuatro**
  procesos y **tres** PostgreSQL a la vez. Es la plataforma donde más conviene que el PO lo pruebe.
- **Que la alternativa `java -jar` ahorre la memoria de §6/V8.** No se midió. Está como observación,
  no como recomendación.
