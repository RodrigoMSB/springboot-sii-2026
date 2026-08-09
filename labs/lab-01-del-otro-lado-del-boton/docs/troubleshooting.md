# Troubleshooting · Lab 01

Cita el número cuando pidas ayuda (`"me pasa la L1-03"`).

| # | Síntoma | Qué pasa | Qué haces |
|---|---|---|---|
| **L1-01** | `Could not resolve placeholder 'DGT_DB_URL'` | Estás en `prod` y no exportaste las variables | Es el comportamiento correcto. Si querías `dev`: `--spring.profiles.active=dev`. |
| **L1-02** | Exporté la variable y **sigue** diciendo que falta | La exportaste en una terminal y corres Maven en otra. O la exportaste después de arrancar. | `echo $DGT_DB_URL` en la **misma** terminal donde corres `./mvnw`. Es el error nº 1 de la sesión. |
| **L1-03** | `'url' must start with "jdbc"` | Tu `${DGT_DB_URL}` llegó sin resolver hasta Hikari | Te falta el TODO_2: nadie comprobó las variables antes de que arrancara el `DataSource`. Ese mensaje inútil **es el punto del ejercicio**. |
| **L1-04** | `T3` falla con `largo: 3` esperando que falle, y no falla | Te falta `@Valid` sobre el record anidado | Sin `@Valid`, la validación no entra en `Folio`. Una anotación, media hora de depuración. |
| **L1-05** | `AU-02` falla y no entiendo por qué | Un `@RestController` depende de una `@Entity` | Lee el mensaje: nombra el crimen. Y ojo, la caza incluso dentro de `ResponseEntity<Tramite>`. |
| **L1-06** | El `90-validar.sh` dice "Alguien modificó un test del enunciado" | Editaste (o formateó tu IDE) un archivo de `enunciado/` | `./bin/95-recuperar.sh --solo-enunciado`. Respalda antes, solo. |
| **L1-07** | `docker compose` falla: `port is already allocated` | Otro PostgreSQL ocupa el puerto | El compose del curso publica un puerto **efímero**; si aun así choca, mira `docker ps`. No apagues contenedores que no sean tuyos. |
| **L1-08** | Contenedores raros tras `./mvnw verify` | Son de Testcontainers | Ver **T-11** del Lab 00. `99-destruir.sh` no los toca a propósito. |
| **L1-09** | El puerto 8099 está ocupado | Algo escucha ahí | `./bin/start-lab.sh --puerto 8100`. El script te dice **quién** lo ocupa. |
