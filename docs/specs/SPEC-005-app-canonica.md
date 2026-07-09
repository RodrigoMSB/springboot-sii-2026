# SPEC-005 · App canónica `dgt-tramites-api`: el primer ladrillo

| Campo | Valor |
|---|---|
| ID | SPEC-005 |
| Título | App canónica: esqueleto Maven, 7 entidades, Flyway, seeds, ArchUnit con fixtures |
| Autor | Arquitecto |
| Aprueba | PO (Rodrigo) — **APROBADA** |
| Depende de | SPEC-000 ejecutada, **spike S-1 con veredicto VIABLE** |
| Estado | LISTA PARA EJECUCIÓN (condicionada al spike) |

> **Instrucción de ejecución (mocito):** primer paso, guardar este archivo íntegro en
> `docs/specs/SPEC-005-app-canonica.md` y commitearlo antes de ejecutar.
>
> **GUARDA (bloqueante):** esta SPEC asume el veredicto del spike S-1 (SPEC-000 §10.B).
> Si fue `VIABLE`, ejecuta tal cual. Si fue `VIABLE CON AJUSTE`, aplica el ajuste
> reportado a la redacción de AU-02 y decláralo como discrepancia. Si fue `NO VIABLE`,
> **DETENTE** y repórtalo: la SPEC-000 necesita un `-R1` antes de poner ladrillos.

---

## §1 · Objetivo

Que exista `dgt-tramites-api` **compilando, corriendo y vigilada**: el tronco de
referencia del que las SPEC de labs (007+) tallarán sus `starter/` y `solucion/`. Al
cierre de esta SPEC hay una aplicación Spring Boot 4.1 real con su dominio completo,
su esquema migrado, sus datos semilla, y las 7 reglas ArchUnit mordiendo — cada una con
la prueba de que muerde.

**Qué NO es esta SPEC:** no construye los crímenes (esos son estados derivados por lab),
no implementa la emisión de folio con contador bloqueado (Lab 06), no implementa
seguridad (Lab 07) ni mensajería (Lab 11). El tronco es limpio y mínimo; los labs le
agregan músculo y le plantan cadáveres.

## §2 · Ubicación y estructura

La app vive en la raíz del repo, espejo de `app-mi-dgt/` del curso de Cypress:

```
dgt-tramites-api/
├── mvnw, mvnw.cmd, .mvn/          # Maven Wrapper
├── pom.xml                        # Boot 4.1.0 exacto · java.version=25
├── compose.yaml                   # PostgreSQL, tag con patch fijado
└── src/
    ├── main/java/cl/dgt/tramites/
    │   ├── domain/
    │   │   ├── entity/            # las 7 entidades — SOLO jakarta.*, cero Spring
    │   │   └── exception/         # excepciones de dominio
    │   ├── application/           # servicios @Service (Spring vive aquí, no en domain)
    │   ├── infrastructure/
    │   │   └── repository/        # interfaces Spring Data
    │   └── web/
    │       ├── dto/               # records
    │       ├── mapper/            # mapeo manual entidad ↔ DTO
    │       └── controller/
    ├── main/resources/
    │   ├── application.yml        # SIN credenciales (ver §4, nota del pecado original)
    │   └── db/migration/          # V1, V2
    └── test/java/cl/dgt/tramites/
        ├── arquitectura/          # las 7 reglas AU + meta-tests
        │   └── fixtures/violaciones/   # las clases que violan a propósito
        └── dominio/               # tests de RN a nivel dominio
```

**Resolución de capas (vinculante, deriva de AU-01…07):** las entidades usan solo
`jakarta.persistence` (AU-03 lo permite: Jakarta no es Spring). Los servicios con
`@Service` viven en `application`, nunca en `domain`. Los repositorios Spring Data viven
en `infrastructure.repository` (extienden `org.springframework.data`, luego no pueden
estar en `domain`). `web` habla con `application` y con DTOs; jamás toca
`domain.entity` (AU-01/AU-02).

## §3 · Entregables

### E1 · Esqueleto Maven

- `spring-boot-starter-parent` **4.1.0** exacto; `java.version=25`.
- Dependencias main: `web`, `data-jpa`, `validation`, `actuator`, `flyway-core` (+
  `flyway-database-postgresql` si el BOM lo separa — verificar contra el BOM, no de
  memoria), driver `postgresql`.
- Dependencias test: `spring-boot-starter-test` (trae Jupiter 6 + AssertJ + Mockito),
  **Testcontainers 2.x con coordenadas nuevas** (`testcontainers-postgresql` — verificar
  el artefacto exacto y `@ServiceConnection`), `com.tngtech.archunit:archunit` **core**
  1.4.x (la versión exacta la fija el spike), `awaitility`.
- **Sin Lombok, sin Kotlin** (D-003). Constructores y accessors a mano: eso también es
  contenido del curso.

### E2 · `compose.yaml` + arranque local

PostgreSQL con **patch fijado** (re-verificar el tag vigente al ejecutar; el de
referencia al 2026-07-09 era `postgres:16-alpine3.24`) e integración
`spring-boot-docker-compose`: el alumno jamás escribe una cadena de conexión.

**Nota del pecado original (déjala como comentario en el compose):** la credencial del
compose (`POSTGRES_PASSWORD: dgt-dev`) es una credencial de laboratorio para una base
desechable local — está versionada a propósito y documentada. **El crimen del Lab 01 es
otro:** credenciales de *producción* en `application.yml`. Que nadie confunda las dos
cosas ni nos acuse de predicar sin practicar. `application.yml` no contiene ninguna
credencial (D-012).

### E3 · Migración `V1__esquema_base.sql`

Las 7 tablas + `contador_folio` (tabla técnica, con comentario SQL explicando su rol en
RN-02). Constraints desde el día uno:

- `contribuyente.rut` y `usuario.rut`: `UNIQUE`.
- `folio.numero`: `UNIQUE` (RN-01). `folio.tramite_id`: `UNIQUE` (un trámite, a lo más
  un folio — el suelo de RN-05).
- `tramite.estado`: `NOT NULL`.
- **Sin `CHECK (monto >= 0)` en `linea_f29`**: ese contrato se agrega en el lab del M8
  — es la lección "restricciones como contratos", no se regala aquí.
- **Sin columna `total` en `formulario29`**: RN-06 es derivado (SPEC-000 §5).

### E4 · Migración `V2__datos_semilla.sql`

- Contribuyentes: `11111111-1` Valentina Rojas · `12345678-5` Comercial Andina SpA.
- Usuarios: Carolina (FUNCIONARIO), Ignacio (FISCALIZADOR), Valentina (CONTRIBUYENTE).
  `claveHash` = hash BCrypt **versionado** (un hash no es un secreto); la clave de demo
  en texto plano vive solo en `docs/` del proyecto como "clave de laboratorio".
- Un puñado de trámites en estados variados con sus F29 y líneas, suficientes para que
  los primeros labs tengan qué listar.

### E5 · Dominio con sus reglas testeadas

- Las 7 entidades (SPEC-000 §4), **todo `@ManyToOne`/`@OneToOne` con `fetch = LAZY`
  explícito** (AU-04 nace cumplida).
- Máquina de estados del `Tramite`: `BORRADOR → PRESENTADO → PAGADO → FOLIADO`, sin
  retrocesos — método de transición que lanza excepción de dominio ante transición
  ilegal, **con su test parametrizado** que recorre la matriz completa de transiciones
  (legales e ilegales).
- `Formulario29.total()`: método derivado que suma sus líneas, **con test** (RN-06).
- Javadoc estilo "Javadoc-como-clase" (P del ADN): el porqué en el punto de uso. Este
  código lo van a *leer* 18 alumnos; se escribe para ser leído.

### E6 · Rebanada web mínima

Un solo caso vertical, correcto de punta a punta, para que las reglas ArchUnit tengan
código real que vigilar: `GET /api/contribuyentes/{rut}` → `application` →
`infrastructure` → **DTO record** (sin `claveHash`, sin `puntajeRiesgoInterno`) +
`@RestControllerAdvice` con `ProblemDetail` para el caso no-encontrado. Con su test
(`@WebMvcTest` o `RestTestClient`, a tu juicio; el que uses queda como referencia de
estilo para los labs) **que además verifica RN-03 dinámicamente**: el JSON serializado
no contiene los campos prohibidos.

### E7 · La suite de arquitectura (el corazón de esta SPEC)

En `test/.../arquitectura/`:

1. **Las 7 reglas AU-01…07** como `@Test` Jupiter con `ClassFileImporter` (artefacto
   core, según el spike). Cada `because(...)` nombra el crimen, no la regla.
2. **Los 7 fixtures negativos** en `fixtures/violaciones/`: una clase por regla que la
   viola a propósito (p. ej. `ControladorQueFiltraEntidad` devolviendo
   `ResponseEntity<Contribuyente>` para AU-02). Solo en el classpath de test.
3. **Los 7 meta-tests**: cada uno importa main + su fixture y verifica que la regla
   **falla** (la caza) — y la suite normal, que importa solo main, verifica que **pasa**.
   *Un guardián sin prueba de que muerde es un adorno* — y aquí cada guardián trae su
   mordida certificada.

Importar con `DO_NOT_INCLUDE_TESTS` en las reglas de producción para que los fixtures no
contaminen la vigilancia del main.

### E8 · CI: el job `app`

Extender `material-ci.yml` con un job `app`: `setup-java` Temurin 25 → `./mvnw verify`
dentro de `dgt-tramites-api/`. Los runners `ubuntu-latest` traen Docker: Testcontainers
corre nativo. Actualizar el filtro de paths del workflow (`dgt-tramites-api/**`).
Verificación real: run verde **citado**, con el log mostrando la suite de arquitectura
ejecutada (no solo `conclusion: success` — ya sabes cómo se hace).

## §4 · Criterios de aceptación

- [ ] SPEC-005 commiteada antes que el código.
- [ ] Guarda del spike resuelta y declarada (VIABLE / ajuste aplicado).
- [ ] `./mvnw verify` **verde local**, salida citada: compila en Java 25 sobre Boot 4.1.0.
- [ ] La app **arranca** con `compose.yaml` (`spring-boot:run`): Flyway aplica V1+V2 y
      `/actuator/health` responde `UP` — evidencia citada (log de migraciones + curl).
- [ ] `GET /api/contribuyentes/11111111-1` responde el DTO de Valentina, sin campos
      prohibidos (RN-03), y un rut inexistente responde `ProblemDetail` 404.
- [ ] Test de máquina de estados y test de `total()` derivado: verdes.
- [ ] Las 7 reglas AU verdes sobre main **y** los 7 meta-tests verdes (cada regla caza
      su fixture). 7 + 7, sin excepciones.
- [ ] `application.yml` sin credenciales; el compose lleva su comentario del pecado
      original.
- [ ] Job `app` en Actions: **verde y citado**.
- [ ] Commits con prefijo `SPEC-005:`; push hecho (o PR, si el candado ya se activó).

## §5 · Reporte

Salida de `./mvnw verify`, log del arranque (migraciones + health), salida de los
meta-tests (las 7 mordidas), URL del run de Actions, `git log --oneline`, discrepancias
y hallazgos del ejecutor — sin tocarlos, como siempre. Si alguna decisión de diseño de
esta SPEC te obligó a elegir entre dos caminos razonables (p. ej. `@WebMvcTest` vs
`RestTestClient` en E6), declara cuál elegiste y por qué: esa elección se vuelve el
estilo de referencia de los labs.

## §6 · Bitácora

| Fecha | Decisión | Razón |
|---|---|---|
| (fecha de ejecución) | Nace `dgt-tramites-api`: tronco de referencia del curso (Boot 4.1.0 · Java 25 · 7 entidades · Flyway V1+V2 · rebanada web con DTO · 7 reglas ArchUnit, cada una con fixture negativo y meta-test que certifica la mordida). Los labs derivan de este tronco. | El primer ladrillo se pone con los guardianes ya despiertos: es más barato nacer vigilado que instalar la vigilancia con la casa habitada. |
