# Mapa Laboratorio ↔ Módulo del temario contratado

> **Nota (SPEC-038).** Los labs **12** y **13** se retiraron del curso, junto con `examen-huecos/`:
> siguen enteros en el tag `material-v1.11.1`. El **módulo 15** lo cubre ahora la **demostración
> con Docker del lab de microservicios** (`demos-instructor/microservicios-docker/`) más el empaquetado que entrega el
> proyecto final. Los temas que sólo tocaban esos dos labs pasan de **cubiertos** a
> **mencionados**: se nombran donde corresponde y no se practican.


*Documento de trazabilidad para la entrega al SII. Rehecho por la **SPEC-034** contra el arco de
catorce labs, actualizado por la **SPEC-035** (que cerró la brecha del proyecto final), por la
**SPEC-037**, que añadió el Lab 14 de microservicios, y por la **SPEC-046**, que añadió el Lab 05b
y con él el `@ManyToMany` que hasta ahora figuraba como complemento pendiente del M5.*

**Fuente de verdad:** `TEMARIO-SPRING-BOOT-SII-v3.md`, §«Estructura del Programa» y §«Matriz
Módulo × Sesión». **Donde el material y el temario discrepen, manda el temario** y la discrepancia
se declara aquí. No se reinterpreta el temario para que calce.

> ⚠️ **Este documento dice que hay huecos.** El arco nuevo no es el antiguo con otros nombres:
> cambió lo que se enseña. **Ocho** de los treinta y cinco temas comprometidos **no se cubren**.
> Está todo en la §4. Un mapa que dijera que está todo cubierto sería más cómodo y no serviría
> para nada.
>
> El noveno hueco —el proyecto final, tema XXXV— **se cerró en la SPEC-035**: `proyecto-final/` es
> el instrumento con el que se evalúa. Lo que sigue sin instrumento es el otro 50 % de la nota
> (conocimientos 30 % y ejercicios 20 %): ver §4.1.

---

## 0 · Cómo leer este documento

Cuatro niveles, y la diferencia importa:

| Nivel | Qué significa |
|---|---|
| **Cubierto** | El alumno **lo practica**. Hay un lab y un paso concreto donde ocurre |
| **Parcial** | Se practica el núcleo del tema, pero falta parte de lo comprometido |
| **Mencionado** | Aparece en la explicación o en «lo que no vimos hoy». **No se practica** |
| **No cubierto** | No aparece |

> **Nota sobre los niveles.** La SPEC-034 pedía tres (Cubierto / Mencionado / No cubierto). Se usa
> un cuarto, **Parcial**, porque sin él habría que redondear: llamar «cubierto» a un tema al que le
> falta la mitad, o «no cubierto» a uno cuyo núcleo sí se practica. Las dos cosas serían falsas, y
> este documento existe justamente para no redondear.

> **Nota sobre los temas SII.** El temario contratado asigna los temas a los módulos **por rangos**
> (`M5 · Temas V – VIII`) y **no los nombra uno a uno**. Se verificó: no hay lista nominal ni en el
> `.md` ni en el `.docx`. Por lo tanto la clasificación por tema de la §3 está **deducida del
> contenido de su módulo**, no copiada de una lista. Donde un rango es homogéneo, la deducción es
> directa; donde no, se dice.

---

## 1 · Lab → módulo(s) → temas

| Lab | Módulo(s) del temario | Temas | Qué se practica de verdad |
|---|---|---|---|
| **00** hola-mundo | M1 (parte) | I | Que arranque; el `pom.xml` y los starters; recarga sin recompilar |
| **01** web | M2 (parte) · M3 (parte) | III, IX | `@RestController`, rutas, `@PathVariable`/`@RequestParam`/`@RequestBody`, DTO con record, `ResponseEntity` con estado y `Location` |
| **02** di | **M1** · M3 (parte) | I, IX | **El lab que explica qué es Spring**: contenedor IoC, beans, inyección por constructor, dos candidatos y `@Primary`/`@Qualifier`, capa de servicio |
| **03** errores | **M3** | XI, XII | Excepción de dominio propia, `@RestControllerAdvice`, `@Valid` con las anotaciones estándar, 404 con cuerpo, 400 con los campos, y el mensaje interno que no sale |
| **04** jpa | **M5** (parte) | V, VI | `@Entity`, `JpaRepository`, CRUD, consultas derivadas, *dirty checking*, persistencia comprobada tras reiniciar |
| **05** relaciones | **M5** (parte) | VII | `@ManyToOne`, `@OneToMany(mappedBy)`, LAZY contra EAGER contado, `LazyInitializationException`, consulta que cruza la relación |
| **06** rendimiento | **M5** (cierre) · **M7** (parte) | VIII, XXX | El N+1 **medido**: 201 consultas contra 1, con `JOIN FETCH`, `@EntityGraph` y proyección |
| **07** concurrencia | **M7** · M8 (parte) | X, XXXI | `@Transactional`, secuencia de negocio irrepetible bajo concurrencia, bloqueo pesimista, y **una migración correctiva** (`V2__`) que añade la restricción como segunda defensa |
| **08** testing | **M4** · M6 (parte) | XXI, XXII | JUnit, AAA, el **rojo provocado**, `assertThrows`, Mockito (`@Mock`/`when`/`verify`), `@WebMvcTest` + MockMvc, `@SpringBootTest` y cuándo no usarlo |
| **09** seguridad | **M9** | XIII, XIV, XV | Cadena de filtros, usuarios en memoria y en base con `UserDetailsService`, BCrypt con sal y costo, JWT (anatomía, firma simétrica, y que **cualquiera lo lee**), validación con OAuth2 Resource Server, roles y **401 frente a 403** |
| **10** resiliencia | M10 (parte) · **M13** (mitad) | XVI, XXXIII | `RestClient`, timeouts de conexión y lectura, reintentos **y su contraindicación**, circuit breaker con sus tres estados, degradación elegante |
| **11** observabilidad | **M14** (parte) · M11 (parte) | XVIII, XXVII | Actuator con lista blanca nominal, `traceId` en el MDC, métrica de negocio con Micrometer, health indicator propio que **nombra la causa**, liveness contra readiness |
| **12** tareas | **M12** | XXIII, XXIV | `@Scheduled`, `fixedRate` contra `fixedDelay`, cron de seis campos con zona, `@Async` y sus trampas, hilos virtuales, y la **tarea duplicada en dos instancias** |
| **13** empaquetado | **M15** (parte) · M1 (parte) | II, XXXIV | Jar ejecutable y `java -jar`, jar por capas, **qué es un contenedor** (20 min de pizarra), imagen OCI con Jib sin Docker, y la misma imagen en tres entornos |
| **14** microservicios | M10 (parte) · M13 (parte) · M14 (parte) | XVI, XXVIII, XXXIII | **Cuatro servicios, tres bases, sin Docker**: una base por servicio y el JOIN imposible, la llamada HTTP entre procesos, el **fallo en cascada medido** (500 → 200 degradado), API gateway con enrutado y JWT en la puerta, **correlation id por tres logs**, consistencia eventual con el evento que se pierde — y cuándo **no** partir un sistema |

**Horas del material:** 15 labs × 3 h = **45 h**. El contrato compromete **36 h en 12 sesiones**.
Ver §5.

---

## 2 · Módulo → dónde se cubre

**Esta es la tabla que responde la pregunta del SII.** Los 15 módulos, ninguno omitido.

| # | Módulo | Horas | Nivel | Dónde | Qué falta de lo comprometido |
|---|---|---|---|---|---|
| **M1** | Fundamentos y Configuración | 2,0 | **Parcial** | lab-02 (IoC/beans, entero) · lab-13 paso 5 (perfiles) · lab-00 paso 4 (starters) | Crear el proyecto desde cero con Initializr · `@ConfigurationProperties` con record validado |
| **M2** | Controladores REST, Versionado, OpenAPI | 2,5 | **Parcial** | lab-01 pasos 1–6 | **OpenAPI/SpringDoc y Swagger UI** · **versionado nativo de Framework 7** · `@PutMapping`/`@PatchMapping`/`@DeleteMapping` |
| **M3** | Capas, DTOs, Validaciones, Excepciones | 3,0 | **Parcial** | lab-02 paso 6 (capas) · lab-01 paso 4 (DTO) · lab-03 pasos 2–5 | **`ProblemDetail` (RFC 9457)** — se usa un record propio · validaciones personalizadas (`ConstraintValidator`), grupos, i18n · MapStruct |
| **M4** | Testing I: JUnit y Mockito | 1,5 | **Cubierto** | lab-08 pasos 1–4 | (complementos: `@ParameterizedTest`, AssertJ, `ArgumentCaptor`, spy) |
| **M5** | Persistencia con JPA e Hibernate | 4,0 | **Cubierto** | lab-04 pasos 1–9 · lab-05 pasos 1–6 · lab-05b pasos 1–6 · lab-06 pasos 1–5 | (complementos: `Pageable`/`Sort` y endpoint paginado · `JdbcClient` · `@OneToOne` · `CascadeType`) |
| **M6** | Testing II: Integración y Testcontainers | 1,5 | **Parcial** | lab-08 pasos 5–6 (`@WebMvcTest`, `@SpringBootTest`, pirámide) | **Testcontainers 2 y `@ServiceConnection`** · **RestTestClient** · `@DataJpaTest` (mencionado) · H2 · `spring-boot-docker-compose` |
| **M7** | Transacciones y Optimización | 2,0 | **Cubierto** | lab-07 pasos 1–5 · lab-06 pasos 1–5 | (complementos: propagación · aislamiento y anomalías · `@Version` optimista · índices y plan de ejecución · HikariCP como tema) |
| **M8** | Migraciones: Flyway y Liquibase | 1,5 | **Parcial** | lab-07 paso 5 (migración correctiva con restricción) · Flyway en uso en 6 labs | **Liquibase, entero** · la mecánica de Flyway como tema (historial, repetibles, baseline, `ddl-auto` como antipatrón) |
| **M9** | Spring Security 7 y JWT | 4,0 | **Parcial** | lab-09 pasos 1–6 | **CORS** · **cabeceras de seguridad (HSTS, CSP)** · `@PreAuthorize` (mencionado) · Argon2 · firma asimétrica · refresh tokens (mencionado) · passkeys |
| **M10** | Comunicación entre Servicios | 2,5 | **Parcial (alto)** | lab-10 pasos 2, 3, 5 (`RestClient`, timeouts, degradación) · **lab-microservicios pasos 3, 6, 7** (llamada real entre dos procesos, gateway, propagación de trazas) | **gRPC, entero** · **`@HttpExchange`** · Feign · WebClient · mitigación SSRF |
| **M11** | Logging Estructurado, AOP y Archivos | 3,0 | **Parcial (bajo)** | lab-11 paso 2 (MDC y correlación) · niveles de log | **AOP, entero** (`@Aspect`, advice, pointcuts, auditoría) · **manejo de archivos, entero** (`MultipartFile`, MIME, streaming) · logging JSON (configurado y apagado: mencionado) |
| **M12** | Asincronía, Scheduling y Eventos | 2,0 | **Parcial** | lab-12 pasos 1–5 | **Eventos de aplicación, entero** (`@EventListener`, `@TransactionalEventListener`) · `CompletableFuture` · la protección contra doble ejecución **se muestra pero no se implementa** |
| **M13** | Mensajería y Resiliencia | 2,5 | **Parcial (mitad)** | lab-10 pasos 3–5 (retry, circuit breaker, fallback) · **lab-microservicios pasos 4–5** (el circuito protegiendo una llamada entre servicios de verdad, con los dos comportamientos medidos) | **Mensajería, entera**: RabbitMQ/Kafka, AMQP, DLQ, idempotencia · rate limiter y bulkhead (mencionados) · `@Retryable`/`@ConcurrencyLimit` del núcleo |
| **M14** | Observabilidad, Métricas y Caché | 2,0 | **Parcial** | lab-11 pasos 1, 3, 4, 5 · **lab-microservicios paso 7** (correlación a través de tres procesos) | **Caché, entero** (`@Cacheable`, Caffeine, hit-rate) · **OpenTelemetry**, con su árbol de tiempos (el id de correlación sí se practica) · Prometheus/Grafana (mencionado) · `@Endpoint` propio |
| **M15** | Contenedores y Proyecto Final | 2,0 | **Parcial** | lab-13 pasos 1–5 · **`proyecto-final/`** | Buildpacks (`build-image`) — se usa Jib · arranque acelerado (AOT/Leyden, GraalVM) · `compose.yaml` · graceful shutdown |

**Recuento por módulo:** **3 Cubiertos** (M4, M5, M7) · **12 Parciales** · 0 sin ningún contenido.

(M15 sigue Parcial pese a tener ya su proyecto final: le faltan Buildpacks y el arranque acelerado.)

---

## 3 · Los 35 temas, uno por uno

Deducidos del contenido de su módulo (ver la nota de la §0).

| Tema | Módulo | Contenido | Nivel | Respaldo |
|---|---|---|---|---|
| I | M1 | Fundamentos, IoC, beans | **Cubierto** | lab-02, los seis pasos |
| II | M1 | Configuración externalizada y perfiles | **Cubierto** | lab-13 paso 5; `application.yml` en los 14 |
| III | M2 | Controladores REST | **Cubierto** | lab-01 pasos 1–6 |
| IV | M2 | Versionado nativo y OpenAPI | **No cubierto** | — |
| V | M5 | Entidades y configuración JPA | **Cubierto** | lab-04 paso 1 |
| VI | M5 | Repositorios y consultas | **Cubierto** | lab-04 pasos 2–8 (derivadas); lab-06 paso 2 (`@Query` con JPQL) |
| VII | M5 | Relaciones | **Cubierto** | lab-05 pasos 1–6 (`@ManyToOne`, `@OneToMany`) · lab-05b pasos 1–6 (`@ManyToMany`) |
| VIII | M5 | Fetch, N+1 y proyecciones | **Cubierto** | lab-06 pasos 1–5 |
| IX | M3 | Arquitectura en capas y DTOs | **Cubierto** | lab-02 paso 6; lab-01 paso 4 |
| X | M7 | Transacciones | **Cubierto** | lab-07 pasos 1–5 |
| XI | M3 | Validaciones | **Parcial** | lab-03 paso 4 (estándar; faltan personalizadas, grupos, i18n) |
| XII | M3 | Manejo de excepciones | **Parcial** | lab-03 pasos 2, 3, 5 (falta `ProblemDetail`) |
| XIII | M9 | Cadena de filtros y autenticación | **Cubierto** | lab-09 pasos 1–3 |
| XIV | M9 | JWT | **Cubierto** | lab-09 pasos 4–5 |
| XV | M9 | Autorización | **Parcial** | lab-09 paso 6 (por ruta; falta method security) |
| XVI | M10 | Comunicación HTTP | **Parcial** | lab-10 pasos 2–3 · lab-microservicios pasos 3, 6 (llamada entre dos procesos y gateway; falta el cliente declarativo) |
| XVII | M10 | **gRPC** | **No cubierto** | — |
| XVIII | M11 | Logging y correlación | **Cubierto** | lab-11 paso 2 |
| XIX | M11 | **AOP** | **No cubierto** | — |
| XX | M11 | **Manejo de archivos** | **No cubierto** | — |
| XXI | M4 | Pruebas unitarias | **Cubierto** | lab-08 pasos 1–4 |
| XXII | M6 | Pruebas de integración | **Parcial** | lab-08 pasos 5–6 (faltan Testcontainers y RestTestClient) |
| XXIII | M12 | Scheduling | **Cubierto** | lab-12 pasos 1–2, 5 |
| XXIV | M12 | Hilos virtuales y asincronía | **Cubierto** | lab-12 pasos 3–4 |
| XXV | M12 | **Eventos de aplicación** | **No cubierto** | — |
| XXVI | M13 | **Mensajería** | **No cubierto** | — |
| XXVII | M14 | Actuator y métricas | **Cubierto** | lab-11 pasos 1, 3, 4, 5 |
| XXVIII | M14 | Trazas / OpenTelemetry | **Parcial** | **lab-microservicios paso 7**: el mismo id de correlación en los logs de tres servicios, medido. Falta OpenTelemetry y su árbol de tiempos |
| XXIX | M14 | **Caché** | **No cubierto** | — |
| XXX | M7 | Optimización de consultas | **Cubierto** | lab-06 pasos 1–5 |
| XXXI | M8 | Flyway | **Parcial** | lab-07 paso 5 (migración correctiva; falta la mecánica como tema) |
| XXXII | M8 | **Liquibase** | **No cubierto** | — |
| XXXIII | M13 | Resiliencia | **Cubierto** | lab-10 pasos 3–5 · lab-microservicios pasos 4–5 (el mismo patrón entre dos procesos) |
| XXXIV | M15 | Contenedores y empaquetado | **Cubierto** | lab-13 pasos 1–5 |
| XXXV | M15 | Proyecto final integrador | **Cubierto** | `proyecto-final/` (SPEC-035): brief, rúbrica de tres ejes y defensa |

**Recuento por tema:**

| Nivel | Cuántos | Cuáles |
|---|---|---|
| **Cubierto** | **20** | I, II, III, V, VI, VII, VIII, IX, X, XIII, XIV, XVIII, XXI, XXIII, XXIV, XXVII, XXX, XXXIII, XXXIV, **XXXV** |
| **Parcial** | **7** | XI, XII, XV, XVI, XXII, **XXVIII**, XXXI |
| **Mencionado** | **0** | — |
| **No cubierto** | **8** | IV, XVII, XIX, XX, XXV, XXVI, XXIX, XXXII |
| | **35** | |

---

## 4 · Las brechas

Todo lo que el contrato compromete y el material **no** entrega. Nueve temas y varios bloques
dentro de temas parciales. Cada uno con dónde estaba y qué haría falta.

### 4.1 · La brecha que hay que mirar primero

| Módulo/tema | Nivel | Dónde estaba | Qué haría falta |
|---|---|---|---|
| **XXXV · Proyecto final integrador y su rúbrica** (M15) | **No cubierto** | `lab-13-capsula-y-egreso` (v0.8.0), con rúbrica de tres ejes, guía de defensa y solución de referencia | Recuperarlo y adaptarlo al arco nuevo. **Es el 50 % de la evaluación contratada** y hoy el material no tiene con qué evaluar |

El temario compromete `Evaluación: Proyecto final 50 % · Evaluación de conocimientos 30 % ·
Ejercicios 20 %` y `Aprobación: nota mínima 4,0 y 75 % de asistencia`. **El material no contiene
ningún instrumento de evaluación**: ni examen, ni rúbrica, ni proyecto. Los catorce labs son
construcción guiada, sin nota.

Es la única brecha que impide **cerrar el contrato**, no solo cubrir contenido.

### 4.2 · Temas no cubiertos

| Módulo/tema | Nivel | Dónde estaba | Qué haría falta |
|---|---|---|---|
| **XVII · gRPC** (M10) | No cubierto | `lab-08-diplomacia-con-tesoreria/demo-grpc` (v0.8.0), que compilaba y respondía, con job propio en el CI | **Se evaluó en la SPEC-037 §4 para el lab de microservicios y se descartó, medido:** en `repo-maven/` no hay runtime de gRPC ni `protobuf-maven-plugin`, y sobre todo **no hay `protoc`**, que viaja como binario por plataforma. Traerlo son la pila completa más dos ejecutables nativos en la maleta. Sigue siendo demo del relator, no práctica del alumno: la brecha es menor de lo que parece |
| **XIX · AOP** (M11) | No cubierto | `lab-09-caja-negra` (v0.8.0): aspecto de auditoría con RUT enmascarado | Un paso en el lab-11: `@Aspect` que audite el dominio. Cabe; el PO lo descartó al absorber el lab de AOP en observabilidad (SPEC-031 §0) |
| **XX · Manejo de archivos** (M11) | No cubierto | `lab-09-caja-negra` (v0.8.0): carga con MIME real por *magic bytes*, anti *path traversal*, descarga en streaming | Un lab propio o media sesión. Es el tema más grande de los que faltan y no tiene sitio natural en el arco actual |
| **XXV · Eventos de aplicación** (M12) | No cubierto | `lab-11-latidos` (v0.8.0): eventos `AFTER_COMMIT` | Un paso en el lab-12: `@TransactionalEventListener`. Encaja bien con el paso 3 (`@Async`) |
| **XXVI · Mensajería** (M13) | No cubierto | `lab-12-amortiguadores` (v0.8.0): RabbitMQ, DLQ, consumidor idempotente | Un lab de colas. **Descartado por el PO** (SPEC-032 §0): no corre sin Docker, y el SII no lo tiene |
| **XXIX · Caché** (M14) | No cubierto | `lab-10-observabilidad` antiguo (v0.8.0): Caffeine con TTL, hit-rate medido e invalidación | Un paso en el lab-11. Cabe sin forzar las tres horas |
| **XXXII · Liquibase** (M8) | No cubierto | Nunca se practicó: el mapa anterior ya lo declaraba como comparación conceptual | El temario dice «Flyway **y** Liquibase». Bastaría una comparación honesta de 15 min |
| **IV · Versionado nativo y OpenAPI** (M2) | No cubierto | Nunca estuvo en el arco nuevo; el antiguo tenía SpringDoc en el `pom` de la app canónica | Un paso en el lab-01 (Swagger UI navegable) y otro para el versionado de Framework 7 |

### 4.3 · Bloques que faltan dentro de temas parciales

| Módulo/tema | Nivel | Dónde estaba | Qué haría falta |
|---|---|---|---|
| **XXII · Testcontainers 2 y RestTestClient** (M6) | No cubierto dentro de un tema Parcial | La app canónica y los labs antiguos usaban Testcontainers con `@ServiceConnection` | **No es recuperable tal cual**: Testcontainers exige Docker, que el SII no tiene. El material lo sustituye por PostgreSQL embebido, que cubre la necesidad pero **no es el tema comprometido** |
| **XII · `ProblemDetail` (RFC 9457)** (M3) | No cubierto dentro de un tema Parcial | Nunca; el arco nuevo usa un record propio | Cambiar el tipo de respuesta del lab-03. Es una edición pequeña y alinea con el estándar |
| **XV · Method security `@PreAuthorize`** (M9) | Mencionado | `lab-07-el-portero` (v0.8.0) | Un paso en el lab-09 |
| **IX/M9 · CORS y cabeceras de seguridad** | No cubierto | `lab-08-diplomacia-con-tesoreria` (v0.8.0): CORS nominal y endurecimiento | Un paso en el lab-09. El temario lo pide explícitamente en la práctica de M9 |
| **XXVIII · OpenTelemetry** (M14) | Mencionado | La app canónica tenía trazas | El temario lo llama «ciudadano de primera clase». Hoy hay `traceId` propio en el MDC, que resuelve el problema dentro de una aplicación pero no es OTel |
| **XXXI · Mecánica de Flyway** (M8) | Parcial | — | El alumno escribe una migración (lab-07 paso 5) pero nadie le explica la tabla de historial, las repetibles ni el baseline |
| **M1 · `@ConfigurationProperties`** | No cubierto | — | La práctica de M1 lo pide con record validado. Hoy sólo se usa `@Value` |
| **M2 · PUT/PATCH/DELETE** | No cubierto | — | El «CRUD REST completo» del temario nunca actualiza ni borra por HTTP |
| **M5 · Paginación (`Pageable`)** | No cubierto | — | La práctica de M5 pide «un endpoint paginado con filtros y ordenamiento dinámico» |
| **M12 · Bloqueo distribuido** | Mencionado | `lab-11-latidos` (v0.8.0): candado en base con expiración | El lab-12 **muestra el problema y no lo resuelve**, declarado a propósito (D-032-5). El temario pide la tarea «protegida contra doble ejecución» |
| **M15 · Buildpacks y arranque acelerado** | No cubierto | `lab-13-capsula-y-egreso` (v0.8.0) empaquetaba con Buildpacks | El material usa **Jib** en su lugar, por la misma razón de siempre: Buildpacks necesita Docker. El resultado (imagen OCI) es el mismo; la herramienta comprometida, no |

**Todo lo recuperable está en el tag `material-v0.8.0`:**

```bash
git show material-v0.8.0:labs/lab-09-caja-negra/README.md
git checkout material-v0.8.0 -- labs/lab-13-capsula-y-egreso/
```

---

## 5 · Contenido nuevo que el temario no pedía

Suma, y hay que declararlo para que el SII sepa qué recibe de más.

| Qué | Dónde | Por qué está |
|---|---|---|
| **Un lab entero de testing** (3 h) | lab-08 | El temario dedica 1,5 h a M4 y 1,5 h a M6. El material le da **3 h a un lab de testing dedicado** con el rojo provocado en vivo. La encuesta dijo que **12 de 18 alumnos nunca escribió un test** |
| **Un lab entero de concurrencia** (3 h) | lab-07 | El temario cubre transacciones en M7 (2,0 h) junto con optimización. El material le da tres horas a la carrera entre hilos, con el fallo reproducido |
| **Un lab entero de inyección de dependencias** (3 h) | lab-02 | M1 son 2,0 h para fundamentos **y** configuración. La encuesta dijo que **17 de 18 no sabían explicar qué hace Spring Boot** |
| **Veinte minutos de «qué es un contenedor»** | lab-13 paso 3 | El temario da M15 por sabido a nivel de contenedores. En este grupo, nadie había visto uno |
| **Un lab entero de microservicios** (3 h) | lab-microservicios | El temario **no le asigna módulo ni horas**, pese a que el contrato se titula «Desarrollo de Microservicios en Java». La encuesta lo puso **primero de ocho, con 16 de 18 votos**. Cierra el hueco entre el título del contrato y su temario (§6.4) |
| **La maleta entera** (JDK, Maven, dependencias, PostgreSQL e imagen base en el repositorio) | todo el material | El temario exige `Docker Desktop`, `Red: acceso a Maven Central, Docker Hub y GitHub`. **Las máquinas del SII no tienen nada de eso.** El material corre sin red y sin Docker |

Ese último punto es una **desviación deliberada del contrato en los requisitos técnicos**, no en el
contenido, y conviene tenerla a mano: el temario §«Requisitos técnicos» compromete Docker y acceso
a Docker Hub. El material renunció a ambos porque en la sala no existen — y esa renuncia es la
causa directa de tres de las brechas (Testcontainers, mensajería con RabbitMQ, Buildpacks).

---

## 6 · Las discrepancias estructurales

Se declaran aquí porque afectan a la entrega y su resolución es del PO, no del material.

### 6.1 · Horas y sesiones

| | Contrato | Material |
|---|---|---|
| Horas | **36,0** | **45,0** (15 × 3 h) |
| Sesiones | **12** de 3 h | **15** de 3 h |

Nueve horas y tres sesiones por encima (eran seis y dos antes del lab de microservicios, ver §6.4). Y el reparto por módulo tampoco coincide: el material da
3 h a módulos que el contrato dota con 1,5 (M4, M6, M8) y menos de lo comprometido a otros
(M11 tiene 3,0 h contratadas y se cubre en parte de una sesión compartida).

### 6.2 · El orden

El contrato enseña seguridad (S07–S08) → comunicación (S08–S09) → logging/AOP (S09–S10) →
asincronía (S10) → mensajería (S10–S11) → observabilidad (S11–S12) → empaquetado (S12).

El material enseña **testing (08)** → seguridad (09) → resiliencia (10) → observabilidad (11) →
tareas (12) → empaquetado (13). El testing se adelantó a propósito —para que a partir de ahí todo
lo demás pueda probarse— y la mensajería desapareció.

### 6.3 · Un módulo por lab no es la regla

Cinco módulos se reparten entre labs (M1 en 00/02/13, M3 en 01/02/03, M5 en 04/05/06, M7 en 06/07,
M13 su mitad de resiliencia en 10 y 14) y tres labs cubren varios módulos (08 cubre M4 y parte de
M6; 11 cubre M14 y parte de M11; **14 toca M10, M13 y M14 sin ser el titular de ninguno**). La
tabla de la §1 lo refleja.

### 6.4 · El título del contrato promete algo que su temario no reparte

El contrato se llama **«Desarrollo de Microservicios en Java»**. Sus quince módulos y sus treinta
y cinco temas **no incluyen ninguno de microservicios**: hay comunicación entre servicios (M10) y
resiliencia (M13), que son piezas del tema, pero no la arquitectura, ni el gateway, ni una base por
servicio, ni la consistencia eventual.

Es una discrepancia del contrato consigo mismo, y hasta la SPEC-037 el material la heredaba: el
arco antiguo la cubría con `lab-14-la-dgt-se-parte-en-pedazos`, que se retiró con el resto del arco
(SPEC-033) por necesitar Docker Compose y seis servicios.

**El `lab-microservicios` (SPEC-037) la cierra**, con cuatro procesos que el alumno arranca a
mano y sin Docker. Con eso el material cubre el título además del temario. Lo que queda para el PO
es la aritmética: son **tres horas más** sobre un contrato que ya iba seis por encima, y el lab de microservicios
**no mapea a ningún módulo contratado como titular** — igual que le pasaba al Lab 14 antiguo, que
por eso estaba congelado.

---

## 7 · Qué hacer con esto

Tres decisiones, y las tres son del PO:

1. **El proyecto final (§4.1).** Es lo único que impide cerrar el contrato tal como está escrito.
   Recuperarlo del tag y adaptarlo es un trabajo acotado.
2. **Las brechas baratas.** Caché, eventos, AOP, `@PreAuthorize`, CORS y cabeceras, `ProblemDetail`
   y Liquibase son **un paso cada una** dentro de labs que ya existen. Sumadas, cubren siete de las
   brechas y no requieren un lab nuevo.
3. **Las brechas caras, que son consecuencia de no tener Docker.** Testcontainers, mensajería y
   Buildpacks. Aquí no hay solución técnica dentro del aula: o se negocia con el SII una
   sustitución declarada (embebido en vez de Testcontainers, Jib en vez de Buildpacks, y la
   mensajería fuera), o se consigue Docker.

---

*Mantener este documento al día es parte de cerrar cualquier SPEC que mueva contenido entre labs.
Si esta tabla y el temario discrepan, el error está aquí.*
