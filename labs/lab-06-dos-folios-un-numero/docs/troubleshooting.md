# Troubleshooting · Lab 06

Errores frecuentes de esta sesión. Busca tu síntoma.

## L6-01 · `E1` rojo aunque escribí el bloqueo

Síntoma: pusiste `tomarConBloqueo()` (con `@Lock(PESSIMISTIC_WRITE)`) y `E1` sigue rojo, con
`folio_pkey` o duplicados.

Causa: falta `@Transactional` en `emitir(...)`. Un candado pesimista se **suelta** al cerrar la
transacción; sin una transacción que lo sostenga, el `FOR UPDATE` bloquea y libera en el mismo
suspiro. **TODO_1 y TODO_4 van juntos.**

## L6-02 · Flyway no arranca: "violates check constraint"

Síntoma: la app no levanta; el log dice que la migración V3 falló porque una fila existente
viola el `CHECK`.

Causa: pusiste `CHECK (monto >= 0)`. La semilla (V2) tiene **créditos negativos** (código 511).
El contrato correcto es `CHECK (monto <> 0)`: ninguna línea vale cero. En este dominio el
negativo es legítimo.

## L6-03 · `E4` rojo: `expected: NL but was: N+1L`

Síntoma: el rollback dejó el contador avanzado.

Causa: tomaste el número en una transacción aparte (`@Transactional(propagation = REQUIRES_NEW)`).
Esa transacción confirma sola, así que el número se gasta aunque la emisión revierta. Toma el
número en la **misma** transacción (propagación por defecto, `REQUIRED`).

## L6-04 · `E3` rojo: "Expecting code to raise a throwable"

Síntoma: `E3` esperaba que la base rechazara el monto cero, y no lo rechazó.

Causa: falta la migración `V3__check_montos.sql`, o el nombre de la constraint no es
`ck_linea_f29_monto_no_cero` (el test lo busca por nombre). Revisa el archivo y el nombre.

## L6-05 · Mi `@Transactional` "no hace nada"

Síntoma: pusiste `@Transactional` y el comportamiento no cambia (ni el lock, ni el rollback).

Causa: el proxy transaccional solo actúa si el método es **`public`** y se llama **desde otra
clase** (a través del bean). Un `@Transactional` en un método `private`, o llamado con
`this.emitir(...)` desde la misma clase, es invisible para Spring. Ver `TEORIA.md §2`.

## L6-06 · El sabotaje `--concurrencia` no choca

Síntoma: corres `./bin/start-lab.sh --dir starter --concurrencia 2` y no aparece el crimen.

Causa: una carrera es una carrera. El script reintenta hasta 6 veces; si aun así no choca, sube
la concurrencia: `--concurrencia 8`. En el starter el choque es cuestión de tiempo.

## L6-07 · El puerto 8099 está ocupado

Síntoma: `El puerto 8099 ya está ocupado`.

Causa: otra cosa (o una DGT anterior) lo tiene. No es tu error. Usa otro: `--puerto 8100`. Y si
quedó una DGT a medio bajar: `./bin/99-destruir.sh`. Ver T-01 del Lab 00.

## L6-08 · Docker no responde

Síntoma: `El demonio de Docker no responde`.

Causa: Docker Desktop no está corriendo. Este lab **necesita** Docker: los cuatro tests del
enunciado levantan un PostgreSQL real con Testcontainers. Ver T-03 del Lab 00.

## L6-09 · Veo contenedores de Testcontainers tras validar

Es normal: `./mvnw verify` levanta contenedores propios (y un vigilante, `ryuk`). `99-destruir.sh`
NO los toca —no los levantó este lab—, pero te avisa que están. Para limpiarlos, ver T-11 del Lab 00.
