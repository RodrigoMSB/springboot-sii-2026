# SPEC-011 · Lab 05 «Once segundos»

| Campo | Valor |
|---|---|
| ID | SPEC-011 |
| Título | Quinto laboratorio: el clímax — el N+1 medido, no contado (S05 · M5 1,0 h + M6 1,5 h + M7 0,5 h) |
| Autor | Arquitecto |
| Aprueba | PO (Rodrigo) — **APROBADA** |
| Depende de | SPEC-010 |
| Estado | LISTA PARA EJECUCIÓN |

> **Instrucción de ejecución (mocito):** guardar este archivo íntegro en
> `docs/specs/SPEC-011-lab05-once-segundos.md` y commitearlo en rama antes de ejecutar.
> Apila sobre la pila y decláralo. Protocolo de dos etapas vigente.

---

## §0 · Resoluciones previas

1. **El cinturón de los `rm` (pendiente por segunda vez):** todo borrado con variable
   en los scripts del repo (derivación, limpieza, `bin/`) se niega a correr si la
   variable está vacía o no apunta dentro del árbol esperado. Aplícalo y cita un
   ejemplo del patrón usado. La alarma de la herramienta no debe volver a preguntar.
2. **El caveat del `@OneToOne` inverso (tu hallazgo 2 de la SPEC-010): se INCLUYE en
   las mediciones, no se excluye.** Esas consultas extra son N+1 real. La lección se
   enuncia así en la teoría: *"declaraste LAZY y un rincón de Hibernate igual te
   traiciona — por eso se mide, no se confía."* Nada de bytecode enhancement en este
   curso: se nombra como salida avanzada, no se instala.

## §1 · Objetivo

Que exista `labs/lab-05-once-segundos/`: **el clímax del curso.** El alumno sale con:
el N+1 vivido con cronómetro, medido con un contador de consultas, y resuelto con
`@EntityGraph` y proyecciones; la paginación expuesta correctamente; y su primera
prueba de integración completa con RestTestClient + Testcontainers. Encadenamiento:
`starter/` = `solucion/` del Lab 04 + el listado ingenuo + los huecos.

Este lab implementa **P-16 en su forma canónica**: `solucion-con-n1/` y `solucion/`
conviven en el repo. Ambas pasan la suite funcional completa; **solo la segunda pasa el
contador de consultas.** Esa convivencia ES la definición ejecutable de optimizar: mismo
comportamiento, distinto costo, y un test que distingue lo que el ojo no.

## §2 · El crimen

El `starter/` trae `GET /api/v1/tramites` — un listado inocente que itera trámites y
arma su resumen tocando las relaciones (contribuyente, F29, folio). Con la semilla
normal, responde al tiro. El guion del relator (10 min):

1. `./bin/start-lab.sh --lotes 50000` (bandera de sabotaje P-04; la cifra final de
   lotes y el mecanismo de siembra masiva son a tu juicio — el criterio es que el
   listado ingenuo **duela en segundos de reloj** en una máquina normal). El WARN
   condicional: `[WARN] Con 50.000 lotes, el listado ingenuo hará miles de consultas.
   Es el escenario de la Guía 02.`
2. `time curl .../api/v1/tramites` proyectado. La espera. En vivo. La sala mirando el
   cursor parpadear.
3. El contador: el `90` (o un `bin/medir.sh`, a tu juicio) imprime **cuántas consultas
   SQL costó esa petición**.
4. Carolina, canónica (SPEC-000 §9): *"Ayer el listado tardó once segundos. Hoy,
   veintitrés. No agregamos código: agregamos trámites. No quiero oír la palabra
   'optimizar' hasta que me muestres un número."*

## §3 · Los tres actos

- **Acto 1 · Choque:** los segundos y el número (miles de consultas por UNA petición).
- **Acto 2 · El parche bruto que FUNCIONA:** volver las relaciones a EAGER. ¡El
  contador baja! ¡Una consulta! La guía confronta con el número completo: una consulta
  **gigante** — el producto cartesiano, la memoria, y el peaje pagado por TODOS los
  endpoints que tocan la entidad, la necesiten o no (AU-04 además lo caza: el guardián
  del Lab 04 impide institucionalizar el parche). El acto 2 enseña que "menos
  consultas" no es la métrica; es "las consultas correctas para ESTA pantalla".
- **Acto 3 · La forma correcta:** `@EntityGraph`/`JOIN FETCH` **dirigido** al caso de
  uso (la pista del Lab 04 se cobra: *"esta es la pregunta que no te habías hecho"*),
  proyección para el listado (no viajan columnas que nadie pinta) y paginación real.

## §4 · Los TODOs (4 × ~15 min)

1. **TODO_1 — El contador:** completar el test del enunciado que **cuenta consultas**
   (andamio con el mecanismo de conteo YA construido — Hibernate `Statistics` o proxy
   del DataSource, a tu juicio, con Javadoc extenso; el alumno escribe el presupuesto y
   la aserción: *el listado paginado cuesta ≤ N consultas*). Es el arquetipo de la
   pieza 2 de SPEC-000 §7.4: no se aprueba tecleando más código.
2. **TODO_2 — `@EntityGraph`:** el listado carga lo que el resumen necesita en la
   consulta principal. El contador pasa. La suite funcional no cambia — eso es
   refactorizar (P-16).
3. **TODO_3 — Proyección + paginación expuesta:** proyección (interfaz o record) para
   el listado, y respuesta paginada propia (`PaginaDto`) que no filtra la estructura
   interna de `Page` (compromiso del temario, M5).
4. **TODO_4 — La integración completa (M6):** prueba con **RestTestClient** contra
   Testcontainers vía `@ServiceConnection`, con estrategia de datos propia (builder del
   enunciado): crea trámites, pagina, verifica el shape del JSON. Su primera IT de
   punta a punta escrita por él.

M7 (0,5 h) entra **solo en teoría**: qué es el proxy transaccional y la transacción de
solo lectura en los listados (aplicada en el andamio, señalada, no tecleada). El plato
fuerte de M7 es el Lab 06.

## §5 · Anatomía

La de SPEC-000 §7.6 completa, más `solucion-con-n1/` (P-16). El `90` exige: **ambas
soluciones verdes en lo funcional, solo `solucion/` verde en el contador** — y lo dice
con esas palabras en su salida. `TEORIA.md`: N+1 (detección, medición, `@EntityGraph`
vs `JOIN FETCH`, cuándo cada uno), proyecciones, paginación; M6 completo (pirámide,
slices, la mentira del dialecto H2 — **con el caso real de este repo**: nuestras
migraciones son PostgreSQL puro y H2 ni arranca, mejor argumento imposible —,
Testcontainers 2 y el renombre de coordenadas, `@ServiceConnection`, RestTestClient,
estrategia de datos); el caveat del `@OneToOne` inverso incluido en las cuentas (§0.2).
**Siembra del Lab 06:** *"el listado ya vuela y tienes un número que lo prueba. La
próxima semana, dos contribuyentes aprietan 'emitir' al mismo tiempo… y se llevan el
mismo folio. El cronómetro se cambia por un fiscalizador."* Plantillas con trampa
registrada y la transcripción natural: **la salida del contador antes/después, pegada
tal cual** (la pregunta de criterio: *"el acto 2 bajó el contador a 1 — ¿por qué igual
estaba mal?"*).

## §6 · Verificación del ejecutor (etapa 1)

Citado, sobre estado limpio: (1) `91-e2e` ×2 — ahora con **tres** estados: starter
falla contador y TODOs; `solucion-con-n1/` pasa funcional y falla contador (citado con
el número); `solucion/` pasa todo; (2) el crimen cronometrado: `--lotes N` sembrado,
`time curl` citado con segundos reales de tu máquina y el conteo de consultas del
starter vs solución; (3) el WARN condicional de `--lotes` aparece sobre el umbral y no
bajo él; (4) acto 2 medido: EAGER en copia → contador=1 citado → AU-04 rojo citado (el
guardián impide el parche); (5) manifiesto discrimina; (6) `deriva` (eslabones nuevos,
incluida `solucion-con-n1/` en el mecanismo — declara cómo) y `siembra` (audita L4→L5)
verdes en el runner; (7) CI verde, run citado; (8) `ESTADO.md` al día; (9) §0.1
aplicado con ejemplo del patrón; estimación honesta por TODO.

## §7 · La Prueba del PO (etapa 2 — cierra la SPEC)

Invitación literal, resultados esperados escritos, tres pasos: (1) sembrar con
`--lotes`, `time curl` al listado del starter — **sentir los segundos** y ver el número
de consultas; (2) lo mismo en `solucion/` — el instante y el número chico; (3) `90
--dir solucion` → aprobado, citando la línea que distingue a las dos soluciones.
Declara Java/Docker por paso y el tiempo estimado de la siembra masiva. Recuerda la
fila de pruebas acumuladas del PO.

## §8 · Criterios de aceptación

- [ ] §0 ejecutado (cinturón de `rm` con ejemplo; caveat incluido en las cuentas).
- [ ] SPEC commiteada antes del material; rama + PR apilado y declarado.
- [ ] Lab 05 completo con `solucion-con-n1/` (P-16 canónica); evidencia §6 íntegra.
- [ ] El contador de consultas es determinista (mismo número en corridas repetidas —
      cítalo ×2) y el presupuesto está justificado en el Javadoc del enunciado.
- [ ] TODOs ≤ ~15 min c/u (o recorte aplicado y declarado).
- [ ] Siembra L4→L5 auditada; Lab 05 siembra el 06 con el gancho del fiscalizador.
- [ ] `ESTADO.md` y bitácora al día.
- [ ] **Prueba del PO reportada** — cierra la SPEC.
- [ ] Commits `SPEC-011:`; checks verdes citados.

## §9 · Reporte

Evidencia de §6 (segundos y números incluidos), tiempos por TODO, decisiones declaradas
(mecanismo de conteo, cifra de lotes, integración de `solucion-con-n1/` en `deriva`),
URL del run, `git log --oneline`, discrepancias y hallazgos — sin tocarlos. Cierra con
la invitación del §7.
