# INFORME-SPEC-027 · Lab 3.5 «JPA» — rehecho con el formato del PO

**SPEC:** SPEC-027 + **anotaciones A1** · **Ejecuta:** mocito · **Fecha:** 15 de agosto de 2026
**Rama:** `spec-027-lab-3-5-jpa` · desde `main` (`b9bd2e4`, `material-v0.4.0`)
**Máquina:** Mac Studio del PO — Darwin 25.5.0, `arm64`

---

## 1 · Veredicto en una línea

**EL LAB EXISTE, PERSISTE Y EL GUION FUNCIONA** — `practica/` arranca limpio tal como se entrega,
`solucion/` corre las diez demostraciones con su SQL a la vista, **lo guardado sobrevive a apagar
el programa**, la base se puede mirar desde DBeaver mientras corre, los cuatro endpoints
responden, y **seguir `PASOS.md` de principio a fin produce exactamente la misma salida que la
solución**; la cadena de derivación está igual que en `main`.

---

## 2 · Qué se hizo

El Lab 3.5 anterior (`lab-03b-guardar-y-recuperar`) se descartó completo. Esta rama sale de
`main`, donde **ese lab nunca existió**: no hubo nada que borrar ni que revertir — el Lab 04
está intacto y la cadena es la de `main` por construcción, no por reparación.

El lab nuevo es de **construcción guiada**: el alumno escribe las clases en vivo, junto al
instructor, un paso a la vez.

```
labs/lab-03c-jpa/
├── README.md      media página: qué se aprende, los dos directorios, cómo se corre
├── PASOS.md       el guion del instructor, ocho pasos
├── practica/      proyecto Maven completo y ejecutable, INCOMPLETO
└── solucion/      el mismo, TERMINADO
```

Los dos proyectos llevan la maleta de v0.4.0 (shim `mvnw` + JDK embebido + `repo-maven`) y
arrancan con `./mvnw spring-boot:run`. No son aplicaciones web: corren las demos y terminan.

**Una sola tabla y ninguna relación.** `@ManyToOne` y compañía son del Lab 04; hoy el tema es más
básico: cómo una fila y un objeto pasan a ser la misma cosa.

### Las ocho demos

| | Qué demuestra |
|---|---|
| 1 · `guardar()` | el objeto entra sin id y sale con id · INSERT |
| 2 · `buscarPorId()` | `Optional`, y por qué no es capricho · SELECT por PK |
| 3 · `listarTodas()` | `findAll` — cómodo y peligroso |
| 4 · `buscarPorAutor()` | la primera derivada: el nombre ES la consulta |
| 5 · `buscarConDosCondiciones()` | `And` + `After` |
| 6 · `actualizar()` | **sin `save()`**: el UPDATE aparece solo al cerrar |
| 7 · `borrar()` | `deleteById`, con count antes y después |
| 8 · `contar()` | `count()` contra `findAll().size()` |

Cada método lleva encima un comentario de cinco a ocho líneas —qué demuestra, qué SQL genera, y
una pregunta para pensar—. En `practica/` esos comentarios están **enteros**, porque son la
instrucción, y el cuerpo es un `// escribe aquí`.

---

## 3 · Dos decisiones de detalle

**Dónde vive la base de datos.** La estructura que la SPEC fija no incluye un paquete de
configuración, así que los dos `@Bean` del PostgreSQL embebido viven en `Lab35Application`, bajo
un separador que dice *«no hace falta leer esto para el laboratorio»*. Inventar un
`config/EmbeddedPostgresConfig` habría añadido un archivo que la estructura no pedía y que el
alumno no necesita abrir.

**Ocho pasos para ocho demos, con dos pasos dobles.** La SPEC pide **8 pasos**, y también que los
pasos 1 y 2 sean crear la entidad y el repositorio: quedan 6 pasos para 8 métodos. Se agruparon
los dos pares que se explican juntos —**paso 4**: `buscarPorId` + `listarTodas`; **paso 8**:
`borrar` + `contar`— y el guion lo dice en su encabezado. Las otras cuatro demos tienen su paso
propio, incluida la 6, que es la que justifica el laboratorio.

---

## 4 · V4 · la prueba real: seguir el guion

Es la verificación que la SPEC declara decisiva, y **encontró tres defectos en el guion**. Se
hizo escribiendo el código *solo con lo que dice `PASOS.md`*, sin mirar la solución:

| Defecto | Cómo se arregló |
|---|---|
| El paso 3 no daba los datos concretos, así que la consola no podía coincidir | Se añadió la tabla con los tres textos, autores y fechas |
| El paso 7 pedía «cambiarlo con el setter», pero el paso 1 no lo incluía en la entidad | El paso 7 dice ahora que hay que añadir `setTexto`, y por qué aparece justo ahí |
| La consola esperada del paso 8 decía `countByAutor("Carolina") -> 2` | Es **1**: el paso 7 borró una observación de Carolina. Corregido, y aprovechado como pregunta para la sala |

Con eso corregido, se siguió el guion entero sobre `practica/` en su sitio y se comparó la
consola contra la de `solucion/`:

```
$ diff <(salida de solucion) <(salida de practica seguida del guion)
  SALIDAS IDENTICAS — el guion lleva al mismo resultado
```

`practica/` se restauró después a su estado de entrega, verificado: los dos `.gitkeep` en su
sitio, las ocho llamadas comentadas y los ocho `// escribe aquí` intactos.

---

## 5 · Verificación

| # | Prueba | Resultado |
|---|---|---|
| **V1** | `practica/` tal como se entrega | `exit=0`, **0 líneas con ERROR o Exception**; Flyway aplica V1 y el programa termina |
| **V2** | `solucion/` con las ocho demos | `exit=0`; las ocho imprimen (§6) |
| **V3** | El SQL de las ocho | citado en §6: INSERT, SELECTs, **UPDATE sin `save()`**, DELETE, COUNT |
| **V4** | Seguir `PASOS.md` sobre `practica/` | **salidas idénticas** a `solucion/` (§4) |
| **V5** | Offline | **0 descargas** en los dos proyectos (`./mvnw` es offline por defecto) |
| **V6** | Derivación y CI | **26 eslabones en sincronía**, único rojo la frontera 07→08 — exactamente como `main` |
| **V7** | `ls labs/lab-03c-jpa` | `PASOS.md README.md practica solucion` |

**Un rojo que sí fue nuestro, y se corrigió.** El job `siembra` exige que todo lab con sucesor
tenga un `TEORIA.md` que siembre el módulo siguiente (P-18), y este lab no lleva ninguno: su
documento de enseñanza es `PASOS.md`.

```
[ERROR] labs/lab-03c-jpa no tiene TEORIA.md
```

Se resolvió **enseñándole el nombre al gate** —si no hay `TEORIA.md`, mira `PASOS.md`, con la
misma exigencia— y, sobre todo, haciendo la siembra de verdad: `PASOS.md` cierra ahora con las
dos preguntas que quedan abiertas y que son el Lab 04, incluida la que este lab no llegó a
tocar: *¿qué pasa cuando una entidad apunta a otra?*

**V6, con detalle:** el lab nuevo no entra en la cadena de derivación y no debe entrar — no
deriva del tronco, es un proyecto aparte con su propio paquete (`cl.dgt.jpa`). El verificador
recorre `labs/lab-NN-*` con `solucion/`+`starter/`; `lab-03c-jpa` tiene `practica/`, así que ni
lo mira. No se tocó `verificar-toda-derivacion.sh`.

---

## 6 · La consola de `solucion/`, citada

```
=== 1 · GUARDAR · save() ===
  antes de guardar -> id = null
Hibernate:
    insert into observacion (autor, fecha, texto) values (?, ?, ?)
  después de guardar -> id = 1
  (guardadas 2 más, para las demos siguientes)

=== 2 · BUSCAR POR ID · findById() ===
Hibernate:
    select o1_0.id, o1_0.autor, o1_0.fecha, o1_0.texto from observacion o1_0 where o1_0.id=?
  id 1 -> Observacion{id=1, texto='Revisión anual sin hallazgos.', autor='Carolina', fecha=2026-03-10}
  id 9999 -> no existe

=== 3 · LISTAR TODAS · findAll() ===
Hibernate:
    select o1_0.id, o1_0.autor, o1_0.fecha, o1_0.texto from observacion o1_0
  3 observaciones:
    Observacion{id=1, ...}  ·  {id=2, ...}  ·  {id=3, ...}

=== 4 · BUSCAR POR AUTOR · findByAutor() ===
Hibernate:
    select ... from observacion o1_0 where o1_0.autor=?
  autor = Carolina -> 2

=== 5 · DOS CONDICIONES · findByAutorAndFechaAfter() ===
Hibernate:
    select ... from observacion o1_0 where o1_0.autor=? and o1_0.fecha>?
  autor = Carolina y fecha > 2026-06-01 -> 1

=== 6 · ACTUALIZAR SIN save() · dirty checking ===
Hibernate:
    select ... from observacion o1_0 where o1_0.id=?
  antes:  Revisión anual sin hallazgos.
  después: Revisión anual: se detecta diferencia menor.
  NO llamamos a save(). El UPDATE aparece justo aquí abajo,
  cuando esta transacción se cierre:
Hibernate:
    update observacion set autor=?, fecha=?, texto=? where id=?

=== 7 · BORRAR · deleteById() ===
Hibernate: select count(*) from observacion o1_0
  filas antes:  3
Hibernate: select ... where o1_0.id=?
Hibernate: delete from observacion where id=?
  filas después: 2

=== 8 · CONTAR · count() vs findAll().size() ===
Hibernate: select count(*) from observacion o1_0
  count()          -> 2
Hibernate: select o1_0.id, o1_0.autor, o1_0.fecha, o1_0.texto from observacion o1_0
  findAll().size() -> 2
Hibernate: select count(o1_0.id) from observacion o1_0 where o1_0.autor=?
  countByAutor("Carolina") -> 1
```

**La demo 6 es la que justifica el laboratorio.** El `UPDATE` aparece *después* del último
`println`, cuando el método termina y la transacción se cierra. Nadie llamó a `save`.

Y la 8 deja el contraste servido: `count()` pide un número, `findAll().size()` se trae las filas
enteras a memoria para contarlas. Con dos da igual; con quinientas mil, no.

---

## 6.b · Las anotaciones A1

### A1.1 · La base persiste de verdad

El Postgres embebido usa ahora un **directorio de datos fijo** dentro de cada proyecto
(`.datos-pg/`, ignorado por git), con `setCleanDataDirectory(false)`. Antes nacía vacío en cada
arranque, que es una base en memoria con pasos extra — y el lab se llama guardar y recuperar.

**El duplicado, resuelto con `deleteAll()`.** Como la base persiste, las tres observaciones del
paso 3 se sumarían en cada corrida. De las dos opciones que la anotación planteaba se eligió que
`guardar()` limpie la tabla antes de sembrar, y por dos razones:

- **Es visible y explicable en clase.** Está en la primera línea del método que el alumno escribe,
  con tres líneas de comentario que dicen por qué. La alternativa —«borra el directorio cuando
  quieras empezar de nuevo»— es una instrucción de mantenimiento que se olvida y que deja al
  alumno con quince filas sin entender por qué.
- **No toca el esquema.** `deleteAll()` borra filas; la tabla sigue siendo cosa de Flyway, como
  manda la anotación. Nada de `create-drop`.

El `PASOS.md` dice además dónde vive la base y que borrar `.datos-pg/` es la forma de volver a
cero, para quien lo necesite.

### A1.2 · Puerto fijo, y los dos proyectos a la vez

| | `practica/` | `solucion/` |
|---|---|---|
| PostgreSQL | **55432** | **55433** |
| HTTP | 8099 | 8100 |

Puertos distintos y bases distintas: **pueden correr los dos al mismo tiempo**, y así el
instructor puede tener la solución levantada mientras el alumno trabaja. Los datos de conexión
completos —host, puerto, base, usuario, clave— están en la sección «Mirar la base por fuera» del
README, con la frase que resume el punto: *ver el objeto en la consola es ver la memoria; ver la
fila en la tabla es ver la persistencia*.

### A1.3 · El controller

Cuatro llamadas sobre `web/ObservacionController.java`, completo en `solucion/` y declarado con
los cuerpos vacíos en `practica/`. Devuelve la entidad directamente, sin DTO, y el comentario lo
dice en una línea.

**Un detalle de construcción que hubo que resolver:** en `practica/` el controller no puede
mencionar `Observacion` ni el repositorio, porque no existen todavía —el proyecto no compilaría—.
Los métodos llegan con tipos comodín (`List<?>`, `ResponseEntity<?>`, `Map`) y el paso 10 dice
explícitamente que cambiarlos por `Observacion` es parte del trabajo. Es la misma solución que
`DemosJpa` usa para no referenciar lo que el alumno aún no escribió.

### A1.5 · La aplicación se queda corriendo

Entra `spring-boot-starter-web` en los dos poms. Es condición de A1.2 y A1.3: sin web la
aplicación terminaba al acabar las demos, y no habría cómo conectarse con un cliente SQL ni
llamar a los endpoints. Documentado en el README, en el encabezado de `PASOS.md` y en el javadoc
de `Lab35Application`: **se apaga con Ctrl+C**.

---

## 6.c · Verificación de las anotaciones

### V8 · lo guardado sobrevive al apagado

Primera ejecución completa, después Ctrl+C, después arrancar con solo `listarTodas()`:

```
=== 3 · LISTAR TODAS · findAll() ===
  3 observaciones:
    Observacion{id=2, texto='Solicita certificado de situación.', autor='Carolina', fecha=2026-08-01}
    Observacion{id=3, texto='Diferencias en el F29 de julio.', autor='Ignacio', fecha=2026-07-15}
    Observacion{id=4, texto='Creada desde Postman.', autor='Rodrigo', fecha=2026-08-15}
```

Las mismas filas **sin haber vuelto a guardar nada**, incluida la que se creó por HTTP en la
ejecución anterior. Y Flyway lo confirma a su manera:

```
Schema "public" is up to date. No migration necessary.
```

### V9 · la base, mirada desde fuera

Con la aplicación corriendo, un cliente SQL externo contra `localhost:55433`
(usuario y clave `postgres`):

```
 id | texto                                | autor    | fecha
----+--------------------------------------+----------+-----------
  2 | Solicita certificado de situación.   | Carolina | 2026-08-01
  3 | Diferencias en el F29 de julio.      | Ignacio  | 2026-07-15
  4 | Creada desde Postman.                | Rodrigo  | 2026-08-15
```

El `id=1` no está porque el paso 7 lo borró. La tabla dice exactamente lo que dijo la consola.

### V10 · los cuatro endpoints

```
GET  /api/observaciones                 -> 200  [{…"id":2}, {…"id":3}]
GET  /api/observaciones?autor=Carolina  -> 200  [{"texto":"Solicita certificado…","autor":"Carolina","id":2}]
GET  /api/observaciones/2               -> 200  {"texto":"Solicita certificado…","id":2}
GET  /api/observaciones/9999            -> 404
POST /api/observaciones                 -> 201  {"texto":"Creada desde Postman.","autor":"Rodrigo","fecha":"2026-08-15","id":4}
```

### V11 · `practica/` en su estado de entrega

Con el controller vacío incluido: **0 errores**, la aplicación queda arriba, la base escucha en
55432, y `GET /api/observaciones` responde `200` con lista vacía —el controller todavía no
consulta nada—. Los dos `.gitkeep`, las ocho llamadas comentadas, los ocho `// escribe aquí` de
las demos y los tres del controller, intactos.

### V12 · el guion completo, diez pasos

Se siguió `PASOS.md` de principio a fin sobre `practica/` y se comparó contra `solucion/`:

```
$ diff <(consola de solucion) <(consola de practica seguida del guion)
  SALIDAS IDENTICAS — 18 sentencias SQL cada una
```

Y los endpoints de `practica/` (puerto 8099) responden igual: `200` en los GET, `404` en el id
inexistente y `201` en el POST.

**Un tropiezo del arnés que conviene dejar escrito**, porque cuesta media hora la primera vez: al
comparar las dos consolas aparecían tres `SELECT` de más en la solución. No era del lab — era el
`curl` con el que yo esperaba a que la aplicación levantara, que entraba por el endpoint mientras
las demos todavía corrían y metía su propio SQL en el log. Se rehicieron las dos corridas sin esa
espera y el diff quedó limpio. Y el segundo, de la misma familia: un `cp -p` preservó la fecha de
un archivo restaurado, quedó más antiguo que su `.class`, Maven no recompiló y corrió código
viejo — exactamente lo que el propio `derivar-desde-tronco.sh` advierte en su comentario.

---

## 7 · Lo que queda

1. **La prueba de aceptación del PO**: correr `solucion/`, y después seguir `PASOS.md` sobre
   `practica/` como lo haría en sala.
2. **El PR #32 quedó cerrado sin merge**, con un comentario que apunta a esta SPEC. Su contenido
   sigue en el historial por si algo sirve para otro lab.
3. **La PPT**, si el PO la quiere: el guion ya trae, paso por paso, qué se explica y qué debe
   verse.
4. **El Lab 04 sigue derivando del Lab 03**, como en `main`. Si algún día el 3.5 debe entrar en
   la cadena, es una decisión aparte — hoy es un proyecto independiente y no la toca.
5. **SPEC-025 / PR #31** sigue esperando firma.

---

## 8 · Cierre

Un laboratorio nuevo con un formato nuevo: se corre, se mira, y se construye. Cuatro archivos en
la raíz, dos proyectos que arrancan solos, y un guion que se probó siguiéndolo — que es la única
forma de saber si un guion sirve.

En posición de merge cuando el PO lo autorice. PR en draft.
