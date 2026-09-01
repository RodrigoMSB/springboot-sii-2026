# Demostración · El lab 14 con Docker

El **mismo sistema** del `lab-14-microservicios`: gateway, contribuyentes, trámites y auditoría,
con sus tres bases separadas. El mismo código, las mismas rutas, el mismo circuit breaker.

**Lo único que cambia es cómo se arranca.** Y ése es exactamente el tema.

> ⚠️ **Esto no es un laboratorio y el alumno no lo corre.** No lleva `practica/`, ni `PASOS.md`,
> ni bloques para pegar: no hay nada que teclear. Necesita **Docker**, que las máquinas del SII no
> tienen — y ése fue el motivo de retirar el lab 14 antiguo. Se proyecta, se mira, y se vuelve al
> laboratorio de verdad.

---

## El único gráfico que hace falta

```
          EN EL LABORATORIO                     AQUÍ

   ┌────────────┬────────────┐            $ docker compose up
   │ terminal 1 │ terminal 2 │                     │
   │ contribuy. │  trámites  │                     ▼
   ├────────────┼────────────┤            ┌─────────────────┐
   │ terminal 3 │ terminal 4 │            │  siete          │
   │ auditoría  │  gateway   │            │  contenedores   │
   └────────────┴────────────┘            │  en el orden    │
                                          │  correcto       │
   y el orden lo pones tú,                └─────────────────┘
   cada vez, sin equivocarte
```

---

## Lo que necesita

| | |
|---|---|
| **Docker Desktop** corriendo | única cosa que no viaja en el repositorio |
| **Red, la primera vez** | para bajar `eclipse-temurin:25-jre-alpine` (233 MB) y `postgres:16-alpine` (388 MB). Después, ninguna |
| RAM | **871 MiB** las siete piezas en reposo, medido. Techo configurado: 2,5 GiB (4 × 448 MB + 3 × 256 MB) |
| Disco | **1,28 GB** en imágenes (233 MB de capa base compartida por las cuatro) |

**Baja las dos imágenes antes de la clase.** Es lo único de esta demostración que necesita
internet, y hacerlo delante de la sala con el wifi del SII es la forma más segura de perder diez
minutos.

---

## Cómo se levanta

```bash
cd demos-instructor/lab-14-docker
./construir.sh          # los cuatro jar, con el Maven del curso, sin red
docker compose up       # y esto es lo que hay que mirar
```

`construir.sh` está aparte a propósito: lo que la demostración tiene que enseñar es
`docker compose up` **a pelo**. Si un script lo tapara, el protagonista sería el script.

Para apagarlo todo y no dejar rastro:

```bash
docker compose down -v  # -v se lleva también las tres bases
```

---

## Las direcciones

| | |
|---|---|
| **La puerta** | **http://localhost:8220** — es la única que hay que recordar |
| contribuyentes, directo | http://localhost:8221 |
| trámites, directo | http://localhost:8222 |
| auditoría, directo | http://localhost:8223 |
| las tres bases | `localhost:55470`, `55471`, `55472` — usuario `svc_<servicio>`, clave `<servicio>-dev` |

Dentro de la red del compose, los cuatro servicios escuchan en el **8080**. Los 822x son solo
cómo se ven desde fuera, y eso lo decide el `ports:` del compose — no el programa.

---

## El guion de la demostración · quince minutos

### 0 · Antes de que entre nadie

```bash
./construir.sh && docker compose up -d
```

Levantarlo delante de la sala la primera vez es construir cuatro imágenes en directo. Que esté
arriba, y para el momento del arranque se hace `docker compose down` y `up` otra vez —eso sí son
21 segundos y se ve bien—.

---

### 1 · Que funciona igual (2 min)

Lo primero es quitar la sospecha de que esto es otro sistema.

```bash
TOKEN=$(curl -s -X POST http://localhost:8220/auth/login \
  -H 'Content-Type: application/json' \
  -d '{"usuario":"carolina","clave":"dgt2026"}' | sed 's/.*"token":"\([^"]*\)".*/\1/')

curl -s -H "Authorization: Bearer $TOKEN" http://localhost:8220/tramites/1
```

```json
{"id":1,"tipo":"DECLARACION_F29","estado":"EN_PROCESO",
 "rutContribuyente":"11111111-1","nombreContribuyente":"Carolina Fuentes Aravena",
 "estadoDelNombre":"OK","creadoEn":"..."}
```

**Qué señalar:** es la misma respuesta del laboratorio, con el mismo `estadoDelNombre`. Ese
`nombreContribuyente` **no está en la base de trámites**: vino por HTTP desde otro contenedor.

---

### 2 · El arranque, que es el motivo de todo esto (3 min) ⭐

```bash
docker compose down
docker compose up
```

Y se deja correr, mirando el log.

**Qué señalar, en este orden:**

1. **Las tres bases arrancan primero.** Nadie se lo dijo en el momento: está escrito una vez, en
   los `depends_on`.
2. **Los servicios esperan a que sus bases estén SANAS**, no un `sleep`. `condition:
   service_healthy` pregunta al `HEALTHCHECK` de verdad. Un `sleep 30` funciona en la máquina de
   quien lo escribió y falla en la del vecino.
3. **El gateway es el último.** El README del laboratorio dice el orden a mano —contribuyentes →
   trámites → auditoría → gateway, de dentro hacia fuera— y advierte de lo que pasa si te
   equivocas: el circuito se abre por fallos que solo ocurrieron durante el arranque y tarda 18 s
   en curarse. **Aquí ese orden lo cumple la máquina, y no se puede equivocar.**

**Son 21 segundos** hasta que el gateway responde. Comparar con abrir cuatro terminales, hacer
cuatro `cd` y cuatro `./mvnw spring-boot:run` en el orden correcto.

> Y la trampa que hay que desactivar antes de que alguien la muerda: esto **no** significa que el
> sistema *necesite* ese orden. Significa que arrancar ordenado evita el circuito abierto de los
> primeros segundos. La dependencia es de **arranque**, no de vida — y el bloque 4 lo demuestra.

---

### 3 · Las bases ya no van dentro del programa (2 min)

```bash
docker compose ps
```

Siete contenedores: cuatro servicios y **tres PostgreSQL**.

**Qué señalar:** en el laboratorio, cada servicio levanta su propia base **dentro de su propio
proceso**, con Zonky, y por eso `ContribuyentesApplication` tenía un `MotorDePostgres` y dos
guardas de puerto y de candado antes del `SpringApplication.run`. Abrir las dos clases al lado:

```java
// en el laboratorio                        // aquí
public static void main(String[] args)      public static void main(String[] args) {
        throws IOException {                    SpringApplication.run(...);
    new MotorDePostgres(55460).levantar();  }
    SpringApplication.run(...);
}
```

**Un `main` que ya no tiene que montar su propia infraestructura.** Las guardas de puerto y
candado —que existían porque cuatro terminales se pisan— aquí sobran: cada contenedor tiene su
red y su sistema de archivos.

Y la frontera entre servicios es **más fuerte** que en el laboratorio:

```bash
docker compose exec tramites getent hosts db-tramites        # una IP
docker compose exec tramites getent hosts db-contribuyentes  # nada
```

Cada servicio y su base comparten una red privada de dos miembros. El JOIN «rápido» para ahorrarse
la llamada HTTP no es que esté prohibido: **es que no hay por dónde intentarlo.**

---

### 4 · Se hablan por nombre (2 min)

```bash
docker compose exec tramites getent hosts contribuyentes
```

```
172.31.0.3        contribuyentes
```

**Qué señalar:** en el laboratorio, `application.yml` decía `http://localhost:8211`. Aquí dice
`http://contribuyentes:8080`. Lo único que cambió es lo que hay **antes de los dos puntos**.

Esa IP **cambia en cada arranque**, y a nadie le importa: el nombre lo resuelve el DNS interno de
la red del compose. Es el *service discovery* que el lab 14 antiguo montaba con un Eureka
—un servicio más que arrancar, configurar y operar—, hecho aquí por la plataforma y gratis.

Y el puerto dejó de importar: **los cuatro escuchan en el 8080**. Repartir puertos para que no
choquen era un problema de tener todo en la misma máquina.

---

### 5 · Matar un contenedor ⭐⭐ (4 min)

**Es el bloque que justifica la demostración entera.** Y tiene una sorpresa, así que conviene
hacerlo en los dos órdenes.

#### 5a · Lo que NO pasa

```bash
docker compose kill contribuyentes
docker compose ps -a
```

```
contribuyentes      Exited (137) 52 seconds ago
```

**Sigue muerto.** El `restart: unless-stopped` está puesto y no ha hecho nada.

**Por qué, y es la lección:** una política de reinicio responde a que el **proceso** se muera, no
a que un **operador** mate el contenedor. Docker distingue las dos cosas a propósito: si matas
algo a mano, es porque querías que se quedara muerto. Un orquestador que te lo resucitara sería
un orquestador con el que no se puede trabajar.

Mientras tanto, el sistema responde:

```bash
curl -s -H "Authorization: Bearer $TOKEN" http://localhost:8220/tramites/1
```

```json
{... "nombreContribuyente":null, "estadoDelNombre":"NO_DISPONIBLE" ...}
```

**HTTP 200.** Es el circuit breaker del laboratorio, intacto: el orquestador no lo sustituye.

#### 5b · Lo que sí pasa

Se levanta otra vez y ahora se mata **el proceso, desde dentro** — que es lo que ocurre de verdad
cuando algo revienta, se queda sin memoria o el kernel lo mata:

```bash
docker compose up -d contribuyentes
docker compose exec contribuyentes sh -c 'kill -TERM 1'
```

Y se mira, sin tocar nada más:

```
t+00s  estadoDelNombre=NO_DISPONIBLE   contribuyentes: Up 47 seconds (healthy)  <- todavía el viejo
t+03s  estadoDelNombre=NO_DISPONIBLE   contribuyentes: Up 2 seconds (health: starting)
t+07s  estadoDelNombre=NO_DISPONIBLE   contribuyentes: Up 6 seconds (healthy)
t+11s  estadoDelNombre=OK              contribuyentes: Up 10 seconds (healthy)
```

**El servicio se murió y volvió solo en once segundos, y el usuario nunca vio un error.**

Tres cosas que señalar, y las tres importan:

1. **A las tres décimas ya hay un contenedor nuevo.** Fíjate en el contador de `Up`: se reinició.
   Eso es lo que cuatro terminales no hacen. En el laboratorio, un servicio que muere se queda
   muerto hasta que alguien lo nota y vuelve a teclear `./mvnw spring-boot:run`.
2. **Entre t+07s y t+11s el contenedor ya estaba sano y la respuesta seguía degradada.** Esos
   cuatro segundos son el **circuit breaker**, que todavía no había vuelto a probar. El
   orquestador levanta el proceso; decidir cuándo confiar otra vez en él sigue siendo del
   programa.
3. **Todo el rato, HTTP 200.** El circuit breaker del lab 14 y el reinicio de Compose son dos
   protecciones **distintas y complementarias**: una tapa el hueco mientras dura, la otra hace que
   dure poco. Ninguna sustituye a la otra.

---

### 6 · El cierre (2 min)

```bash
docker compose down -v
```

**Lo que hay que dejar dicho, y es lo contrario de una venta:**

> El orquestador **no arregló nada del sistema**. El código es el mismo, byte a byte —hay un
> verificador que lo comprueba—. Lo que hizo fue quitar de en medio el trabajo de **operarlo**:
> el orden de arranque, la espera a que el vecino esté listo, las direcciones que cambian, y
> levantar lo que se cayó.
>
> Eso no es poco: es exactamente el trabajo que en el laboratorio hacían cuatro terminales y una
> persona acordándose del orden. Pero **el fallo en cascada, el circuit breaker y la decisión de
> qué devolver cuando no hay respuesta siguen siendo tuyos.** Un orquestador no te ahorra pensar
> en que el vecino se cae; te ahorra levantarlo a mano cuando pasa.

Y la segunda, que es del curso entero:

> Todo esto se paga en **una pieza más que operar**. Docker Desktop, un compose que mantener, unas
> imágenes que reconstruir y actualizar. Con un servicio, no compensa. Con siete contenedores y
> cuatro terminales, ya compensaba antes de terminar de contarlo.

---

## Qué se reutilizó del lab 14 antiguo, y qué no

El lab antiguo (`material-v0.8.0`, `labs/lab-14-la-dgt-se-parte-en-pedazos/`) tenía Compose,
Dockerfile, Config Server y Eureka. Se leyó entero antes de escribir nada.

**Se reutilizó:**

| | |
|---|---|
| **El orden de las capas del Dockerfile** | Todo lo que no depende del módulo va antes del `ARG`, para que las cuatro imágenes compartan la capa base. Aquello se midió: 626 MB de diferencia con cinco servicios |
| **`depends_on: condition: service_healthy`** | Arranque ordenado sin un solo `sleep`, contra healthchecks de verdad |
| **`mem_limit` en cada contenedor** | Sin techo, cada JVM dimensiona su heap contra la RAM de la máquina anfitriona, y «cuánta RAM necesita esto» pasa a depender de quién pregunte |
| **El aviso sobre el orden** | «La dependencia es de arranque, no de vida». Está en el bloque 2 |
| **La idea de la frontera de datos** | Que la separación entre servicios se haga **real** y no sea una convención escrita en un README |

**Se descartó:**

| | Por qué |
|---|---|
| **Eureka (`dgt-registro`)** | El DNS de la red del compose ya resuelve nombres. Meter un registro sería añadir un servicio de infraestructura para hacer lo que la plataforma hace gratis, y la demostración va justo de eso |
| **Config Server (`dgt-config`)** | Otro proceso más, y el lab 14 actual no lo usa: sus direcciones son tres líneas de `application.yml`. Añadirlo aquí sería demostrar un sistema que el alumno no ha visto |
| **Una sola instancia de PostgreSQL con dos bases y dos usuarios** | La frontera por `GRANT` es correcta y vive dentro del mismo motor. Aquí son **tres motores en tres redes**, que es más fuerte y además se ve en `docker compose ps` |
| **`bin/start-lab.sh` y sus siete banderas** | Envolvían `docker compose` en un script. Aquí el protagonista tiene que ser `docker compose up` |
| **El escalado a dos instancias** | Necesita un balanceador y un registro. El lab 14 actual no balancea, y demostrarlo sería enseñar algo que el laboratorio no tiene |
| **Los cuatro números del circuit breaker** | Eran el ejercicio del alumno en un lab de tres horas. Aquí no hay ejercicio |

---

## La copia, y cómo se vigila

`sistema/` es una **copia** del `solucion/` del `lab-14-microservicios`. Copiar código es aceptar
que se separe, así que hay un verificador:

```bash
python3 tools/verificar-demo-docker.py
```

Compara los dos árboles archivo a archivo y exige que sean **idénticos byte a byte** salvo en
cuatro sitios, que son exactamente las piezas que el orquestador reemplaza:

| | |
|---|---|
| `infra/MotorDePostgres.java`, `PuertoLibre.java`, `CandadoLibre.java` | **no están**: la base es un contenedor |
| las tres `*Application.java` con base | pierden la línea que levantaba esa base |
| los `pom.xml` | sin Zonky, y con el `repackage` encendido — el jar ejecutable es lo que se despliega |
| los `application.yml` | `localhost:puerto` pasa a ser un nombre de servicio |

Hoy: **32 archivos idénticos, 11 con diferencia declarada, 9 retirados a propósito.**

**No necesita Docker**, así que corre en el CI (job `demo-docker`). Es lo único de esta carpeta
que el CI puede mirar, y es justo lo que hace falta: si alguien arregla un defecto en el
laboratorio y no aquí, se pone rojo.

**Si el laboratorio y la demostración discrepan, manda el laboratorio.** Es el que dictan los
alumnos.

---

## Si algo falla

| Síntoma | Qué pasa |
|---|---|
| `Cannot connect to the Docker daemon` | Docker Desktop no está corriendo |
| `no such file .../target/lab14-*-docker-0.1.0.jar` | Falta `./construir.sh`. La imagen no compila: solo copia el jar |
| `port is already allocated` | Algo ocupa el 8220–8223 o el 5547x. `docker compose down` de otra copia, o el lab 14 corriendo a la vez |
| Un servicio se queda en `health: starting` para siempre | `docker compose logs <servicio>`. Casi siempre es la base: `docker compose ps` dirá si la suya está sana |
| `pull access denied` / timeout al construir | Es la primera vez y no hay red. Las dos imágenes base se bajan antes de la clase |
| Todo lento la primera vez | Se están construyendo cuatro imágenes. Las siguientes son segundos |
