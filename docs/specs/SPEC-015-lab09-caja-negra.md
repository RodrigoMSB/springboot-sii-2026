# SPEC-015 · Lab 09 «Caja negra»

| Campo | Valor |
|---|---|
| ID | SPEC-015 |
| Título | Noveno laboratorio: encontrar la aguja en 400 MB — logging estructurado, MDC, AOP y archivos (S09 · M10 0,5 h + M11 2,5 h) |
| Autor | Arquitecto |
| Aprueba | PO (Rodrigo) — **APROBADA** |
| Depende de | SPEC-014 |
| Estado | LISTA PARA EJECUCIÓN |

> **Instrucción de ejecución (mocito):** guardar este archivo íntegro en
> `docs/specs/SPEC-015-lab09-caja-negra.md` y commitearlo en rama antes de ejecutar.
> Apila sobre la pila y decláralo. Protocolo de dos etapas vigente.

---

## §1 · Objetivo

Que exista `labs/lab-09-caja-negra/`: la sesión donde el alumno aprende a **ver adentro
de un sistema en marcha**. El crimen ya ocurrió; no hay que arreglar código roto, hay
que **encontrar la verdad** en el ruido. El alumno sale con: logging estructurado en
JSON, correlación por `traceId` con MDC, un aspecto de auditoría que registra toda
invocación al dominio sin ensuciarlo, y carga/descarga de adjuntos con validación
estricta. Encadenamiento: `starter/` = `solucion/` del Lab 08 + el sistema "mudo" + los
huecos.

## §2 · El crimen

El `starter/` tiene los logs del practicante: texto plano, sin correlación, con
`System.out.println` sueltos y un `catch` que traga la excepción. Guion del relator
(10 min):

1. `./bin/start-lab.sh --dir starter --caos` (bandera P-04: dispara una tormenta de
   peticiones concurrentes legítimas + la operación culpable escondida entre ellas, y
   genera un log grande — la cifra a tu juicio; el criterio es que buscar a mano
   **duela**).
2. Carolina proyecta el problema: *"Ayer se emitió el folio 4471 a un contribuyente
   que no correspondía. Aquí están los logs de la jornada."* Abre el archivo: un muro
   de líneas entrelazadas de 30 peticiones simultáneas, sin forma de saber cuáles
   pertenecen a la misma operación.
3. El intento a mano: `grep 4471` devuelve 200 líneas de 15 peticiones distintas
   mezcladas. Imposible reconstruir **qué** pasó en **cuál** petición.
4. Carolina: *"No te pido que arregles el folio — ya lo anulamos. Te pido que la
   próxima vez pueda responderle al fiscalizador en cinco minutos, no en cinco horas.
   Quiero un sistema que sepa contar lo que hizo."*

La lección: **un sistema que no se puede observar no se puede operar; el log no es para
el programador, es para el que llega a las 3 AM sin saber qué pasó.**

## §3 · Los tres actos

- **Acto 1 · Choque:** el `grep` inútil sobre el muro entrelazado.
- **Acto 2 · El parche bruto que FUNCIONA:** agregar más `println` con más detalle.
  Más ruido no es más señal: ahora hay 400 líneas entrelazadas en vez de 200, y el
  `catch` que traga sigue escondiendo el error raíz. La guía: **el problema no es
  cuánto registras, es que no puedes seguir el hilo de una sola petición.**
- **Acto 3 · La forma correcta:** JSON estructurado + `traceId` por petición en el MDC
  (un filtro lo siembra al entrar, todo log de esa petición lo lleva) → filtrar por un
  `traceId` reconstruye la operación completa, aislada. El aspecto de auditoría registra
  entradas/salidas del dominio de forma transversal. El `catch` que traga se reemplaza
  por propagación con contexto.

## §4 · Los TODOs (4 × ~15 min)

1. **TODO_1 — El hilo de Ariadna (MDC):** filtro que genera/propaga un `traceId` por
   petición y lo pone en el MDC; el patrón de log lo incluye. Test del enunciado: dos
   peticiones concurrentes producen logs con `traceId` **distintos**, y todas las líneas
   de una misma petición comparten el suyo (verificado capturando el appender, no
   leyendo un archivo).
2. **TODO_2 — Log estructurado en JSON:** activar el formato JSON nativo de Boot; cada
   línea es un objeto con `timestamp`, `level`, `traceId`, `logger`, `message` y campos
   de negocio. Test: la salida parsea como JSON y contiene el `traceId`.
3. **TODO_3 — El auditor invisible (AOP):** `@Aspect` con `@Around` sobre el paquete de
   servicios de dominio que registra invocación, argumentos (con **datos sensibles
   enmascarados** — el RUT parcial, jamás la clave) y tiempo de ejecución, sin tocar
   una línea de la lógica de negocio. Test: invocar un servicio deja el rastro de
   auditoría; el aspecto **no** intercepta la autoinvocación (la trampa del proxy, misma
   familia que `@Transactional` — la teoría lo conecta explícitamente). El código de
   negocio permanece idéntico (diff citado).
4. **TODO_4 — Los adjuntos con desconfianza (M11 archivos):** `POST .../adjuntos` con
   `MultipartFile`: validar tamaño, **tipo MIME real** (magic bytes, no la extensión ni
   el header que el cliente dice), y sanear el nombre (nada de `../`). Descarga en
   streaming sin cargar el archivo entero en memoria. Tests: un `.exe` renombrado a
   `.pdf` se rechaza; un nombre con path traversal se neutraliza.

M10 (0,5 h) entra en teoría: la propagación automática del `traceId` a las llamadas
salientes (el cliente de TESO del Lab 08 hereda el contexto de traza — se muestra, se
conecta con el Lab 14/observabilidad, no se teclea aquí).

## §5 · Anatomía

La de SPEC-000 §7.6 completa. `TEORIA.md`: M11 núcleo (niveles de log y el criterio de
cada uno; JSON estructurado y por qué un agregador lo ama; MDC y correlación contada
como el número de pedido en una cocina; AOP — aspecto/join point/pointcut/advice con la
analogía propia, los tipos de advice, expresiones de pointcut, el límite del proxy; el
`catch` que traga como antipatrón nombrado; carga de archivos y sus trampas: MIME real,
path traversal, memoria) + la media hora de M10 (propagación de contexto saliente).
**Siembra del Lab 10:** *"ahora el sistema sabe contar lo que hizo. Y contando, Carolina
notó algo raro: el cierre nocturno del viernes se ejecutó dos veces. Hay dos servidores,
y los dos se creyeron el único. La próxima semana, el reloj tiene un problema de
identidad."* Plantillas con trampa registrada y la transcripción natural: **dos líneas
de log JSON de la misma petición con su `traceId`, pegadas tal cual** (la pregunta de
criterio: *"antes tenías 200 líneas y ninguna respuesta; ¿qué cambió, además del
formato?"*).

## §6 · Verificación del ejecutor (etapa 1)

Citado, sobre estado limpio: (1) `91-e2e` ×2 — starter falla nombrando los 4 TODOs,
solución 100 %; (2) el crimen vivido: en el starter, `grep` del folio culpable
devolviendo el muro entrelazado (citado); en la solución, filtrar por `traceId`
reconstruye la operación aislada (citado, el contraste); (3) acto 2 medido: más
`println` → más ruido, mismo problema (citado); (4) los `traceId` concurrentes son
distintos y consistentes (test citado); (5) el JSON parsea (citado); (6) el aspecto no
ensucia el negocio (diff del servicio: idéntico) y no intercepta autoinvocación (test
citado); (7) el `.exe` disfrazado de `.pdf` rechazado y el path traversal neutralizado
(ambos citados); (8) manifiesto discrimina; (9) `deriva` y `siembra` (audita L8→L9)
verdes en el runner; (10) CI verde, run citado; (11) `ESTADO.md` al día; estimación
honesta por TODO.

## §7 · La Prueba del PO (etapa 2 — cierra la SPEC)

Invitación literal, resultados esperados escritos, tres pasos: (1) starter con `--caos`,
`grep` del folio culpable → **el muro entrelazado, imposible de seguir**; (2) solución,
mismo caos, filtrar por un `traceId` → **la operación completa, aislada y legible** —
la aguja encontrada; (3) `90 --dir solucion` → aprobado. Declara Java/Docker por paso.
Recuerda la fila acumulada del PO (Labs 00–08).

## §8 · Criterios de aceptación

- [ ] SPEC commiteada antes del material; rama + PR apilado y declarado.
- [ ] Lab 09 completo (las 5 piezas); evidencia §6 íntegra, actos medidos.
- [ ] El aspecto deja el código de negocio idéntico (diff citado) y respeta el límite
      del proxy (test de autoinvocación).
- [ ] La validación de adjuntos usa MIME real (magic bytes), no la extensión; path
      traversal neutralizado — ambos con test.
- [ ] Los datos sensibles en la auditoría van enmascarados (nunca clave, RUT parcial).
- [ ] TODOs ≤ ~15 min c/u (o recorte aplicado y declarado).
- [ ] Siembra L8→L9 auditada; Lab 09 siembra el 10 con el gancho del reloj duplicado.
- [ ] `ESTADO.md` y bitácora al día.
- [ ] **Prueba del PO reportada** — cierra la SPEC.
- [ ] Commits `SPEC-015:`; checks verdes citados.

## §9 · Reporte

Evidencia de §6, tiempos por TODO, decisiones declaradas (cómo se captura el log en los
tests, tamaño del log del `--caos`, mecanismo de MIME real elegido), URL del run,
`git log --oneline`, discrepancias y hallazgos — sin tocarlos. Cierra con la invitación
del §7.
