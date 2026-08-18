# INFORME-SPEC-031 · El Lab 07 de Testing y la tercera carpeta

**SPEC:** SPEC-031 · **Ejecuta:** mocito · **Fecha:** 18 de agosto de 2026
**Rama:** `spec-031-lab-07-testing` · **Tag al cierre:** `material-v0.7.0`
**Máquina:** Mac Studio del PO (Darwin 25.5.0, `arm64`) · JDK 25.0.4 y Maven 3.9.11 de la maleta

---

## 1 · Veredicto en una línea

**EL LAB 07 DE TESTING EXISTE Y FUNCIONA, Y LA TERCERA CARPETA ESTRENÓ SIN ENTRAR AL
REPOSITORIO** — `solucion/` corre **9 tests, 0 fallos** offline y desde cero; seguir `PASOS.md`
sobre `practica/` produce **los mismos cuatro archivos byte a byte** (salvo comentarios) y el
mismo 9/0; `instructor/` tiene los **15 archivos** de la solución con la misma estructura, sin
`mvnw` ni `target`, y `git add -A` añade **cero** de ellos. **Hay una desviación que sí importa y
no se maquilló:** los tiempos del paso 6 **no sostienen** el titular «`@SpringBootTest` es el más
lento» — al correr la suite al revés se da vuelta, y el guion lo dice (§7.a). Y queda **una
colisión de numeración** con el `lab-07-el-portero` del arco antiguo (§7.b).

---

## 2 · Qué nace, y con qué forma

```
labs/lab-07-testing/
├── README.md          qué se aprende, las tres carpetas, «lo que no vimos hoy»
├── PASOS.md           el guion: paso 0 + seis pasos, 581 líneas
├── practica/          20 archivos · el código de producción completo, src/test/ VACÍO
├── solucion/          24 archivos · lo mismo + los cuatro archivos de test
└── instructor/        16 archivos · la copia explicada. NO versionada
```

**El dominio**, sin narrativa y sin base de datos: un catálogo de cuatro productos en una lista
en memoria (`ProductoRepositoryLista`), un `ProductoService` con un cálculo puro
(`precioConIva`, IVA 19 % con redondeo), un `porId` que lanza `ProductoNoEncontradoException`, un
controller con tres endpoints y un `@RestControllerAdvice` que traduce la excepción a 404 con
cuerpo. **Todo dado y andando**: no hay nada que reparar.

**Los nueve tests de `solucion/`**, uno por concepto de la SPEC:

| Archivo | Paso | Qué enseña |
|---|---|---|
| `ProductoServiceTest` (4) | 1, 2, 3 | `@Test`, `assertEquals`, preparar–ejecutar–comprobar, `assertThrows` |
| `ProductoServiceConDobleTest` (2) | 4 | `@Mock`, `when().thenReturn()`, `verify` |
| `ProductoControllerTest` (2) | 5 | `@WebMvcTest`, `MockMvc`, `jsonPath`, el 200 y el 404 |
| `ContextoDeSpringTest` (1) | 6 | `@SpringBootTest`, y por qué hay uno solo |

**Duración estimada de la sesión: 2 h 40 min**, dentro de las tres horas con veinte de margen —
paso 0: 15 min · paso 1: 25 · **paso 2: 20** · paso 3: 20 · paso 4: 35 · paso 5: 35 · paso 6: 25
· cierre y «lo que no vimos hoy»: 5. El margen es deliberado: 12 de 18 alumnos escribe su primer
test aquí y los pasos 1 y 4 se atascan.

**La estructura de tres carpetas**, medida:

| | archivos | líneas de comentario | criterio |
|---|---|---|---|
| `practica/` | 20 | **0** en `src/` | sin una línea de documentación |
| `solucion/` | 24 | 11 | una o dos líneas donde no es evidente |
| `instructor/` | 16 | ~700 | línea por línea, sin límite |

---

## 3 · La cadena de preguntas

El lab está construido sobre una sola, que se hace en el paso 0 con la aplicación corriendo:

> Todo esto funciona. **¿Cómo lo sabemos?** Porque lo acabamos de mirar. Mañana, cuando alguien
> toque el IVA, ¿quién lo mira?

Y se remata con `./mvnw test` → `BUILD SUCCESS` **con cero tests**: un verde sobre una suite
vacía no dice que el código esté bien, dice que nadie preguntó. De ahí sale el arco entero:

1. **paso 1** — se escribe el primer test y sale verde. *¿Y eso qué demuestra?*
2. **paso 2** — nada, todavía. Se rompe el IVA y sale el **rojo**. *Ahora sí.*
3. **paso 3** — el éxito está cubierto. *¿Y el fallo?*
4. **paso 4** — se prueban dos piezas a la vez. *¿Cuál falló?*
5. **paso 5** — el servicio está cubierto. *¿Y el 404, que no está escrito en ninguna clase?*
6. **paso 6** — todo esto sin Spring. *¿Cuándo hace falta levantarlo?*

---

## 4 · Tabla de verificación

| # | Resultado |
|---|---|
| **V1** | ✅ `practica/` arranca y `./mvnw test` pasa con la suite vacía |
| **V2** | ✅ `solucion/`: **9 tests, 0 fallos** |
| **V3** | ✅ El rojo del paso 2, citado, y el verde de vuelta |
| **V4** | ✅ Tiempos medidos — **y desmentido el titular fácil** (§7.a) |
| **V5** | ✅ `PASOS.md` seguido entero sobre `practica/` → mismo resultado que `solucion/` |
| **V6** | ✅ `instructor/`: 15 de 15 archivos, misma estructura, sin `mvnw`/`target`/`.mvn` |
| **V7** | ✅ `git status` no ve `instructor/`; `git add -A` añadiría **0** de sus archivos |
| **V8** | ✅ `ls` da las cinco entradas |
| **V9** | ✅ **0 descargas**, y `repo-maven/` sin un solo archivo nuevo |

### V1 · `practica/` en su estado de entrega

```
$ cd labs/lab-07-testing/practica && ./mvnw test
[INFO] BUILD SUCCESS
[INFO] Total time:  0.963 s
```

Sin línea de `Tests run`: la suite está vacía porque los tests son lo que el alumno escribe. Y la
aplicación arranca:

```
o.s.boot.tomcat.TomcatWebServer : Tomcat started on port 8093 (http) with context path '/'
cl.dgt.testing.Lab07Application : Started Lab07Application in 0.783 seconds (process running for 0.95)

$ curl http://localhost:8093/productos/2
{"id":2,"nombre":"Tóner negro","precioNeto":68900}

$ curl -i http://localhost:8093/productos/99
HTTP/1.1 404
{"mensaje":"No existe el producto 99"}

$ curl http://localhost:8093/productos/valor-total
{"valorConIva":420891}
```

### V2 · `solucion/`

```
[INFO] Running cl.dgt.testing.ProductoServiceTest
[INFO] Tests run: 4, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 0.034 s
[INFO] Running cl.dgt.testing.ContextoDeSpringTest
[INFO] Tests run: 1, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 1.242 s
[INFO] Running cl.dgt.testing.ProductoServiceConDobleTest
[INFO] Tests run: 2, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 0.077 s
[INFO] Running cl.dgt.testing.ProductoControllerTest
[INFO] Tests run: 2, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 0.369 s
[INFO] Tests run: 9, Failures: 0, Errors: 0, Skipped: 0
[INFO] BUILD SUCCESS
```

### V3 · El paso 2, reproducido

`TASA_IVA` de `0.19` a `0.10`:

```
[ERROR] Tests run: 4, Failures: 1, Errors: 0, Skipped: 0 <<< FAILURE!
[ERROR] cl.dgt.testing.ProductoServiceTest.elPrecioConIvaSeRedondeaAlPesoMasCercano <<< FAILURE!
org.opentest4j.AssertionFailedError: expected: <5938> but was: <5489>
	at cl.dgt.testing.ProductoServiceTest.elPrecioConIvaSeRedondeaAlPesoMasCercano(ProductoServiceTest.java:20)

[ERROR] Failures:
[ERROR]   ProductoServiceConDobleTest.elValorDelCatalogoSumaLosPreciosConIva:36 expected: <3570> but was: <3300>
[ERROR]   ProductoServiceTest.elPrecioConIvaSeRedondeaAlPesoMasCercano:20 expected: <5938> but was: <5489>
[ERROR] Tests run: 9, Failures: 2, Errors: 0, Skipped: 0
[INFO] BUILD FAILURE
```

**Un carácter, dos tests rojos.** El segundo es un regalo que no estaba previsto en la SPEC y que
el guion aprovecha: el test de Mockito, que nunca menciona la constante, también cae — la red
atrapa más de lo que uno cree haber tendido.

Restaurado a `0.19`:

```
[INFO] Tests run: 9, Failures: 0, Errors: 0, Skipped: 0
[INFO] BUILD SUCCESS
```

**Y el rojo del paso 3 también se comprobó** (cambiar `99L` por `2L`), porque el guion lo propone
y no se propone lo que no se ha corrido:

```
org.opentest4j.AssertionFailedError: Expected cl.dgt.testing.services.ProductoNoEncontradoException
to be thrown, but nothing was thrown.
```

### V4 · Los tiempos del paso 6

Suite completa, orden alfabético (`@SpringBootTest` primero):

| Clase | Qué levanta | Tiempo |
|---|---|---|
| `ProductoServiceTest` (4 tests) | nada | **0,034 s** |
| `ProductoServiceConDobleTest` (2) | Mockito | **0,077 s** |
| `ProductoControllerTest` (2) | `@WebMvcTest` | **0,369 s** |
| `ContextoDeSpringTest` (1) | `@SpringBootTest` | **1,242 s** |

La misma suite en orden inverso (`-Dsurefire.runOrder=reversealphabetical`):

```
ProductoServiceTest          0,037 s
ProductoServiceConDobleTest  0,481 s
ProductoControllerTest       1,033 s   ← @WebMvcTest
ContextoDeSpringTest         0,221 s   ← @SpringBootTest
```

**Se dieron vuelta.** Ver §7.a: es la desviación de esta SPEC y está resuelta en el guion.

Y el arranque de contexto que Spring reporta él mismo, que es la comparación limpia:

```
Started ContextoDeSpringTest in 0.685 seconds    (@SpringBootTest, primero)
Started ProductoControllerTest in 0.245 seconds  (@WebMvcTest, segundo)
```

### V5 · `PASOS.md` seguido entero sobre `practica/`

Ejecutado paso a paso, con `./mvnw test` entre cada uno:

| Paso | Tras escribirlo |
|---|---|
| 1 (primer test) | `Tests run: 1, Failures: 0` — 0,028 s |
| 1 (los tres) | `Tests run: 3, Failures: 0` — 0,036 s |
| 2 (romper) | `expected: <5938> but was: <5489>` en `ProductoServiceTest.java:17` |
| 2 (deshacer) | `Tests run: 3, Failures: 0` |
| 3 (`assertThrows`) | `Tests run: 4, Failures: 0` |
| 4, 5 y 6 | `Tests run: 9, Failures: 0` · `BUILD SUCCESS` |

Y la comprobación que cierra V5 — los cuatro archivos resultantes contra los de `solucion/`:

```
--- ProductoServiceTest ---           identico (salvo comentarios)
--- ProductoServiceConDobleTest ---   identico (salvo comentarios)
--- ProductoControllerTest ---        identico (salvo comentarios)
--- ContextoDeSpringTest ---          identico (salvo comentarios)
```

`practica/` quedó restaurada a su estado de entrega: `src/test/java/cl/dgt/testing/` con sólo su
`.gitkeep`.

> **La línea 17 y la línea 20.** El rojo del paso 2 sale en `:17` siguiendo el guion sobre
> `practica/` y en `:20` sobre `solucion/`, que lleva tres líneas de comentario más. `PASOS.md`
> cita **17**, que es la que verá la sala.

### V6 · `instructor/`

```
$ diff <(cd solucion && find pom.xml src -type f | sort) <(cd instructor && find pom.xml src -type f | sort)
MISMA lista de archivos y misma estructura de carpetas (15 archivos)

$ for X in mvnw mvnw.cmd .mvn target .gitignore; do …
ausente (bien): mvnw
ausente (bien): mvnw.cmd
ausente (bien): .mvn
ausente (bien): target
ausente (bien): .gitignore
```

Y la comprobación que impide que la copia explicada se desvíe en silencio: quitados los
comentarios, **los 15 archivos son idénticos** a los de `solucion/`.

```
idéntico: src/test/java/cl/dgt/testing/ProductoServiceConDobleTest.java
idéntico: src/test/java/cl/dgt/testing/ProductoServiceTest.java
idéntico: src/test/java/cl/dgt/testing/ProductoControllerTest.java
idéntico: src/test/java/cl/dgt/testing/ContextoDeSpringTest.java
idéntico: src/main/resources/application.yml
idéntico: src/main/java/cl/dgt/testing/Lab07Application.java
idéntico: src/main/java/cl/dgt/testing/repositories/ProductoRepositoryLista.java
idéntico: src/main/java/cl/dgt/testing/repositories/ProductoRepository.java
idéntico: src/main/java/cl/dgt/testing/models/Producto.java
idéntico: src/main/java/cl/dgt/testing/controllers/ManejadorDeErrores.java
idéntico: src/main/java/cl/dgt/testing/controllers/ProductoController.java
idéntico: src/main/java/cl/dgt/testing/controllers/ErrorRespuesta.java
idéntico: src/main/java/cl/dgt/testing/services/ProductoNoEncontradoException.java
idéntico: src/main/java/cl/dgt/testing/services/ProductoService.java
idéntico: pom.xml
```

(La primera versión del `application.yml` de `instructor/` traía el puerto de `practica/`, 8093.
La comprobación lo cazó y se corrigió a 8094. Es exactamente para lo que sirve.)

Se añadió **un archivo que la SPEC no pide**, `instructor/LEEME.md`: dice que esto no es un
proyecto, por qué no está versionado y en qué orden leerlo. Sin él, quien abra la carpeta dentro
de seis meses intentará compilarla.

### V7 · `git status` con `instructor/` en disco

```
$ ls -d labs/lab-07-testing/instructor
labs/lab-07-testing/instructor          (la carpeta EXISTE en disco)

$ git status --short
 M .gitignore
 M ESTADO.md
 M README.md
 M docs/decisiones.md
?? labs/lab-07-testing/

$ git check-ignore -v labs/lab-07-testing/instructor/pom.xml
.gitignore:52:labs/*/instructor/	labs/lab-07-testing/instructor/pom.xml

$ git add -An labs/lab-07-testing/ | grep -c instructor
0
```

Y tras el commit real, `git diff --cached --name-only | grep -c instructor` → **0**.

### V8 · La estructura

```
$ ls labs/lab-07-testing
PASOS.md   README.md   instructor   practica   solucion
```

### V9 · Offline

Con `target/` borrado en los dos proyectos:

```
$ ./mvnw test
[INFO] Tests run: 9, Failures: 0, Errors: 0, Skipped: 0
[INFO] BUILD SUCCESS
[INFO] Total time:  3.990 s

coincidencias de "Downloading|Downloaded from|Could not resolve|offline mode": 0
archivos cambiados en repo-maven: 0
```

El segundo número es la prueba fuerte: si algo hubiera hecho falta bajar, habría aparecido en
`repo-maven/` o el build habría muerto. Todo lo que el lab usa —incluido
`spring-boot-webmvc-test`, que es nuevo en el curso— ya viajaba en la maleta.

---

## 5 · Transversales

| Gate | Resultado |
|---|---|
| `siembra` (lógica del CI, local) | ✅ `FALLOS=0` · `labs/lab-07-testing/PASOS.md siembra` |
| `deriva` | ✅ **no le aplica**: el bucle recorre una lista fija de labs del arco antiguo, y `lab-07-testing` no está en ella. No hay derivación que declarar (regla de la SPEC) |
| CRLF / `.gitattributes` | ✅ los dos `mvnw` en LF; `mvnw` y `mvnw.cmd` ya cubiertos en `.gitattributes` (líneas 15 y 20) |
| Tamaños (`tools/verificar-tamanos.sh`) | ✅ `[OK] Ningún archivo supera los 95 MB`. El más pesado del lab nuevo: `PASOS.md`, **20 KB** |
| Maleta | ✅ shim `mvnw` + `.mvn/wrapper` copiados del Lab 02, sin tocar |
| Puertos | ✅ 8093 / 8094, libres — el mapa del curso llegaba hasta 8100 (Lab 03b) sin usar estos dos |
| `logging.level.root: WARN` | ✅ en los dos proyectos |
| Prohibiciones | ✅ sin narrativa DGT, sin personajes, sin ArchUnit, sin `bin/`, sin validadores, sin manifiestos, sin derivación, sin base de datos, sin `sudo`, sin LFS, sin credenciales |

**Ningún otro lab fue tocado.** Los cambios fuera de `labs/lab-07-testing/` son cuatro archivos
de la raíz y `docs/`: `.gitignore`, `README.md`, `ESTADO.md` y `docs/decisiones.md`.

> **Nota sobre el árbol de trabajo:** al empezar la SPEC había cambios locales sin commitear en
> `lab-00-hola-mundo` y `lab-01-web` (código de práctica del PO). **Se dejaron intactos y fuera de
> todos los commits.**

---

## 6 · Decisiones tomadas al ejecutar

Cuatro filas nuevas en `docs/decisiones.md`:

- **D-031-1 · La estructura de tres carpetas.** Con su razón: un alumno que tiene delante ocho
  líneas explicando el método que va a teclear **lee el bloque y no escucha**.
- **D-031-2 · `instructor/` no viaja al repositorio.** La razón es pedagógica: versionarla
  anularía D-031-1 el mismo día.
- **D-031-3 · El lab de testing no lleva base de datos.**
- **D-031-4 · Los números del paso 6 se citan con su matiz, no como titular.** Ver §7.a.

Y cinco decisiones de construcción que la SPEC dejaba abiertas:

1. **`practica/` no lleva esqueletos de test, sólo `.gitkeep`.** La SPEC permite «la firma, la
   línea imperativa y `// escribe aquí`», pero un `@Test` vacío **pasa en verde** — y estrenar el
   lab con un verde que no comprueba nada, dos pasos antes de enseñar que un verde vacío no
   demuestra nada, es contradecirse por escrito. La regla es un techo, no un piso, y aquí el
   techo es cero.
2. **El código de producción de `practica/` es el de `solucion/` con los comentarios quitados**, y
   está comprobado programáticamente que no difiere en nada más. Así no puede derivar.
3. **`@Mock` + `new ProductoService(repositorio)` en vez de `@InjectMocks`.** La SPEC pide
   `@Mock`, `when` y `verify`, y no menciona `@InjectMocks`. Construir a mano deja ver que el
   doble es un objeto corriente que se entrega por el constructor — el argumento del Lab 02
   cobrado otra vez.
4. **Los datos del test de Mockito son 1000 y 2000, no los precios reales.** Para que la suma
   esperada (3570) se haga de cabeza. Quien saca la calculadora acaba copiando lo que imprimió el
   programa, y ese test ya no comprueba nada.
5. **`valorDelCatalogo()` existe para que el paso 4 tenga un método que use las dos cosas a la
   vez** —el repositorio y el cálculo propio—. Es el único candidato honesto para el doble.

---

## 7 · Sorpresas y desviaciones

### 7.a · La desviación: los tiempos del paso 6 no dicen lo que la SPEC esperaba

La SPEC pide medir cuánto tarda `@SpringBootTest` «comparado con los anteriores» y sacar la regla
del 90 %. Medido, la comparación **no sale en la dirección esperada de forma estable**:

| | alfabético | inverso |
|---|---|---|
| `ProductoControllerTest` (`@WebMvcTest`) | 0,369 s | **1,033 s** |
| `ContextoDeSpringTest` (`@SpringBootTest`) | **1,242 s** | 0,221 s |

**Lo caro no es la anotación: es el primer arranque de Spring**, unos 0,7 s que paga quien llegue
primero. En una aplicación de nueve clases, sin base de datos y sin seguridad, el contexto
completo no cuesta apenas más que la capa web sola.

**No se maquilló el número.** Si el guion dijera «`@SpringBootTest` es 36 veces más lento», el
primer alumno que corra la suite al revés descubriría que es falso y, con razón, dejaría de
creerse el resto del material. Lo que `PASOS.md` y `instructor/` dicen es lo que sí se sostiene y
no depende del orden:

> de **0,03 s** sin Spring a **0,7 s** con Spring. **Veinte veces.**

Con el matiz explícito de que 0,7 s es el **suelo**: en una aplicación real ese contexto trae pool
de conexiones, Hibernate, seguridad y cachés, y se paga una vez por cada configuración de contexto
distinta. **La regla del 90 % se conserva intacta**; lo que cambia es el número con que se
justifica. Registrado como **D-031-4**.

### 7.b · Dos labs con el número 07

`labs/lab-07-el-portero` (arco antiguo) y `labs/lab-07-testing` (arco vigente) conviven en
`labs/`. **No se estorban** —son proyectos independientes, con puertos y paquetes distintos, y el
gate `siembra` los procesa a los dos sin quejarse— pero la ambigüedad es real al hablar. Se hizo
lo único que cabía sin violar la prohibición de tocar otros labs: **declararla en voz alta** en el
`README.md` de la raíz y en `ESTADO.md`, con la regla de citar siempre el nombre completo, nunca
«el Lab 07». **La renumeración es del PO** y va con la SPEC de reempaquetado.

### 7.c · `@WebMvcTest` ya no viene en `spring-boot-starter-test`

La SPEC dice que `spring-boot-starter-test` «trae JUnit 5, AssertJ, Mockito y MockMvc». Trae
MockMvc (vía `spring-test`), pero **no trae `@WebMvcTest`**: en Spring Boot 4 la autoconfiguración
de test se partió por tecnología y `@WebMvcTest` se fue a un artefacto propio,
`spring-boot-webmvc-test`, con paquete nuevo:

```
Boot 3:  org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
Boot 4:  org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest
```

Se añadió la dependencia al `pom.xml` de los dos proyectos (ya estaba en `repo-maven/`, así que
V9 no se resintió). **Es la trampa número uno del paso 5** —todo lo que hay escrito en internet es
de Boot 3 y al copiarlo el import no resuelve—, y por eso está avisada en `PASOS.md`, en el
`pom.xml` de `solucion/` y con doce líneas en `instructor/`. Lo mismo con **`@MockitoBean`, que
sustituyó a `@MockBean`**.

### 7.d · El paso 2 rompe dos tests, no uno

Cambiar `TASA_IVA` tumba también `elValorDelCatalogoSumaLosPreciosConIva`, el test de Mockito,
que nunca menciona la constante. No estaba previsto y es mejor así: se ve que la red atrapa más
de lo que uno cree haber tendido. El guion lo aprovecha.

### 7.e · El aviso de consola de Mockito

En JDK 25, correr los tests con Mockito escupe tres avisos que asustan y no son un problema
(`Mockito is currently self-attaching…`, `A Java agent has been loaded dynamically…`, `Sharing is
only supported for boot loader classes…`). **No se silenciaron**: hacerlo exige configurar el
`argLine` de Surefire, ninguna otra pieza del repositorio lo hace, y meter maquinaria en el pom
del lab más introductorio del arco es peor que nombrarlo. Está nombrado en `PASOS.md` y en
`instructor/`, con la frase que lo despacha en diez segundos: la línea que importa sigue siendo
`Tests run: 9, Failures: 0`.

---

## 8 · Lo que queda

**Del PO:**

1. **La renumeración del arco vigente** (§7.b). Dos labs con el 07 funciona, pero no se sostiene
   cuando entre el siguiente.
2. **La migración de los labs 00 a 06 a la estructura de tres carpetas.** Su `practica/` conserva
   los bloques explicativos largos que D-031-1 retira. Hoy el arco es **asimétrico**: el alumno
   pasa de un `practica/` que le explica todo a uno que no le explica nada. Es una SPEC de
   reempaquetado y la SPEC-031 la deja explícitamente fuera de alcance.
3. **Dónde entra el testing en el temario contratado.** El mapa `docs/temario/MAPA-LAB-MODULO.md`
   **no se tocó** —está fuera del alcance de esta SPEC— y el lab nuevo todavía no aparece en él.

**Del material, ya cubierto pero conviene tenerlo escrito:**

- **`instructor/` no está respaldada por Git**, por diseño (D-031-2). La copia de esta ejecución
  vive sólo en el disco donde se generó. Quien la quiera, la vuelve a generar a partir de
  `solucion/` — el `LEEME.md` dice cómo y el informe deja constancia de que existió y qué contenía.
- **«Lo que no vimos hoy»**, en el README y en tres líneas, como pide la SPEC: tests de
  persistencia (`@DataJpaTest`), cobertura, y TDD.

**Nada bloquea el merge.** Las nueve verificaciones están en verde con su salida citada, y las dos
desviaciones (§7.a y §7.b) están resueltas y declaradas, no pendientes.
