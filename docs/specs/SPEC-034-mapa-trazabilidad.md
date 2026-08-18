# SPEC-034 · El mapa de trazabilidad, rehecho contra el arco nuevo

**Emite:** Arquitecto · **Ejecuta:** mocito
**Fecha:** 18 de agosto de 2026
**Rama:** `spec-034-mapa-trazabilidad` desde `main` (v1.0.0) · PR contra `main`
**Prefijo de commits:** `SPEC-034: <qué>`
**Autorización:** merge y tag (patch) sin firma del PO.

---

## 0 · Por qué esto no es un cambio de nombres

`docs/temario/MAPA-LAB-MODULO.md` es **el documento de trazabilidad que se entrega al SII**:
demuestra que cada módulo contratado tiene laboratorio. Está escrito contra el arco antiguo, que
ya no existe.

El problema de fondo: el arco nuevo **no es el viejo con otros nombres**. Cambió lo que se
enseña. Hay temas del contrato que el material nuevo ya no cubre —al menos gRPC, AOP,
Testcontainers, mensajería/colas, Liquibase, migraciones— y hay contenido nuevo que el temario
no pedía con ese peso.

Por lo tanto: **el trabajo no es cuadrar la tabla, es medir la cobertura real y declarar los
huecos con honestidad.** Un mapa que diga que todo está cubierto cuando no lo está es peor que
no tener mapa: es un problema contractual esperando a la entrega.

**Esta SPEC no cambia ni un lab.** Solo mide y documenta.

---

## 1 · Fuente de verdad

`docs/temario/TEMARIO-SPRING-BOOT-SII-v3.md` — los 15 módulos, los 35 temas (I–XXXV) y la
matriz módulo × sesión. **Donde el material y el temario discrepen, manda el temario** y la
discrepancia se declara. No se reinterpreta el temario para que calce.

## 2 · Trabajo

### 2.1 · Inventario de cobertura real

Leer los 14 labs (README, PASOS y el código de `solucion/`) y, para cada uno, determinar qué
módulos y qué temas cubre **de verdad**. No por el nombre del lab: por lo que el alumno hace.

Tres niveles: **Cubierto** (lo practica) · **Mencionado** (aparece, no se practica) ·
**No cubierto**.

### 2.2 · El mapa nuevo

1. Tabla lab → módulo(s) → temas → horas, con la numeración nueva.
2. Tabla inversa módulo → dónde se cubre.
3. Sección de brechas explícita.
4. Contenido nuevo que el temario no pedía.
5. Las discrepancias estructurales de sesiones y horas.

### 2.3 · Coherencia del resto de `docs/temario/`

Revisar `README.md` y cualquier otro documento que cite la numeración vieja. **El `.docx` y el
`.md` del temario contratado NO se tocan.**

---

## 3 · Cómo se declara una brecha (formato obligatorio)

| Módulo/tema | Nivel | Dónde estaba | Qué haría falta |
|---|---|---|---|

## 4 · Verificación

| # | Prueba | Criterio |
|---|---|---|
| V1 | Los 15 módulos en la tabla inversa | Ninguno omitido |
| V2 | Los 35 temas rastreados | Cada uno con su nivel; citar el conteo |
| V3 | Cada «cubierto» con respaldo | El lab y el paso concreto. Sin respaldo, no es cubierto |
| V4 | `grep` de numeración vieja en `docs/temario/` | Cero, salvo el temario contratado y los históricos |
| V5 | Las brechas | Todas con las cuatro columnas del §3 |

## 5 · Entregable

`INFORME-SPEC-034` con el conteo de cobertura y la lista de brechas en el resumen.
`ESTADO.md` al día.

## 6 · Prohibiciones

- ❌ Tocar cualquier lab.
- ❌ Tocar el temario contratado.
- ❌ Declarar «cubierto» sin poder señalar el lab y el paso.
- ❌ Reinterpretar el temario para que calce con el material.
- ❌ Omitir una brecha por incómoda.

---

## 7 · Ajuste anotado durante la ejecución

**Se usa un cuarto nivel, «Parcial».** Con los tres de §2.1 habría que redondear: llamar
«cubierto» a un tema al que le falta la mitad (M9 sin CORS ni cabeceras) o «no cubierto» a uno
cuyo núcleo sí se practica (M6 con slices pero sin Testcontainers). Las dos cosas serían falsas, y
la SPEC existe para no redondear. Queda declarado en el mapa §0 y en el informe §7.a.
