# Teoría · Lab 14 — La DGT se parte en pedazos

*Módulo 15 · Arquitectura de microservicios, despliegue e interoperabilidad.*
*Última sesión. Aquí no se construye: se mira lo construido desde arriba y se decide.*

---

## §1 · La pregunta que nadie hace a tiempo

Durante trece sesiones construimos **una** aplicación. Una base de datos, un despliegue, un
log, un `pom.xml`. Hoy la partimos en seis piezas, y la pregunta del día no es *cómo* se
parte —eso lo hace el framework— sino **por qué querrías hacerlo**.

Casi todo el material que vas a encontrar sobre microservicios responde a la primera y
salta la segunda. Este laboratorio hace lo contrario: te enseña los seis patrones
funcionando, y después te enseña **lo que se rompe**, para que la decisión la tomes tú con
los dos lados de la cuenta delante.

Un aviso desde el principio, y va en serio:

> **Un sistema distribuido no es una aplicación mejor. Es una aplicación distinta, con
> problemas que la anterior no tenía y que no se resuelven programando mejor.**

---

## §2 · Qué es, de verdad, un microservicio

No es «un servicio pequeño». El tamaño es la consecuencia, no la definición.

Un microservicio es una **unidad de despliegue independiente que es dueña de sus datos**.
Las dos mitades importan, y la segunda es la que casi nadie respeta:

- **Despliegue independiente**: puedes subir una versión nueva de `dgt-contribuyentes` sin
  tocar `dgt-tramites`, sin coordinar una ventana, sin pedirle permiso a otro equipo.
- **Dueño de sus datos**: nadie más entra a su base. Ni de lectura.

En este laboratorio esa frontera no es una convención escrita en un README: mira
`sistema/db-init/01-bases-y-usuarios.sql`. Hay dos bases y dos usuarios, y el usuario de
trámites **no tiene el GRANT** para entrar en la base de contribuyentes. Si alguien
intenta el JOIN «rápido» para ahorrarse la llamada HTTP, el motor le dice que no.

### El monolito distribuido: el peor de los dos mundos

Cuando se salta la segunda mitad —dos servicios, dos despliegues, **una base de datos
compartida**— sale un bicho con nombre propio: **monolito distribuido**.

| | Monolito | Microservicios | Monolito distribuido |
|---|---|---|---|
| Latencia entre módulos | nanosegundos | milisegundos | **milisegundos** |
| Transacción atómica | sí | no | **no** |
| Desplegar por separado | no | sí | **no** (la base los ata) |
| Depurar un fallo | un log | N logs + traza | **N logs + traza** |

Se pagan todos los costos y no se cobra ninguna ventaja. Y es, con diferencia, el
resultado más común de «vamos a migrar a microservicios».

---

## §3 · Los seis patrones

### 3.1 · Service Discovery — la guía telefónica

**El problema.** Con puertos fijos y direcciones escritas a mano, cada instancia nueva es
una edición de configuración en todos los que la llaman. Y como en este laboratorio los
servicios arrancan en **puerto efímero**, ni ellos saben dónde están.

**La analogía.** Una guía telefónica. Los servicios se dan de alta al nacer («soy
DGT-CONTRIBUYENTES, estoy en tal IP y puerto») y llaman cada pocos segundos para decir que
siguen vivos. Quien deja de llamar, se tacha.

**En el laboratorio.** `dgt-registro`, un Eureka Server. Panel en `localhost:8761`.

**Lo que hay que entender y casi nadie explica:** el registro **miente**. Siempre. Entre
que una instancia muere y que alguien la tacha pasan segundos, y durante esos segundos la
guía anuncia a un muerto. No es un defecto de Eureka: es su diseño. Ante la duda prefiere
seguir anunciando a alguien que quizá solo tuvo un corte de red, antes que sacar de
rotación a media flota por un hipo.

Eso tiene una consecuencia de arquitectura, no de configuración:

> **Todo lo que llame a otro servicio tiene que estar preparado para que la dirección que
> le dieron no responda.** El circuit breaker no está ahí por si acaso: está ahí porque la
> lista *es* falible, por definición.

### 3.2 · Config Server — una sola fuente de verdad

**El problema.** El timeout hacia Tesorería está escrito en cinco `application.yml`.
Cambiarlo son cinco ediciones, cinco compilaciones y cinco despliegues. El día que alguien
cambie cuatro, nadie se entera hasta que falla el quinto.

**La analogía.** El tablón de anuncios de la oficina. Una hoja, y todos leen la misma.

**En el laboratorio.** `dgt-config`, con backend `native`: la configuración son archivos
en `sistema/config-repo/`, que puedes abrir y editar. **El TODO de hoy vive ahí dentro.**

En producción el backend es un repositorio Git, y no por moda: se quiere el **historial**
(quién subió el timeout a 30 s, y cuándo) y la **reversión** (`git revert` sobre el cambio
de configuración que tumbó el sistema, sin recompilar nada). Es el mismo Config Server con
otro `spring.cloud.config.server.*`.

**Sin `bootstrap.yml`.** Hasta Spring Boot 2.4 los clientes usaban un `bootstrap.yml` y una
fase de arranque previa. Esa fase **ya no está en el camino por defecto**. Hoy es una línea:

```yaml
spring:
  config:
    import: "configserver:http://dgt-config:8888"
```

Si encuentras un tutorial con `bootstrap.yml`, estás leyendo material de hace cinco años.

**Y el precio, que también hay que decirlo:** una sola fuente de verdad es también un solo
sitio donde equivocarse **para todos a la vez**. Un error en `application.yml` no tumba un
servicio: tumba seis.

### 3.3 · API Gateway — una sola puerta

**El problema.** El navegador del contribuyente no puede saber que los trámites están en un
puerto y los contribuyentes en otro, y que ambos cambian en cada reinicio.

**La analogía.** La recepción de un edificio. Una dirección pública; dentro, quien haga
falta.

**En el laboratorio.** `dgt-portal`, en el 8099 — el puerto del curso desde el Lab 00. Sus
rutas viven en `config-repo/dgt-portal.yml`: cambiar una ruta es editar texto, no
recompilar.

**Trampa de versiones que le pasa a todo el mundo:** el artefacto **ya no se llama**
`spring-cloud-starter-gateway`. Ese nombre murió en la serie 4.3.x. En el tren 2025.1.x hay
que elegir sabor:

- `spring-cloud-starter-gateway-server-webmvc` — bloqueante, sobre Servlet
- `spring-cloud-starter-gateway-server-webflux` — reactivo, sobre Netty

Aquí usamos el **webmvc**, a propósito: el curso entero se construyó sobre Spring MVC
bloqueante, y regalarte en la última sesión un stack reactivo que no sabes depurar sería un
mal favor. El reactivo aguanta mucha más concurrencia con menos hilos y es la elección
correcta para un gateway con carga real: cuando lo necesites, ya sabrás por qué.

Y el prefijo de las propiedades también cambió: `spring.cloud.gateway.server.webmvc.routes`.
Con el prefijo viejo, Spring lee el bloque, no reconoce nada, **y el gateway arranca sin
ninguna ruta**. Sin error, sin aviso: todo 404.

### 3.4 · Feign — llamar sin escribir el cliente

**El problema.** En el Lab 08 escribiste a mano un cliente HTTP: construir la URI,
serializar, mapear el error. Multiplícalo por cada servicio con el que hables.

**La analogía.** Un intérprete. Le dices *qué* quieres decir; él pone las palabras.

**En el laboratorio.** `ContribuyenteCliente`: una interfaz con anotaciones, sin
implementación. Y mira el `name`:

```java
@FeignClient(name = "dgt-contribuyentes")
```

No es una URL. Es el **nombre lógico** del registro.

**El precio.** El `@GetMapping` de tu cliente tiene que coincidir con la ruta del otro
servicio, y **nada lo verifica al compilar**. Si allá cambian la URL, esto se entera en
producción. Se llama **acoplamiento por contrato**, y no desaparece: se gestiona (versionar
la API, tests de contrato, no romper nunca lo viejo).

Y una tentación que hay que nombrar para poder rechazarla: *«pongamos los DTO en un módulo
compartido y así no se duplican»*. Parece limpieza y es una trampa — el día que cambies un
campo tendrás que desplegar los dos servicios a la vez, y acabas de perder la independencia
por la que partiste el sistema. Por eso `FichaContribuyente` está **declarado dos veces** en
este laboratorio, con `@JsonIgnoreProperties(ignoreUnknown = true)` para que añadir un campo
allá no rompa aquí.

### 3.5 · LoadBalancer — a cuál de las N

**El problema.** El registro devuelve una *lista*. ¿A cuál llamas?

**La analogía.** La cola única del banco: el siguiente cajero libre.

**En el laboratorio.** `lb://dgt-contribuyentes`. Round-robin: por turnos. Y como el
balanceo pasa en el **cliente**, no hay un balanceador central que se caiga.

**Y aquí está la lección escondida del bloque 3: hay DOS cachés, no una.**

1. la del cliente de Eureka, que se baja la lista cada `registry-fetch-interval`;
2. la del **LoadBalancer**, que guarda esa lista otros **35 segundos** por defecto.

Bajar solo la primera no sirve de nada. Durante la construcción de este laboratorio se
midió: una instancia nueva, visible en el panel de Eureka desde el primer segundo, tardaba
**más de medio minuto** en recibir su primera petición. El síntoma —«el registro la ve pero
no le llega tráfico»— parece un fallo del balanceador, y es exactamente lo que se le pidió.

Es el error de razonamiento clásico con las cachés: se optimiza la que se conoce y se olvida
la que hay detrás.

### 3.6 · Circuit Breaker — dejar de insistir

**El problema.** `dgt-contribuyentes` tarda 5 segundos. Cada petición se queda con un hilo
de `dgt-tramites`, que se queda con un hilo del portal. Suficiente tráfico, y el sistema
entero se para por culpa de **una** pieza. Se llama **fallo en cascada**.

**La analogía.** El diferencial de tu casa. No arregla el cortocircuito: corta la corriente
para que no arda el edificio.

**Los tres estados:**

| Estado | Qué hace | Cuándo cambia |
|---|---|---|
| **CERRADO** | deja pasar todo, cuenta fallos | supera el umbral → ABIERTO |
| **ABIERTO** | rechaza sin llamar (*falla rápido*) | pasa el tiempo → MEDIO ABIERTO |
| **MEDIO ABIERTO** | deja pasar unas pocas de prueba | van bien → CERRADO · van mal → ABIERTO |

**En el laboratorio.** Resilience4j, y **el TODO son sus umbrales**. Ahora mismo no hay
ninguno declarado, así que manda el valor por defecto: `minimum-number-of-calls: 100`.

Léelo otra vez. **Cien llamadas** antes de que el circuito llegue siquiera a *opinar*. En
la sesión de hoy no hay cien llamadas; en un servicio interno con poco tráfico, tampoco.
Eso es un **circuit breaker decorativo**: está en el árbol de dependencias, sale en el
diagrama de arquitectura, y no va a abrirse jamás.

> Un patrón de resiliencia mal configurado es peor que no tenerlo: da la tranquilidad sin
> dar la protección.

**Retry, y el orden que importa.** Resilience4j compone los aspectos así:

```
Retry ( CircuitBreaker ( tu método ) )
```

De ahí salen dos cosas que hay que tener en la cabeza:

1. **El `fallbackMethod` va en `@Retry`**, el anillo de fuera. Puesto en
   `@CircuitBreaker`, la excepción se convierte en respuesta válida dentro, el `Retry` de
   fuera cree que todo salió bien a la primera, y **no reintenta nunca**. Sigue habiendo
   anotación, número en la configuración y línea en el diagrama. Y cero reintentos.
2. **El circuito cuenta llamadas, no clics.** Con `max-attempts: 2`, cada petición del
   usuario son **dos** llamadas contadas. Si el umbral pide seis, se abre con tres
   peticiones.

**Y el timeout es lo que convierte «lento» en «fallo».** Un circuit breaker cuenta
excepciones, y esperar no lanza ninguna. Sin `read-timeout`, una instancia que tarda ocho
segundos no le parece un fallo a nadie. Es la lección del Lab 08 —«once segundos»— aplicada
a un vecino que ahora está al otro lado de la red.

---

## §4 · El fallback es una decisión de negocio

El crimen de hoy no es que el sistema se caiga. Es que **no se cae**.

Con `dgt-contribuyentes` apagado, el portal devuelve:

```json
{"id":1,"tipo":"DECLARACION_F29","estado":"EN_PROCESO",
 "rutContribuyente":"11111111-1","nombreContribuyente":null,"atendidoPor":null}
```

HTTP 200. JSON bien formado. Ni una línea roja en ninguna pantalla. Y falta el nombre del
titular de una declaración de impuestos.

Ese `null` lo escribió alguien. Está en `ConsultaDeContribuyentes.fichaDegradada()`, y es
**una elección**, no un detalle técnico:

| Opción | Qué dice al usuario | Cuándo es correcta |
|---|---|---|
| Datos parciales en silencio | «aquí tienes» (y miente por omisión) | cuando el campo es accesorio |
| Datos parciales **marcados** | «esto está incompleto» | casi siempre |
| HTTP 503 honesto | «ahora no puedo» | cuando el dato es esencial |
| Último valor conocido (caché) | «esto es de hace un rato» | si lo viejo sirve |

Las cuatro son defendibles. Lo que no es defendible es **elegir sin darte cuenta de que
estás eligiendo** — y el valor por defecto siempre es «datos parciales en silencio», porque
es lo que sale de escribir el fallback sin pensarlo.

El desafío `99-` de este laboratorio es exactamente cambiar esta elección.

---

## §5 · Cuándo NO usar microservicios

Esta sección no es una nota al pie. Es la mitad del contenido del módulo, y probablemente
lo más útil que te lleves del curso.

### 5.1 · Lo que se paga, con números de este laboratorio

| Costo | Medido aquí |
|---|---|
| **Arranque** | 56 s las seis piezas (con imágenes ya construidas). El monolito: ~8 s |
| **RAM** | 1,54 GiB frente a ~400 MiB del monolito |
| **Latencia** | una llamada de red donde había un `findByRut()` |
| **Build** | cinco proyectos Maven, cinco imágenes, un compose |
| **Depuración** | seis logs en vez de uno; sin traza distribuida, a ciegas |
| **Consistencia** | no hay transacción que abarque dos servicios |
| **Infraestructura nueva** | registro + config server: dos piezas que no dan valor al contribuyente y hay que operar igual |

Y el más caro no sale en la tabla: **una guardia a las tres de la mañana con seis piezas es
otra profesión**. «Está lento» deja de tener una respuesta y pasa a tener seis candidatas.

### 5.2 · No lo hagas si…

- **Tu equipo cabe en una mesa.** La ventaja de microservicios es *organizativa*: equipos
  que despliegan sin pedirse permiso. Con seis personas no hay permiso que pedir, y te
  quedas con todos los costos y ninguna ventaja.
- **No sabes dónde están las costuras.** Partir por el sitio equivocado se paga carísimo:
  cada llamada que cruza la frontera equivocada es una llamada de red que debería haber
  sido un método. Si no puedes dibujar los límites con confianza, todavía no toca.
- **Necesitas transacciones entre las partes.** «Emitir el folio y cobrar» en una
  transacción es trivial en un monolito. Repartido son *sagas*, compensaciones y estados
  intermedios visibles. A veces hay que hacerlo. Nunca sale gratis.
- **No tienes observabilidad.** Sin trazas distribuidas y logs correlacionados (Lab 10), un
  sistema de seis piezas es una caja negra con seis compartimentos. Si vas a partir,
  **primero** la observabilidad; si no, a ciegas.
- **La razón es «es lo moderno».** Es la peor de todas, y la más frecuente.

### 5.3 · Sí, cuando…

- Hay **varios equipos** pisándose en el mismo despliegue.
- Una parte del sistema tiene una carga **muy distinta** al resto y quieres escalarla sola.
- Una parte necesita otro ritmo de despliegue o de disponibilidad.
- Una parte tiene requisitos de cumplimiento o de datos que conviene aislar de verdad.

Y el consejo que da casi todo el que ha hecho las dos cosas: **empieza por un monolito bien
modularizado**. Es más fácil partir un monolito ordenado que juntar diez microservicios mal
partidos. Los trece labs anteriores construyeron justo eso.

---

## §6 · Estado del arte: lo que está muerto

De todo el stack Netflix histórico, **solo Eureka sigue vivo** en el tren actual:

| Pieza | Estado | Sustituto |
|---|---|---|
| **Eureka** | vivo y soportado | — |
| Hystrix | muerto, fuera del release train | **Resilience4j** |
| Ribbon | muerto | **Spring Cloud LoadBalancer** |
| Zuul | muerto | **Spring Cloud Gateway** |

Esto importa más de lo que parece: los tutoriales de Hystrix siguen saliendo altísimo en
los buscadores, con miles de estrellas, escritos con seguridad. Si copias uno, arrastras
dependencias sin mantenimiento a un sistema que va a durar años.

**Cómo comprobarlo tú, en cualquier proyecto:**

```bash
./mvnw dependency:tree | grep -iE 'hystrix|ribbon|zuul|archaius'
```

Sin salida es la respuesta correcta. En este laboratorio lo verifica el validador, y el CI
lo comprueba en cada PR.

Y la versión: Spring Cloud **no** usa el número de Spring Boot. Usa *release trains* con
nombre de año (2025.1.x, «Oakwood»), y cada tren declara con qué versiones de Boot es
compatible. **Mirar esa tabla antes de escribir una dependencia** es la diferencia entre
una tarde de trabajo y una semana.

---

## §7 · El envenenamiento de la ventana en frío

Esto se descubrió **construyendo** este laboratorio, y vale más que muchos apartados de
manual.

`dgt-tramites` arrancaba antes de que `dgt-contribuyentes` se hubiera anotado en el
registro. Las primeras llamadas morían con *«Load balancer does not contain an instance»*.
El circuit breaker las contaba como fallos —**porque lo eran**—, y en cuanto juntó el
mínimo de llamadas vio un 66 % de fallos y **abrió**.

Resultado: un sistema entero sano, recién levantado, con el circuito abierto y devolviendo
respuestas degradadas.

```
01:45:41  ERROR  503 Load balancer does not contain an instance
01:45:42  ERROR  503 ...
01:45:44  ERROR  503 ...
01:45:44  ERROR  503 ...
01:45:46  SUCCESS
01:45:57  SUCCESS
01:45:57  FAILURE_RATE_EXCEEDED
01:45:57  STATE_TRANSITION  CLOSED -> OPEN     <-- 4 fallos de arranque, 6 llamadas mínimas
```

Le pasa a todo el mundo en producción, **en cada despliegue**. Y la causa nunca es el
circuit breaker: es haber aceptado tráfico antes de estar listo para servirlo. Es
literalmente la diferencia entre *liveness* y *readiness* que se enseñó en el Lab 10, ahora
con consecuencias.

Aquí se resolvió arrancando en orden (`depends_on: service_healthy`). En un clúster de
verdad se resuelve con la sonda de *readiness*: hasta que tu balanceador no tenga la lista,
no estás listo, y nadie debería mandarte tráfico.

---

## §8 · Glosario

| Término | Qué es |
|---|---|
| **Service discovery** | Registrarse al nacer y encontrar a otros por nombre lógico |
| **Nombre lógico** | `dgt-contribuyentes` en vez de `10.0.0.7:8080` |
| **Puerto efímero** | `server.port: 0`, el sistema operativo elige |
| **Lease / renovación** | El «sigo vivo» periódico hacia el registro |
| **Eviction** | Tachar de la lista a quien dejó de renovar |
| **Autoconservación** | Modo en que Eureka deja de tachar por sospechar de sí mismo |
| **Fallo en cascada** | Un servicio lento agota los hilos de todos los que lo llaman |
| **Fallar rápido** | Devolver el «no» al instante en vez de esperar el timeout |
| **Fallback** | Qué devolver cuando la llamada no se pudo hacer |
| **Monolito distribuido** | Servicios separados atados por una base compartida |
| **Saga** | Sustituto de la transacción cuando abarca varios servicios |
| **Release train** | Versionado de Spring Cloud por año, no por versión de Boot |

---

## §9 · Cierre del arco — las catorce sesiones

Este laboratorio **no siembra el siguiente: es el último.** El arco cierra aquí, y conviene
mirarlo entero antes de apagar el proyector.

Empezaste con un endpoint que devolvía una lista (Lab 01) y terminas decidiendo si conviene
partir un sistema en seis piezas. Por el camino:

- Aprendiste a **no confiar** en la entrada (03), en el reloj (11), en el vecino (08), ni
  en tu propio tablero de salud (10).
- Viste que un folio duplicado **no se borra: se explica ante un fiscalizador** (06).
- Aprendiste que un `@Transactional` mal puesto, un N+1 y un fallback en silencio se
  parecen en una cosa: **funcionan** hasta que importan.
- Y hoy, que la arquitectura no es elegir la opción moderna: es saber **qué estás pagando y
  a cambio de qué**.

Lo único que te vas a llevar de verdad no es Spring Cloud. Es el hábito de preguntar *«¿y
esto qué pasa cuando se cae?»* antes de que se caiga.

Nos vemos del otro lado del botón.
