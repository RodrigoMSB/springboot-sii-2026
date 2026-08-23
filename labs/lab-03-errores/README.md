# Lab 03 · Errores con forma

Que el camino triste también tenga contrato.

En el Lab 01 apareció un `404` con **cero bytes de cuerpo**. En el Lab 02 volvió a aparecer. Hoy
se arregla — y de paso se arregla el problema contrario, que es peor: el error que **cuenta
demasiado**.

## Qué se aprende

- Que un error previsto —«ese id no existe»— **no es un fallo del sistema**, y no puede
  responderse igual que uno.
- A escribir una **excepción propia** que no sabe nada de HTTP.
- Qué es un `@RestControllerAdvice`: **un solo sitio** donde se traduce cualquier excepción a
  respuesta HTTP, para todos los controllers.
- A validar lo que entra con `@Valid`, y a devolver **qué campo** falló y por qué.
- Por qué el mensaje interno **nunca** sale por la API, y dónde sí tiene que quedar registrado.

## Los dos directorios

| | |
|---|---|
| **`practica/`** | Donde trabajas. **El controller viene dado y ya funciona**: hoy no se escriben endpoints. Lo que falta es `exceptions/`, que llega vacío. |
| **`solucion/`** | El mismo proyecto, con el manejador de errores completo. |

Los dos son proyectos completos y arrancan solos.

> **`models/` y `entities/` no son lo mismo, y por eso no se llaman igual.** Lo que hay en
> `models/` es un objeto que vive **en memoria**: nace, se usa y se muere con el proceso. No hay
> tabla detrás ni JPA de por medio. Desde el Lab 04, cuando aparezca una base de datos de verdad,
> el paquete se llamará `entities/` — y ese cambio de nombre es la señal de que la clase ya no es
> un objeto suelto, sino **una fila**.

## Cómo se corre

```bash
cd practica          # o solucion
./mvnw spring-boot:run
```

**La aplicación se queda corriendo. Se apaga con Ctrl+C.**

| | puerto |
|---|---|
| `practica/` | **8085** |
| `solucion/` | **8086** |

## Los cuatro endpoints

Vienen dados. No se escribe ninguno: se le da forma a lo que devuelven cuando algo sale mal.

```
GET  /productos                        el catálogo — nunca falla
GET  /productos/{id}                   ¿y si el id no existe?          ← pasos 1, 2 y 3
GET  /productos/{id}/cuota?cuotas=N    ¿y si N es 0?                   ← paso 5
POST /productos                        ¿y si el nombre viene vacío?    ← paso 4
```

## Lo que hay que ver el primer minuto

Con `practica/` corriendo:

```bash
curl -s http://localhost:8085/productos/99
```

Sale un **500** con la traza entera dentro del JSON: la clase de la excepción, el archivo, el
número de línea, y cuarenta llamadas de Spring y de Tomcat. Ese es el punto de partida del
laboratorio, y hay que mirarlo despacio antes de arreglarlo.

> **`practica/` viene con la filtración encendida a propósito.** En su `application.yml` hay tres
> ajustes bajo `spring.web.error` que hacen visible lo que normalmente queda oculto. **Se quitan
> en el paso 5**, y `solucion/` no los tiene.

## Lo que devuelve al terminar

```
GET  /productos/99                  404  {"mensaje":"No existe el producto con id 99.","codigo":404,...}
GET  /productos/1/cuota?cuotas=0    500  {"mensaje":"Ocurrió un error inesperado. Inténtalo más tarde.",...}
POST /productos  {"nombre":""}      400  {"mensaje":"Hay datos inválidos...","campos":{"nombre":"..."}}
GET  /noexiste                      404  {"mensaje":"La ruta pedida no existe.","codigo":404,...}
```

Cuatro situaciones distintas, **una sola forma de error**. Eso es lo que hace que la API se pueda
consumir.

## El guion

`PASOS.md` — los cinco pasos de la sesión, con qué escribir en cada uno y qué debe responder el
servidor.
