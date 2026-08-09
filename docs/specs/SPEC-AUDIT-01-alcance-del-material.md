# SPEC-AUDIT-01 · Auditoría de alcance del material

| Campo | Valor |
|---|---|
| ID | SPEC-AUDIT-01 |
| Naturaleza | **Auditoría** — solo lectura, no produce material |
| Título | Verificar que ningún laboratorio enseñe contenido fuera del alcance contratado |
| Autor | Arquitecto |
| Ordena | PO (Rodrigo) |
| Estado | LISTA PARA EJECUCIÓN |

> **Instrucción de ejecución (mocito):** guardar este archivo en
> `docs/specs/SPEC-AUDIT-01-alcance-del-material.md` y commitearlo antes de ejecutar.
> **Esta SPEC es de solo lectura: no modifiques, no corrijas, no propongas cambios en el
> material.** Solo reportas hallazgos con evidencia citada. Las decisiones las toma el PO.

---

## §1 · Por qué existe esta auditoría

Se detectó que el **Lab 01** incorpora **Git** como contenido que el alumno debe ejecutar
(`git log`, `git show` en el guion del crimen y en la teoría §7). **Git no aparece en
ninguno de los 15 módulos del temario contratado con el SII.** Es contenido fuera de
alcance introducido por el arquitecto en la SPEC-007, sin que el PO lo aprobara
explícitamente.

El PO necesita saber **si es un caso aislado o un patrón**. De eso depende qué se corrige
y cuánto.

## §2 · Los 15 módulos oficiales (la vara)

Todo lo que el alumno deba aprender o ejecutar tiene que caber en uno de estos:

| # | Módulo oficial |
|---|---|
| M1 | Fundamentos de Spring Boot y Configuración |
| M2 | Controladores REST y Documentación de APIs |
| M3 | Arquitectura en Capas, Validaciones y Manejo de Excepciones |
| M4 | Persistencia con Spring Data JPA |
| M5 | Transacciones y Optimización de Consultas |
| M6 | Migraciones de Base de Datos (Flyway / Liquibase) |
| M7 | Spring Security y Autenticación con JWT |
| M8 | Consumo de APIs Externas y Comunicación entre Servicios |
| M9 | Logging, AOP y Manejo de Archivos |
| M10 | Procesamiento Asíncrono, Tareas Programadas y Eventos |
| M11 | Mensajería y Resiliencia |
| M12 | Monitoreo, Observabilidad y Caché |
| M13 | Testing: Pruebas Unitarias y de Integración |
| M14 | Empaquetado, Despliegue y Proyecto Final |
| M15 | Microservicios (Lab 14) |

## §3 · Criterio de clasificación (aplícalo literal)

**Va a la columna FUERA DE ALCANCE** si el alumno **tiene que entender o ejecutar** algo
que no es materia de los módulos del §2. Ejemplos del tipo de cosa que buscamos:

- Herramientas ajenas al stack: **Git**, bash avanzado, SQL avanzado, jq, curl más allá
  de invocar un endpoint.
- Conceptos de otras disciplinas: redes, criptografía, sistemas operativos, contenedores
  más allá de levantar una dependencia con un comando dado.
- Cualquier tema que no sea Spring Boot, Java, o la persistencia/testing del temario.

**NO va a esa columna** la infraestructura que el alumno solo **usa** sin tener que
aprenderla: los scripts de `bin/`, que Docker levante una base con un comando entregado,
el Maven Wrapper, el IDE.

**La prueba de la línea divisoria:** ¿el alumno necesita *saber* de esa herramienta para
completar el laboratorio, o solo *ejecutar un comando que le dieron*? Lo primero es fuera
de alcance; lo segundo no.

## §4 · Qué revisar en cada lab (00 a 14)

Para cada laboratorio, leer:

- `README.md` — la narrativa y los objetivos declarados
- `TEORIA.md` — todas las secciones
- `guia/*.md` — las guías paso a paso
- `INSTRUCTOR.md` — el guion del crimen y el minutado
- Los `{{TODO}}` del `starter/` — qué tiene que escribir el alumno
- `plantillas/*.md` — qué se le pide reportar

## §5 · Formato del reporte

Una tabla, un lab por fila:

| Lab | Qué enseña realmente | Módulos oficiales que cubre | Contenido FUERA de alcance |
|---|---|---|---|

Y para **cada** hallazgo de la última columna, un bloque aparte con:

1. **Ruta y línea exacta** del archivo donde aparece.
2. **Cita textual** (máx. 3 líneas).
3. **Qué tiene que hacer el alumno** con eso (¿leerlo? ¿ejecutarlo? ¿entenderlo para
   avanzar?).
4. **Si es bloqueante**: ¿puede completar el lab sin saber de eso, sí o no?

## §6 · Restricciones

- **No corrijas nada.** Ni una línea, ni un typo.
- **No propongas soluciones.** No es tu decisión qué se saca ni cómo.
- **No juzgues si conviene.** Reporta lo que hay, con evidencia.
- Si un caso es dudoso, ponlo en una sección **«Zona gris»** aparte, con tu razonamiento
  de por qué no lo clasificaste — pero sin recomendar.

## §7 · Criterios de aceptación

- [ ] SPEC-AUDIT-01 commiteada antes de ejecutar.
- [ ] Los 14 labs revisados (00 a 14), ninguno omitido.
- [ ] Tabla completa con las cuatro columnas.
- [ ] Cada hallazgo con ruta, línea, cita y respuesta a las cuatro preguntas del §5.
- [ ] Sección «Zona gris» si aplica.
- [ ] **Ningún archivo del material modificado** — confírmalo con `git status` limpio.

## §8 · Reporte

La tabla, los bloques de hallazgos, la zona gris, y el `git status` que prueba que no
tocaste nada. Nada más: sin recomendaciones, sin opiniones sobre qué hacer.
