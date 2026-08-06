# Guía del instructor · Lab 08 — diplomacia con Tesorería

## Antes de la sesión

1. `./bin/91-e2e.sh` — el starter falla los TODOs de resiliencia, la solución pasa. Si falla, no des la clase.
2. **Docker corriendo**, y el puerto **8089** libre (es el de TESO/WireMock). Ensaya:
   `./bin/start-lab.sh --dir starter --teso-lento 30000` (la API se cuelga) y `--dir solucion --teso-lento 30000` (viva).

## ORDEN PARA CLASE (180 min)

### 🔪 00:00 – 00:10 · La escena del crimen (en vivo)

```bash
./bin/start-lab.sh --dir starter --teso-lento 30000
```

Que la sala vea 12 pagos colgados y el `GET /tramites` —que no toca pagos— muriéndose. Entonces, Carolina:

> *«TESO se cayó a las 9. A las 9:02 nosotros también — y nosotros no tenemos nada malo.»*

### 📚 00:10 – 00:50 · Teoría

`TEORIA.md`. Imprescindibles:
- **§1 y §2, el rehén y el timeout.** El hilo/conexión secuestrado. Timeout = presupuesto de espera.
- **§3, por qué agrandar el pool no resuelve.** Posponer no es resolver.
- **§4, la mala noticia rápida.** El 503 elegante. La pregunta de criterio.
- **§9, la hora de M9.** CORS nominal, CSRF con criterio.

**Demo del relator (Feign):** muestra un cliente Feign al lado de un `@HttpExchange`. Misma idea; Feign en
mantenimiento. Criterio: sistema viejo con Feign, quédate; desarrollo nuevo, HTTP Interfaces nativas.

### ☕ 00:50 – 01:00 · Descanso

### 🔧 01:00 – 02:50 · Laboratorio

**El error que cometerá la sala:** agrandar el pool en vez de poner timeout. Muéstrales que con pool N+1 pagos
muere igual. **Segundo:** poner el timeout global en vez de dirigido al cliente de TESO — todos los endpoints
pagan el peaje. **Tercero:** al hacer CORS, poner `*` "para que funcione". Muéstrales `E4`: el intruso pasa.

### ✅ 02:50 – 03:00 · Cierre — LA SIEMBRA DE LOS 400 MB

`90`, reporte, y la siembra del Lab 09 (`TEORIA §12`):

> *«TESO ya no puede matarnos. Pero anoche alguien emitió un folio al contribuyente equivocado, y Carolina
> llegó con 400 MB de logs y una sola pregunta: encuéntralo. La próxima semana traes lupa.»*

## Qué revisar en los reportes

1. **§1, el crimen.** ¿Transcribió el `/tramites` colgado?
2. **§2, el timeout.** ¿Entendió connect vs read, y por qué cortos y dirigidos? Es el corazón.
3. **§3, la mala noticia.** ¿Por qué 503 rápido > 30 s?
4. **§4, CORS.** ¿Por qué `*` es rendirse?
5. **§5, honestidad.** Nunca penalices un "usé `--todo`".
