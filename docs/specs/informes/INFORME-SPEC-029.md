# INFORME-SPEC-029 · Tres labs que enseñan con números

**SPEC:** SPEC-029 · **Ejecuta:** mocito · **Fecha:** 15 de agosto de 2026
**Rama:** `spec-029-labs-04-06` desde `main` (v0.4.0) · **PR:** #35, en draft
**Máquina:** Mac Studio del PO (Darwin 25.5.0, `arm64`) · JDK 25.0.4 y Maven 3.9.11 de la maleta

---

## 1 · Veredicto en una línea

**LOS TRES LABS ESTÁN, Y LOS TRES NÚMEROS SALEN** — `lab-04-relaciones` mide 1 SELECT con LAZY
contra 4 con EAGER; `lab-05-rendimiento` mide **201 consultas contra 1**; y `lab-06-concurrencia`
reparte folios repetidos de verdad —`2026-0002` emitido cuatro veces— y deja de repetirlos con el
candado, comprobado **cuatro corridas seguidas**. V1–V7 medidas lab por lab, incluida la V4 de
seguir el guion entero. Ningún lab existente se tocó. Por el camino, **cuatro suposiciones
resultaron falsas al medirlas** (§7) y una de ellas era mía: un defecto que tumbaba la aplicación.

---

## 2 · Qué nace, y con qué forma

| Lab | Tema | Pasos | Sesión | HTTP (practica/solucion) | Postgres |
|---|---|---|---|---|---|
| `lab-04-relaciones` | Relaciones JPA y su coste | 6 | 60–75 min | 8087 / 8088 | 55434 / 55435 |
| `lab-05-rendimiento` | El N+1, medido | 5 | 60–75 min | 8089 / 8090 | 55436 / 55437 |
| `lab-06-concurrencia` | Dos peticiones, el mismo folio | 5 + paso 0 | 75–90 min | 8091 / 8092 | 55438 / 55439 |

Los tres con exactamente cuatro entradas: `README.md`, `PASOS.md`, `practica/`, `solucion/`. Sin
tests, sin `bin/`, sin validadores, sin manifiestos, sin derivación, sin ArchUnit, sin TODOs con
llaves, sin narrativa DGT y sin citas.

Cada proyecto con **su propia base y su propio `.datos-pg/`** (ignorado por git), persistencia
entre reinicios encendida, como el 3.5c.

**El modelo crece, no cambia.** El Lab 04 introduce `Contribuyente` y `Tramite`; los labs 05 y 06
los reciben **ya escritos y funcionando**, y el 06 suma `Folio`. Tres entidades en total en todo el
arco, que es el techo que puso la SPEC.

---

## 3 · La cadena de preguntas

| Al terminar el lab… | …queda esta pregunta | …y la contesta |
|---|---|---|
| **04** | Traer un trámite dispara un SELECT extra. ¿Y con doscientos? | Lab 05, con un contador |
| **05** | Ya es rápido. ¿Y si dos personas piden lo mismo a la vez? | Lab 06 |
| **06** | La corrección bajo concurrencia no se prueba leyendo: se prueba corriéndola en paralelo | el método de trabajo del resto del curso |

---

## 4 · Tabla de verificación

Todo medido en esta máquina, con la maleta y en modo offline. El arnés apaga **por PID**: aquí
importa el doble, porque hay **dos familias de procesos** —el JVM de cada lab y el `postgres` que
Zonky arranca como hijo suyo.

### Lab 04 · relaciones

| # | Prueba | Resultado |
|---|---|---|
| **V1** | `practica/` en su estado de entrega | ✅ Arranca. `Started Lab04Application in 3.088 seconds`. La entidad no tiene todavía la relación y `ddl-auto: validate` no se queja: comprueba lo que la clase declara, no lo que le sobra a la tabla |
| **V2** | `solucion/` completa | ✅ Las seis demos, con su SQL |
| **V3** | **El número del lab** | ✅ Ver abajo |
| **V4** | Seguir `PASOS.md` sobre `practica/` | ✅ **Salida idéntica** a `solucion/` normalizando solo los ids autogenerados (§7.4) |
| **V5** | Persistencia: correr, apagar, correr | ✅ `al arrancar había 0 contribuyentes y 0 trámites` → segunda corrida: **`3 y 6`** |
| **V6** | Offline | ✅ 0 descargas en ambos proyectos |
| **V7** | `ls` | ✅ `PASOS.md README.md practica solucion` |

**V3 — LAZY contra EAGER.** Los 6 trámites traídos con `findAll()`, **sin tocar** el contribuyente
de ninguno, contando los bloques `Hibernate:` entre las dos marcas que imprime la demo:

| | SELECT |
|---|---|
| `@ManyToOne(fetch = FetchType.LAZY)` | **1** |
| `@ManyToOne(fetch = FetchType.EAGER)` | **4** |

Uno por la lista y tres por los tres contribuyentes distintos, que nadie pidió. Se midió cambiando
una palabra en la entidad y volviendo a correr, sin tocar la demo.

Y el paso 5 sale de verdad, atrapado e impreso:

```
  REVENTÓ, y está bien: LazyInitializationException
  mensaje: Could not initialize proxy [cl.dgt.relaciones.entities.Contribuyente#1] - no session
```

### Lab 05 · rendimiento

| # | Prueba | Resultado |
|---|---|---|
| **V1** | `practica/` en su estado de entrega | ✅ Arranca y siembra: `sembrados: 200 contribuyentes, 1000 trámites`. Ninguna demo corre |
| **V2** | `solucion/` completa | ✅ Las cinco demos con su número |
| **V3** | **Los números del lab** | ✅ Ver abajo |
| **V4** | Seguir `PASOS.md` sobre `practica/` | ✅ **Mismos conteos** (201·1·1·1·1) y misma salida; los milisegundos varían, y el guion lo dice |
| **V5** | Persistencia | ✅ Segunda corrida: `base ya sembrada: 200 contribuyentes y 1000 trámites (persisten de la corrida anterior)` |
| **V6** | Offline | ✅ 0 descargas |
| **V7** | `ls` | ✅ Las cuatro entradas |

**V3 — la misma pantalla, cinco formas de armarla.** 200 contribuyentes, 1.000 trámites:

| | consultas | tiempo |
|---|---|---|
| 1 · `findAll()` + tocar la relación | **201** | 79 ms |
| 2 · `JOIN FETCH` | **1** | 19 ms |
| 3 · `@EntityGraph` | **1** | 20 ms |
| 4 · proyección a un `record` | **1** | 12 ms |
| 5 · pantalla que no usa los trámites | 1 | 2 ms |

Las cinco devuelven lo mismo: `200 contribuyentes · 1000 trámites`.

### Lab 06 · concurrencia

| # | Prueba | Resultado |
|---|---|---|
| **V1** | `practica/` en su estado de entrega | ✅ `Started Lab06Application in 3.226 seconds`, ninguna demo corre |
| **V2** | `solucion/` completa | ✅ Las tres demos |
| **V3** | **Los folios repetidos** | ✅ Ver abajo — **repetido 4 veces** |
| **V4** | Seguir `PASOS.md` sobre `practica/` | ✅ Mismo comportamiento que `solucion/`: 0 repetidos y rechazos de la base en la demo 2, 21 distintos en la 3 |
| **V5** | Persistencia | ✅ `.datos-pg/` sobrevive; segunda corrida arranca sobre la base existente |
| **V6** | Offline | ✅ 0 descargas |
| **V7** | `ls` | ✅ Las cuatro entradas |

**V3 — la carrera.** Sobre el año 2026, que arranca con el folio de apertura `2026-0001`:

| | folios en la tabla | números distintos | repetidos |
|---|---|---|---|
| 1 · diez, de una en una | 11 | 11 | ninguno |
| 2 · veinte **a la vez**, sin candado | 21 | **9** | **8 números** |
| 3 · veinte a la vez, con candado | 21 | **21** | ninguno |

```
REPETIDOS : [2026-0002 (x4), 2026-0003 (x3), 2026-0004 (x2), 2026-0005 (x2),
             2026-0006 (x2), 2026-0007 (x2), 2026-0008 (x2), 2026-0009 (x3)]
```

**Una prueba de concurrencia que sale verde una vez no prueba nada**, así que se corrió **cuatro
veces**. Los números exactos cambian —es una carrera—, el veredicto no:

| corrida | sin candado (distintos) | con candado (distintos) | repetidos con candado |
|---|---|---|---|
| 1 | 9 de 21 | 21 de 21 | ninguno |
| 2 | 10 de 21 | 21 de 21 | ninguno |
| 3 | 9 de 21 | 21 de 21 | ninguno |
| 4 | 10 de 21 | 21 de 21 | ninguno |

Y con la restricción del paso 5 puesta, la demo 2 cambia de síntoma en vez de desaparecer:
`números distintos: 11 · REPETIDOS: ninguno · rechazados por la base: 10`.

---

## 5 · Transversales

| Prueba | Resultado |
|---|---|
| **Convivencia** | ✅ Los **seis** proyectos arriba a la vez: 6 Tomcat (8087–8092) y **6 PostgreSQL** (55434–55439), los doce puertos escuchando, sin un solo `already in use` ni conflicto de directorio de datos |
| **Labs viejos** | ✅ No se tocó ninguno. `git diff --name-only main..HEAD` fuera de los tres directorios nuevos: solo `ESTADO.md`, el informe y una línea de CI |
| **Gate `siembra`** | ✅ 0 fallos tras enseñarle `PASOS.md` (§7.5) |
| **Gate `deriva`** | ✅ No afectado: recorre una lista explícita de labs y los tres nuevos no están en ella |
| **Gate `labs-sh`** | ✅ No afectado: los tres labs no aportan ni un `.sh` |
| **Guarda de 95 MB** | ✅ El archivo más grande de los tres labs es `lab-06-concurrencia/PASOS.md`, **12,2 KB** |
| **Higiene de git** | ✅ Ni un `.datos-pg/` ni un `target/` versionado, con 39 MB de datos por proyecto en disco |
| **`du -sh repo-maven`** | **230 MB**, sin cambios. Los tres labs **no añadieron artefactos**: Zonky, Flyway y `data-jpa` ya viajaban en `main` desde las SPEC-022/023 |

### El CI

| Job | |
|---|---|
| `app`, `grpc`, `lab14`, `labs-sh` (ubuntu y windows), `siembra`, `temario` | ✅ |
| `deriva · labs en sincronía con su base` | ❌ **el rojo que ya traía `main`** |

Comprobado en vez de supuesto: se compararon los `[ERROR]` de la última corrida de `main` con los
de esta rama y son **las mismas 15 líneas**, idénticas — `1 eslabon(es) con deriva silenciosa`,
`13 archivo(s) divergieron sin declararse`, y la misma lista. Es el `lab-08` atrasado respecto del
`lab-07` desde el PR #27, documentado en `ESTADO.md` §2. **7 de 8 en verde, cero rojos nuevos.**

---

## 6 · Decisiones tomadas al ejecutar

**D-029-1 · El candado se agarra del folio de apertura.** Un bloqueo pesimista bloquea **filas**:
si la fila no existe, no hay nada que bloquear. Por eso cada demo deja el año con un folio número 1
y el candado se toma sobre él. Se descartó bloquear «todas las filas del año» porque en
`READ COMMITTED` el hilo que espera no ve las filas que insertó el otro mientras estaba bloqueado —
volverían los duplicados. Queda dicho en el código y en el guion, porque **es** la lección.

**D-029-2 · La demo del crimen informa de las dos formas.** Con la restricción del paso 5 puesta,
la carrera ya no produce duplicados sino **rechazos**. En vez de esconderlo, la misma demo cuenta
las dos cosas —`REPETIDOS` y `rechazados por la base`— y el paso 5 enseña justamente ese cambio de
síntoma. Así `practica/` y `solucion/` coinciden al terminar (V4).

**D-029-3 · `show-sql` encendido en los labs 04 y 06, apagado en el 05.** En el 04 el SQL **es** el
contenido; en el 06 hay una línea que hay que ver (`for no key update`). En el 05 la demo 1 dispara
201 consultas: con el SQL encendido serían mil líneas y el número, que es la lección, se perdería.

**D-029-4 · `open-in-view: false`, explícito.** Con el valor por defecto (`true`) la sesión seguiría
abierta durante toda la petición web y la `LazyInitializationException` del paso 5 del Lab 04 no
ocurriría nunca dentro de un controller. Se apaga para ver el comportamiento de verdad.

**D-029-5 · El gate `siembra` aprende `PASOS.md` también aquí.** Ver §7.5.

---

## 7 · Sorpresas y desviaciones

### 7.1 · `EAGER` no arregla el N+1. Lo empeora.

La SPEC anticipaba, para el paso 5 del Lab 05: «arreglar el N+1 poniendo EAGER en la entidad.
Medirlo: **arregla esta pantalla y rompe todas las demás**».

La primera mitad de esa frase es falsa, y se ve al medirla:

| | LAZY | EAGER |
|---|---|---|
| demo 1 — la pantalla que sí quería los trámites | 201 · 79 ms | **201 · 145 ms** |
| demo 5 — la pantalla que no los quería | 1 · 2 ms | **201 · 58 ms** |

Poner `EAGER` en un `@OneToMany` no hace que Hibernate traiga la colección de una vez: hace que
dispare **las mismas 200 consultas siempre**, en vez de solo cuando alguien toca la lista. La
pantalla que se quería arreglar sigue igual de mal y encima tarda casi el doble; y la que estaba
bien pasa de 1 consulta a 201.

Es **estrictamente peor**, y el guion lo enseña con las dos tablas al lado. La lección resultó más
fuerte que la prevista.

### 7.2 · El `JOIN FETCH` cuesta 1 consulta, no 2

La SPEC decía «volver a medir: 2 consultas». Medido: **1**. Se cita el número real en el guion, en
el README y aquí.

### 7.3 · El SQL del candado no dice `for update`

El guion prometía ver `for update` en la consola. Al buscarlo, **cero ocurrencias** — y la única
coincidencia en el log era el propio título de la demo, que yo había escrito así.

El candado sí estaba: 20 ejecuciones de la consulta bloqueante. Lo que pasa es que **Hibernate 7
sobre PostgreSQL escribe otra cosa**, y `format_sql` además la parte en dos líneas:

```
    where
        f1_0.anio=?
        and f1_0.numero=1
    for
        no key update of f1_0
```

`FOR NO KEY UPDATE` es una variante más fina del bloqueo, y para lo que aquí importa es el mismo
candado — lo confirma el resultado, 21 de 21 en cuatro corridas. Pero **un alumno que busque `for
update` en su consola no encuentra nada y concluye que su candado no funciona**. El guion lo avisa
en un recuadro, y los títulos y comentarios se corrigieron para no prometer un texto que no sale.

### 7.4 · La restricción no se puede poner sobre los datos que la incumplen

Al añadir la migración del paso 5 sobre la base que la demo 2 acababa de llenar de duplicados,
Flyway no puede crear la restricción: PostgreSQL rechaza un `unique` que los datos existentes ya
violan.

Se resolvió **dentro de la migración**, borrando los duplicados y quedándose con el primero de
cada `(anio, numero)`. Y el `delete` se quedó en el material a propósito, porque enseña más que la
restricción: *la restricción que faltaba no se puede poner hasta haber arreglado a mano lo que se
coló sin ella*. Así es exactamente como duele en un sistema real.

*(También, menor: los ids autogenerados no vuelven a 1 tras un `deleteAll` —`bigserial` no
reinicia su secuencia—, así que al comparar `practica/` con `solucion/` los números pueden no
coincidir. Queda avisado en el guion del Lab 04 para que nadie crea que se equivocó.)*

### 7.5 · Un defecto propio: auto-invocación de `@Transactional`

La primera versión del Lab 06 **tumbaba la aplicación** en la demo 2:

```
org.springframework.dao.InvalidDataAccessApiUsageException: No EntityManager with actual
transaction available for current thread - cannot reliably process 'remove' call
```

Causa: `prepararElAnio()` estaba anotado `@Transactional`, pero lo llaman las demos **de su misma
clase**. Una llamada entre métodos del mismo objeto no pasa por el proxy de Spring, así que la
anotación no se aplicaba.

Lo peor no fue el fallo, sino **por qué no apareció antes**: la demo 1 pasaba sin problema porque
la tabla estaba vacía y `deleteByAnio` no tenía nada que borrar. Un verde por casualidad, que es la
peor clase de verde. Se arregló donde corresponde —la transacción la pone el repositorio— y la
trampa quedó explicada en el código, que además es materia del lab.

### 7.6 · El `timeout` de Homebrew, otra vez

Al medir el Lab 04 apareció `error: release version 25 not supported`. Es el mismo mecanismo que
cerró la SPEC-024: `/usr/local/bin/timeout` es un binario x86_64 que corre bajo Rosetta, `uname -m`
devuelve `x86_64`, el shim no encuentra el JDK `macos-aarch64` y cae al Java del sistema **por
diseño**. Culpa del arnés, no del material. El arnés de esta SPEC lo lleva escrito en su cabecera
para no volver a tropezar.

### 7.7 · El gate `siembra`, por tercera vez

Rojo esperado: el fallback a `PASOS.md` lo introdujo la SPEC-027 y sigue **sin mergear**, así que
`main` no lo tiene y esta rama sale de `main`. Se aplicó el mismo arreglo, escrito igual que en las
SPEC-027 y SPEC-028, para que las tres ramas converjan sin pelea.

**Es la tercera vez que el mismo arreglo viaja en una rama distinta.** Se apaga solo en cuanto
mergee cualquiera de los tres PRs; mientras tanto, cada rama nueva que salga de `main` con un lab
de construcción guiada lo va a necesitar otra vez.

---

## 8 · Lo que queda

- **PR #35 en draft, esperando la firma del PO.**
- **La prueba de aceptación es V4**: sentarse con `PASOS.md` y `practica/` y llegar al final sin
  abrir `solucion/`. Es la única que el ejecutor no puede hacer por definición.
- **Tiempos de sesión estimados, no medidos.** Salen de contar pasos y arranques, no de cronometrar
  una clase.
- **Una decisión de fondo para el PO, que no es del mocito:** con los labs 04, 05 y 06 nuevos, el
  arco antiguo (`lab-04-el-arbol-de-tramites`, `lab-05-once-segundos`,
  `lab-06-dos-folios-un-numero`) cubre los mismos tres temas con otro formato. Ahora conviven seis
  laboratorios para tres asuntos. **Qué se dicta y qué se retira es decisión suya.**
- Sigue abierto de antes: **PR #31** (SPEC-025), **PR #33** (SPEC-027), **PR #34** (SPEC-028), los
  tres en draft esperando firma; la anotación **A2.4** de la SPEC-024; y la observación de que en
  `main` conviven dos `mvnw` distintos (§7.4 del INFORME-SPEC-028).
