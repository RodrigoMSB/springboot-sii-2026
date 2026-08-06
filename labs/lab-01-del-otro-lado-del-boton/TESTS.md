# Los tests del Lab 01

Los tests **son el enunciado**. Si están en rojo, te están diciendo qué falta; no son un
juez, son un mapa.

## Los tests del enunciado (`src/test/java/cl/dgt/tramites/enunciado/`)

Están protegidos por `manifiesto-tests.sha256`: si los modificas, el validador lo detecta.
No es desconfianza — un enunciado que se puede editar no es un enunciado. **Los tests que
escribas tú, en cualquier otro paquete, son territorio libre.**

| Test | TODO | Qué exige | Regla |
|---|---|---|---|
| `T1_SinCredencialesEnElRepoTest#ningunRecursoLlevaLaCredencial` | — | Ningún recurso versionado contiene la credencial de utilería | Es un guardián permanente, no una tarea |
| `T1_…#prodUsaPlaceholdersSinDefecto` | **TODO_1** | `application-prod.yml` existe y usa `${VAR}` sin valor por defecto | Un `${VAR:algo}` arranca en silencio |
| `T2_PerfilProdFallaRapidoTest` | **TODO_2** | En `prod`, sin variables, la app **no arranca** y el mensaje las nombra | Fallar rápido *y claro* |
| `T3_DgtPropertiesTest` | **TODO_3** | La configuración se enlaza a un record, y `largo: 3` impide arrancar | RN: el largo del folio es negocio |
| `T4_TramiteEndpointTest` | **TODO_4** | 200 con DTO; 404 con `ProblemDetail` | El camino triste también es contrato |

## Lo que ya venía vigilando (no lo rompas)

| Suite | Qué protege |
|---|---|
| `arquitectura/ArquitecturaTest` | Las 7 reglas de la casa sobre tu código |
| `arquitectura/MordidaDeLosGuardianesTest` | Que cada una de esas 7 reglas **muerde** de verdad |
| `dominio/MaquinaDeEstadosTest` | Las 16 transiciones posibles del trámite (3 legales) |
| `dominio/Formulario29TotalTest` | El total del F29 es derivado, no persistido |
| `web/ContribuyenteControllerTest` | La rebanada web del tronco |
| `web/ContratoRn03IT` | El puntaje de riesgo jamás sale por la API (contra base real) |
| `dominio/SemillaCoherenteIT` | El contador de folios cuadra con los folios sembrados |

Los `*IT` necesitan Docker y los corre `./mvnw verify`, no `./mvnw test`.

## Cómo correrlos

```bash
./mvnw test                                    # todo lo que no necesita Docker
./mvnw test -Dtest='**/enunciado/*Test.java'   # solo tu enunciado
./mvnw test -Dtest='**/arquitectura/*Test.java'
./mvnw verify                                  # + los de integración (necesita Docker)
```
