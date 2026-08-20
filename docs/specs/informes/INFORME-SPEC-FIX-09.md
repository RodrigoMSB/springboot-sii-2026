# INFORME-SPEC-FIX-09 · Los dos pendientes que dejó la SPEC-038

**Ejecuta:** mocito · **Rama:** `fix/pasos-copiables-verificados` · **Fecha:** 20 de agosto de 2026
**Origen:** instrucción directa del PO, no una SPEC del Arquitecto. Cierra los dos pendientes
anotados en el tag `material-v1.2.1`.

---

## 0 · Qué se pedía

El tag de la SPEC-038 dejó escritos dos pendientes:

> *«Pendientes anotados: probarlo en una clase de verdad, el README del lab 04 que repite lo de
> las demos «comentadas», y un job de CI que regenere el guion desde solucion/ y falle si difiere
> — hoy nada vigila esa sincronia.»*

Se cierran los dos que son trabajo del material. El tercero —probarlo en una clase— no lo puede
cerrar nadie desde aquí.

---

## 1 · El README del lab 04 · CERRADO

**Estaba mal, y de la peor manera: contradecía al proyecto.** El `README.md:43` decía:

> En `practica/` las ocho demos están **comentadas** en `Lab04Application`. Cada paso descomenta
> la suya, así que el programa crece contigo: si algo se rompe, sabes qué línea lo rompió.

Y el runner de `practica/` llega así:

```java
    CommandLineRunner run(DemosJpa demos) {
        return args -> {
        };
    }
```

**Vacío.** No hay nada que descomentar.

**Qué se hizo:** se corrigió el texto, conservando la idea pedagógica que sí era verdad —el
programa crece contigo, paso a paso— y añadiendo que el guion trae la línea:

> En `practica/` el `CommandLineRunner` de `Lab04Application` llega **vacío**. Cada paso agrega su
> llamada —el guion trae la línea exacta—, así que el programa crece contigo: si algo se rompe,
> sabes qué línea lo rompió.

**Por qué esta salida y no la otra.** El informe de la SPEC-038 proponía dos caminos y decía que
el segundo era mejor: (a) corregir el README, o (b) **añadir las ocho llamadas comentadas a
`practica/`**, con lo que el guion original recuperaba su sentido y el alumno veía el mapa
completo del lab de un vistazo.

Se hizo **(a)**, y la razón es de riesgo, no de gusto:

- (b) toca `practica/`, y con ello obliga a reescribir los diez pasos del `PASOS.md` que la
  SPEC-038 acaba de verificar pegando bloque a bloque. Tirar una verificación recién hecha para
  rehacerla el mismo día es mal negocio.
- (b) es una **decisión de diseño del material** —qué llega hecho en `practica/`— y esa la toma el
  Arquitecto, no el ejecutor cerrando un pendiente.
- (a) deja los tres documentos diciendo la verdad **hoy**, que es lo que el pendiente pedía.

**(b) sigue disponible y sigue siendo mejor**, y ahora es más barato de lo que parece: si se
adopta, el trabajo es añadir ocho líneas comentadas a `practica/` y cambiar «Se agrega al runner»
por «Se descomenta» en el guion. Queda anotado.

**Comprobado, no leído:**

```
  README: «el CommandLineRunner de Lab04Application llega **vacío**»
  practica/: return args -> { };          (vacío, confirmado)
  PASOS.md: «Se agrega al runner» x8  ·  «Se descomenta» x0
```

Los tres documentos y el código dicen ahora lo mismo.

### 1.b · El mismo defecto está en otros tres labs. **No se tocaron.**

Buscando si el error estaba en más sitios, apareció que sí:

| lab | qué dice | qué hay en `practica/` |
|---|---|---|
| **05** relaciones | «las seis demos están **comentadas**… Cada paso descomenta la suya» | `return args -> { };` **vacío** |
| **06** rendimiento | «las cinco demos están **comentadas**… Cada paso descomenta la suya» | solo `cargador.sembrarSiHaceFalta();` |
| **07** concurrencia | «Las tres demos están **comentadas**… » + «**Se descomenta:**» ×N | `return args -> { };` **vacío** |

**No se corrigieron, a propósito.** Dos razones: no son lo que se pidió, y —la que pesa— la
decisión (a) contra (b) de arriba debería tomarse **una vez para los cuatro labs**, no lab por lab
y por el camino. Si el Arquitecto elige (b), estos tres se arreglan solos con el mismo movimiento.

Es una decisión de una línea, y hasta que se tome, cuatro guiones siguen prometiendo un
descomentado que no existe.

---

## 2 · El job de CI · CERRADO, y no como decía el tag

### 2.1 · Lo que se hizo, y por qué difiere de lo anunciado

El tag prometía *«un job de CI que **regenere** el guion desde solucion/ y falle si difiere del
commiteado»*. **Se implementó una verificación, no una regeneración**, y conviene decir por qué
antes que nada.

Regenerar exige tener commiteada una **plantilla** del guion: el `PASOS.md` menos los bloques de
código. Para el Lab 04 eso son ~500 líneas de prosa duplicadas en dos archivos casi idénticos, con
el reparto de trabajo más frágil que existe: *edita la plantilla, nunca el guion*. Quien no lo
sepa —y nadie lo sabe la primera vez— edita el `PASOS.md`, el CI se pone rojo, y hay que portar el
cambio a mano al otro archivo. Multiplicado por quince labs, son quince documentos fantasma.

La verificación consigue **el mismo invariante** —el guion no promete un código que la solución ya
no tiene— con **una sola fuente de verdad** y sin añadir un archivo al lab.

Lo que se pierde: la regeneración garantizaría además que el guion es *reproducible*. No hace
falta: nadie necesita reconstruir el `PASOS.md` desde cero, lo que hace falta es que no mienta.

### 2.2 · Qué comprueba

`tools/verificar-pasos-copiables.py`, y son **dos comprobaciones de distinta fuerza**:

1. **Toda línea de todo bloque ```java del guion existe en `solucion/`.** Caza el caso corriente:
   alguien renombra un método, cambia un literal o toca una firma, y el guion se queda con lo
   viejo.
2. **Todo bloque que contenga un método completo es idéntico, línea por línea, al método del mismo
   nombre en `solucion/`.** Es la fuerte: no basta con que las líneas existan sueltas, tienen que
   estar en el mismo orden y sin sobras ni faltas.

Las dos ignoran comentarios y líneas en blanco, porque `practica/` va sin documentación y
`solucion/` lleva la suya: los bloques del guion nunca traen los comentarios de la solución.

**Alcance automático:** se activa por el marcador `**Se pega` en un `PASOS.md`. Hoy cubre el Lab
04; si la SPEC-038 se extiende, los labs nuevos entran solos sin tocar el workflow. Y si nadie usa
el formato, la regla queda **armada y no activa**, igual que el job `siembra` cuando nacieron los
labs.

**Lo que NO comprueba, y está dicho en la cabecera del script:** que el guion esté **completo**. Si
alguien añade un método a `solucion/` y no lo menciona en el guion, esto pasa en verde. Vigila que
lo que el guion *dice* sea verdad, no que lo diga todo.

### 2.3 · En verde

```
Guiones con código para pegar (marcador «**Se pega»):
  [OK] lab-04-jpa: 29 bloques · 174 líneas · 17 métodos completos

[OK] 1 guion(es) verificado(s): todo lo que prometen está en solucion/.
```

### 2.4 · Y en rojo — porque un gate que no puede fallar es decorativo

La regla P-05 del ADN, y la lección de la SPEC-FIX-05: **un validador que no se ha visto fallar no
es un validador.** Se le rompió la solución a propósito, de tres maneras distintas, y se comprobó
que las tres se cazan.

**Prueba 1 — renombrar un método del repositorio** (`findByAutor` → `findByAuthor`):

```
[ERROR] 1 desajuste(s) entre el guion y solucion/:
  · lab-04-jpa · bloque 12: esta línea del guion NO está en solucion/
        List<Observacion> findByAutor(String autor);
```

**Prueba 2 — cambiar un literal dentro de una demo** (la fecha de corte, `6, 1` → `7, 1`):

```
salida: 1
[ERROR] 2 desajuste(s) entre el guion y solucion/:
  · lab-04-jpa · bloque 17: esta línea del guion NO está en solucion/
        LocalDate corte = LocalDate.of(2026, 6, 1);
  · lab-04-jpa · bloque 17: el método «buscarConDosCondiciones» del guion NO coincide con el de solucion/
```

**Prueba 3 — añadir una línea al cuerpo de un método.** Esta es la que justifica tener las dos
comprobaciones: **todas** las líneas del guion siguen existiendo en `solucion/`, así que la
comprobación (1) pasa. Solo la (2) lo caza:

```
salida: 1
[ERROR] 1 desajuste(s) entre el guion y solucion/:
  · lab-04-jpa · bloque 24: el método «borrar» del guion NO coincide con el de solucion/
      guion    (6 líneas): ['public void borrar() {', ...]
      solucion (7 líneas): ['public void borrar() {', ...]
```

Y con todo restaurado, verde otra vez y `solucion/` intacta:

```
  [OK] lab-04-jpa: 29 bloques · 174 líneas · 17 métodos completos
  -> salida: 0
```

El mensaje de error dice además **qué hacer**, que es la mitad del valor de un gate:

> El guion promete un código que la solución ya no tiene. Se arregla volviendo a extraer los
> bloques desde `solucion/` — nunca al revés, y nunca tecleándolos a mano (SPEC-038 §2).

### 2.5 · El job

`pasos · el código del guion coincide con solucion/`, en `material-ci.yml`, antes de `labs`. No
necesita Java ni red: compara texto contra texto y corre en segundos. El CI pasa de cuatro jobs a
**cinco**.

---

## 3 · Verificación

| # | Prueba | Resultado |
|---|---|---|
| 1 | El README del lab 04 dice la verdad | ✅ comprobado contra el código, no leído |
| 2 | README, `PASOS.md` y `practica/` coherentes entre sí | ✅ «Se agrega al runner» ×8, «Se descomenta» ×0, runner vacío |
| 3 | El job nuevo, en verde | ✅ 29 bloques · 174 líneas · 17 métodos |
| 4 | El job nuevo **se pone rojo** | ✅ tres roturas distintas, tres detecciones, salida 1 |
| 5 | `solucion/` intacta tras las pruebas | ✅ `git status` limpio |
| 6 | Los cinco jobs del CI, en local | ✅ `temario` · `siembra` · `pasos` · `labs` (37 proyectos, 0 fallos) |
| 7 | Ningún código de lab tocado | ✅ solo `README.md` del lab 04, el workflow, `ESTADO.md` y el script nuevo |

---

## 4 · Lo que este informe NO puede afirmar

- **Que el formato de la SPEC-038 sirva en clase.** Sigue siendo el pendiente que nadie puede
  cerrar desde el repositorio, y el único de los tres que importa de verdad.
- **Que el guion esté completo.** El job vigila que lo que promete sea verdad, no que prometa todo
  (§2.2).
- **Que los labs 05, 06 y 07 estén bien.** Están mal, igual que estaba el 04, y se dejan así a
  propósito hasta que se decida (a) o (b) para los cuatro a la vez (§1.b).
