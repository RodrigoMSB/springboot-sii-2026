# Bloque 2 · Matar al proveedor ⭐ (~40 min)

*El bloque central del laboratorio. Aquí está el único tecleo del día.*

---

## §1 · El crimen, otra vez y en tus manos

```bash
./bin/start-lab.sh --matar-contribuyentes
```

```
  ----------  ANTES: el sistema entero  ----------

     {"id":1,...,"nombreContribuyente":"Valentina Rojas","atendidoPor":"dgt-contribuyentes:801639643da0"}

  ----------  «Ahora apaga el servicio de contribuyentes»  ----------

[OK]    dgt-contribuyentes apagado (las dos instancias)

  ----------  DESPUÉS  ----------

     [2228 ms]  {"id":1,...,"nombreContribuyente":null,"atendidoPor":null}
     [2217 ms]  {"id":1,...,"nombreContribuyente":null,"atendidoPor":null}
```

**Contesta esto en tu reporte antes de seguir leyendo:**

1. ¿Qué código HTTP devolvió el portal?
2. ¿Qué mensaje de error vio el usuario?
3. Si tú fueras el contribuyente mirando esa pantalla, **¿cómo te enterarías de que falta
   el nombre del titular de tu declaración?**

La respuesta a la tercera es: no te enteras. Y hay una firma al final del formulario.

---

## §2 · Quién decidió ese `null`

No lo decidió el framework. Lo decidió una persona, y está escrito aquí:

`sistema/dgt-tramites/src/main/java/cl/dgt/tramites/ConsultaDeContribuyentes.java`

```java
private FichaContribuyente fichaDegradada(String rut, Throwable causa) {
    log.warn("[FALLBACK] No pude consultar al servicio de contribuyentes para {} — {}", rut, causa);
    return new FichaContribuyente(rut, null, null);
}
```

Un *fallback* es **una decisión de negocio disfrazada de detalle técnico**. Había cuatro
opciones sobre la mesa:

| Opción | Qué le dice al usuario |
|---|---|
| Datos parciales en silencio ← *la que está* | «aquí tienes» (y miente por omisión) |
| Datos parciales **marcados** | «esto está incompleto» |
| HTTP 503 honesto | «ahora mismo no puedo» |
| Último valor conocido, desde caché | «esto es de hace un rato» |

Las cuatro son defendibles según el caso. Lo que no es defendible es **elegir sin darte
cuenta de que estás eligiendo** — y el valor por defecto siempre es el primero, porque es
lo que sale de escribir el fallback sin pensarlo.

**Míralo en el log**, que es donde sí quedó constancia:

```bash
cd sistema && docker compose logs dgt-tramites | grep FALLBACK | tail -3
```

El sistema **sabía** que estaba degradado. Lo escribió. Simplemente no se lo dijo a nadie
que pudiera hacer algo al respecto.

---

## §3 · Un servicio lento es peor que uno caído

Levanta al proveedor otra vez y ponlo **lento** en vez de muerto:

```bash
cd sistema && docker compose start dgt-contribuyentes && cd ..
./bin/start-lab.sh --contribuyentes-lento
```

Ahora `dgt-contribuyentes` tarda 5 segundos en contestar. No está caído: está **peor**.

- Un servicio **caído** te contesta «conexión rechazada» en un milisegundo. Te enteras
  enseguida y puedes reaccionar.
- Un servicio **lento** se queda con tu hilo. Y con el del que te llamó a ti. Y con el del
  que le llamó a ese. Suficiente tráfico, y el sistema entero se para por culpa de **una**
  pieza. Se llama **fallo en cascada**.

Mira la salida:

```
     #    tiempo     circuito    nombre del contribuyente
     #1     4228 ms  CLOSED      AUSENTE
     #2     4219 ms  CLOSED      AUSENTE
     ...
     #8     4216 ms  CLOSED      AUSENTE

[WARN]  El circuito sigue CLOSED después de 8 peticiones
```

Dos cosas que anotar:

**Primera: 4,2 segundos, siempre.** El `read-timeout` está en 2 s y el retry hace dos
intentos: 2 + 0,2 + 2 = 4,2. Cada petición del contribuyente cuesta cuatro segundos de
espera para acabar devolviendo un `null`. Estás **pagando el precio completo del fallo** y
además no obtienes el dato.

**Segunda, y es el TODO de hoy: el circuito no abre.** Ocho peticiones, dieciséis llamadas
fallidas, y sigue `CLOSED`.

### Por qué no abre

Ábrelo tú mismo: `sistema/config-repo/dgt-tramites.yml`, al final.

```yaml
  circuitbreaker:
    instances:
      contribuyentes:
        register-health-indicator: true
        # (aquí no hay ni un umbral)
```

La instancia existe. No está rota. Resilience4j la crea con sus valores por defecto:

```
sliding-window-size                           100
minimum-number-of-calls                       100   ← este
failure-rate-threshold                        50 %
wait-duration-in-open-state                   60 s
permitted-number-of-calls-in-half-open-state  10
```

**Cien llamadas** antes de que el circuito llegue siquiera a *opinar*. En la sesión de hoy
no hay cien llamadas. En un servicio interno con poco tráfico, tampoco.

> Eso es un **circuit breaker decorativo**: está en el árbol de dependencias, sale en el
> diagrama de arquitectura, y no va a abrirse jamás. Un patrón de resiliencia mal
> configurado es peor que no tenerlo, porque da la tranquilidad sin dar la protección.

---

## §4 · ⭐ Tu trabajo: los cuatro números

Abre `sistema/config-repo/dgt-tramites.yml` y escribe los cuatro umbrales dentro de
`contribuyentes:`. Los TODO están ahí, cada uno con su tensión explicada.

**Antes de elegir, dos cuentas que hay que tener claras:**

**1. El circuito cuenta llamadas, no clics.** Resilience4j compone así:

```
Retry ( CircuitBreaker ( tu método ) )
```

Con `max-attempts: 2`, **una** petición del contribuyente son **dos** llamadas contadas por
el circuito. Si tu mínimo es 6, se abre a las 3 peticiones.

**2. El escenario tiene ~8 peticiones.** Es lo que cabe en la actividad, y es de un orden
realista para un servicio interno. Si tu circuito necesita más que eso, en esta clase no
abre — y en la guardia de un domingo, tampoco.

### Aplícalo

```bash
./bin/start-lab.sh --reiniciar-tramites
```

El Config Server sirve tu archivo enseguida; el servicio lo lee **al arrancar**. Por eso se
reinicia ese contenedor y no todo el sistema.

### Compruébalo

```bash
./bin/start-lab.sh --contribuyentes-lento
```

Lo que buscas:

```
     #1     4224 ms  OPEN        AUSENTE
     #2       10 ms  OPEN        AUSENTE
     #3        7 ms  OPEN        AUSENTE
```

**De 4224 ms a 7 ms.** Eso es *fallar rápido*.

Y ojo con lo que **no** arregló: el nombre sigue sin llegar. El circuit breaker no repara
nada. Lo que hace es dejar de gastar hilos esperando a quien no va a contestar, y dejar de
echarle tráfico encima al que está intentando levantarse. **Convierte una caída lenta y
cara en un «no» barato.**

### El veredicto

```bash
./bin/90-validar.sh
```

---

## §5 · Míralo por dentro

El estado del circuito no hay que adivinarlo — Actuator lo publica:

```bash
cd sistema
docker compose exec -T dgt-tramites curl -s http://localhost:8081/actuator/circuitbreakers \
  | python3 -m json.tool
```

```json
{
  "circuitBreakers": {
    "contribuyentes": {
      "bufferedCalls": 6,
      "failedCalls": 6,
      "failureRate": "100.0%",
      "notPermittedCalls": 12,
      "state": "OPEN"
    }
  }
}
```

`notPermittedCalls` son las llamadas que el circuito **rechazó sin hacer**. Cada una es un
timeout que no esperaste y un hilo que no bloqueaste.

Y el historial completo de transiciones, que vale su peso en oro cuando algo va mal:

```bash
docker compose exec -T dgt-tramites curl -s \
  http://localhost:8081/actuator/circuitbreakerevents/contribuyentes | python3 -m json.tool | head -40
```

---

## §6 · Ver cómo se cura

Con `wait-duration-in-open-state: 10s`, el circuito se queda abierto diez segundos y
después **pasa a medio abierto** y deja pasar unas pocas llamadas de prueba.

Devuelve el proveedor a la normalidad y mira el ciclo completo:

```bash
./bin/95-recuperar.sh --solo-velocidad
```

**Un detalle que casi todo el mundo se deja**, y que probablemente esté en tu configuración
si no lo copiaste: sin
`automatic-transition-from-open-to-half-open-enabled: true`, el circuito **no** pasa solo a
medio abierto cuando vence el reloj — espera a que llegue una llamada. En un sistema con
poco tráfico se queda abierto mucho más de lo que dice tu configuración, y parece un fallo
del framework. No lo es: es el default.

---

## Para el reporte

- Tus cuatro números **y por qué esos**. Sin el porqué no valen aunque el validador se
  ponga verde.
- Los tiempos antes y después de que abriera el circuito.
- Qué haría falta para que el fallback dejara de mentir por omisión.

Cuando lo tengas: **`03-escalar.md`**.
