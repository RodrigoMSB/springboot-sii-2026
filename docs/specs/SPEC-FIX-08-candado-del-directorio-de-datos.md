# SPEC-FIX-08 · El otro candado: el del directorio de datos

**Emite:** PO (instrucción directa, tras verificar la SPEC-FIX-07 en la VM de Windows)
**Ejecuta:** mocito
**Fecha:** 18 de agosto de 2026
**Rama:** `fix/candado-del-directorio-de-datos` desde `main` (v1.1.2) · PR contra `main`
**Corrige:** SPEC-FIX-07

---

## 0 · Qué se arregla

La SPEC-FIX-07 puso una guarda sobre el **puerto**. Su propio mensaje anticipa, en el último
bloque, que el arranque puede seguir fallando si la aplicación sigue viva en otra terminal — pero
lo anticipa **en texto**, sin comprobarlo. Verificado en Windows por el PO: tras cerrar el
PostgreSQL con `taskkill` dejando la aplicación en pie, lo que sale es la traza cruda de
`could not lock .datos-pg\epg-lock`, sin mensaje.

Son dos candados distintos que se sueltan en momentos distintos:

| | quién lo retiene | cuándo se suelta |
|---|---|---|
| El **puerto** | el proceso de PostgreSQL | al morir el motor |
| **`.datos-pg/epg-lock`** | la **aplicación Java** | al terminar la JVM |

Entre los dos hay un hueco, y es justo el que abre el comando que sugiere la SPEC-FIX-07: matar el
motor libera el puerto y **no** el candado. La guarda del puerto deja pasar, y Zonky revienta.

## 1 · Alcance

`CandadoLibre.exigir(File directorioDatos)`, llamada **inmediatamente después** de
`PuertoLibre.exigir(...)`, en los mismos **20 proyectos**.

- Sonda con `FileChannel.tryLock()` sobre `<directorioDatos>/epg-lock`. **Suelta el candado que
  toma**: aquí sólo se pregunta si está libre; quien lo necesita es Zonky, tres líneas más abajo.
- Si el archivo no existe (primera corrida), no hay con quién chocar: se deja pasar.
- Si no se puede ni comprobar (`IOException`), **se deja pasar**: no se bloquea un arranque
  legítimo por una duda.
- Mensaje con el mismo formato: qué archivo, quién lo retiene, que **no es su código**, y
  **Ctrl+C en la otra terminal** como primera instrucción.
- Sin acentos ni `ñ`, por la consola de Windows.

## 2 · Verificación

| # | Prueba | Criterio |
|---|---|---|
| V1 | El caso del PO: matar el Postgres dejando la app viva | sale el mensaje, **0** trazas de `could not lock` |
| V2 | El comando que ofrece, ejecutado tal cual | el lab arranca después |
| V3 | Sin falsa alarma tras cierre ordenado | rearranque normal, ninguna guarda dispara |
| V4 | Lab 11: ciclo completo del motor | `base-sana`, `base-caida`, `base-sana` |
| V5 | Las tres ramas de `os.name` | un solo comando en cada una |
| V6 | Los 29 proyectos compilan offline | 0 descargas |
| V7 | Las suites siguen verdes | incluida la solución de referencia |

## 3 · Prohibiciones

- ❌ Que la sonda se quede con el candado.
- ❌ Bloquear el arranque cuando la comprobación no es concluyente.
- ❌ Ofrecer en Windows un comando que no exista ahí, o uno destructivo sin decir que lo es.
