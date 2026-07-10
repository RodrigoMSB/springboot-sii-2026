# Curso Spring Boot · SII 2026

Material del curso de Spring Boot para el SII: teoría, labs y su tooling de validación.

## Protocolo SPEC

El arquitecto emite especificaciones numeradas, versionadas en `docs/specs/`. Si la
ejecución difiere de la SPEC, la SPEC manda y la discrepancia se reporta. Los commits
llevan el prefijo `SPEC-NNN: <qué>`.

- **`SPEC-NNN`** — funcionalidades y entregables nuevos. Propósito único.
- **`SPEC-FIX-NN`** — corrección de una SPEC ya ejecutada (bug del material).
- **`SPEC-DIAG-NN` / `SPEC-AUDIT-NN`** — diagnóstico e investigación que no producen
  material. Se versionan siempre.
- **Sufijo `-R1`, `-R2`** — revisión de una SPEC aún no ejecutada.
- **Ninguna ejecución comienza antes de que su SPEC esté commiteada.**

**Toda SPEC va por rama, con PR. Sin excepciones.** `main` está protegida: el push directo
se rechaza y el merge exige los tres checks de `material-ci` (`temario`, `siembra`, `app`)
en verde. Una **`SPEC-FIX-NN`** además termina en tag: `fix/<slug>` → merge a `main` →
tag `material-vX.Y.Z` (patch bump).

El nombre del archivo **no lleva sufijo de versión**: Git ya versiona el contenido. Una
SPEC ejecutada es inmutable; una SPEC aún no ejecutada se revisa como archivo `-R1` nuevo.

## Roles

- **Product Owner** (Rodrigo): aprueba las SPEC y decide los commits de fondo.
- **Arquitecto**: emite las SPEC.
- **Ejecutor**: las ejecuta, registra la evidencia en el repo y maneja git/GitHub.

Decisiones de diseño y su razón: [`docs/decisiones.md`](docs/decisiones.md).
