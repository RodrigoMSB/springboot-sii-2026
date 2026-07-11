# Troubleshooting · Lab 04

| # | Síntoma | Qué pasa | Qué haces |
|---|---|---|---|
| **L4-01** | El `90` dice "El demonio de Docker no responde" | Este lab prueba contra base real | Abre Docker Desktop (T-03 del Lab 00). |
| **L4-02** | `E5.au04PasaSobreProduccion` falla nombrando un campo | Dejaste una relación en EAGER (o sin fetch) | AU-04 te caza. Corrige ESA relación a `fetch = LAZY` (TODO_1). |
| **L4-03** | `E5` falla en "muerde su fixture" | Tu AU-04 no vigila lo que debe | Escribe la regla sobre `fields()` con `@ManyToOne`/`@OneToOne` que exija `fetch`. |
| **L4-04** | `E2` falla: `UnsupportedOperationException` | Dejaste el cuerpo `default` del método | Borra el cuerpo `default`, deja solo la firma. Spring Data la implementa. |
| **L4-05** | `E3` falla: la consulta usa `join fetch` | Usaste `JOIN FETCH` | Quítalo. Es del Lab 05. Navega la relación para filtrar, no para traerla cargada. |
| **L4-06** | `E4` falla: columnas no mapean al record | Los alias SQL no coinciden con el record | `SELECT ... AS periodo, ... AS total`: los alias deben ser `periodo` y `total`. |
| **L4-07** | Mi `findById` sigue disparando varias consultas aunque puse LAZY | Es el `@OneToOne` inverso: Hibernate ignora su LAZY | Es el caveat de la teoría §10. No es tu error. `E1` prueba contribuyente y adjuntos, no el `@OneToOne`. |
| **L4-08** | Tras `verify`, contenedores raros | Testcontainers | Ver T-11 del Lab 00. |
