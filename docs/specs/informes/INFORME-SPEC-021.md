# INFORME-SPEC-021 · Emancipación de Docker — Fase 0

**SPEC:** SPEC-021 · **Ejecuta:** mocito · **Fecha de ejecución:** 12 de agosto de 2026
**Rama:** `spec-021-emancipacion-docker-fase-0`
**Máquina de ejecución:** Mac Studio del PO — Darwin 25.5.0, `arm64` (Apple Silicon)

---

## 1 · Veredicto en una línea

**POC BLOQUEADA EN G1** — `apus.sii.cl` no resuelve por DNS desde la máquina de
ejecución (NXDOMAIN). No hay ruta al Nexus interno, y por regla de la SPEC (§2, §8)
todo lo que sigue se detiene.

---

## 2 · Gates

### G1 · `apus` alcanzable — ❌ ROJO

```
$ curl -sf -o /dev/null -w '%{http_code}\n' http://apus.sii.cl:8081/repository/maven-sii-group/
000
exit=6
```

`exit=6` de curl es *couldn't resolve host*. Con `-v`:

```
* Could not resolve host: apus.sii.cl
* Closing connection
```

Confirmado por tres resolutores independientes:

```
$ nslookup apus.sii.cl
Server:		fe80::1%22
Address:	fe80::1%22#53
** server can't find apus.sii.cl: NXDOMAIN

$ dig +short apus.sii.cl
(sin salida)

$ host apus.sii.cl
Host apus.sii.cl not found: 3(NXDOMAIN)
```

**Control — el DNS de la máquina funciona:**

```
$ dig +short sii.cl
200.10.252.110
```

**Lectura del gate.** Esto no es "`apus` está caído" ni "el firewall bloquea el
puerto": es que el nombre no existe para esta máquina. El resolutor responde
NXDOMAIN de inmediato, no hay timeout. `sii.cl` público sí resuelve, así que la
máquina tiene DNS e internet. El diagnóstico es el que la SPEC anticipaba:
**la máquina no está en la red del SII — falta la VPN.** `apus.sii.cl` es un nombre
interno y solo se resuelve desde dentro.

### G2 · Zonky pasa por `apus` — ⛔ NO EJECUTADO

No se ejecutó, por dos razones independientes, y ambas importan:

**(a) El destino no existe para esta máquina.** Con G1 en rojo, `mvn dependency:get`
no puede alcanzar `apus` bajo ninguna circunstancia.

**(b) La premisa del gate es falsa aquí.** La SPEC dice «Con el `settings.xml` de la
máquina (que ya apunta a `apus`)». Ese archivo **no existe**:

```
$ ls -la ~/.m2/settings.xml
ls: /Users/rodrigosilva/.m2/settings.xml: No such file or directory

$ ls -la ~/.m2
drwxr-xr-x   15 rodrigosilva  staff   480 Aug 27  2025 .lemminx-maven
drwxr-xr-x   57 rodrigosilva  staff  1824 Jun 30 01:34 repository
drwxr-xr-x    3 rodrigosilva  staff    96 Aug 27  2024 wrapper
```

Esto es exactamente la trampa de la ⚠️ nota de honestidad de la SPEC, y conviene
dejarla escrita para que no muerda en el reintento: **aun con la VPN puesta, G2 tal
como está redactado habría resuelto contra Maven Central**, no contra `apus`, porque
sin `settings.xml` Maven usa Central por defecto. Habría dado tres ✅ verdes que no
prueban nada. *Un gate que pasa por la puerta equivocada es un gate que no pasó.*

**Requisito previo para reintentar G2:** instalar en la máquina de prueba el
`settings.xml` real de los alumnos del SII (el que declara el mirror hacia
`maven-sii-group`), y verificar procedencia con `-X` o con
`_remote.repositories` como manda la SPEC.

### G3 · Maven 3.9.11 en `apus` — ⛔ NO EJECUTADO (mismo NXDOMAIN)

Se corrió igual, para dejarlo citado. Falla por el mismo motivo que G1, antes de
llegar a preguntar por la versión:

```
$ curl -sf -o /dev/null -w '%{http_code}\n' -m 15 \
    http://apus.sii.cl:8081/repository/maven-sii-group/org/apache/maven/apache-maven/3.9.11/apache-maven-3.9.11-bin.zip
000
exit=6
```

No se probó el *fallback* a 3.9.9: sin resolución de nombre, la pregunta 3.9.11 vs.
3.9.9 no se puede formular. **Queda indecidida.**

### Versiones decididas

Ninguna. G2 y G3 son quienes deciden versión de binarios Zonky y versión de la
distribución de Maven, y ninguno de los dos pudo formularse. Nada quedó fijado.

---

## 3 · P1 — Vías A y B

**Ambas NO EJECUTADAS.** Las dos dependen de una decisión de G3 (qué versión de
Maven sirve `apus`), y G3 está indeciso. Crear `tools/reapuntar-wrapper.sh` para
reapuntar 30 archivos a una URL cuya validez nadie ha comprobado sería precisamente
el «workaround creativo» que prohíbe §8.

Nota sobre la Vía B: esta máquina *sí* podría bajar una distro de Maven desde
Central y commitearla en `tools/maven/`. **No se hizo a propósito.** La versión a
empaquetar la decide G3, y meter ~10 MB de binarios al repo con el gate en rojo es
comprometer el árbol por una decisión que aún no está tomada. Se hace cuando el
gate esté verde.

### Diagnóstico de P1 — verificado y confirmado

Lo que sí se hizo es comprobar el terreno, que no cuesta nada y no cruza el gate.
**El supuesto de la SPEC es exacto:**

```
$ find . -name maven-wrapper.properties -not -path './.git/*' | wc -l
30

$ find . -name maven-wrapper.properties -not -path './.git/*' -exec grep -h '^distributionUrl' {} \; | sort | uniq -c
  30 distributionUrl=https://repo.maven.apache.org/maven2/org/apache/maven/apache-maven/3.9.11/apache-maven-3.9.11-bin.zip
```

Son 30, contados dinámicamente, y los 30 comparten **exactamente la misma URL** — un
único `sed` los arregla a todos. Dato adicional útil para el script: **ningún archivo
declara `wrapperUrl`** (el `uniq -c` sale vacío), así que la única URL a reapuntar es
`distributionUrl`. La superficie de P1 es menor de lo que se temía.

---

## 4 · POC Lab 01

**NO EJECUTADA** — bloqueada por G2, que es el gate del que depende toda la Vía 4.

Se levantó el mapa del terreno (solo lectura, sin modificar nada) para que la Fase 1
arranque sin redescubrirlo. **Las cuatro capas de P2 están confirmadas al pie de la
letra**, y aparecieron dos cosas que la SPEC no enumera:

| Capa | Ubicación confirmada | Estado |
|---|---|---|
| 1 · `spring-boot-docker-compose` | `starter/pom.xml:85`, `solucion/pom.xml:85` | Confirmado |
| 1b · Testcontainers | `pom.xml:106` (`spring-boot-testcontainers`), `:114` (`testcontainers-postgresql`), `:119` (`testcontainers-junit-jupiter`), en ambos | Confirmado |
| 2 · `compose.yaml` | `starter/compose.yaml`, `solucion/compose.yaml` (2040 bytes, idénticos) | Confirmado |
| 3 · `PostgreSQLContainer` | `{starter,solucion}/src/test/java/cl/dgt/tramites/web/ContratoRn03IT.java` y `.../dominio/SemillaCoherenteIT.java` — 4 archivos exactos | Confirmado |
| 4 · guard `docker info` | `bin/start-lab.sh:54` | Confirmado |

**Hallazgo extra 1 — Docker también vive en `99-destruir.sh`.** No es solo el guard
de `start-lab.sh`:

```
bin/99-destruir.sh:50:if docker info >/dev/null 2>&1; then
bin/99-destruir.sh:54:  ( cd "$DIR_LAB/$PROYECTO" && docker compose down -v >/dev/null 2>&1 ) && BAJADOS=$((BAJADOS + 1))
bin/99-destruir.sh:59:  SOBRANTES="$(docker ps -q --filter label=org.testcontainers 2>/dev/null | wc -l | tr -d ' ')"
```

La SPEC §5.4 ya pedía revisar este script, así que está dentro de alcance — pero hay
lógica real que reemplazar, no solo mensajes: el ciclo de destrucción del lab hoy
*delega en Docker* la limpieza. Con Zonky hay que destruir el directorio temporal y
el proceso hijo, y hacerlo **por ruta exacta del proyecto** (ley post-ALCHEMIA).

**Hallazgo extra 2 — hay una QUINTA capa: `application-dev.yml`.** La SPEC enumera
cuatro y esta no está. El archivo enciende el compose explícitamente:

```yaml
spring:
  docker:
    compose:
      enabled: true
```

y —más delicado— su cabecera pedagógica **le enseña el modelo mental al alumno** y
sus `TODO_1`/`TODO_2` son parte del ejercicio evaluado:

```
#  Boot levanta el compose.yaml y cablea el DataSource solo. Tú no escribes
#  ninguna cadena de conexión aquí, y por tanto no hay ninguna que filtrar.
#
#  TODO_1 y TODO_2 — te faltan dos perfiles hermanos de este:
#    application-test.yml  · para la suite: sin compose (lo levanta Testcontainers)
#    application-prod.yml  · para el servidor: la conexión llega por VARIABLES DE...
```

Esto **no es limpieza mecánica**: reescribir ese texto cambia el enunciado de un
ejercicio del alumno. Requiere criterio del Arquitecto, no del ejecutor. Se sube
como decisión pendiente, no se toca.

**Dónde vive hoy la nota pedagógica a migrar** (§5.2): `starter/compose.yaml:7-19`,
«⚠️ NOTA DEL PECADO ORIGINAL — léela antes de acusarnos de hipócritas», sobre por qué
la credencial `dgt-dev` versionada no es el crimen y `application.yml` con
credenciales de producción sí (D-012). Está íntegra y localizada; no se pierde.
Destino natural, ahora que se conoce el hallazgo 2: **`application-dev.yml`**, que es
donde el alumno ya está leyendo y donde va a quedar la explicación del nuevo
mecanismo.

**Decisión plan A / plan B en los tests:** indecidida. Requiere ejecutar contra
Zonky real, que es lo que G2 bloquea.

**Reglas ArchUnit:** viven en `{starter,solucion}/src/test/java/cl/dgt/tramites/arquitectura`.
No se ejecutaron ni se tocaron.

---

## 5 · Tabla de verificación

Ninguna prueba se ejecutó: V1–V7 presuponen el POC construido, y el POC está
bloqueado en G2. Se registran en rojo para que nadie las lea como pendientes menores.

| # | Prueba | Resultado |
|---|---|---|
| V1 | `starter && ./mvnw test` | ⛔ NO EJECUTADO — depende del POC (G2) |
| V2 | `solucion && ./mvnw test` | ⛔ NO EJECUTADO — ídem |
| V3 | `start-lab.sh` + curls + `99-destruir.sh` | ⛔ NO EJECUTADO — ídem |
| V4 | `90-validar.sh` en starter y solución | ⛔ NO EJECUTADO — ídem |
| V5 | Cronometraje vs. Testcontainers | ⛔ NO EJECUTADO — ídem |
| V6 | `java -version` junto a V1 | ⛔ NO EJECUTADO — ver desviación §7.3 |
| V7 | `ps` filtrado por ruta del proyecto | ⛔ NO EJECUTADO — ídem |
| V8 | `./mvnw help:effective-settings` muestra el mirror de `apus` *(añadida por A1.8)* | 🟡 **PARCIAL** — el cableado está probado, la impresión de las settings efectivas no. Ver Addendum A1.4 |

---

## 6 · Comparativa de tiempos

**No disponible.** Sin POC construido no hay lado "Zonky" que medir.

Sí se deja constatado que **la línea base de Testcontainers es medible en esta
máquina** cuando corresponda, porque el demonio Docker está vivo aquí:

```
$ docker info --format '{{.ServerVersion}}'
28.5.1
```

Es decir: cuando el POC exista, V5 se puede correr entera en este Mac —
Testcontainers en `main` y Zonky en la rama— sin pedir prestada otra máquina.
Lo que hay que recordar es lo contrario de lo habitual: para V1–V4 **hay que apagar
Docker a propósito**, porque en esta máquina está encendido y podría dar un verde
falso.

---

## 7 · Sorpresas y desviaciones

**7.1 · G1 no falló como se esperaba, y el matiz cambia la acción.** La SPEC
anticipaba un firewall del SII. Lo que hay es NXDOMAIN: el nombre no existe para el
resolutor. No es "bloqueado", es "invisible". La acción correctiva no es abrir
puertos: es **conectar la VPN** (o ejecutar esta SPEC desde una máquina dentro de la
red del SII). Mientras esto se corra desde el Mac del PO sin VPN, G1 va a fallar
siempre.

**7.2 · No existe `~/.m2/settings.xml` — la premisa de G2 es falsa.** Detallado en
§2. Es la desviación más importante del informe, porque es la que podría haber
producido un falso verde. Antes de reintentar hay que instalar el `settings.xml` del
SII en la máquina de prueba.

**7.3 · El Java activo es 21, no 25.** V6 pide evidencia explícita de Zonky bajo Java
25. Hoy la shell entrega:

```
$ java -version
openjdk version "21.0.1" 2023-10-17
OpenJDK Runtime Environment GraalVM CE 21.0.1+12.1
$ echo $JAVA_HOME
/Users/rodrigosilva/.sdkman/candidates/java/current
```

El `.sdkmanrc` del repo fija `java=25-tem` (Temurin 25, no GraalVM), pero **no está
aplicado a esta sesión** — falta `sdk env`, o `sdkman_auto_env=true`. Quien reintente
debe correr `sdk env` primero y citar el `java -version` resultante; si no, V6
mediría la compatibilidad de Zonky con el JDK equivocado y la incógnita central de
la SPEC (¿Zonky bajo Java 25?) quedaría sin responder creyendo que se respondió.

**7.4 · Desviación por adición: reconocimiento contra Maven Central.** Hice una
consulta de solo lectura (`curl` a `maven-metadata.xml`, sin descargar artefactos ni
tocar `~/.m2`) para saber si los artefactos de Zonky **existen en el mundo**. No lo
pedía la SPEC. Lo hice porque no cruza ningún gate —no desbloquea `apus`, no
construye nada— y responde barato una incógnita real de la Fase 1: si
`darwin-arm64v8` no existiera, la Fase 1 nacía coja aunque `apus` estuviera perfecto.

> **⚠️ ESTO NO ES G2 Y NO LO SUSTITUYE.** Es Central, no `apus`. No dice absolutamente
> nada sobre qué sirve el Nexus del SII. G2 sigue rojo.

Resultados:

| Artefacto | Existe en Central | 16.x más alta | `16.4.0` |
|---|---|---|---|
| `io.zonky.test.postgres:embedded-postgres-binaries-bom` | Sí | `16.14.0` | — |
| `…:embedded-postgres-binaries-windows-amd64` | Sí | `16.14.0` | HTTP 200 |
| `…:embedded-postgres-binaries-darwin-arm64v8` | Sí | `16.14.0` | HTTP 200 |
| `io.zonky.test:embedded-postgres` | Sí | release `2.2.2` | `2.1.0` existe |
| `io.zonky.test:embedded-database-spring-test` (plan B) | Sí | release `2.8.0` | — |

Tres cosas para el Arquitecto: (a) **`darwin-arm64v8` existe** — la plataforma del PO
está cubierta, incógnita resuelta; (b) la línea 16.x llega hasta **16.14.0**, así que
si `apus` no tiene 16.4.0 hay diez versiones más arriba donde caer; (c) la SPEC fija
`embedded-postgres:2.1.0` pero el release actual es **2.2.2** — vale la pena revisar
si conviene la más nueva, sobre todo por Boot 4.1 / Java 25.

**7.5 · Docker está encendido en la máquina de ejecución** (28.5.1). Ver §6: obliga a
apagarlo explícitamente para V1–V4.

**7.6 · Hallazgos de alcance en el Lab 01.** Ver §4: lógica Docker en
`99-destruir.sh` (no solo mensajes) y la quinta capa `application-dev.yml`, cuyo
texto es contenido evaluado del curso.

**7.7 · `docs/specs/informes/` no existía.** Se creó para alojar este informe.

---

## 8 · Lo que queda para la Fase 1

Bloqueantes, en orden — nada de lo demás avanza sin los dos primeros:

1. **Conectar la VPN del SII** (o mover la ejecución a una máquina dentro de la red)
   y volver a correr G1. Es el único bloqueo real hoy.
2. **Instalar el `settings.xml` del SII** en la máquina de prueba antes de G2, y
   verificar procedencia con `-X` / `_remote.repositories`. Sin esto G2 miente.
3. **Correr `sdk env`** (Temurin 25) antes de cualquier medición, y citar
   `java -version` en la evidencia.

Reintento de la SPEC-021 tal cual, una vez destrabado lo anterior: G1 → G2 → G3 →
Vía A → Vía B → POC Lab 01 → V1-V7. Nada de esta SPEC quedó consumido; se reintenta
completa.

Decisiones que la Fase 1 debe tomar y que este informe deja preparadas:

4. **Versión de Zonky**: `embedded-postgres` 2.1.0 (SPEC) vs. 2.2.2 (actual), y qué
   16.x sirve `apus` — hay hasta 16.14.0 en Central como plan de caída.
5. **Redacción de `application-dev.yml`** (quinta capa): reescribir la cabecera
   pedagógica y los `TODO_1`/`TODO_2`, que hoy le enseñan al alumno "Boot levanta el
   compose" y "la suite la levanta Testcontainers". Es contenido del curso —
   **decisión del Arquitecto**, no del ejecutor.
6. **Ciclo de destrucción sin Docker**: `99-destruir.sh` hoy delega la limpieza en
   `docker compose down -v`. Con Zonky hay que matar el proceso hijo y borrar el
   temporal **por ruta exacta del proyecto**, jamás `pkill postgres`.
7. **Las dos incógnitas de fondo siguen abiertas**, porque solo se responden
   ejecutando: Zonky bajo **Java 25** y bajo **Spring Boot 4.1.0**, y su tiempo de
   arranque frente a Testcontainers.

Sin cambios pendientes de reconciliación: **no se modificó ningún manifiesto**, ni el
del Lab 01 ni los otros 23. La cadena tronco→lab01 sigue intacta en esta rama.

---
---

# Addendum A1 · Anotaciones posteriores al informe

> Anotaciones A1.1 a A1.9, emitidas por el PO tras leer el informe de arriba. El
> informe original **no se modificó** —salvo la fila V8 de la tabla— para que quede
> como registro de lo que se supo en cada momento. Todo lo nuevo vive aquí.

## A1.0 · Qué se ejecutó y qué no

| Anotación | Estado |
|---|---|
| A1.1 · Push y PR en draft | ✅ Hecho — `e091e7b` pusheado, **PR #26** en draft |
| A1.2 / A1.9 · `tools/settings-sii.xml` | ✅ Hecho — sin `<servers>`, con encabezado |
| A1.8 · `.mvn/maven.config` en `starter/` y `solucion/` | ✅ Hecho y verificado |
| A1.8 · V8 | 🟡 Parcial — ver A1.4 abajo |
| A1.3 · G0 · Precondiciones | ⛔ Pendiente — necesita la ventana con VPN |
| A1.4 · G2 reescrito · A1.9 · G2.5 | ⛔ Pendientes — ídem |
| A1.5 / A1.6 / A1.7 | 📝 Registradas para Fase 1 / POC; nada que ejecutar hoy |

## A1.1 · El settings versionado difiere del archivo del PO en un punto

A1.2 describía el archivo entregado como «el de `mirrorOf=*`, id `central`». El
archivo real dice **lo inverso**: `<id>maven-sii-apus</id>` con
`<mirrorOf>central</mirrorOf>`. Se reportó al PO, y A1.9 resolvió versionar
**`mirrorOf=*`**. Es, por tanto, una **divergencia deliberada** respecto del archivo
probado en cancha, y queda anotada como tal:

| | Archivo del PO | `tools/settings-sii.xml` versionado |
|---|---|---|
| `<id>` del mirror | `maven-sii-apus` | `maven-sii-apus` *(igual)* |
| `<url>` | `http://apus.sii.cl:8081/repository/maven-sii-group/` | *(igual, `http://` incluido)* |
| `<mirrorOf>` | `central` | **`*`** ← divergencia A1.9 |
| `<servers>` (5 bloques) | presentes | **ausentes** ← A1.9 |
| `<profiles>` / `<activeProfiles>` | presentes | *(iguales)* |
| Finales de línea | CRLF | LF *(normalizado por `.gitattributes`)* |

Por qué `*` es mejor que `central`, más allá de lo que diga cada archivo:
`mirrorOf=central` solo canaliza el repositorio `central`. Cualquier otro repositorio
que un `pom` declare por su cuenta esquiva el mirror; en el SII eso falla, pero fuera
del SII **resuelve contra internet y el gate pasa en verde por la puerta equivocada**.
Con `*` no hay puertas laterales. Es la misma trampa de la nota de honestidad de la
SPEC, cerrada por construcción.

**Credenciales:** el archivo original traía cinco bloques `<server>` con la
**credencial de despliegue del Nexus (retenida, fuera del repo)**. No entra a ningún
archivo versionado, este informe incluido. El original queda intacto en su ubicación
fuera del árbol; no se copió, no se movió, no se borró. Verificado:

```
$ grep -rn 'deployment123\|<password>' --exclude-dir=.git .
(0 coincidencias)
```

## A1.2 · Corrección a A1.4 — la evidencia de procedencia que hay que buscar NO es «central»

A1.4 fija como prueba de procedencia la línea `Downloading from central:
http://apus.sii.cl:8081/...`. **Esa línea no va a existir nunca**, porque el mirror no
se llama `central`: se llama `maven-sii-apus`. Maven nombra el mirror por su `id`. Lo
que sale en el log —verificado hoy contra el settings ya versionado— es:

```
[INFO] Downloading from maven-sii-apus: http://apus.sii.cl:8081/repository/maven-sii-group/org/springframework/boot/spring-boot-starter-parent/4.1.0/spring-boot-starter-parent-4.1.0.pom
```

**Criterio corregido para G2**, que es el que debe usarse en la ventana con VPN:

- ✅ Gate **VÁLIDO** si el log dice `Downloading from maven-sii-apus: http://apus.sii.cl:8081/...`
- ❌ Gate **INVÁLIDO** si aparece `repo.maven.apache.org` en cualquier línea de descarga

Corolario: como el mirror **no** se llama `central`, `_remote.repositories` tampoco
mentiría —registraría `maven-sii-apus`—, así que el método que A1.4 descartó sigue
sirviendo como contraste independiente. El razonamiento de A1.4 partía de la premisa
equivocada, pero su conclusión (usar el log) es la correcta y más directa.

## A1.3 · A1.8 ejecutado — y la ruta relativa no es la que parece

`.mvn/maven.config` creado en `starter/` y `solucion/`, una línea, idéntica en ambos:

```
-s ../../../../../tools/settings-sii.xml
```

**Cinco niveles, no tres.** Éste es el hallazgo técnico de la anotación: Maven **no**
resuelve el `-s` de `maven.config` contra el directorio de trabajo, sino contra la
ruta del propio archivo `maven.config`. Es decir, hay que subir dos niveles extra
para compensar `.mvn/maven.config`. Medido empíricamente, no deducido:

```
N=3  -> .../labs/lab-01-del-otro-lado-del-boton/tools/settings-sii.xml   (no existe)
N=4  -> .../labs/tools/settings-sii.xml                                  (no existe)
N=5  -> RESUELVE
```

La ruta intuitiva (`../../../`, tres niveles desde `starter/`) habría dejado a todo el
Lab 01 con un `The specified user settings file does not exist` desde el primer
comando. Verificado que funciona en `starter/` **y** en `solucion/`.

**Compatibilidad en Git Bash:** anotada para verificar en sala, según A1.8. La barra
inclinada `/` no debería dar problema —Java normaliza separadores— pero no está
comprobado en Windows y no se declara verde.

**Recomendación para Fase 1 (no ejecutada, es decisión del Arquitecto):** la ruta de
cinco niveles funciona pero es frágil de una forma que A1.8 no anticipaba — no entre
plataformas, sino **ante la copia de la carpeta**. Si un alumno copia `starter/` a su
escritorio (cosa que los alumnos hacen), la ruta apunta al vacío y el error no dice
nada útil. La alternativa que A1.8 dejaba abierta —copiar el settings dentro del
`.mvn/` de cada proyecto, con `-s ../../.mvn/settings-sii.xml`— hace cada proyecto
autocontenido a cambio de tres copias del mismo archivo en el repo.

## A1.4 · V8 · Resultado: PARCIAL, y por una razón que importa

**Lo que sí quedó probado: el cableado funciona.** Con el `maven.config` puesto, Maven
enruta a `apus` sin que nadie toque `~/.m2`, que era el objetivo entero de A1.8:

```
$ cd labs/lab-01-del-otro-lado-del-boton/starter && ./mvnw -B help:effective-settings
[INFO] Artifact org.springframework.boot:spring-boot-starter-parent:pom:4.1.0 is present in the
       local repository, but cached from a remote repository ID that is unavailable in current
       build context, verifying that is downloadable from
       [maven-sii-apus (http://apus.sii.cl:8081/repository/maven-sii-group/, default, releases+snapshots)]
[INFO] Downloading from maven-sii-apus: http://apus.sii.cl:8081/repository/maven-sii-group/...
[FATAL] ... Unknown host apus.sii.cl: nodename nor servname provided, or not known
```

**Lo que no se pudo producir: la impresión de las settings efectivas.** El goal
`help:effective-settings` necesita resolver antes el POM padre del proyecto
(`spring-boot-starter-parent:4.1.0`) y, con `mirrorOf=*`, esa resolución va a `apus`,
que sigue sin resolver por DNS. V8 completo queda para la ventana con VPN. La línea
`Downloading from maven-sii-apus:` es, mientras tanto, evidencia más fuerte que la que
V8 pedía: no muestra el mirror *configurado*, lo muestra *interceptando*.

## A1.5 · ⚠️ Consecuencia mayor de A1.8: el Lab 01 ya no compila fuera del SII

Esto no estaba previsto en ninguna anotación y hay que decidirlo antes de mergear.

Maven marca cada artefacto de la caché local con el `id` del repositorio del que vino.
Al cambiar el mirror a `maven-sii-apus`, **todo lo cacheado desde `central` deja de ser
utilizable** y Maven exige revalidarlo contra el nuevo remoto. No es que la
compilación sea más lenta: **es imposible**. Y no basta con desconectar la red —
probado también en modo offline, con la caché caliente:

```
$ ./mvnw -B -o help:effective-settings
[FATAL] Non-resolvable parent POM: ... (present, but unavailable): Cannot access maven-sii-apus
        (http://apus.sii.cl:8081/repository/maven-sii-group/) in offline mode and the artifact
        org.springframework.boot:spring-boot-starter-parent:pom:4.1.0 has not been downloaded
        from it before.
```

Traducido: en cuanto esto se mergee, **nadie fuera de la red del SII puede correr
`./mvnw` en el Lab 01** — ni el PO en su Mac, ni el instructor preparando la clase, ni
un futuro runner de CI si algún día se le añade el Lab 01. El artefacto *está* en el
disco y Maven se niega a usarlo.

Para el alumno del SII esto es exactamente lo que se quería. Para todos los demás es
una puerta cerrada, y conviene que sea una decisión y no una sorpresa.

**Escotilla de escape, verificada:** un `-s` en la línea de comandos **gana** sobre el
de `maven.config`. Quien necesite compilar fuera del SII puede:

```
$ ./mvnw -s /ruta/a/otro-settings.xml test      # BUILD SUCCESS, cero líneas con apus
```

Comprobado hoy contra un settings vacío: `BUILD SUCCESS`, ninguna referencia a `apus`.
Vale la pena documentarlo en el README del lab si A1.8 se mantiene tal cual.

## A1.6 · G2.5 registrado

Sub-gate nuevo (A1.9): los `dependency:get` de G2 se corren con este settings **sin
credenciales**, comprobando que `apus` permite lectura anónima de `maven-sii-group`.
Si devuelve `401`/`403` → **detenerse y reportar**. La decisión (lectura anónima vía
TI, o la credencial interpolada por `${env.*}`) es del PO. No se improvisa auth.

Queda además advertido en el propio encabezado del `settings-sii.xml`, para que quien
lo lea en frío no caiga en la tentación de pegar la credencial ahí.

## A1.7 · Lo que cambia en «qué queda para la Fase 1»

Se mantiene la lista de §8, con estos ajustes:

- El punto 2 («instalar el `settings.xml`») **queda resuelto**: ya vive en el árbol y
  se aplica solo en el Lab 01. Lo que queda es comprobar que funciona contra `apus`
  de verdad (G2 + G2.5).
- Se añade: **decidir sobre A1.5** (Lab 01 inservible fuera del SII) antes de mergear.
- Se añade: **verificar la ruta de cinco niveles en Git Bash**, en sala.
- Se añade: **evaluar el `.mvn/` autocontenido** frente a la copia de carpeta.
- El criterio de procedencia de G2 es el de A1.2 de este addendum, no el de A1.4.
