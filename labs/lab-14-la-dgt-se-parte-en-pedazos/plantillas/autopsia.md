# Autopsia · Lab 14

*Para cuando algo se rompa y no sepas por qué. Que en un sistema de seis piezas es a
menudo.*

> Rellénala **mientras** investigas, no después. La mitad del valor está en anotar la
> hipótesis **antes** de saber si era cierta: así descubres qué te llevó por el camino
> equivocado, que es lo que se repetirá la próxima vez.

---

## 0 · El síntoma, exacto

Qué viste, transcrito. Sin interpretar, sin resumir.

```

```

Comando que lo produjo:

```bash

```

---

## 1 · ¿Qué pieza?

En un sistema distribuido, la primera pregunta no es *«qué falló»* sino **«dónde»**. Recorre
las seis antes de teorizar:

| Pieza | Estado (`docker compose ps`) | ¿Anotada en el registro? | Última línea rara de su log |
|---|---|---|---|
| dgt-registro | | | |
| dgt-config | | | |
| dgt-portal | | | |
| dgt-contribuyentes 1 | | | |
| dgt-contribuyentes 2 | | | |
| dgt-tramites | | | |
| postgres | | — | |

```bash
cd sistema
docker compose ps -a
docker compose logs <pieza> --tail 50
curl -s http://localhost:8761/eureka/apps | grep -c '<status>UP'
```

---

## 2 · Hipótesis

**Lo que creías que estaba pasando, ANTES de comprobarlo:**

```

```

---

## 3 · Evidencia

Qué comprobaste, y qué salió. Un comando por fila, con su salida real.

| # | Comando | Qué esperabas | Qué salió |
|---|---|---|---|
| 1 | | | |
| 2 | | | |
| 3 | | | |

**Preguntas que casi siempre desatascan este laboratorio:**

- ¿Está la configuración llegando de verdad? → `curl -s localhost:8888/dgt-tramites/default`
- ¿Qué umbrales cargó el circuito? → `/actuator/circuitbreakers` en el puerto 8081
- ¿Qué dice el historial del circuito? → `/actuator/circuitbreakerevents/contribuyentes`
- ¿Es la ventana entre «arrancado» y «estable»? → espera 10 s y repite. Si se cura solo,
  era eso (T-6).

---

## 4 · La causa

**Qué era en realidad:**

```

```

**¿Coincidía con tu hipótesis?**  ☐ Sí  ☐ No

Si no: **¿qué te llevó por el camino equivocado?** Esta es la pregunta cara del documento.

```

```

---

## 5 · La corrección

Qué cambiaste, y cómo comprobaste que quedó arreglado (no «parece que va»: una salida
concreta).

```

```

---

## 6 · Detección

**¿Qué habría hecho que te enteraras antes?**

En un monolito la respuesta suele ser «un log». Aquí casi nunca lo es. Piensa en:

- un *health check* que hubiera fallado en vez de decir UP
- una traza correlacionada que cruzara las seis piezas
- una métrica del circuito publicada en un tablero
- un test que hubiera cazado esto antes de desplegar

```

```

**Y la pregunta del curso entero, la última vez que te la hacemos:**

> Si esto pasara un domingo a las tres de la mañana y tú estuvieras de guardia, **¿qué
> tendrías que tener puesto hoy para resolverlo en diez minutos en vez de en dos horas?**

```

```
