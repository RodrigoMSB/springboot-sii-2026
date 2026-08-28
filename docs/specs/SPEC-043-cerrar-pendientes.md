# SPEC-043 · Cerrar los pendientes del material

**Emite:** el PO · **Fecha:** 28 de agosto de 2026 · **Ejecuta:** el mocito
**Estado de partida:** `main` en `material-v1.5.0`, CI en verde.

---

## Instrucción de trabajo

Cuatro frentes. **Se trabaja de corrido, sin interrumpir al PO.** Las decisiones dudosas se
resuelven **por lo más conservador** y se anotan en el informe. **Si un frente se atasca, se deja,
se sigue con el siguiente y se reporta.** El ejecutor mergea y etiqueta él mismo, por frente o al
final, según convenga.

**Informe único al final**, con lo hecho por frente y lo que quedó fuera. **El frente 1 va primero
en el informe**, con el tiempo medido de resolución.

---

## Frente 1 · El examen de completar huecos — lo más urgente

**El problema.** Al PO le quedan **tres o cuatro clases**. El `proyecto-final/` actual no sirve
para eso: es sobre la DGT, **nunca se anunció**, y su resolución estimada son **tres horas**.

**Lo que se construye.** Una alternativa: **una aplicación pequeña que compila y arranca**, con
**10 a 12 huecos marcados** que el alumno completa. **No se escribe desde cero** — es coherente con
cómo se enseñó todo el curso.

**Qué tiene que cubrir**, de los labs 01 al 09:

- entidad con relación
- repositorio con consulta derivada
- servicio
- controller con DTO
- manejo de errores
- seguridad por rol
- configuración

**Las tres cosas que lo distinguen de un lab:**

1. **No lleva `PASOS.md`.** Cada hueco dice **qué** debe hacer, no **cómo**.
2. **Cada hueco tiene su test**, para que el alumno sepa si está listo **antes de entregar**.
3. **El puntaje es directo**: huecos resueltos sobre el total.

**La medida que manda.** Se mide cuánto tarda en resolverse. **Objetivo: 60 a 90 minutos.** Si se
pasa, **se recorta y se reporta**.

**Lo que no se toca.** El `proyecto-final/` actual **no se borra**: queda como está. **El PO decide
el lunes cuál usa.**

---

## Frente 2 · La documentación del porqué en `instructor/`, labs 08 al 14 y el examen nuevo

Mismo criterio de la **SPEC-041**, que cubrió los labs 04 a 07 (`D-041-1`): **qué hace, qué
alternativas existen, por qué ésta aquí, y en qué caso elegirías otra.**

**Se reusa el formato** —el recuadro `POR QUÉ ·`— **y el arnés que verifica que el código no se
mueve ni un carácter** (el despojador de comentarios de la SPEC-041 §4.1).

---

## Frente 3 · La prueba de pegado en los labs 10, 11, 12 y 13

La **SPEC-039** verificó el **contenido** de los bloques pero no la **colocación**. Un bloque
correcto en el sitio equivocado no compila. Es la deuda declarada en `INFORME-SPEC-039` §5.

**Se hace la V1 completa** —pegar los bloques en `practica/` limpia, en orden, y comprobar que
compila y llega a `solucion/`— **y se corrige lo que aparezca.**

---

## Frente 4 · El respaldo

Al terminar, **sincronizar `instructor/` con el repositorio privado** (`D-042-1`,
`tools/instructor-respaldo.sh respaldar`). Hay trabajo nuevo de los frentes 1 y 2 que, si no, **existe
en una sola máquina**.
