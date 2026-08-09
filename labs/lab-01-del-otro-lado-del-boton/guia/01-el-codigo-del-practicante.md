# Guía 01 · El código del practicante

**Acto 1 · El choque** 💥

El practicante se fue el viernes. Dejó el proyecto funcionando: compila, arranca, responde.
Carolina te pide que lo revises antes de subirlo.

## Lo que ves

```bash
cd starter
./mvnw -q spring-boot:run
```

Arranca. Responde. Los tests pasan. No hay nada roto.

## Lo que Carolina ve

Abre el archivo de configuración y **baja hasta el final**:

```
starter/src/main/resources/application.yml
```

```yaml
---
spring:
  config:
    activate:
      on-profile: prod
  datasource:
    url: jdbc:postgresql://prod-db.dgt.gob.cl:5432/dgt
    username: dgt_app
    password: Dgt2026Pr0d!
```

Ahí está. En la pantalla. Proyectada.

Y fíjate en **por qué nadie lo vio**: ese `---` abre un segundo documento YAML dentro del
mismo archivo, y `on-profile: prod` hace que solo se aplique en producción. Tu `dev` arranca
igual de bien, sin tocarlo. El archivo funciona. El archivo está mal.

> *«Esa es la clave de la base de producción, y está dentro del repositorio que clonaron
> dieciocho personas. Sácala de ahí — y después dime qué más hay que hacer, porque sacarla
> del archivo no es suficiente.»*

## Responde antes de seguir

| Pregunta | Tu respuesta |
|---|---|
| ¿Qué tres valores quedaron escritos en el repositorio? | |
| ¿Por qué la aplicación arrancaba bien en tu portátil aunque esto estuviera ahí? | |
| ¿Cuántas personas tienen hoy una copia de este archivo? | |
| Si la borras ahora mismo del archivo, ¿deja de servirle a quien ya la copió? | |

<details>
<summary>💡 La respuesta que duele</summary>

Un secreto escrito en un archivo del repositorio **deja de ser un secreto en el momento en
que se escribe**, no en el momento en que alguien lo usa. Está en el portátil de cada
persona que trabajó en el proyecto, en el servidor de integración que lo descarga en cada
compilación, y en cualquier copia que alguien hiciera del proyecto.

Borrarla del archivo protege lo que venga **después**. No deshace nada de lo anterior, y
nadie puede decirte quién ya la tiene.

**El archivo se limpia. El secreto sigue comprometido.**

La respuesta profesional es otra, y llega en la Guía 03.
</details>

> La contraseña es **de utilería**: el host `prod-db.dgt.gob.cl` no existe y la clave no
> abre nada. Está plantada para que aprendas a reconocer el problema.

➡️ Siguiente: [`02-el-parche-que-funciona.md`](02-el-parche-que-funciona.md)
