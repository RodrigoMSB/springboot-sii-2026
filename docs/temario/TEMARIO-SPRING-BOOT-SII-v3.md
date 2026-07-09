# PROPUESTA DE SERVICIOS — TALLER

# SPRING BOOT: DESARROLLO DE APLICACIONES EMPRESARIALES Y SERVICIOS EN JAVA

**Versión 3.0 · Julio 2026**

*Stack objetivo: **Java 25 LTS · Spring Boot 4.1.x · Spring Framework 7***
*(baseline del framework: Java 17; el curso compila y ejecuta sobre Java 25 LTS, compatible hasta Java 26)*

| **Parámetro** | **Definición** |
| --- | --- |
| **Duración** | 36 horas cronológicas sincrónicas (+ 8 h estimadas de trabajo autónomo) |
| **Sesiones** | 12 sesiones de 3 horas · 2 sesiones por semana · 6 semanas |
| **Cupos** | 18 participantes |
| **Modalidad** | Online sincrónica (Zoom / Teams), con grabación disponible |
| **Metodología** | 70 % práctica / 30 % teoría |
| **Módulos** | 15 módulos temáticos · cobertura de los 35 temas exigidos (I – XXXV) |
| **Evaluación** | Proyecto final 50 % · Evaluación de conocimientos 30 % · Ejercicios 20 % |
| **Aprobación** | Nota mínima 4,0 (escala 1–7) y 75 % de asistencia |

---

## Introducción

Spring Boot es el framework de referencia para la construcción de aplicaciones Java de nivel empresarial. Su autoconfiguración inteligente, su servidor web embebido y su ecosistema de starters permiten pasar de un proyecto vacío a un servicio productivo en minutos, sin sacrificar rigor arquitectónico.

La analogía es útil: si Java es el idioma y Spring Framework la gramática, Spring Boot es el corrector de estilo que además ya preparó la mesa, encendió el horno y dejó los ingredientes cortados. El desarrollador se dedica a cocinar la lógica de negocio; el andamiaje viene resuelto.

Esta propuesta se construye deliberadamente sobre **Spring Boot 4.1.x y Spring Framework 7**, la generación vigente y con soporte activo. La decisión no es cosmética: la línea 3.x completa alcanzó su fin de soporte de código abierto el 30 de junio de 2026, y Spring Boot 4 introduce cambios estructurales —Jackson 3, JUnit 6 como motor exclusivo de pruebas, Spring Security 7, clientes HTTP declarativos, primitivas de resiliencia en el núcleo del framework, soporte oficial de gRPC y observabilidad nativa sobre OpenTelemetry— que redefinen buena parte de las prácticas recomendadas. Un curso dictado sobre la línea 3.x formaría profesionales para un stack que ya no recibe parches de seguridad.

En cuanto a la plataforma: el requisito mínimo del framework es Java 17, pero el curso compila y ejecuta sobre **Java 25 LTS**, la versión de soporte extendido vigente. La diferencia no es solo de número: los hilos virtuales maduros, los records, el pattern matching completo y las mejoras de arranque de la JVM (caché AOT del Proyecto Leyden) cambian cómo se **escribe** el código moderno, no solo cómo se compila. Formar sobre un LTS con sucesor ya publicado sería repetir, un piso más arriba, el error de enseñar un framework sin soporte.

El programa está diseñado para desarrolladores con experiencia previa. No es una introducción a la programación: es un taller de ingeniería, donde cada módulo entrega herramientas que se acumulan sobre las anteriores y donde la calidad del código —arquitectura limpia, separación de responsabilidades, pruebas automatizadas— es criterio de evaluación y no una recomendación opcional.

## Objetivo General

Desarrollar capacidades para diseñar, construir, probar, observar y desplegar aplicaciones empresariales y servicios REST con Spring Boot 4, con énfasis en arquitectura limpia, seguridad, resiliencia e interoperabilidad entre servicios.

## Objetivos Específicos

Al finalizar el curso, los participantes estarán capacitados para:

- Crear aplicaciones Spring Boot 4 desde cero con configuración externalizada y perfiles multiambiente.
- Diseñar APIs REST versionadas con el soporte nativo de Spring Framework 7, documentadas con OpenAPI y protegidas con JWT sobre Spring Security 7.
- Aplicar una arquitectura en capas con DTOs, evitando la exposición directa del modelo de persistencia.
- Modelar entidades complejas con JPA e Hibernate 7, diagnosticar el problema N+1 y optimizar consultas con proyecciones e índices.
- Versionar el esquema de base de datos con Flyway y evaluar Liquibase como alternativa.
- Escribir pruebas unitarias con JUnit 6 y Mockito, y pruebas de integración con Testcontainers 2, desde etapas tempranas del desarrollo.
- Implementar comunicación entre servicios con clientes HTTP declarativos y patrones de resiliencia, distinguiendo las primitivas nativas del framework de las que aporta Resilience4j.
- Instrumentar aplicaciones con Actuator, Micrometer y trazas distribuidas sobre OpenTelemetry nativo.
- Empaquetar la aplicación como imagen de contenedor OCI y prepararla para despliegue productivo con arranque acelerado.

## Requisitos de Ingreso

### Conocimientos previos (obligatorios)

- Java 8 o superior: clases, interfaces, genéricos, colecciones y manejo de excepciones. Deseable familiaridad con Java 17+ (records, sealed types, switch expressions).
- Programación orientada a objetos: herencia, composición, polimorfismo e interfaces.
- Bases de datos relacionales y SQL: SELECT, JOIN, índices y modelo entidad-relación.
- Uso de línea de comandos, Git y un IDE (IntelliJ IDEA, VS Code o Eclipse).

*Se aplicará una prueba de diagnóstico previa (30 minutos, en línea) para confirmar nivel y homogeneizar el grupo. No es eliminatoria: orienta el ritmo del relator y permite entregar material de nivelación antes de la sesión 1.*

### Requisitos técnicos del participante

| **Recurso** | **Especificación mínima** |
| --- | --- |
| **Equipo** | 8 GB RAM (16 GB recomendado), 20 GB libres en disco, procesador x64 o ARM64 |
| **JDK** | **Java 25 LTS** (Temurin / Eclipse Adoptium). Compatible hasta Java 26 |
| **IDE** | IntelliJ IDEA (Community o Ultimate), VS Code + Extension Pack for Java, o Eclipse |
| **Contenedores** | Docker Desktop o Podman en ejecución — indispensable para Testcontainers (Módulo 6), RabbitMQ (Módulo 13) y empaquetado OCI (Módulo 15) |
| **Otros** | Git, Maven 3.9+ (o el wrapper del proyecto), cliente HTTP (Bruno, Postman o HTTPie) |
| **Red** | Acceso a repositorios Maven Central, Docker Hub y GitHub sin bloqueo de proxy corporativo |

*Se entregará una guía de instalación paso a paso y una sesión opcional de soporte técnico (1 hora, no computable) una semana antes del inicio, de modo que ningún minuto de clase se pierda configurando entornos.*

---

## Estructura del Programa

El programa se organiza en 15 módulos que agrupan los 35 temas exigidos por las bases. La secuencia responde a un criterio pedagógico explícito: las pruebas automatizadas se introducen tempranamente (Módulos 4 y 6) en lugar de relegarse al final, de modo que los participantes escriban código verificable desde la tercera sesión y no como un anexo del proyecto.

| **N°** | **Módulo** | **Horas** | **Temas SII** |
| --- | --- | --- | --- |
| **1** | Fundamentos de Spring Boot 4 y Configuración | 2,0 | I – II |
| **2** | Controladores REST, Versionado Nativo y OpenAPI | 2,5 | III – IV |
| **3** | Arquitectura en Capas, DTOs, Validaciones y Manejo de Excepciones | 3,0 | IX, XI – XII |
| **4** | Testing I: Pruebas Unitarias con JUnit 6 y Mockito | 1,5 | XXI |
| **5** | Persistencia con Spring Data JPA e Hibernate 7 | 4,0 | V – VIII |
| **6** | Testing II: Integración, RestTestClient y Testcontainers 2 | 1,5 | XXII |
| **7** | Transacciones y Optimización de Consultas | 2,0 | X, XXX |
| **8** | Migraciones de Base de Datos: Flyway y Liquibase | 1,5 | XXXI – XXXII |
| **9** | Spring Security 7 y Autenticación con JWT | 4,0 | XIII – XV |
| **10** | Comunicación entre Servicios: HTTP Declarativo y gRPC | 2,5 | XVI – XVII |
| **11** | Logging Estructurado, AOP y Manejo de Archivos | 3,0 | XVIII – XX |
| **12** | Asincronía con Hilos Virtuales, Scheduling y Eventos | 2,0 | XXIII – XXV |
| **13** | Mensajería y Resiliencia | 2,5 | XXVI, XXXIII |
| **14** | Observabilidad sobre OpenTelemetry, Métricas y Caché | 2,0 | XXVII – XXIX |
| **15** | Contenedores, Arranque Acelerado y Proyecto Final | 2,0 | XXXIV – XXXV |
| | **TOTAL** | **36,0** | **I – XXXV** |

*La sumatoria de horas de los 15 módulos equivale exactamente a las 36 horas cronológicas comprometidas.*

### Matriz Módulo × Sesión

Los 15 módulos se dictan en 12 sesiones de 3,0 horas. La tabla siguiente hace explícita la correspondencia, de modo que cada hora comprometida tenga sesión y minuto asignado. Las fracciones reflejan módulos que se completan a caballo entre dos sesiones consecutivas — el laboratorio de cada sesión integra los módulos que la componen.

| **Sesión** | **Semana** | **Módulos (horas asignadas)** | **Total** |
| --- | --- | --- | --- |
| S01 | 1 | M1 (2,0) + M2 (1,0) | 3,0 |
| S02 | 1 | M2 (1,5) + M3 (1,5) | 3,0 |
| S03 | 2 | M3 (1,5) + M4 (1,5) | 3,0 |
| S04 | 2 | M5 (3,0) | 3,0 |
| S05 | 3 | M5 (1,0) + M6 (1,5) + M7 (0,5) | 3,0 |
| S06 | 3 | M7 (1,5) + M8 (1,5) | 3,0 |
| S07 | 4 | M9 (3,0) | 3,0 |
| S08 | 4 | M9 (1,0) + M10 (2,0) | 3,0 |
| S09 | 5 | M10 (0,5) + M11 (2,5) | 3,0 |
| S10 | 5 | M11 (0,5) + M12 (2,0) + M13 (0,5) | 3,0 |
| S11 | 6 | M13 (2,0) + M14 (1,0) | 3,0 |
| S12 | 6 | M14 (1,0) + M15 (2,0) | 3,0 |

*Verificación: cada módulo suma sus horas declaradas (p. ej. M5: 3,0 + 1,0 = 4,0; M9: 3,0 + 1,0 = 4,0) y las 12 sesiones suman 36,0 horas. Cada sesión incluye su laboratorio práctico integrador.*

---

## Detalle de Módulos

### Módulo 1: Fundamentos de Spring Boot 4 y Configuración · 2,0 h · Temas SII: I – II

- Evolución de Spring Framework: del XML infernal a la autoconfiguración. Qué problema resolvió cada generación.
- Spring Boot 4 y Spring Framework 7: qué cambió respecto de la línea 3.x y por qué importa (Jackson 3, anotaciones de nulabilidad JSpecify, starters modulares más livianos, enfoque AOT-first).
- Contenedor IoC, beans, ciclo de vida y contexto de aplicación. Inversión de control explicada como delegación de responsabilidad. Registro programático de beans (`BeanRegistrar`) como novedad de Framework 7.
- Creación de proyectos con Spring Initializr: selección de starters, versión de Java y gestor de dependencias.
- Configuración externalizada: `application.yml` frente a `application.properties`. Jerarquía de fuentes de configuración.
- Perfiles: dev, test y prod. Activación por variable de entorno y por argumento de arranque.
- Propiedades tipadas con `@ConfigurationProperties` frente a `@Value`: validación, inmutabilidad con records y metadatos.

**Práctica:** *crear un proyecto Spring Boot 4 desde cero, configurar tres perfiles de ambiente y externalizar propiedades de negocio mediante un record anotado con `@ConfigurationProperties` validado.*

### Módulo 2: Controladores REST, Versionado Nativo y OpenAPI · 2,5 h · Temas SII: III – IV

- Definición de controladores con `@RestController` y diseño de recursos orientado a sustantivos, no a verbos.
- Mapeo de rutas: `@GetMapping`, `@PostMapping`, `@PutMapping`, `@PatchMapping` y `@DeleteMapping`.
- Manejo de parámetros: `@PathVariable`, `@RequestParam` y `@RequestBody`.
- Respuestas estructuradas con `ResponseEntity`: códigos de estado, cabeceras y ubicación de recursos creados.
- **Versionado de API nativo de Spring Framework 7**: negociación por ruta, cabecera o parámetro de consulta, sin bibliotecas de terceros.
- Serialización con Jackson 3: qué cambió respecto de Jackson 2 y las nuevas propiedades de configuración de Spring Boot 4.1.
- Integración de SpringDoc OpenAPI (línea compatible con Boot 4): generación automática de la especificación y Swagger UI navegable.
- Anotaciones de documentación: `@Operation`, `@ApiResponse`, `@Schema` y configuración de metadatos de la API.

**Práctica:** *construir un CRUD REST completo con códigos de estado correctos y publicar su documentación interactiva OpenAPI, incluyendo un endpoint versionado con el mecanismo nativo del framework.*

### Módulo 3: Arquitectura en Capas, DTOs, Validaciones y Manejo de Excepciones · 3,0 h · Temas SII: IX, XI – XII

- Separación de responsabilidades: Controlador, Servicio y Repositorio. Qué decisión vive en cada capa.
- Servicios con `@Service` e interfaces para desacoplar el contrato de la implementación (principio de inversión de dependencias).
- Inyección por constructor frente a `@Autowired` en campo: por qué la primera es la única defendible en código profesional.
- DTOs con records de Java: por qué nunca se serializa una entidad JPA hacia el exterior (ciclos, LazyInitializationException, filtración del esquema).
- Mapeo entidad ↔ DTO: mapeadores manuales frente a MapStruct. Costos y beneficios de cada enfoque.
- Validaciones declarativas con `@Valid`: `@NotNull`, `@NotBlank`, `@Size`, `@Min`, `@Max`, `@Email`, `@Pattern`.
- Validaciones personalizadas: anotaciones propias, `ConstraintValidator` y grupos de validación.
- Mensajes de error internacionalizables en `ValidationMessages.properties`.
- Manejo global de excepciones con `@RestControllerAdvice` y `@ExceptionHandler`.
- Excepciones de dominio propias y respuestas de error estandarizadas con `ProblemDetail` (RFC 9457).

**Práctica:** *refactorizar el CRUD hacia una arquitectura en capas con DTOs, validaciones personalizadas y un manejador global que responda siempre con ProblemDetail.*

### Módulo 4: Testing I: Pruebas Unitarias con JUnit 6 y Mockito · 1,5 h · Temas SII: XXI

- Por qué el testing se enseña aquí y no al final: probar es una técnica de diseño, no una tarea de cierre.
- Estructura Arrange–Act–Assert y nomenclatura de pruebas que documenta el comportamiento esperado.
- **JUnit 6 (Jupiter)**: qué cambia respecto de JUnit 5 y qué permanece. Nota de contexto: Spring Boot 4 eliminó JUnit 4 y su BOM gestiona Jupiter 6 — el material y los ejemplos del curso usan el motor que el alumno encontrará al crear un proyecto hoy.
- `@Test`, `@BeforeEach`, `@AfterEach`, `@DisplayName`, `@ParameterizedTest` y aserciones expresivas con AssertJ.
- Mockito: `@Mock`, `@InjectMocks`, distinción entre mock, stub y spy.
- Verificación de interacciones con `verify()` y captura de argumentos con `ArgumentCaptor`.
- Qué no se debe mockear: tipos que no son propios, objetos de valor y la propia clase bajo prueba.

**Práctica:** *escribir la suite unitaria completa de la capa de servicio construida en el Módulo 3, aislando el repositorio con Mockito y cubriendo los caminos de excepción.*

### Módulo 5: Persistencia con Spring Data JPA e Hibernate 7 · 4,0 h · Temas SII: V – VIII

- Configuración de JPA e Hibernate 7: origen de datos, dialecto y estrategia de generación de esquema.
- Anotaciones de entidad: `@Entity`, `@Id`, `@GeneratedValue`, `@Column`, `@Table`.
- Operaciones CRUD con `JpaRepository` y métodos de consulta derivados: `findBy`, `existsBy`, `countBy`, `deleteBy`.
- Consultas personalizadas con `@Query`: JPQL frente a SQL nativo. Cuándo cada uno.
- `JdbcClient` (sucesor moderno de JdbcTemplate): consultas fluidas, parámetros nombrados y operaciones por lote.
- Relaciones `@OneToOne`, `@OneToMany`, `@ManyToOne` y `@ManyToMany`. Unidireccional frente a bidireccional y quién es el dueño de la relación.
- `CascadeType` y `FetchType`: por qué EAGER es una trampa disfrazada de comodidad — y por qué el fetch se declara siempre explícito.
- El problema N+1: cómo detectarlo con el registro de SQL, cómo **medirlo** contando consultas, y cómo resolverlo con `@EntityGraph` y `JOIN FETCH`.
- Paginación con `Pageable` y `Page`, ordenamiento dinámico con `Sort`.
- Proyecciones de interfaz y de clase para no traer columnas que nadie usará.
- Personalización de la respuesta paginada expuesta en el endpoint REST (sin filtrar la estructura interna de `Page`).

**Práctica:** *modelar un dominio con cinco entidades relacionadas, provocar deliberadamente un N+1, medirlo, y resolverlo con `@EntityGraph`; exponer un endpoint paginado con filtros y ordenamiento dinámico.*

### Módulo 6: Testing II: Integración, RestTestClient y Testcontainers 2 · 1,5 h · Temas SII: XXII

- Pirámide de pruebas: por qué una prueba de integración lenta no reemplaza a diez unitarias rápidas.
- `@SpringBootTest` frente a slices de contexto: `@WebMvcTest` y `@DataJpaTest`.
- Prueba de controladores: MockMvc y **RestTestClient**, el cliente de pruebas HTTP unificado de Spring Framework 7. Verificación de estado, cabeceras y cuerpo JSON.
- Pruebas de repositorio con `@DataJpaTest` y transacciones de prueba con rollback automático.
- Bases de datos en memoria (H2): utilidad y su límite peligroso — el dialecto que engaña.
- **Testcontainers 2**: levantar PostgreSQL real en Docker durante la prueba. `@ServiceConnection`, reutilización de contenedores, y la advertencia práctica: la línea 2.x **renombró las coordenadas Maven** (`testcontainers-postgresql`), por lo que la mayoría de los tutoriales y respuestas en línea muestran las coordenadas antiguas.
- Integración con `spring-boot-docker-compose`: dependencias locales sin escribir cadenas de conexión.
- Estrategia de datos de prueba: fixtures, builders y aislamiento entre pruebas.

**Práctica:** *probar el CRUD de extremo a extremo y ejecutar la capa de repositorio contra un PostgreSQL real levantado con Testcontainers 2, usando `@ServiceConnection`.*

### Módulo 7: Transacciones y Optimización de Consultas · 2,0 h · Temas SII: X, XXX

- `@Transactional`: qué hace realmente el proxy y por qué la autoinvocación de un método no la activa.
- Propagación: REQUIRED, REQUIRES_NEW, NESTED, SUPPORTS y MANDATORY. Casos de uso reales — y el caso en que REQUIRES_NEW parece la solución y no lo es.
- Aislamiento: READ_COMMITTED, REPEATABLE_READ y SERIALIZABLE. Lecturas sucias, no repetibles y fantasma.
- Bloqueo pesimista y optimista: `SELECT … FOR UPDATE`, `@Version`, y el diseño de secuencias de negocio sin saltos.
- Rollback: comportamiento por defecto ante excepciones marcadas y no marcadas, y uso de `rollbackFor`.
- Transacciones de solo lectura y su efecto sobre el flush de Hibernate.
- Índices: cuándo ayudan, cuándo estorban y cómo verificarlo leyendo un plan de ejecución.
- Connection pooling con HikariCP: tamaño del pool, tiempos de espera y detección de fugas de conexión.

**Práctica:** *implementar una operación transaccional multitabla con una secuencia de negocio irrepetible bajo concurrencia, provocar el fallo y verificar el rollback; optimizar una consulta lenta midiendo antes y después.*

### Módulo 8: Migraciones de Base de Datos: Flyway y Liquibase · 1,5 h · Temas SII: XXXI – XXXII

- Por qué `ddl-auto=update` no es una estrategia de migración, sino una promesa de incidente en producción.
- Flyway en Spring Boot: configuración, convención de nombres `V1__descripcion.sql` y tabla de historial.
- Migraciones versionadas, repetibles y de reparación. Baseline sobre bases de datos existentes.
- Restricciones como contratos: llevar los invariantes del dominio a `CHECK` y `UNIQUE` mediante migraciones.
- Liquibase como alternativa: changesets en XML, YAML y SQL, y capacidad de rollback declarativo.
- Comparativa práctica: simplicidad y adopción de Flyway frente a portabilidad y rollback de Liquibase.
- Migraciones seguras en despliegue continuo: cambios compatibles hacia adelante y hacia atrás.

**Práctica:** *versionar el esquema del proyecto con Flyway desde cero, aplicar una migración correctiva que agregue una restricción de integridad, y replicar el flujo con Liquibase para contrastar ambas herramientas.*

### Módulo 9: Spring Security 7 y Autenticación con JWT · 4,0 h · Temas SII: XIII – XV

- Cadena de filtros de seguridad: cómo una petición atraviesa Spring Security antes de tocar el controlador.
- Configuración con `SecurityFilterChain` y los nuevos valores por defecto de Spring Security 7 (incluido el emparejamiento de rutas con `PathPatternRequestMatcher`).
- Autenticación con usuarios en memoria y contra base de datos mediante `UserDetailsService`.
- Almacenamiento de contraseñas: `PasswordEncoder`, BCrypt y Argon2. Por qué jamás se almacena texto plano ni MD5.
- Autorización por roles y autoridades: `@PreAuthorize`, `@PostAuthorize` y seguridad a nivel de método.
- Anatomía de un JWT: header, payload y signature. Firma simétrica frente a asimétrica.
- Enfoque didáctico: generación y validación de tokens con un filtro propio, para entender el mecanismo por dentro.
- Enfoque productivo: OAuth2 Resource Server. Validación de JWT declarativa, con `JwtDecoder` y rotación de claves.
- Refresh tokens, expiración, revocación y almacenamiento seguro en el cliente.
- Panorama de autenticación moderna: passkeys / WebAuthn como dirección de la industria (nivel conceptual).
- CORS: configuración correcta y errores frecuentes. CSRF: cuándo deshabilitarlo es legítimo y cuándo es negligencia.
- Cabeceras de seguridad (HSTS, CSP, X-Content-Type-Options) y limitación de tasa básica.

**Práctica:** *construir un flujo completo de login con emisión de JWT, proteger endpoints por rol, migrar la validación al OAuth2 Resource Server y endurecer la API con CORS, CSRF y cabeceras de seguridad.*

### Módulo 10: Comunicación entre Servicios: HTTP Declarativo y gRPC · 2,5 h · Temas SII: XVI – XVII

- Panorama de clientes HTTP en Spring Framework 7: `RestClient` (síncrono), `WebClient` (reactivo) y `RestTemplate` (legado, en mantenimiento).
- `RestClient`: API fluida, manejo de errores, tiempos de espera y autenticación.
- HTTP Interface Clients con `@HttpExchange`: definir el cliente como una interfaz Java y dejar que Spring genere la implementación. Registro por grupos, el enfoque recomendado actualmente.
- Seguridad del cliente HTTP: mitigación de SSRF con el filtrado de direcciones incorporado en Spring Boot 4.1.
- Propagación automática de contexto de trazas (OpenTelemetry) en clientes declarativos de Spring Framework 7.
- Spring Cloud OpenFeign: configuración con `@FeignClient` e interceptores. Estado actual: proyecto en mantenimiento — criterios para sostenerlo en sistemas existentes y para no elegirlo en desarrollos nuevos.
- **Spring gRPC**, ahora con soporte oficial en Spring Boot 4.1: qué es gRPC, cuándo elegirlo frente a REST, y demostración de un servicio con su starter (nivel conceptual + demo del relator).
- Comparativa fundamentada: `RestClient` + `@HttpExchange` frente a Feign frente a WebClient frente a gRPC.
- Tiempos de espera, reintentos y degradación elegante ante servicios externos indisponibles.

**Práctica:** *consumir una API externa con RestClient, refactorizarla hacia un HTTP Interface Client declarativo con timeouts correctos, y comparar contra un cliente Feign equivalente; demostración guiada de un servicio gRPC.*

### Módulo 11: Logging Estructurado, AOP y Manejo de Archivos · 3,0 h · Temas SII: XVIII – XX

- Logback y Log4j2: configuración, appenders y rotación de archivos (incluida la rotación de archivos para Log4j incorporada en Boot 4.1).
- Niveles de registro (TRACE, DEBUG, INFO, WARN, ERROR) y el criterio para elegir cada uno.
- Registro estructurado en JSON: soporte nativo de Spring Boot y su valor en entornos de agregación de logs.
- MDC (Mapped Diagnostic Context) y correlación de peticiones mediante identificador de traza.
- Programación orientada a aspectos: aspecto, punto de unión, punto de corte y consejo. La preocupación transversal como concepto.
- Aspectos con `@Aspect` y tipos de consejo: `@Before`, `@After`, `@AfterReturning`, `@AfterThrowing` y `@Around`.
- Expresiones de punto de corte: sintaxis, combinadores y patrones de selección.
- Casos de uso: auditoría, medición de tiempos de ejecución y registro transversal sin contaminar la lógica de negocio.
- Límites de AOP basado en proxies: la autoinvocación no se intercepta (misma trampa que `@Transactional`).
- Carga de archivos con `MultipartFile`: validación de tamaño, tipo MIME real y saneamiento del nombre.
- Almacenamiento local y en servicios de objetos (S3-compatible) tras una abstracción propia.
- Descarga y transmisión de archivos grandes sin agotar la memoria.

**Práctica:** *instrumentar la aplicación con registro estructurado y MDC, crear un aspecto de auditoría que registre toda invocación al dominio, y construir un servicio de carga y descarga de archivos con validación estricta.*

### Módulo 12: Asincronía con Hilos Virtuales, Scheduling y Eventos · 2,0 h · Temas SII: XXIII – XXV

- `@EnableScheduling` y `@Scheduled`: tareas periódicas dentro de la aplicación.
- Expresiones cron: sintaxis, zonas horarias y errores clásicos de interpretación.
- `fixedRate` frente a `fixedDelay`: la diferencia que arruina un proceso nocturno.
- Tareas programadas en entornos con múltiples instancias: el problema de la ejecución duplicada y sus soluciones (bloqueo distribuido).
- **Hilos virtuales, ya maduros en Java 25**: activación en Spring Boot, qué cargas se benefician realmente y qué cambia en el dimensionamiento de pools. Structured concurrency como dirección de la plataforma (vista conceptual).
- `@EnableAsync` y `@Async`: ejecución fuera del hilo de la petición, sus trampas (autoinvocación, pérdida de contexto) y su relación con los hilos virtuales.
- Configuración de ejecutores propios y manejo de excepciones en métodos asíncronos.
- `CompletableFuture` para componer resultados asíncronos.
- Eventos de aplicación: `ApplicationEventPublisher`, `@EventListener` y eventos de dominio propios.
- `@TransactionalEventListener`: publicar eventos solo cuando la transacción confirmó. Desacoplamiento sin inconsistencia.

**Práctica:** *implementar una tarea programada de depuración de datos protegida contra doble ejecución, procesar notificaciones de forma asíncrona sobre hilos virtuales, y desacoplar un efecto secundario del dominio mediante un evento transaccional.*

### Módulo 13: Mensajería y Resiliencia · 2,5 h · Temas SII: XXVI, XXXIII

- Mensajería asíncrona: por qué una cola es un amortiguador entre sistemas con ritmos distintos.
- RabbitMQ frente a Apache Kafka: cola de mensajes contra registro distribuido de eventos. Criterios de elección.
- Spring AMQP: configuración de exchanges, colas y bindings.
- Productores y consumidores: envío, recepción, acuse de recibo y reintentos.
- Dead letter queues: qué hacer con el mensaje que nadie pudo procesar.
- Idempotencia del consumidor: la garantía que hace tolerable la entrega duplicada.
- Patrones de resiliencia: circuit breaker, retry, rate limiter, bulkhead y time limiter.
- **Primitivas de resiliencia nativas de Spring Framework 7**: `@Retryable` y `@ConcurrencyLimit` en el núcleo del framework. Cuándo bastan.
- Resilience4j en Spring Boot: circuit breaker completo, configuración declarativa, estados y métodos de respaldo. Criterio de elección frente a las primitivas del núcleo.

**Práctica:** *publicar y consumir mensajes con RabbitMQ levantado en contenedor, enrutar los fallos a una dead letter queue con consumidor idempotente, y proteger una llamada externa combinando las primitivas nativas con un circuit breaker de Resilience4j.*

### Módulo 14: Observabilidad sobre OpenTelemetry, Métricas y Caché · 2,0 h · Temas SII: XXVII – XXIX

- Los tres pilares de la observabilidad: métricas, trazas y registros. Qué pregunta responde cada uno.
- Spring Boot Actuator: endpoints estándar (`/health`, `/info`, `/metrics`) y su exposición controlada.
- Health indicators propios y sondas de disponibilidad (liveness y readiness) para orquestadores.
- Endpoints de gestión personalizados con `@Endpoint`.
- Micrometer: contadores, temporizadores y medidores. Métricas de negocio, no solo de infraestructura.
- **OpenTelemetry como ciudadano de primera clase** en Spring Boot 4.1: trazas distribuidas con propagación automática, exportación OTLP y correlación traza-log.
- Exportación a Prometheus y visualización en Grafana (nivel conceptual y demostración).
- Abstracción de caché de Spring: `@Cacheable`, `@CachePut`, `@CacheEvict` y sus condiciones.
- Caffeine como proveedor local: tamaño, expiración y estadísticas. Redis como caché distribuida (nivel conceptual).
- Qué se cachea y qué no: invalidación, consistencia y el costo de un caché mentiroso.

**Práctica:** *exponer métricas de negocio propias vía Micrometer, seguir una petición de extremo a extremo con trazas OpenTelemetry correlacionadas con los logs, y acelerar una consulta costosa con Caffeine midiendo la tasa de aciertos.*

### Módulo 15: Contenedores, Arranque Acelerado y Proyecto Final · 2,0 h · Temas SII: XXXIV – XXXV

- Empaquetado ejecutable: jar por capas y su efecto en la reconstrucción de imágenes.
- `spring-boot:build-image` con Buildpacks: imagen OCI sin escribir un Dockerfile. Dockerfile explícito como alternativa y cuándo conviene.
- **Arranque acelerado**: caché AOT de la JVM (Proyecto Leyden, disponible en Java 25) y compilación nativa con GraalVM — qué promete cada vía, qué cuesta y cómo decidir (demostración del relator).
- Configuración por variables de entorno y gestión de secretos: nada sensible viaja dentro de la imagen.
- `compose.yaml` integrado: levantar las dependencias locales del proyecto con soporte nativo de Spring Boot.
- Apagado elegante (graceful shutdown) y sondas de orquestador: la aplicación como buen ciudadano del clúster.
- Presentación del proyecto final integrador: alcance, rúbrica y defensa.

**Práctica:** *empaquetar el proyecto del curso como imagen OCI con Buildpacks, verificar su arranque con perfiles productivos y sondas activas, y presentar el proyecto final ante la rúbrica.*

---

## Proyecto Final Integrador

El proyecto final es la columna de evaluación del curso (50 % de la nota). Cada participante construye, sobre el dominio trabajado en los laboratorios, un servicio completo que demuestre las competencias de los 15 módulos. Se entrega con código fuente, migraciones, suite de pruebas, documentación OpenAPI e imagen de contenedor.

### Rúbrica del proyecto final

| **Criterio** | **Peso** | **Qué se evalúa** |
| --- | --- | --- |
| Arquitectura y diseño | 20 % | Separación de capas, DTOs, inversión de dependencias, ausencia de fugas del modelo de persistencia |
| Correctitud funcional | 20 % | Los casos de uso comprometidos funcionan; manejo de errores con ProblemDetail |
| Pruebas automatizadas | 20 % | Unitarias e integración con Testcontainers; los invariantes de negocio están cubiertos |
| Seguridad | 15 % | Autenticación JWT operativa, autorización por rol, contraseñas correctamente almacenadas |
| Persistencia y rendimiento | 15 % | Esquema migrado con Flyway, consultas sin N+1, transacciones correctas |
| Observabilidad y despliegue | 10 % | Actuator configurado, métricas de negocio, imagen OCI funcional |

*La evaluación de conocimientos (30 %) se rinde en la última semana. Los ejercicios de módulo (20 %) se recogen mediante los reportes entregables de cada laboratorio.*

---

## Bibliografía y Recursos

- Documentación oficial de Spring Boot 4 y Spring Framework 7 (docs.spring.io).
- *Spring in Action*, 7.ª edición — Craig Walls.
- *High-Performance Java Persistence* — Vlad Mihalcea.
- *Release It! Design and Deploy Production-Ready Software*, 2.ª edición — Michael T. Nygard.
- Guías de referencia de Testcontainers 2, Flyway y RabbitMQ.

---

## Anexo: Registro de cambios v2 → v3

| # | Cambio | Razón |
| --- | --- | --- |
| 1 | Plataforma: Java 21 LTS → **Java 25 LTS** (portada, requisitos, Módulos 12 y 15) | Java 25 es el LTS vigente desde septiembre de 2025. Declarar 21 en julio de 2026 repetiría el desfase que la v2 corrigió respecto del framework. Se explicita además el baseline real del framework (17) para eliminar la contradicción interna. |
| 2 | Testing: JUnit 5 → **JUnit 6 (Jupiter)** (Módulos 4 y 6) | El BOM de Spring Boot 4.1 gestiona JUnit Jupiter 6. El alumno que cree un proyecto hoy encontrará JUnit 6; enseñar "JUnit 5" introduce una discrepancia inmediata con su entorno. |
| 3 | **Matriz Módulo × Sesión** (nueva sección) | Los 15 módulos y las 12 sesiones existían como tablas independientes sin correspondencia explícita. La matriz asigna cada hora comprometida a una sesión concreta y hace verificable la sumatoria por ambas vías. |
| 4 | Módulo 6: **Testcontainers 2** con advertencia de coordenadas renombradas; se añade **RestTestClient** | Testcontainers 2.x renombró sus artefactos Maven; el material en línea muestra mayoritariamente las coordenadas antiguas. RestTestClient es el cliente de pruebas HTTP introducido por Spring Framework 7. |
| 5 | Módulo 10: se añade **Spring gRPC** (conceptual + demo) y la mitigación de **SSRF** del cliente HTTP | Ambos son novedades de Spring Boot 4.1 (junio 2026). gRPC se incorpora a nivel conceptual sin alterar las horas del módulo. |
| 6 | Módulo 2: se explicita **Jackson 3** y sus propiedades de configuración | Cambio estructural de Boot 4 con impacto directo en la serialización que el alumno escribirá. |
| 7 | Módulo 7: se añade **bloqueo pesimista/optimista y secuencias de negocio sin saltos** | Prepara el terreno del laboratorio de concurrencia sin alterar los temas SII cubiertos (X, XXX). |
| 8 | Módulo 8: se añade **restricciones como contratos** (CHECK/UNIQUE vía migración) | Los invariantes de dominio se llevan a la base de datos mediante migraciones: práctica profesional y conexión directa con el proyecto final. |
| 9 | Módulo 9: se añade panorama de **passkeys / WebAuthn** (conceptual) | Dirección actual de la industria en autenticación; se menciona sin costo de horas. |
| 10 | Módulo 12: hilos virtuales pasan de mención a **eje del módulo**, con structured concurrency como vista conceptual | En Java 25 los hilos virtuales son la opción por defecto razonable para cargas bloqueantes; el temario los trataba como novedad periférica. |
| 11 | Módulo 14: OpenTelemetry pasa de mención a **ciudadano de primera clase** | Spring Boot 4.1 actualizó y profundizó el soporte nativo de OTel; el framing "Micrometer Tracing como reemplazo de Sleuth" quedó obsoleto. |
| 12 | Módulo 15: se añade **caché AOT de la JVM (Proyecto Leyden)** junto a GraalVM | Estado del arte en arranque acelerado sobre Java 25: dos vías con costos distintos, presentadas comparativamente. |
| 13 | Módulo 11: se generaliza "AWS S3, Azure Blob" a **"servicios de objetos (S3-compatible)"** | Neutralidad de proveedor para una institución pública; la abstracción propia es el contenido, no la marca. |

*Lo que NO cambió: la estructura de 15 módulos, las horas por módulo (36,0 en total), la cobertura de los 35 temas SII (I–XXXV), la calendarización (12 × 3 h), las ponderaciones de evaluación (50/30/20), los criterios de aprobación y la rúbrica del proyecto final. La v3 actualiza el contenido al estado del arte de julio de 2026 sin alterar ningún compromiso estructural de la propuesta adjudicada.*
