# Guion de reinicio de sala

**Para el instructor. Diez minutos al empezar la próxima sesión.**

La sesión 1 se fue en pelear con las máquinas: Javas de distintas versiones, Maven que no
descargaba, Docker que no estaba. Nada de eso vuelve a pasar — el material ya no le pide nada a
la máquina salvo **Git**. Pero hay que reiniciar limpio, y eso son diez minutos bien invertidos.

---

## 1 · Clon fresco. No `git pull`.

**Dilo así, textual:** *«Borra la carpeta del curso y clona de nuevo. No hagas `git pull`.»*

Por qué, en una línea para quien pregunte: el repositorio cambió de raíz —ahora trae dentro
Maven, las dependencias y el JDK—, y sobre un clon viejo un `pull` deja mezclados el material
nuevo y los restos del viejo. Clonar de cero tarda menos que diagnosticar esa mezcla.

```bash
cd ~            # o donde tengan sus cosas
rm -rf springboot-sii-2026
git clone <URL-del-repo>
```

⚠️ **No dentro de OneDrive, Documentos sincronizados ni escritorio corporativo.** Es el primer
sospechoso de la sección 4.

---

## 2 · Deshacer el parche de emergencia de la sesión 1

En la sesión 1 se agregó a mano un `PATH` hacia un Maven descargado. **Ya no hace falta** — el
`./mvnw` trae el suyo — y aunque el shim es inmune a ese parche, una sala limpia se diagnostica
mejor.

Que cada alumno mire si lo tiene:

```bash
grep -n 'apache-maven' ~/.bashrc
```

- **Sin salida** → nada que hacer, siguiente.
- **Con salida** → que comente esa línea poniéndole un `#` delante, con un editor, y cierre y
  vuelva a abrir la terminal.

⚠️ **Que no borre nada más de ese archivo.** Solo la línea que el `grep` señaló. Si alguien tiene
dudas, que le muestre la pantalla al instructor antes de tocar.

---

## 3 · La secuencia de arranque

Cuatro comandos. Léelos en voz alta y que los sigan a la vez:

```bash
git clone <URL-del-repo>
cd springboot-sii-2026/labs/lab-00-estacion-base
./bin/00-verificar.sh
```

Lo que tienen que ver:

```
[OK]    git está en el PATH
[OK]    El clon tiene todas sus piezas (Maven, dependencias, JDK y los labs)
[INFO]  Ensamblando el JDK embebido si hace falta (la primera vez tarda)…
[OK]    JDK embebido listo: openjdk version "25.0.4" …
[INFO]  Java del sistema: …  — el curso NO lo usa
[OK]    ./mvnw usa el Maven del repositorio: Apache Maven 3.9.11 …
[OK]    Espacio libre: … GB

  5/5 verificaciones
  ESTACIÓN LISTA
```

**Esa línea de «Java del sistema» va a decir cosas distintas en cada máquina —17, 21, o nada— y
eso es exactamente lo que queremos.** Si alguien la ve y se preocupa, esa es la buena noticia
del día: ya no importa.

Con `ESTACIÓN LISTA`, al Lab 01.

### Lo que NO debería aparecer: el cartel del Firewall

Cuando levanten la app (`./bin/start-lab.sh`), Windows **no debería** sacar el cartel de
«Windows Defender ha bloqueado algunas características de esta aplicación». Si apareciera, es un
cartel que pide administrador y que el alumno de una máquina corporativa **no puede aceptar**.

Ya está resuelto de raíz: la app se ata solo a `localhost`, no a todas las interfaces, así que
Windows no tiene nada que preguntar. Si aun así aparece en alguna máquina:

- **Que le dé a Cancelar.** No hace falta permitir nada — el laboratorio se usa desde la propia
  máquina y todo va por `localhost`.
- Si tras cancelar la app funciona igual (`curl http://localhost:8099/actuator/health` responde),
  no hay nada que arreglar: el cartel era ruido.
- Si la app NO funciona tras cancelar, avísame: sería un caso nuevo.

---

## 4 · Plan B — si UNA máquina falla

Míralo en este orden. Son los tres sospechosos de siempre en máquinas corporativas Windows.

### a · ¿Git está?

```bash
git --version
```

Si no responde: no hay curso en esa máquina hasta instalarlo. Es el único requisito, y es el
único que no podemos meter en el repositorio. Que se emparejen con un compañero mientras tanto.

### b · ¿El clon está en una carpeta sincronizada?

```bash
pwd
```

Si la ruta contiene `OneDrive`, `Dropbox`, `Google Drive` o similar: **mover el clon fuera de
ahí**. El sincronizador toca archivos mientras Maven y el JDK los escriben, y produce fallos que
parecen del material y no lo son.

```bash
cd ~ && rm -rf springboot-sii-2026 && git clone <URL-del-repo>
```

Nota: el `%USERPROFILE%\Documents` de muchas máquinas corporativas **es** OneDrive sin que se
note. Que miren la ruta completa, no el nombre de la carpeta.

### c · ¿El antivirus retuvo el ensamblado del JDK?

Síntoma exacto — el shim aborta diciendo:

```
[ERROR] El JDK ensamblado NO coincide con su firma.
```

Ese mensaje significa que los trozos del JDK se juntaron mal. En una máquina corporativa el
sospechoso es el antivirus, que escanea el archivo de 134 MB recién creado y a veces lo deja a
medias. Qué hacer:

```bash
rm -rf tools/jdk/runtime
cd labs/lab-01-del-otro-lado-del-boton/starter && ./mvnw -version
```

Si vuelve a fallar dos veces seguidas, no insistas en sala: esa máquina necesita una exclusión
del antivirus para la carpeta del curso, y eso es una gestión de TI. Emparéjalo con un compañero
y sigue — no detengas a diecisiete personas por una.

---

### d · ¿La app quedó viva después de `99-destruir.sh`?

En Windows, el desmontaje es lo menos probado del material. Si un alumno dice que
`./bin/start-lab.sh` se queja de que **el puerto 8099 está ocupado**, es que un arranque
anterior no murió del todo. Cómo verlo y resolverlo, en Git Bash:

```bash
netstat -ano | grep 8099        # ¿hay algo escuchando? La última columna es el PID
taskkill //PID <ese-pid> //F    # las dos barras son a propósito en Git Bash
```

Si pasa en más de una máquina, díselo al equipo del material: es un hallazgo, no una anécdota.

## 5 · Lo que ya NO hay que verificar (y no hay que dejar que se verifique)

Si alguien de la sala —o de TI— propone comprobar alguna de estas, di que no hace falta:

| | Por qué ya no |
|---|---|
| Que tengan Java 25 instalado | El JDK viaja en el repositorio y `./mvnw` usa el suyo. El Java de la máquina es irrelevante |
| Que tengan Maven | Viaja en `tools/maven` |
| Que tengan Docker | El curso no lo usa: PostgreSQL corre como proceso hijo, no en contenedor |
| Que lleguen a Maven Central | No se descarga nada: las dependencias viajan en `repo-maven` |
| Que el proxy corporativo esté configurado | No hay tráfico que proxear |

El curso entero, del Lab 00 al Lab 07, corre con el cable de red desenchufado. Está verificado
así, no supuesto.
