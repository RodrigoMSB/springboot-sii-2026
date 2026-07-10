# Lab 00 · Estación Base

> *«El año pasado te enseñé a probar el portal. Hoy te enseño lo que hay detrás del
> botón. Y te advierto una cosa: un folio emitido dos veces no se borra. Se explica.
> Ante un fiscalizador.»*
>
> — **Carolina Espinoza**, Jefa de la Plataforma de Trámites, DGT

Bienvenido. Antes de escribir una línea de código, tu máquina tiene que estar lista.
Este laboratorio no evalúa nada y no computa horas: es el chequeo previo al despegue.
**Si lo haces hoy, la sesión 1 empieza a la hora.** Si lo dejas para el lunes, empieza
media hora tarde y tus 17 compañeros te van a mirar.

**Tiempo estimado: 40 minutos** (30 de ellos son descargas: pon un café).

---

## El pre-vuelo, en cuatro pasos

| # | Qué haces | Con qué |
|---|---|---|
| 1 | **Instalas** lo que falta | [`guia/01-instala-tu-estacion.md`](guia/01-instala-tu-estacion.md) |
| 2 | **Verificas** que la máquina está lista | `./bin/00-verificar.sh` |
| 3 | **Levantas** la DGT y la ves viva | `./bin/start-lab.sh` |
| 4 | **Destruyes** todo, sin dejar rastro | `./bin/99-destruir.sh` |

Después del paso 3, si escribes esto:

```bash
curl http://localhost:8080/api/contribuyentes/11111111-1
```

Valentina Rojas te va a responder. Ese es el momento en que la DGT deja de ser una
diapositiva.

---

## Reglas de esta carpeta

- **Si un script falla, no es culpa tuya: es información.** Cada `[ERROR]` trae una flecha
  `->` con qué hacer al respecto. Nunca verás un stacktrace en la cara.
- **`99-destruir.sh` solo toca lo que este curso levantó.** Si tienes otros contenedores
  corriendo —el proyecto del trabajo, otra base de datos—, ni los mira.
- **¿Algo no cuadra?** [`docs/troubleshooting.md`](docs/troubleshooting.md) tiene una tabla
  numerada. Cita el número (`T-03`) cuando pidas ayuda: nos ahorramos veinte mensajes.
- **¿Tu institución no autoriza Docker?** No es un problema, es un escenario previsto:
  `./bin/00-verificar.sh --sin-docker` y habla con el instructor.

---

## Para el Instructor

**La semana previa a la sesión 1:**

1. Pide a los 18 alumnos que corran `./bin/00-verificar.sh` y te manden la salida
   **completa**, pegada tal cual. No un "me funciona": la salida.
2. Cuenta los `ESTACIÓN LISTA`. Cada `ESTACIÓN INCOMPLETA` es una llamada de 10 minutos
   que ahorras el lunes.
3. Los fallos se agrupan en tres familias, casi siempre:
   - **Java equivocado** (suelen tener un 17 o un 21 de otro curso) → `T-02`.
   - **Docker Desktop cerrado** → `T-03`. Es el más común y el más rápido de arreglar.
   - **Proxy corporativo** → `T-05`. Es el más lento: empiézalo el martes, no el viernes.
4. Los alumnos que reporten `--sin-docker` van a una lista aparte. Con ellos, los módulos
   de Testcontainers, imagen OCI y doble instancia se ven como **demo tuya**, no como
   laboratorio. Avísales antes, no el día.

**El día de la sesión 1**, ten esto corriendo en tu máquina antes de que entre nadie:
`./bin/start-lab.sh`. Si tu propia estación falla delante de 18 personas, ninguna guía
te salva.
