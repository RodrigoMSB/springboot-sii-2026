# Curso Spring Boot · SII 2026

Material del curso de Spring Boot para el SII: teoría, labs y su tooling de validación.

> 📌 **Por qué este curso se enseña así → [`MANIFIESTO.md`](MANIFIESTO.md)**
> Léelo antes de dictar una sola sesión.

## Los laboratorios

**Quince laboratorios, del 00 al 14.** El alumno construye en vivo junto al instructor: todos
tienen la misma forma —`README.md`, `PASOS.md` (el guion de la sesión), y tres carpetas— y se
corren con `./mvnw spring-boot:run`. **No necesitan Docker ni instalar nada**: Java, Maven,
PostgreSQL y hasta la imagen base de los contenedores viajan dentro del repositorio.

| Lab | Tema | Qué se lleva |
|---|---|---|
| [`lab-00-hola-mundo`](labs/lab-00-hola-mundo/) | Que Spring Boot arranque | Una clase, una anotación y un `main`. Nada más |
| [`lab-01-web`](labs/lab-01-web/) | El primer endpoint | Ruta, parámetro y cuerpo; y el código de estado como parte de la respuesta |
| [`lab-02-di`](labs/lab-02-di/) | Inyección de dependencias | **Qué es Spring**: tú declaras qué necesitas y el contenedor te lo entrega |
| [`lab-03-errores`](labs/lab-03-errores/) | Errores con forma | El camino triste también es contrato: 404 con cuerpo, 400 con los campos |
| [`lab-04-jpa`](labs/lab-04-jpa/) | Guardar y recuperar | Una clase y una tabla son la misma cosa. El SQL sale en la consola |
| [`lab-05-relaciones`](labs/lab-05-relaciones/) | Relaciones JPA | LAZY dispara 1 SELECT; EAGER, 4. Y la `LazyInitializationException` |
| [`lab-06-rendimiento`](labs/lab-06-rendimiento/) | El N+1 | De **201 consultas a 1**, medido en pantalla. Y por qué `EAGER` lo empeora |
| [`lab-07-concurrencia`](labs/lab-07-concurrencia/) | Dos peticiones, el mismo folio | Correcto en secuencia, incorrecto en paralelo. Se prueba corriéndolo |
| [`lab-08-testing`](labs/lab-08-testing/) | Testing | Un test sirve el día que se pone **rojo**. JUnit, Mockito, `@WebMvcTest` |
| [`lab-09-seguridad`](labs/lab-09-seguridad/) | Seguridad | Cerrado por defecto. BCrypt con sal, el JWT que **cualquiera lee**, y 401 frente a 403 |
| [`lab-10-resiliencia`](labs/lab-10-resiliencia/) | Resiliencia | De **30 s a 2 ms**: timeout, reintento (que empeora la caída) y el circuito |
| [`lab-11-observabilidad`](labs/lab-11-observabilidad/) | Observabilidad | Con la base caída: liveness **200**, readiness **503**, y el health nombra la causa |
| [`lab-12-tareas`](labs/lab-12-tareas/) | Tareas y asincronía | `@Scheduled`, `@Async` (3,03 s → 0,004 s), hilos virtuales, y la tarea duplicada |
| [`lab-13-empaquetado`](labs/lab-13-empaquetado/) | Empaquetado | El jar, las capas, qué es un contenedor, y una imagen OCI **sin Docker y sin red** |
| [`lab-14-microservicios`](labs/lab-14-microservicios/) | Microservicios | Cuatro procesos y tres bases: el JOIN imposible, el fallo en cascada (**500 → 200 degradado**), el gateway, y cuándo **no** partir un sistema |

### Las tres carpetas de cada lab

| | |
|---|---|
| `practica/` | Donde trabaja el alumno. **Sin documentación**: la firma, una línea imperativa y `// escribe aquí` |
| `solucion/` | El proyecto terminado, con comentarios **breves** donde algo no es evidente |
| `instructor/` | Los mismos archivos de `solucion/`, explicados **línea por línea**. **No viaja al repo** |

`instructor/` **no es un proyecto**: no tiene `mvnw`, ni `.mvn`, y no se compila. Son los archivos
para leer mientras se enseña — desde por qué está cada `import` hasta cada dependencia del `pom`.

Está excluida en el [`.gitignore`](.gitignore) de la raíz (`labs/*/instructor/`) por una razón
pedagógica: **es la chuleta de quien dicta**. Si viajara en el clon, el alumno leería la
explicación en vez de escuchar — que es justo lo que `practica/` sin documentación evita. Si no
está en tu clon, no falta nada: la genera quien prepara la sesión, a partir de `solucion/`.

Problemas de entorno: [`docs/troubleshooting.md`](docs/troubleshooting.md) ·
[`docs/entorno-alumno.md`](docs/entorno-alumno.md).

## Protocolo SPEC

El arquitecto emite especificaciones numeradas, versionadas en `docs/specs/`. Si la
ejecución difiere de la SPEC, la SPEC manda y la discrepancia se reporta. Los commits
llevan el prefijo `SPEC-NNN: <qué>`.

- **`SPEC-NNN`** — funcionalidades y entregables nuevos. Propósito único.
- **`SPEC-FIX-NN`** — corrección de una SPEC ya ejecutada (bug del material).
- **`SPEC-DIAG-NN` / `SPEC-AUDIT-NN`** — diagnóstico e investigación que no producen
  material. Se versionan siempre.
- **Sufijo `-R1`, `-R2`** — revisión de una SPEC aún no ejecutada.
- **Ninguna ejecución comienza antes de que su SPEC esté commiteada.**

**Toda SPEC va por rama, con PR. Sin excepciones.** Se deroga la cláusula transitoria
("directo a `main` mientras no se dicte"). Una **`SPEC-FIX-NN`** además termina en tag:
`fix/<slug>` → merge a `main` → tag `material-vX.Y.Z` (patch bump).

> ⚠️ **La regla es convencional, no está enchufada.** `main` **no** tiene protección en el
> servidor: GitHub no permite proteger ramas en repos privados del plan Free, y el push
> directo entraría. Todo PR ejecuta los tres checks de `material-ci` (`temario`, `siembra`,
> `app`), pero **ninguno es obligatorio para mergear**. El candado está especificado y
> congelado (SPEC-FIX-01 §3.1); se activa el día que haya GitHub Pro o el repo deje de ser
> privado. Aquí no se declara activo lo que no lo está.

El nombre del archivo **no lleva sufijo de versión**: Git ya versiona el contenido.

**Cuándo una SPEC se vuelve inmutable:** al **cerrar**, es decir, cuando su PR se mergea —
no cuando se commitea. Mientras su PR vive, los ajustes acordados se anotan *dentro* de la
SPEC. Una vez cerrada, se corrige con una `SPEC-FIX-NN` o se revisa como `-R1` nuevo.

**Verificación en dos etapas.** Todo script y todo flujo lo ejecuta **primero el ejecutor**,
sobre estado limpio, y cita la salida en su reporte. Solo entonces el PO lo corre como
aceptación final. **El PO jamás es el primero en correr algo.**

**Toda SPEC actualiza [`ESTADO.md`](ESTADO.md) al cerrar.** Un `ESTADO.md` desactualizado es
un bug del material, no un descuido.

## Roles

- **Product Owner** (Rodrigo): aprueba las SPEC y decide los commits de fondo.
- **Arquitecto**: emite las SPEC.
- **Ejecutor**: las ejecuta, registra la evidencia en el repo y maneja git/GitHub.

Decisiones de diseño y su razón: [`docs/decisiones.md`](docs/decisiones.md).
