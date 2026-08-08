# SPEC-020 · Lab 14 «La DGT se parte en pedazos»

| Campo | Valor |
|---|---|
| ID | SPEC-020 |
| Título | Laboratorio de microservicios: seis piezas, un sistema, y qué pasa cuando una se cae |
| Autor | Arquitecto |
| Aprueba | PO (Rodrigo) — **APROBADA** |
| Depende de | SPEC-019 (Lab 13) |
| Estado | LISTA PARA EJECUCIÓN |

> **Instrucción de ejecución (mocito):** guardar este archivo íntegro en
> `docs/specs/SPEC-020-lab14-microservicios.md` y commitearlo en rama antes de ejecutar.
> **Base:** apila sobre `spec/019`.
>
> **Este lab es distinto a todos los anteriores. Léelo entero antes de construir nada.**
> No se teclea una aplicación: se **levanta un sistema, se rompe y se observa**. El único
> tecleo real son cuatro números de configuración. Si tu automatismo de los labs previos
> choca con esto, manda esta SPEC.

---

## §1 · Objetivo

Que exista `labs/lab-14-la-dgt-se-parte-en-pedazos/`: el laboratorio de cierre que cubre
el alcance del **título oficial del curso** (*"Desarrollo de Microservicios en Java"*) y
el objetivo contractual de **despliegue e interoperabilidad**. El alumno sale entendiendo
qué sostiene a qué en un sistema distribuido, cómo se degrada cuando una pieza cae, y
por qué el costo operacional de partir un sistema es real.

**Origen del material:** el PO aportó un set de seis prácticas de Spring Cloud (Eureka,
Gateway, Config Server, Feign, LoadBalancer, CircuitBreaker) que se trasladan al dominio
DGT y se corrigen según §6. **Los seis patrones se conservan; el dominio y la calidad
cambian.**

## §2 · Stack (verificado, no supuesto)

| Componente | Versión | Nota |
|---|---|---|
| Java | **25** | El del curso, sin excepción |
| Spring Boot | **4.1.0** | El del curso |
| **Spring Cloud** | **2025.1.2** ("Oakwood") | Publicado 2026-06-11; compatible con Boot 4.0.7 e **introduce compatibilidad con Boot 4.1.0** |

Se importa vía BOM: `org.springframework.cloud:spring-cloud-dependencies:2025.1.2`.

**Advertencia de estado del arte (va también a la teoría):** del stack Netflix histórico
**solo Eureka sigue vivo** y soportado en el tren actual. Hystrix, Ribbon y Zuul están
muertos y fuera del release train — si aparecen en un tutorial que el alumno encuentre,
está leyendo material caducado. **Verifica que ninguna dependencia del lab los arrastre.**

**Config Server sin `bootstrap.yml`:** la fase bootstrap ya no está en el camino por
defecto de Boot; los clientes usan `spring.config.import`. El material del PO usaba el
modelo antiguo — hay que migrarlo.

## §3 · La arquitectura: 6 procesos, 4 proyectos

| # | Servicio | Rol | Puerto | Patrón |
|---|---|---|---|---|
| 1 | **dgt-registro** | Eureka Server: la guía telefónica | 8761 | Service Discovery |
| 2 | **dgt-config** | Config Server **backend `native`** (archivos locales, **no Git**) | 8888 | Configuración centralizada |
| 3 | **dgt-portal** | API Gateway: la única puerta al exterior | **8099** (el del curso) | Enrutamiento |
| 4 | **dgt-contribuyentes** | Proveedor: datos del contribuyente | efímero | Servicio registrado |
| 5 | *(la misma imagen, 2ª instancia)* | — | efímero | **LoadBalancer** |
| 6 | **dgt-tramites** | Consumidor: el servicio que ya conocen | efímero | **Feign + CircuitBreaker + Retry + fallback** |

**Puertos efímeros a propósito** en 4/5/6: con puertos fijos no se puede levantar una
segunda instancia, y esa es media lección del lab.

**Decisión del PO — Config Server `native`:** la configuración de cada servicio vive en
una carpeta de archivos versionada en el propio lab (`config-repo/` con
`application.yml` común + `dgt-tramites.yml`, `dgt-contribuyentes.yml`,
`dgt-portal.yml`). Razones: el alumno **ve** la configuración y la edita, no depende de
red ni de un repo externo, y el lab enseña *configuración centralizada*, no Git. La
teoría declara en dos frases que en producción esto vive en un repo Git por el historial
y la reversión — el alumno se lleva el concepto y sabe cuál es la versión productiva.

**Alcance del dominio:** `dgt-contribuyentes` y `dgt-tramites` son versiones **reducidas**
del monolito, no el monolito partido de verdad. Suficiente para que la interacción sea
real (el trámite necesita el nombre del contribuyente), sin cargar trece labs de código.
Declara qué dejaste fuera y por qué.

## §4 · El crimen (10 min, en vivo)

Las seis piezas levantadas, el panel de Eureka con todos anotados, el portal
respondiendo. Carolina:

> *"Funciona precioso. Ahora apaga el servicio de contribuyentes."*

Se apaga **una** pieza. El portal **sigue respondiendo** — pero los trámites vuelven sin
el nombre del contribuyente. Funciona a medias, y nada en pantalla dice que algo se cayó.

> *"Un monolito caído es una pantalla en blanco: lo ves y sabes que estás jodido. Un
> sistema distribuido caído es peor — funciona a medias, y nadie sabe qué mitad. Y
> alguien va a firmar una declaración con datos incompletos sin enterarse. Hoy vas a
> aprender a mirar los pedazos."*

## §5 · La sesión (una sola, 3 h) — cuatro bloques

| Bloque | Min | Qué hace el alumno |
|---|---|---|
| **Teoría** | ~35 | Qué es una arquitectura de microservicios, los seis patrones, **cuándo NO conviene** (el costo operacional es contenido, no nota al pie), y el estado del arte del §2 |
| **1 · Levantar y mirar** | ~20 | `docker compose up`; ver el registro poblarse, el portal enrutar por nombre lógico, la config bajando del server. **Dibuja en su reporte quién depende de quién** |
| **2 · Matar al proveedor** ⭐ | ~40 | Apaga `dgt-contribuyentes` → observa el fallback. Lo pone lento → ve abrirse el circuito. **Aquí está el único tecleo: los umbrales de Resilience4j** (§6.1) |
| **3 · Escalar** | ~30 | Levanta la 2ª instancia, ve el balanceo repartir, mata una, el sistema no se entera |
| **4 · Matar al registro** | ~15 | **Demo del relator** (por tiempo): apaga Eureka y el sistema *sigue funcionando un rato* por el caché de los clientes, y después se degrada. *La pieza más crítica no falla como esperas* |
| Cierre | ~10 | Reporte y despedida |

## §6 · Las correcciones al material del PO (obligatorias)

| # | Defecto auditado | Corrección |
|---|---|---|
| 1 | **Sin configuración de Resilience4j** — el circuito abría con defaults invisibles | Umbrales **explícitos y comentados** (ventana, mínimo de llamadas, tasa de fallo, tiempo abierto, llamadas en half-open). **Y ese es el TODO del lab**: el alumno los modifica y comprueba el efecto |
| 2 | **Tests vacíos** (solo `contextLoads`) | Tests que muerden: el fallback dispara, el circuito abre tras N fallos y **falla rápido** (medido), el balanceo reparte entre instancias |
| 3 | **Credenciales en properties** y `ddl-auto=update` | Variables de entorno + Flyway. **No podemos contradecir los Labs 01 y 06 en la última sesión** |
| 4 | Gateway en **puerto 443** (requiere root) | Puerto 8099, la convención del curso |
| 5 | Datos recargados en cada request (duplicación) | Carga única o desde base |
| 6 | Paquetes inconsistentes (`cl.sii` vs `com.todocodeacademy`) | Todo bajo `cl.dgt.*` |
| 7 | Config Server con `bootstrap.yml` + backend Git | Backend **`native`** + clientes con `spring.config.import` |

## §7 · Anatomía

La de SPEC-000 §7.6, **con estas diferencias declaradas** (el `deriva` y el `siembra`
deben contemplarlas sin parches ad-hoc — este lab **no deriva del anterior**: es un
sistema nuevo, y hay que declararlo en el mecanismo):

- **No hay `starter/` y `solucion/` de un proyecto**: hay `sistema/` con los cuatro
  proyectos Maven + `config-repo/` + `compose.yaml`. El "starter" es el sistema con los
  umbrales de Resilience4j sin declarar (defaults invisibles); la "solución", con ellos
  explícitos y ajustados.
- **`bin/`**: `start-lab.sh` levanta las seis piezas y espera a que el registro las vea
  todas (sin `sleep` ciego); banderas `--matar-contribuyentes`, `--contribuyentes-lento`,
  `--escalar` y `--matar-registro` para reproducir cada bloque; `90-validar.sh` con los
  tests; `99-destruir.sh` que baja **solo lo del lab**.
- **`TEORIA.md`**: los seis patrones con la analogía de cada uno; **cuándo NO usar
  microservicios** (sección propia, con criterio: latencia de red, consistencia
  eventual, costo operacional, "no lo hagas por moda"); el estado del arte del §2; y el
  cierre del arco del curso.
- **`diagramas/`**: el mapa del sistema en mermaid, y el mismo mapa con la pieza caída.
- **`INSTRUCTOR.md`**: minutado, el guion del crimen, la demo del bloque 4, y **qué
  hacer si la máquina del alumno no aguanta seis contenedores** (ver §8.5).
- **Este lab no siembra** (es el último): el job `siembra` debe eximirlo, como al Lab 00
  y al 13.

## §8 · Verificación del ejecutor (etapa 1)

Citado, sobre estado limpio: (1) el sistema completo levanta y **las seis piezas quedan
registradas** — cita el estado del registro; (2) el crimen: `--matar-contribuyentes` y
el portal respondiendo degradado (citado, con el JSON antes y después); (3)
`--contribuyentes-lento` → el circuito abre y **falla rápido** (tiempos citados
antes/después de abrir); (4) `--escalar` → el balanceo reparte entre las dos instancias
(citado con el conteo por instancia); matar una → el sistema sigue (citado); (5)
`--matar-registro` → **cuánto tarda en degradarse** (medido y citado; es el dato de la
demo del relator); (6) el TODO muerde: con umbrales laxos el circuito **no** abre en el
escenario del lab, con los correctos sí — ambos citados; (7) los tests son
**deterministas** (×3 corridas citadas, sin `Thread.sleep`); (8) **ninguna dependencia
arrastra Hystrix/Ribbon/Zuul** — verifícalo en el árbol de dependencias y cítalo; (9)
`deriva` y `siembra` verdes con este eslabón especial declarado; (10) CI verde — **si
seis contenedores hacen inviable el job, repórtalo y declara la exclusión en el YAML con
su razón, no relajes los tests**; (11) `ESTADO.md` al día: **14 labs**.

**§8.5 · Consumo de recursos — dato obligatorio del reporte:** mide y cita **cuánta RAM
consume el sistema completo levantado**. Es el dato que decide si un alumno con 8 GB
puede correr el lab. Si no cabe, propón el plan B (levantar cinco piezas en vez de seis,
o el bloque 3 como demo) — **pero mídelo, no lo estimes.**

## §9 · La Prueba del PO (etapa 2 — cierra la SPEC)

Invitación literal, tres pasos: (1) levantar el sistema y **ver el panel de Eureka con
las seis piezas anotadas**; (2) `--matar-contribuyentes` y ver el portal respondiendo
degradado — *el sistema funcionando a medias sin avisar*; (3) `90-validar.sh` →
aprobado. Declara Java/Docker por paso, el tiempo de arranque y la RAM que necesita.

## §10 · Criterios de aceptación

- [ ] SPEC commiteada antes del material; rama apilada sobre `spec/019`, PR abierto.
- [ ] Los **seis patrones** presentes y demostrados: discovery, gateway, config
      centralizada, Feign, balanceo, circuit breaker con fallback.
- [ ] Las **siete correcciones** de §6 aplicadas.
- [ ] Spring Cloud **2025.1.2** sobre Boot 4.1.0 y Java 25, compilando.
- [ ] Sin Hystrix/Ribbon/Zuul en el árbol de dependencias (citado).
- [ ] Config Server en modo `native`, sin Git ni `bootstrap.yml`.
- [ ] Evidencia §8 íntegra, **incluida la medición de RAM**.
- [ ] `siembra` exime al Lab 14 con su razón citada.
- [ ] `ESTADO.md` y bitácora al día — **14 labs**.
- [ ] **Prueba del PO reportada** — cierra la SPEC.
- [ ] Commits `SPEC-020:`; checks verdes citados.

## §11 · Reporte

Evidencia de §8 (RAM incluida), qué dejaste fuera del dominio y por qué, decisiones
declaradas (umbrales elegidos y su porqué, cómo se orquesta el arranque ordenado, cómo
se resolvió el CI), URL del run, `git log --oneline`, discrepancias y hallazgos — sin
tocarlos. Cierra con la invitación del §9.

**Y tu juicio honesto sobre lo más importante: ¿cabe esto en 3 horas?** Si crees que no,
dilo con el desglose de tiempos reales que mediste. Es el dato más valioso del reporte.
