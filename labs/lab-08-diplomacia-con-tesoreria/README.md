# Lab 08 · Diplomacia con Tesorería

> *«TESO se cayó a las 9. A las 9:02 nosotros también — y nosotros no tenemos nada malo.
> Explícame cómo el problema de OTRO servicio se volvió NUESTRA caída. Y después haz que
> no vuelva a pasar.»*
>
> — **Carolina Espinoza**

La DGT ya tiene portero. Pero ahora necesita hablar con **Tesorería** (TESO) para confirmar
pagos. Y TESO, cuando anda de malas, se demora treinta segundos… o no contesta. El cliente
ingenuo espera para siempre: el hilo de la petición queda de rehén, el pool se llena de
rehenes, y **la API entera se cuelga por culpa de un servicio ajeno** — hasta el listado de
trámites, que no toca pagos.

Hoy la DGT aprende a hablar con extraños sin morir en el intento.

**Sesión 8** · Módulo 9 (endurecimiento, 1,0 h) + Módulo 10 (clientes HTTP y resiliencia, 2,0 h) · **3 horas**

---

## Lo que vas a hacer

| | Actividad | Tiempo |
|---|---|---|
| 🔪 | **La escena del crimen** — TESO lento cuelga la API entera | 10 min |
| 📚 | Teoría: clientes HTTP, timeouts, HTTP Interfaces, degradación, CORS/cabeceras | ~40 min |
| ☕ | Descanso | 10 min |
| 🔧 | **Laboratorio: los TODOs** | ~110 min |
| ✅ | Validar, reporte, y la siembra del Lab 09 | 10 min |

## Los TODOs

| | Qué | Dónde | ≈ |
|---|---|---|---|
| **TODO_1** | El timeout dirigido: connect + read cortos en el cliente de TESO | `config/TesoreriaConfig.java` | 15 min |
| **TODO_2** | La mala noticia elegante: la falla → 503 `ProblemDetail`, el trámite intacto | `web/controller/`, `domain/` | 15 min |
| **TODO_3** | La escalera colapsada: migrar a `@HttpExchange` (cliente declarativo) | `infrastructure/teso/` | 15 min |
| **TODO_4** | El endurecimiento: CORS nominal (no `*`) + cabeceras de seguridad | `config/SeguridadConfig.java` | 15 min |

Los tres tests del enunciado —`E1` (timeout), `E2` (degradación) y `E4` (endurecimiento)— no
los escribes: los lees. `TODO_3` no trae test nuevo: es un **refactor**, y su prueba es que la
misma suite sigue verde antes y después (ya lo sabes desde el Lab 05).

## El pre-vuelo — siente el secuestro

```bash
cd ..
./bin/start-lab.sh --dir starter --teso-lento 30000
```

Verás 12 confirmaciones de pago dispararse contra un TESO que tarda 30 s, y luego un `curl`
a `GET /tramites` —que no tiene nada que ver con pagos— **colgado**. La API entera, secuestrada
por su dependencia. En la solución, el mismo sabotaje: el pago responde 503 rápido y el listado
vive.

Este lab **necesita Docker** (PostgreSQL y el WireMock de TESO). Cuando termines:
`./bin/90-validar.sh --dir starter`.

## Para el Instructor

`./bin/91-e2e.sh` antes de la sesión. El guion está en [`INSTRUCTOR.md`](INSTRUCTOR.md).
La siembra del Lab 09 —los 400 MB de logs— está en `TEORIA.md §12`.
