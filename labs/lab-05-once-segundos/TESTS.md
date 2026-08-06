# Los tests del Lab 05

## El enunciado (`enunciado/`) — protegido, necesita Docker

| Test | Rol | Qué mide |
|---|---|---|
| `E1_ContadorDeConsultasIT` | **el contador** | una página cuesta ≤ 3 consultas (el COSTO) |
| `E2_ListadoFuncionalIT` | **el funcional** | el listado devuelve lo correcto, con nuestra forma de página (el COMPORTAMIENTO) |

`BaseRendimientoIT` trae el contador ya construido (Hibernate `Statistics`). Es determinista.

## Tus tests (`integracion/`) — territorio libre

| Test | TODO | Qué |
|---|---|---|
| `ListadoIntegracionTest` | **TODO_2** | tu IT de punta a punta (RestTestClient + Testcontainers) |

## Las dos soluciones (P-16)

| Directorio | Funcional (E2) | Contador (E1) |
|---|---|---|
| `starter/` | ✅ (funciona, pero es el N+1) | ❌ |
| `solucion-con-n1/` | ✅ | ❌ (el «antes») |
| `solucion/` | ✅ | ✅ (el «después») |

El `90` valida cualquiera con `--dir`. Necesita Docker (corre `verify`).

## Comandos

```bash
./bin/90-validar.sh --dir starter
./bin/90-validar.sh --dir solucion-con-n1   # pasa funcional, falla contador
./bin/90-validar.sh --dir solucion
```
