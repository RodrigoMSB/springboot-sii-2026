# Lab 10 · Observabilidad

La aplicación funciona y no cuenta nada.

En el Lab 09 el circuito abrió, el servicio se degradó y el usuario recibió una respuesta rara
pero razonable. **Nadie se enteró.** Hoy la aplicación aprende a decir qué está viviendo: si está
sana, cuánto trabajo hizo, y —cuando algo se cae— **qué** se cayó.

## Qué se aprende

- Qué expone **Actuator**, y sobre todo **qué no se expone nunca** en producción.
- Un **id de correlación** por petición en el MDC: seguir *una* petición entre miles en el log.
- Una **métrica de negocio** propia, y verla subir.
- Un **health indicator** que no dice «UP» porque sí: consulta la base y **nombra** la causa.
- **Liveness contra readiness**, que no son lo mismo y confundirlos cuesta caro: uno reinicia el
  proceso, el otro lo saca de rotación.

## El momento del laboratorio

Se tira la base de datos con la aplicación viva:

```
                    base arriba          base caída
liveness            200 UP               200 UP        ← el proceso está sano
readiness           200 UP               503 DOWN      ← no puede atender
```

y el health dice **qué** pasó, no sólo que pasó:

```json
{"status":"DOWN","components":{"baseDeDatos":{"status":"DOWN",
  "details":{"causa":"la base de datos no responde",
             "detalle":"HikariPool-1 - Connection is not available..."}}}}
```

**Liveness sigue en 200 a propósito.** Reiniciar el proceso no arregla una base caída: sólo tira
las peticiones que estaba atendiendo. Lo que hay que hacer es **sacarlo de rotación** hasta que la
base vuelva — y eso es readiness.

## Las tres carpetas

| | |
|---|---|
| **`practica/`** | Donde trabajas. Sin Actuator, sin traceId, sin métricas ni health propio |
| **`solucion/`** | Todo puesto y funcionando |
| **`instructor/`** | Los mismos archivos explicados línea por línea. **No viaja en el repositorio** |

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
POST /tramites                      emite un trámite (y mueve la métrica)
GET  /tramites                      los lista
POST /simulador/base-caida          tira la base de datos
POST /simulador/base-sana           la vuelve a levantar

GET  /actuator/health               con detalle, incluido el indicador propio
GET  /actuator/health/liveness      ¿hay que reiniciar el proceso?
GET  /actuator/health/readiness     ¿puede atender peticiones?
GET  /actuator/metrics/dgt.tramites.emitidos
```

## Lo que no vimos hoy

- **Prometheus y Grafana**: quién recoge estas métricas cada quince segundos y las dibuja. Hoy se
  miran a mano con `curl`, que sirve para entender el mecanismo y para nada más.
- **Tracing distribuido** (OpenTelemetry, Zipkin): el `traceId` de hoy vive dentro de esta
  aplicación. Cuando son cinco servicios, hace falta que el id **viaje** entre ellos y que algo
  dibuje el recorrido completo. El filtro del paso 2 ya está preparado —respeta el id que llega en
  la cabecera—, pero el resto es otra sesión.

## El guion

`PASOS.md` — los cinco pasos de la sesión.
