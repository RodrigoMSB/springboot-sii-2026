# Lab 06 · Dos folios, un número

> *«Un folio emitido dos veces no se borra. Se explica. Ante un fiscalizador. Y de paso:
> el folio 8 no existe — ¿dónde está? Los folios no se saltan. Esto no es una tabla más:
> es un libro foliado.»*
>
> — **Carolina Espinoza**

La semana pasada convertiste "va lento" en un número. Hoy el cronómetro se cambia por un
fiscalizador. La emisión de folios funciona perfecta… con un usuario. Cuando dos
contribuyentes aprietan «emitir» en el mismo milisegundo, los dos leen «el último folio
es el 41», los dos escriben el 42, y tu libro foliado se rompe: un número repetido, o un
salto donde no puede haberlo.

No es un bug que veas con un cronómetro. Solo aparece cuando dos cosas pasan **a la vez**.

**Sesión 6** · Módulo 7 (transacciones y concurrencia, 1,5 h) + Módulo 8 (Flyway a fondo, 1,5 h) · **3 horas**

---

## Lo que vas a hacer

| | Actividad | Tiempo |
|---|---|---|
| 🔪 | **La escena del crimen** — `--concurrencia` y el folio duplicado | 10 min |
| 📚 | Teoría: el proxy transaccional, propagación, aislamiento, bloqueo pesimista vs optimista; Flyway a fondo | ~40 min |
| ☕ | Descanso | 10 min |
| 🔧 | **Laboratorio: los TODOs** | ~110 min |
| ✅ | Validar, reporte, y la siembra del Lab 07 | 10 min |

## Los TODOs

| | Qué | Dónde | ≈ |
|---|---|---|---|
| **TODO_1** | El contador **bloqueado**: tomar el número con `SELECT … FOR UPDATE` | `application/EmisionService.java`, `infrastructure/repository/` | 15 min |
| **TODO_2** | La **idempotencia** (RN-05): mismo trámite → mismo folio, 201 y luego 200 | `application/EmisionService.java` | 15 min |
| **TODO_3** | La **restricción como contrato**: migración `V3` con un `CHECK` en `linea_f29` | `src/main/resources/db/migration/` | 15 min |
| **TODO_4** | La **transacción** que ata todo: emitir es atómico, y un fallo lo revierte entero | `application/EmisionService.java` | 15 min |

Los cuatro tests del enunciado —`E1` a `E4`— no los escribes: los lees. `E1` prueba la
concurrencia real (RN-01 + RN-02), `E2` la idempotencia (RN-05), `E3` el contrato de la
base, `E4` el rollback. Juntos son el criterio, y el mismo criterio juzga tu trabajo y a
la solución de referencia.

> **Ojo con el enganche TODO_1 ⇄ TODO_4:** un candado pesimista solo funciona *dentro* de
> una transacción (se suelta al cerrarla). Por eso `E1` no se pone verde con el bloqueo
> solo: necesita también la transacción. El candado vive en el dato, y la transacción es
> lo que lo sostiene. Los dos, o ninguno.

## El pre-vuelo — siente la carrera

```bash
cd ..
./bin/start-lab.sh --dir starter --concurrencia 2
```

Verás dos emisiones dispararse a la vez sobre trámites distintos. En el starter, tarde o
temprano una se cae con HTTP 500 (`duplicate key`) o dos se llevan el mismo número: la PK
del folio te salvó del duplicado real, pero a costa de tumbar una emisión **válida**. Ese
es el crimen.

Este lab **necesita Docker**. Cuando termines: `./bin/90-validar.sh --dir starter`.

## Para el Instructor

`./bin/91-e2e.sh` antes de la sesión. El guion del crimen está en
[`INSTRUCTOR.md`](INSTRUCTOR.md). La siembra del Lab 07 —el portero— está en `TEORIA.md §12`.
