# SPEC-047 · El lab 14 con Docker — versión de demostración del instructor

**Emite:** el PO · **Ejecuta:** el mocito
**Fecha:** 31 de agosto de 2026
**Rama:** `spec-047-lab-14-docker-demostracion` desde `main` · PR contra `main`
**Prefijo de commits:** `SPEC-047: <qué>`
**Autorización:** commit, PR, merge y tag sin firma del PO. Trabaja de corrido, sin interrumpirlo.

---

## 0 · Para qué

El PO va a **mostrar y ejecutar** el sistema de microservicios con Docker en su Mac, proyectando.

**Los alumnos no lo corren** — no tienen Docker, y ése fue el motivo de retirar el lab viejo. Es
**material de demostración, no un laboratorio.**

---

## 1 · Qué construir

Una versión del `lab-14-microservicios` actual —los mismos cuatro servicios: **gateway,
contribuyentes, trámites y auditoría**— levantada con **Docker Compose** en vez de a mano en
cuatro terminales.

**El sistema hace exactamente lo mismo; cambia cómo se arranca.**

---

## 2 · Antes de diseñar nada

Recuperar el lab viejo desde `material-v0.8.0`
(`labs/lab-14-la-dgt-se-parte-en-pedazos/`) y **leerlo entero**: tenía Compose, config server,
Dockerfile y una teoría que el PO valoraba.

**Lo que sirva se reutiliza; lo que no, se descarta y se dice por qué.**

---

## 3 · Estructura

**No lleva `practica/` ni `PASOS.md` ni bloques copiables** — no hay nada que teclear.

Lleva:

- el sistema **listo para levantar**
- un `README.md` con el **guion de demostración**
- la **guía en PDF del instructor**, con lo de siempre: el problema para la abuelita, la técnica,
  los diagramas y qué señalar en cada momento

---

## 4 · Lo que la demostración tiene que hacer visible

Es el motivo de que exista: **qué aporta el orquestador.**

1. **Cuatro terminales y un orden de arranque** contra `docker compose up`.
2. **Las bases dejan de ser Zonky embebido** y pasan a ser **contenedores de Postgres**.
3. **Los servicios se hablan por nombre** en vez de por `localhost:puerto`.
4. **Qué pasa al matar un contenedor:** Compose lo puede **reiniciar solo**, cosa que a mano no
   ocurría.

---

## 5 · Cuidado con dos cosas

### 5.1 · No tocar el `lab-14-microservicios` actual

El de la maleta es **el que dictan los alumnos** y tiene que seguir funcionando **sin Docker**.
Éste va aparte, **con su propio directorio**.

### 5.2 · Docker no viaja en la maleta ni puede colarse en el CI

Este lab necesita Docker, y **el CI no lo tiene**. Dejar claro **cómo se excluye** y **verificar
que ningún job se rompa por su culpa**.

---

## 6 · Verificación

**Levantando el sistema completo y citando el flujo de punta a punta por el gateway.**

---

## 7 · Cierre

Commit, PR, merge y tag. `main` al terminar.
