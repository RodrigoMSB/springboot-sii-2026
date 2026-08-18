# Informe · SPEC-FIX-08 · El otro candado: el del directorio de datos

**Ejecuta:** mocito · **Fecha:** 18 de agosto de 2026
**Rama:** `fix/candado-del-directorio-de-datos` · **Commit:** `ab08b1e`

---

## 1 · El hueco que dejó la SPEC-FIX-07

La guarda del puerto anticipaba este caso **en su tercer bloque de texto**, pero no lo comprobaba.
Peor: el comando que ella misma sugiere es lo que lo provoca. Matar el motor libera el puerto y
deja el candado puesto, porque no lo retiene el mismo.

| | quién lo retiene | cuándo se suelta |
|---|---|---|
| El **puerto** | el proceso de PostgreSQL | al morir el motor |
| **`.datos-pg/epg-lock`** | la **aplicación Java** | al terminar la JVM |

Medido aquí, con la aplicación viva y el PostgreSQL ya muerto:

```
postgres: 0 · JVM viva: 1
lsof -t .datos-pg/epg-lock: [8044]     <- el candado lo retiene la JVM
```

## 2 · El mensaje, tal como sale

Reproducido el caso del PO: `lab-06-rendimiento/practica` arrancado, el PostgreSQL muerto a mano
—el `taskkill` de Windows— y la aplicación dejada en pie. Después, el mismo proyecto arrancado en
otra terminal:

```
 ESTE MISMO PROYECTO YA ESTA CORRIENDO
-----------------------------------------------------------------------------
 El archivo .datos-pg/epg-lock esta tomado por otra aplicacion viva, asi
 que este arranque no puede usar la base.

 Ese candado lo retiene el PROGRAMA, no PostgreSQL: sigue puesto aunque su
 motor ya no este. Es lo que pasa si mataste el PostgreSQL a mano pero la
 aplicacion que lo levanto sigue en pie.

 NO es un error de tu codigo.

 Ve a la terminal donde lo tienes arrancado y cierralo con Ctrl+C.

 Si no das con esa terminal:

     lsof -t .datos-pg/epg-lock | xargs kill -9
=============================================================================

  arranques: 0 · traza cruda de epg-lock: 0 · BeanCreationException: 0
```

## 3 · Las dos decisiones que importan

**La sonda suelta lo que toma.** `tryLock()` y `release()` en la línea siguiente. Si se quedara
con el candado, el que no podría arrancar sería Zonky tres líneas más abajo — la guarda se
convertiría en el problema. Que no ocurre está medido: en cada arranque de esta verificación la
sonda corrió y el motor arrancó igual (V3).

**Si no se puede comprobar, se deja pasar.** Un `IOException` —permisos, un sistema de archivos
raro— no bloquea el arranque. Bloquear por una duda convertiría una guarda pedagógica en un lab
que no arranca sin explicación. El caso contrario, `OverlappingFileLockException`, sí es
concluyente: el candado lo tiene esta misma JVM.

## 4 · Verificación

| # | Prueba | Resultado |
|---|---|---|
| V1 | El caso del PO: Postgres muerto, app viva | mensaje citado en §2 · **0 arranques, 0 `could not lock`, 0 `BeanCreationException`** |
| V2 | El comando ofrecido, ejecutado tal cual | candado liberado y **arranca en 2,342 s** |
| V3 | Sin falsa alarma tras cierre ordenado | cierre en 2 s, puerto y candado libres, rearranque **1 arranque, 0 guardas** |
| V4 | Lab 11, ciclo completo | `base-sana` 200 · `base-sana` 200 · `base-caida` 200 (readiness **503**) · `base-sana` 200 (readiness **200**) · **0 guardas** |
| V5 | Las tres ramas de `os.name` | ver §5 |
| V6 | Los 29 proyectos compilan offline | **fallos: 0 · descargas: 0** (más la solución de referencia) |
| V7 | Las suites | lab-08 `solucion` **9/9** · solución de referencia **10/10** · 0 rojos |
| V8 | El commit no arrastra nada ajeno | 27 archivos, todos de esta corrección |

V7 importaba: `OverlappingFileLockException` se trata como «tomado», y eso significa `System.exit`
dentro de un bean, que **mata la JVM de surefire**. Con un contexto por suite no ocurre — pero
había que medirlo.

**Una prueba que no se pudo hacer como se quería:** el cierre ordenado se disparó con `SIGTERM`,
no con `Ctrl+C`. Arrancando la aplicación en segundo plano desde un shell no interactivo, el shell
le deja `SIGINT` en ignorar y la JVM no se entera. Es un artefacto del arnés, no del lab: `SIGTERM`
recorre el mismo *shutdown hook*. Queda dicho por si lo quieres comprobar a mano.

## 5 · Windows

**Verificado aquí:** la elección de rama, forzando `os.name` sobre la clase compilada.

```
os.name = Windows 11
     taskkill /F /IM java.exe
     (OJO: eso cierra TODOS los labs que tengas abiertos)

os.name = Linux / Mac OS X
     lsof -t .datos-pg/epg-lock | xargs kill -9
```

En Windows no hay equivalente de `lsof` que diga qué proceso tiene abierto un archivo, así que lo
honesto es ofrecer la maza y avisar de que es una maza. **La primera instrucción del mensaje sigue
siendo Ctrl+C en la otra terminal**, que es lo que casi siempre corresponde.

**Un detalle de presentación:** arriba el separador sale `/` porque la comprobación corre en
macOS. En Windows real, `File.getPath()` imprimirá `.datos-pg\epg-lock` — el mismo texto que viste
en la traza cruda.

**Falta verificar en la VM, y es tuyo:** que la guarda dispara ahí igual, repitiendo tus pasos
—`taskkill` al PostgreSQL, aplicación en pie, arrancar el proyecto otra vez—. Debe salir el
recuadro, no la traza.

## 6 · Y la SPEC-FIX-07 queda verificada en Windows

Tu comprobación en la VM, anotada donde corresponde (`INFORME-SPEC-FIX-07.md` §6): `netstat -ano |
findstr :PUERTO` muestra **dos filas, IPv4 e IPv6, con el mismo PID**, y `taskkill /F /PID <pid>`
funciona tal como está escrito. Las dos filas dicen `LISTENING`, así que la redacción —«el PID de
la fila que dice LISTENING»— sigue llevando al PID correcto.
