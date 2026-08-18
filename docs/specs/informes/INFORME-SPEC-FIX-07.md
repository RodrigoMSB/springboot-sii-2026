# Informe · SPEC-FIX-07 · El puerto ocupado, dicho con nombre y apellido

**Ejecuta:** mocito · **Fecha:** 18 de agosto de 2026
**Rama:** `fix/puerto-ocupado-con-mensaje` · **Commit:** `84de893`

---

## 1 · La premisa, medida antes de tocar nada

El encargo decía que el alumno ve `Failed to start bean 'webServerStartStop'`. Reproducido, hay
**tres** situaciones distintas, no una:

| # | Qué sobrevive de la corrida anterior | Qué leía el alumno |
|---|---|---|
| A | Nada; dos carpetas con el **mismo puerto** por error | **Nada. No fallaba** — se conectaban a la misma base |
| B | Sólo el PostgreSQL | `could not lock .datos-pg/epg-lock`, bajo cinco `BeanCreationException` |
| C | El PostgreSQL **y** la JVM | `Failed to start bean 'webServerStartStop'` (habla del puerto **HTTP**) |

El caso C es el del encargo. El B es más frecuente y peor: el mensaje útil va enterrado. El A no
estaba previsto y es el más grave de los tres, porque **no falla**: ver §4.

Y una corrección a la premisa: **pasar de `practica/` a `solucion/` no choca**. Desde la SPEC-032
cada carpeta tiene su propio puerto. Lo que choca es **rearrancar el mismo proyecto**, o tenerlo
abierto en dos terminales. El mensaje se redactó con esa frase equivocada y **se corrigió antes de
commitear**: hoy dice «una corrida anterior de ESTE mismo proyecto».

## 2 · Qué se hizo

`PuertoLibre.exigir(puerto)` antes de cada `EmbeddedPostgres.builder()`, en **20 proyectos**: labs
04, 05, 06, 07, 09 y 11 × tres carpetas, más `proyecto-final/base` y la solución de referencia.

Tres decisiones que conviene saber:

- **`System.exit(1)`, no una excepción.** Una excepción la envuelve Spring y el mensaje termina
  bajo la traza, que es exactamente el problema que se venía a resolver.
- **Sin acentos ni `ñ`.** La consola de Windows no es UTF-8 por defecto y los rompería.
- **En el lab 11, la guarda va DESPUÉS de la comprobación de idempotencia.** Ahí el motor se
  levanta bajo demanda; si la guarda fuera lo primero, el segundo `POST /simulador/base-sana`
  encontraría el puerto ocupado **por el motor de la propia aplicación** y la mataría. Verificado
  en V4.

## 3 · El mensaje, tal como sale

Reproducido: `lab-06-rendimiento/practica` arrancado, la JVM matada dejando el PostgreSQL
huérfano en el 55436, y el proyecto vuelto a arrancar.

```
 EL PUERTO 55436 YA ESTA OCUPADO
-----------------------------------------------------------------------------
 Ahi es donde este proyecto levanta su PostgreSQL, asi que no puede arrancar.

 Lo mas probable: quedo un PostgreSQL vivo de una corrida anterior de ESTE
 mismo proyecto. Al cerrar con Ctrl+C, o al cerrar la terminal de golpe, el
 motor a veces sobrevive al programa que lo levanto.

 NO es un error de tu codigo.

 Cierra el que quedo y vuelve a arrancar:

     lsof -ti:55436 | xargs kill -9

 Si sigue igual, es que ese mismo proyecto TODAVIA corre en otra terminal:
 vuelve a ella y cierralo con Ctrl+C. La aplicacion retiene el archivo
 .datos-pg/epg-lock aunque su PostgreSQL ya no este.

 Y si tampoco es eso, hay otro programa usando el 55436 en tu maquina.
=============================================================================
  [arranques: 0 · BeanCreationException: 0]
```

Y el comando que ofrece, ejecutado tal cual:

```
$ lsof -ti:55436 | xargs kill -9
$ ./mvnw spring-boot:run
  ... Started Lab06Application in 2.275 seconds (process running for 2.45)
  [epg-lock: 0]
```

## 4 · Lo que el chequeo destapó · lab-09 compartía la base en silencio

`lab-09-seguridad/practica` declaraba `PUERTO_BASE = 55441` — **el mismo de su `solucion/`**. El
`README.md` del lab siempre dijo 55440. Con las dos carpetas levantadas no aparecía ningún error:
la segunda en arrancar **se conectaba a la base de la primera**. En un lab de seguridad, eso
significa que los usuarios de una carpeta salían en la otra.

No lo habría encontrado la guarda —habría matado la segunda aplicación sin explicar por qué—; lo
encontró medir el caso A antes de escribir código. Corregido a 55440, y comprobado con las dos
levantadas a la vez:

```
practica (8095/55440): 1 arranque · postgres propio: 97114
solucion (8096/55441): 1 arranque · postgres propio: 97184
motores escuchando: 55440=1 55441=1   <- dos bases separadas
```

## 5 · Verificación

| # | Prueba | Resultado |
|---|---|---|
| V1 | Huérfano real y rearranque | mensaje citado en §3 · **0 arranques, 0 `BeanCreationException`** |
| V2 | El comando ofrecido, ejecutado tal cual | **arranca en 2,275 s**, `epg-lock: 0` |
| V3 | Sin falsas alarmas | `solucion/` (55437) **arranca** con el huérfano de `practica/` (55436) vivo |
| V4 | Lab 11, dos `POST /simulador/base-sana` seguidos | **200 y 200**, liveness 200, guarda no dispara |
| V4b | Lab 11 con el 55443 tomado por otro | la guarda **sí** dispara y nombra el 55443 |
| V5 | `proyecto-final/base` con el 55444 tomado | mensaje correcto · **0 arranques** |
| V6 | Las tres ramas de `os.name` | ver §6 — un solo comando en cada una |
| V7 | Los 29 proyectos compilan offline | **fallos: 0 · descargas: 0** |
| V8 | Las suites de tests | lab-08 `solucion` **9/9**; solución de referencia **10/10**; 0 rojos |
| V9 | El commit no arrastra nada ajeno | 27 archivos, todos de esta corrección |

V8 importaba más de lo que parece: `System.exit(1)` dentro de un bean **mata la JVM de surefire**.
Con los puertos bien repartidos no ocurre, pero había que medirlo, no suponerlo.

## 6 · Windows · lo verificado y lo que falta verificar

**Verificado aquí:** que se elige la rama correcta. Forzando `os.name` sobre la clase ya
compilada, cada sistema ve **un solo** comando, el suyo:

```
os.name = Windows 11
     netstat -ano | findstr :55436
     taskkill /F /PID <el PID de la fila que dice LISTENING>

os.name = Linux      ->  lsof -ti:55436 | xargs kill -9
os.name = Mac OS X   ->  lsof -ti:55436 | xargs kill -9
```

**Falta verificar en la VM, y es tuyo:** que esos dos comandos hacen el trabajo en Windows real.
Aquí no hay Windows y no me lo voy a inventar. Lo concreto a comprobar:

1. Levantar cualquier lab con base, cerrar la ventana de golpe, y volver a arrancar: debe salir el
   recuadro con la rama de Windows.
2. `netstat -ano | findstr :55436` debe listar la fila `LISTENING`.
3. `taskkill /F /PID <ese PID>` debe cerrarlo, y el lab arrancar después.

**Un detalle que ya cambié por precaución:** el texto decía «el PID de la última columna».
`findstr :55436` también casa filas de conexión donde el 55436 es el puerto **remoto**, y ahí el
PID es el de otro programa. Ahora dice **«el PID de la fila que dice LISTENING»**. Si en la VM ves
más de una fila, eso es justo lo que estaba mal.

## 7 · Lo que no se hizo, y por qué

- **El caso C** (sobrevive también la JVM) sigue mostrando `webServerStartStop`, porque ahí el
  primer puerto que choca es el **HTTP**, antes de que exista ningún bean de Postgres. Estaba
  fuera del encargo, que hablaba del puerto de la base. Se puede resolver igual —la misma sonda
  sobre `server.port` en el `main()`— si lo quieres en una SPEC aparte.
- **No se cambia de puerto automáticamente.** El puerto fijo está documentado en cada `README.md`
  y en `PASOS.md`; moverlo solo dejaría esa documentación mintiendo.
- **En el lab 11 la aplicación muere a mitad de petición** si el puerto está tomado: `curl`
  devuelve `000`, no un código HTTP. Es coherente con los demás labs y el mensaje sale igual, pero
  queda dicho por si prefieres que ahí devuelva 503 y siga viva.
