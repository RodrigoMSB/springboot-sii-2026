# Guía del instructor para la defensa · Lab 13

> Esta es la pieza que hace **replicable** el juicio humano. Si otro relator —Carlos, o quien venga—
> toma este documento y evalúa parecido a como evaluarías tú, el examen sirve. Si no, el eje
> Criterio es una opinión con nombre de rúbrica.
>
> Duración de la defensa: **8–10 minutos por alumno**. No más. Con estas preguntas alcanza.

---

## Las preguntas

Están ordenadas: las dos primeras casi siempre bastan para situar el nivel; las demás afinan.

1. **«Si solo pudieras escribir tres tests, ¿cuáles y por qué?»**
2. **«¿Qué NO probaste a propósito?»**
3. **«El brief no decía si paginar. ¿Qué decidiste, y qué te habría hecho decidir lo contrario?»**
4. **«Un colega llega mañana a mantener esto: ¿por dónde empieza?»**
5. **«¿Qué parte de tu entrega te da más miedo que se rompa en producción?»**

**No preguntes «¿cómo funciona `@Transactional`?».** Eso es memoria, y la memoria ya no es el
producto: la máquina la tiene mejor. Estas cinco preguntan por criterio, y el criterio no se puede
buscar.

**Cómo escuchar:** si la respuesta empieza con *«porque es la buena práctica»* o *«porque así se
hace»*, repregunta una vez: *«¿y qué pasaría si no lo hicieras?»*. La segunda respuesta es la que
vale — ahí se ve si hay modelo mental o repetición.

---

## Respuestas calibradas

Tres preguntas, cuatro niveles, un párrafo concreto cada uno. No son guiones a comparar palabra por
palabra: son **anclas** para que dos relatores pongan el mismo nivel al mismo alumno.

### Pregunta 1 · «Si solo pudieras escribir tres tests, ¿cuáles y por qué?»

**Insuficiente** — *«Los tres que escribí»*, o una lista sin criterio: *«uno del controlador, uno del
servicio y uno del repositorio»*. Enumera capas, no riesgos. Si se le pregunta por qué esos, responde
con la estructura del código en vez de con lo que puede fallar.

**Suficiente** — *«El camino feliz, que sin eso no hay nada; el 404 del RUT que no existe, porque lo
decidí yo y quiero que quede fijo; y el del rol, porque el brief lo pedía.»* Distingue lo obligatorio
de lo decidido. Todavía razona test por test, no sobre el conjunto.

**Competente** — *«El total del período, porque es el número que lee Carolina y un `JOIN` mal agrupado
lo infla sin que nada falle. El 403 al contribuyente, porque el consolidado agrega datos y agregarlos
sube el valor de la fuga. Y el 404, porque devolver una lista vacía para un RUT inexistente es una
mentira cortés.»* Elige por **riesgo y por costo del fallo**, no por cobertura. Nombra el fallo
silencioso.

**Destacado** — Lo anterior, y además: *«Los tres cubren lo que falla en silencio. El camino feliz no
está entre ellos porque si eso se rompe me entero en el primer clic; lo que no se nota es un total
inflado o un permiso de más. Y no probé el formato del JSON a propósito: atar el contrato campo por
campo le ata las manos al que venga a refactorizar.»* Razona sobre **qué fallos son visibles y cuáles
no**, y justifica lo que deja fuera.

### Pregunta 2 · «¿Qué NO probaste a propósito?»

**Insuficiente** — *«Nada, probé todo»*, o silencio. No distingue entre no haber probado y haber
decidido no probar. A veces enumera lo que le faltó tiempo de hacer, presentándolo como decisión.

**Suficiente** — *«No probé el arranque de la aplicación ni la configuración, porque eso lo cubre
Spring.»* Identifica una zona y da una razón, aunque sea genérica.

**Competente** — *«No probé los nombres exactos de los campos del JSON. Si mañana renombramos uno, eso
es una decisión, no un accidente, y un test ahí solo haría ruido. Tampoco volví a probar la
seguridad de los endpoints antiguos: ya tienen sus pruebas desde el Lab 07 y duplicarlas es tener dos
sitios que actualizar.»* Distingue **contrato de implementación** y evita duplicar cobertura.

**Destacado** — Lo anterior, y nombra un riesgo asumido: *«Tampoco probé el consolidado con un
contribuyente de miles de trámites. Sé que es el escenario que va a doler y lo dejé fuera por alcance:
si tuviera media hora más, escribiría el test con el contador de consultas del Lab 05 antes que
cualquier otro.»* Sabe **qué haría a continuación y por qué eso primero**.

### Pregunta 3 · «¿Qué te habría hecho decidir lo contrario?»

*(sobre paginar, sobre el 404, o sobre cualquier borde que haya resuelto)*

**Insuficiente** — *«Nada, es lo correcto.»* Trata su decisión como una verdad, no como una elección
con contexto. No hay escenario alternativo en su cabeza.

**Suficiente** — *«Si fueran muchos trámites, habría paginado.»* Reconoce que la decisión depende del
contexto, pero en abstracto: «muchos» sin número.

**Competente** — *«Si el batch tuviera que recorrer todos los contribuyentes en una ventana corta,
paginar dejaría de ser opcional: sin cursor tendría que traerlo todo en memoria. Con decenas de
trámites por contribuyente, paginar hoy es complejidad sin problema que resolver.»* Nombra el
**umbral** y quién lo cruzaría.

**Destacado** — Lo anterior, y además cómo se enteraría: *«Y no lo decidiría de nuevo por intuición:
el Lab 10 dejó las métricas puestas, así que pondría un timer en el endpoint y lo revisaría cuando el
percentil 95 pasara de medio segundo. Prefiero que la decisión la dispare un número y no una
sensación.»* Cierra el círculo entre **decisión, medición y revisión**.

---

## La gramática del feedback

El orden importa, y no es cortesía: un alumno que se siente juzgado deja de escuchar en la primera
frase, y entonces el feedback no llega aunque sea correcto.

**1 · Una fortaleza real, primero.** Real: nombrada con concreción, no *«en general bien»*. Si no
encuentras ninguna, mira otra vez — siempre hay algo, y si de verdad no lo hay, ese es el titular.

> *«Tu decisión del 404 estaba bien pensada y bien defendida: distinguiste "no existe" de "no tiene
> nada", que es exactamente la distinción que evita un incidente.»*

**2 · Cada crítica, convertida en acción.** No un adjetivo sobre la entrega: un verbo sobre lo que
hay que hacer.

| ❌ No digas | ✅ Di |
|---|---|
| «Tu arquitectura es confusa» | «Mueve el mapeo fuera del controlador y el patrón queda claro» |
| «Faltan tests» | «Te falta el del total por período: es el número que lee Carolina» |
| «El código está sucio» | «Estos tres nombres dicen *qué* hacen; que digan *para qué*» |
| «No entendiste el brief» | «Los bordes están ahí para decidirse: elige uno y escribe el porqué» |

**3 · El cierre, siempre el mismo.** El criterio **se entrena**. Un Suficiente hoy es un Competente en
el próximo proyecto, y decirlo no es consuelo: es información verdadera sobre cómo funciona esto.

> *«El criterio no se tiene: se construye decidiendo y viendo las consecuencias. Hoy decidiste tres
> cosas y defendiste dos. En seis meses vas a defender las tres.»*

---

## Qué hacer con el que no alcanza

**Si el núcleo está en rojo** (Correctitud u Oficio Insuficiente):

- Se le entrega la salida del boletín, que ya dice exactamente qué falló.
- **Plazo: una semana.** Corrige y vuelve a entregar. Solo se re-evalúa lo que estaba en rojo; lo
  aprobado no se re-examina.
- No hay defensa nueva si el Criterio ya estaba ≥ Suficiente: ese eje queda ganado.

**Si el Criterio está Insuficiente** (con el núcleo verde) — el caso delicado:

- Es el que más duele, porque la entrega «funciona». Hay que decirlo sin rodeos y sin humillar:
  *«tu código hace lo que pide el brief. Lo que hoy no pudiste hacer es explicarme por qué lo hiciste
  así, y eso es lo que este curso certifica.»*
- **Se repite solo la defensa**, en un plazo de una semana, sobre la misma entrega. No hay que
  escribir código nuevo: hay que volver con las decisiones pensadas.
- Se le entrega la lista de bordes del brief y se le pide que escriba media página por cada uno. La
  mayoría descubre que **sí** había decidido, y que lo que le faltó fue el lenguaje para contarlo.

**Lo que se le reconoce igual, en los dos casos.** El alumno terminó trece sesiones y entregó una
aplicación que compila, pasa una suite, se empaqueta y arranca. Eso es más de lo que llega a hacer
mucha gente con años de oficio, y se le dice con esas palabras. **No aprobar un examen no es no haber
aprendido nada**, y confundir las dos cosas es la forma más rápida de que alguien no vuelva a
intentarlo.
