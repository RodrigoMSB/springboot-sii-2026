# ¿En qué va el curso?

*Una página, sin jerga. Si llevas dos semanas sin mirar el repo, empieza aquí.*
*Última actualización: SPEC-015.*

---

## 1 · Qué existe hoy

- **El temario definitivo** (v3, julio 2026): `docs/temario/`. Son 36 horas, 12 sesiones
  de 3, 15 módulos. El `.md` manda; el `.docx` es lo que se le entrega al SII.
- **La aplicación del curso**: `dgt-tramites-api/`. Es el backend de la DGT — lo que hay
  detrás del botón. Arranca, se conecta a su base de datos y responde. Tiene siete reglas
  de arquitectura que la vigilan, y cada regla trae una prueba de que muerde.
- **El pre-vuelo del alumno**: `labs/lab-00-estacion-base/`. El chequeo que hace en su casa
  antes de la sesión 1.
- **Lab 01** (`labs/lab-01-del-otro-lado-del-boton/`): una contraseña de producción en el
  historial de git — se rota, no se borra.
- **Lab 02** (`labs/lab-02-el-folio-que-se-filtro/`): un endpoint filtra el puntaje de riesgo
  de un contribuyente; se tapa con un DTO (lista blanca) y se instalan los guardianes ArchUnit.
- **Lab 03** (`labs/lab-03-red-de-seguridad/`): la suite llega en rojo — los tests son el
  enunciado. Validaciones, RUT chileno, errores con contrato, y los primeros tests Mockito.
- **Lab 04** (`labs/lab-04-el-arbol-de-tramites/`): todo en `EAGER` — un muro de JOINs. Se
  corrige a LAZY, se instala AU-04, y se planta la bomba del Lab 05.
- **Lab 05** (`labs/lab-05-once-segundos/`): el clímax. El N+1 medido con un contador de
  consultas, no contado. Dos soluciones conviven (P-16): `solucion-con-n1/` (13 consultas) y
  `solucion/` (3) — mismo comportamiento, distinto costo.
- **Lab 06** (`labs/lab-06-dos-folios-un-numero/`): la concurrencia. Dos emisiones a la vez se
  llevan el mismo folio; se resuelve con bloqueo pesimista (`SELECT … FOR UPDATE`) en la misma
  transacción, idempotencia por `tramiteId`, y la primera migración correctiva (un `CHECK` en
  `linea_f29`). RN-01, RN-02 y RN-05 por fin con suelo, probadas con concurrencia real.
- **Lab 07** (`labs/lab-07-el-portero/`): la seguridad. La API se cierra por defecto (Spring
  Security 7), hay login real contra la tabla de usuarios (BCrypt de la semilla), el JWT se
  valida por su firma (no se cree — un token adulterado da 401), y la emisión de folios exige
  el rol FUNCIONARIO (403 para el resto). El secreto de firma vive fuera del repo. Es el primer
  lab que **rompe hacia atrás**: los tests heredados ganaron autenticación, declarados en la
  derivación.
- **Lab 08** (`labs/lab-08-diplomacia-con-tesoreria/`): la resiliencia. Entra TESO (WireMock),
  el servicio externo que confirma pagos. El cliente ingenuo sin timeout cuelga la API entera
  cuando TESO se pone lento; se resuelve con timeout corto y dirigido, degradación elegante
  (503 rápido, el trámite intacto), el cliente migrado a `@HttpExchange`, y el endurecimiento
  (CORS nominal + cabeceras). La hora de M9 diferida del Lab 07 se cobra aquí.
- **Lab 09** (`labs/lab-09-caja-negra/`): la observabilidad. El sistema mudo (texto plano, sin
  correlación) donde buscar un folio en el log es imposible. Se resuelve con `traceId` por
  petición en el MDC, logging JSON estructurado, un aspecto de auditoría (AOP) que registra el
  dominio sin ensuciarlo (RUT enmascarado, respeta el límite del proxy), y carga de adjuntos con
  desconfianza (MIME real por magic bytes, anti path-traversal, descarga en streaming).
- **La caja de herramientas** de los scripts: `labs/lib/lib-comunes.sh`. La comparten los
  doce labs que vienen.
- **La memoria del proyecto**: por qué se decidió cada cosa está en `docs/decisiones.md`.
  Las especificaciones, en `docs/specs/`.
- **Un CI que muerde**: cada cambio comprueba que el temario cuadra, que los scripts son
  correctos y que la aplicación pasa sus 45 tests.

## 2 · Qué falta

Los doce laboratorios del curso, uno por sesión. Ninguno está escrito todavía:

| | | |
|---|---|---|
| Lab 10 · Latidos | Lab 11 · Amortiguadores | Lab 12 · Cápsula y egreso |

Faltan también las diapositivas y el material del instructor.

**Pendiente del PO:** correr las pruebas de aceptación acumuladas — Lab 00 (los tres comandos
de su README), y los Labs 01 a 09, cada uno con su Prueba del PO. Todas diferidas; los
laboratorios están verificados por el ejecutor, pero el PO aún no los ha corrido. La pila de
PRs (#6 a la del Lab 09) espera su palabra, empezando por el #6.

**Pendiente de infraestructura:** `main` no tiene protección en el servidor (GitHub no la
permite en repos privados del plan Free). El candado está especificado y congelado.

## 3 · Qué viene ahora

**SPEC-016: el Lab 10, «El reloj con problema de identidad».** El sistema ya sabe contar lo que
hizo. Y contando, Carolina notó que el cierre nocturno del viernes se ejecutó DOS veces: hay dos
servidores y los dos se creyeron el único. La próxima sesión: tareas programadas y cómo hacer que
una tarea que debe correr una vez, corra una sola vez en un mundo de muchas instancias.

## 4 · Si estás perdido

Tres comandos. Diez minutos. Vas a ver la DGT funcionando:

```bash
cd labs/lab-00-estacion-base

./bin/00-verificar.sh     # ¿tu máquina está lista?
./bin/start-lab.sh        # levanta la DGT
./bin/99-destruir.sh      # y déjalo todo como estaba
```

¿Quieres ver de qué trata el curso en un minuto? Ve el crimen del Lab 01:

```bash
cd labs/lab-01-del-otro-lado-del-boton/starter
git log --oneline -- src/main/resources/application.yml
git show dc70ed6 -- src/main/resources/application.yml     # la contraseña, en pantalla
```

Entre el segundo y el tercero, haz lo que el script te diga: pídele a la DGT que te hable
de Valentina Rojas. Cuando te responda, ya viste de qué trata el curso.

Si algo falla, `labs/lab-00-estacion-base/docs/troubleshooting.md` tiene una tabla con
números. Cita el número.
