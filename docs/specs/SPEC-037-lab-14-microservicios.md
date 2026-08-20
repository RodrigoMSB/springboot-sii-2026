# SPEC-037 · Lab 14 · Microservicios — el sistema repartido

**Emite:** Arquitecto · **Ejecuta:** mocito
**Fecha:** 19 de agosto de 2026
**Rama:** `spec-037-lab-14-microservicios` desde `main` · PR contra `main`
**Prefijo de commits:** `SPEC-037: <qué>`
**Autorización:** merge y tag sin firma del PO.

> **Ejecutada.** Informe en `docs/specs/informes/INFORME-SPEC-037.md`, con las nueve
> verificaciones citadas, las dos desviaciones declaradas (el gateway escrito a mano y el
> orden del paso 2) y la decisión sobre gRPC.

---

## 0 · Qué se hace

El curso cierra con el tema que más interesa a los alumnos: **microservicios fue el primero de
ocho en la encuesta, con 16 de 18 votos.** El lab del arco viejo
(`lab-14-la-dgt-se-parte-en-pedazos`) se retiró porque necesitaba Docker Compose y seis
servicios. Su teoría era buena: **recuperarla desde `material-v0.8.0` y leerla entera antes de
diseñar nada** — hay criterio ahí que no conviene perder.

Este lab lo reemplaza con **cuatro procesos que el alumno arranca a mano**, sin Docker, sin
registry, sin nada que no viaje en la maleta. Que el alumno vea los procesos, sus puertos y sus
bases es una ventaja pedagógica, no una limitación: con Compose eso queda escondido.

**Es el lab más grande del arco.** Aplica la regla de siempre: tres horas tope, y lo que no cabe
va a «lo que no vimos hoy» con su nombre.

---

## 1 · La arquitectura (cerrada — no rediseñar)

```
                    POSTMAN
                       │ :8200
                       ▼
             ┌──────────────────┐
             │     GATEWAY      │  enruta · valida JWT · circuit breaker
             └───┬──────────┬───┘
          :8201  │          │  :8202
        ┌────────▼─────┐  ┌─▼──────────────┐
        │CONTRIBUYENTES│◄─┤    TRÁMITES    │
        │  PG :55450   │  │   PG :55451    │
        └──────────────┘  └───────┬────────┘
                                  │ evento
                          ┌───────▼────────┐
                          │   AUDITORÍA    │  :8203
                          │   PG :55452    │
                          └────────────────┘
```

**Tres bases distintas, una por servicio.** Es el punto: Trámites **no puede** hacer JOIN con
Contribuyentes; tiene que preguntarle por HTTP. Esa imposibilidad es la lección.

**Fuera del alcance, y se dice por qué:** Eureka y Config Server. Son dos servicios de
infraestructura más que el alumno solo configuraría sin entender, y en tres horas no caben. Se
explican en teoría y se resuelven con URLs en configuración — que además es lo que hacen muchos
sistemas reales. Va a «lo que no vimos hoy».

---

## 2 · Estructura

```
labs/lab-14-microservicios/
├── README.md          incluye el diagrama y el orden de arranque
├── PASOS.md
├── practica/
│   ├── gateway/           proyecto Maven
│   ├── contribuyentes/    proyecto Maven
│   ├── tramites/          proyecto Maven
│   └── auditoria/         proyecto Maven
├── solucion/          los mismos cuatro, completos
└── instructor/        los archivos documentados, no ejecutable, gitignored
```

Cada servicio con su maleta, su puerto y su base. Reglas de la casa sin cambios: `practica/` sin
documentación, `solucion/` con poca, `instructor/` con todo.

⚠️ **Cuatro terminales abiertas es la mayor fricción operativa del lab.** El README debe traer
el **orden de arranque** (contribuyentes → trámites → auditoría → gateway) y una nota de qué
pasa si se arranca al revés. Y las guardas de puerto y candado (SPEC-FIX-07 y 08) tienen que
estar en los cuatro: aquí es donde más falta van a hacer.

⚠️ Si arrancar cuatro procesos resulta demasiado en la práctica —memoria, tiempo, confusión—
**reportarlo antes de terminar**: se podría fundir auditoría dentro de trámites y quedarían
tres. La medición manda sobre el diagrama.

---

## 3 · Los pasos

**Paso 0 · El monolito que teníamos** (10 min, sin teclado)
Recordar que todo esto era una sola app con una base. Plantear la pregunta que el lab responde:
qué se gana y qué se paga al partirlo. Que apuesten antes.

**Paso 1 · Levantar el sistema**
El alumno arranca los cuatro y comprueba que están vivos. Cuatro terminales, cuatro puertos.
Esto es infraestructura, no código: que lo vean funcionando antes de tocarlo.

**Paso 2 · Una base por servicio**
Pedir un trámite y ver que trae el nombre del contribuyente. Mostrar en el código que **no hay
JOIN**: hay una llamada HTTP. Y en las bases, que la tabla del otro no existe.

**Paso 3 · La llamada entre servicios**
Escribir el cliente HTTP de Trámites hacia Contribuyentes. Ver los dos logs a la vez: la
petición saliendo de uno y entrando al otro.

**Paso 4 · Matar un servicio** ← el momento fuerte
Apagar Contribuyentes y volver a pedir el trámite. **Medir cuánto tarda en fallar y qué error
sale.** Un servicio caído se llevó al otro por delante. Eso es el fallo en cascada, y es la
razón de existir de todo lo que viene después.

**Paso 5 · Circuit breaker y degradación**
Proteger la llamada. Volver a matar Contribuyentes: ahora el trámite responde **sin** el nombre,
en vez de no responder. Degradar en vez de caer. **Citar los dos comportamientos medidos.**

**Paso 6 · El gateway**
Un solo puerto de entrada. El cliente pide a `:8200` y no sabe que hay tres servicios detrás.
Enrutado por ruta, y el JWT validado en la puerta.

**Paso 7 · Seguir una petición por tres servicios**
Correlation ID: una petición entra por el gateway y aparece con el mismo id en los tres logs. Es
lo que hace depurable un sistema repartido — y sin esto, no lo es.

**Paso 8 · Consistencia eventual**
Trámites avisa a Auditoría. Ver que el trámite responde **antes** de que auditoría registre, y
que si auditoría está caída el trámite igual se crea. No hay transacción que abarque dos
servicios: hay que elegir.

**Cierre** — volver a la pregunta del paso 0, ahora con la experiencia encima: qué ganamos
(equipos independientes, desplegar por separado, fallar por partes) y qué pagamos (latencia,
consistencia, cuatro terminales, depurar es más difícil). **Sin vender microservicios**: el
alumno tiene que salir sabiendo también cuándo NO.

**Si los ocho pasos no caben en tres horas**, recortar por el final (8, luego 7) y decirlo.

---

## 4 · gRPC

La SPEC-036 dejó gRPC pendiente de decidir. **Si el mocito recomendó traerlo aquí y el tooling
de protobuf funciona offline**, entra como paso adicional entre el 3 y el 4: la misma llamada
entre servicios, ahora en binario, comparando. Si no funciona offline o no cabe en tres horas,
**no entra** y queda declarado en el mapa.

## 5 · Verificación

| # | Prueba | Criterio |
|---|---|---|
| V1 | Los cuatro de `practica/` arrancan en su estado de entrega | Citado |
| V2 | Los cuatro de `solucion/` levantados a la vez, sistema completo | Citar el flujo de punta a punta por el gateway |
| V3 | Paso 4 | El error y el tiempo medidos con Contribuyentes caído |
| V4 | Paso 5 | El mismo caso con circuit breaker: respuesta degradada. Citar ambos |
| V5 | Paso 7 | El mismo correlation id en los tres logs. Citarlos |
| V6 | Paso 8 | Trámite creado con Auditoría caída. Citado |
| V7 | Seguir `PASOS.md` completo sobre `practica/` | Se llega al sistema de `solucion/` |
| V8 | Memoria y tiempo con los cuatro arriba | Reportado. Si es inviable en una máquina de alumno, decirlo |
| V9 | Offline · `instructor/` invisible · tamaño | 0 descargas; git no ve `instructor/` |

**V3 y V4 son el corazón del lab**: sin esos dos números medidos, el lab no enseña nada que no
se pueda leer en un blog.

## 6 · Entregable

`INFORME-SPEC-037`: la arquitectura final (si cambió, por qué), los pasos con su duración, los
números de V3/V4, la decisión sobre gRPC, y qué quedó en «lo que no vimos hoy». Actualizar
`MAPA-LAB-MODULO.md` — este lab cierra brechas. `ESTADO.md` al día.

## 7 · Prohibiciones

- ❌ Docker, Compose, Eureka, Config Server, registry o cualquier cosa que no viaje en la maleta.
- ❌ Más de cuatro servicios. Si sobra alguno, se quita, no se suma.
- ❌ Pasar de tres horas.
- ❌ Vender microservicios: el cierre dice también cuándo no usarlos.
- ❌ Declarar un número sin la salida que lo respalde.
- ❌ sudo · LFS · credenciales · ≥95 MB · que `instructor/` llegue al repositorio.
