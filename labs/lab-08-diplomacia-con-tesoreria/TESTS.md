# Los tests del Lab 08

## El enunciado (`enunciado/`) — protegido, necesita Docker

| Test | TODO | Qué prueba |
|---|---|---|
| `E1_TimeoutIT` | TODO_1 | el pago falla RÁPIDO (timeout) **y** la API sigue viva en paralelo |
| `E2_DegradacionEleganteIT` | TODO_2 | TESO caído → 503 ProblemDetail limpio; el trámite no cambia de estado |
| `E4_EndurecimientoIT` | TODO_4 | CORS nominal (intruso rechazado) + cabecera CSP presente |

`BaseResilienciaIT` levanta PostgreSQL + TESO (WireMock, tag fijado) como **contenedores
singleton** (viven todo el JVM: el contexto de Spring se cachea entre clases, y un contenedor
por-clase dejaría al contexto apuntando a un puerto muerto). Configura a TESO por su admin API.

## TODO_3 no trae test nuevo

Migrar a `@HttpExchange` es un **refactor**: su prueba es que `E1` y `E2` siguen verdes antes y
después. Comportamiento idéntico, distinto código — P-16 sin segunda carpeta. Se demuestra, no
se promete (Lab 05).

## Regresión

Los tests de seguridad del Lab 07 se movieron a `seguridad/` y los de concurrencia del Lab 06 a
`concurrencia/`: siguen verdes, probando que la resiliencia no rompió nada. `ContratoRn03IT` y
`ContribuyenteControllerTest` (heredados) siguen autenticados desde el Lab 07.

## Comandos

```bash
./bin/90-validar.sh --dir starter
./bin/90-validar.sh --dir solucion
```
