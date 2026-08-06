# Lab 04 · El árbol de trámites

> *«Hoy no vinimos a apagar un incendio. Vinimos a entender qué compramos cada vez que
> escribimos una anotación de fetch. El que no declara el fetch, lo está declarando igual —
> solo que no sabe cuál.»*
>
> — **Carolina Espinoza**

Hoy no hay nada roto. La app funciona, los tests pasan. Pero pide un solo trámite y mira el
log SQL: **medio árbol de la base de datos viaja para responder una ficha.** Todo está en
`FetchType.EAGER`, "para que no salga más el `LazyInitializationException`". Nadie lo ha
notado. Ese es el punto.

**Sesión 4** · Módulo 5, primera parte (persistencia) · **3 horas**

---

## Lo que vas a hacer

| | Actividad | Tiempo |
|---|---|---|
| 🔪 | **La escena del crimen** — el muro de JOINs por pedir una ficha | 10 min |
| 📚 | Teoría: relaciones, fetch, cascade, repositorios, JPQL, `JdbcClient` | ~40 min |
| ☕ | Descanso | 10 min |
| 🔧 | **Laboratorio: 4 TODOs** | ~110 min |
| ✅ | Validar, reporte, y la siembra del Lab 05 | 10 min |

## Los cuatro TODOs

| | Qué | Dónde | ≈ |
|---|---|---|---|
| **TODO_1** | Corregir las relaciones a LAZY e **instalar AU-04** (el guardián del fetch) | `domain/entity/`, `arquitectura/` | 15 min |
| **TODO_2** | Consultas derivadas (el nombre del método es la consulta) | `infrastructure/repository/` | 15 min |
| **TODO_3** | JPQL multi-entidad con `@Query` (sin `JOIN FETCH` — todavía) | `infrastructure/repository/` | 15 min |
| **TODO_4** | Un reporte agregado con `JdbcClient`, sin tocar entidades | `application/` | 15 min |

## El pre-vuelo — vive el crimen

```bash
cd ..
./bin/start-lab.sh --dir starter
curl http://localhost:8099/api/v1/tramites/1
# y ahora mira el log:
grep -A20 'select' .estado/dgt.log | head -40
```

Cuenta las tablas que viajaron. Guárdate esa cara de "y qué importa".

Este lab **necesita Docker** (los tests prueban contra una base real). Cuando termines:

```bash
cd .. && ./bin/90-validar.sh --dir starter
```

## Para el Instructor

`./bin/91-e2e.sh` antes de la sesión. El guion del crimen (el muro de JOINs proyectado) está
en [`INSTRUCTOR.md`](INSTRUCTOR.md). La **siembra del Lab 05** es la más importante del curso
hasta ahora: no la improvises, está en `TEORIA.md §11`.
