# ¿En qué va el curso?

*Una página, sin jerga. Si llevas dos semanas sin mirar el repo, empieza aquí.*
*Última actualización: SPEC-032 — nacen los labs 08, 09, 11 y 12; el arco nuevo llega hasta el empaquetado (tag `material-v0.8.0`).*

---

## 1 · Qué existe hoy

- **El temario definitivo** (v3, julio 2026): `docs/temario/`. Son 36 horas, 12 sesiones
  de 3, 15 módulos. El `.md` manda; el `.docx` es lo que se le entrega al SII.
- **La aplicación del curso**: `dgt-tramites-api/`. Es el backend de la DGT — lo que hay
  detrás del botón. Arranca, se conecta a su base de datos y responde. Tiene siete reglas
  de arquitectura que la vigilan, y cada regla trae una prueba de que muerde.
- **EL ARCO VIGENTE — trece labs de construcción guiada (00 a 12, con el hueco del 10).** El alumno construye en
  vivo junto al instructor: `README.md` + `PASOS.md` + `practica/` + `solucion/`, sin tests ni
  validadores, y se corren con `./mvnw spring-boot:run` sin Docker ni instalar nada. Nacieron
  porque la encuesta a los 18 alumnos dijo que **17 no saben explicar qué hace Spring Boot** y
  que un tercio no programa en Java, y el material arrancaba reparando un secreto filtrado.
  - `labs/lab-00-hola-mundo/` — que arranque. Una clase, una anotación, un `main`. 15 min.
  - `labs/lab-01-web/` — el primer endpoint. Ruta, parámetro, cuerpo, y `ResponseEntity`.
  - `labs/lab-02-di/` — **el lab que explica qué es Spring**. Dos implementaciones de la misma
    interfaz, la app que deja de arrancar, y `/productos/quien` diciendo cuál se inyectó.
  - `labs/lab-03-errores/` — el camino triste con contrato: 404 con cuerpo, 400 con los campos
    que fallaron, y el mensaje interno que nunca sale.
  - `labs/lab-03b-jpa/` — **JPA desde cero**. Una clase y una tabla son la misma cosa; el SQL
    sale en la consola y la base se puede mirar por fuera con DBeaver mientras corre.
  - `labs/lab-04-relaciones/` — `@ManyToOne`, `@OneToMany(mappedBy)`, LAZY y la
    `LazyInitializationException`. El número: 1 SELECT con LAZY, **4 con EAGER**.
  - `labs/lab-05-rendimiento/` — el N+1 con un contador de consultas en pantalla. El número:
    **201 consultas contra 1**, con `JOIN FETCH`, `@EntityGraph` o proyección.
  - `labs/lab-06-concurrencia/` — 20 emisiones simultáneas del mismo folio. El número: sin
    candado salen 9 o 10 números distintos de 21, con repetidos; con candado, **21 de 21**.
  - `labs/lab-07-testing/` — **el lab de testing**, y el hueco más grande que dejó la encuesta:
    12 de los 18 alumnos nunca escribió un test automatizado. El proyecto llega entero y andando
    y sin un solo test; se escriben nueve en seis pasos —JUnit, `assertThrows`, Mockito,
    `@WebMvcTest` con `MockMvc`, y un `@SpringBootTest`—. El momento del lab es el paso 2: se
    rompe el IVA a propósito y sale `expected: <5938> but was: <5489>`. El número del cierre:
    **de 0,03 s sin Spring a 0,7 s con Spring, veinte veces**. Sin base de datos, a propósito.
    Estrena la **tercera carpeta**, `instructor/` (ver abajo).

  - `labs/lab-08-seguridad/` — la API abierta se cierra. El default de Spring Security (todo 401
    sin escribir una línea), la cadena de filtros, **BCrypt con sal** —dos hashes distintos para la
    misma clave, mirados en la tabla con DBeaver—, el JWT que se decodifica en vivo para ver que
    **cualquiera lo lee**, el filtro que lo valida, y la matriz final: **401 sin token, 403 con
    token y sin rol**. Usuarios en PostgreSQL embebido; productos en memoria.
  - `labs/lab-09-resiliencia/` — el vecino que no responde. Tesorería es WireMock dentro del mismo
    proceso, con mando a distancia. Los números: **30,01 s** con el cliente ingenuo, 2,04 s con
    timeout, **6,44 s con tres intentos** (el reintento empeora la caída), y **0,002 s con cero
    llamadas HTTP** cuando el circuito abre. Resilience4j núcleo declarado a mano, con las
    transiciones CLOSED→OPEN→HALF_OPEN→CLOSED en consola.
  - **falta el `lab-10-observabilidad`** — hueco conocido: el nombre lo ocupa el lab del arco
    antiguo y el PO decidió no inventar uno provisional. Se construye en la SPEC de reempaquetado
    (SPEC-032 §10.1).
  - `labs/lab-11-tareas/` — `fixedDelay` frente a `fixedRate`, el cron de **seis** campos con zona
    explícita, `@Async` (**3,03 s → 0,004 s**) con sus tres trampas, y los hilos virtuales en una
    línea de YAML. El paso 5 levanta **dos instancias** y el cierre nocturno se ejecuta **dos
    veces en el mismo segundo**; la solución (candado distribuido) se nombra y no se implementa.
  - `labs/lab-12-empaquetado/` — el cierre del curso. El fat jar (20,9 MB) y `java -jar`, el jar
    por capas, veinte minutos de **qué es un contenedor** sin teclear nada, una **imagen OCI de
    138,9 MB construida con Jib sin Docker y sin red**, abierta con `tar` para contar sus diez
    capas, y la misma imagen en tres entornos sin recompilar.

- **La imagen base de Jib viaja en el repositorio (SPEC-032).** `tools/jib-base/`, **122 MB**: las
  capas de `eclipse-temurin:25-jre`. Mismo criterio que `tools/jdk/` y `repo-maven/` — sin eso, el
  Lab 12 intentaría bajar la imagen de un registro y en el SII no correría. Va marcada `binary` en
  `.gitattributes` (el `text=auto` la corrompería) y se regenera con
  `rm -rf tools/jib-base && DGT_ONLINE=1 ./mvnw package jib:buildTar` desde `lab-12-empaquetado/`.

- **La estructura de tres carpetas (SPEC-031).** Rige desde el `lab-07-testing`:
  `practica/` **sin documentación** —la firma, una línea imperativa y `// escribe aquí`—,
  `solucion/` con comentarios **breves**, e `instructor/` con **todo explicado línea por línea**.
  La tercera **no viaja al repositorio**: está en el `.gitignore` de la raíz
  (`labs/*/instructor/`) porque es la chuleta de quien dicta — si el alumno la tuviera en el
  clon, leería la explicación en vez de escuchar. No es un proyecto: sin `mvnw`, sin `.mvn`, sin
  `target`. La generan de nuevo, a partir de `solucion/`, quien prepare la sesión.
  **Los labs 00 a 06 todavía NO están migrados** a esta forma: su `practica/` conserva los
  bloques explicativos largos. La migración es una SPEC de reempaquetado pendiente del PO.

- **Lab 07 del arco antiguo** (`labs/lab-07-el-portero/`) — ⚠️ **choca de número con el
  `lab-07-testing` del arco nuevo**; conviven sin estorbarse (son proyectos independientes) y la
  renumeración del arco nuevo sigue pendiente. Se citan por nombre completo, nunca como «el Lab
  07». Su contenido: la seguridad. La API se cierra por defecto (Spring
  Security 7), hay login real contra la tabla de usuarios (BCrypt de la semilla), el JWT se
  valida por su firma (no se cree — un token adulterado da 401), y la emisión de folios exige
  el rol FUNCIONARIO (403 para el resto). El secreto de firma vive fuera del repo. Es el primer
  lab que **rompe hacia atrás**: los tests heredados ganaron autenticación, declarados en la
  derivación.
- **Lab 08** (`labs/lab-08-diplomacia-con-tesoreria/`): la resiliencia. Entra TESO (WireMock),
  el servicio externo que confirma pagos. El cliente ingenuo sin timeout cuelga la API entera
  cuando TESO se pone lento; se resuelve con timeout corto y dirigido, degradación elegante
  (503 rápido, el trámite intacto), el cliente migrado a `@HttpExchange`, y el endurecimiento
  (CORS nominal + cabeceras). La hora de M9 diferida del Lab 07 se cobra aquí.
- **Lab 09** (`labs/lab-09-caja-negra/`): la observabilidad. El sistema mudo (texto plano, sin
  correlación) donde buscar un folio en el log es imposible. Se resuelve con `traceId` por
  petición en el MDC, logging JSON estructurado, un aspecto de auditoría (AOP) que registra el
  dominio sin ensuciarlo (RUT enmascarado, respeta el límite del proxy), y carga de adjuntos con
  desconfianza (MIME real por magic bytes, anti path-traversal, descarga en streaming).
- **Lab 10** (`labs/lab-10-observabilidad/`): el tablero que mentía. `/actuator/health` responde
  `UP` con la base muerta y la API devolviendo 500. Se resuelve con un health check propio que
  consulta la base y **nombra** lo que se cayó, liveness y readiness separados (reiniciar y sacar
  de rotación son acciones opuestas), métricas de negocio con Micrometer publicadas en formato
  Prometheus, lista blanca nominal de endpoints (`/env` y `/heapdump` dejan de existir) y un caché
  Caffeine con TTL, hit-rate medido e invalidación explícita al escribir.
- **Lab 11** (`labs/lab-11-latidos/`): el reloj con problema de identidad. El cierre nocturno se
  ejecuta una vez por instancia: con dos servidores, dos cierres, totales duplicados y el mismo
  aviso dos veces al contribuyente. Se resuelve con un candado distribuido en la base (atómico, con
  expiración y con el reloj del motor, no el de cada máquina), `fixedDelay` en vez de `fixedRate`,
  el cron con zona `America/Santiago` explícita, notificaciones asíncronas sobre hilos virtuales de
  Java 25, y eventos `AFTER_COMMIT` — si la transacción revierte, el aviso no sale.
- **Lab 12** (`labs/lab-12-amortiguadores/`): mensajería y resiliencia. El aviso se manda al aire:
  con el servicio de avisos caído se evapora, la API responde 201 y nadie puede decir cuáles se
  perdieron. Se resuelve entregándolo a una cola durable (RabbitMQ), con un consumidor idempotente
  —«exactly once» no existe—, una DLQ que aparta al mensaje envenenado con su causa sin atascar a
  los buenos, y un circuit breaker que deja de golpear a Tesorería cuando está caída.
- **Lab 13** (`labs/lab-13-capsula-y-egreso/`): el **examen de egreso**, y el único lab sin crimen.
  El alumno recibe un *brief* de negocio deliberadamente incompleto en los bordes, y tres horas para
  entregar un consolidado correcto, seguro, probado por él y empaquetado como imagen OCI. Su
  validador no cuenta huecos: emite un **boletín de tres ejes** (Correctitud · Oficio · Criterio)
  que **declara quién mide cada uno** y **no puede aprobar a nadie** — el eje Criterio es humano y
  el umbral es núcleo verde *y* criterio ≥ Suficiente. Trae rúbrica, guía de defensa con respuestas
  calibradas por nivel, y una `solucion-referencia/` que dice de entrada que es UNA solución.
- **Lab 14** (`labs/lab-14-la-dgt-se-parte-en-pedazos/`) — ⚠️ **CONGELADO, sin módulo contractual**:
  el lab de **microservicios**. Y es el único que no se teclea: se levanta un sistema
  de **seis piezas** —registro Eureka, Config Server, gateway, dos instancias del proveedor y el
  consumidor—, se rompe y se mira. Los seis patrones funcionando (discovery, gateway, configuración
  centralizada, Feign, balanceo, circuit breaker con fallback) sobre Spring Cloud **2025.1.2** y
  Boot 4.1.0. El crimen es que **no se cae**: con una pieza apagada el portal devuelve HTTP 200 con
  un JSON válido al que le falta el nombre del titular, y nadie avisa. El único tecleo son cuatro
  umbrales de Resilience4j, y el criterio es medible: con los valores por defecto hacen falta CIEN
  llamadas para que el circuito opine, así que en la sesión no abre nunca. La teoría dedica una
  sección entera a **cuándo NO usar microservicios**, con los costos medidos de este mismo lab
  (1,54 GiB de RAM y 56 s de arranque, frente a una sola pieza).
  **Está construido y verificado, y NO se dicta**: la auditoría SPEC-AUDIT-01 constató que el
  temario contratado no tiene módulo de microservicios ni sesión 14. Su destino lo decide el PO.
- **La portabilidad, blindada**: `.gitattributes` en la raíz fija los finales de línea (LF
  para los `.sh`, CRLF para los `.cmd`). Sin él, Git para Windows convertía los scripts al
  clonar y bash dejaba de ejecutarlos con un `bad interpreter` incomprensible — un bloqueo
  total del alumno de Windows el día 1. El CI vigila que no se degrade.
- **El toolchain, fijado**: `.sdkmanrc` en la raíz (`java=25-tem`). Quien clone el repo con SDKMAN
  cae solo en la versión correcta; sin él, el pom compila contra Java 25 y nada lo declaraba.
- **La caja de herramientas** de los scripts: `labs/lib/lib-comunes.sh`. La comparten los
  catorce labs. El Lab 14 le añade `bin/lib-sistema.sh`, su vocabulario propio para hablarle a
  seis procesos en vez de a uno.
- **El manifiesto pedagógico**: `MANIFIESTO.md`, en la raíz. Por qué el curso se enseña así —el
  concepto por encima de la receta, la memoria narrativa, el aprendizaje que ocurre en la sala o
  no ocurre— y qué se le pide a quien dicta. Es texto de autor del PO. Quien vaya a dar una
  sesión lo lee **antes** que cualquier otra cosa del repo; el README enlaza a él desde arriba.
- **La memoria del proyecto**: por qué se decidió cada cosa está en `docs/decisiones.md`.
  Las especificaciones, en `docs/specs/`.
- **Un CI que muerde**: cada cambio comprueba que el temario cuadra, que los scripts son
  correctos, que la aplicación pasa su suite completa (106 tests en el Lab 13) y que el sistema de
  microservicios del Lab 14 compila **y que su gate muerde** — el CI exige que el starter FALLE, no
  solo que la solución pase.

## 1.b · Alcance contra el contrato (SPEC-AUDIT-01 y SPEC-FIX-02)

El mapa **lab ↔ módulo del temario** vive en `docs/temario/MAPA-LAB-MODULO.md`. Es el documento
de trazabilidad para la entrega al SII, y dice tres cosas que conviene tener presentes:

- **Los 15 módulos contratados tienen laboratorio.** La numeración del material ya cuadra con
  la del temario (se corrigió en los labs 03, 09, 10, 11, 12 y 13).
- **Dos discrepancias estructurales siguen abiertas y son del PO**, no del material: los labs
  10 a 13 ocupan cuatro sesiones donde el contrato compromete tres (S10–S12) y en otro orden;
  y el temario compromete **12 sesiones**, mientras el material llega a la 14.
- **El Lab 14 está congelado**: no mapea a ningún módulo contratado.

Git salió del Lab 01 y Python de las guías del Lab 14: ninguno de los dos es materia de un
curso de Spring Boot. Y la teoría del Lab 01 se reordenó (SPEC-FIX-03) para que **responda al
crimen en su sección 1** en vez de en la 7: el bloque fundacional —contenedor y
autoconfiguración— entra después, por una puerta declarada. gRPC entró en el Lab 08 —teoría y demo ejecutable— porque el M10
contratado lo promete y no estaba.

## 1.c · La emancipación de Docker · el material autocontenido (SPEC-022 y SPEC-023)

Tres hechos del terreno, y la estrategia sale sola:

1. Las máquinas de los alumnos del SII **no tienen Docker y no pueden instalarlo** (sin admin).
2. Su firewall bloquea todo internet **salvo el Nexus interno `apus.sii.cl:8081`**… que solo se
   puede probar desde dentro del SII, y **no habrá VPN jamás**.
3. **GitHub sí funciona** desde esas máquinas, rápido y sin problemas. Es la única puerta
   confirmada abierta.

La conclusión: **todo lo que el curso necesita viaja DENTRO del repositorio**. El alumno hace
`git clone` y el material funciona sin volver a tocar la red nunca más.

**Estado: Fase 0 ejecutada y verificada. Labs 01 y 02 convertidos.** El informe está en
`docs/specs/informes/INFORME-SPEC-022.md`.

- **Docker fuera del material.** PostgreSQL llega como dependencia Maven (Zonky): se extrae a
  una carpeta temporal y corre como proceso hijo del JVM. Sin demonio, sin admin. Es PostgreSQL
  **de verdad** —16.14, el mismo motor— no un H2 disfrazado. Empezó en los labs que la SPEC-022
  migró (ya retirados con el arco antiguo), hoy lo usan **los ocho labs del arco vigente** y los
  labs **08 al 11**, que lo estrenaron en la SPEC-025.
- **`repo-maven/` en la raíz**: 224 MB, 274 jars. Todas las dependencias de los dos labs.
- **`tools/maven/`**: la distribución de Maven, 10 MB, commiteada.
- **`mvnw` ya no descarga nada**: es un shim que usa ese Maven y ese repositorio, en modo
  offline. Para el alumno no cambia una sola letra: sigue siendo `./mvnw test`.
- **La prueba reina, pasada dos veces:** suite completa verde con **el cable de red
  desenchufado**, sobre un clon fresco y con `~/.m2` apartado. Cero descargas intentadas. Si
  funciona en modo avión, funciona detrás de cualquier firewall — porque para el material son
  la misma cosa.

**`apus` no murió, se degradó a plan B.** `tools/settings-sii.xml` sigue versionado, sin
credenciales, con su encabezado pedagógico. Ya no está en el camino crítico de nada: su gate solo
podía correrse el día de clase, y eso era una ruleta, no un plan. La SPEC-021 lo intentó y se
quedó en `NXDOMAIN`; su informe se conserva como registro de por qué se cambió de estrategia.

**Fase 1 hecha (SPEC-023): el tronco y los labs 03 a 07 también.** Con eso **la primera mitad
del curso completa corre sin internet y sin Docker** — doce proyectos más, suites verdes,
`90-validar` en verde corriendo offline, y los quince eslabones de la derivación en sincronía
desde el tronco hasta el Lab 07. Informe en `docs/specs/informes/INFORME-SPEC-023.md`.

Los dos números pedagógicos que no podían romperse, medidos y sanos: el contador del Lab 05
sigue marcando **13 consultas** en la versión con N+1 contra un presupuesto de 3, y los cuatro
tests de concurrencia del Lab 06 pasan sin un solo reintento.

El peso apenas se movió: `repo-maven/` pasó de 225 a **230 MB** al sumar cinco labs. Los labs
comparten casi todas sus dependencias.

**Lo que falta (Fase 2):** ya solo los labs 12, 13 y 14 — los 08 a 11 están hechos (§1.f). El
13 sigue siendo un lab de contenedores en un curso sin Docker, y eso es decisión del Arquitecto
antes que técnica; el 12 arrastra RabbitMQ. Siguen anotados el rediseño de los `TODO_1`/`TODO_2`
del perfil `dev`.

**Mergeado a `main` y etiquetado `material-v0.3.0`** (PRs #27 y #28, en ese orden).

**Verificado en modo avión, con el cable fuera** (vuelo 3, 13 de agosto): los **siete** labs,
quince suites, **cero descargas intentadas**, `90-validar` APROBADO en las siete soluciones y
cero procesos huérfanos. El número que importa para la sala: **34 segundos** desde el
`git clone` hasta la aplicación sirviendo, con la caché de binarios borrada y sin una sola
conexión de red.

Con eso quedan cerradas también las dos verificaciones que la SPEC-022 tenía diferidas.

## 1.d · Los validadores dicen la verdad (SPEC-FIX-05)

Los vuelos de las SPEC-022/023 destaparon hallazgos **preexistentes**, ninguno causado por la
migración. El que les da nombre: un `99-destruir.sh` que declaraba `[OK] Archivos borrados` y
`3/3 · Todo quedó como estaba` sobre un borrado que había **abortado**.

- **El guard de `borrar_seguro` ya no es ciego a los symlinks.** Comparaba la ruta lógica
  (`/tmp/…`) contra la física (`/private/tmp/…`) y abortaba borrados legítimos. Ahora resuelve
  ambas con `pwd -P`. **No se aflojó**: sigue negándose a la ruta vacía, a la raíz, a `$HOME` y
  a todo lo que caiga fuera del repo.
- **39 declaraciones de éxito mentirosas, corregidas** en 35 archivos, tras inventariar los
  **256 sitios** que declaran éxito en los 81 scripts del repo — labs 08 a 14 incluidos. La más
  extendida no era la del nombre: era `"API detenida (PID N)"`, que en 14 labs felicitaba tras
  esperar 15 segundos sin comprobar si el proceso se había ido.
- **Los 14 labs saben cómo se llaman.** Los `90-validar` de los labs 04 y 05 se presentaban como
  «LAB 02».
- **Regla nueva de la casa, A-04:** todo arnés de verificación imprime TODO lo que calcula. Es
  A-02 mirándose al espejo — aquella prohíbe declarar sin medir, esta prohíbe medir sin mostrar.

Cero código Java tocado. Informe en `docs/specs/informes/INFORME-SPEC-FIX-05.md`.

## 1.e · El Java viaja en la maleta (SPEC-024)

Quedó demostrado en vivo el 13 de agosto: la VM Windows del PO tenía **Java 17** y el curso pide
25. Ese era el último eslabón donde el material le pedía algo a la máquina del alumno — y todo
lo que se le pide a dieciocho máquinas distintas falla en alguna. La sesión 1 fue eso.

**El JDK 25 viaja ahora dentro del repositorio.** Eclipse Temurin `jdk-25.0.4+7`, para Windows
x64 y Mac Apple Silicon, partido en trozos de 80 MB porque GitHub rechaza los archivos de más de
100. El shim `./mvnw` los junta la primera vez, **verifica el sha256 contra el que publica
Temurin**, extrae, y usa ese JDK **ignorando cualquier Java que la máquina tenga**. Si la firma
no cuadra, aborta: un JDK que no se puede verificar no se usa.

Nada de esto toca el entorno del alumno: `JAVA_HOME` y `PATH` se configuran solo para el proceso
de Maven. Ni `.bashrc`, ni variables de usuario, ni permisos de administrador.

**La lista de prerrequisitos del curso quedó en una palabra: Git.**

La prueba que lo resume, con `JAVA_HOME` apuntando a un GraalVM 21 y ese Java primero en el
`PATH`: `Java version: 25.0.4, vendor: Eclipse Adoptium` y la suite del Lab 01 en verde, 46
tests. El problema «N alumnos con N Javas distintos» pasó a ser cero problemas.

**El Lab 00 se migró con él.** Fuera Docker, Docker Hub, Maven Central y el requisito de tener
Java; dentro, Git, la integridad del clon y el ensamblado real del JDK. Da `5/5 · ESTACIÓN
LISTA` con el cable desenchufado, y el Java del sistema baja a informativo: *«el curso NO lo
usa»*.

Y hay **guion para la próxima sesión**: `docs/guion-reinicio-de-sala.md` — clon fresco y no
`pull`, cómo deshacer el parche de emergencia de la sesión 1, la secuencia de arranque con su
salida esperada, y el plan B con los tres sospechosos de las máquinas corporativas.

**Volado y verificado** (vuelo 4, 13 de agosto, nueve minutos): el Lab 00 y los siete labs, con
el cable desenchufado **y con un `JAVA_HOME` hostil apuntando a un Java 21 durante todo el
vuelo**. Quince suites, **cero descargas**, `ESTACIÓN LISTA` y las siete soluciones APROBADO. El
JDK se ensambló **dentro del avión**, en tres segundos.

El número para la sala: **38 segundos** desde el `git clone` hasta la aplicación sirviendo, con
todo frío — sin `target/`, sin caché de binarios, sin JDK ensamblado y sin red. El clon pasa a
1,0 GB.

**Probado en Windows real, en cuatro vueltas.** Encontraron tres defectos que desde macOS eran
invisibles —el `tar` de Git Bash que no abre ZIP, un chequeo del Lab 00 que comparaba rutas
absolutas entre plataformas y daba `[ERROR]` con todo funcionando, y el cartel del Firewall— y
los tres están cerrados. `mvnw.cmd` en `cmd.exe`: **46/46 · BUILD SUCCESS en 40 s**. El
desmontaje mata el JVM nativo, verificado con `netstat` y `tasklist`. Y la app se ata a
`localhost`, así que Windows ya no pide un permiso de administrador que el alumno no tiene.

Ninguno de esos tres se podía cazar razonando: es el argumento entero a favor de probar en la
máquina del alumno.

**Y confirmado en la máquina que más se parece a la sala** (14 de agosto): VM corporativa de
Netec, **Windows 11 x64 nativo**, MINGW64, con un **Temurin 17 instalado de sistema** y antivirus
corporativo. Todo a la primera y **cero defectos nuevos**: `git clone` de 475 MB en **21 s**, Lab
00 **5/5 ESTACIÓN LISTA**, `./mvnw verify` en frío **BUILD SUCCESS · 46 + 7/7 IT · 1m28s** con
PostgreSQL 16.14, `start-lab.sh` **sin cartel del Firewall**, y el desmontaje dejando el 8099 sin
un solo `LISTENING`.

Con eso el tour queda completo sobre **tres plataformas** —Mac, Windows ARM y Windows x64
corporativo— y **tres Javas hostiles derrotados**: GraalVM 21, Temurin 17.0.13 y Temurin 17.0.20.
Que la tercera plataforma no encontrara nada es lo que convierte los tres arreglos anteriores en
correcciones reales y no en parches de una máquina.

Informe en `docs/specs/informes/INFORME-SPEC-024.md`.

**Mergeado a `main` y etiquetado `material-v0.4.0`** (PR #30).

## 1.f · La diplomacia también viaja en la maleta (SPEC-025)

Desde el Lab 08 entra el segundo actor externo: **Tesorería (TESO)**, el servicio que confirma
pagos. Se simulaba con **WireMock en un contenedor Docker** — imposible en el SII. Ahora WireMock
llega como **librería Java corriendo dentro del mismo proceso**, que además es más simple que lo
que había: WireMock nació librería y el contenedor era el envoltorio.

**Los labs 08, 09, 10 y 11 quedaron sin Docker.** Con eso, **del Lab 00 al 11 el curso corre
entero sin demonio, sin permisos de administrador y sin red**: PostgreSQL por Zonky, TESO por
WireMock in-process, Maven y el JDK desde el propio repositorio.

- **En los tests**, cada contexto levanta su TESO en un puerto que elige el sistema y lo gobierna
  por **API Java directa** en vez de por HTTP contra su puerto de administración: si te equivocas
  en el nombre de un método, ya no compila.
- **En dev**, lo levanta la propia aplicación en el **8089 de siempre**, así que las guías, los
  `curl` del alumno y el flag `--teso-lento` siguen siendo verdad palabra por palabra.
- **Todo atado a `127.0.0.1`.** Y de paso se redujo exposición: el `compose.yaml` publicaba TESO
  en *todas* las interfaces (`"8089:8080"`).

**Los dos demos en vivo, que eran los que de verdad usaban Docker, se conservan enteros.** El
`--db-caida` del Lab 10 mata la base embebida por PID —nunca por nombre— y el tablero sigue
delatándola; el `--instancias 2` del Lab 11 levanta dos servidores contra **una sola** base, y el
cierre nocturno corre una vez con el candado y dos sin él.

**Y la cadena vuelve a estar en sincronía del Lab 07 al Lab 11**: el rojo de `deriva` era el Lab
08 atrasado respecto del 07, y se apagó **migrando, no declarando** — medido, el Lab 08 pasó de 13
divergencias a **cero**.

Informe en `docs/specs/informes/INFORME-SPEC-025.md`.

## 2 · Qué falta

**Ningún laboratorio del arco original.** El curso está construido: **catorce labs**, los 35 temas
oficiales cubiertos, y el alcance del título del contrato («Desarrollo de Microservicios en Java»)
cerrado por el Lab 14.

**Lo que sí queda por decidir, y es del PO:** qué se hace con los **labs 07 al 14**, que siguen
con el formato antiguo (enunciado con TODOs, `bin/` de validación, ArchUnit, Docker). Migrarlos
al formato guiado, mantenerlos, o retirar parte. La SPEC-030 no los tocó.

Faltan las diapositivas y el material del instructor para sala.

La renumeración del temario se cuadra en la actualización contractual pendiente; en el repo ya
está aplicada. Faltan también las diapositivas y el material del instructor.

**Nota sobre las pruebas de aceptación:** las de los labs 00 a 07 se corren ahora **sin Docker
y sin red**. Ya no hay que abrir Docker Desktop antes: `./mvnw` y los `bin/` funcionan tal cual
sobre un clon recién hecho. Los labs **12 a 14** siguen necesitando Docker; del 00 al 11, no.

**Pendiente del PO — y en los labs de construcción guiada es LA prueba, no una más:** sentarse con
`PASOS.md` y `practica/` y llegar al final **sin abrir `solucion/`**. Si siguiendo el guion no se
llega al resultado, el guion está mal. Es la única prueba que el ejecutor no puede hacer por
definición: quien escribió el guion no puede juzgar si se entiende.

**Pendiente del PO:** correr las pruebas de aceptación acumuladas — Lab 00 (los tres comandos
de su README), y los Labs 01 a 14, cada uno con su Prueba del PO. Todas diferidas; los
laboratorios están verificados por el ejecutor, pero el PO aún no los ha corrido. La pila de PRs
de los labs 01 a 09 **ya está mergeada a `main`** (PRs #6 a #15), así que la fila acumulada puede
correrse desde `main` limpio, con Java 25 activo (`sdk env` en la raíz).

**Pendiente de infraestructura:** `main` no tiene protección en el servidor (GitHub no la
permite en repos privados del plan Free). El candado está especificado y congelado.

**El CI: el rojo de `deriva` sigue ahí, pero se movió.** Falla desde el PR #27 (SPEC-022) porque
el `lab-08` iba atrasado respecto del `lab-07`. La SPEC-025 lo apagó por la vía honesta —migrando
el 08, el 09, el 10 y el 11— y **la cadena está en sincronía del Lab 07 al Lab 11**. La frontera
roja pasó de 07→08 a **11→12**: el Lab 12 no está migrado, y el guard dice la verdad al señalarlo.

Hoy `deriva` falla por **dos eslabones**, y los dos están declarados:

1. **El Lab 07 no tiene base verificable.** Su base era el Lab 06 del arco antiguo, retirado en la
   SPEC-030. El gate lo dice con esas palabras en vez de compararlo contra el tronco, que no es su
   base — hacerlo reportaría divergencias falsas. Se apaga cuando se decida qué pasa con los labs
   07 al 14.
2. **El Lab 12 va atrasado respecto del 11** (20 archivos). Se apaga migrando el 12, no declarando
   divergencias que no lo son.

Los otros siete jobs, en verde.

**Anotación abierta · A2.4 — el cartel del Firewall durante `verify`.** Sigue abierta, pero con
**una mitad resuelta**. La SPEC-025 midió qué escucha durante `verify` en macOS y el resultado
descarta al sospechoso principal: **el Tomcat de los IT con `RANDOM_PORT` se ata a `127.0.0.1`**
—los IT corren bajo el perfil `dev`, así que A2.3 sí les llega— y el PostgreSQL de Zonky se ata a
`127.0.0.1` y `[::1]`. Ni un solo proceso del laboratorio escucha fuera de loopback. **No se
aplicó candado**: no se toca a ciegas lo que no se puede reproducir aquí, y el único candado que
quedaría vive en archivos que comparten el tronco y los labs 01–11. Lo que falta es una sola
pregunta, en Windows: **qué ejecutable nombra el cartel**. Detalle en §6 del
`INFORME-SPEC-025.md`.

**A3.1 — cerrada, y no era del material.** Durante la SPEC-025 pareció que los JVM que Maven
bifurca ignoraban el JDK embebido: las suites morían con *«class file version 69.0 … up to 65.0»*.
La causa era el arnés de medición, no el curso: envolvía `./mvnw` en `timeout`, y el `timeout` de
esta máquina es el binario **x86_64 del Homebrew de Intel**, que corre bajo Rosetta y hace que
`uname -m` devuelva `x86_64`. El shim creía estar en un Mac Intel y se caía al Java del sistema —
su comportamiento **diseñado** (SPEC-024 §7.3). Re-verificado con `JAVA_HOME` hostil y sin
`timeout`: las tres formas de fork —surefire, failsafe y `spring-boot:run`— usan el JDK embebido,
citando el binario. **El registro del vuelo 4 era correcto.** Detalle en §7 del
`INFORME-SPEC-025.md`.

**Queda una recomendación, no un defecto:** el fallback del shim es **silencioso**. En un Mac
Intel de verdad, el alumno recibiría un `UnsupportedClassVersionError` sin pista. Una línea de
`[INFO]` lo arregla; es una SPEC-FIX uniforme sobre los diecisiete proyectos y el parche ya está
escrito en el informe.

## 3 · Qué viene ahora

**Ya no falta material de laboratorio.** Con el Lab 14 construido, los catorce laboratorios están
escritos, verificados por el ejecutor y con su CI en verde. Y el arco nuevo ya llega hasta la
concurrencia.

Hay **cuatro PRs en draft esperando firma**: #31 (SPEC-025, labs 08–11 sin Docker), #33 (SPEC-027,
Lab 3b con las anotaciones A1), #34 (SPEC-028, labs 00–03 del arranque) y #35 (SPEC-029, labs
04–06 del arco nuevo).

> **Nota para quien mergee el primero:** los PRs #33, #34 y #35 llevan **el mismo arreglo** al job
> `siembra` del CI —enseñarle que un lab de construcción guiada enseña con `PASOS.md` y no con
> `TEORIA.md`—, escrito igual en los tres para que no peleen. En cuanto entre uno, los otros dos
> traen ese trozo ya resuelto.

Lo que falta es que el PO corra la fila de **pruebas de aceptación acumuladas** (Labs 00 a 14) y
cierre los PRs abiertos. Los laboratorios están verificados por el ejecutor; ninguno lo ha corrido
su dueño, y esa es la etapa que cierra cada SPEC.

Después, las diapositivas y el material de sala.

## 4 · Si estás perdido

Dos comandos. Cinco minutos. Vas a ver arrancar tu primera aplicación:

```bash
cd labs/lab-00-hola-mundo/solucion
./mvnw spring-boot:run
```

Sale el banner de Spring Boot y un `Hola, mundo`. No hace falta instalar nada: Java y Maven
viajan dentro del repositorio.

¿Quieres ver en un minuto de qué trata el curso? El Lab 02 tiene un endpoint que responde el
nombre de la clase que Spring eligió y construyó, sin que nadie haya escrito un `new`:

```bash
cd labs/lab-02-di/solucion
./mvnw spring-boot:run
# en otra terminal:
curl http://localhost:8084/productos/quien     # -> ProductoRepositoryLista
```

Cambia una anotación en `ProductoRepositoryLista` y esa respuesta cambia, sin tocar el
controller. Eso es Spring, y es el Lab 02 entero.

Si algo falla, [`docs/troubleshooting.md`](docs/troubleshooting.md) tiene una tabla con números.
Cita el número.
