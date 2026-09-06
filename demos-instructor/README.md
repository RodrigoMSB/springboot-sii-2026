# `demos-instructor/` — lo que el instructor proyecta y el alumno no corre

Esta carpeta **no es material del alumno**. Lo que hay aquí lo levanta el instructor en su
máquina, delante de la sala, y **nadie más lo ejecuta**.

Existe porque hay cosas que vale la pena **enseñar** y no se pueden **poner en la maleta**. La
regla del curso es que todo lo que el alumno necesita viaja dentro del repositorio y funciona sin
red y sin instalar nada (`D-022-3`). Docker no cumple esa regla —las máquinas del SII no lo tienen
y no pueden instalarlo, y ése fue el motivo de retirar el lab de microservicios antiguo—, así que no puede ser un
laboratorio. Pero verlo funcionar durante quince minutos sí cabe en una sesión.

## La regla de esta carpeta

> **Nada de aquí es requisito para aprobar el curso, y nada de aquí entra en la maleta del
> alumno.** Si un día algo de esto se vuelve imprescindible, deja de ser una demostración y hay
> que replantearlo como laboratorio — con las tres restricciones de la sala delante.

## Y el CI

**Ningún job del CI levanta nada de esta carpeta.** El job `labs` recorre `labs`,
y `proyecto-final`; `demos-instructor/` no está en esa lista, así que sus
proyectos Maven no entran en la compilación offline y no hay ninguna excepción que mantener. La
exclusión es estructural.

Lo que sí hay es un job que **no necesita Docker**: `demo-docker` corre
`tools/verificar-demo-docker.py`, que comprueba que la copia del lab de microservicios no se haya separado del
laboratorio del que salió.

### Y una consecuencia nueva de la regla, que conviene tener escrita

`microservicios-discovery/` es la primera cosa del repositorio que **no compila offline**: Eureka y
el Config Server no están en `repo-maven/`, y **no se metieron a propósito**. Serían megas de
artefactos de Spring Cloud en el clon de los dieciocho alumnos para un material que ningún alumno
ejecuta — que es exactamente lo que esta carpeta existe para evitar.

Su `construir.sh` usa `DGT_ONLINE=1` y necesita red **una vez**, antes de la clase. Es el mismo
trato que las imágenes de Docker de la otra demostración, y por la misma razón.

**Esto no rompe nada del CI ni de la maleta:** el job `labs` no mira aquí, `repo-maven/` no se
toca, y el invariante `D-022-3` sigue diciendo lo que decía — que todo lo que el **alumno** necesita
viaja en el repositorio y funciona sin red.

## Qué hay

| | |
|---|---|
| [`microservicios-docker/`](microservicios-docker/) | El sistema de microservicios del lab de microservicios, **con Docker Compose** en vez de cuatro terminales. Demuestra qué aporta un orquestador |
| [`microservicios-discovery/`](microservicios-discovery/) | El mismo sistema **más un registro (Eureka) y un Config Server**. Demuestra qué aporta descubrir por nombre y sacar la configuración del programa — y qué cuesta. **Sin Docker** |

### Las dos se contradicen a propósito, y eso es material

La demostración con Docker enseña que el DNS de la red del compose **ya hace** el descubrimiento por
nombre, gratis, y que por eso el lab antiguo montaba un Eureka que allí sobraría. La de discovery
monta ese Eureka.

No es una incoherencia: es **la pregunta que el alumno tiene que saber hacerse**. Con una
plataforma debajo (Kubernetes, un compose, lo que sea), el descubrimiento viene dado y añadir un
registro es duplicar. Sin plataforma —seis JVM en dos máquinas que administras tú, que es donde
está media administración pública— hace falta montarlo.

**Si se dictan las dos, el orden es Docker primero y discovery después**, y la frase que las cose
está en el bloque 2 de la segunda: *Eureka no compite con Docker; compite con no tener plataforma.*
