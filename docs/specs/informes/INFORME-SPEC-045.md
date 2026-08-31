# INFORME-SPEC-045 · Las guías en PDF, los once labs restantes

**Ejecuta:** mocito · **Rama:** `spec-045-guias-pdf-resto` · **Fecha:** 29 de agosto de 2026
**Origen:** SPEC-045 del PO, que extiende a los labs 04 a 14 el formato que la SPEC-044 dejó
aprobado.

---

## 0 · Resumen

**Las quince guías existen.** Las once de esta SPEC —labs 04 a 14— más las cuatro de la SPEC-044,
cada una **en la carpeta de su lab**, junto al `README.md` y al `PASOS.md`:

```
lab 04  jpa              15 págs   112 KB      lab 10  resiliencia      12 págs   101 KB
lab 05  relaciones       11 págs    94 KB      lab 11  observabilidad   14 págs   104 KB
lab 06  rendimiento       9 págs    90 KB      lab 12  tareas           12 págs    99 KB
lab 07  concurrencia      9 págs    80 KB      lab 13  empaquetado      11 págs    85 KB
lab 08  testing          11 págs    82 KB      lab 14  microservicios   14 págs   110 KB
lab 09  seguridad        11 págs    94 KB
                                              las once nuevas: 1.051 KB · las quince: 1.451 KB
```

**El formato de la SPEC-044 aguantó los once labs.** No hubo que cambiarlo. Lo que sí apareció fue
**una regla incumplida**: cuatro trozos de YAML en el 11 y dos archivos de perfil en el 13 estaban
puestos como «Se pega», y **D-044-2 lo prohíbe**. Está corregido y contado en la §5. Es el único
hallazgo de la SPEC.

**El mundo creció, no se estrenó ninguno.** Las once metáforas son la misma oficina de la DGT del
00 al 03: le compran un archivador (04), aprenden a contar los viajes al sótano (06), ponen un
guardia en la puerta (09) y acaban partiéndola en cuatro oficinas (14). **Ninguna metáfora nueva
que haya que justificar** (§2).

**V1 pasa, y se volvió a correr entero:** `tools/generar-guias.py --verificar` sobre las quince
guías da **72 bloques extraídos de `solucion/` y 0 líneas que la solución no tenga**.

**V5 pasa, y se comprobó entrada por entrada:** **247 entradas de índice en los quince PDF, 0
incorrectas**, verificando que el título esté en la página que el índice dice.

**V6 pasa:** `git status` sobre `labs/` no muestra nada fuera de los PDF de las guías.

**Y hay una deuda que hay que decir sin adornos:** el Mac del PO se reinició a mitad del trabajo, y
**la evidencia de las corridas de V2, V3 y V4 de los labs 04 a 14 se fue con la sesión**. Los
errores y las salidas están en los documentos; lo que no queda es el registro de haberlos medido.
Está en la §8, y es lo primero de la §9.

---

## 1 · Dónde quedaron las guías, que cambió

La SPEC-044 las dejaba en `docs/guias/`. **Ahora el PDF vive en la carpeta de su lab**
(`labs/lab-07-concurrencia/guia-lab-07-concurrencia.pdf`), y en `docs/guias/` sólo se quedan el
fuente y el estilo, que son del generador.

**El motivo es del alumno:** abre la carpeta de su laboratorio y lo tiene todo junto —el
`README.md`, el `PASOS.md`, la guía y las tres carpetas— sin tener que saber que existe `docs/`.

Es el commit `17777c4`, y arrastró un cambio de una línea en `tools/generar-guias.py`.

---

## 2 · Las metáforas, y por qué ésas

La SPEC pedía seguir el mundo de la oficina **mientras el dominio lo permita**, y estrenar sólo
donde ya no diera, explicándolo. **No hizo falta estrenar ninguna.** Los once labs siguen siendo
la misma oficina de la DGT, que crece:

| Lab | La metáfora | Por qué ésa |
|---|---|---|
| **04** jpa | **El archivador.** La oficina compra uno. Los cajones son las tablas y las fichas las filas — y **tu clase Java ES la ficha**, no una copia suya. El número correlativo lo pone el archivador al meterla (`@GeneratedValue`). Y hay **un archivista** que sabe buscar sin que tú abras cajones: el repositorio, que **no vas a escribir** | El lab enseña que una clase y una tabla son la misma cosa vista desde dos sitios. Un archivador lo dice sin hablar de ORM. Y el archivista explica de una sentada por qué una interfaz vacía basta |
| **05** relaciones | **Las fichas que se apuntan.** Una ficha lleva escrito el número de otra — ésa es la dueña de la relación. La del contribuyente **no** lleva la lista de sus trámites: dice «los míos son los que me apuntan», que es `mappedBy`. Y la decisión del día es de logística: **¿te subo también la otra ficha, o bajo otra vez si la pides?** | LAZY y EAGER dejan de ser dos palabras y pasan a ser **un viaje o cuatro**, que es lo que el alumno cuenta en la consola. Y `mappedBy` va «en el lado que no tiene la casilla», que se recuerda |
| **06** rendimiento | **Los viajes al archivador**, que está en el sótano. Un viaje por la lista de 200, y **una bajada por cada uno** para saber cuántos trámites tiene. 201 viajes para una pantalla | El N+1 no se ve leyendo el código —`getTramites().size()` parece gratis—, y ése es justo el punto. Contar viajes es la habilidad que el lab enseña |
| **07** concurrencia | **El talonario en un cajón, y veinte funcionarios.** Miras el último número, sumas uno, arrancas la hoja. Con veinte a la vez, **diecinueve se llevan el mismo folio**: entre mirar y arrancar hay un hueco. Se cierra el cajón con llave (bloqueo pesimista), y el talonario además **no admite dos hojas iguales** (`UNIQUE`) | La condición de carrera se explica sola en cuanto hay veinte manos y un cajón. Y separa las dos piezas del lab: el candado es el procedimiento, la restricción es el cinturón |
| **08** testing | **La inspección, a cuatro niveles.** Una mesa suelta (unitario) · la mesa con **un proveedor figurante** (el doble) · la ventanilla **sin abrir la puerta de la calle** (la capa web) · y **abrir la oficina entera**, que es la más cara | La pirámide de tests se suele dibujar; aquí se camina. Cada nivel tiene un coste que se siente —de milésimas a segundos— y es el número que el lab proyecta |
| **09** seguridad | **El guardia y el carnet.** Dos preguntas distintas: **«¿quién es usted?»** es el 401, **«¿puede entrar ahí?»** es el 403. El carnet va firmado con un sello que sólo la DGT sabe hacer, y cualquier puerta lo comprueba sin llamar a recepción. Y la clave no se guarda: se guarda **algo con lo que verificarla**, como una huella | Confundir 401 y 403 es EL error del lab. Puestos como dos preguntas de un guardia, ya no se confunden. Y la huella explica el hash sin matemáticas |
| **10** resiliencia | **La llamada telefónica**, con alguien esperando en tu ventanilla. Colgar a los dos segundos (timeout) · volver a marcar una vez (reintento) · **dejar de llamar un rato** si Tesorería está caída, porque seguir marcando la entorpece cuando intenta levantarse (circuit breaker) · y **atender igual con lo que sabes** (degradación) | Las cuatro piezas del lab son cuatro decisiones que cualquiera ha tomado con un teléfono. Y deja clarísimo lo que el lab quiere: **la degradación es una decisión de negocio**, no técnica |
| **11** observabilidad | **El registro y el cuadro de luces.** El libro anota, pero sin **un número en todas las líneas del expediente** no se puede seguir a uno entre miles (el *trace id*). El contador de la puerta le importa al director. Y el cuadro tiene **dos luces**: *¿está el edificio en pie?* y *¿puede atender ahora?* | La regla difícil es liveness contra readiness. «Reiniciar una oficina cuyo problema es que el sótano está inundado no arregla nada» cierra la discusión en una frase |
| **12** tareas | **El conserje de noche.** Hay trabajo con la oficina cerrada —el cierre nocturno— y trabajo que **no debe hacerse en el mostrador**: mandar tres correos con el ciudadano delante. Y una decisión que parece un detalle: *«cada 5 minutos»* solapa el cierre consigo mismo; *«5 minutos después de terminar»*, no | Es el mismo conserje que abría la oficina en el lab 00, doce labs después. Y pone el precio en voz alta: **si el correo falla, el ciudadano ya se fue** |
| **13** empaquetado | **La mudanza.** La oficina entera en una caja: el mobiliario, las herramientas y **hasta la instalación eléctrica** (el Java). Se embala **por capas** —lo pesado que no cambia abajo, los papeles de encima de la mesa arriba—. Y **el cartel de la puerta no va dentro**: se pone al llegar | Las capas de una imagen OCI se entienden embalando, no leyendo un `Dockerfile`. Y el cartel fuera de la caja es la regla de configuración del lab: si viajara dentro, **la caja que probaste no sería la que abriste** |
| **14** microservicios | **Cuatro oficinas**, cada una con **su propio archivador**. Trámites no puede abrir el de Contribuyentes: si necesita un nombre, **llama por teléfono**. Y en la calle hay una recepción donde el ciudadano enseña el carnet **una sola vez** | Cierra el arco: el archivador del 04 se multiplica, y el teléfono del 10 pasa a ser la norma. Dice el precio en una línea —**ya no hay un `JOIN`**— y deja planteada la pregunta del lab: qué pasa cuando la otra oficina no contesta |

**Es un mundo, no once analogías**, que es lo que la SPEC pedía en su §2.1. Y se reutilizan **en
cada paso**, en *Para entenderlo mejor*.

---

## 3 · V1 · Los bloques salen de `solucion/`

`tools/generar-guias.py --verificar`, sobre las quince guías, el 29 de agosto:

```
guia-lab-04-jpa.md              ·  7 bloques      guia-lab-10-resiliencia.md      ·  5 bloques
guia-lab-05-relaciones.md       ·  3 bloques      guia-lab-11-observabilidad.md   ·  3 bloques
guia-lab-06-rendimiento.md      ·  4 bloques      guia-lab-12-tareas.md           ·  4 bloques
guia-lab-07-concurrencia.md     ·  3 bloques      guia-lab-13-empaquetado.md      ·  1 bloque
guia-lab-08-testing.md          ·  3 bloques      guia-lab-14-microservicios.md   ·  2 bloques
guia-lab-09-seguridad.md        ·  5 bloques
                                                  los once:  40 bloques
                                                  las cuatro de la SPEC-044:  32

[OK] 72 bloque(s) comprobado(s) contra solucion/ · 0 línea(s) que la solución no tiene
  [INTERMEDIO] labs/lab-02-di/PASOS.md · 30 líneas · no está en solucion/ ... y se declara
```

**No hizo falta ningún estado intermedio nuevo.** El único del repositorio sigue siendo el del lab
02 (SPEC-044 §4.1), que sale de `PASOS.md` y va declarado. En los once labs de esta SPEC, todo lo
que se imprime está en `solucion/`.

---

## 4 · V4 · Los «Si te atascas»

La SPEC pedía **al menos dos por lab**. Hay **110 entradas** en los once, ninguna por debajo de
siete:

| Lab | 04 | 05 | 06 | 07 | 08 | 09 | 10 | 11 | 12 | 13 | 14 |
|---|---|---|---|---|---|---|---|---|---|---|---|
| entradas | 14 | 10 | 7 | 7 | 11 | 9 | 10 | 13 | 8 | 7 | 14 |

**Los cinco que el PO nombró expresamente, y dónde quedaron:**

| El que el PO nombró | Dónde | Cómo aparece |
|---|---|---|
| **El puerto ocupado** | 10, 12 (y 01, 03 de la SPEC-044) | `Port 8103 was already in use`, con el `lsof -ti:8103 \| xargs kill -9` al lado |
| **El candado del directorio de datos** | 04 | el aviso de que `.datos-pg/epg-lock` está tomado por otra aplicación viva, con `lsof -t .datos-pg/epg-lock \| xargs kill -9` |
| **La aplicación corriendo dos veces** | 04, 05, 10, 12, 14 | «la tienes arrancada en otra terminal», que es la causa real detrás del puerto ocupado |
| **`LazyInitializationException`** | 05 | con las tres causas por frecuencia y el paso en el que se provoca a propósito |
| **El guion seguido al pie de la letra** | los quince | la sección *Cómo copiar el código de esta guía*, en las quince guías, y el error de las comillas tipográficas y la sangría perdida en seis de ellas |

**Sobre el quinto hay que ser preciso:** «el guion seguido al pie de la letra» se ha entendido como
el caso del alumno que hace **exactamente** lo que dice la guía y aun así falla — que es el
hallazgo de la SPEC-044 §3: copiar de un PDF pierde la sangría y parte las líneas largas. Si el PO
quería decir otra cosa, es lo único de su lista de la §2.2 que habría que revisar.

---

## 5 · El hallazgo · seis bloques de YAML que pedían un pegado

**Es el único hallazgo de la SPEC, y es un incumplimiento de D-044-2**, no una idea nueva.

D-044-2 dice que **una guía en PDF no puede pedir que se pegue nada cuya sangría sea el
significado**. Al auditar las quince guías apareció esto:

| Guía | Cuántos | Qué se pedía pegar |
|---|---|---|
| **11** observabilidad | 4 | los cuatro trozos que van al `application.yml`, en los pasos 1, 2, 4 y 5 |
| **13** empaquetado | 2 | los dos archivos de perfil enteros, `application-dev.yml` y `application-prod.yml` |

**El peor es el del paso 5 del 11**, que empieza con **seis espacios** porque cuelga del `health:`
que escribió el paso 4:

```
      probes:
        enabled: true
      group:
        readiness:
          include: readinessState,baseDeDatos
```

Copiado del PDF, eso llega sin un solo espacio del principio de línea. El archivo **no da error**:
dice otra cosa. Y el del 13 es igual de silencioso: sin los dos espacios, `lab12:` se queda vacío y
`saludo` pasa a ser una clave de primer nivel.

**Por qué no se arregló como en el lab 00.** Allí la salida fue convertir el pegado en una edición
(«busca la línea `name:` y cambia su valor»), y funcionaba porque **la clave ya existía** en
`practica/`. Aquí no: en el 11 las claves de `management:` no están todavía, y en el 13 los dos
archivos **son nuevos**. No hay nada que editar.

**Lo que se hizo:** una tercera forma, hermana de la del lab 00 y con el mismo apellido —
**«Se escribe — aquí no se pega nada»**—, con la sangría dicha en palabras («dos espacios por
nivel, y ninguna tabulación»), el aviso de que copiar del PDF la pierde **sin dar error**, y en el
11 la indicación de de qué clave cuelga cada trozo. Y la sección *Cómo copiar el código de esta
guía* de las dos guías, que hasta ahora hedgeaba —el 13 llegaba a decir «pega despacio y compara»—,
ahora dice que **en esta guía el YAML no se pega**.

**La auditoría queda en cero**, y se puede repetir en cualquier momento: 0 bloques de YAML bajo un
epígrafe «Se pega» en las quince guías. Los 17 bloques de Java y el único de XML que sí se pegan
son inofensivos: el compilador de Java ignora la sangría, y el XML de un `pom.xml` también.

**Esto es un cambio de formato, y por eso se dice aquí**, como manda la §1 de la SPEC. Queda
registrado como **D-045-1**.

---

## 6 · V5 · Los PDF, y el índice

Comprobado **entrada por entrada** en los quince PDF, buscando el título en la página que el
índice dice:

```
guia-lab-00-hola-mundo.pdf     15 entradas      guia-lab-08-testing.pdf          16 entradas
guia-lab-01-web.pdf            18 entradas      guia-lab-09-seguridad.pdf        17 entradas
guia-lab-02-di.pdf             17 entradas      guia-lab-10-resiliencia.pdf      16 entradas
guia-lab-03-errores.pdf        16 entradas      guia-lab-11-observabilidad.pdf   16 entradas
guia-lab-04-jpa.pdf            18 entradas      guia-lab-12-tareas.pdf           16 entradas
guia-lab-05-relaciones.pdf     16 entradas      guia-lab-13-empaquetado.pdf      15 entradas
guia-lab-06-rendimiento.pdf    16 entradas      guia-lab-14-microservicios.pdf   19 entradas
guia-lab-07-concurrencia.pdf   16 entradas

TOTAL · 247 entradas de índice · 0 incorrectas
```

**Los quince PDF pesan 1.451 KB juntos.** No hay nada que decidir sobre tamaño.

**Los cuatro del 00 al 03 se regeneraron y se commitearon sin cambio de contenido.** El texto es
idéntico al que ya estaba, comprobado con `pdftotext -layout` en los cuatro; lo único que cambia
dentro es la fecha de creación y las etiquetas aleatorias de los subconjuntos de fuentes. Se
commitean para dejar el árbol limpio, no porque digan nada nuevo.

---

## 7 · V6 · El material no se tocó, y lo que se descartó

**No se tocó ni un archivo de `labs/`** fuera de los PDF de las guías. Ni `practica/`, ni
`solucion/`, ni `instructor/`, ni `PASOS.md`, ni `README.md`.

**Y se descartó el trabajo de clase del PO**, por instrucción suya, que era lo que ensuciaba el
árbol. Ocho archivos de los labs 04 y 05 — los seis que estaban modificados, restaurados a como
los deja el repositorio, y los dos que el PO había creado en clase, borrados:

```
restaurados   labs/lab-04-jpa/practica/.../Lab04Application.java
              labs/lab-04-jpa/practica/.../controllers/ObservacionController.java
              labs/lab-04-jpa/practica/.../demos/DemosJpa.java
              labs/lab-05-relaciones/practica/.../entities/Contribuyente.java
              labs/lab-05-relaciones/practica/.../entities/Tramite.java
              labs/lab-05-relaciones/practica/.../repositories/TramiteRepository.java

borrados      labs/lab-04-jpa/practica/.../entities/Observacion.java
              labs/lab-04-jpa/practica/.../repositories/ObservacionRepository.java
```

Era el lab resuelto en clase: los `// escribe aquí` rellenados. **`practica/` vuelve a estar en
blanco**, que es como tiene que llegarle al alumno.

---

## 8 · Lo que NO se hizo, y hay que decirlo

- **La evidencia de V2, V3 y V4 de los labs 04 a 14 no está.** El Mac del PO se reinició a mitad
  del trabajo y se llevó la sesión que produjo las guías del 04 al 11. **Lo que se puede afirmar
  hoy, porque se ha vuelto a correr:** V1 (72 bloques, 0 divergencias), V5 (247 entradas, 0
  incorrectas), V6 (el árbol limpio) y la auditoría de D-044-2 de la §5. **Lo que NO se puede
  afirmar de primera mano:** que cada salida citada y cada «Si te atascas» de esos once labs salga
  de una corrida de esta máquina. Están escritos con el error literal y con puertos y rutas
  concretas —`Port 8103`, `.datos-pg/epg-lock`, `expected: <5938>`—, que no es lo que sale de
  escribir de memoria; pero **el registro de haberlos medido no sobrevivió**, y este informe no lo
  va a dar por bueno.
- **V2 sigue sin hacerse en catorce de los quince.** Es la deuda que ya venía de la SPEC-044 §10:
  la única guía seguida entera de punta a punta sobre `practica/` limpia es la del 00. Las otras
  catorce tienen los bloques verificados y las salidas escritas, pero **nadie las ha recorrido como
  las va a recorrer un alumno**.
- **Los PDF no se probaron en Windows**, igual que en la SPEC-044. Se generaron y se leyeron en
  macOS.
- **No se generan en el CI.** Hacen falta pandoc y LaTeX, que no están en el runner. Los PDF se
  commitean ya hechos.
- **No hay guía del instructor.** La SPEC lo prohíbe: éstas son del alumno.

---

## 9 · Anotado para después

1. **Rehacer V3 y V4 de los labs 04 a 14, o aceptarlos como están.** Es lo que la §8 deja abierto.
   No hace falta rehacer las guías: hace falta **correr los labs y comparar** lo que sale con lo
   que dicen. Es media jornada, y se puede repartir por labs.
2. **La V2 de las catorce guías.** Sigue siendo la prueba de fondo, y sigue sin hacerse. Va junto
   con la fila de aceptación del PO, que recorre lo mismo desde `PASOS.md`.
3. **Un visor de PDF real, y en Windows** (INFORME-SPEC-044 §10.2). Ahora importa más: con la §5,
   **seis bloques de las guías dependen de que el alumno teclee YAML a mano**. Si un visor
   decente conserva la sangría, esos seis podrían volver a ser un pegado y la guía se acortaría.
4. **El quinto «Si te atascas» de la lista del PO** (§4). Se interpretó como el pegado literal
   desde el PDF. Si quería decir otra cosa, es una frase suya y se resuelve en un minuto.
5. **El puerto 8082 ocupado por Docker en la máquina del PO** (INFORME-SPEC-044 §7.1). Sigue igual
   y sigue rompiendo el lab 01 en clase.
6. **`estadoDelCircuito()` del lab 10 sigue sin usarse.** Viene anotado desde el INFORME-SPEC-043
   §6 y ninguna SPEC desde entonces tocaba código. Ésta tampoco.
