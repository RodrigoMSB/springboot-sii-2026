# INFORME-SPEC-038 · Proyecto final fácil y completo, y retiro de material

**Ejecuta:** mocito · **Rama:** `spec-038-proyecto-final` · **Fecha:** 3 de septiembre de 2026
**Origen:** SPEC-038 del PO.

---

## 0 · Resumen

**Dos cosas: se retira material y el proyecto final deja de ser un examen sorpresa.**

| | |
|---|---|
| **Retirado** | `examen-huecos/`, `lab-12-tareas`, `lab-13-empaquetado` — fuera de `main`, enteros en `material-v1.11.1` |
| **El brief** | **cerrado**: ni un borde abierto. El alumno implementa, no adivina |
| **`ejemplo/`** | **la pieza nueva**: el mismo encargo resuelto sobre otra entidad, pieza por pieza |
| **`base/`** | trae más resuelto: OpenAPI, la métrica ya declarada, consola limpia, test en verde |
| **La rúbrica** | los seis criterios del contrato, con el comando que comprueba cada uno |

**Todo lo medible está medido.** Los tres proyectos en verde con **cero WARNING**, los cuatro curl
dando **401 / 403 / 404 / 200** con el total correcto, Swagger mostrando el endpoint, la métrica
moviéndose y `jib:buildTar` produciendo **286 MB** en los tres.

**36 proyectos compilan offline** (eran 41: salen 6 de los retirados, entra 1 de `ejemplo/`).

**Cuatro puntos de la spec resultaron falsos o incompletos al medir**, corregidos y declarados en
§6:

1. **El «archivo de retirados» no es una carpeta**: es el mecanismo de la SPEC-033 — fuera de
   `main`, recuperable desde el tag.
2. **`labs/README.md` no existía.** Se creó.
3. **Swagger daba 401**: la cadena de seguridad exigía token en todo. Hubo que abrir sus rutas.
4. **Argon2 no; BCrypt sigue** en el proyecto final — y hay una **inconsistencia con el lab 09**
   que dejo señalada para decisión del PO (§6.4).

**El tag.** La cabecera pide `material-v1.12.0`, y **esta vez sí está libre**: es la primera spec
de la serie cuyo número de tag no colisiona.

---

## 1 · Retirar material

### 1.1 · Qué salió

```
examen-huecos/              40 MB
labs/lab-12-tareas/        324 KB
labs/lab-13-empaquetado/   179 MB
docs/guias/fuente/guia-lab-12-tareas.md
docs/guias/fuente/guia-lab-13-empaquetado.md
```

Los dos PDF salieron con sus carpetas.

**No se borró nada del historial.** Recuperables uno a uno:

```bash
git show material-v1.11.1:labs/lab-12-tareas/PASOS.md
git checkout material-v1.11.1 -- labs/lab-13-empaquetado/
git checkout material-v1.11.1 -- examen-huecos/
```

### 1.2 · Lo que se sacó del CI y de los verificadores

| archivo | qué cambió |
|---|---|
| `.github/workflows/material-ci.yml` | el job `labs` recorre `labs proyecto-final`, sin `examen-huecos` |
| `tools/verificar-instructor.py` | ya no busca `examen-huecos/instructor` |
| `tools/instructor-respaldo.sh` | ya no respalda esa carpeta |
| `.gitignore` | fuera las dos reglas de `examen-huecos` |
| `demos-instructor/README.md` | la frase que citaba la lista del job |

### 1.3 · El grep que la spec pedía

Sobre los labs vivos y `proyecto-final/`, buscando `lab-12`, `lab-13`, `examen-huecos`, `lab12.` y
`lab13.`:

```
$ grep -rn "lab-12\|lab-13\|examen-huecos\|lab12\.\|lab13\." \
    labs/lab-0*/ labs/lab-14*/ proyecto-final/ demos-instructor/ tools/ .github/ README.md
   (sin resultados en labs vivos ni en proyecto-final)
```

Lo que apareció estaba en el CI, los verificadores y los README, y está corregido. **Ningún lab
vivo ni `proyecto-final/` referenciaban a los tres retirados**, ni por carpeta ni por propiedad de
yml.

### 1.4 · `labs/README.md`

**No existía** — ver §6.2. Se creó, con la tabla de los catorce labs vivos y la sección que la
spec pedía:

> **Los números 12 y 13 no se reutilizan.** Un lab nuevo tomaría el siguiente libre, para que las
> referencias del historial y de los informes sigan queriendo decir lo que decían.

Y qué conserva el curso de esos temas: el **empaquetado** vive en el proyecto final, que se entrega
con su imagen OCI; de las **tareas programadas** queda el problema de las dos instancias, nombrado
en el cierre del lab 07 al hablar del turno que vive en la base y no en la JVM.

### 1.5 · `tools/jib-base/` se queda

**No se tocó.** Lo usa el proyecto final, que entrega su imagen OCI construida con Jib. Es la misma
razón por la que sobrevivió a la SPEC-033.

---

## 2 · El brief, cerrado

`brief/requerimientos.md` reescrito entero. Se fue la nota del relator que decía *«está
deliberadamente incompleto en los bordes, y esos bordes son parte del examen»*; ahora hay una
especificación sin huecos.

**Lo que quedó fijado, con su tabla en el brief:**

| caso | respuesta |
|---|---|
| Sin token | 401 |
| Token de CONTRIBUYENTE | 403 |
| RUT que no existe | 404 con `{"mensaje": ...}` |
| Falta `desde` o `hasta` | 400 con `{"mensaje": ...}` |
| Contribuyente sin trámites en el período | 200, `tramites: []`, `totalDeclarado: 0` |
| Todo bien | 200 |

**Los campos, exactamente seis en la raíz y cinco por trámite:**

```
rut · razonSocial · desde · hasta · tramites · totalDeclarado
    tramites[]:  id · tipo · estado · fecha · montoDeclarado
```

**`totalDeclarado` suma todos los trámites del período, sin filtrar por estado.** Está dicho con su
razón: la pregunta es cuánto se **declaró**, no cuánto se pagó.

**Y `puntaje_riesgo` salió de la migración**, como pedía la spec: no se puede filtrar lo que no
existe, así que el criterio de la lista blanca deja de poder aprobarse por accidente.

**Sin batch, sin paginación, sin reporte escrito** — dicho explícitamente en un apartado *«lo que
NO hay que hacer»*, porque hacerlo tampoco suma.

---

## 3 · `ejemplo/`, la pieza nueva

`proyecto-final/ejemplo/` **viaja en el repositorio**. Es `base/` más el mismo encargo resuelto
sobre otra entidad, uno a uno:

| El encargo del alumno | El ejemplo |
|---|---|
| `GET /consolidados/{rut}?desde&hasta` | `GET /resumenes/{codigo}?desde&hasta` |
| `ConsolidadoContribuyente`, `TramiteDelConsolidado` | `ResumenOficina`, `TramiteDelResumen` |
| consulta por RUT y fechas | consulta por código de oficina y fechas |
| `ConsolidadoService` | `ResumenService` |
| `ConsolidadoController` | `ResumenController` |
| sólo FISCALIZADOR: 401 y 403 | sólo FISCALIZADOR: 401 y 403 |
| 404 si el RUT no existe | 404 si el código no existe |
| dos tests: servicio y controller | dos tests: servicio y controller |

**Cada archivo lleva dos líneas arriba** —qué hace y cuál es su equivalente— y los comentarios de
dentro explican **por qué**, no qué. Ejemplos de lo que explican: por qué el DTO es un `record`
inmutable y una lista blanca; por qué `BigDecimal` y no `double`; por qué `compareTo` y no `equals`
al comparar importes en un test; por qué el `@RequestParam` sin `required = false` produce el 400
sin escribir un `if`.

**El esquema creció lo mínimo que la spec autorizaba**: una tabla `oficina` con tres filas —la
tercera sin trámites, que es el borde del ejemplo— y una columna `oficina_codigo` en `tramite`.

**Y una diferencia que el ejemplo declara en voz alta**, porque si no sería una trampa: el ejemplo
filtra por `oficinaCodigo`, que es una columna del propio trámite; el encargo filtra por el RUT, que
vive en `Contribuyente`, así que **la consulta del alumno navega la relación** y ahí sí conviene un
`join fetch`. Está dicho en el comentario de su `TramiteRepository` y en el brief.

---

## 4 · Lo que `base/` trae resuelto

| | estado |
|---|---|
| Entidades, migración, datos, login, JWT, cadena | ya estaba |
| Actuator `health` e `info` | ya estaba · **se añadió `metrics`** |
| **OpenAPI** | **nuevo**: `springdoc-openapi-starter-webmvc-ui` 2.8.13, en la maleta y en el pom |
| **Contador de negocio** | **nuevo**: `ContadorDeConsolidados` con `dgt.consolidados.emitidos` |
| Jib | ya estaba |
| **Test de contexto que pasa** | **nuevo**: `ContextoDeSpringTest` |
| **Consola limpia** | **nuevo**: `.mvn/jvm.config` + Surefire con `trimStackTrace` |

**La maleta creció 1,3 MB** con springdoc y sus transitivas (58 entradas nuevas en `repo-maven/`,
casi todas BOM y POM de resolución). Capturadas con el procedimiento de la SPEC-023, con el matiz
que el informe anterior dejó escrito:

```bash
DGT_ONLINE=1 ./mvnw compile -Dmaven.repo.local=<raíz>/repo-maven
```

**El contador está declarado y esperando.** El alumno pone una línea:

```java
    contador.emitidos().increment();
```

---

## 5 · Validación

### 5.1 · Los tres proyectos

```
base                             [INFO] Tests run: 1, Failures: 0, Errors: 0, Skipped: 0
ejemplo                          [INFO] Tests run: 4, Failures: 0, Errors: 0, Skipped: 0
instructor/solucion-referencia   [INFO] Tests run: 4, Failures: 0, Errors: 0, Skipped: 0

WARNINGs:  base 0  ·  ejemplo 0  ·  solucion-referencia 0
```

> **«Cuatro» y no «tres»**, que es lo que decía la spec: son **tres archivos** de test —contexto,
> servicio y controller— y **cuatro métodos**, porque el de servicio tiene dos (el total y el 404).
> Mismo caso que el parametrizado de la SPEC-035.

### 5.2 · Los cuatro curl sobre `solucion-referencia/`

```
1 · sin token                401
2 · token CONTRIBUYENTE      403
3 · RUT que no existe        {"mensaje":"No existe el contribuyente 99.999.999-9"} [404]
4 · el caso bueno            rut=76.111.111-1  razonSocial=Comercial Andes Ltda.
                             trámites=4  totalDeclarado=6330000.0
```

**El total, contra los datos sembrados:**

```
1.200.000 + 950.000 + 3.400.000 + 780.000 = 6.330.000
```

Los 3.400.000 son de un trámite **PENDIENTE** y suman igual — que es el criterio del brief. La
rúbrica usa ese número para cazar al alumno que filtre por estado: le saldría 2.930.000.

**Y los campos, exactamente los que el brief fija:**

```
campos raíz:          ['desde', 'hasta', 'razonSocial', 'rut', 'totalDeclarado', 'tramites']
campos de un trámite: ['estado', 'fecha', 'id', 'montoDeclarado', 'tipo']
```

### 5.3 · Los dos bordes

```
sin trámites   {"rut":"78.333.333-3","razonSocial":"Inversiones Atacama Ltda.",
                "desde":"2026-01-01","hasta":"2026-12-31","tramites":[],"totalDeclarado":0} [200]

falta `hasta`  {"mensaje":"Faltan `desde` y `hasta`, o no tienen formato YYYY-MM-DD"} [400]
```

### 5.4 · Swagger, la métrica y Jib

```
/swagger-ui.html   200
endpoints          ['/auth/login', '/consolidados/{rut}']     ← en solucion-referencia
                   ['/auth/login', '/resumenes/{codigo}']     ← en ejemplo

dgt.consolidados.emitidos = 2.0    (solucion-referencia, tras dos peticiones)
dgt.consolidados.emitidos = 3.0    (ejemplo, tras tres)

jib:buildTar       base 286M  ·  ejemplo 286M  ·  solucion-referencia 286M
```

**El endpoint aparece en Swagger sin una sola anotación de OpenAPI** en el controller, que es lo que
la spec pedía.

### 5.5 · El CI local

```
[INFO] 36 proyectos · 0 fallos            (41 antes: −6 retirados, +1 ejemplo)

verificar-temario.py            VEREDICTO: las 5 verificaciones PASAN
verificar-pasos-copiables.py    [OK] 14 guion(es) verificado(s)
verificar-guion-vs-practica.py  [OK] Todo lo que los guiones prometen es verdad
verificar-instructor.py         [OK] 18 XML · 171 .java · 15/15 carpetas
verificar-demo-docker.py        [OK] la demostración dice el mismo código que el laboratorio
generar-guias.py --verificar    [OK] 78 bloque(s) · 0 líneas que la solución no tenga
```

---

## 6 · Los puntos de la spec que resultaron falsos, y qué se hizo

### 6.1 · El «archivo de retirados» no es una carpeta

La spec dice: *«Mover al archivo de retirados, donde está el lab 14 antiguo.»*

**No existe ninguna carpeta de archivo.** Se comprobó: no hay `archivo/`, ni `retirados/`, ni
`docs/archivo/`. Lo que la SPEC-033 hizo con el arco antiguo —y que `ESTADO.md` §1.a documenta— fue
**sacarlo de `main` y dejarlo en el historial y en los tags**:

```bash
git show material-v0.8.0:labs/lab-13-capsula-y-egreso/README.md
```

Se aplicó **el mismo mecanismo**, que cumple el «no se borra nada» de la spec: nada desaparece del
historial, y `ESTADO.md` §1.a y `labs/README.md` dicen cómo recuperarlo con el tag correcto
(`material-v1.11.1`).

### 6.2 · `labs/README.md` no existía

La spec pide *«una línea en `labs/README.md`»*. Ese archivo **no existía**: la tabla de labs vivía
sólo en el README de la raíz.

Se **creó**, con la tabla de los catorce labs y la sección de los números retirados. Y se quitaron
las dos filas del README de la raíz.

### 6.3 · Swagger daba 401

La spec pide *«`/swagger-ui.html` funcionando»*. Con la dependencia puesta y nada más, **daba 401**:
la cadena de `base/` exige token en todo salvo `/auth/login` y `/actuator/health`.

```
  /swagger-ui.html      -> 401
  /v3/api-docs          -> 401
```

Se abrieron sus tres rutas en `SeguridadConfig`, con el motivo escrito en el código: la
documentación describe **qué** endpoints hay, no devuelve un dato, y es la primera pantalla que
alguien abre para entender el servicio. Después:

```
  /swagger-ui.html -> 200
  endpoints:  ['/auth/login']
```

### 6.4 · BCrypt frente a Argon2 · **decisión pendiente del PO**

La spec **no menciona** el codificador de contraseñas del proyecto final, y **no se tocó**: sigue en
`BCryptPasswordEncoder`, con los hashes BCrypt sembrados en la migración.

**Pero el lab 09 pasó a Argon2id con la SPEC-036**, hace dos specs. Así que hoy el curso enseña
Argon2 y su proyecto final usa BCrypt.

**No lo cambié por mi cuenta** porque tocar el codificador obliga a regenerar los hashes sembrados,
a ampliar `clave_hash` de 60 a 120 y a declarar BouncyCastle en tres poms más — y nada de eso está
en el alcance de esta spec. **Es una inconsistencia real del material**, y la decisión es del PO:

- **Dejarlo en BCrypt** y decir en el brief que el proyecto final usa el codificador anterior a
  propósito, porque la seguridad no es lo que se evalúa ahí.
- **Alinearlo con Argon2**, que son los cuatro cambios de arriba en `base/`, `ejemplo/` y
  `solucion-referencia/`. Media hora, y lo puedo hacer en cuanto se decida.

### 6.5 · Dos cosas menores que la spec no anticipaba

- **La ruta de `jib.baseImageCache`.** `solucion-referencia/` está un nivel más abajo que `base/`,
  así que su `.mvn/maven.config` necesita `../../../tools/jib-base` y no `../../`. Con la ruta
  heredada, `jib:buildTar` fallaba con *«Cannot run Jib in offline mode»*. Corregido.
- **El test de controller y la cadena de seguridad.** El `@WebMvcTest` intentaba montar la cadena
  entera, que necesita el `JwtDecoder`. Se resolvió con `@AutoConfigureMockMvc(addFilters = false)`,
  y está explicado en el comentario del ejemplo: lo que ese test prueba es el 404 y su cuerpo, no la
  seguridad — la seguridad se comprueba con los curl de la rúbrica. Es el criterio del Lab 08: cada
  test al nivel más barato que responda su pregunta.

---

## 7 · Lo que queda para el PO

1. **Fijar el plazo de entrega** del proyecto final. Es lo único que el material deja en blanco a
   propósito: el README dice «el plazo lo fija el relator».
2. **Decidir sobre BCrypt / Argon2** en el proyecto final (§6.4).
3. **`docs/temario/MAPA-LAB-MODULO.md` sigue citando lab-12 y lab-13** en sus filas de módulos. No
   se tocó: la spec limitaba el grep a «ningún lab vivo ni `proyecto-final/`», y ese mapa refleja el
   **contrato adjudicado**, cuyos módulos no cambian porque el material se reordene. Si el PO quiere
   que refleje lo que hoy se dicta, es un cambio aparte y hay que revisar que
   `verificar-temario.py` siga verde.

## 8 · El tag

La cabecera pide **`material-v1.12.0`**, y **por primera vez en esta serie el número está libre**:
no colisiona con la serie histórica v0.x. Se cierra con ése.
