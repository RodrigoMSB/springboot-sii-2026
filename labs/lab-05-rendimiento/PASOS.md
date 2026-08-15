# Pasos · Lab 05 · Rendimiento

Cinco pasos. Se construyen en `practica/`, en vivo, uno a la vez. Después de cada paso se reinicia
el programa y **se mira el número** antes de seguir.

```bash
cd practica
./mvnw spring-boot:run
```

**Se queda corriendo**: se apaga con **Ctrl+C**. Escucha en el **8089** y su base en el **55436**
(`solucion/`, en el 8090 y el 55437).

La base se siembra sola la primera vez: 200 contribuyentes con 5 trámites cada uno.

> **Hoy no se lee SQL.** Está apagado a propósito (`show-sql: false`): con 201 consultas serían
> mil líneas. Hoy se cuenta, y el contador ya viene hecho en
> `soporte/ContadorDeConsultas.java`.

El molde para medir es siempre el mismo, y se repite en las cinco demos:

```java
contador.reiniciar();
long empezo = System.currentTimeMillis();

// ... lo que se quiere medir ...

System.out.println("  CONSULTAS: " + contador.consultas()
        + "   ·   TIEMPO: " + (System.currentTimeMillis() - empezo) + " ms");
```

---

## Paso 1 · El crimen, medido

**Se explica:** la pantalla es «lista de contribuyentes, con cuántos trámites tiene cada uno». Se
escribe de la forma más natural del mundo: traer todos, y para cada uno mirar su lista.

**Se escribe:** la demo 1. `contribuyentes.findAll()`, recorrer, y sumar `c.getTramites().size()`
de cada uno. Medir con el molde.

**Se descomenta:** `demos.elCrimen();`

**En consola:**

```
=== 1 · EL CRIMEN · findAll() y tocar la relación ===
  200 contribuyentes · 1000 trámites
  CONSULTAS: 201   ·   TIEMPO: 79 ms
```

**201.** Y aquí se para y se descompone el número en voz alta:

- **1** consulta para traer los 200 contribuyentes.
- **200** más: una por cada `c.getTramites()`, porque la relación es LAZY y hasta ese momento
  nadie había pedido los trámites.

`1 + N`. De ahí el nombre: **N+1**.

**Lo que hay que señalar, y es lo que hace peligroso a este problema:** el código está bien
escrito. Es legible, hace lo que dice, y con tres contribuyentes de prueba va instantáneo. El
problema no aparece hasta que hay datos de verdad.

**La pregunta del paso:** ¿en qué línea exacta se disparan las 200? (En la del `for`, no en la del
`findAll`.)

---

## Paso 2 · `JOIN FETCH`

**Se explica:** el problema no es la entidad — es que se pidieron los contribuyentes sin decir que
también harían falta sus trámites. Se puede decir: **en la misma consulta, tráete las dos cosas**.

**Se escribe:** en `repositories/ContribuyenteRepository.java`:

```java
@Query("select distinct c from Contribuyente c left join fetch c.tramites")
List<Contribuyente> conJoinFetch();
```

y la demo 2: **exactamente igual que la 1**, cambiando `findAll()` por `conJoinFetch()`.

El `distinct` hace falta porque el join devuelve el contribuyente repetido una vez por trámite.

**Se descomenta:** `demos.conJoinFetch();`

**En consola:**

```
=== 2 · JOIN FETCH · traerlo todo de una vez ===
  200 contribuyentes · 1000 trámites
  CONSULTAS: 1   ·   TIEMPO: 19 ms
```

**De 201 a 1.** El mismo resultado —200 contribuyentes, 1.000 trámites—, la misma pantalla, el
mismo código en el bucle. **Lo único que cambió es cómo se pidió.**

Y no se tocó la entidad. Eso es lo que hay que subrayar: el arreglo vive en la consulta.

---

## Paso 3 · `@EntityGraph`

**Se explica:** lo mismo, sin escribir JPQL. Se declara un método y se le dice, por anotación, qué
relación tiene que venir cargada.

**Se escribe:** en el repositorio:

```java
@EntityGraph(attributePaths = "tramites")
List<Contribuyente> findAllBy();
```

y la demo 3, igual que las anteriores.

**Se descomenta:** `demos.conEntityGraph();`

**En consola:**

```
=== 3 · @EntityGraph · lo mismo, sin JPQL ===
  200 contribuyentes · 1000 trámites
  CONSULTAS: 1   ·   TIEMPO: 20 ms
```

**El mismo 1.** Entonces, ¿cuál se usa?

| | Cuándo |
|---|---|
| `JOIN FETCH` | La consulta ya es a medida (filtros, orden, varias condiciones). Se ve todo junto en un sitio. |
| `@EntityGraph` | La consulta no tiene nada especial y solo hay que decidir **qué se trae**. Menos que escribir, y no se puede equivocar la sintaxis. |

Los dos son correctos. Lo incorrecto es no elegir ninguno.

---

## Paso 4 · Proyección

**Se explica:** hasta ahora se traen **entidades**: objetos completos, con todas sus columnas, que
Hibernate se queda vigilando por si cambian. ¿Y si la pantalla solo muestra tres datos?

**Se escribe:** el record `dto/ResumenContribuyente.java`:

```java
package cl.dgt.rendimiento.dto;

public record ResumenContribuyente(String rut, String razonSocial, long cuantosTramites) {
}
```

y en el repositorio:

```java
@Query("""
        select new cl.dgt.rendimiento.dto.ResumenContribuyente(c.rut, c.razonSocial, count(t))
        from Contribuyente c
        left join c.tramites t
        group by c.id, c.rut, c.razonSocial
        order by c.id
        """)
List<ResumenContribuyente> resumen();
```

y la demo 4, que suma los `cuantosTramites` e imprime la primera fila.

**Se descomenta:** `demos.conProyeccion();`

**En consola:**

```
=== 4 · PROYECCIÓN · traer solo lo que se muestra ===
  200 contribuyentes · 1000 trámites
  CONSULTAS: 1   ·   TIEMPO: 12 ms
  primera fila -> ResumenContribuyente[rut=71.001.007-1, razonSocial=Contribuyente 001 Ltda., cuantosTramites=5]
```

Una consulta, como el paso 2 — pero **menos datos**: 200 objetos pequeños en vez de 200 entidades
con 1.000 entidades colgando. La cuenta la hizo la base, que es quien mejor sabe contar.

**Lo que se pierde:** estos objetos **no se pueden modificar**. No son entidades, nadie los
vigila, no hay `save()` que valga. Para una pantalla de solo lectura, eso no es una pérdida: es
justamente lo que se quería.

---

## Paso 5 · La trampa

**Se explica:** casi todo el mundo, al ver el N+1 por primera vez, propone lo mismo: «si el
problema es que los trámites no vienen, que vengan siempre». Es decir, `EAGER` en la entidad.

Se va a probar. Y se va a medir, que es distinto de opinar.

**Se escribe:** en `entities/Contribuyente.java`, una palabra:

```java
@OneToMany(mappedBy = "contribuyente", fetch = FetchType.EAGER)
```

**Se descomenta:** `demos.laPantallaQueNoNecesitaTramites();` — si no estaba ya. Es un listado que
solo usa la razón social y **no toca ni un trámite**.

**En consola, antes de tocar nada (LAZY):**

```
=== 1 · EL CRIMEN ===                    CONSULTAS: 201   ·   TIEMPO: 79 ms
=== 5 · LA OTRA PANTALLA ===             CONSULTAS: 1     ·   TIEMPO: 2 ms
```

**Y con `EAGER`:**

```
=== 1 · EL CRIMEN ===                    CONSULTAS: 201   ·   TIEMPO: 145 ms
=== 5 · LA OTRA PANTALLA ===             CONSULTAS: 201   ·   TIEMPO: 58 ms
```

Hay que leer esa tabla dos veces, porque dice algo más fuerte de lo que se esperaba:

| | LAZY | EAGER |
|---|---|---|
| demo 1 — la pantalla que sí quería los trámites | 201 · 79 ms | **201 · 145 ms** |
| demo 5 — la pantalla que no los quería | 1 · 2 ms | **201 · 58 ms** |

1. **`EAGER` no arregló el N+1.** Sigue en 201. Poner la relación en EAGER no hace que Hibernate
   sea listo: hace que dispare las mismas 200 consultas **siempre**, en vez de solo cuando alguien
   toca la lista. Y encima aquí tardó casi el doble.
2. **Rompió la pantalla que estaba bien.** La demo 5 no pide trámites, no los usa, no los muestra
   — y ahora paga 201 consultas por ellos.

> **La regla, entera:** el arreglo va **en la consulta**, no en el mapeo. `LAZY` en la entidad
> —siempre, y escrito— y cada pantalla pide lo que necesita con `JOIN FETCH`, `@EntityGraph` o una
> proyección. `EAGER` es una decisión tomada en el sitio equivocado: en la entidad, que la usan
> todas las pantallas, en vez de en la consulta, que es de una sola.

**Volver a `LAZY` antes de terminar.**

---

## Al terminar

`practica/` imprime los mismos números que `solucion/` — los milisegundos varían de una máquina a
otra y de una corrida a otra; **las consultas, no**. Ese es el número que importa.

Lo que hay que poder decir con las propias palabras:

> N+1 es una consulta para la lista y una más por cada elemento. Se ve contando, no mirando. Se
> arregla en la consulta, pidiendo de entrada lo que se va a necesitar.

### Lo que siembra este lab

La pantalla ya es rápida: de 201 consultas a 1.

Pero todo lo de hoy se midió con **un solo hilo**, haciendo una cosa detrás de otra. Y un sistema
de verdad no funciona así: hay muchas personas pidiendo cosas **a la vez**, y algunas piden lo
mismo en el mismo instante.

> **La pregunta que abre el Lab 06** — dos funcionarios emiten un folio en el mismo segundo.
> ¿Qué número le toca a cada uno?

El Lab 06 lo pone en un número también, pero de otro tipo: cuántos folios repetidos salen. Y la
respuesta, la primera vez, no va a gustar.
