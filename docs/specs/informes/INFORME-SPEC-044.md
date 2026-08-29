# INFORME-SPEC-044 · Las guías en PDF

**Ejecuta:** mocito · **Rama:** `spec-044-guias-pdf` · **Fecha:** 28 de agosto de 2026
**Origen:** SPEC-044 del Arquitecto, sobre la instrucción del PO: más analogías de la vida
cotidiana, el problema antes de la solución.

---

## 0 · Resumen

**Las cuatro guías están hechas y verificadas.** Labs 00, 01, 02 y 03, en `docs/guias/`:

```
guia-lab-00-hola-mundo.pdf   11 páginas    94 KB
guia-lab-01-web.pdf          15 páginas   111 KB
guia-lab-02-di.pdf           15 páginas    96 KB
guia-lab-03-errores.pdf      13 páginas   101 KB
                                          402 KB en total
```

**El código no se teclea: se extrae.** `tools/generar-guias.py` saca cada bloque de `solucion/` y
comprueba que toda línea impresa esté ahí. **32 bloques extraídos, 0 líneas que la solución no
tenga** (§4, V1).

**V2 pasa**: siguiendo solo la guía del 00, sin abrir `PASOS.md` ni `solucion/`, se llega al
resultado — y el Java queda **idéntico** al de la solución (§5).

**V4 se ganó midiendo: once errores reproducidos**, cuatro en el 00, tres en el 01, dos en el 02 y
dos en el 03, cada uno con el texto literal que sale en pantalla (§6).

**Y el formato tuvo un hallazgo que cambió las guías**, encontrado justo donde la SPEC quería que
se buscara —al revisar el 00 antes de replicar—: **copiar de un PDF pierde la sangría y parte las
líneas largas.** Está medido en la §3 y obligó a rediseñar un paso.

---

## 1 · El formato que quedó

La estructura del §2 de la SPEC, tal cual, con una sección añadida y tres recuadros de color.

**Por documento:** portada · índice · *Antes de empezar* (qué vas a lograr, qué necesitas, **cómo
copiar el código**, la puesta a punto) · *El caso* con la metáfora · los pasos · *Lo que
aprendiste* · *Para profundizar* · *Antes de cerrar*.

**Por paso:** Qué vamos a hacer · Para entenderlo mejor · El problema · La alternativa y por qué no
· Se pega · Se corre · Lo que vas a ver · **Vas bien si…** · **Si te atascas**.

**La sección que se añadió y no estaba en la SPEC: «Cómo copiar el código de esta guía».** Es
consecuencia directa del hallazgo de la §3, y va en las cuatro.

**Los tres recuadros de color**, que son lo que hace el documento navegable de un vistazo:

| Recuadro | Color | Para qué |
|---|---|---|
| **Vas bien si…** | verde | el cierre comprobable de cada paso |
| **Si te atascas** | ámbar | las causas por frecuencia, con el error literal |
| *(sin título)* | azul | la metáfora, cuando se presenta |
| *(sin título)* | gris | las notas al margen — lo que varía entre corridas, los avisos |

**La cadena de producción:** Markdown con marcadores → `tools/generar-guias.py` → pandoc → xelatex.
Se eligió LaTeX y no una impresión desde el navegador por una razón concreta de la V5: **el índice
con números de página correctos** sale solo, y desde HTML habría que calcularlo.

---

## 2 · La metáfora de cada lab, y por qué ésa

La SPEC pedía **una metáfora por lab**, sostenida hasta el final y coherente con la del lab
anterior donde el dominio siga siendo el mismo. El dominio es el mismo en los cuatro, así que no
son cuatro metáforas: **es un mundo que crece**, la oficina de la DGT.

| Lab | La metáfora | Por qué ésa |
|---|---|---|
| **00** hola mundo | **La oficina que abre por la mañana.** El `main` gira la llave; **el conserje** —el contenedor— recorre las salas y pone en marcha lo que encuentra anotado. El `@Bean CommandLineRunner` es **una nota pegada en la puerta**; el `application.yml`, **el cartel de la entrada**; el `pom.xml`, **el pedido de material** | El lab enseña que algo se ejecuta sin que nadie lo llame. Una nota que otro lee al pasar es exactamente eso, y no exige saber nada de programación para entenderlo |
| **01** web | **La ventanilla.** La oficina deja de cerrar y pone una ventanilla. La ruta es **el número de la ventanilla**; `@PathVariable`, **lo escrito en el cartel**; `@RequestParam`, **las casillas del formulario del mostrador**; `@RequestBody`, **el formulario entregado por la ranura**; y `ResponseEntity`, **el sello del papel** | Las tres formas de que llegue un dato son tres gestos distintos en un mostrador real, y la diferencia entre ellas —lo que identifica, lo que filtra, lo que es largo o privado— se explica sola |
| **02** di | **El proveedor.** La ventanilla no fabrica: encarga. La interfaz es **el contrato de suministro**; la clase anotada, **el proveedor acreditado**; el contenedor, **la administración que decide**. Con dos proveedores acreditados **la oficina no abre** | Es la metáfora que hace evidente el paso 4, que es el corazón del lab: una administración que elige a dedo entre dos contratos idénticos sería peor que una que se planta |
| **03** errores | **El papel que explica por qué.** Un error no es un portazo: es un papel con qué pasó, con qué código y cuándo. El `@RestControllerAdvice` es **la mesa de informaciones** que lo redacta. Y lo que **no** dice el papel —los problemas internos— queda en **el registro interno** | La regla difícil del lab es qué NO se le cuenta a quien llama. «Lo que se dice y lo que se apunta» lo deja en una frase que se recuerda |

Las cuatro se reutilizan en **cada paso**, en la sección *Para entenderlo mejor*, que es lo que la
SPEC pedía: no una analogía suelta por concepto, sino un mundo al que se vuelve.

---

## 3 · El hallazgo del formato · copiar de un PDF rompe el código

**Es el motivo por el que la SPEC mandaba revisar el 00 antes de escribir los otros tres, y valió
la pena.**

Al comprobar qué recibe de verdad el alumno al copiar un bloque, la capa de texto del PDF devuelve
esto para el YAML del paso 3:

```
spring:
application:
name: mi-primera-app
```

**Toda la sangría, perdida.** Y en Java es peor, porque además parte las líneas: el bloque de tres
`System.out.println` sale así, con la línea de en medio cortada donde tenía dos espacios dentro de
la cadena:

```
'System.out.println();'
'System.out.println("'
'System.out.println();'
```

Comparado con lo que dice la solución:

```
'System.out.println();'
'System.out.println("  Hola, mundo. Esto lo escribí yo.");'
'System.out.println();'
```

**Se intentó arreglar en el PDF, y no se pudo.** Tres caminos probados y descartados, cada uno
comprobado con `pdftotext` sobre el PDF resultante:

| Intento | Resultado |
|---|---|
| Redefinir `\FancyVerbSpace` a un espacio visible | igual: la sangría se sigue perdiendo |
| `accsupp` con `ActualText` en cada espacio | igual |
| `--listings` en vez de `fancyvrb`, con `keepspaces=true` | **peor**: parte aún más líneas |

**Lo que se hizo, que es la decisión conservadora:** no pelearse con el PDF, y **quitar de las
guías toda dependencia de la sangría copiada**.

1. **El paso del YAML dejó de pedir que se pegue un bloque.** Ahora dice: *abre el archivo, busca
   la línea `name:` y cambia su valor*. El bloque se sigue mostrando, pero como referencia. Es más
   seguro **y** es lo que se haría en un proyecto de verdad.
2. **Las cuatro guías llevan una sección «Cómo copiar el código de esta guía»** en *Antes de
   empezar*, que dice sin adornos que esto pasa, que con Java casi nunca importa —el compilador
   ignora la sangría—, que si una línea se parte el editor la marca, y que **el código completo
   está en `solucion/`** como red de seguridad.
3. **El «Si te atascas» del paso 2 del lab 00 cubre el caso**, con el error literal.

**Lo que esto significa para los once labs restantes, si el formato sigue:** todo bloque de YAML,
y cualquier bloque donde la sangría sea el significado, tiene que plantearse como una edición y no
como un pegado. En Java no hace falta.

---

## 4 · V1 · Los bloques salen de `solucion/`, y está comprobado

`tools/generar-guias.py` no admite código escrito en el fuente: el Markdown lleva **marcadores**, y
el generador los sustituye por lo que extrae. Cinco modos:

| Modo | Qué saca |
|---|---|
| `entero` | el archivo completo |
| `metodo` | un método, con sus anotaciones, cerrando llaves |
| `entre` | lo que hay entre dos anclas |
| `clave` | un bloque de primer nivel de un YAML |
| `xml` | el `<dependency>` o `<plugin>` que contiene un texto |

Y después **comprueba**: cada línea impresa tiene que existir, tal cual, en el archivo de
`solucion/` del que dice venir.

```
guia-lab-00-hola-mundo.md  ·  4 bloque(s) extraído(s) de solucion/
guia-lab-01-web.md         · 10 bloque(s)
guia-lab-02-di.md          ·  9 bloque(s)
guia-lab-03-errores.md     ·  9 bloque(s)

[OK] 32 bloque(s) comprobado(s) contra solucion/ · 0 línea(s) que la solución no tiene
```

### 4.1 · La excepción, declarada: un bloque que no puede salir de `solucion/`

El lab 02 escribe en el paso 2 un controlador que el paso 6 reescribe. **Ese estado intermedio no
está en `solucion/`** —la solución guarda el estado final— y sin él la guía dejaría al alumno con
un archivo que no compila.

`PASOS.md` ya tiene ese problema resuelto: lleva los bloques intermedios marcados
(`<!-- pasos:intermedio · … -->`), y el job `pasos` del CI los cuenta y los imprime en vez de
comprobarlos.

**Se hizo lo mismo, en vez de teclear el bloque en la guía:** un modo `pasos` que lo extrae de
`PASOS.md`, el único sitio del repositorio que lo tiene. Y el generador lo **declara** en su
salida, para que saltárselo sea una decisión visible:

```
  [INTERMEDIO] labs/lab-02-di/PASOS.md · 30 líneas · no está en solucion/ por ser un
               estado a medias, y se declara
```

**Es uno de treinta y tres bloques.** Los otros treinta y dos salen de `solucion/`.

---

## 5 · V2 · Seguir la guía del 00 sin abrir nada más

Sobre una copia limpia de `practica/`, haciendo solo lo que dice la guía:

```
PASO 1 · arrancar sin tocar nada
  Started HolaMundoApplication in 0.479 seconds

PASO 2 · pegar las tres líneas donde dice la guía
  Started HolaMundoApplication in 0.482 seconds
    Hola, mundo. Esto lo escribí yo.

PASO 3 · cambiar SOLO el valor de name
  [la-app-de-carolina] ... Starting HolaMundoApplication ...
  [la-app-de-carolina] ... Started HolaMundoApplication in 0.479 seconds
```

Y el resultado, comparado con la solución por su código desnudo:

```
Archivos .java comparados: 1 · con divergencia de codigo: 0 · solo en A: 0 · solo en B: 0
```

**Idéntico.** La única diferencia en el `application.yml` es el nombre que la propia guía pide
cambiar:

```
<     name: la-app-de-carolina
>     name: mi-primera-app
```

**V2 pasa.** Los pasos 1, 2 y 3 tienen resultado observable; el 4 es de lectura y no produce nada.

---

## 6 · V4 · Los once errores, reproducidos

La SPEC pedía **al menos dos por lab**, cada uno con el error literal. Hay once. Ninguno está
escrito de memoria: se rompió a propósito y se copió lo que salió.

### Lab 00 · cuatro

| # | Qué se rompió | Lo que sale |
|---|---|---|
| 1 | Pegar con **comillas tipográficas**, que es lo que hace un PDF | `illegal character: '“'` · `not a statement` · `';' expected` |
| 2 | Pegar **fuera del método**, suelto en la clase | `<identifier> expected` · `illegal start of type` |
| 3 | `./mvnw` desde **la carpeta equivocada** | `zsh: no such file or directory: ./mvnw` |
| 4a | Perder la **sangría del YAML** | **no da error**: el corchete del nombre desaparece y la línea empieza en `[           main]` |
| 4b | Un **tabulador** en el YAML | `found character '\t(TAB)' that cannot start any token. (Do not use \t(TAB) for indentation)` |

**El 1 y el 4a son los que justifican el ejercicio.** El primero es el error propio de una guía en
PDF, y no se me habría ocurrido sin probarlo. El segundo **no falla**: acepta y miente, que es
mucho peor que un error.

### Lab 01 · tres

| # | Qué se rompió | Lo que sale |
|---|---|---|
| 1 | El **puerto ocupado** | `Web server failed to start. Port 8082 was already in use.` |
| 2 | Falta el `import` de `@PathVariable` | `cannot find symbol` · `symbol: class PathVariable` |
| 3 | Falta `@RestController` | **arranca sin quejarse** y devuelve `{"timestamp":"…","status":404,"error":"Not Found","path":"/hola"}` |

### Lab 02 · dos

| # | Qué se rompió | Lo que sale |
|---|---|---|
| 1 | **Dos candidatos** sin `@Primary` | `Parameter 0 of constructor in cl.dgt.di.services.ProductoService required a single bean, but 2 were found:` y los nombra a los dos |
| 2 | **Ningún** `@Repository` | `required a bean of type 'cl.dgt.di.repositories.ProductoRepository' that could not be found.` |

### Lab 03 · dos

| # | Qué se rompió | Lo que sale |
|---|---|---|
| 1 | Falta `@Valid` | **no falla: acepta.** `HTTP/1.1 201` con `{"id":4,"nombre":"","precio":-5}` |
| 2 | Falta `@RestControllerAdvice` | el 404 vuelve a ser `{"timestamp":"…","status":500,"error":"Internal Server Error","path":"/productos/99"}` |

---

## 7 · V3 · Las salidas citadas son de una corrida

Todas las salidas de las cuatro guías salen de correr `solucion/` y pedirle por HTTP, el 28 de
agosto de 2026. Ejemplos, tal cual se capturaron:

```
lab 01   GET /saludos/Carolina   {"mensaje":"Hola, Carolina.","para":"Carolina","formal":false}
         POST /saludos           HTTP/1.1 201
         GET /saludo (sin el parámetro obligatorio)
                                 {"timestamp":"…","status":400,"error":"Bad Request","path":"/saludo"}

lab 02   GET /productos/quien    ProductoRepositoryLista

lab 03   GET /productos/99       {"mensaje":"No existe el producto con id 99.","codigo":404,…}
         POST con el cuerpo mal  {"…","campos":{"precio":"el precio debe ser mayor que cero",
                                                "nombre":"el nombre es obligatorio"}}
         GET /productos/1/cuota?cuotas=0
             al cliente          {"mensaje":"Ocurrió un error inesperado. Inténtalo más tarde.",…}
             en el servidor      java.lang.ArithmeticException: / by zero
```

**Lo que varía entre corridas se dice en el propio documento**, en un recuadro gris: los tiempos,
el PID, el `timestamp` de los errores, y el orden de las claves dentro de `campos`.

### 7.1 · Una desviación en la captura, y hay que decirla

**El puerto 8082 del lab 01 está ocupado en esta máquina por Docker** (`com.docker.backend`), así
que la captura de sus salidas se hizo arrancando en el 8482 con
`-Dspring-boot.run.arguments=--server.port=8482`.

**Los cuerpos de las respuestas son los reales**; lo único que no corresponde a la corrida es el
número de puerto que aparece en los `curl` de la guía, que es el del curso.

Y conviene que el PO lo sepa por otro motivo: **si el 8082 está ocupado en la máquina desde la que
se dicta**, el lab 01 no arrancará en clase hasta cerrar Docker o cambiar el puerto.

---

## 8 · V5 y V6

**V5 · los cuatro PDF abren, y el índice apunta bien.** Comprobado entrada por entrada, buscando
el título en la página que el índice dice:

```
guia-lab-00-hola-mundo.pdf     11 págs    94 KB   15 entradas, todas apuntan a su página
guia-lab-01-web.pdf            15 págs   111 KB   18 entradas, todas apuntan a su página
guia-lab-02-di.pdf             15 págs    96 KB   17 entradas, todas apuntan a su página
guia-lab-03-errores.pdf        13 págs   101 KB   16 entradas, todas apuntan a su página
```

**66 entradas de índice, todas correctas.** Y se miraron páginas en pantalla, no solo el texto
extraído: los recuadros salen con su color, el banner de Spring se ve entero, los acentos y el `·`
se imprimen bien, y el código sale coloreado con su marca de línea partida.

**Los PDF no pesan:** **402 KB los cuatro**. No hay nada que decidir sobre tamaño.

**V6 · el material no se tocó.**

```
$ git status --porcelain labs/   (aparte del trabajo del PO en los labs 04 y 05)
  ninguno
```

---

## 9 · Lo que NO se hizo

- **No se tocó ni un archivo de `labs/`.** Ni `practica/`, ni `solucion/`, ni `instructor/`, ni
  `PASOS.md`, ni `README.md`. La SPEC-044 solo produce PDF y su fuente.
- **V2 se hizo solo en el lab 00**, que es lo que la SPEC pedía. En el 01, 02 y 03 se verificaron
  los bloques (V1) y las salidas (V3), pero **no se siguió la guía entera de punta a punta**. Es la
  deuda de esta SPEC y está en la §10.
- **Los PDF no se probaron en Windows.** Se generaron y se leyeron en macOS. El PDF es el mismo
  archivo en las dos plataformas; lo que puede cambiar es el visor y, con él, qué recupera al
  copiar — que es justo lo delicado (§3).
- **No se generan en el CI.** Hacen falta pandoc y una distribución de LaTeX, que no están en el
  runner y que no viajan en el repositorio. Los PDF se commitean ya hechos.
- **No hay guía del instructor.** La SPEC lo prohíbe expresamente: estas son del alumno, y las
  notas de conducción viven en `PASOS.md` y en `instructor/`.
- **El trabajo del PO en `practica/`** —los ocho archivos de los labs 04 y 05— sigue sin tocarse.

---

## 10 · Anotado para después

1. **La V2 de los labs 01, 02 y 03.** Es la prueba de fondo y solo está hecha en el 00. En los
   otros tres el código está verificado, pero nadie ha seguido la guía entera como la seguirá un
   alumno.
2. **Un visor de PDF real, y en Windows.** La §3 se midió con `pdftotext`. Un lector como Preview o
   Acrobat puede recuperar mejor los espacios — o peor. Media hora, y decide si la advertencia de
   la guía se puede suavizar o hay que reforzarla.
3. **El puerto 8082 ocupado por Docker en la máquina del PO** (§7.1). No es del material, pero
   rompe el lab 01 en clase.
4. **Si el formato sigue a los once labs restantes**, dos cosas aprendidas aquí ahorran trabajo:
   todo bloque sensible a la sangría se plantea como edición y no como pegado (§3), y los estados
   intermedios se sacan de `PASOS.md` con `modo=pasos` (§4.1).
5. **`estadoDelCircuito()` del lab 10 sigue sin usarse.** Venía anotado del INFORME-SPEC-043 §6 y
   sigue igual: esta SPEC no tocaba código.
