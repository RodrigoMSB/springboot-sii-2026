# Curso Spring Boot · SII 2026

Material del curso de Spring Boot para el SII: teoría, labs y su tooling de validación.

## Protocolo SPEC

El arquitecto emite especificaciones numeradas, versionadas en `docs/specs/`. Si la
ejecución difiere de la SPEC, la SPEC manda y la discrepancia se reporta. Los commits
llevan el prefijo `SPEC-NNN: <qué>`.

- **`SPEC-NNN`** — funcionalidades y entregables nuevos. Propósito único.
- **`SPEC-FIX-NN`** — corrección de una SPEC ya ejecutada (bug del material).
- **Sufijo `-R1`, `-R2`** — revisión de una SPEC aún no ejecutada.
- **Ninguna ejecución comienza antes de que su SPEC esté commiteada.**

## Roles

- **Product Owner** (Rodrigo): aprueba las SPEC y decide los commits de fondo.
- **Arquitecto**: emite las SPEC.
- **Ejecutor**: las ejecuta, registra la evidencia en el repo y maneja git/GitHub.

Decisiones de diseño y su razón: [`docs/decisiones.md`](docs/decisiones.md).
