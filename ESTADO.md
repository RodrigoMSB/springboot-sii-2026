# ¿En qué va el curso?

*Una página, sin jerga. Si llevas dos semanas sin mirar el repo, empieza aquí.*
*Última actualización: SPEC-019.*

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
- **Lab 10** (`labs/lab-10-observabilidad/`): el tablero que mentía. `/actuator/health` responde
  `UP` con la base muerta y la API devolviendo 500. Se resuelve con un health check propio que
  consulta la base y **nombra** lo que se cayó, liveness y readiness separados (reiniciar y sacar
  de rotación son acciones opuestas), métricas de negocio con Micrometer publicadas en formato
  Prometheus, lista blanca nominal de endpoints (`/env` y `/heapdump` dejan de existir) y un caché
  Caffeine con TTL, hit-rate medido e invalidación explícita al escribir.
- **Lab 11** (`labs/lab-11-latidos/`): el reloj con problema de identidad. El cierre nocturno se
  ejecuta una vez por instancia: con dos servidores, dos cierres, totales duplicados y el mismo
  aviso dos veces al contribuyente. Se resuelve con un candado distribuido en la base (atómico, con
  expiración y con el reloj del motor, no el de cada máquina), `fixedDelay` en vez de `fixedRate`,
  el cron con zona `America/Santiago` explícita, notificaciones asíncronas sobre hilos virtuales de
  Java 25, y eventos `AFTER_COMMIT` — si la transacción revierte, el aviso no sale.
- **Lab 12** (`labs/lab-12-amortiguadores/`): mensajería y resiliencia. El aviso se manda al aire:
  con el servicio de avisos caído se evapora, la API responde 201 y nadie puede decir cuáles se
  perdieron. Se resuelve entregándolo a una cola durable (RabbitMQ), con un consumidor idempotente
  —«exactly once» no existe—, una DLQ que aparta al mensaje envenenado con su causa sin atascar a
  los buenos, y un circuit breaker que deja de golpear a Tesorería cuando está caída.
- **Lab 13** (`labs/lab-13-capsula-y-egreso/`): el **examen de egreso**, y el único lab sin crimen.
  El alumno recibe un *brief* de negocio deliberadamente incompleto en los bordes, y tres horas para
  entregar un consolidado correcto, seguro, probado por él y empaquetado como imagen OCI. Su
  validador no cuenta huecos: emite un **boletín de tres ejes** (Correctitud · Oficio · Criterio)
  que **declara quién mide cada uno** y **no puede aprobar a nadie** — el eje Criterio es humano y
  el umbral es núcleo verde *y* criterio ≥ Suficiente. Trae rúbrica, guía de defensa con respuestas
  calibradas por nivel, y una `solucion-referencia/` que dice de entrada que es UNA solución.
- **El toolchain, fijado**: `.sdkmanrc` en la raíz (`java=25-tem`). Quien clone el repo con SDKMAN
  cae solo en la versión correcta; sin él, el pom compila contra Java 25 y nada lo declaraba.
- **La caja de herramientas** de los scripts: `labs/lib/lib-comunes.sh`. La comparten los
  doce labs que vienen.
- **La memoria del proyecto**: por qué se decidió cada cosa está en `docs/decisiones.md`.
  Las especificaciones, en `docs/specs/`.
- **Un CI que muerde**: cada cambio comprueba que el temario cuadra, que los scripts son
  correctos y que la aplicación pasa su suite completa (106 tests en el Lab 13).

## 2 · Qué falta

**Ninguno de los trece.** El curso está construido: los 35 temas oficiales, cubiertos.

Queda pendiente un **lab adicional de microservicios**, cuyo código aportará el PO, que cerrará el
alcance del título oficial («Desarrollo de Microservicios en Java»). Su SPEC se emitirá por separado.

Faltan también las diapositivas y el material del instructor para sala.

La renumeración del temario se cuadra en la actualización contractual pendiente; en el repo ya
está aplicada. Faltan también las diapositivas y el material del instructor.

**Pendiente del PO:** correr las pruebas de aceptación acumuladas — Lab 00 (los tres comandos
de su README), y los Labs 01 a 13, cada uno con su Prueba del PO. Todas diferidas; los
laboratorios están verificados por el ejecutor, pero el PO aún no los ha corrido. La pila de PRs
de los labs 01 a 09 **ya está mergeada a `main`** (PRs #6 a #15), así que la fila acumulada puede
correrse desde `main` limpio, con Java 25 activo (`sdk env` en la raíz).

**Pendiente de infraestructura:** `main` no tiene protección en el servidor (GitHub no la
permite en repos privados del plan Free). El candado está especificado y congelado.

## 3 · Qué viene ahora

**El lab de microservicios.** Con los trece laboratorios construidos y los 35 temas oficiales
cubiertos, lo que queda es el lab adicional que cierra el alcance del título del curso. El PO aporta
el código; la SPEC se emite por separado.

Mientras tanto, lo que de verdad falta no es material: es que el PO corra la fila de **pruebas de
aceptación acumuladas** (Labs 00 a 13) y cierre los PRs abiertos. Los laboratorios están verificados
por el ejecutor; ninguno lo ha corrido su dueño.

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
