# Notas del instructor · Lab 3.5 «Guardar y recuperar»

*No va en la carpeta del lab: el alumno no lo ve.*

---

## Qué mata este lab

El hueco mayor del curso: **nadie enseñaba persistencia**. Los alumnos venían usando entidades
desde el Lab 01 sin que ningún lab explicara qué es JPA ni qué problema resuelve, y el Lab 04
(fetch, N+1) asumía ese piso. De ahí que el 04 se sintiera incomprensible para quien llegaba sin
base.

El lab es **constructivo, no un crimen**: el alumno toma una tabla vacía y escribe el código que
la conecta con la aplicación. No hay código heredado que arreglar ni bug que cazar — hay algo que
no existe y que al final de la sesión existe y funciona.

---

## El arco de la sesión

| Tiempo | Qué |
|---|---|
| ~10 min | La demo sobre la **solución**, proyectada: un objeto entra a la base y vuelve |
| ~15 min | Teoría (PPT): el mapa — tu clase ES la tabla |
| ~60 min | Los tres TODOs, ~20 min cada uno |
| ~15 min | `--ver-sql`: leer juntos el `INSERT` y el `SELECT` que nadie escribió |
| ~10 min | Cierre: `90-validar` y la demo otra vez, ahora sobre el código del alumno |

### La demo, y por qué va primero

```bash
./bin/start-lab.sh --dir solucion       # levantar ANTES de la clase
./bin/91-demo-jpa.sh
```

Se ve el `POST` guardando, el `GET` recuperando lo mismo, y —si se levantó con `--ver-sql`— el
SQL que Hibernate generó.

**Enseñar el destino antes del camino es deliberado.** El alumno que sabe a dónde va tolera
mucho mejor los veinte minutos en que su código todavía no arranca. Después se baja la solución
y se empieza en el `starter`.

Si quiere el SQL en la demo, levante así: `./bin/start-lab.sh --dir solucion --ver-sql`.

---

## Los tres TODOs, y qué mide cada uno

| | Qué hace el alumno | El test exige |
|---|---|---|
| **TODO_1** | Anota la clase: `@Entity`, `@Id`, `@Column`, `@ManyToOne` | Que Hibernate la conozca **en su metamodelo**, y que la relación sea LAZY |
| **TODO_2** | `extends JpaRepository` + la consulta derivada | Que `save` devuelva el `id` generado, y que la búsqueda traiga lo del contribuyente pedido y no lo del vecino |
| **TODO_3** | Conecta el servicio al repositorio | Que guardar y recuperar funcione de punta a punta |

En el `starter`, la interfaz del repositorio declara `save` con la misma firma genérica que
`JpaRepository` para que los tests **compilen** antes de estar resuelta. Al extender, la hereda
igual: el alumno puede borrar esa línea o dejarla, y en ninguno de los dos casos rompe nada.

---

## Dónde se atascan

| Síntoma | Qué pasó |
|---|---|
| `LazyInitializationException` en el TODO_3 | Falta `@Transactional`. **Deje que ocurra**: encontrársela una vez enseña más que la teoría. Está explicada en PARA-EL-SABADO §3. |
| La app no arranca tras el TODO_2 | El nombre del método no cuadra con la entidad. El error de arranque dice qué propiedad no encontró — léalo con ellos, es el mejor error posible. |
| E1 falla y «está todo puesto» | Casi siempre falta `fetch = FetchType.LAZY`: el segundo test de E1 lo exige aparte. |
| El `id` viene `null` en E2 | Falta `@GeneratedValue`, o la entidad no está mapeada. |

---

## El detalle que engancha con el Lab 04

El `@ManyToOne` **LAZY explícito** del TODO_1 no es capricho: el valor por defecto es EAGER.
Vale la pena decirlo en voz alta y dejarlo colgando —*«¿por qué habría de importar cuándo se trae
el contribuyente?»*— porque esa pregunta es el Lab 04 completo.

En el 04 se convierte en número: un contador de consultas y un presupuesto.

---

## La historia del «antes de JPA», y por qué NO está en la sesión

`PARA-EL-SABADO.md` §5 muestra en una página cómo se leía una tabla con JDBC crudo: cuarenta
líneas, el SQL concatenado —con la inyección que eso permite—, el mapeo por número de columna, la
conexión que se filtra y el `catch` vacío.

**Está fuera de la sesión a propósito.** Es material de valoración, no de construcción: sirve
para que el alumno entienda qué le están ahorrando, y se lee cuando ya sabe lo que le ahorraron.
Meterlo en las tres horas convertiría un lab de «construye tu primer mapeo» en un lab de «arregla
el desastre de otro», que es una sesión distinta.

Si en clase alguien pregunta por la inyección SQL —y suele pasar cuando se ve el `?` del log—,
ahí está la página, y basta con decir: *el `?` que están viendo es exactamente lo que la impide.*

---

## Piloto de empaquetado

Este lab estrena el formato nuevo: el alumno ve **`README.md`, el código con sus TODOs, y
`bin/`**. No hay `guia/`, ni `TESTS.md`, ni `INSTRUCTOR.md` dentro del lab. Lo operativo vive
junto a cada `TODO_N`, en el archivo donde se hace el trabajo, para que no haya que cambiar de
ventana.

Si el formato convence, el reempaquetado de los demás labs es una SPEC aparte.
