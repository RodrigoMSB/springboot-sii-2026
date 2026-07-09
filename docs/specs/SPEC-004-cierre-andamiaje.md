# SPEC-004 · Cierre del andamiaje: bitácora, gobernanza v2 y CI del material

| Campo | Valor |
|---|---|
| ID | SPEC-004 |
| Título | Registro de la decisión Docker + gobernanza v2 (rama/tag) + CI del material |
| Autor | Arquitecto |
| Aprueba | PO (Rodrigo) — **APROBADA** |
| Depende de | SPEC-002, SPEC-003 |
| Estado | LISTA PARA EJECUCIÓN |

> **Instrucción de ejecución (mocito):** primer paso, guardar este archivo íntegro en
> `docs/specs/SPEC-004-cierre-andamiaje.md` y commitearlo **antes** de ejecutar
> cualquier cambio.

---

## 1. Objetivo

Dejar el andamiaje del proyecto **cerrado y sin cabos sueltos** antes de la SPEC-000:
(a) saldar la referencia colgante D-007 en la bitácora, (b) completar la gobernanza con
el mecanismo que le faltaba (rama + tag para las SPEC-FIX), y (c) rescatar el verificador
del temario hacia CI para que la coherencia deje de depender de una sesión efímera.

Es una SPEC de tres acciones bajo un solo propósito: **que ninguna regla del proyecto
viva solo en la memoria de una conversación.** Cierra los tres pendientes reportados por
el ejecutor en las SPEC-002 y SPEC-003.

## 2. Acción A — Saldar la referencia colgante D-007

El ADN (P-17) referencia la "decisión D-007" (Docker con respaldo `--sin-docker`), que
proviene del documento de traspaso y **nunca se registró en nuestra bitácora**. El PO la
ratifica aquí. Agregar a `decisiones.md`:

| Fecha | Decisión | Razón |
|---|---|---|
| (fecha de ejecución) | **Docker es requisito del curso, con modo `--sin-docker` de respaldo probado en CI** para los laboratorios donde exista sustituto razonable. Los temas que no tienen sustituto sin Docker (Testcontainers, imagen OCI, doble instancia) pasan a demo del relator en ese escenario. Ratifica y registra la decisión D-007 del documento de traspaso, hasta ahora referencia colgante en el ADN (P-17). | Instalar Docker en una institución del Estado es una gestión que puede fallar; si falla, el respaldo ya está escrito y verificado. Fallar hacia lo simple. |

Luego, en `docs/adn/adn-cypress.md`, actualizar la anotación de P-17: donde dice
"decisión D-007, aún no registrada en decisiones.md", reemplazar por la referencia a la
fila recién creada (fecha). La referencia deja de colgar.

## 3. Acción B — Gobernanza v2 (el nombre CON el mecanismo)

El ejecutor reportó (SPEC-002, hallazgos 1–3) que la gobernanza de Cypress ata cada
SPEC-FIX a una rama y un tag, y que existen prefijos `SPEC-DIAG` / `SPEC-AUDIT`. Nuestra
adopción tomó los nombres sin el mecanismo. Se completa así — actualizar la sección
**"Protocolo SPEC"** del `README.md`:

**Se adopta:**

1. **Toda SPEC-FIX-NN se ejecuta en rama propia y termina en tag:**
   `fix/<slug>` → merge a `main` → tag `material-vX.Y.Z` (patch bump). Las SPEC-NNN de
   features siguen pudiendo ir directo a `main` mientras el material no esté dictándose;
   desde el primer dictado, también van por rama.
2. **Prefijos `SPEC-DIAG-NN` (diagnóstico) y `SPEC-AUDIT-NN` (auditoría)** quedan
   disponibles para trabajos de investigación que no producen material — se usan cuando
   se necesiten, y **se versionan siempre** (la lección de Cypress: sus DIAG/AUDIT
   existieron y no quedaron en el repo).

**Se rechaza (con registro):**

3. **Sufijos de versión en el nombre del archivo** (`SPEC-001-v1.1.md`): no se adoptan.
   Git ya versiona el contenido; una SPEC **ejecutada es inmutable**, y una SPEC aún no
   ejecutada se revisa como archivo nuevo con sufijo `-R1`. Duplicar la versión en el
   nombre del archivo fue ruido en Cypress (dos archivos vivos para la misma SPEC).

Registrar en `decisiones.md`:

| Fecha | Decisión | Razón |
|---|---|---|
| (fecha de ejecución) | Gobernanza v2: toda SPEC-FIX va en rama `fix/<slug>` con merge a `main` y tag; se habilitan los prefijos SPEC-DIAG/SPEC-AUDIT (siempre versionados); se rechazan los sufijos de versión en nombres de archivo. | Adoptar el nombre sin el mecanismo es un adorno (hallazgo del ejecutor, SPEC-002). Git versiona el contenido: una SPEC ejecutada es inmutable y una revisión pre-ejecución es un archivo `-R1` nuevo. |

## 4. Acción C — CI del material: el verificador deja de ser efímero

1. **Rescatar el verificador.** El script `verificar_temario.py` de la SPEC-003 vive en
   el scratchpad y muere con la sesión. Reescribirlo/consolidarlo como
   `tools/verificar-temario.py`, versionado, con las **cinco comprobaciones (a–e) de la
   SPEC-003 §3.3** — incluida la corrección del falso rojo de su D1 (primera celda
   numérica, no forma del texto) y la lectura de tablas del `.docx` con `python-docx`
   (su D2). Debe: exit 0 si todo cuadra, exit 1 con detalle si no, y salida sin colores
   ANSI (`[OK]`/`[ERROR]`), greppeable.

   *Nota de convenciones:* la regla "prohibido Python" del andamiaje de labs aplica al
   `bin/` que ejecuta el **alumno** (portabilidad macOS/Git Bash). `tools/` es tooling
   interno del repo y corre en CI sobre `ubuntu-latest`: Python es válido ahí. Dejar
   esta distinción escrita en un comentario de cabecera del script.

2. **Crear `.github/workflows/material-ci.yml`** con dos jobs:

   - **`temario`**: se dispara en cambios a `docs/temario/**` o `tools/**`. Instala
     `python-docx`, corre `tools/verificar-temario.py`. Rojo = el `.md` y el `.docx`
     divergieron o la matriz no cuadra.
   - **`siembra`**: implementa la regla registrada en P-18 — toda `TEORIA.md` de un lab
     con sucesor contiene una sección de siembra (detección por patrón "siembra",
     insensible a mayúsculas, no por título literal). **Guarda de activación:** si no
     existe `labs/` o está vacío, el job termina en éxito con el mensaje
     `[INFO] Sin labs aún: la regla de siembra queda armada, no activa.` Así la regla
     entra en vigor sola cuando nazca el primer lab, sin SPEC adicional.

3. **Registrar en `decisiones.md`:**

| Fecha | Decisión | Razón |
|---|---|---|
| (fecha de ejecución) | El material tiene CI propio (`material-ci.yml`): verificador de coherencia del temario (fuente ⇄ build, matriz que cuadra) y regla de siembra armada para cuando existan labs. | Hoy la coherencia está verificada; nada impedía que la próxima edición la rompiera en silencio (hallazgo del ejecutor, SPEC-003). Lo que el proyecto predica se compila en algo que muerde. |

4. **Verificación real:** tras el push, confirmar que el workflow corrió **en GitHub
   Actions** y quedó verde. Citar el run (URL o ID). Si el runner falla por permisos o
   configuración del repo, reportar el bloqueo con el log — no declarar verde lo que no
   corrió.

## 5. Criterios de aceptación

- [ ] SPEC-004 commiteada antes que sus cambios (verificable en `git log`).
- [ ] `decisiones.md` tiene las **tres** filas nuevas (Docker/D-007, gobernanza v2, CI).
- [ ] P-17 del ADN ya no tiene la referencia colgante.
- [ ] README con la gobernanza v2 (rama+tag, DIAG/AUDIT, rechazo de sufijos).
- [ ] `tools/verificar-temario.py` versionado, exit 0 contra el temario actual.
- [ ] `material-ci.yml` existe; el run de Actions quedó **verde y citado**.
- [ ] El job `siembra` reporta el `[INFO]` de regla armada (sin labs aún).
- [ ] Commits con prefijo `SPEC-004:`; push hecho.

## 6. Reporte

Salida completa de `tools/verificar-temario.py` en local, URL/ID del run verde de
Actions, `git log --oneline`, discrepancias si las hubo, y hallazgos del ejecutor si
aparecen (sin tocarlos, como siempre).
