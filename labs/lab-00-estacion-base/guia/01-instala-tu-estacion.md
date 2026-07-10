# Guía 01 · Instala tu estación

Cuatro cosas. Ni una más. Si ya tienes alguna, salta esa sección — pero **verifica la
versión**: la mitad de los problemas del primer día son una Java vieja de otro curso.

Al final, `./bin/00-verificar.sh` te dirá si acertaste. No adivines: córrelo.

---

## 1 · Java 25 (Temurin)

El curso usa **Java 25 LTS**. No 21, no 17.

| Sistema | Cómo |
|---|---|
| **macOS** | `brew install --cask temurin@25` · o el `.pkg` de <https://adoptium.net> |
| **Windows** | El `.msi` de <https://adoptium.net> (marca «Set JAVA_HOME») |
| **Linux** | El `.tar.gz` de <https://adoptium.net>, o el paquete de tu distro |

Comprueba:

```bash
java -version     # debe decir 25.x.x
```

Si dice otra cosa, tienes varias Java instaladas y el `PATH` elige la equivocada → **T-02**.

---

## 2 · Docker

Para la base de datos. El curso **no** te va a pedir que escribas una cadena de conexión:
Boot levanta el contenedor solo.

| Sistema | Cómo |
|---|---|
| **macOS / Windows** | Docker Desktop: <https://www.docker.com/products/docker-desktop/> (en Windows, backend **WSL2**) |
| **Linux** | Docker Engine + `docker compose` |

Comprueba:

```bash
docker info       # debe responder sin error
```

> **Docker Desktop tiene que estar ABIERTO**, no solo instalado. Es el error nº 1 del
> primer día (**T-03**).

> **¿Tu institución no lo autoriza?** No estás fuera del curso. Corre
> `./bin/00-verificar.sh --sin-docker` y avisa al instructor: los temas que necesitan
> contenedores los verás como demo del relator (**T-04**).

---

## 3 · Git

| Sistema | Cómo |
|---|---|
| **macOS** | `xcode-select --install` o `brew install git` |
| **Windows** | <https://git-scm.com/download/win> — **trae Git Bash, que será tu terminal** |
| **Linux** | El paquete de tu distro |

En Windows, **todo el curso se corre desde Git Bash**. Ni CMD ni PowerShell: los scripts
del andamiaje son bash.

---

## 4 · Un editor

Cualquiera que entienda Java. Sugerencias, sin obligación:

- **IntelliJ IDEA** (Community basta) — la más cómoda para Spring.
- **VS Code** + *Extension Pack for Java*.

No necesitas configurar nada especial: el proyecto trae su Maven Wrapper (`./mvnw`), así
que ni siquiera hace falta instalar Maven.

---

## Y ahora, la prueba

```bash
cd labs/lab-00-estacion-base
./bin/00-verificar.sh
```

Buscas esta línea:

```
  7/7 verificaciones
  ESTACIÓN LISTA
```

Si ves `ESTACIÓN INCOMPLETA`, cada `[ERROR]` trae su flecha `->` con qué hacer. No es un
regaño: es una lista de tareas.

➡️ Siguiente: [`02-conoce-el-terreno.md`](02-conoce-el-terreno.md)
