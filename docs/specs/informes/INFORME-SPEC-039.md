# INFORME-SPEC-039 · Carpeta `archivo/` y `lab-microservicios`

**Ejecuta:** mocito · **Rama:** `spec-039-archivo-y-nombre` · **Fecha:** 3 de septiembre de 2026
**Origen:** SPEC-039 del PO.

---

## 0 · Resumen

**Dos cosas, las dos mecánicas y las dos verificadas de punta a punta.**

| | |
|---|---|
| **`archivo/`** | Los labs 12 y 13, enteros y a la vista. Fuera del CI **por estructura**, sin una sola excepción escrita |
| **`lab-microservicios`** | El lab de cierre pierde el número. Con él, la demo, las guías, las propiedades, los `artifactId` y los nombres de contenedor |

**Validado:**

- `ls archivo/` → las dos carpetas y el README.
- Los **cuatro servicios arrancan** y la llamada trámites → contribuyentes da **200 con el nombre**.
- La demo con Docker levanta **los siete contenedores**, ya llamados `microservicios-docker-*`, y
  el `grep` del traceId sigue devolviendo los cuatro servicios.
- **36 proyectos compilan offline** — los mismos que antes.
- Los **seis verificadores en verde**.

**Dos puntos de la spec resultaron falsos al medir**, corregidos y declarados en §4:

1. **El tag no trae las «tres carpetas».** `instructor/` nunca viajó al repositorio (D-031-2), así
   que del tag sólo salen `practica/` y `solucion/`.
2. **El `grep` de validación no da cero**, y no puede darlo: quedan 96 apariciones, y **89 de ellas
   están en actas históricas** que no se deben tocar. Se declaran una por una en §4.2.

---

## 1 · La carpeta `archivo/`

### 1.1 · Qué hay dentro

```
archivo/
├── README.md
├── lab-12-tareas/        PASOS.md · README.md · guia-lab-12-tareas.pdf · practica/ · solucion/
└── lab-13-empaquetado/   PASOS.md · README.md · guia-lab-13-empaquetado.pdf · practica/ · solucion/
```

Traídos con `git checkout material-v1.11.1 --` y movidos con `git mv`, **enteros y sin tocar una
línea**.

**El lab 14 antiguo no va**, como pedía la spec: su versión viva es `labs/lab-microservicios/` más
la demostración con Docker. **`examen-huecos/` tampoco**: se queda sólo en el tag.

### 1.2 · La exclusión es estructural — comprobado, no supuesto

La spec pedía que `archivo/` quedara fuera del CI «igual que hoy está `demos-instructor/`», y que
la exclusión fuese estructural. **Se comprobó leyendo qué recorre cada pieza**:

| pieza | qué recorre | ¿ve `archivo/`? |
|---|---|---|
| job `labs` | `find labs proyecto-final -name pom.xml` | no |
| job `siembra` | `find labs -maxdepth 1 -type d -name 'lab-*'` | no |
| job `labs-sh` | `find labs -name '*.sh'` | no |
| `verificar-pasos-copiables.py` | `(RAIZ/'labs').iterdir()` | no |
| `verificar-guion-vs-practica.py` | `(RAIZ/'labs').glob('lab-*')` | no |
| `verificar-instructor.py` | `RAIZ.glob('labs/*/instructor')` + proyecto final | no |
| `verificar-demo-docker.py` | dos rutas fijas | no |
| `generar-guias.py` | `docs/guias/fuente/*.md` | no |
| `verificar-temario.py` | `docs/temario/` | no |

**Ninguno recorre el repositorio entero**, así que no hubo que escribir ni una excepción. El punto 5
de la spec —«si algún verificador recorre el repo entero, se le enseña a saltar `archivo/`»— **no
hizo falta**.

Y los seis siguen en verde con `archivo/` dentro:

```
temario           VEREDICTO: las 5 verificaciones PASAN
pasos-copiables   [OK] 14 guion(es) verificado(s)
guion-vs-practica [OK] Todo lo que los guiones prometen sobre `practica/` es verdad
instructor        [OK] `instructor/` está al día con `solucion/` y su XML es válido
demo-docker       [OK] la demostración dice el mismo código que el laboratorio
generar-guias     [OK] 78 bloque(s) · 0 líneas que la solución no tenga
```

### 1.3 · El `.gitignore`

Se añadió **`archivo/*/instructor/`**, y la razón está escrita ahí mismo: `archivo/` guarda material
retirado completo, y «completo» incluye su chuleta — pero **D-031-2 no cambia porque un lab deje de
dictarse**. Sin esa línea, las carpetas `instructor/` de los labs archivados habrían empezado a
viajar al clon del alumno, que es justo lo que esa regla impide desde la SPEC-031.

```
$ git check-ignore -v archivo/lab-12-tareas/instructor/LEEME.md
.gitignore:65:archivo/*/instructor/   archivo/lab-12-tareas/instructor/LEEME.md
```

### 1.4 · `ESTADO.md` §1.a

Reescrita con una tabla que dice **dónde vive cada cosa retirada y por qué ahí**: carpeta para lo
que sigue siendo material útil, tag para lo que fue instrumento de su momento. Cinco filas: los dos
labs archivados, `examen-huecos/`, el lab 14 antiguo y el arco antiguo completo.

---

## 2 · `lab-14-microservicios` → `lab-microservicios`

### 2.1 · Lo que se movió

```
labs/lab-14-microservicios          →  labs/lab-microservicios
demos-instructor/lab-14-docker      →  demos-instructor/microservicios-docker
docs/guias/fuente/guia-lab-14-microservicios.md   →  guia-lab-microservicios.md
docs/guias/fuente/guia-demo-lab-14-docker.md      →  guia-demo-microservicios-docker.md
labs/…/guia-lab-14-microservicios.pdf             →  guia-lab-microservicios.pdf
demos-instructor/…/guia-demo-lab-14-docker.pdf    →  guia-demo-microservicios-docker.pdf
```

Todo con `git mv`, así que el historial de cada archivo sigue entero.

### 2.2 · Lo que cambió por dentro

| qué | de | a |
|---|---|---|
| Propiedades de los yml y sus `@Value` | `lab14.*` | `microservicios.*` |
| Clave del bloque en el yml | `lab14:` | `microservicios:` |
| `artifactId` y `<name>` de los 8 poms del lab y los 4 de la demo | `lab14-*` | `microservicios-*` |
| `spring.application.name` | `lab14-*` | `microservicios-*` |
| Issuer del token | `lab14-gateway` | `microservicios-gateway` |
| Nombres de contenedor de Compose | `lab-14-docker-*` | `microservicios-docker-*` |
| Nombre humano en títulos | «Lab 14 · Microservicios» | **«Lab · Microservicios»** |

**En las tres carpetas** (`practica/`, `solucion/`, `instructor/`) y en `demos-instructor/`.

Los nombres de contenedor **no se cambiaron a mano**: los pone Compose a partir del nombre de la
carpeta del proyecto. Lo que sí se actualizó es la **salida esperada** que el README y la guía
muestran, porque si no habrían quedado mintiendo.

### 2.3 · Lo que NO cambió, como pedía la spec

- **Los puertos**: 8210-8213 en el lab, 8220-8223 en la demo.
- **Los nombres de las clases**: `GatewayApplication`, `TramiteService`, `TablaDeRutas`…
- **Los nombres de los cuatro servicios**: gateway, contribuyentes, trámites, auditoría.
- **Los tags históricos** y las actas de `docs/specs/`.

### 2.4 · Un cambio que la spec no listaba y hacía falta

`docs/guias/fuente/guia-demo-microservicios-docker.md` extraía un bloque del yml con
`modo=clave clave=lab14`. Al renombrar la propiedad, el generador dejó de encontrarla:

```
[ERROR] no se encontró la clave YAML `lab14`
```

Corregido a `clave=microservicios`. **Lo cazó `generar-guias.py --verificar`**, que es exactamente
para lo que existe.

---

## 3 · Validación

### 3.1 · `archivo/`

```
$ ls archivo/
README.md   lab-12-tareas   lab-13-empaquetado

$ ls archivo/lab-12-tareas/
PASOS.md  README.md  guia-lab-12-tareas.pdf  instructor  practica  solucion
```

(`instructor/` está en el disco de quien prepara y **no viaja** — §1.3.)

### 3.2 · Los cuatro servicios del lab

```
  contribuyentes   Started ContribuyentesApplication in 2.431 seconds
  auditoria        Started AuditoriaApplication in 2.404 seconds
  tramites         Started TramitesApplication in 2.478 seconds
  gateway          Started GatewayApplication in 1.67 seconds
```

**La llamada trámites → contribuyentes**, que es lo que la spec pide comprobar:

```
$ curl http://localhost:8212/tramites/1
{"id":1,"tipo":"DECLARACION_F29","estado":"EN_PROCESO","rutContribuyente":"11111111-1",
 "nombreContribuyente":"Carolina Fuentes Aravena","estadoDelNombre":"OK",...}   [200]
```

Y por el gateway, con token: **el mismo 200 con el nombre resuelto**.

### 3.3 · La demo con Docker

**Docker estaba disponible**, así que se corrió entera y no hubo que declararla pendiente.

```
$ ./construir.sh && docker compose up -d --build
$ docker compose ps

SERVICE             STATUS
auditoria           Up 21 seconds (healthy)
contribuyentes      Up 21 seconds (healthy)
db-auditoria        Up 23 seconds (healthy)
db-contribuyentes   Up 23 seconds (healthy)
db-tramites         Up 23 seconds (healthy)
gateway             Up 9 seconds (healthy)
tramites            Up 15 seconds (healthy)
```

**Los siete**, y con los nombres nuevos. El bloque 6 de la demostración —el traceId cruzando cuatro
contenedores— sigue funcionando:

```
$ docker compose logs --no-color | grep SPEC39 | awk -F'|' '{print $1}' | sort -u
auditoria-1
contribuyentes-1
gateway-1
tramites-1
```

Y los jars que copia el Dockerfile son los nuevos: `microservicios-<módulo>-docker-0.1.0.jar`.

`docker compose down -v` sin residuos.

### 3.4 · El CI local

```
[INFO] 36 proyectos · 0 fallos      ← los mismos que antes: archivo/ no entra
```

---

## 4 · Los puntos de la spec que resultaron falsos, y qué se hizo

### 4.1 · El tag no trae las «tres carpetas»

La spec dice: *«Traer desde `material-v1.11.1` … con sus tres carpetas cada uno.»*

**El tag sólo tiene dos.** `instructor/` nunca viajó al repositorio público — es la decisión
**D-031-2**, aplicada por el `.gitignore` desde la SPEC-031:

```
$ git ls-tree -r --name-only material-v1.11.1 labs/lab-12-tareas/ | grep -c instructor
0
```

**Qué se hizo:** las dos carpetas versionadas vienen del tag, y las `instructor/` se restauraron
desde el **repositorio privado de respaldo** (`springboot-sii-2026-instructor`, que sí las tiene).
Así los labs archivados quedan **completos en el disco de quien prepara** y siguen **sin viajar al
clon del alumno** — que es exactamente el trato de los labs vivos. Ver §1.3.

### 4.2 · El `grep` no da cero, y no debe darlo

La spec pide: *«`grep -rn "lab-14\|lab14" --exclude-dir=archivo --exclude-dir=.git .` devuelve cero
líneas.»*

**Devuelve 96**, y todas son correctas. Medido con el comando de la spec y clasificado entero:

| dónde | cuántas | qué son |
|---|---|---|
| `docs/specs/informes/*.md` | **77** | **Actas de lo ocurrido.** Un informe que dice «se creó `labs/lab-14-microservicios/`» describe un hecho fechado. Reescribirlo sería falsificar el registro. De esas 77, **21 son de este mismo informe**, que cita los nombres viejos para explicar el cambio |
| `docs/specs/SPEC-*.md` | **12** | Las specs originales, con los nombres de rama y de carpeta de entonces. Mismo criterio. Dos de ellas lo llevan **en el propio nombre del archivo** (`SPEC-020-lab14-microservicios.md`, `SPEC-047-lab-14-docker-demostracion.md`) |
| `ESTADO.md` | **3** | Dos por el **lab 14 antiguo** (`lab-14-la-dgt-se-parte-en-pedazos`, que no es éste) y una por el job de CI `lab14` que la SPEC-033 retiró |
| `.github/workflows/material-ci.yml` | **2** | El **nombre del job retirado** `lab14`, en los dos comentarios que explican por qué ya no está y qué ocupó su sitio |
| `docs/temario/MAPA-LAB-MODULO.md` | **1** | El lab 14 antiguo, citado al explicar cómo se cubría un módulo en el arco viejo |
| `demos-instructor/microservicios-docker/README.md` | **1** | El lab antiguo, citado con su tag `material-v0.8.0` para decir de dónde viene esta demostración |

**Ni una sola apunta al lab de cierre de hoy.** Las siete de fuera de `docs/specs/` hablan o del
**lab 14 antiguo**, que es otro lab y sigue en su tag, o de un **job de CI retirado**.

**Lo que sí se trató**, uno a uno: `README.md`, `labs/README.md`, `demos-instructor/README.md`, el
README y la guía de la demo, el README y el `PASOS.md` del lab, las dos fuentes de guía,
`generar-guias.py`, `verificar-demo-docker.py`, el CI, `ESTADO.md` (las vivas),
`MAPA-LAB-MODULO.md` (las vivas), `docs/decisiones.md` (la ruta de D-047-2) y
`CONTEXTO-MOCITO.md`.

**El criterio, para que se pueda repetir:** *un nombre en un acta describe el pasado y se queda; un
nombre en un documento que dice cómo es el repositorio hoy, o en una ruta que alguien va a teclear,
se actualiza.*

### 4.3 · Dos residuos que aparecieron al medir

- **Los `target/` viejos.** El primer `./construir.sh` dejó conviviendo los jars `lab14-*` con los
  `microservicios-*`, y el Dockerfile habría copiado el que no era. Se limpiaron los `target/` y se
  reconstruyó. **Vale la pena decirlo en la sala**: un renombre de `artifactId` deja basura que
  parece código bueno.
- **`docs/temario/MAPA-LAB-MODULO.md`** sigue con su matriz citando `lab-12` y `lab-13` en las filas
  de módulos — pendiente heredado de la SPEC-038, no de ésta, y sigue en manos del PO.

---

## 5 · El tag

La cabecera pide **`material-v1.13.0`**, y **está libre**. Se cierra con ése.
