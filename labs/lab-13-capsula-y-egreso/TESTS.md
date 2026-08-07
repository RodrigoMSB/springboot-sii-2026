# Tests · Lab 13

**Este lab no tiene tests de enunciado.** Es la diferencia con los doce anteriores, y es deliberada:
**tú decides qué probar y cuánto**, y esa decisión es materia de evaluación.

No hay `manifiesto-tests.sha256` que proteger, porque no hay enunciado que proteger.

## Qué sí corre, y quién lo escribió

| Paquete | Quién lo escribió | Qué vigila |
|---|---|---|
| **el tuyo** | **tú** | Lo que decidas del consolidado. Es lo que se evalúa. |
| `mensajeria/` | Lab 12 | Cola, idempotencia, DLQ, circuit breaker |
| `latidos/` | Lab 11 | Scheduling, candado distribuido, `@Async`, evento transaccional |
| `tablero/` | Lab 10 | Health real, métricas, exposición, caché |
| `trazabilidad/` | Lab 09 | `traceId`, log JSON, auditoría, adjuntos |
| `resiliencia/` | Lab 08 | Timeout de TESO, degradación elegante |
| `seguridad/` | Lab 07 | Puerta cerrada, login, firma, roles |
| `concurrencia/` | Lab 06 | Folio único, idempotencia, rollback |
| `arquitectura/` | Lab 02→ | AU-01…AU-07 y sus siete mordidas |

**Los heredados no son decorado.** Si tu consolidado rompe algo de lo anterior —una regla de
arquitectura, la seguridad de un endpoint viejo, el hilo de la traza— te enteras en el mismo `verify`.
El eje Oficio lo mira.

## Cómo se comprueba que tu endpoint hace lo que pide el brief

**Desde fuera, por HTTP, contra la aplicación empaquetada.** No con un test dentro de tu árbol.

Y hay tres razones, en orden de importancia:

1. **Tú decides qué probar.** Un test del examinador dentro de tu proyecto sería un enunciado
   encubierto: codificarías para pasarlo, que es lo contrario de lo que este examen mide.
2. **Un `verify` verde puede comprarse.** Con aserciones tautológicas, con `@Disabled`, con un test
   que no puede fallar. La aceptación no le pregunta a tus tests: le pregunta a la **aplicación**.
3. **El brief exige la entrega empaquetada.** Comprobarla contra la imagen OCI y no contra
   `spring-boot:run` es comprobar lo que de verdad se entrega.

Lo hace `bin/lib-aceptacion.sh`: levanta las dependencias con el compose, corre tu imagen en esa red,
y le pregunta por HTTP. Puedes leerlo — no hay nada oculto.

## La detección de flaky

`bin/91-e2e.sh` corre la suite **tres veces**. Si los tres resultados no coinciden, lo declara y el
eje Oficio es **Insuficiente**.

No se negocia, y no es capricho: **un test que a veces pasa no es una prueba, es una moneda.** Una
suite con una moneda dentro no protege nada — y peor, entrena al equipo a volver a lanzar el pipeline
en vez de mirar qué pasó.

Si tu suite flakea, la salida no es reintentar: es **rediseñar** el test. Awaitility sobre una
condición, barreras deterministas, nunca `Thread.sleep` — AU-05 lo prohíbe desde el Lab 03 y no ha
dejado de tener razón.

## Una nota sobre cuántos tests escribir

El boletín no cuenta tus tests. La rúbrica tampoco.

Lo que la defensa te va a preguntar es: **«si solo pudieras conservar tres, ¿cuáles y por qué?»**.
Escribe pensando en esa pregunta y vas a escribir mejores tests que optimizando cobertura.
