# SPEC-FIX-06 · El Lab 00 sin migrar y el fósil «Lab 3b»

**Emite:** PO (instrucción directa) · **Ejecuta:** mocito
**Fecha:** 18 de agosto de 2026
**Rama:** `fix/lab-00-y-fosil-3b` desde `main` (v1.1.0) · PR contra `main`
**Corrige:** SPEC-033

---

## 0 · Qué se arregla

Dos deudas que la SPEC-033 dejó y que su informe declaró:

1. **`lab-00-hola-mundo/practica` nunca se migró** a la estructura de tres carpetas. Conservaba el
   javadoc y un bloque explicativo de ocho líneas sobre `CommandLineRunner`. No se migró porque el
   PO tenía ahí un cambio local sin commitear y migrarlo lo habría destruido (INFORME-SPEC-033
   §6.e y §8.1). **El PO descartó ese cambio**, así que ya se puede.

2. **El fósil «Lab 3b».** La renumeración de la SPEC-033 mapeaba `03b → 04`, pero el material
   escribía el título como «3b» y el patrón no encajó. Quedaron referencias a un lab con un nombre
   que ya no existe.

## 1 · Alcance

- `practica/` del Lab 00: se quita el bloque largo; queda la firma, **una línea imperativa** y
  `// escribe aquí`.
- «Lab 3b» → «Lab 04» en todo el material vivo, incluidas las copias locales de `instructor/`.
- **Ningún cambio de comportamiento.** Sólo comentarios y texto.

## 2 · Verificación

| # | Prueba | Criterio |
|---|---|---|
| V1 | `lab-00`: `practica/` y `solucion/` arrancan | citar la consola |
| V2 | `lab-04`: `practica/` y `solucion/` arrancan | citar las 8 demos |
| V3 | Sólo cambian comentarios y texto | comparar el código sin comentarios |
| V4 | Cero «Lab 3b» en el repositorio | `grep`, incluido `instructor/` |
| V5 | Los 29 proyectos compilan offline | 0 descargas |

## 3 · Prohibiciones

- ❌ Cambiar el comportamiento de un lab.
- ❌ Documentación en `practica/` más allá de la línea imperativa y el `// escribe aquí`.
