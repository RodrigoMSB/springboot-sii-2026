# Los tests del Lab 03

**Los tests SON el enunciado.** En rojo, son tu lista de tareas.

## El enunciado (`enunciado/`) — protegido por manifiesto, y con `@DisplayName` en español

| Test | TODO | Compromisos | Docker |
|---|---|---|---|
| `E1_ValidacionDelRequestTest` | **TODO_1** | 201 al crear; 400 que nombra campos ante tipo inválido | no |
| `E2_RutValidoTest` | **TODO_2** | RUTs válidos aceptados; DV falso y basura rechazados (parametrizado) | no |
| `E3_TransicionIlegalTest` | **TODO_3** | transición legal 200; ilegal 409 con forma exacta | no |
| `E4_AU05VigilaTest` | (ya instalada) | AU-05 pasa, muerde su fixture, y **vigila que ningún test tuyo duerma** | no |

## Tus tests (`servicio/`) — territorio libre, el manifiesto NO los toca

| Test | TODO | Qué |
|---|---|---|
| `TramiteServiceTest` | **TODO_4** | lo escribes tú: Mockito + `ArgumentCaptor`. El `90` verifica que existe y pasa |

Este lab **no necesita Docker**: todo es `@WebMvcTest` + unit + ArchUnit. El `90` corre `test`.

## Comandos

```bash
./bin/90-validar.sh --dir starter     # el marcador de progreso
./mvnw test -Dtest='E2_RutValidoTest' # un compromiso concreto
./mvnw test                            # toda la suite
```

Con la config de surefire de este lab, `./mvnw test` muestra los `@DisplayName` en el reporte
XML (`target/surefire-reports/`): la suite se lee como especificación.
