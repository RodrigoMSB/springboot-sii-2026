# Los tests del Lab 06

## El enunciado (`enunciado/`) — protegido, necesita Docker

| Test | Rol | Qué prueba |
|---|---|---|
| `E1_EmisionConcurrenteIT` | **la concurrencia** | 12 hilos emiten a la vez: folios únicos (RN-01) **y** secuencia sin saltos (RN-02) |
| `E2_IdempotenciaIT` | **la idempotencia** | mismo trámite → mismo folio, 201 y luego 200 (RN-05) |
| `E3_CheckMontoCeroIT` | **el contrato** | la base rechaza un monto cero por JDBC crudo (la V3) |
| `E4_RollbackIT` | **el rollback** | una falla post-emisión revierte el folio y devuelve el número |

`BaseConcurrenciaIT` trae el arnés de hilos (ExecutorService + latch, cero `sleep`) y el
`EmisorQueFallaDespues` para `E4`. Es determinista en veredicto: `E1` corre igual 3 de 3 veces.

## El guardián de la semilla

`dominio/SemillaCoherenteIT` sigue verde tras las emisiones: la invariante `contador ⇄
MAX(folio)`, que antes sostenía solo el `INSERT` de la V2, ahora la mantiene el código de
producción. `E1` lo comprueba explícitamente al final (el contador queda en el último folio).

## Cómo se enganchan los TODOs con los tests

| TODO | Test que lo prueba | Nota |
|---|---|---|
| TODO_1 (bloqueo) + TODO_4 (transacción) | `E1` | el lock necesita la transacción: los dos, o `E1` sigue rojo |
| TODO_2 (idempotencia) | `E2` | |
| TODO_3 (V3 `CHECK`) | `E3` | `monto <> 0`, no `>= 0` |
| TODO_4 (rollback) | `E4` | además, guarda contra el parche `REQUIRES_NEW` del acto 2b |

## Comandos

```bash
./bin/90-validar.sh --dir starter     # tu trabajo
./bin/90-validar.sh --dir solucion    # la referencia — el mismo criterio
```

Necesita Docker (corre `verify`: los cuatro son de integración contra PostgreSQL real).
