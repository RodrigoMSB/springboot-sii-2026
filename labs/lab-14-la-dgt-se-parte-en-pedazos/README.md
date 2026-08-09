# Lab 14 · La DGT se parte en pedazos

> *«Funciona precioso. Ahora apaga el servicio de contribuyentes.»*
> — Carolina, Jefa de la Plataforma de Trámites

**Sesión 14 · 3 h · Arquitectura de microservicios, despliegue e interoperabilidad.**

> ⚠️ **Este laboratorio está CONGELADO y no tiene módulo contractual asignado.** La
> auditoría SPEC-AUDIT-01 constató que el temario contratado no incluye un módulo de
> microservicios —su M15 es «Contenedores, Arranque Acelerado y Proyecto Final», que cubre
> el Lab 13— ni una sesión 14. El material se conserva íntegro; su encaje con el SII lo
> decide el PO. Hasta entonces, no se dicta como parte del programa contratado.

---

## Este laboratorio es distinto a los trece anteriores

No vas a teclear una aplicación. **Vas a levantar un sistema, romperlo y mirar qué pasa.**

El único tecleo real son **cuatro números** en un archivo de configuración. Todo lo demás
está escrito, funcionando y comentado, y tu trabajo es leerlo, ejecutarlo, romperlo y
sacar conclusiones. Si te parece poco trabajo, espera al bloque 4.

La razón es simple: en tres horas no se construye un sistema distribuido. Lo que sí se
puede hacer en tres horas —y no se puede hacer leyendo— es **verlo caer**.

---

## El crimen

Seis piezas levantadas. El panel del registro con todas anotadas. El portal respondiendo.
Carolina apaga **una** pieza.

El portal **sigue respondiendo**. HTTP 200. JSON bien formado. Ni una línea roja.

Solo que los trámites vuelven **sin el nombre del contribuyente**, y nada en pantalla lo
dice.

> *«Un monolito caído es una pantalla en blanco: lo ves y sabes que estás jodido. Un
> sistema distribuido caído es peor — funciona a medias, y nadie sabe qué mitad. Y alguien
> va a firmar una declaración con datos incompletos sin enterarse. Hoy vas a aprender a
> mirar los pedazos.»*

---

## El mapa: seis piezas, cinco proyectos

| # | Pieza | Qué hace | Puerto |
|---|---|---|---|
| 1 | **dgt-registro** | La guía telefónica (Eureka Server) | 8761 |
| 2 | **dgt-config** | Config Server — la configuración de todos | 8888 |
| 3 | **dgt-portal** | El gateway: la única puerta al exterior | **8099** |
| 4 | **dgt-contribuyentes** | Quién es el dueño de un RUT | efímero |
| 5 | *(la misma imagen otra vez)* | La segunda instancia — el balanceo | efímero |
| 6 | **dgt-tramites** | El que necesita al de al lado. El que sufre | efímero |

*Más PostgreSQL, con **dos bases y dos usuarios**: cada servicio dueño de los suyos, sin
credenciales para entrar en los del vecino.*

Los diagramas están en `diagramas/`: el mapa entero, y el mismo mapa con la pieza caída.

**Por qué los puertos son efímeros:** con un puerto fijo no se puede levantar una segunda
instancia — la segunda muere con «Address already in use», y ahí se va media lección. Y si
ni ellas saben en qué puerto quedaron, ¿cómo las encuentra nadie? No las encuentra: le
pregunta al registro. Ese es el patrón entero.

---

## Cómo se corre

```bash
./bin/start-lab.sh                        # las seis piezas, todo sano
./bin/start-lab.sh --matar-contribuyentes # ⭐ el crimen
./bin/start-lab.sh --contribuyentes-lento # el circuito abriéndose
./bin/start-lab.sh --escalar              # el balanceo, y matar una instancia
./bin/start-lab.sh --matar-registro       # la demo del bloque 4
./bin/start-lab.sh --reiniciar-tramites   # tras editar tu configuración

./bin/90-validar.sh                       # el veredicto
./bin/99-destruir.sh                      # dejarlo todo como estaba
```

El panel del registro: **http://localhost:8761**
La única puerta: **http://localhost:8099/api/v1/tramites**

---

## Lo que le pide a tu máquina

**Medido, no estimado**, con las seis piezas levantadas y en reposo (Apple Silicon, Docker
Desktop, imágenes ya construidas):

| | |
|---|---|
| **RAM del sistema completo** | **1,54 GiB** (7 contenedores) |
| Techo configurado | 2,94 GiB — cada contenedor con su `mem_limit` |
| **Arranque** | **56 s** con las imágenes ya construidas |
| Primera vez, medido por partes | 21 s compilar (repositorio Maven vacío, 146 MB de descarga) + 10 s construir las cinco imágenes + 56 s arrancar |
| Disco | **1,22 GB** en imágenes (232 MB de capa base compartida + 992 MB propios) |

Sobre la «primera vez»: los 31 s de compilar y construir se midieron con una conexión
rápida y con la imagen base (`eclipse-temurin:25-jre-alpine`, 232 MB) ya descargada. Con
una conexión lenta, lo que manda es la descarga —146 MB de dependencias más 232 MB de
imagen base—, no el procesador. Presupuesta unos minutos la primera vez y ninguno las
siguientes.

Desglose por pieza, en reposo:

```
dgt-tramites          296 MiB      dgt-registro       233 MiB
dgt-contribuyentes×2  275 MiB c/u  dgt-portal         212 MiB
dgt-config            199 MiB      postgres            80 MiB
```

**¿Cabe en 8 GB?** Sí, con margen: 1,54 GiB de contenedores más lo que gaste Docker
Desktop. Lo que conviene es cerrar el IDE pesado y el navegador con cuarenta pestañas
mientras dure la sesión.

**¿Y si tu máquina no llega?** Está previsto, y está en `INSTRUCTOR.md`, §«Plan B»: se baja
a cinco piezas (una sola instancia de contribuyentes) y el bloque 3 se hace como demo del
relator. Se pierde la práctica del balanceo; no se pierde ningún concepto.

---

## Tu trabajo: cuatro números

Está en `sistema/config-repo/dgt-tramites.yml`, en el bloque
`resilience4j.circuitbreaker`. Ahora mismo **no hay ni un umbral declarado**, así que
manda el valor por defecto de Resilience4j:

```
minimum-number-of-calls: 100
```

Cien llamadas antes de que el circuito llegue a *opinar* sobre si el vecino está caído.
En la sesión de hoy no hay cien llamadas. En un servicio interno con poco tráfico, tampoco.

Eso es un **circuit breaker decorativo**: está en el árbol de dependencias, sale en el
diagrama de arquitectura, y no va a abrirse jamás.

Tu trabajo es declarar los cuatro umbrales con los valores que **tú** elijas y puedas
defender. No hay una respuesta única; hay una condición que cumplir:

> con el proveedor lento, el circuito tiene que **abrirse** dentro del escenario del
> laboratorio.

El validador comprueba la condición. La defensa la haces tú, en el reporte.

---

## Qué NO está aquí, y por qué

Este sistema es una **reducción deliberada** del monolito de trece sesiones, no el monolito
partido de verdad. Se quedó fuera:

| Fuera | Por qué |
|---|---|
| Folio, F29, líneas, adjuntos, cierre nocturno | El laboratorio trata de **cómo se hablan los servicios**, no del dominio. Cargarlo entero serían mil líneas que nadie va a leer hoy |
| Seguridad (JWT, ClaveÚnica, roles) | Un gateway con autenticación de borde es media sesión por sí solo. Se nombra en la teoría, no se construye |
| Trazas distribuidas | Es lo primero que hace falta para operar esto de verdad — y por eso el Lab 10 va **antes** que este. Aquí se señala la ausencia, no se rellena |
| Sagas / consistencia eventual | Se explica el problema en la teoría (§5.2). Construir una saga es otra sesión |
| Kafka | Decisión del curso (D-005): RabbitMQ práctico, Kafka conceptual. Aquí ni uno ni otro |

**Lo que sí es real y no está recortado:** las seis piezas, los seis patrones, las dos bases
con sus dos usuarios, y todas las formas de caerse.

Y el monolito no desapareció: sigue entero en `labs/lab-13-capsula-y-egreso/`. Ponlos al
lado y mira qué ganaste y qué pagaste. Ese es el ejercicio de verdad.

---

## Las tres horas

| Bloque | Min | Qué pasa |
|---|---|---|
| 🔪 **El crimen** | 10 | En vivo. Se apaga una pieza y el sistema miente |
| 📚 **Teoría** | 35 | Los seis patrones · **cuándo NO** · el estado del arte |
| ☕ Descanso | 10 | |
| 🔧 **1 · Levantar y mirar** | 20 | El registro poblándose. Dibujas quién depende de quién |
| 🔧 **2 · Matar al proveedor** ⭐ | 40 | El fallback. El circuito. **Los cuatro números** |
| 🔧 **3 · Escalar** | 30 | Dos instancias, el balanceo, y matar una |
| 🎬 **4 · Matar al registro** | 15 | Demo del relator. No falla como esperas |
| ✅ Cierre | 10 | Reporte y despedida |

Las guías están en `guia/`, una por bloque.

---

## Para el instructor

Todo el minutado, el guion del crimen palabra por palabra, la demo del bloque 4 con sus
dos actos, el plan B de memoria y los errores que va a cometer la sala están en
**`INSTRUCTOR.md`**.

Dos avisos que no pueden esperar a que lo abras:

1. **Levanta el sistema antes de que llegue la gente.** La primera vez compila cinco
   proyectos y construye cinco imágenes. No es algo que se haga delante de la sala.
2. **El bloque 4 es demo tuya, no práctica.** Son quince minutos y hay que apagar y
   levantar piezas en un orden concreto.
