# SPEC-FIX-07 · El puerto ocupado, dicho con nombre y apellido

**Emite:** PO (instrucción directa) · **Ejecuta:** mocito
**Fecha:** 18 de agosto de 2026
**Rama:** `fix/puerto-ocupado-con-mensaje` desde `main` (v1.1.1) · PR contra `main`
**Corrige:** SPEC-032 (los labs con base de datos) y SPEC-035 (`proyecto-final/base`)

---

## 0 · Qué se arregla

Si queda un PostgreSQL huérfano de una corrida anterior, el lab no arranca y lo que el alumno
lee **no menciona ni el puerto ni Postgres**. Según qué haya sobrevivido, ve una de dos cosas:

- si sobrevivió también la JVM, `Failed to start bean 'webServerStartStop'` — que habla del
  puerto HTTP, no de la base;
- si sobrevivió sólo el motor, `could not lock .datos-pg/epg-lock`, enterrado bajo cinco
  `BeanCreationException` anidadas.

Ninguno de los dos apunta a la causa. El alumno concluye que rompió su código.

## 1 · Alcance

Una clase nueva, `PuertoLibre`, en los **20 proyectos con PostgreSQL embebido**: los seis labs
con base de datos (04, 05, 06, 07, 09, 11) en sus tres carpetas, `proyecto-final/base` y la
solución de referencia.

- `exigir(int puerto)` se llama **antes** de `EmbeddedPostgres.builder()`. Si el puerto está
  tomado, imprime el diagnóstico por `System.err` y termina con `System.exit(1)`.
- Se comprueba con un `ServerSocket` de prueba sobre `localhost`, con `setReuseAddress(false)`.
- El mensaje dice **el puerto**, **la causa probable**, que **no es un error de su código**, y
  **el comando exacto** para cerrarlo — sólo el de su sistema operativo, elegido con `os.name`.
- Sin acentos ni `ñ`: la consola de Windows no es UTF-8 por defecto.

## 1.1 · Y un choque de puertos que esto destapó

`lab-09-seguridad/practica` declaraba `PUERTO_BASE = 55441`, **el mismo de su `solucion/`**. Su
`README.md` siempre dijo 55440. Con las dos carpetas levantadas no fallaba nada: la segunda se
conectaba **a la base de la primera** y compartían usuarios en silencio. Se corrige a 55440, que
es lo que el material documenta.

## 2 · Verificación

| # | Prueba | Criterio |
|---|---|---|
| V1 | Huérfano real y rearranque | citar el mensaje, 0 arranques, 0 `BeanCreationException` |
| V2 | El comando que ofrece, ejecutado tal cual | el lab arranca después |
| V3 | Sin falsas alarmas | `solucion/` arranca con el huérfano de `practica/` vivo |
| V4 | Lab 11 (motor bajo demanda) | dos `POST /simulador/base-sana` seguidos: 200 y 200 |
| V5 | Las tres ramas de `os.name` | Windows / Linux / Mac, un solo comando cada una |
| V6 | Los 29 proyectos compilan offline | 0 descargas |
| V7 | Las suites siguen verdes | incluida la solución de referencia |
| V8 | Lab 09: cada carpeta con su base | `practica/` 55440 y `solucion/` 55441, a la vez |

## 3 · Prohibiciones

- ❌ Enterrar el mensaje en un stacktrace.
- ❌ Mostrar el comando de un sistema operativo que no es el del alumno.
- ❌ Cambiar de puerto automáticamente: el puerto fijo es parte del material.
- ❌ Documentación en `practica/` más allá de lo que ya permite la estructura de tres carpetas.
