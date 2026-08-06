# SPEC-016 · Lab 10 «El tablero que mentía» — Observabilidad (M12)

| Campo | Valor |
|---|---|
| ID | SPEC-016 |
| Título | Nuevo laboratorio de Observabilidad: health real, métricas de negocio y caché (M12) |
| Autor | Arquitecto |
| Aprueba | PO (Rodrigo) — **APROBADA** (capa humana presentada y aceptada) |
| Depende de | main consolidado (Labs 00–09) · toolchain Java 25 activo |
| Estado | LISTA PARA EJECUCIÓN |

> **Instrucción de ejecución (mocito):** guardar este archivo íntegro en
> `docs/specs/SPEC-016-lab10-observabilidad.md` y commitearlo en rama antes de ejecutar.
> Partes desde `main` limpio (no apilado). Protocolo de dos etapas vigente.

---

## §0 · Resoluciones previas y contexto de numeración

1. **Ramas viejas:** tras esta SPEC, borra las ramas de lab ya mergeadas a `main`
   (`spec/007-*` … `spec/015-*`) que estén 100 % contenidas en `main`. Confírmalo con
   `git branch --merged main` antes de borrar; cita la lista. No toques ramas no
   mergeadas.
2. **Numeración de labs — decisión de arquitecto:** este es el **Lab 10** en el flujo
   del alumno (va después del Lab 09 «Caja negra»: del log estructurado a las métricas
   es un paso recto). Los dos labs que ya estaban diseñados como «Latidos» y
   «Amortiguadores» se **corren un número**: pasan a ser Lab 11 y Lab 12, y el examen de
   egreso pasa a Lab 13. La renumeración final del temario se cuadra en la actualización
   contractual pendiente; por ahora, en el repo, este lab nace como
   `labs/lab-10-observabilidad/`. Regístralo en `decisiones.md` y en `ESTADO.md`.
3. **Nota del warning de Guice/Unsafe** (hallazgo del toolchain): agrégala al
   troubleshooting del Lab 00 (`T-NN`): *"al compilar verás un WARNING de
   sun.misc.Unsafe / Guice — es ruido del wrapper de Maven sobre JDK 25, ajeno a tu
   código, no rompiste nada"*. No es parte del núcleo de esta SPEC, pero cae en la misma
   pasada.

## §1 · Objetivo

Que exista `labs/lab-10-observabilidad/`: la sesión que cubre el **Módulo 12 oficial
(Monitoreo, Observabilidad y Caché)**, hueco detectado en la revisión contractual. El
alumno sale con: health checks que dicen la verdad (liveness/readiness reales, no
superficiales), métricas de negocio propias con Micrometer, endpoints de Actuator
expuestos con criterio, y una consulta costosa acelerada con caché midiendo el hit-rate.
Encadenamiento: `starter/` = `solucion/` del Lab 09 + el tablero mentiroso + los huecos.

## §2 · El crimen

El `starter/` tiene Actuator con la configuración ingenua: `/health` responde `UP`
**siempre que el proceso Java esté vivo**, sin mirar si puede hacer su trabajo. Guion del
relator (10 min):

1. `./bin/start-lab.sh` con la app arriba y su base de datos. `curl /actuator/health` →
   `{"status":"UP"}`. Todo verde.
2. `./bin/start-lab.sh --db-caida` (bandera P-04: levanta la app pero **sin** su base de
   datos, o la tumba con el compose). La app sigue "viva" como proceso.
3. `curl /actuator/health` → **sigue diciendo `UP`**. El tablero miente. Y
   `curl /api/v1/tramites` → 500, porque no hay base.
4. Carolina: *"El monitoreo dice que estamos perfecto. Los contribuyentes dicen que no
   pueden declarar. ¿A quién le creo? Un semáforo que siempre está en verde no es un
   semáforo: es un adorno. Haz que me diga la verdad — y que cuando algo se caiga, me
   diga QUÉ se cayó."*

Lección estructural: **un health que solo confirma que el proceso existe es un adorno;
readiness es poder atender, no estar encendido.**

## §3 · Los tres actos

- **Acto 1 · Choque:** el `UP` con la base caída.
- **Acto 2 · El parche bruto que FUNCIONA:** un health indicator propio que hace
  `SELECT 1` a la base y devuelve DOWN si falla. ¡Ya no miente sobre la base! La guía
  confronta: ¿y TESO? ¿y el disco? ¿y la diferencia entre *liveness* (¿reinicio el pod?)
  y *readiness* (¿le mando tráfico?)? Un health que mezcla las dos hace que Kubernetes
  reinicie la app cuando en realidad solo TESO está lento. Chequear una cosa no es
  observabilidad: es tapar el agujero que te mordió hoy.
- **Acto 3 · La forma correcta:** liveness y readiness **separados** (grupos de
  Actuator), readiness que agrega los checks que importan (base propia sí; dependencias
  externas con criterio), métricas de negocio que permiten *ver venir* el problema en
  vez de reaccionar, y caché donde el costo lo justifica.

## §4 · Los TODOs (4 × ~15 min)

1. **TODO_1 — El health que no miente:** `HealthIndicator` propio para la base + separar
   **liveness** y **readiness** con los grupos de Actuator (`management.endpoint.health.
   group.*`). Test del enunciado: con la base caída, readiness = DOWN **y nombra el
   componente**; liveness sigue UP (la app no debe reiniciarse por una dependencia).
2. **TODO_2 — Métricas de negocio (Micrometer):** un contador de folios emitidos y un
   timer de la emisión (`@Timed` o `MeterRegistry`), expuestos en `/actuator/metrics` y
   en formato Prometheus (`/actuator/prometheus`). Test: emitir un folio incrementa el
   contador. La lección: *métricas de negocio, no solo de infraestructura — el CPU no te
   avisa que dejaste de emitir folios; un contador en cero, sí.*
3. **TODO_3 — Exposición con criterio:** configurar qué endpoints de Actuator se exponen
   y cuáles no (nada de `*` a lo bruto: `/health`, `/info`, `/metrics`, `/prometheus`
   sí; `/env`, `/beans`, `/heapdump` no en producción). Test: un endpoint sensible NO
   responde con la config de prod. Conecta con la doctrina de seguridad del Lab 07.
4. **TODO_4 — Caché con medición (Caffeine):** `@Cacheable` sobre una consulta costosa
   real (un reporte agregado del Lab 04/05), con Caffeine, TTL declarado y estadísticas
   activadas. Test del enunciado: la segunda llamada idéntica **no** golpea la base
   (verificado contando invocaciones al repositorio o por el hit-rate de las stats), y
   la invalidación (`@CacheEvict`) funciona cuando el dato cambia. La lección del acto:
   *un caché sin invalidación es un mentiroso con buena memoria — qué se cachea y qué
   no.* Redis se nombra como caché distribuida (nivel conceptual), no se instala.

En teoría/andamio (lectura, no tecleo): la propagación de trazas a Prometheus/Grafana
(demo del relator, conecta con el `traceId` del Lab 09), y el panorama de los tres
pilares (métricas/trazas/logs) cerrando el arco de observabilidad que abrió el Lab 09.

## §5 · Anatomía

La de SPEC-000 §7.6 completa. El `starter/` añade al compose (si lo usas para el
`--db-caida`) el mecanismo para tumbar la base de forma reproducible — declara cómo.
`TEORIA.md`: M12 completo (Actuator y sus endpoints; liveness vs readiness con la
analogía del semáforo y del hospital —«¿está vivo?» vs «¿puede recibir pacientes?»—;
health indicators propios; Micrometer, contadores/timers/gauges, métricas de negocio;
Prometheus/Grafana a nivel conceptual y demo; abstracción de caché de Spring,
`@Cacheable`/`@CachePut`/`@CacheEvict`, Caffeine local con TTL y estadísticas, Redis
distribuida conceptual, invalidación y consistencia). **Siembra del Lab 11 «Latidos»:**
*"ya sabes si tu sistema está sano y puedes medir lo que hace. Pero el viernes pasado el
cierre nocturno se ejecutó dos veces — hay dos servidores y los dos se creyeron el
único. La próxima semana, el reloj tiene un problema de identidad."* Plantillas con
trampa registrada y la transcripción natural: **el `UP` mentiroso con la base caída,
pegado tal cual, junto al readiness que dice la verdad después** (pregunta de criterio:
*"el proceso estaba vivo y respondía `UP`. ¿Por qué eso era una mentira?"*).

## §6 · Verificación del ejecutor (etapa 1)

Citado, sobre estado limpio: (1) `91-e2e` ×2 — starter falla nombrando los 4 TODOs,
solución 100 %; (2) el crimen vivido: `--db-caida` en el starter → `/health` dice `UP`
mientras `/api/v1/tramites` da 500 (ambos citados); en la solución → readiness DOWN
nombrando la base, liveness UP, y el detalle citado; (3) acto 2 medido: el `SELECT 1`
arregla la base pero la guía muestra que readiness/liveness siguen mezclados hasta el
acto 3 (citado); (4) la métrica de negocio sube al emitir un folio (antes/después
citados desde `/actuator/prometheus`); (5) el endpoint sensible no responde en perfil
prod (citado); (6) el caché: segunda llamada sin golpe a la base (conteo o hit-rate
citado) y `@CacheEvict` invalidando (citado); (7) manifiesto discrimina; (8) `deriva`
(nuevo eslabón desde Lab 09) y `siembra` (audita L9→L10) verdes en el runner; (9) CI
verde con el job `app` corriendo los tests nuevos, run citado; (10) `ESTADO.md`
actualizado con la renumeración; ramas viejas borradas (§0.1) con la lista citada;
estimación honesta por TODO.

## §7 · La Prueba del PO (etapa 2 — cierra la SPEC)

Invitación literal, resultados esperados escritos, tres pasos: (1) `--db-caida` en el
starter y `curl /actuator/health` → **ver el `UP` que miente** con la base caída; (2) lo
mismo en la solución → readiness dice `DOWN` y **nombra qué se cayó**; (3) `90 --dir
solucion` → aprobado. Declara Java/Docker por paso. Recuerda que la fila de pruebas
acumuladas del PO (Labs 00–10) ya puede correrse desde `main` limpio, con Java 25 activo.

## §8 · Criterios de aceptación

- [ ] SPEC commiteada antes del material; rama + PR desde main.
- [ ] Lab 10 completo (las 5 piezas); evidencia §6 íntegra, actos medidos.
- [ ] Liveness y readiness separados; readiness nombra el componente caído.
- [ ] Métrica de negocio propia visible en `/actuator/prometheus`.
- [ ] Caché con hit medido y `@CacheEvict` verificado.
- [ ] Endpoints sensibles cerrados en perfil prod.
- [ ] Renumeración registrada (decisiones.md + ESTADO.md); ramas viejas borradas.
- [ ] Nota del warning Guice/Unsafe agregada al troubleshooting del Lab 00.
- [ ] Siembra L9→L10 auditada; Lab 10 siembra el 11 «Latidos».
- [ ] **Prueba del PO reportada** — cierra la SPEC.
- [ ] Commits `SPEC-016:`; checks verdes citados.

## §9 · Reporte

Evidencia de §6, tiempos por TODO, decisiones declaradas (mecanismo de `--db-caida`,
qué dependencias entran en readiness y por qué), URL del run, `git log --oneline`,
ramas borradas, discrepancias y hallazgos — sin tocarlos. Cierra con la invitación del §7.
