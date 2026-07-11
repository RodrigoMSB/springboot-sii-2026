# Teoría · Módulo 7 (transacciones y concurrencia) + Módulo 8 (Flyway a fondo)

## Índice

1. [El crimen: una carrera por el mismo número](#1-el-crimen-una-carrera-por-el-mismo-número)
2. [El proxy transaccional (y la autoinvocación que no transacciona)](#2-el-proxy-transaccional-y-la-autoinvocación-que-no-transacciona)
3. [Aislamiento y sus anomalías](#3-aislamiento-y-sus-anomalías)
4. [Bloqueo pesimista vs optimista (`@Version`) — cuándo cada uno](#4-bloqueo-pesimista-vs-optimista-version--cuándo-cada-uno)
5. [Acto 2a · `synchronized`: el candado en el lugar equivocado](#5-acto-2a--synchronized-el-candado-en-el-lugar-equivocado)
6. [Acto 2b · `REQUIRES_NEW`: el número que se gasta](#6-acto-2b--requires_new-el-número-que-se-gasta)
7. [Propagación, `rollbackFor` y `readOnly`](#7-propagación-rollbackfor-y-readonly)
8. [Acto 3 · La forma correcta: bloqueo dirigido, en la misma transacción](#8-acto-3--la-forma-correcta-bloqueo-dirigido-en-la-misma-transacción)
9. [La idempotencia (RN-05)](#9-la-idempotencia-rn-05)
10. [M8 · Flyway a fondo](#10-m8--flyway-a-fondo)
11. [M8 · Restricciones como contratos (y por qué el `CHECK` no es redundante)](#11-m8--restricciones-como-contratos-y-por-qué-el-check-no-es-redundante)
12. [Tabla DO / DON'T · Glosario](#12-tabla-do--dont--glosario)
13. [Conclusiones y siembra del Módulo 8](#13-conclusiones-y-siembra-del-módulo-8)

---

## 1. El crimen: una carrera por el mismo número

La emisión ingenua hace tres cosas: **lee** el contador (`el último folio es el 41`),
**suma uno** (42), **guarda** el folio. Con un usuario, impecable.

Con dos a la vez, el desastre. Los dos leen 41. Los dos calculan 42. Los dos intentan
escribir el folio 42. Y ahí pasa una de dos:

- Si no hubiera restricción, tendrías **dos folios 42**: el duplicado que Carolina no
  puede explicar ante un fiscalizador (RN-01 violada).
- Como la PK del folio **sí** existe, la base rechaza el segundo: una emisión **válida**
  se cae con `duplicate key`. El contribuyente apretó «emitir» y recibió un 500. Y el
  contador, si además hubo *lost update*, quedó descuadrado: un **salto** en el libro
  (RN-02 violada).

Ninguna de las dos es aceptable. Este bug no se ve con un cronómetro: solo aparece cuando
dos cosas pasan **a la vez**, y por eso se prueba con concurrencia real, no leyendo el
código.

---

## 2. El proxy transaccional (y la autoinvocación que no transacciona)

Cuando anotas un método con `@Transactional`, Spring no llama tu código directo: lo
envuelve en un **proxy** que abre la transacción antes y la confirma (o revierte) después.

Dos consecuencias que muerden:

1. Un `@Transactional` sobre un método **`private`**, o **llamado desde la misma clase**
   (`this.emitir(...)`), no hace nada: la llamada no pasa por el proxy, así que nadie
   abre la transacción. Es el error más silencioso del framework.
2. Un candado pesimista (`SELECT … FOR UPDATE`) solo tiene sentido **dentro** de una
   transacción: el lock se libera cuando la transacción cierra. Si tu método de emisión
   no es transaccional, el `FOR UPDATE` bloquea… y suelta el lock en el mismo suspiro,
   antes de que escribas el folio. Por eso, en este lab, **el bloqueo (TODO_1) y la
   transacción (TODO_4) son inseparables**.

---

## 3. Aislamiento y sus anomalías

El nivel de aislamiento decide qué ve una transacción de lo que otras están haciendo.
PostgreSQL, por defecto, corre en **READ COMMITTED**: cada sentencia ve lo ya confirmado,
pero dos lecturas seguidas pueden diferir. Las anomalías clásicas:

- **Dirty read** — leer lo que otra transacción escribió y aún no confirmó. READ
  COMMITTED ya lo evita.
- **Non-repeatable read** — releer una fila y encontrarla cambiada.
- **Lost update** — dos transacciones leen el mismo valor, ambas lo pisan, y una
  actualización se pierde. **Es exactamente nuestro contador.**

Subir el aislamiento (REPEATABLE READ, SERIALIZABLE) mata más anomalías, pero cuesta:
más conflictos, más reintentos, menos concurrencia. La alternativa quirúrgica no es subir
el aislamiento de *todo*, es **bloquear el dato que importa**. Eso es el acto 3.

---

## 4. Bloqueo pesimista vs optimista (`@Version`) — cuándo cada uno

Dos estrategias para que dos no pisen el mismo dato:

- **Pesimista** (`PESSIMISTIC_WRITE`, `SELECT … FOR UPDATE`): «cierro la fila con llave
  antes de tocarla; el que llegue espera». Serializa de verdad. Es lo correcto cuando el
  conflicto es **frecuente** y el recurso es **puntual y caliente** — como un contador
  que TODA emisión toca.
- **Optimista** (`@Version`): «no bloqueo nada; al guardar, comparo la versión que leí
  con la actual, y si cambió, reviento con `OptimisticLockException` y reintento». Cuesta
  poco cuando el conflicto es **raro**. Es lo correcto para editar entidades que rara vez
  chocan (una ficha, un formulario).

Para el contador de folios el conflicto no es raro: es el punto por el que pasa todo el
mundo. Ahí gana el pesimista. Para el resto del dominio, si algún día hay ediciones
concurrentes, `@Version` es más barato. La ingeniería es saber cuál compras.

---

## 5. Acto 2a · `synchronized`: el candado en el lugar equivocado

El primer parche que se le ocurre a cualquiera: `synchronized` en el método de emisión.
Y **funciona** — el test de concurrencia pasa. En clase lo vimos en verde.

Pero funciona **en UNA JVM**. `synchronized` es un candado del *proceso*: dos hilos de la
misma JVM lo respetan. El Lab 10 va a correr **dos instancias** de la DGT detrás de un
balanceador, y la JVM de la izquierda no comparte monitor con la de la derecha. Cada una
cree que tiene el candado. Vuelve el duplicado.

El candado está en el **código**. Tiene que estar en el **dato** — donde todas las
instancias lo ven: la base de datos.

---

## 6. Acto 2b · `REQUIRES_NEW`: el número que se gasta

El segundo parche parece elegante: «aíslo la toma del número en su propia transacción»,
con `@Transactional(propagation = REQUIRES_NEW)`. Suena a orden.

Y crea un bug peor. `REQUIRES_NEW` **suspende** la transacción externa y abre una nueva,
que **confirma sola**. Así que el número queda tomado y confirmado *antes* de que termine
la emisión. Si luego algo falla y la transacción externa revierte, el folio desaparece…
pero **el número ya se gastó**. El contador quedó en 42, y no hay folio 42. Un salto en el
libro foliado. RN-02 violada, otra vez — ahora por "elegancia".

El test `E4` lo demuestra sin piedad: con `REQUIRES_NEW`, el contador esperaba volver a 1
y quedó en 2. `expected: 1L but was: 2L`. El número, gastado.

La lección: la toma del número y la escritura del folio son **un solo hecho**. Van en la
**misma** transacción, o no van.

---

## 7. Propagación, `rollbackFor` y `readOnly`

- **Propagación** — qué hace un `@Transactional` cuando ya hay una transacción abierta.
  `REQUIRED` (el default) se **une** a la existente: es lo que queremos casi siempre.
  `REQUIRES_NEW` abre una nueva y suspende la de afuera (el acto 2b, con su costo).
- **`rollbackFor`** — por defecto Spring revierte ante `RuntimeException` y `Error`, pero
  **no** ante excepciones *checked*. Si una checked debe revertir, se declara:
  `@Transactional(rollbackFor = IOException.class)`. Un rollback que no ocurre porque la
  excepción era checked es otro bug silencioso.
- **`readOnly = true`** — declara que la transacción no escribe. Hibernate se ahorra el
  control de cambios, y la intención queda escrita. Es lo que llevan los listados del Lab
  05.

---

## 8. Acto 3 · La forma correcta: bloqueo dirigido, en la misma transacción

```java
@Transactional
public ResultadoEmision emitir(Long tramiteId) {
    // ... idempotencia (RN-05) ...
    ContadorFolio c = contadores.tomarConBloqueo().orElseThrow(); // SELECT ... FOR UPDATE
    long numero = c.siguiente();
    return ResultadoEmision.nuevo(FolioDto.de(folios.save(new Folio(numero, tramite))));
}
```

El `tomarConBloqueo()` es una consulta con `@Lock(PESSIMISTIC_WRITE)`. El primer emisor
cierra la fila del contador; los demás **esperan** a que confirme, y recién entonces leen
el valor ya actualizado. Se serializan sobre el dato caliente, y solo sobre él: el resto
de la app sigue concurrente. El candado vive en el dato, dentro de la transacción que
persiste el folio. Y detrás, el `UNIQUE` de la V1 como **red final**: defensa en
profundidad — si el código algún día falla, la base no perdona.

---

## 9. La idempotencia (RN-05)

Un cliente reintenta: la red se cortó, no supo si su «emitir» llegó. Si tu API crea un
folio en cada llamada, el reintento genera un segundo. RN-05 dice: **mismo `tramiteId`,
mismo folio**. La primera vez se crea (201); el reintento devuelve el mismo (200), sin
crear nada.

En el código: antes de tomar un número, `folios.findByTramiteId(id)` — si ya hay folio,
se devuelve. Y como suelo, el `UNIQUE (tramite_id)` de la V1: si dos reintentos corren
*a la vez* y ambos pasan la comprobación, la base rechaza al segundo. El adorno de la V1
resultó ser un contrato.

---

## 10. M8 · Flyway a fondo

Flyway versiona el esquema. Tres tipos de migración:

- **Versionadas** (`V1__…`, `V2__…`, `V3__…`) — se aplican **una vez**, en orden, y no se
  vuelven a tocar. Son la historia del esquema.
- **Repetibles** (`R__…`) — se re-aplican cada vez que cambia su contenido (vistas,
  funciones, datos de referencia). No llevan versión.
- **Baseline** — la línea de partida para adoptar Flyway sobre una base que ya existía.

La tabla **`flyway_schema_history`** es una **bitácora inmutable**: cada migración
aplicada, su checksum, cuándo y por quién. Si editas una migración YA aplicada, el
checksum no cuadra y Flyway se planta. No es burocracia: es la misma idea que nuestra
gobernanza de SPECs —*una migración es inmutable desde que se aplica*—, y por la misma
razón: el pasado no se reescribe, se corrige con un paso nuevo hacia adelante. Por eso la
V3 no toca la V1: **agrega**.

**Liquibase**, comparado, hace lo mismo con changelogs en XML/YAML/JSON y ofrece
*rollback declarativo* (defines cómo deshacer cada cambio). Flyway apuesta por SQL plano
y migraciones hacia adelante. Con criterio: Liquibase da portabilidad entre motores y
rollback declarado; Flyway da simplicidad y SQL que cualquiera lee. Para un equipo casado
con PostgreSQL, el SQL plano de Flyway suele ganar en claridad.

---

## 11. M8 · Restricciones como contratos (y por qué el `CHECK` no es redundante)

La V1 dejó una ausencia **deliberada**: `linea_f29.monto` sin restricción. La V3 la pone,
con una migración correctiva — como se agregan contratos a una tabla que ya vive en
producción, con datos dentro.

El contrato **no** es `monto >= 0`. En este dominio los créditos SON negativos: lo dice
`LineaF29`, lo prueba `Formulario29TotalTest`, lo siembra la V2. Prohibir el negativo
rompería la mitad del F29. El contrato correcto es **`CHECK (monto <> 0)`**: ninguna línea
vale cero. Una línea de monto cero no dice nada — es ruido, casi siempre un error de
carga.

> **La pregunta de criterio:** *si un día pusieras validación en Java para el monto, ¿el
> `CHECK` no sería redundante?* No. La validación de Java protege lo que **entra por la
> API**. Pero la tabla la tocan también los scripts de migración, las cargas masivas, el
> `psql` de madrugada de un DBA apurado. La última línea de defensa no vive en el
> framework: vive en el **motor**. El `CHECK` es lo que hace que el dato malo sea
> imposible *venga de donde venga* — y eso no es redundante con una validación que solo
> mira una de las puertas.

El test `E3` lo prueba insertando una línea de monto cero **por JDBC crudo**, saltándose
todo Java. La base la rechaza con `ck_linea_f29_monto_no_cero`. Si borras la V3, la base
la acepta, y `E3` se pone rojo: el contrato no existía.

---

## 12. Tabla DO / DON'T · Glosario

| ✅ DO | ❌ DON'T |
|---|---|
| Bloquear el **dato** caliente (`FOR UPDATE`) | Bloquear el **código** (`synchronized`) |
| Tomar el número y escribir el folio en **una** transacción | "Aislar" el número con `REQUIRES_NEW` |
| Idempotencia por `tramiteId` (200 en el reintento) | Crear un folio nuevo en cada llamada |
| Restricción en la base, como contrato | Confiar solo en la validación de Java |
| Probar con concurrencia real (hilos + latch) | Probar la emisión con un solo hilo y creerle |
| `V3` que **agrega** | Editar la `V1` ya aplicada |

- **Bloqueo pesimista** — cerrar la fila con llave antes de tocarla (`FOR UPDATE`).
- **Bloqueo optimista** — no cerrar; al guardar, comparar versión (`@Version`) y reintentar.
- **Lost update** — dos leen el mismo valor, ambos lo pisan, una escritura se pierde.
- **Propagación `REQUIRES_NEW`** — suspende la transacción externa y abre otra que confirma sola.
- **Idempotencia** — repetir la operación da el mismo resultado, sin efectos nuevos.
- **`flyway_schema_history`** — bitácora inmutable de las migraciones aplicadas.

---

## 13. Conclusiones y siembra del Módulo 8

Hoy aprendiste que la corrección bajo concurrencia no se teclea con más código: se
consigue poniendo el candado en el lugar correcto —el dato, no el código— y en el momento
correcto —la misma transacción—. Y que una restricción en la base no es burocracia: es la
última línea de defensa, la que sostiene la regla cuando el framework no está mirando.

Tienes folios únicos, secuenciales e idempotentes, y cuatro tests que lo prueban y lo
defienden.

🌱 **Siembra del Módulo 8 (que abre el M9) — "La puerta con portero".**

Los folios ya no se repiten, no se saltan, y un reintento no crea basura. Hay un solo
problema, y es grande: **cualquiera con `curl` puede emitirlos.** No hay quién pregunte
*«¿y usted quién es?»* en la puerta.

La próxima semana, la puerta tiene portero: autenticación y autorización. Valentina puede
ver sus trámites; no los de Comercial Andina. Carolina, funcionaria, puede más; Ignacio,
fiscalizador, otra cosa. Y nada de eso puede vivir en un `if` perdido en un controlador.

El Módulo 9 se llama *«La puerta con portero»*.
