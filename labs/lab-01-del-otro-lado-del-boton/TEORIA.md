# Teoría · Módulo 1 (+ primera hora del Módulo 2)

## Índice

1. [El contenedor: quién construye tus objetos](#1-el-contenedor-quién-construye-tus-objetos)
2. [Autoconfiguración: el mayordomo que adivina](#2-autoconfiguración-el-mayordomo-que-adivina)
3. [Configuración externalizada](#3-configuración-externalizada)
4. [Perfiles](#4-perfiles)
5. [`@Value` vs `@ConfigurationProperties`](#5-value-vs-configurationproperties)
6. [Fallar rápido, y fallar claro](#6-fallar-rápido-y-fallar-claro)
7. [Un secreto filtrado no se borra: se rota](#7-un-secreto-filtrado-no-se-borra-se-rota)
8. [M2 · El contrato REST: DTO y `ProblemDetail`](#8-m2--el-contrato-rest-dto-y-problemdetail)
9. [Tabla DO / DON'T](#9-tabla-do--dont)
10. [Glosario](#10-glosario)
11. [Conclusiones y siembra del Módulo 2](#11-conclusiones-y-siembra-del-módulo-2)

---

## 1. El contenedor: quién construye tus objetos

En Java normal, tú escribes `new ContribuyenteService(new ContribuyenteRepository(...))`.
Cada `new` es una decisión: *esta* clase depende de *esa* implementación, para siempre,
compilada.

Spring te quita el `new`. Tú declaras qué necesitas —lo pides en el constructor— y el
**contenedor IoC** te lo entrega. IoC es *Inversión de Control*: el control de quién
construye a quién se invierte.

```java
@Service
public class TramiteService {
    private final TramiteRepository repositorio;

    public TramiteService(TramiteRepository repositorio) {   // "necesito esto"
        this.repositorio = repositorio;
    }
}
```

**Por constructor, siempre.** Hay una regla de arquitectura (`AU-06`) que falla si inyectas
por campo con `@Autowired`. No es purismo: un objeto que se construye con `new` en un test,
sin levantar Spring, se prueba en milisegundos. Uno que necesita el contenedor entero para
existir, no.

> **Analogía.** Un restaurante. El cocinero no va al mercado a comprar tomates: pide
> tomates y aparecen. No sabe de qué proveedor vinieron, y por eso el dueño puede cambiar
> de proveedor sin que el cocinero se entere. El contenedor es el proveedor. Tu servicio es
> el cocinero: escribe qué ingredientes necesita en la puerta de la cocina —el
> constructor— y se dedica a cocinar.

---

## 2. Autoconfiguración: el mayordomo que adivina

Pusiste `spring-boot-starter-web` en el `pom.xml` y de pronto tienes un servidor HTTP
escuchando. No lo configuraste. ¿Magia?

No: **condiciones**. Boot trae cientos de clases de autoconfiguración que dicen, en
esencia, *"si veo Tomcat en el classpath **y** el usuario no definió su propio servidor,
configuro uno"*. Es `@ConditionalOnClass`, `@ConditionalOnMissingBean`.

La regla de oro: **la autoconfiguración se retira en cuanto tú apareces.** Si defines tu
propio `DataSource`, Boot no define el suyo. Nunca pelea contigo.

Para ver qué decidió y por qué:

```bash
./mvnw spring-boot:run -Dspring-boot.run.arguments=--debug
```

Sale un informe con dos listas: *Positive matches* (lo que configuró y por qué) y
*Negative matches* (lo que no configuró y qué le faltó). El día que algo "no funciona por
arte de magia", ese informe es tu respuesta.

> **Analogía.** Un mayordomo que lleva treinta años en la casa. Si llegas a las once de la
> noche, te deja la cena servida sin preguntar. Si le dices *"hoy ceno fuera"*, no la
> sirve. Nunca discute. Lo insoportable de un mayordomo así es que, cuando algo no está
> donde esperabas, no sabes qué asumió. `--debug` es preguntarle.

---

## 3. Configuración externalizada

Lo que cambia entre tu portátil y el servidor de la DGT **no puede vivir compilado**. Boot
lee configuración de muchas fuentes, y las ordena por precedencia. De menor a mayor, en lo
que nos importa hoy:

1. `application.yml` (dentro del `jar`)
2. `application-<perfil>.yml`
3. **Variables de entorno**
4. Argumentos de línea de comandos (`--server.port=8099`)

Lo de arriba manda sobre lo de abajo. Por eso una variable de entorno puede sobrescribir
un valor del `yml` sin recompilar nada, y por eso **los secretos viajan por ahí**.

La sintaxis del placeholder tiene una trampa mortal:

```yaml
password: ${DGT_DB_PASSWORD}            # si falta, no arranca
password: ${DGT_DB_PASSWORD:cambiame}   # si falta, arranca con "cambiame"
```

Los dos puntos son un valor por defecto. Parecen amables. Son un desastre: la aplicación
arranca en silencio, se conecta a donde no debe, y el error aparece un martes de madrugada
lejos de quien lo escribió.

---

## 4. Perfiles

Un perfil es una etiqueta. Activas `dev` y Spring carga `application-dev.yml` encima de
`application.yml`, y enciende los beans marcados `@Profile("dev")`.

Los tres del curso:

| Perfil | Base de datos | Para qué |
|---|---|---|
| `dev` | La levanta Docker Compose, en tu portátil | Programar |
| `test` | La levanta Testcontainers, desde el propio test | La suite |
| `prod` | La que digan las variables de entorno | El servidor |

Se activa con `--spring.profiles.active=prod`, con la variable `SPRING_PROFILES_ACTIVE`, o
—como en este lab— con un valor por defecto: `active: ${DGT_PERFIL:dev}`.

**El perfil no es el entorno.** El perfil dice *qué configuración cargar*; el entorno dice
*qué valores tienen los secretos*. Confundirlos es cómo termina una contraseña de
producción dentro de un `application-prod.yml`.

---

## 5. `@Value` vs `@ConfigurationProperties`

```java
public SelloService(@Value("${dgt.folio.largo}") int largo) { ... }   // ❌
```

Funciona. Y tiene tres agujeros:

1. **Es una cadena mágica.** Renombra `dgt.folio.largo` en el YAML y esto compila igual.
2. **No se valida.** `largo: 3` arranca perfecto y emite folios de tres dígitos.
3. **Se disgrega.** Tres propiedades, tres `@Value`, tres sitios donde equivocarse.

Frente a:

```java
@Validated
@ConfigurationProperties(prefix = "dgt")
public record DgtProperties(@NotBlank String institucion, @Valid Folio folio) {
    public record Folio(@NotBlank String prefijo, @Min(6) int largo) {}
}
```

Un `record` es **inmutable**: nadie cambia el prefijo del folio en caliente. La validación
convierte un error de configuración en un **arranque fallido**, no en un documento oficial
mal emitido.

> Un invariante que necesita un test para sostenerse ya perdió. Este no puede violarse:
> la aplicación no arranca.

---

## 6. Fallar rápido, y fallar claro

Son dos cosas distintas, y la segunda cuesta trabajo.

Quita `DGT_DB_URL` y arranca en `prod` con la configuración obvia. Spring falla. ¿Con qué
mensaje?

```
Failed to instantiate [com.zaxxer.hikari.HikariDataSource]:
Factory method 'dataSource' threw exception with message: 'url' must start with "jdbc"
```

Falló rápido, sí. Pero ese mensaje no nombra la variable, no dice quién debía definirla, y
manda a quien lo lee —a las tres de la mañana— a leer el código fuente de Hikari.

Por eso la solución trae un `BeanFactoryPostProcessor` que corre **antes** de que se
instancie ningún bean, y dice:

```
El perfil 'prod' no arranca sin sus secretos. Faltan estas variables de entorno:
[DGT_DB_URL, DGT_DB_USER, DGT_DB_PASSWORD]. Defínelas en el entorno del servidor,
jamás en un archivo del repositorio. Ejemplo: export DGT_DB_PASSWORD='...'
```

**Fallar rápido no es solo que falle: es que falle antes, y con un mensaje que se pueda
accionar.** Esa diferencia es tuya, no del framework.

---

## 7. Dónde viven los secretos, y por qué uno expuesto se rota

Las secciones anteriores dijeron **cómo** se externaliza la configuración. Esta dice **qué
no puede estar dentro del repositorio jamás**, y qué se hace cuando ya lo estuvo.

### La regla, y su criterio

No es «no pongas contraseñas en el código». Es más útil formularlo así:

> **Un valor va fuera del repositorio cuando, si se filtrara, habría que cambiarlo.**

Ese criterio se aplica solo y explica los dos casos de este laboratorio:

| Valor | ¿Versionado? | Por qué |
|---|---|---|
| `dgt.folio.prefijo: DGT` | **Sí** | Es una decisión de negocio. Si se filtra, no pasa nada. |
| La clave de `compose.yaml` (`dgt-dev`) | **Sí** | Abre una base desechable que vive en tu portátil y muere con `docker compose down -v`. Si se filtra, no protege nada. |
| La clave de `prod-db.dgt.gob.cl` | **JAMÁS** | Abre la base de producción de un servicio tributario. |

**La diferencia no es el archivo. Es qué protege el secreto y qué pasa si se filtra.**

### Dónde vive entonces

En el **entorno del proceso**, no en el proyecto. El repositorio solo dice **cómo se llama**
la variable; quién la rellena es el sistema que despliega:

```yaml
# application-prod.yml — esto SÍ se versiona: no es el secreto, es su nombre
spring:
  datasource:
    url: ${DGT_DB_URL}
    username: ${DGT_DB_USER}
    password: ${DGT_DB_PASSWORD}
```

De menos a más maduro, quién define esas variables: un archivo `.env` fuera del repositorio
→ variables del servidor de despliegue → un gestor de secretos (Vault, AWS Secrets Manager,
Azure Key Vault) que además **rota solo** y deja registro de quién leyó qué. Spring Boot lee
las tres igual: para la aplicación, un secreto siempre es una variable de entorno.

Y fíjate en lo que **no** lleva ese YAML: valores por defecto. Un
`${DGT_DB_PASSWORD:cambiame}` arrancaría en silencio contra la base equivocada. Los dos
puntos parecen amables; son un desastre.

### Y si ya estuvo escrito: se rota

Aquí está la lección que casi todo el mundo aprende tarde.

Quitar la contraseña del archivo protege lo que venga **después**. No deshace nada de lo
anterior: mientras estuvo escrita, cualquiera con acceso al proyecto pudo copiarla —una
copia local, una descarga del servidor de integración, una captura de pantalla— y **no
existe forma de saber quién lo hizo**. Un secreto expuesto no se «desexpone».

> **La única acción que restablece la seguridad es cambiar el secreto.**

Rotar significa pedir una credencial nueva para ese usuario de base de datos. En ese
instante, la vieja deja de abrir nada, esté donde esté y la tenga quien la tenga. La
credencial filtrada no desaparece: se vuelve **inútil**, que es lo único que se puede
conseguir.

En este laboratorio, rotar es cambiar la clave del `compose.yaml` y recrear la base. En la
DGT real es una llamada al DBA, un despliegue con la variable nueva, y un incidente
registrado — porque alguien va a preguntar cuánto tiempo estuvo expuesta y quién pudo verla.

**El orden importa, y es el que va a evaluarse:** primero rotar (corta el acceso), después
limpiar el archivo (evita que se repita). Al revés, pasas el rato limpiando mientras la
credencial sigue abriendo la puerta.

---

## 8. M2 · El contrato REST: DTO y `ProblemDetail`

Tu endpoint del TODO_4 devuelve un `TramiteDto`, no un `Tramite`. Dos razones:

**La entidad es de la base de datos, no del mundo.** Tiene relaciones perezosas que
explotan al serializarse, y campos que nadie de fuera debe ver. El DTO es la frontera donde
decides qué sale.

**El camino triste también es contrato.** Un `id` inexistente no devuelve una traza de 300
líneas con los nombres de tus clases dentro. Devuelve un `ProblemDetail` (RFC 9457):

```json
{"type":"https://dgt.cl/errores/tramite-no-encontrado",
 "title":"Trámite no encontrado","status":404,
 "detail":"No existe un trámite con id 999","id":999}
```

El camino feliz lo escribe cualquiera.

---

## 9. Tabla DO / DON'T

| ✅ DO | ❌ DON'T |
|---|---|
| Inyectar por constructor | `@Autowired` en el campo |
| `${VAR}` sin valor por defecto para los secretos | `${VAR:cambiame}` |
| `@ConfigurationProperties` + `@Validated` | Un `@Value` por cada propiedad |
| Rotar la credencial filtrada | Borrarla del archivo y respirar tranquilo |
| Devolver un DTO | Devolver la entidad "para no duplicar código" |
| `ProblemDetail` en el `@RestControllerAdvice` | Dejar salir la traza |
| Versionar la clave del `compose` de laboratorio | Versionar la clave de producción |

---

## 10. Glosario

- **Bean** — un objeto que construye y administra el contenedor de Spring.
- **IoC / Inyección de dependencias** — tú declaras qué necesitas; el contenedor te lo da.
- **Autoconfiguración** — beans que Boot define *si* se cumplen ciertas condiciones y *si*
  tú no los definiste antes.
- **Perfil** — etiqueta que activa un conjunto de configuración y de beans.
- **Placeholder** — `${...}`. Se resuelve contra el `Environment`.
- **`BeanFactoryPostProcessor`** — código que corre antes de instanciar los beans. Sirve
  para negarse a arrancar.
- **DTO** — objeto de transferencia. Lo que tu API dice, no lo que tu base guarda.
- **`ProblemDetail`** — formato estándar (RFC 9457) para los errores de una API.
- **Rotar** — cambiar un secreto para que el filtrado deje de servir.

---

## 11. Conclusiones y siembra del Módulo 2

Hoy aprendiste que **borrar no es remediar**, que la configuración es código y merece tipos,
y que fallar rápido solo sirve si el mensaje se puede accionar.

También escribiste tu primer endpoint. Devuelve un `TramiteDto`, con cuatro campos elegidos
a mano. Costó tres líneas más que devolver la entidad directamente.

🌱 **Siembra del Módulo 2 — "Controladores REST y el contrato de la API".**

Y ahora piénsalo: alguien, la semana que viene, con prisa, va a mirar tu `TramiteService` y
va a decir *"¿para qué duplicar campos? Devuelvo la entidad y listo"*. Compilará. Los tests
pasarán. La API responderá.

Y en esa respuesta viajará el `puntajeRiesgoInterno` de un contribuyente. Un número que la
DGT calcula sobre una persona y que jamás debía salir de sus servidores.

**La próxima semana alguien lo hace. Y se filtra un folio.**

El Módulo 2 se llama *«El folio que se filtró»*, y empieza con Carolina proyectando una
respuesta JSON en la pantalla. Tú ya sabes qué campo va a estar subrayado en rojo.
