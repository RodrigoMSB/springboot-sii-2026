# INFORME-SPEC-040 · Coherencia entre labs — nombres y anotaciones

**Ejecuta:** mocito · **Rama:** `spec-040-coherencia-labs` · **Fecha:** 23 de agosto de 2026
**Origen:** SPEC-040 del Arquitecto, con la ampliación del §3 que el PO pasó aparte.

---

## 0 · Resumen

**Los quince labs llaman igual a las mismas cosas.** La tabla de coherencia queda en **cero
divergencias**: ninguna clase con el mismo nombre vive en dos paquetes distintos ni lleva dos
anotaciones distintas, y ningún paquete de rol conocido mezcla estereotipos sin razón (§5).

**Nada cambió de comportamiento.** Las 99 declaraciones de ruta HTTP del arco son **idénticas
antes y después** (diff vacío), los números de los labs 05, 06 y 07 son los mismos, y los
endpoints de los labs 04, 09, 11 y 12 devuelven lo mismo. Es un renombre, y se comporta como tal.

**56 de 56 proyectos Maven compilan offline**, cero descargas intentadas.

**El §1 pidió medir antes de tocar, y valió la pena:** de los cuatro puntos que la SPEC daba por
medidos, **uno era falso** — el lab 01 no tenía un paquete `web/` de controllers que arreglar
(§2). El barrido encontró además **cuatro divergencias más** que la SPEC no listaba, y **dos
defectos preexistentes** ajenos a esta SPEC (§4).

**116 archivos:** 96 `.java`, 17 `.md`, 3 `.gitkeep`.

---

## 1 · La tabla de coherencia · antes y después

### Antes

| Rol | Cómo se llamaba |
|---|---|
| Controllers | `controllers/` en 01, 02, 03, 08, 09, 10, 11, 12, 13, 14 · **`web/`** en 04 |
| Servicios | `services/` en 02, 08, 09, 10, 12, 14 · **`servicios/`** en 07 |
| Clases `Demos*` | Las cuatro con **`@Component`**, siendo servicios inyectados por constructor |
| Paquetes repetidos | **`seguridad/seguridad`**, **`observabilidad/observabilidad`**, **`tareas/tareas`** |
| Seguridad del gateway | **`gateway/seguridad`**, con las mismas dos clases que el lab 09 |
| Trío de errores | `exceptions/` + `dto/` en 03 · **`controllers/` + `services/`** en 08 |
| `@Component` dentro de `services/` | **`SembradorDeUsuarios`** (09) y **`Instancia`** (12) |
| Fontanería del arranque | **raíz** en 04, 05, 06, 07, 09 · `infra/` en 11, 14 y `proyecto-final` |

### Después

| Rol | Paquete | Anotación | Labs |
|---|---|---|---|
| Capa web | `controllers/` | `@RestController` (24/24) | 01, 02, 03, 04, 08, 09, 10, 11, 12, 13, 14 |
| Lógica | `services/` | `@Service` (9/9) | 02, 07, 08, 09, 10, 12, 14 |
| Demostraciones | `demos/` | `@Service` (4/4) | 04, 05, 06, 07 |
| Almacén | `repositories/` | `@Repository` o interfaz | 02, 04, 05, 06, 07, 08, 09, 11, 14 |
| Fila de tabla | `entities/` | `@Entity` (13/13) | 04, 05, 06, 07, 09, 11, 14 |
| Objeto en memoria | `models/` | — (4/4) | 02, 03, 08, 09 |
| Cuerpo de respuesta | `dto/` | — (6/6) | 01, 03, 06, 08 |
| Errores | `exceptions/` | `@RestControllerAdvice` + la excepción | 03, 08 |
| Configuración | `config/` | `@Configuration` (2/2) | 09, 14 |
| Fontanería | `infra/` | — y `@Component` | 04, 05, 06, 07, 09, 11, 14 |
| Andamiaje del lab | `soporte/` | `@Component` (4/4) | 06, 09, 12 |

**Clases con el mismo nombre en paquetes o anotaciones distintas: 0.**

---

## 2 · Lo que la SPEC daba por medido, y no lo estaba

> **El lab 01 no tenía nada que arreglar.** La SPEC §1 lo listaba junto al 04 como un lab con
> `web/` en vez de `controllers/`. No es el mismo caso: en el lab 01 **`web` es el paquete raíz
> del lab** —`cl.dgt.web`, igual que `cl.dgt.di` en el 02 o `cl.dgt.jpa` en el 04—, y sus
> controllers ya vivían en `cl.dgt.web.controllers`. El lab 04 sí lo era: `cl.dgt.jpa.web`.

Renombrar el paquete raíz del lab 01 a `cl.dgt.controllers` habría sido absurdo. **No se tocó**,
y es la razón por la que el §1 exige medir antes.

---

## 3 · Qué se renombró, lab por lab

### 3.1 · Lo que pedía la SPEC §2

| Lab | De | A |
|---|---|---|
| **04** | `cl.dgt.jpa.web` | `cl.dgt.jpa.controllers` |
| **07** | `cl.dgt.concurrencia.servicios` | `cl.dgt.concurrencia.services` |
| **04, 05, 06, 07** | `Demos*` con `@Component` | `Demos*` con `@Service` · el nombre `demos/` se mantiene |
| **09** | `seguridad/seguridad` | `config/` + `services/` (§3.3) |
| **11** | `observabilidad/observabilidad` | `infra/` (§3.3) |
| **12** | `tareas/tareas` | `programadas/` (§3.3) |

### 3.2 · Lo que encontró el barrido del §1

| Lab | Qué | Por qué |
|---|---|---|
| **14** | `gateway/seguridad` → `config/` + `services/` | Son las **mismas dos clases** que el lab 09 (`SeguridadConfig`, `ServicioDeTokens`). Dejarlas con otro nombre habría creado la divergencia que esta SPEC viene a cerrar |
| **08** | `controllers/ManejadorDeErrores` → `exceptions/` · `services/ProductoNoEncontradoException` → `exceptions/` · `controllers/ErrorRespuesta` → `dto/` | El lab **03 enseña ese reparto cinco labs antes**. El 08 reusaba las mismas tres clases con los mismos nombres y las guardaba en otro sitio |
| **09, 12** | `services/SembradorDeUsuarios` y `services/Instancia` → `soporte/` | Son `@Component` y **no son servicios**: uno es un `CommandLineRunner` de semilla, el otro un bean que sólo sabe su propio nombre. Con ellos fuera, `services/` significa `@Service` y nada más. `soporte/` ya existía en el lab 06 con `CargadorDeDatos` y `ContadorDeConsultas`, que hacen exactamente ese papel |
| **04, 05, 06, 07, 09** | `CandadoLibre` y `PuertoLibre` → `infra/` | Misma clase, mismo papel, dos sitios: raíz en cinco labs e `infra/` en el 11, en los tres servicios del lab 14 y en `proyecto-final/base` |

### 3.3 · Los tres paquetes repetidos: por qué no los tres se llaman `config`

La SPEC §2.4 proponía `config` para los tres y dejaba abierta la puerta: *«si el contenido no es
configuración sino otra cosa, elegir el nombre que corresponda y decirlo»*. Se miró el contenido
de cada uno:

- **Lab 09 · `seguridad/seguridad`** → **`config/` + `services/`.** Tenía tres clases, y no eran
  lo mismo: `SeguridadConfig` es `@Configuration`, pero `ServicioDeTokens` y
  `UsuarioDetailsService` son `@Service`. Meter dos servicios en un paquete llamado `config/`
  habría cambiado un nombre malo por otro. Partirlo deja cada clase donde el propio curso enseña
  que va. El lab 14 recibió el mismo trato por la misma razón (§3.2).
- **Lab 11 · `observabilidad/observabilidad`** → **`infra/`.** Ahí no hay configuración: hay
  `CandadoLibre`, `MotorDePostgres`, `PuertoLibre`, `FiltroDeCorrelacion` y `SaludDeLaBase`. Es
  **exactamente** el contenido que el lab 14 y `proyecto-final/base` ya llamaban `infra/`. El
  nombre no se inventó: ya existía en el repositorio.
- **Lab 12 · `tareas/tareas`** → **`programadas/`.** `CierreNocturno` y `Recordatorio` son dos
  `@Component` con `@Scheduled`: son las tareas programadas del lab, no su configuración.
  `programadas/` es el vocabulario del propio lab y **no choca con ningún nombre estructural** que
  se use en otro sitio — que es lo que hacía malo a `servicios/`, no el estar en español.

---

## 4 · Los defectos preexistentes que destapó el barrido

Ninguno lo introduce esta SPEC.

### 4.1 · Seis `pom.xml` de `instructor/` no se podían leer

Los `instructor/pom.xml` de los labs **08, 09, 10, 11, 12 y 13** llevan una regla decorativa de
guiones **dentro de un comentario XML**, y eso es ilegal en XML:

```
Non-parseable POM .../lab-13-empaquetado/instructor/pom.xml: in comment after two dashes (--)
next character must be > not - (position: START_DOCUMENT ...) @ line 5, column 6
```

**Los seis proyectos no compilaban, y nadie se había enterado**: `labs/*/instructor/` está en el
`.gitignore`, así que el job `labs` del CI —que compila 37 proyectos— nunca los ve. Se descubrió
porque esta SPEC compila **los 56**, `instructor/` incluido.

Se arreglaron **en local** para poder verificar V1 y V7. **El arreglo no viaja al repositorio**,
porque esos archivos no están versionados: hay que corregirlo en lo que genera `instructor/` a
partir de `solucion/`. **Queda anotado para el Arquitecto.**

### 4.2 · Un guion que prometía una carpeta vacía que no lo estaba

El árbol de `labs/lab-11-observabilidad/PASOS.md` decía:

```
observabilidad/                →  pasos 2 y 4 (llega vacía)
```

**No llegaba vacía:** `practica/` trae ya `CandadoLibre`, `MotorDePostgres` y `PuertoLibre`. Al
renombrarla a `infra/` se corrigieron las dos cosas — el nombre y el hecho:

```
infra/                         →  pasos 2 y 4 (FiltroDeCorrelacion y SaludDeLaBase;
                                   el resto de `infra/` llega dado)
```

### 4.3 · Un javadoc que defendía lo contrario de lo que ahora hace el código

`instructor/.../demos/DemosJpa.java` no sólo llevaba `@Component`: **argumentaba** que `@Service`
no encajaba —*«aquí ninguno de los dos encajaría — esto no es una regla de negocio ni un almacén,
es una colección de demostraciones»*—. Cambiar sólo la anotación habría dejado al instructor con
un texto que contradice el código que tiene delante. **Se reescribió el párrafo** para que
explique la razón nueva: la clase recibe el repositorio por constructor y hace trabajo con él,
igual que `ProductoService` en el lab 02, y por eso lleva la misma anotación.

Es el caso que justifica la ampliación del §3 del PO.

---

## 5 · Lo que NO se unificó, y por qué

| Qué | Razón |
|---|---|
| **`models/` vs `entities/`** | Lo prohíbe la SPEC §2.5, y con razón: uno es un `record` en memoria, el otro una fila. **Se documentó** con una nota en el README de los **diez** labs donde aparece cada uno — `models/` en 02, 03 y 08; `entities/` en 04, 05, 06, 07, 11 y 14; y el **09**, que tiene los dos y lleva su propia redacción |
| **`demos/`** | Lo dice la SPEC §2.3: es material didáctico, no una capa. Lo que se unificó ahí fue la anotación |
| **`enrutado/` (lab 14)** | Mezcla `@RestController` (`Enrutador`) y `@Component` (`TablaDeRutas`), pero es el **subsistema de enrutado**, que es el tema del lab. Sacar `Enrutador` a `controllers/` lo separaría de la tabla de rutas que usa, y es controller sólo de refilón: no expone un recurso, reenvía todo |
| **`tesoreria/` (lab 10)** | Mezcla `@Component` (`ClienteTesoreria`) y `@Configuration` (`TesoreriaSimulada`), pero agrupa **todo lo que tiene que ver con Tesorería**: el cliente y su simulador. El lab 14 llama `clientes/` a un paquete que sólo tiene clientes; no son el mismo paquete con dos nombres, son dos recortes distintos |
| **`soporte/`, `clientes/`, `programadas/`** | Cada uno con un estereotipo único y coherente. No tienen contraparte con otro nombre |
| **`repositories/` con `@Repository` y sin él** | No es divergencia: las interfaces que extienden `JpaRepository` no la llevan —Spring las detecta igual—, y las implementaciones en memoria sí. Es la misma regla en los nueve labs |
| **`exceptions/` con y sin anotación** | Tampoco: el manejador es `@RestControllerAdvice` y la excepción no lleva nada. Idéntico en el 03 y en el 08 |
| **`proyecto-final/`** | **Fuera del alcance**: la SPEC §1 habla de *«los 15 labs»*, y la SPEC-039 ya lo había dejado fuera por la misma razón — no es un laboratorio, es el examen. Su `infra/` ya coincide con el nombre elegido. Su `consolidado/seguridad/` sí es un recorte vertical con seis papeles en un paquete (controller, config, dos servicios, entidad y repositorio), **hecho a propósito**: es la autenticación **ya resuelta** que el alumno recibe sin tener que escribirla. **Queda anotado**: si el PO quiere alinearlo, es una decisión suya, no un descuido |

---

## 6 · Verificación

### V1 · Los proyectos compilan y arrancan

```
proyectos Maven encontrados: 56
-----------------------------------------
TOTAL: 56   COMPILAN: 56   FALLAN: 0
```

Son los **30 de la SPEC** (`practica/` + `solucion/` de los 15 labs, con el lab 14 aportando ocho)
más los 15 `instructor/`, más `proyecto-final/base` y su solución de referencia. Los seis
`instructor/` que fallaban lo hacían por el defecto **preexistente** del §4.1, no por esta SPEC:
dos de ellos —labs **10 y 13**— **no fueron tocados por esta SPEC en absoluto**.

**Arrancan**, además de compilar: labs 04, 05, 06, 07, 09, 11, 12 y el gateway del 14, corridos
de verdad para V2 y V3.

### V2 · Los números, antes y después

| Lab | Número | Antes | Después |
|---|---|---|---|
| **05** relaciones | `Hibernate:` emitidos en la corrida · `LazyInitializationException` | 20 · 2 | **20 · 2** |
| **06** rendimiento | Consultas de las cinco demos | 201 · 1 · 1 · 1 · 1 | **201 · 1 · 1 · 1 · 1** |
| **07** concurrencia | folios / distintos / rechazados, en las tres demos | 11·11·0 — 11·11·10 — **21·21·0** | **11·11·0 — 11·11·10 — 21·21·0** |
| **08** testing | Suite | 9 tests, 0 fallos, BUILD SUCCESS | **9 tests, 0 fallos, BUILD SUCCESS** |

Los tres primeros se comprobaron con **`diff` de las secciones completas** de la consola, no
mirando los números a ojo. Los tres dan `IDENTICO`. El titular del lab 06 —**201 consultas contra
1**— y el del 07 —**21 de 21 con candado**— siguen exactamente donde estaban.

### V3 · Los endpoints

**Las rutas, medidas en las 99 declaraciones `@…Mapping` del arco:**

```
declaraciones de ruta ANTES:   99
declaraciones de ruta DESPUES: 99
=== diff (vacio = ninguna ruta cambio) ===
IDENTICAS
```

**Las respuestas**, con las aplicaciones corriendo de verdad:

| Lab | Prueba | Resultado |
|---|---|---|
| **04** | `GET /api/observaciones/{1,2,3,4,999}` | `404 · 200 · 200 · 404 · 404`, con los mismos cuerpos JSON. `diff` **IDENTICO** |
| **09** | sin token · login `ana` · con token · `quien-soy` · `administracion` con USUARIO y con ADMIN | `401 · token · 200 · {"usuario":"ana","roles":"ROLE_ADMIN"} · 403 · 200`. `diff` **IDENTICO** |
| **11** | liveness y readiness, con la base sana y caída | `200 · 200 · 200 · **503**` nombrando la causa. **IDENTICO** normalizando milisegundos |
| **12** | `/tramites/quien`, `/sincrono`, `/asincrono` | `200 · 200 · 200`, mismos campos y valores. **IDENTICO** normalizando el orden de claves del JSON y el hilo |
| **14** gateway | `/salud` · login `carolina` · clave mala · ruta protegida sin token | `200 · 200 con token · **401** · **401**` |

En los labs 11 y 12 el `diff` crudo no salía vacío: cambiaban **milisegundos**, el **número del
worker** y el **orden de las claves** de un `Map` de respuesta. Se normalizaron esas tres cosas
—con un script, no a ojo— y entonces sí: **IDENTICO**. El orden de claves de un `HashMap` no es
estable entre corridas y no lo introdujo esta SPEC.

### V4 · Los bloques «se pega»

**El verificador del CI, en verde, en los quince:**

```
[OK] 15 guion(es) verificado(s): todo lo que prometen está en solucion/.
```

Con los **mismos recuentos** que dejó la SPEC-039 —146 bloques, 87 métodos— así que no se perdió
ni se coló ningún bloque.

**Los bloques se regeneraron desde `solucion/`, no se editaron a mano.** Se escribió un
regenerador que, para cada bloque «Se pega» que crea un archivo, **lee la sentencia `package` del
archivo real de `solucion/`** y la escribe en el guion. Cuatro líneas regeneradas, cada una con su
origen impreso:

```
lab-09-seguridad: «package cl.dgt.seguridad.seguridad;» -> «package cl.dgt.seguridad.config;»
                  (de labs/lab-09-seguridad/solucion/.../config/SeguridadConfig.java)
```

> **Una de las cuatro salió mal y se corrigió.** El regenerador arrastraba el último archivo
> nombrado, y le puso `services` al bloque de `AuthController`, que no lleva encabezado propio.
> Se detectó revisando la salida —por eso el regenerador **imprime cada cambio con su origen**, A-04—
> y se rehízo leyendo `solucion/.../controllers/AuthController.java`. El verificador no lo habría
> cazado: `cl.dgt.seguridad.services` **sí existe** ahora, así que la línea era «verdadera» y
> falsa a la vez.

**V1 de la SPEC-038, rehecha sobre `practica/` limpia:**

- **Lab 09** — se copió `practica/`, se pegaron los 3 bloques de archivo entero y las 2
  dependencias del `pom.xml`, y **compila**. `UsuarioDetailsService` y `ServicioDeTokens` salen
  **idénticos** a `solucion/` (23 y 32 líneas de código). `SeguridadConfig` no, **y es correcto**:
  el guion lo construye en los pasos 2, 3, 4, 5 y 6, así que su primer bloque es un estado
  intermedio a propósito.
- **Lab 04** — el bloque de `ObservacionController` —el archivo que tocó el renombre— es
  **idéntico** a `solucion/` (35 líneas). El pegado completo del lab 04 exige el motor de la
  SPEC-039, que aquella SPEC decidió **no commitear**; por eso el pegado de punta a punta se hizo
  en el 09.

### V5 · Barrido final de coherencia

La tabla está en el §1. El resultado que la cierra:

```
=== 3 · CLASES CON EL MISMO NOMBRE EN VARIOS LABS: ¿mismo paquete? ===

  clases con el mismo nombre en paquetes/anotaciones distintas: 0
```

Y por paquete: `controllers` 24/24 `@RestController`, `services` 9/9 `@Service`, `demos` 4/4
`@Service`, `entities` 13/13 `@Entity`, `config` 2/2 `@Configuration`, `soporte` 4/4
`@Component`. Las mezclas que quedan están justificadas una a una en el §5.

### V6 · `grep` de nombres viejos

En material vivo —`labs/`, `docs/`, `proyecto-final/`, `README.md`, `ESTADO.md`, `tools/`—
excluyendo los informes históricos, y **sobre texto además de código** (`.md`, `.java`, `.xml`,
`.yml`, `.properties`, `.sh`, `.cmd`, `.py`):

| Qué se buscó | Resultado |
|---|---|
| `cl.dgt.jpa.web`, `jpa/web` | **0** |
| `cl.dgt.concurrencia.servicios`, `servicios/` | **0** |
| `seguridad.seguridad`, `observabilidad.observabilidad`, `tareas.tareas`, `gateway.seguridad` | **0** |
| `testing.controllers.ErrorRespuesta`, `testing.services.ProductoNoEncontrado` | **0** |
| `@Component` en clases `Demos*` | **0** (la única coincidencia es el javadoc del §4.3, que ahora **nombra** las tres anotaciones para explicar por qué se eligió `@Service`) |
| Paquetes `cl.dgt.X.X` repetidos | **0** |
| Paquetes en español que dupliquen uno inglés | **0** |
| `CandadoLibre`/`PuertoLibre` fuera de `infra/` | **0** |

### V7 · `instructor/`

**Al día**: las 15 carpetas recibieron los mismos renombres que `practica/` y `solucion/`,
incluida la documentación línea por línea —el javadoc del §4.3, los `LEEME.md` de los labs 07,
09, 11, 12 y 14, y el comentario del lab 08 que **cita el mensaje de error** que verá el alumno,
donde el nombre completo de la excepción cambió con el paquete.

**Invisible para git**: `git status` no muestra ni un archivo de `labs/*/instructor/`.

### V8 · Offline

Los 56 proyectos se compilaron con `--offline` contra `repo-maven/`. Descargas intentadas en todo
el barrido: **0**.

---

## 7 · Lo que este informe deja anotado para el Arquitecto

1. **Los seis `instructor/pom.xml` ilegales (§4.1).** Arreglado en local, **no viaja**: hay que
   corregirlo en lo que genera `instructor/`. Y hay una pregunta detrás: el CI compila 37
   proyectos y hay 56; los 15 `instructor/` no los mira nadie **porque no están versionados**, que
   es exactamente la decisión que los deja sin red de seguridad.
2. **`proyecto-final/base` quedó fuera del alcance (§5).** Su `consolidado/seguridad/` reparte
   seis papeles en un paquete. Es deliberado, pero ahora es el único sitio del material que lo
   hace.
3. **El lab 01 nunca tuvo el defecto que la SPEC le atribuía (§2).**

## 8 · Lo que NO se tocó

- Ninguna ruta HTTP, puerto ni nombre de tabla — medido en V3.
- Ningún paso, ningún orden, ningún número — medido en V2.
- **Los trece archivos del PO** que estaban sin seguir o modificados en el árbol de trabajo al
  empezar (su avance a mano por los labs 01, 02 y 03). Siguen exactamente como estaban: `git add`
  los recogió una vez y se sacaron del índice antes de commitear.
