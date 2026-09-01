# INFORME-SPEC-046 · Lab 05b · Muchos a muchos

**Ejecuta:** mocito · **Rama:** `spec-046-lab-05b-muchos-a-muchos` · **Fecha:** 31 de agosto de 2026
**Origen:** SPEC-046 del PO, a partir de una pregunta de un alumno en clase dictando el lab 05.

---

## 0 · Resumen

**El lab 05b existe, entero y en la forma de la casa.** `labs/lab-05b-muchos-a-muchos`, con
`README.md`, `PASOS.md`, `practica/`, `solucion/`, `instructor/` y su guía en PDF de 15 páginas.
Puertos **8110/8111** y **55447/55448**, libres y sin choque con ningún lab. **Cero dependencias
nuevas:** los 41 proyectos Maven del repositorio compilan offline, dos más que antes.

**El número del laboratorio está medido, y sale como decía la SPEC.** Sobre un trámite que ya
lleva cuatro documentos, adjuntar uno más y después quitarlo:

| | adjuntar 1 documento | quitar 1 documento |
|---|---|---|
| `Set<Documento>` | **1** sentencia | **1** sentencia |
| `List<Documento>` | **6** sentencias | **5** sentencias |

Y lo que enseña no es el número: es que con `List` la primera sentencia es
`delete from tramite_documento where tramite_id=?` **sin `documento_id`** — borra la relación
entera del trámite y la reinserta completa. Está citado en §3.

**Tres hallazgos, y ninguno es cosmético:**

1. **Los contadores de colecciones de Hibernate no distinguen.** `getCollectionRecreateCount()`
   parecía hecho a medida para este lab y da **0 con los dos tipos**. Se quitaron del arnés, y se
   dice por qué (§3.3).
2. **La V1 encontró un bloque «Se pega» sin su `import`** — la regla `D-043-3`, incumplida en el
   paso 5. Es exactamente el defecto que la V1 existe para cazar, y no se ve leyendo (§4.1).
3. **`ESTADO.md` traía tres números viejos** de antes de esta SPEC: 146 bloques donde había 173,
   87 métodos donde había 98, y 88 promesas donde había 91. Corregidos a los medidos hoy (§6.2).

**Numeración: el orden alfabético deja el 05b en su sitio.** Comprobado, no supuesto (§5).

---

## 1 · Lo que se construyó

```
labs/lab-05b-muchos-a-muchos/
├── README.md                              qué se lleva el alumno
├── PASOS.md                               el guion, 23 bloques «Se pega»
├── guia-lab-05b-muchos-a-muchos.pdf       15 páginas, 10 bloques extraídos
├── practica/                              sin una línea de documentación
├── solucion/                              10 clases, comentarios breves
└── instructor/                            41 recuadros POR QUÉ · (no viaja)
```

**Dos entidades, ninguna más**, como mandaba la SPEC:

| clase | qué es |
|---|---|
| `entities/Tramite` | el lado **dueño**: lleva el `@JoinTable` |
| `entities/Documento` | el lado **espejo**: `@ManyToMany(mappedBy = "documentos")` |

Y tres tablas, que es la novedad: `tramite`, `documento` y **`tramite_documento`**, que no tiene
clase, no tiene repositorio y nadie escribe.

El resto son andamios: dos repositorios, dos piezas de `soporte/` —el contador de sentencias y un
mirador que lee la tabla intermedia con SQL crudo—, las dos guardas de `infra/` y la clase de
demos. Nombres según la convención de la SPEC-040, sin excepciones.

### 1.1 · Los datos de la demo están elegidos, no puestos al azar

Cinco documentos (`CEDULA`, `ESCRITURA`, `PODER`, `BALANCE`, `VIGENCIA`) y tres trámites:

- **`CEDULA` la piden los tres.** Es lo que hace visible que un documento sirve a varios trámites,
  y lo que usan los pasos 3 y 5.
- **`PODER` lo pide uno solo.** Es el que los pasos 2 y 4 adjuntan y quitan del trámite 1, sin
  tocar el trámite que sí lo tiene de verdad.
- **El trámite 1 lleva cuatro documentos.** Con cuatro, la diferencia del paso 4 sale **1 contra
  6**, que es un número que se recuerda.

---

## 2 · Los seis pasos, con la salida citada

Todo lo que sigue sale de correr `solucion/` sobre estado limpio (`rm -rf .datos-pg`), en esta
máquina, hoy.

### 2.1 · Paso 1 — la relación con `@JoinTable`, y mirar la intermedia

```
=== 1 · LA RELACIÓN · @ManyToMany y @JoinTable ===
  al arrancar había 0 trámites y 0 documentos de la vez anterior
  5 documentos guardados
Hibernate:
    insert into tramite (fecha, rut, tipo) values (?, ?, ?)
Hibernate:
    insert into tramite_documento (tramite_id, documento_id) values (?, ?)
Hibernate:
    insert into tramite_documento (tramite_id, documento_id) values (?, ?)
Hibernate:
    insert into tramite_documento (tramite_id, documento_id) values (?, ?)
Hibernate:
    insert into tramite_documento (tramite_id, documento_id) values (?, ?)
  3 trámites guardados · el 1 lleva 4 documentos
  y CEDULA la piden los tres
  tramite_documento · recién sembrada -> 9 filas
      tramite_id=1  documento_id=1
      tramite_id=1  documento_id=2
      tramite_id=1  documento_id=4
      tramite_id=1  documento_id=5
      tramite_id=2  documento_id=1
      tramite_id=2  documento_id=2
      tramite_id=2  documento_id=3
      tramite_id=3  documento_id=1
      tramite_id=3  documento_id=4
```

**Un `insert` en `tramite` y aparte cuatro en `tramite_documento`.** En el lab 05 la relación
viajaba dentro del insert del trámite, en su columna; aquí es una fila propia en otra tabla.

Y `documento_id=1` —la cédula— aparece con los tres trámites. **Eso es lo que una columna no podía
guardar**, y es el motivo del laboratorio.

La tabla se mira desde el propio programa, con `soporte/MiradorDeLaIntermedia`, que la lee con
`JdbcTemplate` y SQL a pelo. Se descartó darle una `@Entity`: si la intermedia tuviera clase,
dejaría de ser «la tabla que nadie escribió» y el paso 6 se quedaría sin pregunta.

### 2.2 · Paso 2 — agregar y quitar, con los INSERT y DELETE a la vista

```
  trámite 1 · Inicio de actividades · lleva 4 documentos
  --- se ADJUNTA el poder ---
Hibernate:
    insert into tramite_documento (tramite_id, documento_id) values (?, ?)
  tramite_documento · tras adjuntar -> 5 filas
  --- se QUITA el poder ---
Hibernate:
    delete
    from
        tramite_documento
    where
        tramite_id=?
        and documento_id=?
  tramite_documento · tras quitar -> 4 filas
```

Un `insert` de una fila para adjuntar; un `delete` **con las dos claves** para quitar. Y el
documento no se borró: `PODER` sigue en la tabla `documento` y sigue adjunto al trámite 2.

Ese `where` con las dos claves es lo que el paso 4 va a romper, y por eso el guion pide fijarse
en él aquí.

### 2.3 · Paso 3 — el lado espejo con `mappedBy`

```
  documento: Cédula de identidad del representante
  --- todavía NO se ha tocado la colección ---
Hibernate:
    select
        t1_0.documento_id, t1_1.id, t1_1.fecha, t1_1.rut, t1_1.tipo
    from
        tramite_documento t1_0
    join
        tramite t1_1
            on t1_1.id=t1_0.tramite_id
    where
        t1_0.documento_id=?
  aparece en 3 trámites:
    Tramite{id=1, tipo='Inicio de actividades', ...}
    Tramite{id=3, tipo='Término de giro', ...}
    Tramite{id=2, tipo='Cambio de representante legal', ...}
```

La misma tabla intermedia leída al revés: antes `where tramite_id=?`, ahora `where
documento_id=?`. Y el SELECT llega **cuando se toca la colección**, no antes: `@ManyToMany` también
es perezoso.

**El orden que sale es 1, 3, 2 y no está prometido.** Un `Set` no tiene orden y el `select` no
lleva `order by`. Está dicho en el guion y en la guía, en vez de callado: un alumno que construya
algo contando con ese orden se lo va a encontrar roto, y prefiero que se entere aquí.

### 2.4 · Paso 4 — `Set` contra `List`. Está en §3, aparte

### 2.5 · Paso 5 — una consulta que navega la relación

```
Hibernate:
    select
        t1_0.id, t1_0.fecha, t1_0.rut, t1_0.tipo
    from
        tramite t1_0
    left join
        tramite_documento d1_0
            on t1_0.id=d1_0.tramite_id
    left join
        documento d1_1
            on d1_1.id=d1_0.documento_id
    where
        d1_1.codigo=?
  trámites que piden PODER -> 1
    Tramite{id=2, tipo='Cambio de representante legal', ...}
  trámites que piden CEDULA -> 3
```

**Un solo SELECT con dos `join`**, y el de en medio es la tabla intermedia. En el lab 05 salía uno;
aquí salen dos porque hay una tabla más que atravesar. Nadie los escribió: salen del nombre
`findByDocumentosCodigo`.

Se hacen dos llamadas a propósito —`PODER` da 1 y `CEDULA` da 3—: el contraste es lo que enseña
que la relación es de verdad muchos-a-muchos y no dos uno-a-muchos disfrazados.

### 2.6 · Paso 6 — cuándo `@ManyToMany` deja de servir

**No se implementa nada**, como pedía la SPEC. Se hace una pregunta que la tabla no puede
contestar, y la máquina lo dice sola:

```
=== 6 · CUÁNDO @ManyToMany DEJA DE SERVIR ===
  la pregunta: ¿en qué fecha se adjuntó CEDULA al trámite 1, y quién la subió?
  columnas de tramite_documento -> [tramite_id, documento_id]
  la respuesta no está, y no cabe: @ManyToMany manda una tabla de dos claves

  EN QUÉ SE CONVIERTE (no se implementa hoy):
    @Entity @Table(name = "adjunto")
    class Adjunto {
        @Id @GeneratedValue  Long id
        @ManyToOne(LAZY)     Tramite tramite
        @ManyToOne(LAZY)     Documento documento
        LocalDate            fechaAdjunto     <-- el dato propio
        String               subidoPor        <-- y este
    }
    y en Tramite:  @OneToMany(mappedBy = "tramite") Set<Adjunto> adjuntos

  la tabla intermedia deja de ser un @JoinTable y pasa a ser una @Entity.
  Y entonces esto ya no es muchos-a-muchos: son dos @ManyToOne, que es el lab 05 dos veces.
```

**La lista de columnas sale del catálogo de PostgreSQL**, no de un `println` tecleado: se consulta
`information_schema.columns`. Es la diferencia entre afirmar que no cabe una fecha y demostrarlo
(`A-02`).

---

## 3 · El paso 4, medido

### 3.1 · Cómo se midió

La demo carga el trámite y su colección, **reinicia el contador después de la carga**, hace el
cambio, fuerza un `flush()` y lee. Lo que se mide es el cambio y solo el cambio: la carga es
idéntica con los dos tipos —tres SELECT— e incluirla convertiría un 1 contra 6, que se recuerda,
en un 4 contra 9, que no dice nada.

El contador es `soporte/ContadorDeSentencias`, sobre `Statistics.getPrepareStatementCount()` de
Hibernate, encendido con `generate_statistics: true`. Es la misma pieza que el lab 06.

Y la demo imprime las marcas `>>>>>> EMPIEZA EL CONTEO` y `<<<<<< FIN DEL CONTEO` para que se pueda
**contar a mano** las líneas `Hibernate:` que caen entre ellas. **Las dos cuentas dan lo mismo**,
que es la comprobación de que el instrumento no miente.

### 3.2 · Lo medido

Con `Set<Documento>`, tal como queda `solucion/`:

```
  trámite 1 con 4 documentos ya cargados en memoria
  >>>>>> EMPIEZA EL CONTEO · ADJUNTAR UN DOCUMENTO
Hibernate:
    insert into tramite_documento (tramite_id, documento_id) values (?, ?)
  <<<<<< FIN DEL CONTEO · ADJUNTAR UN DOCUMENTO
      SENTENCIAS CONTRA LA BASE: 1
  >>>>>> EMPIEZA EL CONTEO · QUITAR ESE MISMO DOCUMENTO
Hibernate:
    delete
    from
        tramite_documento
    where
        tramite_id=?
        and documento_id=?
  <<<<<< FIN DEL CONTEO · QUITAR ESE MISMO DOCUMENTO
      SENTENCIAS CONTRA LA BASE: 1
```

Cambiando **solo** el campo y su getter a `List<Documento> documentos = new ArrayList<>()`, sin
tocar una línea de la demo:

```
  >>>>>> EMPIEZA EL CONTEO · ADJUNTAR UN DOCUMENTO
Hibernate:
    delete
    from
        tramite_documento
    where
        tramite_id=?
Hibernate:
    insert into tramite_documento (tramite_id, documento_id) values (?, ?)
Hibernate:
    insert into tramite_documento (tramite_id, documento_id) values (?, ?)
Hibernate:
    insert into tramite_documento (tramite_id, documento_id) values (?, ?)
Hibernate:
    insert into tramite_documento (tramite_id, documento_id) values (?, ?)
Hibernate:
    insert into tramite_documento (tramite_id, documento_id) values (?, ?)
  <<<<<< FIN DEL CONTEO · ADJUNTAR UN DOCUMENTO
      SENTENCIAS CONTRA LA BASE: 6

  >>>>>> EMPIEZA EL CONTEO · QUITAR ESE MISMO DOCUMENTO
Hibernate:
    delete
    from
        tramite_documento
    where
        tramite_id=?
Hibernate:  (cuatro insert idénticos)
  <<<<<< FIN DEL CONTEO · QUITAR ESE MISMO DOCUMENTO
      SENTENCIAS CONTRA LA BASE: 5
```

**El comportamiento es el que decía la SPEC, sin matices.** Con `List` en un `@ManyToMany`
bidireccional, Hibernate borra toda la relación del trámite y la reinserta entera en cada cambio.
El `delete` va **sin `documento_id`**: no borra la fila que cambió, borra las cinco.

La cuenta es **1 `delete` + N `insert`**, con N el tamaño final de la colección: 1+5 al adjuntar
(quedan 5) y 1+4 al quitar (quedan 4). De ahí sale la respuesta a la pregunta que hace la sala
sola: **con 30 documentos serían 31 sentencias por cada uno que se adjunte.**

La razón es la identidad: una `List` sin `@OrderColumn` es una **bolsa** para Hibernate —admite
repetidos, no tiene identidad por elemento— y no puede saber *qué* fila cambió. Un `Set` sí, y por
eso `Documento` lleva `equals`/`hashCode` por su `codigo`. Si alguien borrase ese `equals`, este
paso dejaría de funcionar, y está dicho en `instructor/`.

### 3.3 · Lo que se probó y NO sirvió, que es el hallazgo

`Statistics` ofrece dos contadores que parecen escritos para este laboratorio:

```
getCollectionRecreateCount()   «colecciones recreadas»
getCollectionUpdateCount()     «colecciones actualizadas»
```

«Recrear una colección» es, palabra por palabra, lo que hace la bolsa. Se instrumentaron y se
midió, en esta misma demo y con el mismo cambio:

```
con Set   ->  SENTENCIAS: 1   ·   RECREADAS: 0   ·   ACTUALIZADAS: 1
con List  ->  SENTENCIAS: 6   ·   RECREADAS: 0   ·   ACTUALIZADAS: 1
```

**Idénticos en las dos últimas columnas**, mientras la SQL de al lado enseñaba el `delete` masivo y
los cinco `insert`. Hibernate contabiliza el caso de la bolsa como una **actualización** de la
colección, no como una recreación.

**Se quitaron del contador y del código que se entrega**, y no por simplificar: un arnés que
imprimiera «RECREADAS: 0» mientras la base borra y reinserta la relación entera estaría enseñando
lo contrario de lo que pasa. Un instrumento que dice algo falso es peor que no tener instrumento
(`A-02`). Queda escrito en `instructor/ContadorDeSentencias.java` y en el `LEEME.md`, para que
nadie los vuelva a intentar creyendo que es una mejora.

---

## 4 · Verificación

### 4.1 · V1 — pegar los bloques sobre `practica/` limpia

Se copió `practica/` a un directorio aparte y se aplicaron **los 23 bloques «Se pega» del guion, en
orden, siguiendo la letra de cada instrucción**. Compilar y correr.

**Encontró un defecto real, y es de los que no se ven leyendo:**

> **El paso 5 mandaba pegar un método que usa `List<Tramite>` en la clase de demos, y ningún paso
> añadía `import java.util.List;` a ese archivo.**
>
> ```
> DemosManyToMany.java:[138,9] cannot find symbol
>   symbol:   class List
> ```
>
> El `import java.util.List` sí estaba en el bloque del **repositorio**, dos bloques antes, y a
> quien escribe el guion le parece que ya está puesto. Es exactamente `D-043-3`: un bloque «Se
> pega» trae sus `import` o no es copiable.

Corregido: el paso 5 pasó de tres bloques a cuatro, y el nuevo es el `import` que faltaba, con su
frase — «es el mismo `List`, y en ese archivo todavía no está».

Con eso, la V1 pasa entera:

```
[OK] compilación limpia
[OK] practica/ pegada al pie de la letra imprime lo MISMO que solucion/
     214 líneas de salida comparadas, 0 diferencias
```

**Dos veredictos falsos que hubo que descartar antes**, y los dos eran del arnés de prueba, no del
material: la primera pasada pegó dos veces el mismo bloque por un desfase de índices, y la segunda
insertó las llamadas del runner en orden inverso (paso 6 primero). Se dicen porque un informe que
solo cuenta el intento bueno hace creer que la V1 salió a la primera, y no salió.

### 4.2 · El resto

```
python3 tools/verificar-pasos-copiables.py
  [OK] lab-05b-muchos-a-muchos: 23 bloques · 153 líneas · 10 métodos completos · 1 de solo mirar
  [OK] 16 guion(es) verificado(s): todo lo que prometen está en solucion/.

python3 tools/verificar-guion-vs-practica.py
  [OK] lab-05b-muchos-a-muchos: 0 nuevos · 5 dentro-de · 0 archivo-entero · 0 carpetas-vacías
  Promesas comprobadas: ... = 96 en total
  [OK] Todo lo que los guiones prometen sobre `practica/` es verdad.

python3 tools/generar-guias.py --verificar
  [OK] 82 bloque(s) comprobado(s) contra solucion/ · 0 línea(s) que la solución no tiene

python3 tools/verificar-instructor.py
  [OK] labs/lab-05b-muchos-a-muchos/instructor  ·  10/10 .java
  Comprobados: 21 XML · 210 .java · 18/18 carpetas con su documento de entrada

python3 tools/verificar-temario.py
  VEREDICTO: las 5 verificaciones PASAN.

compilación offline de todo el arco (lo que hace el job `labs`)
  [INFO] 41 proyectos Maven · 0 fallos
```

**Cero bloques intermedios en este guion.** El único bloque que no se comprueba contra `solucion/`
es el de `List` del paso 4, y es «de solo mirar»: cuelga de un «En consola», no de un «Se pega»,
porque es un experimento de un minuto que se deshace. Es el mismo patrón que el EAGER del paso 4
del lab 05.

**Nota sobre la maleta:** no hizo falta capturar nada. Las dependencias del 05b son **exactamente**
las del lab 05, y ésa es media lección: pasar de una relación a-muchos a una de muchos-a-muchos no
cuesta ni una librería nueva ni una línea de SQL escrita a mano.

### 4.3 · Un rojo del verificador que era del verificador

`verificar-guion-vs-practica.py` marcó en rojo esta línea del README:

> *«…y las seis demos llegan **vacías**. Los dos instrumentos de `soporte/` —el contador y el
> mirador— vienen hechos»*

leyéndola como «`soporte/` llega vacía» y encontrando dos `.java` dentro. Las dos frases hablan de
cosas distintas y están en la misma línea de una tabla; la heurística no puede distinguirlas. Es
la fragilidad que `A-01` declara.

**Se arregló en el README, no en el verificador.** Bajar el listón de una regla porque una frase
mía no le gusta es la forma barata de poner algo en verde, y la frase se dice igual de bien sin el
`soporte/` entre comillas invertidas. Queda anotado por si vuelve a aparecer en otro lab.

---

## 5 · La numeración, comprobada

La SPEC pedía decirlo si el orden alfabético dejaba el `05b` en mal sitio. **No lo deja.**

```
$ ls labs/
...
lab-05-relaciones
lab-05b-muchos-a-muchos
lab-06-rendimiento
...
```

Está en su sitio, y no por suerte: comparando `lab-05-relaciones` con `lab-05b-muchos-a-muchos`, el
primer carácter que difiere es el guion (`-`, 0x2D) contra la `b` (0x62), y el guion ordena antes.
Y `lab-05b` va antes que `lab-06` porque `5` < `6`. **No hay nada que arreglar y no se tocó nada.**

**No se renumeró nada**, como mandaba la SPEC. Queda escrito como `D-046-1`, con el costo de la
alternativa: renumerar del 06 al 14 tocaría dieciocho carpetas, quince `PASOS.md`, los `artifactId`
de los `pom.xml`, los paquetes Java, los puertos, el `MAPA-LAB-MODULO`, el temario entregado al SII
y todos los informes que citan un lab por su número. A cambio de que los números fueran
correlativos.

---

## 6 · Lo que esta SPEC tocó fuera de su carpeta

### 6.1 · Documentos al día

- **`ESTADO.md`** — el arco pasa a dieciséis labs, con su fila en la tabla.
- **`docs/decisiones.md`** — `D-046-1`. Van 109.
- **`docs/CONTEXTO-MOCITO.md`** — §8 reescrito, la tabla de puertos, los contadores del CI, y
  tres cifras del respaldo que estaban viejas desde la SPEC-043 (§6.2).
- **`docs/temario/MAPA-LAB-MODULO.md`** — el M5 listaba **`@ManyToMany` como complemento
  pendiente**, y ya no lo es: el lab 05b lo cubre. También el tema VII, «Relaciones», que ahora
  cita los dos labs. **Las cuentas de cobertura no cambian** —siguen 20 cubiertos, 7 parciales, 8
  sin cubrir— porque `@ManyToMany` figuraba como complemento de un módulo ya cubierto, no como uno
  de los ocho huecos.
- **`proyecto-final/README.md`** — una línea que contaba los labs.

### 6.2 · Números que estaban viejos, y por cuánto

`ESTADO.md` y `CONTEXTO-MOCITO.md` citaban cifras del CI y del respaldo que ya no eran ciertas
**antes** de esta SPEC. Medidos hoy, y corregidos:

| dónde | decía | es | desde cuándo estaba viejo |
|---|---|---|---|
| job `pasos` | 146 bloques, 87 métodos | **196 bloques, 108 métodos** | el 05b aporta 23 y 10; los otros 27 y 11 son de la SPEC-043 en adelante |
| job `guion-practica` | 88 promesas | **96 promesas** | el 05b aporta 5 |
| job `labs` | 39 proyectos | **41 proyectos** | el 05b aporta 2 |
| respaldo de `instructor/` | 245 archivos, 16 carpetas | **293 archivos, 18 carpetas** | el 05b aporta 14 y 1; el resto es SPEC-043 |
| recuadros `POR QUÉ ·` | 140 | **181** | el 05b aporta 41 |
| KB de las guías | 1.451 los quince | **1.604 los dieciséis** | el 05b aporta 116 KB; los otros 37 son de regenerar los cuatro primeros en la SPEC-045 |

**Un `ESTADO.md` desactualizado es un bug del material**, y éste llevaba dos SPEC arrastrando
números. Queda dicho aquí para que se sepa que no es la primera vez y que conviene medirlos al
cerrar en vez de sumarlos de memoria.

---

## 7 · Lo que el lab deja dicho en su cierre

Las dos cosas que la SPEC pedía, y están en `PASOS.md` («Al terminar»), en el README, en la guía
(«Lo que aprendiste») y en el `LEEME.md` de `instructor/`:

**1 · El N+1 del lab 06 aplica aquí igual, y peor.** Listar 200 trámites y tocar los documentos de
cada uno son 201 consultas. Peor por dos motivos concretos: cada una de esas 200 atraviesa **dos**
tablas en vez de una, y la colección del otro lado hace lo mismo al revés. Las tres salidas del
lab 06 —`join fetch`, `@EntityGraph` y la proyección— valen aquí sin cambiarles una coma.

**2 · `@ManyToMany` es LAZY por defecto, y aun así se declara.** `@ManyToOne` y `@OneToOne` son
EAGER; `@OneToMany` y `@ManyToMany` son LAZY. La regla detrás es la cardinalidad, no el
rendimiento, y nadie se acuerda de cuál es cuál. Por eso `Tramite` lleva escrito
`fetch = FetchType.LAZY` aunque sea redundante: quien lea la clase dentro de un año no va a ir a
buscar la especificación, va a suponer.

Y hay una tercera, que no estaba pedida y sale sola del lab: **el paso 4 siembra el lab 07.** Si
dos usuarios adjuntan un documento al mismo trámite a la vez con `List`, los dos borran la
relación entera y los dos la rehacen desde lo que cada uno tenía en memoria. Uno pierde su cambio
y nadie ve un error.

---

## 8 · Decisiones de ejecución que conviene conocer

**El guion empieza con `Set` y cambia a `List` para medir, no al revés.** La SPEC describe el
comportamiento de `List` primero, y la tentación era declararlo así en el paso 1 y arreglarlo en
el 4. Se hizo al revés por una razón concreta: si el paso 1 pegara `List`, los bloques de los
pasos 1 y 3 dejarían de estar en `solucion/` —que guarda el estado final— y habría que marcarlos
como **intermedios**, o sea sin comprobar contra la solución. Empezando por `Set`, los 23 bloques
del guion se verifican y el bloque de `List` queda como un experimento «de solo mirar».

Es además el patrón del lab 05, que declara LAZY y en el paso 4 cambia a EAGER para contar y
vuelve. El alumno reconoce la forma. Y la prosa lo dice sin rodeos: *«todo el mundo escribe `List`.
Aquí está escrito `Set`, y este paso es la razón.»*

**El trámite guarda el RUT como texto, no un `@ManyToOne` a `Contribuyente`.** La SPEC decía «dos
entidades, ninguna más», y se cumplió. Además, una tercera entidad metería un `@ManyToOne` y un
`@ManyToMany` en la misma clase, y en la pizarra los dos se pisan: el alumno deja de saber cuál de
los dos produjo cuál SELECT.

**`Documento` trae `equals`/`hashCode` ya escritos en `practica/`.** No es un paso del lab: es la
condición para que el paso 4 signifique algo, y pedirla como ejercicio sería regalar el final. El
guion y la guía sí explican por qué está ahí, y `instructor/` trae el recuadro completo con las
tres alternativas malas (no ponerlos, ponerlos por `id`, ponerlos por todos los campos).

---

## 9 · La metáfora, y el mundo que crece

La casa pide una metáfora por lab y que sea el mismo mundo (SPEC-045 §2.1). El 04 es el
archivador, el 05 son las fichas que se apuntan entre sí. El 05b es **el cajón de las
correspondencias**: un tercer cajón lleno de papelitos, y en cada papelito dos números y nada más.

Funciona porque contesta las cinco preguntas del lab sin forzarla:

- adjuntar un documento es **meter un papelito**; quitarlo es **sacarlo**;
- quitar el papelito **no toca la ficha del documento**, que sigue en su cajón;
- `mappedBy` es leer el cajón por la otra columna;
- `List` es «sacar todos los papelitos del trámite, tirarlos y volver a escribirlos»;
- y el paso 6 es que **en el papelito no cabe una fecha**: hay que ascenderlo a ficha.

---

## Lo que NO se hizo

- **No se implementó la entidad `Adjunto` del paso 6.** La SPEC decía «se explica y se muestra en
  qué se convierte, **sin implementarlo**», y así está: se enseña la pregunta, se demuestra con
  `information_schema` que la respuesta no cabe, y se dibuja la clase. Implementarla sería otro
  laboratorio.

- **No se probó en Windows.** Como todo lo que sale de esta máquina. Los dos puertos son nuevos y
  las guardas son las mismas del lab 05, que sí está probado allí, pero **el 05b concretamente no
  se ha corrido en Windows** — y Windows es la plataforma que encuentra los defectos.

- **No se siguió la GUÍA de punta a punta como la seguiría un alumno.** Lo que se hizo fue la V1
  sobre `PASOS.md`, que es otro documento con otros bloques: el guion trae 23 y la guía extrae 10.
  Los diez de la guía sí están comprobados byte a byte contra `solucion/` por el generador, pero
  **nadie ha seguido la guía entera con `practica/` delante**. Sigue siendo la deuda de la
  SPEC-045 §8, ahora sobre dieciséis guías en vez de quince.

- **No se abrió el PDF en un visor real ni en Windows.** Se comprobó lo que se puede comprobar
  desde aquí: 15 páginas, 112 KB, ninguna línea desbordada, dos páginas revisadas a ojo con los
  recuadros y las tablas renderizando bien. Es la misma deuda que arrastran las quince guías
  anteriores (INFORME-SPEC-044 §10).

- **No se midió cuánto tarda de verdad la sesión.** El tope de la SPEC son tres horas y el lab está
  dimensionado para eso —seis pasos, dos entidades, 23 bloques—, pero es una estimación, no un
  cronómetro. Se cierra dictándolo una vez.

- **No se re-midieron los «Si te atascas» de la guía.** Los ocho están escritos con el error
  literal que produce cada equivocación, deducido del código y de los errores que salieron
  construyéndolo, pero **no se provocó cada uno a propósito para copiar su salida**. Es la misma
  deuda declarada en INFORME-SPEC-045 §8 para los labs 04 a 14, y el 05b la hereda.

- **No se tocó el temario ni su `.docx`.** El `MAPA-LAB-MODULO` sí, porque listaba `@ManyToMany`
  como complemento pendiente y ya no lo es. El temario entregado al SII habla de un arco «del 00 al
  14» que sigue siendo verdad, y el 05b es material adicional dentro del módulo M5, ya cubierto.

- **No se arregló `verificar-guion-vs-practica.py`.** Su heurística de «carpeta vacía» no puede
  distinguir dos frases en la misma línea (§4.3). Se reescribió la frase del README. Hacer el
  verificador más listo es una SPEC aparte y no la pedía ésta.

- **No se recontaron las entradas de índice de los PDF.** `ESTADO.md` afirmaba «247 entradas de
  índice, todas apuntan a su página», medido en la SPEC-045 sobre quince guías. Con dieciséis ese
  número ya no vale, y **no se volvió a medir**: la frase se retiró de `ESTADO.md` en vez de
  actualizarse a ojo. Se recupera corriendo la comprobación de la SPEC-045 sobre las dieciséis.
