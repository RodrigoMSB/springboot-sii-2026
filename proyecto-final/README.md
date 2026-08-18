# Proyecto final · Consolidado del contribuyente

**Esto no es un laboratorio.** Es el instrumento con el que se evalúa el curso.

En los catorce labs se construyó en vivo, con guion y con la solución al lado. Aquí no hay guion:
hay un requerimiento de negocio incompleto en los bordes, tres horas, y una rúbrica que se puede
leer antes de empezar.

## Qué hay aquí

| | |
|---|---|
| [`brief/requerimientos.md`](brief/requerimientos.md) | **El encargo.** Empieza por aquí |
| [`rubrica/rubrica.md`](rubrica/rubrica.md) | **Cómo se te evalúa.** Léela antes de escribir código |
| [`plantillas/reporte.md`](plantillas/reporte.md) | Lo que entregas junto al código. **Es parte del examen** |
| `base/` | El proyecto del que partes: compila, arranca, y no tiene el encargo |
| `instructor/` | Solución de referencia y guía de defensa. **No viaja en el repositorio** |

## Cómo se corre

```bash
cd base
./mvnw spring-boot:run
```

Escucha en el **8107**. La base de datos (PostgreSQL embebido, puerto 55444) la levanta la propia
aplicación: sin Docker y sin instalar nada, como todo el curso.

```bash
# el login ya funciona: viene resuelto
curl -X POST localhost:8107/auth/login -H 'Content-Type: application/json' \
     -d '{"usuario":"ana","clave":"secreta"}'
```

| usuario | clave | rol |
|---|---|---|
| `ana` | `secreta` | **FISCALIZADOR** |
| `luis` | `secreta` | **CONTRIBUYENTE** |

## Qué trae `base/` resuelto

Lo que **no** se evalúa hoy, para que las tres horas se vayan en el encargo:

- El esquema y los datos sembrados: tres contribuyentes y ocho trámites repartidos en dos años.
  Uno de los contribuyentes **no tiene trámites** — está ahí a propósito.
- Las entidades `Contribuyente` y `Tramite` con su relación, y sus repositorios básicos.
- **La autenticación completa**: `POST /auth/login`, emisión y validación de JWT, y la cadena de
  filtros exigiendo token en todo salvo el login y `/actuator/health`.
- Actuator y Jib, configurado y listo para `./mvnw package jib:buildTar`.

## Qué tienes que escribir tú

El endpoint del consolidado y todo lo que lleva detrás: el DTO, la consulta, el servicio, la regla
de **quién** puede verlo, los errores con forma, y tus pruebas. Más el reporte.

## Cómo se compone tu nota

El contrato del curso reparte la evaluación así:

| | Peso | Instrumento |
|---|---|---|
| **Proyecto final** | **50 %** | **Este proyecto** y su defensa |
| Evaluación de conocimientos | 30 % | ⚠️ *no está en este repositorio* |
| Ejercicios | 20 % | ⚠️ *no está en este repositorio* |

> **Dicho con todas las letras:** este material cubre el 50 % de la evaluación contratada. La prueba
> de conocimientos y la nota de ejercicios **no tienen instrumento aquí** y las define quien dicta.
> No es un olvido de este documento: es información que hace falta para cerrar el curso, y está
> registrada en `ESTADO.md` y en el informe de la SPEC-035.

La aprobación exige nota mínima 4,0 y 75 % de asistencia. Dentro de **este** instrumento, el umbral
es **núcleo verde Y Criterio ≥ Suficiente**: todo funcionando y sin criterio **no aprueba**.

## Los catorce labs están detrás de cada requisito

No se evalúa nada que no se haya enseñado. La correspondencia completa está en el informe de la
SPEC-035; en corto:

| Lo que pide el encargo | Dónde se enseñó |
|---|---|
| Endpoint REST con parámetros | Lab 01 |
| Capas y servicio | Lab 02 |
| Errores con forma (404, 400) | Lab 03 |
| Entidad y repositorio | Lab 04 |
| Relación entre entidades | Lab 05 |
| Una consulta que no es trivial | Lab 06 |
| Transacción de sólo lectura | Lab 07 |
| Las pruebas | Lab 08 |
| Rol, 401 y 403 | Lab 09 |
| Empaquetado e imagen OCI | Lab 13 |
