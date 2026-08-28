# SPEC-042 · Respaldo de `instructor/` en repositorio privado

**Emite:** el PO · **Ejecuta:** mocito · **Fecha:** 27 de agosto de 2026

---

## 1 · El problema

Los **102 bloques** de la SPEC-041 —y todo el contenido de `instructor/` de los quince labs—
existen **solo en la máquina del PO**. `labs/*/instructor/` está en el `.gitignore` por D-031-2,
así que **no hay respaldo**: un disco que falla, o un `git clean -xdf`, se lleva el material que
más trabajo tiene encima.

Lo dejó anotado el informe de la SPEC-041, §6, con tres salidas posibles. El PO eligió.

## 2 · La decisión del PO

**Repositorio privado aparte, solo para `instructor/`.** Mantiene el material fuera del alcance
del alumno y deja de depender de una sola máquina.

**D-031-2 no cambia:** el repositorio público sigue sin llevar `instructor/`.

## 3 · Qué hacer

1. **Crear el repositorio privado** —el ejecutor propone el nombre, algo como
   `springboot-sii-2026-instructor`— y **verificar que quede privado, no público**. Es lo único
   que no puede fallar aquí.
2. **Subir el contenido de los quince `instructor/`**, con la estructura de carpetas que permita
   reponerlos en su sitio.
3. **Un `README.md`** con: qué es, por qué está separado, cómo se restaura sobre un clon fresco
   del repositorio público, y cómo se sincroniza después de cambiarlo.
4. **Los `target/` heredados no se suben** (el hallazgo del INFORME-SPEC-041 §7): son basura, y
   este es el momento de no arrastrarla. **Se borran también del disco**, que es lo que
   corresponde ahora que hay respaldo.
5. **El mecanismo de sincronización, lo más simple posible** — un script en `tools/` que copie en
   las dos direcciones, o lo que le parezca mejor al ejecutor. Se documenta cuál se eligió y por
   qué.

## 4 · Verificación exigida

- Que el repositorio **sea privado**.
- Que el contenido subido sea **idéntico** al del disco.
- Que **la restauración funcione de verdad**: probada sobre un **clon fresco en `/tmp`**.

## 5 · Entrega

Trabajo de corrido. Commit y push por el ejecutor. Informe al terminar.
