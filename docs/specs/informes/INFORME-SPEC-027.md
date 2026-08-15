# INFORME-SPEC-027 · Lab 3.5 «JPA» — rehecho con el formato del PO

**SPEC:** SPEC-027 · **Ejecuta:** mocito · **Fecha:** 15 de agosto de 2026
**Rama:** `spec-027-lab-3-5-jpa` · desde `main` (`b9bd2e4`, `material-v0.4.0`)
**Máquina:** Mac Studio del PO — Darwin 25.5.0, `arm64`

---

## 1 · Veredicto en una línea

**EL LAB EXISTE Y EL GUION FUNCIONA** — `practica/` arranca limpio tal como se entrega,
`solucion/` corre las ocho demos con su SQL a la vista, y **seguir `PASOS.md` de principio a fin
produce exactamente la misma salida que la solución**; el lab anterior quedó eliminado y la
cadena de derivación está igual que en `main`, ni mejor ni peor.

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
