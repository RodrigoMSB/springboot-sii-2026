# INFORME-SPEC-030 · Un solo arco hasta el Lab 06

**SPEC:** SPEC-030 · **Ejecuta:** mocito · **Fecha:** 15 de agosto de 2026
**Rama:** `spec-030-consolidar-arco-nuevo` · **Tag al cierre:** `material-v0.5.0`
**Máquina:** Mac Studio del PO (Darwin 25.5.0, `arm64`) · JDK 25.0.4 y Maven 3.9.11 de la maleta

---

## 1 · Veredicto en una línea

**`main` QUEDÓ CON UN SOLO ARCO HASTA EL LAB 06** — los tres PRs (#33, #34, #35) están dentro,
los siete labs viejos del 00 al 06 salieron, los 16 proyectos del arco nuevo arrancan y sus
números siguen saliendo iguales. **Dos cosas no se hicieron como decía la SPEC, y las dos por
medirlas:** la renumeración es una cascada bloqueada de punta a punta (§4) y el borrado hizo que
el gate `deriva` empezara a inventar divergencias falsas (§3), lo que obligó a arreglar el propio
gate. El PR #32 quedó cerrado con su comentario.

---

## 2 · Paso 1 · Los tres PRs dentro de `main`

| PR | Rama | Qué trajo | Conflicto |
|---|---|---|---|
| **#33** | `spec-027-lab-3-5-jpa` | `lab-03c-jpa` | ninguno |
| **#34** | `spec-028-labs-00-03` | `lab-00-hola-mundo`, `lab-01-web`, `lab-02-di`, `lab-03-errores` | `material-ci.yml` + `ESTADO.md` |
| **#35** | `spec-029-labs-04-06` | `lab-04-relaciones`, `lab-05-rendimiento`, `lab-06-concurrencia` | `material-ci.yml` + `ESTADO.md` |

El conflicto era el previsto: las tres ramas llevaban **el mismo arreglo del gate `siembra`** (el
fallback a `PASOS.md`) con distinto comentario. El código era idéntico byte a byte; solo cambiaba
el texto que lo explicaba. Se resolvió dejando una copia con una redacción que cubre el arco
entero y no solo el Lab 3.5. En `ESTADO.md`, las dos mitades del inventario eran complementarias
y se conservaron las dos.

**CI de `main` tras los tres merges** (corrida `31918346247`): 7 de 8 en verde, y el rojo de
`deriva` **idéntico línea por línea** al de antes de los merges — se compararon los `[ERROR]` de
la corrida previa (`31852434457`) con los de la nueva y son **las mismas 15 líneas**.

**PR #32** (el lab del apóstrofe, descartado en la SPEC-027) ya estaba cerrado sin merge; se le
añadió el comentario que apunta aquí.

---

## 3 · Paso 2 · Qué se llevó por delante el borrado

### 3.a · El inventario previo

Antes de borrar nada:

| Pregunta | Respuesta medida |
|---|---|
| ¿Algún lab del **07 al 14** menciona a los que se van? | **Ninguno.** Cero referencias |
| ¿Los ocho labs **nuevos** citan el troubleshooting del Lab 00? | **Ninguno.** Cero citas |
| ¿Dónde viven los `troubleshooting.md`? | **Uno por lab**, los 15. Las citas del Lab 14 apuntan al suyo |
| ¿Qué recorre la cadena de derivación? | `dgt-tramites-api → 01 → 02 → 03 → 04 → 05 → 06 → 07 → … → 13` |

**La premisa de la SPEC sobre el troubleshooting no se sostuvo.** La SPEC decía «está
referenciado desde otros labs (el 3.5c lo cita)». Medido: el 3.5c **no lo cita**, ni él ni
ninguno de los ocho nuevos. Cada lab tiene el suyo propio. Se movió igualmente, por otra razón
que sí es real: `ESTADO.md` lo citaba, y su contenido sobre Java, proxy, `JAVA_HOME`, el
`WARNING` de `sun.misc.Unsafe` y los CRLF de Windows sigue siendo útil para el arco nuevo.

### 3.b · Lo que se movió

| De | A | Por qué |
|---|---|---|
| `labs/lab-00-estacion-base/docs/troubleshooting.md` | `docs/troubleshooting.md` | Citado desde `ESTADO.md`; contenido de entorno todavía vigente |
| `labs/lab-00-estacion-base/docs/entorno-alumno.md` | `docs/entorno-alumno.md` | **Arregla una ruta que ya estaba rota**: `docs/adn/adn-cypress.md` y la SPEC-002 lo citaban como `docs/entorno-alumno.md`, y esa ruta no existía |

Al troubleshooting se le añadió una cabecera que **distingue qué filas siguen vigentes**
(T-02, T-05, T-09, T-12, T-13) de las que **hablan de herramientas retiradas** (`bin/start-lab.sh`,
`00-verificar.sh`, `99-destruir.sh`, `.estado/`, Docker). **No se reescribió ni una fila**: se
conservó la tabla entera y se le puso encima el mapa para leerla.

### 3.c · El gate `deriva` — lo importante de este paso

Al borrar los labs 01–06, el gate empezó a reportar **3 divergencias en el Lab 07** que **no
existían antes**:

```
[ERROR] Deriva silenciosa: src/main/resources/application.yml difiere de la base y no está declarado
[ERROR] Deriva silenciosa: src/main/java/cl/dgt/tramites/DgtTramitesApiApplication.java …
[ERROR] Deriva silenciosa: src/main/java/cl/dgt/tramites/domain/entity/LineaF29.java …
[ERROR] 3 archivo(s) divergieron sin declararse.
```

**Y eran falsas.** La causa está en una línea del propio script:

```bash
[ -d "$LAB/solucion" ] || continue
```

Un `continue` **mudo**. Al faltar los directorios, la variable `ANTERIOR` nunca avanzaba y se
quedaba en `dgt-tramites-api`, así que el Lab 07 acababa comparándose **contra el tronco, que no
es su base** — entre medio hay seis labs de cambios. El gate no estaba detectando deriva:
la estaba inventando.

Es exactamente la deriva invisible que ese job existe para impedir, cometida por el job. Y choca
de frente con la ley de la casa: **un validador solo puede fallar por la razón que dice**.

**El arreglo** —que no apaga nada, y no declara ni una divergencia— hace que el gate hable:

```
[INFO] labs/lab-01-del-otro-lado-del-boton ya no esta en el repositorio: la cadena se corta aqui.
… (una por cada lab retirado)
[ERROR] labs/lab-07-el-portero no se puede verificar contra su base: la base ya no esta
        en el repositorio. NO se compara contra dgt-tramites-api, que no es
        su base — eso reportaria divergencias falsas.
        Pendiente de la decision del PO sobre los labs 07 a 14.
```

y después **re-ancla la cadena** en el Lab 07, para que del 08 al 14 se sigan verificando. Los
labs retirados **siguen en la lista** de la cadena a propósito: es lo que hace que su ausencia se
detecte en vez de pasar en silencio.

**Estado resultante del gate, sin maquillar:**

| | Antes de la SPEC-030 | Después |
|---|---|---|
| Eslabones en rojo | **1** (lab-08 vs lab-07, 13 archivos) | **2** |
| El nuevo | — | lab-07 sin base verificable, **declarado como tal** |
| El de siempre | 13 archivos sin declarar | **idéntico, 13 archivos** |

**El rojo nuevo es real y es correcto que esté**: la cadena está cortada por arriba porque el
arco antiguo hasta el 06 ya no está. Se apaga el día que el PO decida qué se hace con los labs
07–14 — migrarlos, mantenerlos o retirarlos. **No se ha declarado ni una divergencia falsa para
silenciarlo.**

### 3.d · Referencias vivas arregladas

| Archivo | Qué tenía | Qué se hizo |
|---|---|---|
| `docs/guion-reinicio-de-sala.md` | `cd labs/lab-00-estacion-base && ./bin/00-verificar.sh` y `cd labs/lab-01-…/starter` | Apuntan al `lab-00-hola-mundo/solucion` del arco nuevo |
| `.github/workflows/material-ci.yml` | Dos comentarios con la ruta vieja de `entorno-alumno.md` | Apuntan a `docs/entorno-alumno.md` |
| `docs/troubleshooting.md` | La fila **T-06** citaba `.estado/dgt.log` | Marcada en su propia línea como ruta retirada |
| `tools/vuelo-3-modo-avion.sh`, `vuelo-4-modo-avion.sh` | Recorren los labs 01–07 del arco antiguo | Cabecera de **arnés histórico**: se conservan como el procedimiento con el que se midió lo que citan sus informes, no como algo ejecutable |

**Lo que NO se tocó, y a propósito:** `docs/specs/*` y `docs/decisiones.md` siguen mencionando
los labs retirados. Son el **registro de lo que pasó**; reescribirlos sería falsificar el
historial.

---

## 4 · Paso 3 · La renumeración — NO aplicada, y por qué

La SPEC pedía:

| Hoy | Queda |
|---|---|
| `lab-03c-jpa` | `lab-04-jpa` |
| `lab-04-relaciones` | `lab-05-relaciones` |
| `lab-05-rendimiento` | `lab-06-rendimiento` |
| `lab-06-concurrencia` | `lab-07-concurrencia` |

y advertía del choque del último con `lab-07-el-portero`, resolviendo: «no renumerar el
06-concurrencia… **El resto de la renumeración sí se aplica**».

**El resto no se puede aplicar.** La renumeración no son cuatro cambios independientes: es una
**cascada**, y cada destino está ocupado por el lab siguiente:

```
  lab-03c-jpa          -> lab-04-…   destino ocupado por lab-04-relaciones
  lab-04-relaciones    -> lab-05-…   destino ocupado por lab-05-rendimiento
  lab-05-rendimiento   -> lab-06-…   destino ocupado por lab-06-concurrencia
  lab-06-concurrencia  -> lab-07-…   destino ocupado por lab-07-el-portero   ← BLOQUEADO
```

Al bloquearse el último eslabón se bloquean todos. Aplicar «el resto» dejaría **dos labs con el
mismo número** —`lab-06-rendimiento` junto a `lab-06-concurrencia`, o `lab-04-jpa` junto a
`lab-04-relaciones`—, que es peor que el estado actual y contradice el propio criterio de la
SPEC («que la secuencia se lea bien»).

**Decisión: no se renumeró nada.** La secuencia actual es `00, 01, 02, 03, 03c, 04, 05, 06` —
ocho labs contiguos con un medio paso heredado del «Lab 3.5». Se lee bien; el único resto es el
nombre `03c`.

**Se desbloquea** el día que el PO decida sobre los labs 07–14: si el `lab-07-el-portero` se
retira o se renumera, la cascada entera cae sola en un solo movimiento.

---

## 5 · Verificación

| # | Prueba | Resultado |
|---|---|---|
| **V1** | Los 16 proyectos: compilan y `spring-boot:run` | ✅ 16 de 16. Números abajo |
| **V2** | `ls labs/` | ✅ Los ocho nuevos + `lab-07-el-portero` a `lab-14-…` + `lib`. **Ninguno del 00 al 06 viejo** |
| **V3** | Referencias a los directorios borrados | ✅ **Cero enlaces rotos** fuera del registro histórico (§3.d) |
| **V4** | CI en `main` | Ver §6 |
| **V5** | `du -sh labs/*` en limpio | ✅ De **100 KB** (`lab-00`) a **216 KB** (`lab-06`). Ninguno cerca de 1 MB |
| **V6** | Labs 07–14 | ✅ `git diff --name-only main..HEAD` no toca ni un archivo suyo |

### Los números del material, tras el borrado

**Lab 04 · relaciones** — los 6 trámites sin tocar su contribuyente:

```
LAZY  -> 1 SELECT          EAGER -> 4 SELECT
REVENTÓ, y está bien: LazyInitializationException
mensaje: Could not initialize proxy [cl.dgt.relaciones.entities.Contribuyente#1] - no session
```

**Lab 05 · rendimiento** — 200 contribuyentes, 1.000 trámites:

```
1 · EL CRIMEN        CONSULTAS: 201  ·  66 ms
2 · JOIN FETCH       CONSULTAS: 1    ·  18 ms
3 · @EntityGraph     CONSULTAS: 1    ·  17 ms
4 · PROYECCIÓN       CONSULTAS: 1    ·   9 ms
5 · LA OTRA PANTALLA CONSULTAS: 1    ·   2 ms
```

**Lab 06 · concurrencia** — 20 emisiones simultáneas:

```
practica (sin la restricción, estado del paso 2):
  2 · EL CRIMEN     21 folios · 15 distintos
     REPETIDOS: [2026-0002 (x2), 2026-0005 (x2), 2026-0006 (x2),
                 2026-0007 (x2), 2026-0009 (x2), 2026-0010 (x2)]

solucion (con candado y restricción):
  2 · EL CRIMEN     REPETIDOS: ninguno · rechazados por la base: 9
  3 · CON CANDADO   21 folios · 21 distintos · REPETIDOS: ninguno
```

Los números exactos de la demo 2 varían en cada corrida —es una carrera—; el veredicto no. Van
**seis corridas** con el mismo: sin candado siempre hay repetidos, con candado nunca.

**Lab 00 a 03 y 3.5c** — arranque y endpoints verificados: `lab-00` arranca, imprime y termina;
los demás levantan su Tomcat y responden (200 en `/hola`, `/productos/quien` y
`/api/observaciones`; 404 con cuerpo en `/productos/99`; 400 con los campos en el POST inválido).

---

## 6 · El CI, job por job

| Job | Antes de la SPEC-030 | Después |
|---|---|---|
| `app · dgt-tramites-api (verify)` | ✅ | ✅ |
| `grpc · la demo del Lab 08` | ✅ | ✅ |
| `lab14 · el sistema de microservicios` | ✅ | ✅ |
| `labs-sh · andamiaje (ubuntu-latest)` | ✅ | ✅ |
| `labs-sh · andamiaje (windows-latest)` | ✅ | ✅ |
| `siembra` | ✅ | ✅ |
| `temario` | ✅ | ✅ |
| `deriva` | ❌ 1 eslabón (13 archivos) | ❌ **2 eslabones** |

**El rojo de `deriva` creció, y está explicado**: al eslabón preexistente (lab-08 vs lab-07, los
mismos 13 archivos) se suma el Lab 07, que ya no tiene base verificable en el repositorio. No es
un defecto nuevo del material: es la consecuencia declarada de retirar el arco antiguo hasta el
06, y espera la decisión del PO sobre los labs 07–14.

---

## 7 · Sorpresas y desviaciones

1. **El gate `deriva` inventaba divergencias** (§3.c). El hallazgo más serio de la SPEC: un
   `continue` mudo convertía un validador en un mentiroso. Arreglado sin apagar nada.
2. **La renumeración es una cascada bloqueada** (§4). La SPEC la trataba como cuatro cambios
   independientes.
3. **La premisa del troubleshooting era falsa** (§3.a): ningún lab nuevo lo citaba, y cada lab
   tiene el suyo. Se movió igualmente, por una razón distinta y real.
4. **Mover `entorno-alumno.md` arregló una ruta que llevaba rota desde antes**: dos documentos
   lo citaban como `docs/entorno-alumno.md` y ahí no había nada.

---

## 8 · Lo que queda

- **La decisión de fondo, del PO: qué se hace con los labs 07 al 14.** De ella dependen dos cosas
  que hoy están a medias: el rojo de `deriva` y la renumeración bloqueada.
- **La prueba de aceptación del arco nuevo sigue pendiente**: sentarse con `PASOS.md` y
  `practica/` y llegar al final sin abrir `solucion/`, en los ocho labs. Es la única que el
  ejecutor no puede hacer por definición.
- **PR #31** (SPEC-025, labs 08–11 sin Docker) sigue abierto en draft. Toca labs del 07 al 14, así
  que su destino va con la decisión anterior.
- Sigue abierta la anotación **A2.4** de la SPEC-024 (el cartel del Firewall en Windows).
- **Observación heredada**: en los labs 08–14 conviven dos `mvnw` distintos — los del arco nuevo y
  los labs 01–07 llevaban el shim de la maleta, y del 08 en adelante sigue el wrapper original de
  Apache, que descarga Maven de internet. Con el arco antiguo retirado, hoy **el primer lab con el
  wrapper viejo es el 08**.
