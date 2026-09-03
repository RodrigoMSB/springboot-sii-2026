# Los laboratorios

Trece laboratorios, del 00 al 14. Cada uno tiene su `README.md`, su `PASOS.md` —el guion de la
sesión— y su guía en PDF.

| Lab | Tema |
|---|---|
| `lab-00-hola-mundo` | Que Spring Boot arranque |
| `lab-01-web` | El primer endpoint |
| `lab-02-di` | Inyección de dependencias |
| `lab-03-errores` | Errores con forma |
| `lab-04-jpa` | Guardar y recuperar |
| `lab-05-relaciones` | Relaciones JPA |
| `lab-05b-muchos-a-muchos` | Muchos a muchos |
| `lab-06-rendimiento` | El N+1 |
| `lab-07-concurrencia` | Dos peticiones, el mismo folio |
| `lab-08-testing` | Testing |
| `lab-09-seguridad` | Seguridad |
| `lab-10-resiliencia` | Resiliencia |
| `lab-11-observabilidad` | Observabilidad |
| `lab-microservicios` | Microservicios |

## Los números 12 y 13 no están, y no es un error

**`lab-12-tareas` y `lab-13-empaquetado` se retiraron del curso** con la SPEC-038, junto con
`examen-huecos/`. **No se borró nada**: siguen enteros en el historial y en el tag
`material-v1.11.1`.

```bash
git show material-v1.11.1:labs/lab-12-tareas/PASOS.md
git checkout material-v1.11.1 -- labs/lab-13-empaquetado/
```

**Los números 12 y 13 no se reutilizan.** Un lab nuevo tomaría el siguiente libre, para que las
referencias del historial y de los informes sigan queriendo decir lo que decían.

Lo que el curso conserva de esos dos temas: el **empaquetado** vive en el proyecto final, que se
entrega con su imagen OCI construida con Jib; y de las **tareas programadas** queda el problema de
las dos instancias, nombrado en el cierre del lab 07 al hablar del turno que vive en la base y no
en la JVM.

## Las tres carpetas de cada lab

| | |
|---|---|
| `practica/` | Donde trabaja el alumno. **Sin documentación**: la firma, una línea imperativa y `// escribe aquí` |
| `solucion/` | El proyecto terminado, con comentarios **breves** donde algo no es evidente |
| `instructor/` | Los mismos archivos de `solucion/`, explicados **línea por línea**. **No viaja al repo** |
