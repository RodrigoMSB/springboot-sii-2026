# SPEC-FIX-01 · Desarmar la trampa de los checks y enchufar el candado

| Campo | Valor |
|---|---|
| ID | SPEC-FIX-01 |
| Naturaleza | Corrección de material ejecutado (SPEC-004: `material-ci.yml`) + descongelamiento del §10.A de la SPEC-000 |
| Autor | Arquitecto |
| Aprueba | PO (Rodrigo) — **APROBADA** (GitHub Pro ya activo, decisión del PO) |
| Rama / Tag | `fix/checks-y-candado` → merge a `main` → tag **`material-v0.1.1`** |
| Estado | LISTA PARA EJECUCIÓN |

> **Instrucción de ejecución (mocito):** primer paso, guardar este archivo íntegro en
> `docs/specs/SPEC-FIX-01-checks-y-candado.md` y commitearlo en la rama
> `fix/checks-y-candado` antes de ejecutar. Esta SPEC-FIX **estrena el mecanismo** de la
> gobernanza v2: rama propia, merge a `main`, tag.

---

## §0 · Orden de las operaciones (importa)

La trampa se desarma **antes** de armar el candado. Si activas los required checks con
el filtro de rutas vigente, esta misma SPEC-FIX — que toca YAML y docs — podría quedar
colgada esperando un check que nunca corre. Secuencia obligatoria: §1 (tag base) →
§2 (CI) → merge del PR → §3 (candado) → §4 (prueba de rechazo) → tag.

## §1 · Tag base retroactivo

Antes de corregir nada: tag **`material-v0.1.0`** sobre el commit de merge del PR #2
(`0633199`, nacimiento del tronco). La gobernanza pide que las FIX incrementen patch;
para incrementar algo tiene que existir el cero. Push del tag.

## §2 · Corrección del `material-ci.yml` (el bug)

**El bug:** el filtro de rutas a nivel de workflow hace que un PR que solo toca
`docs/specs/` o `decisiones.md` no dispare ningún check. Con required checks activos,
ese PR queda **esperando eternamente** un check que nadie va a lanzar (hallazgo del
ejecutor, reportado en SPEC-000 y reiterado en SPEC-005).

**La corrección:** en el evento `pull_request`, **se elimina el filtro de rutas** — todo
PR ejecuta los tres jobs, siempre. Costo medido: `temario` y `siembra` corren en
segundos; `app` en ~1 minuto. Un minuto por PR es el precio de que ningún PR quede
colgado jamás y de que ningún merge entre sin los tres veredictos. Se descartaron las
alternativas: el job centinela no-op protege menos (un `temario` roto podría mergear si
no es requerido), y el filtro fino por job no existe en Actions (D1 de la SPEC-004).
El evento `push` a `main` puede conservar filtros si quieres ahorrar runs post-merge;
el `workflow_dispatch` se mantiene.

**Además, en el mismo YAML:**

- El job `app` declara su exclusión de Windows **con la razón escrita en el YAML**
  (Testcontainers no funciona en runners Windows hospedados — mismo patrón documentado
  que el Lab 11 de Cypress / P-12). Resuelve el hallazgo 1 de la SPEC-005.
- Comentario de cabecera actualizado: por qué ya no hay filtro en PR (cita esta SPEC-FIX).

## §3 · El candado (§10.A de la SPEC-000, descongelado por el PO)

Con GitHub Pro activo:

1. Ruleset sobre `main`: **push directo prohibido**, PR obligatorio, required status
   checks: `temario`, `siembra`, `app`.
2. README, sección "Protocolo SPEC": se deroga la cláusula transitoria ("directo a main
   mientras no se dicte") — **toda SPEC va por rama con PR**, sin excepciones ni fechas.
3. Bitácora — las dos filas congeladas del §10.A, más el rescate de la huérfana:

| Fecha | Decisión | Razón |
|---|---|---|
| (fecha) | `main` queda protegida por ruleset: PR obligatorio con checks `temario`, `siembra` y `app` requeridos; se deroga la cláusula transitoria de la gobernanza v2. Habilitado por GitHub Pro (decisión comercial del PO). | El candado se instala cuando hay código que proteger — lo hay desde la SPEC-005. Una sola regla es mejor que dos con fecha de cambio. |
| (fecha) | **El rojo del `temario` es un semáforo, no una falla:** si el `.md` cambia y el `.docx` diverge, el rojo significa "regenerar el build con el arquitecto". No se regenera en CI. | Un build automático sin la línea gráfica del entregable comercial es peor que un rojo honesto. Se revisita si las ediciones se vuelven frecuentes. (Decisión que cayó congelada con el §10.A sin depender de él; se rescata — hallazgo 2 del ejecutor, SPEC-000.) |
| (fecha) | Se confirma la semilla del contador de folios: `contador_folio` nace en 1, coherente con el trámite FOLIADO sembrado que porta el folio 1. La coherencia contador ⇄ folios sembrados es invariante de toda semilla futura. | Un contador en 0 haría que el Lab 06 emitiera un folio repetido en su primer intento: la lección se contaminaría con un bug de semilla (hallazgo 3 del ejecutor, SPEC-005, ratificado por el arquitecto). |

## §4 · La prueba de que el candado existe

El criterio que la SPEC-000 dejó abierto, ahora sí: intentar un push directo a `main`
(un commit trivial en una rama local) y **citar el rechazo del servidor** (el error
`GH006`/`protected branch` o equivalente del ruleset). La evidencia inversa de la
SPEC-000 (`el push habría entrado`) queda superada y se referencia como el "antes".

## §5 · Cierre de los criterios abiertos de la SPEC-000

En `docs/specs/SPEC-000-especificacion-maestra.md` **no se edita nada** (una SPEC
ejecutada es inmutable). El cierre se registra aquí: los tres criterios que quedaron
abiertos (protección activa, filas de bitácora del §10.A, README) se cumplen por esta
SPEC-FIX. Este archivo es la referencia cruzada.

## §6 · Criterios de aceptación

- [ ] Tag `material-v0.1.0` sobre `0633199`, pusheado.
- [ ] `material-ci.yml` sin filtro de rutas en `pull_request`; exclusión de Windows del
      job `app` documentada en el YAML.
- [ ] **El PR de esta misma SPEC-FIX ejecuta los tres checks** (la corrección se
      demuestra a sí misma: es un PR que toca YAML y docs, y reporta los tres verdes).
- [ ] Ruleset activo en `main`; push directo **rechazado con evidencia citada**.
- [ ] README con la cláusula transitoria derogada.
- [ ] `decisiones.md` con las tres filas de §3.
- [ ] Merge por PR, rama borrada, tag **`material-v0.1.1`** pusheado.
- [ ] Commits con prefijo `SPEC-FIX-01:`.

## §7 · Reporte

Los tres checks del propio PR (URL del run), el rechazo del push directo (salida
completa), los dos tags visibles en el remoto, `git log --oneline --tags`,
discrepancias y hallazgos — sin tocarlos.
