# SPEC-001 · Bootstrap del repositorio y destilación del ADN Cypress

| Campo | Valor |
|---|---|
| ID | SPEC-001 |
| Título | Bootstrap del repo + destilación del ADN del curso Cypress |
| Autor | Arquitecto |
| Aprueba | Product Owner (Rodrigo) |
| Estado | EN EJECUCIÓN |
| Fecha | 2026-07-09 |

## 1. Objetivo

Crear el repositorio del curso Spring Boot SII 2026 con su andamiaje
documental mínimo, y producir docs/adn/adn-cypress.md: el destilado con
EVIDENCIA CITADA de las prácticas pedagógicas del curso de Cypress que este
curso hereda, y de las que rechaza.

## 2. Fuente y destino

- FUENTE (solo lectura, prohibido modificar):
  '/Users/rodrigosilva/SII/Copia de CYPRESS/cypress-sii-2026'
- DESTINO: '/Users/rodrigosilva/SII/springboot-sii-2026'
  Si ya existe un repo del curso en otra ruta, DETENTE y pregunta al PO.

## 3. Bootstrap del repositorio

1. git init + rama principal `main`.
2. Estructura inicial:
   docs/specs/        ← las SPEC (esta incluida)
   docs/adn/          ← destilados de cursos de referencia
   docs/decisiones.md ← registro: | Fecha | Decisión | Razón |
   README.md          ← 10 líneas máx: qué es, protocolo SPEC, roles
3. .gitignore desde el primer commit: target/, *.class, *.jar, .idea/,
   .DS_Store, *.log, *.pem, *.key, *.p12, *.jks, .env, node_modules/
4. Convención de commits: "SPEC-NNN: <qué>" (ej: "SPEC-001: bootstrap del
   repo"). Un commit por hito lógico, no un commit gigante.
5. Crea el repo en GitHub como PRIVADO (nombre: springboot-sii-2026) y
   configura el remoto. Si no tienes credenciales activas, deja todo
   commiteado local y repórtalo — no es bloqueante.

## 4. Entregable principal: docs/adn/adn-cypress.md

REGLA DE ORO (no negociable): NADA DE MEMORIA. Cada práctica lleva:
  (a) ruta del archivo fuente que la demuestra,
  (b) cita textual corta (máx 5 líneas),
  (c) "Traslado": cómo se aplica al curso de Spring Boot.
Si una hipótesis no tiene evidencia, NO se omite en silencio: va a la
sección "No verificado" con el comando de búsqueda y su salida.

Estructura del documento:
  1. Propósito (3 líneas; su consumidor es la futura SPEC-000).
  2. Prácticas verificadas — una subsección por práctica:
     ### P-NN · <nombre>
     **Evidencia:** <ruta> → <cita>
     **Por qué funciona:** <2-3 líneas>
     **Traslado a Spring Boot:** <lab/herramienta/mecanismo concreto>
  3. Anti-herencias — lo que NO se copia, con evidencia del defecto.
  4. No verificado — hipótesis sin evidencia, con comando + salida.

## 5. Hipótesis a verificar (del análisis del arquitecto)

Prácticas:
  H1.  eslint.config.mjs usa configs.globals (NO recommended) a propósito;
       el comentario dice "la historia no se reescribe".
  H2.  La regla "un PO no asevera" caza TRES formas: should, and, expect.
  H3.  starter/ excluido del lint con razón escrita ({{TODO}} no es JS
       válido; el código del alumno jamás falla el lint del material).
  H4.  labs/lab-03-*/bin/start-lab.sh --delay: el WARN pedagógico solo
       aparece si delay > 4000 ms y nombra la guía exacta (Guía 04).
  H5.  Rúbrica del Lab 13: el nivel Insuficiente de Correctitud incluye
       "pipeline deshonesto (|| true, gate decorativo)".
  H6.  cy.login colapsa 4 vías de autenticación de labs previos en un
       comando con parámetro via ("escalera colapsada"). Lab 08, TEORIA §4.
  H7.  labs/lab-13-*/rubrica/guia-instructor.md trae preguntas exactas para
       destapar criterio + respuestas de ejemplo calibradas por nivel.
  H8.  Misma guía: sección "Feedback que forma" (fortaleza primero, cada
       crítica convertida en acción).
  H9.  Toda TEORIA.md cierra con "Conclusiones y siembra del Módulo N+1"
       (verificar en al menos 3 labs distintos).
  H10. Guía 04 del Lab 03 = tres actos: choque → parche bruto (que FUNCIONA
       pero se cuestiona su costo) → forma correcta.
  H11. El reporte entregable pide TRANSCRIBIR el error exacto ("transcríbelo"),
       no opinar sobre él.
  H12. .github/workflows/material-ci.yml corre matriz ubuntu+windows con la
       exclusión del Lab 11 documentada en el propio YAML.
  H13. docs/decisiones.md registra la deuda de accesibilidad propia
       (contraste 4.14 en f29.html) y su fix, con la razón pedagógica.
  H14. bin/validar-lab.sh: dos modos (--dir starter | --dir solucion), el
       mismo criterio juzga ambos; contadores dinámicos; exit 0/1.

Anti-herencias:
  A1.  validar-egreso.sh verifica código con grep frágil (p. ej. comillas
       simples hardcodeadas en "Cypress.Commands.add('login'"). Traslado:
       en Java el criterio se verifica con tests compilados + ArchUnit;
       bash solo orquesta, jamás inspecciona código con regex.
  A2.  La rúbrica declara "sin tests flaky" pero ningún mecanismo lo mide.
       Traslado: el script 91 corre la suite 3 veces; si el resultado
       difiere, es flaky y el boletín lo declara.
  A3.  La "trampa registrada" (casilla "¿Consultaste la solución?") NO
       existe en ninguna plantilla — se citaba de segunda mano. Confírmalo
       (grep -rn en labs/*/plantillas/) y decláralo en "No verificado"
       con comando y salida.

## 6. Registro en decisiones.md

Al terminar, agrega:
| <fecha> | El ADN del curso de Cypress se destila en docs/adn/adn-cypress.md
con evidencia citada; toda hipótesis sin evidencia queda declarada, no
omitida. | Un curso nuevo no hereda leyendas: hereda prácticas verificadas. |

## 7. Criterios de aceptación

- [ ] La SPEC-001 está versionada en docs/specs/ ANTES del entregable.
- [ ] adn-cypress.md existe; cada práctica tiene ruta + cita + traslado.
- [ ] Ninguna hipótesis desapareció: confirmada, refutada o "no verificado".
- [ ] decisiones.md tiene su fila.
- [ ] Commits con prefijo SPEC-001; push hecho (o bloqueo reportado).

## 8. Reporte final al arquitecto y al PO

- Tabla: hipótesis confirmadas / refutadas / no verificadas (números).
- Cualquier hallazgo NO listado en §5 que merezca entrar al ADN: repórtalo
  aparte como "hallazgo del ejecutor" — no lo agregues al documento sin
  aprobación (trivial-safe: lo trivial se aplica y declara; lo que requiere
  decisión, se informa y no se toca).
- git log --oneline del trabajo.
