# Lab 12 · Amortiguadores

> *«Anoche el servicio de avisos estuvo caído dos horas. Doscientos contribuyentes tienen su folio
> y ninguno lo sabe. Y lo peor: nadie puede decirme cuáles doscientos, porque esos avisos no
> quedaron en ninguna parte. Un aviso que se pierde en silencio es peor que un error: el error, al
> menos, se ve.»*
>
> — **Carolina Espinoza**

El Lab 11 sacó las notificaciones del hilo de la petición. Progreso real — pero el aviso se manda
**al aire**. `@Async` hizo justo lo que promete: mover el trabajo a otro hilo. **Mover no es
guardar**, y la memoria de un proceso se va con el proceso.

Hoy el aviso deja de perderse: espera en una cola que sobrevive a los reinicios.

**Sesión 12** · Módulo 13 — Mensajería y Resiliencia · **3 horas**

---

## Lo que vas a hacer

| | Actividad | Tiempo |
|---|---|---|
| 🔪 | **La escena del crimen** — los avisos evaporados, sin un solo error | 10 min |
| 📚 | Teoría: colas, DLQ, idempotencia, circuit breaker | ~40 min |
| ☕ | Descanso | 10 min |
| 🔧 | **Laboratorio: los TODOs** | ~110 min |
| ✅ | Validar, reporte, y la siembra del examen de egreso | 10 min |

## Los TODOs

| | Qué | Dónde | ≈ |
|---|---|---|---|
| **TODO_1** | El aviso va a la cola, no al aire | `application/evento/` | 12 min |
| **TODO_2** | El consumidor idempotente: el duplicado no duplica | `application/ConsumidorDeAvisos.java` | 15 min |
| **TODO_3** | La cola de los muertos (DLQ) | `config/AmqpConfig.java` | 12 min |
| **TODO_4** | El circuit breaker sobre TESO | `infrastructure/teso/` | 15 min |

Los cuatro tests del enunciado —`E1` a `E4`— no los escribes: los lees. `E<n>` corresponde a
`TODO_<n>`. Verifican que el aviso **espere** en la cola con el consumidor caído, que el mismo
mensaje entregado dos veces produzca **un** envío, que el envenenado caiga a la DLQ **con su causa**
mientras la cola sigue fluyendo, y que el circuito abierto falle **en milisegundos** sin tocar la
red.

## El pre-vuelo — mira evaporarse un aviso

```bash
cd ..
./bin/start-lab.sh --avisos-caidos
```

Dos fases. Primero emite folios con el servicio de avisos caído: la API responde **201 a todo**, sin
un solo error. Después **reinicia** la aplicación —eso es «levantar el servicio de avisos»— y
compara:

- **starter** → no llega nada. Esos avisos no existen en ninguna parte.
- **solución** → los mensajes seguían en la cola, sobrevivieron al reinicio, y se entregan solos.

Este lab **necesita Docker** y levanta **dos** contenedores: PostgreSQL y RabbitMQ. La primera
pasada es la lenta. La consola del broker queda en <http://localhost:15672> (`dgt` / `dgt-dev`) —
úsala: ver las colas por dentro enseña más que leer un log.

Cuando termines: `./bin/90-validar.sh --dir starter`.

## Para el Instructor

`./bin/91-e2e.sh` antes de la sesión. El guion está en [`INSTRUCTOR.md`](INSTRUCTOR.md).
La siembra del examen de egreso está en `TEORIA.md §11`.
