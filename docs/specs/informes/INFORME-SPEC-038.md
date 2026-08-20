# INFORME-SPEC-038 · `PASOS.md` con el código listo para pegar — piloto en el Lab 04

**Ejecuta:** mocito · **Rama:** `spec-038-pasos-copiables-lab04` · **Fecha:** 20 de agosto de 2026
**Estado:** ejecutado y verificado. **Es un piloto: la decisión de extenderlo al resto es del PO.**

---

## 0 · Resumen

`labs/lab-04-jpa/PASOS.md` trae ahora, en cada paso, el **bloque exacto que va en el archivo**,
con el archivo y el sitio dichos sin ambigüedad. **Un solo archivo cambió en todo el repositorio.**

Los bloques **no están tecleados**: se extraen de `solucion/` con un script, y se verificó por dos
caminos independientes que cada línea de cada bloque proviene de ahí (§3).

La prueba que importa —la V1, que es literalmente lo que hará el PO en clase— se corrió de punta a
punta: **partir de `practica/` limpia, pegar solo los bloques, en orden, sin abrir `solucion/`.**
Los diez pasos compilan y arrancan, las ocho demos imprimen lo mismo que `solucion/`, y el
resultado final es idéntico a `solucion/` salvo comentarios.

**El guion creció de 393 a 719 líneas**, pero la parte que se *lee* creció apenas **+95**: casi
todo lo añadido es código que se copia, no prosa que se lee (§6). No se vuelve inmanejable.

**Y el piloto encontró tres defectos preexistentes en el guion del Lab 04** (§5). Los tres son de
la clase que solo aparece cuando alguien intenta seguir el guion al pie de la letra, que es
exactamente para lo que servía este ejercicio.

---

## 1 · El formato, tal como quedó

Cada paso conserva lo que ya tenía —**Se explica**, **En consola**— y suma:

- **`Se pega:`** el bloque, con el archivo y el sitio. Tres formas, según el caso:
  - **archivo entero** (pasos 1, 2 y 10): «archivo **nuevo** … el archivo entero».
  - **método entero** (pasos 3 a 8): «**reemplazando el método `guardar()` entero** (desde su
    firma hasta su llave de cierre)». Reemplazar un método completo no tiene ambigüedad posible:
    la firma está a la vista y el bloque trae la suya.
  - **línea suelta** (imports, métodos del repositorio): «**arriba**, con los imports» / «**dentro
    de la interfaz**».
- **`Se agrega al runner:`** la línea exacta que va en `Lab04Application`.
- Cuando un paso toca más de un sitio, los bloques van rotulados **`(1 de 3)`, `(2 de 3)`,
  `(3 de 3)`** y en el orden en que conviene pegarlos.
- El bloque va **antes** de «En consola», que es lo que se mira después de pegar.

Y una nota al principio del documento que explica cómo leerlo, y remite a `instructor/` para el
*por qué* — que es la división de trabajo que pedía la SPEC.

---

## 2 · Dos pasos completos, citados

### 2.1 · Uno simple — paso 2

> ## Paso 2 · El repositorio
>
> **Se explica:** no hace falta escribir la clase que guarda y busca. Se declara una interfaz y
> Spring Data genera la implementación al arrancar.
>
> Eso solo ya trae `save`, `findById`, `findAll`, `deleteById` y `count`. Los métodos propios
> llegan en los pasos 5, 6 y 8.
>
> **Se pega:** archivo **nuevo**
> `practica/src/main/java/cl/dgt/jpa/repositories/ObservacionRepository.java` — el archivo entero.
>
> ```java
> package cl.dgt.jpa.repositories;
>
> import cl.dgt.jpa.entities.Observacion;
> import org.springframework.data.jpa.repository.JpaRepository;
>
> public interface ObservacionRepository extends JpaRepository<Observacion, Long> {
> }
> ```
>
> **Se agrega al runner:** nada todavía.
>
> **En consola:** otra vez, arranca y termina. Sin errores.

Nótese lo que **no** trae: `import java.util.List;` ni `import java.time.LocalDate;`. Los dos
llegan en los pasos 5 y 6, con el método que los necesita. Es la regla de imports de la SPEC §1
aplicada al pie de la letra.

### 2.2 · Uno que toca dos archivos — paso 7

> ## Paso 7 · Actualizar sin `save()`
>
> **Se explica:** este es el momento raro del laboratorio, y conviene decirlo antes: vamos a
> cambiar un dato en la base **sin llamar a `save`**. Dentro de una transacción, el objeto que
> cargaste queda vigilado; al cerrar, Hibernate compara y lanza el `UPDATE` solo.
>
> Hace falta un setter en la entidad —`setTexto`— que hasta ahora no existía: es la primera vez
> que se modifica una observación.
>
> **Se pega (1 de 3):** en `entities/Observacion.java`, **junto a los getters**, antes del
> `@Override` del `toString()`.
>
> ```java
>     public void setTexto(String texto) { this.texto = texto; }
> ```
>
> **Se pega (2 de 3):** en `demos/DemosJpa.java`, **arriba**, con los imports.
>
> ```java
> import org.springframework.transaction.annotation.Transactional;
> ```
>
> **Se pega (3 de 3):** en `demos/DemosJpa.java`, **reemplazando el método `actualizar()` entero**.
> Ojo con la anotación `@Transactional`: va con el bloque, encima de la firma.
>
> ```java
>     @Transactional
>     public void actualizar() {
>         seccion(6, "ACTUALIZAR SIN save() · dirty checking");
>
>         Observacion observacion = repositorio.findById(primerId).orElseThrow();
>         System.out.println("  antes:  " + observacion.getTexto());
>
>         observacion.setTexto("Revisión anual: se detecta diferencia menor.");
>         System.out.println("  después: " + observacion.getTexto());
>         System.out.println("  NO llamamos a save(). El UPDATE aparece justo aquí abajo,");
>         System.out.println("  cuando esta transacción se cierre:");
>     }
> ```
>
> **Se agrega al runner:**
>
> ```java
>             demos.actualizar();
> ```

Tres bloques, dos archivos, cada uno con su sitio. La `@Transactional` viaja **dentro** del bloque
del método —no como una instrucción aparte— porque una anotación que hay que acordarse de poner es
una anotación que alguien va a olvidar.

---

## 3 · V4 · los bloques están extraídos, no tecleados

La regla de fondo de la SPEC §2. Se resolvió con un extractor que **lee los archivos de
`solucion/`** y compone los bloques: saca métodos completos localizando su firma y contando llaves,
saca líneas sueltas por su contenido, y quita los comentarios pedagógicos (porque `practica/` va
sin documentación, SPEC §3).

El `PASOS.md` se genera sustituyendo marcadores `{{...}}` de una plantilla por esos bloques, así
que **el documento no contiene una sola línea de código escrita a mano**.

Verificado por **dos caminos independientes**:

**(a) Cada línea de cada bloque existe en `solucion/`:**

```
  bloques verificados: 21
  lineas que NO provienen de solucion/: 0
```

**(b) Al revés — cada bloque extraído aparece VERBATIM en el `PASOS.md` que quedó escrito:**

```
  bloques de solucion/ presentes VERBATIM en PASOS.md: 21/21
  bloques ```java en PASOS.md: 29  (los que sobran son runner y curl)
```

El segundo es el que de verdad protege: comprueba lo que quedó en el documento, no lo que el
generador *creía* haber puesto.

> **Nota para cuando se extienda al resto:** el extractor y la plantilla quedaron fuera del
> repositorio a propósito, porque la V6 de esta SPEC exige que solo cambie `PASOS.md`. Si el piloto
> se aprueba, lo que corresponde es promoverlos a `tools/` y añadir al CI un job que regenere el
> guion y falle si el resultado difiere del commiteado — con eso, tocar `solucion/` sin actualizar
> el guion pasa a ser imposible. **Hoy esa garantía no existe:** los bloques están sincronizados
> porque se acaban de extraer, no porque nada lo vigile.

---

## 4 · La verificación

### V1 · pegar los bloques en `practica/` limpia, en orden — **CUMPLE** ★

Es la prueba de esta SPEC. Se partió de `practica/` en su estado de entrega exacto (comprobado con
`git status` vacío), se pegaron **solo** los bloques del `PASOS.md`, en el orden del guion, y se
compiló y arrancó en cada paso:

```
########## PASO 1   [COMPILA] [ARRANCA]     entities/Observacion.java (archivo nuevo)
########## PASO 2   [COMPILA] [ARRANCA]     repositories/ObservacionRepository.java (archivo nuevo)
########## PASO 3   [COMPILA] [ARRANCA]     imports + cabecera + método guardar() + runner
########## PASO 4   [COMPILA] [ARRANCA]     imports + buscarPorId() + listarTodas() + runner
########## PASO 5   [COMPILA] [ARRANCA]     import + findByAutor() + buscarPorAutor() + runner
########## PASO 6   [COMPILA] [ARRANCA]     import + findByAutorAndFechaAfter() + demo + runner
########## PASO 7   [COMPILA] [ARRANCA]     setTexto() + import + actualizar() + runner
########## PASO 8   [COMPILA] [ARRANCA]     countByAutor() + borrar() + contar() + runner
########## PASO 9   [ARRANCA]               runner con una sola llamada
########## PASO 10  [COMPILA] [ARRANCA]     ObservacionController.java (archivo entero)
```

**Diez de diez.** Ningún paso necesitó tocar nada que el guion no dijera.

### V2 · el resultado final vs `solucion/` — **CUMPLE**

```
  [IDÉNTICO] entities/Observacion.java
  [DIFIERE]  repositories/ObservacionRepository.java
      4d3
      < import java.util.List;
      5a5
      > import java.util.List;
  [DIFIERE]  demos/DemosJpa.java
      2d1
      < import org.springframework.stereotype.Component;
      4a4,5
      > import org.springframework.stereotype.Component;
      > import org.springframework.transaction.annotation.Transactional;
      8d8
      < import org.springframework.transaction.annotation.Transactional;
  [IDÉNTICO] web/ObservacionController.java
  [DIFIERE]  Lab04Application.java
      29c29
      <     static final int PUERTO_BASE = 55432;
      ---
      >     static final int PUERTO_BASE = 55433;
```

**Las tres diferencias son las tres inofensivas y hay que decirlo con nombre:**

1. **El orden de los imports** en dos archivos. No cambia nada —Java no se entera— y depende de
   dónde los pegue cada uno. Está avisado en el propio guion, para que nadie crea que se equivocó.
2. **El puerto de la base**, 55432 contra 55433. Es de diseño: `practica/` y `solucion/` corren a
   la vez sin pisarse.

Ni una diferencia de código. Los dos archivos que se pegan enteros —la entidad y el controller—
salen **idénticos**.

### V3 · las 8 demos tras pegar — **CUMPLE**

Comparadas contra la salida de `solucion/`, normalizando los `id` (que avanzan entre arranques,
§5.3):

```
  IDÉNTICO: las 8 demos imprimen exactamente lo mismo
  (31 líneas comparadas)
```

Y el momento del laboratorio, el `UPDATE` del paso 7 que aparece **después** del último `println`,
salió donde el guion dice:

```
  NO llamamos a save(). El UPDATE aparece justo aquí abajo,
  cuando esta transacción se cierre:
Hibernate:
    update
        observacion
    set
        autor=?,
        fecha=?,
        texto=?
    where
        id=?
```

El paso 10 también, por HTTP:

```
  GET  /api/observaciones      -> [{"texto":"Solicita certificado de situación.",...,"id":20}, ...]
  GET  ?autor=Carolina         -> [{"texto":"Solicita certificado de situación.",...,"id":20}]
  GET  /api/observaciones/9999 -> HTTP 404
  POST /api/observaciones      -> HTTP 201  {"texto":"Creada desde Postman.",...,"id":23}
```

Y el cierre: Ctrl+C, arrancar otra vez, y la observación creada por HTTP sigue ahí.

### V4 · cada bloque ↔ su origen en `solucion/` — **CUMPLE**

Ver §3: 21/21 por los dos caminos, 0 líneas tecleadas.

### V5 · los imports — **CUMPLE**

Ningún paso dejó un bloque que no compilara por un import no mencionado. Se demuestra por V1: los
diez pasos compilan, y el compilador es el único juez que importa aquí. Los imports se reparten
así, cada uno con el paso que lo necesita:

| paso | import que introduce | por qué ahí |
|---|---|---|
| 3 | `Observacion`, `ObservacionRepository`, `LocalDate` | el campo, el constructor y `LocalDate.of(...)` |
| 4 | `List`, `Optional` | `findAll()` devuelve lista, `findById()` devuelve `Optional` |
| 5 | `java.util.List` *(en el repositorio)* | `List<Observacion> findByAutor(...)` |
| 6 | `java.time.LocalDate` *(en el repositorio)* | el parámetro de la fecha de corte |
| 7 | `Transactional` | la anotación del método |

### V6 · el lab sin tocar — **CUMPLE**

```
$ git status --short
 M labs/lab-04-jpa/PASOS.md

  archivos del lab 04 modificados: 1
```

`practica/`, `solucion/` e `instructor/` sin una línea de cambio. Se restauraron con
`git checkout` tras el paseo y se comprobó que el árbol quedó limpio.

*(Los archivos sin trackear de los labs 01 y 02 son del PO, son anteriores a esta SPEC y no se
tocaron.)*

---

## 5 · Tres defectos del guion que el piloto destapó

Ninguno lo introduce esta SPEC. Los tres estaban desde antes y **solo aparecen cuando alguien
intenta seguir el guion literalmente**, que es lo que este ejercicio obliga a hacer.

### 5.1 · «Se descomenta» — no había nada comentado

El `PASOS.md` decía **ocho veces** «**Se descomenta:** `demos.guardar();`», y el `README.md` del
lab lo repite: *«En `practica/` las ocho demos están **comentadas** en `Lab04Application`»*.

No lo están. El runner de `practica/` llega así:

```java
    @Bean
    CommandLineRunner run(DemosJpa demos) {
        return args -> {
        };
    }
```

**Vacío.** No hay nada que descomentar: hay que **escribir** las ocho líneas.

**Qué se hizo:** el guion dice ahora «**Se agrega al runner:**» y entrega la línea exacta. Es una
corrección de un hecho, no un cambio de paso.

**Qué queda pendiente y es del Arquitecto:** `README.md:43` sigue diciendo lo mismo. **No se
tocó**, porque la V6 de esta SPEC exige que solo cambie `PASOS.md`. Hay dos salidas y las dos son
legítimas: corregir esa línea del README, o —mejor— **añadir las ocho llamadas comentadas a
`practica/`**, con lo que el guion original recupera su sentido y el alumno ve de un vistazo el
mapa completo del laboratorio. La segunda toca `practica/` y por eso no se hizo aquí.

### 5.2 · El paso 9 decía «3 observaciones» y son **2**

El guion mostraba:

```
  3 observaciones:
    Observacion{id=2, ...}
    Observacion{id=3, ...}
```

Un `3` encabezando dos filas. Lo que de verdad imprime, medido:

```
=== 3 · LISTAR TODAS · findAll() ===
  2 observaciones:
    Observacion{id=17, texto='Solicita certificado de situación.', autor='Carolina', ...}
    Observacion{id=18, texto='Diferencias en el F29 de julio.', autor='Ignacio', ...}
```

**Dos**, porque el paso 8 borró una y sigue borrada — que es justamente lo que el paso quiere
demostrar. Corregido, y con una frase que lo dice en voz alta en vez de dejarlo al ojo del lector.

### 5.3 · Los `id` no son `1, 2, 3` más que la primera vez

`guardar()` empieza con `repositorio.deleteAll()` —línea que, de paso, **el guion nunca
mencionaba**— pero borrar filas no devuelve atrás la secuencia de la base. Medido, arrancando una
vez por paso como manda el guion:

| corrida | ids |
|---|---|
| paso 3 | 1, 2, 3 |
| paso 4 | 4, 5, 6 |
| paso 5 | 7, 8, 9 |
| paso 6 | 10, 11, 12 |
| paso 7 | 13, 14, 15 |
| paso 8 | 16, 17, 18 |

Todos los bloques «En consola» del guion muestran `id = 1`. **A partir del segundo arranque eso
deja de ser verdad**, y en clase el segundo arranque llega en el paso 4.

No se reescribieron los bloques —seguirían siendo falsos en la corrida siguiente— sino que se
avisa una vez, arriba del documento, y se explica por qué: *lo que importa no es el número, sino
que el `id` pase de `null` a algo*. Y se añadió una frase sobre `deleteAll()` donde aparece.

> **Un aviso metodológico sobre este hallazgo:** durante las primeras pruebas los `id` saltaban de
> 3 a 34. No era del lab: era **mi arnés** matando PostgreSQL con `kill -9`, lo que fuerza
> recuperación por WAL y adelanta la secuencia hasta 32 posiciones. Al apagar con `SIGINT` —que es
> lo que hace el Ctrl+C del alumno— los ids avanzan de tres en tres, como en la tabla. Se corrigió
> el arnés y se volvió a medir todo desde cero.

---

## 6 · Cuánto creció el documento

La pregunta que la SPEC pide contestar para poder decidir si esto se extiende al resto.

```
  antes:   393 líneas   13 864 bytes
  después: 719 líneas   24 127 bytes
```

**+83 % en líneas.** Pero ese número, solo, engaña. Partido en lo que se lee y lo que se copia:

```
  PROSA (lo que de verdad se lee):  antes 299  ->  después 394   (+95)
  CÓDIGO (lo que se copia):         antes  95  ->  después 326   (+231)
```

**Tres cuartas partes de lo que creció es código**, que no se lee: se copia. La prosa creció +95
líneas, y de esas, unas 25 son el aviso de cómo leer el guion y los dos avisos de consola del
encabezado; el resto son las frases de «dónde va este bloque», que es precisamente el trabajo que
esta SPEC le quita al instructor en vivo.

Por paso, que es como se usa:

| paso | antes | después | de las cuales, código |
|---|---|---|---|
| 1 · La entidad | 27 | 75 | 49 |
| 2 · El repositorio | 21 | 27 | 8 |
| 3 · Guardar | 45 | 94 | 46 |
| 4 · Buscar por id, y listar | 31 | 67 | 34 |
| 5 · Buscar por autor | 31 | 52 | 19 |
| 6 · Dos condiciones | 30 | 54 | 20 |
| 7 · Actualizar sin `save()` | 43 | 73 | 34 |
| 8 · Borrar y contar | 44 | 71 | 36 |
| 9 · La prueba de que quedó guardado | 38 | 56 | 18 |
| 10 · Lo mismo, por HTTP | 43 | 96 | 59 |

**Veredicto: no se vuelve inmanejable.** El paso más largo son 96 líneas y 59 de ellas son el
controller entero, que se copia de una vez. Nadie lee el documento de corrido: lee un paso, pega,
mira la consola, pasa al siguiente. Los dos pasos más gordos (1 y 10) lo son porque pegan un
archivo completo, que es justo el caso más cómodo de todos.

Lo que **sí** conviene tener presente si esto se extiende: en un lab con más archivos —el 14, sin
ir más lejos, son cuatro servicios— el documento crecería bastante más que un 83 %, y ahí la
decisión de qué llega hecho en `practica/` pesa más que el formato del guion.

---

## 7 · Lo que este informe NO puede afirmar

- **Que en clase funcione.** V1 demuestra que los bloques están completos y correctos: pegándolos
  al pie de la letra se llega a `solucion/`. Que ahorren el trabajo mental que el PO describe
  —*«en clases no tengo tiempo de pensar en nada que no sea lo que estoy pasando»*— solo lo dice
  una clase de verdad. **Es un piloto, y esa es la prueba que falta.**
- **Que el guion se mantenga sincronizado solo.** Hoy los bloques cuadran porque se acaban de
  extraer. Nada impide que mañana alguien toque `solucion/` y el guion quede mintiendo. La
  garantía es el job de CI descrito en §3, y **no está hecho**: promoverlo depende de si el piloto
  se aprueba.
- **Que esto sea lo correcto para los otros catorce labs.** El Lab 04 es de los más regulares:
  cuatro archivos, métodos independientes, un solo proyecto. Los labs 07, 12 y 14 tienen mucho más
  código y menos simetría, y el formato podría necesitar ajustes.
- **Que funcione en Windows.** Todo se midió en macOS. El formato es texto plano y no debería
  variar, pero los `curl` del paso 10 y el Ctrl+C del paso 9 se comportan distinto en `cmd.exe`.
