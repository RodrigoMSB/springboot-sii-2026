# INFORME-SPEC-023 · Fase 1 — La flota completa hasta el Lab 07

**SPEC:** SPEC-023 · **Ejecuta:** mocito · **Fecha:** 13 de agosto de 2026 (turno nocturno)
**Rama:** `spec-023-fase-1-labs-03-07`, apilada sobre `spec-022-material-autocontenido`
**Máquina:** Mac Studio del PO — Darwin 25.5.0, `arm64`

---

## 1 · Veredicto en una línea

**FASE 1 EJECUTADA Y VERIFICADA EN MODO AVIÓN** — el tronco y los labs 01 a 07 corren la
receta autocontenida con el cable de red desenchufado: siete labs, quince suites, cero
descargas intentadas y `90-validar` APROBADO en las siete soluciones. La cadena de derivación
cierra entera y el simulacro del alumno va de `git clone` a app viva en **34 segundos**.

---

## 2 · Precondición: en qué estado quedó la SPEC-022

§1 exigía cerrar la 022 antes de arrancar. Se cerró:

- **El vuelo 2 nunca despegó.** El script quedó en pista desde las 00:58 esperando un corte
  de red que no llegó; se abortó a las 01:08 con el entorno restaurado. No es un fallo del
  material —el Lab 02 estaba verde en `--offline` con cero descargas— sino un vuelo que no
  salió. **V6 y V10 se trasladaron al vuelo 3** en vez de pedir dos cortes de cable.
- `INFORME-SPEC-022.md` quedó sin nada en estado pendiente, con esas dos filas marcadas
  DIFERIDA y su motivo.
- La 022 no salió con fallas, así que esta SPEC arrancó como estaba previsto.

Precondiciones propias, verificadas: **Java 25** (`Temurin-25+36`), internet para la captura
y `~/.m2` disponible.

---

## 3 · Tabla por proyecto

Doce proyectos migrados. Tiempo de réplica = reloj desde abrir el proyecto hasta su suite en
verde, incluyendo lo que hubo que resolver a mano.

| # | Proyecto | Réplica | Qué NO fue mecánico |
|---|---|---|---|
| 1 | `dgt-tramites-api` (tronco) | ~12 min | No tenía perfil `dev`: dependía de que Boot detectara el `compose.yaml`. Hubo que dárselo |
| 2 | Lab 03 · starter + solucion | ~2 min | Nada. Réplica limpia |
| 3 | Lab 04 · starter + solucion | ~6 min | `BasePersistenciaIT` es una `@TestConfiguration` de nivel superior importada con `@Import` |
| 4 | Lab 05 · starter + solucion + **solucion-con-n1** | ~25 min | El `--lotes` sembraba con `psql` dentro del contenedor. Ver §5 |
| 5 | Lab 06 · starter + solucion | ~15 min | Destapó el fallo de aislamiento. Ver §5 |
| 6 | Lab 07 · starter + solucion | ~8 min | Tres perfiles declarados divergentes por el secreto JWT; `BaseSeguridadIT` con el helper de login |

**Lo que este dato dice para la Fase 2:** la parte mecánica es rápida y ya está automatizada.
El tiempo real se va en **leer el `bin/` de cada lab** —cada uno trae su propia sorpresa de
Docker— y en los tests que no siguen el patrón común. Presupuesto honesto para los labs 08–14:
no menos de lo que costaron el 05 y el 06, porque el 08 en adelante añade WireMock.

---

## 4 · Verificaciones N1–N6, citadas

Todo con red (captura a `repo-maven`), salvo N4 que se corrió **offline** a propósito.

### N1 · `starter`: falla lo del alumno, y solo eso

Los números de esta tabla son los del **vuelo 3** (§9), no los del turno nocturno: el arnés
nocturno calculaba los «fallos ajenos» pero no llegaba a imprimirlos, así que la primera
versión de este informe los daba por cero sin evidencia. El vuelo los midió de verdad y
corrigió dos.

| Lab | Fallos del `starter` | Fuera de `enunciado/` |
|---|---|---|
| 01 | `Tests run: 46, Failures: 5, Errors: 2` | 0 |
| 02 | `Tests run: 38, Failures: 4` | 0 |
| 03 | `Tests run: 60, Failures: 3, Errors: 14` | **2** — ver abajo |
| 04 | `Tests run: 40, Failures: 1` | 0 |
| 05 | `Tests run: 41, Errors: 1` | **2** — ver abajo |
| 06 | `Tests run: 11, Failures: 2, Errors: 1` | 0 |
| 07 | `Tests run: 22, Failures: 6, Errors: 1` | 0 |

**Los dos «ajenos» no son ajenos: son huecos del alumno que viven fuera de `enunciado/`**, y
están DECLARADOS como tales en el `derivacion-starter.txt` de su lab:

```
lab-03 · src/test/java/cl/dgt/tramites/servicio/TramiteServiceTest.java
lab-05 · src/test/java/cl/dgt/tramites/integracion/ListadoIntegracionTest.java

$ grep UnsupportedOperationException …
        throw new UnsupportedOperationException("{{TODO_4}}");
        throw new UnsupportedOperationException("{{TODO_2}}");
```

Son los TODO donde el alumno escribe SUS tests —el `90` comprueba que existan y pasen—, así que
en el `starter` **tienen que** fallar. El criterio «solo puede fallar `enunciado/`» era mío y
era demasiado estrecho: el criterio correcto es «solo puede fallar lo declarado como hueco».
Defecto del arnés de verificación, no del material.

### N2 · `solucion`: verde

```
### lab-03-red-de-seguridad/solucion    EXIT=0  15s   fallos totales: 0
### lab-04-el-arbol-de-tramites/solucion EXIT=0 16s   fallos totales: 0
### lab-05-once-segundos/solucion        EXIT=0 21s   fallos totales: 0
### lab-06-dos-folios-un-numero/solucion EXIT=0 16s   fallos totales: 0
### lab-07-el-portero/solucion           EXIT=0 20s   fallos totales: 0
```

Tronco: `Tests run: 38` + `Tests run: 7` · `BUILD SUCCESS` · `starting PostgreSQL 16.14`.

**Las 7 reglas ArchUnit y sus 7 mordidas, verdes en los doce proyectos.** Muestra:

```
ARCH: Tests run: 8, Failures: 0, Errors: 0 -- in ArquitecturaTest
ARCH: Tests run: 7, Failures: 0, Errors: 0 -- in MordidaDeLosGuardianesTest
```

### N3 · `start-lab.sh` + curls + `99-destruir.sh`

```
lab-03  start-lab EXIT=0 · 7s   health=200  /api/contribuyentes/11111111-1 -> 200
lab-04  start-lab EXIT=0 · 7s   health=200  -> 200
lab-05  start-lab EXIT=0 · 8s   health=200  -> 200
lab-06  start-lab EXIT=0 · 9s   health=200  -> 200
lab-07  start-lab EXIT=0 · 8s   health=200  -> 401   <-- correcto: es el lab del portero
```

El `401` del Lab 07 no es un fallo: ese lab cierra la API por defecto, y un `200` ahí sería
la noticia mala. Destrucción limpia en los cinco:

```
[OK] API detenida (PID …)
[OK] PostgreSQL embebido detenido: no quedó ningún proceso
```

### N4 · `90-validar.sh` en ambos estados — **corrido OFFLINE**

```
lab-03   solucion EXIT=0 :: 6/6 :: LAB 03 APROBADO     starter EXIT=1 :: 4/6 :: NO APROBADO
lab-04   solucion EXIT=0 :: 2/2 :: APROBADO            starter EXIT=1 :: 1/2 :: NO APROBADO
lab-05   solucion EXIT=0 :: 2/2 :: APROBADO            starter EXIT=1 :: 1/2 :: NO APROBADO
lab-06   solucion EXIT=0 :: 2/2 :: LAB 06 APROBADO     starter EXIT=1 :: 1/2 :: NO APROBADO
lab-07   solucion EXIT=0 :: 2/2 :: LAB 07 APROBADO     starter EXIT=1 :: 1/2 :: NO APROBADO
```

Estos veredictos salieron **sin `DGT_ONLINE`**, es decir con `--offline` contra `repo-maven/`.
Es la prueba de que la validación que corre el alumno no necesita red.

*(Los labs 04 y 05 imprimen «LAB 02 APROBADO». Es un bug preexistente, ver §7.3.)*

### N5 · `ps` por ruta exacta

Cero huérfanos tras cada ciclo, en los cinco labs y también tras el `--lotes`:

```
antes de destruir:  app=1  pg=1
despues:            app=0  pg=0
```

### N6 · §4.1 y §4.2 — lo que no puede romperse

**§4.1 · El N+1 sigue doliendo, con los números exactos.**

`solucion-con-n1` (el N+1 vivo) — el contador mide lo mismo que con Docker:

```
java.lang.AssertionError:
[el N+1 dispara una consulta por trámite; una proyección, no]
Expecting actual:
  13L
to be less than or equal to:
  3L
```

`solucion` (con proyección) — pasa:

```
[INFO] Tests run: 1, Failures: 0, Errors: 0 -- in cl.dgt.tramites.enunciado.E1_ContadorDeConsultasIT
[INFO] Tests run: 2, Failures: 0, Errors: 0 -- in cl.dgt.tramites.enunciado.E2_ListadoFuncionalIT
```

**13 contra un presupuesto de 3.** El contraste pedagógico sobrevive intacto a Zonky.

**§4.2 · La carrera del Lab 06 sigue ocurriendo.** Los cuatro, verdes, sin un solo reintento:

```
[INFO] Tests run: 1, Failures: 0 -- in cl.dgt.tramites.enunciado.E1_EmisionConcurrenteIT
[INFO] Tests run: 1, Failures: 0 -- in cl.dgt.tramites.enunciado.E2_IdempotenciaIT
[INFO] Tests run: 1, Failures: 0 -- in cl.dgt.tramites.enunciado.E3_CheckMontoCeroIT
[INFO] Tests run: 1, Failures: 0 -- in cl.dgt.tramites.enunciado.E4_RollbackIT
```

Cero *flakiness* observada. Y no es suerte: el aislamiento por base (§5) es justo lo que hace
que estos tests no se pisen con el resto de la suite.

---

## 5 · Derivación, eslabón por eslabón (§3)

**Los quince eslabones, en sincronía:**

```
tronco -> lab01/solucion      [OK]      lab04/sol -> lab05/solucion   [OK]
lab01/sol -> lab01/starter    [OK]      lab05/sol -> lab05/starter    [OK]
lab01/sol -> lab02/solucion   [OK]      lab05/sol -> lab05/con-n1     [OK]
lab02/sol -> lab02/starter    [OK]      lab05/sol -> lab06/solucion   [OK]
lab02/sol -> lab03/solucion   [OK]      lab06/sol -> lab06/starter    [OK]
lab03/sol -> lab03/starter    [OK]      lab06/sol -> lab07/solucion   [OK]
lab03/sol -> lab04/solucion   [OK]      lab07/sol -> lab07/starter    [OK]
lab04/sol -> lab04/starter    [OK]
```

**La frontera Lab 07 → Lab 08 queda desincronizada, como estaba previsto.** El Lab 08 sigue
con Docker y Testcontainers; se reconcilia en la Fase 2.

Para llegar aquí hubo que unificar tres archivos que divergían **solo en cosmética**: los
labs 01–02 se migraron a mano en la SPEC-022 y el resto con herramienta, y el orden de los
imports y el texto de un comentario no coincidían. La verificación de derivación los cazó, que
es exactamente para lo que existe.

---

## 6 · Peso del repositorio

| | SPEC-022 | SPEC-023 | Δ |
|---|---|---|---|
| `repo-maven/` | 225 MB · 274 jars | **230 MB · 287 jars** | +5 MB · +13 jars |

Cinco labs más costaron **cinco megas**: comparten casi todo, y lo que se sumó son artefactos
concretos (Spring Security y Nimbus del Lab 07, sobre todo). Guard de 95 MB en verde; el
archivo más pesado sigue siendo el binario de PostgreSQL para Darwin, 28,7 MB.

**Proyección para la Fase 2:** si los labs 08–14 se comportan igual, el techo queda **muy por
debajo** de los 400–700 MB que el PO aceptó. Lo que puede mover la aguja es WireMock (Lab 08+)
y Spring Cloud (Lab 14), no el volumen de labs.

---

## 7 · Sorpresas y desviaciones

### 7.1 · El fallo de diseño que destapó el Lab 06 — y su arreglo

La SPEC-022 dejó **una base de datos por JVM**. Con labs de solo lectura eso bastaba. El Lab
06 lo rompió a la primera: sus tests de concurrencia **emiten folios de verdad**, y el
guardián de la semilla —que cuenta cuántos folios hay— encontró catorce donde debe haber uno:

```
SemillaCoherenteIT.laSemillaEsLaQueCreemos:90   expected: 1  but was: 14
SemillaCoherenteIT.nadieMasTieneFolio:81        expected: 0  but was: 13
```

**Era mi diseño, no el material.** Testcontainers daba un contenedor por contexto de Spring, o
sea una base limpia por contexto; mi singleton daba una sola para toda la suite.

Arreglo: **un motor por JVM, una base por contexto.** `PostgresEmbebido.nuevaBase()` crea una
base vacía en el mismo PostgreSQL y Flyway la migra desde cero. Reproduce exactamente la
semántica anterior a coste de milisegundos, en vez de un arranque de PostgreSQL por clase.
Propagado a los 16 proyectos, labs 01–02 incluidos.

Detalle que costó pensarlo: el `@DynamicPropertySource` **no puede** registrar la creación como
proveedor (`registro.add(..., PostgresEmbebido::nuevaBase)`), porque Spring puede invocarlo más
de una vez y cada llamada crearía otra base. Se pide una vez y se guarda:

```java
String url = PostgresEmbebido.nuevaBase();
registro.add("spring.datasource.url", () -> url);
```

### 7.2 · Zonky no trae `psql`, y eso rompió el `--lotes` del Lab 05

El `start-lab.sh --lotes N` sembraba masivamente con `docker compose exec -T postgres psql`.
Sin contenedor, mi primer arreglo fue buscar el `psql` del paquete extraído. No existe:

```
$ ls $TMPDIR/embedded-pg/PG-*/bin/
initdb  pg_ctl  postgres
```

Zonky empaqueta el **servidor**, no el cliente. Solución: la siembra la hace la propia
aplicación al arrancar — `SembradorDeLotes`, un `ApplicationRunner` con `@Profile("dev")` y
`@ConditionalOnProperty(name = "dgt.lotes")`, y `start-lab.sh` le pasa `--dgt.lotes=N`.

Sigue siendo **SQL masivo y no N llamadas HTTP**, que era el punto: si los trámites se crearan
por la API estaríamos midiendo la API en vez de preparar el escenario. Verificado con 200
lotes:

```
[OK] La DGT está viva en el puerto 8099
[OK] Sembrados 200 lotes (los insertó la app al arrancar)
```

Es contenido nuevo en `src/main` de los tres proyectos del Lab 05 — la única adición de código
de producción de esta SPEC, y va declarada aquí porque restaura una función documentada que la
migración había roto.

### 7.3 · Bug preexistente: los labs 04 y 05 se identifican como «LAB 02»

```
lab-04  solucion :: 2/2 verificaciones :: LAB 02 APROBADO
lab-05  solucion :: 2/2 verificaciones :: LAB 02 APROBADO
```

Confirmado preexistente en `HEAD`, línea 100 de sus `bin/90-validar.sh`: el script se copió del
Lab 02 y nadie cambió el rótulo. **No se tocó** — no es de esta SPEC, igual que el
`borrar_seguro`. Candidato natural a entrar en la SPEC-FIX-05.

### 7.4 · Mi primer transformador destrozó cuatro archivos

Buscaba el bloque del contenedor emparejando llaves desde `@TestConfiguration`. En
`BaseConcurrenciaIT` esa anotación es de **nivel superior** y más abajo había otra clase
anidada: el emparejador saltó a las llaves equivocadas y borró todo lo del medio, incluido el
arnés de hilos.

Se revirtieron **todos** los tests con `git checkout` y se rehízo con un transformador que
sustituye el **método** exacto y decide según la `@TestConfiguration` sea de nivel superior (se
queda, publicando el `DataSource`) o anidada (desaparece y el test gana el
`@DynamicPropertySource`). Segundo fallo de la v1, del mismo lote: saltaba los tests que solo
hacen `@Import(Base…)` porque no contienen la palabra `PostgreSQLContainer`, y esos se quedaban
con la propiedad muerta encendida — con lo que arrancaban una segunda base que chocaba con la
del `@Import`.

Queda anotado porque la Fase 2 va a usar la misma herramienta: está en
`docs/specs/informes/` como referencia, y su regla es **sustituir métodos, no emparejar
llaves**.

### 7.5 · Un enunciado que mandaba usar Testcontainers

`ListadoIntegracionTest` del Lab 05 (el TODO_2 que escribe el alumno) decía en su javadoc:
*«Testcontainers vía `@ServiceConnection`»*. Instruir algo imposible es peor que no instruir
nada, así que se reescribió a la base embebida. Mismo criterio que en la SPEC-022 con el
comentario de coordenadas de Testcontainers. Los `@DisplayName` y los mensajes `as(...)`, sin
tocar.

### 7.6 · El tronco no tenía perfil `dev`

Se apoyaba en que Boot detectara el `compose.yaml` sin declarar nada. Al quitarlo se quedaba
sin `DataSource`. Ahora declara `spring.profiles.active: ${DGT_PERFIL:dev}` y lleva el mismo
`application-dev.yml` que los labs — idéntico byte a byte, para no abrir una divergencia nueva
en la raíz de la cadena.

---

## 9 · Vuelo 3 — la verificación en modo avión

Ejecutado el 13 de agosto, 12:46–12:54. Caja negra completa en
`/tmp/caja-negra-vuelo3.log`. Despegó solo 36 s después de lanzarse, y aterrizó con la red
todavía cortada: **ningún tramo quedó contaminado**.

### Evidencia de aislamiento, antes de tocar nada

```
--- ping -c1 github.com ---    ping: cannot resolve github.com: Unknown host   exit=68
--- curl repo1.maven.org ---   curl: (6) Could not resolve host    http=000    exit=6
--- IPs no-loopback ---        (ninguna)
--- ruta por defecto ---       (sin ruta)
--- el .m2 del usuario ---     apartado
--- docker ---                 sin daemon
--- java ---                   openjdk version "25" 2025-09-16 LTS
```

### Los siete labs, con el cable fuera

| Lab | `solucion` | descargas | `start-lab` | huérfanos | `90-validar` solución / starter |
|---|---|---|---|---|---|
| 01 | `EXIT=0` · 14 s | **0** | `health=200` | 0 | APROBADO / NO APROBADO |
| 02 | `EXIT=0` · 15 s | **0** | `health=200` | 0 | APROBADO / NO APROBADO |
| 03 | `EXIT=0` · 14 s | **0** | `health=200` | 0 | APROBADO / NO APROBADO |
| 04 | `EXIT=0` · 14 s | **0** | `health=200` | 0 | APROBADO / NO APROBADO |
| 05 | `EXIT=0` · 19 s | **0** | `health=200` | 0 | APROBADO / NO APROBADO |
| 06 | `EXIT=0` · 15 s | **0** | `health=200` | 0 | APROBADO / NO APROBADO |
| 07 | `EXIT=0` · 18 s | **0** | `health=200` | 0 | APROBADO / NO APROBADO |

**Cero descargas intentadas en las quince suites.** No es que fallaran: Maven ni lo intentó.

Los labs 01 y 02 se revalidaron enteros porque su evidencia del vuelo 1 estaba vencida (los
cambié después, §7.1 y §5). Ahora está fresca — y con ella se cierran **V6 y V10 de la
SPEC-022**.

### §4.1 y §4.2, ya sin red

```
############ §4.1 · el N+1 con el cable fuera ############
Expecting actual:
  13L
to be less than or equal to:
  3L
contador en solucion (debe pasar): Tests run: 1, Failures: 0, Errors: 0

############ §4.2 · la carrera del Lab 06 con el cable fuera ############
Tests run: 1, Failures: 0 -- in E1_EmisionConcurrenteIT
Tests run: 1, Failures: 0 -- in E2_IdempotenciaIT
Tests run: 1, Failures: 0 -- in E3_CheckMontoCeroIT
Tests run: 1, Failures: 0 -- in E4_RollbackIT
```

Los mismos 13 contra 3, y la misma carrera verde, sin red y sin Docker.

### V10-bis · el simulacro del alumno

Caché de binarios de Zonky borrada antes de empezar, para que el arranque en frío lo sea de
verdad:

```
git clone                   4s      (486 MB)
Lab 06 · ./mvnw verify     21s      EXIT=0 · descargas=0     <-- primer comando, todo frío
Lab 07 · ./bin/start-lab.sh 9s      EXIT=0 · health=200
------------------------------
TOTAL                      34s
```

**34 segundos desde el `git clone` hasta la aplicación sirviendo**, sin una sola conexión de
red. Ese es el número que importa para la sala.

### Veredicto del vuelo

```
VUELO 3 CON FALLAS EN: lab-03-red-de-seguridad/starter-FALLOS-AJENOS
                       lab-05-once-segundos/starter-FALLOS-AJENOS
```

**Las dos son falsas alarmas del arnés**, diagnosticadas y explicadas en N1: son los huecos que
el alumno rellena, declarados en su `derivacion-starter.txt`, y deben fallar en el `starter`.
Ninguna otra prueba falló. El material pasó el vuelo entero.

---

## 8 · Lo que queda

**El vuelo 3 ya voló** — resultados en §9. Sigue versionado en
`tools/vuelo-3-modo-avion.sh`, con el mismo diseño probado: espera hasta detectar el corte,
vigila entre tramos que la red no vuelva, caja negra con timestamps
(`/tmp/caja-negra-vuelo3.log`), veredicto (`/tmp/veredicto-vuelo3.txt`) y restauración de
`~/.m2`. **No relanza Docker al aterrizar**, como pidió §6.

Se prepara solo: clona en pista (es local, no necesita red) y aparta `~/.m2` **después** del
despegue — si el vuelo no sale, el entorno del PO queda intacto, que es la lección del vuelo 2.

Cubre **N1–N5 en avión de los siete labs, del 01 al 07**, §4.1 y §4.2 con el cable fuera, y
**V10-bis**: clon fresco → suite del Lab 06 → `start-lab` del Lab 07, cronometrado y con la
caché de Zonky borrada para que el arranque en frío sea de verdad frío.

**Los labs 01 y 02 entran aunque ya volaron** (adición pedida por el Arquitecto al aprobar este
informe). Su evidencia del vuelo 1 está **vencida**: los toqué después, y no de forma cosmética
—el aislamiento por contexto de §7.1 cambió `PostgresEmbebido` en los dos, la unificación de §5
reescribió `SemillaCoherenteIT` y `ContratoRn03IT`, y sus manifiestos se regeneraron—. Validar
con evidencia caducada es no validar. Al revalidarlos enteros se cierran de paso V6 y V10 de la
SPEC-022, que estaban esperando este vuelo.

**Duración real: 8 minutos y 28 segundos** (12:46:06 → 12:54:34), contra los 13–16 estimados.
La estimación fue conservadora: los ciclos de `90-validar` salieron más rápidos de lo previsto.
Para relanzarlo:

```bash
nohup tools/vuelo-3-modo-avion.sh > /tmp/vuelo3.out 2>&1 &
```

**Fase 2:**

1. **Labs 08 a 11** — el salto real: hoy levantan WireMock en contenedor. Hay que decidir si
   pasa a in-process (`wiremock-standalone` como dependencia) y capturar ese jar.
2. **Lab 13** — Jib, y qué significa un lab de contenedores en un curso sin Docker. Decisión
   del Arquitecto, no técnica.
3. **Lab 12 y Lab 14** — el 12 usa los mismos contenedores singleton que el 08; el 14 son cinco
   proyectos Spring Cloud.
4. **SPEC-FIX-05**, ya con dos hallazgos que la esperan: el `borrar_seguro` con symlinks
   (SPEC-022 §8.5) y el rótulo «LAB 02» de los labs 04 y 05 (§7.3 de aquí), que el vuelo volvió
   a exhibir: los labs 04 y 05 anuncian «LAB 02 APROBADO» estando aprobados ellos.
5. **Rediseño de los `TODO_1`/`TODO_2`** del perfil `dev`, todavía marcados
   `PROVISORIO SPEC-022`.
6. **Los 30 `maven-wrapper.properties`** siguen apuntando a `repo.maven.apache.org`. Ya no los
   lee nadie en tronco ni labs 01–07 (el shim los ignora), pero son archivos que dicen algo
   falso. Limpieza cuando el shim llegue a los catorce labs.
7. **Verificar el shim en Git Bash sobre Windows**, en sala. Sigue sin probarse en la
   plataforma real.
