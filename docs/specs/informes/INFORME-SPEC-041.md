# INFORME-SPEC-041 · La documentación de `instructor/` que explica el porqué — labs 04 a 07

**Ejecuta:** mocito · **Rama:** `spec-041-instructor-el-porque` · **Fecha:** 27 de agosto de 2026
**Origen:** SPEC-041 del PO, emitida después de dictar el lab 04 con `instructor/` al lado.

---

## 0 · Resumen

**102 decisiones técnicas documentadas** con el formato de cuatro partes, repartidas en los cuatro
labs: **34** en el 04, **22** en el 05, **23** en el 06 y **23** en el 07 (§2).

**Las ocho que la SPEC nombraba están las ocho**, y el barrido encontró **94 más** que la lista no
pedía (§3).

**Cero líneas de código tocadas, y está medido**, no declarado: el código desnudo —sin comentarios
ni líneas en blanco— de los **37 archivos `.java`** de `instructor/` es **idéntico** al de
`solucion/`, antes y después. **0 divergencias** (§4).

**Los cuatro proyectos compilan**, con las fuentes de `instructor/` montadas sobre un proyecto de
verdad: **BUILD SUCCESS offline, 37 clases** (§4).

**Nada de esto viaja al repositorio, y es lo previsto:** `labs/*/instructor/` está en el
`.gitignore` por D-031-2. Este informe y la SPEC son lo único que se commitea. Es una consecuencia
que conviene tener delante, y está en §6.

---

## 1 · El formato, y por qué éste

La SPEC pedía cuatro cosas por decisión, en orden y sin adornos. Se fijó **un solo recuadro**, con
la misma forma en los `.java`, el `application.yml`, el `pom.xml` y las migraciones `.sql`:

```java
// =============================================================================
//  POR QUÉ · @GeneratedValue(strategy = GenerationType.IDENTITY)
// -----------------------------------------------------------------------------
//  QUÉ HACE · …
//
//  ALTERNATIVAS · …
//
//  POR QUÉ ESTA · …
//
//  CUÁNDO OTRA · …
// =============================================================================
```

Tres decisiones de forma que conviene dejar escritas:

- **Va pegado al código, no en el javadoc.** El javadoc de estas clases ya cuenta *qué hace* la
  pieza; el recuadro responde otra pregunta y se pone justo encima de la anotación o de la línea
  que decide, para que se lea con ella delante.
- **Es greppable.** `grep -rn 'POR QUÉ ·' instructor/` lista las decisiones de un lab en un
  segundo. Es lo que hace posible el inventario de la §2 — y lo que le permite a quien prepara la
  clase repasar solo los porqués.
- **En XML, reglas con `=` y jamás dos guiones seguidos** (D-FIX10-1). Los cuatro `pom.xml` se
  comprobaron después: parsean, y tienen **cero comentarios con `--`**.

**Donde no hay alternativa real, se dice y se pasa.** Ejemplo, en `Observacion.java`:

```
//  ALTERNATIVAS · Ninguna real. Sin @Entity la clase es un objeto Java normal y
//  ninguna de las otras anotaciones significa nada. Lo único parecido es
//  @Embeddable, que no es una alternativa sino otra cosa […]
//
//  Se dice y se pasa.
```

No se inventaron opciones para rellenar el formato.

Y **el `LEEME.md` de los cuatro labs** explica la convención, con el recuadro de ejemplo y el
`grep`. Quien abra `instructor/` sin contexto sabe qué está mirando.

---

## 2 · Las decisiones documentadas, por lab

### Lab 04 · JPA — **34**

| Archivo | Decisión |
|---|---|
| `entities/Observacion.java` | `@Entity` · `@Table(name=…)` · **`@GeneratedValue(IDENTITY)`** · `Long` frente a `long` · **`@Column(nullable, length)`** · `LocalDate` · **el `protected` del constructor vacío** · un solo setter |
| `repositories/ObservacionRepository.java` | una interfaz y no una clase · **`JpaRepository` frente a `CrudRepository` y `PagingAndSortingRepository`** · consulta derivada frente a `@Query` · `long` de retorno |
| `demos/DemosJpa.java` | `@Service` frente a `@Component` y `@Repository` · la dependencia por constructor y el campo `final` · `@Transactional` a secas · `deleteAll()` frente a `deleteAllInBatch()` |
| `controllers/ObservacionController.java` | `@RestController` · devolver la entidad sin DTO · `@RequestParam(required=false)` frente a `Optional` · `ResponseEntity` frente al retorno pelado · 201 frente a 200 |
| `Lab04Application.java` | `@SpringBootApplication` · `CommandLineRunner` · el `DataSource` como `@Bean` |
| `application.yml` | `show-sql` frente al logger · `ddl-auto: validate` · Flyway frente a Liquibase |
| `pom.xml` | el PostgreSQL de Zonky frente a Testcontainers y H2 · el scope `runtime` |
| `db/migration/V1__observacion.sql` | `BIGSERIAL` · `VARCHAR(n)` frente a `TEXT` |
| `infra/` | `System.exit(1)` frente a la excepción · el sondeo con `ServerSocket` · `tryLock()` frente a `lock()` |

### Lab 05 · Relaciones — **22**

| Archivo | Decisión |
|---|---|
| `entities/Contribuyente.java` | `unique = true` · `@Column(name = "razon_social")` · **`mappedBy`, y qué pasa en el lado equivocado** · `@OneToMany` sin `cascade` ni `orphanRemoval` · `List` frente a `Set`, e inicializada |
| `entities/Tramite.java` | **`fetch = LAZY` en un `@ManyToOne`, y por qué la especificación pone `EAGER`** · **`@JoinColumn`, y qué ocurre si se omite** |
| `demos/DemosRelaciones.java` | **`@Transactional(readOnly = true)` frente a `@Transactional` a secas** |
| `repositories/` | `Optional` frente a la entidad pelada · `findByContribuyenteRut` y el guion bajo que desambigua |
| `application.yml` | `open-in-view: false` · `ddl-auto: validate` · `show-sql` |
| `V1__contribuyente_y_tramite.sql` | la clave foránea, sin `ON DELETE` y sin índice declarado |
| `pom.xml` · `Lab05Application` · `infra/` | los mismos cinco del lab 04 |

### Lab 06 · Rendimiento — **23**

| Archivo | Decisión |
|---|---|
| `repositories/ContribuyenteRepository.java` | **las tres formas de arreglar el N+1 y cuál se elige cuándo** · el `distinct` y qué pasa si se olvida · `findAllBy` y ese `By` final |
| `dto/ResumenContribuyente.java` | un `record` frente a una clase y frente a la proyección por interfaz |
| `soporte/CargadorDeDatos.java` | `@Component` aquí y `@Service` en las demos · `saveAll()` frente a `save()` en el bucle (y por qué **no** agrupa en lotes) |
| `soporte/ContadorDeConsultas.java` | `getPrepareStatementCount()` frente a las otras cifras de `Statistics` |
| `entities/` | `@OneToMany` sin `fetch` y **qué pasaría con `EAGER`** · `LAZY` también en el `@ManyToOne` |
| `application.yml` | `show-sql` **apagado** aquí y encendido en el 04 y el 05 · `generate_statistics` frente a un proxy de DataSource · `open-in-view` · `ddl-auto` |
| `demos` · `V1` · `pom.xml` · `Lab06Application` · `infra/` | los heredados |

### Lab 07 · Concurrencia — **23**

| Archivo | Decisión |
|---|---|
| `repositories/FolioRepository.java` | **`@Lock(PESSIMISTIC_WRITE)` frente a las otras cuatro defensas** (pesimista compartido, optimista con `@Version`, restricción única, secuencia) · `@Query` con `@Param` · un `@Transactional` sobre un método del repositorio |
| `services/EmisorDeFolios.java` | `@Transactional` en el servicio y no en el repositorio · **el candado se pide sobre la fila de apertura, y no sobre otra** |
| `demos/DemosConcurrencia.java` | un pool de plataforma frente a hilos virtuales · un `CountDownLatch` frente a soltar las tareas sin más |
| `entities/Folio.java` | `int` frente a `Integer` |
| `V2__folio_unico_por_anio.sql` | **una restricción `UNIQUE` teniendo ya el candado**: por qué son dos capas y no dos opciones |
| `V1` · `application.yml` · `pom.xml` · `Lab07Application` · `entities/` · `infra/` | los heredados |

---

## 3 · Los ocho casos que la SPEC nombraba

| # | Caso | Dónde quedó |
|---|---|---|
| 1 | `GenerationType` y sus cuatro estrategias | `lab-04/entities/Observacion.java` |
| 2 | `fetch = LAZY`, y por qué la especificación pone `EAGER` en `@ManyToOne` | `lab-05/entities/Tramite.java` |
| 3 | `@Transactional(readOnly = true)` frente a `@Transactional` | `lab-05/demos/DemosRelaciones.java` (y el 06) |
| 4 | `JpaRepository` frente a `CrudRepository` y `PagingAndSortingRepository` | `lab-04/repositories/ObservacionRepository.java` |
| 5 | `mappedBy` y qué pasa si se pone en el lado equivocado | `lab-05/entities/Contribuyente.java` |
| 6 | `nullable` / `length` en `@Column` y **qué valida realmente** | `lab-04/entities/Observacion.java` |
| 7 | el `protected` del constructor sin argumentos | `lab-04/entities/Observacion.java` |
| 8 | `@JoinColumn` y qué ocurre si se omite | `lab-05/entities/Tramite.java` |

**Los ocho, y 94 más.** La SPEC decía que la lista no era cerrada, y no lo era.

El caso que le dio nombre a la SPEC quedó así, en resumen: `IDENTITY` delega el número a la
columna autoincremental y **no puede aplazar el INSERT** —necesita el id de vuelta—, con lo que
**desactiva el batching**; `AUTO` deja la elección a Hibernate y en PostgreSQL 6 significa una
secuencia global; `SEQUENCE` reserva números por adelantado y **es la única que agrupa INSERTs**;
`TABLE` es portátil y la más lenta; `UUID` no es correlativo. Se eligió `IDENTITY` porque el
INSERT que sale en consola no menciona el id —que es la lección del paso 1—, porque la columna ya
es `BIGSERIAL` en Flyway, y porque es lo que el alumno se encuentra. Se cambiaría a `SEQUENCE`
para cargas masivas y a `UUID` para ids generados en varios sitios.

---

## 4 · Verificación

### 4.1 · El código no se movió — medido, no declarado

`instructor/` es una copia de `solucion/` con más documentación, así que hay una comprobación
exacta disponible: **quitar todos los comentarios y las líneas en blanco de los dos y compararlos**.
Se escribió para eso un despojador (`codigo-desnudo.py`, en el scratchpad) que respeta los
literales de cadena, y se corrió **antes de tocar nada** y **al terminar**:

```
ANTES:    Archivos .java comparados: 37 · con divergencia de codigo: 0
DESPUÉS:  Archivos .java comparados: 37 · con divergencia de codigo: 0
```

La misma comprobación se corrió después de **cada archivo editado**, no solo al final.

### 4.2 · Compilan

`instructor/` no es un proyecto —no tiene `mvnw` ni `.mvn`—, así que para compilarlo se montó su
`src/main` y su `pom.xml` sobre una copia de `solucion/`, dentro del repositorio (en `labs/*/.e2e/`,
que el `.gitignore` ya ignora) para que el shim `./mvnw` encontrara `tools/maven/`:

```
lab-04-jpa             BUILD SUCCESS offline · 7 .class
lab-05-relaciones      BUILD SUCCESS offline · 8 .class
lab-06-rendimiento     BUILD SUCCESS offline · 11 .class
lab-07-concurrencia    BUILD SUCCESS offline · 11 .class
```

**37 clases, que son los 37 `.java`.** Con `-o`: cero descargas posibles. Los bancos se borraron
al terminar.

### 4.3 · Los formatos siguen siendo válidos

```
[OK] 4 pom.xml parsean, cero comentarios con doble guion · 4 application.yml parsean
```

Los `application.yml` se comprobaron parseándolos y **leyendo los valores** que los bloques
documentan, no solo mirando que el archivo abriera:

```
[OK] lab-05 yml parsea · open-in-view = False · ddl-auto = validate
[OK] lab-06 yml parsea · show-sql = False · generate_statistics = True
[OK] lab-07 yml parsea · open-in-view = False · ddl-auto = validate
```

### 4.4 · El verificador de la casa

```
$ python3 tools/verificar-instructor.py
Carpetas `instructor/` encontradas: 16
  [OK] labs/lab-04-jpa/instructor  ·  7/7 .java
  [OK] labs/lab-05-relaciones/instructor  ·  8/8 .java
  [OK] labs/lab-06-rendimiento/instructor  ·  11/11 .java
  [OK] labs/lab-07-concurrencia/instructor  ·  11/11 .java
  …
Comprobados: 19 XML · 171 .java · 16/16 carpetas con su documento de entrada
[OK] `instructor/` está al día con `solucion/` y su XML es válido.
```

---

## 5 · Lo que NO se hizo

- **No se tocó `practica/`, `solucion/`, `PASOS.md` ni una línea de código.** Era el alcance de la
  SPEC y se respetó entero; la §4.1 lo mide.
- **No se tocaron los labs 00 a 03 ni 08 a 14, ni `proyecto-final/`.** El criterio nuevo vale para
  ellos igual, pero la SPEC pedía cuatro labs. Queda anotado como candidato en la §7.
- **No se commiteó nada de `instructor/`**, porque no puede: §6.
- **No se añadió un verificador del formato al CI.** Sería el gate decorativo que P-05 castiga:
  la carpeta no existe en el runner. Si el formato hay que vigilarlo, el sitio es
  `tools/verificar-instructor.py`, que corre donde los archivos están — está anotado en la §7.
- **El trabajo en el árbol del PO se dejó como estaba.** `git status` traía cuatro archivos suyos
  en `labs/lab-04-jpa/practica/` —lo que escribió dictando la clase— y no se han tocado ni
  commiteado.

---

## 6 · La consecuencia que hay que tener delante

**Este trabajo existe solo en esta máquina.** `labs/*/instructor/` está en el `.gitignore` de la
raíz por D-031-2, así que las 102 decisiones documentadas **no viajan en ningún commit**: en el PR
de esta SPEC solo van la propia SPEC, este informe y `ESTADO.md`.

Que estén aquí es lo que hacía falta —es esta la máquina desde la que el PO dicta—, pero conviene
decir en voz alta lo que implica:

- **Un clon fresco no las trae.** Si el repositorio se clona en otro sitio, `instructor/` se
  regenera a partir de `solucion/` y estos bloques no estarán.
- **Un `git clean -xdf` se las lleva.** Nada las protege: para git son archivos ignorados.
- Lo mismo valía ya para toda la documentación de `instructor/`; esta SPEC no crea el problema,
  lo hace más caro.

**No se cambió D-031-2 por cuenta propia** —la razón pedagógica sigue en pie: si `instructor/`
viajara, el alumno tendría la chuleta delante y `practica/` sin documentar perdería su motivo—,
pero la decisión de si esto se respalda de alguna forma es del PO. Tres caminos, sin recomendar
ninguno de oficio: dejarlo como está y asumir que se regenera; guardar un respaldo fuera del
repositorio; o versionar `instructor/` en una rama huérfana que el alumno no clona.

---

## 7 · Anotado para después

1. **Los otros once labs y el `proyecto-final`.** El criterio de la SPEC-041 vale igual para
   todos; hoy solo lo cumplen cuatro. Los labs 09 (seguridad) y 10 (resiliencia) son los que más
   lo piden: casi todo lo que hay en ellos pudo ser otra cosa.
2. **Vigilar el formato desde `tools/verificar-instructor.py`.** Hoy nada comprueba que un bloque
   `POR QUÉ ·` traiga sus cuatro partes. Es barato de añadir y corre donde los archivos existen.
3. **El respaldo de `instructor/`**, si el PO decide que hace falta (§6).
