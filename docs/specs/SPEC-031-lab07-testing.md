# SPEC-031 · Lab 07 «Testing» — y la estructura de tres carpetas

**Emite:** Arquitecto · **Ejecuta:** mocito
**Fecha:** 17 de agosto de 2026
**Rama:** `spec-031-lab-07-testing` desde `main` (v0.6.1) · PR contra `main`
**Prefijo de commits:** `SPEC-031: <qué>`

---

## 0 · Qué se hace

Dos cosas:

1. **Crear el Lab 07 · Testing.** No existe en el material y es el hueco más grande que dejó la
   encuesta: **12 de los 18 alumnos nunca escribió un test automatizado**. Va aquí, después de
   la concurrencia, porque a partir de este lab los siguientes pueden usar tests con naturalidad.
2. **Estrenar la estructura de tres carpetas** (decisión del PO), que rige de aquí en adelante.

El Lab 09 viejo (logging/AOP) queda descartado: su contenido se absorbe en el futuro lab de
observabilidad. No se toca en esta SPEC.

---

## 1 · La estructura de tres carpetas — nueva regla de la casa

```
labs/lab-07-testing/
├── README.md
├── PASOS.md
├── practica/          proyecto Maven ejecutable, INCOMPLETO, SIN documentación
├── solucion/          proyecto Maven ejecutable, COMPLETO, con POCA documentación
└── instructor/        SOLO archivos .java/.yml comentados. NO ejecutable. NO va al repositorio
```

### `practica/` — sin documentación
Lo que hoy son bloques de 5 a 8 líneas explicando cada método **desaparece**. Solo queda:

- La firma del método o la clase que hay que completar.
- Una línea que diga qué hay que hacer, en imperativo, sin explicar el porqué.
- `// escribe aquí`.

El porqué lo pone el instructor en voz alta. El alumno no lee un manual mientras teclea.

### `solucion/` — poca documentación
El código terminado, con comentarios **breves**: una o dos líneas donde algo no es evidente, y
nada donde sí lo es. Sirve como referencia rápida, no como material de estudio.

### `instructor/` — todo documentado, fuera del repositorio
- Copia de **cada archivo** de la solución (`.java`, `.yml`, y el `pom.xml`), con la misma
  estructura de carpetas que el proyecto (`src/main/java/...`).
- **No es un proyecto ejecutable**: sin `mvnw`, sin `target`, sin `.mvn`. Son los archivos para
  leer mientras se enseña.
- **Documentación completa, línea por línea**: desde los `import` (por qué está cada uno y qué
  aporta) hasta la última llave. Cada anotación, cada método, cada propiedad del `yml`, cada
  dependencia del `pom`. Aquí no hay límite de largo: cuanto más explicado, mejor.
- **Va en `.gitignore`** (`labs/*/instructor/`). No viaja al repositorio: es material del
  instructor. Debe quedar registrado en el README de la raíz que esa carpeta existe y por qué no
  está versionada.

Esta estructura aplica **solo al Lab 07** en esta SPEC. Los labs 00–06 se migrarán después, en
una SPEC de reempaquetado, si el PO lo confirma.

---

## 2 · Reglas heredadas (sin cambios)

Maleta (shim mvnw + JDK embebido + `repo-maven`), `practica/` compila y arranca en su estado de
entrega, carpetas vacías con `.gitkeep`, `logging.level.root: WARN`, PASOS.md con
«se explica / se escribe / se corre / en consola».

**Sin narrativa DGT, sin citas de personajes, sin ArchUnit, sin `bin/`, sin validadores, sin
manifiestos, sin derivación.**

**Puertos:** HTTP 8093 (practica) / 8094 (solucion). Sin base de datos (ver §3).

**Duración objetivo: 3 horas, como los demás. Lab corto y completo, no exhaustivo.** Si algo no
cabe, se deja fuera y se nombra en el README como «lo que no vimos hoy».

---

## 3 · Contenido del Lab 07

**El punto de partida:** un proyecto que ya funciona —un `ProductoService` con su repositorio y
su controller, todo dado y andando— y **ni un solo test**. Nada roto, nada que reparar: hoy no
se arregla, se protege.

**Sin base de datos.** El repositorio es en memoria (una lista), como en el Lab 02. Meter JPA
aquí obligaría a hablar de `@DataJpaTest` y transacciones de test, y este lab es sobre aprender
a testear, no sobre testear persistencia. Se nombra en «lo que no vimos hoy».

**Dependencia:** `spring-boot-starter-test` (trae JUnit 5, AssertJ, Mockito y MockMvc). En
`practica/` viene ya en el `pom.xml`.

### Los pasos

**Paso 1 · El primer test**
Un test de JUnit sobre un método puro del servicio (un cálculo, sin dependencias). `@Test`,
llamar, `assertEquals`. Correr con `./mvnw test` y ver el verde. Explicar la estructura
preparar–ejecutar–comprobar, y que el nombre del test es una frase que se lee.

**Paso 2 · El test que avisa**
Cambiar el código de producción a propósito (romper el cálculo). Correr: **rojo**, con el
esperado y el obtenido en pantalla. Deshacer: verde. *Ese* es el trabajo de un test — avisar
antes que el usuario. Es el momento fuerte del lab.

**Paso 3 · El camino triste**
`assertThrows`: comprobar que pedir un id que no existe lanza la excepción correcta. Y por qué
probar el fallo importa tanto como probar el éxito.

**Paso 4 · Aislar con Mockito**
El servicio depende del repositorio. Se sustituye por un doble: `@Mock`, `when(...).thenReturn`,
y `verify` para comprobar que se llamó. Explicar por qué se aísla: se prueba **una** pieza, no
el conjunto; y el test corre en milisegundos porque no hay nada real detrás.

**Paso 5 · Probar el endpoint sin levantar el servidor**
`@WebMvcTest` + `MockMvc`: pedir `/productos/1`, comprobar el 200 y el contenido del JSON. Y el
404 del id que no existe. Sin puerto, sin navegador, sin Postman.

**Paso 6 · Cuándo levantar Spring entero**
`@SpringBootTest`: qué hace, cuánto tarda comparado con los anteriores (**medirlo y citarlo en el
guion**), y la regla que se llevan: el 90% de los tests no necesita levantar Spring. Se usa
cuando lo que se prueba es precisamente el cableado.

**Cierre — «lo que no vimos hoy»**, en el README, en tres líneas: tests de persistencia
(`@DataJpaTest`), cobertura, y TDD. No caben en tres horas y no se fingen.

---

## 4 · Verificación (citar salidas)

| # | Prueba | Criterio |
|---|---|---|
| V1 | `practica/`: `./mvnw spring-boot:run` y `./mvnw test` | Arranca; los tests que el alumno debe escribir aún no existen — la suite pasa vacía o con los que vengan dados |
| V2 | `solucion/`: `./mvnw test` | Todos verdes; citar el resumen de JUnit |
| V3 | El paso 2 reproducido: romper el código de producción y correr | **Citar el rojo**, con esperado y obtenido. Restaurar y citar el verde |
| V4 | Tiempos del paso 6 | Citar cuánto tarda un test unitario, uno con `@WebMvcTest` y uno con `@SpringBootTest` |
| V5 | Seguir `PASOS.md` completo sobre `practica/` | Se llega al mismo resultado que `solucion/` |
| V6 | `instructor/` | Contiene todos los archivos de la solución, con la misma estructura de carpetas, documentados de principio a fin. **No** contiene mvnw ni target |
| V7 | `git status` con `instructor/` presente | No aparece: el `.gitignore` lo cubre |
| V8 | `ls labs/lab-07-testing` | README.md, PASOS.md, practica/, solucion/, instructor/ |
| V9 | Offline | 0 descargas |

---

## 5 · Entregable

`INFORME-SPEC-031`, formato de 8 secciones. Incluir el tiempo estimado de sesión y los tiempos
medidos de V4. `ESTADO.md` al día. **Merge y tag autorizados sin firma del PO**: al verificar,
mergea y etiqueta el bump que corresponda.

---

## 6 · Prohibiciones

- ❌ Tocar cualquier otro lab.
- ❌ Base de datos en este lab.
- ❌ Documentación en `practica/` más allá de la línea imperativa y el `// escribe aquí`.
- ❌ Que `instructor/` sea ejecutable o llegue al repositorio.
- ❌ Narrativa DGT, citas de personajes, ArchUnit, `bin/`, validadores.
- ❌ Alargar el lab más allá de las tres horas: si sobra contenido, va a «lo que no vimos hoy».
- ❌ sudo · LFS · credenciales · ≥95 MB · verde sin salida citada.
