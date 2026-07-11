# Guía 02 · El candado en el dato

Ya viste la carrera. Ahora los intentos de arreglarla — dos que **funcionan** y siguen
mal, y el que es correcto.

## Acto 2a — `synchronized` (funciona… en una JVM)

El reflejo: poner `synchronized` en el método de emisión.

```java
public synchronized ResultadoEmision emitir(Long tramiteId) { ... }
```

Y **pasa** el test de concurrencia. En una JVM, `synchronized` serializa los hilos: uno
entra, los demás esperan. Se ve resuelto.

No lo está. `synchronized` es un candado del **proceso**. El Lab 10 correrá **dos**
instancias de la DGT detrás de un balanceador; la JVM de la izquierda no comparte monitor
con la de la derecha. Cada una cree que tiene la llave. Vuelve el duplicado. El candado
está en el **código**; tiene que estar en el **dato**.

## Acto 2b — `REQUIRES_NEW` (elegante, y peor)

El segundo intento parece de manual: aislar la toma del número en su propia transacción.

```java
@Transactional(propagation = Propagation.REQUIRES_NEW)
public long tomar() { ... } // confirma SOLO, aparte de la emisión
```

`REQUIRES_NEW` suspende la transacción de la emisión y abre una nueva que **confirma
sola**. El número queda gastado antes de que el folio se escriba. Si luego algo falla y la
emisión revierte, el folio desaparece pero el número **no vuelve**: un salto en el libro.

El test `E4` lo caza: esperaba que el contador volviera a su valor y lo encontró avanzado
—`expected: 1L but was: 2L`—. El número, gastado.

## Acto 3 — el bloqueo dirigido, en la misma transacción (TODO_1 + TODO_4)

La forma correcta pone el candado en el **dato** y dentro de **una** transacción:

```java
@Transactional                                   // TODO_4: una sola transacción, atómica
public ResultadoEmision emitir(Long tramiteId) {
    Tramite tramite = tramites.findById(tramiteId).orElseThrow(...);
    // ... idempotencia (TODO_2, ver guía 03) ...
    ContadorFolio c = contadores.tomarConBloqueo()  // TODO_1: SELECT ... FOR UPDATE
                                .orElseThrow(...);
    long numero = c.siguiente();
    return ResultadoEmision.nuevo(FolioDto.de(folios.save(new Folio(numero, tramite))));
}
```

Y la consulta bloqueante, en el repositorio del contador:

```java
@Lock(LockModeType.PESSIMISTIC_WRITE)
@Query("SELECT c FROM ContadorFolio c WHERE c.id = 1")
Optional<ContadorFolio> tomarConBloqueo();
```

El primer emisor cierra la fila del contador; los demás **esperan** a que confirme y recién
entonces leen el número ya actualizado. Se serializan sobre el dato caliente y solo sobre
él. El candado vive en la base —donde TODA instancia lo ve— y dentro de la transacción que
persiste el folio —así que si algo falla, el rollback devuelve el número—.

> **Por qué TODO_1 y TODO_4 son inseparables:** un `FOR UPDATE` sin transacción suelta el
> candado al instante (la transacción implícita de la consulta cierra enseguida). Prueba a
> poner el bloqueo sin `@Transactional`: `E1` sigue rojo. El candado necesita la
> transacción que lo sostenga.

Sigue con [`03-idempotencia-y-el-contrato.md`](03-idempotencia-y-el-contrato.md).
