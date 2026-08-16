# Lab 02 · Inyección de dependencias

Quién construye los objetos, si en el código no hay ni un `new`.

Este es **el laboratorio que explica qué es Spring**. Todo lo anterior se podía hacer sin
framework; lo de hoy es la razón de que exista. Si al final del día hay que quedarse con una
sola idea del curso, es esta.

## Qué se aprende

- Que hay un **contenedor** que, al arrancar, encuentra las clases anotadas, las construye una
  sola vez, y se las entrega a quien las declare.
- Que **el constructor es una declaración de necesidades**: «para funcionar necesito esto».
- Que si el contrato es una interfaz, la implementación se puede cambiar **sin tocar a quien la
  usa**.
- Qué pasa cuando hay **dos candidatos** para el mismo hueco, y las dos formas de resolverlo:
  `@Primary` y `@Qualifier`.
- Por qué se pone una **capa de servicio** en medio, incluso cuando todavía no hace nada.

## Los dos directorios

| | |
|---|---|
| **`practica/`** | Donde trabajas. `models/`, `repositories/`, `services/` y `controllers/` llegan **vacíos**. |
| **`solucion/`** | El mismo proyecto, terminado, con las dos implementaciones y la capa de servicio. |

Los dos son proyectos completos y arrancan solos.

## Cómo se corre

```bash
cd practica          # o solucion
./mvnw spring-boot:run
```

**La aplicación se queda corriendo. Se apaga con Ctrl+C.**

| | puerto |
|---|---|
| `practica/` | **8083** |
| `solucion/` | **8084** |

## Los endpoints al terminar

```
GET /productos          el catálogo
GET /productos/quien    qué implementación quedó inyectada   ← el endpoint del laboratorio
GET /productos/{id}     uno, o 404
```

El del medio es el que importa. No devuelve datos del negocio: devuelve **el nombre de la clase
que Spring eligió y construyó**. Cambiando una anotación en un archivo, esa respuesta cambia —
y ni el controller ni el servicio se tocan.

```bash
curl http://localhost:8084/productos/quien
ProductoRepositoryLista
```

## El paso que hay que llegar a hacer

En el **paso 4** la aplicación **deja de arrancar**, a propósito. Ese error es el contenido del
laboratorio, no un accidente: hay que leerlo entero, en voz alta, y ver que Spring dice
exactamente qué pasa y qué se puede hacer.

Que falle **al arrancar** y no en producción es la mitad del valor de todo esto.

## El guion

`PASOS.md` — los seis pasos de la sesión, con qué escribir en cada uno y qué debe responder el
servidor.
