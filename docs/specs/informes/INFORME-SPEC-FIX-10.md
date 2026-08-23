# INFORME-SPEC-FIX-10 · Los pendientes de la SPEC-040

**Ejecuta:** mocito · **Rama:** `spec-fix-10-pendientes-040` · **Fecha:** 23 de agosto de 2026
**Origen:** encargo del PO, que lo tituló **SPEC-FIX-09**.

> ⚠️ **El número cambió, y conviene saber por qué.** `INFORME-SPEC-FIX-09.md` ya existe desde el
> 20 de agosto: es la SPEC que puso el job `pasos` en el CI. Escribir este encima habría borrado
> aquel. Va como **SPEC-FIX-10**, que era el primer número libre. Si el PO prefiere otra cosa, se
> renombra: no hay nada atado al número.

---

## 0 · Resumen

**Los tres encargos, cerrados** — pero dos de ellos no en la forma que el encargo suponía, y eso es
lo primero que hay que leer:

| # | Encargo | Qué pasó |
|---|---|---|
| **1** | Los seis `instructor/pom.xml` ilegibles | **No hay «lo que genera `instructor/`»**: no existe generador, la hace una persona a mano (D-031-2). Lo que sí se puede arreglar de verdad —y se arregló— es la **regla** y la **vigilancia**. Y de paso se encontró **un daño que introduje yo en la SPEC-040** (§1.2) |
| **2** | El guion del Lab 11 promete vacía una carpeta que no lo está | **Ya estaba corregido**: lo arregló la propia SPEC-040 (commit `852dd1b`). Se verificó, y se explica la decisión que se tomó (§2) |
| **3** | Barrer si hay más de lo mismo | Barrido **mecánico y completo**: **88 promesas** en los quince guiones. **Cero** incumplidas hoy. El barrido dejó una herramienta y un job del CI, no una lista (§3) |

**Lo que queda en el repositorio**, que es lo único que sobrevive a esta sesión:

- `tools/verificar-guion-vs-practica.py` + el job **`guion-practica`** del CI — 88 promesas.
- `tools/verificar-instructor.py` — a mano, y con razón declarada.
- Tres decisiones en `docs/decisiones.md`, entre ellas la regla que faltaba.

---

## 1 · Los seis `pom.xml` de `instructor/`

### 1.1 · No existe «lo que genera `instructor/`»

El encargo dice *«corrige lo que genera `instructor/`»*. Se buscó, y **no hay tal cosa**:

```
tools/  ->  jdk  jib-base  maven  settings-sii.xml
            verificar-pasos-copiables.py  verificar-tamanos.sh  verificar-temario.py
```

`docs/decisiones.md`, **D-031-2**, lo dice con todas las letras: *«La genera quien prepara la
sesión, a partir de `solucion/`»*. Es un proceso **humano**. No hay programa que corregir.

Así que el defecto no se puede arreglar en el generador. Lo que sí se puede es lo que se hizo:
**escribir la regla** donde quien la genera la lee, y **poner algo que la compruebe**.

> **Y hay un matiz que la SPEC-040 no dejó claro y conviene corregir aquí.** `instructor/LEEME.md`
> declara: *«Esto no es un proyecto. No tiene `mvnw`, ni `.mvn`, y no se compila.»* Se comprobó, y
> es verdad: ninguna de las 15 carpetas tiene `mvnw` ni `.mvn`. El `pom.xml` de `instructor/` es
> **la copia explicada del pom**, no un archivo de construcción — en el Lab 13 es literalmente el
> contenido de la clase. Yo los compilé en la SPEC-040 apuntándoles un Maven externo, que es algo
> que el diseño nunca previó.
>
> **Eso rebaja el defecto, pero no lo borra**: un `pom.xml` que no es XML válido lo marca en rojo
> cualquier editor, y quien prepara la clase lo abre creyendo que el material está roto. Sigue
> mereciendo arreglo — pero como defecto de documentación, no de construcción. La SPEC-040 lo
> contó como «seis proyectos que no compilan», y eso era medir con la vara equivocada.

### 1.2 · Un daño que introduje yo en la SPEC-040

El barrido de este encargo encontró esto en `labs/lab-13-empaquetado/instructor/pom.xml`:

```
Y el shim `mvnw` del curso siempre pasa `==offline`, que Jib
```

**Eso lo rompí yo.** El arreglo de la SPEC-040 convirtió *toda* secuencia de dos o más guiones
dentro de un comentario XML en `=`, sin mirar si era una regla decorativa o texto con significado.
`--offline` es una bandera de Maven, y quedó convertida en `==offline`: una bandera que no existe,
en el archivo que el instructor lee en voz alta.

**Reparado**, y no devolviéndole los dos guiones —que volverían a romper el XML— sino diciéndolo de
una forma que sí cabe en un comentario:

```
Y el shim `mvnw` del curso siempre invoca Maven en modo
offline (la bandera `-o`), que Jib respeta.
```

Se buscó el resto del daño de forma exhaustiva —runs de `=` pegados a una palabra, precedidos de
palabra, y flechas `-->` rotas— en los 19 XML bajo `instructor/`. **Ése era el único.**

### 1.3 · La regla, escrita

**D-FIX10-1**, en `docs/decisiones.md`: *en un comentario XML no se escriben dos guiones seguidos*.
Las reglas decorativas van con `=`; una bandera larga se nombra por su forma corta (`-o`) o se
describe en palabras.

No es estilo, es la gramática de XML. El defecto nació de aplicar a un `.xml` el estilo de
comentarios de los `.java`, donde `//  -----` es perfectamente legal.

### 1.4 · La vigilancia: `tools/verificar-instructor.py`

**Un job del CI para esto no se puede escribir, y no se escribió.** `instructor/` no viaja; en el
runner no existe; un job suyo pasaría siempre. Eso es exactamente el **gate decorativo** que el ADN
del curso castiga (P-05) — protegería *menos* que no tenerlo, porque daría verde sin haber mirado
nada. Queda como **D-FIX10-2**.

La comprobación se lleva donde los archivos sí están. Comprueba, sobre las 16 carpetas:

1. Todo XML **parsea**, y ningún comentario XML contiene `--`.
2. `instructor/` está **al día con `solucion/`**: mismo juego de `.java`. *(Es lo que la SPEC-040
   tuvo que arreglar a mano al renombrar paquetes, y lo que se rompe en el siguiente renombre.)*
3. Cada `.java` declara el `package` que le toca por su ruta.
4. Cada carpeta tiene su documento de entrada.

```
Carpetas `instructor/` encontradas: 16
  [OK] labs/lab-00-hola-mundo/instructor  ·  1/1 .java
  ...
  [OK] labs/lab-14-microservicios/instructor  ·  41/41 .java
  [OK] proyecto-final/instructor  ·  examen: sólo XML y paquetes

Comprobados: 19 XML · 171 .java · 16/16 carpetas con su documento de entrada

[OK] `instructor/` está al día con `solucion/` y su XML es válido.
```

**Y falla cuando debe** — se reintrodujo el defecto a propósito en el lab 10:

```
[ERROR] 2 problema(s) en `instructor/`:
  · labs/lab-10-resiliencia/instructor/pom.xml:~2 · un comentario XML contiene «--», que es ilegal
  · labs/lab-10-resiliencia/instructor/pom.xml · no parsea como XML: not well-formed (invalid token): line 3, column 4
```

Si no hay `instructor/` en el clon, **lo dice** —con el número de carpetas encontradas, 0— en vez
de declararse verde en silencio. A-02 y A-04.

> **Lo que sigue siendo verdad y no tiene arreglo dentro de D-031-2:** el contenido de esas
> carpetas **no viaja**. Quien ya tenga un `instructor/` generado antes de hoy sigue teniendo los
> `pom.xml` rotos hasta que corra el verificador. Lo único que se puede versionar es la regla y el
> verificador — y eso es lo que se versionó.

### 1.5 · Los seis, y los labs 10 y 13

El encargo pide regenerar *«los seis, incluidos los de los labs 10 y 13, que la SPEC-040 no
tocaba»*. Precisión: la SPEC-040 **sí arregló los seis**, los labs 10 y 13 incluidos — lo que no
tocó de esos dos labs fue su *contenido*, porque no tenían renombres. Estado hoy, medido: **0
comentarios ilegales en los 19 XML** de las 16 carpetas.

---

## 2 · El Lab 11 · ya estaba corregido, y por qué así

**Lo arregló la SPEC-040**, en el commit `852dd1b`. Antes:

```
observabilidad/                →  pasos 2 y 4 (llega vacía)
```

Hoy:

```
infra/                         →  pasos 2 y 4 (FiltroDeCorrelacion y SaludDeLaBase;
                                   el resto de `infra/` llega dado)
```

Y lo que `practica/` trae de verdad, leído de git:

```
labs/lab-11-observabilidad/practica/src/main/java/cl/dgt/observabilidad/infra/CandadoLibre.java
labs/lab-11-observabilidad/practica/src/main/java/cl/dgt/observabilidad/infra/MotorDePostgres.java
labs/lab-11-observabilidad/practica/src/main/java/cl/dgt/observabilidad/infra/PuertoLibre.java
```

**La decisión fue «que el guion diga la verdad», no «que `practica/` llegue vacía», y se sostiene.**
Las tres clases son fontanería que el alumno **no debe escribir**: `MotorDePostgres` arranca el
PostgreSQL embebido, y `CandadoLibre`/`PuertoLibre` son las guardas que la SPEC-FIX-07/08 puso
precisamente para que un puerto ocupado no parezca un error del alumno. Vaciar la carpeta
significaría o pedirle que las escriba —tres clases de infraestructura que no enseñan nada del
Lab 11, que va de observabilidad— o dejarlas en otro sitio sólo para que el árbol del guion
cuadrara. Lo barato y lo honesto era la frase.

De paso quedó dicho **qué** se escribe en esa carpeta (`FiltroDeCorrelacion` y `SaludDeLaBase`) y
qué llega dado, que antes tampoco estaba.

---

## 3 · El barrido · 88 promesas, y un job que las vigila

### 3.1 · Qué se barrió

No se buscó a ojo. Se escribió `tools/verificar-guion-vs-practica.py`, que lee los quince
`PASOS.md` y `README.md` y comprueba **cuatro formas de promesa**:

| Forma en el guion | Qué exige de `practica/` | Cuántas |
|---|---|---|
| «Se pega: archivo **nuevo** `practica/X`» | X **NO** está (y **SÍ** está en `solucion/`) | **27** |
| «Se pega: en `practica/X`» | X **SÍ** está, o lo crea un paso anterior | **45** |
| «Se pega: `practica/X` — el archivo entero» | X **SÍ** está | **4** |
| «`carpeta/` llega vacía» | ni un `.java` dentro | **12** |
| | | **88** |

**Resultado: cero promesas incumplidas.** Los quince en verde.

### 3.2 · Por qué lee de git y no del disco

El verificador toma `practica/` de `git ls-tree HEAD`, no del sistema de archivos. La razón es
concreta: **en la copia de trabajo de este repositorio hay ahora mismo trece archivos del PO**
—su avance a mano por los labs 01, 02 y 03, que es la prueba de aceptación pendiente—. Leyendo el
disco, el verificador diría que el Lab 01 miente cuando promete `controllers/` vacío. Diría rojo en
cada clon donde alguien esté siguiendo un lab, que es justo cuando más molesta.

### 3.3 · La prueba de que no es un gate decorativo

Un verificador que nunca ha fallado no protege nada. Se corrió contra el tag **`material-v1.3.0`**
—el estado anterior a la SPEC-040— en un worktree aparte:

```
[ERROR] 3 promesa(s) que `practica/` no cumple:

  · lab-09-seguridad/README.md:24 · declara `seguridad/` vacía y `practica/` trae 3: ...
  · lab-11-observabilidad/PASOS.md:17 · declara `observabilidad/` vacía y `practica/` trae 4:
      CandadoLibre.java, Lab11Application.java, MotorDePostgres.java, PuertoLibre.java
  · lab-12-tareas/PASOS.md:18 · declara `tareas/` vacía y `practica/` trae 1: Lab12Application.java
```

**Caza el defecto del Lab 11 para el que se escribió.** Los otros dos son del paquete que
tartamudeaba —`seguridad/seguridad`, `tareas/tareas`—: con el nombre repetido, «la carpeta
`seguridad/`» era ambigua y el verificador no podía saber a cuál de las dos se refería el guion.
La SPEC-040 quitó los tartamudeos, y con ellos la ambigüedad.

### 3.4 · Los falsos positivos que hubo que quitar, y qué enseñan

La primera versión dio **11 hallazgos**; nueve eran del verificador, no del material. Vale la pena
dejarlos escritos porque son la forma del problema:

| Falso positivo | Por qué | Cómo se arregló |
|---|---|---|
| 5 en el Lab 01 · «manda pegar dentro de `HolaController` y `practica/` no lo trae» | **Lo crea el paso 1** del propio guion. A esa altura de la clase el alumno ya lo tiene delante | Se lleva la cuenta de los archivos que un paso anterior manda crear |
| 2 en el Lab 02 · «nombra un archivo que `practica/` no trae» | El encabezado «Se pega: archivo **nuevo**» y la ruta caen en **líneas distintas** — la ruta es larga y envuelve | El encabezado se lee hasta la valla de código, no hasta el fin de línea |
| 1 en el Lab 03 · «dice archivo nuevo y `practica/` YA lo trae» | La palabra «nuevo» venía **del nombre del archivo**: `ProductoNuevoDto.java` | Se reconoce la frase `archivo **nuevo**`, no la palabra suelta |
| 1 en el Lab 09 · lo mismo | «es un endpoint **nuevo**, no reemplaza a ninguno», en la prosa de continuación | Igual |

Los **dos** que quedaron tras cada refinamiento resultaron ser también falsos positivos, y por eso
el resultado final es cero. **Nada del material tuvo que cambiarse en este barrido.**

### 3.5 · Y ahora lo vigila el CI

Job **`guion-practica`**, hermano del `pasos`. Aquí **sí** puede estar en el CI, al revés que el de
`instructor/`: las dos partes que compara —`PASOS.md` y `practica/`— están versionadas. Queda como
**D-FIX10-3**.

El CI pasa de cinco jobs a **seis**.

---

## 4 · Verificación

| Gate | Resultado |
|---|---|
| `guion-practica` (nuevo) | **88 promesas · 15 guiones · 0 incumplidas** |
| `verificar-instructor.py` (nuevo, a mano) | **19 XML · 171 .java · 16/16** |
| `pasos` | `[OK] 15 guion(es) verificado(s)` |
| `temario` | `VEREDICTO: las 5 verificaciones PASAN` |
| `tamanos` | `[OK] Ningún archivo supera los 95 MB` |
| Los dos verificadores nuevos **fallan cuando deben** | Probado: v1.3.0 para uno, defecto reintroducido para el otro |

**No se tocó ni una línea de código de lab, ni un guion, ni un README.** Los cambios son: dos
herramientas nuevas, un job del CI, tres decisiones y el `ESTADO.md`. Más la reparación del
`==offline` en `instructor/`, que no viaja.

---

## 5 · Lo que queda anotado

1. **El contenido de `instructor/` sigue sin respaldo.** Es el costo aceptado de D-031-2, no un
   defecto nuevo. Lo único versionable —la regla y el verificador— ya está versionado. Si el PO
   quiere que además esté respaldado, eso es reabrir D-031-2, y es decisión suya.
2. **La SPEC-040 midió `instructor/` con la vara equivocada** al contar «seis proyectos que no
   compilan» sobre carpetas que su propio `LEEME.md` declara no compilables (§1.1). El defecto era
   real; su tamaño, menor del que dije.
3. **El número de esta SPEC** cambió de `-09` a `-10` para no pisar un informe existente.
4. **`proyecto-final/instructor` no sigue las convenciones de un lab** —no tiene `LEEME.md` sino
   `NOTA.md` y `guia-defensa.md`, y no es espejo de ningún `solucion/`—. Es deliberado: es el
   examen. El verificador lo trata aparte y comprueba de él sólo el XML y los paquetes.
