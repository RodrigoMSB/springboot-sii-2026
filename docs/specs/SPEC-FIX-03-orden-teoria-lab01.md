# SPEC-FIX-03 · Reordenar la teoría del Lab 01

| Campo | Valor |
|---|---|
| ID | SPEC-FIX-03 |
| Naturaleza | **Corrección de material ejecutado** — coherencia pedagógica |
| Título | Que la teoría del Lab 01 responda al crimen antes de irse a lo fundacional |
| Autor | Arquitecto |
| Ordena | PO (Rodrigo) |
| Rama / Tag | `fix/orden-teoria-lab01` → merge a `main` → tag `material-v0.2.1` |
| Estado | LISTA PARA EJECUCIÓN |

> **Instrucción de ejecución (mocito):** guardar en
> `docs/specs/SPEC-FIX-03-orden-teoria-lab01.md` y commitear en la rama antes de ejecutar.
>
> **Esta corrección NO toca código.** No se modifica el `starter/`, ni la `solucion/`, ni
> los TODOs, ni los tests, ni el crimen, ni los scripts. **Solo se reordena y se enlaza
> texto** en la teoría y en el material del instructor. Si te encuentras editando un
> `.java`, un `.yml` de la aplicación o un `.sh`, **detente: te saliste del alcance.**

---

## §1 · El problema

La teoría del Lab 01 (`TEORIA.md`) tiene este orden:

1. El contenedor: quién construye tus objetos
2. Autoconfiguración: el mayordomo que adivina
3. Configuración externalizada
4. Perfiles
5. `@Value` vs `@ConfigurationProperties`
6. Fallar rápido, y fallar claro
7. **Un secreto filtrado no se borra: se rota** ← el tema del crimen
8. M2 · El contrato REST: DTO y `ProblemDetail`
9. Tabla DO / DON'T · 10. Glosario · 11. Conclusiones y siembra

**El crimen de la sesión es una credencial de producción dentro del repositorio.** La
teoría abre con el contenedor IoC —sin una sola línea que conecte con lo que la sala
acaba de ver arder— y el tema del crimen no llega hasta la **sección 7 de 11**.

Consecuencia: el alumno vive un incendio y luego atraviesa seis secciones que, desde su
punto de vista, «salen de la nada». El material es bueno; **el orden desperdicia la
motivación que el crimen acababa de generar.**

**Verificado** contra `labs/lab-01-del-otro-lado-del-boton/TEORIA.md`: la sección 1
comienza en «En Java normal, tú escribes `new ContribuyenteService(...)`», sin puente
desde el crimen. **Los demás labs no tienen este defecto** (sus secciones 1 nombran el
crimen directamente); esta corrección es solo del Lab 01.

## §2 · El orden nuevo

| Nuevo # | Sección | Viene de |
|---|---|---|
| **1** | Dónde viven los secretos, y por qué uno expuesto se rota | era la 7 |
| **2** | Configuración externalizada | era la 3 |
| **3** | Perfiles | era la 4 |
| **4** | `@Value` vs `@ConfigurationProperties` | era la 5 |
| **5** | Fallar rápido, y fallar claro | era la 6 |
| **6** | El contenedor: quién construye tus objetos | era la 1 |
| **7** | Autoconfiguración: el mayordomo que adivina | era la 2 |
| **8** | M2 · El contrato REST: DTO y `ProblemDetail` | sin cambio |
| 9–11 | DO/DON'T · Glosario · Conclusiones y siembra | sin cambio |

**La lógica:** las secciones 1 a 5 son **una sola línea argumental que nace del crimen** —
está mal → dónde va entonces → cómo cambia por ambiente → cómo se lee bien → qué pasa
cuando falta. El bloque fundacional (6 y 7) entra **después**, por una puerta nueva y
declarada.

## §3 · Los dos textos nuevos (lo único que se escribe)

### 3.1 · Puente de entrada, antes de la sección 1

Un párrafo corto que ate la teoría al crimen. Redáctalo con tus palabras respetando el
tono del material; la idea obligatoria es esta:

> *La contraseña que acabas de ver no está ahí por torpeza: está ahí porque nadie decidió
> dónde debía vivir. Antes de entender cómo funciona Spring por dentro, vamos a resolver
> el problema que tienes encima de la mesa: dónde va cada valor de una aplicación, y qué
> se hace cuando uno se expuso.*

### 3.2 · Puerta de transición, antes de la sección 6

La frase que le dice al alumno que cambia el tema, y por qué. Idea obligatoria:

> *Ya sabes dónde va cada cosa y qué hacer cuando un secreto se expone. Queda una pregunta
> que probablemente te hiciste al arrancar la aplicación y que aún no hemos contestado:
> **¿por qué esto arranca solo?** Tú no configuraste un servidor web, y sin embargo hay
> uno escuchando. Para responderlo hay que mirar cómo funciona Spring por dentro.*

**Nada más se escribe.** El contenido de las secciones se mueve **tal cual está**: no lo
reescribas, no lo acortes, no lo mejores.

## §4 · Qué hay que tocar

| Archivo | Qué hacer |
|---|---|
| `labs/lab-01-*/TEORIA.md` | Reordenar los bloques según §2 · renumerar títulos y el índice · insertar los dos textos de §3 · **corregir los enlaces internos del índice** (los anchors cambian de posición) |
| `labs/lab-01-*/INSTRUCTOR.md` | Actualizar el orden de la teoría en el minutado y en el guion, si los enumera |
| `labs/lab-01-*/README.md` | Si describe el orden de la teoría, alinearlo |
| `labs/lab-01-*/guia/*.md` | **Solo si** alguna guía referencia una sección por número (ej. «ver teoría §5»). Verifícalo con grep y corrige las referencias, no el contenido |

**No se tocan:** `starter/`, `solucion/`, `bin/`, tests, `plantillas/`, `docs/troubleshooting.md`
(salvo que referencie secciones por número), ni ningún archivo de los otros 13 labs.

## §5 · Verificación

Citada en el reporte:

1. **Nada de código cambió:** `git diff --stat` no debe mostrar ningún `.java`, ningún
   `.yml` bajo `src/`, ningún `.sh`. Cítalo.
2. **El contenido se movió íntegro:** compara el conjunto de párrafos antes y después
   (por ejemplo, ordenando las líneas no vacías de ambas versiones y diffeando). Aparte
   de los dos textos nuevos de §3 y los títulos renumerados, **no debe haber diferencias**.
   Cita el resultado.
3. **El índice cuadra:** todos los enlaces internos apuntan a una sección existente y en
   la posición correcta. Verifícalo, no lo supongas.
4. **La sección 1 ahora es la de los secretos**, y arranca con el puente. Cítala.
5. **Ninguna guía quedó con una referencia rota** a un número de sección.
6. El `90-validar.sh` del Lab 01 sigue en verde (no debería verse afectado, pero
   confírmalo) y el CI queda verde. Run citado.
7. `ESTADO.md` y `decisiones.md` con su fila:

| Fecha | Decisión | Razón |
|---|---|---|
| (fecha) | La teoría del Lab 01 se reordena: las secciones que responden al crimen van primero; el contenedor y la autoconfiguración entran después, con una transición explícita. | El crimen genera una pregunta y la teoría debe contestarla de inmediato. Con el orden anterior el tema del crimen aparecía en la sección 7 de 11, y las seis primeras «salían de la nada» para el alumno. Verificado que ningún otro lab tiene este defecto. |

## §6 · Criterios de aceptación

- [ ] SPEC-FIX-03 commiteada antes de sus cambios, en rama propia.
- [ ] Orden nuevo aplicado según §2; índice y anchors correctos.
- [ ] Los dos textos de §3 presentes.
- [ ] **Cero archivos de código modificados** (§5.1 citado).
- [ ] **Contenido íntegro** salvo los dos textos nuevos (§5.2 citado).
- [ ] Sin referencias rotas a secciones en guías, README o instructor.
- [ ] `90-validar.sh` verde; CI verde, run citado.
- [ ] `ESTADO.md` y bitácora al día.
- [ ] Commits `SPEC-FIX-03:`; PR a main; tag `material-v0.2.1`.

## §7 · Reporte

Las siete verificaciones de §5 con su evidencia, el índice nuevo, `git log --oneline`,
discrepancias y hallazgos.

**Y si al ejecutar encuentras que el reordenamiento rompe una dependencia que la SPEC no
previó** —una sección que se apoya en otra anterior, una referencia cruzada dentro del
propio texto— **detente y repórtalo antes de improvisar una solución.**
