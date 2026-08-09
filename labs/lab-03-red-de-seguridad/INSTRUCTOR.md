# Guía del instructor · Lab 03

## Antes de la sesión

1. `./bin/91-e2e.sh` — starter en rojo, solución en verde. Si falla, no des la clase.
2. Este lab **no necesita Docker**. Pero sí Java 25.

## ORDEN PARA CLASE (180 min)

### 🔪 00:00 – 00:10 · La escena del crimen (en vivo)

Proyecta la terminal. Corre el validador sobre el starter:

```bash
./bin/90-validar.sh --dir starter
```

Deja que la pantalla roja se asiente. El `90` lista los compromisos en español. **No expliques
todavía.** Deja que alguien pregunte *"¿está roto?"*. Y respondes con Carolina:

> *«No está roto. Cada test rojo es un compromiso que acordamos con QA y que aún no cumplimos.
> No me traigas código: tráeme verde. Y los tests se leen — el que implementa sin leer el
> test, implementa otra cosa.»*

Este es el giro del curso: hasta ahora escribían código y después lo probaban. Hoy el test
viene primero, y es el enunciado.

### 📚 00:10 – 00:50 · Teoría (M3 resto + M4)

`TEORIA.md`. No te saltes:
- **§1, leer un test al revés.** Proyecta `E2_RutValidoTest` y léelo del assert hacia arriba.
- **§8, la triangulación.** Hardcodea el validador a `return true` EN VIVO, corre `E2`, y
  muestra el caso feliz pasando y el parametrizado desmintiéndolo. Es la lección del lab.
- **§6, JUnit 6 vs 4/5.** Muestra las señales de un tutorial viejo (`@RunWith`, `org.junit`).

### ☕ 00:50 – 01:00 · Descanso

### 🔧 01:00 – 02:50 · Laboratorio

**El error que cometerá la sala:** implementarán antes de leer. Alguien hará el módulo 11 "de
memoria" y su `E2` fallará en un caso raro (el DV 'K', o el RUT `1-9`). **No des el
algoritmo:** pregunta *«¿corriste el test parametrizado, o solo el caso feliz?»*. La red los
educa mejor que tú.

**Segundo:** en el TODO_4, alguien dejará el `@Mock TramiteDto` (el olor plantado) o mockeará
de más. Pregunta *«¿qué comportamiento tiene un record que necesites simular?»*.

Usa el `90` como marcador colectivo: proyéctalo cada 20 minutos, celebra cuando el conteo
sube. `9/14… 12/14…`.

### ✅ 02:50 – 03:00 · Cierre

`90`, reporte, y la **siembra del Módulo 5**:

> *«La red ya existe. La próxima semana trabajan con la base de datos de verdad, y va a venir
> configurada "como siempre funcionó": todo EAGER. Y va a funcionar, con tres trámites de
> prueba. Ese es el punto. Hay un guardián, AU-04, que ya se los advirtió sin que lo
> notaran. La próxima semana lo van a entender de golpe.»*

## Qué revisar en los reportes

1. **§2, la triangulación.** ¿Entendió por qué un test suelto no basta? Es el corazón del lab.
2. **§3, la transcripción.** ¿Copió el caso parametrizado exacto (`rutConDvFalso[2]`)?
3. **§5, qué NO se mockea.** Si mockeó el DTO, no entendió Mockito aunque el test pase.
4. **§7, honestidad.** Nunca penalices un "sí".
5. **§8, la siembra.** Si nombró AU-04, ya conecta la estructura con lo que viene.
