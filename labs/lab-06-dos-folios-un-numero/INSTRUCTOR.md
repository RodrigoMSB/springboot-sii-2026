# Guía del instructor · Lab 06 — dos folios, un número

## Antes de la sesión

1. `./bin/91-e2e.sh` — el starter falla los 4 tests, la solución pasa. Si falla, no des la clase.
2. **Docker corriendo.** Ensaya el sabotaje: `./bin/start-lab.sh --dir starter --concurrencia 6`.
   La carrera es una carrera: puede tardar uno o dos intentos en chocar. El script reintenta.
3. Ensaya en vivo hasta que salga limpio: los 500 y/o el número repetido en pantalla.

## ORDEN PARA CLASE (180 min)

### 🔪 00:00 – 00:10 · La escena del crimen (en vivo)

Proyecta la terminal:

```bash
./bin/start-lab.sh --dir starter --concurrencia 6
```

Que la sala vea dos (o más) emisiones dispararse a la vez, y una caerse con HTTP 500
(`duplicate key`) o dos llevarse el mismo folio. No expliques todavía. Y entonces, Carolina:

> *«Un folio emitido dos veces no se borra. Se explica. Ante un fiscalizador. Y de paso: el
> folio 8 no existe — ¿dónde está? Los folios no se saltan. Esto no es una tabla más: es un
> libro foliado.»*

### 📚 00:10 – 00:50 · Teoría

`TEORIA.md`. Imprescindibles:
- **§2, el proxy transaccional.** El `@Transactional` en un `private` o autoinvocado no hace
  nada. Y el lock necesita la transacción. Es el enganche TODO_1 ⇄ TODO_4.
- **§5 y §6, los dos parches del acto 2.** `synchronized` (funciona en UNA JVM; el Lab 10 lo
  rompe) y `REQUIRES_NEW` (gasta el número: muestra `E4` en rojo, `expected 1 but was 2`).
- **§11, la restricción como contrato.** OJO: el `CHECK` es `monto <> 0`, no `monto >= 0` (los
  créditos son negativos). La última línea de defensa vive en el motor, no en Java.

### ☕ 00:50 – 01:00 · Descanso

### 🔧 01:00 – 02:50 · Laboratorio

**El error que cometerá la sala:** pondrán `synchronized` y el test pasará. Déjalos celebrar
diez segundos, luego pregunta: *«¿y cuando corran dos instancias?»*. Que descubran que el
candado está en el código, no en el dato.

**Segundo error:** alguien "aislará" la toma del número con `REQUIRES_NEW`. `E4` se pondrá
rojo. Es el mejor momento del lab: el parche elegante crea el salto. Muéstralo.

**Tercero:** al escribir la V3, alguien pondrá `monto >= 0` y Flyway ni siquiera arrancará
(la semilla tiene créditos negativos). Es la lección viva: la restricción debe caber en el
dominio real, no en el que imaginamos.

### ✅ 02:50 – 03:00 · Cierre — LA SIEMBRA DEL PORTERO

`90`, reporte, y la siembra del Lab 07 (`TEORIA §13`):

> *«Los folios ya no se repiten, no se saltan, y un reintento no crea basura. Hay un solo
> problema, y es grande: cualquiera con curl puede emitirlos. Nadie pregunta en la puerta "¿y
> usted quién es?". La próxima semana, la puerta tiene portero.»*

## Qué revisar en los reportes

1. **§1, el crimen.** ¿Transcribió el 500 / el número repetido?
2. **§2, los dos parches.** ¿Entendió por qué `synchronized` y `REQUIRES_NEW` fallan? Es el corazón.
3. **§3, el contrato.** ¿Entendió por qué `monto <> 0` y no `monto >= 0`? ¿Por qué el CHECK no es redundante con Java?
4. **§4, la siembra.** ¿Anticipa el portero?
5. **§5, honestidad.** Nunca penalices un "usé `--todo`".
