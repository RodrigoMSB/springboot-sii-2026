# ¿En qué va el curso?

*Una página, sin jerga. Si llevas dos semanas sin mirar el repo, empieza aquí.*
*Última actualización: SPEC-044 — hay cuatro guías en PDF que el alumno puede seguir solo.*

---

## 1 · Qué existe hoy

- **El temario definitivo** (v3, julio 2026): `docs/temario/`. Son 36 horas, 12 sesiones de 3,
  15 módulos. El `.md` manda; el `.docx` es lo que se le entrega al SII.

- **EL ARCO, COMPLETO: quince labs de construcción guiada, del 00 al 14.** Es todo el material
  que el alumno usa. Cada uno con `README.md`, `PASOS.md` y **tres carpetas** (`practica/`,
  `solucion/`, `instructor/`). Sin Docker, sin instalar nada, sin red.

  | | lab | el número que se proyecta |
  |---|---|---|
  | 00 | `hola-mundo` | que arranque. Una clase, una anotación, un `main` |
  | 01 | `web` | ruta, parámetro, cuerpo y `ResponseEntity` |
  | 02 | `di` | **el lab que explica qué es Spring**: dos implementaciones y `/productos/quien` |
  | 03 | `errores` | 404 con cuerpo, 400 con los campos, y el mensaje interno que nunca sale |
  | 04 | `jpa` | una clase y una tabla son la misma cosa; el SQL en la consola |
  | 05 | `relaciones` | **1 SELECT con LAZY, 4 con EAGER**, y la `LazyInitializationException` |
  | 06 | `rendimiento` | el N+1: **201 consultas contra 1** |
  | 07 | `concurrencia` | 20 emisiones a la vez: **21 de 21** con candado |
  | 08 | `testing` | el rojo provocado: `expected: <5938> but was: <5489>`. De 0,03 s a 0,7 s con Spring |
  | 09 | `seguridad` | **401** sin token, **403** con token y sin rol. Dos hashes para la misma clave |
  | 10 | `resiliencia` | **30,01 s → 0,002 s**, y de 1 llamada HTTP a **0** con el circuito abierto |
  | 11 | `observabilidad` | base caída: liveness **200**, readiness **503** nombrando la causa |
  | 12 | `tareas` | **3,03 s → 0,004 s**, y el cierre nocturno **dos veces en el mismo segundo** |
  | 13 | `empaquetado` | imagen OCI de **138,9 MB** construida sin Docker y sin red |
  | 14 | `microservicios` | cuatro procesos, tres bases: con un servicio caído, **HTTP 500 → HTTP 200 degradado**, y las llamadas a un muerto **congeladas en 3** |

- **EL INSTRUMENTO DE EVALUACIÓN: `proyecto-final/`.** Recuperado del arco antiguo y adaptado
  (SPEC-035). No es un lab: es con lo que el PO certifica. Un requerimiento de negocio incompleto
  en los bordes («el consolidado del contribuyente»), tres horas, y una rúbrica de tres ejes
  —Correctitud, Oficio, Criterio— con el umbral que define el curso: **núcleo verde Y Criterio ≥
  Suficiente**. Todo funcionando y sin criterio **no aprueba**.
  - `base/` (8107) — compila y arranca: dominio, datos sembrados y la autenticación JWT resueltos.
    El encargo, no.
  - `brief/` · `rubrica/` · `plantillas/reporte.md` — lo que el alumno lee y entrega.
  - `instructor/` — la solución de referencia (10 tests, verificada) y la guía de defensa con
    respuestas calibradas por nivel. **No viaja al repositorio**: traería las respuestas dentro.
  - Los **20 requisitos del encargo están atados a su lab y su paso**: no se evalúa nada que no se
    haya enseñado.

- **Los quince `PASOS.md` traen el código listo para pegar** (SPEC-038 y -039): cada paso dice el
  bloque exacto, el archivo y el sitio. El instructor lo tiene en una ventana y `practica/` en la
  otra. `instructor/` queda para preparar la clase y para el *por qué*.

- **La estructura de tres carpetas rige en los quince** (SPEC-031, -032, -033, -037): `practica/` sin
  una línea de documentación, `solucion/` con comentarios breves, e `instructor/` con todo
  explicado línea por línea. La tercera **no viaja al repositorio** (`labs/*/instructor/` en el
  `.gitignore`): es la chuleta de quien dicta, y versionarla anularía el motivo de haber vaciado
  `practica/`. La genera quien prepara la sesión, a partir de `solucion/` — y al terminar
  corre `python3 tools/verificar-instructor.py`, que comprueba que quedó al día con
  `solucion/` y que sus `pom.xml` son XML válido. **Ese chequeo no puede estar en el CI**:
  la carpeta no viaja, así que en el runner no existe y el job pasaría siempre (D-FIX10-2).

- **HAY GUÍAS EN PDF QUE EL ALUMNO SIGUE SOLO** (SPEC-044), para los labs **00, 01, 02 y 03**:
  `docs/guias/`. Es un formato nuevo, distinto de `PASOS.md` —que es el guion de quien dicta—:
  aquí no hay notas de conducción, y cada paso trae **el problema antes de la solución**, **la
  alternativa que se descartó**, un **«Vas bien si…»** comprobable y un **«Si te atascas»** con el
  error literal que el alumno va a ver.
  - **Una metáfora por lab, y es un mundo que crece**: la oficina de la DGT que abre (00), le pone
    una ventanilla (01), encarga a un proveedor (02) y aprende a explicar por qué un trámite no
    procede (03).
  - **El código no se teclea: se extrae de `solucion/`** con `tools/generar-guias.py`, que además
    comprueba que cada línea impresa esté ahí (`D-044-1`). 32 bloques, 0 divergencias.
  - **Los «Si te atascas» están medidos**: once errores rotos a propósito y copiados de la
    pantalla.
  - **11, 15, 15 y 13 páginas · 402 KB los cuatro.**
  - **Falta la prueba de fondo en tres de los cuatro**: la guía del 00 se siguió entera sobre
    `practica/` limpia y llega al resultado; las del 01, 02 y 03 no. Está en `INFORME-SPEC-044` §10.
  - **Si el formato convence, los once labs restantes van en otra SPEC.** Es la decisión que el
    material espera del PO.

- **HAY UN SEGUNDO INSTRUMENTO DE EVALUACIÓN, y es corto** (SPEC-043): `examen-huecos/`. Una
  aplicación pequeña que compila y arranca, con **doce huecos marcados** que el alumno completa —
  **48 líneas de código en total**. Cubre lo de los labs 01 al 09: entidad con relación, tres
  consultas derivadas, servicio, controller con DTO, los dos errores con forma, seguridad por rol y
  configuración externa.
  - **No lleva `PASOS.md`**: cada hueco dice **qué** tiene que hacer, nunca cómo.
  - **Cada hueco tiene su test**, y **el puntaje es directo**: la nota es la última línea de
    `./mvnw test`, `RESUELTOS: n de 12`. El alumno la ve antes de entregar.
  - **Los doce huecos son independientes, y está medido**: resolviéndolos de uno en uno la cuenta de
    verdes va 0, 1, 2 … 12 sin saltos. Es lo que hace honesto el puntaje directo.
  - `base/` arranca en **3,8 s** y da 0 de 12; `solucion/` da 12 de 12; una corrida de la suite
    tarda **7 segundos**.
  - **Estimado en 60 a 90 minutos, NO medido.** Es lo único que falta y sólo lo dice un alumno:
    `INFORME-SPEC-043` §1.6 explica cómo se cierra en quince minutos.
  - **El `proyecto-final/` no se tocó.** Sigue entero, y son dos instrumentos, no uno.

- **`instructor/` tiene respaldo, y sigue sin viajar al repositorio público** (SPEC-042). Las
  dieciséis carpetas —los quince labs y el proyecto final, **245 archivos**— están en un
  repositorio **privado** aparte, `springboot-sii-2026-instructor`, con las mismas rutas para que
  restaurar sea copiar. **D-031-2 no cambia** (D-042-1): el clon del alumno sigue sin traerlas.
  - **El puente es `tools/instructor-respaldo.sh`**, con tres verbos: `estado` compara los dos
    árboles por huella `sha256` y no escribe nada · `respaldar` copia disco → privado ·
    `restaurar` copia privado → disco. Las dos copias terminan comparando y diciendo cuántas
    huellas cuadran.
  - **Probado sobre clones frescos en `/tmp`**: el clon público llega con **0** carpetas
    `instructor/`, y tras `restaurar` tiene las **16** con las **245 huellas idénticas** a las de
    la máquina del PO. El repositorio privado da **401** a un `git clone` sin credenciales; el
    público, 200.
  - **Se borraron los `target/` heredados** del hallazgo de la SPEC-041 §7: **258 archivos** de
    salida de compilación en una carpeta que no es un proyecto, con copias rancias de los recursos
    dentro. El `.gitignore` del repositorio privado los deja fuera para siempre.

- **En los labs 04 a 07, `instructor/` responde «¿y por qué ésa?»** (SPEC-041). La documentación
  explicaba qué hace cada anotación, pero no por qué esa y no otra — que es lo que pregunta el
  alumno y lo que el instructor tiene que responder sin pensar. Lo destapó el PO dictando el lab
  04: ante «¿y por qué `IDENTITY`?» no había respuesta a mano.
  Ahora **cada decisión técnica lleva un recuadro `POR QUÉ ·` con cuatro partes** —qué hace, qué
  alternativas existen, por qué se eligió ésta aquí, en qué caso elegirías otra—, y donde no hay
  alternativa real lo dice y se pasa. Son **102 decisiones**: 34 en el 04, 22 en el 05, 23 en el
  06 y 23 en el 07, en los `.java`, el `application.yml`, el `pom.xml` y las migraciones SQL.
  - **Cero código tocado, medido**: el código desnudo —sin comentarios— de los 37 `.java` de
    `instructor/` es idéntico al de `solucion/`, antes y después. Y los cuatro compilan offline.
  - **No viaja al repositorio, y es lo previsto** (D-031-2): lo que se commitea es el informe.
    Un clon fresco no trae estos bloques. Está dicho en `INFORME-SPEC-041` §6, con las tres
    salidas posibles, porque la decisión es del PO.
  - **Y desde la SPEC-043 lo tienen también los labs 08 al 14 y el examen nuevo**: 57 recuadros
    más, que dejan el material en **140**. Cero código movido, medido sobre los **200 `.java`** de
    las diecisiete carpetas `instructor/`, y las once fuentes compilan offline.
    **Con una salvedad dicha en voz alta:** la densidad de los labs 08 a 14 es de 7 recuadros por
    lab contra los 21 de los labs 04 a 07. Están cubiertas las decisiones que cada lab enseña; no se
    repitió el andamiaje heredado (`pom.xml`, clase de arranque, `infra/`), que ya está documentado
    en el 04. `INFORME-SPEC-043` §2.4 lo mide y lo acota.

- **Los quince labs llaman igual a las mismas cosas** (SPEC-040). Antes no: el alumno veía `web/`
  en un lab y `controllers/` en el siguiente, `servicios/` en español en uno solo, una clase que
  hacía de servicio anotada `@Component`, y paquetes que tartamudeaban —`seguridad/seguridad`—.
  Aprendía que da igual, y no da igual. Hoy **un rol, un nombre, una anotación**: `controllers/`
  con `@RestController`, `services/` con `@Service`, `repositories/`, `entities/` con `@Entity`,
  `dto/`, `exceptions/`, `config/` con `@Configuration`, `infra/` para la fontanería del arranque
  y `soporte/` para el andamiaje del lab. **Cero divergencias medidas**, y las que se dejaron a
  propósito están justificadas una a una en el informe.
  - **`models/` y `entities/` siguen llamándose distinto, y ahora el README dice por qué**: uno es
    un `record` que vive en memoria, el otro una fila de una tabla. Es una diferencia que enseña.
  - No cambió **ninguna ruta HTTP** (las 99 declaraciones del arco son idénticas), ni un número, ni
    un paso. Es un renombre, y se verificó como tal.

- **La maleta**: el alumno solo necesita Git. Dentro del repositorio viajan
  - `tools/jdk/` — el JDK 25, partido en trozos y ensamblado al vuelo por el shim `mvnw`
  - `tools/maven/` + `repo-maven/` — Maven y **todas** las dependencias (D-022-1)
  - `tools/jib-base/` — las capas de `eclipse-temurin:25-jre`, para que el Lab 13 construya su
    imagen OCI sin salir a la red (D-032-1)
  - y PostgreSQL, que llega como dependencia Maven y arranca como proceso hijo — sin Docker

- **La portabilidad, blindada**: `.gitattributes` fija los finales de línea (LF para los `.sh`,
  CRLF para los `.cmd`) y marca `binary` todo lo que no debe tocarse (`repo-maven`, `tools/jdk`,
  `tools/jib-base`). El CI vigila que no se degrade.

- **El manifiesto pedagógico**: `MANIFIESTO.md`. Por qué el curso se enseña así. Quien vaya a
  dictar lo lee **antes** que nada.

- **Un CI de seis jobs** (`.github/workflows/material-ci.yml`):
  `temario` (el `.md` y el `.docx` no divergen) · `siembra` (todo lab con sucesor siembra el
  siguiente) · **`labs`** (los **37** proyectos Maven compilan **offline**, y falla si alguien
  necesitó la red: 36 en `labs/` —el Lab 14 aporta ocho, cuatro servicios × dos carpetas— más
  `proyecto-final/base`) · **`pasos`** (los **quince** guiones traen el código para pegar y ninguno
  promete un código que `solucion/` ya no tenga: 146 bloques y 87 métodos comprobados contra la
  solución) · **`guion-practica`** (lo que los quince guiones prometen de `practica/` —carpetas
  vacías, archivos que ya están, archivos por crear— es lo que `practica/` trae: **88 promesas**
  comprobadas) · `labs-sh` (los scripts, en Linux y en Git Bash).

## 1.a · Lo que se retiró, y dónde está

La **SPEC-033** sacó de `main` el arco antiguo entero: los ocho labs `lab-07-el-portero` a
`lab-14-la-dgt-se-parte-en-pedazos`, el tronco `dgt-tramites-api`, `labs/lib/` con la maquinaria
de derivación y los tres scripts `tools/vuelo-*`. **2.120 archivos.**

**No se borró nada del historial**: todo sigue en los commits anteriores y en los tags
`material-v0.4.0` a `material-v0.8.0`. Para recuperar cualquier cosa:

```bash
git show material-v0.8.0:labs/lab-13-capsula-y-egreso/README.md
git checkout material-v0.8.0 -- dgt-tramites-api/
```

Se retiró porque quedó **sin consumidores**: se verificó con `grep` que ningún lab del arco nuevo
cita el arco viejo, ni `labs/lib`, ni el tronco. Cero acoplamiento.

Con él se fueron cuatro jobs del CI —`app`, `lab14`, `grpc` y `deriva`—, que no se apagaron: se
quedaron sin objeto. En su lugar nació `labs`.

> ℹ️ **Las secciones 1.b a 1.f son memoria, no estado.** Cuentan cómo se llegó hasta aquí —el
> alcance contra el contrato, la salida de Docker, el JDK y las dependencias dentro del
> repositorio— y **nombran labs del arco antiguo que ya no están** (§1.a). Lo que describen de la
> maleta sigue vigente y es la razón de que el material corra sin red; lo que dicen de los labs
> 07 al 14, no. Para el estado de hoy basta la §1.

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

## 1.g · Cuando el puerto está ocupado, el lab lo dice (SPEC-FIX-07 y SPEC-FIX-08)

Los seis labs con base de datos levantan su PostgreSQL en un puerto fijo. Si quedaba uno vivo de
una corrida anterior —cerrar la terminal de golpe basta—, el siguiente arranque moría con
`Failed to start bean 'webServerStartStop'` o con `could not lock .datos-pg/epg-lock` debajo de
cinco excepciones anidadas. **Ninguno de los dos nombra el puerto ni Postgres**, así que el alumno
concluye que rompió su código y se pone a depurar donde no hay nada roto.

Ahora, antes de arrancar el motor, se sondea el puerto. Si está tomado, el programa termina
imprimiendo el puerto, la causa probable, **que no es un error de su código**, y el comando exacto
para cerrarlo — sólo el de su sistema operativo, elegido con `os.name`.

Está en los 20 proyectos con PostgreSQL embebido: los labs 04, 05, 06, 07, 09 y 11 en sus tres
carpetas, `proyecto-final/base` y la solución de referencia.

Y hay **dos** candados, no uno: el puerto lo retiene PostgreSQL y se libera al morir el motor;
`.datos-pg/epg-lock` lo retiene la aplicación Java y sobrevive al motor. Si alguien mata el
PostgreSQL a mano y deja la aplicación en pie —lo verificó el PO en Windows—, el puerto queda
libre y el candado no. Por eso hay una segunda guarda, `CandadoLibre`, que manda a cerrar la otra
terminal con Ctrl+C.

Informes en `docs/specs/informes/INFORME-SPEC-FIX-07.md` y `INFORME-SPEC-FIX-08.md`.

## 2 · Qué falta

**Del material, nada bloqueante.** El arco está completo: quince labs, los tres formatos de
carpeta en todos, y el CI verificando que los 37 proyectos compilan offline.

**Pendiente del PO, y en los labs guiados es LA prueba, no una más:** sentarse con `PASOS.md` y
`practica/` y llegar al final **sin abrir `solucion/`**. Si siguiendo el guion no se llega al
resultado, el guion está mal. Es la única prueba que el ejecutor no puede hacer por definición:
quien escribió el guion no puede juzgar si se entiende.

Los quince están verificados por el ejecutor —cada uno con sus salidas citadas en el informe de
su SPEC— pero el PO no los ha corrido de punta a punta.

**Lo que sí está cerrado desde la SPEC-043: los quince guiones se han PEGADO.** Faltaba en el 10,
11, 12 y 13 —era la deuda declarada en `INFORME-SPEC-039` §5— y apareció de todo: el guion del lab
10 mandaba pegar sentencias de constructor «dentro de la clase», y pegado al pie de la letra **ni
siquiera parseaba**. Los cuatro guiones están corregidos y la prueba pasa: compilan y el resultado
es idéntico a `solucion/`. Eso quita una clase entera de sorpresas de la fila de aceptación, pero
**no la sustituye**: que el código encaje no dice que el guion se entienda.

En el **Lab 14** esa fila importa más que en los otros catorce, por dos razones concretas: es el
único que abre **cuatro terminales y tres PostgreSQL a la vez** (memoria y confusión medidas en
`INFORME-SPEC-037` §6/V8), y **todo se midió en macOS**. Los tres defectos que encontró la
SPEC-024 eran invisibles desde macOS. Es el lab que más conviene probar en la VM de Windows.

**Faltan las diapositivas y el material de sala.** `instructor/` cubre la parte de código; una
presentación, no.

**Del contrato: el mapa ya está rehecho, y dice que hay huecos.**
`docs/temario/MAPA-LAB-MODULO.md` (SPEC-034, al día con la SPEC-037) mide la cobertura real contra
los 15 módulos y los 35 temas. El resultado, medido con el lab y el paso que lo respalda:

| Nivel | Temas |
|---|---|
| **Cubierto** | **20** de 35 |
| **Parcial** | 7 |
| **Mencionado** | 0 |
| **No cubierto** | **8** |

*(El Lab 14 subió el tema XXVIII —trazas— de «Mencionado» a «Parcial»: la correlación entre tres
servicios ahora se practica; OpenTelemetry sigue sin verse.)*

Y por módulo: **3 cubiertos** (M4 Testing I, M5 Persistencia, M7 Transacciones) y **12 parciales**.

**El tema XXXV ya está cubierto (SPEC-035):** `proyecto-final/` es el instrumento con el que se
evalúa, recuperado del antiguo `lab-13-capsula-y-egreso` y adaptado al arco nuevo. Con eso, la
cobertura sube a **20 temas cubiertos de 35**.

**Lo que sigue sin instrumento es el otro 50 % de la evaluación:**

| | Peso | Instrumento |
|---|---|---|
| Proyecto final | **50 %** | ✅ `proyecto-final/` — y ahora también `examen-huecos/` |
| Evaluación de conocimientos | **30 %** | ❌ **no existe** |
| Ejercicios | **20 %** | ⚠️ **`examen-huecos/` podría serlo, y es decisión del PO** |

**Sobre esa última fila:** el examen de huecos se construyó como alternativa corta al proyecto
final, no como instrumento de la casilla de ejercicios. Pero da una nota numérica, objetiva y
automática sobre los labs 01 a 09, que es exactamente lo que esa casilla pedía. Usarlo así, usarlo
en lugar del proyecto final, o usar los dos, es una decisión del PO y no del material.

Los quince labs son construcción guiada y no llevan nota, así que no sirven como «ejercicios
evaluados» sin definir antes qué se puntúa. Las dos casillas vacías son una decisión del PO, no un
trabajo pendiente del material.

Las ocho brechas restantes: gRPC, AOP, manejo de archivos, eventos de aplicación, mensajería,
caché, Liquibase y OpenAPI/versionado. **Siete de ellas son un paso dentro de un lab que ya existe**; las
caras —Testcontainers, mensajería y Buildpacks— lo son porque las tres exigen Docker, que la sala
del SII no tiene. El mapa las detalla con qué haría falta para cada una.

**Y el alcance de microservicios ya está cubierto (SPEC-037).** El título del contrato
—«Desarrollo de Microservicios en Java»— prometía algo que su propio temario no reparte: ninguno
de los 15 módulos ni de los 35 temas es de microservicios. Lo cubría el antiguo Lab 14, retirado
con el arco viejo por necesitar Docker Compose y seis servicios.

El **`lab-14-microservicios`** lo reconstruye en formato guiado: **cuatro procesos que el alumno
arranca a mano, tres bases de datos, cero Docker**. Su teoría se recuperó entera del tag
`material-v0.8.0` y se leyó antes de diseñar nada.

Lo que queda para el PO es la aritmética, no el material: son **tres horas más** sobre un contrato
que ya iba seis por encima, y el lab **no mapea a ningún módulo contratado como titular** — toca
M10, M13 y M14 sin ser el dueño de ninguno. Está declarado en el mapa, §6.4.

## 3 · Qué viene ahora

**El material está terminado.** Quince labs, numerados 00 a 14, con las tres carpetas, la maleta
completa y el CI verde. No queda laboratorio por escribir.

Lo que viene, en orden:

0. **Elegir con qué se evalúa, antes del lunes.** Están los dos instrumentos: el `proyecto-final/`
   de tres horas y el `examen-huecos/` de hora y media. La decisión es del PO y es la más urgente,
   porque quedan tres o cuatro clases. Si se elige el examen de huecos, lo que falta es un número:
   cuánto tarda de verdad (`INFORME-SPEC-043` §1.6, se cierra en quince minutos).
1. **La fila de aceptación del PO.** Sentarse con cada `PASOS.md` sobre `practica/`, sin abrir
   `solucion/`, del 00 al 14. Es la etapa que cierra cada SPEC y la única que el ejecutor no puede
   hacer. **Empezar por el 14**: es el más grande, el más nuevo, el único con cuatro terminales, y
   el único que no se ha probado nunca fuera de macOS.
2. **Decidir sobre las brechas del mapa** (§2): las siete baratas primero, y después negociar con
   el SII las tres que dependen de Docker.
3. **Las diapositivas y el material de sala.** Las guías en PDF de la SPEC-044 cubren la parte
   del alumno en cuatro labs; una presentación para proyectar, no.
4. **Resolver la aritmética del contrato con el SII** (§2 y mapa §6.4): el material va **nueve
   horas y tres sesiones** por encima de lo contratado, y el lab-14 no tiene módulo titular. Es una
   conversación, no un trabajo pendiente del material.

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
