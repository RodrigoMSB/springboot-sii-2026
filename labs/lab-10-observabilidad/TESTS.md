# Tests · Lab 10

Los del **enunciado** (`src/test/java/cl/dgt/tramites/enunciado/`) no se tocan: los protege
`manifiesto-tests.sha256`. `E<n>` corresponde a `TODO_<n>`. Todo lo demás que quieras escribir es
territorio libre — el manifiesto jamás castiga al alumno bueno.

| Test | Qué prueba | Verifica |
|---|---|---|
| `E1_HealthQueNoMienteIT` | Con la base **viva**, readiness es `UP` y el componente `baseDeDatos` **aparece nombrado**. Liveness es `UP` y **no** lo incluye. Con la base **muerta**, readiness cae a `DOWN`, nombra `baseDeDatos` y adjunta la causa; liveness sigue `UP`. | TODO_1 |
| `E2_MetricasDeNegocioIT` | El contador y el timer están **registrados antes** de la primera emisión (una serie ausente no se distingue de un scrape fallido). Emitir un folio suma 1 en `dgt_folios_emitidos_total{resultado="nuevo"}` y cronometra. Un reintento idempotente (RN-05) suma en `reusado`, **no** en `nuevo`. | TODO_2 |
| `E3_ExposicionConCriterioIT` | `/actuator/env`, `/beans` y `/heapdump` responden **404 incluso con token de FUNCIONARIO**: no están expuestos. `/actuator/health` responde sin credencial. `/actuator/prometheus` da 401 sin token y 200 con token. Y `application-prod.yml` declara lista blanca nominal, sin `*`. | TODO_3 |
| `E4_CacheMedidoIT` | La segunda llamada idéntica al reporte es un **acierto de caché** (medido en las estadísticas de Caffeine, no leyendo el código). Declarar una línea del F29 **invalida** el caché: la siguiente lectura vuelve a la base y el total refleja el cambio. | TODO_4 |

## Cómo se mide, y por qué así

**El health se prueba matando la base de verdad.** `E1` levanta su **propio** contenedor
PostgreSQL —no el singleton compartido— y lo detiene a media prueba. Con el contenedor de todos,
matarlo sería sabotear la suite. Nada de simulacros con mocks: el crimen se vive también en los
tests.

**El caché se prueba con estadísticas, no con anotaciones.** Un `@Cacheable` *escrito* no es un
caché *funcionando*: basta una autoinvocación, un `@EnableCaching` ausente o una errata en el
nombre para que la anotación no haga nada — en silencio y sin un solo error. Por eso `E4` mide
`hitCount` y `missCount` de Caffeine, por **deltas** (son acumulativas, y otros tests ya las
movieron: un test que asume `hitCount == 1` funciona el día que corre solo y se cae el día que
corre acompañado).

**404 no es 401.** `E3` pide `/actuator/env` **con** un token válido de funcionario. Si
respondiera 401 no probaríamos nada — solo que hay un portero. El 404 prueba que no hay puerta.

## Lo que sigue corriendo de los labs anteriores

| Paquete | De dónde viene | Qué sigue vigilando |
|---|---|---|
| `trazabilidad/` | Lab 09 | El `traceId`, el log JSON, el aspecto de auditoría, los adjuntos |
| `resiliencia/` | Lab 08 | Timeout de TESO, degradación elegante, endurecimiento |
| `seguridad/` | Lab 07 | Puerta cerrada, login, firma del token, roles |
| `concurrencia/` | Lab 06 | Folio único bajo concurrencia, idempotencia, rollback |
| `arquitectura/` | Lab 02 en adelante | AU-01…AU-07 y sus siete mordidas |

Cuando el enunciado de un lab se aprueba, deja de ser enunciado y pasa a ser **regresión**: se
muda a su paquete temático y corre para siempre. Es lo que avisa si el trabajo de hoy rompió lo
de ayer — instrumentar con métricas y caché no puede costarte el hilo de la traza.
