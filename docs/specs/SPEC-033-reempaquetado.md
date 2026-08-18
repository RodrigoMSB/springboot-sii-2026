# SPEC-033 · Reempaquetado — retirar el arco viejo, el Lab 10, y unificar el formato

**Emite:** Arquitecto · **Ejecuta:** mocito
**Fecha:** 18 de agosto de 2026
**Rama:** `spec-033-reempaquetado` desde `main` (v0.8.0) · PR contra `main`
**Prefijo de commits:** `SPEC-033: <qué>`
**Autorización:** merge y tag sin firma del PO.

---

## 0 · Qué se hace

El arco guiado tiene doce labs (00 a 12, sin el 10) y el material que el alumno usa está
completo. Quedan tres cabos, y los tres dependen entre sí:

1. **El arco viejo sigue en `main`** (labs 07 al 14 del material antiguo). Está obsoleto: todo su
   contenido vive ya en el arco nuevo o fue descartado por el PO.
2. **El Lab 10 no existe** porque su nombre lo ocupa un lab viejo.
3. **Los labs 00 a 06 no tienen la estructura de tres carpetas** — conservan los bloques
   explicativos largos en `practica/` y no tienen `instructor/`.

Esta SPEC cierra los tres, **en este orden**, porque el 2 depende del 1.

---

## 1 · Paso 1 · Retirar el arco viejo

Eliminar de `main` los ocho labs del arco antiguo (`lab-07-el-portero` a
`lab-14-la-dgt-se-parte-en-pedazos`).

**No se borran del historial.** Quedan en los commits anteriores y en los tags `v0.4.0` a
`v0.8.0`, recuperables.

**Antes de borrar, inventariar y reportar** qué depende de ellos: `labs/lib/`, el gate `deriva`,
`dgt-tramites-api`, los manifiestos, el CI, y cualquier documento que los cite. Si algo del
material nuevo depende de algo que vive ahí, **detenerse y reportar** en vez de improvisar.

**Consecuencias esperadas, a resolver en esta SPEC:** el gate `deriva` se queda sin objeto;
`dgt-tramites-api`, `labs/lib/`, los manifiestos y los `bin/` se retiran si nadie los usa; y los
jobs del CI que verificaban el arco viejo pierden objeto y hay que ajustarlos.

**Objetivo de este paso: `main` sin rojos.**

## 2 · Paso 2 · El Lab 10 · Observabilidad

Con el nombre libre, crear `labs/lab-10-observabilidad` con el contenido ya especificado en la
SPEC-032 §4 y las mismas reglas de aquella. Puertos: HTTP 8101 / 8102, Postgres 55442 / 55443.

## 3 · Paso 3 · Unificar el formato de los labs 00 a 06

Los siete labs pasan a la estructura de tres carpetas: `practica/` sin bloques explicativos,
`solucion/` con poca documentación, e `instructor/` con todo — ese contenido **no se pierde**: se
mueve.

⚠️ **Este paso reescribe material que el PO ya probó en clase.** Ningún cambio de comportamiento,
solo de comentarios.

⚠️ El PO tiene cambios locales sin commitear en `lab-00` y `lab-01`. **No tocarlos.** Si hay
conflicto, detenerse y avisar.

## 4 · Paso 4 · La numeración final

Renombrar `lab-03b-jpa` → `lab-04-jpa` y correr el resto en cascada hasta `13 empaquetado`.

**Si esta cascada resulta más frágil de lo previsto**, detenerse, dejar los nombres actuales y
reportar: la numeración es cosmética y no vale romper material probado.

---

## 5 · Verificación

| # | Prueba | Criterio |
|---|---|---|
| V1 | Los 14 labs, `practica/` y `solucion/` | Todos arrancan; citar |
| V2 | Los números del material | **Iguales a antes** |
| V3 | Los siete labs migrados: diff de comportamiento | Solo cambian comentarios |
| V4 | `instructor/` en los 14 | Presente, completo, no ejecutable, invisible para git |
| V5 | `ls labs/` | Solo los 14 nuevos |
| V6 | `grep -r` de referencias al arco viejo | Cero enlaces rotos en material vivo |
| V7 | CI en `main` | Job por job. Sin rojos, o el que quede explicado |
| V8 | Offline y tamaño | 0 descargas; `du -sh labs/*` reportado |
| V9 | `git status` de los cambios locales del PO | Intactos, fuera de todos los commits |

## 6 · Entregable

`INFORME-SPEC-033`, formato de 8 secciones. `ESTADO.md` reescrito. Mergear y etiquetar
`material-v1.0.0` si los cuatro pasos quedan cerrados.

## 7 · Prohibiciones

- ❌ Borrar nada del historial de git.
- ❌ Tocar los cambios locales sin commitear del PO.
- ❌ Cambiar el comportamiento de un lab al migrar comentarios.
- ❌ Apagar un job del CI sin explicar por qué perdió objeto.
- ❌ Documentación en `practica/` más allá de la línea imperativa.
- ❌ Que `instructor/` sea ejecutable o llegue al repositorio.
- ❌ Romper material probado por una renumeración cosmética.
- ❌ sudo · LFS · credenciales · ≥95 MB · verde sin salida citada.
