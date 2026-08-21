# Pasos · Lab 05 · Relaciones

Seis pasos. Se construyen en `practica/`, en vivo, uno a la vez. Después de cada paso se reinicia
el programa y se mira la consola antes de seguir.

```bash
cd practica
./mvnw spring-boot:run
```

**El programa se queda corriendo**: se apaga con **Ctrl+C**. Escucha en el **8087** y su base en
el **55434** (`solucion/`, en el 8088 y el 55435).

En `practica/` el `CommandLineRunner` de `Lab05Application` llega **vacío**. Cada paso llena un método de la clase de demos y agrega su llamada.

> **Lo que hay que mirar hoy no es lo que imprimen los métodos.** Es el SQL que sale entre medio,
> y sobre todo **cuántas veces**.

---

## Paso 1 · La relación

**Se explica:** un trámite pertenece a un contribuyente. Muchos trámites, un contribuyente: de ahí
el nombre `@ManyToOne`. Y una regla que vale para todo JPA: **la relación vive donde está la
columna**. Abrir `db/migration/V1__contribuyente_y_tramite.sql` y mirar la última línea de la
tabla `tramite` — ahí está `contribuyente_id`. La clase tiene que decir lo mismo.

**Se pega (1 de 5):** en `entities/Tramite.java`, **arriba**, con los imports.

```java
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
```

**Se pega (2 de 5):** en el mismo archivo, **donde dice `// escribe aquí`**.

```java
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "contribuyente_id", nullable = false)
    private Contribuyente contribuyente;
```

**Se pega (3 de 5):** en el mismo archivo, **reemplazando el constructor público entero** — el de
tres parámetros. Ahora son cuatro.

```java
    public Tramite(String tipo, String estado, LocalDate fecha, Contribuyente contribuyente) {
        this.tipo = tipo;
        this.estado = estado;
        this.fecha = fecha;
        this.contribuyente = contribuyente;
    }
```

**Se pega (4 de 5):** en el mismo archivo, **antes de la llave que cierra la clase**, su getter.

```java
    public Contribuyente getContribuyente() {
        return contribuyente;
    }
```

**Se pega (5 de 5):** en `practica/src/main/java/cl/dgt/relaciones/demos/DemosRelaciones.java`, primero estos imports **arriba**, y después **reemplazando el método
`guardarConRelacion()` entero**. Empieza contando lo que quedó de la vez anterior —la base
persiste, como en el Lab 04— y borrando: los trámites primero, que la clave foránea no deja borrar
un contribuyente que tenga trámites.

```java
import cl.dgt.relaciones.entities.Contribuyente;
import cl.dgt.relaciones.entities.Tramite;
import java.time.LocalDate;
```

```java
    public void guardarConRelacion() {
        seccion(1, "GUARDAR CON RELACIÓN · @ManyToOne");

        System.out.println("  al arrancar había " + contribuyentes.count() + " contribuyentes y "
                + tramites.count() + " trámites de la vez anterior");

        tramites.deleteAll();
        contribuyentes.deleteAll();

        Contribuyente andes = contribuyentes.save(
                new Contribuyente("76.543.210-K", "Comercial Andes Ltda."));
        Contribuyente rutaSur = contribuyentes.save(
                new Contribuyente("77.111.222-3", "Transportes Ruta Sur SpA"));
        Contribuyente espiga = contribuyentes.save(
                new Contribuyente("78.999.888-1", "Panadería La Espiga EIRL"));
        System.out.println("  3 contribuyentes guardados");

        Tramite primero = tramites.save(new Tramite(
                "Declaración F29", "RECIBIDO", LocalDate.of(2026, 3, 10), andes));
        this.primerTramiteId = primero.getId();

        tramites.save(new Tramite("Certificado de situación", "EMITIDO", LocalDate.of(2026, 7, 2), andes));
        tramites.save(new Tramite("Declaración F29", "RECIBIDO", LocalDate.of(2026, 4, 5), rutaSur));
        tramites.save(new Tramite("Inicio de actividades", "APROBADO", LocalDate.of(2026, 1, 20), rutaSur));
        tramites.save(new Tramite("Declaración F29", "OBSERVADO", LocalDate.of(2026, 5, 18), espiga));
        tramites.save(new Tramite("Cambio de domicilio", "APROBADO", LocalDate.of(2026, 6, 30), espiga));
        System.out.println("  6 trámites guardados, 2 por contribuyente");
        System.out.println("  el trámite " + primerTramiteId + " es de " + andes.getRazonSocial());
    }
```

**Se agrega al runner:** en `Lab05Application.java`, dentro de `return args -> {`:

```java
            demos.guardarConRelacion();
```

**En consola:**

```
=== 1 · GUARDAR CON RELACIÓN · @ManyToOne ===
  al arrancar había 0 contribuyentes y 0 trámites de la vez anterior
Hibernate:
    insert
    into
        contribuyente
        (razon_social, rut)
    values
        (?, ?)
  3 contribuyentes guardados
Hibernate:
    insert
    into
        tramite
        (contribuyente_id, estado, fecha, tipo)
    values
        (?, ?, ?, ?)
  6 trámites guardados, 2 por contribuyente
```

**Lo que hay que señalar:** el INSERT de `tramite` incluye `contribuyente_id`. **No hay un INSERT
en ninguna tercera tabla.** Una relación uno-a-muchos es una columna, y punto.

La primera vez ese contador dice `0 y 0`. **La segunda vez dirá `3 y 6`**: eso es la base
persistiendo, y es la prueba de que lo guardado sobrevivió al Ctrl+C.

> **A partir de la segunda vez que se corra, los ids no empezarán en 1.** La demo borra las filas,
> pero el contador que genera los ids (`bigserial`) no se reinicia: sigue por donde iba. No es un
> error y no hay que arreglarlo — es cómo funciona una secuencia en la base. Por eso, al comparar
> con `solucion/`, los números pueden no coincidir aunque todo lo demás sí.

---

## Paso 2 · Navegar, y ver el segundo SELECT

**Se explica:** ahora se va del trámite a su contribuyente. Y aquí aparece lo importante del día:
el contribuyente **no vino** con el trámite. Vino el trámite y nada más. Cuando se le pide el
contribuyente, JPA va a la base a buscarlo — y eso es un SELECT más.

El método ya viene con `@Transactional(readOnly = true)`. Es a propósito: sin eso, la sesión
estaría cerrada al llegar aquí. Eso es el paso 5.

**Se pega:** en `practica/src/main/java/cl/dgt/relaciones/demos/DemosRelaciones.java`, **reemplazando el método `navegarDeTramiteAContribuyente()` entero**. Carga el
trámite, lo imprime, imprime una marca, y **solo entonces** pide el contribuyente.

```java
    @Transactional(readOnly = true)
    public void navegarDeTramiteAContribuyente() {
        seccion(2, "NAVEGAR · tramite -> contribuyente");

        Tramite tramite = tramites.findById(primerTramiteId).orElseThrow();
        System.out.println("  trámite cargado: " + tramite);
        System.out.println("  --- todavía NO se ha tocado el contribuyente ---");

        String razon = tramite.getContribuyente().getRazonSocial();
        System.out.println("  ahora sí: " + razon);
    }
```

**Se agrega al runner:** en `Lab05Application.java`, dentro de `return args -> {`:

```java
            demos.navegarDeTramiteAContribuyente();
```

**En consola:**

```
=== 2 · NAVEGAR · tramite -> contribuyente ===
Hibernate:
    select
        t1_0.id, t1_0.contribuyente_id, t1_0.estado, t1_0.fecha, t1_0.tipo
    from
        tramite t1_0
    where
        t1_0.id=?
  trámite cargado: Tramite{id=1, tipo='Declaración F29', estado='RECIBIDO', fecha=2026-03-10}
  --- todavía NO se ha tocado el contribuyente ---
Hibernate:
    select
        c1_0.id, c1_0.razon_social, c1_0.rut
    from
        contribuyente c1_0
    where
        c1_0.id=?
  ahora sí: Comercial Andes Ltda.
```

**El segundo SELECT sale DESPUÉS de la marca.** No antes. Esa es toda la definición de LAZY, y
está ahí, en el orden de las líneas.

---

## Paso 3 · El lado espejo

**Se explica:** hasta ahora se va del trámite al contribuyente. ¿Y al revés? La tabla
`contribuyente` no tiene ninguna columna que apunte a `tramite`, así que hay que decirle a JPA
dónde mirar: `mappedBy = "contribuyente"` significa «la relación está guardada en el campo
`contribuyente` de la clase `Tramite`».

**Este lado no guarda nada.** Sirve para navegar y nada más.

**Se pega (1 de 4):** en `entities/Contribuyente.java`, **arriba**, con los imports.

```java
import jakarta.persistence.OneToMany;
import java.util.ArrayList;
import java.util.List;
```

**Se pega (2 de 4):** en el mismo archivo, **donde dice `// escribe aquí`**.

```java
    @OneToMany(mappedBy = "contribuyente")
    private List<Tramite> tramites = new ArrayList<>();
```

**Se pega (3 de 4):** en el mismo archivo, **antes de la llave que cierra la clase**.

```java
    public List<Tramite> getTramites() {
        return tramites;
    }
```

**Se pega (4 de 4):** en `practica/src/main/java/cl/dgt/relaciones/demos/DemosRelaciones.java`, el import **arriba** y después **reemplazando el método
`listarTramitesDeUnContribuyente()` entero**.

```java
import java.util.List;
```

```java
    @Transactional(readOnly = true)
    public void listarTramitesDeUnContribuyente() {
        seccion(3, "LADO ESPEJO · @OneToMany(mappedBy)");

        Contribuyente andes = contribuyentes.findByRut("76.543.210-K").orElseThrow();
        System.out.println("  contribuyente: " + andes.getRazonSocial());
        System.out.println("  --- todavía NO se ha tocado la lista ---");

        List<Tramite> suyos = andes.getTramites();
        System.out.println("  tiene " + suyos.size() + " trámites:");
        suyos.forEach(t -> System.out.println("    " + t));
    }
```

**Se agrega al runner:** en `Lab05Application.java`, dentro de `return args -> {`:

```java
            demos.listarTramitesDeUnContribuyente();
```

**En consola:**

```
=== 3 · LADO ESPEJO · @OneToMany(mappedBy) ===
Hibernate:
    select ... from contribuyente c1_0 where c1_0.rut=?
  contribuyente: Comercial Andes Ltda.
  --- todavía NO se ha tocado la lista ---
Hibernate:
    select ... from tramite t1_0 where t1_0.contribuyente_id=?
  tiene 2 trámites:
    Tramite{id=1, tipo='Declaración F29', ...}
    Tramite{id=2, tipo='Certificado de situación', ...}
```

Otra vez el mismo patrón: el segundo SELECT llega cuando se toca la lista.

**La pregunta del paso:** si se borra un trámite de esa lista en Java, ¿cambia algo en la base?
(No. Este lado no manda.)

---

## Paso 4 · LAZY contra EAGER, contado

**Se explica:** LAZY tiene fama de complicado y EAGER de cómodo. Se va a medir cuál sale caro.

**Se pega:** en `practica/src/main/java/cl/dgt/relaciones/demos/DemosRelaciones.java`, **reemplazando el método `lazyContraEager()` entero**. Trae los 6 trámites
con `findAll()` y **no toca el contribuyente de ninguno**.

```java
    public void lazyContraEager() {
        seccion(4, "LAZY vs EAGER · contar los SELECT");

        System.out.println("  >>>>>> EMPIEZA EL CONTEO — cuenta los 'Hibernate:' hasta la marca de fin");
        List<Tramite> todos = tramites.findAll();
        System.out.println("  <<<<<< FIN DEL CONTEO — " + todos.size() + " trámites traídos");
        System.out.println("  (no se tocó el contribuyente de ninguno)");
    }
```

**Se agrega al runner:** en `Lab05Application.java`, dentro de `return args -> {`:

```java
            demos.lazyContraEager();
```

**En consola, con LAZY (como está ahora):**

```
=== 4 · LAZY vs EAGER · contar los SELECT ===
  >>>>>> EMPIEZA EL CONTEO — cuenta los 'Hibernate:' hasta la marca de fin
Hibernate:
    select ... from tramite t1_0
  <<<<<< FIN DEL CONTEO — 6 trámites traídos
  (no se tocó el contribuyente de ninguno)
```

**Un SELECT.**

Ahora, sin tocar la demo, cambiar **una palabra** en `entities/Tramite.java`:

```java
@ManyToOne(fetch = FetchType.EAGER)     // ← LAZY pasa a EAGER
```

Reiniciar y volver a contar entre las mismas dos marcas:

```
  >>>>>> EMPIEZA EL CONTEO — cuenta los 'Hibernate:' hasta la marca de fin
Hibernate:  select ... from tramite t1_0
Hibernate:  select ... from contribuyente c1_0 where c1_0.id=?
Hibernate:  select ... from contribuyente c1_0 where c1_0.id=?
Hibernate:  select ... from contribuyente c1_0 where c1_0.id=?
  <<<<<< FIN DEL CONTEO — 6 trámites traídos
```

**Cuatro.** Uno por la lista, y tres por los tres contribuyentes distintos — que **nadie pidió**.

| | SELECT |
|---|---|
| LAZY | 1 |
| EAGER | 4 |

**Volver a LAZY antes de seguir.** Y la regla, que conviene decir entera:

> **LAZY siempre, y escrito.** `@ManyToOne` es EAGER por defecto: si no se declara, se paga sin
> saberlo. EAGER no es «más cómodo», es «más caro siempre, se use o no».

**La pregunta del paso:** aquí la diferencia son 3 SELECT. ¿Y con 6.000 trámites? Esa pregunta es
el Lab 06 entero.

---

## Paso 5 · El error que todo el mundo se encuentra

**Se explica:** si la relación se va a buscar cuando se toca… ¿qué pasa si se toca **cuando ya no
hay a dónde ir**? El repositorio abre una transacción, la cierra, y devuelve un objeto
desconectado de la base. Pedirle ahora la relación es pedirle un SELECT a una sesión que ya no
existe.

**Se pega (1 de 2):** en `practica/src/main/java/cl/dgt/relaciones/demos/DemosRelaciones.java`, **arriba**, con los imports.

```java
import org.hibernate.LazyInitializationException;
```

**Se pega (2 de 2):** en el mismo archivo, **reemplazando el método `elErrorDeLaSesionCerrada()`
entero**. Es exactamente lo mismo que la demo 2 — pero fíjate en que este método **no** lleva
`@Transactional`.

```java
    public void elErrorDeLaSesionCerrada() {
        seccion(5, "LazyInitializationException · fuera de la transacción");

        Tramite tramite = tramites.findById(primerTramiteId).orElseThrow();
        System.out.println("  trámite cargado (y la sesión ya se cerró): " + tramite);

        try {
            String razon = tramite.getContribuyente().getRazonSocial();
            System.out.println("  razón social: " + razon + "   <-- si ves esto, algo cambió");
        } catch (LazyInitializationException e) {
            System.out.println("  REVENTÓ, y está bien: " + e.getClass().getSimpleName());
            System.out.println("  mensaje: " + e.getMessage());
        }
    }
```

**Se agrega al runner:** en `Lab05Application.java`, dentro de `return args -> {`:

```java
            demos.elErrorDeLaSesionCerrada();
```

**En consola:**

```
=== 5 · LazyInitializationException · fuera de la transacción ===
Hibernate:
    select ... from tramite t1_0 where t1_0.id=?
  trámite cargado (y la sesión ya se cerró): Tramite{id=1, ...}
  REVENTÓ, y está bien: LazyInitializationException
  mensaje: Could not initialize proxy [cl.dgt.relaciones.entities.Contribuyente#1] - no session
```

**`no session`.** Ahí está dicho: no hay sesión abierta con la base.

Este es probablemente el error de JPA que más veces se va a encontrar en su vida profesional, y
casi nunca se explica bien. **No es un fallo de JPA**: es JPA diciendo «me pediste algo que
requiere ir a la base, y ya no tengo por dónde». Se arregla de tres formas, y el orden importa:

1. **Pedir el dato dentro de la transacción**, como en la demo 2.
2. **Traerlo de entrada con un JOIN FETCH** cuando se sabe que va a hacer falta. → Lab 06.
3. Convertir a un DTO antes de salir de la transacción. → también Lab 06.

Lo que **no** se hace es poner EAGER para que no moleste: eso apaga este error y enciende el del
paso 4 en todas las demás pantallas.

---

## Paso 6 · Un método cuyo nombre cruza la relación

**Se explica:** en el Lab 04 el nombre del método era la consulta. Eso sigue valiendo, **y
atraviesa relaciones**: un trámite no tiene RUT, pero su contribuyente sí, y se puede decir así.

**Se pega (1 de 3):** en `repositories/TramiteRepository.java`, **arriba**, con los imports.

```java
import java.util.List;
```

**Se pega (2 de 3):** en el mismo archivo, **dentro de la interfaz**.

```java
    List<Tramite> findByContribuyenteRut(String rut);
```

**Se pega (3 de 3):** en `practica/src/main/java/cl/dgt/relaciones/demos/DemosRelaciones.java`, **reemplazando el método `consultaQueCruzaLaRelacion()` entero**.

```java
    public void consultaQueCruzaLaRelacion() {
        seccion(6, "CONSULTA DERIVADA QUE NAVEGA · findByContribuyenteRut()");

        List<Tramite> deAndes = tramites.findByContribuyenteRut("76.543.210-K");
        System.out.println("  trámites del RUT 76.543.210-K -> " + deAndes.size());
        deAndes.forEach(t -> System.out.println("    " + t));
    }
```

**Se agrega al runner:** en `Lab05Application.java`, dentro de `return args -> {`:

```java
            demos.consultaQueCruzaLaRelacion();
```

**En consola:**

```
=== 6 · CONSULTA DERIVADA QUE NAVEGA · findByContribuyenteRut() ===
Hibernate:
    select
        t1_0.id, t1_0.contribuyente_id, t1_0.estado, t1_0.fecha, t1_0.tipo
    from
        tramite t1_0
    left join
        contribuyente c1_0
            on c1_0.id=t1_0.contribuyente_id
    where
        c1_0.rut=?
  trámites del RUT 76.543.210-K -> 2
```

**Un solo SELECT, con el `left join` dentro.** Nadie escribió ese JOIN: salió del nombre del
método, leyendo `Contribuyente` + `Rut` como «el campo `rut` de la propiedad `contribuyente`».

---

## Al terminar

`practica/` imprime exactamente lo mismo que `solucion/`. Si algo no cuadra, `solucion/` está ahí
para comparar archivo por archivo.

Lo que hay que poder decir con las propias palabras:

> La relación vive donde está la columna. LAZY significa que se va a buscar cuando la toques, y
> eso se ve como un SELECT más. EAGER lo trae siempre, lo uses o no.

### Lo que siembra este lab

En el paso 2 salió un SELECT de más. Uno. No pasa nada.

En el paso 4, sin tocar una sola línea de la demo, salieron cuatro donde había uno. Y eran seis
trámites y tres contribuyentes.

> **La pregunta que abre el Lab 06** — si traer un trámite dispara un SELECT extra por su
> contribuyente… ¿qué pasa cuando la pantalla trae **doscientos**?

El Lab 06 lo pone en un número: la misma lista, medida con un contador de consultas, y las tres
formas de arreglarlo sin tocar el mapeo.
