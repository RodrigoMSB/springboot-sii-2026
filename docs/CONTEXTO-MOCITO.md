# CONTEXTO-MOCITO.md

**Lo primero que leo cuando me reinician.** Si estoy arrancando sin memoria de este repositorio,
este archivo tiene lo que necesito para trabajar sin preguntar nada obvio.

*Escrito el 27 de agosto de 2026. Revisado el 28 de agosto de 2026, sobre `main` en
`material-v1.5.0`, con el PO dictando el lab 05 mientras se revisaba.*

---

## 1 · Qué es esto

El material del **curso de Spring Boot para el SII**, año 2026. Repositorio
`springboot-sii-2026`, en `~/SOFTWARE/SII/XPERTIS/springboot-sii-2026`. Privado, en GitHub,
cuenta `RodrigoMSB`.

No es una aplicación: es **material de enseñanza**. Lo que aquí se llama "el producto" son
quince laboratorios, un proyecto de evaluación, el temario y el tooling que verifica que todo
eso siga siendo verdad.

---

## 2 · Quiénes somos

| Rol | Quién | Qué hace |
|---|---|---|
| **Product Owner** | Rodrigo (`rodrigo.silva@neitcom-compliance.cl`) | Decide. Aprueba las SPEC. Dicta el curso. Es quien corre la aceptación final |
| **Arquitecto** | Claude | Emite las SPEC numeradas |
| **Ejecutor** | yo, *el mocito* | Las ejecuta, deja la evidencia en el repositorio y maneja git y GitHub |

**Tengo autorización permanente de commit, PR, merge y tag.** No hay que pedirla cada vez. El PO
me pasa una SPEC numerada y espera que trabaje de corrido: ejecución, informe con evidencia
citada, rama, PR, merge y tag, sin interrumpirlo por el camino.

**Lo que sí sigue siendo del PO:** la fila de aceptación. El PO jamás es el primero en correr
algo — yo corro todo primero, sobre estado limpio, y cito la salida.

---

## 3 · Las leyes de la casa

Están escritas en tres sitios, y los tres mandan:

- **`ESTADO.md`** — qué existe hoy, qué falta y qué viene. Una página, sin jerga. **Toda SPEC lo
  actualiza al cerrar. Un `ESTADO.md` desactualizado es un bug del material, no un descuido.**
- **`docs/decisiones.md`** — **102** decisiones con fecha y razón. Las que llevan identificador
  (`D-022-1`, `D-031-2`, `D-FIX10-2`…) se citan por su código en informes y comentarios.
- **`docs/specs/informes/`** — un informe por SPEC ejecutada. Es la memoria larga del proyecto:
  qué se hizo, qué se midió y con qué salida. **Cuando algo no cuadra, la respuesta suele estar
  aquí antes que en el código.**
- **`MANIFIESTO.md`** — por qué el curso se enseña así. Se lee antes de dictar.
- **`docs/adn/adn-cypress.md`** — el ADN destilado de un curso anterior: `P-01` a `P-18`
  (prácticas verificadas) y `A-01` a `A-04` (anti-herencias). De ahí salen las reglas que más se
  citan.

### Las que más me caen encima

- **`A-02` · No se declara sin medir.** Nada se da por bueno sin la salida que lo prueba.
- **`A-04` · No se mide sin mostrar.** Todo arnés de verificación imprime **todo** lo que calcula.
- **`P-05` · Nada de gates decorativos.** Un job que pasa siempre protege menos que no tenerlo.
  Es la razón de que `instructor/` se verifique a mano y no en el CI (`D-FIX10-2`).
- **`A-01` · Verificar código fuente con `grep` es frágil por construcción.** Cuando se pueda,
  se parsea.
- **`D-041-1` · En `instructor/`, toda decisión técnica documenta cuatro cosas y en este orden:**
  qué hace, qué alternativas existen, por qué se eligió ésta aquí, en qué caso elegirías otra.
  El recuadro se encabeza `POR QUÉ ·` para poder listarlo con un `grep`. Donde no hay
  alternativa real, se dice y se pasa: no se inventan opciones para rellenar el formato.
- **`D-042-1` · `instructor/` se respalda en un repositorio PRIVADO aparte.** `D-031-2` no
  cambia: el repositorio público sigue sin llevarla.
- **`D-022-3` · Offline por defecto.** `--offline` es el contrato, no una limitación.
- **`D-022-4` · Git LFS prohibido, sin excepciones.**
- **`D-022-5` · Techo de 95 MB por archivo.** GitHub rechaza los de más de 100.
- **`D-FIX10-1` · En un comentario XML no se escriben dos guiones seguidos.** Las reglas
  decorativas de los `pom.xml` documentados van con `=`. `--` dentro de un comentario hace el
  documento no parseable.
- **Una SPEC es inmutable desde que CIERRA** (merge de su PR), no desde que se commitea.
- **Si la ejecución difiere de la SPEC, la SPEC manda y la discrepancia se reporta.**

### El tono de la casa

Los documentos de este repositorio están escritos en **castellano, en prosa llana, sin jerga y
sin adornos**. Se nombran números concretos («201 consultas contra 1», «30,01 s → 0,002 s»),
se cita la salida real, y se dice lo que **no** se hizo con la misma claridad que lo que sí.
Nunca se declara verde algo que no se miró. Los títulos de commit y de sección son frases, no
etiquetas. Cuando escribo, escribo así.

---

## 4 · La estructura

```
springboot-sii-2026/
├── ESTADO.md                  ← empieza aquí para saber en qué va el curso
├── MANIFIESTO.md              ← por qué se enseña así
├── README.md                  ← protocolo SPEC, roles, las tres carpetas
├── .gitattributes             ← finales de línea y binarios. La portabilidad
├── .sdkmanrc                  ← java=25-tem
├── docs/
│   ├── CONTEXTO-MOCITO.md     ← este archivo
│   ├── decisiones.md          ← 102 decisiones con fecha y razón
│   ├── adn/adn-cypress.md     ← P-01..P-18, A-01..A-04
│   ├── entorno-alumno.md
│   ├── guion-reinicio-de-sala.md
│   ├── troubleshooting.md     ← tabla de problemas con número. Se cita el número
│   ├── specs/                 ← SPEC-NNN-*.md
│   │   └── informes/          ← INFORME-SPEC-NNN.md  (la memoria larga)
│   ├── spikes/
│   └── temario/
│       ├── TEMARIO-SPRING-BOOT-SII-v3.md    ← el .md MANDA
│       ├── TEMARIO-SPRING-BOOT-SII-v3.docx  ← lo que se entrega al SII
│       ├── MAPA-LAB-MODULO.md               ← trazabilidad lab ↔ módulo
│       └── README.md
├── labs/
│   ├── lab-00-hola-mundo ... lab-14-microservicios   ← quince, del 00 al 14
├── proyecto-final/            ← el instrumento de evaluación (50 % de la nota)
│   ├── base/ brief/ rubrica/ plantillas/ instructor/
├── repo-maven/                ← TODAS las dependencias, ~230 MB
├── tools/
│   ├── jdk/                   ← JDK 25 Temurin, partido en trozos de 80 MB
│   ├── maven/                 ← la distribución de Maven
│   ├── jib-base/              ← capas de eclipse-temurin:25-jre (lab 13)
│   ├── settings-sii.xml       ← el Nexus del SII, plan B, sin credenciales
│   ├── instructor-respaldo.sh ← el puente con el repositorio privado (D-042-1)
│   ├── verificar-temario.py
│   ├── verificar-instructor.py
│   ├── verificar-pasos-copiables.py
│   ├── verificar-guion-vs-practica.py
│   └── verificar-tamanos.sh
└── .github/workflows/material-ci.yml   ← seis jobs
```

### La anatomía de un lab

Los quince tienen exactamente la misma forma:

```
lab-NN-nombre/
├── README.md      ← qué se lleva el alumno
├── PASOS.md       ← EL GUION DE CLASE. Con el código listo para pegar
├── practica/      ← donde trabaja el alumno. SIN una línea de documentación
├── solucion/      ← el proyecto terminado, comentarios BREVES
└── instructor/    ← los mismos archivos, explicados línea por línea
```

- **`practica/`** — la firma, una línea imperativa y `// escribe aquí`. Nada más.
- **`solucion/`** — completa, con uno o dos comentarios donde algo no es evidente.
- **`instructor/`** — **NO VIAJA A ESTE REPOSITORIO** (`labs/*/instructor/` en el `.gitignore`,
  decisión `D-031-2`). No es un proyecto: no tiene `mvnw` ni `.mvn` y no se compila. Es la
  chuleta de quien dicta. La genera quien prepara la sesión, a partir de `solucion/`.
  Lo mismo con `proyecto-final/instructor/`, que lleva la solución de referencia.
  **Desde la SPEC-042 sí tiene respaldo**, en un repositorio privado aparte
  (`springboot-sii-2026-instructor`): dieciséis carpetas, **245 archivos**, las mismas rutas.
  **En los labs 04 a 07** cada decisión técnica lleva además su recuadro `POR QUÉ ·` — son
  **102** (SPEC-041). Los otros once labs y el proyecto final aún no.
- **`PASOS.md`** — el guion de la sesión. Cada paso trae un bloque **«Se pega»** con el código
  exacto, el archivo y el sitio. Está extraído de `solucion/` y **el CI vigila que no le
  prometa al instructor un código que la solución ya no tiene.**

**Consecuencia práctica que se olvida cada vez:** si trabajo en `instructor/`, **el CI no lo ve
y el commit no lo lleva**. Lo que viaja de ese trabajo es el informe. La verificación se corre a
mano con `python3 tools/verificar-instructor.py`, y **el respaldo también: nada lo dispara.**
Si toco `instructor/`, al terminar corro `tools/instructor-respaldo.sh respaldar` o el respaldo
se queda atrás sin avisar (INFORME-SPEC-042 §9).

### Los quince labs y su puerto

| Lab | Tema | Puerto | El número que se proyecta |
|---|---|---|---|
| 00 | `hola-mundo` | — | que arranque |
| 01 | `web` | 8082 | ruta, parámetro, cuerpo, `ResponseEntity` |
| 02 | `di` | 8084 | **qué es Spring**: `/productos/quien` |
| 03 | `errores` | 8086 | 404 con cuerpo, 400 con los campos |
| 04 | `jpa` | 8100 | una clase y una tabla son la misma cosa |
| 05 | `relaciones` | 8088 | 1 SELECT con LAZY, 4 con EAGER |
| 06 | `rendimiento` | 8090 | el N+1: 201 consultas contra 1 |
| 07 | `concurrencia` | 8092 | 20 emisiones a la vez: 21 de 21 con candado |
| 08 | `testing` | 8094 | el rojo provocado. 0,03 s → 0,7 s con Spring |
| 09 | `seguridad` | 8096 | 401 sin token, 403 sin rol |
| 10 | `resiliencia` | 8098 | 30,01 s → 0,002 s |
| 11 | `observabilidad` | 8102 | liveness 200, readiness 503 |
| 12 | `tareas` | 8104 | 3,03 s → 0,004 s |
| 13 | `empaquetado` | 8106 | imagen OCI de 138,9 MB sin Docker ni red |
| 14 | `microservicios` | 8213 | cuatro procesos, tres bases. 500 → 200 degradado |

`proyecto-final/base` corre en el **8107**. El puerto histórico del curso es el **8099**.

---

## 5 · Las tres restricciones que lo explican casi todo

Las máquinas de los alumnos del SII:

1. **No tienen Docker y no pueden instalarlo** (sin permisos de administrador).
2. **Su firewall bloquea todo internet** salvo un Nexus interno que no se puede probar desde
   fuera. **No habrá VPN jamás.**
3. **GitHub sí funciona.** Es la única puerta confirmada abierta.

De ahí sale todo el diseño: **todo lo que el curso necesita viaja DENTRO del repositorio.**

- **El JDK 25** (Temurin `jdk-25.0.4+7`, Windows x64 y Mac Apple Silicon) va partido en trozos
  en `tools/jdk/`. El shim `./mvnw` los ensambla la primera vez, **verifica el sha256** y usa
  ese JDK **ignorando cualquier Java que la máquina tenga**. No toca `JAVA_HOME` ni `PATH` del
  usuario: solo el proceso de Maven.
- **Maven y todas las dependencias** en `tools/maven/` y `repo-maven/`. `./mvnw` no descarga
  nada: es un shim en modo offline.
- **PostgreSQL** llega como dependencia Maven (Zonky), se extrae a una carpeta temporal y corre
  como proceso hijo del JVM. Es PostgreSQL 16.14 de verdad, no un H2 disfrazado.
- **WireMock** (el servicio Tesorería, desde el lab 08) corre como **librería in-process**, no
  como contenedor.
- **La imagen base de Jib** en `tools/jib-base/`, para que el lab 13 construya su imagen OCI
  sin red.

**La lista de prerrequisitos del curso es una palabra: Git.**

La prueba que vale es el **modo avión**: clon fresco, `~/.m2` apartado, cable desenchufado.
Cero descargas intentadas. Si funciona sin red, funciona detrás de cualquier firewall.

---

## 6 · Comandos habituales

### Correr un lab

```bash
cd labs/lab-04-jpa/solucion      # o practica/
./mvnw spring-boot:run           # en Windows: mvnw.cmd
./mvnw test
./mvnw verify
```

`./mvnw` es el shim del curso: usa el JDK y el Maven del repositorio, en modo offline. **Nunca
se llama a un `mvn` del sistema.** Los labs se apagan con Ctrl+C.

### Los verificadores

```bash
python3 tools/verificar-temario.py            # .md <-> .docx no divergen (pide python-docx)
python3 tools/verificar-instructor.py         # instructor/ al día con solucion/ + XML válido
python3 tools/verificar-pasos-copiables.py    # PASOS.md no promete código que solucion/ no tiene
python3 tools/verificar-guion-vs-practica.py  # lo que PASOS.md promete de practica/, practica/ lo trae
bash   tools/verificar-tamanos.sh             # ningún archivo pasa de 95 MB
```

### El respaldo de `instructor/` (SPEC-042)

```bash
tools/instructor-respaldo.sh estado      # compara por sha256 los dos árboles. NO escribe nada
tools/instructor-respaldo.sh respaldar   # disco -> repositorio privado
tools/instructor-respaldo.sh restaurar   # repositorio privado -> disco
```

Por defecto espera el clon privado **al lado**, en `../springboot-sii-2026-instructor`; si no,
`--destino RUTA`. Las dos copias terminan comparando huellas y diciendo cuántas cuadran.

**`verificar-instructor.py` es el único que el CI no puede correr** — la carpeta no existe en el
runner. Se corre a mano, aquí, donde los archivos sí están.

### Compilar todo el arco offline (lo que hace el job `labs`)

```bash
for p in $(find labs proyecto-final -name pom.xml -not -path '*/instructor/*' -not -path '*/target/*'); do
  d=$(dirname "$p"); [ -x "$d/mvnw" ] && (cd "$d" && ./mvnw -q -o compile) || true
done
```

Son **37 proyectos**. El lab 14 aporta ocho (cuatro servicios × dos carpetas).

### El CI

`.github/workflows/material-ci.yml`, **seis jobs**:

| Job | Qué protege |
|---|---|
| `temario` | el `.md` y el `.docx` no divergen |
| `siembra` | toda `TEORIA.md` con sucesor siembra el módulo N+1 (`P-18`) |
| `labs` | los **37** proyectos Maven compilan **offline**. Falla si alguien necesitó la red |
| `pasos` | los quince guiones traen el código y no prometen lo que la solución no tiene (146 bloques, 87 métodos) |
| `guion-practica` | lo que los guiones prometen de `practica/` es lo que `practica/` trae (88 promesas) |
| `labs-sh` | los scripts, en Linux y en Git Bash |

`pull_request` **no lleva filtro de rutas** a propósito: con filtro, un PR que solo tocara
`docs/` quedaría colgado esperando un veredicto que nadie iba a emitir.

**El rojo del job `temario` es un semáforo, no una falla**: si se edita el `.md` hay que
regenerar el `.docx`.

---

## 7 · El protocolo de trabajo

### Nomenclatura

- **`SPEC-NNN`** — entregable nuevo, propósito único.
- **`SPEC-FIX-NN`** — corrección de una SPEC ya ejecutada (bug del material).
- **`SPEC-DIAG-NN` / `SPEC-AUDIT-NN`** — diagnóstico, no producen material.
- **Sufijo `-R1`, `-R2`** — revisión de una SPEC aún no ejecutada.
- El nombre del archivo **no lleva sufijo de versión**: Git ya versiona el contenido.

### El ciclo, de principio a fin

```bash
git switch -c spec-041-instructor-porque      # rama: slug de la SPEC
#   ... ejecutar ...
git add <solo lo que toca esta SPEC>
git commit -m "SPEC-041: <qué, en una frase>"
#   ... y el informe, en su propio commit:
git commit -m "SPEC-041: informe"
git push -u origin spec-041-instructor-porque
gh pr create --title "SPEC-041 · <título>" --body "..."
gh pr merge --merge                            # esperando el verde del CI
git switch main && git pull
git tag material-vX.Y.Z && git push origin material-vX.Y.Z
```

- **Toda SPEC va por rama, con PR. Sin excepciones.**
- **Ninguna ejecución comienza antes de que su SPEC esté commiteada.** Si el PO me pasa la SPEC
  por chat, la escribo a `docs/specs/` y la commiteo primero.
- El prefijo del commit es `SPEC-NNN: <qué>`, en minúscula, frase, sin punto final.
- **El informe va en su propio commit**, al final, en `docs/specs/informes/INFORME-SPEC-NNN.md`.
- **`ESTADO.md` se actualiza al cerrar.** Va en el commit del informe.
- **Tag al cerrar**: `material-vX.Y.Z`. Patch bump para un `SPEC-FIX`, minor para una SPEC que
  agrega material. Hoy vamos en `material-v1.5.0`.

> ⚠️ `main` **no** tiene protección en el servidor: GitHub no la permite en repos privados del
> plan Free. La regla es convencional. El candado está especificado y congelado
> (SPEC-FIX-01 §3.1) y se activa el día que haya GitHub Pro. **Aquí no se declara activo lo que
> no lo está.**

### El informe

Es el entregable, no un trámite. Estructura que se usa:

```markdown
# INFORME-SPEC-NNN · <título>

**Ejecuta:** mocito · **Rama:** `<rama>` · **Fecha:** <fecha en palabras>
**Origen:** SPEC-NNN del Arquitecto.

## 0 · Resumen        ← lo importante en negrita, con números
## 1..N · Secciones   ← una por eje de trabajo, con la salida citada
## Lo que NO se hizo  ← siempre. Y por qué
```

Con **salida real citada en bloques de código**, no parafraseada. Números concretos. Y lo que
quedó fuera, dicho.

---

## 8 · Estado al 28 de agosto de 2026

- `main` en **`material-v1.5.0`**, CI en verde (último run sobre `main`: `success`, 2 m 07 s).
- **Desde el 27 cerraron dos SPEC**, y son las dos sobre `instructor/`:
  - **SPEC-041** (`material-v1.4.0`) — **102 recuadros `POR QUÉ ·`** en los labs 04 a 07: 34 en el
    04, 22 en el 05, 23 en el 06, 23 en el 07. Cero código tocado, y medido: el código desnudo de
    los 37 `.java` es idéntico al de `solucion/`. La pidió el PO después de dictar el lab 04 y
    quedarse sin respuesta ante «¿y por qué `IDENTITY`?».
  - **SPEC-042** (`material-v1.5.0`) — el respaldo privado de las dieciséis `instructor/`, y de
    paso el borrado de **258 archivos** de `target/` heredados que no pintaban nada ahí.
- **El respaldo está al día**, comprobado hoy: `estado` da 245 archivos a cada lado y **245
  huellas que cuadran**.
- **El PO está dictando ahora mismo.** El árbol trae trabajo suyo en `practica/` de hoy: cinco
  archivos del lab 04 (dos de ellos nuevos, sin seguimiento) y tres del lab 05, el último tocado
  a las 12:22. **No se tocan ni se commitean** (§9).
- **El material está terminado**: quince labs, las tres carpetas en todos, la maleta completa.
  No queda laboratorio por escribir.
- **Lo que falta es del PO, no del material:**
  1. La **fila de aceptación**: sentarse con cada `PASOS.md` sobre `practica/` sin abrir
     `solucion/`, del 00 al 14. Empezar por el 14. Es la única prueba que yo no puedo hacer:
     quien escribió el guion no puede juzgar si se entiende.
  2. Las **diapositivas y el material de sala**. No existen.
  3. Dos casillas de evaluación vacías: **conocimientos (30 %)** y **ejercicios (20 %)**. El
     proyecto final cubre el otro 50 %.
  4. La **aritmética del contrato**: el material va nueve horas y tres sesiones por encima de lo
     contratado, y el lab 14 no tiene módulo titular.
- **Anotado para después, del lado del material** (INFORME-SPEC-041 §7 e INFORME-SPEC-042 §9):
  los recuadros `POR QUÉ ·` en los **once labs restantes y el proyecto final** —los que más lo
  piden son el 09 y el 10—; que `verificar-instructor.py` vigile que un bloque traiga sus cuatro
  partes; y que ese mismo verificador llame a `instructor-respaldo.sh estado`, para que el
  respaldo desactualizado avise solo.
- Cobertura del temario, medida en `docs/temario/MAPA-LAB-MODULO.md`: **20 de 35 temas
  cubiertos**, 7 parciales, 8 sin cubrir. Las ocho brechas: gRPC, AOP, manejo de archivos,
  eventos de aplicación, mensajería, caché, Liquibase y OpenAPI/versionado. Tres de ellas son
  caras porque exigen Docker.

---

## 9 · Trampas conocidas

- **`instructor/` no viaja.** Si trabajo ahí, `git status` no lo muestra y el CI no lo ve. Lo que
  se commitea es el informe. **Y el respaldo no es automático:** al terminar,
  `tools/instructor-respaldo.sh respaldar`. Un `git clean -xdf` sigue llevándose la carpeta del
  disco; lo que cambió con la SPEC-042 es que ahora se puede recuperar.
- **`git status` puede traer trabajo del PO en clase.** El PO dicta con `practica/` abierta y deja
  archivos a medio hacer ahí. **Nunca commitear `labs/*/practica/**` sin que lo pidan
  explícitamente**: es el punto de partida del alumno y tiene un job de CI que lo vigila
  (`guion-practica`).
- **`.datos-pg/`** es el directorio de datos del PostgreSQL embebido. Está ignorado. Si entra,
  entran decenas de MB.
- **Dos candados, no uno**, en los labs con base de datos: el puerto lo retiene PostgreSQL y se
  libera al morir el motor; `.datos-pg/epg-lock` lo retiene la aplicación Java y sobrevive.
  Ambos tienen guarda con mensaje explícito (`PuertoLibre` y `CandadoLibre`, en `infra/`).
- **`repo-maven/`, `tools/jdk/` y `tools/jib-base/` están marcados `binary`** en
  `.gitattributes`. No se tocan a mano.
- **Windows es la plataforma que encuentra los defectos.** Tres bugs que desde macOS eran
  invisibles salieron en la VM del PO. Si algo se midió solo en macOS, se dice.
- **El arco antiguo ya no está** (SPEC-033, 2.120 archivos). Sigue en el historial y en los tags
  `material-v0.4.0` a `material-v0.8.0`:
  ```bash
  git show material-v0.8.0:labs/lab-13-capsula-y-egreso/README.md
  ```
- **Un lab tarda en arrancar en frío.** El JDK se ensambla la primera vez. 38 segundos desde el
  `git clone` hasta la aplicación sirviendo, con todo frío y sin red.

---

## 10 · Convenciones de nombres del código (SPEC-040)

**Un rol, un nombre, una anotación**, en los quince labs. Cero divergencias medidas.

| Rol | Paquete | Anotación |
|---|---|---|
| Capa web | `controllers/` | `@RestController` |
| Lógica | `services/` | `@Service` |
| Demostraciones del lab | `demos/` | `@Service` |
| Almacén | `repositories/` | `@Repository` o interfaz |
| Fila de tabla | `entities/` | `@Entity` |
| Objeto en memoria | `models/` | — (suelen ser `record`) |
| Cuerpo de respuesta | `dto/` | — |
| Errores | `exceptions/` | `@RestControllerAdvice` |
| Configuración | `config/` | `@Configuration` |
| Fontanería del arranque | `infra/` | — |
| Andamiaje del lab | `soporte/` | `@Component` |

`models/` y `entities/` se llaman distinto **a propósito**: uno es un objeto que vive en memoria,
el otro es una fila de una tabla. Es una diferencia que enseña, y el README lo dice.

Paquete raíz: `cl.dgt.<lab>`. Universo del material: la **DGT**, una dirección de trámites
ficticia (SPEC-000).

---

## 11 · Por dónde empezar si estoy perdido

```bash
cd labs/lab-00-hola-mundo/solucion && ./mvnw spring-boot:run
```

Y para ver de qué trata el curso en un minuto:

```bash
cd labs/lab-02-di/solucion && ./mvnw spring-boot:run
curl http://localhost:8084/productos/quien     # -> ProductoRepositoryLista
```

Si algo falla: `docs/troubleshooting.md` tiene una tabla con números. **Se cita el número.**
