# Pasos · Lab 07 · Concurrencia

Un paso 0 de teoría corta y cinco pasos de laboratorio. Se construye en `practica/`, en vivo.

```bash
cd practica
./mvnw spring-boot:run
```

**Se queda corriendo**: se apaga con **Ctrl+C**. Escucha en el **8091** y su base en el **55438**
(`solucion/`, en el 8092 y el 55439).

En `practica/` el `CommandLineRunner` de `Lab07Application` llega **vacío**. El andamiaje —lanzar los hilos, contar
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

Abrir `services/EmisorDeFolios.java` y **leer el método `emitirIngenuo`**, que viene dado:

```java
@Transactional
public Folio emitirIngenuo(int anio) {
    int ultimo = folios.maxNumeroDe(anio).orElse(0);
    return folios.save(new Folio(anio, ultimo + 1));
}
```

Dos líneas. **Léanse buscándoles el error, porque no lo tienen.**

**Se pega:** en `practica/src/main/java/cl/dgt/concurrencia/demos/DemosConcurrencia.java`, **reemplazando el método `deUnoEnUno()` entero**.

```java
    public void deUnoEnUno() {
        seccion(1, "DE UNO EN UNO · secuencial");

        prepararElAnio();
        for (int i = 0; i < 10; i++) {
            emisor.emitirIngenuo(ANIO);
        }
        informe();
    }
```

**Se agrega al runner:** en `Lab07Application.java`, dentro de `return args -> {`:

```java
            demos.deUnoEnUno();
```

**En consola:**

```
=== 1 · DE UNO EN UNO · secuencial ===
  año 2026 reiniciado: solo el folio 2026-0001
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

**Se pega:** en `practica/src/main/java/cl/dgt/concurrencia/demos/DemosConcurrencia.java`, **reemplazando el método `elCrimen()` entero**. Es la demo 1 con una sola
diferencia: las diez emisiones salen **a la vez**.

```java
    public void elCrimen() {
        seccion(2, "EL CRIMEN · " + EN_PARALELO + " emisiones a la vez, sin protección");

        prepararElAnio();
        enParalelo(i -> emisor.emitirIngenuo(ANIO));
        informe();
    }
```

**Se agrega al runner:** en `Lab07Application.java`, dentro de `return args -> {`:

```java
            demos.elCrimen();
```

**En consola:**

```
=== 2 · EL CRIMEN · 20 emisiones a la vez, sin protección ===
  año 2026 reiniciado: solo el folio 2026-0001
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

## Paso 4 · El turno con nombre

**Se explica:** hace falta que los veinte hilos hagan **cola**, y que la cola la gestione la base,
no Java. PostgreSQL tiene justo eso: un **lock con nombre**, que se pide con
`pg_advisory_xact_lock(n)`.

Lo que hay que entender del mecanismo cabe en tres frases:

- Se pide **un nombre**, no una fila. Aquí el nombre es el número del año: `2026`.
- El primero que lo pide se lo lleva; **los demás esperan ahí** hasta que él termine.
- Se suelta **solo**, cuando la transacción confirma o aborta — eso es el `xact` de su nombre.
  Nadie tiene que acordarse de soltarlo.

**Lo importante, y es lo que distingue este paso:** **no hay ninguna fila que bloquear.** El turno
existe porque alguien lo pide, no porque haya un dato que lo represente.

**El orden importa:** primero el turno, **después** leer el máximo. Al revés no serviría de nada,
porque se leería antes de tener el turno.

**Se pega (1 de 3):** en `practica/src/main/java/cl/dgt/concurrencia/repositories/FolioRepository.java`, **dentro de la interfaz**.

```java
    @Query(value = "select pg_advisory_xact_lock(:anio)", nativeQuery = true)
    Object tomarElTurnoDelAnio(@Param("anio") long anio);
```

> **Dos rarezas de esa firma, y las dos tienen respuesta.** Devuelve `Object` y no `void` porque es
> un `select` —no lleva `@Modifying`, que es para `update` y `delete`— y Spring Data necesita un
> tipo de retorno para ejecutarlo como consulta; el valor se ignora. Y el parámetro es `long` y no
> `int` porque `pg_advisory_xact_lock` tiene dos formas —una que toma un `bigint` y otra que toma
> dos `int`—, y con `long` se elige la primera sin ambigüedad.

**Se pega (2 de 3):** en `practica/src/main/java/cl/dgt/concurrencia/services/EmisorDeFolios.java`, **arriba**, con los imports.

```java
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.atomic.AtomicBoolean;
```

**Se pega:** en el mismo archivo, **entre los campos**, encima de `private final FolioRepository folios;`.

```java
    private static final Logger log = LoggerFactory.getLogger(EmisorDeFolios.class);

    // Una sola línea por proceso: con veinte hilos, veinte líneas iguales no enseñan nada.
    private final AtomicBoolean turnoAnunciado = new AtomicBoolean();
```

**Se pega:** en el mismo archivo, **antes de la llave que cierra la clase** — es un método nuevo,
no reemplaza a ninguno.

```java
    @Transactional
    public Folio emitirConTurno(int anio) {
        folios.tomarElTurnoDelAnio(anio);

        if (turnoAnunciado.compareAndSet(false, true)) {
            log.info("[TURNO] pg_advisory_xact_lock({}) · el turno vive en la base, no en Java", anio);
        }

        int ultimo = folios.maxNumeroDe(anio).orElse(0);
        return folios.save(new Folio(anio, ultimo + 1));
    }
```

> **El `AtomicBoolean` es sólo para la demo**, y conviene decirlo: sin él, los veinte hilos
> imprimirían la misma línea veinte veces y taparían el informe. `compareAndSet` es atómico, así
> que con veinte hilos entrando a la vez la línea sale **exactamente una**.

**Se pega (3 de 3):** en `practica/src/main/java/cl/dgt/concurrencia/demos/DemosConcurrencia.java`, **dentro del método `conTurno()`**, donde
dice `// escribe aquí`. Es la demo 2 llamando a `emitirConTurno` en vez de a `emitirIngenuo`.

```java
        prepararElAnio();
        enParalelo(i -> emisor.emitirConTurno(ANIO));
        informe();
```

**Se agrega al runner:** en `practica/src/main/java/cl/dgt/concurrencia/Lab07Application.java`, dentro de `return args -> {`:

```java
            demos.conTurno();
```

**En consola:**

```
=== 3 · CON TURNO · 20 a la vez, con un lock con nombre ===
  año 2026 reiniciado: solo el folio 2026-0001
  folios en la tabla : 21
  números distintos  : 21
  REPETIDOS          : ninguno
  rechazados por la base : 0
  emitidos: [2026-0001, 2026-0002, ... , 2026-0021]
```

**Veintiuno de veintiuno.** Los hilos siguen saliendo todos juntos; lo que cambia es que ahora
hacen cola dentro de la base.

**Y hay que mirar el SQL**, que está encendido en este laboratorio:

```
Hibernate:
    select
        pg_advisory_xact_lock(?)
```

**Eso es todo.** Una línea, ninguna tabla nombrada, ningún `for update`. Buscar `for update` en la
consola de este laboratorio **no encuentra nada**, y no es que falle: es que no se está bloqueando
ninguna fila.

> **La frase del paso, y conviene decirla despacio:** este turno **no es un `synchronized`**. Vive
> en la base, así que la cola funciona igual entre veinte hilos de una JVM, entre dos procesos y
> entre dos máquinas. Es exactamente lo que le va a faltar al Lab 12 cuando descubra que
> `@Scheduled` se dispara en las dos instancias a la vez.

**La pregunta del paso:** ¿esto es más lento? Sí, los hilos hacen cola. ¿Comparado con repartir el
mismo folio a cuatro contribuyentes?

---

## Paso 5 · El cinturón

**Se explica:** el turno del paso 4 vive **en el código**, y protege mientras todas las
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
=== 2 · EL CRIMEN · 20 emisiones a la vez, sin protección ===
  folios en la tabla : 11
  números distintos  : 11
  REPETIDOS          : ninguno
  rechazados por la base : 10
  y los rechazó diciendo : ERROR: duplicate key value violates unique constraint "folio_anio_numero_unico"
```

**Cambió el síntoma.** Ya no hay folios repetidos: ahora hay **diez peticiones que fallaron**, y la
base dice exactamente por qué.

> Ésa es también la salida que da `solucion/` desde el primer arranque, porque allí la V2 ya está
> puesta. Si alguien compara las dos carpetas y ve números distintos en la demo 2, es esto.

Y esto hay que dejarlo claro para que nadie se lleve la idea equivocada: **la restricción no
arregla la carrera**. La carrera sigue ocurriendo exactamente igual — diez hilos calcularon un
número que ya estaba tomado. Lo que hace la restricción es **impedir que el daño llegue a la
tabla**, convirtiendo un dato corrupto silencioso en un error ruidoso.

| | qué hace | qué NO hace |
|---|---|---|
| **El turno** (paso 4) | Evita la carrera: nadie calcula un número tomado | No protege de código que no lo use |
| **La restricción** (paso 5) | Impide que un duplicado entre, venga de donde venga | No evita que la petición falle |

**Hacen falta las dos.** Y en la demo 3, con el turno puesto, no hay ni repetidos ni rechazos: es
el turno el que hace que la restricción nunca tenga que intervenir.

---

## Al terminar

`practica/` da los mismos resultados que `solucion/`: sin repetidos en las demos 1 y 3, y con
rechazos en la 2. **Los números exactos de la demo 2 varían en cada corrida** — es una carrera.

Lo que hay que poder decir con las propias palabras:

> Un código correcto de uno en uno puede ser incorrecto en paralelo, y la diferencia no se ve
> leyéndolo. Si el dato está en la base, el cerrojo va en la base. Y una restricción no arregla la
> carrera: solo evita que el daño se guarde.

### Lo que queda fuera

Tres defensas más para el mismo invariante. Se nombran porque el día que haga falta una de ellas,
lo primero es saber que existe:

- **`@Lock(PESSIMISTIC_WRITE)`**, el bloqueo pesimista sobre una fila. Es lo correcto cuando **lo
  que se protege es esa fila**: el saldo de una cuenta, el stock de un producto. Hoy no se usó
  porque lo que hay que proteger es un **cálculo sobre toda la tabla** (`max(numero) + 1`), y no
  hay una fila natural que lo represente.
- **`@Version`**, el bloqueo optimista. No hace esperar a nadie: deja pasar a todos y, al
  confirmar, el segundo se encuentra con que la versión cambió y tiene que reintentar. Es mejor
  **cuando los choques son raros**, y peor cuando son la norma — que es el caso de hoy.
- **Secuencias** de PostgreSQL (`nextval`). Atómicas por construcción, no hacen cola y son las más
  rápidas de todas. Su precio: **dejan huecos**, porque una transacción que aborta se lleva su
  número. Se usan cuando esos huecos se toleran; un folio tributario saltado hay que explicarlo.

### Lo que siembra este lab

El que se lleva de aquí es un método de trabajo, no una anotación:

> **La corrección bajo concurrencia no se prueba mirando el código. Se prueba corriéndolo en
> paralelo.**

El error del paso 2 no se habría encontrado leyendo, ni con una prueba normal, ni probando a mano
—una persona no puede pulsar el botón veinte veces en el mismo milisegundo—. Hizo falta escribir
algo que lo hiciera a propósito.

Ese es el hilo del que tira el resto del curso: cada laboratorio que viene tiene un arnés que
provoca el fallo antes de que lo provoque un usuario.


---

## Anexo · ver los folios repetidos en vivo

**Esto es destructivo y opcional.** Borra la base del laboratorio y deshace el paso 5. No hace
falta para nada de la sesión: está aquí por si alguien quiere ver con sus ojos los duplicados que
el README describe, después de haber puesto la restricción.

Se hace **en `practica/`** y con la aplicación parada:

```bash
# 1. quitar la restricción: se borra la migración del paso 5
rm src/main/resources/db/migration/V2__folio_unico_por_anio.sql

# 2. borrar la base entera, porque Flyway ya anotó esa migración como aplicada
#    y no la puede "desaplicar"
rm -rf .datos-pg

# 3. arrancar de nuevo
./mvnw spring-boot:run
```

Ahora la demo 2 vuelve a su forma original:

```
=== 2 · EL CRIMEN · 20 emisiones a la vez, sin protección ===
  folios en la tabla : 21
  números distintos  : 9
  REPETIDOS          : [2026-0002 (x4), 2026-0003 (x3), 2026-0004 (x2), ...]
  rechazados por la base : 0
```

**Ahí está el daño de verdad**: nadie falló, nadie se quejó, y cuatro contribuyentes tienen el
folio `2026-0002`. Es el argumento entero del paso 5 en una pantalla — un dato corrupto en
silencio es peor que un error ruidoso.

Para volver atrás: recuperar la migración con `git checkout -- src/main/resources/db/migration/`
y borrar `.datos-pg` otra vez.
