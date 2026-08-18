# Pasos · Lab 12 · Empaquetado

Cinco pasos, y el tercero **no se teclea**: son veinte minutos de explicación con la pantalla
apagada. Es deliberado — en este curso nadie ha visto nunca un contenedor, y construir uno sin
saber qué es no enseña nada.

```bash
cd practica
./mvnw package
```

Escucha en el **8105** (`solucion/`, en el 8106).

Lo que se escribe hoy vive casi todo en el `pom.xml`:

```
pom.xml                     →  pasos 2 y 4 (las capas y Jib)
resources/application-*.yml →  paso 5 (los perfiles)
```

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
20.9 MB   lab12-empaquetado-0.1.0.jar
```

Veintiún megas para una aplicación de dos clases. Se abre a ver qué hay dentro:

```bash
jar tf target/lab12-empaquetado-0.1.0.jar | head
unzip -p target/lab12-empaquetado-0.1.0.jar META-INF/MANIFEST.MF | grep Class
```

```
META-INF/MANIFEST.MF
org/springframework/boot/loader/...
BOOT-INF/classes/...          ← nuestro código
BOOT-INF/lib/...              ← 40 y pico de jars: Spring, Tomcat, Jackson...

Main-Class:  org.springframework.boot.loader.launch.JarLauncher
Start-Class: cl.dgt.empaquetado.Lab12Application
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
java -jar target/lab12-empaquetado-0.1.0.jar
```

```
Tomcat started on port 8105 (http)
Started Lab12Application in 1.445 seconds
```

**Sin Maven. Sin código fuente. Sin instalar nada más que Java.** Eso es lo que se despliega.

---

## Paso 2 · Las capas

**Se explica:** el jar de arriba es un solo bloque de 21 MB. Si mañana se corrige una palabra en un
mensaje, se construye otro jar de 21 MB **entero** y hay que moverlo entero.

Y sin embargo, de esos 21 MB, **20,9 no cambiaron**: son Spring, Tomcat y Jackson, exactamente los
mismos de ayer. Lo que cambió son unos pocos kilobytes.

**Se escribe:** en `pom.xml`, dentro del plugin de Spring Boot:

```xml
      <plugin>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-maven-plugin</artifactId>
        <configuration>
          <layers>
            <enabled>true</enabled>
          </layers>
        </configuration>
      </plugin>
```

**Se corre:**

```bash
./mvnw package
java -Djarmode=tools -jar target/lab12-empaquetado-0.1.0.jar list-layers
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

Hoy esto no cambia nada: el jar sigue pesando lo mismo. **Sirve en el paso 4**, cuando cada capa
pase a ser una capa de la imagen — y entonces desplegar una corrección dejará de mover 21 MB para
mover unos KB.

---

## Paso 3 · Qué es un contenedor

**No se teclea nada. Veinte minutos.**

Es la única parte del curso donde se explica algo que el alumno no ha visto nunca, y saltársela
para llegar antes al paso 4 lo convierte en una receta.

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

La imagen está hecha de **capas apiladas**, cada una con los cambios sobre la anterior — de ahí el
paso 2. Y son **de sólo lectura y compartidas**: si diez imágenes usan la misma base de Java, esa
base se guarda **una vez**.

> Ahí está el ahorro real, y es la frase del paso: al desplegar una corrección no se mueven 139 MB.
> Se mueve **la capa que cambió**, que son unos kilobytes. Todo lo demás ya está en el servidor.

### OCI, Docker y por qué no hace falta Docker

**OCI** (*Open Container Initiative*) es el **estándar** de formato de imagen. **Docker** es un
programa que construye y ejecuta imágenes OCI — el más conocido, y no el único.

Hoy se construye una imagen OCI **sin Docker**, con Jib. Y eso importa aquí más que en ningún otro
sitio: en las máquinas del SII no hay Docker y no se puede instalar.

---

## Paso 4 · Construir la imagen

**Se explica:** Jib es un plugin de Maven que escribe una imagen OCI directamente. No necesita
demonio de Docker, ni permisos de administrador, ni un `Dockerfile`.

**Se escribe:** en `pom.xml`, dentro de `<plugins>`:

```xml
      <plugin>
        <groupId>com.google.cloud.tools</groupId>
        <artifactId>jib-maven-plugin</artifactId>
        <version>3.5.2</version>
        <configuration>
          <from>
            <image>eclipse-temurin:25-jre</image>
          </from>
          <to>
            <image>lab12-empaquetado:0.1.0</image>
          </to>
          <container>
            <ports>
              <port>8105</port>
            </ports>
            <environment>
              <SPRING_PROFILES_ACTIVE>prod</SPRING_PROFILES_ACTIVE>
            </environment>
          </container>
        </configuration>
      </plugin>
```

> **La imagen base viaja en el repositorio.** `.mvn/maven.config` de este proyecto contiene
> `-Djib.baseImageCache=../../../tools/jib-base`, y ahí están las capas de `eclipse-temurin:25-jre`
> commiteadas, igual que el JDK y las dependencias de Maven. Sin eso, Jib intentaría bajarla de un
> registro y en el SII esto no funcionaría. Si alguien borra esa carpeta, el error es explícito:
> `Cannot run Jib in offline mode; eclipse-temurin:25-jre not found in local Jib cache`.

**Se corre:**

```bash
./mvnw package jib:buildTar
```

**En consola:**

```
[INFO] Containerizing application to file at 'target/jib-image.tar'...
[INFO] Built image tarball at target/jib-image.tar
[INFO] BUILD SUCCESS
```

**Una imagen de contenedor, sin Docker y sin internet.**

### Y ahora se abre

Una imagen es un tar. Se puede mirar por dentro con las herramientas de siempre:

```bash
mkdir -p /tmp/img && tar -xf target/jib-image.tar -C /tmp/img
ls /tmp/img
cat /tmp/img/manifest.json
```

**En consola:**

```json
[{"Config":"config.json",
  "RepoTags":["lab12-empaquetado:0.1.0"],
  "Layers":["617772c7....tar.gz", "a7fb98a8....tar.gz", ... ]}]
```

Y las capas, con su peso:

```
  capa 1:    39.6 MB     ┐
  capa 3:    20.4 MB     ├─ la base: sistema mínimo + JRE 25
  capa 4:    60.1 MB     ┘
  capa 7:    18.6 MB     ── las dependencias (Spring, Tomcat...)
  capas 2, 5, 6, 8, 9, 10: menos de 0,1 MB cada una  ── NUESTRO CÓDIGO
```

**Aquí se para y se señala.** 120 MB son la base, 18,6 MB las dependencias, y lo nuestro no llega a
0,1 MB. Cuando mañana se corrija un mensaje y se vuelva a construir, **las nueve primeras capas
son idénticas** y no se mueven: se despliegan esos kilobytes. Ese es el paso 2 cobrando.

Y la configuración de la imagen:

```
  arquitectura : amd64 / linux
  Entrypoint   : java -cp @/app/jib-classpath-file cl.dgt.empaquetado.Lab12Application
  puertos      : ['8106/tcp']
  entorno      : ['SPRING_PROFILES_ACTIVE=prod']
```

Tres cosas que notar:

1. **`linux`, aunque esto se construyó en un Mac.** La imagen es para donde va a correr, no para
   donde se construye. (Y `amd64` es el valor por defecto de Jib; se cambia con `<platforms>`.)
2. **No hay `java -jar`.** Jib no mete el fat jar: pone las clases y los jars sueltos, en capas
   separadas. Por eso puede separar «dependencias» de «aplicación».
3. **El puerto y el perfil quedaron escritos en la imagen.** Son valores por defecto — y el paso 5
   va justamente de que se pueden cambiar sin tocarla.

---

## Paso 5 · La misma imagen en todas partes

**Se explica:** queda la pregunta que cierra el curso. Hay tres ambientes —desarrollo, pruebas,
producción— y en cada uno la base de datos es otra, la URL de Tesorería es otra y los tiempos de
espera son otros.

> ¿Se construye una imagen para cada ambiente?

**No.** Y hay que decir por qué, porque el instinto dice que sí:

> Si se construye una imagen para producción **distinta** de la que se probó, entonces **lo que se
> probó no es lo que se despliega**. Todo el trabajo de pruebas queda en suspenso: la imagen que
> llega al usuario nunca la ejecutó nadie.

Un artefacto, todos los ambientes. Lo que cambia va **fuera**.

**Se escribe:** dos archivos de perfil. `resources/application-dev.yml`:

```yaml
lab12:
  saludo: Hola desde DESARROLLO
  tesoreria-url: http://localhost:9098/pagos
```

y `resources/application-prod.yml`:

```yaml
lab12:
  saludo: Hola desde PRODUCCIÓN
  tesoreria-url: ${TESORERIA_URL:https://tesoreria.example.cl/pagos}
```

**Se corre — el mismo jar, tres veces:**

```bash
java -jar target/lab12-empaquetado-0.1.0.jar
java -jar target/lab12-empaquetado-0.1.0.jar --spring.profiles.active=dev
SPRING_PROFILES_ACTIVE=prod TESORERIA_URL=https://tesoreria.sii.cl/api/pagos \
  java -jar target/lab12-empaquetado-0.1.0.jar
```

**En consola:**

```json
{"perfilesActivos":[],      "saludo":"Hola desde el entorno por defecto",
                            "tesoreriaUrl":"http://localhost:9999/tesoreria-falsa"}

{"perfilesActivos":["dev"], "saludo":"Hola desde DESARROLLO",
                            "tesoreriaUrl":"http://localhost:9098/pagos"}

{"perfilesActivos":["prod"],"saludo":"Hola desde PRODUCCIÓN",
                            "tesoreriaUrl":"https://tesoreria.sii.cl/api/pagos"}
```

**El mismo archivo, byte por byte.** No se recompiló nada entre una ejecución y la siguiente.

### El orden de precedencia, que es lo que hay que saberse

De menos a más fuerte — **gana el último**:

```
application.yml  <  application-<perfil>.yml  <  variable de entorno  <  argumento de línea de comandos
```

Se ve en la tercera ejecución: `application-prod.yml` propone una URL de ejemplo, y
`TESORERIA_URL` la pisa. Por eso el valor está escrito como `${TESORERIA_URL:...}`: **lo de después
de los dos puntos es el respaldo**, no el valor.

> **Y aquí va la regla que cierra el laboratorio:** los secretos —claves de base de datos, el
> secreto de firma del Lab 08— **nunca** van en un `application-prod.yml`, porque ese archivo está
> dentro de la imagen y dentro del repositorio. Van por variable de entorno, que las pone el
> orquestador en el momento de arrancar y no quedan escritas en ninguna parte.

---

## Al terminar

`practica/` construye lo mismo que `solucion/`: un jar de 20,9 MB por capas, una imagen OCI de
138,9 MB, y el mismo artefacto comportándose distinto en tres ambientes.

Lo que hay que poder decir con las propias palabras:

> Un fat jar lleva Tomcat dentro y arranca con `java -jar`. Las capas separan lo que cambia
> siempre de lo que no cambia nunca, y por eso un despliegue mueve kilobytes y no megas. Un
> contenedor es un proceso normal al que el núcleo le aisló lo que ve. Y la imagen se construye
> **una vez**: si se recompila para producción, lo que se probó no es lo que se despliega.

### Lo que siembra este lab — y cierra el curso

Este es el último laboratorio, así que lo que se siembra no lo recoge otra sesión: lo recoge quien
vuelva el lunes a su trabajo.

En trece sesiones se armó una aplicación que arranca, expone endpoints, guarda en una base de
datos, no se cae bajo concurrencia, está probada, cerrada con llave, sobrevive a que el vecino
falle, hace su trabajo a tiempo, y hoy sale de la máquina donde nació.

Lo que queda por delante —y hay que nombrarlo para que nadie se vaya creyendo que el mapa está
completo— es **quién cuida eso una vez desplegado**: quién arranca la imagen, cuántas copias,
qué pasa cuando una se cae, dónde se miran las métricas y los registros, y cómo llega una versión
nueva sin cortar el servicio.

> **Lo que se lleva:** hasta hoy, «funciona» quería decir «funciona en mi máquina, ahora, mientras
> yo lo miro». Desde hoy quiere decir otra cosa: **un artefacto que alguien que no eres tú puede
> arrancar en una máquina que no es la tuya, y que sigue funcionando cuando nadie está mirando.**

Y la pregunta que conviene llevarse puesta, la misma que abrió el Lab 07:

> Cuando cambies algo la semana que viene — **¿qué test lo demuestra?**
