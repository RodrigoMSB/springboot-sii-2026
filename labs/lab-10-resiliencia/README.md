# Lab 10 · Resiliencia

Tu aplicación está bien escrita. La de al lado, no responde.

Hoy no se arregla nuestro código: se aprende a **sobrevivir al código de otros**. La aplicación
llama a Tesorería para confirmar un pago, y Tesorería se va a poner lenta y se va a caer — a
propósito, con un botón.

## El número del laboratorio

Los tiempos que espera el usuario, medidos en la máquina donde se preparó el material:

| situación | el usuario espera | llamadas HTTP reales |
|---|---|---|
| Tesorería sana | 0,15 s | 1 |
| **Tesorería a 30 s, cliente ingenuo** | **30,01 s** | 1 |
| Tesorería a 30 s, con timeout de 2 s | 2,04 s — y un error 500 | 1 |
| Tesorería a 30 s, con timeout y 3 intentos | 6,44 s | 3 |
| Tesorería caída, primera petición | 0,22 s | 1 |
| **Tesorería caída, circuito ya abierto** | **0,002 s** | **0** |

Las dos negritas son el arco del día: **de 30 segundos a 2 milésimas**, y de golpear un servicio
caído a dejarlo en paz.

## Qué se aprende

- Que **un cliente HTTP sin timeout espera para siempre**, y que ese es el valor por defecto.
- Que un timeout no arregla nada: convierte una espera infinita en un **error rápido**, que es
  mejor pero no es funcionar.
- Cuándo un reintento ayuda y cuándo **empeora la caída** — con el número que lo demuestra.
- Qué es un **circuit breaker**, sus tres estados, y por qué dejar de llamar es una forma de
  ayudar.
- Que la última pieza no es técnica: **qué responder cuando no hay respuesta.**

## Tesorería, sin Docker

El servicio de al lado es **WireMock corriendo como librería dentro del mismo proceso**. No hay
contenedor, no hay segundo terminal, no hay nada que instalar. Y trae mando a distancia:

```bash
curl -X POST http://localhost:8097/simulador/sana
curl -X POST "http://localhost:8097/simulador/lenta?segundos=30"
curl -X POST http://localhost:8097/simulador/caida
```

## Las tres carpetas

| | |
|---|---|
| **`practica/`** | Donde trabajas. El cliente llega **sin timeout** y el servicio **sin protección** |
| **`solucion/`** | Timeout, reintento, circuito y degradación, funcionando |
| **`instructor/`** | Los mismos archivos explicados línea por línea. **No viaja en el repositorio** |

## Cómo se corre

```bash
cd practica          # o solucion
./mvnw spring-boot:run
```

| | HTTP | Tesorería simulada |
|---|---|---|
| `practica/` | **8097** | 9097 |
| `solucion/` | **8098** | 9098 |

## Los endpoints

```
GET  /pagos/{id}              consulta el pago en Tesorería
GET  /pagos/estado-circuito   el estado del circuito y sus métricas
POST /simulador/sana|lenta|caida    el mando a distancia de Tesorería
```

## El paso que hay que llegar a hacer

El **paso 4**, cuando el circuito abre. Se piden pagos con Tesorería caída y se mira el contador
de llamadas HTTP reales:

```
petición 1: 0.215s   circuito=OPEN   httpReales=1
petición 2: 0.002s   circuito=OPEN   httpReales=1
petición 3: 0.002s   circuito=OPEN   httpReales=1
```

**El contador no se mueve.** Las peticiones 2 y 3 se resolvieron sin tocar la red: el circuito
sabe que Tesorería está caída y ya no la molesta. Cien veces más rápido, y cero presión sobre un
servicio que está intentando levantarse.

## Lo que no vimos hoy

- **Bulkheads**: reservar un número fijo de hilos por dependencia, para que la que se cuelga no se
  lleve por delante los hilos que atienden a todo lo demás.
- **Rate limiting**: protegerse del exceso de llamadas *entrantes*, que es el problema simétrico
  del de hoy.
- **Backoff exponencial con jitter**: reintentar esperando cada vez más y con un margen aleatorio,
  para que mil clientes no reintenten todos en el mismo milisegundo. Hoy el reintento espera
  200 ms fijos, que es suficiente para ver el mecanismo y no para producción.

## El guion

`PASOS.md` — los cinco pasos de la sesión.
