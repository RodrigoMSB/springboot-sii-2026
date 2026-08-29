# SPEC-044 · Las guías en PDF — el laboratorio que el alumno puede hacer solo

**Emite:** Arquitecto · **Ejecuta:** mocito
**Fecha:** 28 de agosto de 2026
**Rama:** `spec-044-guias-pdf` desde `main` · PR contra `main`
**Prefijo de commits:** `SPEC-044: <qué>`
**Autorización:** merge y tag sin firma del PO. Trabaja de corrido, sin interrumpirlo.

---

## 0 · De dónde sale esto

El PO dicta otro curso —Kafka— cuyas guías son PDF que el alumno sigue por su cuenta, y que
funcionan. Quiere lo mismo aquí. Su instrucción, textual: **más tiempo para analogías de la vida
cotidiana, plantear el problema y la solución que ofrece Spring Boot.**

Hoy el material tiene `README.md` (qué se aprende), `PASOS.md` (el guion con los bloques) y los
140 recuadros de `instructor/` (el porqué). Está todo el contenido; lo que no hay es **un
documento que el alumno pueda seguir solo, sin el instructor al lado**.

**Esta SPEC no inventa contenido: lo reordena y le agrega lo que falta.**

---

## 1 · Qué hace bueno al PDF de referencia — copiar esto

Del PDF de Kafka (lab 02), tres cosas que las guías actuales no tienen y que sí se copian:

1. **Una metáfora que se sostiene el laboratorio entero.** No una analogía suelta por concepto:
   un mundo que se reutiliza y crece. En Kafka son los camiones, el jefe de flota, el nombre
   pintado en la puerta. Se vuelve a ella cada vez que aparece algo nuevo.
2. **«Vas bien si…»** al cierre de cada paso, con algo comprobable. El alumno sabe solo si va
   bien, sin preguntarle a nadie.
3. **«Si te atascas»** con las causas **ordenadas por frecuencia**, cada una con el error literal
   que va a ver en pantalla. No teoría de fallos: el texto exacto y qué hacer.

Y una cuarta que ese PDF **no** tiene y aquí sí va, porque es lo que el PO pidió:

4. **La alternativa que se descartó.** No basta con «Spring hace esto». Va también: cómo se hacía
   antes o cómo podrías hacerlo hoy, y por qué no. Ese material ya existe — son los 140 recuadros
   `POR QUÉ ·` de `instructor/`, que están escritos exactamente con esa estructura.

## 2 · La estructura de cada guía

**Portada** — nombre del curso, número y título del lab, una frase que diga qué se demuestra,
duración estimada, versión del stack.

**Antes de empezar** — qué se logra (dos párrafos, en cristiano), qué hay que tener listo (tabla:
requisito · cómo lo compruebas), y los comandos de puesta a punto.

**El caso** — el problema en lenguaje de negocio, y **la metáfora del lab**, presentada aquí y
reutilizada después.

**Los pasos**, cada uno con:

| Sección | Qué lleva |
|---|---|
| Qué vamos a hacer | dos o tres líneas |
| Para entenderlo mejor | la metáfora aplicada a **este** paso |
| El problema | qué duele sin esto. Antes de la solución, siempre |
| Cómo se hacía antes / la alternativa | y por qué se descartó — de los recuadros de `instructor/` |
| Se pega | el bloque exacto, con su archivo y su sitio |
| Lo que vas a ver | la salida real, recortada a lo que importa |
| Vas bien si… | algo comprobable |
| Si te atascas | causas por frecuencia, con el error literal |

**Lo que aprendiste** — cuatro puntos numerados, cada uno con su párrafo. No una lista de viñetas.

**Para profundizar** — lo que no entra en clase pero se puede hacer con lo que quedó montado.

**Antes de cerrar** — cómo dejar la máquina limpia y qué se lleva.

## 3 · Reglas duras

- **Es del alumno, se sigue sin instructor al lado.** Nada de «di en voz alta», «pregunta a la
  clase» ni notas de conducción. Eso vive en las guías del instructor y no se mezcla.
- **Los bloques de código salen de `solucion/`**, no se teclean. Es la regla de la SPEC-038 y
  aquí vale igual: un bloque tecleado aparte se desincroniza en cuanto alguien toque la solución.
- **Las salidas de consola son reales**, copiadas de una corrida, no inventadas. Donde varíen
  entre corridas (ids, puertos, tiempos), se dice en el documento.
- **Una sola metáfora por lab**, y coherente con la del lab anterior donde el dominio siga siendo
  el mismo. El lab 04 ya tiene el archivador y el archivista: el 05 continúa ese mundo, no
  estrena otro.
- **El «Si te atascas» se gana midiendo.** Rompe a propósito lo que un alumno rompería —el
  paquete mal escrito, el import que falta, el puerto ocupado, la app corriendo dos veces— y
  copia el error que sale. Un troubleshooting inventado es peor que no tenerlo.
- No se toca `practica/`, `solucion/`, `instructor/`, `PASOS.md` ni `README.md`. **Esta SPEC solo
  produce PDFs.**

## 4 · Alcance y entrega

**Cuatro labs en esta SPEC: 00, 01, 02 y 03.** Son los que el PO ya dictó, así que sabe cómo
salieron y puede juzgar si la guía sirve. Si el formato convence, los demás van en otra SPEC.

⚠️ **Haz el 00 completo primero y párate ahí a revisarlo tú** contra las seis reglas del §3 antes
de escribir los otros tres. Un formato malo replicado cuatro veces es cuatro veces el trabajo de
arreglarlo.

**Dónde van:** `docs/guias/` en el repositorio — estas sí viajan, son del alumno. Commitea el
**fuente** (el generador o el markdown) y el PDF. Si el PDF pesa, dilo y decidimos.

Genera con lo que ya usaste para el PDF de estudio; el resultado importa más que la herramienta.

## 5 · Verificación

| # | Prueba | Criterio |
|---|---|---|
| V1 | Cada bloque de código del PDF ↔ `solucion/` | Extraído, no tecleado. Citar cómo se verificó |
| V2 | Seguir la guía del 00 de punta a punta sobre `practica/` limpia | Se llega al resultado, sin abrir `PASOS.md` ni `solucion/` |
| V3 | Las salidas citadas | De una corrida real. Citar la corrida |
| V4 | Los «Si te atascas» | Cada causa reproducida, con el error literal. Citar al menos dos por lab |
| V5 | Los cuatro PDF | Abren, el índice apunta a las páginas correctas, se leen en pantalla |
| V6 | El material | `git status` no muestra cambios en `labs/` fuera de `docs/guias/` |

**V2 y V4 son las de fondo.** V2 es lo que hará el alumno; V4 es lo único que no se puede
escribir de memoria.

## 6 · Entregable

`INFORME-SPEC-044`: el formato que quedó, la metáfora elegida por lab y por qué, el resultado de
V2, los errores reproducidos en V4, y cuántas páginas salió cada guía. Si algo del formato no
funcionó al replicarlo, dilo — es información para decidir si sigue a los once labs restantes.

## 7 · Prohibiciones

- ❌ Tocar el material de los labs. Solo se producen PDFs.
- ❌ Notas de instructor en una guía del alumno.
- ❌ Salidas de consola inventadas o troubleshooting sin reproducir.
- ❌ Bloques tecleados a mano en vez de extraídos de `solucion/`.
- ❌ Replicar el formato a los cuatro labs antes de revisar el primero.
- ❌ sudo · LFS · credenciales · ≥95 MB · verde sin salida citada.
