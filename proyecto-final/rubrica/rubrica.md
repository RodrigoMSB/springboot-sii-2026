# Rúbrica · Proyecto final

**Vale el 70 % de la nota del curso.** El 30 % restante es la evaluación de conocimientos, que se
rinde aparte.

**Seis criterios, los del temario adjudicado.** Cada uno tiene tres niveles y **un comando que lo
comprueba**: la corrección no es una impresión, es una salida de consola.

> ## El umbral
>
> **Se aprueba con «suficiente» en los SEIS criterios.**
>
> No hay compensación entre ellos: un «bien» en pruebas no cubre un «insuficiente» en seguridad.
> Son seis cosas distintas y el trabajo tiene que estar hecho en las seis.

| # | Criterio | Peso |
|---|---|---|
| 1 | Arquitectura y diseño | **20 %** |
| 2 | Correctitud | **20 %** |
| 3 | Pruebas | **20 %** |
| 4 | Seguridad | **15 %** |
| 5 | Persistencia y rendimiento | **15 %** |
| 6 | Observabilidad y despliegue | **10 %** |

---

## Antes de corregir · dejar el proyecto en marcha

```bash
cd base                     # la carpeta que entregó el alumno
./mvnw test                 # criterio 3
./mvnw spring-boot:run      # queda corriendo, en el 8107

# y en otra terminal, el token de fiscalizador:
TOKEN=$(curl -s -X POST localhost:8107/auth/login \
  -H 'Content-Type: application/json' \
  -d '{"usuario":"ana","clave":"dgt2026"}' | sed 's/.*"token":"\([^"]*\)".*/\1/')
```

---

## 1 · Arquitectura y diseño · 20 %

Que cada pieza esté donde le toca y no sepa de más.

| | |
|---|---|
| **Insuficiente** | Toda la lógica en el controller, o el repositorio llamado desde él. Devuelve la entidad en vez de un DTO |
| **Suficiente** | Controller → servicio → repositorio, cada uno en su paquete. DTO propio. Dependencias por constructor |
| **Bien** | Además: el controller no tiene ni un `if` de negocio, el DTO es un `record` inmutable, y los nombres de fuera y de dentro pueden cambiar por separado |

**Cómo se comprueba:**

```bash
# el controller delega y no calcula
cat src/main/java/cl/dgt/consolidado/controllers/ConsolidadoController.java

# y NO devuelve la entidad
grep -r "Contribuyente\b" src/main/java/cl/dgt/consolidado/controllers/
```

> **El DTO es una lista blanca**, y es el criterio que más se cae: si devuelve la entidad, cualquier
> columna que alguien añada mañana se publica sola. Ese es exactamente el incidente que el
> requerimiento quería evitar.

---

## 2 · Correctitud · 20 %

Que responda lo que el brief dice, en los seis casos.

| | |
|---|---|
| **Insuficiente** | Falla alguno de los cuatro `curl`, o el total está mal, o salen campos que el brief no pide |
| **Suficiente** | Los cuatro `curl` dan 401, 403, 404 y 200. El total cuadra. Los campos son exactamente los seis |
| **Bien** | Además: distingue 404 de lista vacía, y el 400 sale con cuerpo cuando falta un parámetro |

**Cómo se comprueba — los cuatro `curl` de la corrección:**

```bash
# 1 · sin token  ->  401
curl -s -o /dev/null -w "%{http_code}\n" \
  "localhost:8107/consolidados/76.111.111-1?desde=2026-01-01&hasta=2026-12-31"

# 2 · token de CONTRIBUYENTE  ->  403
TC=$(curl -s -X POST localhost:8107/auth/login -H 'Content-Type: application/json' \
     -d '{"usuario":"luis","clave":"dgt2026"}' | sed 's/.*"token":"\([^"]*\)".*/\1/')
curl -s -o /dev/null -w "%{http_code}\n" -H "Authorization: Bearer $TC" \
  "localhost:8107/consolidados/76.111.111-1?desde=2026-01-01&hasta=2026-12-31"

# 3 · RUT que no existe  ->  404 con cuerpo
curl -s -w " [%{http_code}]\n" -H "Authorization: Bearer $TOKEN" \
  "localhost:8107/consolidados/99.999.999-9?desde=2026-01-01&hasta=2026-12-31"

# 4 · el caso bueno  ->  200, y el total tiene que ser 6330000
curl -s -H "Authorization: Bearer $TOKEN" \
  "localhost:8107/consolidados/76.111.111-1?desde=2026-01-01&hasta=2026-12-31"
```

**El número contra los datos sembrados.** Para `76.111.111-1` en 2026 hay cuatro trámites:

```
1.200.000 + 950.000 + 3.400.000 + 780.000 = 6.330.000
```

Los 3.400.000 son de un trámite **PENDIENTE** y suman igual: si el total sale **2.930.000**, el
alumno filtró por estado y eso es **insuficiente** en este criterio.

**Y los dos bordes:**

```bash
# contribuyente sin trámites  ->  200 con lista vacía y total 0
curl -s -H "Authorization: Bearer $TOKEN" \
  "localhost:8107/consolidados/78.333.333-3?desde=2026-01-01&hasta=2026-12-31"

# falta un parámetro  ->  400 con cuerpo
curl -s -w " [%{http_code}]\n" -H "Authorization: Bearer $TOKEN" \
  "localhost:8107/consolidados/76.111.111-1?desde=2026-01-01"
```

---

## 3 · Pruebas · 20 %

Que haya dos tests y que prueben algo.

| | |
|---|---|
| **Insuficiente** | No hay tests, no compilan, o alguno está rojo. O prueban que `1 == 1` |
| **Suficiente** | Los dos que pide el brief, en verde: servicio con `@Mock` comprobando el total, controller con `MockMvc` comprobando el 404 |
| **Bien** | Además: el test del servicio se pone rojo si se filtra por estado, y compara `BigDecimal` con `compareTo` y no con `equals` |

**Cómo se comprueba:**

```bash
./mvnw test
```

Tienen que salir **al menos tres** en verde: el de contexto que ya venía, y los dos del encargo.

**Y la comprobación que de verdad separa:** romper el código y ver si el test avisa.

```bash
# en ConsolidadoService, filtrar el total sólo por PAGADO, y correr otra vez:
./mvnw test          # el test del servicio TIENE que ponerse rojo
```

Si sigue verde, el test no prueba lo que dice probar. Es **insuficiente** aunque estuviera verde
antes.

> **Testcontainers no se exige**, y no es un olvido: las máquinas de la sala no tienen Docker. Los
> tests sobre el **PostgreSQL embebido** que trae `base/` valen exactamente igual para esta rúbrica.

---

## 4 · Seguridad · 15 %

Que sólo el fiscalizador vea el consolidado.

| | |
|---|---|
| **Insuficiente** | El endpoint responde sin token, o responde a un CONTRIBUYENTE. O el 403 sale como 401 |
| **Suficiente** | Sin token 401, con token de CONTRIBUYENTE 403, con FISCALIZADOR 200. La regla está en la cadena de filtros |
| **Bien** | Además: no hay ningún `if` de rol dentro del controller ni del servicio — la autorización vive en un solo sitio |

**Cómo se comprueba:** los `curl` 1 y 2 del criterio 2, y:

```bash
grep -rn "FISCALIZADOR" src/main/java/
```

Tiene que aparecer en `SeguridadConfig` y **en ningún otro sitio**.

> **401 y 403 no son lo mismo**, y confundirlos baja este criterio: 401 es «no sé quién eres», 403
> es «sé quién eres y esto no te toca».

---

## 5 · Persistencia y rendimiento · 15 %

Que la consulta sea una, no una por fila.

| | |
|---|---|
| **Insuficiente** | Trae el contribuyente y recorre `getTramites()` filtrando en Java. O dispara un SELECT por trámite |
| **Suficiente** | Una consulta con el filtro por RUT y fechas hecho **en la base** |
| **Bien** | Además: `join fetch` donde hace falta, `between` para el rango y un `order by` explícito |

**Cómo se comprueba** — encender el SQL y contar:

```bash
# en application.yml:  spring.jpa.show-sql: true
./mvnw spring-boot:run
# y una petición:
curl -s -H "Authorization: Bearer $TOKEN" \
  "localhost:8107/consolidados/76.111.111-1?desde=2026-01-01&hasta=2026-12-31" > /dev/null
```

En la consola tiene que salir **un `select` de trámites**, no cuatro. Es el N+1 del Lab 06 con otro
disfraz.

---

## 6 · Observabilidad y despliegue · 10 %

Que se pueda operar y que salga de la máquina.

| | |
|---|---|
| **Insuficiente** | `jib:buildTar` falla, o el health no responde |
| **Suficiente** | La imagen se construye, `/actuator/health` responde `UP`, y el contador se mueve |
| **Bien** | Además: el endpoint aparece en `/swagger-ui.html` sin que se haya anotado nada |

**Cómo se comprueba:**

```bash
curl -s localhost:8107/actuator/health

# el contador, después de haber pedido algún consolidado
curl -s -H "Authorization: Bearer $TOKEN" \
  localhost:8107/actuator/metrics/dgt.consolidados.emitidos

# la documentación
curl -s -o /dev/null -w "%{http_code}\n" -L localhost:8107/swagger-ui.html

# y la imagen
./mvnw package jib:buildTar
ls -lh target/jib-image.tar
```

El contador y Swagger **vienen dados** en `base/`: lo único que el alumno pone es la línea
`contador.emitidos().increment()`. Si el contador está en 0 después de varias peticiones, esa línea
falta.

---

## La solución de referencia

`instructor/solucion-referencia/` tiene **una** solución completa, no *la* solución. Si la del
alumno difiere y él la defiende, puede estar igual de bien o mejor — salvo en lo que el brief fija
de forma cerrada, que no admite variantes: las rutas, los códigos de estado, los campos que salen y
el criterio del total.
