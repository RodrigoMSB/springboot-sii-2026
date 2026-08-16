# Lab 00 · Hola mundo

Que una aplicación Spring Boot arranque, y que lo primero que imprima lo hayas escrito tú.

Es el laboratorio más corto del curso —quince minutos— y para quien llega en cero es el más
importante: es la primera vez que ve algo suyo funcionando. Todo lo que venga después se cuelga
de lo que se ve hoy.

## Qué se aprende

- Que una aplicación Spring Boot es **una clase con una anotación y un `main`**. Nada más.
- Que arrancarla no es correr un `main` pelado: arranca un **contenedor** que después hace cosas
  por ti.
- Que hay un archivo de configuración (`application.yml`) donde se cambian cosas **sin
  recompilar**.
- A leer un `pom.xml` por encima: qué es el `parent`, qué es una dependencia, qué es un plugin.

## Los dos directorios

| | |
|---|---|
| **`practica/`** | Donde trabajas. Está incompleto a propósito: el método que imprime llega vacío. |
| **`solucion/`** | El mismo proyecto, terminado. Para comparar si algo no sale. |

Los dos son proyectos completos y arrancan solos.

## Cómo se corre

```bash
cd practica          # o solucion
./mvnw spring-boot:run
```

No hace falta instalar nada: Java y Maven viajan dentro del repositorio.

Esta aplicación **arranca, imprime y termina sola**. No se queda corriendo, porque todavía no
hay nada que atienda peticiones — eso llega en el Lab 01.

## Lo primero que vas a ver

```
  .   ____          _            __ _ _
 /\\ / ___'_ __ _ _(_)_ __  __ _ \ \ \ \
( ( )\___ | '_ | '_| | '_ \/ _` | \ \ \ \
 \\/  ___)| |_)| | | | | || (_| |  ) ) ) )
  '  |____| .__|_| |_|_| |_\__, | / / / /
 =========|_|==============|___/=/_/_/_/

 :: Spring Boot ::                (v4.1.0)
```

Ese dibujo es el **banner** de Spring Boot. Si aparece, la mitad de la batalla está ganada: Java
está bien, Maven está bien, y el proyecto compiló.

> **Antes del banner salen cuatro líneas que empiezan con `WARNING:` y hablan de
> `sun.misc.Unsafe`.** No son tuyas ni son un error: las escribe Maven al arrancar sobre Java 25.
> Se ignoran.

## El guion

`PASOS.md` — los cuatro pasos de la sesión, con qué escribir en cada uno y qué debe aparecer en
la consola.
