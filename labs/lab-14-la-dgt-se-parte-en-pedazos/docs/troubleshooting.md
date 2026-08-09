# Troubleshooting · Lab 14

*Tabla numerada. Cita la fila (T-4, T-9…) cuando pidas ayuda: es más rápido para todos.*

---

## T-1 · El arranque tarda muchísimo la primera vez

**Síntoma.** `./bin/start-lab.sh` lleva cuatro minutos en «Compilando los cinco proyectos».

**Es normal.** La primera vez se descargan las dependencias del tren de Spring Cloud, se
compilan cinco proyectos y se construyen cinco imágenes Docker. Entre cuatro y seis
minutos, según tu red.

**A partir de la segunda vez: menos de un minuto.** Medido: 56 s con las imágenes ya
construidas.

**Si supera los diez minutos** y no ves progreso, casi siempre es la red y no Maven. Corta
con Ctrl-C y reintenta: lo ya descargado no se vuelve a bajar.

---

## T-2 · El registro no llega a las seis piezas

**Síntoma.** `[ERROR] Solo 4 pieza(s) llegaron al registro en 240 s`.

```bash
cd sistema
docker compose ps -a          # ¿quién no está "healthy"?
docker compose logs dgt-contribuyentes | tail -40
```

Causas más frecuentes, en orden:

1. **Falta memoria.** Con menos de ~3 GB libres para Docker, alguna JVM muere al arrancar.
   Cierra el IDE y el navegador y reintenta. Ver el plan B en `INSTRUCTOR.md`.
2. **PostgreSQL no arrancó** → `dgt-contribuyentes` y `dgt-tramites` no arrancan.
   `docker compose logs postgres`.
3. **El Config Server no está sano** → nadie que dependa de él arranca. Es el efecto
   dominó de `fail-fast: true`, y es a propósito: arrancar con media configuración es peor
   que no arrancar.

Para ver a Eureka hablar: sube su log a DEBUG en `config-repo/application.yml`
(`logging.level.com.netflix.discovery: DEBUG`) y reinicia.

---

## T-3 · «missing table [contribuyente]» al arrancar

**Síntoma.** El servicio muere con `SchemaManagementException: Schema validation: missing
table`.

**Causa.** Flyway no corrió, así que Hibernate valida contra una base vacía.

**En Spring Boot 4 la autoconfiguración de Flyway está en un módulo aparte:**
`flyway-core` por sí solo **no la enciende**. Hace falta también:

```xml
<dependency>
  <groupId>org.springframework.boot</groupId>
  <artifactId>spring-boot-flyway</artifactId>
</dependency>
```

Es la misma trampa que documenta el `pom.xml` del tronco. Si el pom ya la tiene, mira si
el volumen de PostgreSQL quedó a medias: `docker compose down -v` y vuelve a levantar.

---

## T-4 · El fallback no dispara: la excepción sale hacia fuera

**Síntoma.** Con el proveedor caído recibes un 500 en vez de la ficha degradada. En el log,
algo del estilo *«fallbackMethod not found»*.

**Causa.** La firma del método de fallback no coincide. Resilience4j lo busca **por
reflexión**, y exige:

> mismos parámetros que el método original, **más** un `Throwable` al final.

```java
public  FichaContribuyente buscar(String rut) { ... }
private FichaContribuyente fichaDegradada(String rut, Throwable causa) { ... }
//                                        ^^^^^^^^^^ ^^^^^^^^^^^^^^^^
```

**Lo peligroso de este fallo es que la aplicación arranca perfectamente.** Te enteras la
primera vez que el vecino se cae — en producción, a las tres de la mañana. Por eso hay un
test que lo comprueba (`FallbackYRetryTest`).

---

## T-5 · El portal devuelve 404 en todo

**Síntoma.** `curl localhost:8099/api/v1/tramites` → 404, aunque el registro tiene las seis
piezas.

**Causa casi segura: el prefijo de las rutas.** En el tren 2025.1.x es

```yaml
spring.cloud.gateway.server.webmvc.routes
```

Los tutoriales viejos usan `spring.cloud.gateway.routes` a secas. Con ese prefijo, Spring
lee el bloque, no reconoce nada, **y el gateway arranca sin ninguna ruta**. Sin error, sin
aviso: todo 404, y parece que el problema está en otro sitio.

Comprueba qué configuración le llegó de verdad al portal:

```bash
curl -s http://localhost:8888/dgt-portal/default
```

---

## T-6 · El portal devuelve 503 justo después de arrancar

**Síntoma.** `503 Unable to find instance for dgt-tramites`, y a los pocos segundos
funciona.

**No es un fallo: es la ventana entre «arrancado» y «estable».** El balanceador del portal
todavía no se ha bajado la lista de instancias del registro.

Por eso `start-lab.sh` espera a **tres respuestas completas seguidas** antes de darte el
control, en vez de conformarse con que los contenedores estén `healthy`.

**Si lo ves fuera del arranque**, mira T-9.

---

## T-7 · Cambié `dgt-tramites.yml` y no pasó nada

Dos cosas, en este orden:

**1. ¿Reiniciaste el consumidor?** El Config Server sirve tu archivo enseguida, pero el
servicio lo lee **al arrancar**:

```bash
./bin/start-lab.sh --reiniciar-tramites
```

**2. ¿La sangría es correcta?** Los umbrales van **dentro** de `contribuyentes:`, alineados
con `register-health-indicator`. Y aquí está la trampa:

> Si la sangría está mal, **el servicio arranca igual**. Spring ignora en silencio lo que
> no encaja donde lo pusiste, Resilience4j crea la instancia con los valores por defecto, y
> tu configuración simplemente no existe.

Comprueba qué cargó de verdad:

```bash
cd sistema
docker compose exec -T dgt-tramites curl -s http://localhost:8081/actuator/circuitbreakers
```

Si `failureRateThreshold` sigue en `50.0%` y no pusiste 50, o si el circuito no abre nunca,
tu bloque no se está aplicando.

**Lo mismo pasa si el NOMBRE no coincide.** La instancia se llama `contribuyentes` en
`ConsultaDeContribuyentes.CIRCUITO`. Si en el YAML escribes `contribuyente` sin la ese,
Resilience4j crea *otra* instancia con los defaults y sigue tan tranquilo. Es la forma
número uno de perder una tarde.

---

## T-8 · Maté una instancia y el sistema tarda en recuperarse

**Es el contenido del bloque 3, no un fallo.** Entre que una instancia muere y que el
sistema deja de mandarle tráfico pasan segundos: el registro tiene que dejar de verla
renovar, tacharla, y los clientes tienen que releer la lista.

Con los valores por defecto de Eureka serían ~90 s. Este laboratorio los baja a ~15 s en
`config-repo/application.yml`, con la nota entera sobre por qué **no** debes copiar esos
números a producción sin pensar.

Si pasan más de 60 segundos, entonces sí mira T-2: probablemente la instancia superviviente
también se cayó.

---

## T-9 · El registro ve la instancia nueva, pero no le llega tráfico

**Síntoma.** Levantas una segunda instancia, aparece en `localhost:8761` al instante, y
todas las peticiones siguen yendo a la primera durante medio minuto.

**Causa: hay DOS cachés, no una.**

| Caché | Qué guarda | Default |
|---|---|---|
| Cliente de Eureka | la lista del registro | 30 s |
| **Spring Cloud LoadBalancer** | esa misma lista, otra vez | **35 s** |

Bajar solo la primera no sirve: el balanceador sigue repartiendo con la lista vieja. Las dos
están bajadas a 5 s en `config-repo/application.yml`.

Es el error de razonamiento clásico con las cachés: se optimiza la que se conoce y se olvida
la que hay detrás.

---

## T-10 · «resilience4j-spring-boot3» en un proyecto Boot 4

**Síntoma.** Ves ese artefacto en `dependency:tree` y piensas que hay un desajuste de
versiones.

**No lo hay.** `spring-boot3` es el **nombre del módulo** que le puso Resilience4j, no la
versión de Boot con la que funciona. Es el que referencia el tren de Spring Cloud 2025.1.x,
que está probado contra Boot 4.1.0.

No lo cambies ni busques un `resilience4j-spring-boot4`: no existe.

---

## T-11 · El puerto 8099 está ocupado

Es el puerto del curso desde el Lab 00, así que lo más probable es que sea un laboratorio
anterior que quedó levantado.

```bash
lsof -nP -iTCP:8099 -sTCP:LISTEN        # quién es
```

Si es un lab anterior, su `./bin/99-destruir.sh` lo baja. Si es otra cosa tuya, bájala tú:
este laboratorio publica el 8099, el 8761 y el 8888, y los tres tienen que estar libres.

---

## T-12 · Quiero mi máquina como estaba

```bash
./bin/99-destruir.sh              # contenedores, red, volúmenes, target/
./bin/99-destruir.sh --imagenes   # y además el ~1 GB de imágenes del lab
```

Sin `--imagenes` se conservan las cinco imágenes: el próximo arranque tarda segundos en vez
de minutos.

**No toca nada que no haya creado este laboratorio.** Si ves contenedores de otros
proyectos tuyos después de correrlo, siguen ahí a propósito.
