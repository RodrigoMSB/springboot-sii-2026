# Troubleshooting · Lab 03

| # | Síntoma | Qué pasa | Qué haces |
|---|---|---|---|
| **L3-01** | `E2` pasa el caso feliz pero falla un parametrizado | Hardcodeaste o el módulo 11 tiene un borde mal | Corre `./mvnw test -Dtest='E2_RutValidoTest'` y mira el índice `[N]` que falla: es el RUT exacto. Casos borde: DV 'K', RUT corto (`1-9`). |
| **L3-02** | `E1` da 201 donde espera 400 | La validación no dispara | ¿Anotaste el record (`@NotBlank`, `@Pattern`, `@RutValido`)? El `@Valid` del controlador solo actúa si el record tiene anotaciones. |
| **L3-03** | `E1` da 400 pero sin `$.campos.tipo` | Te falta el handler de validación en el advice | Escribe el handler de `MethodArgumentNotValidException` (TODO_1). Mira el `{{TODO_1}}` en `ManejadorDeErrores`. |
| **L3-04** | `E3` da 500 donde espera 409 | Falta el handler de la transición ilegal | TODO_3: el handler de `TransicionIlegalException`. Copia el patrón de los otros handlers. |
| **L3-05** | Copié un test de internet y usa `@RunWith` / `org.junit.Test` | Es JUnit 4 | Estás en JUnit 6 (Jupiter). Ver TEORIA §6, la tabla de señales. |
| **L3-06** | `E4.ningunTestDuerme` falla | Algún test tuyo llama a `Thread.sleep` | Quítalo. Si necesitabas esperar algo, usa Awaitility (`await().until(...)`). AU-05. |
| **L3-07** | El `90` dice "TODO_4: faltan tus tests" pero yo escribí uno | No pasan, o dejaste el `throw` del andamio | Borra el `throw new UnsupportedOperationException` y el `@Mock` del DTO. Tus tests deben pasar. |
| **L3-08** | El `90` dice "modificó un test del enunciado" | Editaste algo en `enunciado/` (a veces el IDE reformatea) | `./bin/95-recuperar.sh --solo-enunciado`. Tus tests van en `servicio/`, no en `enunciado/`. |
