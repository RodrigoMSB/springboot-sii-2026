# Demostración · Service Discovery y Config Server

El **mismo sistema** del `lab-microservicios` —gateway, contribuyentes, trámites y auditoría, con
sus tres bases separadas— **más dos piezas de infraestructura** que el laboratorio no tiene y que
el alumno va a encontrarse en cualquier proyecto real:

| | |
|---|---|
| **El registro** (Eureka) | Los servicios se inscriben al arrancar y se buscan **por nombre** |
| **El Config Server** | La configuración de los seis vive **fuera de ellos** |

**Lo que cambia es dónde están las respuestas a dos preguntas:** «¿dónde está el otro servicio?» y
«¿cuál es mi configuración?». En el laboratorio las dos están escritas dentro de cada programa. Aquí
las dos las contesta alguien más.

> ⚠️ **Esto no es un laboratorio y el alumno no lo corre.** No lleva `practica/`, ni `PASOS.md`, ni
> bloques para pegar: no hay nada que teclear. Se proyecta, se mira, y se vuelve al laboratorio de
> verdad — que es el que dictan los alumnos y el que no se toca.

---

## Lo primero, porque manda sobre el resto

**Esta demostración es OPCIONAL y va al final.** Si el tiempo aprieta, o si algo no arranca, el
laboratorio de microservicios y la demostración con Docker cubren la sesión enteros. Nada de aquí es
requisito de nada.

Y una advertencia que conviene tener delante desde el principio: son **seis procesos**, y seis
procesos tardan **veinticinco segundos** en estar listos. El apartado «¿Es proyectable?» de más
abajo dice, sin adornos, qué aguanta esto en directo y qué no.

---

## Lo que necesita

| | |
|---|---|
| **Red, la primera vez** | Para bajar Eureka y el Config Server. **Y sólo la primera vez** — ver abajo |
| RAM | **2,4 GB** los nueve procesos (seis JVM + tres PostgreSQL embebidos), medido |
| Puertos | 8230–8233, **8761**, **8888**, y 55480–55482 para las bases |
| Docker | **No.** Esta demostración no lo usa |

### La red, y por qué aquí sí

El resto del curso compila **offline**, contra el `repo-maven/` que viaja en el repositorio
(`D-022-3`). **Eureka y el Config Server no están ahí, y no se metieron a propósito**: son megas de
artefactos de Spring Cloud que acabarían en el clon de los dieciocho alumnos para un material que
ningún alumno ejecuta. `demos-instructor/` es, por definición, lo que no viaja en la maleta.

Así que `construir.sh` usa `DGT_ONLINE=1` —el modo que el shim del curso trae para quien prepara el
material— y deja lo que baje en el `~/.m2` de esta máquina.

> **Compila una vez ANTES de la clase.** Es el mismo trato que la demostración con Docker («baja las
> dos imágenes antes de entrar»), y por la misma razón: pelearse con el wifi del SII delante de la
> sala es la forma más segura de perder diez minutos.
>
> Para comprobar el día antes que la clase no va a necesitar red:
> ```bash
> ./construir.sh --sin-red     # falla si falta algo en la caché
> ```

---

## Cómo se levanta

```bash
cd demos-instructor/microservicios-discovery
./construir.sh          # los seis jar. UNA VEZ, con red, antes de la clase
./levantar.sh           # los seis procesos, en orden, esperando a cada uno
```

Y para dejarlo todo limpio:

```bash
./apagar.sh             # los seis abajo
./apagar.sh --a-lo-bruto   # y además mata lo que haya quedado suelto en los puertos
```

También va uno a uno, que es lo que pide el bloque 1:

```bash
./levantar.sh contribuyentes
./apagar.sh   contribuyentes
```

### Por qué aquí hay un script y en la demostración con Docker no

Aquélla envuelve `docker compose up` en nada, y a propósito: **lo que tenía que enseñar era ese
comando**. Aquí el protagonista es otro —el registro y el Config Server— y el arranque es sólo el
peaje para llegar a él. Seis terminales abiertas en el orden correcto no enseñan nada que este
README no diga mejor en cuatro líneas, y sí son **seis oportunidades de equivocarse delante de la
sala**.

Los logs quedan en `.estado/<servicio>.log`, uno por servicio. Para seguir uno en vivo:

```bash
tail -f .estado/tramites.log
```

---

## Las direcciones

| | |
|---|---|
| **El panel del registro** | **http://localhost:8761** — es el que se proyecta |
| **El Config Server** | http://localhost:8888/tramites/default |
| **La puerta** | **http://localhost:8230** |
| contribuyentes, trámites, auditoría | 8231, 8232, 8233 |
| las tres bases | 55480, 55481, 55482 |

Los dos usuarios son los del laboratorio: `carolina` / `dgt2026` y `jefatura` / `dgt2026`.

```bash
TOKEN=$(curl -s -X POST http://localhost:8230/auth/login \
  -H 'Content-Type: application/json' \
  -d '{"usuario":"carolina","clave":"dgt2026"}' | sed 's/.*"token":"\([^"]*\)".*/\1/')

curl -s -H "Authorization: Bearer $TOKEN" http://localhost:8230/tramites/1
```

**El 8761 y el 8888 no son del bloque 823x a propósito**: son los puertos que Eureka y el Config
Server usan por convención, y son los que el alumno va a ver en todos los tutoriales que lea después.
Los otros cuatro sí siguen la numeración del curso — 820x el laboratorio, 821x su solución, 822x la
demostración con Docker, 823x ésta.

### Dos endpoints que el laboratorio no tiene

Existen para poder **enseñar** en vez de **deducir**, y están declarados como diferencia con el
laboratorio:

| | |
|---|---|
| `GET :8230/rutas` | La tabla de rutas del gateway **tal como está ahora**. Es el instrumento del bloque 3 |
| `GET :8232/a-quien-veo` | Lo que trámites **cree** que hay en el sistema, leído de su copia local del registro. Es el instrumento del bloque 4 |

---

## El orden de arranque

**Es el apartado que hay que leer antes de tocar nada**, porque aquí el orden sí tiene consecuencias
y en el laboratorio no las tenía.

```
1. config      →  2. registro  →  3. los cuatro servicios
   :8888           :8761           gateway, contribuyentes, trámites, auditoría
```

`levantar.sh` lo cumple solo. Lo que sigue es **por qué** ese orden, y qué pasa exactamente si se
rompe — medido, no supuesto.

### Si el Config Server no está: los cuatro servicios NO ARRANCAN

No es una degradación: es una muerte en el arranque. `spring.config.import` es una dependencia
dura, y sin el Config Server el servicio no sabe ni en qué puerto escuchar.

```
$ ./levantar.sh --sin-config

  (sin el Config Server, a propósito: los cuatro servicios van a MORIR)
  registro        :8761  listo en 3s
  gateway         :8230  MURIÓ

[ERROR] gateway no llegó a arrancar. Las últimas líneas de su log:
  org.springframework.cloud.config.client.ConfigClientFailFastException:
      Could not locate PropertySource and the resource is not optional, failing
```

**Y es el comportamiento correcto**, aunque asuste: arrancar sin saber a qué base conectarse sería
mucho peor que no arrancar. Pero convierte al Config Server en un **requisito de arranque** de los
cuatro, y eso es una pieza de infraestructura nueva que puede tumbar el sistema entero. Es parte del
precio, y se dice.

> Se puede ablandar con `optional:configserver:...`, y entonces el servicio arranca con lo que tenga
> a mano. Aquí **no** se ha puesto, para que la demostración enseñe el comportamiento de fábrica —
> que es el que el alumno se va a encontrar.

### Si el registro no está: arrancan igual, pero no se ven

Aquí la respuesta es la contraria, y el contraste es la mitad de la lección. Los cuatro servicios
**arrancan sin problema** sin Eureka: el cliente de Eureka reintenta en segundo plano y no bloquea
nada. Lo que no funciona es que se encuentren:

```
WARN GATEWAY - No servers available for service: tramites
WARN GATEWAY - [GATEWAY] tramites no contestó: IllegalStateException
```

**Config Server y registro son dependencias de distinta naturaleza**, y conviene señalarlo con el
dedo: uno hace falta para **existir**, el otro para **trabajar**.

### Y el detalle que parece un error y no lo es

El Config Server arranca **antes** que el registro, y sin embargo **se registra en él**. Se inscribe
unos segundos tarde, cuando el registro aparece, y no pasa nada: el cliente reintenta solo.

Es la primera cosa que esta demostración enseña sobre un registro, y va gratis: **nadie tiene que
estar arriba en el momento exacto.**

---

## El guion de la demostración · veinte minutos

### 0 · Antes de que entre nadie

```bash
./construir.sh          # si no lo hiciste ya. Necesita red la primera vez
./levantar.sh           # 25 s
```

**Déjalo arriba.** Y ten dos cosas abiertas y proyectadas antes de empezar:

1. **El panel del registro**, http://localhost:8761, en una pestaña del navegador
2. **`config-repo/gateway.yml`** en el editor

Comprueba que sirve, que son cinco segundos y evita una sorpresa:

```bash
TOKEN=$(curl -s -X POST http://localhost:8230/auth/login \
  -H 'Content-Type: application/json' \
  -d '{"usuario":"carolina","clave":"dgt2026"}' | sed 's/.*"token":"\([^"]*\)".*/\1/')
curl -s -H "Authorization: Bearer $TOKEN" http://localhost:8230/tramites/1
```

---

### 1 · El registro · «¿quién existe?» (4 min)

**Se proyecta el panel: http://localhost:8761**

Lo que se ve es una tabla con **cinco filas**, una por servicio:

```
Instances currently registered with Eureka

Application       AMIs    Availability Zones   Status
AUDITORIA         n/a (1)         (1)          UP (1) - auditoria:8233
CONFIG            n/a (1)         (1)          UP (1) - config:8888
CONTRIBUYENTES    n/a (1)         (1)          UP (1) - contribuyentes:8231
GATEWAY           n/a (1)         (1)          UP (1) - gateway:8230
TRAMITES          n/a (1)         (1)          UP (1) - tramites:8232
```

**Qué señalar, y el orden importa:**

1. **Nadie escribió esa tabla.** No hay un archivo con las cinco direcciones: cada servicio se
   presentó solo al arrancar. Es la diferencia entera con el laboratorio, y está a la vista.
2. **El Config Server sale en la lista.** La infraestructura no está exenta de las reglas que le
   pone al resto.
3. **Son cinco, y los procesos son seis.** El que falta es el registro: no se inscribe en sí mismo.
   Con varios nodos sí lo haría —así se replican entre ellos—, y ahí está la respuesta a la pregunta
   que alguien va a hacer en el bloque 4.

**Y ahora el momento que hay que enseñar en directo.** En otra terminal:

```bash
./apagar.sh contribuyentes      # se recarga el panel: quedan CUATRO
./levantar.sh contribuyentes    # se recarga otra vez
```

```
  contribuyentes  :8231  listo en 5s
```

**Aparece en el panel a los 6 segundos del lanzamiento** — medido, tres corridas. Se lanza, se
cuenta hasta seis, se recarga el navegador y está.

> **Y una que sorprende, porque contradice lo que todo el mundo espera de Eureka.** Al apagarlo con
> `./apagar.sh`, contribuyentes desaparece del panel **al instante**, no a los 90 segundos.
>
> Es que hay dos maneras de morirse. Con un apagado ordenado (`SIGTERM`, que es lo que hace `Ctrl+C`
> y lo que hace un despliegue normal), Spring ejecuta su gancho de cierre y el cliente de Eureka
> **se da de baja él mismo**: una llamada, y el registro queda al día en milisegundos.
>
> Los 90 segundos —`lease-expiration-duration-in-seconds`— son para la OTRA muerte: el proceso al que
> matan de golpe, el que se queda sin memoria, la máquina que se apaga. Ahí nadie avisa, y el registro
> sólo puede darse cuenta por **ausencia de latidos**.
>
> Y eso también se midió, con un `kill -9` a contribuyentes:
>
> ```
> t+ 0s  el registro TODAVÍA lo da por vivo (HTTP 200)
> t+30s  el registro TODAVÍA lo da por vivo (HTTP 200)
> t+61s  el registro TODAVÍA lo da por vivo (HTTP 200)
> t+86s  el registro lo tacha
> ```
>
> **Ochenta y seis segundos dando por buena la dirección de un proceso que ya no existe.** Ése es el
> número que hay que dejar dicho, porque explica el resto del curso: el circuit breaker del
> laboratorio no está «por si acaso», está porque **la lista es falible por diseño** y todo el que
> llame a otro servicio tiene que estar preparado para que la dirección que le dieron no conteste.

---

### 2 · Llamar por nombre ⭐ (5 min)

**Es el bloque que hace visible el patrón**, y cabe en una línea de configuración.

#### El antes y el después

Se abre `config-repo/tramites.yml` proyectado, al lado del `application.yml` del laboratorio:

```yaml
# labs/lab-microservicios/solucion/tramites/  ·  EL LABORATORIO
microservicios:
  contribuyentes:
    url: http://localhost:8211        # una máquina y un puerto, escritos a mano

# demos-instructor/microservicios-discovery/config-repo/tramites.yml  ·  AQUÍ
microservicios:
  contribuyentes:
    url: http://contribuyentes        # un nombre. No hay máquina y no hay puerto
```

Y en el código, `ClienteContribuyentes.java`, la otra mitad del cambio:

```java
// EL LABORATORIO
this.http = RestClient.builder().baseUrl(url).requestFactory(fabrica).build();

// AQUÍ
this.http = RestClient.builder()
        .baseUrl(url)
        .requestFactory(fabrica)
        .requestInterceptor(balanceador)   // <- la línea nueva
        .build();
```

**Qué señalar:** el cambio es **una línea de YAML y una línea de Java**. Todo lo demás —el circuit
breaker, el timeout, la correlación, la lógica— está intacto.

#### Que ese nombre no lo resuelve el sistema operativo

```bash
ping -c1 contribuyentes
```

```
ping: cannot resolve contribuyentes: Unknown host
```

**Ahí está la diferencia con la demostración de Docker.** Allí `contribuyentes` lo resolvía el DNS
de la red del compose, gratis, y por eso allí Eureka sobraba. Aquí no hay plataforma debajo: son
seis JVM en un portátil, y el nombre **sólo existe dentro del registro**.

#### Quién lo resuelve, entonces

```bash
curl -s localhost:8232/a-quien-veo
```

```json
{
  "momento": "2026-09-06T21:03:16.357123Z",
  "fuente": "la copia local del registro que guarda este proceso",
  "servicios": {
    "contribuyentes": ["192.168.100.218:8231"],
    "tramites":       ["192.168.100.218:8232"],
    "auditoria":      ["192.168.100.218:8233"],
    "gateway":        ["192.168.100.218:8230"],
    "config":         ["192.168.100.218:8888"]
  },
  "cuantos": 5
}
```

**Lee la palabra «copia».** Trámites no le pregunta a Eureka en cada llamada: se baja el registro
entero cada pocos segundos y lo guarda. Esa frase es la que hay que dejar sembrada, porque es la que
explica el bloque 4 entero.

#### Y el remate: mover un servicio de puerto

**Es lo que una URL fija no puede hacer, y conviene hacerlo en vivo.** Se edita
`config-repo/contribuyentes.yml`, `port: 8231` → `port: 8299`, y se reinicia **sólo** contribuyentes:

```bash
./apagar.sh contribuyentes
# (arrancarlo en el puerto nuevo)
```

**Nadie toca trámites. Nadie toca el gateway. Nadie recompila nada.** Y el trámite vuelve a traer el
nombre.

Lo medido, y hay que contarlo con honestidad porque no es instantáneo:

```
t+ 0.1s  estadoDelNombre=NO_DISPONIBLE
t+ 4.5s  estadoDelNombre=NO_DISPONIBLE
...
t+23.3s  estadoDelNombre=OK   ->  Carolina Fuentes Aravena
```

**Veintitrés segundos, y el registro no tuvo la culpa de casi ninguno.** La aritmética, que es la
parte que enseña:

| | |
|---|---|
| ~5 s | lo que tarda trámites en bajarse un registro donde contribuyentes ya está en el 8299 |
| ~18 s | **el circuit breaker**, que se había abierto con los primeros fallos y sólo reintenta cada 10 s |

En el log de trámites se ve, y merece proyectarse:

```
18:03:58.483 WARN [CIRCUITO] HALF_OPEN -> OPEN      <- reintentó demasiado pronto
18:04:09.389 WARN [CIRCUITO] OPEN -> HALF_OPEN
18:04:09.661 WARN [CIRCUITO] HALF_OPEN -> CLOSED    <- y se curó solo
```

> **El registro dijo la verdad en cinco segundos. El que tardó en creérsela fue el circuit
> breaker.** Son dos mecanismos independientes, y el sistema va a la velocidad del más lento. Es el
> mismo patrón que la demostración con Docker enseña en su bloque 5b: el orquestador levanta el
> proceso en tres décimas, y decidir cuándo volver a confiar en él **sigue siendo del programa**.

---

### 3 · La configuración, fuera del programa (4 min)

#### Primero, lo que ya no está dentro

Se abre `sistema/gateway/src/main/resources/application.yml`. **Entero, son cuatro líneas:**

```yaml
spring:
  application:
    name: gateway
  config:
    import: "configserver:http://localhost:8888"
```

**Qué señalar:** en el laboratorio este archivo tenía el puerto, las tres direcciones, el secreto
del JWT y el formato del log. Aquí quedan dos cosas, y las dos son irreducibles: **cómo se llama**
(es la pregunta que le hace al Config Server) y **dónde preguntar**. En algún punto la cadena tiene
que tocar suelo, y toca aquí.

Y lo que el Config Server le contesta se puede mirar a mano, que es media demostración:

```bash
curl -s localhost:8888/gateway/default
```

#### Y ahora el cambio en caliente

El instrumento es `GET /rutas`, que enseña la tabla del gateway tal como está:

```bash
curl -s localhost:8230/rutas
```

```json
{"rutas":[{"prefijo":"/contribuyentes","servicio":"contribuyentes","destino":"http://contribuyentes"},
          {"prefijo":"/tramites",      "servicio":"tramites",      "destino":"http://tramites"},
          {"prefijo":"/auditoria",     "servicio":"auditoria",     "destino":"http://auditoria"}]}
```

**Se edita `config-repo/gateway.yml` proyectado**, delante de la sala:

```yaml
  tramites:
    url: http://tramites-que-no-existe
```

Y **antes de refrescar**, dos comprobaciones que valen mucho porque separan las dos mitades:

```bash
curl -s localhost:8888/gateway/default | grep tramites   # el Config Server YA lo sirve
curl -s localhost:8230/rutas                             # el gateway NO se ha enterado
```

> **El Config Server no avisa a nadie.** No hay notificación, ni sondeo, ni magia. Guardar el
> archivo no cambia nada en ningún servicio: alguien tiene que pedir el cambio.

```bash
curl -s -X POST localhost:8230/actuator/refresh
```

```json
["microservicios.tramites.url"]
```

**Ahí está la demostración entera, en esa respuesta.** El endpoint devuelve, con nombre y apellido,
**qué propiedad ha cambiado**. Y el efecto es inmediato y se ve en pantalla:

```bash
curl -s localhost:8230/rutas       # destino: http://tramites-que-no-existe
curl -s -H "Authorization: Bearer $TOKEN" localhost:8230/tramites/1
```

```
HTTP 503   {"error":"el servicio tramites no contestó",...}
```

**Sin recompilar, sin reiniciar, sin desplegar.** Se deshace el cambio, se refresca otra vez, y
vuelve el 200 **al instante** (medido: 0,1 s).

#### Lo que NO se recarga, y hay que decirlo

Es la mitad honesta del bloque, y el alumno se la va a encontrar el primer día:

| | |
|---|---|
| **Sí se recarga** | Lo que lee un bean anotado con `@RefreshScope` — aquí, `TablaDeRutas` |
| **No se recarga** | `server.port`. Lo usa el servidor web al arrancar y ya no vuelve a mirarlo. **Cambiarlo exige reiniciar el servicio** |
| **No se recarga** | El puerto de la base embebida: está en el `main()`, antes de que exista el `Environment` |
| **No se recarga** | Las propiedades `eureka.*` — ver más abajo, y es a propósito |

> **`@RefreshScope` no es gratis ni automático.** Es una anotación que alguien tiene que poner, en
> el bean concreto que lee la propiedad. Un Config Server sin `@RefreshScope` sirve configuración
> centralizada, sí — pero para que llegue hay que reiniciar, y entonces la mitad de la gracia se ha
> ido.

> **Una arista de versiones que se encontró midiendo, y que está desactivada en la configuración.**
>
> De fábrica, `POST /actuator/refresh` reconstruye el cliente de Eureka. En esta combinación —Spring
> Cloud 2025.1.3 sobre Spring Boot 4.1.0— al reconstruirlo el servicio **se da de baja del registro y
> no consigue volver a inscribirse**, con el servidor de Eureka vivo y contestando:
>
> ```
> INFO  TRAMITES - Unregistering ...
> INFO  TRAMITES - deregister  status: 200          <- se fue
> WARN  TRAMITES - registration failed Cannot execute request on any known server
> ```
>
> Se queda fuera del registro hasta que alguien lo reinicie, y `GET /tramites/1` pasa a 503. O sea:
> **el bloque 3 rompía el bloque 4.**
>
> Está resuelto en `config-repo/application.yml` con `eureka.client.refresh.enable: false`, y allí
> está explicado con la traza entera. El precio es que las propiedades `eureka.*` ya no se recargan
> en caliente. Si algún día el tren de Spring Cloud declara compatibilidad con Boot 4.1, esto es lo
> primero que hay que volver a probar.

---

### 4 · Apagar el registro ⭐⭐ (5 min)

**Es el bloque que justifica la demostración entera**, y es el que hay que hacer despacio.

La pregunta que todo el mundo se hace al ver un registro por primera vez es la buena: *si todo el
mundo depende de esa cosa para encontrarse, ¿no acabamos de meter un punto único de fallo en medio
del sistema?*

**La respuesta es que no, y no se afirma: se mata el registro en directo y se mira.**

```bash
./apagar.sh registro
```

Y ahora, sin tocar nada más, se deja corriendo una sonda cada quince segundos:

```bash
while true; do
  printf '%s  ' "$(date +%T)"
  curl -s -o /dev/null -w 'HTTP %{http_code}  ' -H "Authorization: Bearer $TOKEN" \
       localhost:8230/tramites/1
  curl -s localhost:8232/a-quien-veo | python3 -c 'import sys,json;print("ve",json.load(sys.stdin)["cuantos"],"servicios")'
  sleep 15
done
```

**Lo medido, cinco minutos seguidos con Eureka muerto:**

```
t+  0s  HTTP 200  nombre=OK   tramites-ve=4   registro=(no responde)
t+ 30s  HTTP 200  nombre=OK   tramites-ve=4   registro=(no responde)
t+ 60s  HTTP 200  nombre=OK   tramites-ve=4   registro=(no responde)
...
t+303s  HTTP 200  nombre=OK   tramites-ve=4   registro=(no responde)
```

**Cinco minutos. Ni un solo error. El usuario no se entera de nada.**

**Por qué, y es la frase del bloque:** porque **el registro no está en el camino de ninguna
petición**. Cada servicio se bajó una copia de la lista y la tiene en su memoria; cuando trámites
llama a contribuyentes, va **directo**. Al registro sólo se le pregunta para actualizar la copia, y
si no contesta, cada uno se queda con la última que tenía y sigue trabajando.

Mientras tanto, en el log, cada cinco segundos:

```
INFO TRAMITES - DiscoveryClient_TRAMITES/tramites:8232 - was unable to refresh its cache!
     This periodic background refresh will be retried in 5 seconds.
     status = Cannot execute request on any known server
```

**Eso es lo único que pasa.** Ruido en el log, y nada más. Conviene enseñarlo: es la diferencia
entre *un componente que falla* y *un componente en el camino crítico*.

#### Y ahora, qué SÍ se rompe — que es la otra mitad

Un registro caído no se nota **mientras nada se mueva**. Se nota en cuanto algo cambia, y ahí se
para para siempre. Con Eureka todavía muerto, se mueve contribuyentes al 8299 y se reinicia:

```
== con el registro MUERTO, contribuyentes se reinicia en el 8299 ==
   contribuyentes vivo y sano en :8299
   (pero no ha podido inscribirse en ningún sitio)

     t+15s  estadoDelNombre=NO_DISPONIBLE   trámites sigue viendo 192.168.100.218:8231
     t+30s  estadoDelNombre=NO_DISPONIBLE   trámites sigue viendo 192.168.100.218:8231
     t+45s  estadoDelNombre=NO_DISPONIBLE   trámites sigue viendo 192.168.100.218:8231
     t+60s  estadoDelNombre=NO_DISPONIBLE   trámites sigue viendo 192.168.100.218:8231
```

**Contribuyentes está perfectamente vivo en el 8299 y trámites nunca lo va a encontrar**, porque el
único que podía contárselo está muerto. Y no se cura solo: se queda así indefinidamente.

> **La conclusión, y es más fina que «Eureka no es un punto único de fallo»:**
>
> Un registro caído no rompe lo que ya estaba funcionando — **congela** el sistema en la foto que
> cada uno tenía. Lo que se pierde no es el tráfico: es la **capacidad de cambiar**. Nada se puede
> desplegar, ni mover, ni escalar, ni sustituir, porque nadie se va a enterar.
>
> Dicho de otra manera: **el registro no está en el camino de las peticiones, está en el camino de
> los despliegues.** Y por eso en producción se corren varios nodos de Eureka replicándose entre
> ellos — que es, por cierto, la razón de que un Eureka Server sea cliente de sí mismo, esa casilla
> que en el bloque 1 estaba apagada.

#### Y volver a encenderlo, que sorprende más que apagarlo

```bash
./levantar.sh registro
```

**No es instantáneo, y el camino tiene un bache que conviene enseñar.** Medido:

```
t+ 4s  estadoDelNombre=NO_DISPONIBLE   trámites ve  192.168.100.218:8231   <- la dirección VIEJA
t+14s  estadoDelNombre=NO_DISPONIBLE   trámites ve  (no lo ve)             <- ¡se quedó sin nada!
t+24s  estadoDelNombre=NO_DISPONIBLE   trámites ve  192.168.100.218:8299   <- ya lo ve bien
t+45s  estadoDelNombre=OK                                                  <- y por fin sirve
```

**Cuarenta y cinco segundos**, y cada tramo tiene su culpable:

| | |
|---|---|
| 0 → 4 s | arranca el registro, **vacío**: no recuerda nada de antes de morir |
| 4 → 14 s | los clientes se bajan ese registro vacío y **borran su copia buena**. Durante unos segundos ven **menos** que mientras Eureka estaba muerto |
| 14 → 24 s | los servicios notan que el registro no los conoce y **se vuelven a inscribir** solos |
| 24 → 45 s | el **circuit breaker** otra vez: tarda en volver a confiar |

> **La línea de t+14s es la que hay que señalar con el dedo.** Un registro que vuelve **vacío** deja
> el sistema momentáneamente **peor** que teniéndolo apagado: apagado, cada uno conservaba su última
> copia buena; encendido y vacío, todos se creen la lista nueva —que no tiene a nadie— y tiran la
> que servía.
>
> Es contraintuitivo y es real, y es exactamente la razón de existir del **modo de
> autopreservación** de Eureka, que aquí está apagado a propósito (ver `sistema/registro/.../application.yml`).
> En producción se deja encendido, y esto es lo que evita.

---

### 5 · El cierre · qué se ganó y qué se pagó (2 min)

```bash
./apagar.sh
```

**Lo que hay que dejar dicho, y es lo contrario de una venta:**

> Las dos piezas resolvieron dos problemas **reales**: las direcciones dejaron de estar escritas a
> mano en cuatro sitios, y la configuración dejó de estar dentro del artefacto. Con **una** instancia
> de cada servicio en **una** máquina, ninguno de los dos problemas dolía —y por eso el laboratorio
> no las lleva—. Con instancias que se crean y se destruyen solas, los dos son el trabajo del día.
>
> Y lo que se pagó está a la vista, sin restar nada:
>
> - **Dos procesos más** que arrancar, vigilar, actualizar y explicar.
> - **Un requisito de arranque nuevo**: sin el Config Server, los cuatro servicios no arrancan.
> - **Veinticinco segundos** hasta poder pedir la primera cosa, contra los pocos que costaba un
>   servicio suelto.
> - **Configuración con relojes**: 5 s de refresco, 30 s de latido, 90 s de expiración. Números que
>   antes no existían y que ahora hay que conocer para entender por qué algo tarda.
> - **Y una arista de versiones** que hubo que desactivar a mano para que el refresco no echara al
>   servicio del registro.

Y la comparación honesta, que es la que cierra la sesión:

> En la demostración con Docker, `contribuyentes` se resolvía **por nombre** también — y allí no
> había ni registro ni Config Server: lo hacía el DNS de la red del compose, **gratis**.
>
> **Eureka no compite con eso: compite con no tener plataforma.** Si vas a desplegar sobre
> Kubernetes o sobre un orquestador cualquiera, el descubrimiento y buena parte de la configuración
> ya te vienen dados, y montar un Eureka encima es duplicar. Si despliegas seis JVM en dos máquinas
> que administras tú —que es donde está media banca y media administración pública—, esto es
> exactamente lo que necesitas.
>
> **La pregunta no es «¿registro sí o no?». Es «¿ya tengo una plataforma que me lo dé?».**

---

## Los números, todos medidos

En un Mac con Apple Silicon, 64 GB de RAM. Tres corridas salvo donde se dice.

| | |
|---|---|
| **Arranque de los seis, hasta servir de punta a punta** | **24 – 27 s** |
| Los seis procesos respondiendo a `/salud` | 24 – 26 s |
| Memoria, seis JVM (con `-Xmx` puesto) | **2 319 MB** de RSS |
| Memoria, tres PostgreSQL embebidos | 91 MB |
| **Memoria total** | **≈ 2,4 GB** |
| Aparecer en el panel tras lanzarlo | **6,1 s** |
| Desaparecer del panel con apagado ordenado | **inmediato** (< 1 s) |
| Desaparecer del panel con `kill -9` | **86 s** |
| Encontrar un servicio que cambió de puerto | 5 s el registro, **23 s** con el circuit breaker |
| Recargar configuración en caliente | **inmediato**; deshacerlo, 0,1 s |
| **Aguante con el registro muerto** | **5 min sin un solo error** (se paró la medición, no el sistema) |
| Recuperación al volver el registro | **45 s** |
| Compilar los seis (con la caché caliente) | 18 s |

Y uno que no es un número pero cuenta igual: **con los 30 s de fábrica de
`registry-fetch-interval-seconds`, el sistema tardaba 48 s en servir aunque los seis procesos
estuvieran arriba a los 25 s** — 24 segundos de HTTP 503 con todo sano. Está bajado a 5 s y
explicado en `config-repo/application.yml`.

---

## ¿Es proyectable en vivo?

**Sí, con dos condiciones y una advertencia.** Ésta es la respuesta que el PO necesita:

**Las dos condiciones:**

1. **`./construir.sh` corrido antes de la clase**, con red. Si esto se intenta con el wifi del SII
   delante de la sala, se pierde la sesión.
2. **`./levantar.sh` corrido antes de que entre nadie**, y dejado arriba. Son 25 segundos de pantalla
   parada: se pueden enseñar una vez, a propósito, pero no se pueden regalar.

**La advertencia, y va en serio:** los bloques 1, 2, 3 y 5 son **rápidos y fiables** — se hicieron
varias veces seguidas sin una sorpresa. El **bloque 4 es el mejor y el más lento**: matar el
registro y esperar a que no pase nada son cinco minutos si se quieren cinco minutos, y **volver a
encenderlo son 45 segundos de pantalla en rojo** antes de que se recupere.

> **Recomendación concreta:** hacer el bloque 4 hasta «cinco minutos y ni un error», dejar el
> registro muerto, contar de palabra lo que pasa al encenderlo —está aquí medido, con la tabla— y
> **no encenderlo en vivo**. Si sobra tiempo, se enciende y se enseña el bache de t+14s, que es
> precioso. Si no, no se ha perdido nada.

**Lo que NO es frágil**, y conviene decirlo porque era la duda de partida:

- El arranque **no falla según el momento**. `levantar.sh` espera a que cada pieza responda y luego a
  que el sistema sirva de verdad; en las corridas de esta SPEC no hubo ni un arranque fallido.
- El orden **no perdona**, pero tampoco hay que recordarlo: lo cumple el script, y si se rompe a
  propósito el error es explícito y está documentado.
- **No hay esperas indeterminadas.** Todos los tiempos de arriba son repetibles dentro de un par de
  segundos.

**Lo que sí muerde, y hay que saberlo:**

| | |
|---|---|
| Un `kill -9` a un servicio con base **deja vivo su PostgreSQL** y el puerto ocupado. Al reintentar, el programa lo dice con el comando exacto | `./apagar.sh --a-lo-bruto` lo limpia |
| El circuit breaker **añade 10–20 s** a toda recuperación, y no es culpa del registro | Está explicado en el bloque 2 y merece la pena contarlo |
| Si se refresca la configuración de un servicio **sin** `eureka.client.refresh.enable: false`, se cae del registro | Ya está puesto. No se quite |

---

## Qué se recuperó del lab antiguo, y qué no

El lab antiguo (`material-v0.8.0`, `labs/lab-14-la-dgt-se-parte-en-pedazos/`) tenía las dos piezas
que esta demostración recupera. **Se leyó entero antes de escribir nada.**

**Se reutilizó:**

| | |
|---|---|
| **La idea entera**: Eureka Server + Config Server con backend `native` | Es lo que la SPEC pedía recuperar, y la decisión del backend sigue siendo la correcta: la configuración se **ve** y se edita con un editor, proyectada, sin hacer un commit en mitad de la clase |
| **`config-repo/` como directorio versionado al lado del sistema** | Mismo sitio, mismo criterio |
| **El BOM del tren de Spring Cloud**, con una sola propiedad de versión | Y su aviso: Spring Cloud usa trenes con nombre de año, no el versionado de Boot |
| **La advertencia sobre el stack Netflix** | De Eureka, Hystrix, Ribbon y Zuul, **sólo Eureka sigue vivo**. Un tutorial que use los otros tres está caducado |
| **`@EnableEurekaServer` y `@EnableConfigServer` como piezas de cero código** | Catorce líneas de Java entre las dos |

**Se descartó:**

| | Por qué |
|---|---|
| **Los nombres `dgt-registro`, `dgt-config`, `dgt-portal`** | El sistema vivo se llama gateway / contribuyentes / trámites / auditoría. Un prefijo `dgt-` en un sistema donde todo es de la DGT no distingue nada |
| **`dgt-portal` como quinta pieza** | Era el gateway del lab antiguo. Aquí ya hay un gateway, escrito a mano y legible entero, y es el que los alumnos acaban de estudiar |
| **El escalado a dos instancias de contribuyentes** | Es la mejor demostración posible de un registro, y aun así se dejó fuera: son **siete** procesos y el laboratorio no balancea. Enseñarlo sería enseñar algo que el material no tiene. Queda anotado como lo primero que añadir si esto crece |
| **`bin/start-lab.sh` y sus siete banderas** | `levantar.sh` hace una cosa y se lee en una pantalla |
| **Una sola instancia de PostgreSQL con dos bases y dos usuarios** | Aquí son tres Zonky embebidos, uno por servicio, exactamente como en el laboratorio. La demostración es sobre discovery, no sobre bases |
| **Los tests del enunciado** (`BalanceoTest`, `FallbackYRetryTest`…) | Eran el ejercicio de un lab de tres horas. Aquí no hay ejercicio |
| **Docker y el `compose.yaml`** | Ver más abajo: es la decisión de fondo de esta demostración |

### Por qué SIN Docker, habiendo una demostración con Docker al lado

Era la decisión abierta de la SPEC, y se resolvió así por tres razones, la primera de las cuales
manda sobre las otras dos:

1. **Dentro de Compose, Eureka sobra — y la demostración de al lado ya lo dice.** Su bloque 4 enseña
   `getent hosts contribuyentes` y explica que eso es «el service discovery que el lab antiguo
   montaba con un Eureka, hecho aquí por la plataforma y gratis». Montar Eureka **dentro** de
   Compose sería demostrar un registro redundante, contradiciendo el material que el PO ya tiene.
   **El sitio honesto de un registro es donde no hay plataforma que lo dé**: seis JVM en un portátil,
   que es exactamente lo que el alumno acaba de ejecutar en el laboratorio.
2. **Es la continuación del LABORATORIO, no de la otra demostración.** El alumno viene de cuatro
   terminales con `localhost:8211` escritos a mano. Esto le enseña qué hacer con eso mismo, sin
   cambiarle el mundo por debajo a la vez.
3. **No necesita Docker**, y por tanto tiene una pieza menos que puede fallar. Lo que gana Compose
   —el orden de arranque— aquí lo da `levantar.sh`, que además espera a que el sistema **sirva** y no
   sólo a que responda.

**Lo que se pierde con esta decisión, dicho sin adornos:** el reinicio automático de un servicio
caído (`restart: unless-stopped`), y los límites de memoria por contenedor. Lo primero no se
necesita aquí; lo segundo se resolvió a mano con `-Xmx` en `levantar.sh`, y allí está explicado.

---

## Las diferencias con el laboratorio, una por una

`sistema/` sale del `solucion/` del `lab-microservicios`. **Copiar código es aceptar que se separe**,
así que la lista está aquí, entera y a la vista:

| | Qué cambia y por qué |
|---|---|
| **Dos servicios nuevos** | `registro/` y `config/`. No existen en el laboratorio |
| **Los cuatro `application.yml`** | Se quedan en cuatro líneas: nombre y dirección del Config Server. Todo lo demás se fue a `config-repo/` |
| **Los cuatro `pom.xml`** | +3 dependencias (eureka-client, config, actuator), +BOM del tren, y el `repackage` encendido para que `java -jar` funcione |
| **`ClienteContribuyentes` y `ClienteAuditoria`** | +1 parámetro y +1 línea: el interceptor del balanceador |
| **`Enrutador`** | Lo mismo: +1 parámetro, +1 línea |
| **`TablaDeRutas`** | +`@RefreshScope`, que es el bloque 3 |
| **`SeguridadConfig`** | `/rutas` y `/actuator/**` abiertos. **Decisión de demostración**, no recomendación |
| **`RutasController`** (nuevo) | Instrumento del bloque 3 |
| **`RegistroController`** (nuevo) | Instrumento del bloque 4 |
| **Los tres `*Application.java`** | Sólo el puerto de la base: 5546x → 5548x |
| **Todo lo demás** | **Idéntico.** El circuit breaker, la correlación, las entidades, los repositorios, los controladores, las migraciones |

**Si el laboratorio y esta demostración discrepan, manda el laboratorio.** Es el que dictan los
alumnos.

---

## Si algo falla

| Síntoma | Qué pasa |
|---|---|
| `MURIÓ` con `ConfigClientFailFastException` | El Config Server no está. Es lo esperado: mira el apartado del orden de arranque |
| `el puerto 8231 ya está ocupado` | Otra copia corriendo. `./apagar.sh --a-lo-bruto` |
| `El archivo .datos-pg/epg-lock esta tomado` | Quedó un PostgreSQL vivo de un `kill -9`. El propio mensaje trae el comando; o `./apagar.sh --a-lo-bruto` |
| `No servers available for service: tramites` | Un servicio todavía no está en la copia local del registro. Se cura solo en ≤ 5 s. Si no se cura, mira el panel: probablemente no llegó a inscribirse |
| El panel del 8761 muestra menos servicios de los que hay | Espera 30 s: la primera inscripción no siempre es instantánea. Si sigue faltando, `tail .estado/<servicio>.log` |
| `Could not resolve dependencies ... spring-cloud` | Es la primera compilación y no hay red. Ver «La red, y por qué aquí sí» |
| Un servicio arranca pero no aparece en el panel | ¿Se refrescó su configuración sin `eureka.client.refresh.enable: false`? Reinícialo y comprueba que la línea sigue en `config-repo/application.yml` |
