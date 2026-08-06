# Autopsia · Lab 06

Un lugar para pensar en voz alta mientras depuras. No se entrega; es para ti.

## La carrera

- ¿En qué línea EXACTA de la emisión ingenua dos hilos se pisan? (entre "lee" y "escribe")
- Si pusieras un log del número tomado justo antes de guardar, ¿verías el mismo número dos veces?

## El candado

- Escribí el bloqueo (`tomarConBloqueo`) pero `E1` sigue rojo. ¿Le puse `@Transactional`?
- ¿Entiendo por qué `synchronized` "funciona" aquí y fallaría con dos instancias?

## La transacción

- ¿Mi método de emisión es `@Transactional`? ¿Es `public`? ¿Lo llamo desde OTRA clase (no `this`)?
- `E4` rojo con `expected X but was X+1`: ¿metí un `REQUIRES_NEW` en algún lado?

## El contrato

- ¿Puse `monto >= 0` y Flyway no arrancó? Mira la semilla: hay créditos negativos.
- ¿El nombre de mi constraint es `ck_linea_f29_monto_no_cero`? (E3 lo busca por nombre)

## La idempotencia

- Emito dos veces el mismo trámite: ¿201 y luego 200? ¿O revienta el `UNIQUE (tramite_id)`?
