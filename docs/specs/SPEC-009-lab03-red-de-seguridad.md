# SPEC-009 · Lab 03 «Red de seguridad»

| Campo | Valor |
|---|---|
| ID | SPEC-009 |
| Título | Tercer laboratorio: la suite llega en rojo — los tests son el enunciado (S03 · M3 1,5 h + M4 1,5 h) |
| Autor | Arquitecto |
| Aprueba | PO (Rodrigo) — **APROBADA** |
| Depende de | SPEC-008 |
| Estado | LISTA PARA EJECUCIÓN |

> **Instrucción de ejecución (mocito):** guardar este archivo íntegro en
> `docs/specs/SPEC-009-lab03-red-de-seguridad.md` y commitearlo en rama antes de
> ejecutar. Apila sobre PRs abiertos si corresponde y decláralo. Protocolo de dos
> etapas vigente.

---

## §1 · Objetivo

Que exista `labs/lab-03-red-de-seguridad/`: la sesión que invierte la relación del
alumno con los tests. Aquí **no se le pide que escriba tests para su código: se le
entrega una suite en rojo (~14 tests, cifra final a tu juicio) y los tests SON el
enunciado.** El alumno sale con: validaciones declarativas y un validador propio de RUT
chileno, el manejo global de errores completo, sus primeros tests unitarios con Mockito
escritos por él, y AU-05 vigilando. Encadenamiento: `starter/` = `solucion/` del Lab 02
+ la suite roja + los huecos.

## §2 · El crimen

No hay endpoint roto: hay una **bandeja de entrada**. Guion del relator: Carolina abre
el repo, corre el `90`, y la pantalla se llena de rojo. *"Antes de que preguntes: no
está roto. Esto es lo que el equipo de QA acordó con nosotros — cada test rojo es un
compromiso que aún no cumplimos. No me traigas código: tráeme verde. Y ojo: los tests
se leen. El que implementa sin leer el test, implementa otra cosa."*

Los tests del enunciado llevan `@DisplayName` narrativo en español (*"un RUT con dígito
verificador falso se rechaza con 400 y nombra el campo"*) — la suite se lee como una
especificación, porque lo es.

## §3 · Los tres actos

- **Acto 1 · Choque:** ~14 rojos. La sala no sabe por dónde partir. La guía 02 enseña a
  leer un test como contrato (Arrange–Act–Assert al revés: primero el Assert).
- **Acto 2 · El parche bruto que FUNCIONA:** la guía hace que el alumno ponga en verde
  UN test hardcodeando el valor esperado (el validador de RUT que retorna `true` para
  el caso feliz del test). Pasa. Y dos tests más abajo, el caso parametrizado con 6
  RUTs lo desmiente: **los tests triangulan** — engañar a uno es fácil; engañar al
  conjunto exige implementar de verdad. Esa es la lección de por qué una suite es una
  red y un test suelto es un hilo.
- **Acto 3 · La forma correcta:** implementar contra el contrato, test por test, con el
  `90` como marcador de progreso (`9/14… 12/14… 14/14`).

## §4 · Los TODOs (4 × ~15 min)

1. **TODO_1 — Validación declarativa:** el request de `POST /api/v1/tramites`
   (`CrearTramiteRequest` record) con `@Valid`, `@NotBlank`, `@Pattern`, etc.; los
   tests rojos exigen 400 `ProblemDetail` **que nombra los campos inválidos** (el
   advice ya existente se extiende para el detalle de campos).
2. **TODO_2 — `@RutValido`:** anotación propia + `ConstraintValidator` con el módulo 11
   chileno. Tests parametrizados del enunciado con RUTs válidos, inválidos y con
   formato basura. Mensaje internacionalizable en `ValidationMessages.properties`.
3. **TODO_3 — El error con contrato:** la transición ilegal de estado del trámite
   (máquina de estados del tronco) se mapea a `ProblemDetail` 409 con tipo propio;
   los tests exigen el shape exacto del JSON de error.
4. **TODO_4 — El alumno escribe tests (M4):** suite unitaria propia del servicio de
   trámites — mockear el repositorio, `ArgumentCaptor` para verificar qué se persiste,
   AssertJ, y la lección "qué no se mockea" (el andamio trae un mock del DTO como olor
   señalado a eliminar). Estos tests van **fuera** de `enunciado/` (territorio libre) y
   el `90` verifica que existen y pasan, sin manifiesto sobre ellos.

**AU-05** (`ningún test contiene Thread.sleep`) entra instalado en el enunciado de este
lab, con su fixture y meta-test heredados del patrón del tronco; TEORIA.md la explica
(la alternativa es Awaitility, ya en el classpath — su uso real llega con los labs
asíncronos).

## §5 · Anatomía

La de SPEC-000 §7.6 completa. `TEORIA.md`: resto de M3 (validaciones, validadores
propios, i18n de mensajes, advice y excepciones de dominio) + M4 (JUnit 6 — qué cambió
desde JUnit 5, AAA, nomenclatura narrativa, Mockito, mock/stub/spy, qué no mockear),
con la advertencia de contexto: *el alumno que googlee encontrará tutoriales de JUnit
4/5; qué señales lo delatan.* **Siembra del Lab 04:** *"la red ya existe... y la
próxima semana la app viene configurada 'como funcionaba': todo EAGER. Nadie lo notará.
Ese es el punto."* Plantillas con trampa registrada y transcripción literal (la
natural: el reporte de Surefire de un test parametrizado fallando, con el caso exacto).

## §6 · Verificación del ejecutor (etapa 1)

Citado, sobre estado limpio: (1) `91-e2e` ×2 — starter en rojo **con el conteo exacto**
y nombres legibles, solución 14/14; (2) el Acto 2 medido: hardcodea el validador en
copia y cita que el caso feliz pasa y los parametrizados lo desmienten; (3) TODO_4
verificado en ambos sentidos: la solución trae la suite propia pasando, y el `90`
sobre un starter al que solo le falta TODO_4 lo dice con claridad; (4) AU-05 muerde:
planta un `Thread.sleep` en un test de copia y cita el rojo; (5) manifiesto discrimina
(ya sabes el rito); (6) job `deriva` verde con el eslabón nuevo; job `siembra`
auditando ahora al Lab 02 de verdad — cita ambos; (7) CI verde, run citado;
(8) `ESTADO.md` al día; estimación honesta por TODO.

## §7 · La Prueba del PO (etapa 2 — cierra la SPEC)

Invitación literal, resultados esperados escritos, tres pasos: (1) correr
`90 --dir starter` y **ver la pantalla en rojo con ~14 compromisos nombrados en
español** — el momento de la clase; (2) abrir un test del enunciado indicado por el
README y leerlo como contrato (sin correr nada); (3) `90 --dir solucion` → `14/14 ·
Carolina aprueba. Por ahora.` Declara qué pasos necesitan Java 25/Docker. Recuerda las
pruebas pendientes acumuladas del PO con sus rutas.

## §8 · Criterios de aceptación

- [ ] SPEC commiteada antes del material; rama + PR (apilamiento declarado).
- [ ] Lab 03 completo (las 5 piezas); evidencia §6 íntegra, sabotajes incluidos.
- [ ] Los tests del enunciado con `@DisplayName` narrativo en español, legibles como
      especificación.
- [ ] TODOs ≤ ~15 min c/u (o recorte aplicado y declarado).
- [ ] AU-05 instalada con fixture y meta-test.
- [ ] Siembra Lab 02→03 auditada por CI; Lab 03 siembra el 04.
- [ ] `ESTADO.md` y bitácora al día.
- [ ] **Prueba del PO reportada** — cierra la SPEC.
- [ ] Commits `SPEC-009:`; checks verdes citados.

## §9 · Reporte

Evidencia de §6, tiempos por TODO, conteo final de tests del enunciado y por qué,
URL del run, `git log --oneline`, discrepancias y hallazgos — sin tocarlos. Cierra con
la invitación del §7.
