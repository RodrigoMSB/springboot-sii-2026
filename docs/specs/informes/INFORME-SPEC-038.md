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
4. **El proyecto final seguía en BCrypt** mientras el lab 09 ya enseñaba Argon2id. Señalado, y
   **alineado después a petición del PO** — §8.1.

**Y tres agregados del PO sobre este mismo PR**, en §8: Argon2id en el proyecto final, el plazo de
entrega y la nota del mapa de módulos.

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

### 6.4 · BCrypt frente a Argon2 · **resuelto: alineado con el lab 09**

La spec original no mencionaba el codificador, así que la primera versión de este trabajo dejó el
proyecto final en `BCryptPasswordEncoder` y señaló la inconsistencia: el **lab 09 pasó a Argon2id
con la SPEC-036**, y el proyecto final seguía en BCrypt.

**El PO decidió alinearlo**, y está hecho — ver §9.1.

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

## 7 · El tag

La cabecera pide **`material-v1.12.0`**, y **por primera vez en esta serie el número está libre**:
no colisiona con la serie histórica v0.x. Se cierra con ése.

---

## 8 · Los tres agregados del PO

Pedidos sobre este mismo PR, después de la primera revisión.

### 8.1 · Argon2id en el proyecto final

Alineado con el lab 09. Los cuatro cambios, en **`base/`, `ejemplo/` y
`instructor/solucion-referencia/`**:

1. **`Argon2PasswordEncoder.defaultsForSpringSecurity_v5_8()`** en `SeguridadConfig`, con el motivo
   comentado: lento en tiempo **y en memoria**, que es lo que deja fuera a las tarjetas gráficas, y
   los parámetros de la fábrica porque elegirlos mal deja Argon2 peor que BCrypt.
2. **`clave_hash` de 60 a 120**, con la razón en la propia migración: un hash Argon2 mide unos 95 y
   su largo **depende de los parámetros** — no es fijo como el de BCrypt.
3. **`bcprov-jdk18on` declarado en los tres poms.** Ya estaba en la maleta: lo capturó la SPEC-036
   para el lab 09, así que `repo-maven/` **no cambió** y los 36 proyectos siguen compilando offline.
4. **Hashes regenerados**, con el propio `Argon2PasswordEncoder` del proyecto:

```
ana   $argon2id$v=19$m=16384,t=2,p=1$2WZREBTpf3Q2qNoiFbO1cg$u9NGi9Dj2pW98+bSI50H9FvsCqrlqbckQQ6xEx8MvnI
luis  $argon2id$v=19$m=16384,t=2,p=1$k2zUdDw7e1DQxTSf86+LyQ$qhEPAHw2vBCgP/1R/9xW/R3IbyoRFn/7ubipO5JVWr4
```

Los dos son de la palabra `secreta` y **no se parecen**: es la sal, y sirve como demostración
adicional del lab 09 dentro del propio proyecto final.

**Medido después del cambio:**

```
base                             Tests run: 1, Failures: 0, Errors: 0
ejemplo                          Tests run: 4, Failures: 0, Errors: 0
instructor/solucion-referencia   Tests run: 4, Failures: 0, Errors: 0

login ana        200          ← con el hash Argon2 sembrado
login luis       200
clave mala       401

1 sin token       401
2 CONTRIBUYENTE   403
3 RUT inexistente 404
4 el bueno        200  ·  total = 6.330.000

36 proyectos compilan offline · repo-maven sin cambios
```

### 8.2 · El plazo de entrega

**Viernes 25 de septiembre de 2026, 23:59**, en el `README.md` de `proyecto-final/` y en el cierre
del brief.

**Por qué esa fecha y no el 18**, que era el viernes de las tres semanas naturales: el **18 y el 19
de septiembre son feriados en Chile**. Una entrega el viernes 18 habría sido, en la práctica, una
entrega el jueves 17 para todo el mundo — y con la semana de Fiestas Patrias por delante. El 25 da
las tres semanas completas de trabajo útil. **Está escrito en el README con esa razón**, para que
si el PO la mueve sepa qué está moviendo.

### 8.3 · La nota del mapa de módulos

Tres líneas al inicio de `docs/temario/MAPA-LAB-MODULO.md`, sin tocar la matriz —que es lo que
`verificar-temario.py` comprueba, y sigue verde—:

> Los labs **12** y **13** se retiraron del curso, junto con `examen-huecos/`: siguen enteros en el
> tag `material-v1.11.1`. El **módulo 15** lo cubre ahora la **demostración con Docker del lab 14**
> más el empaquetado que entrega el proyecto final. Los temas que sólo tocaban esos dos labs pasan
> de **cubiertos** a **mencionados**.

---

## 9 · Lo que queda para el PO

**`docs/temario/MAPA-LAB-MODULO.md` sigue citando lab-12 y lab-13 en las filas de su matriz.** La
nota del §8.3 lo advierte al principio del documento, pero las filas no se reescribieron: esa matriz
refleja el **contrato adjudicado**, cuyos módulos no cambian porque el material se reordene, y
tocarla obliga a revisar `verificar-temario.py` y la coherencia con el `.docx` que se le entrega al
SII. Si el PO quiere que la matriz refleje lo que hoy se dicta, es un cambio aparte.
