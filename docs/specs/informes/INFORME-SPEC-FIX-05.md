# INFORME-SPEC-FIX-05 · El validador que miente y sus parientes

**SPEC:** SPEC-FIX-05 · **Ejecuta:** mocito · **Fecha:** 13 de agosto de 2026
**Rama:** `spec-fix-05-validadores-honestos` · **Tag al cierre:** `material-v0.3.1`
**Máquina:** Mac Studio del PO — Darwin 25.5.0, `arm64`

---

## 1 · Veredicto en una línea

**LOS CUATRO INQUILINOS, DESALOJADOS** — el guard de symlinks arreglado sin aflojarlo, 39
declaraciones de éxito mentirosas corregidas en 35 archivos, los 14 labs sabiendo cómo se
llaman, y la regla A-04 registrada junto a A-01 y A-02. Cero código Java tocado.

---

## 2 · Inquilino 1 · `borrar_seguro` ciego a symlinks

**La causa, en una línea:** `pwd` devuelve la ruta **lógica** y `git rev-parse --show-toplevel`
la **física**. Demostrado antes de tocar nada:

```
$ cd /tmp/demo-symlink
pwd    (logico): /tmp/demo-symlink
pwd -P (fisico): /private/tmp/demo-symlink
```

En macOS `/tmp` es symlink a `/private/tmp`, así que un repo clonado ahí producía
`/tmp/…` por un lado y `/private/tmp/…` por el otro. No cuadraban, y el cinturón abortaba un
borrado perfectamente legítimo — **dudando bien, pero por la razón equivocada**.

**El arreglo:** `pwd -P` en las dos puntas de la comparación (`borrar_seguro` y `raiz_repo`).
Sin `realpath`, que no existe en el macOS base. El cinturón no se afloja: solo compara peras
con peras.

### F1 · Reproducción del caso del vuelo 1, con sus negativos

Clon en `/tmp`, `raiz_repo` devolviendo `/private/tmp/fix05`:

| Caso | Antes | Ahora |
|---|---|---|
| Borrar `.estado` **dentro** del repo, clon en `/tmp` | `cae fuera del repo — abortado` · `EXIT=1` · **sigue ahí** | `EXIT=0` · **BORRADO** |
| Borrar `/tmp/no-tocar-esto`, **fuera** del repo | abortado | `cae fuera del repo — abortado` · `EXIT=1` · **intacto** |
| Ruta vacía | abortado | `ruta vacía — abortado` |
| `$HOME` | abortado | `ruta peligrosa (/Users/rodrigosilva) — abortado` |
| `/` | abortado | `ruta peligrosa (/) — abortado` |
| Ruta física sin symlink (`/private/tmp/…`) | borraba | `EXIT=0` · **BORRADO** — no se rompió el caso normal |

**Y el ciclo completo, que es lo que importa.** `start-lab` + `99-destruir` con el clon en
`/tmp`:

```
[OK]    API detenida (PID 53042)
[OK]    PostgreSQL embebido detenido: no quedó ningún proceso
[OK]    Archivos temporales del lab borrados
  3/3 verificaciones
  Todo quedó como estaba
--- .estado despues: BORRADO (BIEN)
--- huerfanos: 0
```

Ese `3/3 · Todo quedó como estaba` **ahora es verdad**. Antes se imprimía igual, palabra por
palabra, sobre un borrado que había abortado. Ese es el bug que da nombre a la SPEC.

---

## 3 · Inquilino 2 · Inventario completo de las declaraciones de éxito

Universo auditado: **81 scripts** (`labs/*/bin/*.sh`, `labs/lib/*.sh`, `tools/*.sh`) y **256
sitios** que declaran éxito. Los labs 08–14 entran, como manda §2.

El clasificador (`/tmp/auditar.py`) recorre cada archivo, sigue la estructura de bloques
`if`/`case`/`loop`, y para cada `paso_ok` o `[OK]` literal determina si el bloque que lo
contiene tiene una rama de fallo. No es un muestreo: son los 256.

### Antes y después

| Clasificación | Antes | Después |
|---|---:|---:|
| CONDICIONAL — depende del resultado real | 147 | **220** |
| SIN-ALTERNATIVA — dentro de un `if`, sin rama de fallo | 85 | 19 |
| INCONDICIONAL — se imprime pase lo que pase | 24 | 17 |
| **TOTAL** | **256** | **256** |

### Los seis patrones corregidos

| # | Patrón | Qué mentía | Archivos |
|---|---|---|---:|
| A | `"API detenida (PID N)"` | Se imprimía tras el bucle de espera **aunque el proceso siguiera vivo** a los 15 s | 14 |
| A2 | `"Instancias detenidas (N)"` | El contador sumaba la instancia aunque no hubiera muerto | 3 |
| B | `"Archivos temporales del lab borrados"` | **El bug que da nombre.** Tres `borrar_seguro` seguidos, códigos de retorno ignorados | 13 + 1 |
| C1 | `"Tu trabajo quedó respaldado en X/"` | El `tar \| tar` del respaldo, sin comprobar | 13 |
| C2 | `"Tests del enunciado restaurados"` | El `cp` de restauración, sin comprobar | 13 |
| C3 | `"Solución aplicada sobre X/"` | El `tar \| tar` de la solución, sin comprobar | 13 |
| D | `"PostgreSQL del laboratorio detenido (N compose)"` | Decía «detenido» con `N=0`, es decir sin haber bajado nada | 6 |
| E1 | `derivar-desde-tronco.sh` · `"Derivado: X -> Y"` | El `tar \| tar` de la derivación, sin comprobar | 1 |
| E2 | `lab-14/95-recuperar` · respaldo y config | Dos `cp … 2>/dev/null` con el error tragado | 1 |
| E3 | `lab-14/99-destruir` · artefactos | El contador no distinguía borrado de fallo | 1 |

En C1 y en el respaldo del Lab 14, el fallo además **aborta**: recuperar sin respaldo es
perder el trabajo del alumno, así que no se sigue.

### Los 36 que quedan, uno a uno — por qué son honestos

El inventario incluye los honestos porque **son la evidencia de que la caza fue exhaustiva**.

| Sitios | Qué son | Por qué es honesto |
|---:|---|---|
| 10 | `printf '[OK] Pruébalo tú mismo:'` en los `start-lab` de los labs 00–10 | Es una **cabecera**, no una verificación: no llama a `paso_ok` ni toca el contador. Y es inalcanzable si la app no arrancó — el `esperar_url` de arriba hace `exit 1` |
| 3 | `lab-12/start-lab:171`, `lab-14/start-lab:120` y `:127` | Van justo después de un `if ! …; then paso_fail; exit 1; fi`. Inalcanzables si la operación falló |
| 2 | `verificar-derivacion.sh:70`, `tools/verificar-tamanos.sh:53` | Guardados por un `if` que hace `exit 1` antes de llegar |
| 2 | El cuerpo de `paso_ok()` y `requiere_comando()` en `lib-comunes.sh` | Son la implementación de la mensajería, no un sitio de uso |
| 19 | `paso_ok` en ramas `else` que reportan un **estado observado**, no una operación | *«La API no estaba corriendo»*, *«No había instancias corriendo»*: el `if` comprobó el estado y el mensaje lo describe. No hay operación cuyo resultado ignorar |

`resumen_final` no necesitó cambios: ya calculaba el denominador como `OK + FALLOS`, ambos
alimentados por `paso_ok`/`paso_fail`. El contador nunca fue el mentiroso — mentían las
llamadas. Corregidas ellas, el `N/N` pasa a contar verificaciones que verificaron.

---

## 4 · Inquilino 3 · Los labs que no sabían cómo se llaman

Auditados los 14 labs por **auto-identificación**: veredictos (`LAB NN APROBADO`), cabeceras
(`Lab NN · validando`) y desmontajes (`Desmontando el Lab NN`).

| Lab | Hallazgo | Corregido |
|---|---|---|
| 00 | Sin auto-rótulo (no tiene `90-validar`) | — |
| 01, 02, 03 | Coherentes | — |
| **04** | `Desmontando el Lab 04` y `Lab 04 · validando`, pero veredicto **`LAB 02 APROBADO`** | ✅ 2 rótulos |
| **05** | Ídem: cabeceras correctas, veredicto **`LAB 02`** | ✅ 2 rótulos |
| 06 … 12 | Coherentes | — |
| 13 | Veredicto propio y deliberado (es el examen final: *«NÚCLEO VERDE / TODAVÍA NO»*), sin número que equivocar | — |
| 14 | `EL LAB 14 ESTÁ APROBADO` — correcto | — |

**Verificación posterior: 14/14 correctos**, con el detector sin una sola discrepancia.

**Referencias cruzadas: se conservan.** Los 14 labs mencionan «Lab 00» (su tabla de
troubleshooting), el 12 menciona el 11, el 13 menciona 01/03/09/12. Son punteros legítimos a
otros labs, no errores de identidad — confundirlos con el bug habría sido el refactor
oportunista que §7 prohíbe.

**Puertos:** el de la casa es `DGT_PUERTO_DEFECTO=8099`. Los literales encontrados —`15672`
(consola de RabbitMQ, Lab 12), `8888` (Config Server) y `8081` (Lab 14)— son correctos para
sus labs.

---

## 5 · Inquilino 4 · La regla nueva, y la ironía del mismo día

Nace **A-04** en `docs/adn/adn-cypress.md`, junto a A-01 y A-02, con su fila en
`docs/decisiones.md`:

> **Todo arnés de verificación imprime TODO lo que calcula.** Un número que se calcula y no se
> imprime no existe; un número que se reporta sin haberse impreso es una mentira estructural,
> aunque resulte correcto.

Se registra como **anti-herencia nacida en casa**, no destilada del curso de Cypress, con el
precedente de P-15…P-18 (principios adoptados por hallazgo del ejecutor, Addendum SPEC-002).
Es A-02 mirándose al espejo: **aquella prohíbe declarar sin medir; esta prohíbe medir sin
mostrar.** Las dos protegen que el veredicto sea auditable por alguien que no estaba delante.

**Arneses vivos de `tools/`, auditados:** `verificar-tamanos.sh` y `vuelo-3-modo-avion.sh`
imprimen todo lo que calculan. Ninguno incumplía.

**Y la ironía, que va al informe porque callarla sería incumplir la regla el día que se
escribe:** el arnés que monté para verificar esta misma FIX (`/tmp/f345.sh`) calculaba la
cadena de derivación con `printf` en vez de con su función `say`, así que el resultado salió
por stdout y **no quedó en el log**. Lo detecté al leer el log y ver la sección F6 vacía; la
volví a correr imprimiendo donde corresponde (§6). A-04 mordiendo a su propio autor el mismo
día — que es exactamente para lo que sirve una regla.

---

## 6 · Tabla de verificación

| # | Prueba | Resultado |
|---|---|---|
| **F1** | Reproducción symlink + negativos + ciclo real | ✅ Ver §2 |
| **F2** | Ciclo `start-lab` + `90-validar` + `99-destruir`, labs 01–07, offline | ✅ Ver abajo |
| **F3** | Labs 08–14: `bash -n` + shellcheck + ejecución de lo que no pide Docker | ✅ Ver abajo |
| **F4** | Rótulos de los 14 labs | ✅ 14/14 |
| **F5** | Suites de solución, labs 01–07 | ✅ Ver abajo |
| **F6** | Derivación y manifiestos | ✅ Ver abajo |

### F2 · Ciclo completo, labs 01–07, offline

```
lab-01  start-lab=0 health=200  destruir=0 huerfanos=0  90-validar=0 :: LAB 01 APROBADO
lab-02  start-lab=0 health=200  destruir=0 huerfanos=0  90-validar=0 :: LAB 02 APROBADO
lab-03  start-lab=0 health=200  destruir=0 huerfanos=0  90-validar=0 :: LAB 03 APROBADO
lab-04  start-lab=0 health=200  destruir=0 huerfanos=0  90-validar=0 :: LAB 04 APROBADO   <-- decía LAB 02
lab-05  start-lab=0 health=200  destruir=0 huerfanos=0  90-validar=0 :: LAB 05 APROBADO   <-- decía LAB 02
lab-06  start-lab=0 health=200  destruir=0 huerfanos=0  90-validar=0 :: LAB 06 APROBADO
lab-07  start-lab=0 health=200  destruir=0 huerfanos=0  90-validar=0 :: LAB 07 APROBADO
```

Y los `99-destruir`, ahora diciendo la verdad porque la comprobaron:

```
[OK] API detenida (PID …)   [OK] PostgreSQL embebido detenido: no quedó ningún proceso
[OK] Archivos temporales del lab borrados
```

### F3 · Labs 08–14

```
bash -n: los 81 scripts OK
shellcheck (sin SC1091, informativo y preexistente): 0 hallazgos
```

Ejecutado lo que no requiere Docker: `95-recuperar.sh --help` en los siete, `EXIT=0` en todos.
El resto de sus scripts (`start-lab`, `90-validar`, `91-e2e`) **levanta contenedores**, así que
se quedan en `bash -n` + shellcheck, como prevé §5/F3. Su verificación funcional llegará con la
Fase 2, cuando esos labs se migren.

### F5 · Suites de solución — la FIX no tocó comportamiento Java

```
lab-01  EXIT=0 :: Tests run: 46, Failures: 0, Errors: 0, Skipped: 0
lab-02  EXIT=0 :: Tests run: 42, Failures: 0, Errors: 0, Skipped: 0
lab-03  EXIT=0 :: Tests run: 61, Failures: 0, Errors: 0, Skipped: 0
lab-04  EXIT=0 :: Tests run: 42, Failures: 0, Errors: 0, Skipped: 0
lab-05  EXIT=0 :: Tests run: 41, Failures: 0, Errors: 0, Skipped: 0
lab-06  EXIT=0 :: Tests run: 40, Failures: 0, Errors: 0, Skipped: 0
lab-07  EXIT=0 :: Tests run: 40, Failures: 0, Errors: 0, Skipped: 0
```

Respaldado por el diff: **0 archivos `.java` modificados** de los 35 tocados.

### F6 · Integridad

```
tronco -> lab01/solucion         [OK] solucion en sincronía con su base (dgt-tramites-api)
lab01/sol -> lab01/starter       [OK] starter en sincronía con su base (solucion)
lab01 -> lab02                   [OK]     lab04 -> lab05   [OK]
lab02 -> lab03                   [OK]     lab05 -> lab06   [OK]
lab03 -> lab04                   [OK]     lab06 -> lab07   [OK]

manifiestos: los 7 labs, starter:ok solucion:ok
```

La cadena tronco→07 sigue en sincronía y la frontera 07→08 igual que en `v0.3.0`. **No hizo
falta regenerar nada**, y la razón es estructural: la derivación compara los proyectos Maven
(`starter/`, `solucion/`), y los `bin/` viven un nivel más arriba, en el lab. Los cambios de
scripts no la mueven por construcción.

---

## 7 · Sorpresas y desviaciones

**7.1 · El bug de clase era mucho más grande que su instancia.** La SPEC anticipaba que
`borrar_seguro` fuera una instancia; lo fue de un patrón con **39 sitios mentirosos** en 35
archivos. El más extendido no era el del nombre, sino `"API detenida (PID N)"`: en 14 labs, el
script felicitaba tras un bucle de espera de 15 s sin comprobar si el proceso se había ido.

**7.2 · Mi primer detector no servía, y lo dice el propio inventario.** El heurístico inicial
—mirar tres líneas hacia atrás buscando `if`/`&&`— tenía falsos positivos (marcaba condicionales
de una sola línea como `[ "$X" -eq 1 ] && paso_ok`) y falsos negativos (se saltaba el bug
original, porque la línea anterior contenía un `&&` de otra cosa). Se reemplazó por un
clasificador que sigue la estructura real de bloques. Se registra porque el inventario solo vale
si el método que lo produjo se puede auditar.

**7.3 · A-04 mordió a su autor el mismo día.** Detallado en §5.

**7.4 · Los labs 13 y 14 no tienen el rótulo estándar, y está bien.** El 13 usa un veredicto
propio —*«NÚCLEO VERDE… y eso NO es la aprobación»*— porque es el examen final y su criterio no
es binario. Se dejó intacto: cambiarlo habría sido el refactor oportunista que §7 prohíbe.

**7.5 · Un `[OK]` que parecía mentira y no lo era.** Los diez `printf '[OK] Pruébalo tú mismo:'`
salían como incondicionales en el inventario. No se tocaron: son cabeceras de la lista de
`curl`, no llaman a `paso_ok`, no tocan el contador, y son inalcanzables si la app no arrancó.
Corregirlos habría sido ruido.

---

## 8 · Lo que queda

**Nada de esta FIX.** Los cuatro inquilinos están desalojados y verificados.

Heredado, sin cambios:

1. **Fase 2** — labs 08 a 14 a la receta autocontenida. El salto real está en el 08–11
   (WireMock en contenedor → in-process) y en el 13 (Jib, un lab de contenedores en un curso sin
   Docker: decisión del Arquitecto antes que técnica).
2. **Verificación funcional de los `bin/` de los labs 08–14.** Esta FIX los dejó correctos y
   linteados, pero solo se pudieron **ejecutar** los que no piden Docker. Cuando la Fase 2 los
   migre, sus ciclos completos deben correrse — y ahí se comprobará de verdad que los `[OK]`
   corregidos dicen lo que pasó.
3. **Rediseño de los `TODO_1`/`TODO_2`** del perfil `dev`, todavía marcados
   `PROVISORIO SPEC-022`.
4. **Los 30 `maven-wrapper.properties`** apuntando a `repo.maven.apache.org`. Nadie los lee en
   tronco ni labs 01–07; se limpian cuando el shim llegue a los catorce.
5. **Verificar el shim en Git Bash sobre Windows**, en sala.
