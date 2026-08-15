# INFORME-SPEC-026 · Lab 3.5 «El apóstrofe» — la persistencia desde cero

**SPEC:** SPEC-026 · **Ejecuta:** mocito · **Fecha:** 15 de agosto de 2026
**Rama:** `spec-026-lab-3-5-el-apostrofe` · desde `main` (`b9bd2e4`, `material-v0.4.0`)
**Máquina:** Mac Studio del PO — Darwin 25.5.0, `arm64`

---

## 1 · Veredicto en una línea

**EL LAB QUE FALTABA EXISTE, Y NO ROMPIÓ A NADIE** — el alumno vive la inyección SQL en un DAO
heredado que funciona, la mata con una entidad y un repositorio, e instala el guardián que
prohíbe volver atrás; las ocho verificaciones pasaron, el Lab 04 sigue verde tras re-derivarse, y
la cadena está en sincronía del tronco al Lab 07 con el único rojo de siempre en la frontera
07→08.

---

## 2 · Precondición · el estado de la SPEC-025

**La contradicción del vuelo 4 está CERRADA**, no abierta: se resolvió antes de empezar esta
SPEC. No era un defecto del material sino del arnés de medición — el `timeout` de esta máquina es
el binario x86_64 del Homebrew de Intel, corre bajo Rosetta y hace que `uname -m` devuelva
`x86_64`, con lo cual el shim creía estar en un Mac Intel y se caía al Java del sistema (su
comportamiento diseñado). Detalle en §7 del `INFORME-SPEC-025.md`.

Como no queda nada abierto, **no hubo que estacionar nada**. La rama `spec-025-fase-2-labs-08-11`
sigue intacta en `9d3bf12` y su **PR #31 continúa OPEN en draft**, esperando la firma del PO.
Esta SPEC salió de `main` (v0.4.0), que no depende de ella.

---

## 3 · El lab

### El crimen (D-026-3)

`starter/src/main/java/cl/dgt/tramites/infrastructure/legacy/ReporteInternoLegacyDao.java`, 40
líneas de JDBC crudo con los cuatro pecados marcados en el propio archivo:

```java
String sql = "SELECT c.rut, o.texto, o.autor, o.creada_en "
           + "FROM observacion_interna o "
           + "JOIN contribuyente c ON c.id = o.contribuyente_id "
           + "WHERE c.rut = '" + rut + "'";        // PECADO 1
```

…más el mapeo por número de columna (2), los `close()` que solo corren en el camino feliz (3), y
el `catch (SQLException e) {}` que convierte cualquier error en una lista vacía (4).

La tabla `observacion_interna` llega en una migración nueva (`V3`) **sin entidad JPA**: mapearla
es el trabajo del alumno. La semilla deja observaciones de **dos** contribuyentes a propósito —
sin eso, filtrar la tabla entera se vería igual que filtrar lo tuyo.

### Los cuatro TODOs (D-026-5)

| | Qué | Dónde | Lo verifica |
|---|---|---|---|
| **TODO_1** | `@Entity` + `@Id` + `@Column` + `@ManyToOne` LAZY | `domain/entity/ObservacionInterna.java` | `E1_EntidadMapeadaIT` |
| **TODO_2** | `extends JpaRepository` + `findByContribuyenteRut` | `infrastructure/repository/ObservacionInternaRepository.java` | `E2_ConsultaDerivadaIT` |
| **TODO_3** | Migrar el servicio al repositorio, y mirar el SQL | `application/ObservacionInternaService.java` | `E3_EndpointMigradoIT` |
| **TODO_4** | Enterrar el DAO e instalar AU-03b | `test/arquitectura/ReglasDelApostrofe.java` | `E4_GuardianJdbcTest` |

El test de la comilla se llama, como pedía la SPEC, `el_apostrofe_ya_no_es_codigo`.

---

## 4 · Dos decisiones que la cadena de derivación obligó

Son desviaciones respecto de la letra de la SPEC, ambas para poder cumplir V6 y V7 sin tocar los
labs 05–13. Se declaran porque cambian dónde vive el código, no qué enseña.

**Primero, el hecho que las fuerza.** Se midió antes de diseñar: `ReglasDeLaCasa.java`,
`ArquitecturaTest.java`, `application.yml` y `application-dev.yml` son **byte a byte idénticos en
los labs 03, 04 y 05**, y ni el 05 ni el 06 los declaran en su allowlist. Cambiar cualquiera de
ellos en el Lab 3.5 obliga a propagarlo hasta el 13 — o a romper la cadena en 04→05.

### 4.1 · AU-03b nace en su propio archivo

La SPEC pedía «instalar la regla ArchUnit AU-03b». Vive en
`arquitectura/ReglasDelApostrofe.java`, **no** dentro de `ReglasDeLaCasa`. Como archivo nuevo
viaja hacia adelante sin romper nada; dentro de `ReglasDeLaCasa` habría exigido tocar del 05 al 13.

Sigue el patrón de la casa para guardianes que instala el alumno (el mismo del Lab 02): en el
`starter` es un cascarón tautológico con su TODO, y el test exige las dos mitades —que la regla
pase sobre el código y que **muerda** a su fixture—.

**Consecuencia declarada:** AU-03b viaja hasta el Lab 04 y ahí se detiene; del 05 en adelante el
archivo todavía no existe. Integrarlo con las otras siete es trabajo de la SPEC de reempaquetado,
que ya va a pasar por esos labs. Está escrito en las notas del instructor.

### 4.2 · El log de SQL se enciende con un flag, no editando el `.yml`

La SPEC decía «propiedad ya preparada, comentada». `application-dev.yml` es uno de los cuatro
archivos idénticos de arriba, así que se resolvió con `./bin/start-lab.sh --ver-sql`, que pasa
`spring.jpa.show-sql`, `format_sql` y el nivel de log de Hibernate como argumentos de ese
arranque.

Quedó mejor de lo que pedía la letra: el log ruidoso **dura lo que dura ese arranque** y no se
queda encendido para los labs siguientes. El alumno hace el mismo gesto —activar algo y mirar el
resultado— y el README y el TODO_3 lo dicen igual.

**Efecto neto:** el Lab 3.5 no modifica **ni un solo archivo compartido** con su base. Su
`derivacion-solucion.txt` no declara ninguna divergencia porque no hay ninguna: todo lo suyo son
adiciones.

---

## 5 · Verificación · V1 a V8

Todo lo de abajo corrió **offline** (`./mvnw` es offline por defecto): `Downloading from: 0`.

### V1 · el starter falla SOLO en el enunciado

Con `-Dmaven.test.failure.ignore=true` para ver las dos fases —el fallo de E4 en surefire, si no,
corta failsafe antes de que corran los IT—:

```
Tests run:  2, Failures: 0   cl.dgt.tramites.web.ContribuyenteControllerTest
Tests run:  2, Failures: 0   cl.dgt.tramites.servicio.TramiteServiceTest
Tests run:  8, Failures: 0   cl.dgt.tramites.arquitectura.ArquitecturaTest
Tests run:  7, Failures: 0   cl.dgt.tramites.arquitectura.MordidaDeLosGuardianesTest
Tests run:  3, Failures: 2   TODO_4 · AU-03b
Tests run:  3, Failures: 0   cl.dgt.tramites.dominio.Formulario29TotalTest
Tests run: 18, Failures: 0   cl.dgt.tramites.dominio.MaquinaDeEstadosTest
Tests run:  3, Failures: 0   cl.dgt.tramites.web.ContratoRn03IT
Tests run:  2, Failures: 2   TODO_2 · findByContribuyenteRut
Tests run:  2, Failures: 1   TODO_3 · el endpoint usa el repositorio
Tests run:  2, Failures: 2   TODO_1 · ObservacionInterna está mapeada
Tests run:  4, Failures: 0   cl.dgt.tramites.dominio.SemillaCoherenteIT
```

Siete fallos, los siete en `enunciado/`. Y los mensajes dicen qué falta, no qué falló:

```
E1_EntidadMapeadaIT.laEntidadEstaMapeada  [Hibernate no conoce esta clase: todavía no es una @Entity (TODO_1)]
E1_EntidadMapeadaIT.laRelacionEsLazy      [falta el @ManyToOne hacia Contribuyente (TODO_1)]
E2_ConsultaDerivadaIT (x2)                [no hay ningún bean de ObservacionInternaRepository: la interfaz todavía no extiende JpaRepository (TODO_2)]
E4_GuardianJdbcTest.elMuertoEstaEnterrado [ReporteInternoLegacyDao sigue en el proyecto (TODO_4)]
```

El único test del enunciado que pasa en el starter es `E3.elCaminoHonestoNoCambia`, y **debe**
pasar: el DAO heredado funciona en el camino feliz. Ese es el punto del lab.

### V2 · la solución, verde

```
Tests run: 43, Failures: 0, Errors: 0   (unitarios)
Tests run: 13, Failures: 0, Errors: 0   (integración)
BUILD SUCCESS
```

AU-03b mordiendo, citado desde `E4_GuardianJdbcTest`: la regla pasa sobre producción y falla
sobre el fixture `AU03B_ClaseQueHablaJdbcCrudo` — las dos mitades.

### V3 · la demo sobre el starter — el golpe

```
GET /api/internal/observaciones?rut=11111111-1
  [{"rutContribuyente":"11111111-1","texto":"Presenta sus declaraciones dentro de plazo…"},
   {"rutContribuyente":"11111111-1","texto":"Solicitó certificado de situación tributaria…"}]
  -> 2 observación(es). Todas de Valentina Rojas.

GET /api/internal/observaciones?rut=11111111-1' OR '1'='1
  [{"rutContribuyente":"11111111-1", …},
   {"rutContribuyente":"11111111-1", …},
   {"rutContribuyente":"12345678-5","texto":"Representante legal no ubicable…"},
   {"rutContribuyente":"12345678-5","texto":"Fiscalización en curso. NO divulgar fuera del área."},
   {"rutContribuyente":"12345678-5","texto":"Diferencias reiteradas entre F29 declarado y pagado."}]
  -> 5 observación(es).

[WARN]  La consulta maliciosa devolvió 5 en vez de 2
```

Filtró tres observaciones de otro contribuyente, incluida una fiscalización en curso marcada
como no divulgable. Solo lectura, como manda la SPEC: nada de `DROP`.

### V4 · la misma demo sobre la solución

```
GET /api/internal/observaciones?rut=11111111-1' OR '1'='1
  []
  -> 0 observación(es).
[OK]  La consulta maliciosa devolvió 0 — el apóstrofe dejó de ser código.
```

### V5 · ciclo completo, ambos estados

```
start-lab   [OK] La DGT está viva en el puerto 8099
99-destruir [OK] API detenida (PID 12643)
            [OK] PostgreSQL embebido detenido: no quedó ningún proceso
            3/3 verificaciones · Todo quedó como estaba

huérfanos (por ruta):  postgres embebidos: 0   LISTEN 8099: 0

90-validar  solucion -> LAB 3.5 APROBADO      (5/5 verificaciones)
            starter  -> LAB 3.5 NO APROBADO   (4/5 verificaciones)
```

El validador se adaptó: en este lab tres de los cuatro compromisos son de **integración**, así que
corre `verify` apuntando a los dos plugins (`-Dtest` y `-Dit.test`) en vez de solo `test`.

### V6 · el Lab 04 tras la re-derivación

```
lab-04/solucion  ->  Tests run: 15, Failures: 0, Errors: 0   BUILD SUCCESS
lab-04/starter   ->  falla en E1_RelacionesLazyIT, E2_ConsultasDerivadasIT,
                     E3_JpqlMultiEntidadIT, E4_ReporteJdbcIT, E5_AU04InstaladaTest
                     — sus propios TODOs, y nada más
```

El Lab 04 recibió ocho archivos del 3.5 (la migración, la entidad, el repositorio, la vista, el
servicio, el controlador, la regla y su fixture) más un `GuardianJdbcTest` propio, para que AU-03b
**siga corriendo** allí: un guardián que deja de correr al lab siguiente no era un guardián.

Y ese lab es justo donde conviene comprobarlo, porque escribe SQL a propósito: su `ReporteService`
usa `JdbcClient`, y AU-03b lo deja pasar —es SQL con parámetros y recursos gestionados— mientras
sigue cazando el JDBC a pelo.

### V7 · derivación y manifiestos

**17 eslabones en sincronía**, del tronco al Lab 07, incluidos los dos nuevos
(`lab-03/solucion → lab-03b/solucion` y `lab-03b/solucion → lab-03b/starter`) y los dos del Lab 04
re-derivado. El único rojo es la frontera **07→08**, la de siempre —el Lab 08 no está migrado— y
la SPEC la deja «como esté».

Manifiestos regenerados y verificados en los cuatro proyectos:

```
lab-03b-el-apostrofe          5 archivos    solucion: [OK]   starter: [OK]
lab-04-el-arbol-de-tramites   6 archivos    solucion: [OK]   starter: [OK]
```

### V8 · el piloto de empaquetado

```
$ find labs/lab-03b-el-apostrofe -maxdepth 1 -name '*.md'
  ./PARA-EL-SABADO.md
  ./README.md
```

**Exactamente 2.** No hay `guia/`, ni `TESTS.md`, ni `INSTRUCTOR.md`, ni `TEORIA.md`. Las notas
del instructor viven fuera, en `docs/instructor/lab-03b.md`.

Para ser exactos: hay otros cuatro `.md` más adentro —`docs/clave-de-laboratorio.md` y
`README-FIXTURES.md`, uno de cada por proyecto— que **se heredan del tronco** y existen igual en
todos los labs. No son documentación de este lab.

### Transversales

| | |
|---|---|
| Guard de 95 MB | `[OK] Ningún archivo supera los 95 MB.` |
| `repo-maven/` | **230 MB, sin cambios** — este lab no añade ni una dependencia |
| Descargas intentadas | **0** |
| shellcheck · `bash -n` | limpios en los scripts nuevos y adaptados |
| Labs 05–13 y 14 | **cero archivos tocados** |

### El CI, medido sobre la rama

**7 de 8 jobs en verde.** El único rojo es `deriva`, y es el **mismo que `main` ya tenía**: la
frontera 07→08, porque el Lab 08 no está migrado. Esta SPEC no lo empeora ni lo toca.

Un job sí se puso rojo por culpa nuestra y se corrigió: **`siembra`**. La regla P-18 exige que
todo lab con sucesor tenga un `TEORIA.md` que siembre el módulo siguiente… y el empaquetado
piloto prohíbe precisamente ese archivo, porque su rol lo cumple `PARA-EL-SABADO.md`.

```
[ERROR] labs/lab-03b-el-apostrofe no tiene TEORIA.md
[ERROR] 1 lab(s) sin siembra. Ver P-18 en docs/adn/adn-cypress.md.
```

Se resolvió **enseñándole el nombre nuevo al gate**, no debilitándolo ni exceptuando al lab: si
no hay `TEORIA.md`, mira `PARA-EL-SABADO.md`, y le exige exactamente lo mismo. P-18 habla de
sembrar el módulo siguiente, no del nombre del archivo. Es una decisión que el reempaquetado va a
heredar, así que conviene que conste aquí.

De paso, la siembra del Lab 04 pasó a ser **explícita**, con su propio subtítulo en §9 de
`PARA-EL-SABADO.md`: la pregunta «¿y si trae de más?» y el enganche con el `LAZY` que el alumno
acaba de escribir sin saber todavía por qué importa.

---

## 6 · Un detalle que costó, y que conviene recordar

`E3_EndpointMigradoIT` **pasaba en el starter**, y no debía: la inyección tenía que filtrar. Lo
delató una discrepancia — el `curl` de la demo sí filtraba y el test no.

La causa: el RUT malicioso se construía con `UriComponentsBuilder…build().encode()` y el cliente
volvía a codificar la cadena, así que el `%20` se convertía en `%25 20` y al servidor le llegaba
otro texto. La consulta no encontraba nada y el test pasaba **por el motivo equivocado**, que es
la peor forma de pasar.

Ahora el RUT viaja como variable de plantilla (`?rut={rut}`) y lo codifica el cliente una sola
vez, igual que un navegador. Queda anotado en el propio test, porque el próximo que escriba un
test de inyección va a tropezar con lo mismo.

---

## 7 · Lo que queda

1. **La PPT del Lab 3.5** la hace el Arquitecto, después de que el PO apruebe el lab. El índice
   que la teoría necesita está desarrollado en `PARA-EL-SABADO.md`, en las diez secciones que la
   SPEC fijó.
2. **La prueba de aceptación del PO** sobre este lab: la demo, los cuatro TODOs y `90-validar`.
3. **AU-03b del Lab 05 en adelante** (§4.1), para la SPEC de reempaquetado.
4. **El reempaquetado de los demás labs**, si el piloto convence.
5. **La renumeración**, si el PO la quiere: hoy el lab es `lab-03b-el-apostrofe` y se muestra como
   «Lab 3.5». Renombrar del 04 al 13 es SPEC aparte.
6. **SPEC-025 / PR #31** sigue esperando firma; nada de esta SPEC depende de ella.

---

## 8 · Cierre

Un laboratorio nuevo, dos proyectos, ocho verificaciones y un vecino que no se rompió. El alumno
llega al Lab 04 sabiendo qué es una entidad, por qué su relación dice LAZY, y cómo mirar el SQL
que no escribió.

En posición de merge cuando el PO lo autorice. PR en draft.
