# Lab 12 · Empaquetado

El último laboratorio del curso, y el único sobre **salir de la máquina propia**.

Hasta hoy todo se corrió con `./mvnw spring-boot:run`. Eso no existe en producción: en producción
hay un artefacto que alguien construyó una vez y que arranca solo. Hoy se construye ese artefacto,
dos veces: como jar y como **imagen de contenedor**.

## Los números del laboratorio

| | tamaño |
|---|---|
| El jar ejecutable | **20,9 MB** |
| La imagen OCI completa | **138,9 MB** |
| — de los cuales, la base con el JRE | 120,1 MB en 3 capas |
| — y el código propio de la aplicación | **menos de 0,1 MB** |

Ese último renglón es el paso 2: **lo que cambia en cada despliegue son unos pocos KB**, y por eso
las capas importan.

## Qué se aprende

- Qué es un **jar ejecutable**, qué lleva dentro, y por qué `java -jar` basta para arrancarlo.
- El **jar por capas**: separar lo que cambia todos los días de lo que no cambia nunca.
- **Qué es un contenedor**, explicado desde cero — en qué se diferencia de una máquina virtual y
  qué problema resuelve de verdad.
- Construir una **imagen OCI con Jib**, sin Docker instalado y sin salir a internet, y mirarla por
  dentro.
- Por qué **la imagen no se recompila para cada ambiente**, y cómo la misma imagen se comporta
  distinto en dev y en prod.

## Sin Docker. De verdad

Este laboratorio construye una imagen de contenedor **sin tener Docker instalado**. Jib no necesita
demonio: escribe el `.tar` de la imagen directamente.

Y funciona **sin internet**, que es lo que hace falta en el SII: la imagen base
(`eclipse-temurin:25-jre`) viaja dentro del repositorio, en `tools/jib-base/`, igual que viajan el
JDK y las dependencias de Maven. Sin eso, Jib intentaría bajarla de un registro y el laboratorio
se caería en la primera orden.

> Lo que **no** se puede hacer sin Docker es **ejecutar** la imagen. Se construye, se abre y se
> inspecciona; correrla queda para quien tenga un motor de contenedores. La aplicación se ejecuta
> igualmente con `java -jar`, que es lo que se hace en el paso 1.

## Las tres carpetas

| | |
|---|---|
| **`practica/`** | Donde trabajas. Sin capas, sin Jib y sin perfiles |
| **`solucion/`** | El jar por capas, Jib configurado y los tres perfiles |
| **`instructor/`** | Los mismos archivos explicados línea por línea. **No viaja en el repositorio** |

## Cómo se corre

```bash
cd practica          # o solucion
./mvnw package                       # construye el jar
java -jar target/lab12-empaquetado-0.1.0.jar
./mvnw package jib:buildTar          # construye la imagen OCI
```

| | puerto |
|---|---|
| `practica/` | **8105** |
| `solucion/` | **8106** |

## El endpoint

```
GET /donde-estoy    qué perfil está activo, qué configuración ganó, y si va en contenedor
```

## El paso que hay que llegar a hacer

El **paso 5**. El mismo jar, sin recompilar nada, arrancado tres veces:

```
$ java -jar app.jar
{"perfilesActivos":[], "saludo":"Hola desde el entorno por defecto"}

$ java -jar app.jar --spring.profiles.active=dev
{"perfilesActivos":["dev"], "saludo":"Hola desde DESARROLLO"}

$ SPRING_PROFILES_ACTIVE=prod TESORERIA_URL=https://tesoreria.sii.cl/api/pagos java -jar app.jar
{"perfilesActivos":["prod"], "tesoreriaUrl":"https://tesoreria.sii.cl/api/pagos"}
```

**El mismo archivo, byte por byte.** Lo que cambia está fuera. Ese es el principio que hace posible
probar en un ambiente y desplegar en otro con alguna confianza: si se recompilara para producción,
lo que se probó no sería lo que se despliega.

## Lo que no vimos hoy

- **Kubernetes** y los orquestadores: quién arranca esa imagen, cuántas copias, y qué hace cuando
  una se cae.
- **Registries**: dónde se guardan las imágenes y cómo llegan al servidor que las va a correr.
- **CI/CD**: que todo esto lo haga una máquina en cada cambio, en vez de una persona.

Los tres son el paso siguiente natural, y ninguno cabe hoy.

## El guion

`PASOS.md` — los cinco pasos de la sesión.
