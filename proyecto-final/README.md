# Proyecto final · Consolidado del contribuyente

**Vale el 70 % de la nota del curso.** El 30 % restante es la evaluación de conocimientos, que se
rinde aparte.

Se hace **en la casa, con plazo**. No es una carrera contra el reloj: es un encargo completo,
especificado sin huecos, con un ejemplo del mismo encargo ya resuelto al lado.

## Qué hay aquí

| | |
|---|---|
| [`brief/requerimientos.md`](brief/requerimientos.md) | **El encargo, cerrado.** Empieza por aquí |
| [`rubrica/rubrica.md`](rubrica/rubrica.md) | **Cómo se te evalúa.** Seis criterios, cada uno con su comando |
| `base/` | **Donde trabajas.** Compila, arranca y su test pasa. No tiene el encargo |
| `ejemplo/` | **El mismo encargo, resuelto sobre otra entidad.** Ábrelo al lado y traduce |
| [`plantillas/reporte.md`](plantillas/reporte.md) | Opcional. Para dejar por escrito alguna decisión |
| `instructor/` | Solución de referencia y guía de defensa. **No viaja en el repositorio** |

## El camino más corto

1. Lee `brief/requerimientos.md`. Está **cerrado**: no hay nada que decidir, sólo que implementar.
2. Lee `rubrica/rubrica.md`. Son seis criterios y **cada uno dice con qué comando se comprueba**.
3. Abre `ejemplo/` al lado de `base/`. Es el mismo encargo sobre otra entidad, pieza por pieza.
4. Traduce las seis piezas a `base/`.
5. Comprueba con los cuatro `curl` de la rúbrica. Los mismos que se van a correr al corregir.

## Cómo se corre

```bash
cd base
./mvnw spring-boot:run
```

| | puerto HTTP | PostgreSQL |
|---|---|---|
| `base/` | **8107** | 55444 |
| `ejemplo/` | **8108** | 55445 |

La base de datos la levanta la propia aplicación: sin Docker y sin instalar nada, como todo el
curso.

```bash
# el login ya funciona: viene resuelto
curl -X POST localhost:8107/auth/login -H 'Content-Type: application/json' \
     -d '{"usuario":"ana","clave":"dgt2026"}'
```

| usuario | clave | rol |
|---|---|---|
| `ana` | `dgt2026` | **FISCALIZADOR** |
| `luis` | `dgt2026` | **CONTRIBUYENTE** |

## Qué trae `base/` resuelto

Todo lo que el encargo **no** evalúa:

- El esquema, los datos sembrados, las entidades y sus repositorios.
- **La autenticación completa**: login, JWT y cadena de filtros.
- **Actuator** (`health`, `info`, `metrics`) y la métrica de negocio **ya declarada** —
  `ContadorDeConsolidados`, que expone `dgt.consolidados.emitidos`. Tú pones una línea.
- **OpenAPI**: `/swagger-ui.html` funciona, y tu endpoint aparecerá ahí solo.
- **Jib**: `./mvnw package jib:buildTar` construye la imagen OCI, sin Docker y sin red.
- Un **test de contexto** en verde y la consola limpia (traza recortada, sin WARNING de arranque).

Lo que **no** trae es el encargo: ni el endpoint, ni los DTO, ni la consulta, ni la regla de rol,
ni los dos tests.

---

## La entrega

**Un zip de tu carpeta `base/`, con el encargo hecho, por correo.**

> ## Plazo · viernes 25 de septiembre de 2026, 23:59
>
> Tres semanas desde que se entrega el encargo. La fecha esquiva a propósito la semana de Fiestas
> Patrias: el 18 y el 19 son feriados, y una entrega el viernes 18 habría sido una entrega el
> jueves 17 para todo el mundo.

Antes de mandarlo, comprueba tú mismo lo que se va a comprobar al corregir:

```bash
cd base
rm -rf target .datos-pg          # que el zip no lleve basura ni tu base local

./mvnw test                      # los tres tests en verde
./mvnw package jib:buildTar      # la imagen se construye
ls -lh target/jib-image.tar
```

Y los cuatro `curl` de la rúbrica, con la aplicación corriendo. **Son literalmente los que se van a
ejecutar**: están escritos, uno a uno, en `rubrica/rubrica.md` §2.

### Qué se corrige

1. `./mvnw test`
2. Los cuatro `curl`: **401, 403, 404 y 200** con el total correcto
3. `./mvnw package jib:buildTar`

Nada más, y nada menos. Si las tres cosas salen, la parte mecánica está hecha; lo que queda es lo
que la rúbrica llama arquitectura, y eso se lee.
