# SPEC-FIX-02 · Corrección de alcance del material

| Campo | Valor |
|---|---|
| ID | SPEC-FIX-02 |
| Naturaleza | **Corrección de material ejecutado** — derivada de SPEC-AUDIT-01 |
| Título | Sacar Git del Lab 01, quitar Python de las guías, cuadrar la numeración y cerrar las brechas contractuales |
| Autor | Arquitecto |
| Ordena | PO (Rodrigo) — **«arregla todo, no quiero más errores»** |
| Rama / Tag | `fix/alcance-del-material` → merge a `main` → tag `material-v0.2.0` |
| Estado | LISTA PARA EJECUCIÓN |

> **Instrucción de ejecución (mocito):** guardar este archivo en
> `docs/specs/SPEC-FIX-02-alcance-del-material.md` y commitearlo en la rama antes de
> ejecutar. Trabaja en el orden de las secciones: §1 y §2 son correcciones de material,
> §3 es coherencia interna, §4 es una brecha de contenido y §5 queda **congelada**
> esperando decisión del PO.

---

## §0 · De dónde sale esta SPEC

La auditoría SPEC-AUDIT-01 encontró cuatro cosas. El PO instruyó corregirlas todas.
Se ejecutan en este orden porque §1 y §2 son bugs claros, §3 es deuda declarada, §4 es
una promesa contractual incumplida, y §5 es una decisión que no me corresponde.

**Nota de método, y aplica a toda esta SPEC:** no busques justificar lo que ya está
escrito. Si al ejecutar encuentras que una corrección no procede, **repórtalo con
evidencia** — pero el criterio del PO es explícito: **lo que no es Spring Boot, no se
enseña en un curso de Spring Boot**, aunque exista una línea del temario que lo permita.

## §1 · Sacar Git del Lab 01 (hallazgo H-1)

**El problema:** el crimen del Lab 01 exige que el alumno ejecute `git log` y `git show`
para encontrar una contraseña escondida en el historial, y la teoría §7 explica el modelo
de datos de Git (commits que conservan lo borrado, clones, forks, reescritura de historia,
`filter-repo`). **Git no es materia de este curso.** Que figure como prerrequisito de
ingreso no lo convierte en contenido enseñable: significa que el alumno ya lo sabe, no que
la clase deba usarlo para armar su escena.

**La corrección:** el crimen pasa a ser **la contraseña de producción a la vista en
`starter/src/main/resources/application.yml`**. El alumno la ve al abrir el archivo. Todo
lo demás del lab se mantiene: externalizar con variables de entorno, los tres perfiles,
las propiedades tipadas, el fallo claro en `prod`, y la lección de que **una credencial
expuesta se rota, no se borra** — que sigue siendo válida y sigue siendo Spring Boot,
porque trata de dónde viven los secretos de una aplicación.

**Archivos a corregir** (rutas y líneas del reporte de auditoría):

| Archivo | Qué hacer |
|---|---|
| `guia/01-el-codigo-del-practicante.md` (líneas 20, 31, 57) | Eliminar los comandos `git log` / `git show`. El alumno abre el `application.yml` y la ve. |
| `TEORIA.md` §7 (líneas ~191-215) | Reescribir sin Git: la sección pasa a tratar **dónde deben vivir los secretos y por qué una credencial expuesta se rota**. Fuera: commits, clones, forks, SHA, `filter-repo`. |
| `INSTRUCTOR.md` (líneas 32, 38) | Reescribir el guion del crimen: se abre el archivo, se proyecta, se deja el silencio, viene la frase de Carolina. Sin terminal de git. |
| `docs/troubleshooting.md` (línea 7, fila L1-01) | Eliminar la fila: era sobre `git log` con ruta relativa. Renumerar si corresponde. |
| `starter/.../application.yml` (líneas 7, 8) | La credencial **se queda ahí, visible**. Es el crimen. Verifica que sea de utilería declarada (host ficticio, password obvia) y que el README lo diga. |
| Historia del `starter/` | Si el `starter/` tiene commits plantados a propósito para el crimen (`dc70ed6` «ajustes de conexión»), **ya no hacen falta**. Decide si los dejas inertes o los limpias, y **declara qué hiciste**: si limpiarlos obliga a rehacer la derivación, dilo y déjalos. |

**Frase de Carolina, actualizada** (para el crimen y el README):

> *«Esa es la clave de la base de producción, y está dentro del repositorio que clonaron
> dieciocho personas. Sácala de ahí — y después dime qué más hay que hacer, porque
> sacarla del archivo no es suficiente.»*

**Criterio de verificación:** `grep -rniE '\bgit (log|show|commit|clone|filter-repo)\b'`
sobre `labs/lab-01-*/` (excluyendo `bin/`, donde `95-recuperar.sh` usa git internamente
para nombrar respaldos y **eso no se toca**) debe volver vacío en el material del alumno.

## §2 · Quitar Python de las guías del Lab 14 (hallazgo H-3)

**El problema:** las guías usan `python3 -m json.tool` para formatear JSON. Al alumno nunca
se le pidió instalar Python — ni en el Lab 00 ni en el temario.

**La corrección:** reemplazar por algo que no exija instalar nada. Opciones, en orden de
preferencia: dejar el `curl` sin tubería (el JSON crudo se lee), o usar una herramienta ya
requerida. **No introduzcas una dependencia nueva para resolver esto.**

Archivos: `guia/01-levantar-y-mirar.md` (59, 125), `guia/02-matar-al-proveedor.md`
(212, 236), `docs/troubleshooting.md` (110, 153), `INSTRUCTOR.md` (59, 144).

**Criterio:** `grep -rn python labs/lab-14-*/` vacío en material del alumno (guías,
teoría, plantillas, troubleshooting, instructor).

## §3 · Cuadrar la numeración de módulos (deuda declarada)

**El problema:** los labs 10 a 14 citan números de módulo que **no coinciden** con el
temario contratado. Ejemplo: el Lab 10 dice «Módulo 12» y en el temario ese contenido es
el M14. La deuda quedó registrada en `decisiones.md` cuando se insertó el lab de
observabilidad, y sigue abierta.

**La corrección:** el **temario contratado manda**. Recorre los 15 directorios de lab y
corrige toda cita de número de módulo para que apunte al del temario v3
(`docs/temario/TEMARIO-SPRING-BOOT-SII-v3.md`, §Estructura del Programa). Revisa
`README.md`, `TEORIA.md` e `INSTRUCTOR.md` de cada lab.

**Entrega además una tabla lab → módulo(s) del temario**, que es el documento que hoy no
existe y que hace falta para la entrega al SII.

## §4 · gRPC: la brecha de contenido (hallazgo del ejecutor)

**El problema:** el M10 del temario contratado dice literalmente *«Comunicación entre
Servicios: HTTP Declarativo **y gRPC**»*. `grep -ri grpc labs/` no devuelve nada en
material del alumno. **Está prometido y no está.**

**La corrección:** agregar gRPC al **Lab 08** (Diplomacia con Tesorería), que es el lab de
comunicación entre servicios, **a nivel conceptual y demo del relator**, no como TODO. El
temario lo describe así y ese alcance basta:

- Una sección en `TEORIA.md`: qué es gRPC, en qué se diferencia de REST (contrato binario
  vs texto, streaming, generación de stubs), **cuándo elegirlo y cuándo no**.
- Una demo guionada en `INSTRUCTOR.md` con el starter de Spring gRPC de Boot 4.1: mostrar
  un servicio respondiendo. Que sea **reproducible** — si no logras que corra en el stack
  del curso, dilo y déjalo solo como teoría, con la razón escrita.
- **Sin TODO, sin tocar el presupuesto de tecleo del Lab 08**, que ya está en ~50 min.

## §5 · El Lab 14 queda CONGELADO (hallazgo H-2)

**El problema:** el Lab 14 (microservicios) **no mapea a ningún módulo del temario
contratado**. La palabra «microservicio» no aparece ni una vez en el temario v3. El M15
contratado son 2,0 h de contenedores y proyecto final — que ya cubre el Lab 13. Y el
temario declara 36,0 h en **12 sesiones**; el Lab 14 sería la sesión 14.

**Qué hacer:** **NADA.** No lo borres, no lo modifiques (salvo la corrección de Python del
§2 y la numeración del §3), no lo justifiques. Es una decisión contractual del PO, no
técnica.

Deja constancia en `ESTADO.md` y en `decisiones.md`:

| Fecha | Decisión | Razón |
|---|---|---|
| (fecha) | El Lab 14 (microservicios) queda **congelado**, sin módulo contractual asignado, hasta que el PO resuelva su encaje con el SII. El material se conserva íntegro. | La auditoría SPEC-AUDIT-01 constató que ni «microservicios» ni una sesión 14 existen en el temario contratado. Decidir su destino es del PO, no del arquitecto ni del ejecutor. |

## §6 · Criterios de aceptación

- [ ] SPEC-FIX-02 commiteada antes de sus cambios, en rama `fix/alcance-del-material`.
- [ ] **§1:** `grep` de comandos git vacío en el material del alumno del Lab 01
      (`bin/` excluido). El crimen funciona sin Git: verifícalo corriendo el
      `91-e2e.sh` y citando el resultado.
- [ ] **§1:** la teoría §7 reescrita **sin** modelo de datos de Git, conservando la
      lección de la rotación de credenciales.
- [ ] **§2:** `grep -rn python labs/lab-14-*/` vacío en material del alumno; los comandos
      corregidos funcionan (cítalos ejecutados).
- [ ] **§3:** toda cita de número de módulo coincide con el temario v3; tabla lab→módulo
      entregada.
- [ ] **§4:** sección de gRPC en el Lab 08 (teoría + demo, o solo teoría con la razón
      escrita si la demo no corre).
- [ ] **§5:** Lab 14 intacto, con su fila en `decisiones.md` y `ESTADO.md`.
- [ ] Los 15 labs siguen pasando su `90-validar.sh`; CI verde, run citado.
- [ ] `ESTADO.md` al día. Commits `SPEC-FIX-02:`; PR a main; tag `material-v0.2.0`.

## §7 · Reporte

Evidencia de cada corrección (los `grep` vacíos, el `91-e2e` verde, los comandos nuevos
ejecutados), la tabla lab→módulo, el estado del gRPC, URL del run, `git log --oneline`,
discrepancias y hallazgos — sin tocarlos.

**Y si al ejecutar encuentras que alguna corrección de esta SPEC está mal planteada,
dilo.** Esta SPEC nace de una auditoría que destapó errores del arquitecto; que tenga
errores propios es perfectamente posible.
