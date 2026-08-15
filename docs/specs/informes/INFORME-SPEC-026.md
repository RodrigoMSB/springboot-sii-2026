# INFORME-SPEC-026 · Lab 3.5 «Guardar y recuperar» — la persistencia desde cero

**SPEC:** SPEC-026 · **Ejecuta:** mocito · **Fecha:** 15 de agosto de 2026
**Rama:** `spec-026-lab-3-5-el-apostrofe` · desde `main` (`b9bd2e4`, `material-v0.4.0`)
**Máquina:** Mac Studio del PO — Darwin 25.5.0, `arm64`

---

## 1 · Veredicto en una línea

**EL LAB QUE FALTABA EXISTE, Y NO ROMPIÓ A NADIE** — el alumno toma una tabla vacía, escribe la
entidad, el repositorio y el servicio que la conectan, y ve el SQL que Hibernate generó por él;
la solución está verde, el starter falla solo en sus tres compromisos, el Lab 04 sigue verde tras
re-derivarse, y la cadena está en sincronía con el único rojo de siempre en la frontera 07→08.

---

## 2 · El cambio de marco, y qué implicó

Este lab se construyó dos veces. La primera versión era **«El apóstrofe»**: un DAO de JDBC crudo
heredado, con una inyección SQL adentro, que el alumno mataba. El PO reescribió el README y con
él cambió la naturaleza del lab: **«Guardar y recuperar»**, constructivo, sin crimen.

No es un retoque de texto. Cambia qué hace el alumno durante tres horas: ya no arregla el
desastre de otro, sino que escribe desde cero algo que no existía. El orden pedagógico también se
invierte — **primero se ve el destino** (la demo sobre la solución) y después se construye.

### Qué salió

| | |
|---|---|
| `ReporteInternoLegacyDao` | el DAO heredado con sus cuatro pecados |
| `91-demo-inyeccion.sh` | la demo del apóstrofe |
| `ReglasDelApostrofe` + AU-03b + fixture + `E4_GuardianJdbcTest` | el guardián y su mordida |
| La semilla de `V3` | la tabla nace **vacía**: llenarla es el ejercicio |
| En el Lab 04: el guardián, su fixture y `GuardianJdbcTest` | solo existían para sostener aquel TODO_4 |

### Qué entró

| | |
|---|---|
| **Tres TODOs** en vez de cuatro | entidad · repositorio que guarda y busca · servicio conectado |
| `POST` en el endpoint | el `GET` ya estaba; ahora se puede guardar por HTTP |
| `bin/91-demo-jpa.sh` | guarda un objeto, lo recupera, y **cita el SQL** que Hibernate generó |

### El directorio se renombró

`labs/lab-03b-el-apostrofe/` → **`labs/lab-03b-guardar-y-recuperar/`**. Dejar «el apóstrofe» en
la ruta de un lab que ya no trata de eso habría sido una mentira permanente, y el directorio
tenía dos días y ningún consumidor fuera de esta rama.

**La rama conserva el nombre viejo** (`spec-026-lab-3-5-el-apostrofe`) porque su PR ya existe y
renombrarla obligaría a abrir otro. Es la única incoherencia que queda, y es deliberada.

---

## 3 · El lab

### Lo que el alumno hace

| | Qué | Dónde | Lo verifica |
|---|---|---|---|
| **TODO_1** | `@Entity` + `@Id` + `@Column` + `@ManyToOne` LAZY | `domain/entity/ObservacionInterna.java` | `E1_EntidadMapeadaIT` |
| **TODO_2** | `extends JpaRepository` + `findByContribuyenteRut` | `infrastructure/repository/ObservacionInternaRepository.java` | `E2_GuardarYRecuperarIT` |
| **TODO_3** | Conectar el servicio, y mirar el SQL | `application/ObservacionInternaService.java` | `E3_ServicioConectadoIT` |

Los tests miden lo que se puede medir sin ambigüedad: E1 le pregunta a Hibernate **en su
metamodelo** si conoce la clase, y exige aparte que la relación sea LAZY; E2 exige que `save`
devuelva el `id` que generó el motor y que la búsqueda traiga lo del contribuyente pedido y no lo
del vecino; E3 exige el viaje completo por el servicio.

### La demo

```
POST /api/internal/observaciones      -> 201
GET  /api/internal/observaciones?rut= -> el mismo objeto de vuelta

── 3 · Y este es el SQL que tú no escribiste ────────────────
     select c1_0.id, c1_0.puntaje_riesgo_interno, c1_0.razon_social, c1_0.rut
     from contribuyente c1_0
     where c1_0.rut=?
     insert into observacion_interna (autor, contribuyente_id, creada_en, texto)
     values (?, ?, ?, ?)
```

Los nombres de columna salen de la entidad del alumno y los `?` son los parámetros. Es la lámina
completa de lo que enseña el lab, y sale de un script.

Sobre el `starter`, la misma demo responde **HTTP 500** con el aviso de que es lo esperado hasta
completar los TODOs, y dónde mirar.

---

## 4 · Tres cosas que costaron

### 4.1 · El starter no compilaba

Los tests llaman a `save`, que lo aporta `JpaRepository` — y en el starter la interfaz todavía no
lo extiende. La suite entera fallaba a compilar, no solo el enunciado.

Se resolvió declarando `save` en la interfaz del starter **con la misma firma genérica** que
`JpaRepository`:

```java
<S extends ObservacionInterna> S save(S observacion);
```

Al extender, esa firma se hereda idéntica: el alumno puede borrar la línea o dejarla, y en
ninguno de los dos casos hay choque de nombres. Si se hubiera declarado
`ObservacionInterna save(ObservacionInterna)` —lo natural— el alumno se habría encontrado un
error de erasure incomprensible al extender.

### 4.2 · Los tests dependían del orden

`E2` y `E3` comparten anotaciones, y por tanto **el mismo contexto de Spring y la misma base**.
Las aserciones absolutas (`hasSize(2)`, `containsExactly`, `isEmpty`) se contaminaban entre
clases según cuál corriera primero.

Se hicieron relativas —`contains`, `doesNotContain`— sin perder la lección: que la consulta trae
lo del contribuyente que se pide **y no lo del vecino** se sigue afirmando, ahora de una forma
que no depende del orden. Comprobado corriendo la solución dos veces seguidas: `BUILD SUCCESS`
las dos.

### 4.3 · El validador apuntaba a un patrón vacío

`90-validar.sh` buscaba los compromisos con `-Dtest='**/enunciado/*Test.java'`. Este lab ya no
tiene ningún test **unitario** en el enunciado: los tres son de integración. El patrón no casaba
con nada y surefire fallaba —correctamente— con «no tests were executed», lo que dejaba la
solución en `NO APROBADO` con la suite verde.

Se apagó surefire en ese paso a propósito y **failsafe conserva su guard**: si
`**/enunciado/*IT.java` dejara de casar algún día, grita en vez de pasar en verde con cero tests
corridos. El porqué está escrito en el propio script.

---

## 5 · Verificación

Todo corrió **offline** (`./mvnw` es offline por defecto): `Downloading from: 0`.

### El starter falla SOLO en el enunciado

```
E1_EntidadMapeadaIT.laEntidadEstaMapeada   [Hibernate no conoce esta clase: todavía no es una @Entity (TODO_1)]
E1_EntidadMapeadaIT.laRelacionEsLazy       [falta el @ManyToOne hacia Contribuyente (TODO_1)]
E2_GuardarYRecuperarIT (x2)                [no hay ningún bean de ObservacionInternaRepository (TODO_2)]
E3_ServicioConectadoIT (x2)                » UnsupportedOperation {{TODO_3}}
```

Seis fallos, los seis en `enunciado/`. Los 40 tests unitarios y el resto de los IT, verdes.

### La solución, verde

```
Tests run: 40, Failures: 0, Errors: 0   (unitarios)
Tests run: 13, Failures: 0, Errors: 0   (integración)
BUILD SUCCESS
```

### Ciclo y validadores

```
90-validar   solucion -> LAB 3.5 APROBADO      (5/5 verificaciones)
             starter  -> LAB 3.5 NO APROBADO   (4/5 verificaciones)

99-destruir  3/3 verificaciones · Todo quedó como estaba
huérfanos:   postgres embebidos: 0   ·   LISTEN 8099: 0
```

### El Lab 04, tras la re-derivación

```
lab-04/solucion  ->  Tests run: 15, Failures: 0, Errors: 0
lab-04/starter   ->  falla en E1, E2, E3, E4 y E5 — sus propios TODOs, y nada más
```

Recibió los seis archivos del 3.5 (la migración, la entidad, el repositorio, la vista, el
servicio y el controlador). El trabajo del alumno viaja hacia adelante.

### Derivación y manifiestos

**28 eslabones en sincronía.** El único rojo es la frontera **07→08**, la de siempre —el Lab 08
no está migrado— y la SPEC la deja «como esté».

```
lab-03b-guardar-y-recuperar   4 archivos en el manifiesto
lab-04-el-arbol-de-tramites   6 archivos en el manifiesto
```

### El piloto de empaquetado

```
$ find labs/lab-03b-guardar-y-recuperar -maxdepth 1 -name '*.md'
  ./PARA-EL-SABADO.md
  ./README.md
```

**Exactamente 2**, y el `README.md` es el texto del PO, palabra por palabra. Las notas del
instructor viven fuera, en `docs/instructor/lab-03b.md`.

### Transversales

| | |
|---|---|
| Guard de 95 MB | `[OK] Ningún archivo supera los 95 MB.` |
| `repo-maven/` | **230 MB, sin cambios** — el lab no añade ni una dependencia |
| shellcheck · `bash -n` | limpios |
| Labs 05–13 y 14 | **cero archivos tocados** |
| CI | 7 de 8; el único rojo es `deriva`, el mismo que `main` ya tenía |

---

## 6 · La decisión de ingeniería que sigue en pie

El Lab 3.5 **no modifica ni un solo archivo compartido** con su base, y eso no es casualidad:
`ReglasDeLaCasa`, `ArquitecturaTest` y los dos `application*.yml` son byte a byte idénticos en
los labs 03, 04 y 05, y ni el 05 ni el 06 los declaran. Tocar cualquiera obligaba a propagar
hasta el 13 — o a romper la cadena en 04→05, con los labs 05–13 prohibidos.

De ahí que el log de SQL se encienda con `./bin/start-lab.sh --ver-sql` y no editando
`application-dev.yml`. Quedó mejor de lo que la SPEC pedía: el log ruidoso dura lo que dura ese
arranque y no se queda encendido para los labs siguientes.

**Y con el cambio de marco esta restricción dejó de costar nada**, porque el guardián AU-03b
—que era la otra pieza que la chocaba— ya no existe.

---

## 7 · Lo que queda

1. **La PPT del Lab 3.5** la hace el Arquitecto. El índice que necesita está desarrollado en
   `PARA-EL-SABADO.md`.
2. **La prueba de aceptación del PO**: la demo sobre la solución, los tres TODOs, `90-validar`.
3. **El material de inyección SQL retirado.** Sobrevive **una página** en `PARA-EL-SABADO.md`
   §5 —el JDBC crudo, la concatenación y sus cuatro problemas— como *valoración*, no como
   ejercicio; y las notas del instructor explican por qué está fuera de la sesión. El DAO
   ejecutable, la demo del golpe y el guardián AU-03b **no sobreviven en ninguna parte**: si el
   PO los quiere para otro lab, están en el historial de esta rama.
4. **El reempaquetado de los demás labs**, si el piloto convence.
5. **La renumeración**, si el PO la quiere: hoy el directorio es `lab-03b-guardar-y-recuperar` y
   se muestra como «Lab 3.5».
6. **SPEC-025 / PR #31** sigue esperando firma; nada de esta SPEC depende de ella.

---

## 8 · Cierre

Un laboratorio nuevo, dos proyectos, un vecino que no se rompió, y un alumno que llega al Lab 04
sabiendo qué es una entidad, por qué su relación dice LAZY, y cómo mirar el SQL que no escribió.

En posición de merge cuando el PO lo autorice. PR en draft.
