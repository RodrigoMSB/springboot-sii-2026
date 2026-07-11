# Guía 01 · La aguja en el pajar

## La escena del crimen

```bash
./bin/start-lab.sh --dir starter --caos
```

30 peticiones a la vez, y un log donde buscar es imposible:

```
     grep peticion dgt.log  ->  60 líneas entrelazadas de 60 peticiones.
     Sin traceId, no hay forma de saber cuáles son de la MISMA operación.
```

Grepeás el folio culpable y salen 200 líneas de 15 peticiones distintas, mezcladas. ¿Cuáles
son de la operación que buscás? Imposible saberlo.

## Por qué pasa

El log del practicante (`config/RegistroIngenuo`) es texto plano, una línea por petición, **sin
correlación**. Y hay `System.out.println` sueltos y un `catch` que se traga el error. Con una
petición se lee; con treinta es un muro.

> **La lección:** el problema no es cuánto registrás (más `println` = más ruido), es que no
> podés seguir el hilo de UNA petición. Un sistema que no se puede observar no se puede operar.

## Lo que Carolina exige

> *«No te pido que arregles el folio —ya lo anulamos—. Quiero un sistema que sepa contar lo que
> hizo.»*

Hoy le damos ese sistema: cada petición con su hilo (`traceId`), cada línea en JSON, cada
invocación auditada. La próxima vez, la respuesta al fiscalizador es en cinco minutos.

Sigue con [`02-el-hilo-y-el-json.md`](02-el-hilo-y-el-json.md).
