# Lab 05 · Once segundos

> *«Ayer el listado tardó once segundos. Hoy, veintitrés. No agregamos código: agregamos
> trámites. No quiero oír la palabra 'optimizar' hasta que me muestres un número.»*
>
> — **Carolina Espinoza**

La bomba que plantaste la semana pasada explota hoy. Hiciste lo correcto —LAZY explícito— y
ese mismo LAZY, iterado por un listado inocente sobre cincuenta mil lotes, tarda segundos de
reloj y dispara **miles de consultas** para responder UNA página.

Esta es la sesión del cronómetro. Aquí se aprende que *lo correcto también se mide*.

**Sesión 5** · Módulo 5 (cierre, 1,0 h) + Módulo 6 (1,5 h) + Módulo 7 (teoría, 0,5 h) · **3 horas**

---

## Lo que vas a hacer

| | Actividad | Tiempo |
|---|---|---|
| 🔪 | **La escena del crimen** — `time curl` y el número de consultas | 10 min |
| 📚 | Teoría: N+1, medición, `@EntityGraph`, proyecciones, paginación, M6 (integración) | ~40 min |
| ☕ | Descanso | 10 min |
| 🔧 | **Laboratorio: los TODOs** | ~110 min |
| ✅ | Validar, reporte, y la siembra del Lab 06 | 10 min |

## Los TODOs

| | Qué | Dónde | ≈ |
|---|---|---|---|
| **TODO_1** | Hacer volar el listado **sin cambiar lo que devuelve**: una proyección paginada | `infrastructure/repository/`, `application/` | 15 min |
| **TODO_2** | Escribir tú una prueba de integración completa (RestTestClient + Testcontainers) | `src/test/.../integracion/` | 15 min |

Los otros dos tests del enunciado —**el contador** (`E1`) y **el funcional** (`E2`)— no los
escribes: los lees. `E1` mide el costo; `E2` mide el comportamiento. Juntos definen
optimizar: *cambiar el costo sin cambiar el comportamiento.*

## Las dos soluciones (mira esto)

Este lab trae **dos** soluciones de referencia:

- `solucion-con-n1/` — el «antes». Funciona perfecto. Pasa el funcional. **Falla el contador.**
- `solucion/` — el «después». Mismo comportamiento, contador en verde.

Corre el `90` sobre cada una y verás la diferencia. Esa convivencia ES la definición
ejecutable de optimizar: dos códigos que hacen lo mismo, uno cuesta 13 consultas y el otro 3.

## El pre-vuelo — siente los segundos

```bash
cd ..
./bin/start-lab.sh --dir starter --lotes 5000
{ time curl -s -o /dev/null "http://localhost:8099/api/v1/tramites?page=0&size=5000"; }
grep -c '    select' .estado/dgt.log     # cuenta las consultas
```

Este lab **necesita Docker**. Cuando termines: `./bin/90-validar.sh --dir starter`.

## Para el Instructor

`./bin/91-e2e.sh` antes de la sesión. El guion del crimen cronometrado está en
[`INSTRUCTOR.md`](INSTRUCTOR.md). La siembra del Lab 06 —el fiscalizador— está en `TEORIA.md §11`.
