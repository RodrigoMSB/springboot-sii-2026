# SPEC-012 · Lab 06 «Dos folios, un número»

| Campo | Valor |
|---|---|
| ID | SPEC-012 |
| Título | Sexto laboratorio: el folio duplicado bajo concurrencia, y las restricciones como contratos (S06 · M7 1,5 h + M8 1,5 h) |
| Autor | Arquitecto |
| Aprueba | PO (Rodrigo) — **APROBADA** |
| Depende de | SPEC-011 |
| Estado | LISTA PARA EJECUCIÓN |

> **Instrucción de ejecución (mocito):** guardar este archivo íntegro en
> `docs/specs/SPEC-012-lab06-dos-folios-un-numero.md` y commitearlo en rama antes de
> ejecutar. Apila sobre la pila y decláralo. Protocolo de dos etapas vigente.

---

## §1 · Objetivo

Que exista `labs/lab-06-dos-folios-un-numero/`: la sesión donde **RN-01, RN-02 y RN-05
por fin tienen suelo** (el hueco anotado desde la SPEC-005). El alumno sale con: la
emisión de folios construida con contador bloqueado, idempotente por `tramiteId`,
probada bajo concurrencia real; y su primera migración correctiva con la lección
"restricciones como contratos" (el `CHECK` reservado desde la V1). Encadenamiento:
`starter/` = `solucion/` del Lab 05 + la emisión ingenua + los huecos.

## §2 · El crimen

El `starter/` trae `POST /api/v1/tramites/{id}/folio` — una emisión ingenua: lee el
contador, suma uno, guarda. Con un usuario, perfecta. Guion del relator (10 min):

1. `./bin/start-lab.sh --concurrencia 2` (bandera P-04: el script dispara N emisiones
   **en paralelo** sobre trámites distintos; mecanismo a tu juicio — el criterio es que
   el duplicado aparezca de forma reproducible, con reintentos del sabotaje si la
   carrera no sale a la primera).
2. En pantalla: **dos trámites distintos, el mismo número de folio.**
3. Carolina, canónica: *"Un folio emitido dos veces no se borra. Se explica. Ante un
   fiscalizador. Y de paso: el folio 8 no existe — ¿dónde está? Los folios no se
   saltan. Esto no es una tabla más: es un libro foliado."*

## §3 · Los tres actos

- **Acto 1 · Choque:** el duplicado en pantalla, y el salto en la secuencia.
- **Acto 2 · Los parches brutos que FUNCIONAN (dos, en escalera):**
  a) `synchronized` en el método de emisión. ¡Funciona! El sabotaje local pasa. La guía
  confronta: funciona **en UNA instancia** — el Lab 10 correrá dos, y la JVM no
  sincroniza con la JVM del vecino. El candado está en el lugar equivocado: en el
  código, no en el dato.
  b) `REQUIRES_NEW` para "aislar" la toma del número. Parece elegante… y cuando la
  transacción externa revierte, **el número ya se gastó**: saltos en el libro foliado
  (RN-02 violada). El test del enunciado lo demuestra (resolución técnica de SPEC-000
  §5, que este lab paga).
- **Acto 3 · La forma correcta:** `SELECT … FOR UPDATE` sobre `contador_folio` **en la
  misma transacción** que persiste el folio (bloqueo pesimista dirigido: el candado
  vive en el dato), con el `UNIQUE` de la V1 como red final — la defensa en
  profundidad: si el código falla, la base no perdona.

## §4 · Los TODOs (4 × ~15 min)

1. **TODO_1 — El contador bloqueado:** la emisión con `PESSIMISTIC_WRITE` (o query
   `FOR UPDATE`, a tu juicio — declara) en la misma transacción. Test del enunciado de
   concurrencia real: N hilos emiten sobre N trámites (ExecutorService + latch, cero
   `sleep` — AU-05 vigila), y el resultado exige **folios únicos Y secuencia sin
   saltos** (RN-01 + RN-02 en una sola aserción doble).
2. **TODO_2 — La idempotencia (RN-05):** mismo `tramiteId` → mismo folio; primera
   emisión **201**, reintentos **200 con el mismo cuerpo**. Test del enunciado que
   emite dos veces y compara. El `UNIQUE (tramite_id)` de la V1 deja de ser adorno.
3. **TODO_3 — La restricción como contrato (M8):** migración `V3__check_montos.sql`
   con `CHECK (monto >= 0)` en `linea_f29` — la lección reservada a propósito desde la
   SPEC-005. Test del enunciado que intenta persistir un monto negativo **saltándose la
   validación de Java** (inserción directa) y verifica que **la base** lo rechaza: la
   última línea de defensa no vive en el framework.
4. **TODO_4 — El rollback completo:** si algo falla después de emitir (el andamio
   provoca una excepción post-emisión dentro de la transacción), **no queda folio
   huérfano ni número gastado** — y el mismo test, corrido contra la variante
   `REQUIRES_NEW` del acto 2b, muestra el salto. El contraste es el contenido.

La invariante del guardián de la semilla (`contador ⇄ MAX(folio)`) ahora la mantiene
**código en producción**, no solo la semilla: `SemillaCoherenteIT` debe seguir verde
tras las emisiones de los tests (cítalo).

## §5 · Anatomía

La de SPEC-000 §7.6 completa. `TEORIA.md`: M7 (el proxy transaccional y la
autoinvocación que no transacciona, propagación con `REQUIRES_NEW` como caso con
costo, aislamiento y sus anomalías, bloqueo pesimista vs optimista con `@Version`
—cuándo cada uno—, rollback por defecto y `rollbackFor`, read-only) + M8 (Flyway a
fondo: versionadas/repetibles/baseline, la tabla de historial como bitácora inmutable
—rima con nuestra gobernanza y la teoría lo dice—, restricciones como contratos,
Liquibase comparado con criterio). El **desafío 99-** natural: replicar la V3 como
changeset de Liquibase y comparar el rollback declarativo (SKIP si no se hace, P-15).
**Siembra del Lab 07:** *"los folios ya son únicos, secuenciales e idempotentes. Hay un
solo problema: cualquiera con curl puede emitirlos. La próxima semana, la puerta tiene
portero."* Plantillas con trampa registrada y la transcripción natural: **el error de
la base rechazando el monto negativo, pegado tal cual** (pregunta de criterio: *"la
validación de Java ya rechazaba negativos — ¿por qué el CHECK no es redundante?"*).

## §6 · Verificación del ejecutor (etapa 1)

Citado, sobre estado limpio: (1) `91-e2e` ×2 — starter falla nombrando los 4 TODOs,
solución 100 %; (2) el crimen reproducible: `--concurrencia 2` sobre el starter con el
duplicado citado en pantalla (y declara el mecanismo de reproducibilidad de la
carrera); (3) acto 2 medido, **ambos parches**: `synchronized` pasa el sabotaje local
(citado) con la nota de la limitación; `REQUIRES_NEW` deja el salto (test citado en
rojo con los números de folio); (4) el `CHECK` muerde: inserción directa de monto
negativo rechazada por la base, error citado; (5) el test de concurrencia del enunciado
es **determinista en veredicto** (×3 corridas — si flakea, se rediseña, no se
reintenta); (6) manifiesto discrimina; (7) `deriva` y `siembra` (audita L5→L6) verdes
en el runner; (8) CI verde, run citado; (9) `ESTADO.md` al día; estimación honesta por
TODO.

## §7 · La Prueba del PO (etapa 2 — cierra la SPEC)

Invitación literal, resultados esperados escritos, tres pasos: (1) el sabotaje de
concurrencia sobre el starter — **ver dos folios con el mismo número en pantalla**;
(2) el mismo sabotaje sobre la solución — únicos y sin saltos; (3) `90 --dir solucion`
→ aprobado. Declara Java/Docker por paso. Recuerda la fila acumulada del PO (Labs
00–05) con la nota de que la pila crece.

## §8 · Criterios de aceptación

- [ ] SPEC commiteada antes del material; rama + PR apilado y declarado.
- [ ] Lab 06 completo (las 5 piezas); evidencia §6 íntegra, ambos parches del acto 2
      medidos.
- [ ] El test de concurrencia sin `sleep` (AU-05 verde) y determinista (×3 citado).
- [ ] RN-01, RN-02 y RN-05 con suelo: sus tests del enunciado en verde en la solución.
- [ ] `SemillaCoherenteIT` sigue verde tras las emisiones.
- [ ] TODOs ≤ ~15 min c/u (o recorte aplicado y declarado).
- [ ] Siembra L5→L6 auditada; Lab 06 siembra el 07 con el gancho del portero.
- [ ] `ESTADO.md` y bitácora al día.
- [ ] **Prueba del PO reportada** — cierra la SPEC.
- [ ] Commits `SPEC-012:`; checks verdes citados.

## §9 · Reporte

Evidencia de §6, tiempos por TODO, decisiones declaradas (mecanismo del lock, mecanismo
de reproducibilidad de la carrera), URL del run, `git log --oneline`, discrepancias y
hallazgos — sin tocarlos. Cierra con la invitación del §7.
