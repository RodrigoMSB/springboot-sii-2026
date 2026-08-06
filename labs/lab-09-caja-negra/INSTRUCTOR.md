# Guía del instructor · Lab 09 — caja negra

## Antes de la sesión

1. `./bin/91-e2e.sh` — el starter falla los 4 TODOs, la solución pasa. Si falla, no des la clase.
2. **Docker corriendo.** Ensaya: `./bin/start-lab.sh --dir starter --caos` (el muro) y `--dir solucion --caos` (el traceId aísla la operación).

## ORDEN PARA CLASE (180 min)

### 🔪 00:00 – 00:10 · La escena del crimen (en vivo)

```bash
./bin/start-lab.sh --dir starter --caos
```

Que la sala vea el muro entrelazado: `grep` de un folio devuelve líneas de 15 peticiones mezcladas.
Entonces, Carolina:

> *«Quiero un sistema que sepa contar lo que hizo.»*

### 📚 00:10 – 00:50 · Teoría

`TEORIA.md`. Imprescindibles:
- **§4, MDC.** El número de pedido en la cocina. El `finally` que limpia.
- **§5 y §6, AOP y el límite del proxy.** El auditor invisible; la autoinvocación que no se ve (misma trampa que @Transactional).
- **§8, archivos.** Magic bytes, no la extensión. Path traversal.

### ☕ 00:50 – 01:00 · Descanso

### 🔧 01:00 – 02:50 · Laboratorio

**El error que cometerá la sala:** olvidar el `MDC.remove` en el `finally` → el traceId de una petición
contamina la siguiente (un bug sutil y feo). Muéstralo. **Segundo:** poner el `log.info` en cada método en
vez de un aspecto — funciona, pero ensucia el negocio; el diff del servicio ya no es idéntico. **Tercero:**
confiar en el `Content-Type` para el MIME — muéstrales el `.exe` disfrazado pasando.

### ✅ 02:50 – 03:00 · Cierre — LA SIEMBRA DEL RELOJ

`90`, reporte, y la siembra del Lab 10 (`TEORIA §11`):

> *«Ahora el sistema sabe contar lo que hizo. Y contando, Carolina notó que el cierre nocturno del viernes
> se ejecutó DOS veces. Hay dos servidores, y los dos se creyeron el único.»*

## Qué revisar en los reportes

1. **§1, el muro.** ¿Entendió por qué más `println` no ayuda?
2. **§2, el traceId.** ¿Qué cambió además del formato? La correlación. Es el corazón.
3. **§3, el aspecto.** ¿El código de negocio quedó idéntico? ¿Enmascaró el RUT?
4. **§4, los archivos.** ¿Por qué los magic bytes y no la extensión?
5. **§5, honestidad.** Nunca penalices un "usé `--todo`".
