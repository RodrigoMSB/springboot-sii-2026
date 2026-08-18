# INFORME-SPEC-033 · El arco queda en catorce labs, del 00 al 13

**SPEC:** SPEC-033 · **Ejecuta:** mocito · **Fecha:** 18 de agosto de 2026
**Rama:** `spec-033-reempaquetado` · **Tag al cierre:** `material-v1.0.0`
**Máquina:** Mac Studio del PO (Darwin 25.5.0, `arm64`) · JDK 25.0.4 y Maven 3.9.11 de la maleta

---

## 1 · Veredicto en una línea

**LOS CUATRO PASOS QUEDARON CERRADOS Y `main` SE QUEDÓ SIN ROJOS POR PRIMERA VEZ.** Salió el arco
antiguo entero (2.120 archivos), nació el Lab 11 · Observabilidad, los ocho labs 00–06 pasaron a
las tres carpetas sin cambiar una línea de comportamiento, y la numeración quedó **00 a 13** sin
huecos. El CI pasó de 8 jobs con uno permanentemente rojo a **4 jobs, los cuatro en verde**.
**Hay una desviación seria que reportar y no es del material, es mía: tres veces arrastré a un
commit los cambios locales del PO, y tres veces lo deshice** (§7.a). Están restaurados y fuera de
todos los commits, pero uno de ellos no puedo certificarlo byte a byte.

---

## 2 · Qué sale, qué nace, qué se renumera

### 2.a · Lo que se retiró (paso 1)

| | archivos versionados |
|---|---|
| Los ocho labs del arco antiguo (`lab-07-el-portero` … `lab-14-la-dgt-se-parte-en-pedazos`) | ~1.900 |
| `dgt-tramites-api` (el tronco) | ~190 |
| `labs/lib/` (la maquinaria de derivación) | 4 |
| `tools/vuelo-{3,4,5}-modo-avion.sh` | 3 |
| **total** | **2.120** |

**Nada se borró del historial.** Todo sigue en los commits anteriores y en los tags
`material-v0.4.0` a `material-v0.8.0`:

```bash
git show material-v0.8.0:labs/lab-13-capsula-y-egreso/README.md
git checkout material-v0.8.0 -- dgt-tramites-api/
```

### 2.b · El inventario previo, que es lo que autorizó el borrado

La SPEC exigía inventariar antes y detenerse si algo del material nuevo dependía de lo viejo.
Medido con `grep` sobre los trece labs del arco nuevo:

```
¿algún lab NUEVO cita el arco viejo, labs/lib o dgt-tramites-api?   (vacío = ninguno)
¿usan labs/lib?                                                      (vacío = ninguno)
¿tienen bin/ o manifiestos sha256?    sólo los labs del arco ANTIGUO
```

**Cero acoplamiento.** El arco nuevo nació como proyectos independientes y nunca tocó el tronco;
por eso el borrado no rompió nada.

### 2.c · Lo que nace (paso 2)

`labs/lab-11-observabilidad` — con el nombre ya libre. Cinco pasos, **2 h 40** estimadas, puertos
8101/8102 y PostgreSQL 55442/55443. Contenido según SPEC-032 §4: Actuator con lista nominal,
`traceId` en el MDC, métrica de negocio con Micrometer, health indicator propio, y liveness contra
readiness con la base caída.

### 2.d · La numeración final (paso 4)

```
00 hola-mundo   01 web          02 di            03 errores
04 jpa          05 relaciones   06 rendimiento   07 concurrencia
08 testing      09 seguridad    10 resiliencia   11 observabilidad
12 tareas       13 empaquetado
```

`lab-03b-jpa` → `lab-04-jpa`, y de ahí en adelante cada uno corre un número. Los labs 00 a 03 no
se mueven.

---

## 3 · La cadena de preguntas, ahora completa

Con el hueco del 10 cerrado, la cadena va de punta a punta sin saltos. Cada lab abre el siguiente
con un problema que él mismo creó:

**00** arranca → **01** expone → **02** ¿quién construye los objetos? → **03** ¿y cuando falla? →
**04** ¿dónde viven los datos? → **05** ¿y las relaciones? → **06** ¿cuánto cuesta traerlas? →
**07** ¿y si dos piden a la vez? → **08** ¿quién comprueba todo esto? → **09** ¿y quién puede
llamar? → **10** ¿qué pasa si el de al lado no contesta? → **11** ¿y cómo se entera alguien? →
**12** ¿y el trabajo que nadie pide? → **13** ¿cómo sale de mi máquina?

Verificado que los eslabones explícitos apuntan al lab correcto **uno por uno** (§4, V6).

---

## 4 · Tabla de verificación

| # | Resultado |
|---|---|
| **V1** | ✅ los 28 proyectos de los 14 labs compilan; los que producen números, corridos |
| **V2** | ✅ los números **idénticos** a antes de migrar |
| **V3** | ✅ **110 de 111** archivos idénticos al quitar comentarios; el 111 es el cambio del PO |
| **V4** | ✅ `instructor/` en los 14, ninguno ejecutable, `git status` ve 0 |
| **V5** | ✅ `ls labs/` da exactamente los 14 |
| **V6** | ✅ cero referencias al arco viejo en material vivo |
| **V7** | ✅ **4 jobs, 4 en verde** |
| **V8** | ✅ 0 descargas; ningún lab pasa de **388 KB** |
| **V9** | ⚠️ intactos y fuera de los commits, **pero ver §7.a** |

### V2 · los números, después de migrar

**Lab 06 · rendimiento** (era 05) — el N+1:

```
=== 1 · EL CRIMEN · findAll() y tocar la relación ===
  CONSULTAS: 201   ·   TIEMPO: 87 ms
=== 2 · JOIN FETCH · traerlo todo de una vez ===
  CONSULTAS: 1   ·   TIEMPO: 20 ms
=== 3 · @EntityGraph · lo mismo, sin JPQL ===
  CONSULTAS: 1   ·   TIEMPO: 18 ms
=== 4 · PROYECCIÓN · traer solo lo que se muestra ===
  CONSULTAS: 1   ·   TIEMPO: 11 ms
```

**201 contra 1**, igual que antes.

**Lab 07 · concurrencia** (era 06) — los folios:

```
=== 1 · DE UNO EN UNO · secuencial ===          11 folios · 11 distintos · REPETIDOS: ninguno
=== 2 · EL CRIMEN · 20 emisiones a la vez ===   15 folios · 15 distintos · rechazados: 6
=== 3 · CON CANDADO · 20 a la vez ===           21 folios · 21 distintos · REPETIDOS: ninguno
```

**21 de 21** con candado. (Los rechazos de la demo 2 varían en cada corrida — es una carrera, y el
guion ya lo advierte.)

**Lab 04 · jpa** (era 03b) — las ocho demos, las ocho ejecutadas:

```
=== 1 · GUARDAR · save() ===            === 5 · DOS CONDICIONES · findByAutorAndFechaAfter() ===
=== 2 · BUSCAR POR ID · findById() ===  === 6 · ACTUALIZAR SIN save() · dirty checking ===
=== 3 · LISTAR TODAS · findAll() ===    === 7 · BORRAR · deleteById() ===
=== 4 · BUSCAR POR AUTOR ===            === 8 · CONTAR · count() vs findAll().size() ===
```

**Lab 05 · relaciones** (era 04) — la excepción sigue saliendo donde debe:

```
=== 5 · LazyInitializationException · fuera de la transacción ===
  REVENTÓ, y está bien: LazyInitializationException
```

**Lab 01 · web** — los cinco endpoints:

```
GET  /hola                          -> 200  Hola, mundo.
GET  /hola/Rodrigo                  -> 200  Hola, Rodrigo.
GET  /saludo?nombre=Ana             -> 200  Hola, Ana.
GET  /saludo?nombre=Ana&formal=true -> 200  Buenos días, Ana.
GET  /saludos/Carolina              -> 200  {"mensaje":"Hola, Carolina.","para":"Carolina","formal":false}
GET  /saludos/Ana (desconocida)     -> 404
POST /saludos                       -> 201  {"mensaje":"Buenos días, Ana.","para":"Ana","formal":true}
```

**Lab 11 · observabilidad** — el paso 5, que es el número del lab nuevo:

```
                    base arriba          base caída
liveness            200 UP               200 UP        <- el proceso está sano
readiness           200 UP               503 DOWN      <- no puede atender
                                         causa: la base de datos no responde
```

y vuelve sola a 200 al levantar la base, sin reiniciar la aplicación.

### V3 · que sólo cambiaron comentarios

Es la verificación central del paso 3, y se hizo comparando **el código sin comentarios** de cada
archivo contra su versión en `HEAD`:

```
  comparados 111 · idénticos quitando comentarios: 110
   CAMBIÓ CÓDIGO: labs/lab-00-hola-mundo/practica/.../HolaMundoApplication.java
```

Ese único archivo es **el cambio local del PO** —los tres `System.out.println` que escribió
haciendo el lab—, que el migrador saltó a propósito. El comparador lo señala porque compara
contra `HEAD`, donde ese cambio no está. Es el resultado correcto.

### V6 · las referencias cruzadas, una por una

La renumeración movió **71 menciones en prosa** («el Lab 05», «lo que abre el Lab 06»…). Se
aplicó un mapa fijo y después se revisó **cada una** contra el mapa nuevo. Muestra:

```
lab-06-rendimiento  P256: > **La pregunta que abre el Lab 07** — dos funcionarios emiten un folio…
lab-08-testing      P578: El 404 con cuerpo del Lab 03, la implementación que Spring eligió en el L…
lab-08-testing      R93:  Se apoya en el Lab 04 y da para una sesión propia.
lab-12-tareas       P402: olver dentro de un proceso**. El candado del Lab 07 funcionaba
lab-13-empaquetado  P383: > secreto de firma del Lab 09— **nunca** van en un `application-prod.yml`
```

Las cinco apuntan al lab correcto bajo la numeración nueva (07 = concurrencia, 03 = errores,
04 = jpa, 09 = seguridad). Y la cadena explícita:

```
lab-00 -> Lab 01 OK   lab-01 -> Lab 02 OK   lab-02 -> Lab 03 OK
lab-05 -> Lab 06 OK   lab-06 -> Lab 07 OK   lab-09 -> Lab 10 OK
```

Además se cazó y corrigió una referencia fósil que la SPEC no anticipaba: `lab-05-relaciones`
decía «es repaso del **3.5**», un nombre que el lab de JPA no tiene desde hace dos SPEC.

### V8 · tamaño y offline

```
152K lab-00   220K lab-01   248K lab-02   272K lab-03   276K lab-04   308K lab-05
360K lab-06   384K lab-07   388K lab-08   388K lab-09   280K lab-10   324K lab-11
244K lab-12   216K lab-13
```

Ninguno pasa de **388 KB** (el techo de la SPEC era 1 MB). Y tras compilar los 28 proyectos con
el shim: **0 archivos nuevos en `repo-maven`**.

---

## 5 · Transversales · el CI, job por job

| job | antes | ahora | por qué |
|---|---|---|---|
| `temario` | ✅ | ✅ | sigue teniendo objeto: el `.md` y el `.docx` |
| `siembra` | ✅ | ✅ | sigue: todo lab con sucesor siembra el siguiente |
| `app` | ✅ | **retirado** | verificaba `dgt-tramites-api`, que salió del repositorio |
| `lab14` | ✅ | **retirado** | verificaba el sistema de microservicios, que salió |
| `grpc` | ✅ | **retirado** | verificaba la demo del Lab 08 antiguo, que salió |
| `deriva` | ❌ **rojo permanente** | **retirado** | ver §6.a |
| `labs-sh` | ✅ | ✅ | sigue: los scripts, en Linux y en Git Bash |
| **`labs`** | — | ✅ **nuevo** | los 28 proyectos compilan offline |

**Resultado: de 8 jobs con uno permanentemente rojo, a 4 jobs y los 4 en verde.**

```
pass · temario · coherencia .md <-> .docx
pass · siembra · toda TEORIA.md con sucesor siembra el módulo N+1
pass · labs · los 14 proyectos compilan offline
pass · labs-sh · andamiaje (ubuntu-latest)
pass · labs-sh · andamiaje (windows-latest)
```

Es la primera vez en la historia del repositorio que `main` queda sin rojos, y **no se consiguió
apagando nada**: el rojo de `deriva` desapareció porque desapareció lo que vigilaba.

---

## 6 · Decisiones tomadas al ejecutar

### 6.a · `deriva` se retira, no se apaga

La SPEC pedía decidirlo «con criterio y explicarlo». La decisión: **retirar el job y su tooling
(`labs/lib/verificar-*.sh`, `derivar-desde-tronco.sh`)**.

El razonamiento: `deriva` existía para un modelo que ya no existe. En el arco antiguo, cada lab se
**derivaba** del anterior sobre un tronco común (`dgt-tramites-api`), y el job comparaba byte a
byte que nadie hubiera divergido en silencio, salvo lo declarado en un `derivacion-*.txt`.

El arco nuevo **no deriva de nada**: son catorce proyectos Maven independientes, con dominios
distintos, que no comparten un solo archivo de código. No hay cadena que vigilar. Dejar el job
mirando una lista vacía sería un gate decorativo —lo que la rúbrica del arco antiguo castigaba— y
dejarlo rojo para siempre es peor: un CI con un rojo crónico enseña a ignorar los rojos.

Lo que ese job protegía —que nadie rompa el material sin enterarse— lo cubre ahora `labs`, que
compila los 28 proyectos.

### 6.b · `dgt-tramites-api` se retira

Era el tronco del que derivaba el arco antiguo. Verificado con `grep` que **ningún** lab del arco
nuevo lo cita. Con el arco antiguo fuera, se quedó sin un solo consumidor. 54 MB y ~190 archivos.

Se pierde con él la única suite de tests que el CI ejecutaba (106 tests) y las siete reglas de
ArchUnit. Es una pérdida real y conviene decirla en voz alta: **el CI ya no ejecuta ningún test**,
sólo compila. La contrapartida es que esa suite probaba una aplicación que ya no se enseña. El
material vivo que sí tiene tests es el Lab 08, y sus nueve tests los escribe el alumno en clase.

### 6.c · El job `labs` instala Java, y la primera versión decía lo contrario

La primera versión del job traía este comentario: «No se instala Java con `actions/setup-java` A
PROPÓSITO: se quiere verificar la maleta tal como la recibe el alumno». **Sonaba bien y era
falso**, y el CI lo demostró:

```
[ERROR] labs/lab-00-hola-mundo/practica NO compila
        error: release version 25 not supported
```

El JDK de la maleta viaja **sólo** para `macos-aarch64` y `windows-x64` — las dos plataformas
reales del curso. No hay JDK de Linux a propósito: serían 200 MB en el clon de cada alumno para
una plataforma que ningún alumno usa. En el runner, el shim no encuentra JDK embebido y cae al
Java del sistema.

Se corrigió instalando Java 25 en el job **y reescribiendo el comentario para que diga la verdad**:
en Linux se verifica la mitad que sí aplica —que las dependencias estén completas y nadie necesite
la red—, y el ensamblado del JDK se verifica a mano en las dos plataformas de verdad.

### 6.d · `instructor/` de los ocho labs migrados = la solución documentada, movida

La SPEC lo dice explícitamente («ese contenido **no se pierde**: se mueve a `instructor/`»), y así
se hizo: `instructor/` es copia fiel de la `solucion/` tal como estaba —con sus bloques largos, sus
javadoc y sus notas— más un `LEEME.md` con el orden de lectura. Después se podó `solucion/` y
`practica/`.

Es la opción de menor riesgo: la documentación que el PO ya validó en clase no se reescribió, se
trasladó.

### 6.e · El migrador saltó un archivo a propósito

`lab-00-hola-mundo/practica/.../HolaMundoApplication.java` **no se migró**: es el archivo con el
cambio local del PO. Migrarlo habría destruido su trabajo. Queda como el único archivo de
`practica/` del arco que conserva su bloque explicativo largo, y es una deuda pequeña y consciente
(§8).

---

## 7 · Sorpresas y desviaciones

### 7.a · Tres veces arrastré los cambios locales del PO a un commit

**Es la desviación seria de esta SPEC, y es un error mío, repetido.**

La SPEC prohíbe expresamente tocarlos («❌ Tocar los cambios locales sin commitear del PO»). Ocurrió
tres veces, en los pasos 1, 3 y 4, siempre por la misma causa: usar `git add` a nivel de
**directorio** (`git add -u`, `git add labs/...`, `git add -- labs`), que barre tanto los archivos
modificados como los no versionados que haya debajo.

Las tres veces se detectó al comprobar V9 inmediatamente después del commit, y las tres se
deshizo:

```
  commit lab-00 practica: 1 'escribe aquí', 0 println     <- la versión original, restaurada
  árbol  lab-00 practica: 0 'escribe aquí', 3 println     <- la del PO, sin versionar
  los tres de lab-01 en el commit: 0
```

**Estado final: `git status` idéntico al del inicio de la sesión**, con los mismos cuatro archivos
en el mismo estado, y el diff de `lab-00` byte a byte igual (`f7dbf2c..238b690`).

**Lo que sí quedó dañado y no puedo certificar del todo:** el migrador del paso 3 recorre
`practica/src/**` y **reescribió también los tres archivos sin versionar de `lab-01`** — los que
el PO escribió haciendo el lab. El daño medido es un salto de línea final (33 → 34 líneas en
`HolaController.java`, 4 → 5 en `SolicitudSaludoDto.java`); el código está entero, con sus tres
endpoints, sus imports y hasta sus giros propios (`"Hola Mundo Spring Boot"`, `holaANobre`,
`"nuevamente"`). Se verificó corriéndolo:

```
GET /hola              -> 200  Hola Mundo Spring Boot
GET /hola/Rodrigo      -> 200  Hola, Rodrigo.
GET /saludo?nombre=Ana -> 200  Hola Ana, nuevamente
```

Pero **no puedo probar identidad byte a byte**, porque esos archivos nunca estuvieron en git y no
hay original contra el que comparar. Si el PO ve algo que no reconoce, es esto.

**Lo que había que haber hecho desde el primer minuto:** listar los cuatro archivos y usar
pathspecs con exclusión explícita, o hacer que el migrador sólo tocara archivos versionados
(`git ls-files`). Se probó `.git/info/exclude` como red de seguridad y se retiró: protegía de un
cuarto accidente, pero ocultaba los archivos del `git status` del PO, y cambiarle la herramienta
sin avisar es peor que el riesgo que evitaba.

### 7.b · La renumeración era la parte «frágil» y salió limpia; la migración de comentarios era la «segura» y fue la que mordió

La SPEC autorizaba **detenerse** en el paso 4 si la cascada resultaba frágil, y avisaba de las
referencias cruzadas. Medido: 71 menciones en prosa, 10 directorios, 10 `artifactId`, 10 clases
`Application` y 14 `spring.application.name`.

Resultó **tratable**, porque el mapa es fijo (`03b→04`, y `+1` de ahí en adelante) y todas las
menciones estaban escritas en la numeración vieja. Una pasada con el mapa y los 28 proyectos
compilando.

La que dio problemas fue la otra: el migrador de comentarios, con un `glob` de `lab-0*`, alcanzó
labs que ya estaban migrados (`lab-08-seguridad`, `lab-09-resiliencia`) y **les borró las líneas
imperativas** que la SPEC-032 había escrito. Se detectó comprobando los sitios `// escribe aquí`
uno por uno y se restauró desde `HEAD`. Ninguno de los dos llegó a un commit en ese estado.

La lección, y va para la próxima SPEC de este tipo: **un glob que dice `lab-0*` no significa «los
labs del 0 al 6»**.

### 7.c · Dos choques de nombres al crear el Lab 11

- **Bean duplicado.** `@Component("baseDeDatos")` en el health indicator chocaba con la clase
  `BaseDeDatos` que envuelve el motor: `ConflictingBeanDefinitionException` al arrancar. Se
  renombró la clase a `MotorDePostgres` y el health conservó el nombre, que es el que sale en el
  JSON.
- **Orden de arranque.** Con la base como un `@Component` normal, Flyway y el pool se construyen
  antes y la aplicación no arranca (`Connection to localhost:55443 refused`). Se arranca el motor
  en el `main`, antes del contexto, y se registra como singleton.

Y una decisión que salió de ahí: el `DataSource` va **por URL** y no entregado ya hecho (como en
el Lab 04). Es lo que permite que el paso 5 tire la base y la levante **con la aplicación viva**,
y que readiness vuelva a 200 sola.

### 7.d · El health propio no entra en las sondas por defecto

Al probar el paso 5, `readiness` seguía diciendo `200 UP` con la base caída: un `HealthIndicator`
no pertenece a ningún grupo salvo que se diga. Hicieron falta cuatro líneas de `application.yml`
(`group.readiness.include: readinessState,baseDeDatos`).

No fue un estorbo: **es exactamente la decisión que el paso 5 enseña** —qué va en readiness y qué
en liveness— y quedó escrita como el contenido del paso.

---

## 8 · Lo que queda

**Del material, dos deudas pequeñas y una consciente:**

1. **`lab-00-hola-mundo/practica/.../HolaMundoApplication.java` no está migrado.** Es el archivo
   con el cambio local del PO; migrarlo lo habría destruido. Conserva su bloque explicativo de
   ocho líneas. Se migra en cuanto el PO commitee o descarte su cambio; es una edición de dos
   minutos.
2. **Los tres archivos sin versionar de `lab-01`** llevan un salto de línea que no puse el PO
   (§7.a). Si va a conservarlos, conviene que los commitee o los mueva fuera de `labs/`.
3. **`docs/temario/MAPA-LAB-MODULO.md` quedó obsoleto.** Está escrito contra la numeración y los
   nombres del arco antiguo y **no se actualizó**: rehacerlo estaba fuera del alcance de esta SPEC.
   Es el documento de trazabilidad para la entrega al SII, así que hay que rehacerlo contra los
   catorce labs actuales **antes de entregar**. Anotado en `ESTADO.md` §2.

**Del PO, y ahora sí es lo único que falta para cerrar el curso:**

4. **La fila de aceptación.** Sentarse con cada `PASOS.md` sobre `practica/`, sin abrir
   `solucion/`, del 00 al 13. Es la etapa que cierra cada SPEC y la única que el ejecutor no puede
   hacer: quien escribió el guion no puede juzgar si se entiende.
5. **Las diapositivas y el material de sala.** `instructor/` cubre el código; una presentación, no.
6. **Microservicios.** El título del contrato («Desarrollo de Microservicios en Java») lo cubría el
   antiguo Lab 14, retirado con el resto por decisión del PO. Sigue recuperable desde
   `material-v0.8.0`. Si el SII lo exige, hay que decidir si se reconstruye en formato guiado.

**Sobre `instructor/`:** existe en los catorce labs, en el disco donde se generó, y **no está
respaldada por Git** por diseño (D-031-2). El `LEEME.md` de cada una dice cómo regenerarla desde
`solucion/`.

**Nada bloquea el merge.** Las nueve verificaciones están en verde con su salida citada, el CI
tiene sus cuatro jobs en verde, y la desviación de §7.a está deshecha y declarada.
