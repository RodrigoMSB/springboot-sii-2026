# INFORME-SPEC-035 · Vuelve la evaluación

**SPEC:** SPEC-035 · **Ejecuta:** mocito · **Fecha:** 18 de agosto de 2026
**Rama:** `spec-035-proyecto-final` · **Tag al cierre:** `material-v1.1.0`
**Máquina:** Mac Studio del PO (Darwin 25.5.0, `arm64`) · JDK 25.0.4 y Maven 3.9.11 de la maleta

---

## 1 · Veredicto en una línea

**EL CURSO YA TIENE CON QUÉ EVALUAR.** `proyecto-final/` recupera el instrumento que la SPEC-033 se
llevó de arrastre: los tres ejes, el umbral —núcleo verde **Y** Criterio ≥ Suficiente—, el brief de
Carolina con sus bordes, la plantilla de reporte y la guía de defensa con respuestas calibradas.
**Los 20 requisitos del encargo están atados a su lab y a su paso** (V3), y la rúbrica **muerde**:
verificado que romper una línea de producción de la referencia pone su suite en rojo, en los dos
casos que la rúbrica nombra (V5). El tema XXXV pasa a Cubierto y la cobertura sube a **20 de 35**.
**Lo que sigue sin instrumento es el otro 50 % de la nota** —conocimientos 30 % y ejercicios 20 %—
y eso es decisión del PO, no trabajo pendiente del material (§8).

---

## 2 · Qué se recuperó, qué se adaptó y qué se rehízo

| Pieza | Origen (`material-v0.8.0`) | Qué se hizo |
|---|---|---|
| Los **tres ejes** y el umbral | `rubrica/rubrica-evaluacion.md` | **Conservados intactos** en su criterio. Reescritos los descriptores (§2.a) |
| Las **dos señales de alarma** | ídem | **Conservadas**, y ahora **verificadas** (V5) |
| «Lo que esta rúbrica NO evalúa» | ídem | Conservado tal cual: cantidad de código, parecido con la referencia, velocidad, uso de IA |
| El **brief de Carolina** | `brief/requerimientos-dgt.md` | Conservado su tono y sus huecos. Adaptado (§2.b) |
| La **plantilla de reporte** | `plantillas/reporte-egreso.md` | Conservadas las siete secciones. Adaptadas dos preguntas (§2.b) |
| La **guía de defensa** | `rubrica/guia-instructor.md` | Conservadas las cinco preguntas y las respuestas calibradas. Renumerados los labs que cita |
| La **solución de referencia** | `solucion-referencia/` | **Rehecha entera**: la vieja era la app canónica del arco antiguo, que ya no existe |
| `base/` | `starter/` | **Nuevo**: el antiguo derivaba del tronco retirado |

### 2.a · Lo que cambió en la rúbrica, y por qué

Los descriptores viejos citaban instrumentos que **ya no existen**: `90-validar.sh`, `91-e2e.sh`,
los siete guardianes ArchUnit, las reglas de derivación. Cada uno se sustituyó por **algo que el
relator puede comprobar en minutos**, y la rúbrica ahora dice *cómo* en una columna propia:

| Antes | Ahora | Cómo se comprueba |
|---|---|---|
| «los 7 guardianes verdes» | el controlador no conoce la entidad · el DTO es lista blanca | leer `controllers/` buscando imports de `entities/` · leer el DTO |
| «las 3 corridas del `91` no coinciden» | suite flaky | `./mvnw test` tres veces |
| «la imagen OCI no se construye» | ídem | `./mvnw package jib:buildTar` |
| «una regla ArchUnit rota» | el `puntaje_riesgo` sale en la respuesta | `curl` y buscar el campo |

Se añadió además una **§«Comprobación rápida»** con los comandos exactos —los cuatro `curl` de los
bordes y el consolidado— y lo esperable: `401 · 403 · 404 · 400` y **4 trámites, total 6.330.000**.
Diez minutos por entrega.

### 2.b · Lo que se quitó del brief

Nada de lo que el arco nuevo no enseña:

| Salió | Por qué |
|---|---|
| «la aplicación que vienes construyendo desde la sesión 1» | En el arco nuevo no hay una app continua: cada lab es independiente. Por eso existe `base/` |
| «¿Buildpacks o Dockerfile?» (reporte §4) | No se enseña Buildpacks. Sustituida por preguntas sobre secretos en la imagen y **el peso del artefacto** |
| «piensa en el Lab 10» (reporte §5) | Renumerado a **Lab 11**, que es observabilidad en el arco actual |
| El batch como entregable | Se conserva **como borde a decidir**, no como código exigible: la asincronía se enseña (Lab 12) pero no da tiempo en tres horas |

Y se **añadió** un borde nuevo, que el brief viejo no tenía: el contribuyente **sin trámites en el
período** (`78.333.333-3`, sembrado a propósito). Distinguirlo del RUT inexistente es la clase de
decisión que este examen mide, y ahora hay un dato que lo provoca.

---

## 3 · Tabla de verificación

| # | Prueba | Resultado |
|---|---|---|
| **V1** | `base/` arranca limpio | ✅ |
| **V2** | La referencia resuelve el brief | ✅ 10 tests · los 5 bordes · imagen OCI |
| **V3** | Cada requisito ↔ el lab que lo enseñó | ✅ **20 de 20**, verificado contra los `PASOS.md` |
| **V4** | Cada descriptor ↔ cómo se comprueba | ✅ columna propia en los ejes 1 y 2; el eje 3 es la defensa |
| **V5** | Las dos señales de alarma | ✅ **las dos muerden**, citado abajo |
| **V6** | `git status` | ✅ `instructor/` invisible (hubo que añadir la regla) |
| **V7** | Offline | ✅ 0 descargas |
| **V8** | Tiempo estimado | ✅ **2 h 30 – 3 h 00**, con su base |

### V1 · `base/` arranca, con el encargo ausente

```
Successfully applied 1 migration to schema "public", now at version v1
Tomcat started on port 8107 (http)
Started ConsolidadoApplication in 4.103 seconds

POST /auth/login              -> {"token":"eyJraWQiOiJiLXJJdDBSOENUTXdnejVPX2Nic0JzUFU1..."}
GET  /actuator/health         -> 200
GET  /consolidados/76.111.111-1 -> 404   (no existe el endpoint: es lo que hay que escribir)
```

### V2 · La referencia resuelve el brief

```json
{ "rut": "76.111.111-1", "razonSocial": "Comercial Andes Ltda.",
  "desde": "2026-01-01", "hasta": "2026-12-31",
  "cuantosTramites": 4, "totalDelPeriodo": 6330000.0,
  "tramites": [ … ] }
```

**Los cinco bordes:**

```
  sin token                                  401
  luis (CONTRIBUYENTE)                       403
  RUT inexistente                            404
  sin período                                400
  contribuyente SIN trámites en el período   200   (total 0, no 404)
```

El total excluye los dos trámites de 2025 (500.000 y 2.100.000): 1.200.000 + 950.000 + 3.400.000 +
780.000 = **6.330.000**. Y `puntajeRiesgo` no aparece en la respuesta.

**Su suite, tres corridas seguidas:**

```
  corrida 1: Tests run: 10, Failures: 0, Errors: 0, Skipped: 0
  corrida 2: Tests run: 10, Failures: 0, Errors: 0, Skipped: 0
  corrida 3: Tests run: 10, Failures: 0, Errors: 0, Skipped: 0
```

**El empaquetado:** `./mvnw package jib:buildTar` → `target/jib-image.tar`, **281,8 MB**, 10 capas,
`Entrypoint: java -cp @/app/jib-classpath-file cl.dgt.consolidado.ConsolidadoApplication`, puerto
8108 y `SPRING_PROFILES_ACTIVE=prod`. Sin Docker y sin red.

### V3 · los 20 requisitos, cada uno con su lab y su paso

Verificado con un comprobador que exige que el paso citado exista literalmente en el `PASOS.md`:

```
Endpoint REST con @PathVariable y @RequestParam  lab-01-web           pasos 2,3    ok
DTO con record (lista blanca)                    lab-01-web           paso 4       ok
Capa de servicio e inyección por constructor     lab-02-di            pasos 2,6    ok
Excepción de dominio propia                      lab-03-errores       paso 2       ok
@RestControllerAdvice y el 404 con cuerpo        lab-03-errores       paso 3       ok
El 400 de la petición mal formada                lab-03-errores       paso 4       ok
Entidad JPA y repositorio                        lab-04-jpa           pasos 1,2    ok
Consulta derivada (findByRut)                    lab-04-jpa           paso 5       ok
Relación @ManyToOne / @OneToMany                 lab-05-relaciones    pasos 1,3    ok
Consulta que cruza la relación                   lab-05-relaciones    paso 6       ok
@Query con JPQL                                  lab-06-rendimiento   paso 2       ok
No traer de más: el total en su consulta         lab-06-rendimiento   pasos 1,4    ok
@Transactional(readOnly)                         lab-07-concurrencia  paso 1       ok
Tests unitarios con Mockito                      lab-08-testing       paso 4       ok
@WebMvcTest + MockMvc + jsonPath                 lab-08-testing       paso 5       ok
@SpringBootTest para el cableado real            lab-08-testing       paso 6       ok
Rol por ruta, 401 y 403                          lab-09-seguridad     paso 6       ok
JWT (viene resuelto en base/, se usa)            lab-09-seguridad     pasos 4,5    ok
Empaquetar: jar por capas e imagen OCI           lab-13-empaquetado   pasos 2,4    ok
Perfil productivo y secretos fuera               lab-13-empaquetado   paso 5       ok

V3: todos los requisitos tienen su lab y su paso
```

**No se evalúa nada que no se haya enseñado.**

### V5 · las dos señales de alarma, reproducidas

**1 · Una prueba que no puede fallar.** Se quitó el filtro de período de la consulta del total —una
línea— y se corrió la suite de la referencia:

```
org.opentest4j.AssertionFailedError: el total se salió del período ==> expected: <0> but was: <-1>
[ERROR]   ConsolidadoIntegracionTest.elTotalDe2026NoArrastraLosTramitesDe2025:32
[ERROR] Tests run: 7, Failures: 1
```

Restaurada: `Tests run: 7, Failures: 0 · BUILD SUCCESS`.

Y el segundo caso, la regla de rol:

```
java.lang.AssertionError: Status expected:<403> but was:<200>
[ERROR]   SeguridadDelConsolidadoTest.unContribuyenteAutenticadoNoPuedeVerConsolidados:37
```

**Las dos muerden.** La rúbrica cita ambos resultados para que el relator sepa que la técnica
funciona antes de aplicarla a una entrega.

**2 · Suite flaky.** No hizo falta simularla: **le pasó a la referencia mientras se construía**
(§7.a).

### V8 · el tiempo, y en qué se basa

**Estimación: 2 h 30 – 3 h 00** para un alumno que completó el arco. Base de la estimación:

| Parte | Medido en la referencia | Estimado |
|---|---|---|
| Producción (6 archivos) | **163 líneas** | 45–60 min |
| Tests (4 clases) | **236 líneas** | 60–75 min |
| La regla de rol | 1 línea | 2 min |
| Construir la imagen | — | 5 min |
| Leer brief y rúbrica | — | 15 min |
| El reporte (7 secciones) | — | 20–30 min |

**Cabe, y va justo.** Dos cosas lo descomprimen y están en el diseño: `base/` entrega resuelto todo
lo que no se evalúa (dominio, datos, autenticación), y el brief dice explícitamente **«tus pruebas
— tú decides cuáles y cuántas»**: la referencia lleva cuatro clases porque es una referencia, no
porque sean el mínimo. Con dos clases el total baja a unas 280 líneas y ~2 h 15.

**No se recortó el brief.** El aviso de la SPEC («si el trabajo se dispara, recortar y reportar») no
se activó: 399 líneas en tres horas, con el andamiaje dado y el dominio ya conocido de los labs 05,
06 y 07, es exigente y no desmedido.

---

## 4 · Transversales

- **Ningún lab fue tocado.** El diff son `proyecto-final/`, `.gitignore`, el mapa, `ESTADO.md`, la
  SPEC y este informe.
- **`.gitignore`**: hubo que **añadir** `proyecto-final/instructor/`. La regla existente era
  `labs/*/instructor/` y no cubría esta ruta — se detectó con `git check-ignore` antes de commitear
  (V6). Sin eso, el examen habría viajado con las respuestas dentro.
- **El mapa de trazabilidad se actualizó el mismo día**, como pedía la nota final de la SPEC-034:
  XXXV pasa a Cubierto, el recuento a **20 / 6 / 1 / 8**, y la §4.1 se reescribió como «medio
  instrumento puesto, medio por definir».
- CI: esta SPEC no toca `labs/` ni `repo-maven/`; el job `labs` no la ve. Los cuatro jobs siguen
  verdes.

---

## 5 · Decisiones tomadas al ejecutar

### 5.a · Qué entrega `base/` resuelto, y por qué la autenticación entera

`base/` trae el dominio, los datos sembrados y **la autenticación JWT completa** (login, emisión,
validación, cadena de filtros exigiendo token). Lo que **no** trae es una línea del encargo.

El criterio: **tres horas se van en el encargo, no en volver a teclear el Lab 09.** Retecleando la
infraestructura de seguridad se irían 40 minutos en algo que ya se evaluó en su lab. Lo que sí se
evalúa hoy es la **decisión de quién puede ver el consolidado** — una línea en la cadena, y saber
por qué va ahí.

### 5.b · El dominio es el que el alumno ya conoce

`contribuyente` + `tramite` son las dos tablas de los labs 05, 06 y 07, con dos añadidos: `monto`
(para que haya un total que calcular) y `puntaje_riesgo` (el dato interno que no debe salir, que es
el incidente que Carolina nombra en el brief).

Reutilizar el dominio no es pereza: el alumno no gasta ni un minuto en entender de qué va la
aplicación, y lo gasta entero en el encargo.

### 5.c · Los datos están sembrados para provocar los bordes

- **Ocho trámites en dos años** → el filtro de período importa, y el total mal filtrado da un
  número distinto (8.930.000 en vez de 6.330.000).
- **Un contribuyente sin trámites** (`78.333.333-3`) → el borde «no existe» contra «no tiene nada».
- **Dos usuarios con la misma clave y distinto rol** → 403 contra 401 se prueba sin preparar nada.

---

## 6 · Sorpresas y desviaciones

### 6.a · La referencia salió flaky, y es la mejor calibración que tiene la rúbrica

Al añadir el test de integración, la suite empezó a comportarse así:

```
  ConsolidadoControllerTest sola          → 3 tests, 0 fallos
  ConsolidadoControllerTest con las demás → 3 errores
```

**Causa:** el `@Bean` del PostgreSQL embebido vivía en la clase `@SpringBootApplication`. Los slices
`@WebMvcTest` cargan esa clase como configuración raíz, así que levantaban un **segundo** motor en
el mismo puerto. Y una segunda vez, por lo mismo: `@SpringBootTest` y
`@SpringBootTest + @AutoConfigureMockMvc` son **dos configuraciones distintas** → dos contextos →
dos motores.

**Arreglado** moviendo la configuración de la base a una `@Configuration` propia
(`infra/BaseEmbebida`) e igualando las anotaciones de los dos `@SpringBootTest`. Tres corridas
idénticas después.

Lo valioso: **le pasó exactamente lo que la rúbrica castiga como Insuficiente en Oficio.** Está
escrito en la rúbrica —«si le pasó a la referencia, le va a pasar a alguien»— y en la nota del
instructor, con la causa. Un criterio que el relator ya vio ocurrir lo aplica mejor que uno que sólo
leyó.

### 6.b · Los tests unitarios no habrían cazado una consulta rota

Al preparar V5 apareció que el test de servicio **mockea el repositorio** y el de controller
**mockea el servicio**: una JPQL mal escrita pasaba por delante de los dos sin que nada fallara.

Por eso la referencia lleva `ConsolidadoIntegracionTest` con `@SpringBootTest` contra la base
sembrada — y es justamente el test que V5 pone en rojo. Es también el contenido del descriptor
Destacado de Correctitud: «el total no se infla **y hay una prueba que lo demuestra**».

(Se resolvió con `@SpringBootTest`, que el Lab 08 sí enseña, y no con `@DataJpaTest`, que está en su
«lo que no vimos hoy». No se usa lo que no se enseñó, ni siquiera en la referencia.)

### 6.c · El artefacto pesa 281 MB, y se deja así a propósito

El jar de la referencia pesa **168 MB** y la imagen **281 MB**. La causa, medida:

```
  binarios de PostgreSQL dentro del jar: 107.3 MB
    embedded-postgres-binaries-darwin-amd64        linux-amd64
    embedded-postgres-binaries-linux-amd64-alpine  windows-amd64
```

El PostgreSQL embebido arrastra los binarios de **cuatro plataformas** que dentro de una imagen
Linux no se usan nunca.

**No se arregló, y es una decisión.** Arreglarlo bien (perfil que sólo cargue la base embebida fuera
de producción, o excluir los binarios del empaquetado) es media hora que el alumno no tiene, y no es
el tema del examen. En cambio **se convirtió en materia evaluable**: el reporte §4 le pregunta si el
tamaño le parece razonable y qué sobra, y la guía de defensa lo marca como uno de los dos bordes que
valen Destacado. Es un problema real, de los que aparecen en cualquier proyecto, y verlo sin que te
lo señalen es exactamente lo que este examen mide.

### 6.d · La revisión de seguridad encontró un fail-open real, y tenía razón

La revisión automática del commit marcó tres cosas en `proyecto-final/base`. Dos eran
intencionales y declaradas —el valor por defecto del secreto en el perfil de **laboratorio**, y los
hashes BCrypt de los dos usuarios sembrados, que el brief publica—. **La tercera era un defecto de
verdad:**

```yaml
# application-prod.yml, como estaba
secreto: ${DGT_JWT_SECRETO:CAMBIAME-en-produccion-32-bytes-minimo-o-no-arranca}
```

El comentario decía «o no arranca». **Arrancaba.** En producción, sin la variable, la aplicación
firmaba tokens con una clave escrita en el repositorio: cualquiera que leyera el repo podía
fabricarse un token de FISCALIZADOR, y nada fallaría — que es la peor forma de fallar.

Y lo grave no es sólo el agujero: es que **contradecía lo que este mismo artefacto enseña**. El
reporte pregunta al alumno «¿dónde viven los secretos de tu imagen?», y el Lab 13 dedica una nota a
que el respaldo de un valor de producción debe ser visiblemente falso para que el olvido se note.

**Corregido a fail-closed**, sin respaldo:

```
PlaceholderResolutionException: Could not resolve placeholder 'DGT_JWT_SECRETO'
arranques: 0
```

Con la variable puesta arranca y el login responde 200. El perfil de laboratorio conserva su valor
por defecto, que ahí sí es correcto: el proyecto tiene que arrancar sin configurar nada.

Y se convirtió en **descriptor comprobable** del eje Oficio: «el perfil productivo arranca con un
secreto por defecto» pasa a Insuficiente, y la rúbrica dice cómo se comprueba
(`SPRING_PROFILES_ACTIVE=prod ./mvnw spring-boot:run` sin la variable: debe negarse). El material no
puede castigar en la rúbrica lo que su propia base hacía.

**Un aviso de método, porque casi me lo trago:** la primera comprobación del fail-closed dio
`BUILD FAILURE` y la di por buena. No era el placeholder: era el error de versión de Java que
aparece al envolver `./mvnw` en `timeout` —la trampa que `docs/decisiones.md` ya documenta, porque
bajo Rosetta el shim cree estar en un Mac Intel y cae al Java del sistema—. Un falso verde con
forma de rojo. Se repitió sin `timeout` y ahí sí salió el mensaje real.

### 6.e · Un fósil de la SPEC-033 que no se tocó

`lab-04-jpa` conserva el título **«Lab 3b · JPA»** en su `README.md`, su `PASOS.md` y la
`<description>` de sus dos `pom.xml`. Es un resto de la renumeración de la SPEC-033: el mapa de
números convertía `03b → 04`, pero el título estaba escrito «3b» y no encajó con el patrón.

**No se corrigió**: esta SPEC prohíbe tocar los labs, y la corrección no es urgente. Queda anotado
para una `SPEC-FIX` — son cuatro cadenas de texto.

---

## 7 · Lo que queda, y es del PO

### 7.a · El otro 50 % de la evaluación no tiene instrumento

Esto es lo que el PO necesita para hablar con el SII:

| | Peso | Instrumento | Estado |
|---|---|---|---|
| **Proyecto final** | **50 %** | `proyecto-final/` | ✅ **puesto y verificado** |
| Evaluación de conocimientos | **30 %** | — | ❌ **no existe** |
| Ejercicios | **20 %** | — | ❌ **no existe** |

Los catorce labs son **construcción guiada y no llevan nota**: no sirven como «ejercicios evaluados»
sin definir antes qué se puntúa y con qué escala. Y no hay ninguna prueba de conocimientos.

**No es trabajo pendiente del material: es una decisión.** Con el 50 % puesto ya se puede certificar
la mitad que más pesa, pero hasta que esas dos casillas se llenen, el curso **no puede poner una
nota final completa** en los términos del contrato. Está dicho en el `README.md` del proyecto, en
`ESTADO.md` y en el mapa §4.1 — para que nadie lo descubra el día de la entrega, que es como se
descubrió esta brecha.

### 7.b · Lo demás

- **Las ocho brechas de contenido** siguen abiertas (mapa §4.2): gRPC, AOP, archivos, eventos,
  mensajería, caché, Liquibase y OpenAPI. Siete son un paso dentro de un lab existente.
- **`instructor/` del proyecto no está respaldada por Git**, por diseño. La `NOTA.md` de dentro dice
  qué contiene y cómo verificarla.
- **El fósil «Lab 3b»** (§6.e), para una `SPEC-FIX`.
- **La fila de aceptación del PO** sigue pendiente, y ahora incluye una prueba más: **resolver el
  proyecto final** con el brief delante, sin mirar la referencia. Es la única forma de saber si tres
  horas alcanzan de verdad — mi estimación es un cálculo, no una medición con un alumno.
