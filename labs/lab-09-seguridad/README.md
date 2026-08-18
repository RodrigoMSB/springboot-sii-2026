# Lab 09 · Seguridad

La API del laboratorio anterior funcionaba. Y cualquiera podía llamarla.

Hoy se cierra: quién eres, cómo lo demuestras en cada petición, y qué puedes hacer una vez
dentro. Al terminar, las mismas rutas de siempre responden **401** al que no se identifica y
**403** al que se identificó pero no le corresponde.

## Qué se aprende

- Que Spring Security, al entrar, **cierra todo por defecto** — y por qué ese default es el
  correcto.
- Qué es una **cadena de filtros** y cómo se declara qué ruta es pública y cuál no.
- Por qué una contraseña **nunca** se guarda, y qué guarda **BCrypt** en su lugar.
- Qué hay dentro de un **JWT**, por qué **cualquiera puede leerlo**, y qué garantiza entonces
  la firma.
- Cómo se valida un token en cada petición, y qué pasa con uno manipulado.
- La diferencia entre **401 y 403**, con las dos peticiones que las provocan.

## Las tres carpetas

| | |
|---|---|
| **`practica/`** | Donde trabajas. La API llega **abierta**; `seguridad/` y `services/` llegan vacías |
| **`solucion/`** | El mismo proyecto cerrado, con login, token, filtro y roles |
| **`instructor/`** | Los mismos archivos explicados línea por línea. **No viaja en el repositorio** |

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
ana   / secreta   ADMIN
luis  / secreta   USUARIO
```

**La misma clave a propósito.** El paso 3 mira la tabla y encuentra dos hashes distintos.

## La matriz al terminar

| petición | quién | respuesta |
|---|---|---|
| `GET /productos` | sin token | **401** |
| `GET /productos` | ana o luis | 200 |
| `GET /productos/administracion` | ana (ADMIN) | 200 |
| `GET /productos/administracion` | luis (USUARIO) | **403** |
| `GET /productos` | token con firma falsa | **401** |
| `POST /auth/login` | clave equivocada | **401** |

Las dos negritas son el laboratorio: **401 es «no sé quién eres», 403 es «sé quién eres y no
te toca»**.

## El paso que hay que llegar a hacer

El **paso 4**. Se saca el token recién emitido, se parte por los puntos y se decodifica el trozo
del medio — sin clave, sin permiso, sin nada:

```json
{ "iss": "lab08", "sub": "ana", "exp": 1787071711, "scope": "ROLE_ADMIN" }
```

**Está a la vista.** Un JWT no oculta: garantiza. Lo que la firma impide no es leerlo, es
**cambiarlo**. Quien se lleve esa idea del día no meterá nunca un dato sensible en un token.

## Lo que no vimos hoy

- **OAuth2 y OpenID Connect**: delegar la identidad en un tercero (Google, el ClaveÚnica del
  Estado). Es la forma habitual en producción y da para una sesión propia.
- **Refresh tokens**: el token de hoy dura 30 minutos y cuando expira se vuelve a hacer login.
  En producción hay un segundo token para renovar sin volver a pedir la clave.
- **Seguridad a nivel de método** (`@PreAuthorize` con expresiones sobre el objeto: «puedes
  editar este producto sólo si es tuyo»). Hoy se protege por ruta, que es el 80 % de los casos.

## El guion

`PASOS.md` — los seis pasos de la sesión.
