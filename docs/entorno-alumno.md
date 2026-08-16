# Entorno del alumno — guía verificada por CI

> **Para ti, que vas a correr esto en tu máquina.** El instructor trabaja en macOS; el
> material corre igual en Linux y en Windows con **Git Bash**. Lo que aquí se describe
> está **verificado por el CI** del repositorio: cada cambio ejecuta `shellcheck` sobre
> todos los scripts del andamiaje y comprueba que **parsean con el bash de Windows**.

Lo que el CI verifica, este documento lo explica. **Lo que el CI no puede verificar, este
documento lo advierte.** Esa segunda mitad es la importante.

---

## 1 · Requisitos

| Herramienta | Versión | Verifica con |
|---|---|---|
| **Java (Temurin)** | **25 LTS** | `java -version` |
| **Docker Desktop** / Docker Engine | reciente, con el demonio **abierto** | `docker info` |
| **Git** | reciente (en Windows trae Git Bash) | `git --version` |

No necesitas Maven: el proyecto trae `./mvnw`.

---

## 2 · Windows: Git Bash, y nada más

Todo el andamiaje (`00-verificar.sh`, `start-lab.sh`, `99-destruir.sh`) es **bash puro**.
En Windows lo corres desde **Git Bash**, jamás desde CMD ni PowerShell.

Docker Desktop debe usar el backend **WSL2** para levantar contenedores Linux.

---

## 3 · Las fronteras del CI (léelas)

El CI comprueba mucho, pero no todo. Aquí es exactamente donde deja de cubrirte:

- **El CI comprueba que los scripts *parsean* en Windows, no que *funcionan*.** Los
  runners de Windows de GitHub no ofrecen contenedores Linux, así que allí no se puede
  levantar PostgreSQL. El día que corras `start-lab.sh` en tu Windows, será la primera
  vez que ese flujo completo se ejecute en Windows. Si algo falla, **es un bug nuestro**:
  repórtalo con la salida.

- **El CI corre con Java preinstalado; tu máquina, no.** `JAVA_HOME` y el `PATH` son cosa
  tuya. Que `java -version` diga 25 no garantiza que `JAVA_HOME` apunte al mismo sitio
  (**T-09**).

- **El CI tiene salida a internet sin proxy.** Tu institución quizá no. Maven Central y
  Docker Hub tienen que ser alcanzables (**T-05**), y esa gestión con TI puede tardar
  días. Empiézala pronto.

- **El CI no mide el espacio en tu disco.** El curso baja unos 3 GB entre imágenes y
  dependencias.

---

## 4 · El modo `--sin-docker`

Instalar Docker en una institución del Estado es una gestión que puede fallar. Si falla,
el respaldo ya está escrito:

```bash
./bin/00-verificar.sh --sin-docker
```

El veredicto será `ESTACIÓN LISTA (MODO SIN DOCKER: capacidades reducidas)`. Significa
esto, con precisión:

| Puedes | No puedes |
|---|---|
| Leer, compilar y navegar todo el código | Levantar la aplicación (necesita PostgreSQL) |
| Correr los tests unitarios y los de arquitectura | Correr los tests de integración (Testcontainers) |
| Seguir toda la teoría y las guías | Construir la imagen OCI, ni levantar dos instancias |

Los temas sin sustituto pasan a ser **demo del relator**. No pierdes contenido: pierdes
las manos en el teclado para tres temas concretos. **Avisa al instructor la semana previa**,
no el día de la sesión.

---

## 5 · Si algo se rompe

`docs/troubleshooting.md`, tabla numerada. Cita el número. Y recuerda la regla de la casa:

> Si un script falla, no es culpa tuya: es información.
