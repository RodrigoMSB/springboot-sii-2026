# Lab 10 · El tablero que mentía

> *«El monitoreo dice que estamos perfecto. Los contribuyentes dicen que no pueden declarar.
> ¿A quién le creo? Un semáforo que siempre está en verde no es un semáforo: es un adorno.
> Haz que me diga la verdad — y que cuando algo se caiga, me diga QUÉ se cayó.»*
>
> — **Carolina Espinoza**

El Lab 09 le enseñó al sistema a **contar lo que hizo**. Hoy aprende a decir **cómo está** — y a
no mentir cuando está mal. El tablero que dejó el practicante responde `UP` mientras el proceso
Java respire, aunque PostgreSQL lleve una hora muerto y todos los endpoints devuelvan 500.

Un health que solo confirma que el proceso existe es un adorno. **Readiness es poder atender, no
estar encendido.**

**Sesión 10** · Módulo 14 — Observabilidad sobre OpenTelemetry, Métricas y Caché · **3 horas**

---

## Lo que vas a hacer

| | Actividad | Tiempo |
|---|---|---|
| 🔪 | **La escena del crimen** — `UP` con la base muerta | 10 min |
| 📚 | Teoría: Actuator, liveness vs readiness, Micrometer, caché | ~40 min |
| ☕ | Descanso | 10 min |
| 🔧 | **Laboratorio: los TODOs** | ~110 min |
| ✅ | Validar, reporte, y la siembra del Lab 11 | 10 min |

## Los TODOs

| | Qué | Dónde | ≈ |
|---|---|---|---|
| **TODO_1** | El health que no miente: indicador propio + liveness/readiness separados | `config/observabilidad/`, `application.yml` | 15 min |
| **TODO_2** | Métricas de negocio: contador de folios y timer de la emisión | `application/EmisionService.java` | 15 min |
| **TODO_3** | Exposición con criterio: lista blanca, y cerrar `/env` en producción | `application.yml`, `application-prod.yml` | 15 min |
| **TODO_4** | Caché con medición: Caffeine, TTL, estadísticas e invalidación | `config/`, `application/` | 15 min |

Los cuatro tests del enunciado —`E1` a `E4`— no los escribes: los lees. `E<n>` corresponde a
`TODO_<n>`. Verifican que readiness caiga nombrando el componente mientras liveness aguanta, que
el contador suba al emitir, que `/actuator/env` no exista, y que el caché ahorre viajes **y** se
invalide cuando el dato cambia.

## El pre-vuelo — mira mentir al tablero

```bash
cd ..
./bin/start-lab.sh --db-caida
```

Levanta la app con su base, la deja sana, y **después** tumba PostgreSQL. Verás
`/actuator/health` respondiendo `UP` mientras `/api/v1/tramites` devuelve 500. Ese es el crimen.
Corre lo mismo con `--dir solucion` y compara: readiness cae y **dice qué se cayó**; liveness
sigue `UP`, porque reiniciar la aplicación no levantaría la base.

Este lab **necesita Docker**. Cuando termines: `./bin/90-validar.sh --dir starter`.

## Para el Instructor

`./bin/91-e2e.sh` antes de la sesión. El guion está en [`INSTRUCTOR.md`](INSTRUCTOR.md).
La siembra del Lab 11 —el reloj con problema de identidad— está en `TEORIA.md §11`.
