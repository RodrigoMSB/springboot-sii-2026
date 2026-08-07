# SPEC-019 · Lab 13 «Cápsula y egreso»

| Campo | Valor |
|---|---|
| ID | SPEC-019 |
| Título | Decimotercer laboratorio: el examen de egreso — brief, empaquetado y defensa (M14) |
| Autor | Arquitecto |
| Aprueba | PO (Rodrigo) — **APROBADA** |
| Depende de | SPEC-018 (Lab 12) |
| Estado | LISTA PARA EJECUCIÓN |

> **Instrucción de ejecución (mocito):** guardar este archivo íntegro en
> `docs/specs/SPEC-019-lab13-capsula-y-egreso.md` y commitearlo en rama antes de
> ejecutar. **Base:** apila sobre `spec/018`. Protocolo de dos etapas vigente.
>
> **Este lab es distinto a los doce anteriores. Léelo entero antes de construir nada:
> no tiene crimen, no tiene TODOs numerados, y su validador no es un checklist de
> huecos sino un boletín de tres ejes.** Si algo de tu automatismo de los labs previos
> choca con esto, manda esta SPEC.

---

## §1 · Objetivo

Que exista `labs/lab-13-capsula-y-egreso/`: la sesión final. Cubre el **Módulo 14
oficial** (Empaquetado, Despliegue y Proyecto Final Integrador) y es, además, **el
instrumento de evaluación del curso**. El alumno recibe un **brief de negocio** —no
instrucciones— y tres horas para demostrar criterio; el relator recibe una rúbrica que
hace su juicio replicable.

## §2 · Por qué no hay crimen

Los doce labs anteriores abrieron con un incendio porque el alumno necesitaba que le
mostraran el problema. **Hoy el alumno es quien tiene que verlo.** La sesión abre con
Carolina entregando un requerimiento y yéndose:

> *"Necesito que los fiscalizadores puedan pedir un consolidado de un contribuyente:
> sus trámites, el estado de cada uno y el total declarado del período. Lo van a usar
> desde el portal y desde un proceso batch nocturno. Tiene que estar en producción el
> lunes. No te voy a decir cómo hacerlo — para eso te contraté. Y cuando lo entregues,
> quiero que me expliques **por qué** lo hiciste así."*

El brief está deliberadamente incompleto en los bordes (¿pagina o no? ¿qué pasa si el
contribuyente no existe? ¿el batch necesita autenticación distinta?). **Detectar los
huecos y resolverlos con criterio declarado es parte de lo evaluado**, no un defecto del
enunciado. El `brief/requerimientos-dgt.md` lo dice en su encabezado: *"si algo no está
especificado, decídelo tú y déjalo escrito en tu reporte."*

## §3 · Alcance del entregable del alumno

Sobre la app que viene construyendo (`starter/` = `solucion/` del Lab 12, íntegra), el
alumno debe entregar, en 3 horas:

1. El endpoint del consolidado, correcto, seguro (rol FISCALIZADOR) y documentado.
2. Sus pruebas — **él decide cuáles y cuántas**: eso es materia de evaluación.
3. La aplicación **empaquetada como imagen OCI** (`spring-boot:build-image`, Buildpacks)
   y arrancando con perfil productivo, sondas activas y apagado elegante.
4. Su **reporte de egreso** (`plantillas/reporte-egreso.md`): decisiones tomadas, huecos
   del brief y cómo los resolvió, qué **no** hizo y por qué, y la trampa registrada
   (*"¿consultaste `solucion-referencia/`? ¿en qué momento y por qué?"*).

El M14 (empaquetado y despliegue) **se dicta como teoría + demo en la primera hora** —
Buildpacks vs Dockerfile, jar por capas, arranque acelerado (caché AOT de Leyden y
GraalVM nativo, comparados sin instalar), secretos por variables de entorno, graceful
shutdown y sondas. El alumno lo aplica al empaquetar su entrega.

## §4 · El boletín de tres ejes (el validador de este lab)

`bin/90-validar.sh` de este lab **no cuenta TODOs**: emite un boletín. Tres ejes,
distinta naturaleza cada uno, y el boletín **declara quién mide qué** (nada de criterios
que ningún mecanismo verifica — anti-herencia A-02):

| Eje | Cómo se mide | Qué mira |
|---|---|---|
| **Correctitud** | automático | El endpoint responde lo pedido; la suite pasa; la imagen OCI se construye y la app arranca con `/actuator/health` en UP. **Insuficiente si el pipeline es deshonesto**: `@Disabled` sobre tests del enunciado, `catch (Exception e) {}` vacío, aserciones tautológicas, o un test que no puede fallar |
| **Oficio** | semi-automático | ArchUnit verde (los 7 guardianes siguen mordiendo); **sin flaky**: el `91` corre la suite **3 veces** y si el resultado difiere, lo declara; sin credenciales en archivos trackeados; migraciones versionadas |
| **Criterio** | humano | La defensa oral y el reporte de egreso. Guiado por `rubrica/guia-instructor.md` |

**Umbral de aprobación: Núcleo verde (Correctitud + Oficio) Y Criterio ≥ Suficiente.**
Un alumno con todo verde y sin criterio no aprueba: eso es exactamente lo que el curso
no quiere formar.

El boletín **no da nota numérica**: da nivel por eje (Insuficiente / Suficiente /
Competente / Destacado) y el detalle de qué lo llevó ahí. *Es un boletín, no un trofeo.*

## §5 · La rúbrica y la guía del instructor

`rubrica/rubrica-evaluacion.md`: los tres ejes × cuatro niveles, con descriptores
**concretos y verificables** (nada de "buena arquitectura": *"el controlador no conoce la
entidad; la lógica vive en la capa de aplicación; el DTO es lista blanca"*).

`rubrica/guia-instructor.md` — la pieza que hace replicable el juicio humano, y que
**debe traer**:

- **Las preguntas exactas** que destapan criterio, no memoria:
  *"Si solo pudieras escribir tres tests, ¿cuáles y por qué?"* ·
  *"¿Qué NO probaste a propósito?"* ·
  *"Un colega llega mañana a mantener esto: ¿por dónde empieza?"* ·
  *"El brief no decía si paginar. ¿Qué decidiste y qué te habría hecho decidir lo
  contrario?"* ·
  *"¿Qué parte de tu entrega te da más miedo que se rompa en producción?"*
- **Respuestas de ejemplo calibradas por nivel** para al menos tres de esas preguntas:
  qué suena a Suficiente, qué a Competente, qué a Destacado. Un párrafo concreto por
  nivel, no un adjetivo.
- **La gramática del feedback**: una fortaleza real primero; cada crítica convertida en
  acción (*no "tu arquitectura es confusa" sino "mueve el mapeo fuera del controlador y
  el patrón queda claro"*); y el cierre: *el criterio se entrena — un Suficiente hoy es
  un Competente en el próximo proyecto*.
- **Qué hacer con el que no alcanza**: qué se le pide, en qué plazo, y qué se le
  reconoce igual.

## §6 · Anatomía

La de SPEC-000 §7.6, **con estas diferencias declaradas** (este lab es la excepción, y
el `deriva`/`siembra` deben contemplarlo sin parches ad-hoc):

- **No hay** `starter/` con `{{TODO}}`: el starter es la app completa del Lab 12. Los
  huecos los define el brief, no un marcador.
- **`solucion-referencia/`** (no `solucion/`) con su **`NOTA.md`**: *"esta es UNA
  solución, no LA solución. Si la tuya difiere y defiende su criterio, puede estar
  igual de bien o mejor."*
- **`brief/requerimientos-dgt.md`** en lenguaje de negocio, con sus bordes abiertos.
- **`rubrica/`** con los dos documentos de §5.
- **`plantillas/reporte-egreso.md`** con la trampa registrada.
- **`TEORIA.md`**: M14 completo (empaquetado por capas, Buildpacks vs Dockerfile,
  imagen OCI, arranque acelerado —Leyden y GraalVM comparados—, secretos, graceful
  shutdown, sondas de orquestador) **más el cierre del arco narrativo**: qué construyó
  el alumno en 13 sesiones, y la despedida de Carolina. **Este lab no siembra** (no
  tiene sucesor): el job `siembra` del CI debe eximirlo por la misma regla que exime al
  Lab 00, y hay que verificarlo.
- **`INSTRUCTOR.md`**: minutado de la sesión (≈60' teoría+demo de empaquetado / ≈110'
  examen / ≈10' cierre), cómo entregar el brief sin dar pistas, y **cuánto ayudar
  durante el examen** (regla explícita: se responde sobre herramientas, no sobre
  diseño).

## §7 · Verificación del ejecutor (etapa 1)

Citado, sobre estado limpio: (1) la `solucion-referencia/` pasa el boletín completo, con
la salida citada íntegra (los tres ejes); (2) **el boletín muerde**: sabotea la
referencia en copia y cita el rojo de cada caso — un `@Disabled` sobre un test, un
`catch` vacío, una credencial en archivo trackeado, y una regla ArchUnit rota; (3) **la
detección de flaky funciona**: introduce un test no determinista en copia y cita que el
`91` lo declara tras las 3 corridas; (4) la **imagen OCI se construye de verdad**
(`spring-boot:build-image` citado) y la app arranca desde el contenedor con
`/actuator/health` UP — cita el `docker run` y el curl; (5) el starter (app del Lab 12)
**no** aprueba el boletín tal cual: falta el consolidado (citado — el examen tiene algo
que hacer); (6) `deriva` verde con este eslabón especial y `siembra` **eximiendo** al
Lab 13 con su mensaje citado; (7) CI verde, run citado; (8) `ESTADO.md` al día:
**los 13 labs construidos**; (9) declara el tiempo estimado del examen para un alumno
promedio y si las 3 h alcanzan — si crees que no, dilo, es el dato más valioso del
reporte.

## §8 · La Prueba del PO (etapa 2 — cierra la SPEC)

Invitación literal, tres pasos: (1) leer el `brief/requerimientos-dgt.md` **como lo
leerá el alumno** y ver si el requerimiento se entiende sin ayuda; (2) correr
`90-validar.sh --dir solucion-referencia` y ver el **boletín de tres ejes** completo;
(3) leer `rubrica/guia-instructor.md` y juzgar si con eso **otro relator** (Carlos
Marín) podría evaluar igual que Rodrigo. Ese tercer paso es el que de verdad importa:
el examen sirve si es replicable.

## §9 · Criterios de aceptación

- [ ] SPEC commiteada antes del material; rama apilada sobre `spec/018`, PR abierto.
- [ ] Lab 13 completo según §6, con sus diferencias declaradas (sin TODOs, con brief,
      rúbrica y `solucion-referencia/` + `NOTA.md`).
- [ ] El boletín de tres ejes emite niveles, no nota; declara quién mide cada eje.
- [ ] Evidencia §7 íntegra, **incluidos los cuatro sabotajes** y la detección de flaky.
- [ ] Imagen OCI construida y app arrancando desde el contenedor, citado.
- [ ] La guía del instructor trae preguntas exactas + respuestas calibradas por nivel +
      la gramática del feedback.
- [ ] `siembra` exime al Lab 13 con su razón citada.
- [ ] `ESTADO.md` y bitácora al día — **13 labs**.
- [ ] **Prueba del PO reportada** — cierra la SPEC.
- [ ] Commits `SPEC-019:`; checks verdes citados.

## §10 · Reporte

Evidencia de §7, tu juicio honesto sobre si el examen cabe en 3 horas, decisiones
declaradas (qué bordes del brief dejaste abiertos a propósito y cuáles cerraste), URL
del run, `git log --oneline`, discrepancias y hallazgos — sin tocarlos. Cierra con la
invitación del §8.

---

## §11 · Nota para el arquitecto (no ejecutable)

Tras este lab, el curso queda con **13 laboratorios construidos** y los 35 temas
oficiales cubiertos. Queda pendiente un **lab adicional de microservicios**, cuyo
código aportará el PO, que cerrará el alcance del título oficial del curso
("Desarrollo de Microservicios en Java"). Su SPEC se emitirá por separado.
