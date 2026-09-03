# Pasos · Lab 13 · Empaquetado

Cuatro pasos, y el tercero **no se teclea**: son veinte minutos de explicación con la pantalla
apagada. Es deliberado — en este curso nadie ha visto nunca un contenedor, y hablar de imágenes sin
saber qué son no enseña nada.

```bash
cd practica
./mvnw package
```

Escucha en el **8105** (`solucion/`, en el 8106).

Lo que se escribe hoy son tres cosas pequeñas:

```
pom.xml                     →  paso 2 (las capas)
resources/application-*.yml →  paso 4 (los perfiles)
```

**Los nombres de los jar son distintos en cada carpeta**, y conviene tenerlo presente al copiar
órdenes:

```
practica/target/lab13-empaquetado-0.1.0.jar
solucion/target/lab13-empaquetado-solucion-0.1.0.jar
```

Los comandos de abajo van escritos para `practica/`.

---

## Paso 1 · El jar

**Se explica:** durante doce laboratorios, la aplicación arrancó con `./mvnw spring-boot:run`. Eso
es una herramienta de desarrollo: compila, arma el classpath y lanza. **En un servidor no hay
Maven, ni código fuente.** Hay un archivo.

**Se corre:**

```bash
./mvnw package
ls -lh target/*.jar
```

**En consola:**

```
21M   lab13-empaquetado-0.1.0.jar
```

Veintiún megas para una aplicación de dos clases. Se abre a ver qué hay dentro:

```bash
jar tf target/lab13-empaquetado-0.1.0.jar | head
unzip -p target/lab13-empaquetado-0.1.0.jar META-INF/MANIFEST.MF
```

```
META-INF/MANIFEST.MF
org/springframework/boot/loader/...
BOOT-INF/classes/...          ← nuestro código
BOOT-INF/lib/...              ← 40 y pico de jars: Spring, Tomcat, Jackson...

Main-Class:  org.springframework.boot.loader.launch.JarLauncher
Start-Class: cl.dgt.empaquetado.Lab13Application
```

**Lo que hay que notar, y es lo que explica los 21 MB:** este jar lleva **Tomcat dentro**. No se
despliega en un servidor de aplicaciones — trae el suyo. Por eso se llama *fat jar*.

Y las dos líneas del manifiesto son el truco entero:

| | |
|---|---|
| `Main-Class` | lo que arranca de verdad: un cargador de Spring Boot |
| `Start-Class` | **nuestra** clase, que el cargador buscará después |

Hace falta ese rodeo porque el `java -jar` estándar **no sabe leer jars dentro de un jar**. El
cargador de Spring Boot sí.

**Se corre, y aquí está el momento:**

```bash
java -jar target/lab13-empaquetado-0.1.0.jar
```

```
Tomcat started on port 8105 (http)
Started Lab13Application in 1.445 seconds
```

**Sin Maven. Sin código fuente. Sin instalar nada más que Java.** Eso es lo que se despliega.

---

## Paso 2 · Las capas

**Se explica:** el jar de arriba es un solo bloque de 21 MB. Si mañana se corrige una palabra en un
mensaje, se construye otro jar de 21 MB **entero** y hay que moverlo entero.

Y sin embargo, de esos 21 MB, **casi todo no cambió**: son Spring, Tomcat y Jackson, exactamente
los mismos de ayer. Lo que cambió son unos pocos kilobytes.

**Se pega:** en `practica/pom.xml`, **dentro del plugin de Spring Boot**, donde dice
`<!-- escribe aquí -->`.

```xml
        <configuration>
          <layers>
            <enabled>true</enabled>
          </layers>
        </configuration>
```

**Se corre:**

```bash
./mvnw package
java -Djarmode=tools -jar target/lab13-empaquetado-0.1.0.jar list-layers
```

**En consola:**

```
dependencies
spring-boot-loader
snapshot-dependencies
application
```

**Cuatro capas, ordenadas de lo que menos cambia a lo que más:**

| capa | qué lleva | cada cuánto cambia |
|---|---|---|
| `dependencies` | Spring, Tomcat, Jackson… | cuando se toca el `pom.xml` |
| `spring-boot-loader` | el cargador | cuando se sube de versión de Boot |
| `snapshot-dependencies` | librerías en desarrollo | rara vez |
| `application` | **nuestro código** | **cada despliegue** |

**Hoy esto no cambia nada medible:** el jar pesa lo mismo y arranca igual. Conviene decirlo al
escribirlo, para que nadie sienta que hizo algo inútil. Lo que se acaba de construir es una
**estructura**, y su valor se entiende con el paso 3.

> En Spring Boot 3 el modo se llamaba `layertools` y el subcomando `list`. Si alguien trae un
> ejemplo viejo de internet y no le funciona, es eso.

---

## Paso 3 · Qué es un contenedor

**No se teclea nada. Veinte minutos.**

Es la única parte del curso donde se explica algo que el alumno no ha visto nunca. Y hoy tiene
además otra función: es donde el paso 2 cobra sentido.

### El problema

Se despliega el jar del paso 1 en un servidor y no arranca. El servidor tiene Java 17 y el jar se
compiló con Java 25. Se instala Java 25, y entonces se rompe **otra** aplicación que necesitaba la
17. Aparece la frase:

> «En mi máquina funciona.»

Y es literalmente cierta, y por eso es tan difícil de resolver: el jar es idéntico, lo que cambia
es **todo lo que hay debajo** — la versión de Java, las bibliotecas del sistema, la zona horaria,
la codificación por defecto.

### La primera solución: la máquina virtual

Llevarse la máquina entera: sistema operativo, Java, la aplicación, todo en un archivo. Funciona,
y es caro: cada máquina virtual lleva **un sistema operativo completo** — gigabytes, y un minuto o
más para arrancar.

### El contenedor

La idea: **compartir el núcleo del sistema operativo y aislar todo lo demás.**

Un contenedor no lleva sistema operativo. Es un **proceso normal** de la máquina anfitriona, al que
el núcleo le ha mentido sobre el mundo:

- ve su propio sistema de archivos (y no el de la máquina)
- ve sus propios procesos, y se cree el número 1
- ve su propia red, con su propia IP
- tiene un tope de memoria y de procesador

Todo eso lo hace **el núcleo de Linux**, con dos mecanismos que conviene nombrar aunque no se
profundice: *namespaces* (qué ve) y *cgroups* (cuánto puede consumir).

| | máquina virtual | contenedor |
|---|---|---|
| lleva sistema operativo | **sí**, completo | no: comparte el núcleo |
| tamaño típico | varios GB | decenas o cientos de MB |
| arranque | de 30 s a minutos | **milisegundos** |
| aislamiento | total (hardware virtual) | bueno, pero comparte núcleo |

### Y la imagen

La **imagen** es la plantilla; el **contenedor** es una imagen corriendo. La misma relación que
entre una clase y un objeto, y esa comparación funciona bien en una sala de programadores.

La imagen está hecha de **capas apiladas**, cada una con los cambios sobre la anterior. Y son **de
sólo lectura y compartidas**: si diez imágenes usan la misma base de Java, esa base se guarda **una
vez**.

**Aquí es donde el paso 2 cobra.** Una imagen típica de esta aplicación repartiría así sus ciento
treinta y ocho megas:

```
  ~120 MB   la base: sistema mínimo + JRE 25        ┐
   ~18 MB   las dependencias (Spring, Tomcat...)    ├─ no cambian casi nunca
  < 0,1 MB  NUESTRO CÓDIGO                          ┘  cambia cada día
```

Esas cuatro capas del jar se convierten en capas de la imagen. Y entonces:

> Al desplegar una corrección no se mueven 138 MB. Se mueve **la capa que cambió**, que son unos
> kilobytes. Todo lo demás ya está en el servidor.

Sin el `layers.enabled` del paso 2, el jar sería un solo archivo indivisible dentro de la imagen y
cualquier cambio movería los 21 MB enteros.

### OCI, Docker y por qué hoy no se construye ninguna imagen

**OCI** (*Open Container Initiative*) es el **estándar** de formato de imagen. **Docker** es un
programa que construye y ejecuta imágenes OCI — el más conocido, y no el único.

Y aquí va la parte honesta: **en las máquinas de esta sala no hay Docker y no se puede instalar.**
Así que este laboratorio explica qué es un contenedor y **no construye ninguno**. Construir un
archivo que nadie puede ejecutar, para abrirlo con `tar` y mirar un JSON, ocupa media hora y enseña
menos que este dibujo.

Para quien quiera probarlo fuera del curso, el nombre que hay que buscar está en el cierre.

---

## Paso 4 · La misma aplicación en todas partes

**Se explica:** queda la pregunta que cierra el curso. Hay tres ambientes —desarrollo, pruebas,
producción— y en cada uno la base de datos es otra, la URL de Tesorería es otra y los tiempos de
espera son otros.

> ¿Se construye un artefacto para cada ambiente?

**No.** Y hay que decir por qué, porque el instinto dice que sí:

> Si se construye para producción algo **distinto** de lo que se probó, entonces **lo que se probó
> no es lo que se despliega**. Todo el trabajo de pruebas queda en suspenso: lo que llega al
> usuario nunca lo ejecutó nadie.

Un artefacto, todos los ambientes. Lo que cambia va **fuera**.

**Se pega:** archivo **nuevo** `practica/src/main/resources/application-dev.yml` — el archivo
entero.

```yaml
lab13:
  saludo: Hola desde DESARROLLO
  tesoreria-url: http://localhost:9098/pagos
```

**Se pega:** archivo **nuevo** `practica/src/main/resources/application-prod.yml` — el archivo
entero.

```yaml
lab13:
  saludo: Hola desde PRODUCCIÓN
  tesoreria-url: ${TESORERIA_URL:https://tesoreria.example.cl/pagos}
```

**Se corre — el mismo jar, tres veces.** Se construye **una vez** y no se vuelve a tocar:

```bash
./mvnw package

java -jar target/lab13-empaquetado-0.1.0.jar
java -jar target/lab13-empaquetado-0.1.0.jar --spring.profiles.active=dev
TESORERIA_URL=https://tesoreria.sii.cl/pagos \
  java -jar target/lab13-empaquetado-0.1.0.jar --spring.profiles.active=prod
```

(Entre una y otra, `Ctrl+C`. Y en cada una, `curl http://localhost:8105/donde-estoy`.)

**En consola:**

```json
{"perfilesActivos":[],"saludo":"Hola desde el entorno por defecto",
 "tesoreriaUrl":"http://localhost:9999/tesoreria-falsa","javaVersion":"25.0.4"}

{"perfilesActivos":["dev"],"saludo":"Hola desde DESARROLLO",
 "tesoreriaUrl":"http://localhost:9098/pagos","javaVersion":"25.0.4"}

{"perfilesActivos":["prod"],"saludo":"Hola desde PRODUCCIÓN",
 "tesoreriaUrl":"https://tesoreria.sii.cl/pagos","javaVersion":"25.0.4"}
```

**El mismo archivo, byte por byte.** No se reconstruyó nada entre una ejecución y la siguiente.

### El orden de precedencia, que es lo que hay que saberse

De menos a más fuerte — **gana el último**:

```
application.yml  <  application-<perfil>.yml  <  variable de entorno  <  argumento de línea de comandos
```

Se ve en la tercera ejecución: `application-prod.yml` propone
`https://tesoreria.example.cl/pagos`, y `TESORERIA_URL` la pisa con la de verdad. Por eso el valor
está escrito como `${TESORERIA_URL:...}`: **lo de después de los dos puntos es el respaldo**, no el
valor.

> **Y aquí va la regla que cierra el laboratorio:** los secretos —claves de base de datos, el
> secreto de firma del Lab 09— **nunca** van en un `application-prod.yml`, porque ese archivo viaja
> dentro del jar, y el jar se descomprime con `unzip` como cualquier zip. Además está en el
> repositorio, con lo que el secreto quedaría en el historial de Git para siempre — y borrarlo
> después no lo borra.
>
> Los secretos van por variable de entorno, que las pone quien despliega en el momento de arrancar
> y no quedan escritas en ninguna parte.

---

## Lo que no vimos hoy

**Construir la imagen.** El paso 3 explicó qué es un contenedor y no construyó ninguno, porque en
esta sala no hay Docker. Para quien quiera probarlo fuera del curso, el nombre es **Jib**
(`jib-maven-plugin`, de Google): construye una imagen OCI **sin demonio Docker** —escribe el
`.tar` directamente, sin permisos de administrador y sin `Dockerfile`—, y toma el jar por capas del
paso 2 para convertir cada capa del jar en una capa de la imagen. Se declara en el `pom.xml`, se
ejecuta con `./mvnw package jib:buildTar`, y el resultado se abre con `tar -xf` como cualquier tar.
Es exactamente lo que hace el proyecto final de este curso, así que hay un ejemplo funcionando al
que mirar.

Y tres cosas más, que son el paso siguiente natural y ninguna cabe hoy:

- **Kubernetes** y los orquestadores: quién arranca esa imagen, cuántas copias, y qué hace cuando
  una se cae. (Y ahí vuelven el liveness y el readiness del Lab 11.)
- **Registries**: dónde se guardan las imágenes y cómo llegan al servidor que las va a correr.
- **CI/CD**: que todo esto lo haga una máquina en cada cambio, en vez de una persona.

---

## Al terminar

`practica/` construye lo mismo que `solucion/`: un jar de 21 MB en cuatro capas, y el mismo
artefacto comportándose distinto en tres ambientes.

Lo que hay que poder decir con las propias palabras:

> Un fat jar lleva Tomcat dentro y arranca con `java -jar`. Las capas separan lo que cambia
> siempre de lo que no cambia nunca, y por eso un despliegue mueve kilobytes y no megas. Un
> contenedor es un proceso normal al que el núcleo le aisló lo que ve. Y el artefacto se construye
> **una vez**: si se reconstruye para producción, lo que se probó no es lo que se despliega.

### Lo que siembra este lab

En trece sesiones se armó una aplicación que arranca, expone endpoints, guarda en una base de
datos, no se cae bajo concurrencia, está probada, cerrada con llave, sobrevive a que el vecino
falle, cuenta lo que le pasa, hace su trabajo a tiempo, y hoy sale de la máquina donde nació.

**Una** aplicación. Un artefacto, un puerto, una base de datos, un log.

Y esa palabra es lo que siembra el **Lab 14**, que es el último: mañana esa misma DGT son **cuatro
programas distintos con tres bases de datos separadas**, y la pregunta deja de ser cómo se
construye y pasa a ser **qué se gana y qué se paga** al partirla. El artefacto de hoy es la unidad
de despliegue de mañana — un microservicio es, antes que nada, algo que se despliega solo.

Lo que queda por delante —y hay que nombrarlo para que nadie se vaya creyendo que el mapa está
completo— es **quién cuida eso una vez desplegado**: quién lo arranca, cuántas copias, qué pasa
cuando una se cae, dónde se miran las métricas y los registros, y cómo llega una versión nueva sin
cortar el servicio.

> **Lo que se lleva:** hasta hoy, «funciona» quería decir «funciona en mi máquina, ahora, mientras
> yo lo miro». Desde hoy quiere decir otra cosa: **un artefacto que alguien que no eres tú puede
> arrancar en una máquina que no es la tuya, y que sigue funcionando cuando nadie está mirando.**

Y la pregunta que conviene llevarse puesta, la misma que abrió el Lab 08:

> Cuando cambies algo la semana que viene — **¿qué test lo demuestra?**
