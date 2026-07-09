# SPEC-003 · Versionado del Temario v3 (fuente + build)

| Campo | Valor |
|---|---|
| ID | SPEC-003 |
| Título | Incorporar el Temario v3 al repo: `.md` fuente, `.docx` build |
| Autor | Arquitecto |
| Aprueba | PO (Rodrigo) — **APROBADA** |
| Depende de | SPEC-002 |
| Estado | LISTA PARA EJECUCIÓN (tras cierre de SPEC-002) |

> **Instrucción de ejecución (mocito):** ejecutar **solo después** de cerrar la SPEC-002.
> Primer paso, guardar este archivo íntegro en `docs/specs/SPEC-003-temario-v3.md` y
> commitearlo **antes** de ejecutar cualquier cambio.

---

## 1. Objetivo

Versionar el Temario v3 (estado del arte, julio 2026) producido por el arquitecto, con el
`.md` como fuente de verdad y el `.docx` como build entregable al SII, y registrar sus 13
cambios en `decisiones.md`.

## 2. Insumos

El PO deja en `/Users/rodrigosilva/SII/SPRINGBOOT/incoming/`:

- `TEMARIO-SPRING-BOOT-SII-v3.md`
- `TEMARIO-SPRING-BOOT-SII-v3.docx`

Si `incoming/` no existe o falta un archivo: **DETENTE y pide al PO.**

## 3. Acciones

1. Mover ambos archivos a `docs/temario/`. `incoming/` queda vacío y **no se versiona**
   (agregarlo a `.gitignore` como `incoming/`).

2. Crear `docs/temario/README.md` (10 líneas máx.) declarando:
   - El `.md` es la **FUENTE**; toda modificación futura se hace ahí.
   - El `.docx` es el **BUILD** entregable al SII, generado por el arquitecto. Se
     versiona como **excepción declarada** a la regla "lo regenerable no se versiona":
     el repo no posee (aún) el generador, y el entregable comercial debe ser citable por
     versión exacta.
   - Ante divergencia entre ambos, **manda el `.md`**.

3. **Verificación de coherencia** (nada de memoria) — extraer texto del `.docx`
   (`pandoc -t plain`) y comprobar contra el `.md`:

   | # | Verificación |
   |---|---|
   | a | Ambos declaran "Java 25 LTS" y "Spring Boot 4.1.x" |
   | b | Ambos contienen los 15 módulos y el total "36,0" |
   | c | Ambos contienen la Matriz Módulo × Sesión con 12 filas S01..S12 |
   | d | La matriz **CUADRA**: suma por sesiones = 36,0 y la suma de horas asignadas a cada módulo = su columna de la tabla de módulos. Verificar con **cálculo real, no a ojo**; citar el desglose completo |
   | e | El anexo tiene 13 filas de cambios |

   Si algo **no** cuadra: reportarlo y **no commitear** el archivo divergente.

4. Registro en `decisiones.md` (una sola fila, no trece):

   | Fecha | Decisión | Razón |
   |---|---|---|
   | (fecha de ejecución) | Se adopta el Temario v3 (Java 25 LTS · Boot 4.1.x · JUnit 6 · Testcontainers 2 · matriz Módulo×Sesión) como temario vigente del curso; el detalle de los 13 cambios vive en el anexo del propio temario. | La v3 actualiza el contenido al estado del arte de julio de 2026 sin alterar ningún compromiso estructural de la propuesta adjudicada (36,0 h · 12×3 h · 35 temas · 50/30/20). |

## 4. Criterios de aceptación

- [ ] SPEC-003 commiteada antes que el temario.
- [ ] `docs/temario/` contiene `.md` + `.docx` + `README.md`.
- [ ] Las 5 verificaciones de coherencia pasan, **con evidencia citada**.
- [ ] `decisiones.md` tiene su fila.
- [ ] `incoming/` en `.gitignore`.
- [ ] Commits con prefijo `SPEC-003:`; push hecho.

## 5. Reporte

Salidas de la verificación 3.d (el desglose numérico completo), `git log --oneline`,
discrepancias si las hubo.
