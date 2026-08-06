# SPEC-007 · Lab 01 «Del otro lado del botón»

| Campo | Valor |
|---|---|
| ID | SPEC-007 |
| Título | Primer laboratorio del curso: el crimen de la credencial versionada (S01 · M1 2,0 h + M2 1,0 h) |
| Autor | Arquitecto |
| Aprueba | PO (Rodrigo) — **APROBADA** (capa humana presentada y aceptada) |
| Depende de | SPEC-006 (Lab 00 y `lib-comunes.sh`) |
| Estado | LISTA PARA EJECUCIÓN |

> **Instrucción de ejecución (mocito):** primer paso, guardar este archivo íntegro en
> `docs/specs/SPEC-007-lab01-del-otro-lado-del-boton.md` y commitearlo en rama antes de
> ejecutar. Rige el protocolo de dos etapas: tú pruebas todo primero con evidencia
> citada; la Prueba del PO cierra.

---

## §0 · Resoluciones previas (ejecutar antes del lab)

1. **Merge del PR #5.** El PO decidió diferir su prueba del Lab 00: el PR se mergea con
   ese criterio **abierto y declarado** (precedente del tag v0.1.1). La prueba pendiente
   del PO entra al `ESTADO.md`, sección "Qué falta".
2. **Contenedores de Testcontainers** (tu anotación 1): `99-destruir.sh` (el del Lab 00
   y todos los futuros) **no los toca** — no los creó él — pero si los detecta, imprime
   `[INFO]` apuntando a una entrada nueva de troubleshooting (T-NN) que explica qué son
   y cómo limpiarlos (`docker ps --filter label=org.testcontainers` + el comando). El
   "Todo quedó como estaba" se refiere, explícitamente, a lo que el lab creó.
3. **Regla de mutabilidad** (tu anotación 2, ratificada): *una SPEC es inmutable desde
   que **cierra** (merge de su PR), no desde que se commitea; mientras su PR vive, se
   anota dentro.* Agregar al Protocolo SPEC del README.

## §1 · Objetivo

Que exista `labs/lab-01-del-otro-lado-del-boton/` completo y verificado: la primera
sesión real del curso (S01 de la matriz: M1 completo + primera hora de M2). El alumno
sale con: la app corriendo en su máquina, el crimen de la credencial entendido y
corregido de verdad (no cosméticamente), perfiles dev/test/prod operativos, propiedades
tipadas con record validado, y su primer contacto con el contrato REST del tronco.

## §2 · El crimen (los primeros 10 minutos, los ejecuta el relator)

En el `starter/` de este lab, `application.yml` contiene una credencial con cara de
producción (`spring.datasource.password: Dgt2026Pr0d!` y una URL a un host
`prod-db.dgt.gob.cl` ficticio) — **plantada a propósito, con un commit propio en la
historia del starter** cuyo mensaje inocente ("ajustes de conexión") es parte de la
escena. Guion del crimen (va en `INSTRUCTOR.md`, minutado):

1. El relator abre el repo "recién entregado por el practicante". Todo funciona.
2. `git log --oneline -- src/main/resources/application.yml` → ahí está el commit.
3. `git show <sha>` → la contraseña, en pantalla, proyectada.
4. La frase de Carolina: *"Alguien ya la 'borró'. Muéstrame que entiendes por qué eso
   no arregló nada."* — porque el starter que recibe el alumno **ya no tiene** la
   contraseña en el archivo vigente: solo en el historial. El crimen del Lab 01 es que
   **borrar no es remediar**.

**Restricción de seguridad del material:** la credencial plantada es obviamente ficticia
(dominio `.gob.cl` inexistente, password de utilería) y el `README` del lab lo declara.
No se usa ninguna credencial real ni con formato de proveedor real (nada que parezca
AWS/GitHub token: los scanners de secretos del alumno corporativo no deben aullar).

## §3 · Los tres actos (P-10)

- **Acto 1 · Choque:** el secreto está en el historial; el archivo vigente "está limpio".
- **Acto 2 · El parche bruto que FUNCIONA:** la guía 03 hace que el alumno "resuelva"
  moviendo la credencial a `application-dev.yml`... que también está trackeado. El
  validador lo deja pasar en lo funcional (la app arranca) y la guía lo confronta:
  ¿qué cambió de verdad? ¿quién puede leer ese archivo? ¿y el historial?
- **Acto 3 · La forma correcta:** credenciales por variables de entorno / properties
  externas; `application.yml` con placeholders (`${DGT_DB_PASSWORD}`); los tres
  perfiles; y la respuesta a "¿y el historial?": **la credencial expuesta se rota, no
  se borra** — reescribir la historia queda mencionado como cirugía mayor con su costo
  (la regla sagrada: la historia no se reescribe), la rotación como la respuesta
  profesional. El lab lo enseña conceptualmente; la "rotación" en el lab es cambiar la
  clave de la BD local del compose.

## §4 · Los TODOs (presupuesto: 4 × ~15 min)

1. **TODO_1** — Externalizar la conexión: placeholders + variables de entorno; la app
   arranca sin ninguna credencial en archivos trackeados.
2. **TODO_2** — Perfiles `dev` / `test` / `prod`: cada uno con su fuente de
   configuración; `prod` **falla rápido y claro** si falta la variable (no arranca con
   defaults silenciosos).
3. **TODO_3** — `DgtProperties`: record inmutable con `@ConfigurationProperties`
   (`institucion`, `folio.prefijo`, `folio.largo`), validado (`@NotBlank`, `@Min`),
   consumido por un servicio existente (reemplaza un `@Value` plantado que la guía
   señala como olor).
4. **TODO_4** — (la hora de M2) Primer endpoint propio del alumno:
   `GET /api/tramites/{id}` devolviendo DTO record (sin entidad — AU-01/AU-02 ya
   vigilan) con 404 `ProblemDetail` para id inexistente, siguiendo el estilo de
   referencia del tronco.

Cada TODO con la anatomía de SPEC-000 §7.3 (Javadoc → andamio → qué/porqué/RN →
Pista 2 inline → `UnsupportedOperationException("{{TODO_N}}")` donde aplique).

## §5 · Anatomía y andamiaje

Estructura completa de SPEC-000 §7.6. Puntos específicos:

- **`starter/` y `solucion/`** son proyectos Maven completos derivados del tronco
  (`dgt-tramites-api` en su estado SPEC-005 + el crimen plantado). Declara en el
  reporte cómo materializaste la derivación (copia con script, subtree, lo que elijas)
  porque **se vuelve el mecanismo de encadenamiento de los 12 labs** — y recuerda el
  compromiso: `solucion/` del Lab 01 será el `starter/` del Lab 02.
- **Tests del enunciado** en `src/test/java/**/enunciado/**`: verifican los 4 TODOs
  (arranque por perfil sin credenciales trackeadas incluye un test que **falla si algún
  archivo trackeado contiene la contraseña de utilería** — ese chequeo es un test JUnit
  leyendo el árbol de recursos, no un grep del validador: A-01). Manifiesto
  `manifiesto-tests.sha256` acotado a `enunciado/`.
- **`bin/`**: hereda `lib-comunes.sh`. `90-validar.sh` con `--dir starter|solucion`
  (mismo criterio para ambos), de solo lectura, contadores dinámicos.
  `91-e2e.sh`: el ciclo canónico (starter virgen → exit 1 → aplica solución →
  exit 0). `95-recuperar.sh` (respalda antes de sobrescribir). `99-destruir.sh` con
  la política de §0.2. `start-lab.sh` en el puerto de la casa (8099, de la constante).
- **`plantillas/reporte-entregable.md`**: incluye la **trampa registrada** (*"¿Consultaste
  `solucion/`? ¿En qué actividad y por qué?"*) y al menos una pregunta de transcripción
  literal de un error (P-11) — la natural aquí: el arranque en `prod` sin la variable.
- **`TEORIA.md`**: M1 (contenedor IoC, autoconfiguración, configuración externalizada,
  perfiles, `@ConfigurationProperties`) + la primera hora de M2, con analogías propias
  (no recicladas de otros cursos), DO/DON'T, glosario, y **siembra del Lab 02**: el
  endpoint del TODO_4 devuelve un DTO... ¿y si alguien, apurado, devolviera la entidad?
  *"La próxima semana alguien lo hace. Y se filtra un folio."*
- **`INSTRUCTOR.md`**: orden minutado de la sesión 1 (10' crimen / ~40' teoría / 10'
  café / ~110' lab / 10' cierre), el guion del crimen (§2), "el error que cometerá la
  sala" (predicción: olvidarán exportar la variable y culparán a Maven), y qué revisar
  en los reportes.

## §6 · Verificación del ejecutor (etapa 1)

Con salidas citadas, sobre estado limpio:

1. `91-e2e.sh` completo: starter virgen falla el `90` (exit 1, citando **qué** falla),
   solución aplicada pasa (exit 0). El ciclo dos veces (idempotencia).
2. `90 --dir solucion` en modo espectador: veredicto completo citado.
3. El test anti-credencial muerde: planta la contraseña de utilería en un archivo
   trackeado de la solución (copia de trabajo, no el repo) y cita el rojo.
4. La app de la solución arranca en los tres perfiles; `prod` sin variable **falla
   rápido con mensaje claro** (citado — esa salida es material del reporte del alumno).
5. El crimen es visible: `git log` + `git show` del starter muestran la credencial en
   el historial y su ausencia en el archivo vigente (citado — es el guion del relator).
6. ArchUnit del starter y de la solución: verdes (los 7 + fixtures siguen vigentes en
   ambos derivados).
7. Manifiesto: alterar un test de `enunciado/` (copia) rompe el `90`; agregar un test
   propio fuera de `enunciado/` **no** lo rompe.
8. CI verde (el job `siembra` ahora tiene un lab real que auditar — recuerda: el Lab 01
   sin sucesor existente queda exento por la regla, pero su TEORIA.md **ya debe
   sembrar**; cita cómo quedó resuelto en el check).
9. `ESTADO.md` actualizado (incluida la prueba pendiente del PO sobre el Lab 00).

## §7 · La Prueba del PO (etapa 2 — cierra la SPEC)

Invitación literal al final del reporte, con resultados esperados escritos:

```
Rodrigo, tu turno (15 minutos) — vas a vivir el crimen como lo verá la sala:
  cd /Users/rodrigosilva/SII/SPRINGBOOT && git checkout <rama> && git pull
  cd labs/lab-01-del-otro-lado-del-boton

  1) Lee README.md (5 min — la historia de Carolina)
  2) cd starter && git log --oneline -- src/main/resources/application.yml
     git show <sha-que-te-indicará-el-README>
     → esperas: VER LA CONTRASEÑA EN PANTALLA. Ese es el momento de la clase.
  3) cd .. && ./bin/90-validar.sh --dir solucion
     → esperas: [OK] N/N — Carolina aprueba. Por ahora.

Si algo NO sale como lo escrito, es un bug mío: pégame la salida tal cual.
```

(El PO puede además correr el lab entero si quiere; estos tres pasos son el mínimo de
aceptación. Su prueba pendiente del Lab 00 puede hacerse en la misma sentada.)

## §8 · Criterios de aceptación

- [ ] §0 ejecutado (merge PR #5, política Testcontainers con su T-NN, regla de
      mutabilidad en el README).
- [ ] SPEC-007 commiteada antes que el material; trabajo por rama + PR.
- [ ] Lab 01 completo según §2–§5 (anatomía íntegra: las 5 piezas no negociables).
- [ ] Toda la evidencia de §6 citada, sabotajes incluidos.
- [ ] Los 4 TODOs cumplen la anatomía y el presupuesto (~15 min c/u — declara tu
      estimación honesta por TODO).
- [ ] `TEORIA.md` siembra el Lab 02; CI `siembra` verde con el lab presente.
- [ ] `ESTADO.md` al día. Bitácora: una fila por el nacimiento del Lab 01.
- [ ] **Prueba del PO reportada** — cierra la SPEC; hasta entonces el PR espera.
- [ ] Commits `SPEC-007:`; checks verdes citados.

## §9 · Reporte

Evidencia completa de §6, estimación de tiempos por TODO, la decisión del mecanismo de
derivación starter/solución (§5) explicada, URL del run, `git log --oneline`,
discrepancias y hallazgos — sin tocarlos. Cierra con la invitación del §7.
