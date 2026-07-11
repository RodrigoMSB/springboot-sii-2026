# Los tests del Lab 09

## El enunciado (`enunciado/`) — protegido, necesita Docker

| Test | TODO | Qué prueba |
|---|---|---|
| `E1_TrazabilidadIT` | TODO_1 | el traceId se propaga al MDC; dos peticiones, dos traceId distintos |
| `E2_LogJsonIT` | TODO_2 | una línea de log parsea como JSON y contiene el traceId |
| `E3_AuditoriaIT` | TODO_3 | el aspecto audita (RUT enmascarado) y NO intercepta la autoinvocación |
| `E4_AdjuntosIT` | TODO_4 | un .exe disfrazado de .pdf se rechaza; el path traversal se neutraliza |

`BaseObservabilidadIT` levanta PostgreSQL (contenedor singleton) y **captura los logs con un
appender en memoria** —no lee un archivo—, así el test inspecciona el evento (su MDC, su
mensaje). Para el JSON, renderiza el evento con el MISMO encoder que la consola.

## El código de negocio, idéntico

El aspecto es transversal: los servicios de dominio quedan **byte a byte iguales** que el Lab 08
(lo garantiza la derivación — no divergen). El auditor es invisible para el negocio.

## Regresión

Los tests de resiliencia del Lab 08 se movieron a `resiliencia/`, los de seguridad a `seguridad/`,
los de concurrencia a `concurrencia/`: todos siguen verdes.

## Comandos

```bash
./bin/90-validar.sh --dir starter
./bin/90-validar.sh --dir solucion
```
