# `demos-instructor/` — lo que el instructor proyecta y el alumno no corre

Esta carpeta **no es material del alumno**. Lo que hay aquí lo levanta el instructor en su
máquina, delante de la sala, y **nadie más lo ejecuta**.

Existe porque hay cosas que vale la pena **enseñar** y no se pueden **poner en la maleta**. La
regla del curso es que todo lo que el alumno necesita viaja dentro del repositorio y funciona sin
red y sin instalar nada (`D-022-3`). Docker no cumple esa regla —las máquinas del SII no lo tienen
y no pueden instalarlo, y ése fue el motivo de retirar el lab 14 antiguo—, así que no puede ser un
laboratorio. Pero verlo funcionar durante quince minutos sí cabe en una sesión.

## La regla de esta carpeta

> **Nada de aquí es requisito para aprobar el curso, y nada de aquí entra en la maleta del
> alumno.** Si un día algo de esto se vuelve imprescindible, deja de ser una demostración y hay
> que replantearlo como laboratorio — con las tres restricciones de la sala delante.

## Y el CI

**Ningún job del CI levanta nada de esta carpeta.** El job `labs` recorre `labs`,
`proyecto-final` y `examen-huecos`; `demos-instructor/` no está en esa lista, así que sus
proyectos Maven no entran en la compilación offline y no hay ninguna excepción que mantener. La
exclusión es estructural.

Lo que sí hay es un job que **no necesita Docker**: `demo-docker` corre
`tools/verificar-demo-docker.py`, que comprueba que la copia del lab 14 no se haya separado del
laboratorio del que salió.

## Qué hay

| | |
|---|---|
| [`lab-14-docker/`](lab-14-docker/) | El sistema de microservicios del lab 14, **con Docker Compose** en vez de cuatro terminales. Demuestra qué aporta un orquestador |
