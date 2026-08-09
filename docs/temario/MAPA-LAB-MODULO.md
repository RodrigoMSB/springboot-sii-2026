# Mapa Laboratorio ↔ Módulo del temario contratado

*Documento de trazabilidad para la entrega al SII. Generado por la SPEC-FIX-02 §3.*

**Fuente de verdad:** `docs/temario/TEMARIO-SPRING-BOOT-SII-v3.md`, §«Estructura del
Programa» y §«Matriz Módulo × Sesión`. Donde el material y el temario discrepen, **manda el
temario**.

---

## 1 · La correspondencia

| Lab | Nombre | Módulo(s) del temario | Sesión de la matriz | Horas del contrato |
|---|---|---|---|---|
| **00** | Estación base | *Ninguno* — pre-vuelo | *Ninguna* | No computa |
| **01** | Del otro lado del botón | **M1** Fundamentos de Spring Boot 4 y Configuración · **M2** Controladores REST, Versionado Nativo y OpenAPI | S01 | 2,0 + 1,0 |
| **02** | El folio que se filtró | **M2** Controladores REST… · **M3** Arquitectura en Capas, DTOs, Validaciones y Excepciones | S02 | 1,5 + 1,5 |
| **03** | Red de seguridad | **M3** Arquitectura en Capas… · **M4** Testing I: JUnit 6 y Mockito | S03 | 1,5 + 1,5 |
| **04** | El árbol de trámites | **M5** Persistencia con Spring Data JPA e Hibernate 7 | S04 | 3,0 |
| **05** | Once segundos | **M5** Persistencia (cierre) · **M6** Testing II: Testcontainers 2 · **M7** Transacciones (teoría) | S05 | 1,0 + 1,5 + 0,5 |
| **06** | Dos folios, un número | **M7** Transacciones y Optimización de Consultas · **M8** Migraciones: Flyway y Liquibase | S06 | 1,5 + 1,5 |
| **07** | El portero | **M9** Spring Security 7 y Autenticación con JWT | S07 | 3,0 |
| **08** | Diplomacia con Tesorería | **M9** Security (endurecimiento) · **M10** Comunicación entre Servicios: HTTP Declarativo y gRPC | S08 | 1,0 + 2,0 |
| **09** | Caja negra | **M10** Comunicación (cierre) · **M11** Logging Estructurado, AOP y Manejo de Archivos | S09 | 0,5 + 2,5 |
| **10** | El tablero que mentía | **M14** Observabilidad sobre OpenTelemetry, Métricas y Caché | ⚠️ ver §2 | 2,0 (M14 completo) |
| **11** | Latidos | **M12** Asincronía con Hilos Virtuales, Scheduling y Eventos | ⚠️ ver §2 | 2,0 (M12 completo) |
| **12** | Amortiguadores | **M13** Mensajería y Resiliencia | ⚠️ ver §2 | 2,5 (M13 completo) |
| **13** | Cápsula y egreso | **M15** Contenedores, Arranque Acelerado y Proyecto Final | ⚠️ ver §2 | 2,0 (M15 completo) |
| **14** | La DGT se parte en pedazos | **NINGUNO** — congelado | *Ninguna* | — |

**Cobertura:** los 15 módulos del temario tienen laboratorio. Los 35 temas SII (I–XXXV)
quedan cubiertos por esos 15 módulos según la tabla del temario.

---

## 2 · ⚠️ Las dos discrepancias estructurales que esta tabla NO puede resolver

Se declaran aquí porque afectan a la entrega y su resolución es del PO, no del material.

### 2.1 · Los labs 10 a 13 no encajan en las sesiones S10–S12

La matriz del temario reparte los cuatro últimos módulos en **tres** sesiones:

| Sesión | Módulos (horas) |
|---|---|
| S10 | M11 (0,5) + **M12** (2,0) + **M13** (0,5) |
| S11 | **M13** (2,0) + **M14** (1,0) |
| S12 | **M14** (1,0) + **M15** (2,0) |

El material los reparte en **cuatro** laboratorios de 3 h, uno por módulo, y **en otro
orden**:

| Material | Módulo | Orden del contrato |
|---|---|---|
| Lab 10 | M14 Observabilidad | va **tercero** (S11–S12) |
| Lab 11 | M12 Asincronía | va **primero** (S10) |
| Lab 12 | M13 Mensajería | va **segundo** (S10–S11) |
| Lab 13 | M15 Proyecto final | va **cuarto** (S12) |

Es decir: el contrato enseña asincronía → mensajería → observabilidad → empaquetado; el
material enseña observabilidad → asincronía → mensajería → empaquetado.

**Consecuencias:** cuatro sesiones donde el contrato compromete tres, y las horas por
módulo no cuadran (el material dedica 3 h a módulos que el contrato dota con 2,0 o 2,5).
Por eso la columna «Sesión de la matriz» va marcada para estos cuatro.

El motivo del reordenamiento está registrado en `docs/decisiones.md` (SPEC-016 §0.2): el
lab de observabilidad se insertó como Lab 10 por criterio pedagógico —del log estructurado
a las métricas hay un paso recto—, y la renumeración del temario se dejó pendiente para
«la actualización contractual». Esa actualización sigue pendiente.

### 2.2 · El temario compromete 12 sesiones; el material tiene 14

`Duración: 36 horas · Sesiones: 12 sesiones de 3 horas`. El material llega a la **sesión
14**. El Lab 13 (examen) es la sesión 13 y el Lab 14 la 14: ambas caen fuera de las doce
contratadas, aunque el contenido del Lab 13 (M15) sí está contratado.

---

## 3 · Brechas de contenido conocidas

| Tema comprometido | Estado |
|---|---|
| **gRPC** (M10, temas XVI–XVII) | Cubierto a nivel **conceptual** en `lab-08/TEORIA.md` §10 y como demo del relator en su `INSTRUCTOR.md`. Sin TODO — ver SPEC-FIX-02 §4. |
| **Liquibase** (M8, temas XXXI–XXXII) | El temario dice «Flyway **y** Liquibase». El material usa Flyway y compara Liquibase conceptualmente en `lab-06/TEORIA.md` §10. |

---

*Mantener este documento al día es parte de cerrar cualquier SPEC que mueva un lab de
módulo. Si esta tabla y el temario discrepan, el error está aquí.*
