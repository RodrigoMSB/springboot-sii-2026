# Guía del instructor · Lab 07 — el portero

## Antes de la sesión

1. `./bin/91-e2e.sh` — el starter falla los 4 TODOs de seguridad, la solución pasa. Si falla, no des la clase.
2. **Docker corriendo.** Ensaya el crimen: `./bin/start-lab.sh --dir starter --crimen` (anónimo 201, base64 200)
   y `--dir solucion --crimen` (los dos 401).

## ORDEN PARA CLASE (180 min)

### 🔪 00:00 – 00:10 · La escena del crimen (en vivo)

```bash
./bin/start-lab.sh --dir starter --crimen
```

Emite un folio anónimo (201). Luego fabrica un token en vivo: `echo -n 'ladron:FUNCIONARIO' | base64`,
y úsalo (200). Que la sala vea lo fácil. Entonces, Carolina:

> *«Acabo de emitir un folio desde la micro. Yo ni siquiera trabajo en emisión.»*

### 📚 00:10 – 00:50 · Teoría

`TEORIA.md`. Imprescindibles:
- **§3, denegar por defecto.** La lista blanca es de puertas abiertas.
- **§6, un token sin firma es una opinión.** La frase del lab: codificar ≠ cifrar ≠ firmar.
- **§8, 401 vs 403.** La analogía del edificio. Valentina recibe 403, no 401.

### ☕ 00:50 – 01:00 · Descanso

### 🔧 01:00 – 02:50 · Laboratorio

**El error que cometerá la sala:** poner la lista blanca al revés (cerrar lo privado, abrir el resto)
y dejar un endpoint sin regla → abierto. Muéstrales `E1`: la ruta sin regla nace CERRADA.

**Segundo:** al validar, alguien decodificará el JWT "a mano" para leer el rol y confiará en él. `E3`
lo mata: la firma es lo que se valida, no el contenido.

**Tercero:** confundir 401 y 403. Que corran `E4` y vean a Valentina recibir 403 (autenticada, sin rol).

### ✅ 02:50 – 03:00 · Cierre — LA SIEMBRA DE TESORERÍA

`90`, reporte, y la siembra del Lab 08 (`TEORIA §12`):

> *«La puerta ya tiene portero. Pero mañana la DGT habla con Tesorería para confirmar pagos, y TESO
> se demora treinta segundos. La próxima semana, toda tu API se muere esperando un pago. Trae
> paciencia. O mejor: no la traigas.»*

## Qué revisar en los reportes

1. **§1, el crimen.** ¿Transcribió el 201 anónimo y el token fabricado?
2. **§2, la firma.** ¿Entendió que el servidor VALIDA la firma, no lee el contenido? Es el corazón.
3. **§3, 401 vs 403.** ¿Distingue "no sé quién eres" de "no puedes esto"?
4. **§4, el secreto.** ¿Entendió por qué la clave va fuera del repo?
5. **§5, honestidad.** Nunca penalices un "usé `--todo`".
