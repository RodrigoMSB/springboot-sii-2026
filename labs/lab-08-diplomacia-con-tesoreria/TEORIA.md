# Teoría · Módulo 10 (clientes HTTP y resiliencia) + la hora de M9 (endurecimiento)

## Índice

1. [El rehén: por qué TESO nos mata](#1-el-rehén-por-qué-teso-nos-mata)
2. [Timeouts: un presupuesto de espera](#2-timeouts-un-presupuesto-de-espera)
3. [Por qué el acto 2 (agrandar el pool) no resuelve](#3-por-qué-el-acto-2-agrandar-el-pool-no-resuelve)
4. [Degradación elegante: la mala noticia rápida](#4-degradación-elegante-la-mala-noticia-rápida)
5. [Los clientes HTTP: RestClient, WebClient, RestTemplate](#5-los-clientes-http-restclient-webclient-resttemplate)
6. [HTTP Interfaces y la escalera colapsada](#6-http-interfaces-y-la-escalera-colapsada)
7. [Feign, y cuándo](#7-feign-y-cuándo)
8. [SSRF: cuando la URL la elige el atacante](#8-ssrf-cuando-la-url-la-elige-el-atacante)
9. [La hora de M9: CORS, CSRF y cabeceras](#9-la-hora-de-m9-cors-csrf-y-cabeceras)
10. [Tabla DO / DON'T · Glosario](#10-tabla-do--dont--glosario)
11. [Conclusiones](#11-conclusiones)
12. [Siembra del Lab 09](#12-siembra-del-lab-09)

---

## 1. El rehén: por qué TESO nos mata

Cada petición HTTP que atiende tu API ocupa un **hilo** del pool de Tomcat. Si ese hilo, para
responder, llama a TESO y TESO tarda 30 segundos, el hilo queda **secuestrado** 30 segundos:
un rehén. No puede atender a nadie más mientras espera.

Con el pool lleno de rehenes —todos esperando a TESO— no queda ni un hilo libre. Entonces
llega un `curl` a `GET /tramites`, un endpoint que **no tiene nada que ver con pagos**, y no
hay quién lo atienda. La API entera se cae. No por un bug tuyo: por la lentitud de otro.

> **La lección estructural:** sin timeout, el hilo de la petición es un rehén; con el pool
> lleno de rehenes, la app está secuestrada por su dependencia.

---

## 2. Timeouts: un presupuesto de espera

Un timeout es una decisión: *"espero como máximo esto, y si no, me rindo"*. Dos, porque hay
dos esperas distintas:

- **connect timeout** — cuánto esperas a que se ABRA la conexión. En una red sana es
  instantáneo; si tarda, el otro está caído. Corto (cientos de ms).
- **read timeout** — cuánto esperas la RESPUESTA una vez conectado. Depende de cuánto tarda
  la operación en su mejor día. TESO confirma un pago en decenas de ms; si tarda casi un
  segundo, algo anda mal.

Cortos **a propósito**: es mejor un "no pude, reintenta" en un segundo que un "espera…" de
treinta. Y **dirigidos**: el timeout va en el cliente de TESO, no global — cada dependencia
tiene su propio presupuesto.

---

## 3. Por qué el acto 2 (agrandar el pool) no resuelve

La tentación: *"si el pool se llena de rehenes, hagamos el pool más grande"*. Y funciona…
un rato. Con pool N, aguantas N-1 pagos colgados. Basta el N-ésimo para volver a caer.

Solo compraste tiempo, pagándolo en memoria (cada hilo cuesta), y no arreglaste nada: el
hilo sigue siendo rehén, solo hay más rehenes posibles. Y un timeout global gigante hace que
**todos** los endpoints paguen el peaje de una espera pensada para uno. Posponer no es
resolver. El candado no es "más hilos": es "no esperar para siempre".

---

## 4. Degradación elegante: la mala noticia rápida

Cuando TESO no responde a tiempo, la respuesta correcta no es un 500 con un stacktrace. Es
un **503** honesto: *"Tesorería no está disponible; tu trámite no cambió; reintenta en unos
segundos"*. Un `ProblemDetail` limpio, con la sugerencia de reintento, sin tripas.

Y el trámite queda **íntegro**: como el timeout corta ANTES de tocarlo, no avanza de estado.
La API sigue viva, honesta, y rápida en su mala noticia.

> **La pregunta de criterio:** ¿por qué responder 503 rápido es MEJOR servicio que intentarlo
> 30 segundos? Porque a los 30 segundos el usuario ya se fue, el hilo ya secuestró a otros, y
> la respuesta es la misma: no se pudo. La rapidez de la mala noticia ES el servicio.

---

## 5. Los clientes HTTP: RestClient, WebClient, RestTemplate

- **`RestTemplate`** — el clásico, síncrono. En mantenimiento: sirve para código viejo, no
  para nuevo.
- **`WebClient`** — reactivo (no bloqueante). Potente si tu stack es reactivo; si no, arrastra
  complejidad que no necesitas.
- **`RestClient`** (Spring 6.1+) — síncrono, con la API fluida de WebClient. Es el default
  sano para una app tradicional como la nuestra. Es el que respalda nuestro cliente de TESO.

La elección no es "el más nuevo": es "el que encaja con tu modelo de concurrencia".

---

## 6. HTTP Interfaces y la escalera colapsada

Llamar a TESO a mano —`restClient.get().uri(...).retrieve()...`— funciona, pero repartes la
forma de la llamada por todo el código. Un **HTTP Interface** lo declara una vez:

```java
@HttpExchange
public interface TesoreriaClient {
    @GetExchange("/pagos/{referencia}")
    ConfirmacionPago confirmar(@PathVariable String referencia);
}
```

Declaras QUÉ llamas; Spring genera el CÓMO, sobre un `RestClient` que trae los timeouts. Es
la **escalera colapsada** (P-06): las formas acumuladas de llamar a TESO —el RestClient a
mano, el cliente declarativo— terminan detrás de UN puerto (`TesoreriaPort`) con su
implementación declarativa. La app depende del puerto, no del transporte.

Migrar a esto (TODO_3) es un **refactor**: no cambia el comportamiento, así que la misma
suite sigue verde. Eso no se promete, se demuestra (Lab 05).

---

## 7. Feign, y cuándo

**Spring Cloud OpenFeign** hizo populares los clientes declarativos años antes de que Spring
los trajera de fábrica. Hoy está en **mantenimiento**: recibe correcciones, no rumbo nuevo.

El criterio: en un sistema que YA usa Feign, seguir con Feign es razonable. En un desarrollo
NUEVO sobre Spring Boot moderno, los HTTP Interfaces nativos hacen lo mismo sin una
dependencia externa que algún día habrá que jubilar. (La comparación práctica, lado a lado,
es demo del relator — ver `INSTRUCTOR.md`.)

---

## 8. SSRF: cuando la URL la elige el atacante

**Server-Side Request Forgery**: si tu servidor hace una petición HTTP a una URL que viene de
datos del usuario, un atacante puede hacer que tu servidor llame a donde él quiera —la red
interna, el metadata de la nube—. Es el servidor quien "confía" en la URL.

En nuestro cliente de TESO no hay superficie: la URL viene de configuración fija
(`dgt.teso.base-url`), no del usuario. Pero si algún día un endpoint aceptara una URL del
cliente para llamarla, el cliente HTTP de Boot 4.1 trae mitigaciones (validación del destino)
que habría que activar. Se nombra, se señala en la config; no se teclea hoy.

---

## 9. La hora de M9: CORS, CSRF y cabeceras

El Lab 07 dejó esto anotado; aquí se cobra.

- **CORS** — el portero de los **navegadores**. Decide qué ORÍGENES (dominios de front)
  pueden llamar a tu API desde un navegador. Lo declaras nominal: solo `https://mi.dgt.cl`,
  **nunca `*`**. Poner `*` es rendirse —le dices al navegador "que llame cualquiera"—. Un
  preflight (la petición `OPTIONS` que el navegador manda antes) desde otro origen no recibe
  permiso. Ojo: CORS es del navegador; un `curl` o un servicio backend lo ignora (por eso no
  es "seguridad", es "política de navegadores").
- **CSRF** — un ataque donde un sitio malicioso usa TU sesión (tu cookie) para hacer
  peticiones en tu nombre. Aquí lo deshabilitamos **con razón**: esta API es sin estado, la
  credencial viaja en el header `Authorization` (no en una cookie que el navegador mande
  sola), así que no hay sesión que secuestrar. Deshabilitarlo en una app con cookies de
  sesión SÍ sería negligencia — el criterio es la clave, no la casilla.
- **Cabeceras de seguridad** — `Content-Security-Policy`, `X-Content-Type-Options`, etc.:
  instrucciones al navegador para no ejecutar lo que no debe. Baratas, y suman.

---

## 10. Tabla DO / DON'T · Glosario

| ✅ DO | ❌ DON'T |
|---|---|
| Timeout corto y dirigido por cliente | Sin timeout (espera infinita) o uno global gigante |
| Degradar con 503 honesto y rápido | 500 con stacktrace tras 30 s |
| Cliente declarativo (`@HttpExchange`) detrás de un puerto | Llamadas HTTP a mano regadas por el código |
| CORS nominal (`https://mi.dgt.cl`) | `CORS *` |
| Deshabilitar CSRF con criterio (API sin estado) | Deshabilitarlo "porque molesta" en una app con cookies |

- **Timeout** — presupuesto máximo de espera (connect y read).
- **Rehén** — un hilo bloqueado esperando a un servicio externo.
- **Degradación elegante** — fallar rápido y honesto en vez de colgar.
- **HTTP Interface** — cliente declarativo (`@HttpExchange`).
- **CORS** — qué orígenes de navegador pueden llamar a la API.
- **SSRF** — forzar al servidor a llamar una URL que elige el atacante.

---

## 11. Conclusiones

Hoy la DGT dejó de ser rehén de Tesorería. Un timeout corto y dirigido corta la espera; una
degradación elegante da la mala noticia rápido y deja el trámite intacto; un cliente
declarativo colapsa la escalera en un solo puerto; y el endurecimiento (CORS, cabeceras)
cierra el resto del edificio. TESO se cae, y nosotros no.

---

## 12. Siembra del Lab 09

🌱 **Siembra del Módulo 10 (que abre el M11) — "La caja negra".**

TESO ya no puede matarnos. Pero anoche pasó algo peor y más callado: **alguien emitió un
folio al contribuyente equivocado**. No hubo caída, no saltó ninguna alarma. Solo Carolina,
esta mañana, con **400 MB de logs** de una noche entera, y una sola pregunta: *encuéntralo.*

¿Puedes? ¿Tus logs dicen quién hizo qué, cuándo, y con qué trámite? ¿O son un río de líneas
sin correlación, donde una petición se pierde entre mil? La próxima semana traes lupa:
logging estructurado, trazas correlacionadas, y observabilidad — para que la caja negra
cuente la historia, no la esconda.

El Módulo 11 se llama *«La caja negra»*.
