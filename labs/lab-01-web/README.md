# Lab 01 · Web — el primer endpoint

Que un método escrito por el alumno conteste desde el navegador.

En el Lab 00 la aplicación arrancó, imprimió y se murió. Hoy se le agrega **una línea al
`pom.xml`** y deja de morirse: levanta un servidor y se queda esperando a que alguien pregunte.

## Qué se aprende

- Que **un endpoint es un método anotado**. No hay que registrar rutas en ninguna parte.
- Las tres formas en que un dato entra a la aplicación: en la **ruta**, después del **`?`**, y
  en el **cuerpo** de la petición.
- Que el JSON **no se arma a mano**: se devuelve un objeto Java y alguien lo convierte.
- Que el **código de estado** (200, 201, 404) también es parte de la respuesta.

## Los dos directorios

| | |
|---|---|
| **`practica/`** | Donde trabajas. `controllers/` y `dto/` llegan **vacíos**: todo lo de hoy lo escribes tú. |
| **`solucion/`** | El mismo proyecto, terminado, con los seis endpoints. |

Los dos son proyectos completos y arrancan solos.

## Cómo se corre

```bash
cd practica          # o solucion
./mvnw spring-boot:run
```

A diferencia del Lab 00, **la aplicación se queda corriendo**. Es lo que tiene que pasar: está
esperando peticiones. **Se apaga con Ctrl+C.**

| | puerto |
|---|---|
| `practica/` | **8081** |
| `solucion/` | **8082** |

Son distintos a propósito: se pueden tener los dos corriendo a la vez, en dos terminales, para
comparar.

Cuando arranque, la consola lo dice:

```
... INFO --- [lab01-web] [main] o.s.boot.tomcat.TomcatWebServer : Tomcat started on port 8081 (http) with context path '/'
... INFO --- [lab01-web] [main] cl.dgt.web.Lab01Application     : Started Lab01Application in 0.776 seconds
```

## Cómo se prueba

Los `GET` se prueban **desde el navegador**, escribiendo la URL en la barra:

```
http://localhost:8081/hola
```

El `POST` del paso 5 no se puede probar así —el navegador solo sabe hacer GET desde la barra—,
y para eso está **Postman**. En el guion va también el `curl` equivalente, por si se prefiere la
terminal:

```bash
curl -i -X POST http://localhost:8081/saludos \
     -H 'Content-Type: application/json' \
     -d '{"nombre":"Carolina","formal":true}'
```

El `-i` es importante: hace que `curl` muestre también la **primera línea con el código de
estado**, que en el paso 6 es justo lo que se está mirando.

## Los endpoints al terminar

```
GET  /hola                              texto
GET  /hola/{nombre}                     texto, el dato en la ruta
GET  /saludo?nombre=X&formal=true       texto, el dato tras el ?
GET  /saludos/{nombre}                  JSON — 404 si no está en la agenda
POST /saludos                           JSON, responde 201
```

## El guion

`PASOS.md` — los seis pasos de la sesión, con qué escribir en cada uno y qué debe responder el
servidor.
