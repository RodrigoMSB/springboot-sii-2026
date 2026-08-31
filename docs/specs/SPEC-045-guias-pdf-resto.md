# SPEC-045 · Las guías en PDF, los once labs restantes

**Emite:** el PO · **Ejecuta:** el mocito
**Fecha:** 29 de agosto de 2026
**Rama:** `spec-045-guias-pdf-resto` desde `main` · PR contra `main`
**Prefijo de commits:** `SPEC-045: <qué>`
**Autorización:** commit, PR, merge y tag sin firma del PO. Trabaja de corrido, sin interrumpirlo.

---

## 0 · De dónde sale esto

La SPEC-044 produjo las guías del alumno para los labs 00, 01, 02 y 03, y el PO aprobó el formato
**tal como quedó**. Esto lo extiende a los once que faltan: **04, 05, 06, 07, 08, 09, 10, 11, 12,
13 y 14**.

---

## 1 · Alcance

**Mismo formato, mismas reglas y misma verificación que en los cuatro ya hechos.**

**El formato no se cambia** salvo que un lab lo exija. **Si lo exige, se dice en el informe.**

---

## 2 · Las dos cosas que en estos labs pesan más

### 2.1 · Las metáforas

El **04** y el **05** ya tienen la suya en las guías del instructor —**el archivador y el
archivista**, las fichas que se apuntan entre ellas— y son la continuación natural de la oficina
del 00 al 03.

**Se sigue ese mundo mientras el dominio lo permita.** Donde ya no dé, se estrena una y **se
explica por qué en el informe**.

> **Un mundo que crece vale más que once analogías sueltas.**

### 2.2 · Los «Si te atascas»

Estos labs tienen **bases de datos, puertos, procesos huérfanos y cuatro servicios a la vez**. Ahí
el troubleshooting deja de ser un adorno: **es lo que decide si un alumno sigue solo o se queda
parado**.

**Se reproducen midiendo**, como en la SPEC-044. Los que el PO nombra expresamente:

- el puerto ocupado
- el candado del directorio de datos tomado
- la aplicación corriendo dos veces
- el `LazyInitializationException`
- el guion seguido al pie de la letra

---

## 3 · Las reglas, heredadas de la SPEC-044

- **Es del alumno, se sigue sin instructor al lado.** Nada de notas de conducción.
- **Los bloques de código salen de `solucion/`**, extraídos, no tecleados (`D-044-1`). Los estados
  intermedios, de `PASOS.md`, y declarados.
- **Las salidas de consola son reales.** Lo que varíe entre corridas, se dice en el documento.
- **Nada cuya sangría sea el significado se pega: se edita** (`D-044-2`).
- **El «Si te atascas» se gana midiendo.**
- No se toca `practica/`, `solucion/`, `instructor/`, `PASOS.md` ni `README.md`. **Solo se producen
  PDF y su fuente.**

## 4 · Verificación

| # | Prueba | Criterio |
|---|---|---|
| V1 | Cada bloque del PDF ↔ `solucion/` | Extraído, no tecleado. Los intermedios, declarados |
| V2 | Seguir una guía de punta a punta sobre `practica/` limpia | Se llega al resultado |
| V3 | Las salidas citadas | De una corrida real |
| V4 | Los «Si te atascas» | Reproducidos, con el error literal. Al menos dos por lab |
| V5 | Los PDF | Abren, el índice apunta a las páginas correctas, se leen |
| V6 | El material | `git status` no muestra cambios en `labs/` |

## 5 · Entregable

**Informe único al final**, con **las metáforas elegidas**, **los errores reproducidos** y **las
páginas por guía**. Si un lab obligó a tocar el formato, se dice.

**Si un lab se atasca, se deja, se sigue con el siguiente y se reporta.**
