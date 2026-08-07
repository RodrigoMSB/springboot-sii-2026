# Guía del instructor · Lab 13 — el examen de egreso

## Antes de la sesión

1. `./bin/91-e2e.sh` — el starter no aprueba, la referencia sí, y la suite no es flaky (**tres
   corridas**: tarda). Si falla, no examines a nadie.
2. **Precalienta los buildpacks.** `cd solucion-referencia && ./mvnw spring-boot:build-image` descarga
   varios cientos de megas la primera vez. Hazlo el día antes. Si veinte alumnos lo descubren a la
   vez en la sala, pierdes media sesión.
3. Ten leída `rubrica/guia-instructor.md`. Las defensas son 8–10 minutos por alumno y no hay tiempo
   para improvisar preguntas.

## ORDEN PARA CLASE (180 min)

### 📚 00:00 – 01:00 · Teoría y demo del empaquetado

`TEORIA.md` §§1–8. Es la única hora de contenido nuevo del día, y el alumno lo va a aplicar en la
siguiente: dilo así.

Imprescindibles:

- **§2, el jar por capas.** Con números: sesenta megas contra unos cientos de kilobytes por
  despliegue.
- **§3, Buildpacks vs Dockerfile.** La demo en vivo (`./mvnw spring-boot:build-image`) y la tabla de
  criterio. Insiste: «prefiero controlarlo todo» no es una razón concreta.
- **§5, Leyden y GraalVM.** No los instales. Lo que se evalúa es el criterio de esa tabla.
- **§6, secretos.** Es el crimen del Lab 01 en su versión final: en el historial de capas de una
  imagen publicada.
- **§7, apagado elegante.** «Cada despliegue tira peticiones a la basura y nadie lo registra como
  incidente.»

**Cierra la hora entregando el brief**, y sal del rol de profesor:

> *«De aquí en adelante no soy quien te enseña: soy quien recibe tu entrega. Ahí tienen el
> requerimiento.»*

### 🔧 01:00 – 02:50 · El examen

**Cómo entregar el brief sin dar pistas.** Repártelo, di una sola frase —*«está incompleto a
propósito; lo que falte, decídelo tú y escribe por qué»*— y **cállate**. La primera media hora va a
ser incómoda: gente releyendo, gente preguntando cosas que el brief no responde. Aguántalo. Esa
incomodidad **es** el examen.

**Cuánto ayudar — la regla, y es estricta:**

> **Se responde sobre HERRAMIENTAS. No se responde sobre DISEÑO.**

| ✅ Responde | ❌ No respondas |
|---|---|
| «¿Cómo se llama el goal de la imagen?» | «¿Debería paginar?» |
| «Se me cayó el contenedor, ¿cómo veo el log?» | «¿Está bien que devuelva 404?» |
| «¿Dónde estaba el `@PreAuthorize`?» | «¿Qué tests debería escribir?» |
| «Testcontainers no arranca» | «¿Esto está bien hecho?» |

Cuando te pregunten diseño, la respuesta es **siempre** la misma, y sin ironía:

> *«Decídelo tú y escribe por qué. Eso es lo que estoy evaluando.»*

Lo van a intentar tres o cuatro veces. Repite la frase las tres o cuatro veces.

**Los últimos 20 minutos:** recuérdales el reporte de egreso. Mucha gente lo deja para el final y
entrega media página escrita con prisa — y es el eje que decide si aprueban.

### ✅ 02:50 – 03:00 · Cierre

Corre el boletín con alguien voluntario en pantalla, para que vean que **no da nota** y que dice
explícitamente «CRITERIO · NO MEDIDO AQUÍ». Es la última lección del curso y conviene que la vean
funcionando, no que se la cuenten.

Lee la despedida de Carolina (`TEORIA.md §10`). Despacio. Es el cierre de trece semanas y merece
treinta segundos de silencio después.

**Las defensas** se agendan aparte, 8–10 minutos por alumno, con `rubrica/guia-instructor.md`
delante. No las improvises al final de la sesión: la gente está cansada y tú también, y el eje
Criterio se merece atención.

## Errores que vas a ver

1. **Empezar a teclear sin leer el brief entero.** Se nota a los diez minutos: implementan el camino
   feliz y descubren el rol a la hora y media.
2. **Preguntar por los bordes en vez de decidirlos.** La regla de arriba.
3. **Escribir veinte tests triviales.** Confunden cantidad con cobertura. No lo corrijas durante el
   examen: es exactamente lo que la pregunta 1 de la defensa destapa.
4. **Dejar el reporte para el final.** Avísalo dos veces: al empezar y a los 20 minutos del cierre.
5. **Empaquetar a última hora** y descubrir que los buildpacks descargan. De ahí el punto 2 del
   pre-vuelo.

## Si alguien no alcanza

`rubrica/guia-instructor.md`, sección final. Resumen: el núcleo se corrige en una semana; la defensa
se repite sobre la misma entrega; y en los dos casos se le reconoce en voz alta lo que sí hizo.

**No aprobar un examen no es no haber aprendido nada**, y confundir las dos cosas es la forma más
rápida de que alguien no vuelva a intentarlo.
