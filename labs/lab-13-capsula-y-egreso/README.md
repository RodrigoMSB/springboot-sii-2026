# Lab 13 · Cápsula y egreso

> *«Necesito que los fiscalizadores puedan pedir un consolidado de un contribuyente: sus trámites, el
> estado de cada uno y el total declarado del período. Lo van a usar desde el portal y desde un
> proceso batch nocturno. Tiene que estar en producción el lunes. No te voy a decir cómo hacerlo —
> para eso te contraté. Y cuando lo entregues, quiero que me expliques **por qué** lo hiciste así.»*
>
> — **Carolina Espinoza**, y se va

**Hoy no hay crimen.**

Los doce labs anteriores abrieron con un incendio porque necesitabas que te mostraran el problema.
Hoy el que tiene que verlo eres tú.

**Sesión 13** · Módulo 14 (empaquetado, despliegue y proyecto final) · **3 horas** · **Es el examen**

---

## Cómo es esta sesión

| | Actividad | Tiempo |
|---|---|---|
| 📚 | Teoría + demo: empaquetado, imagen OCI, secretos, apagado elegante | ~60 min |
| 🔧 | **El examen**: lees el brief y entregas | ~110 min |
| ✅ | Cierre y defensa | ~10 min |

## Lo que entregas

1. **El endpoint** del consolidado: correcto, seguro y documentado.
2. **Tus pruebas** — tú decides cuáles y cuántas. Eso es materia de evaluación.
3. La aplicación **empaquetada como imagen OCI** y arrancando con perfil productivo.
4. Tu **[reporte de egreso](plantillas/reporte-egreso.md)**.

El requerimiento está en **[`brief/requerimientos-dgt.md`](brief/requerimientos-dgt.md)**. Está
escrito en lenguaje de negocio y **le faltan cosas a propósito**: encontrar los huecos y resolverlos
con criterio declarado es parte del examen, no un defecto del enunciado.

## Cómo se te evalúa

Tres ejes, y cada uno declara quién lo mide:

| Eje | Lo mide | Qué mira |
|---|---|---|
| **Correctitud** | automático | ¿Funciona, y funciona de verdad? |
| **Oficio** | semi-automático | ¿Está bien hecho por dentro? |
| **Criterio** | **humano** | ¿Sabes por qué lo hiciste así? |

```bash
./bin/90-validar.sh                # tu entrega
./bin/90-validar.sh --sin-imagen   # más rápido, sin el empaquetado OCI
```

Emite un **boletín**, no una nota. Y **no puede aprobarte**: lo más que puede decir es «el núcleo está
verde». El veredicto necesita la defensa oral.

> **El umbral es núcleo verde Y criterio ≥ Suficiente.** Todo verde y sin criterio **no aprueba**.
> No es severidad: es la tesis del curso — la máquina escribe la sintaxis mejor que tú, y lo que se
> certifica aquí es lo otro.

**Lee la [rúbrica](rubrica/rubrica-evaluacion.md) antes de empezar.** No es un examen sorpresa: es un
examen difícil, y saber cómo se mide es parte de poder hacerlo bien.

## Antes de empezar

Este lab **necesita Docker** y levanta PostgreSQL, RabbitMQ y TESO. El empaquetado OCI descarga
buildpacks la primera vez: hazlo una vez antes de la sesión.

```bash
cd ..
./bin/start-lab.sh          # (el start-lab del Lab 12 sirve: es la misma app)
```

## Para el Instructor

`./bin/91-e2e.sh` antes de la sesión — comprueba que el examen pide algo, que la referencia lo
entrega, y que la suite **no es flaky** (tres corridas).

El guion está en [`INSTRUCTOR.md`](INSTRUCTOR.md) y la defensa en
[`rubrica/guia-instructor.md`](rubrica/guia-instructor.md).
