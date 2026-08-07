# NOTA · Esta es UNA solución, no LA solución

Si la tuya difiere y **defiende su criterio**, puede estar igual de bien — o mejor.

Este directorio existe para que puedas **comparar**, no para que copies. Y si lo consultaste durante
el examen, no pasa nada: anótalo en tu reporte. Mirar la referencia no está prohibido; está
**registrado**. Se evalúa la honestidad, no la pureza.

---

## Lo que esta referencia decidió, y por qué

Cinco bordes del brief. Ninguna de estas respuestas es la única posible.

| Borde | Qué decidió esta referencia | Por qué |
|---|---|---|
| **¿Paginar?** | No | Un contribuyente tiene decenas de trámites, no millones. Paginar hoy es complejidad sin problema que resolver. Se revisa cuando un número lo pida — y el Lab 05 dejó el contador de consultas para medirlo. |
| **RUT inexistente** | `404`, no lista vacía | Una lista vacía es una respuesta legítima a «no tiene trámites». Para «no existe» es una mentira cortés, y el fiscalizador que teclea mal tiene que enterarse. |
| **¿Período obligatorio?** | Sí | «El total declarado del período» exige saber de qué período. Un default silencioso —«el mes actual»— haría que el batch del día 1 consolidara el mes equivocado a las 00:05. |
| **El batch** | El mismo endpoint, con un token de servicio con rol FISCALIZADOR | Dos caminos hacia el mismo dato son dos sitios donde arreglar el próximo bug, y solo uno se acuerda de arreglar quien lo encuentra. |
| **Qué campos salen** | Lista blanca explícita en el DTO | Doctrina del Lab 02. Un DTO que se construye *quitando* campos vuelve a filtrarlos el día que alguien agrega una columna. Y el brief lo avisaba: *«el año pasado tuvimos un incidente con un puntaje interno»*. |

## Dos decisiones técnicas que también podrían ser otras

**SQL agregado en vez del ORM.** Es una pregunta de columnas y sumas; cargar el árbol de entidades
traería objetos que nadie va a usar y reabriría el N+1 del Lab 05. El precio: hay SQL a mano que
mantener. Una solución con JPA y una proyección bien hecha sería igual de defendible.

**Dos consultas, no una.** El `SUM` sobre un producto cartesiano de trámites × líneas da un total
inflado, y arreglarlo con subconsultas anidadas hace la consulta ilegible. Dos consultas simples le
ganan a una ingeniosa: la ingeniosa se rompe cuando alguien le agrega una tabla.

> Si tú resolviste esto con una sola consulta y **sabes explicar cómo evitaste el total inflado**, tu
> solución es mejor que esta. Ese es exactamente el tipo de cosa que sube a Destacado.

## Los cinco tests, y por qué esos cinco

Están en `src/test/java/cl/dgt/tramites/egreso/ConsolidadoIT.java`. No son una lista completa: son un
**criterio de selección**. Cubren lo que puede romperse en silencio —el total inflado, el permiso de
más— antes que lo que se nota al primer clic.

Lo que deliberadamente **no** prueban: los nombres exactos de los campos del JSON (eso lo fija el DTO
y cambiarlo es una decisión, no un accidente) ni el orden de los trámites más allá de que sea estable.
Probar de más le ata las manos al que venga a refactorizar.

---

*Si tu entrega difiere de esta en todo menos en que funciona y sabes defenderla: enhorabuena. Eso era
el examen.*
