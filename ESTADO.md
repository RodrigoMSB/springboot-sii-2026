# ¿En qué va el curso?

*Una página, sin jerga. Si llevas dos semanas sin mirar el repo, empieza aquí.*
*Última actualización: SPEC-FIX-04.*

---

## 1 · Qué existe hoy

- **El temario definitivo** (v3, julio 2026): `docs/temario/`. Son 36 horas, 12 sesiones
  de 3, 15 módulos. El `.md` manda; el `.docx` es lo que se le entrega al SII.
- **La aplicación del curso**: `dgt-tramites-api/`. Es el backend de la DGT — lo que hay
  detrás del botón. Arranca, se conecta a su base de datos y responde. Tiene siete reglas
  de arquitectura que la vigilan, y cada regla trae una prueba de que muerde.
- **El pre-vuelo del alumno**: `labs/lab-00-estacion-base/`. El chequeo que hace en su casa
  antes de la sesión 1.
- **Lab 01** (`labs/lab-01-del-otro-lado-del-boton/`): una contraseña de producción en el
  historial de git — se rota, no se borra.
- **Lab 02** (`labs/lab-02-el-folio-que-se-filtro/`): un endpoint filtra el puntaje de riesgo
  de un contribuyente; se tapa con un DTO (lista blanca) y se instalan los guardianes ArchUnit.
- **Lab 03** (`labs/lab-03-red-de-seguridad/`): la suite llega en rojo — los tests son el
  enunciado. Validaciones, RUT chileno, errores con contrato, y los primeros tests Mockito.
- **Lab 04** (`labs/lab-04-el-arbol-de-tramites/`): todo en `EAGER` — un muro de JOINs. Se
  corrige a LAZY, se instala AU-04, y se planta la bomba del Lab 05.
- **Lab 05** (`labs/lab-05-once-segundos/`): el clímax. El N+1 medido con un contador de
  consultas, no contado. Dos soluciones conviven (P-16): `solucion-con-n1/` (13 consultas) y
  `solucion/` (3) — mismo comportamiento, distinto costo.
- **Lab 06** (`labs/lab-06-dos-folios-un-numero/`): la concurrencia. Dos emisiones a la vez se
  llevan el mismo folio; se resuelve con bloqueo pesimista (`SELECT … FOR UPDATE`) en la misma
  transacción, idempotencia por `tramiteId`, y la primera migración correctiva (un `CHECK` en
  `linea_f29`). RN-01, RN-02 y RN-05 por fin con suelo, probadas con concurrencia real.
- **Lab 07** (`labs/lab-07-el-portero/`): la seguridad. La API se cierra por defecto (Spring
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

- **Docker fuera de los labs 01 y 02.** PostgreSQL llega como dependencia Maven (Zonky): se
  extrae a una carpeta temporal y corre como proceso hijo del JVM. Sin demonio, sin admin.
  Es PostgreSQL **de verdad** —16.14, el mismo motor— no un H2 disfrazado.
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

**Lo que falta (Fase 2):** los labs 08 a 14. El salto real está en el 08–11, que hoy levantan
WireMock en contenedor y habría que pasar a in-process; y en el 13, que es un lab de
contenedores en un curso sin Docker — eso es decisión del Arquitecto antes que técnica. Siguen
anotados el rediseño de los `TODO_1`/`TODO_2` del perfil `dev` y la reconciliación de la
frontera Lab 07 → Lab 08.

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

Informe en `docs/specs/informes/INFORME-SPEC-024.md`.

## 2 · Qué falta

**Ningún laboratorio.** El curso está construido: **catorce labs**, los 35 temas oficiales
cubiertos, y el alcance del título del contrato («Desarrollo de Microservicios en Java») cerrado
por el Lab 14.

Faltan las diapositivas y el material del instructor para sala.

La renumeración del temario se cuadra en la actualización contractual pendiente; en el repo ya
está aplicada. Faltan también las diapositivas y el material del instructor.

**Nota sobre las pruebas de aceptación:** las de los labs 00 a 07 se corren ahora **sin Docker
y sin red**. Ya no hay que abrir Docker Desktop antes: `./mvnw` y los `bin/` funcionan tal cual
sobre un clon recién hecho. Los labs 08 a 14 siguen necesitando Docker hasta la Fase 2.

**Pendiente del PO:** correr las pruebas de aceptación acumuladas — Lab 00 (los tres comandos
de su README), y los Labs 01 a 14, cada uno con su Prueba del PO. Todas diferidas; los
laboratorios están verificados por el ejecutor, pero el PO aún no los ha corrido. La pila de PRs
de los labs 01 a 09 **ya está mergeada a `main`** (PRs #6 a #15), así que la fila acumulada puede
correrse desde `main` limpio, con Java 25 activo (`sdk env` en la raíz).

**Pendiente de infraestructura:** `main` no tiene protección en el servidor (GitHub no la
permite en repos privados del plan Free). El candado está especificado y congelado.

## 3 · Qué viene ahora

**Ya no falta material de laboratorio.** Con el Lab 14 construido, los catorce laboratorios están
escritos, verificados por el ejecutor y con su CI en verde.

Lo que falta es que el PO corra la fila de **pruebas de aceptación acumuladas** (Labs 00 a 14) y
cierre los PRs abiertos. Los laboratorios están verificados por el ejecutor; ninguno lo ha corrido
su dueño, y esa es la etapa que cierra cada SPEC.

Después, las diapositivas y el material de sala.

## 4 · Si estás perdido

Tres comandos. Diez minutos. Vas a ver la DGT funcionando:

```bash
cd labs/lab-00-estacion-base

./bin/00-verificar.sh     # ¿tu máquina está lista?
./bin/start-lab.sh        # levanta la DGT
./bin/99-destruir.sh      # y déjalo todo como estaba
```

¿Quieres ver de qué trata el curso en un minuto? Ve el crimen del Lab 01:

```bash
cd labs/lab-01-del-otro-lado-del-boton/starter
git log --oneline -- src/main/resources/application.yml
git show dc70ed6 -- src/main/resources/application.yml     # la contraseña, en pantalla
```

Entre el segundo y el tercero, haz lo que el script te diga: pídele a la DGT que te hable
de Valentina Rojas. Cuando te responda, ya viste de qué trata el curso.

Si algo falla, `labs/lab-00-estacion-base/docs/troubleshooting.md` tiene una tabla con
números. Cita el número.
