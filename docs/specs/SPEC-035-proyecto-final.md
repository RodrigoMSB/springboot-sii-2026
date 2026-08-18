# SPEC-035 · El proyecto final — recuperar la evaluación

**Emite:** Arquitecto · **Ejecuta:** mocito
**Fecha:** 18 de agosto de 2026
**Rama:** `spec-035-proyecto-final` desde `main` (v1.0.1) · PR contra `main`
**Prefijo de commits:** `SPEC-035: <qué>`
**Autorización:** merge y tag sin firma del PO.

---

## 0 · Qué pasó y qué se hace

El mapa de trazabilidad (SPEC-034) destapó la brecha que impide cerrar el contrato: **el proyecto
final integrador no existe en el material nuevo.** El contrato pesa la evaluación en
`Proyecto final 50 % · Conocimientos 30 % · Ejercicios 20 %`, y los catorce labs son construcción
guiada: nadie los aprueba ni los reprueba.

El instrumento existía en el antiguo `lab-13-capsula-y-egreso` y se fue de arrastre al retirar el
arco viejo en la SPEC-033. Ninguna SPEC decidió eliminarlo: fue un descuido del Arquitecto.

**Esta SPEC recupera la evaluación y la adapta al arco nuevo.**

## 1 · El material de origen (recuperar, no reinventar)

De `material-v0.8.0`: la rúbrica, el brief, la plantilla de reporte, la solución de referencia y la
guía de defensa. **Leerlos enteros antes de adaptar.**

## 2 · El proyecto: `proyecto-final/`

```
proyecto-final/
├── README.md · brief/ · rubrica/ · plantillas/
├── base/                        proyecto Maven de arranque, sin la lógica del encargo
└── instructor/                  referencia + guía de defensa. NO va al repositorio
```

Puertos 8107 (base) / 8108 (referencia). El encargo debe poder resolverse con **lo que el arco
nuevo enseñó y sólo eso**.

⚠️ **Verificar que el encargo es resoluble en el tiempo que el contrato le asigna.**

## 3 · La rúbrica adaptada

Tres ejes —Correctitud, Oficio, Criterio— y el umbral: núcleo verde Y Criterio ≥ Suficiente.
Reescribir los descriptores contra lo que sí existe, conservando las dos señales de alarma (prueba
que no puede fallar · suite flaky) y diciendo **cómo** se comprueba cada descriptor.

## 4 · La defensa y el peso contractual

El README debe decir cómo se compone la nota y qué queda sin instrumento.

## 5 · Verificación

| # | Prueba | Criterio |
|---|---|---|
| V1 | `base/` arranca | limpio |
| V2 | La referencia resuelve el brief | citar endpoints y tests |
| V3 | Cada requisito ↔ el lab que lo enseñó | ningún requisito huérfano |
| V4 | Cada descriptor ↔ cómo se comprueba | sin excepción |
| V5 | Las dos señales de alarma, reproducidas | citar el resultado |
| V6 | `git status` | `instructor/` invisible |
| V7 | Offline | 0 descargas |
| V8 | Tiempo estimado | reportado, con su base |

## 6 · Entregable

`INFORME-SPEC-035`. `ESTADO.md` al día. Mergear y etiquetar.

## 7 · Prohibiciones

- ❌ Tocar los catorce labs.
- ❌ Evaluar contenido que el arco nuevo no enseña.
- ❌ Descriptores que no se puedan comprobar.
- ❌ Reinventar la rúbrica desde cero.
- ❌ Que `instructor/` llegue al repositorio.
- ❌ sudo · LFS · credenciales · ≥95 MB · verde sin salida citada.
