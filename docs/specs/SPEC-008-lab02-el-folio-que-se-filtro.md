# SPEC-008 · Lab 02 «El folio que se filtró»

| Campo | Valor |
|---|---|
| ID | SPEC-008 |
| Título | Segundo laboratorio: la filtración de datos y la instalación de los guardianes (S02 · M2 1,5 h + M3 1,5 h) |
| Autor | Arquitecto |
| Aprueba | PO (Rodrigo) — **APROBADA** |
| Depende de | SPEC-007 |
| Estado | LISTA PARA EJECUCIÓN |

> **Instrucción de ejecución (mocito):** guardar este archivo íntegro en
> `docs/specs/SPEC-008-lab02-el-folio-que-se-filtro.md` y commitearlo en rama antes de
> ejecutar. Si el PR #6 sigue abierto (espera la Prueba del PO), apila tu rama sobre él
> y decláralo. Rige el protocolo de dos etapas.

---

## §0 · Resoluciones previas

1. **Job `deriva` en CI** (tu hallazgo 1 de la SPEC-007): re-deriva `starter/` y
   `solucion/` de cada lab desde sus fuentes con `derivar-desde-tronco.sh` y compara
   contra lo versionado (`diff -r`, excluyendo `target/`). Divergencia = rojo con el
   listado de archivos. Es el problema fuente ⇄ build del temario, misma medicina.
   Nota: los archivos del crimen (plantados a propósito) deben quedar contemplados por
   el mecanismo de derivación, no como excepciones ad-hoc del diff — declara cómo.
2. **Los `*IT` en los derivados** (tu hallazgo 2): se quedan. Entrada de
   troubleshooting nueva: "corriste `./mvnw verify` y aparecieron contenedores" —
   referencia a la T-11 existente.
3. **TODO_2 del Lab 01**: confirma en el reporte que el recorte a ~15 min quedó
   aplicado en la rama del PR #6 (esqueleto escrito, el alumno completa la lógica).

## §1 · Objetivo

Que exista `labs/lab-02-el-folio-que-se-filtro/`: la sesión donde el curso muestra los
dientes. El alumno sale con: la filtración entendida y tapada con DTOs y capas, los
guardianes ArchUnit instalados **por él** (con la prueba de que muerden), y el endpoint
documentado con OpenAPI y versionado nativo. Encadenamiento: `starter/` = `solucion/`
del Lab 01 + el crimen plantado + los huecos nuevos.

## §2 · El crimen

En el `starter/`, un endpoint nuevo con cara de apuro:
`GET /api/v1/contribuyentes/{rut}/ficha` devuelve **la entidad `Contribuyente`
directamente** ("era para ayer, después lo arreglo" — el comentario está en el código).
Por el JSON viajan `claveHash`... y `puntajeRiesgoInterno`: el número con que la DGT
decide a quién fiscalizar.

Guion del relator (INSTRUCTOR.md, minutado): un `curl` proyectado, el puntaje en
pantalla, y Carolina: *"No te llamé porque hay un bug. Te llamé porque **nada lo
impidió**. Arréglalo — y después haz que sea imposible repetirlo."*

**Consistencia con el tronco:** el starter de este lab lleva la suite de arquitectura
**reducida a andamios del enunciado** (los guardianes aún no existen para el alumno:
instalarlos ES el lab). La `solucion/` restaura el 7+7 completo del tronco. El job
`deriva` debe contemplar esta transformación como parte del mecanismo, no como parche.

## §3 · Los tres actos

- **Acto 1 · Choque:** el curl. El puntaje interno, afuera.
- **Acto 2 · El parche bruto que FUNCIONA:** `@JsonIgnore` sobre los dos campos. El
  JSON sale limpio, el test manual pasa, todos contentos. La guía confronta: la entidad
  sigue siendo el contrato público — el próximo campo sensible que alguien agregue
  **nace filtrado por defecto**. Ocultar campos uno a uno es una lista negra; el
  contrato correcto es una lista blanca.
- **Acto 3 · La forma correcta:** DTO record (lista blanca), capa de servicio con
  interfaz e inyección por constructor, y los guardianes que hacen estructuralmente
  imposible el retroceso.

## §4 · Los TODOs (4 × ~15 min)

1. **TODO_1 — Tapar la filtración:** `FichaContribuyenteDTO` (record) + mapper en
   `application/`; el endpoint devuelve el DTO. Test del enunciado con la lección A-01
   heredada del tronco: `containsOnlyKeys(...)` — se exige lo permitido, no se enumera
   lo prohibido (RN-03).
2. **TODO_2 — La capa que faltaba:** la lógica sale del controlador hacia
   `FichaService` (interfaz + implementación, constructor injection). El controlador
   queda en su rol: traducir HTTP, nada más.
3. **TODO_3 — Instalar los guardianes:** el alumno escribe AU-01 y AU-02 con
   `dependOnClassesThat()` (el andamio y la pista explican por qué **no**
   `haveRawReturnType`: el genérico se escapa — la trampa está medida, spike S-1), más
   el fixture violador y su meta-test. *Un guardián sin prueba de que muerde es un
   adorno* — esa frase va en el Javadoc del andamio.
4. **TODO_4 — El contrato visible (M2):** anotaciones OpenAPI del endpoint
   (`@Operation`, `@ApiResponse`, `@Schema` del DTO) y verificación del versionado
   nativo `/api/v1/` — con Swagger UI navegable como evidencia del reporte.

## §5 · Anatomía

La de SPEC-000 §7.6 completa. Específicos: `TEORIA.md` cubre el resto de M2 (OpenAPI,
`ResponseEntity`, versionado nativo, Jackson 3) y la primera mitad de M3 (capas, DTOs,
inyección por constructor vs campo, mapeo manual vs MapStruct — criterio, no dogma);
**siembra del Lab 03**: *"los guardianes vigilan la estructura; ¿quién vigila el
comportamiento? La próxima semana, la suite llega en rojo — y los tests serán el
enunciado."* Con el Lab 02 presente, el job `siembra` del CI pasa a auditar de verdad
la TEORIA del Lab 01: cita ese check. `plantillas/` con trampa registrada y una
transcripción literal (la natural: el mensaje de ArchUnit cazando al fixture, completo).
Reporte entregable incluye la pregunta de criterio: *"¿por qué `@JsonIgnore` era
insuficiente si el JSON salía limpio?"*

## §6 · Verificación del ejecutor (etapa 1)

Citado, sobre estado limpio: (1) `91-e2e.sh` dos veces — starter virgen falla
nombrando los 4 TODOs, solución 100%; (2) el crimen es visible: curl al starter con
`puntajeRiesgoInterno` en la salida; curl a la solución con solo los campos de la lista
blanca; (3) el Acto 2 medido: aplica `@JsonIgnore` en copia y cita que el JSON sale
limpio **y** que el test del enunciado de TODO_3 (guardianes) sigue en rojo — el
parche engaña al ojo, no al guardián; (4) el meta-test del alumno muerde: fixture
presente → rojo de ArchUnit citado completo; (5) manifiesto discrimina (alterar
enunciado rompe; test propio no); (6) encadenamiento verificado: `starter/` ==
`solucion/` Lab 01 + transformaciones declaradas (el job `deriva` en verde lo
certifica); (7) Swagger UI respondiendo, captura o curl del JSON de la spec; (8) CI
cinco/seis checks verdes, run citado; (9) `ESTADO.md` al día.

## §7 · La Prueba del PO (etapa 2 — cierra la SPEC)

Invitación literal al final del reporte, resultados esperados escritos, tres pasos:
(1) el curl de la filtración en el starter — *ver el puntaje prohibido en pantalla*;
(2) el mismo curl en la solución — la lista blanca; (3) descomentar/activar el fixture
violador según instrucción exacta y correr el `90` — ver a ArchUnit cazarlo **con
nombre y apellido**. Declara qué pasos necesitan Java 25 y cuáles no, como en la
SPEC-007. La prueba pendiente del PO sobre Labs 00/01 puede despacharse en la misma
sentada; recuérdaselo con las rutas.

## §8 · Criterios de aceptación

- [ ] §0 ejecutado (job `deriva` verde, troubleshooting, recorte TODO_2 confirmado).
- [ ] SPEC commiteada antes del material; rama + PR; apilamiento declarado si aplica.
- [ ] Lab 02 completo (las 5 piezas no negociables); evidencia §6 citada íntegra.
- [ ] Estimación honesta por TODO (≤ ~15 min c/u o recorte aplicado).
- [ ] Siembra: Lab 01 auditado de verdad por el CI; Lab 02 siembra el 03.
- [ ] `ESTADO.md` al día; bitácora con su fila.
- [ ] **Prueba del PO reportada** — cierra la SPEC.
- [ ] Commits `SPEC-008:`; checks verdes citados.

## §9 · Reporte

Evidencia de §6, tiempos por TODO, cómo quedó resuelta la transformación
starter/solución dentro del mecanismo `deriva`, URL del run, `git log --oneline`,
discrepancias y hallazgos — sin tocarlos. Cierra con la invitación del §7.
