# Lab 01 · Del otro lado del botón

> *«El año pasado te enseñé a probar el portal. Hoy te enseño lo que hay detrás del
> botón. Y te advierto una cosa: un folio emitido dos veces no se borra. Se explica.
> Ante un fiscalizador.»*
>
> — **Carolina Espinoza**, Jefa de la Plataforma de Trámites

Hoy recibes el código que dejó el practicante. Compila. Los tests pasan. La aplicación
arranca. Y aun así, Carolina va a proyectar una pantalla en los primeros diez minutos y
la sala se va a quedar en silencio.

**Sesión 1** · Módulo 1 completo (2,0 h) + primera hora del Módulo 2 · **3 horas**

---

## Lo que vas a hacer

| | Actividad | Tiempo |
|---|---|---|
| 🔪 | **La escena del crimen** — la vives, no te la cuentan | 10 min |
| 📚 | Teoría: el contenedor, la autoconfiguración, la configuración externalizada | ~40 min |
| ☕ | Descanso | 10 min |
| 🔧 | **Laboratorio: 4 TODOs** | ~110 min |
| ✅ | Validar, reporte, y la siembra del Lab 02 | 10 min |

## Los cuatro TODOs

| | Qué | Dónde | ≈ |
|---|---|---|---|
| **TODO_1** | Externalizar la conexión: el perfil `prod` pide sus secretos al entorno | `application-prod.yml` | 15 min |
| **TODO_2** | Tres perfiles, y que `prod` **falle rápido y claro** sin sus variables | `application-*.yml` + `config/` | 15 min |
| **TODO_3** | `DgtProperties`: configuración tipada, inmutable y **validada** | `config/DgtProperties.java` | 15 min |
| **TODO_4** | Tu primer endpoint: `GET /api/tramites/{id}` + 404 con `ProblemDetail` | `web/controller/` | 15 min |

Todo lo demás viene escrito, funcionando, y con Javadoc que explica **por qué**. Léelo:
este código lo escribieron para que tú lo leas.

---

## El pre-vuelo

```bash
cd starter
./mvnw test          # rojo. Está bien. Los tests son tu enunciado.
```

Y cuando creas que terminaste:

```bash
cd ..
./bin/90-validar.sh              # valida tu starter/
./bin/90-validar.sh --dir solucion   # el MISMO validador juzga la solución
```

No hay dos verdades: si la solución no pasara este validador, el validador estaría roto.

## Cuando te atasques

- `./bin/95-recuperar.sh --solo-enunciado` — restaura los tests si los rompiste sin querer.
- `./bin/95-recuperar.sh --todo` — copia la solución encima. **Respalda tu trabajo antes.**
  No es hacer trampa: es comparar. Pero anótalo en tu reporte. La casilla existe, y
  responderla con honestidad vale más que un lab perfecto.
- `docs/troubleshooting.md` — tabla numerada. Cita el número cuando pidas ayuda.

## Una aclaración necesaria

La contraseña que vas a ver en el historial de `starter/` (`Dgt2026Pr0d!`, hacia un host
`prod-db.dgt.gob.cl`) es **de utilería**. El dominio no existe, la clave no abre nada, y
está ahí a propósito para que aprendas a reconocer el problema. No es una filtración real
de nadie.

Y la contraseña de `compose.yaml` (`dgt-dev`) también está versionada a propósito, y
también está bien: abre una base desechable que vive en tu portátil y muere con
`docker compose down -v`. **La diferencia no es el archivo. Es qué protege el secreto y
qué pasa si se filtra.** Aprender a distinguirlas es medio Lab 01.

---

## Para el Instructor

**Antes de la sesión:** corre `./bin/91-e2e.sh`. Prueba dos cosas de una pasada: que el
`starter/` está genuinamente incompleto (el validador sale 1) y que tiene solución (sale
0). Si eso falla, no des la clase.

**El crimen** está guionizado, minuto a minuto, en [`INSTRUCTOR.md`](INSTRUCTOR.md).
Ensáyalo. El silencio de la sala cuando aparece la contraseña en pantalla es el momento
pedagógico de la sesión, y se pierde si buscas el comando en vivo.

**El error que cometerá la sala:** al llegar al TODO_2, alguien exportará la variable en
una terminal y correrá Maven en otra. Culpará a Spring. Ten preparado `echo $DGT_DB_URL`.

**Los reportes** vienen con dos preguntas que importan más que el código: la transcripción
literal del error de arranque en `prod`, y la casilla de honestidad sobre `solucion/`.
Léelas antes que el diff.
