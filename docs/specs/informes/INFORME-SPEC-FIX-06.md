# INFORME-SPEC-FIX-06 · Las dos deudas de la SPEC-033, saldadas

**SPEC:** SPEC-FIX-06 · **Ejecuta:** mocito · **Fecha:** 18 de agosto de 2026
**Rama:** `fix/lab-00-y-fosil-3b` · **Tag al cierre:** `material-v1.1.1`
**Máquina:** Mac Studio del PO (Darwin 25.5.0, `arm64`)

---

## 1 · Veredicto en una línea

**LAS DOS DEUDAS ESTÁN SALDADAS Y NINGÚN LAB CAMBIÓ DE COMPORTAMIENTO.** El `practica/` del Lab 00
—el último del arco con bloque explicativo— pasa a la estructura nueva (39 → 23 líneas, código
idéntico), y «Lab 3b» desaparece del repositorio: **13 reemplazos** en cuatro labs, más **7** en las
copias locales de `instructor/`. Los cuatro proyectos tocados arrancan y dan lo que daban. De
propina, la cadena de siembra del Lab 03 vuelve a resolver.

---

## 2 · Qué se tocó

### 2.a · El Lab 00, migrado

Antes: javadoc de seis líneas + un bloque de ocho explicando `CommandLineRunner`. Después:

```java
    @Bean
    CommandLineRunner run() {
        return args -> {
            // Paso 2 · imprime un mensaje tuyo al arrancar.
            // escribe aquí
        };
    }
```

La línea imperativa se redactó contra el paso 2 de su propio `PASOS.md` («Que imprima algo tuyo»),
que es donde el instructor explica en voz alta lo que el bloque decía por escrito.

### 2.b · El fósil, y era mayor de lo anotado

El INFORME-SPEC-035 §6.e lo dio por «cuatro cadenas de texto en `lab-04-jpa`». Medido, eran **8 en
cuatro labs** — y 7 más en `instructor/`:

| Archivo | Reemplazos |
|---|---|
| `lab-04-jpa/README.md` · `PASOS.md` (títulos) | 2 |
| `lab-04-jpa/{practica,solucion}/pom.xml` (`<description>`) | 2 |
| `lab-03-errores/PASOS.md` (la siembra) | 2 |
| `lab-05-relaciones/README.md` · `PASOS.md` (referencias cruzadas) | 4 |
| `lab-04-jpa/instructor/` (pom y yml) | 3 |
| `lab-{03,05,06,07}/instructor/` (comentarios que citaban el lab) | 4 |
| **total** | **13 versionados + 7 locales** |

Que la anotación se quedara corta tiene una causa concreta: cuando la escribí busqué sólo dentro de
`lab-04-jpa`, que era donde había visto el síntoma. Las referencias cruzadas desde otros labs no las
miré.

---

## 3 · Verificación

| # | Prueba | Resultado |
|---|---|---|
| **V1** | Lab 00, los dos proyectos | ✅ |
| **V2** | Lab 04, los dos proyectos | ✅ 8 demos |
| **V3** | Sólo cambian comentarios y texto | ✅ |
| **V4** | Cero «Lab 3b» | ✅ ni en `instructor/` |
| **V5** | 29 proyectos offline | ✅ 0 descargas |

### V1 · el Lab 00

```
practica/  Started HolaMundoApplication in 0.442 seconds
           mensajes propios impresos: 0     <- el hueco está por llenar, como debe ser

solucion/  Started HolaMundoApplication in 0.422 seconds
             Hola, mundo. Esto lo escribí yo.
```

### V2 · el Lab 04

```
practica/  Successfully applied 1 migration to schema "public", now at version v1
           Started Lab04Application in 2.995 seconds
           demos ejecutadas: 0              <- los huecos están por llenar

solucion/  Started Lab04Application in 2.898 seconds
           === 1 · GUARDAR · save() ===              === 5 · DOS CONDICIONES ===
           === 2 · BUSCAR POR ID · findById() ===    === 6 · ACTUALIZAR SIN save() ===
           === 3 · LISTAR TODAS · findAll() ===      === 7 · BORRAR · deleteById() ===
           === 4 · BUSCAR POR AUTOR ===              === 8 · CONTAR · count() vs size() ===
           total: 8 demos
```

> **Un falso rojo, y por qué se cita.** El primer intento del `solucion/` del Lab 04 dio
> `APPLICATION FAILED TO START · Failed to start bean 'webServerStartStop'`. No era el cambio: era
> un PostgreSQL huérfano del `practica/` reteniendo el 55432, de un `pkill` que no llegó a matarlo.
> Se limpiaron los puertos y arrancó. Se deja escrito porque es el fallo que más va a confundir a
> quien corra dos proyectos del mismo lab seguidos.

### V3 · sólo comentarios y texto

```
  archivos de código comparados: 3 · idénticos: 3
  lab-00 practica: código idéntico quitando comentarios: True · líneas 39 -> 23
```

(La comparación de los `pom.xml` ignora `<description>`, que es texto y es justo lo que se cambió.)

### V4 · cero fósiles

```
grep -rn "Lab 3b|lab3b|Lab03b" labs/ proyecto-final/ README.md ESTADO.md docs/temario/
  ninguno, ni en instructor/
```

### V5 · compilación offline

```
  29 proyectos · fallos: 0 · descargas: 0
  gate siembra: FALLOS=0
```

---

## 4 · Un efecto lateral que suma

La cadena de siembra del Lab 03 **volvió a resolver**. Decía «La pregunta que abre el **Lab 3b**»,
que ya no es el nombre de nada; ahora dice «Lab 04», que es su sucesor real:

```
  lab-00-hola-mundo  -> Lab 01   OK        lab-05-relaciones   -> Lab 06   OK
  lab-01-web         -> Lab 02   OK        lab-06-rendimiento  -> Lab 07   OK
  lab-02-di          -> Lab 03   OK        lab-09-seguridad    -> Lab 10   OK
  lab-03-errores     -> Lab 04   OK
```

El comprobador de la SPEC-033 marcaba el Lab 03 como «(sin cadena)». No era que le faltara: era que
apuntaba a un nombre muerto.

---

## 5 · Lo que queda

**De estas dos deudas, nada.** Eran las últimas que la SPEC-033 dejó anotadas.

Del resto del proyecto sigue pendiente lo que ya estaba: la fila de aceptación del PO (ahora
incluye resolver el proyecto final con el brief delante), las ocho brechas de contenido del mapa, y
los instrumentos del 30 % y el 20 % de la evaluación.

**Y una nota de método para el próximo que anote una deuda:** cuando se detecta un fósil de
renombrado, la anotación tiene que medirlo en **todo** el repositorio, no en el archivo donde se vio
el síntoma. Ésta se quedó a un tercio.
