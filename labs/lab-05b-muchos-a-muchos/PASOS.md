# Pasos · Lab 05b · Muchos a muchos

Seis pasos. Se construyen en `practica/`, en vivo, uno a la vez. Después de cada paso se reinicia
el programa y se mira la consola antes de seguir.

```bash
cd practica
./mvnw spring-boot:run
```

**El programa se queda corriendo**: se apaga con **Ctrl+C**. Escucha en el **8110** y su base en
el **55447** (`solucion/`, en el 8111 y el 55448).

En `practica/` el `CommandLineRunner` de `Lab05bApplication` llega **vacío**. Cada paso llena un
método de la clase de demos y agrega su llamada.

> **Este lab tiene tres tablas y dos clases.** La tercera tabla no tiene clase, no tiene
> repositorio y nadie la escribió: la declara una anotación. Lo que hay que mirar hoy es **qué le
> pasa a esa tabla** con cada cosa que se hace en Java.

---

## Paso 1 · La relación, y la tabla que aparece

**Se explica:** en el Lab 05 la relación era **una columna** — `contribuyente_id`, dentro de
`tramite`. Hoy no cabe: un trámite pide varios documentos **y** el mismo documento sirve a varios
trámites. Una columna no puede guardar eso por ninguno de los dos lados.

Abrir `db/migration/V1__tramite_documento_y_la_intermedia.sql` y mirar la **tercera** tabla:

```sql
create table tramite_documento (
    tramite_id    bigint not null references tramite (id),
    documento_id  bigint not null references documento (id),
    primary key (tramite_id, documento_id)
);
```

Dos columnas y nada más, y **la clave primaria son las dos juntas**. Eso es una relación
muchos-a-muchos en la base: **una tabla de pares**. En Java se declara con `@ManyToMany` y
`@JoinTable`, y las dos cosas tienen que decir lo mismo.

**Se pega (1 de 5):** en `entities/Tramite.java`, **arriba**, con los imports.

```java
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import java.util.LinkedHashSet;
import java.util.Set;
```

**Se pega (2 de 5):** en el mismo archivo, **donde dice `// escribe aquí`** por primera vez.

```java
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "tramite_documento",
            joinColumns = @JoinColumn(name = "tramite_id"),
            inverseJoinColumns = @JoinColumn(name = "documento_id"))
    private Set<Documento> documentos = new LinkedHashSet<>();
```

**Se pega (3 de 5):** en el mismo archivo, **donde dice `// escribe aquí`** por segunda vez.

```java
    public Set<Documento> getDocumentos() {
        return documentos;
    }

    public void adjuntar(Documento documento) {
        documentos.add(documento);
    }

    public void quitar(Documento documento) {
        documentos.remove(documento);
    }
```

**Se pega (4 de 5):** en `practica/src/main/java/cl/dgt/muchosamuchos/demos/DemosManyToMany.java`,
estos imports **arriba**.

```java
import cl.dgt.muchosamuchos.entities.Documento;
import cl.dgt.muchosamuchos.entities.Tramite;
import java.time.LocalDate;
```

**Se pega (5 de 5):** en el mismo archivo, **reemplazando el método `laRelacionYSuTabla()`
entero**. Empieza contando lo que quedó de la vez anterior —la base persiste, como en el Lab
04— y borrando: los trámites primero, que son los que sujetan las filas de la intermedia.

```java
    public void laRelacionYSuTabla() {
        seccion(1, "LA RELACIÓN · @ManyToMany y @JoinTable");

        System.out.println("  al arrancar había " + tramites.count() + " trámites y "
                + documentos.count() + " documentos de la vez anterior");

        tramites.deleteAll();
        documentos.deleteAll();

        Documento cedula = documentos.save(
                new Documento("CEDULA", "Cédula de identidad del representante"));
        Documento escritura = documentos.save(
                new Documento("ESCRITURA", "Escritura de constitución"));
        Documento poder = documentos.save(
                new Documento("PODER", "Poder simple ante notario"));
        Documento balance = documentos.save(
                new Documento("BALANCE", "Balance del último ejercicio"));
        Documento vigencia = documentos.save(
                new Documento("VIGENCIA", "Certificado de vigencia de la sociedad"));
        System.out.println("  5 documentos guardados");

        Tramite inicio = new Tramite("Inicio de actividades", "76.543.210-K", LocalDate.of(2026, 3, 10));
        inicio.adjuntar(cedula);
        inicio.adjuntar(escritura);
        inicio.adjuntar(balance);
        inicio.adjuntar(vigencia);
        this.tramiteInicioId = tramites.save(inicio).getId();

        Tramite representante = new Tramite("Cambio de representante legal", "77.111.222-3", LocalDate.of(2026, 4, 5));
        representante.adjuntar(cedula);
        representante.adjuntar(escritura);
        representante.adjuntar(poder);
        tramites.save(representante);

        Tramite termino = new Tramite("Término de giro", "78.999.888-1", LocalDate.of(2026, 6, 30));
        termino.adjuntar(cedula);
        termino.adjuntar(balance);
        tramites.save(termino);

        System.out.println("  3 trámites guardados · el " + tramiteInicioId + " lleva 4 documentos");
        System.out.println("  y CEDULA la piden los tres");

        mirador.imprimirTodo("recién sembrada");
    }
```

**Se agrega al runner:** en `Lab05bApplication.java`, dentro de `return args -> {`:

```java
            demos.laRelacionYSuTabla();
```

**En consola:**

```
=== 1 · LA RELACIÓN · @ManyToMany y @JoinTable ===
  al arrancar había 0 trámites y 0 documentos de la vez anterior
Hibernate:
    insert
    into
        documento
        (codigo, nombre)
    values
        (?, ?)
  5 documentos guardados
Hibernate:
    insert
    into
        tramite
        (fecha, rut, tipo)
    values
        (?, ?, ?)
Hibernate:
    insert
    into
        tramite_documento
        (tramite_id, documento_id)
    values
        (?, ?)
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

**Lo que hay que señalar:** hay **un INSERT en `tramite`** y, aparte, **cuatro INSERT en
`tramite_documento`**. En el Lab 05 el INSERT del trámite ya traía la relación dentro, en su
columna. Aquí la relación es **una fila propia en otra tabla**, y por eso se inserta aparte.

Y las nueve filas: `documento_id=1` (CEDULA) aparece con los tres trámites. **Eso es lo que una
columna no podía guardar.**

**La pregunta del paso:** ¿dónde está declarado el nombre `tramite_documento`? En una anotación,
en una sola clase. Nadie escribió un repositorio, ni una entidad, ni un `insert`.

---

## Paso 2 · Agregar y quitar, mirando la intermedia

**Se explica:** la colección de Java y la tabla de la base son la misma cosa vista dos veces.
Añadir un objeto a un `Set` en memoria es, al hacer `flush`, un `insert` en `tramite_documento`.
Quitarlo es un `delete`. **No hay que escribir ninguno de los dos.**

**Se pega:** en `practica/src/main/java/cl/dgt/muchosamuchos/demos/DemosManyToMany.java`,
**reemplazando el método `agregarYQuitar()` entero**.

```java
    @Transactional
    public void agregarYQuitar() {
        seccion(2, "AGREGAR Y QUITAR · los INSERT y DELETE de la intermedia");

        Tramite tramite = tramites.findById(tramiteInicioId).orElseThrow();
        Documento poder = documentos.findByCodigo("PODER").orElseThrow();
        System.out.println("  trámite " + tramite.getId() + " · " + tramite.getTipo()
                + " · lleva " + tramite.getDocumentos().size() + " documentos");

        System.out.println("  --- se ADJUNTA el poder ---");
        tramite.adjuntar(poder);
        tramites.flush();
        mirador.imprimirDelTramite(tramiteInicioId, "tras adjuntar");

        System.out.println("  --- se QUITA el poder ---");
        tramite.quitar(poder);
        tramites.flush();
        mirador.imprimirDelTramite(tramiteInicioId, "tras quitar");
    }
```

**Se agrega al runner:** en `Lab05bApplication.java`, dentro de `return args -> {`:

```java
            demos.agregarYQuitar();
```

**En consola:**

```
=== 2 · AGREGAR Y QUITAR · los INSERT y DELETE de la intermedia ===
Hibernate:
    select ... from tramite t1_0 where t1_0.id=?
Hibernate:
    select ... from documento d1_0 where d1_0.codigo=?
Hibernate:
    select
        d1_0.tramite_id, d1_1.id, d1_1.codigo, d1_1.nombre
    from
        tramite_documento d1_0
    join
        documento d1_1
            on d1_1.id=d1_0.documento_id
    where
        d1_0.tramite_id=?
  trámite 1 · Inicio de actividades · lleva 4 documentos
  --- se ADJUNTA el poder ---
Hibernate:
    insert
    into
        tramite_documento
        (tramite_id, documento_id)
    values
        (?, ?)
  tramite_documento · tras adjuntar -> 5 filas
      tramite_id=1  documento_id=1
      tramite_id=1  documento_id=2
      tramite_id=1  documento_id=3
      tramite_id=1  documento_id=4
      tramite_id=1  documento_id=5
  --- se QUITA el poder ---
Hibernate:
    delete
    from
        tramite_documento
    where
        tramite_id=?
        and documento_id=?
  tramite_documento · tras quitar -> 4 filas
      tramite_id=1  documento_id=1
      tramite_id=1  documento_id=2
      tramite_id=1  documento_id=4
      tramite_id=1  documento_id=5
```

**Tres cosas que señalar, y las tres importan:**

1. **El tercer SELECT es la colección**, y llega cuando se toca —`getDocumentos().size()`—, no
   antes. Es el mismo LAZY del Lab 05: `@ManyToMany` **también** es perezoso.
2. **Adjuntar es un `insert` de una fila.** Quitar es un `delete` **con las dos claves en el
   `where`**. Precisos, mínimos.
3. **No se borró el documento.** `quitar()` deshace el par, no la ficha: PODER sigue en la tabla
   `documento` y sigue adjunto al trámite 2. Es la diferencia entre romper una relación y borrar
   una cosa, y en material tributario es una diferencia cara.

**La pregunta del paso:** ese `delete` de ahí arriba lleva las dos claves. **Recuérdenlo, porque
en el paso 4 va a dejar de llevarlas.**

---

## Paso 3 · El lado espejo

**Se explica:** hasta ahora la relación se navega en un sentido: del trámite a sus documentos. El
otro sentido —«¿en qué trámites aparece esta cédula?»— se declara con `mappedBy`, **exactamente
igual que el `@OneToMany` del Lab 05**: nombra el **campo** del otro lado, y **no guarda nada**.
La tabla intermedia la sigue mandando `Tramite`, que es quien tiene el `@JoinTable`.

**Se pega (1 de 3):** en `entities/Documento.java`, **arriba**, con los imports.

```java
import jakarta.persistence.ManyToMany;
import java.util.LinkedHashSet;
import java.util.Set;
```

**Se pega (2 de 3):** en el mismo archivo, **donde dice `// escribe aquí`**.

```java
    @ManyToMany(mappedBy = "documentos")
    private Set<Tramite> tramites = new LinkedHashSet<>();
```

**Se pega (3 de 3):** en el mismo archivo, **antes de `equals`**, su getter.

```java
    public Set<Tramite> getTramites() {
        return tramites;
    }
```

Y en `practica/src/main/java/cl/dgt/muchosamuchos/demos/DemosManyToMany.java`, **reemplazando el
método `elLadoEspejo()` entero** — con su import `java.util.Set` arriba:

```java
import java.util.Set;
```

```java
    @Transactional(readOnly = true)
    public void elLadoEspejo() {
        seccion(3, "LADO ESPEJO · @ManyToMany(mappedBy)");

        Documento cedula = documentos.findByCodigo("CEDULA").orElseThrow();
        System.out.println("  documento: " + cedula.getNombre());
        System.out.println("  --- todavía NO se ha tocado la colección ---");

        Set<Tramite> suyos = cedula.getTramites();
        System.out.println("  aparece en " + suyos.size() + " trámites:");
        suyos.forEach(t -> System.out.println("    " + t));
    }
```

**Se agrega al runner:** en `Lab05bApplication.java`, dentro de `return args -> {`:

```java
            demos.elLadoEspejo();
```

**En consola:**

```
=== 3 · LADO ESPEJO · @ManyToMany(mappedBy) ===
Hibernate:
    select ... from documento d1_0 where d1_0.codigo=?
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

Mismo patrón del Lab 05: el segundo SELECT llega cuando se toca la colección. Y es **la misma
tabla intermedia leída al revés**: antes `where tramite_id=?`, ahora `where documento_id=?`.

**El orden en que salen los tres no está prometido.** Un `Set` no tiene orden, y ese SELECT no
lleva `order by`. Si el orden importa, se pide.

**La pregunta del paso:** si se quita un trámite de `cedula.getTramites()` en Java, ¿se borra
alguna fila? (No. Este lado no manda: **es el mismo `mappedBy` del Lab 05.** Lo único que cambia
es que aquí lo que no se toca es una tabla, no una columna.)

---

## Paso 4 · Set contra List — el paso del laboratorio

**Se explica:** todo el mundo escribe `List`. Aquí está escrito `Set`, y este paso es la razón.

La demo hace el cambio más pequeño posible: **adjuntar un documento a un trámite que ya lleva
cuatro**, y después quitarlo. Un contador imprime cuántas sentencias fueron a la base **por ese
cambio y solo por ese cambio** — la carga queda fuera del conteo, a propósito.

**Se pega:** en `practica/src/main/java/cl/dgt/muchosamuchos/demos/DemosManyToMany.java`,
**reemplazando el método `setContraList()` entero**.

```java
    @Transactional
    public void setContraList() {
        seccion(4, "SET CONTRA LIST · el mismo cambio, medido");

        Tramite tramite = tramites.findById(tramiteInicioId).orElseThrow();
        Documento poder = documentos.findByCodigo("PODER").orElseThrow();
        System.out.println("  trámite " + tramite.getId() + " con "
                + tramite.getDocumentos().size() + " documentos ya cargados en memoria");

        System.out.println("  >>>>>> EMPIEZA EL CONTEO · ADJUNTAR UN DOCUMENTO");
        contador.reiniciar();
        tramite.adjuntar(poder);
        tramites.flush();
        informe("ADJUNTAR UN DOCUMENTO");

        System.out.println("  >>>>>> EMPIEZA EL CONTEO · QUITAR ESE MISMO DOCUMENTO");
        contador.reiniciar();
        tramite.quitar(poder);
        tramites.flush();
        informe("QUITAR ESE MISMO DOCUMENTO");
    }
```

**Se agrega al runner:** en `Lab05bApplication.java`, dentro de `return args -> {`:

```java
            demos.setContraList();
```

**En consola, con `Set` (como está ahora):**

```
=== 4 · SET CONTRA LIST · el mismo cambio, medido ===
  trámite 1 con 4 documentos ya cargados en memoria
  >>>>>> EMPIEZA EL CONTEO · ADJUNTAR UN DOCUMENTO
Hibernate:
    insert
    into
        tramite_documento
        (tramite_id, documento_id)
    values
        (?, ?)
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

**Una y una.**

Ahora, **sin tocar la demo**, cambiar tres líneas en `entities/Tramite.java` — los dos imports y
el campo:

```java
import java.util.ArrayList;
import java.util.List;

    private List<Documento> documentos = new ArrayList<>();

    public List<Documento> getDocumentos() {
        return documentos;
    }
```

Reiniciar y volver a mirar el mismo tramo:

```
  >>>>>> EMPIEZA EL CONTEO · ADJUNTAR UN DOCUMENTO
Hibernate:  delete from tramite_documento where tramite_id=?
Hibernate:  insert into tramite_documento (tramite_id, documento_id) values (?, ?)
Hibernate:  insert into tramite_documento (tramite_id, documento_id) values (?, ?)
Hibernate:  insert into tramite_documento (tramite_id, documento_id) values (?, ?)
Hibernate:  insert into tramite_documento (tramite_id, documento_id) values (?, ?)
Hibernate:  insert into tramite_documento (tramite_id, documento_id) values (?, ?)
  <<<<<< FIN DEL CONTEO · ADJUNTAR UN DOCUMENTO
      SENTENCIAS CONTRA LA BASE: 6

  >>>>>> EMPIEZA EL CONTEO · QUITAR ESE MISMO DOCUMENTO
Hibernate:  delete from tramite_documento where tramite_id=?
Hibernate:  insert into tramite_documento (tramite_id, documento_id) values (?, ?)
Hibernate:  insert into tramite_documento (tramite_id, documento_id) values (?, ?)
Hibernate:  insert into tramite_documento (tramite_id, documento_id) values (?, ?)
Hibernate:  insert into tramite_documento (tramite_id, documento_id) values (?, ?)
  <<<<<< FIN DEL CONTEO · QUITAR ESE MISMO DOCUMENTO
      SENTENCIAS CONTRA LA BASE: 5
```

**Seis y cinco.** El número medido, para el mismo cambio:

| | adjuntar 1 documento | quitar 1 documento |
|---|---|---|
| `Set<Documento>` | **1** sentencia | **1** sentencia |
| `List<Documento>` | **6** sentencias | **5** sentencias |

**Y hay que mirar el primer `delete` de la versión con `List`, porque es lo que de verdad pasa:**

```sql
delete from tramite_documento where tramite_id=?
```

**Sin `documento_id`.** No borra la fila que cambió: **borra la relación entera de ese trámite** y
después la vuelve a insertar completa, una fila por documento. Con `Set` el `delete` llevaba las
dos claves; aquí lleva una.

**Por qué.** Una `List` es una **bolsa** (`bag` en la jerga de Hibernate): admite repetidos y no
tiene identidad por elemento, así que Hibernate no puede saber *qué* fila cambió. Lo único que
sabe hacer con seguridad es tirar todo y rehacerlo. Un `Set` sí tiene identidad —para eso
`Documento` lleva `equals` y `hashCode` por su `codigo`—, y entonces cada cambio es la sentencia
que le corresponde.

**Volver a `Set` antes de seguir.** Y la regla, entera:

> **En un `@ManyToMany`, `Set`. Siempre.** No es preferencia de estilo: es la diferencia entre una
> sentencia y seis, y crece con el tamaño de la colección. Un trámite con 30 documentos son 31
> sentencias por cada documento que se adjunte.

**La pregunta del paso:** ¿y si dos usuarios adjuntan un documento al mismo trámite a la vez, con
`List`? Los dos borran la relación entera y los dos la rehacen desde lo que cada uno tenía en
memoria. **Eso es el Lab 07.**

---

## Paso 5 · Un método cuyo nombre atraviesa la tabla intermedia

**Se explica:** en el Lab 05 el nombre del método cruzaba **una columna**. Aquí cruza **una tabla
que no tiene clase**, y sale igual: se nombra la colección y dentro de ella el campo.

**Se pega (1 de 4):** en `repositories/TramiteRepository.java`, **arriba**, con los imports.

```java
import java.util.List;
```

**Se pega (2 de 4):** en el mismo archivo, **dentro de la interfaz**.

```java
    List<Tramite> findByDocumentosCodigo(String codigo);
```

**Se pega (3 de 4):** en `practica/src/main/java/cl/dgt/muchosamuchos/demos/DemosManyToMany.java`,
este import **arriba** — es el mismo `List`, y en ese archivo todavía no está.

```java
import java.util.List;
```

**Se pega (4 de 4):** en el mismo archivo, **reemplazando el método `consultaQueNavega()`
entero**.

```java
    @Transactional(readOnly = true)
    public void consultaQueNavega() {
        seccion(5, "CONSULTA QUE NAVEGA · findByDocumentosCodigo()");

        List<Tramite> conPoder = tramites.findByDocumentosCodigo("PODER");
        System.out.println("  trámites que piden PODER -> " + conPoder.size());
        conPoder.forEach(t -> System.out.println("    " + t));

        List<Tramite> conCedula = tramites.findByDocumentosCodigo("CEDULA");
        System.out.println("  trámites que piden CEDULA -> " + conCedula.size());
        conCedula.forEach(t -> System.out.println("    " + t));
    }
```

**Se agrega al runner:** en `Lab05bApplication.java`, dentro de `return args -> {`:

```java
            demos.consultaQueNavega();
```

**En consola:**

```
=== 5 · CONSULTA QUE NAVEGA · findByDocumentosCodigo() ===
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
    Tramite{id=1, ...}
    Tramite{id=2, ...}
    Tramite{id=3, ...}
```

**Un solo SELECT con DOS `join`**, y el de en medio es la tabla intermedia. En el Lab 05 salía
uno; aquí salen dos porque hay una tabla más que atravesar. **Nadie los escribió**: salieron del
nombre `findByDocumentosCodigo` — «por la colección `documentos`, su campo `codigo`».

Este SELECT tampoco lleva `order by`, así que el orden no está prometido. Si hace falta:
`findByDocumentosCodigoOrderById`.

**La pregunta del paso:** ¿y si además de los trámites hicieran falta sus documentos? Cada trámite
iría a buscar los suyos, uno por uno. **Eso es el Lab 06, y aquí sale peor** — está dicho al
final.

---

## Paso 6 · Cuándo `@ManyToMany` deja de servir

**Se explica:** `@ManyToMany` es cómodo mientras la relación **no tenga nada que decir de sí
misma**. En cuanto la tabla intermedia necesita **un dato propio** —la fecha en que se adjuntó el
documento, quién lo subió, si está vigente— se acabó: `@JoinTable` manda una tabla de exactamente
dos columnas y no hay dónde ponerlo.

**Este paso no implementa nada.** Enseña la pregunta y el dibujo de la respuesta.

**Se pega:** en `practica/src/main/java/cl/dgt/muchosamuchos/demos/DemosManyToMany.java`,
**reemplazando el método `cuandoDejaDeServir()` entero**.

```java
    public void cuandoDejaDeServir() {
        seccion(6, "CUÁNDO @ManyToMany DEJA DE SERVIR");

        System.out.println("  la pregunta: ¿en qué fecha se adjuntó CEDULA al trámite "
                + tramiteInicioId + ", y quién la subió?");
        System.out.println("  columnas de tramite_documento -> " + mirador.columnas());
        System.out.println("  la respuesta no está, y no cabe: @ManyToMany manda una tabla de dos claves");
        System.out.println();
        System.out.println("  EN QUÉ SE CONVIERTE (no se implementa hoy):");
        System.out.println("    @Entity @Table(name = \"adjunto\")");
        System.out.println("    class Adjunto {");
        System.out.println("        @Id @GeneratedValue  Long id");
        System.out.println("        @ManyToOne(LAZY)     Tramite tramite");
        System.out.println("        @ManyToOne(LAZY)     Documento documento");
        System.out.println("        LocalDate            fechaAdjunto     <-- el dato propio");
        System.out.println("        String               subidoPor        <-- y este");
        System.out.println("    }");
        System.out.println("    y en Tramite:  @OneToMany(mappedBy = \"tramite\") Set<Adjunto> adjuntos");
        System.out.println();
        System.out.println("  la tabla intermedia deja de ser un @JoinTable y pasa a ser una @Entity.");
        System.out.println("  Y entonces esto ya no es muchos-a-muchos: son dos @ManyToOne, que es el lab 05 dos veces.");
    }
```

**Se agrega al runner:** en `Lab05bApplication.java`, dentro de `return args -> {`:

```java
            demos.cuandoDejaDeServir();
```

**En consola:**

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

**Lo que hay que señalar:** la máquina lo dice sola — `columnas de tramite_documento ->
[tramite_id, documento_id]`. Dos. No hay hueco para una fecha.

**Y la parte que tranquiliza:** convertirla **no es aprender nada nuevo**. Un `@ManyToMany`
partido en dos son **dos `@ManyToOne`**, que es exactamente el Lab 05, dos veces. Lo que se gana
es una tabla con nombre, con `@Id` propio y con sitio para sus columnas. Lo que se pierde es la
comodidad de `tramite.adjuntar(documento)`.

**La pregunta del paso, y es de criterio:** en el SII, ¿cuántas relaciones muchos-a-muchos de
verdad **no** acaban queriendo una fecha? La respuesta honesta es «pocas». Por eso mucha gente con
oficio salta este paso y **empieza** con la entidad intermedia.

---

## Al terminar

`practica/` imprime exactamente lo mismo que `solucion/`. Si algo no cuadra, `solucion/` está ahí
para comparar archivo por archivo.

Lo que hay que poder decir con las propias palabras:

> Un muchos-a-muchos es una tabla de pares que nadie escribe: la declara `@JoinTable`. El lado que
> la lleva es el dueño; el otro usa `mappedBy` y no guarda nada. Y la colección va en `Set`,
> porque con `List` cada cambio borra la relación entera y la rehace.

### Las dos cosas que este lab deja dichas

**1 · El N+1 del Lab 06 aplica aquí igual, y peor.**

Listar 200 trámites y tocar los documentos de cada uno son **201 consultas**, exactamente como en
el Lab 06. Peor por dos motivos: cada una de esas 200 consultas atraviesa **dos tablas** en vez de
una, y la colección del otro lado hace lo mismo al revés — pedir los trámites de cada documento
son otras tantas. Las tres salidas del Lab 06 —`join fetch`, `@EntityGraph` y la proyección—
valen aquí **sin cambiarles una coma**.

**2 · `@ManyToMany` es LAZY por defecto, y aun así se declara.**

`@ManyToOne` es EAGER por defecto y `@ManyToMany` es LAZY: son valores distintos para anotaciones
que se parecen, y nadie los recuerda bien. Por eso en `Tramite` está escrito
`@ManyToMany(fetch = FetchType.LAZY)` **aunque sea redundante**: quien lee la clase sabe qué pasa
sin ir a buscar la especificación. Escribir lo que ya se cumple por defecto cuesta cinco palabras;
suponerlo cuesta un incidente.

### Lo que siembra este lab

En el paso 5 salió un SELECT con dos `join` para una consulta. Uno solo, y rápido.

En el paso 4, sin tocar una línea de la demo, salieron **seis sentencias donde había una**. Y eran
cuatro documentos.

> **La pregunta que abre el Lab 06** — si adjuntar un documento a un trámite de cuatro cuesta
> seis sentencias mal escrito… ¿qué cuesta pintar una pantalla con **doscientos** trámites y sus
> documentos?

El Lab 06 lo pone en un número —**201 consultas contra 1**— y lo arregla de tres formas sin tocar
el mapeo.
