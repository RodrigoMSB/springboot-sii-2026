# INFORME-SPEC-039 · El formato «se pega» en los catorce labs restantes

**Ejecuta:** mocito · **Rama:** `spec-039-pasos-copiables-resto` · **Fecha:** 20 de agosto de 2026
**Origen:** instrucción directa del PO. Extiende la **SPEC-038** (piloto en el Lab 04) al resto.

---

## 0 · Resumen

**Los quince guiones del arco traen ya el código para pegar**, con el archivo y el sitio dichos, y
**el job `pasos` del CI los vigila a los quince**: 15 guiones, 146 bloques, 1.081 líneas y 87
métodos comprobados **línea por línea contra `solucion/`**.

**`proyecto-final/` queda fuera, a propósito** (§4.1): no es un laboratorio, es el examen.

Solo cambian archivos `.md` y el verificador. **Ni una línea de código de lab tocada.**

**El ejercicio destapó ocho defectos preexistentes**, tres de ellos de los que rompen la clase
(§3). El más caro lo cazó la máquina, no yo: el `SembradorDeUsuarios` del Lab 09 prometía un código
con **una línea menos** que la solución.

**Y hay una deuda declarada, que es lo primero que hay que leer de este informe:** la verificación
V1 —pegar los bloques en `practica/` limpia y comprobar que se llega a `solucion/`— se ejecutó
**en diez de los quince labs**. En los cinco restantes se verificó el contenido de los bloques,
pero no el pegado. Está en §5, con el detalle lab por lab.

---

## 1 · Qué quedó hecho, lab por lab

| lab | secciones convertidas | bloques que vigila el CI | V1 pegado |
|---|---|---|---|
| **00** hola-mundo | 2 | 1 · 3 líneas | ✅ compila · idéntico |
| **01** web | 6 | 13 · 50 líneas · 5 métodos · 2 intermedios | ✅ compila · idéntico (solo orden de imports) |
| **02** di | 7 | 6 · 106 líneas · 12 métodos · 5 intermedios | ✅ compila · **7 archivos idénticos** |
| **03** errores | 5 | 14 · 66 líneas · 7 métodos · 1 intermedio | ✅ compila · idéntico (solo orden) |
| **04** jpa *(SPEC-038)* | — | 28 · 166 líneas · 21 métodos | ✅ ya verificado en la 038 |
| **05** relaciones | 12 | 24 · 104 líneas · 9 métodos | ✅ compila · **8 archivos idénticos** |
| **06** rendimiento | 11 | 20 · 92 líneas · 6 métodos · 1 intermedio | ✅ compila · **11 archivos idénticos** |
| **07** concurrencia | 6 | 9 · 36 líneas · 4 métodos | ✅ compila · **11 archivos idénticos** |
| **08** testing | 7 | 7 · 120 líneas · 9 métodos · 1 intermedio | ✅ **test-compile** · **13 archivos** (con `src/test`) |
| **09** seguridad | 6 + 4 re-extraídos | 12 · 156 líneas · 12 métodos · 1 intermedio | ✅ compila · **13 archivos idénticos** |
| **10** resiliencia | 4 | 6 · 45 líneas · 1 método · 1 intermedio | ⚠️ no pegado (§5) |
| **11** observabilidad | 5 | 4 · 53 líneas · 4 métodos | ⚠️ no pegado (§5) |
| **12** tareas | 4 | 6 · 42 líneas · 5 métodos | ⚠️ no pegado (§5) |
| **13** empaquetado | 3 | **0** (sus bloques son XML y YAML) | ⚠️ no pegado (§5) |
| **14** microservicios | 7 | 6 · 41 líneas · 1 método · 8 intermedios | ⚠️ no pegado aquí; **sí en la SPEC-037** (V7: 48 archivos idénticos) |

---

## 2 · Cómo se hizo, y por qué así

El piloto se hizo a mano. Catorce labs a mano no se hacen, así que lo primero fue un motor:

- **`analizar.py`** — qué le falta a `practica/` respecto de `solucion/`, a nivel de archivo, de
  método, de import y de campo. Es lo que dice qué bloques hacen falta.
- **`bloques.py`** — extrae de `solucion/` los bloques que un *spec* describe (archivo entero,
  método entero con sus anotaciones, imports, campos, declaraciones de interfaz con su `@Query`) y
  sabe **pegarlos** en `practica/` simulando al instructor.
- **`v1.sh`** — reset de `practica/` → pegar en orden → compilar → comparar contra `solucion/`.
- **`pegar_guion.py`** — la variante fuerte: pega **los bloques tal como quedaron escritos en el
  `PASOS.md`**, no los que yo extraje. Es la que se usó en los labs 08 y 09.

**El motor se validó antes de usarlo**, reproduciendo el Lab 04 —ya verificado y commiteado en la
SPEC-038— bloque a bloque: **7 de 7 idénticos** a lo que hay en el repositorio.

**Lo que NO se commitea:** el motor se queda fuera. Es andamiaje de una migración que ya ocurrió, y
lo que hay que conservar —que el guion no mienta— lo garantiza el job `pasos`, que sí está en
`tools/`. Meter cuatro scripts más al repositorio para no volver a usarlos sería ruido.

---

## 3 · Los defectos preexistentes que destapó

Ninguno lo introduce esta SPEC. Todos salen de intentar seguir el guion literalmente.

### 3.1 · Bloques sin `package` ni `import` — labs 02, 09

El caso más repetido y el que más duele en vivo. El guion del **Lab 02** mostraba así el archivo
que hay que crear:

```java
@Repository
public class ProductoRepositoryLista implements ProductoRepository {
```

Pegado tal cual, **no compila**: falta el `package` y los cuatro imports. Lo mismo en cuatro
bloques del Lab 02 y en tres del Lab 09. **Corregidos**: ahora todos esos bloques son el archivo
entero, extraído de `solucion/`.

### 3.2 · Un guion que prometía código que la solución no tiene — Lab 09

Lo cazó el verificador, no yo:

```
· lab-09-seguridad · bloque 2: el método «run» del guion NO coincide con el de solucion/
    guion    (10 líneas)
    solucion (11 líneas)
```

Al `SembradorDeUsuarios` del guion le faltaba esta línea, que `solucion/` sí imprime:

```java
System.out.println("[semilla] usuarios ana/secreta (ADMIN) y luis/secreta (USUARIO)");
```

**Corregido re-extrayendo el archivo desde `solucion/`**, que es la regla de la SPEC-038 §2: nunca
al revés. Es exactamente el defecto que el job `pasos` existe para que no vuelva a pasar, y ha
aparecido en su primera aplicación masiva.

### 3.3 · «Se descomenta» sobre runners vacíos — labs 05, 06, 07

El mismo defecto que la SPEC-FIX-09 cerró en el Lab 04, y que aquel informe dejó anotado en estos
tres. Los `README.md` y los `PASOS.md` decían que las demos llegan **comentadas** en
`Lab0NApplication`; los runners llegan **vacíos**.

**Corregidos los tres**, y en los dos documentos. Es una decisión que tomé solo y está en §4.2.

### 3.4 · Pasos sin bloque de código — labs 05, 07

Varios pasos describían el código en prosa y no lo mostraban:

> **Se escribe:** la demo 2. Cargar el trámite `primerTramiteId`, imprimirlo, imprimir una marca, y
> **solo entonces** pedir `tramite.getContribuyente().getRazonSocial()`.

El alumno tenía que redactarlo. **Ahora traen el método entero.** Es, de los ocho, el que más
justifica la SPEC-038: son seis pasos del Lab 05 y tres del Lab 07 en los que el guion pedía algo
que no enseñaba.

### 3.5 · Un método que nadie mencionaba — Lab 06

`DemosRendimiento.informe(...)` —el ayudante que imprime el contador de consultas y el tiempo, o
sea **el número del laboratorio**— está en `solucion/`, no está en `practica/`, y **el guion no lo
nombraba en ningún paso**. Sin él no compila ninguna de las cinco demos. **Añadido al paso 1.**

### 3.6 · Un bloque sin rótulo — Lab 08

Los dos tests extra del paso 1 («Y ahora dos más, del mismo tamaño…») colgaban de un bloque sin
ninguna indicación de dónde van. **Rotulado.**

### 3.7 · Un endpoint sin sus imports — Lab 09

`quienSoy()` se daba como bloque, pero sus tres imports (`AuthenticationPrincipal`, `Jwt`, `Map`)
no aparecían en ningún sitio. Pegado tal cual, no compila. **Añadidos.**

### 3.8 · El `pom.xml` sin rotular — labs 09, 11, 13

Tres labs añaden dependencias o plugins y el guion decía «se escribe en `pom.xml`» sin decir
dónde. **Ahora dicen «dentro de `<dependencies>`» / «dentro de `<plugins>`».** Se descubrió porque
el Lab 09 **no compilaba** al pegar solo los bloques Java: faltaban las dos dependencias.

---

## 4 · Las decisiones que tomé solo

### 4.1 · `proyecto-final/` queda fuera

**No se le aplicó el formato, y es deliberado.** Su `README.md` lo dice en la tercera línea:

> **Esto no es un laboratorio.** Es el instrumento con el que se evalúa el curso. […] Aquí no hay
> guion.

No tiene `PASOS.md` porque no debe tenerlo. Ponerle bloques para pegar sería **entregarle al alumno
las respuestas del examen** y destruir el instrumento con el que el PO certifica. Es la decisión
más conservadora posible: no tocarlo.

### 4.2 · Los READMEs de los labs 05, 06 y 07

La SPEC-FIX-09 dejó anotado que estos tres arrastran el defecto del «descomenta» y **no los tocó**,
esperando una decisión: corregir el texto, o poner las llamadas comentadas en `practica/`.

**Los corregí por texto**, porque al convertir sus `PASOS.md` a «Se agrega al runner» el README
pasaba a contradecir al guion en el mismo commit. Dejar eso a medias era peor que cualquiera de las
dos salidas. **La otra opción —las llamadas comentadas en `practica/`— sigue disponible y sigue
siendo mejor**, y ahora es más barata: son ocho líneas por lab y cambiar un rótulo.

### 4.3 · La marca `pasos:intermedio`

Varios labs escriben en un paso una versión que un paso posterior reescribe: el Lab 01 lo hace dos
veces, el Lab 02 cinco, el Lab 14 ocho. Ese código **es real y el instructor lo pega**, pero no está
en `solucion/`, que guarda el estado final.

Se marcan con `<!-- pasos:intermedio · razón -->` y el verificador los salta — **pero los cuenta y
los imprime**, para que saltarse un bloque sea una decisión visible y no una forma barata de poner
el CI en verde.

### 4.4 · El verificador, afinado cuatro veces

Cada lab nuevo encontró un límite del job que nació con la SPEC-FIX-09:

1. **Solo mira bloques que cuelgan de un «Se pega»** — un guion también enseña código que ya está en
   `practica/`, para mirarlo y criticarlo (el `.orElseThrow()` sin argumentos del Lab 03), y ese no
   se pega ni tiene por qué estar en `solucion/`.
2. **Marcadores anclados a principio de línea** — buscarlos con `rfind` sobre el texto crudo hacía
   que «Se pega» citado a media frase contara como marcador.
3. **«Se agrega al runner» también se vigila** — esas líneas están en el `Lab0NApplication` de
   `solucion/`, y dejarlas fuera perdía cobertura real.
4. **Métodos sin modificador de acceso** — los tests de JUnit son package-private, y sin esto la
   comprobación fuerte no los veía. Los cuatro archivos de test del Lab 08 quedaban sin vigilar.

### 4.5 · El andamiaje no se commitea

Ver §2. Se conserva en el informe qué hace y por qué; lo que tiene que sobrevivir es el job `pasos`.

---

## 5 · La deuda: la V1 que falta

**Lo que se pedía:** pegar los bloques en `practica/` limpia, en orden, sin abrir `solucion/`, y
comprobar que compila y llega al mismo resultado.

**Se hizo en diez labs** — 00, 01, 02, 03, 04, 05, 06, 07, 08 y 09 — con el resultado de la tabla
de §1: todos compilan y el resultado final es idéntico a `solucion/` salvo el orden de los imports.

**No se hizo en cinco:**

| lab | por qué |
|---|---|
| **10, 11, 12** | Sus bloques son **fragmentos que se insertan en sitio** —un campo aquí, dos líneas dentro de un método allá—, y colocarlos mecánicamente necesita una regla escrita a mano por bloque. El arnés no llega, y hacerlo a mano habría costado más que el resto del trabajo junto |
| **13** | Sus bloques son **XML y YAML**, no Java: ni el arnés ni el job `pasos` los tocan (§6) |
| **14** | Mismo caso que 10–12, **pero su pegado completo ya está verificado**: la SPEC-037 hizo el paseo entero de los ocho pasos sobre `practica/` limpia con resultado **48 archivos idénticos a `solucion/`**, y esta SPEC **no cambió el contenido de ningún bloque suyo**, solo los rótulos |

**Lo que sí está verificado en los cinco:** que cada línea de cada bloque existe en `solucion/`, y
que cada método completo coincide con el suyo línea por línea. Es la comprobación del job `pasos`,
y es fuerte — pero **no es lo mismo que haber pegado**. Un bloque correcto colocado en el sitio
equivocado sigue sin compilar, y eso en estos cinco labs no se ha probado.

---

## 6 · Lo que el job `pasos` sigue sin ver

- **Los bloques que no son Java.** El **Lab 13 tiene cero bloques vigilados**: los suyos son
  fragmentos de `pom.xml` y de `application*.yml`. Lo mismo, en menor medida, en los labs 09, 11 y
  12. Extender la comprobación a XML y YAML es un trabajo aparte y no trivial (hay que comparar
  fragmentos contra un archivo con otra estructura).
- **Que el guion esté completo.** Vigila que lo que el guion *dice* sea verdad, no que lo diga
  todo. El defecto §3.5 —un método que la solución tiene y el guion nunca mencionaba— **pasaría en
  verde**. Lo encontró `analizar.py`, que no está en el CI.

---

## 7 · Lo que queda por repasar o arreglar

Por orden de lo que más puede doler en una sala:

1. **La V1 que falta en los labs 10, 11, 12 y 13** (§5). Es lo único de esta entrega que no está
   probado como se usa.
2. **Probarlo en una clase de verdad.** Sigue siendo el pendiente de la SPEC-038 y el único que
   importa: que los bloques ahorren el trabajo mental que el PO describe solo lo dice una clase.
3. **Decidir lo de las llamadas comentadas en `practica/`** (§4.2) para los labs 04, 05, 06 y 07 a
   la vez. Hoy los cuatro dicen la verdad; la otra opción sigue siendo mejor material.
4. **Extender el job `pasos` a XML y YAML** (§6), o aceptar por escrito que el Lab 13 no está
   cubierto.
5. **Los `instructor/` no se tocaron**, y en varios labs describen el guion viejo («se descomenta»,
   «se escribe»). No viajan al repositorio, así que no rompen nada al alumno — pero quien prepare
   la clase leerá una cosa y dictará otra.
6. **El orden de los imports.** En seis labs, pegar los bloques deja los imports en distinto orden
   que `solucion/`. No cambia nada y está avisado en el guion del Lab 04; en los demás no se avisó.
7. **Los números de consola no se revisaron.** Esta SPEC no tocó ningún bloque «En consola», así
   que si alguno estaba desactualizado —como los tres que encontró la SPEC-038 en el Lab 04— sigue
   estándolo. Nadie los ha vuelto a medir salvo en los labs 04 y 14.

---

## 8 · Lo que este informe NO puede afirmar

- **Que los cinco labs sin V1 se peguen bien.** Los bloques son correctos; su colocación, no
  probada.
- **Que el formato sirva en clase.** Ver §7.2.
- **Que los guiones estén completos.** El job no lo mira (§6), y `analizar.py` solo se pasó una vez
  por lab.
- **Que funcione en Windows.** Todo se midió en macOS. Son documentos de texto, así que el riesgo es
  bajo, pero es el mismo «riesgo bajo» que escondió tres defectos en la SPEC-024.
