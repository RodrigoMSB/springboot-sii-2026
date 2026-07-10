# Lab 02 · El folio que se filtró

> *«No te llamé porque hay un bug. Te llamé porque **nada lo impidió**. Arréglalo — y
> después haz que sea imposible repetirlo.»*
>
> — **Carolina Espinoza**

Alguien, con prisa, escribió un endpoint que devuelve la ficha de un contribuyente. Compila,
arranca, responde. Y por el JSON viaja `puntajeRiesgoInterno`: el número con que la DGT
decide a quién fiscaliza. Nadie lo hizo a propósito. Ese es exactamente el problema.

**Sesión 2** · Módulo 2 (1,5 h) + primera mitad del Módulo 3 (1,5 h) · **3 horas**

---

## Lo que vas a hacer

| | Actividad | Tiempo |
|---|---|---|
| 🔪 | **La escena del crimen** — un `curl`, y el puntaje en pantalla | 10 min |
| 📚 | Teoría: DTOs, capas, OpenAPI, versionado nativo | ~40 min |
| ☕ | Descanso | 10 min |
| 🔧 | **Laboratorio: 4 TODOs** | ~110 min |
| ✅ | Validar, reporte, y la siembra del Lab 03 | 10 min |

## Los cuatro TODOs

| | Qué | Dónde | ≈ |
|---|---|---|---|
| **TODO_1** | Tapar la filtración: una ficha (DTO, lista blanca) en vez de la entidad | `web/dto/`, `application/` | 15 min |
| **TODO_2** | La capa que faltaba: la lógica al `FichaService`, no en el controlador | `application/` | 15 min |
| **TODO_3** | **Instalar los guardianes** AU-01 y AU-02, con la prueba de que muerden | `arquitectura/` | 15 min |
| **TODO_4** | El contrato visible: OpenAPI + versionado `/api/v1/`, con Swagger UI | `web/controller/` | 15 min |

## El pre-vuelo

```bash
cd starter
./mvnw spring-boot:run          # y en otra terminal:
curl http://localhost:8099/api/v1/contribuyentes/12345678-5/ficha
```

Cuenta los campos de esa respuesta. Uno de ellos no debería estar ahí.

Cuando creas que terminaste:

```bash
cd ..
./bin/90-validar.sh              # este lab necesita Docker (prueba contra base real)
```

## Cuando te atasques

- `./bin/95-recuperar.sh --solo-enunciado` — restaura los tests si los rompiste.
- `./bin/95-recuperar.sh --todo` — copia la solución encima (respalda antes). Anótalo en tu
  reporte: la casilla existe, y la honestidad vale más que un lab perfecto.
- `docs/troubleshooting.md` — tabla numerada.

## Para el Instructor

`./bin/91-e2e.sh` antes de la sesión: prueba que el starter está incompleto y tiene solución.
El guion del crimen, minutado, está en [`INSTRUCTOR.md`](INSTRUCTOR.md). El momento clave es
el `curl` con el puntaje en pantalla: ensáyalo.
