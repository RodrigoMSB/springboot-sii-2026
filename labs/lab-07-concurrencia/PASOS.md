# Pasos · Lab 07 · Concurrencia

Un paso 0 de teoría corta y cinco pasos de laboratorio. Se construye en `practica/`, en vivo.

```bash
cd practica
./mvnw spring-boot:run
```

**Se queda corriendo**: se apaga con **Ctrl+C**. Escucha en el **8091** y su base en el **55438**
(`solucion/`, en el 8092 y el 55439).

Las tres demos están **comentadas** en `Lab07Application`. El andamiaje —lanzar los hilos, contar
repetidos, imprimir el informe— **viene dado**: hoy no se aprende a lanzar hilos.

---

## Paso 0 · Dos palabras que hay que tener claras

**Quince minutos, en la pizarra, sin código.** No se da por sabido nada de esto.

### Un hilo

Un programa normal hace una cosa detrás de otra: la línea 1, la línea 2, la línea 3. Eso es **un
hilo de ejecución**: un hilo del que se va tirando.

Una aplicación web tiene **muchos**. Cuando llegan tres peticiones a la vez, el servidor no las
atiende en fila: dedica **un hilo a cada una** y las tres avanzan al mismo tiempo, cada una por su
lado, sobre el mismo código y la misma base de datos.

> **La imagen que sirve:** una oficina con tres funcionarios atendiendo a tres personas a la vez.
> Cada uno sigue el mismo procedimiento. El procedimiento está bien escrito. Y aun así, si los
> tres van a mirar el mismo cuaderno al mismo tiempo, puede salir mal.

Eso es todo lo que hace falta saber hoy: **el mismo método se está ejecutando varias veces a la
vez, y cada vez va por su cuenta.**

### Una transacción

Una transacción es un **«todo o nada»** con la base de datos.

Dentro de una transacción se pueden hacer varias cosas —leer, insertar, actualizar— y al final
pasa una de dos:

- **Confirma** (*commit*): todos los cambios quedan, de golpe.
- **Revierte** (*rollback*): no queda ninguno, como si nunca hubieran pasado.

No hay término medio. Si algo revienta a la mitad, no queda medio trabajo hecho.

En Spring, eso se dice con una anotación:

```java
@Transactional
public Folio emitirIngenuo(int anio) {
    ...
}
```

Cada llamada a ese método abre su transacción al entrar y la confirma al salir. **Y si veinte
hilos llaman a ese método a la vez, hay veinte transacciones abiertas al mismo tiempo.**

> **La pregunta que abre el laboratorio, y conviene dejarla en el aire:** si cada una va por su
> lado… ¿qué ve cada una de lo que están haciendo las otras?

---

## Paso 1 · Emitir folios, de uno en uno

**Se explica:** la regla es «dentro de un año, no puede haber dos folios con el mismo número». Se
implementa de la forma más natural: mirar cuál fue el último, sumar uno, guardar.

Abrir `servicios/EmisorDeFolios.java` y **leer el método `emitirIngenuo`**, que viene dado:

```java
@Transactional
public Folio emitirIngenuo(int anio) {
    int ultimo = folios.maxNumeroDe(anio).orElse(0);
    return folios.save(new Folio(anio, ultimo + 1));
}
```

Dos líneas. **Léanse buscándoles el error, porque no lo tienen.**

**Se escribe:** la demo 1 — `prepararElAnio()`, un bucle de 10 `emisor.emitirIngenuo(ANIO)`, e
`informe()`.

**Se descomenta:** `demos.deUnoEnUno();`

**En consola:**

```
=== 1 · DE UNO EN UNO · secuencial ===
  año 2026 reiniciado: solo el folio de apertura 2026-0001
  folios en la tabla : 11
  números distintos  : 11
  REPETIDOS          : ninguno
  emitidos: [2026-0001, 2026-0002, 2026-0003, ... , 2026-0011]
```

**Once folios, once números distintos, ni un repetido.** Correr otra vez, y otra. Siempre igual.

En este punto cualquiera firmaría que ese método está bien. Y de uno en uno, lo está.

---

## Paso 2 · El crimen

**Se explica:** lo mismo, pero como pasa en producción: **veinte a la vez**. El andamiaje ya está
hecho (`enParalelo`), y hace algo importante: los veinte hilos esperan en una barrera y **salen
todos juntos**, para que la carrera ocurra de verdad y no por casualidad.

**Se escribe:** la demo 2 — `prepararElAnio()`, `enParalelo(i -> emisor.emitirIngenuo(ANIO))`, e
`informe()`. **El mismo método del paso 1.** No se cambia ni una letra.

**Se descomenta:** `demos.elCrimen();`

**En consola:**

```
=== 2 · EL CRIMEN · 20 emisiones a la vez, sin candado ===
  año 2026 reiniciado: solo el folio de apertura 2026-0001
  folios en la tabla : 21
  números distintos  : 9
  REPETIDOS          : [2026-0002 (x4), 2026-0003 (x3), 2026-0004 (x2), 2026-0005 (x2),
                        2026-0006 (x2), 2026-0007 (x2), 2026-0008 (x2), 2026-0009 (x3)]
  emitidos: [2026-0001, 2026-0002, 2026-0002, 2026-0002, 2026-0002, 2026-0003, ...]
```

**Veintiún folios y nueve números.** `2026-0002` se emitió **cuatro veces**.

> Los números exactos cambian en cada corrida — es una carrera, y las carreras no se repiten
> iguales. Lo que no cambia es que **siempre** hay repetidos.

**La explicación, despacio.** Dos hilos, A y B, corriendo el mismo método a la vez:

```
  A: lee el último  ->  1
  B: lee el último  ->  1        (A todavía no ha guardado)
  A: guarda el 2
  B: guarda el 2                 <-- el mismo número
```

La rendija está **entre la primera línea y la segunda**. Dura microsegundos y sobra: la
transacción de A no había confirmado cuando B leyó, así que B no vio nada de lo suyo.

**Lo que hay que decir en voz alta:** el código no cambió. Sigue siendo el mismo del paso 1, el
que todos firmaron. **Lo único que cambió es cuántos a la vez.** Por eso este tipo de error no se
encuentra revisando código, ni con pruebas normales, ni probando a mano: se encuentra corriéndolo
en paralelo, o en producción un martes cualquiera.

---

## Paso 3 · La trampa de `synchronized`

**Se explica.** No se escribe nada; son tres minutos y ahorran un disgusto.

Quien haya visto algo de Java propondrá `synchronized`: poner un cerrojo en el método para que
solo entre un hilo a la vez.

```java
public synchronized Folio emitirIngenuo(int anio) { ... }   // NO
```

Y funciona… **en una máquina**. `synchronized` es un cerrojo **dentro de un proceso Java**: no
sabe que existen otras copias de la aplicación.

En cuanto haya dos instancias —y las hay siempre: para poder actualizar sin cortar el servicio,
para aguantar carga, porque el servidor se reinicia— hay **dos cerrojos independientes**, cada uno
dejando pasar a un hilo. Dos a la vez. El problema vuelve, y vuelve peor: ahora también es más
difícil de reproducir.

> **La regla:** si el dato está compartido en la base, **el cerrojo tiene que estar en la base.**

---

## Paso 4 · El candado que sí sirve

**Se explica:** se le pide a PostgreSQL que bloquee una fila: el primero que llega se la lleva, y
los demás **esperan ahí** hasta que la suelte. Se hace con `@Lock(LockModeType.PESSIMISTIC_WRITE)`.

¿Qué fila? La del **folio de apertura del año**, el número 1, que siempre existe. Y esto no es un
detalle: **un bloqueo pesimista bloquea filas**. Si la fila no existe, no hay nada que bloquear y
no protege nada.

**Se escribe:** en `repositories/FolioRepository.java`:

```java
@Lock(LockModeType.PESSIMISTIC_WRITE)
@Query("select f from Folio f where f.anio = :anio and f.numero = 1")
Optional<Folio> bloquearLaApertura(@Param("anio") int anio);
```

en `servicios/EmisorDeFolios.java`, el método nuevo:

```java
@Transactional
public Folio emitirConCandado(int anio) {
    folios.bloquearLaApertura(anio)
            .orElseThrow(() -> new IllegalStateException(
                    "El año " + anio + " no tiene folio de apertura: no hay nada que bloquear."));

    int ultimo = folios.maxNumeroDe(anio).orElse(0);
    return folios.save(new Folio(anio, ultimo + 1));
}
```

y la demo 3, igual que la 2 pero llamando a `emitirConCandado`.

**El orden importa:** primero el candado, **después** leer el máximo. Al revés no serviría de
nada, porque se leería antes de tener el turno.

**Se descomenta:** `demos.conCandado();`

**En consola:**

```
=== 3 · CON CANDADO · 20 a la vez, con bloqueo pesimista ===
  folios en la tabla : 21
  números distintos  : 21
  REPETIDOS          : ninguno
  emitidos: [2026-0001, 2026-0002, ... , 2026-0021]
```

**Veintiuno de veintiuno.** Los hilos siguen saliendo todos juntos; lo que cambia es que ahora
hacen cola dentro de la base.

**Y hay que mirar el SQL**, que está encendido en este laboratorio:

```
Hibernate:
    select
        f1_0.id, f1_0.anio, f1_0.numero
    from
        folio f1_0
    where
        f1_0.anio=?
        and f1_0.numero=1
    for
        no key update of f1_0
```

Esas dos últimas líneas son el candado.

> **Ojo con el nombre.** Este bloqueo se conoce en todas partes como `SELECT ... FOR UPDATE`, y
> así se llama en la documentación de JPA. **PostgreSQL lo escribe `for no key update`**, que es
> una variante más fina —bloquea la fila para actualizarla sin tocar sus claves—. Buscando `for
> update` en la consola **no se encuentra nada**, y no es que falle: es que se llama de otra
> manera.

**La pregunta del paso:** ¿esto es más lento? Sí, los hilos hacen cola. ¿Comparado con repartir el
mismo folio a cuatro contribuyentes?

---

## Paso 5 · El cinturón

**Se explica:** el candado del paso 4 vive **en el código**, y protege mientras todas las
emisiones pasen por ese método. ¿Y si mañana alguien escribe otro método? ¿O un script de carga?
¿O alguien con un cliente SQL abierto?

La segunda defensa vive **en la base**, y no se le puede escapar nadie.

**Se escribe:** una migración nueva,
`practica/src/main/resources/db/migration/V2__folio_unico_por_anio.sql`:

```sql
-- Antes de poner la restricción hay que limpiar: la demo 2 ya dejó folios
-- repetidos, y PostgreSQL no crea una restricción que los datos existentes ya
-- incumplen. Se queda el primero de cada (anio, numero).
delete from folio f
where f.id > (select min(f2.id)
              from folio f2
              where f2.anio = f.anio
                and f2.numero = f.numero);

alter table folio
    add constraint folio_anio_numero_unico unique (anio, numero);
```

**Ese `delete` de arriba es la mitad de la lección del paso**, y conviene pararse en él: la
restricción que faltaba **no se puede poner** hasta haber limpiado a mano lo que se coló sin ella.
Así es exactamente como duele en un sistema real, donde esas filas no son de mentira.

**En consola:** reiniciar y mirar la demo 2, que **no se ha tocado**:

```
=== 2 · EL CRIMEN · 20 emisiones a la vez, sin candado ===
  folios en la tabla : 11
  números distintos  : 11
  REPETIDOS          : ninguno
  rechazados por la base : 10
```

**Cambió el síntoma.** Ya no hay folios repetidos: ahora hay **diez peticiones que fallaron**.

Y esto hay que dejarlo claro para que nadie se lleve la idea equivocada: **la restricción no
arregla la carrera**. La carrera sigue ocurriendo exactamente igual — diez hilos calcularon un
número que ya estaba tomado. Lo que hace la restricción es **impedir que el daño llegue a la
tabla**, convirtiendo un dato corrupto silencioso en un error ruidoso.

| | qué hace | qué NO hace |
|---|---|---|
| **El candado** (paso 4) | Evita la carrera: nadie calcula un número tomado | No protege de código que no lo use |
| **La restricción** (paso 5) | Impide que un duplicado entre, venga de donde venga | No evita que la petición falle |

**Hacen falta las dos.** Y en la demo 3, con el candado puesto, no hay ni repetidos ni rechazos:
es el candado el que hace que la restricción nunca tenga que intervenir.

---

## Al terminar

`practica/` da los mismos resultados que `solucion/`: sin repetidos en las demos 1 y 3, y con
rechazos en la 2. **Los números exactos de la demo 2 varían en cada corrida** — es una carrera.

Lo que hay que poder decir con las propias palabras:

> Un código correcto de uno en uno puede ser incorrecto en paralelo, y la diferencia no se ve
> leyéndolo. Si el dato está en la base, el cerrojo va en la base. Y una restricción no arregla la
> carrera: solo evita que el daño se guarde.

### Lo que siembra este lab

El que se lleva de aquí es un método de trabajo, no una anotación:

> **La corrección bajo concurrencia no se prueba mirando el código. Se prueba corriéndolo en
> paralelo.**

El error del paso 2 no se habría encontrado leyendo, ni con una prueba normal, ni probando a mano
—una persona no puede pulsar el botón veinte veces en el mismo milisegundo—. Hizo falta escribir
algo que lo hiciera a propósito.

Ese es el hilo del que tira el resto del curso: cada laboratorio que viene tiene un arnés que
provoca el fallo antes de que lo provoque un usuario.
