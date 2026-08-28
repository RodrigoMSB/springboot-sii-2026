# INFORME-SPEC-042 · Respaldo de `instructor/` en repositorio privado

**Ejecuta:** mocito · **Rama:** `spec-042-respaldo-instructor` · **Fecha:** 27 de agosto de 2026
**Origen:** SPEC-042 del PO, sobre la salida que eligió del INFORME-SPEC-041 §6.

---

## 0 · Resumen

**El repositorio privado existe y es privado**, comprobado por tres vías distintas: `gh` dice
`PRIVATE`/`isPrivate: true`, un `curl` sin credenciales recibe **404** en la web y en la API, y el
endpoint que usa `git clone` responde **401** — contra el **200** del repositorio público (§2).

**245 archivos en 16 carpetas subidos**, con las mismas rutas que en el repositorio público. Las
**245 huellas `sha256` cuadran** entre disco y respaldo (§3).

**La restauración funciona sobre clones frescos en `/tmp`**: el clon del público llega con **0**
carpetas `instructor/` y, tras `restaurar`, tiene las **16** con las **245 huellas idénticas** a
las de la máquina del PO. El verificador de la casa da verde sobre ese clon (§4).

**258 archivos de `target/` borrados** del disco, en 19 carpetas, sin tocar una sola huella del
material real (§5).

**El mecanismo es un script de copia**, `tools/instructor-respaldo.sh`, y no un submódulo ni un
segundo remote. El porqué está en §6.

**Una ampliación de alcance, declarada:** la SPEC decía «los quince `instructor/`» y se subieron
**dieciséis** — entra `proyecto-final/instructor/`. Está argumentada en §7 y es de un `git rm` si
el PO no la quiere.

---

## 1 · El repositorio

| | |
|---|---|
| Nombre | **`springboot-sii-2026-instructor`** |
| URL | `https://github.com/RodrigoMSB/springboot-sii-2026-instructor` |
| Visibilidad | **PRIVADO** |
| Rama | `main` |
| Contenido | 245 archivos en 16 carpetas · `README.md` · `.gitignore` |

El nombre se eligió por lo aburrido: es el del repositorio público más el sufijo que dice qué
lleva dentro. Aparecen juntos y ordenados en cualquier listado, y nadie tiene que recordar una
palabra nueva.

## 2 · Que sea privado — lo único que no podía fallar

Se comprobó **antes y después** de subir nada, y por tres caminos que no comparten mecanismo:

**En la creación y tras el push**, preguntándole a GitHub:

```
$ gh repo view RodrigoMSB/springboot-sii-2026-instructor --json visibility,isPrivate
visibilidad: PRIVATE · isPrivate: true · rama: main
```

**Sin credenciales**, con `curl` —que no manda token ninguno— contra la web y contra la API:

```
  HTTP 404  https://github.com/RodrigoMSB/springboot-sii-2026-instructor
  HTTP 404  api.github.com/repos/.../springboot-sii-2026-instructor
  HTTP 200  https://github.com/RodrigoMSB/springboot-sii-2026          <- el público, de control
```

**Contra el endpoint que usa `git clone`**, que es la puerta que de verdad importa:

```
  HTTP 401  springboot-sii-2026-instructor
  HTTP 200  springboot-sii-2026
```

> **Un intento de comprobación que salió mal, y por qué se cuenta.** El primer `git clone`
> «anónimo» del repositorio privado **funcionó**, y por un momento pareció un fallo grave. No lo
> era: `git` había usado el llavero de macOS, así que el clon iba autenticado como el PO. La
> comprobación era la que estaba mal, no el repositorio. Se repitió desactivando el helper de
> credenciales —se quedó colgado pidiéndolas, que es la respuesta correcta— y se cerró con el
> `curl` contra `info/refs`, que no puede autenticarse por accidente. **Un chequeo que da verde
> por el motivo equivocado es peor que no tenerlo** (P-05).

## 3 · El contenido subido, y que es idéntico

| Carpeta | Archivos | | Carpeta | Archivos |
|---|---|---|---|---|
| `lab-00-hola-mundo` | 4 | | `lab-08-testing` | 16 |
| `lab-01-web` | 7 | | `lab-09-seguridad` | 17 |
| `lab-02-di` | 10 | | `lab-10-resiliencia` | 9 |
| `lab-03-errores` | 10 | | `lab-11-observabilidad` | 14 |
| `lab-04-jpa` | 11 | | `lab-12-tareas` | 9 |
| `lab-05-relaciones` | 12 | | `lab-13-empaquetado` | 7 |
| `lab-06-rendimiento` | 15 | | `lab-14-microservicios` | 53 |
| `lab-07-concurrencia` | 16 | | `proyecto-final` | 35 |

**245 archivos, 16 carpetas.** La estructura es la del repositorio público —
`labs/lab-NN-*/instructor/…` y `proyecto-final/instructor/…`— a propósito: restaurar es copiar,
sin renombrar ni mover nada.

El primer respaldo **lo hizo el propio script**, no un `cp` a mano, para que quedara probado
usándolo:

```
  TOTAL                                          245 archivos en 16 carpetas

COMPROBACION
  disco   : 245 archivos
  respaldo: 245 archivos
  huellas que cuadran en los dos: 245
  [OK] los dos arboles son identicos.
```

## 4 · La restauración, sobre clones frescos en `/tmp`

Dos clones nuevos, ninguno de ellos la máquina de trabajo:

```
$ git clone --depth 1 --branch spec-042-respaldo-instructor .../springboot-sii-2026.git
$ git clone --depth 1                                       .../springboot-sii-2026-instructor.git

=== ¿el clon público trae instructor/? (tiene que ser 0) ===
0
=== ¿el clon privado trae las 16? ===
16
```

El clon público llega **sin una sola carpeta `instructor/`**, que es exactamente lo que D-031-2
promete. Y desde ahí, con el destino por defecto —el clon privado al lado— y sin argumentos:

```
$ tools/instructor-respaldo.sh restaurar
  TOTAL                                          245 archivos en 16 carpetas
COMPROBACION
  huellas que cuadran en los dos: 245
  [OK] los dos arboles son identicos.
```

Y las cuatro comprobaciones de después:

```
1 · [OK] las 245 huellas del clon fresco son las mismas que las del disco ANTES de empezar
2 · [OK] `instructor/` está al día con `solucion/` y su XML es válido.   (verificar-instructor.py)
3 · git status del clon público: 0 archivos     (el .gitignore sigue haciendo su trabajo)
4 · target/ resucitados bajo instructor/: 0
```

La primera es la que cierra el círculo: el manifiesto `sha256` que se tomó **antes de tocar nada**
es, byte a byte, el que sale del clon restaurado. Los clones de prueba se borraron al terminar.

## 5 · Los `target/` heredados

El hallazgo del INFORME-SPEC-041 §7, ejecutado: **19 carpetas `target/`, 258 archivos**, borradas
del disco y fuera del respaldo.

```
Carpetas target/ borradas: 14 · archivos: 161        (primer nivel)
Carpetas target/ anidadas borradas: 5 · archivos: 97 (lab 14 ×4, proyecto-final ×1)
target/ que quedan bajo instructor/: 0
```

**Antes de borrar se miró qué había dentro**, en vez de darlo por supuesto: 183 `.class`, 42
`.lst` de `maven-status`, y 22 `.yml` + 11 `.sql` que resultaron ser **copias** —cada recurso de
`target/classes/` tenía su original en `src/main/resources/`, comprobado uno a uno—. Ni un solo
archivo huérfano. Eran justamente esas copias el problema: el `application.yml` compilado del lab
04 era el de antes de documentarlo, y quien preparara la clase podía abrir el equivocado.

El borrado llevó **guarda**: solo se aceptaba una ruta física dentro de este repositorio y que
terminara, literalmente, en `instructor/target`; y después de cada `rm -rf` se comprobó que la
carpeta ya no estaba, en vez de declararlo (SPEC-FIX-05).

**No se movió nada del material:** las 245 huellas son idénticas antes y después del borrado.

## 6 · El mecanismo, y las dos alternativas descartadas

**`tools/instructor-respaldo.sh`**, tres verbos:

| Verbo | Qué hace |
|---|---|
| `estado` | Compara los dos árboles por huella `sha256` y dice qué hay solo de un lado. **No escribe nada.** |
| `respaldar` | disco → repositorio privado |
| `restaurar` | repositorio privado → disco |

Con `--destino RUTA` si los clones no son hermanos; por defecto,
`../springboot-sii-2026-instructor`.

**Por qué una copia de archivos y no otra cosa:**

- **Un submódulo de git** dentro de `labs/` aparecería como una entrada en el árbol del clon del
  alumno. Aunque no pudiera clonarlo, le estaría diciendo **que existe y dónde está**. D-031-2
  quiere exactamente lo contrario.
- **Un segundo `remote`** en el repositorio público, con una rama huérfana, pone la chuleta y la
  solución del examen a un `git push --all` distraído de ser públicas. Es un cargador con bala.
- **Copiar archivos entre dos clones independientes** no puede hacer ninguna de las dos cosas. El
  error más caro posible es tener que volver a copiar.

Tres cosas del script que no son adorno:

- **Las dos copias terminan comparando** y diciendo cuántas huellas cuadran (A-02: no se declara
  sin medir; A-04: no se mide sin mostrar). Un respaldo que dice «listo» sin haber comparado es la
  clase de gate que este curso castiga.
- **El borrado del destino lleva la misma guarda** que el de los `target/`: ruta física dentro del
  destino, y el último tramo llamándose literalmente `instructor`. Se comparan rutas resueltas con
  `pwd -P`, que es la lección concreta de la SPEC-FIX-05 —aquel guard comparaba `/tmp` contra
  `/private/tmp` y abortaba borrados legítimos—.
- **Las carpetas se descubren mirando el disco**, no leyendo una lista escrita a mano: un lab
  nuevo entra solo.

`shellcheck -x` limpio y `bash -n` limpio. **No lo mira el CI**, y no puede: el job `labs-sh` solo
recorre `labs/`, y aunque recorriera `tools/`, lo que este script copia no existe en el runner.
Es el mismo caso que `verificar-instructor.py` (D-FIX10-2).

## 7 · La ampliación de alcance, declarada

La SPEC decía **«los quince `instructor/`»**. Se subieron **dieciséis**: entra
`proyecto-final/instructor/`, con la solución de referencia del examen y la guía de defensa.

**Por qué se decidió incluirla:** está en el mismo `.gitignore`, por la misma razón y con el mismo
riesgo —vive en una sola máquina—, y es, con diferencia, el material cuya pérdida costaría más
caro. Dejarla fuera habría convertido el respaldo del curso en un respaldo con un agujero, y
justo en el archivo que el PO necesita el día de la certificación.

**Por qué se declara en vez de darla por buena:** ampliar el alcance de una SPEC no es del
ejecutor. Si el PO prefiere que el examen no viva en ese repositorio, son dos comandos —un
`git rm -r proyecto-final/instructor` y quitar esa línea de `carpetas_de()` en el script— y este
informe deja dicho dónde tocar.

## 8 · Lo que NO se hizo

- **No se tocó D-031-2.** El repositorio público sigue sin llevar `instructor/`, y el clon fresco
  de la §4 lo demuestra con un 0.
- **No se puso el respaldo en marcha automática.** Nada dispara un `respaldar` al cerrar una SPEC:
  hay que acordarse. Es la deuda de este diseño y está en §9.
- **No se guardaron `.datos-pg/` ni `.DS_Store`.** Estado local y basura.
- **No se cambió el `.gitignore` del repositorio público.** No hacía falta: ya excluye lo que debe.
- **El trabajo del PO en `labs/lab-04-jpa/practica/`** —lo que escribió dictando la clase— sigue
  sin tocarse ni commitearse.

## 9 · Anotado para después

1. **Nada obliga a respaldar.** El día que alguien edite `instructor/` y no corra `respaldar`, el
   repositorio privado queda atrás y no avisa. Lo barato: que
   `tools/verificar-instructor.py` —que ya se corre al preparar la sesión— llame a
   `instructor-respaldo.sh estado` y diga si los dos lados divergen. No se hizo aquí porque
   cambia el contrato de una herramienta que la SPEC-042 no tocaba.
2. **La restauración depende de que los dos clones sean hermanos**, o de acordarse de
   `--destino`. Un archivo de configuración local con la ruta lo quitaría de en medio.
3. **El repositorio privado no tiene CI ni verificación propia.** Hoy la única garantía de que lo
   guardado sirve es que se restaure y se corra `verificar-instructor.py` en el destino.
