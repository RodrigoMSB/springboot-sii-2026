# ¿En qué va el curso?

*Una página, sin jerga. Si llevas dos semanas sin mirar el repo, empieza aquí.*
*Última actualización: SPEC-007.*

---

## 1 · Qué existe hoy

- **El temario definitivo** (v3, julio 2026): `docs/temario/`. Son 36 horas, 12 sesiones
  de 3, 15 módulos. El `.md` manda; el `.docx` es lo que se le entrega al SII.
- **La aplicación del curso**: `dgt-tramites-api/`. Es el backend de la DGT — lo que hay
  detrás del botón. Arranca, se conecta a su base de datos y responde. Tiene siete reglas
  de arquitectura que la vigilan, y cada regla trae una prueba de que muerde.
- **El pre-vuelo del alumno**: `labs/lab-00-estacion-base/`. El chequeo que hace en su casa
  antes de la sesión 1.
- **El primer laboratorio de verdad**: `labs/lab-01-del-otro-lado-del-boton/`. La sesión 1
  completa: una contraseña de producción escondida en el historial de git, y qué se hace de
  verdad al respecto (se rota, no se borra).
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
| Lab 02 · El folio que se filtró | Lab 06 · Dos folios, un número | Lab 10 · Latidos |
| Lab 03 · Red de seguridad | Lab 07 · El portero | Lab 11 · Amortiguadores |
| Lab 04 · El árbol de trámites | Lab 08 · Diplomacia con Tesorería | Lab 12 · Cápsula y egreso |
| Lab 05 · Once segundos | Lab 09 · Caja negra | |

Faltan también las diapositivas y el material del instructor.

**Pendiente del PO:** correr la prueba de aceptación del Lab 00 (los tres comandos de su
README). Quedó diferida al cerrar la SPEC-006; el laboratorio está verificado por el
ejecutor, pero nadie más lo ha corrido.

**Pendiente de infraestructura:** `main` no tiene protección en el servidor (GitHub no la
permite en repos privados del plan Free). El candado está especificado y congelado.

## 3 · Qué viene ahora

**SPEC-008: el Lab 02, «El folio que se filtró».** Alguien devuelve la entidad en vez del
DTO, y en la respuesta viaja el puntaje de riesgo interno de un contribuyente.

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
