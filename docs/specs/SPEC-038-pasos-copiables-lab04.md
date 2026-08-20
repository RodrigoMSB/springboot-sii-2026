# SPEC-038 · `PASOS.md` con el código listo para pegar — piloto en el Lab 04

**Emite:** Arquitecto · **Ejecuta:** mocito
**Fecha:** 19 de agosto de 2026
**Rama:** `spec-038-pasos-copiables-lab04` desde `main` (v1.2.0) · PR contra `main`
**Prefijo de commits:** `SPEC-038: <qué>`
**Autorización:** merge y tag sin firma del PO.

> **Ejecutada.** Informe en `docs/specs/informes/INFORME-SPEC-038.md`. **Es un piloto:** extenderlo
> al resto de los labs es decisión del PO, y el informe §6 da los números para tomarla.

---

## 0 · El problema que resuelve

En clase, el PO comparte pantalla y escribe con los alumnos en `practica/`. Su fuente hasta
ahora es `instructor/`, que está documentado línea por línea — excelente para preparar y para
responder un *por qué*, pero **imposible de usar en vivo**: hay que encontrar el código entre los
comentarios y decidir sobre la marcha qué parte va.

Sus palabras: *«en clases no tengo tiempo de pensar en nada que no sea lo que estoy pasando»*.

La solución: **el `PASOS.md` trae, en cada paso, el bloque exacto que se escribe.** El instructor
lo tiene abierto en una ventana, `practica/` en la otra, y copia de arriba a abajo. `instructor/`
queda para preparar la clase y para el *por qué* cuando alguien pregunta.

Sirve además al alumno que se atrasó o se perdió: hoy el PASOS le dice qué hacer, pero no exacta-
mente qué escribir.

**Esta SPEC es un piloto: solo el Lab 04 (jpa).** Si convence, se aplica al resto.

---

## 1 · El formato de cada paso

Lo que hoy tiene el `PASOS.md` se conserva (qué se explica, qué se corre, qué sale en consola).
**Se agrega el bloque a pegar**, y con reglas duras:

- **El bloque es exactamente lo que va en el archivo.** Nada de fragmentos que haya que adaptar,
  ni `...`, ni pseudocódigo. Si hay que pegarlo tal cual, tiene que compilar tal cual.
- **Los imports que ese paso necesita van con él.** Si el paso agrega un método que requiere un
  import nuevo, el import aparece — indicando que va arriba, no dentro del método.
- **Dice el archivo y dónde va dentro de él**: «reemplaza el `// escribe aquí` del método
  `guardar()`», «agrega este método al final de la interfaz». Sin ambigüedad.
- **Un solo bloque por paso** siempre que se pueda. Si un paso toca dos archivos, dos bloques
  claramente rotulados.
- El bloque va **antes** de la sección «en consola», que es lo que se mira después de pegar.

## 2 · Verificación de que el bloque de verdad sirve

**La regla de fondo: el código de los bloques se extrae de `solucion/`, no se escribe a mano.**
Un bloque tecleado por separado se desincroniza en cuanto alguien toque la solución.

Y hay que probarlo como se usa: **partir de `practica/` limpia, pegar solo los bloques del
`PASOS.md` en el orden que dice, y comprobar que el resultado compila y hace lo mismo que
`solucion/`.** Si al pegar hace falta un import que el paso no menciona, o hay que mover algo de
sitio, **el paso está mal escrito** y se corrige.

## 3 · Lo que NO cambia

- `practica/` sigue sin documentación (firma, línea imperativa, `// escribe aquí`).
- `solucion/` sigue con poca.
- `instructor/` sigue con todo, y sigue fuera del repositorio. **No se toca en esta SPEC.**
- El lab no cambia de comportamiento: los mismos pasos, los mismos números, la misma duración.

## 4 · Verificación

| # | Prueba | Criterio |
|---|---|---|
| V1 | Pegar los bloques en `practica/` limpia, en orden, sin abrir `solucion/` | Compila y arranca en cada paso donde el guion dice que debe arrancar |
| V2 | El resultado final vs `solucion/` | Idéntico salvo comentarios. Citar el diff |
| V3 | Las 8 demos del lab tras pegar | Misma salida que `solucion/`, con su SQL |
| V4 | Cada bloque ↔ su origen en `solucion/` | Extraído, no tecleado. Citar cómo se verificó |
| V5 | Los imports | Ningún paso deja un bloque que no compile por falta de import no mencionado |
| V6 | El lab sin tocar | `practica/`, `solucion/` e `instructor/` sin cambios de código; solo cambia `PASOS.md` |

**V1 es la prueba de esta SPEC.** Es literalmente lo que hará el PO en clase: si pegando los
bloques no sale, el guion no sirve.

## 5 · Entregable

`INFORME-SPEC-038` con el `PASOS.md` resultante citado en al menos dos pasos completos (uno
simple y uno que toque dos archivos), el diff de V2, y una nota sobre cuánto creció el documento
— si se vuelve inmanejable de leer en clase, decirlo, que es información para decidir si se
aplica al resto.

## 6 · Prohibiciones

- ❌ Tocar `practica/`, `solucion/` o `instructor/` del Lab 04, ni ningún otro lab.
- ❌ Bloques con `...`, pseudocódigo, o que haya que adaptar.
- ❌ Bloques tecleados a mano en vez de extraídos de `solucion/`.
- ❌ Cambiar los pasos, su orden, su contenido o la duración del lab.
- ❌ sudo · LFS · credenciales · ≥95 MB · verde sin salida citada.
