# Los tests del Lab 02

Los tests **son el enunciado**. En rojo te dicen qué falta.

## El enunciado (`src/test/java/cl/dgt/tramites/enunciado/`) — protegido por manifiesto

| Test | TODO | Qué exige | Docker |
|---|---|---|---|
| `T1_FichaSinDatosSensiblesIT` | **TODO_1** | La ficha expone SOLO `rut` y `razonSocial` (lista blanca), contra base real | sí |
| `T2_LaCapaDeServicioTest` | **TODO_2** | `FichaController` depende de `FichaService` y NO del repositorio | no |
| `T3_GuardianesInstaladosTest` | **TODO_3** | AU-01 y AU-02 pasan en producción **y** muerden su fixture | no |
| `T4_ContratoOpenApiIT` | **TODO_4** | La spec OpenAPI documenta la ficha versionada; Swagger UI responde | sí |

**Este lab necesita Docker:** dos de sus tests (los `*IT`) levantan PostgreSQL con
Testcontainers. Por eso `90-validar.sh` corre `verify`, no `test`.

## Lo que ya venía vigilando (no lo rompas)

`arquitectura/ArquitecturaTest` y `MordidaDeLosGuardianesTest` — en el starter llegan
**reducidas**: solo AU-03…AU-07 (5 reglas + 5 mordidas). AU-01 y AU-02 los instalas tú
(TODO_3); la solución restaura el 7+7 completo. Además: `MaquinaDeEstadosTest`,
`Formulario29TotalTest`, `ContratoRn03IT`, `SemillaCoherenteIT`.

## Comandos

```bash
./mvnw verify                                  # todo (necesita Docker)
./mvnw test -Dtest='**/enunciado/T2_*.java'    # un TODO que no necesita Docker
./mvnw test -Dtest='**/arquitectura/*Test.java'
```
