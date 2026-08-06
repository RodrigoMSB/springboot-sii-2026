# 99 · Desafío (opcional) — el medidor que ve venir el problema

> Lo opcional **nunca** baja el veredicto. Si no lo haces, el `90` no se entera. Si lo haces y
> queda a medias, tampoco: se cuenta aparte.

Los tres instrumentos de Micrometer responden preguntas distintas:

| Instrumento | Responde | Ya lo usaste |
|---|---|---|
| `Counter` | ¿Cuántos van? | TODO_2 |
| `Timer` | ¿Cuántos y cuánto tardaron? | TODO_2 |
| `Gauge` | ¿Cuántos hay **ahora**? | — |

Un `Counter` solo sube: sirve para «folios emitidos hoy». No sirve para «trámites atascados en
`PRESENTADO` en este momento», que sube y baja — y que es exactamente el número que deja ver un
problema **antes** de que alguien llame.

## El criterio de aceptación

Publica un **gauge** llamado `dgt.tramites.pendientes` que refleje, en cada scrape, cuántos
trámites están en estado `PRESENTADO`.

Se aprueba si:

1. Aparece en `/actuator/prometheus` como `dgt_tramites_pendientes`.
2. **Baja** cuando un trámite avanza a `PAGADO`, sin reiniciar la aplicación.
3. No consulta la base en cada scrape más de una vez.

Sin pistas de implementación: esa es la gracia del `99`.

## Dos trampas, que no son pistas

- Un gauge **no se incrementa**: se *observa*. Si te encuentras llamando a algo parecido a
  `set()` desde el código de negocio, estás construyendo un contador con otro nombre, y se
  desincronizará el día que alguien toque la base por fuera.
- Micrometer mantiene una **referencia débil** al objeto que observa. Si ese objeto se lo lleva
  el recolector de basura, el gauge empieza a publicar `NaN` — y un `NaN` en un panel es peor que
  una serie ausente, porque parece un dato.

## Para pensar (va en el reporte, aunque no lo implementes)

Si tuvieras que alertar con **una sola** de las tres métricas del lab, ¿cuál elegirías y con qué
umbral? Justifícalo en términos de lo que Carolina notaría primero — no de lo que es fácil de
medir.
