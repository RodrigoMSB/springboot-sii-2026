# SPEC-010 · Lab 04 «El árbol de trámites»

| Campo | Valor |
|---|---|
| ID | SPEC-010 |
| Título | Cuarto laboratorio: modelar bien, y plantar sin saberlo la bomba de la próxima semana (S04 · M5 3,0 h) |
| Autor | Arquitecto |
| Aprueba | PO (Rodrigo) — **APROBADA** |
| Depende de | SPEC-009 |
| Estado | LISTA PARA EJECUCIÓN |

> **Instrucción de ejecución (mocito):** guardar este archivo íntegro en
> `docs/specs/SPEC-010-lab04-el-arbol-de-tramites.md` y commitearlo en rama antes de
> ejecutar. Apila sobre la pila (#6→#8) y decláralo. Protocolo de dos etapas vigente.

---

## §1 · Objetivo

Que exista `labs/lab-04-el-arbol-de-tramites/`: la sesión completa de persistencia.
El alumno sale con: las relaciones del dominio corregidas y con fetch **explícito**
(AU-04 instalada por él), métodos derivados, JPQL multi-entidad, y su primer reporte
con `JdbcClient` sin cargar entidades. Encadenamiento: `starter/` = `solucion/` del
Lab 03 + el crimen plantado + los huecos.

**La particularidad dramática de este lab:** su crimen no explota hoy. La corrección
correcta que hace el alumno (LAZY explícito) es, deliberadamente, **la semilla del
crimen del Lab 05** — el listado que itera relaciones perezosas y que a 50.000 lotes
costará once segundos. El curso enseña aquí que "lo correcto" también tiene
consecuencias que hay que medir. Nada de esto se le dice al alumno todavía.

## §2 · El crimen

En el `starter/`, todas las relaciones (`@ManyToOne`, `@OneToMany`, `@OneToOne`) vienen
en `FetchType.EAGER`, con el comentario del practicante: *"EAGER en todo — así no sale
más el LazyInitializationException"*. Todo funciona. Los tests pasan. **Nadie lo nota.
Ese es el punto.**

Guion del relator (10 min): abre el log SQL (`starter/` trae el logging de SQL
activado), pide un solo trámite por id... y proyecta **el muro de JOINs**: medio árbol
de la base de datos viajando para responder una ficha. Pausa. *"Funciona, ¿no? Nadie se
ha quejado. Guárdense esa cara de 'y qué importa' — la vamos a necesitar la próxima
semana."* Y Carolina: *"Hoy no vinimos a apagar un incendio. Vinimos a entender qué
compramos cada vez que escribimos una anotación de fetch. El que no declara el fetch,
lo está declarando igual — solo que no sabe cuál."*

## §3 · Los tres actos

- **Acto 1 · Choque:** el muro de JOINs por pedir una ficha.
- **Acto 2 · El parche bruto que FUNCIONA:** la guía hace quitar el `fetch` de las
  anotaciones ("que decida el framework"). Compila, corre, el muro sigue o cambia según
  el tipo de relación — porque **los defaults de JPA son distintos por anotación**
  (`@ManyToOne` EAGER, `@OneToMany` LAZY) y ahora nadie sabe qué está pasando sin
  mirar la especificación. No declarar no es neutralidad: es delegar a una tabla que
  casi nadie recuerda.
- **Acto 3 · La forma correcta:** fetch **explícito siempre**, LAZY como norma, y AU-04
  vigilando que ninguna relación vuelva a quedar en manos del default.

## §4 · Los TODOs (4 × ~15 min)

1. **TODO_1 — AU-04 y la corrección:** el alumno instala la regla (todo `@ManyToOne` y
   `@OneToOne` declara `fetch` explícito) con fixture y meta-test, y corrige las
   relaciones del dominio a LAZY. Test del enunciado adicional: `findById` de un
   trámite **no** carga sus relaciones (verificado con `PersistenceUnitUtil.isLoaded`,
   no contando consultas — el contador de consultas es la herramienta del Lab 05 y no
   se adelanta).
2. **TODO_2 — Métodos derivados:** `findByContribuyenteRut...`, `existsBy...`,
   `countByEstado` sobre el repositorio de trámites; tests del enunciado en slice de
   persistencia. Incluye el caso con `Pageable` básico (la paginación profunda y las
   proyecciones son del Lab 05).
3. **TODO_3 — JPQL multi-entidad:** `@Query` que responde una pregunta de negocio
   (trámites de un período con F29 presentado), con parámetros nombrados. La pista
   **prohíbe explícitamente** `JOIN FETCH` con la frase: *"existe, y es la respuesta a
   una pregunta que todavía no te has hecho. La próxima semana te la vas a hacer."*
4. **TODO_4 — `JdbcClient`:** reporte agregado (total declarado por período, montos
   sumados en SQL) sin tocar una sola entidad. La lección: no todo lo que lee la base
   merece el peaje del ORM.

El cascade correcto (`Formulario29` → líneas, con `orphanRemoval`) viene **resuelto en
el andamio** con Javadoc extenso — es lectura, no tecleo (la palanca de D-010).

## §5 · Anatomía

La de SPEC-000 §7.6 completa. `TEORIA.md`: M5 primera parte (mapeo de relaciones, dueño
de la relación, uni vs bidireccional, cascade, **la tabla de defaults de fetch por
anotación** — proyectable, es el acto 2 —, repositorios, consultas derivadas, JPQL vs
nativo, `JdbcClient`), con el DO/DON'T estelar: *"DON'T: confiar en el default de
fetch. DO: declararlo aunque coincida."* **Siembra del Lab 05 (la mejor del curso hasta
ahora, cuídala):** *"hiciste lo correcto: LAZY explícito. La próxima semana, ese mismo
LAZY, iterado por un listado inocente sobre 50.000 lotes, va a costar once segundos.
Lo correcto también se mide. Trae cronómetro."* Plantillas con trampa registrada y la
transcripción natural: **el muro de JOINs del acto 1, pegado tal cual** (la pregunta
del reporte: *"¿cuántas tablas viajaron para responder una ficha? Cuéntalas en tu
transcripción"*). Decisión de infraestructura de tests del enunciado (slice con H2 vs
Testcontainers): **a tu juicio, declarada** — con una restricción: si eliges H2, la
TEORIA no vende el dialecto como equivalente (esa mentira se desmonta en el Lab 05
con M6; no la fabriques aquí).

## §6 · Verificación del ejecutor (etapa 1)

Citado, sobre estado limpio: (1) `91-e2e` ×2 — starter falla nombrando los 4 TODOs,
solución 100 %; (2) el crimen visible: el log SQL del `findById` en el starter (el
muro, pegado en el reporte) contra el de la solución (las consultas mínimas);
(3) acto 2 medido: sin `fetch` declarado, cita qué default aplicó cada anotación y
cómo se ve en el SQL; (4) AU-04 muerde: una relación sin fetch explícito en copia →
rojo citado con nombre; (5) la prohibición de la pista es real: la solución de TODO_3
**no contiene** `JOIN FETCH` (el enunciado lo verifica — como test, no como grep);
(6) manifiesto discrimina; (7) `deriva` (8 eslabones) y `siembra` (audita L3→L4)
verdes en el runner, citados; (8) CI verde, run citado; (9) `ESTADO.md` al día;
estimación honesta por TODO.

## §7 · La Prueba del PO (etapa 2 — cierra la SPEC)

Invitación literal, resultados esperados escritos, tres pasos: (1) starter arriba, un
`curl` a la ficha de un trámite, y **mirar el log SQL: el muro de JOINs** — "funciona,
¿no? guárdate esa cara"; (2) lo mismo en la solución: el contraste en pantalla;
(3) `90 --dir solucion` → aprobado. Declara Java/Docker por paso y recuerda la fila de
pruebas acumuladas del PO con sus rutas.

## §8 · Criterios de aceptación

- [ ] SPEC commiteada antes del material; rama + PR apilado y declarado.
- [ ] Lab 04 completo (las 5 piezas); evidencia §6 íntegra, sabotajes incluidos.
- [ ] TODOs ≤ ~15 min c/u (o recorte aplicado y declarado).
- [ ] AU-04 instalada con fixture y meta-test; el dominio de la solución con fetch
      explícito al 100 %.
- [ ] La siembra del Lab 05 presente en TEORIA.md, con el gancho del cronómetro.
- [ ] `ESTADO.md` y bitácora al día.
- [ ] **Prueba del PO reportada** — cierra la SPEC.
- [ ] Commits `SPEC-010:`; checks verdes citados.

## §9 · Reporte

Evidencia de §6 (el muro de JOINs incluido), tiempos por TODO, la decisión H2 vs
Testcontainers explicada, URL del run, `git log --oneline`, discrepancias y hallazgos —
sin tocarlos. Cierra con la invitación del §7.
