# Guía del instructor · Lab 10 — el tablero que mentía

## Antes de la sesión

1. `./bin/91-e2e.sh` — el starter falla los 4 TODOs, la solución pasa. Si falla, no des la clase.
2. **Docker corriendo.** Ensaya las dos pasadas del crimen:
   `./bin/start-lab.sh --db-caida` (el `UP` mentiroso) y
   `./bin/start-lab.sh --dir solucion --db-caida` (readiness cae y nombra la base).
3. Entre pasada y pasada: `./bin/99-destruir.sh`. El `--db-caida` deja PostgreSQL **detenido**
   a propósito (`docker compose stop`, no `down`), para que puedas repetir el experimento sin
   volver a sembrar.

## ORDEN PARA CLASE (180 min)

### 🔪 00:00 – 00:10 · La escena del crimen (en vivo)

```bash
./bin/start-lab.sh --db-caida
```

El script hace las tres cosas en orden y las imprime: app sana → `UP`; tumba PostgreSQL;
`/api/v1/tramites` → **500** y `/actuator/health` → **`UP`**. Deja los dos en pantalla, juntos.
Ese contraste es toda la clase.

Que alguien de la sala lea las dos líneas en voz alta. Entonces, Carolina:

> *«El monitoreo dice que estamos perfecto. Los contribuyentes dicen que no pueden declarar.
> ¿A quién le creo?»*

Remata con la línea del `application.yml` —`management.health.db.enabled: false`— y su
justificación escrita: *«tiraba DOWN cuando Postgres tardaba y nos llenaba de alertas»*. **No la
ridiculices.** Es una decisión razonable tomada por la razón equivocada, y medio auditorio la ha
tomado alguna vez.

### 📚 00:10 – 00:50 · Teoría

`TEORIA.md`. Imprescindibles:

- **§3, liveness vs readiness.** La analogía del hospital. Insiste en que las acciones son
  **opuestas**: reiniciar vs. no mandar tráfico. Y en el costo de mezclarlas: caída de la base
  **más** `CrashLoopBackOff`.
- **§5, qué entra en readiness.** Que TESO **no** entre es el punto fino del día: el Lab 08 nos
  enseñó a sobrevivirlo, meterlo aquí desharía ese trabajo.
- **§7, métricas de negocio.** «El CPU no te avisa que dejaste de emitir folios.»
- **§9 y §10, caché.** Las tres condiciones, y *el mentiroso con buena memoria*.
- **§6, cardinalidad.** Treinta segundos, pero dilos: etiquetar por RUT es una denegación de
  servicio contra tu propio Prometheus.

### ☕ 00:50 – 01:00 · Descanso

### 🔧 01:00 – 02:50 · Laboratorio

**El error que cometerá la sala — por orden de probabilidad:**

1. **Meter la base en `liveness`.** Es lo intuitivo: «si no puede trabajar, está mal». `E1` lo
   caza con el assert de que liveness sigue `UP`. Cuando pase, pregunta: *«¿qué haría Kubernetes
   con eso? ¿y eso levantaría PostgreSQL?»*.
2. **`@Cacheable` sin `@EnableCaching`.** La anotación no hace nada y **no avisa**: el método
   funciona, solo que sigue bajando a la base. Es el fallo más frustrante del lab porque no hay
   error que leer. `E4` lo nombra explícitamente.
3. **Olvidar el `@CacheEvict`.** Todo verde salvo el segundo test de `E4`. Aprovecha: *«no
   falló nada. Solo que el número está mal. ¿Cuánto tardarías en notarlo en producción?»*
4. **Poner `include: "*"` y "arreglarlo" con seguridad.** Aparece cuando alguien intenta que
   `/actuator/env` dé 401 en vez de 404. Vuelve a la tabla del §2: 404 no es 401.
5. **Registrar las métricas dentro de `emitir()`** en vez del constructor. Funciona… hasta que
   nadie emite y la serie desaparece.

### ✅ 02:50 – 03:00 · Cierre — LA SIEMBRA DEL RELOJ

Corre `90`, pide el reporte, y siembra el Lab 11 (`TEORIA §12`):

> *«Ya sabes si tu sistema está sano y puedes medir lo que hace. Y midiendo, Carolina vio que el
> cierre nocturno del viernes se ejecutó DOS veces. Hay dos servidores, y los dos se creyeron el
> único.»*

Engancha con el §10 de hoy: ya dijimos que cada instancia tiene su propio caché y ninguna sabe de
las otras. El mismo problema, con otro disfraz.

## Qué revisar en los reportes

1. **§1, el `UP` mentiroso.** ¿Transcribió las dos respuestas? ¿Entendió *por qué* era mentira —
   que el proceso estuviera vivo no era lo que se preguntaba?
2. **§2, liveness vs readiness.** La pregunta que separa al que entendió: *¿qué pasa si metes la
   base en liveness?* Si responde «se reinicia» pero no llega a «y eso no levanta la base»,
   está a medio camino.
3. **§3, exposición.** ¿Distingue 404 de 401? Es la respuesta que más gente confunde.
4. **§4, el caché.** ¿Sabe decir su hit-rate? ¿Y qué pasa si olvida el evict? Si dice «se rompe»,
   corrige: no se rompe. **Miente.**
5. **§5, honestidad.** Nunca penalices un «usé `--todo`». Se evalúa la honestidad, no la pureza.
