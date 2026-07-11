# Troubleshooting · Lab 05

| # | Síntoma | Qué pasa | Qué haces |
|---|---|---|---|
| **L5-01** | El `90` dice "Docker no responde" | Este lab prueba contra base real | Abre Docker Desktop (T-03 del Lab 00). |
| **L5-02** | `E1` falla: `actual: NNL to be <= 3L` | Tu listado sigue iterando entidades (N+1) | Escribe la proyección paginada (TODO_1) y úsala en ListadoService. |
| **L5-03** | Bajé el contador a 1 con EAGER, pero `ArquitecturaTest#au04` falla | AU-04 caza el EAGER: es el parche prohibido | No es la solución. Usa proyección, no EAGER. Ver Guía 02. |
| **L5-04** | `E2` empezó a fallar tras mi cambio | Cambiaste el COMPORTAMIENTO, no solo el costo | Refactorizar = mismo resultado, menos consultas. Tu proyección debe devolver lo mismo que el N+1. |
| **L5-05** | `E1` da un número distinto cada corrida | No debería: el contador es determinista | Si varía, probablemente tu test siembra datos en orden no determinista. Siembra lo tuyo. |
| **L5-06** | El `time curl` no tarda nada aunque puse --lotes | El N+1 escala con las filas que PIDES | Pide una página grande: `?size=5000`. El costo crece con las filas devueltas. |
| **L5-07** | TODO_2: el `90` dice "faltan tus tests de integración" | Dejaste el `throw` del andamio | Bórralo y escribe la IT (RestTestClient). Mira E2 para el patrón. |
| **L5-08** | Tras `verify`, contenedores de Testcontainers | Normal | Ver T-11 del Lab 00. |
