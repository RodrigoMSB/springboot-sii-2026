# 02 · El tablero que mentía

## Vívelo

```bash
cd ..
./bin/start-lab.sh --db-caida
```

El script levanta la app **con su base sana**, espera a que responda, y solo **después** tumba
PostgreSQL. Ese orden importa: arrancar sin base no serviría —Flyway y la validación de Hibernate
corren al inicio, así que la app ni siquiera levantaría—, y un proceso que no arranca no engaña a
nadie. Lo que queremos ver es un proceso **vivo**, sano según el tablero, e incapaz de trabajar.
Es, además, lo que pasa de verdad: nadie despliega sin base; la base se cae un martes a las tres.

Lo que verás:

```
GET /api/v1/tramites            ->  HTTP 500   <- el negocio está caído
GET /actuator/health            ->  {"status":"UP"}
GET /actuator/health/liveness   ->  {"status":"UP"}
GET /actuator/health/readiness  ->  {"status":"UP"}
```

Compáralo con tu predicción de la guía anterior.

> **El token se pide antes de tumbar la base, y no es un detalle.** El login consulta la tabla
> `usuario`: con PostgreSQL muerto no se puede autenticar nadie. Sin token, `/api/v1/tramites`
> respondería **401** —«no te conozco»— y taparía justo lo que hay que ver: un **500**, que
> significa «te conozco, pero no puedo trabajar». El JWT es sin estado y sigue valiendo sin base.
> Es un buen recordatorio de que un código de estado mal leído manda la investigación al lugar
> equivocado.

## La tercera línea es la peor

Fíjate bien en `readiness`. **La ruta existe** —Boot 4 activa las sondas por defecto— y responde
`UP`.

Eso es peor que si no existiera. Una sonda ausente se nota: el orquestador falla al configurarla
y alguien lo arregla. Una sonda **presente**, conectada al balanceador, que responde `UP` con la
base muerta, es el adorno perfecto: nadie duda de ella justamente porque contesta.

El grupo `readiness` viene con un solo componente, `readinessState`, que significa «la aplicación
terminó de arrancar». Es verdad. Y no es la pregunta.

## Por qué miente

No miente por un bug. Miente porque **está respondiendo a otra pregunta**.

Con `management.health.db.enabled: false` no queda nadie mirando la base. Lo que sigue encendido
—el chequeo de disco, el `ping`— confirma que el proceso Java existe. Y el proceso existe: está
ahí, escuchando, devolviendo 500 con toda puntualidad.

> Preguntar «¿existe el objeto `DataSource`?» es preguntarle al enfermo si respira mirándole la
> fotografía. El objeto lo creó Spring al arrancar y seguirá ahí aunque el motor lleve una hora
> muerto. Hay que ir a la base y volver.

## Y hay un segundo crimen en el mismo archivo

Con la app arriba, autentícate como Carolina y pide esto:

```bash
curl -s localhost:8099/actuator/env | head -c 400
```

Responde. Ahí dentro va la configuración resuelta **entera**: variables de entorno incluidas —es
decir, el secreto de firma del JWT y la contraseña de la base—. Es el crimen del Lab 01 otra vez,
por otra puerta, y lo abrió un `include: "*"` escrito para ahorrar tiempo.

Prueba también `/actuator/heapdump`: descarga la **memoria del proceso**. Todo lo que no estaba
en `/env`, está ahí.

## Las dos preguntas que no son la misma

Antes de arreglar nada, quédate con esto, porque es el eje del lab:

| | Pregunta | Si dice DOWN, ¿qué hago? |
|---|---|---|
| **liveness** | ¿Está **vivo** el proceso? | **Reiniciarlo.** |
| **readiness** | ¿Puede **atender**? | **No mandarle tráfico** — y esperar. |

Hoy la aplicación está **viva** y **no puede atender**. Un solo semáforo no puede decir las dos
cosas, y responder mal a cualquiera de ellas cuesta caro:

- Si mientes sobre readiness, el balanceador te manda tráfico que vas a rechazar.
- Si mientes sobre liveness, alguien reinicia una aplicación perfectamente sana.

→ **[03 · El parche que funciona (y por qué no basta)](03-el-parche-que-funciona.md)**
