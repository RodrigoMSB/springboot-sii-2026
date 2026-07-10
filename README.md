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

**Toda SPEC va por rama, con PR. Sin excepciones.** Se deroga la cláusula transitoria
("directo a `main` mientras no se dicte"). Una **`SPEC-FIX-NN`** además termina en tag:
`fix/<slug>` → merge a `main` → tag `material-vX.Y.Z` (patch bump).

> ⚠️ **La regla es convencional, no está enchufada.** `main` **no** tiene protección en el
> servidor: GitHub no permite proteger ramas en repos privados del plan Free, y el push
> directo entraría. Todo PR ejecuta los tres checks de `material-ci` (`temario`, `siembra`,
> `app`), pero **ninguno es obligatorio para mergear**. El candado está especificado y
> congelado (SPEC-FIX-01 §3.1); se activa el día que haya GitHub Pro o el repo deje de ser
> privado. Aquí no se declara activo lo que no lo está.

El nombre del archivo **no lleva sufijo de versión**: Git ya versiona el contenido. Una
SPEC ejecutada es inmutable; una SPEC aún no ejecutada se revisa como archivo `-R1` nuevo.

**Verificación en dos etapas.** Todo script y todo flujo lo ejecuta **primero el ejecutor**,
sobre estado limpio, y cita la salida en su reporte. Solo entonces el PO lo corre como
aceptación final. **El PO jamás es el primero en correr algo.**

**Toda SPEC actualiza [`ESTADO.md`](ESTADO.md) al cerrar.** Un `ESTADO.md` desactualizado es
un bug del material, no un descuido.

## Roles

- **Product Owner** (Rodrigo): aprueba las SPEC y decide los commits de fondo.
- **Arquitecto**: emite las SPEC.
- **Ejecutor**: las ejecuta, registra la evidencia en el repo y maneja git/GitHub.

Decisiones de diseño y su razón: [`docs/decisiones.md`](docs/decisiones.md).
