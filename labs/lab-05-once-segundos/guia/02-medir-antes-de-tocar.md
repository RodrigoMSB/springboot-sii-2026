# Guía 02 · Medir antes de tocar

**Acto 2 · El parche que baja el contador… y sigue mal** 🔨

La tentación obvia: volver todo a EAGER. Pruébalo mentalmente (o de verdad, en una copia):
el contador baja a **1**. Una sola consulta. ¿Ganaste?

Corre el guardián:

```bash
cd starter && ./mvnw test -Dtest='ArquitecturaTest#au04'
```

`AU-04` te caza: una relación en EAGER pone el build en rojo. El guardián del Lab 04 existe
justo para impedir este parche.

## Contesta

| Pregunta | Tu respuesta |
|---|---|
| Con EAGER, el contador bajó a 1. ¿Por qué esa 1 consulta es un problema? | |
| ¿Qué endpoints, además del listado, pagarían el peaje de EAGER? | |
| Entonces, ¿la métrica es "menos consultas", o "las consultas correctas para esta pantalla"? | |

<details>
<summary>💡 Una consulta gigante no es mejor que N pequeñas</summary>

EAGER convierte el listado en un `JOIN` cartesiano: trae más filas, más columnas, más
memoria. Y lo pagan TODOS los endpoints que tocan la entidad, no solo este listado. Bajaste
el contador y empeoraste el sistema.

La respuesta correcta no es cargar más ni menos: es cargar **lo que esta pantalla pinta, y
nada más**. Eso es una proyección.
</details>

➡️ Siguiente: [`03-la-proyeccion-y-el-numero.md`](03-la-proyeccion-y-el-numero.md)
