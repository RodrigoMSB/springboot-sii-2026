# Troubleshooting · Lab 13

Filas citables. En el examen, cítalas al relator: preguntar por una **herramienta** está permitido.

| # | Síntoma | Qué pasa | Qué hacer |
|---|---|---|---|
| **T13-01** | `spring-boot:build-image` tarda muchísimo la primera vez | Descarga los buildpacks (varios cientos de megas). Solo pasa una vez por máquina. | Paciencia, o precaliéntalo antes de la sesión. `--sin-imagen` te deja avanzar mientras. |
| **T13-02** | `build-image` falla con `docker daemon not running` | Buildpacks construye **con** Docker. | Abre Docker Desktop (T-03 del Lab 00). |
| **T13-03** | El boletín dice «la imagen OCI NO se construye» y no explica más | El detalle está en las últimas líneas que imprime. | `cd <tu-dir> && ./mvnw spring-boot:build-image` y lee el error entero. |
| **T13-04** | La app arranca en local pero no dentro del contenedor | Casi siempre la configuración: dentro del contenedor `localhost` es el propio contenedor, no tu máquina. | Las dependencias se nombran por su nombre de servicio del compose (`postgres`, `rabbitmq`, `teso`). |
| **T13-05** | «No pude descubrir la red del compose» | El compose no llegó a levantar. | `cd <tu-dir> && docker compose ps`. |
| **T13-06** | El boletín dice «Hay tests DESACTIVADOS (@Disabled)» | Hay un `@Disabled` en `src/test`. | **Bórralo o arregla el test.** Un verde comprado apagando pruebas es el pipeline deshonesto que este examen califica Insuficiente. |
| **T13-07** | «Hay al menos un catch VACÍO» y crees que es un falso positivo | Es una **heurística declarada**: caza `catch (...) {}` sin cuerpo. Puede acertar en un caso que tú consideres legítimo. | Si de verdad no debe hacer nada, di **por qué** con un comentario dentro del bloque: deja de estar vacío y la heurística deja de cazarlo. Y ese comentario es justo lo que faltaba. |
| **T13-08** | «Credencial literal en un archivo versionado» | Hay un `password:` o `secret:` con valor literal en un `.yml` trackeado. | Fuera del repo, por variable de entorno (Lab 01). `${VAR}` no dispara la alarma: es la forma correcta. |
| **T13-09** | «Las migraciones tienen huecos» | Falta un número en la serie `V1…Vn`, o hay dos con el mismo. | El esquema se reconstruye aplicándolas en orden: un hueco rompe esa promesa. |
| **T13-10** | El `91` dice **SUITE FLAKY** | Las tres corridas no dieron el mismo resultado. | No reintentes: **rediseña** el test. Awaitility sobre una condición, barreras deterministas, cero `Thread.sleep`. |
| **T13-11** | El boletín dice «NÚCLEO VERDE» y no sé si aprobé | **No aprobaste todavía, y no es un error del script.** El eje Criterio es humano. | Llama al relator. El umbral es núcleo verde **Y** criterio ≥ Suficiente. |
| **T13-12** | La aceptación dice «se esperaba 403, fue 200» | Tu endpoint deja pasar a un contribuyente autenticado. | El brief dice «para los fiscalizadores». `@PreAuthorize`, Lab 07. |
| **T13-13** | La aceptación dice «se esperaba 401, fue 403» (o al revés) | 401 es «no te conozco»; 403 es «te conozco, no puedes». | Lab 07. La distinción importa y el examen la mira. |
| **T13-14** | El total del consolidado sale más grande de lo que debería | Un `JOIN` que multiplica líneas por trámites, o un `SUM` sin filtrar por período. | Es el fallo que **no se nota solo**. Sepáralo en dos consultas o agrupa bien. |
| **T13-15** | Al compilar sale el `WARNING` de `sun.misc.Unsafe` / Guice | Ruido del wrapper de Maven sobre JDK 25. | Nada. Ver `T-12` del Lab 00. |
