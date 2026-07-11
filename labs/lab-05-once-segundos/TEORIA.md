# Teoría · Módulo 5 (cierre) + Módulo 6 + Módulo 7 (teoría)

## Índice

1. [El N+1: qué es, y por qué no se ve](#1-el-n1-qué-es-y-por-qué-no-se-ve)
2. [Medir, no confiar](#2-medir-no-confiar)
3. [El acto 2: EAGER baja el contador… y por qué igual está mal](#3-el-acto-2-eager-baja-el-contador-y-por-qué-igual-está-mal)
4. [`@EntityGraph` vs `JOIN FETCH` vs proyección](#4-entitygraph-vs-join-fetch-vs-proyección)
5. [Paginación, y no filtrar la estructura interna](#5-paginación-y-no-filtrar-la-estructura-interna)
6. [M6 · La pirámide de pruebas](#6-m6--la-pirámide-de-pruebas)
7. [M6 · La mentira del dialecto H2 (con el caso de este repo)](#7-m6--la-mentira-del-dialecto-h2-con-el-caso-de-este-repo)
8. [M6 · Testcontainers, `@ServiceConnection`, RestTestClient](#8-m6--testcontainers-serviceconnection-resttestclient)
9. [M7 (teoría) · El proxy transaccional y `readOnly`](#9-m7-teoría--el-proxy-transaccional-y-readonly)
10. [Tabla DO / DON'T · Glosario](#10-tabla-do--dont--glosario)
11. [Conclusiones y siembra del Módulo 6](#11-conclusiones-y-siembra-del-módulo-6)

---

## 1. El N+1: qué es, y por qué no se ve

Un listado trae N filas en **1** consulta. Luego, por cada fila, toca una relación LAZY —el
contribuyente de cada trámite— y dispara **1 consulta más**. Total: **1 + N** consultas.
Con N=3 (tu semilla), son 4 consultas: instantáneo. Con N=50.000, son 50.001: once segundos.

El N+1 no se ve en desarrollo porque los datos de prueba son pocos. Explota en producción,
donde los datos son muchos. Por eso *no se detecta mirando* — se detecta **midiendo**.

> Y hay un agravante honesto: declaraste `LAZY`, e igual el `@OneToOne` inverso (el F29, el
> folio) Hibernate lo carga por su cuenta (el caveat del Lab 04, §10). Esas consultas extra
> también son N+1. Declaraste bien, y un rincón del framework igual te traiciona. **Por eso
> se mide, no se confía.**

---

## 2. Medir, no confiar

*"Va lento"* no es un dato. *"Una página cuesta 13 consultas y debería costar 3"* sí lo es.

La herramienta: un **contador de consultas**. Las `Statistics` de Hibernate cuentan las
sentencias SQL preparadas alrededor de un bloque de código. El test `E1` lo usa: no mira el
resultado del listado, mira su **costo**, y falla si supera el presupuesto.

Ese test es el arquetipo del criterio que no se aprueba tecleando más código: se aprueba
haciendo la consulta correcta. El contador es determinista — mismo código, mismo número — así
que un presupuesto (`≤ 3`) es una línea que o cruzas o no.

---

## 3. El acto 2: EAGER baja el contador… y por qué igual está mal

La tentación: volver todo a EAGER. ¡El contador baja a 1! Una sola consulta.

Y es un desastre, por tres razones:

1. Esa **una** consulta es un `JOIN` gigante (producto cartesiano): trae más filas y más
   memoria que las N pequeñas.
2. El peaje lo pagan **todos** los endpoints que tocan la entidad, la necesiten o no. El
   `findById` de una ficha cargaría el árbol entero, otra vez.
3. **AU-04 lo caza.** El guardián que instalaste el Lab 04 impide institucionalizar el parche:
   una relación en EAGER pone el build en rojo.

La métrica no es "menos consultas". Es **"las consultas correctas para ESTA pantalla"**. Un
listado necesita cuatro columnas por fila; no necesita el árbol de cada trámite.

---

## 4. `@EntityGraph` vs `JOIN FETCH` vs proyección

Tres formas de matar el N+1, de menor a mayor precisión:

- **`@EntityGraph`** — le dices a un método de repositorio "para ESTA consulta, trae también
  el contribuyente". Sigue cargando la entidad completa, pero sin el N+1.
- **`JOIN FETCH`** (la pista del Lab 04, que hoy se cobra) — lo mismo, en JPQL explícito.
- **Proyección** — la más quirúrgica: no cargas la entidad en absoluto. Un
  `SELECT new TramiteResumenDto(t.id, t.tipo, ..., c.rut)` trae SOLO las columnas que la tabla
  pinta, en una consulta. No hay relaciones que iterar, así que no hay N+1 posible.

Para un **listado** —donde pintas pocas columnas de muchas filas— la proyección gana: menos
datos por la red, menos memoria, cero riesgo de N+1. Para un **detalle** —donde navegas el
árbol de una entidad— `@EntityGraph`.

---

## 5. Paginación, y no filtrar la estructura interna

Un listado se pagina: `?page=0&size=20`. Spring Data devuelve un `Page`. Pero **no lo
devuelvas crudo** por la API: su forma (`pageable`, `sort`, `content`…) es un detalle de la
librería, y quien consume tu API quedaría atado a ella. Expón un contrato propio —`PaginaDto`
con `contenido`, `totalElementos`, `totalPaginas`— que puedas cambiar por dentro sin romper a
nadie afuera.

---

## 6. M6 · La pirámide de pruebas

Muchos tests **unitarios** (rápidos, sin contexto: la lógica pura). Menos de **integración**
(un slice, o el contexto: que las piezas encajan). Pocos de **punta a punta** (todo el
sistema por HTTP). La pirámide: ancha abajo, angosta arriba. Invertirla —todo E2E— da una
suite lenta y frágil.

Hoy escribes tu primera de punta a punta: servidor real, base real, cliente HTTP real.

---

## 7. M6 · La mentira del dialecto H2 (con el caso de este repo)

Muchos cursos te dicen: *"para los tests, usa H2 en memoria, es como PostgreSQL pero rápido"*.
Es mentira, y este repo es la prueba viva.

Nuestras migraciones Flyway son **PostgreSQL puro**: `BIGSERIAL`, `CHECK`, tipos y sintaxis que
H2 no replica sin un "modo de compatibilidad" que *finge* ser PostgreSQL y falla en los bordes.
Si intentaras correr estos tests con H2, la migración `V1` **ni siquiera arrancaría**.

Un test que corre contra un motor distinto del de producción prueba algo que no vas a
desplegar. La base de tus tests debe ser **la misma** que la de producción. Para eso:
Testcontainers.

---

## 8. M6 · Testcontainers, `@ServiceConnection`, RestTestClient

- **Testcontainers** levanta un PostgreSQL real, en Docker, desde el propio test. Vive y muere
  con la prueba.
- **Testcontainers 2** renombró sus coordenadas Maven: `org.testcontainers:postgresql` ya no
  existe; ahora es `testcontainers-postgresql`. La mitad de los tutoriales en línea muestran
  las viejas. (Contenido del módulo.)
- **`@ServiceConnection`** cablea el `DataSource` al contenedor solo: no escribes ninguna URL.
- **`RestTestClient`** (Spring Framework 7) le pega a tu servidor por HTTP, como un cliente
  real. Es lo que usas en `E2` y en tu IT del TODO_2.
- **Estrategia de datos:** cada test siembra lo que necesita y no depende del orden de los
  demás. Un test que asume "ya hay datos" es un test que falla el martes.

---

## 9. M7 (teoría) · El proxy transaccional y `readOnly`

Cuando anotas un método con `@Transactional`, Spring no ejecuta tu código directo: lo envuelve
en un **proxy** que abre la transacción antes y la cierra después. Por eso un `@Transactional`
sobre un método `private`, o llamado desde la misma clase, **no hace nada**: el proxy no se
entera.

`@Transactional(readOnly = true)` en un listado declara "esto no escribe": Hibernate puede
saltarse el control de cambios, y la intención queda escrita. En este lab está aplicado en el
andamio, señalado — no lo tecleas tú. El plato fuerte de M7 (y el proxy en acción) es el Lab 06.

---

## 10. Tabla DO / DON'T · Glosario

| ✅ DO | ❌ DON'T |
|---|---|
| Medir con un contador antes de "optimizar" | Adivinar que "va lento" |
| Proyección para el listado | Cargar la entidad y su árbol para una tabla |
| `@EntityGraph`/`JOIN FETCH` dirigido al caso de uso | EAGER global para "bajar el contador" |
| `PaginaDto` propio | Devolver el `Page` de Spring crudo |
| Testcontainers (el motor de producción) | H2 "porque es rápido" |
| Cada test siembra lo suyo | Depender del orden de los tests |

- **N+1** — 1 consulta para la lista + N para las relaciones de cada fila.
- **Proyección** — traer solo las columnas necesarias, no la entidad.
- **`@EntityGraph`** — cargar relaciones específicas en una consulta.
- **Contador de consultas** — mide el costo en consultas, no en segundos.
- **`@ServiceConnection`** — cablea el DataSource al contenedor de Testcontainers.

---

## 11. Conclusiones y siembra del Módulo 6

Hoy convertiste "va lento" en un número, y ese número en un listado que vuela. Y aprendiste que
lo correcto también se mide: LAZY no era gratis, EAGER tampoco, y la ingeniería es saber qué
compras con cada decisión.

Tienes un listado rápido, y un test que lo prueba y lo defiende.

🌱 **Siembra del Módulo 6 (que abre el M7) — "Concurrencia y transacciones".**

El cronómetro ya no es tu problema. Pero fíjate en algo que has tenido delante todo el tiempo y
no has mirado: el **folio**. Un número que, dice la regla, es *irrepetible*.

La próxima semana, dos contribuyentes van a apretar "emitir" en el mismo milisegundo. Los dos
van a leer "el último folio es el 41". Los dos van a escribir el 42. Y dos declaraciones
distintas van a salir con el **mismo folio**.

No es un bug de rendimiento. Es un bug que no puedes ver con un cronómetro, porque solo aparece
cuando dos cosas pasan **a la vez**. El cronómetro se cambia por un fiscalizador.

> *«Un folio emitido dos veces no se borra. Se explica. Ante un fiscalizador.»* — Carolina te lo
> dijo el primer día. La próxima semana entiendes por qué.

El Módulo 6 se llama *«Dos folios, un número»*.
