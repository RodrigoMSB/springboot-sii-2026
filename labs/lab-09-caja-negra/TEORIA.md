# Teoría · Módulo 11 (observabilidad y archivos) + cierre de M10

## Índice

1. [El sistema mudo](#1-el-sistema-mudo)
2. [Niveles de log, y el criterio de cada uno](#2-niveles-de-log-y-el-criterio-de-cada-uno)
3. [JSON estructurado: por qué un agregador lo ama](#3-json-estructurado-por-qué-un-agregador-lo-ama)
4. [MDC y correlación: el número de pedido en la cocina](#4-mdc-y-correlación-el-número-de-pedido-en-la-cocina)
5. [AOP: el auditor invisible](#5-aop-el-auditor-invisible)
6. [El límite del proxy (otra vez)](#6-el-límite-del-proxy-otra-vez)
7. [El `catch` que traga: un antipatrón con nombre](#7-el-catch-que-traga-un-antipatrón-con-nombre)
8. [Archivos: subir con desconfianza](#8-archivos-subir-con-desconfianza)
9. [Cierre de M10: la traza cruza fronteras](#9-cierre-de-m10-la-traza-cruza-fronteras)
10. [Tabla DO / DON'T · Glosario](#10-tabla-do--dont--glosario)
11. [Conclusiones y siembra del Lab 10](#11-conclusiones-y-siembra-del-lab-10)

---

## 1. El sistema mudo

El log del practicante es texto plano, con `System.out.println` sueltos y un `catch` que se
traga la excepción. Con una petición a la vez, se lee. Con treinta simultáneas, es un muro:
las líneas de operaciones distintas se entrelazan, y `grep 4471` devuelve 200 líneas de 15
peticiones mezcladas. Imposible reconstruir qué pasó en cuál.

> **La lección:** un sistema que no se puede observar no se puede operar. El log no es para el
> programador que lo escribió: es para quien llega a las 3 AM sin saber qué pasó.

---

## 2. Niveles de log, y el criterio de cada uno

- **ERROR** — algo se rompió y alguien debe actuar. Si todo es ERROR, nada lo es.
- **WARN** — algo raro que no rompió (aún): un reintento, una degradación.
- **INFO** — hitos del negocio: "folio emitido", "pago confirmado". El pulso del sistema.
- **DEBUG** — detalle para investigar, apagado en producción.
- **TRACE** — el máximo detalle, casi nunca encendido.

El criterio no es "registrar todo": es registrar lo que alguien va a **necesitar leer**. Más
ruido no es más señal.

---

## 3. JSON estructurado: por qué un agregador lo ama

Una línea de texto plano es para un humano con los ojos. Una línea JSON —`{"timestamp":...,
"level":"INFO","traceId":"...","message":"..."}`— es para una **máquina**: un agregador (ELK,
Loki, Datadog) la indexa y la consulta. *"Dame todas las líneas con `traceId=X` y `level=ERROR`
de las últimas 2 horas"* es una consulta en JSON; en texto plano es un `grep` con los dedos
cruzados.

Boot 4 lo trae de fábrica: `logging.structured.format.console: ecs` y cada línea sale como un
objeto. No hay que armar el formato a mano.

---

## 4. MDC y correlación: el número de pedido en la cocina

En una cocina con diez pedidos a la vez, cada plato lleva el **número de pedido**. Sin él, no
sabrías qué papa frita es de qué mesa. El **MDC** (Mapped Diagnostic Context) es ese número:
un mapa por-hilo donde pones el `traceId` al entrar la petición, y TODA línea de log de esa
petición lo hereda. Filtrar por un `traceId` es juntar todos los platos de una mesa.

Un filtro lo siembra al entrar (`MDC.put("traceId", ...)`) y lo LIMPIA al salir
(`MDC.remove(...)` en un `finally`): el hilo vuelve al pool, y un `traceId` olvidado
contaminaría la siguiente petición. Si el cliente ya trae un `X-Trace-Id`, se respeta —así la
traza cruza servicios—; si no, se genera.

Eso es lo que convierte 200 líneas entrelazadas en una operación seguible. La pregunta de
criterio: *antes tenías 200 líneas y ninguna respuesta; ¿qué cambió, además del formato?* El
`traceId`. El formato lo hace consultable; el `traceId` lo hace **correlacionable**.

---

## 5. AOP: el auditor invisible

Querés registrar cada invocación al dominio —qué método, con qué argumentos, cuánto tardó—.
Podrías poner un `log.info(...)` al principio de cada método. Pero eso ensucia la lógica con
algo que no es lógica, y el día que cambie el formato tocás cien métodos.

**AOP** (Aspect-Oriented Programming) separa ese "qué registrar" (transversal) del "qué hace"
(el negocio). Vocabulario:

- **Aspecto** — el módulo que agrupa la preocupación transversal (la auditoría).
- **Join point** — un punto donde podría aplicarse (la invocación de un método).
- **Pointcut** — la expresión que elige CUÁLES join points (`execution(* ..application..*Service.*(..))`).
- **Advice** — el código que corre: `@Before`, `@After`, `@AfterReturning`, `@AfterThrowing`, y
  `@Around` (el más poderoso: envuelve la invocación, decide si sigue, mide el tiempo).

El código de negocio queda **idéntico**: byte a byte igual que el Lab 08. El auditor es invisible
para él.

**Datos sensibles enmascarados:** el aspecto ve los argumentos, así que debe cuidarlos. Un RUT se
registra parcial (`123***`); la clave jamás llega —el emisor de tokens no es un `*Service`, el
pointcut no lo toca—. Un log de auditoría que filtra una credencial es peor que no tener log.

---

## 6. El límite del proxy (otra vez)

El aspecto se aplica sobre el **proxy** del bean, igual que `@Transactional` (Lab 06). Si un
método público de un servicio llama a OTRO método público del MISMO bean con `this.otro(...)`,
esa llamada NO pasa por el proxy: el aspecto no la ve. Se audita lo que entra por la puerta, no
lo que se llama por dentro. Es la misma familia de trampa; reconocerla es saber cómo funciona
Spring por debajo.

---

## 7. El `catch` que traga: un antipatrón con nombre

```java
try { arriesgado(); } catch (Exception e) { /* nada */ }   // el crimen silencioso
```

Un `catch` vacío convierte un error en un silencio. El sistema sigue como si nada, el dato
queda mal, y no hay una sola línea que lo diga. El reemplazo: registrar CON CONTEXTO y
**re-lanzar** (o traducir a una excepción de dominio, como hicimos con TESO). El aspecto de
auditoría, en su `@Around`, registra la excepción y la re-lanza: nunca la traga.

---

## 8. Archivos: subir con desconfianza

Un archivo que sube un usuario es entrada hostil hasta que se demuestre lo contrario.

- **Tipo REAL, no declarado.** El `Content-Type` y la extensión los elige el cliente: mienten.
  Se juzga por los **magic bytes** —los primeros bytes del contenido—. Un `.exe` empieza con
  `MZ`, un PDF con `%PDF`. Un ejecutable renombrado a `.pdf` se caza mirando el contenido.
- **Path traversal.** Un nombre como `../../etc/passwd` intenta escapar del directorio. Se
  neutraliza quedándose con el ÚLTIMO segmento del nombre: sin barras, no hay escape.
- **Tamaño.** Un archivo de 2 GB puede tumbar el servidor. Se limita, y se rechaza el que se pasa.
- **Memoria.** Descargar un archivo cargándolo entero en memoria no escala. Se hace en
  **streaming**: fluye de disco a la red sin un buffer gigante.

---

## 9. Cierre de M10: la traza cruza fronteras

El `traceId` no tiene que morir en tu API. Cuando llamás a TESO (Lab 08), podés PROPAGARLO en
una cabecera (`X-Trace-Id`), y TESO —si también lo entiende— registra con el mismo id. Así una
operación se sigue a través de VARIOS servicios: la traza distribuida. Es el puente hacia la
observabilidad completa (Lab 14). Hoy se nombra y se muestra; no se teclea.

---

## 10. Tabla DO / DON'T · Glosario

| ✅ DO | ❌ DON'T |
|---|---|
| Un `traceId` por petición en el MDC | Logs sin correlación |
| JSON estructurado (consultable) | Texto plano (grep con los dedos cruzados) |
| Auditar con un aspecto, sin tocar el negocio | Un `log.info` en cada método |
| Enmascarar datos sensibles | Registrar el RUT o la clave completos |
| Juzgar el archivo por su contenido (magic bytes) | Confiar en la extensión / el `Content-Type` |
| Descargar en streaming | Cargar el archivo entero en memoria |
| Registrar y re-lanzar | El `catch` que traga |

- **MDC** — mapa por-hilo para correlacionar logs (el traceId).
- **traceId** — identificador único de una petición/operación.
- **AOP / aspecto / pointcut / advice** — separar lo transversal (auditoría) del negocio.
- **Magic bytes** — los primeros bytes que revelan el tipo real de un archivo.
- **Path traversal** — un nombre de archivo que intenta escapar del directorio.

---

## 11. Conclusiones y siembra del Lab 10

Hoy el sistema dejó de ser mudo: sabe contar lo que hizo, en JSON, correlacionado por `traceId`,
auditado sin ensuciar el negocio, y desconfía de los archivos que le suben. Ante un fiscalizador,
ahora hay una respuesta en cinco minutos, no en cinco horas.

🌱 **Siembra del Módulo 11 (que abre el M12) — "El reloj con problema de identidad".**

Ahora que el sistema sabe contar lo que hizo, Carolina notó algo raro en la bitácora: el
**cierre nocturno del viernes se ejecutó DOS veces**. La DGT corre en dos servidores (para
aguantar la carga), y a medianoche los dos dispararon la misma tarea programada: los dos se
creyeron el único. Dos cierres, dos veces los mismos correos, dos veces los mismos cálculos.

La próxima semana, el reloj tiene un problema de identidad: tareas programadas, y cómo hacer que
en un mundo de dos (o diez) servidores, una tarea que debe correr UNA vez, corra una sola vez.

El Módulo 12 se llama *«El reloj con problema de identidad»*.
