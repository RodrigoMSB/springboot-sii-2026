# Autopsia · Lab 12

Para cuando algo te muerda y no sepas por qué. Se entrega solo si la usas.

**Síntoma exacto** (transcríbelo, no lo describas — P-11):

```
______________________________________________________________________
______________________________________________________________________
```

| | |
|---|---|
| **Hipótesis** — qué crees que pasa, antes de tocar nada | |
| **Evidencia** — qué comprobaste y qué te dijo. La profundidad de la cola, lo que hay en la DLQ y sus cabeceras `x-death`, el estado del circuito, las filas de `aviso_procesado` | |
| **Corrección** — qué cambiaste, y por qué eso y no otra cosa | |
| **Detección** — cómo se habría notado ANTES. ¿Qué test faltaba? ¿Qué métrica del Lab 10 lo habría gritado? | |

> Y una pregunta propia de este lab: **¿tu fallo era ruidoso o silencioso?**
>
> Si el sistema siguió respondiendo 200 mientras el dato se perdía, acabas de reproducir el crimen
> de la sesión en tu propio código. No es mala suerte: es la clase de fallo que este módulo existe
> para enseñarte a ver.

## Antes de pedir ayuda, mira aquí

La consola del broker responde la mitad de las preguntas de este lab:

<http://localhost:15672> (`dgt` / `dgt-dev`)

- **Queues** → cuántos mensajes esperan en `dgt.avisos.q` y cuántos hay en `dgt.avisos.dlq`.
- Un mensaje en la DLQ → sus cabeceras `x-death` dicen **cuántas veces falló y por qué**.
- Si la cola principal crece y no baja: nadie está consumiendo, o algo la atascó.
