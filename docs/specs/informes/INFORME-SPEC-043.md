# INFORME-SPEC-043 · Cerrar los pendientes del material

**Ejecuta:** mocito · **Rama:** `spec-043-cerrar-pendientes` · **Fecha:** 28 de agosto de 2026
**Origen:** SPEC-043 del PO, cuatro frentes, con encargo de trabajar de corrido.

---

## 0 · Resumen

**Los cuatro frentes están hechos.** Ninguno se atascó. Uno se entrega **incompleto y declarado**:
el frente 2, con detalle y número en la §2.4.

**Frente 1 · El examen de completar huecos existe y se corrige solo.** `examen-huecos/`, con
`base/` y `solucion/`. **Doce huecos, 48 líneas de código en total.** `base/` compila, **arranca en
3,8 s** y da **0 de 12**; `solucion/` da **12 de 12**. Una corrida de la suite tarda **7 segundos**.
**Los doce huecos son independientes, y está medido**: resolviéndolos de uno en uno la cuenta de
verdes va 0, 1, 2 … 12 sin saltos, que es lo que permite que el puntaje sea directo (§1).
**El tiempo de resolución no se pudo medir de verdad, y se dice por qué** (§1.6).

**Frente 2 · El porqué en `instructor/`, labs 08 al 14 y el examen.** **57 recuadros `POR QUÉ ·`
nuevos**, que dejan el material en **140**. **Cero código movido, y medido**: el código desnudo de
los **200 `.java`** de las diecisiete carpetas `instructor/` es idéntico al de `solucion/`, antes y
después. Las once fuentes compilan offline (§2). **La densidad es un tercio de la de la SPEC-041 y
está declarada en la §2.4.**

**Frente 3 · La V1 del pegado en los labs 10, 11, 12 y 13.** La deuda del `INFORME-SPEC-039` §5,
saldada. **Apareció de todo**, y lo peor primero: el guion del lab 10 mandaba pegar sentencias de
constructor «dentro de la clase», y **pegado al pie de la letra ni siquiera parseaba**. Corregidos
los cuatro guiones, la V1 pasa: **compilan y el resultado es idéntico a `solucion/`** (§3).

**Frente 4 · El respaldo, sincronizado.** **279 archivos en 17 carpetas**, las 279 huellas cuadran,
y el repositorio sigue dando **404 sin credenciales** (§4).

---

## 1 · El examen de completar huecos

### 1.1 · Qué es, y por qué no se parece al proyecto final

El PO nombró tres problemas del `proyecto-final/` para las tres o cuatro clases que quedan: es un
encargo de negocio sobre la DGT que hay que leer entero, **nunca se anunció**, y su resolución
estimada son tres horas.

Lo que se construyó no los tiene:

| | `proyecto-final/` | `examen-huecos/` |
|---|---|---|
| Qué recibe el alumno | un requerimiento incompleto en los bordes | **doce huecos marcados en el código** |
| Cuánto dura | tres horas | **una hora y media, estimada** (§1.6) |
| Qué escribe | el endpoint y todo lo que lleva detrás | **48 líneas** |
| Cómo se corrige | rúbrica, a mano | **`./mvnw test`: la nota es la última línea** |
| Qué entrega | código **y un reporte** | sólo el código |

**El `proyecto-final/` no se tocó.** Queda exactamente como estaba y el PO decide el lunes cuál usa.

### 1.2 · Las tres cosas que la SPEC pedía que lo distinguieran de un lab

**No lleva `PASOS.md`.** Cada hueco trae un recuadro que dice **qué** tiene que hacer, nunca cómo:

```java
    // =========================================================================
    //  HUECO 04 · Las solicitudes de un estado, de la mas reciente a la mas antigua
    // -------------------------------------------------------------------------
    //  Falta la consulta que devuelve las solicitudes de un estado ORDENADAS
    //  por fecha, de la mas reciente a la mas antigua. El orden lo pone la
    //  consulta, no el codigo Java que la llama.
    //
    //  ESTA LISTO CUANDO · pasa el test H-04
    // =========================================================================
```

**Cada hueco tiene su test.** Doce tests, y la suite imprime la nota al terminar:

```
=============================================================================
 HUECOS
-----------------------------------------------------------------------------
  [OK]    H-01 · la relacion: SCL-01 tiene 5 solicitudes
  [OK]    H-02 · derivada por comuna: Santiago tiene 2 oficinas
  ...
  [OK]    H-12 · seguridad: 401 sin token, 403 con el rol equivocado, 200 con el bueno
-----------------------------------------------------------------------------
 RESUELTOS: 12 de 12
=============================================================================
```

**El puntaje es directo**: huecos resueltos sobre doce. Sin ponderaciones.

### 1.3 · Lo que cubre, de los labs 01 al 09

Los siete ejes que la SPEC nombraba, los siete:

| # | Hueco | Eje | De qué lab viene |
|---|---|---|---|
| **01** | `@OneToMany(mappedBy)` y el conteo | entidad con relación | 05 |
| **02** | `findByComuna` | repositorio · derivada | 04 · 05 |
| **03** | `countByEstado` | repositorio · derivada que cuenta | 04 · 06 |
| **04** | `findByEstadoOrderByFechaDesc` | repositorio · derivada que ordena | 04 · 06 |
| **05** | el `record ResumenOficina` | controller con DTO | 01 · 06 |
| **06** | el `record OficinaBreve` y la lista | controller con DTO | 01 · 06 |
| **07** | la suma de montos | servicio | 02 · 06 |
| **08** | `dgt.examen.tope-de-listado` + `@Value` | **configuración** | 02 · 09 |
| **09** | el POST con 201 y `Location` | controller | 01 |
| **10** | el 404 con cuerpo | **manejo de errores** | 03 |
| **11** | el 400 que nombra el campo | **manejo de errores** | 03 |
| **12** | `hasRole("FISCALIZADOR")` | **seguridad por rol** | 09 |

### 1.4 · La decisión de diseño que lo sostiene: los tests entran por HTTP

Es la que hay que entender antes que ninguna otra, y quedó como `D-043-1`.

**Los doce tests piden por URL y afirman sobre el JSON. Ninguno llama a una clase o a un método que
el alumno tenga que escribir.** No es una preferencia de estilo: es lo único que hace el examen
usable.

Un test que llamara a `repositorio.findByComuna(...)` **no compila** mientras ese método no exista.
Y si la carpeta de tests no compila, **no corre ninguno** — el alumno que aún no ha hecho el hueco
02 no podría probar los otros once. El examen sería inservible en el primer minuto.

Se paga un precio, y se dice: son doce tests de integración con el contexto entero, no doce
unitarios. Tardan 7 segundos en vez de medio. Para un examen de noventa minutos, 7 segundos es
gratis.

### 1.5 · Que los doce huecos sean independientes, medido

El puntaje directo sólo es honesto si resolver once huecos da once. Si uno arrastrara a otro, quien
hiciera once de doce podría sacar cinco.

No se dio por bueno: se midió. Un arnés aplica los huecos **acumulativamente** sobre una copia de
`base/` y corre la suite después de cada uno. Los parches se sacan diffeando `base/` contra
`solucion/`, así que no hay una lista escrita a mano que pueda mentir.

```
huecos resueltos | tests verdes | esperado | veredicto
--------------------------------------------------------------
               0 |            0 |        0 | [OK]
               1 |            1 |        1 | [OK]
               2 |            2 |        2 | [OK]
               3 |            3 |        3 | [OK]
               4 |            4 |        4 | [OK]
               5 |            5 |        5 | [OK]
               6 |            6 |        6 | [OK]
               7 |            7 |        7 | [OK]
               8 |            8 |        8 | [OK]
               9 |            9 |        9 | [OK]
              10 |           10 |       10 | [OK]
              11 |           11 |       11 | [OK]
              12 |           12 |       12 | [OK]

[OK] los doce huecos son independientes: cada uno pone verde su test y solo el suyo.
```

**La escalera no tiene saltos ni retrocesos.** Conseguirlo obligó a rehacer el diseño dos veces: el
lado dueño de la relación y tres consultas del repositorio se dejaron **resueltos** de entrada
porque, siendo huecos, habrían encadenado unos con otros.

> **Un fallo del arnés, contado porque casi pasa por un fallo del material.** La primera corrida dio
> rojo en el escalón 1. No era el examen: el extractor de parches asignaba los `import` de
> `Oficina.java` al hueco 02, porque buscaba `import java.util.List;` sin mirar en qué archivo
> estaba, y esa línea aparece en dos huecos distintos. Se arregló mirando el archivo, y la escalera
> quedó limpia. **Un arnés que da rojo por el motivo equivocado cuesta lo mismo que uno que da
> verde por el motivo equivocado.**

### 1.6 · El tiempo de resolución · lo que se midió y lo que NO

La SPEC pedía medir cuánto tarda en resolverse, con objetivo de 60 a 90 minutos. **Aquí hay que ser
claro: eso no se puede medir desde este lado, y no se va a declarar medido.**

**Lo que sí está medido:**

| | |
|---|---|
| Huecos | **12** |
| Líneas de código que el alumno escribe | **48** en total · 4 de media por hueco |
| Corrida completa de la suite | **6,8 s · 6,9 s · 7,7 s** en tres corridas en caliente |
| Arranque de `base/` en frío | **3,8 s** |

El reparto de las 48 líneas, por archivo:

```
ManejadorDeErrores.java        15      (huecos 10 y 11)
ServicioDeSolicitudes.java     11      (huecos 03, 04, 07 y 08)
Oficina.java                    7      (hueco 01)
ServicioDeOficinas.java         6      (huecos 01, 02, 05 y 06)
SolicitudController.java        2      (hueco 09)
OficinaRepository.java          2      (hueco 02)
SolicitudRepository.java        2      (huecos 03 y 04)
application.yml                 2      (hueco 08)
SeguridadConfig.java            1      (hueco 12)
```

**Lo que NO está medido: cuánto tarda una persona.** Quien escribió los huecos sabe las respuestas,
así que cronometrarme a mí mide mi mecanografía, no el examen. **Sólo lo dice un alumno.**

**La estimación, con su aritmética a la vista:** cuatro líneas por hueco, con el lab correspondiente
abierto al lado, dan entre 5 y 7 minutos por hueco contando leer el recuadro, buscar el patrón en el
lab y correr los tests. **Doce huecos × 5 a 7 minutos = 60 a 84 minutos.** Cae dentro del objetivo,
y por eso no se recortó nada — pero es una estimación, no una medida.

**Cómo se resuelve la duda en quince minutos, si el PO quiere el número antes del lunes:** que una
persona que haya hecho los labs resuelva **tres huecos** cronometrados y se multiplique por cuatro.
Con tres basta para calibrar.

**Y si al final se pasa de 90 minutos, recortar es trivial y por eso no urge decidirlo ahora:** los
huecos son independientes, así que se borran los tres más caros —el 05, el 08 y el 11— y el examen
queda en 9 sin tocar nada más. El puntaje se ajusta solo, porque es «verdes sobre el total».

### 1.7 · Verificación

```
examen-huecos/base       compila offline · arranca en 3,825 s en el 8109 · RESUELTOS: 0 de 12
examen-huecos/solucion   compila offline ·                                 RESUELTOS: 12 de 12
```

Ejercido a mano contra `base/` corriendo, para comprobar que lo resuelto funciona y lo hueco falla:

```
  health sin token                      200
  login de ana                          token de 267 caracteres
  GET /oficinas/SCL-01/ficha            {"codigo":"SCL-01","nombre":"Oficina Santiago Centro",...}  200
  GET /oficinas/SCL-01/conteo           500   <- el hueco 01, vacío
  GET /oficinas/SCL-01/ficha sin token  401
```

Y **`base/` no deja pistas**: los cuatro `import` que se quedaron sin uso al abrir un hueco se van
con él. Dejar `import java.net.URI;` sería decirle al alumno con qué se construye la cabecera
`Location`.

### 1.8 · El examen entra en las tres herramientas que vigilan el material

Sin esto sería lo único del repositorio que nadie comprueba, que es justo lo que castiga `P-05`:

- **El job `labs` del CI** compila ahora **39 proyectos**, no 37. Se **compilan**, no se testean:
  los doce tests de `base/` están rojos a propósito.
- **`tools/verificar-instructor.py`** ve `examen-huecos/instructor`. Y se le cambió una condición:
  el espejo con `solucion/` dependía de que la carpeta fuera un lab, y ahora depende de que **haya**
  una `solucion/` al lado. `proyecto-final/` no la tiene y queda igual que estaba.
- **`tools/instructor-respaldo.sh`** lo respalda: 17 carpetas, no 16.

Reproducido en local el bucle exacto del CI:

```
[INFO] 39 proyectos Maven
[OK] Los 39 proyectos compilan offline.
[OK] examen-huecos/base
[OK] examen-huecos/solucion
```

---

## 2 · La documentación del porqué · labs 08 al 14 y el examen

### 2.1 · Qué se hizo

Mismo criterio de la SPEC-041 (`D-041-1`) y mismo recuadro: qué hace, qué alternativas existen, por
qué ésta aquí, en qué caso elegirías otra. **57 recuadros nuevos**, repartidos así:

| carpeta | recuadros | carpeta | recuadros |
|---|---|---|---|
| lab-08 testing | **11** | lab-12 tareas | **5** |
| lab-09 seguridad | **8** | lab-13 empaquetado | **5** |
| lab-10 resiliencia | **6** | lab-14 microservicios | **7** |
| lab-11 observabilidad | **7** | **examen-huecos** | **8** |

Con los 83 de la SPEC-041, el material va en **140 recuadros**.

Se documentaron las decisiones que cada lab **enseña**, no el andamiaje repetido: `assertEquals`
frente a AssertJ y `@WebMvcTest` frente a `@SpringBootTest` en el 08; el prefijo `ROLE_` y la firma
simétrica en el 09; los umbrales del circuito y el `ignoreExceptions` en el 10; el MDC y el
`HealthIndicator` propio en el 11; `fixedDelay` frente a `fixedRate` y el proxy de `@Async` en el
12; Jib frente a Dockerfile y Buildpacks en el 13; una base por servicio y la caída de auditoría en
el 14.

En el 13 los recuadros van en el `pom.xml`, porque ahí es donde están sus decisiones. **Con reglas
de `=` y cero guiones dobles** (`D-FIX10-1`): el pom sigue parseando, comprobado.

### 2.2 · El código no se movió, y está medido

Se reusó el despojador de comentarios de la SPEC-041 y se corrió **antes** y **después**:

```
ANTES                                                    DESPUÉS
lab-08-testing          13 archivos · 0 divergencias     13 · 0
lab-09-seguridad        13 · 0                           13 · 0
lab-10-resiliencia       6 · 0                            6 · 0
lab-11-observabilidad   10 · 0                           10 · 0
lab-12-tareas            6 · 0                            6 · 0
lab-13-empaquetado       2 · 0                            2 · 0
lab-14/gateway           8 · 0                            8 · 0
lab-14/tramites         13 · 0                           13 · 0
lab-14/contribuyentes   10 · 0                           10 · 0
lab-14/auditoria        10 · 0                           10 · 0
examen-huecos           29 · 0                           29 · 0
```

### 2.3 · Compilan

`instructor/` no es un proyecto, así que se montaron sus fuentes sobre una copia de `solucion/`
dentro del repositorio, en `.e2e/` (que el `.gitignore` ya ignora), para que el shim encontrara
`tools/maven/`:

```
  [OK]    lab-08-testing                                 13 .class
  [OK]    lab-09-seguridad                               14 .class
  [OK]    lab-10-resiliencia                              6 .class
  [OK]    lab-11-observabilidad                          11 .class
  [OK]    lab-12-tareas                                   6 .class
  [OK]    lab-13-empaquetado                              2 .class
  [OK]    examen-huecos                                  30 .class
  [OK]    lab-14-microservicios/gateway                  10 .class
  [OK]    lab-14-microservicios/tramites                 16 .class
  [OK]    lab-14-microservicios/contribuyentes           11 .class
  [OK]    lab-14-microservicios/auditoria                12 .class

[OK] todas las fuentes de instructor/ compilan offline.
```

Y el verificador de la casa:

```
$ python3 tools/verificar-instructor.py
Carpetas `instructor/` encontradas: 17
  [OK] examen-huecos/instructor  ·  29/29 .java
  …
Comprobados: 20 XML · 200 .java · 17/17 carpetas con su documento de entrada
[OK] `instructor/` está al día con `solucion/` y su XML es válido.
```

### 2.4 · **Este frente se entrega incompleto, y aquí está el número**

La SPEC-041 dejó los labs 04 a 07 con **una media de 21 recuadros por lab**. Éstos van en **7**.

| | recuadros por lab |
|---|---|
| Labs 04 a 07 (SPEC-041) | 29 · 18 · 18 · 18 — **media 21** |
| Labs 08 a 14 (SPEC-043) | 11 · 8 · 6 · 7 · 5 · 5 · 7 — **media 7** |

**Qué SÍ está cubierto:** las decisiones distintivas de cada lab — lo que ese lab enseña y no
enseña ningún otro. Un instructor que abra el `instructor/` del lab 10 encuentra respondido por qué
el circuito abre con cinco llamadas y no con cien, y por qué el reintento ignora el rechazo del
circuito.

**Qué NO está cubierto:** el andamiaje heredado que la SPEC-041 sí documentó lab por lab — el
`pom.xml` (Zonky frente a Testcontainers y H2, el scope `runtime`), la clase de arranque, y las tres
clases de `infra/`. Están documentados en los labs 04 a 07 y **no se repitieron aquí**.

**Por qué se paró ahí, dicho sin adornos:** repetir siete veces el mismo recuadro del PostgreSQL de
Zonky añade páginas y no añade respuestas — quien prepara el lab 12 no necesita leer por sexta vez
por qué no se usa H2. Es una decisión de alcance, tomada por lo conservador y anotada, **no una
medida del criterio de la SPEC-041**: si el PO quiere paridad literal, son otra tanda de recuadros y
está en la §6.

---

## 3 · La prueba de pegado en los labs 10, 11, 12 y 13

### 3.1 · La deuda que se salda

`INFORME-SPEC-039` §5 lo dejó escrito: la V1 —pegar los bloques en `practica/` limpia y comprobar
que se llega a `solucion/`— se hizo en diez labs y **no** en cinco. El 14 se cubrió con la SPEC-037.
Quedaban el **10, 11, 12 y 13**, que son exactamente los que la SPEC-043 nombra.

Y lo que ese informe temía era literal: *«un bloque correcto colocado en el sitio equivocado sigue
sin compilar»*.

### 3.2 · Lo primero que se vio, y es medible

**Los labs 01 a 09 y el 14 dan los `import` dentro de sus bloques. El 10, 11, 12 y 13 no daban
ninguno.**

```
lab-00   bloques:  2   lineas import:  0        lab-08   bloques:  8   lineas import: 40
lab-01   bloques: 15   lineas import: 11        lab-09   bloques:  9   lineas import: 42
lab-02   bloques: 11   lineas import: 36        lab-10   bloques:  4   lineas import:  0
lab-03   bloques: 14   lineas import: 19        lab-11   bloques:  5   lineas import:  0
lab-04   bloques: 22   lineas import: 29        lab-12   bloques:  4   lineas import:  0
lab-05   bloques: 16   lineas import: 12        lab-13   bloques:  3   lineas import:  0
lab-06   bloques: 16   lineas import:  7        lab-14   bloques:  7   lineas import:  8
lab-07   bloques:  6   lineas import:  2
```

Los cuatro sin `import` son los cuatro sin V1. No es casualidad: **la V1 es lo único que lo
encuentra**, porque el job `pasos` comprueba que lo que el bloque dice esté en `solucion/`, y unos
`import` que faltan no son nada que comprobar.

### 3.3 · Lab 10 · el pegado literal no parseaba

Pegados los cuatro bloques exactamente donde el guion decía:

```
[ERROR] PagoService.java:[17,9]  illegal start of type
[ERROR] PagoService.java:[17,13] ';' expected
[ERROR] PagoService.java:[22,36] <identifier> expected
[ERROR] PagoService.java:[40,5]  illegal start of expression
```

**La causa:** los bloques del reintento y del circuito son **sentencias de constructor** —empiezan
por `this.reintento = ...`— y el guion mandaba ponerlas «**dentro de la clase**», que es donde
`practica/` tiene el marcador. En Java, una sentencia suelta en el cuerpo de una clase no es nada.

Y había tres cosas más:

- **No se declaraban los campos** `circuito` y `reintento` ni el `log`. El guion decía «el campo y
  el constructor», pero el campo no estaba en ningún bloque.
- **Faltaban enteros** `estadoDelCircuito()`, `metricas()` y el endpoint `GET /pagos/estado-circuito`.
  Y esto es lo llamativo: **el propio guion hace `curl` contra ese endpoint** en su paso 4
  (`PASOS.md:267`), un endpoint que nunca le pidió a nadie que escribiera.
- Los nueve `import` de `PagoService` y los tres de `ClienteTesoreria`, sin dar.

### 3.4 · Labs 11, 12 y 13

- **Lab 11.** `FiltroDeCorrelacion.java` y `SaludDeLaBase.java` se anuncian como «**el archivo
  entero**» y llegaban **sin `package` y sin `import`** — el bloque empezaba en `@Component`. Y los
  pasos 1, 4 y 5 pegan cada uno un trozo bajo `management:`, que es **una sola clave**: pegados como
  tres bloques seguidos, el `application.yml` no es válido y la aplicación no arranca. Ahora el
  guion lo dice con todas las letras.
- **Lab 12.** Los dos archivos nuevos, el mismo caso. Faltaban los `import` de `@EnableScheduling`,
  `@EnableAsync` y `@Async`. Y faltaba entero el **cableado de `CierreNocturno` en el controlador**
  —campo, parámetro del constructor y la línea de `/quien`— que `solucion/` sí tiene.
- **Lab 13.** El bloque de Jib escribía `<version>3.5.2</version>` a mano **teniendo el `pom.xml` la
  propiedad `jib.version` ya declarada**. Ahora usa `${jib.version}`.

### 3.5 · La V1, después de corregir

```
lab 10   compila · 6 archivos .java idénticos a solucion/
lab 11   compila · 10 archivos · 1 divergencia: PUERTO_BASE (55442 en practica, 55443 en
                   solucion), que difiere A PROPÓSITO para que las dos corran a la vez
lab 12   compila · 6 archivos idénticos
lab 13   el pom parsea · compila · 2 .java y los dos .yml nuevos idénticos
```

El `application.yml` del lab 11, después de fundir los tres trozos, comprobado leyendo sus valores y
no sólo mirando que abriera:

```
[OK] yml pegado parsea · exposure: health,info,metrics · show-details: always
     · readiness: readinessState,baseDeDatos
```

**Y el lab 13 se llevó hasta el final**, porque su lección es la imagen y no el `pom.xml`:

```
$ ./mvnw -o package jib:buildTar
target/jib-image.tar · 138.9 MB
```

**138,9 MB, sin Docker y sin red** — el número exacto que el curso proyecta, construido desde el
`pom.xml` que sale de pegar el guion corregido.

### 3.6 · Los dos verificadores del CI, después

```
[OK] 15 guion(es) verificado(s): todo lo que prometen está en solucion/.
Promesas comprobadas: 27 «archivo nuevo» · 48 «pegar dentro de» · 4 «el archivo entero»
                    · 12 «llega vacía» = 91 en total
[OK] Todo lo que los guiones prometen sobre `practica/` es verdad.
```

**Sólo cambian archivos `.md`.** Ni una línea de código de lab tocada.

---

## 4 · El respaldo

```
$ tools/instructor-respaldo.sh respaldar
  [OK] labs/lab-08-testing/instructor                  16 archivos
  [OK] examen-huecos/instructor                        14 archivos
  …
  TOTAL                                          279 archivos en 17 carpetas

COMPROBACION
  disco   : 279 archivos
  respaldo: 279 archivos
  huellas que cuadran en los dos: 279
  [OK] los dos arboles son identicos.
```

Commiteado y subido. Comprobado **después** del push:

```
$ tools/instructor-respaldo.sh estado
  huellas que cuadran en los dos: 279        [OK] los dos arboles son identicos.

$ gh repo view RodrigoMSB/springboot-sii-2026-instructor
  visibilidad: PRIVATE · isPrivate: true

$ curl -s -o /dev/null -w '%{http_code}' https://github.com/RodrigoMSB/springboot-sii-2026-instructor
  404
```

Eran **245 archivos en 16 carpetas** al cerrar la SPEC-042. Los 34 nuevos son la carpeta
`instructor/` del examen.

---

## 5 · Lo que NO se hizo

- **El frente 2 no llega a la densidad de la SPEC-041.** Media de 7 recuadros por lab contra 21. Está
  medido, explicado y acotado en la §2.4.
- **No se tocó el `proyecto-final/`.** Ni un archivo. Era el encargo.
- **No se midió el tiempo real de resolución del examen** (§1.6). No se puede desde aquí, y se dice
  en vez de estimarlo y llamarlo medida.
- **El CI no corre los tests del examen, sólo lo compila.** Los doce están rojos a propósito en
  `base/`: un job que los corriera fallaría siempre. Correr los de `solucion/` sí tendría sentido y
  no se hizo — está en la §6.
- **El examen no se probó en Windows.** Todo se midió en macOS. La casa tiene escrito que Windows es
  la plataforma que encuentra los defectos, así que esto hay que decirlo: el examen usa el mismo
  `mvnw`, el mismo PostgreSQL embebido y las mismas guardas que los quince labs —que sí están
  probados allí—, pero **el examen en concreto no**.
- **No se tocó el trabajo del PO en `practica/`.** El árbol traía ocho archivos suyos de hoy en los
  labs 04 y 05, de la clase que estaba dictando. Siguen sin tocarse y sin commitearse.
- **No se añadió `verificar-instructor.py` al CI** ni se le hizo llamar a `instructor-respaldo.sh
  estado`. Lo primero sigue siendo el gate decorativo que `P-05` castiga; lo segundo estaba anotado
  en el `INFORME-SPEC-042` §9 y sigue anotado, porque cambia el contrato de una herramienta que esta
  SPEC no venía a tocar.

---

## 6 · Anotado para después

1. **El número real del examen.** Que una persona resuelva tres huecos cronometrada y se multiplique
   por cuatro (§1.6). Es lo único que cierra el objetivo de 60 a 90 minutos.
2. **El examen en la VM de Windows.** Media hora, y es la plataforma donde salen los defectos.
3. **Los tests de `examen-huecos/solucion` en el CI.** Están verdes los doce y hoy no los mira nadie:
   si mañana alguien toca `solucion/` y rompe uno, no se entera hasta el día del examen. Es un job
   corto y es el único de los dos proyectos que se puede testear.
4. **La paridad del frente 2 con la SPEC-041**, si el PO la quiere (§2.4).
5. **Que `verificar-instructor.py` vigile el formato del recuadro** —que un `POR QUÉ ·` traiga sus
   cuatro partes— y que llame a `instructor-respaldo.sh estado`. Los dos venían anotados de la
   SPEC-041 y la SPEC-042, y siguen sin hacerse.
6. **`estadoDelCircuito()` del lab 10 no lo llama nadie.** Hallazgo de paso: `solucion/` lo tiene y
   el controlador usa `metricas()`. No se tocó — es código de `solucion/` y esta SPEC sólo venía a
   arreglar guiones.
