# Pasos · Lab 04 · Relaciones

Seis pasos. Se construyen en `practica/`, en vivo, uno a la vez. Después de cada paso se reinicia
el programa y se mira la consola antes de seguir.

```bash
cd practica
./mvnw spring-boot:run
```

**El programa se queda corriendo**: se apaga con **Ctrl+C**. Escucha en el **8087** y su base en
el **55434** (`solucion/`, en el 8088 y el 55435).

Las seis llamadas a las demos están **comentadas** en `Lab04Application`. Cada paso llena un
método de `DemosRelaciones` y descomenta su línea.

> **Lo que hay que mirar hoy no es lo que imprimen los métodos.** Es el SQL que sale entre medio,
> y sobre todo **cuántas veces**.

---

## Paso 1 · La relación

**Se explica:** un trámite pertenece a un contribuyente. Muchos trámites, un contribuyente: de ahí
el nombre `@ManyToOne`. Y una regla que vale para todo JPA: **la relación vive donde está la
columna**. Abrir `db/migration/V1__contribuyente_y_tramite.sql` y mirar la última línea de la
tabla `tramite` — ahí está `contribuyente_id`. La clase tiene que decir lo mismo.

**Se escribe:** en `entities/Tramite.java`, donde dice `// escribe aquí`:

```java
@ManyToOne(fetch = FetchType.LAZY)
@JoinColumn(name = "contribuyente_id", nullable = false)
private Contribuyente contribuyente;
```

más su getter, y el cuarto parámetro del constructor:

```java
public Tramite(String tipo, String estado, LocalDate fecha, Contribuyente contribuyente) {
    ...
    this.contribuyente = contribuyente;
}
```

Y en `demos/DemosRelaciones.java`, la demo 1. Empieza contando lo que quedó de la vez anterior —la
base persiste, como en el Lab 3b— y borrando:

```java
System.out.println("  al arrancar había " + contribuyentes.count() + " contribuyentes y "
        + tramites.count() + " trámites de la vez anterior");

tramites.deleteAll();        // los trámites PRIMERO: la clave foránea no deja
contribuyentes.deleteAll();  // borrar un contribuyente que tenga trámites
```

y después guarda 3 contribuyentes y 6 trámites, 2 por contribuyente, dejando el id del primero en
`this.primerTramiteId`.

**Se descomenta:** `demos.guardarConRelacion();`

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

**Se escribe:** la demo 2. Cargar el trámite `primerTramiteId`, imprimirlo, imprimir una marca, y
**solo entonces** pedir `tramite.getContribuyente().getRazonSocial()`.

El método ya viene con `@Transactional(readOnly = true)`. Es a propósito: sin eso, la sesión
estaría cerrada al llegar aquí. Eso es el paso 5.

**Se descomenta:** `demos.navegarDeTramiteAContribuyente();`

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

**Se escribe:** en `entities/Contribuyente.java`:

```java
@OneToMany(mappedBy = "contribuyente")
private List<Tramite> tramites = new ArrayList<>();
```

más su getter. Y la demo 3: buscar el contribuyente por RUT, imprimir su razón social y una marca,
y solo entonces recorrer `andes.getTramites()`.

**Se descomenta:** `demos.listarTramitesDeUnContribuyente();`

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

**Se escribe:** la demo 4. Traer los 6 trámites con `findAll()` y **no tocar el contribuyente de
ninguno**. Imprimir una marca antes y otra después, para poder contar los bloques `Hibernate:`
que salen entre las dos.

**Se descomenta:** `demos.lazyContraEager();`

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
el Lab 05 entero.

---

## Paso 5 · El error que todo el mundo se encuentra

**Se explica:** si la relación se va a buscar cuando se toca… ¿qué pasa si se toca **cuando ya no
hay a dónde ir**? El repositorio abre una transacción, la cierra, y devuelve un objeto
desconectado de la base. Pedirle ahora la relación es pedirle un SELECT a una sesión que ya no
existe.

**Se escribe:** la demo 5 — exactamente lo mismo que la 2, pero fijándose en que este método
**no** lleva `@Transactional`. Atrapar la excepción e imprimirla, para que el programa siga.

**Se descomenta:** `demos.elErrorDeLaSesionCerrada();`

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
2. **Traerlo de entrada con un JOIN FETCH** cuando se sabe que va a hacer falta. → Lab 05.
3. Convertir a un DTO antes de salir de la transacción. → también Lab 05.

Lo que **no** se hace es poner EAGER para que no moleste: eso apaga este error y enciende el del
paso 4 en todas las demás pantallas.

---

## Paso 6 · Un método cuyo nombre cruza la relación

**Se explica:** en el Lab 3b el nombre del método era la consulta. Eso sigue valiendo, **y
atraviesa relaciones**: un trámite no tiene RUT, pero su contribuyente sí, y se puede decir así.

**Se escribe:** en `repositories/TramiteRepository.java`:

```java
List<Tramite> findByContribuyenteRut(String rut);
```

y la demo 6, que lo llama e imprime el resultado.

**Se descomenta:** `demos.consultaQueCruzaLaRelacion();`

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

> **La pregunta que abre el Lab 05** — si traer un trámite dispara un SELECT extra por su
> contribuyente… ¿qué pasa cuando la pantalla trae **doscientos**?

El Lab 05 lo pone en un número: la misma lista, medida con un contador de consultas, y las tres
formas de arreglarlo sin tocar el mapeo.
