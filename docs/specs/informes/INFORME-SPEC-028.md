# INFORME-SPEC-028 · El curso ya tiene primer día

**SPEC:** SPEC-028 · **Ejecuta:** mocito · **Fecha:** 15 de agosto de 2026
**Rama:** `spec-028-labs-00-03` desde `main` (v0.4.0) · **PR:** #34, en draft
**Máquina:** Mac Studio del PO (Darwin 25.5.0, `arm64`) · JDK 25.0.4 y Maven 3.9.11 de la maleta

---

## 1 · Veredicto en una línea

**LOS CUATRO LABS ESTÁN Y FUNCIONAN, Y LOS CUATRO GUIONES SE SIGUIERON ENTEROS** — `lab-00-hola-mundo`,
`lab-01-web`, `lab-02-di` y `lab-03-errores` nacen con el formato del 3.5c y con V1–V6 medidas una a
una. **V4 —seguir `PASOS.md` completo sobre `practica/`— se hizo lab por lab**, y en los tres labs
web el diff de las respuestas contra `solucion/` salió **idéntico**. Ningún lab existente se tocó.
Por el camino, tres afirmaciones que parecían obvias resultaron falsas al medirlas (§7), y las tres
cambiaron el material antes de escribirlo.

---

## 2 · Qué nace, y con qué forma

| Lab | Tema | El momento | Pasos | Sesión | Puertos (practica / solucion) |
|---|---|---|---|---|---|
| `lab-00-hola-mundo` | Que Spring Boot arranque | «funciona, y lo hice yo» | 4 | 15–20 min | — (sin web) |
| `lab-01-web` | El primer endpoint | «mi código responde por HTTP» | 6 + paso 0 | 45–60 min | 8081 / 8082 |
| `lab-02-di` | Inyección de dependencias | «cambié una anotación y la app hace otra cosa» | 6 | 60–75 min | 8083 / 8084 |
| `lab-03-errores` | Errores con forma | «un 404 que no es un stacktrace» | 5 | 45–60 min | 8085 / 8086 |

Los cuatro con exactamente cuatro entradas: `README.md`, `PASOS.md`, `practica/`, `solucion/`.
Sin tests, sin `bin/`, sin validadores, sin manifiestos, sin derivación, sin TODOs con llaves, sin
ArchUnit, sin narrativa DGT y sin citas de personajes.

**Nada existente se tocó.** Los cuatro conviven con `lab-00-estacion-base`,
`lab-01-del-otro-lado-del-boton`, `lab-02-el-folio-que-se-filtro` y `lab-03-red-de-seguridad`: los
nombres de directorio no colisionan y la única regla de CI que recorre `labs/*` —`siembra`— pasa con
0 fallos (§5).

---

## 3 · La cadena de preguntas

Los cuatro labs no son cuatro temas sueltos: cada uno termina con una pregunta que el siguiente
contesta, y esa pregunta es la sección «Lo que siembra este lab» de su `PASOS.md`.

| Al terminar el lab… | …queda esta pregunta | …y la contesta |
|---|---|---|
| **00** | La aplicación arrancó, imprimió y se murió en medio segundo. ¿Qué hay que agregar para que se quede y conteste? | Lab 01, con una línea en el `pom.xml` |
| **01** | El controller guarda los datos dentro de sí mismo. ¿Quién construye la pieza que se los dé? | Lab 02 |
| **02** | `/productos/99` devolvió `404` con `Content-Length: 0`. Un 404 vacío no dice nada, un stacktrace dice demasiado. | Lab 03 |
| **03** | La aplicación ya arranca, responde, se cablea sola y falla con educación. No se acuerda de nada. | Lab 3.5 |

El `404` con cuerpo vacío del Lab 01 es deliberado y se arrastra a propósito por dos laboratorios
antes de arreglarse. Es el mismo hilo que ya usaba el 3.5c con el `findAll` sin `where`.

---

## 4 · Tabla de verificación

Todo medido en esta máquina, con la maleta (JDK 25.0.4 y Maven 3.9.11 del repositorio) y en modo
offline. El arnés de las peticiones apaga **por PID**, nunca por nombre.

### Lab 00 · hola-mundo

| # | Prueba | Resultado |
|---|---|---|
| **V1** | `practica/` en su estado de entrega | ✅ Arranca y termina. `Started HolaMundoApplication in 0.425 seconds`, sin imprimir mensaje — que es lo correcto: el `run()` llega vacío |
| **V2** | `solucion/` | ✅ Mismo arranque, y después `  Hola, mundo. Esto lo escribí yo.` |
| **V3** | Endpoints | — No aplica: este lab no tiene web, a propósito |
| **V4** | Seguir `PASOS.md` sobre `practica/` | ✅ Tras los pasos 2 y 3: `[la-app-de-carolina] ... Started ...` + el mensaje. Igual que `solucion/` |
| **V5** | Offline | ✅ `descargas: 0` en `clean package` |
| **V6** | `ls labs/lab-00-hola-mundo` | ✅ `PASOS.md README.md practica solucion` |

### Lab 01 · web

| # | Prueba | Resultado |
|---|---|---|
| **V1** | `practica/` con `controllers/` y `dto/` vacíos | ✅ `Tomcat started on port 8081`, se queda arriba, y `/hola` da `404` — el «antes» del paso 0 |
| **V2** | `solucion/` | ✅ `Tomcat started on port 8082`, `Started Lab01Application in 0.813 seconds` |
| **V3** | Los cinco endpoints con `curl -i` | ✅ Ver bloque abajo |
| **V4** | Seguir `PASOS.md` sobre `practica/` | ✅ **diff contra `solucion/` idéntico** (normalizando puerto y fecha) |
| **V5** | Offline | ✅ 0 descargas en ambos proyectos |
| **V6** | `ls` | ✅ Exactamente las cuatro entradas |

```
GET /hola                              200  text/plain    Hola, mundo.
GET /hola/Carolina                     200  text/plain    Hola, Carolina.
GET /saludo?nombre=Carolina            200  text/plain    Hola, Carolina.
GET /saludo?nombre=Carolina&formal=true 200 text/plain    Buenos días, Carolina.
GET /saludos/Carolina                  200  application/json  {"mensaje":"Hola, Carolina.","para":"Carolina","formal":false}
GET /saludos/Pedro                     404  Content-Length: 0
POST /saludos {"nombre":"Carolina","formal":true}
                                       201  application/json  {"mensaje":"Buenos días, Carolina.","para":"Carolina","formal":true}
```

También se comprobó la afirmación del paso 3, en vez de suponerla: quitando el `defaultValue` de
`@RequestParam`, pedir `/saludo?nombre=Carolina` responde **400 Bad Request**.

### Lab 02 · di

| # | Prueba | Resultado |
|---|---|---|
| **V1** | `practica/` con las cuatro carpetas vacías | ✅ `Tomcat started on port 8083`, `/productos` da `404` |
| **V2** | `solucion/` | ✅ `Started Lab02Application in 0.779 seconds` |
| **V3** | Endpoints | ✅ Ver bloque abajo |
| **V4** | Seguir `PASOS.md` sobre `practica/`, los seis pasos | ✅ **diff contra `solucion/` idéntico** |
| **V5** | Offline | ✅ 0 descargas |
| **V6** | `ls` | ✅ Exactamente las cuatro entradas |

```
GET /productos        200  [{"id":1,"nombre":"Resma de papel carta","precio":4990}, … 4 items]
GET /productos/quien  200  ProductoRepositoryLista
GET /productos/2      200  {"id":2,"nombre":"Tóner negro","precio":68900}
GET /productos/99     404
```

El **paso 4 tiene que fallar**, y falla. Medido sobre `practica/` en el punto exacto del guion —con
el controller pidiendo el repositorio y sin servicio todavía:

```
APPLICATION FAILED TO START

Description:

Parameter 0 of constructor in cl.dgt.di.controllers.ProductoController required a single bean, but 2 were found:
	- productoRepositoryFalso: defined in file [.../ProductoRepositoryFalso.class]
	- productoRepositoryLista: defined in file [.../ProductoRepositoryLista.class]

This may be due to missing parameter name information

Action:

Consider marking one of the beans as @Primary, updating the consumer to accept multiple beans, or using @Qualifier …
```

Y el **paso 5**, que es el golpe del laboratorio, con las dos salidas medidas:

| Estado | `/productos/quien` | `/productos` |
|---|---|---|
| con `@Primary` en `ProductoRepositoryLista` | `ProductoRepositoryLista` | los 4 productos reales |
| añadiendo `@Qualifier("productoRepositoryFalso")` | `ProductoRepositoryFalso` | `PRODUCTO DE PRUEBA UNO`, `…DOS` |

Una anotación. Nada más tocado.

### Lab 03 · errores

| # | Prueba | Resultado |
|---|---|---|
| **V1** | `practica/` en su estado de entrega | ✅ `Tomcat started on port 8085`. `/productos` da 200; `/productos/99` da **500 con la traza entera en el cuerpo** — el punto de partida del lab |
| **V2** | `solucion/` | ✅ `Started Lab03Application in 0.794 seconds` |
| **V3** | Endpoints, incluidos 404/400/500/201 | ✅ Ver bloque abajo |
| **V4** | Seguir `PASOS.md` sobre `practica/`, los cinco pasos | ✅ **diff contra `solucion/` idéntico** |
| **V5** | Offline | ✅ 0 descargas |
| **V6** | `ls` | ✅ Exactamente las cuatro entradas |

```
GET  /productos                     200  [ … 3 productos ]
GET  /productos/2                   200  {"id":2,"nombre":"Tóner negro","precio":68900}
GET  /productos/99                  404  {"mensaje":"No existe el producto con id 99.","codigo":404,"timestamp":…}
GET  /productos/1/cuota?cuotas=3    200  1663
GET  /productos/1/cuota?cuotas=0    500  {"mensaje":"Ocurrió un error inesperado. Inténtalo más tarde.","codigo":500,…}
GET  /noexiste                      404  {"mensaje":"La ruta pedida no existe.","codigo":404,…}
POST /productos {"nombre":"Escritorio","precio":89900}
                                    201  {"id":4,"nombre":"Escritorio","precio":89900}
POST /productos {"nombre":"","precio":-5}
                                    400  {"mensaje":"Hay datos inválidos en la petición.","codigo":400,…,
                                          "campos":{"precio":"el precio debe ser mayor que cero",
                                                    "nombre":"el nombre es obligatorio"}}
```

Los estados intermedios del guion también se comprobaron uno a uno, porque un guion que promete un
resultado intermedio y no lo cumple deja al instructor colgado en mitad de la clase:

| Tras el paso | Lo que promete el guion | Medido |
|---|---|---|
| 2 | Sigue siendo **500**, solo mejora el mensaje | ✅ `500`, `"exception":"cl.dgt.errores.exceptions.ProductoNoEncontradoException"` |
| 3 | **404 con cuerpo**, sin traza | ✅ `404 {"mensaje":"No existe el producto con id 99.","codigo":404,…}` |
| 4 (antes) | El POST acepta basura | ✅ `201 {"id":4,"nombre":"","precio":-5}` |
| 4 (después) | **400** con los **dos** campos | ✅ `campos` con `nombre` y `precio` |
| 5 (con handler general, sin el cuarto) | `/noexiste` pasa a dar **500** | ✅ `500` — el efecto colateral, reproducido |
| 5 (con el cuarto handler) | `/noexiste` vuelve a **404** | ✅ `404 {"mensaje":"La ruta pedida no existe."}` |

---

## 5 · Transversales

| Prueba | Resultado |
|---|---|
| **Convivencia** | ✅ Los **seis** proyectos web arriba a la vez (8081–8086). Los seis puertos responden, cada uno lo suyo, y ningún log tiene `Port … was already in use` ni `Web server failed to start` |
| **Gate `siembra`** | ✅ Réplica local sobre lo versionado: **0 fallos**. `lab-00-hola-mundo` queda exento por la regla `*/lab-00-*` (pre-curso) que ya existía; los otros tres traen su sección «Lo que siembra este lab» |
| **Gate `deriva`** | ✅ No afectado: `verificar-toda-derivacion.sh` recorre una **lista explícita** de labs, y los cuatro nuevos no están en ella. Ninguno tiene `derivacion-*.txt`, y no debe tenerlo |
| **Gate `labs-sh`** | ✅ No afectado: los cuatro labs no aportan ni un `.sh` |
| **Guarda de 95 MB** | ✅ El archivo más grande de los cuatro labs es `lab-03-errores/PASOS.md`, **13,0 KB** |
| **`du -sh repo-maven`** | **230 MB**, sin cambios: los cuatro labs no añadieron ni un artefacto — todo lo que usan (`starter`, `starter-web`, `starter-validation` 4.1.0) ya viajaba |

### El CI, y el gate que hubo que enseñar

Primera corrida sobre la rama: **`siembra` en rojo**, con tres errores exactos:

```
[ERROR] labs/lab-01-web no tiene TEORIA.md
[ERROR] labs/lab-02-di no tiene TEORIA.md
[ERROR] labs/lab-03-errores no tiene TEORIA.md
[ERROR] 3 lab(s) sin siembra.
```

La réplica local había dado 0 fallos, y la diferencia estaba en **de dónde salió cada copia del
gate**: el fallback a `PASOS.md` lo introdujo la SPEC-027 y vive en su rama, **sin mergear**. Esta
rama sale de `main`, donde el gate solo conoce `TEORIA.md`.

Se aplicó **el mismo arreglo, escrito igual** que en `spec-027-lab-3-5-jpa`, para que las dos ramas
converjan sin pelea al mergear. Ningún lab queda exento por serlo: P-18 exige sembrar el módulo
siguiente, no un nombre de archivo, y se le exige lo mismo al documento que exista.

Resultado tras el arreglo — corrida `31910698603`:

| Job | |
|---|---|
| `app · dgt-tramites-api (verify)` | ✅ |
| `grpc · la demo del Lab 08` | ✅ |
| `lab14 · el sistema de microservicios` | ✅ |
| `labs-sh · andamiaje (ubuntu-latest)` | ✅ |
| `labs-sh · andamiaje (windows-latest)` | ✅ |
| `siembra · toda TEORIA.md con sucesor…` | ✅ |
| `temario · coherencia .md ↔ .docx` | ✅ |
| `deriva · labs en sincronía con su base` | ❌ **el rojo que ya traía `main`** |

**El rojo de `deriva` no es nuevo, y se comprobó en vez de suponerlo:** se compararon los `[ERROR]`
de la última corrida de `main` con los de esta rama y son **los mismos** — `1 eslabon(es) con deriva
silenciosa`, `13 archivo(s) divergieron sin declararse`, y la misma lista (`mvnw`, `mvnw.cmd`,
`pom.xml`, `application-dev.yml`…). Es el `lab-08` atrasado respecto del `lab-07` desde el PR #27,
documentado en `ESTADO.md` §2, y se apaga migrando el lab-08 (PR #31), no declarando divergencias.
**7 de 8 en verde, cero rojos nuevos.**

---

## 6 · Decisiones tomadas al ejecutar

**D-028-1 · `controllers/` también llega vacío en el Lab 02.** La SPEC nombra `models/`,
`repositories/` y `services/`. Pero el paso 2 del propio guion hace que el alumno escriba el
controller —«un controller que la usa recibiéndola por constructor»—, así que darlo hecho habría
dejado el paso sin trabajo. Se creó `controllers/` con `.gitkeep`.

**D-028-2 · Un endpoint que no devuelve datos del negocio: `/productos/quien`.** El Lab 02 tenía que
contestar «¿quién construyó este objeto?» con un dato y no con una explicación. Devuelve el
`getSimpleName()` de la implementación inyectada. Es lo que permite que el paso 5 sea una medición y
no una promesa.

**D-028-3 · El Lab 02 termina sin `@Qualifier`.** El paso 5 lo prueba y lo quita: el estado final es
solo `@Primary`. Así `practica/` y `solucion/` coinciden al terminar (V4) y el `@Qualifier` se ve
funcionando en vivo, que es donde enseña.

**D-028-4 · `practica/` del Lab 03 viene con la filtración encendida.** Sus tres ajustes de
`spring.web.error` hacen visible en el cuerpo de la respuesta lo que normalmente se queda en el log.
Sin ellos, el paso 1 solo mostraría `{"status":500,"error":"Internal Server Error"}` y la lección se
perdería. **El paso 5 los quita** y `solucion/` no los tiene.

**D-028-5 · El Lab 03 tiene un cuarto handler que la SPEC no pedía.** Ver §7.3: hizo falta.

**D-028-7 · El gate `siembra` aprende `PASOS.md` también en esta rama.** Es el mismo cambio que la
SPEC-027 hizo para el 3.5c, replicado literalmente porque esta rama sale de `main` y allí no está.
Ver §5.

**D-028-6 · `logging.level.cl.dgt: INFO` junto a `root: WARN`.** La SPEC pide `root: WARN` para que
la consola muestre lo del lab. Pero con solo eso desaparecen también las líneas `Starting` y
`Started`, que para un alumno del primer día son **la señal de que la cosa llegó**. Se deja el
logger propio en INFO, y en los labs web también `org.springframework.boot.tomcat`, que es quien
dice el puerto.

---

## 7 · Sorpresas y desviaciones

### 7.1 · `spring.application.name` no sale donde parecía

El paso 3 del Lab 00 promete que cambiar el nombre en `application.yml` «se refleja en el arranque».
Al medirlo, **no** aparece en el texto que se lee primero:

```
INFO 66842 --- [mi-primera-app] [main] cl.dgt.hola.HolaMundoApplication : Starting HolaMundoApplication using Java 25.0.4 …
```

`Starting HolaMundoApplication` es el nombre de la **clase** y no cambia nunca. El nombre de la
**aplicación** sale en el corchete, `[mi-primera-app]`, en todas las líneas. Un alumno que cambie el
nombre y mire donde parecía natural concluye que no funcionó. El guion enseña esa distinción a
propósito, con las dos cosas señaladas en la misma línea.

### 7.2 · En Boot 4.1, `server.error.include-*` se acepta y no hace nada

Escrito así, el `application.yml` del Lab 03 arrancaba sin una queja y la respuesta seguía siendo la
escueta de siempre:

```json
{"timestamp":"…","status":500,"error":"Internal Server Error","path":"/productos/99"}
```

Con **el único cambio** de mover esas mismas claves a `spring.web.error`, la misma petición al mismo
proyecto devolvió el mensaje, la clase de la excepción y las cuarenta líneas de traza. Los metadatos
de Boot 4.1 declaran **los dos** espacios (`server.error.include-*` y `spring.web.error.include-*`),
así que no hay aviso de propiedad desconocida: el nombre viejo se traga en silencio.

Es el mismo defecto que se corrigió antes en el Lab 01, donde
`logging.level.org.springframework.boot.web.embedded.tomcat` no imprimía el puerto porque en Boot 4
`TomcatWebServer` vive en `org.springframework.boot.tomcat` — verificado abriendo el jar. **Dos
líneas de configuración decorativa en un día**, las dos encontradas solo porque se comprobó que
hicieran lo que decían.

### 7.3 · El handler general se comía los 404 de ruta inexistente

Lo destapó la **sonda de arranque del arnés**, que pide `GET /` para saber si el servidor ya
contesta. En el log de `solucion/` apareció esto, sin que ninguna prueba lo buscara:

```
ERROR --- c.d.e.exceptions.ManejadorDeErrores : Error no previsto atendiendo una petición
org.springframework.web.servlet.resource.NoResourceFoundException: No static resource  for request '/'.
```

`NoResourceFoundException` es una `Exception`, así que `@ExceptionHandler(Exception.class)` la
atrapaba y **cualquier URL mal escrita devolvía 500 en vez de 404**. En un lab que trata precisamente
de dar forma a los errores, eso no se podía dejar.

Se añadió un cuarto handler para `NoResourceFoundException` → 404, y —esto es lo importante— **el
hallazgo se convirtió en el cierre del paso 5**: el guion hace que el alumno lo reproduzca (pide
`/noexiste`, obtiene 500), lo arregle, y se lleve la moraleja:

> Una red que atrapa todo atrapa también lo que no debía. Después de poner un handler general hay que
> volver a probar los caminos que ya funcionaban.

### 7.4 · Observación lateral, no tocada: dos `mvnw` distintos en `main`

Al copiar la maleta apareció que en `main` conviven **dos shims**: los labs 01–07 llevan el de la
SPEC-022/024 (el que usa el JDK embebido), y los labs 08–14 todavía llevan el **wrapper original de
Apache**, el que descarga Maven de internet al primer uso. Los cuatro labs nuevos se hicieron con el
bueno, copiado de `lab-01-del-otro-lado-del-boton/solucion`.

No se tocó nada: está fuera del alcance de esta SPEC y los labs 08–11 están en vuelo en el PR #31.
**Queda anotado para el PO**, porque si un alumno llega al Lab 08 en una máquina sin internet, ese
wrapper es un muro.

---

## 8 · Lo que queda

- **PR #34 en draft, esperando la firma del PO.** No se mergea nada sin ella.
- **La prueba de aceptación del PO es V4**: sentarse con `PASOS.md` y `practica/` y llegar al final
  sin abrir `solucion/`. Es la única que no puede hacer el mocito por definición — quien escribió el
  guion no puede juzgar si se entiende.
- **Tiempos de sesión estimados, no medidos.** La tabla de §2 sale de contar pasos y arranques, no de
  cronometrar una clase. El primer pase real los va a corregir.
- **La observación de §7.4** (los dos `mvnw`) espera decisión del PO.
- Sigue abierto de antes: **PR #31** (SPEC-025, labs 08–11) y **PR #33** (SPEC-027 + anotaciones A1),
  los dos en draft esperando firma, y la anotación **A2.4** de la SPEC-024.
