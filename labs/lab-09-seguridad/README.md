# Lab 09 · Seguridad

La API del laboratorio anterior funcionaba. Y cualquiera podía llamarla.

Hoy se cierra: quién eres, cómo lo demuestras en cada petición, y qué puedes hacer una vez
dentro. Al terminar, las mismas rutas de siempre responden **401** al que no se identifica y
**403** al que se identificó pero no le corresponde.

## Qué se aprende

- Que Spring Security, al entrar, **cierra todo por defecto** — y por qué ese default es el
  correcto.
- Qué es una **cadena de filtros** y cómo se declara qué ruta es pública y cuál no.
- Por qué una contraseña **nunca** se guarda, y qué guarda **Argon2id** en su lugar.
- Qué hay dentro de un **JWT**, por qué **cualquiera puede leerlo**, y qué garantiza entonces
  la firma.
- Cómo se valida un token en cada petición, qué pasa con uno manipulado y qué pasa con uno
  **vencido**.
- La diferencia entre **401 y 403**, con las dos peticiones que las provocan.

## Las tres carpetas

| | |
|---|---|
| **`practica/`** | Donde trabajas. La API llega **abierta**; `config/`, `services/` y `soporte/` llegan vacías |
| **`solucion/`** | El mismo proyecto cerrado, con login, token, filtro y roles |
| **`instructor/`** | Los mismos archivos explicados línea por línea. **No viaja en el repositorio** |

> **Este lab tiene los dos paquetes, y no es un descuido.** `entities/Usuario` está **mapeada a
> una tabla**: es una fila de la base. `models/Producto` vive **en memoria** y muere con el
> proceso. Misma aplicación, dos clases de objeto distintas, y por eso dos nombres distintos.

## Cómo se corre

```bash
cd practica          # o solucion
./mvnw spring-boot:run
```

| | HTTP | PostgreSQL |
|---|---|---|
| `practica/` | **8095** | **55440** |
| `solucion/` | **8096** | **55441** |

La base la levanta la propia aplicación como proceso hijo — **sin Docker y sin instalar nada**,
igual que en el Lab 04. Se puede mirar con DBeaver mientras corre, y en el paso 3 hay que
hacerlo.

## Los dos usuarios de la semilla

```
ana   / dgt2026   ADMIN
luis  / dgt2026   USUARIO
```

**La misma clave a propósito.** El paso 3 mira la tabla y encuentra dos hashes distintos —los dos
empiezan por `$argon2id$` y no se parecen en nada.

## La matriz al terminar

Cinco peticiones, y son la prueba de aceptación del laboratorio:

| # | petición | quién | respuesta |
|---|---|---|---|
| 1 | `GET /productos` | sin token | **401** |
| 2 | `GET /productos` | ana (ADMIN) | 200 |
| 3 | `GET /productos` | luis (USUARIO) | 200 |
| 4 | `GET /productos/administracion` | ana (ADMIN) | 200 |
| 5 | `GET /productos/administracion` | luis (USUARIO) | **403** |

Las dos negritas son el laboratorio: **401 es «no sé quién eres», 403 es «sé quién eres y no
te toca»**.

## El paso que hay que llegar a hacer

El **paso 5**. Se sacan los dos tokens, se parten por los puntos y se decodifica el trozo del
medio — sin clave, sin permiso, sin nada:

```json
{ "iss": "lab09", "sub": "ana",  "exp": 1787071711, "scope": "ROLE_ADMIN" }
{ "iss": "lab09", "sub": "luis", "exp": 1787071713, "scope": "ROLE_USUARIO" }
```

**Está a la vista.** Un JWT no oculta: garantiza. Lo que la firma impide no es leerlo, es
**cambiarlo**. Quien se lleve esa idea del día no meterá nunca un dato sensible en un token.

Y ahí está, además, la explicación de las dos últimas filas de la matriz: ese `scope` es lo que
la cadena de filtros lee para decidir el 200 y el 403.

## Lo que no vimos hoy

- **OAuth2 y OpenID Connect**: delegar la identidad en un tercero (Google, el ClaveÚnica del
  Estado). Es la forma habitual en producción y da para una sesión propia.
- **Refresh tokens**: el token de hoy dura 30 minutos —`lab09.jwt.vigencia-segundos` en el yml—
  y cuando expira se vuelve a hacer login. En producción hay un segundo token para renovar sin
  volver a pedir la clave.
- **Seguridad a nivel de método** (`@PreAuthorize` con expresiones sobre el objeto: «puedes
  editar este producto sólo si es tuyo»). Hoy se protege por ruta, que es el 80 % de los casos.

## El guion

`PASOS.md` — los cinco pasos de la sesión.
