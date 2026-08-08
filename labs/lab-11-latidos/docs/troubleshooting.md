# Troubleshooting · Lab 11

Filas citables: si algo te pasa, busca su número y menciónalo en el reporte o al instructor.

| # | Síntoma | Qué pasa | Qué hacer |
|---|---|---|---|
| **T11-01** | Con `--instancias 2` salen **dos** filas de cierre | **No es un bug: es el crimen.** `@Scheduled` es local a la JVM; cada instancia tiene su planificador y ninguno sabe del otro. | TODO_2. Empieza por `guia/02-dos-servidores-un-reloj.md`. |
| **T11-02** | `--instancias 2` falla con `No pude descubrir el puerto de PostgreSQL` | El compose no llegó a levantar, o la instancia 1 no arrancó. | `tail -n 40 .estado/dgt-1.log`. Comprueba a mano: `( cd starter && docker compose port postgres 5432 )`. |
| **T11-03** | La instancia 2 no arranca: `port is already allocated` o el puerto ocupado | La 2 usa el puerto siguiente al de la 1 (8099 → 8100). Algo ya lo tiene. | `./bin/start-lab.sh --puerto 8200 --instancias 2` mueve las dos. |
| **T11-04** | `E2` falla: ocho hilos y **ocho** ganadores | El candado se toma en dos pasos (`if (libre) tomar()`). Entre esas dos líneas cabe la otra instancia entera: la carrera del Lab 06. | Una sola sentencia atómica. `INSERT ... ON CONFLICT ... WHERE`. |
| **T11-05** | `E2` falla: **cero** ganadores | El candado quedó tomado de una corrida anterior y no ha expirado. | Es la razón por la que el test hace un reset duro de la tabla. Si te pasa fuera del test: `DELETE FROM candado_tarea;`. |
| **T11-06** | `E2` falla solo en el test de expiración | Falta la condición de expiración: tu sentencia toma el candado solo si está libre, pero no lo arrebata si venció. | Sin expiración, una instancia que muere deja el cierre bloqueado **para siempre**. |
| **T11-07** | El cierre no vuelve a correr nunca más | Un candado tomado por un proceso que ya no existe. Es el T11-06 en producción. | `SELECT * FROM candado_tarea;` — mira `expira_en`. Y ponle expiración. |
| **T11-08** | `E1` falla: `noneMatch(FixedRateTask)` | Sigue siendo `fixedRateString`. | TODO_1: `fixedDelayString`. Es un cambio de una palabra. |
| **T11-09** | `E1` falla: la zona está vacía | El cron no declara `zone`. | TODO_1, segunda mitad. La constante `ZONA` ya tiene el valor. |
| **T11-10** | `E3` falla: `hiloVirtual` es `false` | O falta `@Async`, o falta `@EnableAsync`, o el executor no es de hilos virtuales. | **Empieza por `@EnableAsync`**: sin él la anotación no hace nada, no falla y no avisa. Van tres veces en el curso (caché, scheduling, async). |
| **T11-11** | El aviso sale en el hilo del llamador aunque pusiste `@Async` | Autoinvocación: alguien llama al método desde **otro método de la misma clase**. El proxy no interviene. | Tercera vez en el curso: `@Transactional` (Lab 06), el aspecto (Lab 09), `@Async` hoy. Llama desde otro bean. |
| **T11-12** | Mi log asíncrono perdió el `traceId` del Lab 09 | El hilo nuevo no hereda el MDC, ni la transacción, ni el `SecurityContext`. El contexto no viaja solo. | Es esperable. Se propaga con un `TaskDecorator`; no es parte de este lab, pero conviene saber por qué pasa. |
| **T11-13** | `E4` falla: tras el rollback el aviso **sí** salió | `@EventListener` normal: reacciona al publicarse, dentro de la transacción. | TODO_4: `@TransactionalEventListener(phase = AFTER_COMMIT)`. |
| **T11-14** | Puse `AFTER_COMMIT` y ahora mi listener no puede escribir en la base | Correcto: ahí ya **no hay transacción**. La anterior se cerró. | Si necesita escribir, que abra la suya (`REQUIRES_NEW`) — y asume que si falla, lo confirmado no se deshace. |
| **T11-15** | El cierre se ejecuta pero el candado nunca se suelta | La liberación está fuera del `finally` y una excepción se la saltó. | `finally`. Siempre. |
| **T11-16** | `docker compose exec postgres psql` no encuentra la base | El contenedor todavía está sembrando (Flyway), o el compose es el del otro directorio. | Espera a que la instancia 1 responda en `/actuator/health` antes de consultar. |
| **T11-17** | Al compilar sale un `WARNING` de `sun.misc.Unsafe` / Guice | Ruido del wrapper de Maven sobre JDK 25, ajeno a tu código. | Nada. Ver `T-12` del Lab 00. |
