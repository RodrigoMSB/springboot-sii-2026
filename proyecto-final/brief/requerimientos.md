# Requerimiento · Consolidado del contribuyente

**De:** Carolina Espinoza — Jefatura de Fiscalización, DGT
**Para:** el equipo de desarrollo
**Fecha:** lunes

---

> **Antes de empezar, lee esto.**
>
> Este documento es un requerimiento de negocio, no una especificación técnica. Está escrito como lo
> escribiría quien lo pide, y **eso incluye sus huecos**.
>
> **Si algo no está especificado, decídelo tú y déjalo escrito en tu reporte.** No preguntes cuál es
> la respuesta correcta: no hay una. Hay decisiones defendibles y decisiones que no lo son, y la
> diferencia entre ambas es lo que se evalúa hoy.

---

## Lo que necesito

Necesito que los fiscalizadores puedan pedir un **consolidado de un contribuyente**: sus trámites, el
estado de cada uno, y el total declarado del período.

Lo van a usar desde dos sitios:

- **El portal**, donde un fiscalizador escribe un RUT y mira el resultado en pantalla.
- **Un proceso batch nocturno**, que recorre una lista de contribuyentes y guarda los consolidados
  para el informe de la mañana.

Tiene que estar en producción **el lunes**.

No te voy a decir cómo hacerlo — para eso te contraté. Y cuando lo entregues, quiero que me expliques
**por qué** lo hiciste así.

## Contexto que te sirve

- Un fiscalizador **no es** un contribuyente. Lo que ve un fiscalizador no lo puede ver cualquiera:
  un consolidado reúne en una sola pantalla lo que hasta ahora estaba disperso.
- El informe de la mañana lo leo yo. Si un número está mal, lo voy a notar el mismo día.
- El año pasado tuvimos un incidente con un **puntaje interno** que salió por una API. No quiero
  saber nada de eso otra vez.

## Lo que voy a mirar cuando me lo entregues

1. Que **funcione**, y que funcione con los datos de verdad.
2. Que esté **desplegable**: quiero poder levantarlo sin que tú estés delante.
3. Que puedas **explicarme tus decisiones**, incluidas las que no te pedí.

## Lo que NO voy a mirar

- Cuántas líneas escribiste.
- Cuántos tests escribiste. Me interesa **cuáles**, y por qué esos.
- Si usaste la misma solución que tu compañero.

---

*— Carolina*

---

<br>

## Nota del relator (esto no lo escribió Carolina)

El requerimiento de arriba es todo lo que tienes. Está **deliberadamente incompleto en los bordes**, y
esos bordes son parte del examen.

Algunos que vas a encontrar —hay más—:

- ¿Qué pasa si el RUT no existe? ¿Es lo mismo que un contribuyente **sin trámites en el período**?
- ¿El período es obligatorio? ¿Y si no lo mandan?
- ¿Qué campos salen y cuáles no? (relee el tercer punto del contexto de Carolina)
- ¿El total se calcula sobre todos los trámites o sólo sobre algunos estados?
- El batch nocturno, ¿se autentica igual que una persona? ¿Necesita otro camino?
- ¿Se pagina el listado? ¿A partir de cuántos trámites?

**No hay una respuesta correcta a ninguna.** Hay respuestas que puedes defender y respuestas que no.
Decide, impleméntalo, y **escribe el porqué** en `plantillas/reporte.md`.

Un borde que resolviste sin darte cuenta y no puedes explicar cuenta menos que un borde que dejaste
fuera a conciencia y sabes decir por qué.

### De dónde partes

`base/` es un proyecto que **compila y arranca**. Trae ya resuelto lo que no se evalúa hoy:

- El esquema y los datos sembrados (`contribuyente`, `tramite`, `usuario`).
- Las entidades y sus repositorios básicos.
- **La autenticación completa**: `POST /auth/login` con dos usuarios,
  `ana` / `secreta` (**FISCALIZADOR**) y `luis` / `secreta` (**CONTRIBUYENTE**), que devuelve un JWT
  válido. La cadena de filtros exige token en todo salvo el login y `/actuator/health`.
- Actuator, y Jib configurado para construir la imagen.

Lo que **no** trae es el encargo. Ni el endpoint, ni el DTO, ni la consulta, ni la regla de quién
puede verlo, ni una sola prueba.

### El alcance de tu entrega

Tienes **tres horas**. Se te pide:

1. El endpoint del consolidado: correcto, seguro y con los errores con forma.
2. **Tus** pruebas — tú decides cuáles y cuántas.
3. La aplicación **empaquetada como imagen OCI** (`./mvnw package jib:buildTar`) y capaz de arrancar
   con el perfil productivo.
4. Tu **reporte**, en `plantillas/reporte.md`.

### Cómo se te evalúa

Tres ejes: **Correctitud**, **Oficio** y **Criterio**.

**El umbral es núcleo verde Y criterio ≥ Suficiente.** Todo verde y sin criterio no aprueba — y eso
no es una amenaza, es la tesis del curso: la máquina escribe la sintaxis mejor que tú, y lo que se te
está evaluando es lo otro.

La rúbrica completa está en `rubrica/rubrica.md`. **Léela antes de empezar.** No es un examen
sorpresa: es un examen difícil.
