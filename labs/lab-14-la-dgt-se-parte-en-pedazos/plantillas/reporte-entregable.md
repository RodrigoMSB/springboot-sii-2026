# Reporte · Lab 14 — La DGT se parte en pedazos

**Nombre:**
**Fecha:**

> El validador mide el estado del sistema. Este reporte mide tu cabeza, y es lo único que
> el instructor no puede automatizar. Transcribe los errores y las salidas **exactas** —no
> los resumas ni los interpretes—; las conclusiones van en su apartado.

---

## 1 · El mapa (bloque 1)

Dibuja las seis piezas y las flechas entre ellas, con **dos tipos de flecha**:
sólida = tráfico real de cada petición · puntos = «me anoto en» / «pido mi configuración a».

```
(a mano, o en ASCII, aquí)




```

Ahora, mirando tu dibujo:

| Si tapas… | ¿Cuántas flechas SÓLIDAS desaparecen? | ¿Qué deja de funcionar exactamente? |
|---|---|---|
| `dgt-registro` | | |
| `dgt-config` | | |
| `dgt-portal` | | |

**La frontera entre las dos bases.** Transcribe el error exacto de:

```
docker compose exec -T postgres psql -U svc_tramites -d dgt_contribuyentes -c 'SELECT 1'
```

```

```

¿Por qué esa frontera es lo que convierte a `dgt-contribuyentes` en un microservicio y no
en un módulo?

```

```

---

## 2 · El crimen (bloque 2)

**JSON con el sistema entero:**

```json

```

**JSON con `dgt-contribuyentes` apagado:**

```json

```

| Pregunta | Respuesta |
|---|---|
| Código HTTP que devolvió el portal | |
| Mensaje de error que vio el usuario | |
| Qué campo falta | |
| Cómo se enteraría el contribuyente | |

**Línea del log de `dgt-tramites` donde sí quedó constancia** (`grep FALLBACK`):

```

```

El sistema **sabía** que estaba degradado y lo escribió. ¿Por qué no basta con eso?

```

```

---

## 3 · Los cuatro números ⭐

**Lo que escribiste** (pega el bloque `circuitbreaker` tal cual):

```yaml

```

**Y por qué.** Una frase por número. Sin esto, el apartado no cuenta aunque el validador
esté verde.

| Umbral | Tu valor | Por qué ese |
|---|---|---|
| ventana / mínimo de llamadas | | |
| tasa de fallo | | |
| tiempo abierto | | |
| llamadas en medio abierto | | |

**La cuenta.** Con tu `max-attempts`, ¿cuántas **peticiones tuyas** hacen falta para que el
circuito llegue a su mínimo de llamadas?

```

```

**Los tiempos, medidos** (de `--contribuyentes-lento`):

| | Milisegundos por petición |
|---|---|
| Antes de que abriera el circuito | |
| Después de que abriera | |

**El circuit breaker no hizo que llegara el nombre del contribuyente.** Entonces, ¿qué
arregló exactamente?

```

```

---

## 4 · Escalar (bloque 3)

**Reparto con dos instancias** (pega el conteo):

```

```

¿Cuántos archivos de configuración tocaste para que la segunda instancia recibiera tráfico?

```

```

**Al matar una instancia:** ¿cuántos segundos tardó el sistema en recuperarse?

```

```

Tres cosas ocurren a la vez durante esos segundos. Nómbralas:

1.
2.
3.

¿Esos segundos son aceptables para la DGT? ¿Y si en vez de un nombre fuera el importe a
pagar?

```

```

---

## 5 · Matar al registro (bloque 4 — demo)

**Antes de la demo**, escribe tu predicción. No la corrijas después: el valor está en el
contraste.

```

```

**Acto 1** — segundos que aguantó el sistema sin registro:

```

```

**Acto 2** — al matar una instancia con el registro apagado:

```

```

Completa la frase con tus palabras:

> El registro NO es un punto único de fallo para ____________, pero SÍ lo es para
> ____________.

---

## 6 · La decisión

Esta es la pregunta del módulo, y la única que no tiene una respuesta correcta.

**Tomando la aplicación que construiste en trece sesiones** (`labs/lab-13-capsula-y-egreso/`,
sigue ahí entera): ¿la partirías en microservicios?

**Tu respuesta** — sí, no, o «solo esta parte» — **y tres razones concretas**, no genéricas:

```



```

Si dijiste que sí: **¿por dónde cortarías, y qué transacción se te rompe al cortar ahí?**
Si dijiste que no: **¿qué tendría que cambiar en la DGT para que cambiaras de opinión?**

```



```

---

## 7 · Lo que costó, medido en tu máquina

| | Tu medición |
|---|---|
| RAM del sistema completo (`docker stats`) | |
| Tiempo de arranque (segunda vez en adelante) | |

Comparado con el monolito de una sola pieza, ¿qué compraste con esa diferencia?

```

```

---

## 8 · La casilla honesta

**¿Consultaste `config-repo-solucion/`? ¿En qué momento y por qué?**

> Mirar la solución **no está prohibido**: está registrado. Se evalúa la honestidad, no la
> pureza. Un «sí, en el TODO 3, porque no entendía la diferencia entre la ventana y el
> mínimo» vale más que un «no» falso.

```

```

---

## 9 · Desafío `99-` (opcional)

¿Lo hiciste? ¿Qué camino elegiste para que el fallback dejara de mentir, y **qué perdiste**
al elegirlo?

```

```

---

## 10 · La pregunta de despedida

Catorce sesiones. Una cosa que hoy harías distinto en un sistema que ya tienes en
producción — o en el próximo que empieces:

```

```
