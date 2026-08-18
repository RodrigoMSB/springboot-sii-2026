# INFORME-SPEC-034 · La cobertura real, medida

**SPEC:** SPEC-034 · **Ejecuta:** mocito · **Fecha:** 18 de agosto de 2026
**Rama:** `spec-034-mapa-trazabilidad` · **Tag al cierre:** `material-v1.0.1`
**Máquina:** Mac Studio del PO (Darwin 25.5.0, `arm64`)

---

## 1 · Veredicto en una línea

**EL MAPA ESTÁ REHECHO Y DICE QUE FALTAN NUEVE TEMAS DE TREINTA Y CINCO.** De los 35 temas
contratados: **19 cubiertos, 6 parciales, 1 mencionado, 9 no cubiertos**. Por módulo: **3
cubiertos y 12 parciales**, ninguno vacío. **La brecha que manda es el tema XXXV —el proyecto
final integrador, 50 % de la evaluación contratada— que sencillamente no existe en el material:
los catorce labs son construcción guiada, sin nota.** Las otras ocho brechas están detalladas con
dónde estaban y qué haría falta; **siete son un paso dentro de un lab que ya existe**, y las tres
caras lo son porque dependen de Docker. Ningún lab fue tocado.

---

## 2 · El conteo, que es el entregable

### Por tema (I–XXXV)

| Nivel | Cuántos | Cuáles |
|---|---|---|
| **Cubierto** | **19** | I, II, III, V, VI, VII, VIII, IX, X, XIII, XIV, XVIII, XXI, XXIII, XXIV, XXVII, XXX, XXXIII, XXXIV |
| **Parcial** | **6** | XI, XII, XV, XVI, XXII, XXXI |
| **Mencionado** | **1** | XXVIII |
| **No cubierto** | **9** | IV, XVII, XIX, XX, XXV, XXVI, XXIX, XXXII, XXXV |

### Por módulo (M1–M15)

| Nivel | Cuántos | Cuáles |
|---|---|---|
| **Cubierto** | **3** | M4 Testing I · M5 Persistencia · M7 Transacciones y Optimización |
| **Parcial** | **12** | M1, M2, M3, M6, M8, M9, M10, M11, M12, M13, M14, M15 |
| **No cubierto** | 0 | — |

Regla usada, escrita para que se pueda auditar: un módulo es **Cubierto** sólo si **todos** sus
temas lo son **y** su línea de «Práctica» del temario es realizable con el material. Basta un tema
parcial para que el módulo sea Parcial. Es una regla estricta a propósito: quien lea esto es el
cliente.

---

## 3 · Las nueve brechas, en una pantalla

| Tema | Qué falta | Dónde estaba | Coste de cerrarla |
|---|---|---|---|
| **XXXV** | **Proyecto final integrador y rúbrica** | `lab-13-capsula-y-egreso` (v0.8.0) | **Alto — y es el que bloquea el contrato** |
| XXVI | Mensajería (RabbitMQ, DLQ, idempotencia) | `lab-12-amortiguadores` (v0.8.0) | Alto · **descartado por el PO**: no corre sin Docker |
| XX | Manejo de archivos (MultipartFile, MIME, streaming) | `lab-09-caja-negra` (v0.8.0) | Alto · no tiene sitio natural en el arco actual |
| XIX | AOP (`@Aspect`, auditoría transversal) | `lab-09-caja-negra` (v0.8.0) | **Bajo — un paso en el lab-11** |
| XXIX | Caché (`@Cacheable`, Caffeine, hit-rate) | `lab-10-observabilidad` antiguo (v0.8.0) | **Bajo — un paso en el lab-11** |
| XXV | Eventos de aplicación (`@TransactionalEventListener`) | `lab-11-latidos` (v0.8.0) | **Bajo — un paso en el lab-12** |
| XVII | gRPC | `lab-08-diplomacia/demo-grpc` (v0.8.0) | **Bajo — era demo del relator, no práctica** |
| XXXII | Liquibase | Nunca se practicó | **Bajo — 15 min de comparación** |
| IV | OpenAPI/Swagger y versionado nativo | Nunca en el arco nuevo | **Bajo — un paso en el lab-01** |

**Siete de las nueve se cierran con un paso dentro de un lab existente.** Las dos altas que
quedan (proyecto final y archivos) más la descartada (mensajería) son las que exigen decisión.

Y el patrón que conviene ver: **tres de las brechas son consecuencia directa de no tener Docker**
—Testcontainers (dentro del tema XXII, parcial), mensajería con RabbitMQ y Buildpacks (dentro de
M15)—. No son descuidos: son el precio pagado por que el material corra en la sala del SII.

---

## 4 · Tabla de verificación

| # | Prueba | Resultado |
|---|---|---|
| **V1** | Los 15 módulos en la tabla inversa | ✅ `módulos presentes: 15 de 15` |
| **V2** | Los 35 temas rastreados con su nivel | ✅ `temas listados: 35` · `cubren I..XXXV sin huecos: True` · suma por nivel = 35 |
| **V3** | Cada afirmación con respaldo real | ✅ **26 respaldos verificados** contra los `PASOS.md` |
| **V4** | Numeración vieja en `docs/temario/` | ✅ cero, salvo la columna «Dónde estaba» de las brechas |
| **V5** | Brechas con las cuatro columnas | ✅ `filas de brecha: 20` · `todas con 4 columnas: sí` |

### V3 · la prueba de fondo, y lo que cazó

Se escribió un verificador que extrae cada fila de la tabla de temas, separa el respaldo por lab,
y comprueba **que el paso citado exista literalmente** en el `PASOS.md` de ese lab:

```
  afirmaciones con respaldo verificadas: 26
  VEREDICTO V3: todos los respaldos apuntan a un lab y un paso que existen
```

En la primera pasada **falló uno**:

```
VI     Cubierto     lab-04 pasos 2–8; lab-06 `@Query`             lab-06 NO tiene 'Paso 8'
```

El respaldo del tema VI citaba `lab-06` **sin decir en qué paso**. La SPEC es explícita: «Sin
respaldo, no es cubierto». Se fue a buscar dónde ocurre de verdad —`ContribuyenteRepository`,
escrito en el paso 2 del lab-06— y la fila quedó
`lab-04 pasos 2–8 (derivadas); lab-06 paso 2 (@Query con JPQL)`. Es exactamente el trabajo que V3
existe para forzar.

### Cómo se midió la cobertura

No por lectura de los README, que es donde un mapa se vuelve optimista. Se hizo `grep` sobre el
**código de `solucion/`** de los 14 labs buscando cada tecnología que el temario nombra, y después
se contrastó con los pasos de `PASOS.md`. Ejemplos de lo que devolvió:

```
  @ConfigurationProperties     — NADA
  springdoc / OpenAPI          — NADA
  @PutMapping/@DeleteMapping   — NADA
  ProblemDetail                — NADA
  Testcontainers               — NADA
  @Aspect / AOP                — NADA
  MultipartFile                — NADA
  RabbitMQ / AMQP              — NADA
  @Cacheable / caché           — NADA
  Liquibase                    — NADA
  gRPC                         — NADA
  OpenTelemetry                — NADA
```

Y dos casos donde el `grep` mintió y hubo que ir al archivo:

- **Eventos de aplicación** salía como presente en `lab-06`. Era un falso positivo: el nombre de
  una clase de Hibernate en el `application.yml`
  (`StatisticalLoggingSessionEventListener: WARN`). Reclasificado a **No cubierto**.
- **Flyway** aparecía en seis labs, lo que sugería M8 cubierto. Al mirar los `PASOS.md`: en cinco
  es andamiaje que llega hecho y nadie explica. Pero en **lab-07 paso 5** el alumno **escribe una
  migración** (`V2__folio_unico_por_anio.sql`) que añade la restricción como segunda defensa. Eso
  sí es la práctica comprometida de M8 («una migración correctiva que agregue una restricción de
  integridad»), así que XXXI subió de Mencionado a **Parcial**.

---

## 5 · Transversales

- **Ningún lab fue tocado.** El diff de esta SPEC son tres archivos: el mapa, `ESTADO.md` y la
  propia SPEC, más este informe.
- **El temario contratado no se tocó**: ni el `.md` ni el `.docx`.
- **`docs/temario/README.md` no cita labs** — se comprobó; no había nada que actualizar.
- El CI no se ve afectado (esta SPEC no toca `labs/` ni `repo-maven/`), y sus cuatro jobs siguen
  en verde.

---

## 6 · Decisiones tomadas al ejecutar

### 6.a · Se mide contra la línea «Práctica» de cada módulo, no contra sus viñetas

Cada módulo del temario termina con una línea *Práctica* en cursiva que dice qué hará el alumno.
Se tomó **esa** como el compromiso, y las viñetas como el temario de la exposición. Es lo que
convierte la medición en algo verificable: «¿el alumno hace esto, sí o no?».

Consecuencia concreta: **M9 quedó Parcial** aunque su núcleo (filtros, BCrypt, JWT, roles) esté
bien cubierto, porque su práctica dice «…y endurecer la API con CORS, CSRF y cabeceras de
seguridad», y CORS y cabeceras no están. Con el criterio contrario habría salido Cubierto, y sería
optimismo.

### 6.b · Los temas se clasifican deducidos de su módulo, y se dice

El temario asigna los temas por **rangos** (`M5 · Temas V – VIII`) y **no los nombra uno a uno**.
Se verificó en el `.md` y extrayendo el texto del `.docx`: no hay lista nominal. Por lo tanto la
tabla de la §3 del mapa está **deducida del contenido de cada módulo**, y así queda escrito en el
propio documento. Inventar títulos de tema para que la tabla luciera oficial habría sido peor que
declarar la limitación.

### 6.c · El «contenido nuevo» se declara, aunque sume

El mapa tiene una §5 con lo que el material da **de más**: tres horas de testing donde el contrato
da 1,5, tres horas de concurrencia, tres de inyección de dependencias, veinte minutos de «qué es
un contenedor», y la maleta entera. Se declara porque el SII tiene que saber qué recibe — y porque
parte de ese exceso explica por qué el material llega a 42 h contra las 36 contratadas.

---

## 7 · Sorpresas y desviaciones

### 7.a · Hizo falta un cuarto nivel

La SPEC pedía tres (Cubierto / Mencionado / No cubierto). Con tres, la mayoría de los módulos
caían en un redondeo falso: M6 tiene slices de test pero no Testcontainers; M13 tiene la
resiliencia entera y de mensajería nada. Llamar a eso «Cubierto» o «No cubierto» sería mentir en
alguna de las dos direcciones.

Se añadió **Parcial**, declarado en el mapa §0 y anotado dentro de la SPEC (§7). Los tres niveles
originales siguen usándose con su significado exacto. Es una desviación del formato pedido, en la
dirección que la propia SPEC exige: no redondear.

### 7.b · La brecha más grande no es un tema de contenido, es la evaluación

Buscando el respaldo del tema XXXV apareció lo que ninguna de las SPEC anteriores había dicho en
voz alta:

```
###### ¿existe examen / proyecto final / rúbrica en el arco nuevo? ######
  NADA
  sin rubrica/ ni solucion-referencia/
```

El temario compromete `Evaluación: Proyecto final 50 % · Conocimientos 30 % · Ejercicios 20 %` y
`Aprobación: nota mínima 4,0 y 75 % de asistencia`. **El material no tiene con qué poner una
nota.** Los catorce labs son construcción guiada: nadie los aprueba ni los reprueba.

El instrumento existía —el antiguo `lab-13-capsula-y-egreso` traía rúbrica de tres ejes, guía de
defensa con respuestas calibradas y una solución de referencia— y salió del repositorio con el
arco antiguo en la SPEC-033. **Ninguna SPEC decidió eliminar la evaluación**: se fue de arrastre.

Es la única brecha que impide cerrar el contrato tal como está escrito, y por eso encabeza el mapa
y este informe.

### 7.c · El mapa anterior decía que estaba todo cubierto, y era verdad entonces

El mapa de la SPEC-FIX-02 afirmaba «los 15 módulos del temario tienen laboratorio». Era cierto
**del arco antiguo**. El arco nuevo no es una renumeración: es material distinto, escrito con otro
criterio pedagógico y bajo la restricción de correr sin Docker y sin red.

Conviene que quede dicho porque explica la diferencia entre los dos documentos: no es que el mapa
anterior estuviera mal, es que describía otro curso.

---

## 8 · Lo que queda

**Tres decisiones del PO, en orden de urgencia:**

1. **El proyecto final (§3, tema XXXV).** Recuperarlo de `material-v0.8.0` y adaptarlo al arco
   nuevo. Sin esto no hay cómo evaluar, y la evaluación es el 50 % del contrato. Es un trabajo
   acotado: el instrumento ya existe y está probado.
2. **Las siete brechas baratas.** AOP, caché, eventos, gRPC, Liquibase, OpenAPI/versionado y
   `@PreAuthorize` son **un paso cada una** dentro de labs que ya existen. Cerrarlas subiría la
   cobertura de 19 a 26 temas sin escribir un lab nuevo. Cabe en una SPEC.
3. **Las tres caras, que son la factura de no tener Docker.** Testcontainers, mensajería y
   Buildpacks. Aquí no hay solución dentro del aula: o se negocia con el SII una sustitución
   **declarada** (PostgreSQL embebido en vez de Testcontainers, Jib en vez de Buildpacks, y la
   mensajería fuera del alcance), o se consigue Docker en las máquinas. El mapa §7 lo plantea así.

**Y una advertencia de mantenimiento:** este mapa se desactualiza en cuanto una SPEC mueva
contenido entre labs. La nota final del documento lo dice, pero conviene repetirlo aquí: **si se
cierra cualquiera de las brechas, hay que volver a este archivo el mismo día.**
