# SPEC-032 · Los cinco labs que faltan — cerrar el arco

**Emite:** Arquitecto · **Ejecuta:** mocito
**Fecha:** 17 de agosto de 2026
**Rama:** `spec-032-labs-08-12` desde `main` (v0.7.0) · PR contra `main`
**Prefijo de commits:** `SPEC-032: <qué>`
**Autorización:** merge y tag sin firma del PO.

---

## 0 · Qué se hace

El arco nuevo llega hasta el Lab 07 (Testing). Esta SPEC crea **los cinco que faltan** y cierra
el curso. Con esto, todo el material que el alumno usa está en el formato guiado y el arco viejo
queda obsoleto por completo.

| Nuevo | Tema | Sale de |
|---|---|---|
| `lab-08-seguridad` | Autenticación, BCrypt, JWT, roles | `lab-07-el-portero` |
| `lab-09-resiliencia` | Timeouts, reintentos, circuit breaker | `lab-08-diplomacia-con-tesoreria` |
| `lab-10-observabilidad` | Actuator, métricas, logging estructurado | `lab-10-observabilidad` + logging del `lab-09-caja-negra` |
| `lab-11-tareas` | `@Scheduled`, `@Async`, hilos virtuales | `lab-11-latidos` |
| `lab-12-empaquetado` | Jar por capas, contenedores, imagen OCI | `lab-13-capsula-y-egreso` |

**Descartados por decisión del PO, no se convierten:** el lab de colas (RabbitMQ) y el de AOP
puro. Microservicios queda fuera de esta SPEC.

**Los labs viejos no se tocan ni se borran** en esta SPEC. Su retirada es una SPEC posterior.

⚠️ **Los directorios nuevos chocan de nombre con los viejos** (`lab-08-...`, `lab-10-...`,
`lab-11-...`). Los nuevos usan los nombres de la tabla y los viejos conservan los suyos
(`lab-08-diplomacia-con-tesoreria` ≠ `lab-08-seguridad`), así que **no hay colisión de
directorio**. Verificarlo antes de crear y reportar si alguno coincide exactamente.

---

## 1 · Reglas — las mismas del Lab 07, sin excepción

**Tres carpetas por lab:**

- `practica/` — proyecto ejecutable, incompleto, **sin documentación**: la firma, una línea
  imperativa y `// escribe aquí`. Nada más.
- `solucion/` — proyecto ejecutable, completo, **poca documentación**: una o dos líneas donde no
  sea evidente.
- `instructor/` — **solo los archivos** (`.java`, `.yml`, `pom.xml`) con la estructura de
  carpetas del proyecto, **documentados de principio a fin, imports incluidos**. No ejecutable
  (sin `mvnw`, `.mvn`, `target`). Cubierto por `.gitignore`, no llega al repositorio.

Maleta (shim + JDK embebido + `repo-maven`), `practica/` arranca en su estado de entrega,
`logging.level.root: WARN`, README con «lo que no vimos hoy», `PASOS.md` con
«se explica / se escribe / se corre / en consola».

**Sin narrativa DGT, sin citas de personajes, sin ArchUnit, sin `bin/`, sin validadores, sin
manifiestos, sin derivación, sin tests-enunciado.**

**Duración: 3 horas por lab, tope.** Es un objetivo duro. Lo que no cabe va a «lo que no vimos
hoy» — nombrarlo honestamente en vez de meterlo a presión.

**Puertos:**

| Lab | HTTP practica / solucion | Postgres (si aplica) |
|---|---|---|
| 08 seguridad | 8095 / 8096 | 55440 / 55441 |
| 09 resiliencia | 8097 / 8098 | — |
| 10 observabilidad | 8101 / 8102 | 55442 / 55443 |
| 11 tareas | 8103 / 8104 | — |
| 12 empaquetado | 8105 / 8106 | — |

**Base de datos solo donde el tema la exige** (08 para usuarios, 10 para el health de la base).
En los demás, repositorio en memoria: menos piezas, menos ruido.

---

## 2 · Lab 08 · Seguridad

**Punto de partida:** la API de productos del arco anterior, con todos sus endpoints abiertos.

**Pasos:**
1. Agregar `spring-boot-starter-security` y arrancar: **todo se cerró solo**, y la consola
   imprime una contraseña generada. Explicar que el default es denegar.
2. Configurar un usuario en memoria y una cadena de filtros mínima: qué rutas son públicas y
   cuáles no.
3. Guardar usuarios en la base con la contraseña cifrada (**BCrypt**). Mostrar el hash en la
   tabla y que dos usuarios con la misma clave tienen hashes distintos.
4. Login que devuelve un **JWT**. Las tres partes del token; pegarlo en un decodificador y ver
   que **cualquiera lee su contenido** — la firma no oculta, garantiza.
5. Filtro que valida el token en cada petición. Probar con token, sin token y con token
   manipulado.
6. Roles: un endpoint solo para ADMIN. **401 vs 403** con las dos peticiones que los provocan.

**No vimos hoy:** OAuth2, refresh tokens, method security fina.

## 3 · Lab 09 · Resiliencia

**Punto de partida:** un servicio que llama a otro. El «otro» es **WireMock como librería
in-process** (ya validado en el PR #31 — reutilizar ese enfoque, no contenedores).

**Pasos:**
1. Llamar al servicio configurado para tardar 30 segundos: la petición del usuario **queda
   colgada**. Medirlo.
2. Poner timeout. Ahora falla rápido — y falla, que no es lo mismo que funcionar.
3. Reintentos: cuándo ayudan, y el aviso de por qué reintentar puede empeorar una caída.
4. Circuit breaker: tras N fallos deja de intentar. Ver el estado abierto y el cierre después
   del periodo. **Citar los estados en consola.**
5. Fallback: qué responder cuando no hay respuesta. La app sobrevive al servicio caído.

**No vimos hoy:** bulkheads, rate limiting, retry con backoff exponencial afinado.

## 4 · Lab 10 · Observabilidad

**Punto de partida:** una app que funciona y no cuenta nada.

**Pasos:**
1. Actuator: `health`, `info`, `metrics`. Qué expone y qué **no** se expone en producción.
2. **Logging estructurado y MDC** (lo que se rescata del lab de AOP): poner un id de correlación
   por petición y seguir una petición concreta entre muchas en el log.
3. Una métrica propia: un contador de negocio y verlo subir en `/actuator/metrics`.
4. Health indicator personalizado.
5. Liveness vs readiness: **tirar la base abajo** y ver que readiness cae nombrando la causa
   mientras liveness sigue arriba.

**No vimos hoy:** Prometheus/Grafana, tracing distribuido.

## 5 · Lab 11 · Tareas y asincronía

**Pasos:**
1. `@Scheduled` con intervalo fijo: la tarea corre sola, sin que nadie la pida.
2. `@Scheduled` con cron y por qué la expresión se lee de izquierda a derecha.
3. `@Async`: devolver la respuesta al usuario **antes** de terminar el trabajo. Medir la
   diferencia con y sin.
4. Hilos virtuales de Java 25: qué cambian y cuándo importan.
5. El problema real: **dos instancias corriendo la misma tarea programada.** Reproducirlo
   levantando dos y ver la tarea duplicada. Nombrar la solución (bloqueo distribuido) sin
   implementarla — dejarlo dicho.

**No vimos hoy:** colas de mensajes, planificadores distribuidos.

## 6 · Lab 12 · Empaquetado — el cierre del curso

**Pasos:**
1. `./mvnw package`: qué es un jar y qué lleva dentro. Ejecutarlo con `java -jar`.
2. Jar por capas: por qué separar dependencias de código propio.
3. **Qué es un contenedor**, desde cero — en este curso el alumno **nunca ha visto uno**, así que
   se explica de verdad (~20 min): qué problema resuelve, en qué se diferencia de una VM.
4. Construir una **imagen OCI con Jib**, sin demonio Docker. Inspeccionar el resultado.
5. Configuración por entorno: la misma imagen en dev y prod cambiando variables, y por qué la
   imagen no se recompila para cada ambiente.

**No vimos hoy:** Kubernetes, registries, CI/CD.

⚠️ Jib necesita una imagen base. Verificar que se pueda construir **sin red** o, si no es
posible, usar `jib:buildTar` con base preempaquetada. **Si no hay forma de que funcione offline,
detenerse y reportar** — no dar por bueno un lab que en el SII no correría.

---

## 7 · Verificación — por cada lab

| # | Prueba | Criterio |
|---|---|---|
| V1 | `practica/` en su estado de entrega | Arranca sin errores |
| V2 | `solucion/` completa | Funciona; citar consola y endpoints |
| V3 | Los números/estados del lab | 08: 401 y 403 citados · 09: colgado→timeout→circuito abierto · 10: readiness caído con causa · 11: tarea duplicada en dos instancias · 12: la imagen construida |
| V4 | Seguir `PASOS.md` completo sobre `practica/` | Se llega al resultado de `solucion/` |
| V5 | `instructor/` | Todos los archivos, misma estructura, documentados de principio a fin, sin mvnw/target |
| V6 | `git status` | `instructor/` no aparece |
| V7 | Offline | 0 descargas |
| V8 | `ls labs/<lab>` | README, PASOS, practica, solucion, instructor |

Transversales: CI sin rojos nuevos respecto de `main` (el rojo de `deriva` es el heredado —
comparar línea por línea); guard de 95 MB; `du -sh labs/*` reportado; ningún lab por encima de
1 MB en limpio.

**Trabajar lab por lab: terminar y verificar el 08 antes de empezar el 09.** Si uno se atasca,
seguir con el siguiente y reportarlo — no bloquear los cinco por uno.

---

## 8 · Entregable

`INFORME-SPEC-032`, formato de 8 secciones, con una tabla por lab (pasos, duración estimada,
puertos, lo que quedó en «no vimos hoy»). `ESTADO.md` al día. Mergear y etiquetar al terminar.

Si algún lab no alcanza a quedar verificado, **mergear los que sí** y reportar el pendiente. Es
preferible entregar tres labs sólidos que cinco a medias.

---

## 9 · Prohibiciones

- ❌ Tocar los labs viejos o los nuevos ya existentes (00 a 07).
- ❌ Docker como requisito en cualquier lab, incluido el 12.
- ❌ Documentación en `practica/` más allá de la línea imperativa.
- ❌ Que `instructor/` sea ejecutable o llegue al repositorio.
- ❌ Pasar de tres horas por lab.
- ❌ Colas de mensajes y AOP puro: descartados por el PO.
- ❌ sudo · LFS · credenciales · ≥95 MB · verde sin salida citada.

---

## 10 · Ajustes acordados durante la ejecución

Anotados aquí, dentro de la SPEC, según el protocolo (los ajustes de una SPEC abierta se anotan
en ella; una vez cerrada se corrige con `SPEC-FIX-NN`).

**10.1 · El Lab 10 sale del alcance.** La verificación previa que pide §0 encontró que
`labs/lab-10-observabilidad` **coincide exactamente** con el nombre del lab nuevo — la premisa de
§0 («no hay colisión de directorio») no se cumple para el 10; sí para los otros cuatro.

Decisión del PO, consultado con el hallazgo medido: **no se inventa un nombre por un choque
temporal.** El lab nuevo se llamará `lab-10-observabilidad` cuando la SPEC de reempaquetado
retire el arco viejo, y se construirá entonces. Esta SPEC entrega **cuatro labs: 08, 09, 11 y
12**, y el 10 queda registrado como pendiente en el informe.

**10.2 · La imagen base de Jib viaja en el repositorio.** El aviso de §6 se activó: Jib **no**
puede construir sin red a menos que su caché de imagen base esté poblada. Medido:

- Jib 3.4.6 no lee bytecode de Java 25 (`Unsupported class file major version 69`); hace falta
  **3.5.2**.
- Con la caché poblada y `--offline`, la imagen se construye. Con la caché vacía:
  `Cannot run Jib in offline mode; eclipse-temurin:25-jre not found in local Jib cache`.
- La caché pesa **122 MB**, con un blob mayor de 60,1 MB (bajo el guard de 95 MB).

Decisión del PO: **adelante, mismo criterio que el JDK embebido** — sin eso el lab no corre en el
SII. Va en `tools/jib-base/`, y el informe documenta qué es, por qué está y cómo se regenera.
