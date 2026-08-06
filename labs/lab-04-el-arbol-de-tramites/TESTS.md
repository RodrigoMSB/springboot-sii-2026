# Los tests del Lab 04

## El enunciado (`enunciado/`) — protegido por manifiesto · necesita Docker

| Test | TODO | Qué exige |
|---|---|---|
| `E1_RelacionesLazyIT` | **TODO_1** | `findById` NO carga contribuyente ni adjuntos (`isLoaded` = false) |
| `E2_ConsultasDerivadasIT` | **TODO_2** | los cuatro métodos derivados, contra la semilla |
| `E3_JpqlMultiEntidadIT` | **TODO_3** | la JPQL trae lo correcto **y** no usa `JOIN FETCH` (verificado por reflexión, no por grep) |
| `E4_ReporteJdbcIT` | **TODO_4** | el total por período (910.000 en 2026-04), sumado en SQL |
| `E5_AU04InstaladaTest` | **TODO_1** | AU-04 pasa sobre producción y muerde su fixture (no necesita Docker) |

`BasePersistenciaIT` es la config compartida de Testcontainers (PostgreSQL real).

Este lab **necesita Docker**: los `*IT` levantan PostgreSQL. Se decidió **Testcontainers y no
H2** porque las migraciones Flyway son PostgreSQL puro (`BIGSERIAL`, etc.) — H2 ni arrancaría,
y además el curso no vende un dialecto como equivalente a otro. El `90` corre `verify`.

## Comandos

```bash
./bin/90-validar.sh --dir starter     # corre verify (Docker)
./mvnw test -Dtest='E5_AU04InstaladaTest'   # el único que no necesita Docker
./mvnw verify                          # toda la suite
```
