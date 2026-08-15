# Notas del instructor · Lab 3.5 «El apóstrofe»

*No va en la carpeta del lab: el alumno no lo ve.*

---

## Qué mata este lab

El hueco mayor del curso: **nadie enseñaba persistencia**. Los alumnos venían usando entidades
desde el Lab 01 sin que ningún lab explicara qué es JPA ni qué problema resuelve, y el Lab 04
(fetch, N+1) asumía ese piso. De ahí que el 04 se sintiera incomprensible para quien llegaba sin
base.

Tres huecos de un golpe: **JPA**, **leer el SQL generado**, y la semilla de seguridad que el
Lab 07 recoge.

---

## El arco de la sesión

El orden importa y no es negociable: **primero el dolor, después la herramienta.** Si se explica
JPA antes del golpe, JPA es una API más que memorizar. Después del golpe, es la respuesta a algo
que acaban de sufrir.

| Tiempo | Qué |
|---|---|
| ~10 min | La demo del apóstrofe, proyectada. `./bin/91-demo-inyeccion.sh` |
| ~10 min | Leer el DAO entero, en pantalla, contando los cuatro pecados |
| ~40 min | Teoría (PPT): del `ResultSet` al mapeo; entidad, repositorio, SQL generado |
| ~60 min | Los cuatro TODOs |
| ~10 min | Cierre: la demo otra vez, ahora sin filtración |

### La demo, en vivo

Levantar la app **antes** de la clase (`./bin/start-lab.sh`) — arrancar en vivo mata el momento.
Después:

```bash
./bin/91-demo-inyeccion.sh
```

Salen las dos consultas con un separador entre medio. El golpe está en la segunda: aparecen tres
observaciones de «Comercial Andina SpA», una de ellas marcada *«Fiscalización en curso. NO
divulgar fuera del área»*.

**Deténgase ahí.** Pregunte qué se escribió de más. La respuesta es: una comilla.

Y remátelo así: *no hubo hackeo, no hubo herramienta, no hubo contraseña robada. Alguien escribió
un apóstrofe en un campo de texto.*

---

## La trampa, y cómo usarla

En cuanto vean el pecado 1, alguien va a decir **«se arregla con `PreparedStatement`»**. Tiene
razón, y hay que reconocerlo antes de seguir: el `?` mata la inyección, de verdad.

La pregunta que abre el resto de la sesión: *¿y los otros tres pecados?* El mapeo por número de
columna sigue igual, la conexión se sigue filtrando, el `catch` sigue vacío, y la próxima consulta
vuelve a pedir cuarenta líneas.

Está desarrollado en `PARA-EL-SABADO.md` §4, con la tabla de qué arregla y qué no. No se
implementa como TODO a propósito: el camino del lab va directo al ORM.

---

## Dónde se atascan

| Síntoma | Qué pasó |
|---|---|
| `LazyInitializationException` en el TODO_3 | Falta `@Transactional(readOnly = true)`. **Deje que ocurra**: encontrársela una vez enseña más que la teoría. |
| La app no arranca tras el TODO_2 | El nombre del método no cuadra con la entidad. El error de arranque dice qué propiedad no encontró — léalo con ellos. |
| E1 falla y "está todo puesto" | Casi siempre falta `fetch = FetchType.LAZY`: el segundo test de E1 lo exige aparte. |
| E4 pasa el primer test y falla el segundo | Instaló la regla pero no borró el DAO, o al revés. Son las dos mitades. |

---

## El detalle que engancha con el Lab 04

El `@ManyToOne` **LAZY explícito** del TODO_1 no es capricho: el valor por defecto es EAGER. Vale
la pena decirlo en voz alta y dejarlo colgando —*«¿por qué habría de importar cuándo se trae el
contribuyente?»*— porque esa pregunta es el Lab 04 completo.

En el 04 se convierte en número: un contador de consultas y un presupuesto.

---

## Estado del guardián AU-03b

Nace aquí, en su propio archivo (`arquitectura/ReglasDelApostrofe.java`) y no dentro de
`ReglasDeLaCasa`. La razón es de ingeniería, no pedagógica: las siete reglas de la casa viajan
por toda la cadena de derivación desde el tronco, y añadir una octava obligaba a tocar todos los
labs posteriores a la vez.

**Consecuencia que conviene saber:** AU-03b viaja hasta el Lab 04 y ahí se detiene — del 05 en
adelante el archivo todavía no existe. Integrarlo con las otras siete es trabajo de la SPEC de
reempaquetado, que ya va a pasar por esos labs.

---

## Piloto de empaquetado

Este lab estrena el formato nuevo: el alumno ve **`README.md`, el código con sus TODOs, y
`bin/`**. No hay `guia/`, ni `TESTS.md`, ni `INSTRUCTOR.md` dentro del lab. Lo operativo vive
junto a cada `TODO_N`, en el archivo donde se hace el trabajo, para que no haya que cambiar de
ventana.

Si el formato convence, el reempaquetado de los demás labs es una SPEC aparte.
