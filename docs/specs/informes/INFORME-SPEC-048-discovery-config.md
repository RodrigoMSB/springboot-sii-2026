# INFORME-SPEC-048 · Service Discovery y Config Server

**Ejecuta:** mocito · **Fecha:** 6 de septiembre de 2026 · **Origen:** SPEC-048 del PO (5 de septiembre).
**Rama:** `spec-048-discovery-config`

> ⚠️ **El número 048 está repetido, y no lo he arreglado yo.** Ya existe
> `INFORME-SPEC-048.md`, de la SPEC-048 del 3 de septiembre —comentarios del Lab 09 y Argon2id en
> el gateway—, mergeada en los PR #65 y #67. Esta SPEC, del 5 de septiembre, vuelve a llamarse 048.
> Este informe se llama `INFORME-SPEC-048-discovery-config.md` **para no pisar el anterior**. Si el
> PO quiere renumerar una de las dos, es un `git mv` y dos enlaces.

---

## 0 · La respuesta, primero

**Sí, es proyectable en vivo.** Con dos condiciones y una advertencia de guion.

| | |
|---|---|
| **Condición 1** | `./construir.sh` corrido **antes de la clase**, con red. Es la única vez que hace falta internet |
| **Condición 2** | `./levantar.sh` corrido **antes de que entre nadie**. Son 25 s de pantalla parada |
| **Advertencia** | El bloque 4 (apagar el registro) es el mejor y el más lento. **Recomiendo hacerlo hasta «cinco minutos y ni un error», dejar el registro muerto y NO volver a encenderlo en vivo** — encenderlo son 45 s de pantalla en rojo. Lo que se ve al encenderlo está medido y está en la tabla del README; se cuenta de palabra |

**No es frágil en el sentido que preocupaba a la SPEC.** No hay arranques que fallen según el
momento, ni esperas indeterminadas, ni un orden que no perdone: el orden lo cumple `levantar.sh`, y
en las corridas de esta SPEC no hubo ni un arranque fallido. Los tiempos son repetibles dentro de un
par de segundos.

**Y lo que sí muerde, dicho antes que nada:** hubo que desactivar a mano una arista de versiones
—`eureka.client.refresh.enable: false`— sin la cual **el bloque 3 rompía el bloque 4**. Está en el
§4 de este informe. No se quite esa línea.

---

## 1 · Qué se recuperó del lab antiguo, y qué se descartó

Se recuperó `labs/lab-14-la-dgt-se-parte-en-pedazos/` de `material-v0.8.0` (73 archivos) y **se leyó
entero** antes de diseñar nada: `README.md`, `TEORIA.md` (431 líneas), `INSTRUCTOR.md`, los cinco
`pom.xml`, el `config-repo/`, el `compose.yaml` y los `bin/*.sh`.

### Se reutilizó

| | |
|---|---|
| **La idea entera** | Eureka Server + Config Server. Es lo que la SPEC pedía recuperar |
| **El backend `native` del Config Server** | Archivos en `config-repo/`, versionados al lado del sistema. La razón del lab antiguo sigue siendo la buena: la configuración **se ve** y se edita con un editor, proyectada, sin hacer un commit en mitad de la clase — y no depende de que GitHub esté arriba |
| **El BOM del tren de Spring Cloud** | Una sola propiedad de versión, y ninguna dependencia de spring-cloud con `<version>` propia |
| **El aviso sobre los trenes** | Spring Cloud no usa el versionado de Boot: usa trenes con nombre de año, y hay que mirar la tabla de compatibilidad antes de escribir una dependencia |
| **El aviso sobre el stack Netflix** | De Eureka, Hystrix, Ribbon y Zuul, **sólo Eureka sigue vivo**. Va en el `pom.xml` de `registro/` |
| **`@EnableEurekaServer` / `@EnableConfigServer`** | Las dos piezas nuevas son 14 líneas de Java entre las dos |

### Se descartó

| | Por qué |
|---|---|
| **Los nombres `dgt-registro`, `dgt-config`, `dgt-portal`** | El sistema vivo se llama gateway / contribuyentes / trámites / auditoría. Un prefijo `dgt-` donde todo es de la DGT no distingue nada. Y los nombres cortos son los que se leen en `http://contribuyentes` |
| **`dgt-portal`** | Era el gateway del lab antiguo. Aquí ya hay uno, escrito a mano y legible entero, y es el que los alumnos acaban de estudiar |
| **El escalado a dos instancias** | **Es lo que más duele descartar**: es la mejor demostración posible de un registro. Serían **siete** procesos, y el laboratorio no balancea — enseñarlo sería enseñar algo que el material no tiene. Queda anotado en el README y en la guía como lo primero que añadir |
| **`bin/start-lab.sh` y sus siete banderas** | `levantar.sh` hace una cosa y se lee en una pantalla |
| **Una instancia de PostgreSQL con dos bases y dos usuarios** | Aquí son tres Zonky embebidos, uno por servicio, **exactamente como en el laboratorio**. La demostración es sobre discovery, no sobre bases |
| **Los tests del enunciado** (`BalanceoTest`, `FallbackYRetryTest`, `UmbralesDelCircuitoTest`) | Eran el ejercicio de un lab de tres horas. Aquí no hay ejercicio |
| **`compose.yaml` y `Dockerfile`** | Ver el §2 |

---

## 2 · De dónde partí, y por qué

**Partí del `solucion/` del `lab-microservicios`, sin Docker.** Era la decisión que la SPEC dejaba
abierta, y se resolvió por tres razones. La primera manda sobre las otras dos:

**1. Dentro de Compose, Eureka sobra — y la demostración de al lado ya lo dice.** Su bloque 4 enseña
`docker compose exec tramites getent hosts contribuyentes` y explica, con estas palabras, que eso es
«el *service discovery* que el lab antiguo montaba con un Eureka —un servicio más que arrancar,
configurar y operar—, hecho aquí por la plataforma y gratis». Montar Eureka **dentro** de Compose
sería demostrar un registro redundante y contradecir el material que el PO ya tiene.

**El sitio honesto de un registro es donde no hay plataforma que lo dé:** seis JVM en un portátil,
que es exactamente lo que el alumno acaba de ejecutar en el laboratorio.

Lejos de ser un problema, esa tensión resultó ser **la mejor frase de la sesión**, y está en las dos
demostraciones: *Eureka no compite con Docker; compite con no tener plataforma.* El
`demos-instructor/README.md` lo dice y recomienda dictar Docker primero.

**2. Es la continuación del LABORATORIO, no de la otra demostración.** El alumno viene de cuatro
terminales con `http://localhost:8211` escrito a mano. Esto le enseña qué hacer con eso mismo, sin
cambiarle el mundo por debajo a la vez.

**3. Una pieza menos que puede fallar.** Lo que Compose aporta —el orden de arranque— aquí lo da
`levantar.sh`, que además espera a que el sistema **sirva** y no sólo a que responda (§4.1).

**Lo que se pierde con esta decisión**, sin restar nada: el reinicio automático de un servicio caído
(`restart: unless-stopped`) y los límites de memoria por contenedor. Lo primero no se necesita aquí;
lo segundo se resolvió a mano con `-Xmx` en `levantar.sh`, y está explicado allí.

### El coste que la SPEC anticipaba, y cómo se pagó

> «sin Compose son seis terminales a mano»

**No son seis terminales.** `levantar.sh` arranca los seis en orden, espera a que cada uno responda,
y después espera a que una petición real cruce el sistema. En `demos-instructor/` esto es legítimo
—no hay `practica/` ni nada que teclear— y la razón de que aquí sí haya script y en la demostración
con Docker no está escrita en la cabecera del propio script: allí el protagonista **era**
`docker compose up`; aquí el protagonista es el registro, y el arranque es sólo el peaje.

---

## 3 · Lo que se construyó

```
demos-instructor/microservicios-discovery/
├── README.md                    el guion de demostración (840 líneas)
├── guia-demo-microservicios-discovery.pdf     la guía del instructor, 12 páginas
├── construir.sh                 los seis jar. --sin-red para comprobar la caché
├── levantar.sh                  los seis en orden; ./levantar.sh <servicio> para uno
├── apagar.sh                    y --a-lo-bruto para lo que quedó suelto
├── config-repo/                 la configuración de los cuatro, fuera de ellos
│   ├── application.yml          lo común: Eureka, actuator, logs
│   └── gateway|contribuyentes|tramites|auditoria.yml
└── sistema/
    ├── registro/                NUEVO · Eureka Server  (:8761)
    ├── config/                  NUEVO · Config Server  (:8888)
    └── gateway|contribuyentes|tramites|auditoria/    copia del lab
```

**77 archivos versionados, 399 KB.** Los `target/` (717 MB, con los binarios de Zonky dentro de los
jar) no se versionan.

### Los puertos

| | |
|---|---|
| gateway / contribuyentes / trámites / auditoría | **8230 – 8233** (la familia: 820x lab, 821x solución, 822x demo Docker, 823x ésta) |
| **registro** | **8761** — el puerto de Eureka **por convención**, el que el alumno verá en todos los tutoriales |
| **config** | **8888** — igual, por convención |
| las tres bases | 55480 – 55482 |

Se comprobó que ninguno choca con nada del repositorio.

### Las diferencias con el laboratorio, una por una

`sistema/` sale del `solucion/` del `lab-microservicios`. La lista completa está en el README de la
demostración; en resumen: los cuatro `application.yml` se quedan en cuatro líneas, los cuatro
`pom.xml` ganan tres dependencias y el BOM, tres clases ganan **una línea** cada una (el interceptor
del balanceador), `TablaDeRutas` gana `@RefreshScope`, y hay **dos controladores nuevos** que existen
para poder medir en vez de afirmar:

| | |
|---|---|
| `GET :8230/rutas` | La tabla de rutas del gateway **tal como está ahora**. Instrumento del flujo 4.3 |
| `GET :8232/a-quien-veo` | Lo que trámites **cree** que hay, leído de su copia local del registro. Instrumento del flujo 4.4 |

**Todo lo demás es idéntico:** el circuit breaker, la correlación, las entidades, los repositorios,
los controladores y las migraciones.

---

## 4 · Los tres problemas reales que aparecieron, y cómo se resolvieron

Los tres se encontraron **midiendo**, no leyendo. Los tres están documentados en el sitio donde
alguien los volverá a encontrar.

### 4.1 · El sistema decía «listo» 24 segundos antes de servir

**Síntoma medido:** los seis procesos respondían a `/salud` a los 25 s, y la primera petición de
punta a punta no devolvía 200 hasta los **48 s**. Veinticuatro segundos de HTTP 503 con todo el
sistema sano:

```
WARN GATEWAY - No servers available for service: tramites
WARN GATEWAY - [GATEWAY] tramites no contestó: IllegalStateException
```

**Causa:** `registry-fetch-interval-seconds` vale **30 s** de fábrica. El gateway arrancó antes que
trámites, se bajó un registro donde trámites todavía no estaba, y hasta la siguiente bajada —hasta
30 s después— para él trámites no existía.

**Dos arreglos, y los dos hacían falta:**

1. `registry-fetch-interval-seconds: 5` en `config-repo/application.yml`, con la medición entera
   escrita al lado. **No debilita el flujo 4.4**: cuando el registro no contesta, el cliente se queda
   con la última copia buena, y da igual cada cuánto lo intente.
2. **`levantar.sh` no dice «listo» hasta que una petición real cruza los cuatro servicios.** Es la
   diferencia entre `liveness` y `readiness` del Lab 11, con consecuencias en segundos de pantalla
   roja.

**Resultado: 24 – 27 s, y el instructor no ve un 503 que no haya pedido.**

Y el número de fábrica no se esconde: **se enseña**, porque es la explicación de por qué un sistema
con registro tarda en estabilizarse tras un despliegue.

### 4.2 · `/actuator/refresh` echaba al servicio del registro ⚠️

**Es el hallazgo grave, y estuvo a punto de costar el bloque 4 entero.**

**Síntoma, reproducido de forma determinista:** un `POST /actuator/refresh` sobre trámites —el
comando del flujo 4.3— hacía que trámites **se diera de baja del registro y no consiguiera volver a
inscribirse**, con el servidor de Eureka **vivo y contestando**:

```
18:15:11.720 INFO  TRAMITES - Unregistering ...
18:15:11.731 INFO  TRAMITES - deregister  status: 200      <- se fue, y el servidor contestó 200
18:15:11.756 WARN  TRAMITES - registration failed
                    Cannot execute request on any known server
18:14:24.527 WARN  TRAMITES - registration failed          <- y a los 30 s otra vez, y otra
```

Se quedaba fuera del registro **hasta que alguien lo reiniciara**, y `GET /tramites/1` pasaba a 503.
Dicho claro: **el flujo 4.3 rompía el flujo 4.4.**

**Diagnóstico.** De fábrica el bean `eurekaClient` está en `@RefreshScope`, así que el refresco lo
reconstruye; en esta combinación —Spring Cloud 2025.1.3 (compilado contra Boot **4.0.8**) sobre el
Boot **4.1.0** del curso— la reconstrucción deja el cliente sin servidores conocidos.

**Se descartó una hipótesis por medición**, y queda escrito para que nadie la vuelva a probar: poner
`eureka.client.service-url` en el `application.yml` **local** en vez de traerlo del Config Server
**no cambia nada**. El problema está en la reconstrucción del cliente, no en de dónde salen sus
propiedades.

**Arreglo:** `eureka.client.refresh.enable: false` en `config-repo/application.yml`. Spring Cloud
pasa a usar `EurekaClientAutoConfiguration$EurekaClientConfiguration` —el cliente no refrescable— en
vez de `$RefreshableEurekaClientConfiguration`. Medido después: **cero bajas**, el refresco sigue
devolviendo 200 con las claves que cambiaron, y los cinco siguen en el registro.

**Lo que se pierde:** las propiedades `eureka.*` dejan de recargarse en caliente. Cambiar la
dirección del registro ahora pide reiniciar. Es un precio pequeño y está dicho en los tres sitios
(configuración, README y guía).

> **Para el PO:** si algún día el tren de Spring Cloud declara compatibilidad con Boot 4.1, **esto es
> lo primero que hay que volver a probar.** Hoy el tren 2025.1.3 declara Boot 4.0.8.

### 4.3 · `POST /actuator/refresh` devolvía HTTP 500 aunque funcionara

Encontrado en el *spike* inicial, antes de escribir una línea del material. El refresco **se
aplicaba** —la propiedad cambiaba— pero el endpoint contestaba 500:

```
ERROR DiscoveryClient : Fetch registry error at startup:
      Cannot invoke "Object.hashCode()" because "key" is null
java.lang.NullPointerException
  at com.netflix.discovery.shared.Application.getByInstanceId(Application.java:181)
  at com.netflix.discovery.DiscoveryClient.updateInstanceRemoteStatus(DiscoveryClient.java:1006)
```

Una demostración en la que el comando correcto devuelve 500 no es una demostración.

**Arreglo:** fijar `eureka.instance.instance-id` a mano. El id deja de venir nulo, el endpoint
devuelve **200 con la lista de propiedades que cambiaron** — que además es la mejor salida posible
para proyectar — y de paso el panel pasa de mostrar `192.168.100.218:tramites:8232` a mostrar
`tramites:8232`.

---

## 5 · Los cuatro flujos del §4 de la SPEC

Todos ejecutados y con la salida citada. El README de la demostración los trae con su guion; aquí va
la evidencia.

### 5.1 · Registro — arrancar un servicio y verlo aparecer

**El panel: http://localhost:8761.** Se ven **cinco** filas y los procesos son **seis**: el que falta
es el registro, que no se inscribe en sí mismo.

```
Instances currently registered with Eureka
AUDITORIA        UP (1) - auditoria:8233
CONFIG           UP (1) - config:8888
CONTRIBUYENTES   UP (1) - contribuyentes:8231
GATEWAY          UP (1) - gateway:8230
TRAMITES         UP (1) - tramites:8232
```

**Cuánto tarda en aparecer — medido:**

```
0· de partida: /eureka/apps/CONTRIBUYENTES -> HTTP 404
1· lo arranco:   contribuyentes  :8231  listo en 5s
2· APARECIÓ a los 6.1s del lanzamiento
   panel: contribuyentes:8231  UP  puerto 8231
```

**Y las dos muertes, las dos medidas** — esto no estaba en la SPEC y resultó ser de lo mejor del
bloque:

| | |
|---|---|
| Apagado ordenado (`SIGTERM`) | Desaparece **al instante**: el cliente **se da de baja él mismo** |
| `kill -9` | **86 segundos** hasta que el registro lo tacha |

```
t+ 0s   el registro TODAVÍA lo da por vivo (HTTP 200)
t+30s   el registro TODAVÍA lo da por vivo (HTTP 200)
t+61s   el registro TODAVÍA lo da por vivo (HTTP 200)
t+86s   el registro lo tacha
```

**Ochenta y seis segundos dando por buena la dirección de un proceso que no existe.** Es el número
que explica hacia atrás el circuit breaker del Lab 10 y del lab de microservicios: la lista **es
falible por diseño**.

### 5.2 · Descubrimiento — llamar por nombre

**El antes y el después de la línea**, que es lo que la SPEC pedía explícito:

```yaml
# EL LABORATORIO — labs/lab-microservicios/solucion/tramites/.../application.yml
microservicios:
  contribuyentes:
    url: http://localhost:8211

# AQUÍ — demos-instructor/microservicios-discovery/config-repo/tramites.yml
microservicios:
  contribuyentes:
    url: http://contribuyentes
```

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

**Una línea de YAML y una línea de Java.** Todo lo demás está intacto.

**Que el nombre no lo resuelve el sistema operativo:**

```
$ ping -c1 contribuyentes
ping: cannot resolve contribuyentes: Unknown host
```

**Quién lo resuelve:**

```json
$ curl -s localhost:8232/a-quien-veo
{"fuente":"la copia local del registro que guarda este proceso",
 "servicios":{"contribuyentes":["192.168.100.218:8231"], ...}, "cuantos":5}
```

**Y el remate: mover contribuyentes al 8299 en el Config Server y reiniciar sólo a él.** Nadie toca
trámites, nadie toca el gateway, nadie recompila:

```
t+ 0.1s  estadoDelNombre=NO_DISPONIBLE
t+ 4.5s  estadoDelNombre=NO_DISPONIBLE
t+23.3s  estadoDelNombre=OK  ->  Carolina Fuentes Aravena
```

**23 segundos, y el registro no tuvo la culpa de casi ninguno** — ~5 s el registro, ~18 s el circuit
breaker, y se ve en el log:

```
18:03:58.483 WARN [CIRCUITO] HALF_OPEN -> OPEN      <- reintentó demasiado pronto
18:04:09.389 WARN [CIRCUITO] OPEN -> HALF_OPEN
18:04:09.661 WARN [CIRCUITO] HALF_OPEN -> CLOSED    <- y se curó solo
```

> **El registro dijo la verdad en cinco segundos; el que tardó fue el circuit breaker.** Es el mismo
> patrón que la demostración con Docker enseña en su bloque 5b, y cerrarlo entre las dos vale más que
> cualquiera de las dos por separado.

### 5.3 · Configuración centralizada, en caliente

**La propiedad elegida es la tabla de rutas del gateway** — se eligió porque su efecto **se ve en
pantalla** (cambia el código HTTP de una petición) y porque es la propiedad de la que va la
demostración entera. El instrumento es `GET /rutas`, que imprime la tabla vigente.

```
1· la tabla ahora:            destino http://tramites
2· cambio config-repo/gateway.yml -> http://tramites-que-no-existe
3· el Config Server YA lo sirve:   microservicios.tramites.url = http://tramites-que-no-existe
4· pero el gateway NO se ha enterado: destino http://tramites
```

> **El Config Server no avisa a nadie.** Guardar el archivo no cambia nada: alguien tiene que pedir
> el cambio. Esa mitad importa tanto como la otra.

```
5· POST /actuator/refresh  ->  ["microservicios.tramites.url"]   HTTP 200
6· la tabla cambió:            destino http://tramites-que-no-existe
7· efecto en pantalla:         GET /tramites/1  ->  HTTP 503
8· deshecho y refrescado:      HTTP 200 en 0,1 s
```

**Sin recompilar, sin reiniciar, sin desplegar.**

**Qué hace falta para que el cambio llegue, que la SPEC pedía documentar explícitamente:**

| | |
|---|---|
| **Basta `POST /actuator/refresh`** | Lo que lee un bean con `@RefreshScope`. Aquí, `TablaDeRutas` |
| **Hace falta REINICIAR** | `server.port`: lo usa el servidor web al arrancar y no vuelve a mirarlo |
| **Hace falta REINICIAR** | El puerto de la base embebida: está en el `main()`, antes de que exista el `Environment`. Está anotado en las tres `*Application.java` |
| **Hace falta REINICIAR** | Las propiedades `eureka.*`, por el §4.2 |

> **`@RefreshScope` no es gratis ni automático**: es una anotación que alguien tiene que poner en el
> bean concreto. Un Config Server sin ella sirve configuración centralizada, sí, pero para que
> llegue hay que reiniciar — y entonces media gracia se ha ido.

### 5.4 · Apagar Eureka — el que más enseña ⭐

**Medido, no supuesto.** Cinco minutos seguidos con el registro muerto:

```
t+  0s  HTTP 200  nombre=OK   tramites-ve=4   registro=(no responde)
t+ 60s  HTTP 200  nombre=OK   tramites-ve=4   registro=(no responde)
t+180s  HTTP 200  nombre=OK   tramites-ve=4   registro=(no responde)
t+303s  HTTP 200  nombre=OK   tramites-ve=4   registro=(no responde)
```

**Cinco minutos, ni un solo error, y se paró la medición — no el sistema.** Lo único que pasa es
ruido en el log cada cinco segundos:

```
INFO TRAMITES - was unable to refresh its cache! ... retried in 5 seconds
     status = Cannot execute request on any known server
```

**Cuánto aguantan la caché, qué pasa al reintentar y qué se rompe primero** — las tres preguntas de
la SPEC, contestadas con medición:

**Cuánto aguantan:** indefinidamente. La copia local no caduca por tiempo; sólo se sustituye cuando
llega una nueva. Mientras el registro no conteste, cada servicio trabaja con la última copia buena.

**Qué pasa al reintentar:** un `WARN` cada 5 s y nada más. El fallo del refresco no toca la copia.

**Qué se rompe primero — y ésta es la mitad que faltaba.** Un registro caído no se nota **mientras
nada se mueva**. Con Eureka todavía muerto, se movió contribuyentes al 8299 y se reinició:

```
   contribuyentes vivo y sano en :8299 (comprobado con lsof en cada muestra)
   (pero no ha podido inscribirse en ningún sitio)

     t+15s  estadoDelNombre=NO_DISPONIBLE   trámites sigue viendo ...:8231
     t+30s  estadoDelNombre=NO_DISPONIBLE   trámites sigue viendo ...:8231
     t+45s  estadoDelNombre=NO_DISPONIBLE   trámites sigue viendo ...:8231
     t+60s  estadoDelNombre=NO_DISPONIBLE   trámites sigue viendo ...:8231
```

**Contribuyentes está perfectamente vivo y trámites no lo va a encontrar nunca**, porque el único que
podía contárselo está muerto. Y no se cura solo.

> **La conclusión, y es más fina que «Eureka no es un punto único de fallo»:**
>
> Un registro caído no rompe lo que ya funcionaba — **congela** el sistema en la foto que cada uno
> tenía. Lo que se pierde no es el tráfico: es la **capacidad de cambiar**. Nada se puede desplegar,
> ni mover, ni escalar, ni sustituir, porque nadie se va a enterar.
>
> **El registro no está en el camino de las peticiones. Está en el camino de los despliegues.**

**Y volver a encenderlo, que sorprende más que apagarlo:**

```
t+ 4s  NO_DISPONIBLE   trámites ve ...:8231     <- la dirección VIEJA
t+14s  NO_DISPONIBLE   trámites ve (nada)       <- ¡se quedó sin nada!
t+24s  NO_DISPONIBLE   trámites ve ...:8299     <- ya lo ve bien
t+45s  OK                                       <- y por fin sirve
```

**La línea de t+14s es el hallazgo.** Un registro que vuelve **vacío** deja el sistema
momentáneamente **peor** que teniéndolo apagado: apagado, cada uno conservaba su última copia buena;
encendido y vacío, todos se creen la lista nueva —que no tiene a nadie— y tiran la que servía. Es
exactamente la razón de existir del **modo de autopreservación** de Eureka, que aquí está apagado a
propósito para que el panel sea legible en clase, y así está dicho en su `application.yml`.

> **Nota de honestidad sobre esta medición.** La primera corrida de este flujo dio «no se recupera
> nunca», y era **falso**: el contribuyentes que había arrancado a mano se había muerto sin que yo lo
> mirara. La medición se rehízo comprobando con `lsof` que el proceso seguía vivo **en cada
> muestra**, y entonces salieron los 45 s. El dato malo no llegó a ningún documento.

---

## 6 · Verificación

| # | Prueba | Resultado |
|---|---|---|
| **V1** | Sistema completo levantado, flujo de punta a punta por el gateway | **OK** — §7. Y un `POST` cruzando los cuatro con un solo `traceId` |
| **V2** | Los cuatro flujos del §4, con comandos y salida citada | **OK** — §5.1 a §5.4 |
| **V3** | El flujo 4.4, medido | **OK** — 5 min sin un error; se rompe la capacidad de cambiar; recuperación 45 s |
| **V4** | Tiempo de arranque total y memoria | **OK** — §7 |
| **V5** | `git diff` sobre `labs/` | **VACÍO** — §8 |
| **V6** | CI sin rojos nuevos | **OK** — §9 |

---

## 7 · V1 y V4 · el sistema, y sus números

**V1 · de punta a punta por la puerta:**

```
$ curl -s -H "Authorization: Bearer $TOKEN" http://localhost:8230/tramites/1
{"id":1,"tipo":"DECLARACION_F29","estado":"EN_PROCESO","rutContribuyente":"11111111-1",
 "nombreContribuyente":"Carolina Fuentes Aravena","estadoDelNombre":"OK",
 "creadoEn":"2026-09-06T20:55:18.928476Z"}
HTTP 200
```

Ese `nombreContribuyente` **no está en la base de trámites**: vino por HTTP de otro proceso, cuya
dirección salió del registro.

**Y un `POST`, que cruza los cuatro:**

```
18:44:29.135 INFO [SPEC048] GATEWAY        - [GATEWAY] POST /tramites -> tramites
18:44:29.170 INFO [SPEC048] TRAMITES       - [TRAMITES] trámite 3 creado para 11111111-1
18:44:29.172 INFO [SPEC048] TRAMITES       - [TRAMITES] pido la ficha de 11111111-1 a contribuyentes
18:44:29.174 INFO [SPEC048] CONTRIBUYENTES - [CONTRIBUYENTES] me piden la ficha de 11111111-1
18:44:29.201 INFO [SPEC048] AUDITORIA      - [AUDITORIA] llega el evento TRAMITE_CREADO del trámite 3
18:44:30.783 INFO [SPEC048] AUDITORIA      - [AUDITORIA] REGISTRADO id=1 del trámite 3
18:44:30.787 INFO [SPEC048] TRAMITES       - [TRAMITES] auditoría acusó recibo del trámite 3
```

**Cuatro servicios, siete líneas, una sola historia** — y los tres saltos resueltos por nombre.

**V4 · los números.** Mac con Apple Silicon, 64 GB de RAM. Tres corridas salvo donde se indique.

| | |
|---|---|
| **Arranque hasta SERVIR de punta a punta** | **24 – 27 s** |
| Los seis procesos respondiendo a `/salud` | 24 – 26 s |
| Memoria, seis JVM (con `-Xmx`) | **2 270 – 2 319 MB** de RSS |
| Memoria, tres PostgreSQL embebidos | **91 MB** |
| **Memoria total** | **≈ 2,4 GB** |
| Compilación de los seis, caché caliente | **18 s** |
| Disco, `target/` (no se versiona) | 717 MB |
| Disco, lo versionado | **399 KB**, 77 archivos |

**Sobre la memoria:** sin `-Xmx`, cada JVM dimensiona su heap contra la RAM de la máquina y los seis
pedían **3 144 MB** en este Mac de 64 GB — o sea, «cuánta memoria necesita esto» dependía de quién
preguntara. Con el techo puesto en `levantar.sh` la cifra es la misma en cualquier máquina. Es la
misma decisión que el `mem_limit` de la demostración con Docker, sólo que allí la toma el
orquestador y aquí hay que tomarla a mano — **una pieza más de trabajo de operación que, sin
plataforma, hace una persona.**

---

## 8 · V5 · `labs/` no se tocó

```
$ git diff main..spec-048-discovery-config -- labs/
(vacío)

$ git status --porcelain labs/
(sin archivos nuevos ni borrados)
```

**Esta rama no cambia ni un byte bajo `labs/`.** Tampoco toca
`demos-instructor/microservicios-docker/`, y su verificador de deriva sigue en verde (§9).

> **Un aviso, para que no sorprenda.** En el árbol de trabajo hay una modificación **preexistente**
> en `labs/lab-08-testing/solucion/.../ProductoService.java` — dos comentarios reformateados a 100
> columnas. **Ya estaba ahí antes de crear esta rama** (sale en el `git status` de partida), **no es
> mía y no la he tocado ni commiteado.** Si el PO la quiere, es un commit aparte; si no, un
> `git checkout` de ese archivo.

---

## 9 · V6 · CI

**Ningún job nuevo, ninguna excepción nueva, y la exclusión sigue siendo estructural.**

| job | efecto |
|---|---|
| `temario` | **verde** — no se toca `docs/temario/` |
| `siembra` | **verde** — no se añade ni se quita ningún `labs/lab-*` |
| `pasos` | **verde** — `verificar-pasos-copiables.py` pasa |
| `guion-practica` | **verde** — `verificar-guion-vs-practica.py` pasa |
| `labs` | **verde** — su bucle recorre `find labs proyecto-final -name pom.xml`; los seis `pom.xml` nuevos viven en `demos-instructor/` y **no entran** (comprobado: 0 coincidencias) |
| `demo-docker` | **verde** — `verificar-demo-docker.py` sólo mira `microservicios-docker/`; 0 referencias a la carpeta nueva |

**Y el paso «Nadie salió a la red» del job `labs` sigue verde**, que era el riesgo real:
`git status --porcelain repo-maven` devuelve **0 archivos**. Las dependencias de Spring Cloud se
bajaron al `~/.m2` de esta máquina con `DGT_ONLINE=1`, **no a `repo-maven/`**.

### La decisión de no meter Spring Cloud en `repo-maven/`, y por qué no rompe `D-022-3`

Eureka y el Config Server **no están** en el repositorio embebido, **a propósito**: serían megas de
artefactos en el clon de los dieciocho alumnos para un material que ningún alumno ejecuta.
`demos-instructor/` es, por definición, lo que no viaja en la maleta.

`construir.sh` usa `DGT_ONLINE=1` —el modo que el propio shim trae para quien prepara el material— y
necesita red **una vez**, antes de la clase. Es el mismo trato que las imágenes de Docker de la otra
demostración.

**`D-022-3` sigue diciendo lo que decía:** todo lo que el **alumno** necesita viaja en el repositorio
y funciona sin red. Esta demostración no la ejecuta ningún alumno.

Y se puede comprobar el día antes:

```
$ ./construir.sh --sin-red
==> registro ... ==> auditoria
Los seis jar:  (los seis, en verde, sin bajar nada)
```

Queda escrito en `demos-instructor/README.md`, porque es la primera vez que la regla de esa carpeta
tiene consecuencias para Maven y no sólo para Docker.

---

## 10 · La guía en PDF, y una mejora del generador

`guia-demo-microservicios-discovery.pdf` · **12 páginas**, con el formato de las últimas: el problema
para la abuelita (la centralita y el tablón de anuncios), la explicación técnica, qué señalar en cada
momento, y **cuatro bloques de código extraídos** de los fuentes —no tecleados—, verificados por el
propio generador.

**Los dos diagramas son SVG de verdad, no ASCII**, y viven versionados en
`docs/guias/diagramas/`:

| | |
|---|---|
| `registro-en-tres-momentos.svg` | Cómo se registran los servicios: se inscribe, se pregunta, **y la llamada va directa** — el tercer momento es el que explica el flujo 4.4 |
| `url-fija-contra-por-nombre.svg` | Qué cambia entre llamar por URL fija y llamar por nombre, con el caso «el día que se mueve al 8299» a los dos lados |

**Hubo que tocar `tools/generar-guias.py`, y conviene saber por qué**, porque es la primera guía del
curso con imágenes:

- Se dejó a pandoc resolver el SVG y emitió `\includesvg`, que **necesita Inkscape** y, sin él,
  compone el diagrama a un tamaño arbitrario ignorando el ancho pedido.
- Ahora el generador **convierte cada SVG a PDF con `rsvg-convert`** y reescribe el enlace. pandoc
  emite un `\includegraphics` normal, el ancho se respeta, y **no hace falta Inkscape**.
- Si falta `rsvg-convert`, el generador **falla con el comando de instalación** (`brew install
  librsvg`) en vez de producir un PDF con los diagramas mal puestos.

**Dependencia nueva para quien REGENERA las guías:** `rsvg-convert`. No afecta al alumno ni al CI
—ningún job genera guías— y es del mismo orden que pandoc y xelatex, que ya hacían falta.

---

## 11 · Lo que este material NO trae, y va dicho en clase

- **Balanceo entre N instancias.** Es la mejor demostración posible de un registro y **no está**:
  serían siete procesos y el laboratorio no balancea. **Es lo primero que añadir si esto crece.**
- **Varios nodos de Eureka replicándose.** Se explica de palabra en el bloque 4.
- **`spring-cloud-bus`**, que propaga un `refresh` a todos a la vez. Necesita un broker.
- **Backend Git para el Config Server.** Aquí son archivos, para poder editarlos proyectados.
- **Seguridad en el Config Server.** Aquí sirve a quien pregunte; uno real va cifrado y autenticado.

---

## 12 · Para el PO · lo que hay que decidir

1. **El número 048 repetido.** Ver la nota de cabecera. Un `git mv` lo arregla.
2. **Si esto se dicta, va al final y es opcional.** Está escrito así en el README y en la guía.
3. **Si se dictan las dos demostraciones, Docker primero.** La frase que las cose —*Eureka no compite
   con Docker; compite con no tener plataforma*— sólo funciona en ese orden.
4. **El bloque 4 hasta «cinco minutos sin un error», y no encender el registro en vivo** salvo que
   sobre tiempo. Es la única recomendación de guion que hago con énfasis.
5. **`eureka.client.refresh.enable: false` no se quita** mientras el tren de Spring Cloud declare
   Boot 4.0.x.
