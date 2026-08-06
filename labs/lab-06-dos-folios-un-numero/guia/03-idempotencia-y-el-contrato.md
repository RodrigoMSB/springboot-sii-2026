# Guía 03 · La idempotencia y el contrato de la base

Dos TODOs más: RN-05 (idempotencia) y el `CHECK` de la V3.

## TODO_2 — La idempotencia (RN-05)

Un contribuyente aprieta «emitir». La red se corta antes de que llegue la respuesta. Él no
sabe si se emitió, así que reintenta. Si tu API crea un folio en **cada** llamada, ahora
tiene dos.

RN-05: **mismo `tramiteId`, mismo folio**. La primera emisión crea (201); el reintento
devuelve el mismo, con 200, sin crear nada.

En `emitir(...)`, antes de tomar un número:

```java
return folios.findByTramiteId(tramiteId)
        .map(f -> ResultadoEmision.reusado(FolioDto.de(f)))          // ya existe: 200
        .orElseGet(() -> ResultadoEmision.nuevo(FolioDto.de(emitirNuevo(tramite)))); // nace: 201
```

Y el controlador traduce ese bit a HTTP:

```java
HttpStatus estado = r.creado() ? HttpStatus.CREATED : HttpStatus.OK;
```

El suelo de todo esto es el `UNIQUE (tramite_id)` que la V1 puso en `folio`. Si dos
reintentos corren *a la vez* y los dos pasan la comprobación, la base rechaza al segundo.
Aquel `UNIQUE` que parecía adorno era un contrato. El test `E2` lo prueba: emite dos veces
y exige un solo folio, 201 y luego 200.

## TODO_3 — La restricción como contrato (M8)

La V1 dejó `linea_f29.monto` **sin** restricción, a propósito: la lección era ponerla tú,
con una migración correctiva. Crea `src/main/resources/db/migration/V3__check_montos.sql`:

```sql
ALTER TABLE linea_f29
    ADD CONSTRAINT ck_linea_f29_monto_no_cero CHECK (monto <> 0);
```

**Ojo con el predicado.** No es `monto >= 0`. En este dominio los créditos SON negativos
—lo dice `LineaF29`, lo prueba `Formulario29TotalTest`, lo siembra la V2—; prohibir el
negativo rompería el F29 y Flyway ni siquiera aplicaría la migración sobre la semilla. El
contrato correcto es **`monto <> 0`**: ninguna línea vale cero. Una línea de cero no dice
nada.

> **La pregunta que te va a hacer Carolina:** *«si mañana validas el monto en Java, ¿este
> `CHECK` no sobra?»* No. Java protege lo que entra por la API. La tabla la tocan también
> los scripts, las cargas masivas, el `psql` de madrugada. La última línea de defensa vive
> en el motor, no en el framework.

El test `E3` inserta una línea de monto cero **por JDBC crudo**, saltándose todo Java, y
exige que la base la rechace con `ck_linea_f29_monto_no_cero`. Sin la V3, la base la
acepta y `E3` se pone rojo.

## Recuerda TODO_4

El `@Transactional` sobre `emitir(...)` no es decorativo: sin él, ni el bloqueo del TODO_1
sirve (guía 02) ni el rollback del `E4` se cumple. Emitir es **un solo hecho** —tomar el
número y escribir el folio— y va en **una** transacción, o no va.

## Cierra

```bash
./bin/90-validar.sh --dir starter
```

Los cuatro tests en verde, la arquitectura intacta, y `SemillaCoherenteIT` demostrando que
el contador sigue cuadrado con el último folio. Completa `plantillas/reporte-entregable.md`
y entrégalo.
