# SPEC-002 · Addendum del ADN y gobernanza de especificaciones

| Campo | Valor |
|---|---|
| ID | SPEC-002 |
| Título | Addendum del ADN (P-15…P-18) + gobernanza SPEC-FIX + regla de siembra |
| Autor | Arquitecto |
| Aprueba | PO (Rodrigo) — **APROBADA** |
| Depende de | SPEC-001 |
| Estado | LISTA PARA EJECUCIÓN |

> **Instrucción de ejecución (mocito):** primer paso, guardar este archivo íntegro en
> `docs/specs/SPEC-002-addendum-adn-y-gobernanza.md` y commitearlo **antes** de ejecutar
> cualquier cambio (lección de D4 de la SPEC-001: ninguna ejecución parte sin su SPEC en git).

---

## 1. Objetivo

Incorporar al ADN los 4 hallazgos del ejecutor (aprobados por el PO), formalizar la
gobernanza de SPEC correctivas y dejar registrada la regla de siembra como futura
verificación de CI.

## 2. Cambios en `docs/adn/adn-cypress.md`

Agregar, con el **mismo estándar de evidencia de la SPEC-001** (ruta + cita de máx. 5
líneas + "Por qué funciona" + "Traslado a Spring Boot"):

### P-15 · Lo opcional es SKIP, jamás FAIL

- **Evidencia:** `labs/lab-03-*/bin/validar-lab.sh` (el `paso_skip` del desafío) y la
  numeración `99-` que lo saca de la secuencia obligatoria.
- **Traslado:** el `90-validar.sh` del curso trata `desafio/` con contador aparte; su
  ausencia jamás baja el veredicto del núcleo.

### P-16 · El antes y el después conviven en el repo

- **Evidencia:** `labs/lab-08-*/solucion/` y `solucion-refactor/`.
- **Traslado:** el Lab 05 versiona `solucion-con-n1/` y `solucion/` para que el alumno
  diffee su propia deuda (misma suite, distinto conteo de queries).

### P-17 · El entorno del alumno tiene documento propio, correlato humano del CI

- **Evidencia:** `docs/entorno-windows.md` + la matriz `windows-latest` de
  `material-ci.yml`.
- **Traslado:** `docs/entorno-alumno.md` del curso, incluido el modo `--sin-docker`
  (D-007); lo que el CI verifica, el doc lo explica.

### P-18 · La siembra es un invariante estructural, no un título

- **Evidencia:** hallazgo H9 de la SPEC-001 (12/12 labs con sucesor siembran; el título
  varía en labs 09–12; el Lab 13 cierra sin sembrar).
- **Traslado:** regla de CI futura — toda `TEORIA.md` de un lab con sucesor contiene una
  sección de siembra (se detecta por patrón "siembra", no por título literal). Queda
  **registrada** aquí; se implementa cuando exista `material-ci.yml` (SPEC posterior).

## 3. Gobernanza de especificaciones

Va al `README.md` del repo (sección **"Protocolo SPEC"**) y a `decisiones.md`:

- **SPEC-NNN:** funcionalidades y entregables nuevos. Propósito único.
- **SPEC-FIX-NN:** corrección de una SPEC ya ejecutada (bug del material).
- **Sufijo -R1, -R2:** revisión de una SPEC aún no ejecutada.
- **Ninguna ejecución comienza antes de que su SPEC esté commiteada.**

Evidencia de origen: `docs/specs/` del curso de Cypress (`SPEC-FIX-01..03`,
`SPEC-001-R1` conviviendo con la línea principal).

## 4. Registro

Fila en `decisiones.md`:

| Fecha | Decisión | Razón |
|---|---|---|
| (fecha de ejecución) | Se adopta la gobernanza SPEC-NNN / SPEC-FIX-NN / -RN y se incorporan P-15…P-18 al ADN por decisión del PO. | Los hallazgos del ejecutor se adoptan con el mismo estándar de evidencia que las hipótesis del arquitecto: el origen no rebaja el rigor. |

## 5. Criterios de aceptación

- [ ] SPEC-002 commiteada antes que sus cambios (verificable en `git log`).
- [ ] `adn-cypress.md` contiene P-15…P-18 con evidencia completa (conteo: 18 `### P-`,
      18 "Por qué funciona", 18 "Traslado").
- [ ] El README tiene la sección "Protocolo SPEC".
- [ ] `decisiones.md` tiene su fila.
- [ ] Commits con prefijo `SPEC-002:`; push hecho.

## 6. Reporte

Conteo final de prácticas, `git log --oneline`, discrepancias si las hubo.
