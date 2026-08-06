# Lab 03 · Red de seguridad

> *«Antes de que preguntes: no está roto. Cada test rojo es un compromiso que aún no
> cumplimos. No me traigas código: tráeme verde. Y ojo: los tests se leen. El que
> implementa sin leer el test, implementa otra cosa.»*
>
> — **Carolina Espinoza**

Hoy no recibes un endpoint roto. Recibes una **bandeja de entrada**: una suite de tests en
rojo. No son un examen — son el **enunciado**. Cada uno tiene un nombre en español que dice
qué debe pasar. Ponerlos en verde es descubrir qué hace este sistema.

**Sesión 3** · resto del Módulo 3 (1,5 h) + Módulo 4 (1,5 h) · **3 horas**

---

## Lo que vas a hacer

| | Actividad | Tiempo |
|---|---|---|
| 🔪 | **La escena del crimen** — corres el `90` y la pantalla se llena de rojo | 10 min |
| 📚 | Teoría: validaciones, validadores propios, errores con contrato, JUnit 6 + Mockito | ~40 min |
| ☕ | Descanso | 10 min |
| 🔧 | **Laboratorio: 4 TODOs** (poner la suite en verde) | ~110 min |
| ✅ | Validar, reporte, y la siembra del Lab 04 | 10 min |

## Los cuatro TODOs

| | Qué | Dónde | ≈ |
|---|---|---|---|
| **TODO_1** | Validación declarativa: el request rechazado en la frontera con 400 que nombra campos | `web/dto/`, `web/controller/` | 15 min |
| **TODO_2** | `@RutValido`: tu validador de RUT chileno (módulo 11) | `web/validacion/` | 15 min |
| **TODO_3** | El error con contrato: la transición ilegal → 409 con forma exacta | `web/controller/` | 15 min |
| **TODO_4** | **Escribes tú los tests** del servicio (Mockito, `ArgumentCaptor`) | `src/test/.../servicio/` | 15 min |

## El pre-vuelo

```bash
cd ..
./bin/90-validar.sh --dir starter
```

La pantalla se llena de rojo, y el `90` te lista los compromisos en español. Ese es tu mapa.

Luego, **abre un test y léelo** — por ejemplo
`starter/src/test/java/cl/dgt/tramites/enunciado/E2_RutValidoTest.java`. La guía 02 te enseña
a leer un test como un contrato.

## El marcador

```bash
./bin/90-validar.sh --dir starter
```

No te dice "aprobado/reprobado" y ya: te dice cuántos compromisos van en verde. `9/14…
12/14… 14/14`. Es tu progreso.

## Cuando te atasques

- `./bin/95-recuperar.sh --solo-enunciado` — restaura los tests si los rompiste.
- `docs/troubleshooting.md` — tabla numerada.
- Los TODO_4 son **tuyos** (fuera de `enunciado/`): el manifiesto no los toca.

## Para el Instructor

`./bin/91-e2e.sh` antes de la sesión. El guion del crimen está en [`INSTRUCTOR.md`](INSTRUCTOR.md):
el momento clave es la pantalla roja y la frase de Carolina. Este lab **no necesita Docker**.
