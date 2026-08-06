# Troubleshooting · Lab 10

Filas citables: si algo te pasa, busca su número y menciónalo en el reporte o al instructor.

| # | Síntoma | Qué pasa | Qué hacer |
|---|---|---|---|
| **T10-01** | `/actuator/health/readiness` responde `UP` con la base muerta | **No es un bug: es el crimen.** Boot 4 activa las sondas por defecto, así que la ruta existe — pero el grupo `readiness` solo trae `readinessState` («terminé de arrancar»), que no mira ninguna dependencia. Una sonda que existe, que alguien conectó al balanceador y que miente es peor que una ausente: la ausente se nota. | TODO_1: declara el grupo `readiness` incluyendo tu indicador. Ver `guia/03`. |
| **T10-01b** | `/actuator/health/readiness` responde **404** | Alguien puso `management.endpoint.health.probes.enabled: false`, o el endpoint `health` no está expuesto. | Revisa `probes.enabled` y `management.endpoints.web.exposure.include`. |
| **T10-02** | `/actuator/health/readiness` responde **401** | El matcher de seguridad es `/actuator/health` exacto y no alcanza las subrutas. | Debe ser `/actuator/health/**`. En este lab ya viene así: si te pasa, revisa que no lo hayas tocado. |
| **T10-03** | Readiness dice `DOWN` pero **no dice qué** | Falta `show-details: always` (dev), o el indicador no adjunta detalles. | `Health.down().withDetail("causa", …)`. |
| **T10-04** | El readiness tarda ~30 s en responder con la base caída | Es el `connection-timeout` de Hikari por defecto. El health no está colgado: está esperando. | Bájalo (`spring.datasource.hikari.connection-timeout`). En este lab ya está en 5 s. |
| **T10-05** | Tras `--db-caida`, la app no vuelve a funcionar | El contenedor de PostgreSQL quedó **detenido** a propósito (`stop`, no `down`), para poder repetir el experimento. | `( cd starter && docker compose start postgres )`, o `./bin/99-destruir.sh` y empezar de cero. |
| **T10-06** | `/actuator/prometheus` responde **404** | Falta el registro `micrometer-registry-prometheus`, o el endpoint no está en la lista de exposición. Micrometer mide; el registro publica. | En este lab la dependencia ya está: revisa `management.endpoints.web.exposure.include`. |
| **T10-07** | `/actuator/prometheus` responde **401** | Correcto: está expuesto pero cerrado. No es un fallo. | Autentícate. `curl -H "Authorization: Bearer <token>"`. |
| **T10-08** | Mi métrica no aparece en el scrape | Se registró dentro del método en vez del constructor, y ese método no se ha ejecutado todavía. Una serie ausente no se distingue de un scrape fallido. | Regístrala al construir el bean. |
| **T10-09** | El contador no sube | La emisión no pasó por donde lo incrementas (p. ej. fue un reintento idempotente, RN-05, que suma en `reusado`). | Mira la etiqueta `resultado` antes de dar por roto el contador. |
| **T10-10** | `E4` dice «no hay CacheManager en el contexto» | Falta `@EnableCaching`. Sin él, `@Cacheable` **no hace nada**, no falla y no avisa. | TODO_4. Es el fallo más frustrante del lab justamente porque no hay error que leer. |
| **T10-11** | `E4` dice «el caché existe pero NO es Caffeine» | Está actuando el `ConcurrentMapCacheManager` por omisión: sin TTL, sin cota y **sin estadísticas**. | Declara el `CaffeineCacheManager` con `recordStats()`. |
| **T10-12** | Los aciertos de caché son 0 aunque el `@Cacheable` está puesto | La llamada no pasa por el proxy: alguien invoca el método desde **otro método de la misma clase**. La trampa del proxy, igual que `@Transactional` (Lab 06) y el aspecto (Lab 09). | Llama desde otro bean. |
| **T10-13** | El reporte devuelve el total viejo tras declarar una línea | Falta el `@CacheEvict`. Y ojo: **no se rompe nada**. Solo miente, rápido y con seguridad. | TODO_4, segunda mitad. |
| **T10-14** | `E4` falla de forma intermitente | Estás comparando valores absolutos de `hitCount`/`missCount`. Son acumulativas desde que nació el bean, y otros tests ya las movieron. | Mide **deltas**, como hace el enunciado. |
| **T10-15** | `E3` falla: `/actuator/env` devuelve 200 | La exposición sigue en `"*"`. | TODO_3. Lista blanca nominal. |
| **T10-16** | `E1` falla: liveness también cayó a `DOWN` | Metiste la base en el grupo `liveness`. Es el error más común del lab. | Pregúntate qué haría Kubernetes con ese DOWN, y si eso levantaría PostgreSQL. |
| **T10-17** | Al compilar sale un `WARNING` de `sun.misc.Unsafe` / Guice | Ruido del wrapper de Maven sobre JDK 25. Es ajeno a tu código. | Nada. No rompiste nada. Ver `T-12` del Lab 00. |
