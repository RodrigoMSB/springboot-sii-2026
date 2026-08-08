# 01 · El semáforo de adorno

## Dónde estamos

El Lab 09 le enseñó al sistema a **contar lo que hizo**: `traceId`, log JSON, auditoría. Ante un
fiscalizador, hay respuesta en cinco minutos.

Pero contar lo que hizo no es lo mismo que decir **cómo está**. Y esa segunda pregunta la
responde, hoy, un tablero que no mira.

## El encargo de Carolina

> *«El monitoreo dice que estamos perfecto. Los contribuyentes dicen que no pueden declarar. ¿A
> quién le creo? Un semáforo que siempre está en verde no es un semáforo: es un adorno. Haz que
> me diga la verdad — y que cuando algo se caiga, me diga QUÉ se cayó.»*

Fíjate en que son **dos** encargos, y el segundo es el difícil:

1. Que no mienta.
2. Que **nombre** lo que falló.

Un `DOWN` anónimo a las tres de la mañana te dice que te levantes, no adónde ir.

## Lo que hay hoy

`starter/src/main/resources/application.yml`, con los comentarios que dejó el practicante:

```yaml
management:
  endpoints:
    web:
      exposure:
        include: "*"       # "para no andar agregando endpoints uno por uno"
  health:
    db:
      enabled: false       # "tiraba DOWN cuando Postgres tardaba y nos llenaba de alertas"
```

Las dos decisiones son **razonables** y las dos están mal, y esa combinación es justamente lo que
las hace peligrosas. Nadie apaga un chequeo de salud por maldad: lo apaga porque genera ruido. El
error no fue apagarlo — fue no preguntarse qué quedaba encendido después.

## Antes de seguir: predice

Antes de correr nada, escribe tu apuesta. Con PostgreSQL muerto y la app en pie:

| | Tu predicción |
|---|---|
| `GET /api/v1/tramites` | HTTP ______ |
| `GET /actuator/health` | ______________ |

Guárdala. En la siguiente guía la comparamos con lo que pasa de verdad.

→ **[02 · El tablero que mentía](02-el-tablero-que-mentia.md)**
