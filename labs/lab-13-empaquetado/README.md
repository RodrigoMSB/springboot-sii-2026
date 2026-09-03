# Lab 13 · Empaquetado

El único laboratorio sobre **salir de la máquina propia** — y el que deja lista la pieza que el
Lab 14 va a repartir en cuatro.

Hasta hoy todo se corrió con `./mvnw spring-boot:run`. Eso no existe en producción: en producción
hay un artefacto que alguien construyó una vez y que arranca solo. Hoy se construye ese artefacto,
se mira por dentro, y se despliega tres veces sin recompilarlo.

## Los números del laboratorio

| | |
|---|---|
| El jar ejecutable | **21 MB**, un solo archivo |
| Sus capas | **4**: `dependencies`, `spring-boot-loader`, `snapshot-dependencies`, `application` |
| Lo que cambia al corregir una línea de código | sólo la capa `application` |

Ese último renglón es el paso 2: **lo que cambia en cada despliegue son unos pocos KB**, y por eso
las capas importan.

## Qué se aprende

- Qué es un **jar ejecutable**, qué lleva dentro, y por qué `java -jar` basta para arrancarlo.
- El **jar por capas**: separar lo que cambia todos los días de lo que no cambia nunca.
- **Qué es un contenedor**, explicado desde cero — en qué se diferencia de una máquina virtual y
  qué problema resuelve de verdad.
- Por qué **el artefacto no se reconstruye para cada ambiente**, y cómo el mismo jar se comporta
  distinto en dev y en prod.
- Y la regla que cierra el día: **los secretos no viajan dentro del artefacto.**

## Las tres carpetas

| | |
|---|---|
| **`practica/`** | Donde trabajas. Sin capas y sin perfiles |
| **`solucion/`** | El jar por capas y los tres perfiles |
| **`instructor/`** | Los mismos archivos explicados línea por línea. **No viaja en el repositorio** |

## Cómo se corre

```bash
cd practica          # o solucion
./mvnw package                                    # construye el jar
java -jar target/lab13-empaquetado-0.1.0.jar      # en solucion/, el jar se llama
                                                  # lab13-empaquetado-solucion-0.1.0.jar
```

| | puerto |
|---|---|
| `practica/` | **8105** |
| `solucion/` | **8106** |

## El endpoint

```
GET /donde-estoy    qué perfil está activo, qué configuración ganó y con qué Java corre
```

## El paso que hay que llegar a hacer

El **paso 4**. El mismo jar, sin recompilar nada, arrancado tres veces:

```
$ java -jar app.jar
{"perfilesActivos":[],"saludo":"Hola desde el entorno por defecto",
 "tesoreriaUrl":"http://localhost:9999/tesoreria-falsa","javaVersion":"25.0.4"}

$ java -jar app.jar --spring.profiles.active=dev
{"perfilesActivos":["dev"],"saludo":"Hola desde DESARROLLO",
 "tesoreriaUrl":"http://localhost:9098/pagos","javaVersion":"25.0.4"}

$ TESORERIA_URL=https://tesoreria.sii.cl/pagos java -jar app.jar --spring.profiles.active=prod
{"perfilesActivos":["prod"],"saludo":"Hola desde PRODUCCIÓN",
 "tesoreriaUrl":"https://tesoreria.sii.cl/pagos","javaVersion":"25.0.4"}
```

**El mismo archivo, byte por byte.** Lo que cambia está fuera. Ese es el principio que hace posible
probar en un ambiente y desplegar en otro con alguna confianza: si se reconstruyera para
producción, lo que se probó no sería lo que se despliega.

Y en la tercera corrida se ve la **precedencia**: el perfil `prod` trae su propia URL de Tesorería
y la variable de entorno la pisa. Las fuentes de configuración se apilan, y gana la de fuera.

## Lo que no vimos hoy

- **Construir la imagen.** El paso 3 explica qué es un contenedor y no lo construye, porque las
  máquinas de la sala no tienen Docker. Para quien quiera probarlo fuera del curso: **Jib**
  (`jib-maven-plugin`) construye una imagen OCI **sin demonio Docker** — escribe el `.tar`
  directamente— y el jar por capas del paso 2 es exactamente lo que Jib necesita para convertir
  cada capa del jar en una capa de la imagen. Es lo que hace el proyecto final.
- **Kubernetes** y los orquestadores: quién arranca esa imagen, cuántas copias, y qué hace cuando
  una se cae.
- **Registries**: dónde se guardan las imágenes y cómo llegan al servidor que las va a correr.
- **CI/CD**: que todo esto lo haga una máquina en cada cambio, en vez de una persona.

## El guion

`PASOS.md` — los cuatro pasos de la sesión.
