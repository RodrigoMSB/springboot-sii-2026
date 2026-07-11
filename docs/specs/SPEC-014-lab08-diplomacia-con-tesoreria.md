# SPEC-014 · Lab 08 «Diplomacia con Tesorería»

| Campo | Valor |
|---|---|
| ID | SPEC-014 |
| Título | Octavo laboratorio: el servicio externo que mata a la API — timeouts, clientes declarativos y endurecimiento (S08 · M9 1,0 h + M10 2,0 h) |
| Autor | Arquitecto |
| Aprueba | PO (Rodrigo) — **APROBADA** |
| Depende de | SPEC-013 |
| Estado | LISTA PARA EJECUCIÓN |

> **Instrucción de ejecución (mocito):** guardar este archivo íntegro en
> `docs/specs/SPEC-014-lab08-diplomacia-con-tesoreria.md` y commitearlo en rama antes
> de ejecutar. Apila sobre la pila y decláralo. Protocolo de dos etapas vigente.

---

## §1 · Objetivo

Que exista `labs/lab-08-diplomacia-con-tesoreria/`: la sesión donde la DGT aprende a
hablar con extraños sin morir en el intento. Entra en escena **TESO** (WireMock, D-002):
la confirmación de pago que mueve el trámite de PRESENTADO a PAGADO. El alumno sale
con: timeouts dirigidos, degradación elegante, el cliente migrado a `@HttpExchange`, y
la API endurecida (CORS, cabeceras — la hora de M9 diferida del Lab 07). Encadenamiento:
`starter/` = `solucion/` del Lab 07 + el cliente ingenuo + los huecos.

## §2 · El crimen

El `starter/` trae el cliente de TESO ingenuo: `RestClient` sin timeout. Guion del
relator (10 min):

1. `./bin/start-lab.sh --dir starter --teso-lento 30000` (bandera P-04: configura el
   `fixedDelay` de WireMock; WARN condicional cuando el delay supera el timeout — o su
   ausencia: *"[WARN] Con TESO a 30 s y sin timeout, el pool se agota. Es el escenario
   de la Guía 02."*).
2. Tres o cuatro confirmaciones de pago disparadas en paralelo. Todas esperando.
3. El golpe: `curl` a `GET /api/v1/tramites` — **un endpoint que no tiene nada que ver
   con pagos** — colgado. La API entera, muerta, esperando a Tesorería.
4. Carolina: *"TESO se cayó a las 9. A las 9:02 nosotros también — y nosotros no
   tenemos nada malo. Explícame cómo el problema de OTRO servicio se volvió NUESTRA
   caída. Y después haz que no vuelva a pasar."*

La lección estructural del lab: **sin timeout, el hilo de la petición es un rehén; con
el pool lleno de rehenes, la app está secuestrada por su dependencia.**

## §3 · Los tres actos

- **Acto 1 · Choque:** la API completa colgada por un servicio ajeno.
- **Acto 2 · El parche bruto que FUNCIONA:** agrandar el pool de hilos (o subir un
  timeout global gigante). ¡Funciona! Con más hilos, aguanta más pagos colgados… La
  guía lo mide: con pool N, bastan N+1 pagos lentos y la API muere igual — solo se
  compró tiempo pagando memoria, y **todos** los endpoints pagan el peaje de un
  timeout pensado para uno. Posponer no es resolver.
- **Acto 3 · La forma correcta:** timeout **corto y dirigido** en el cliente de TESO
  (connect y read por separado, con la razón de cada número en el Javadoc),
  degradación elegante (el pago no confirmable responde 503 `ProblemDetail` con
  reintento sugerido — la API viva, honesta y rápida en su mala noticia), y el cliente
  migrado a `@HttpExchange` declarativo. La **escalera colapsada** (P-06): las formas
  acumuladas de llamar a TESO terminan en un solo puerto (`TesoreriaPort`) con su
  implementación declarativa — la teoría muestra el diagrama de qué guía aportó cada
  peldaño.

## §4 · Los TODOs (4 × ~15 min)

1. **TODO_1 — El timeout dirigido:** connect + read cortos en el cliente TESO. Test
   del enunciado (WireMock con delay > timeout): la llamada falla **rápido** (medido:
   cota superior de tiempo en la aserción) **y la API sigue viva** — el mismo test
   golpea otro endpoint en paralelo y exige respuesta. La aserción doble es el corazón.
2. **TODO_2 — La mala noticia elegante:** la falla del cliente se traduce a 503
   `ProblemDetail` (tipo propio, sin stacktrace, con la sugerencia de reintento);
   el trámite queda íntegro (no avanza de estado). Test del shape exacto.
3. **TODO_3 — La escalera colapsada:** migrar a `@HttpExchange` (interfaz + registro
   por grupos, la config de timeouts viaja con él). **La suite del enunciado no cambia
   y sigue verde**: refactor que preserva comportamiento — el alumno ya sabe (Lab 05)
   que eso se demuestra, no se promete.
4. **TODO_4 — El endurecimiento (M9):** CORS explícito para el origen de Mi DGT
   (permitido nominal, no `*`) + cabeceras de seguridad. Tests: preflight desde el
   origen permitido pasa; desde un origen intruso, no; cabeceras presentes en la
   respuesta.

En teoría y andamio (lectura, no tecleo): la mitigación **SSRF** del cliente HTTP de
Boot 4.1 (señalada en la config con su porqué), el estado real de **Feign**
(mantenimiento: criterio para sistemas viejos vs desarrollos nuevos — la comparativa
práctica es demo del relator en INSTRUCTOR.md, no TODO), y el **rate limiting** básico
como desafío `99-` (limitar el login; SKIP si no se hace, P-15).

## §5 · Anatomía

La de SPEC-000 §7.6 completa. El compose del lab suma el contenedor WireMock (tag
fijado del stack; re-verifica el patch vigente) con sus mappings versionados —
declara la divergencia en el allowlist de derivación. `TEORIA.md`: M10 (panorama
RestClient/WebClient/RestTemplate con criterio, timeouts como presupuesto de espera,
HTTP Interfaces y registro por grupos, Feign, SSRF, degradación) + la hora de M9
(CORS contado como el portero de los navegadores — quién llama a quién y por qué el
`*` es rendirse; CSRF: por qué aquí se deshabilita con razón y cuándo sería
negligencia; cabeceras). **Siembra del Lab 09:** *"TESO ya no puede matarnos. Pero
anoche alguien emitió un folio al contribuyente equivocado, y Carolina llegó con 400 MB
de logs y una sola pregunta: encuéntralo. La próxima semana traes lupa."* Plantillas
con trampa registrada y la transcripción natural: **el 503 elegante completo** (la
pregunta de criterio: *"¿por qué responder 503 rápido es mejor servicio que intentarlo
30 segundos?"*).

## §6 · Verificación del ejecutor (etapa 1)

Citado, sobre estado limpio: (1) `91-e2e` ×2 — starter falla nombrando los 4 TODOs,
solución 100 %; (2) el crimen vivido: en el starter con `--teso-lento`, los pagos
colgados y el endpoint ajeno colgado (citado con tiempos); en la solución, el pago
responde 503 rápido (tiempo citado) y el endpoint ajeno vive; (3) acto 2 medido: pool
agrandado en copia → cita que con N+1 pagos muere igual; (4) el WARN condicional
aparece sobre el umbral y no bajo él; (5) TODO_3 verificado como refactor: misma suite
verde antes y después de la migración a `@HttpExchange` (cítalo — es P-16 sin segunda
carpeta); (6) CORS medido: preflight permitido vs intruso, ambos citados; (7)
manifiesto discrimina; (8) `deriva` (con el compose divergente declarado) y `siembra`
(audita L7→L8) verdes en el runner; (9) CI verde, run citado; (10) `ESTADO.md` al día;
estimación honesta por TODO.

## §7 · La Prueba del PO (etapa 2 — cierra la SPEC)

Invitación literal, resultados esperados escritos, tres pasos: (1) starter con
`--teso-lento`: disparar los pagos y **sentir la API entera colgada** con un curl a
trámites; (2) solución, mismo sabotaje: el 503 elegante al tiro y trámites vivo — *la
mala noticia rápida es buen servicio*; (3) `90 --dir solucion` → aprobado. Declara
Java/Docker por paso. Recuerda la fila acumulada del PO (Labs 00–07).

## §8 · Criterios de aceptación

- [ ] SPEC commiteada antes del material; rama + PR apilado y declarado.
- [ ] Lab 08 completo (las 5 piezas); evidencia §6 íntegra, actos medidos.
- [ ] La aserción doble del TODO_1 (falla rápido Y la API vive) presente y verde.
- [ ] TODO_3 demostrado como refactor (suite idéntica verde antes/después).
- [ ] WireMock con tag fijado y mappings versionados; divergencia declarada.
- [ ] TODOs ≤ ~15 min c/u (o recorte aplicado y declarado).
- [ ] Siembra L7→L8 auditada; Lab 08 siembra el 09 con el gancho de los 400 MB.
- [ ] `ESTADO.md` y bitácora al día.
- [ ] **Prueba del PO reportada** — cierra la SPEC.
- [ ] Commits `SPEC-014:`; checks verdes citados.

## §9 · Reporte

Evidencia de §6, tiempos por TODO, decisiones declaradas (números de timeout elegidos y
su porqué, forma del registro por grupos, qué quedó en el desafío 99), URL del run,
`git log --oneline`, discrepancias y hallazgos — sin tocarlos. Cierra con la invitación
del §7.
