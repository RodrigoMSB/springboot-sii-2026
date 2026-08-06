# Guía 03 · La forma correcta

**Acto 3 · La forma correcta** ✅ (TODO_1 y TODO_2)

El secreto no vive en el repositorio. Vive en el **entorno** donde corre la aplicación, y
el repositorio solo dice *cómo se llama*.

---

## TODO_1 · El perfil `prod` pide sus secretos al entorno

Crea `starter/src/main/resources/application-prod.yml`:

```yaml
spring:
  datasource:
    url: ${DGT_DB_URL}
    username: ${DGT_DB_USER}
    password: ${DGT_DB_PASSWORD}
```

**Sin valores por defecto.** Un `${DGT_DB_PASSWORD:cambiame}` arrancaría en silencio contra
la base equivocada, y el error aparecería un martes de madrugada. Los dos puntos parecen
amables; son un desastre.

Mientras tanto, `application-dev.yml` no necesita ninguna credencial: Boot levanta el
`compose.yaml` y cablea la conexión solo.

> **Pista 1.** También falta `application-test.yml`. En la suite no hay compose: el
> contenedor lo levanta Testcontainers desde el propio test.

---

## TODO_2 · Que `prod` falle rápido **y claro**

Quita las variables y arranca en `prod`. Spring falla. Mira el mensaje:

```
Failed to instantiate [com.zaxxer.hikari.HikariDataSource]:
Factory method 'dataSource' threw exception with message: 'url' must start with "jdbc"
```

Falló rápido, sí. Pero ese mensaje **no nombra la variable que falta**, no dice quién debía
definirla, y manda a quien lo lee a leer el código de Hikari. A las tres de la mañana.

El andamio ya está escrito: `config/VerificadorDeSecretosProd.java` corre en el perfil
`prod`, es un `BeanFactoryPostProcessor` (se ejecuta **antes** de instanciar cualquier bean,
así que gana la carrera contra el `DataSource`) y ya recibe el `Environment`. **Lo único que
falta es el corazón**, marcado con `{{TODO_2}}`: recorrer las tres variables, juntar las que
falten, y lanzar un `IllegalStateException` que las **nombre a todas**.

**Fallar rápido no es solo que falle. Es que falle antes, y con un mensaje accionable.**

> **Pista 2.** `entorno.getProperty("DGT_DB_URL")` devuelve `null` si falta;
> `StringUtils.hasText(...)` te dice si un valor es usable. Junta las que falten y lánzalas
> todas de una vez: nadie quiere arreglar tres errores de uno en uno.

Cuando termines, **transcribe literalmente** el mensaje que produce tu código. Te lo pide el
reporte entregable, y no vale resumirlo.

---

## ¿Y el historial? La credencial se **rota**, no se borra

Ya lo viste en la Guía 01: borrar el commit no sirve, y reescribir el historial es cirugía
mayor —cambia todos los SHA, rompe cada rama abierta, obliga a dieciocho personas a volver a
clonar— y aun así la credencial ya está en la máquina de quien clonó ayer.

**La respuesta profesional es rotar:** cambias la contraseña en el servidor de base de
datos, y lo que quedó en el historial deja de abrir nada. Después, y solo después, decides
si vale la pena reescribir la historia.

En este laboratorio, rotar es esto:

```bash
# compose.yaml
POSTGRES_PASSWORD: dgt-dev-rotada
```

```bash
docker compose down -v && docker compose up -d    # base nueva, clave nueva
```

En la DGT real, es una llamada al DBA y un incidente registrado. El comando es lo de menos.

| Pregunta | Tu respuesta |
|---|---|
| ¿Qué comando exacto usaste para rotar? | |
| Tras rotar, ¿la credencial del historial abre algo? | |
| ¿Por qué no basta con `git filter-repo`? | |

➡️ Siguiente: [`04-configuracion-con-tipos.md`](04-configuracion-con-tipos.md)
