# ADN del curso de Cypress · destilado con evidencia

> Producido por [SPEC-001](../specs/SPEC-001-destilacion-adn-cypress.md).
> Ampliado con P-15…P-18 por [SPEC-002](../specs/SPEC-002-addendum-adn-y-gobernanza.md).

## 1. Propósito

**Qué es:** el conjunto de prácticas pedagógicas y de tooling del curso Cypress SII 2026 que el curso de Spring Boot hereda, cada una con la línea de código que la demuestra.
**De dónde sale:** verificación directa contra `/Users/rodrigosilva/SII/Copia de CYPRESS/cypress-sii-2026` (solo lectura). Ninguna afirmación proviene de memoria ni de citas de segunda mano.
**Quién lo consume:** la futura SPEC-000 del curso de Spring Boot — la lee quien escriba la SPEC-002 en adelante, antes de inventar nada.

> **Regla de este documento:** toda práctica lleva ruta, cita y traslado. Las hipótesis sin evidencia no se omiten: se declaran en §4 con el comando y su salida.

---

## 2. Prácticas verificadas

### P-01 · El lint elige sus reglas, y escribe por qué no eligió las otras

**Evidencia:** `eslint.config.mjs:41-47`
```js
//  Usamos configs.globals (no configs.recommended) A PROPÓSITO: activamos sólo
//  el CONJUNTO MÍNIMO de reglas que pide SPEC-009 §2. El `recommended` trae
//  además `unsafe-to-chain-command` y `no-async-tests` [...] marcarían material
//  HISTÓRICO ya entregado [...] — y la historia NO se reescribe (regla sagrada).
pluginCypress.configs.globals,
```

**Por qué funciona:** la configuración no es una preferencia, es una decisión con costo declarado. Quien la lea en seis meses sabe qué se descartó, por qué, y bajo qué condición se revierte ("un PR aparte con su propia limpieza"). El comentario evita que alguien "mejore" el config rompiendo 8 labs ya entregados.

**Traslado a Spring Boot:** el `checkstyle.xml` / `spotbugs-exclude.xml` del repo lleva un encabezado con el mismo contrato: qué reglas se activan, cuáles del `recommended` se dejaron fuera y contra qué material histórico romperían. Regla sagrada equivalente: **un lab ya entregado no se re-lintea**; si una regla nueva lo marcaría, se activa en un PR con su limpieza.

---

### P-02 · Una regla de arquitectura, ejecutable, que caza todas sus formas

**Evidencia:** `eslint.config.mjs:88-104` — el `no-restricted-syntax` sobre `**/support/pages/**/*.js` declara **tres** selectores, no uno:
```js
selector: "CallExpression[callee.property.name='should']",
selector: "CallExpression[callee.property.name='and']",
selector: "CallExpression[callee.name='expect']",
```
Cada uno con el mismo mensaje: `'Un Page Object NO asevera: mueve el .should() al test (regla de la casa, Lab 08).'`

**Por qué funciona:** la regla "el PO no asevera" tiene tres encarnaciones sintácticas (`.should()`, `.and()`, `expect()`). Cazar solo la primera deja dos puertas abiertas y enseña que la regla es cosmética. El mensaje además **cita el lab** donde se enseñó, cerrando el círculo entre la teoría y el error del alumno.

**Traslado a Spring Boot:** ArchUnit, no grep. La regla de la casa equivalente —*un `@Controller` no contiene lógica de negocio*, *un `@Repository` no se inyecta en un `@Controller`*— se escribe como test ArchUnit en el módulo de arquitectura, y **el mensaje de fallo nombra el lab** donde se enseñó la capa. Se cazan todas las formas: `@Autowired`, constructor injection y `@Inject`.

---

### P-03 · El código incompleto del alumno jamás rompe el tooling del material

**Evidencia:** `eslint.config.mjs:25-31`
```js
//  💡 El `starter/` de cada lab contiene marcadores {{TODO}} — eso NO es
//     JavaScript válido y haría explotar al parser. El código incompleto del
//     alumno JAMÁS debe fallar el lint del material (decisión de SPEC-009 §2).
ignores: [ '**/starter/**', /* … */ ]
```

**Por qué funciona:** dos razones distintas en una línea — una técnica (`{{TODO}}` no parsea) y una pedagógica (no es trabajo del material juzgar el borrador del alumno). Y ambas apuntan a la spec que las autorizó.

**Traslado a Spring Boot:** `starter/` fuera de `maven-checkstyle-plugin` y de la compilación del CI del material. Los `{{TODO}}` en Java tampoco compilan. Se excluye por `<sourceDirectory>` en el perfil `material-ci`, con el comentario citando la SPEC correspondiente.

---

### P-04 · El tooling enseña: el WARN aparece solo cuando el escenario pedagógico se activa

**Evidencia:** `labs/lab-03-inicio-de-actividades/bin/start-lab.sh:88-94`
```bash
if [ "${DGT_DELAY_MS}" -gt 0 ]; then
  log_warn "MODO LENTO activado: cada request tardará ${DGT_DELAY_MS} ms (simula backend estresado)."
  if [ "${DGT_DELAY_MS}" -gt 4000 ]; then
    log_warn "Ojo: ${DGT_DELAY_MS} ms SUPERA el defaultCommandTimeout de 4000 ms de Cypress."
    log_warn "Es el escenario de la Guía 04 del Lab 03 (esperas determinísticas)."
```

**Por qué funciona:** el umbral `> 4000` no es arbitrario: es exactamente el `defaultCommandTimeout` de Cypress. El script no avisa "vas lento"; avisa **"acabas de cruzar la frontera que la Guía 04 te va a enseñar"**, y nombra la guía. El alumno que juega con `--delay` descubre la lección antes de leerla.

**Traslado a Spring Boot:** `start-lab.sh --delay <ms>` sobre el backend de labs. El umbral es el timeout real del cliente que se esté enseñando (`spring.mvc.async.request-timeout`, o el `connectTimeout` de `RestClient`). Cuando el delay lo supera, el WARN nombra la guía del lab de resiliencia/timeouts. El número del umbral se lee de la config, no se hardcodea dos veces.

---

### P-05 · La rúbrica nombra el fraude específico, no "falta de calidad"

**Evidencia:** `labs/lab-13-el-framework-de-la-dgt/rubrica/rubrica-evaluacion.md:15`
```
| **Insuficiente** | El núcleo no corre en verde, o el pipeline es deshonesto (`|| true`, gate decorativo). |
```

**Por qué funciona:** "pipeline deshonesto" es un juicio; `|| true` es un hecho verificable. La rúbrica da al evaluador un criterio que no admite negociación y al alumno una lista de lo que no puede hacer. El eje se llama, textualmente, *"¿La suite pasa? ¿El pipeline es honesto?"* (línea 11).

**Traslado a Spring Boot:** el nivel Insuficiente de Correctitud nombra los equivalentes Java exactos: `-DskipTests` en el job de verificación, `mvn test || true`, `@Disabled` sin issue enlazado, `assertTrue(true)`, y `continue-on-error: true` en el step que debería bloquear. Cada uno es grep-eable por el evaluador y verificable por el `91` (ver A-02).

---

### P-06 · La escalera colapsada: el refactor paga una deuda que el alumno acumuló durante 7 labs

**Evidencia:** `labs/lab-08-la-gran-refactorizacion/TEORIA.md:88-104` — §4 `cy.login`: la escalera de autenticación colapsada
```
Los 7 labs construyeron cuatro formas de autenticarse. `cy.login` las colapsa en
UN comando con un parámetro `via`:
                       cy.login({ via, rut, scope })
   via:'ui'       via:'api'                via:'oidc'        (todos envueltos
   Lab 01         Lab 04                   Lab 07             en cy.session)
```
Y cierra: *"La deuda de repetir el login en 14 archivos, saldada de un golpe."*

**Por qué funciona:** el refactor no se enseña con un ejemplo de juguete. El alumno **escribió él mismo** las cuatro variantes en los labs 01, 04 y 07; el diagrama le muestra su propio historial colapsando. La motivación del DRY no se argumenta: se cobra. La siembra estaba puesta desde el Lab 04 (`TEORIA.md:171`: *"aún repetimos este login en cada test"*).

**Traslado a Spring Boot:** el lab de refactorización colapsa las vías de configuración de test que los labs previos construyeron a mano —`@SpringBootTest` completo, `@WebMvcTest` con `MockMvc`, `@DataJpaTest`, `Testcontainers` crudo— en **una** clase base anotada o un `@TestConfiguration` con parámetro de perfil. El diagrama de la teoría nombra el lab que originó cada rama, y el texto cuantifica la deuda ("este `@BeforeEach` está copiado en N clases").

---

### P-07 · La guía del instructor trae las preguntas exactas y las respuestas calibradas por nivel

**Evidencia:** `labs/lab-13-el-framework-de-la-dgt/rubrica/guia-instructor.md:27-49`
```
## Preguntas para destapar el criterio
- *"Si solo pudieras escribir 3 tests, ¿cuáles y por qué?"* → prioriza riesgo.
- *"¿Qué NO probaste a propósito, y por qué?"* → conoce sus límites (bien) o no pensó en ello (mal).
```
Y, para la misma pregunta, las cuatro respuestas modelo:
```
- ❌ *Insuficiente:* "Porque era el que salía en el brief / el más fácil de hacer."
- 🟡 *Suficiente:* "Es el flujo crítico: si se rompe, la gente no puede declarar."
- 🟢 *Competente:* "Es crítico y de alto riesgo (toca dinero y plazos legales); por eso lo cubro end-to-end…"
- 🟦 *Destacado:* "…y además lo cubro con un gate de accesibilidad porque es un servicio público obligatorio…"
```

**Por qué funciona:** cada pregunta lleva anotado **qué mide** (`→ prioriza riesgo`, `→ mantenibilidad`, `→ comunicación`). Y la calibración por nivel hace que dos instructores distintos puntúen igual: el eje "criterio" deja de ser subjetivo sin dejar de ser humano. La guía es explícita sobre su frontera (línea 3): *"El boletín te da Correctitud y Oficio ya masticados. Esta guía es para lo que ninguna máquina puede juzgar."*

**Traslado a Spring Boot:** el lab de egreso lleva `rubrica/guia-instructor.md` con el mismo esqueleto. Preguntas adaptadas: *"Si solo pudieras escribir 3 tests de integración, ¿cuáles?"*, *"¿Por qué este bean es `@Transactional` y aquel no?"*, *"Un colega extiende esto mañana: ¿por dónde empieza?"*. Cada una con su `→ qué mide` y sus cuatro respuestas modelo. La máquina (Maven + ArchUnit) puntúa Correctitud y Oficio; el humano puntúa Criterio.

---

### P-08 · Feedback que forma: fortaleza primero, crítica convertida en acción

**Evidencia:** `labs/lab-13-el-framework-de-la-dgt/rubrica/guia-instructor.md:62-68`
```
## Feedback que forma (no que aplasta)
- Nombra **una fortaleza real** antes de las mejoras.
- Convierte cada crítica en **una acción**: no "tu arquitectura es confusa" sino
  "mové las aserciones fuera de los Page Objects y el patrón queda claro".
- Recuérdale que el egreso mide **criterio**, y el criterio **se entrena**: un
  Suficiente hoy es un Competente en el próximo proyecto.
```

**Por qué funciona:** el ejemplo del bullet 2 es el mecanismo entero. "Tu arquitectura es confusa" no es accionable y el alumno no puede refutarla ni arreglarla; "mové las aserciones fuera de los Page Objects" se ejecuta esta tarde. Y el bullet 3 convierte la nota en una derivada, no en una etiqueta. Nótese que la fortaleza pedida es **real**, no de cortesía.

**Traslado a Spring Boot:** se copia la sección casi literal a la guía del instructor del lab de egreso, con el ejemplo reescrito en Java: no *"tu diseño está acoplado"* sino *"saca `JdbcTemplate` del `@RestController` y mételo tras una interfaz de repositorio; el test unitario aparece solo"*.

---

### P-09 · Cada teoría siembra el módulo siguiente ⚠️ *(confirmada como invariante; refutada como texto literal)*

**Evidencia — el invariante se cumple en los 12 labs que tienen sucesor:**
- `labs/lab-01-.../TEORIA.md:314` → `## 9. Conclusiones y siembra del Módulo 2`
- `labs/lab-08-.../TEORIA.md:247` → `## 12. Conclusiones y siembra del Módulo 9`
- `labs/lab-04-.../TEORIA.md:171` → `> 🌱 **Siembra del M5:** aún repetimos este login en cada test.`

**Evidencia — el encabezado literal NO es universal.** Los labs 09–12 usan otro título:
```
labs/lab-09-despliegue-sin-miedo/TEORIA.md:254   ## ⏭️ La semana que viene (siembra M10)
labs/lab-11-la-flota-de-fiscalizacion/TEORIA.md:248   ## ⏭️ La recta final (siembra M12)
```
Y `labs/lab-13-.../TEORIA.md:131` cierra con `## 8. Cierre` — correcto: no hay Módulo 14.

**Por qué funciona:** el mecanismo real no es el encabezado, es la **promesa pendiente**. El Lab 04 dice "aún repetimos este login" cuatro labs antes de que el Lab 08 lo cobre (ver P-06). La siembra convierte una molestia que el alumno ya siente en la motivación del módulo siguiente. El cambio de título en 09–12 ("la recta final") es tono, no estructura.

**Traslado a Spring Boot:** invariante obligatorio, título libre: **toda `TEORIA.md` cierra sembrando el módulo N+1, y el último cierra el arco.** Se verifica en CI con un check que exige, en cada `TEORIA.md` salvo el final, la aparición de `siembra` + el número del módulo siguiente. Ejemplo de siembra: el lab de JPA termina con *"tu `findAll()` trae 10.000 filas; el Módulo N+1 se llama paginación"*.

---

### P-10 · Tres actos: choque → parche que FUNCIONA → forma correcta

**Evidencia:** `labs/lab-03-inicio-de-actividades/guia/04-la-carrera-contra-el-reloj.md` — encabezados en líneas 27, 49 y 74:
```
## Acto 1 · El choque 💥        → // ❌ INGENUO (Acto 1) — NO lo dejes así  (línea 32)
## Acto 2 · El parche bruto 🔨  → // 🟡 FUNCIONA, pero… (Acto 2)            (línea 54)
## Acto 3 · La forma correcta ✅ (TODOs 11–12)
```
Y el Acto 2 interroga su propio éxito (líneas 60-62):
```
| ¿Pasó el test ahora? | |
| ¿Qué número pusiste y por qué 10000? ¿Es adivinar? | |
| ¿Qué pasaría si subieras el `defaultCommandTimeout` **global** a 10 s? | |
```
con la respuesta escondida en un `<details>` (línea 67): *"Tratas el síntoma, no la causa."*

**Por qué funciona:** el Acto 2 es la clave, y es contraintuitivo: el parche **pasa el test**. Si el parche fallara, el alumno lo descartaría por la razón equivocada ("no funciona") en vez de por la correcta ("su costo es que todos los tests tarden 10 s en delatar un fallo real"). Se enseña a evaluar el **costo** de una solución que funciona — que es exactamente lo que separa a un junior de un senior. El `<details>` obliga a intentar antes de leer.

**Traslado a Spring Boot:** la guía del lab de N+1 queries sigue los tres actos. Acto 1: el endpoint tarda 3 s (choque). Acto 2: `spring.jpa.properties.hibernate.jdbc.fetch_size=100` — **el endpoint mejora, el test pasa**, y las tres preguntas son *"¿por qué 100?"*, *"¿qué pasa con 10.000 filas?"*, *"¿qué acabas de esconder?"*. Acto 3: `JOIN FETCH` / `@EntityGraph`, la causa. Mismo patrón para el lab de timeouts (`@Transactional(timeout=)` como parche vs. índice faltante) y el de caché (`@Cacheable` tapando una consulta lenta).

---

### P-11 · El reporte pide transcribir el error exacto, no opinar sobre él

**Evidencia:** `labs/lab-03-inicio-de-actividades/guia/04-la-carrera-contra-el-reloj.md:44`
```
| ¿Qué error exacto da Cypress? (transcríbelo) | |
```
Y la misma exigencia viaja al entregable, `labs/lab-03-.../plantillas/reporte-entregable.md:45-47`:
```
| Acto 1: ¿qué error dio el `cy.get` ingenuo con el backend a 5 s? | |
| Acto 2: ¿qué costo tiene subir el `defaultCommandTimeout` global? | |
| Acto 3: ¿cómo esperaste la respuesta real sin números mágicos? | |
```
El patrón se repite en `lab-01/plantillas/reporte-entregable.md:25`: `| Mensaje de error con **clave incorrecta** | |`.

**Por qué funciona:** *"transcríbelo"* obliga a **leer** el stack trace. La mayoría de los alumnos junior no lee el error: ve rojo y cambia código. Pedir la transcripción literal —no un resumen, no una interpretación— convierte el mensaje de error en el instrumento de diagnóstico que es. Y hace el reporte verificable: el instructor sabe si el alumno corrió el escenario o lo imaginó.

**Traslado a Spring Boot:** las plantillas de reporte piden transcripción literal de la excepción y su causa raíz: *"¿Qué excepción exacta lanza Hibernate? (transcribe la línea `Caused by:`)"*. Aplica a `LazyInitializationException`, `DataIntegrityViolationException` y al `NoSuchBeanDefinitionException` del lab de inyección. La pregunta siguiente es siempre el *por qué*, nunca antes.

---

### P-12 · La matriz de CI documenta su propia exclusión en el YAML

**Evidencia:** `.github/workflows/material-ci.yml:79-109`
```yaml
    # Matriz de SO: el mismo material se valida en Ubuntu (instructor/CI histórico)
    # y en Windows. En los runners windows-latest, `bash` ES Git Bash → exactamente
    # el entorno oficial del alumno Windows. Así "soporte Windows" pasa de supuesto
      matrix:
        os: [ubuntu-latest, windows-latest]
        exclude:
          # lab-11 corre Cypress dentro de un contenedor LINUX (cypress/included)
          # vía `docker compose up`. Los runners windows-latest hospedados no [...]
          - os: windows-latest
            lab: lab-11-la-flota-de-fiscalizacion
```

**Por qué funciona:** dos cosas. Primero, *"'soporte Windows' pasa de supuesto"* a hecho verificado — el CI corre el entorno real del alumno (Git Bash), no una aproximación. Segundo, la **exclusión lleva su razón adosada**: quien vea el hueco en la matriz no lo interpretará como olvido ni lo "arreglará" rompiendo el CI. La razón además explica qué hace el alumno Windows en su lugar (Docker Desktop local).

**Traslado a Spring Boot:** `material-ci.yml` con matriz `os: [ubuntu-latest, windows-latest]` × `java: [21, 25]`. Toda exclusión —p. ej. los labs con Testcontainers que no corren en runners Windows hospedados— lleva su comentario `# razón:` en el mismo bloque `exclude`, nombrando qué hace el alumno en su máquina. La existencia del comentario se puede exigir en la revisión de PR.

---

### P-13 · El curso registra su propia deuda, y escribe por qué arreglarla a la vista es la lección

**Evidencia:** `docs/decisiones.md:14`
> *"El Lab 12 (accesibilidad) descubrió, con honestidad, que el propio `f29.html` tenía una violación de contraste preexistente (`--gris-suave: #6b7280` sobre `--azul-claro: #e8eef7`, ratio 4.14 < 4.5 AA). El Lab 13 la corrigió (`#5b6472`, ratio 5.13) sobre el flujo real."*
> Razón registrada: *"Cierre del arco: hasta quienes enseñamos accesibilidad tenemos barreras que no vimos. Arreglarla a la vista de todos es la lección, no la vergüenza. Los tests usan `data-cy` (no color), así que la regresión de los 12 labs quedó intacta."*

**Por qué funciona:** tres capas. (1) La deuda es **real y medida** (4.14 vs 4.5). (2) El fix es **verificable** (5.13). (3) La razón pedagógica está escrita y es la parte que no se puede reconstruir del código: *arreglarla a la vista de todos es la lección*. La última frase es ingeniería pura: explica por qué el fix no rompió 12 labs (los tests no dependen del color). Un `decisiones.md` que solo registrara "se cambió el color" habría perdido las tres.

**Traslado a Spring Boot:** `docs/decisiones.md` existe desde el día 1 (este documento agrega su primera fila) y registra la deuda del propio material con la misma estructura: **medición → fix medido → razón pedagógica → por qué no rompió lo anterior**. Candidatos ya previsibles: la primera vulnerabilidad que OWASP Dependency-Check encuentre en las dependencias del propio curso, y el primer endpoint del material que falle su propio ArchUnit.

---

### P-14 · Un solo validador, dos modos, el mismo criterio

**Evidencia:** `labs/lab-03-inicio-de-actividades/bin/validar-lab.sh:8-9, 22-26`
```bash
#     ./bin/validar-lab.sh                 # valida starter/ (tu trabajo)
#     ./bin/validar-lab.sh --dir solucion  # valida la solución de referencia
OBJETIVO="starter"
    --dir) OBJETIVO="${2:-}"; shift 2 ;;
```
Cierre con contadores dinámicos y veredicto binario (líneas 186-192):
```bash
printf "  ${C_NEGRITA}Resumen:${C_RESET}  ${C_VERDE}%d PASS${C_RESET} · ${C_ROJO}%d FAIL${C_RESET} · ${C_AZUL}%d SKIP${C_RESET}\n\n" "$PASS" "$FAIL" "$SKIP"
if [ "$GATE_OK" -eq 1 ]; then printf "  🏆 LAB APROBADO ..."; exit 0
else printf "  ✖ LAB NO APROBADO — revisa los FAIL de arriba y vuelve a la guía indicada."; exit 1
```
El flag `--dir` está presente en **12 de 13** `validar-lab.sh` (`grep -ln -- "--dir" labs/*/bin/validar-lab.sh | wc -l` → `12`; el Lab 13 usa `validar-egreso.sh --proyecto`).

**Por qué funciona:** el mismo juez evalúa al alumno y a la solución de referencia. Si el criterio se corrompe, **el CI lo detecta primero** contra `solucion/` (P-12 lo corre en cada push) — el alumno nunca ve un validador roto. El `exit 0/1` lo hace componible desde el CI sin parsear texto; los contadores `PASS/FAIL/SKIP` lo hacen legible para el humano. Y el mensaje de fallo *"vuelve a la guía indicada"* devuelve al alumno al material, no a StackOverflow.

**Traslado a Spring Boot:** `bin/validar-lab.sh [--dir starter|solucion]` orquesta, pero **el criterio vive en Java**: `mvn -q -pl labs/lab-NN verify -Dvalidacion` corre los tests, ArchUnit y JaCoCo. El script traduce el resultado a `PASS/FAIL/SKIP` + `exit 0/1` y a la guía a la que volver. El CI lo corre contra `solucion/` en cada push (ver P-12). Cero lógica de evaluación en bash (ver A-01).

---

### P-15 · Lo opcional nunca baja el veredicto

**Evidencia:** `labs/lab-03-inicio-de-actividades/bin/validar-lab.sh:150` y `:158`
```bash
#  7 · Desafío opcional: gemelo.cy.js (no bloquea la aprobación)
    paso_fail "El desafío gemelo.cy.js tiene {{TODO}} sin completar" "Termínalo — Guía 99." "no-gate"
```
El tercer argumento `"no-gate"` es el mecanismo, definido en la línea 56: `[ "${3:-}" = "no-gate" ] || GATE_OK=0`. Y la numeración lo saca de la secuencia obligatoria: `labs/*/guia/99-desafio-*.md` existe en **12 de 13** labs (`99-desafio-el-gemelo.md`, `99-desafio-el-pipeline-que-miente.md`, …).

**Por qué funciona:** el desafío tiene tres estados, no dos. Ausente → `paso_skip` (línea 58: incrementa `SKIP`, nunca toca `GATE_OK`). Presente e incompleto o rojo → `paso_fail` **con** `"no-gate"`: el alumno ve el ✖ y la guía a la que volver, pero el veredicto del núcleo no se mueve. El salto de `04-` a `99-` señala "esto está fuera del camino obligatorio" antes de abrir el archivo. Lo opcional es visible sin ser punitivo: se puede fallar sin reprobar.

**Traslado a Spring Boot:** `90-validar.sh` trata `desafio/` con contador aparte y la misma semántica de tres estados. En Maven, el desafío vive en un perfil (`-Pdesafio`) o un `@Tag("desafio")` excluido del `verify` por defecto; el script lo corre, reporta su resultado y **nunca** lo suma a `GATE_OK`. Regla derivada: si un chequeo puede bajar el veredicto, no es opcional — y si es opcional, se pasa `no-gate` explícito, no se omite el chequeo.

---

### P-16 · El antes y el después conviven, y el CI exige que ambos pasen

**Evidencia:** `labs/lab-08-la-gran-refactorizacion/solucion-refactor/` contiene `antes/flujo-declaracion.cy.js` y `despues/flujo-declaracion.cy.js` — mismo nombre, dos mundos. Cabecera del primero (líneas 2-7):
```js
//  [ANTES] flujo-declaracion.cy.js — SIN Page Objects ni cy.login (el "pecado")
//     FUNCIONA (pasa en verde) pero es largo y frágil: si cambia la pantalla de
//     acceso o el data-cy de un campo, hay que editar AQUÍ (y en 13 archivos más).
```
Y el validador **exige que los dos estén verdes**, `bin/validar-lab.sh:125-131`:
```bash
#  7 · El refactor preserva comportamiento: antes/ y despues/ AMBOS verdes
    paso_ok "solucion-refactor: antes/ y despues/ ambos en verde (mismo comportamiento)"
```

**Por qué funciona:** el `antes/` **pasa en verde** (42 líneas) y el `despues/` también (34 líneas, ~20 % menos; su cabecera invita: *"Cuenta las líneas"*). Que ambos pasen es la definición ejecutable de refactorizar: **cambiar la estructura sin cambiar el comportamiento**. Si el `antes/` estuviera roto, el alumno concluiría que se refactoriza porque el código *falla*, y aprendería la lección equivocada — la misma trampa que el Acto 2 de P-10 desactiva. La Guía 01 lo explota como ejercicio de lectura (`guia/01-la-deuda-en-la-pizarra.md:36`: *"¿Cuántas líneas tiene el `beforeEach` de `antes/`? ¿Y el de `despues/`?"*).

**Traslado a Spring Boot:** el Lab 05 versiona `solucion-con-n1/` y `solucion/` con **la misma suite de tests** y distinto conteo de queries. El validador corre las dos y exige verde en ambas; la diferencia se mide, no se narra: un test con `@SqlCount`/`datasource-proxy` asevera *N+1 queries* en la primera y *1* en la segunda. El alumno diffea su propia deuda, y el CI garantiza que el refactor no cambió comportamiento.

---

### P-17 · El entorno del alumno tiene documento propio, correlato humano del CI

**Evidencia:** `docs/entorno-windows.md:1-7`
```
# Entorno Windows — Git Bash (guía verificada por CI)
> [...] Lo que aquí se describe está **verificado por el CI** en runners
> `windows-latest`, donde `bash` **es Git Bash** — exactamente tu entorno.
```
Y el doc registra lo que el CI **no** puede atrapar (líneas 23-26):
```
> ⚠️ **`nvm-windows` NO lee `.nvmrc` automáticamente.** [...] El `.nvmrc` del repo es
> respetado por `nvm` (Mac/Linux) y por el CI, pero no por `nvm-windows`.
```

**Por qué funciona:** el título reclama una garantía —*"verificada por CI"*— y la matriz de P-12 la respalda: el doc no es una promesa, es la lectura humana de un job que corre en cada push. El párrafo del `nvm-windows` es la otra mitad, y la más honesta: marca con precisión la **frontera** donde el CI deja de cubrir al alumno (el runner tiene Node preinstalado; la máquina del alumno, no). Un doc de entorno que solo dijera lo que funciona sería una trampa.

**Traslado a Spring Boot:** `docs/entorno-alumno.md`, encabezado *"guía verificada por CI"*, respaldado por la matriz `os × java` de P-12. Documenta el modo `--sin-docker` (decisión **D-007**, aún no registrada en `decisiones.md` — ver reporte de SPEC-002) y las fronteras que el CI no cubre: `JAVA_HOME` en Windows, el wrapper `mvnw.cmd` vs `mvnw`, y Testcontainers exigiendo Docker Desktop. Regla: **lo que el CI verifica, el doc lo explica; lo que el CI no puede verificar, el doc lo advierte.**

---

### P-18 · La siembra es un invariante estructural, no un título

**Evidencia:** el invariante se cumple en los 12 labs con sucesor, pero con **dos** encabezados distintos:
```
labs/lab-01-tu-primer-dia-en-la-dgt/TEORIA.md:314   ## 9. Conclusiones y siembra del Módulo 2
labs/lab-08-la-gran-refactorizacion/TEORIA.md:247   ## 12. Conclusiones y siembra del Módulo 9
labs/lab-11-la-flota-de-fiscalizacion/TEORIA.md:248 ## ⏭️ La recta final (siembra M12)
labs/lab-13-el-framework-de-la-dgt/TEORIA.md:131    ## 8. Cierre
```
El Lab 13 no siembra: no hay Módulo 14. (Ver §5 y P-09, del que este es el corolario verificable.)

**Por qué funciona:** una regla de CI escrita contra el **título** (`grep "Conclusiones y siembra del Módulo"`) daría falso rojo en cuatro labs correctos y presionaría a renombrarlos — el tooling dictándole el tono a la pedagogía. Escrita contra el **invariante** (existe una sección de siembra, salvo en el último lab), pasa en los 13 y sigue atrapando el olvido real. Es exactamente la lección de A-01 aplicada al propio material: no confundir la forma del texto con la propiedad que importa.

**Traslado a Spring Boot:** regla de CI **registrada, no implementada** — se activa cuando exista `material-ci.yml` (SPEC posterior). Especificación: para cada `labs/lab-NN/TEORIA.md` con sucesor, exigir una sección que contenga el patrón `siembra` y la referencia al módulo `N+1`; el último lab queda exento por lista explícita, no por silencio. La detección es por patrón, jamás por título literal.

---

## 3. Anti-herencias

### A-01 · Verificar código fuente con `grep` — frágil por construcción

**Evidencia del defecto:** `labs/lab-13-el-framework-de-la-dgt/bin/validar-egreso.sh:52`
```bash
if grep -rq "Cypress.Commands.add('login'" "$SUPPORT" 2>/dev/null; then
```
Las **comillas simples están hardcodeadas dentro del patrón**. Un alumno que escriba `Cypress.Commands.add("login", ...)` —JavaScript idéntico, comillas dobles— falla el núcleo del egreso. Lo mismo en las líneas 62, 67, 91, 96 y 118-122: `grep -rqE "cy\.(intercept|request)\("`, `grep -rq "checkA11y"`, `grep -rqE "\.forEach\(.*=>|CASOS"`. El último es especialmente frágil: cualquier `.forEach(` en cualquier archivo cuenta como "extensión data-driven".

**Por qué es un defecto:** un `grep` sobre código fuente confunde *la forma del texto* con *la propiedad del programa*. Da falsos negativos (comillas dobles, salto de línea, `const login = ...` extraído a una constante) y falsos positivos (la palabra `flaky` dentro de un comentario cuenta como "manejo de flaky", línea 122). El validador termina evaluando estilo de tipeo, no comportamiento — y castiga al alumno que escribe código correcto de otra forma.

**Traslado a Spring Boot:** **bash solo orquesta; el criterio se verifica ejecutando.**
- ¿Existe el comando/bean? → el test lo inyecta y lo llama. Si no existe, no compila.
- ¿Se respeta la arquitectura? → ArchUnit (`noClasses().that().resideInAPackage("..controller..").should().dependOnClassesThat().resideInAPackage("..repository..")`).
- ¿Hay cobertura del flujo crítico? → JaCoCo con `<rule>` sobre el paquete, no `grep` de nombres de test.
- ¿Se prohibió `Thread.sleep` en tests? → regla de Checkstyle/ArchUnit, que entiende sintaxis; nunca `grep sleep`.

Único uso legítimo de `grep` en el validador: detectar la **presencia de texto que es texto** (una línea en un `.yml`, una sección en el reporte markdown del alumno).

---

### A-02 · Declarar "sin tests flaky" sin ningún mecanismo que lo mida

**Evidencia del defecto:** la rúbrica lo exige, `rubrica/rubrica-evaluacion.md:30`
```
| **Insuficiente** | Hay `cy.wait(<número>)`, aserciones dentro de Page Objects, selectores frágiles [...], o tests flaky. |
```
Pero **nada lo mide**. Lo único que el validador hace con la palabra es contarla como *extensión*, `bin/validar-egreso.sh:122`:
```bash
if grep -rqiE "flaky|reintent|retries" "$E2E" "$DIR/cypress.config.js" 2>/dev/null; then info "🔵 Manejo de flaky / retries (M8)"; EXT=$((EXT+1)); fi
```
Búsqueda de un mecanismo de repetición en todo el tooling (`grep -rniE "for .*(1\.\.3|seq |repeat)|3 veces|--repeat" bin/ labs/*/bin/ .github/workflows/`): **una sola coincidencia**, `material-ci.yml:148 → for _ in $(seq 1 30);` — que es el bucle del *health-check*, no una medición de flakiness.

Peor: la solución de referencia del Lab 08 activa `retries: { runMode: 2, openMode: 0 }` (`labs/lab-08-.../solucion/cypress.config.js:10`). Los reintentos **enmascaran** la flakiness que la rúbrica dice castigar. El propio comentario del archivo lo admite (línea 2: *"retries como RED de seguridad (no como cura)"*), pero ningún gate distingue un test verde de un test verde-al-segundo-intento.

**Por qué es un defecto:** una rúbrica con un criterio no medible es una rúbrica con un criterio opcional. El evaluador no puede sostenerlo ante un alumno que pregunta "¿cuál test es flaky?", y el alumno aprende que el requisito era decorativo — exactamente el pecado que la misma rúbrica llama "gate decorativo" (ver P-05). La contradicción está a 15 líneas de distancia en el mismo archivo.

**Traslado a Spring Boot:** el script `91` **mide** la flakiness en vez de nombrarla.
```bash
# 3 corridas de la misma suite, mismo commit, mismo seed.
# Si el conjunto de tests verdes difiere entre corridas → hay flaky. exit 1.
for i in 1 2 3; do mvn -q verify -Dsurefire.reportsDirectory=target/run-$i; done
diff <(resumen target/run-1) <(resumen target/run-2) && diff <(resumen target/run-2) <(resumen target/run-3)
```
Reglas que se derivan: **`maven-surefire-plugin` con `rerunFailingTestsCount` prohibido en el material** (es el `retries` de Cypress), y `@RepeatedTest` disponible para el alumno como herramienta de diagnóstico, no como parche. Si la rúbrica del curso de Spring Boot va a decir "sin tests flaky", el `91` lo prueba; si no puede probarlo, la línea se borra de la rúbrica.

---

## 4. No verificado

### N-01 · La "trampa registrada": casilla `¿Consultaste la solución?` — **NO EXISTE** en ninguna plantilla

Hipótesis A3 del arquitecto. Se citaba de segunda mano. **Confirmada su ausencia.**

Comandos ejecutados (cwd = `/Users/rodrigosilva/SII/Copia de CYPRESS/cypress-sii-2026`):

```console
$ grep -rn -i "consultaste" labs/ docs/ 2>/dev/null; echo "EXIT=$?"
EXIT=1

$ grep -rn -iE "consult(aste|é|e) la soluci" labs/ docs/ 2>/dev/null; echo "EXIT=$?"
EXIT=1

$ grep -rn -i "soluci" labs/*/plantillas/ | head
(sin salida)

$ find labs/*/plantillas -type f | wc -l
      13
```

Es decir: los 13 archivos de plantilla **no mencionan la palabra "solución" ni una sola vez**. Ninguna plantilla pregunta al alumno si consultó la respuesta.

Lo que sí existe en las plantillas son casillas de **verificación ejecutable**, no de confesión:
```
labs/lab-09-.../plantillas/reporte-entregable.md:61  - [ ] `./bin/validar-lab.sh` → **🏆 LAB APROBADO**
labs/lab-13-.../plantillas/reporte-egreso.md:52      - [ ] `./bin/validar-egreso.sh --proyecto <tu-carpeta>` → **Núcleo: APROBADO**
```

**Lectura:** la práctica atribuida al curso no está en el curso. El mecanismo real de honestidad no es preguntarle al alumno si hizo trampa —una casilla que el tramposo no marca—, sino que **el mismo validador juzgue `starter/` y `solucion/`** (P-14) y que el reporte exija transcripciones literales del error (P-11), que solo puede producir quien corrió el escenario.

**Decisión para Spring Boot:** no se hereda la casilla (no existe). Se hereda el mecanismo que sí existe. Si se quisiera una señal de auto-reporte, se diseña de cero y se registra como decisión propia — no como herencia.

---

## 5. Resumen de la verificación

| Hipótesis | Estado |
|---|---|
| H1, H2, H3, H4, H5, H6, H7, H8, H10, H11, H12, H13, H14 | ✅ Confirmadas con cita textual |
| H9 | ⚠️ Invariante confirmado (12/12 labs con sucesor); **encabezado literal refutado** (labs 09–12 usan otro título) |
| A1, A2 | ✅ Defecto confirmado; anti-herencia justificada |
| A3 | ✅ Ausencia confirmada (§4 N-01) — declarada, no omitida |

**Addendum SPEC-002.** P-15…P-18 no provienen de las hipótesis del arquitecto sino de hallazgos del ejecutor durante la verificación, adoptados por decisión del PO y sometidos al mismo estándar de evidencia: ruta, cita y traslado. El origen no rebaja el rigor.
