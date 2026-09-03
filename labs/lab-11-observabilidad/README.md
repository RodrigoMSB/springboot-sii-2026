# Lab 11 · Observabilidad

La aplicación funciona y no cuenta nada.

En el Lab 10 el circuito abrió, el servicio se degradó y el usuario recibió una respuesta rara
pero razonable. **Nadie se enteró.** Hoy la aplicación aprende a decir qué está viviendo: si está
sana, y —cuando algo se cae— **qué** se cayó.

## Qué se aprende

- Qué expone **Actuator**, y sobre todo **qué no se expone nunca** en producción.
- Un **id de correlación** por petición en el MDC: seguir *una* petición entre miles en el log,
  sin tocar una línea del código de negocio.
- Un **health indicator** que no dice «UP» porque sí: consulta la base y **nombra** la causa.
- **Liveness contra readiness**, que no son lo mismo y confundirlos cuesta caro: uno reinicia el
  proceso, el otro lo saca de rotación.

## El momento del laboratorio

Las dos sondas, con la base arriba y con la base caída:

```
                    base arriba          base caída
liveness            200 UP               200 UP        ← el proceso está sano
readiness           200 UP               503 DOWN      ← no puede atender
```

y el health dice **qué** pasó, no sólo que pasó:

```json
{"status":"DOWN","components":{"baseDeDatos":{"status":"DOWN",
  "details":{"causa":"la base de datos no responde",
             "detalle":"Connection to localhost:55442 refused.",
             "milisegundos":2003}}}}
```

**Liveness sigue en 200 a propósito.** Reiniciar el proceso no arregla una base caída: sólo tira
las peticiones que estaba atendiendo. Lo que hay que hacer es **sacarlo de rotación** hasta que la
base vuelva — y eso es readiness.

La columna de la derecha se **explica**, no se corre: provocar la caída en vivo costaba media
aplicación de andamiaje y no enseñaba nada que estas dos columnas no digan.

## Las tres carpetas

| | |
|---|---|
| **`practica/`** | Donde trabajas. Sin Actuator, sin traceId y sin health propio |
| **`solucion/`** | Todo puesto y funcionando |
| **`instructor/`** | Los mismos archivos explicados línea por línea. **No viaja en el repositorio** |

> **`entities/` y `models/` no son lo mismo, y por eso no se llaman igual.** Cada clase de
> `entities/` está **mapeada a una tabla**: lo que se le hace al objeto termina en la base. Los
> `models/` de los labs 02, 03 y 08 son lo contrario — objetos que viven en memoria, sin tabla
> detrás. El nombre distinto es deliberado: dice de un vistazo si hay una fila al otro lado.

## Cómo se corre

```bash
cd practica          # o solucion
./mvnw spring-boot:run
```

| | HTTP | PostgreSQL |
|---|---|---|
| `practica/` | **8101** | **55442** |
| `solucion/` | **8102** | **55443** |

## Los endpoints

```
POST /tramites                      emite un trámite
GET  /tramites                      los lista

GET  /actuator/health               con detalle, incluido el indicador propio
GET  /actuator/health/liveness      ¿hay que reiniciar el proceso?
GET  /actuator/health/readiness     ¿puede atender peticiones?
GET  /actuator/info                 curso y número de lab
```

## Lo que no vimos hoy

- **Métricas**, y quién las recoge. Declarar un `Counter` de trámites emitidos con Micrometer son
  tres líneas; lo que no cabe aquí es **Prometheus** preguntando cada quince segundos y un tablero
  dibujando la curva. Y sin eso, una métrica es un número que se pierde al reiniciar. Por eso
  `metrics` ni siquiera está en la lista de endpoints expuestos.
- **Logs centralizados**: los de todas las instancias en un solo sitio, buscables. Spring Boot 4
  trae logging estructurado nativo —`logging.structured.format.console: ecs`— y cada línea sale
  como JSON sin añadir una dependencia.
- **Tracing distribuido** (OpenTelemetry, Zipkin): el `traceId` de hoy vive dentro de esta
  aplicación. Cuando son cinco servicios, hace falta que el id **viaje** entre ellos y que algo
  dibuje el recorrido completo. El filtro del paso 2 ya está preparado —respeta el id que llega en
  la cabecera—, pero el resto es otra sesión.
- **Alertas**: que alguien se entere a las tres de la mañana sin estar mirando.

## El guion

`PASOS.md` — el paso 0 y los tres pasos de la sesión.
