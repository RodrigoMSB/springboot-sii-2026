# Lab 09 · Caja negra

> *«Ayer se emitió el folio 4471 a un contribuyente que no correspondía. No te pido que
> arregles el folio —ya lo anulamos—. Te pido que la próxima vez pueda responderle al
> fiscalizador en cinco minutos, no en cinco horas. Quiero un sistema que sepa contar lo
> que hizo.»*
>
> — **Carolina Espinoza**

El crimen ya ocurrió. No hay código roto que arreglar: hay una **verdad que encontrar** en
el ruido. El sistema del practicante es mudo — texto plano, sin correlación, un `catch` que
traga el error. Grepear un folio en 400 MB de logs entrelazados de 30 peticiones simultáneas
es buscar una aguja en un pajar hecho de agujas.

Hoy el sistema aprende a **contar lo que hizo**: cada petición con su hilo (`traceId`), cada
línea en JSON, cada invocación al dominio auditada sin ensuciarlo.

**Sesión 9** · Módulo 10 (cierre, 0,5 h) + Módulo 11 (observabilidad y archivos, 2,5 h) · **3 horas**

---

## Lo que vas a hacer

| | Actividad | Tiempo |
|---|---|---|
| 🔪 | **La escena del crimen** — grep inútil sobre el muro entrelazado | 10 min |
| 📚 | Teoría: niveles de log, JSON, MDC, AOP, y las trampas de los archivos | ~40 min |
| ☕ | Descanso | 10 min |
| 🔧 | **Laboratorio: los TODOs** | ~110 min |
| ✅ | Validar, reporte, y la siembra del Lab 10 | 10 min |

## Los TODOs

| | Qué | Dónde | ≈ |
|---|---|---|---|
| **TODO_1** | El hilo de Ariadna: un `traceId` por petición en el MDC | `config/observabilidad/` | 15 min |
| **TODO_2** | Log estructurado en JSON (formato nativo de Boot) | `application.yml` | 15 min |
| **TODO_3** | El auditor invisible: un `@Aspect` que audita el dominio sin tocarlo | `config/observabilidad/` | 15 min |
| **TODO_4** | Los adjuntos con desconfianza: MIME real, path traversal, streaming | `application/`, `web/`, `infrastructure/archivo/` | 15 min |

Los cuatro tests del enunciado —`E1` a `E4`— no los escribes: los lees. Verifican el `traceId`,
el JSON, la auditoría (con su enmascarado y el límite del proxy), y la validación de archivos.

## El pre-vuelo — busca la aguja

```bash
cd ..
./bin/start-lab.sh --dir starter --caos
```

Verás 30 peticiones a la vez y un log donde buscar a mano es imposible: sin `traceId`, no hay
forma de saber qué líneas son de la misma operación. En la solución, el mismo caos: filtras por
un `traceId` y reconstruyes la operación completa, aislada.

Este lab **necesita Docker**. Cuando termines: `./bin/90-validar.sh --dir starter`.

## Para el Instructor

`./bin/91-e2e.sh` antes de la sesión. El guion está en [`INSTRUCTOR.md`](INSTRUCTOR.md).
La siembra del Lab 10 —el reloj con problema de identidad— está en `TEORIA.md §11`.
