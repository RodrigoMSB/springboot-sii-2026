# Teoría · Módulo 2 (resto) + Módulo 3 (primera mitad)

## Índice

1. [Por qué la entidad no es el contrato](#1-por-qué-la-entidad-no-es-el-contrato)
2. [Lista negra vs lista blanca](#2-lista-negra-vs-lista-blanca)
3. [Las capas, y por qué el controlador no piensa](#3-las-capas-y-por-qué-el-controlador-no-piensa)
4. [Inyección por constructor vs por campo](#4-inyección-por-constructor-vs-por-campo)
5. [Mapeo: a mano vs MapStruct (criterio, no dogma)](#5-mapeo-a-mano-vs-mapstruct-criterio-no-dogma)
6. [Guardianes: quién vigila que no vuelva a pasar](#6-guardianes-quién-vigila-que-no-vuelva-a-pasar)
7. [OpenAPI y el versionado nativo](#7-openapi-y-el-versionado-nativo)
8. [Jackson 3](#8-jackson-3)
9. [Tabla DO / DON'T](#9-tabla-do--dont)
10. [Glosario](#10-glosario)
11. [Conclusiones y siembra del Módulo 3](#11-conclusiones-y-siembra-del-módulo-3)

---

## 1. Por qué la entidad no es el contrato

Una `@Entity` es un objeto de la base de datos. Tiene columnas, relaciones perezosas, y a
veces campos que solo existen para el negocio interno — como `puntajeRiesgoInterno`, el
número con que la DGT decide a quién fiscaliza.

Cuando un controlador devuelve la entidad, Jackson la serializa entera. Todo lo que sea un
getter, sale. El crimen de hoy no fue escribir `return contribuyente;` con mala intención:
fue no tener una frontera donde decidir **qué sale**. Esa frontera es el DTO.

> **Analogía.** La entidad es tu billetera: lleva tu carné, tu tarjeta, la foto de tus
> hijos y un papelito con tu clave. El DTO es lo que le muestras al guardia de un edificio:
> tu carné, nada más. Nadie te acusa de esconder la clave — simplemente no la sacas.

---

## 2. Lista negra vs lista blanca

El parche tentador es `@JsonIgnore` sobre los campos sensibles: "escondo el puntaje y listo".
El JSON sale limpio. El test manual pasa. Y es una trampa.

`@JsonIgnore` es una **lista negra**: enumera lo prohibido. Funciona hasta que alguien
agrega un campo nuevo y sensible a la entidad — que **nace expuesto**, porque nadie se
acordó de agregarlo a la lista negra. El descuido es el estado por defecto.

Un DTO es una **lista blanca**: enumera lo permitido. Un campo nuevo en la entidad no puede
filtrarse: para salir, alguien tiene que agregarlo al DTO a mano, y ese acto se ve en el
diff, en la revisión, en la conciencia de quien lo escribe.

> La misma diferencia que hay entre "cierra las puertas que recuerdes" y "todo cerrado,
> abres solo lo que decides".

---

## 3. Las capas, y por qué el controlador no piensa

Un `@RestController` tiene un solo trabajo: traducir HTTP. Recibe una petición, llama a
alguien que sabe, y traduce la respuesta a un código de estado y un cuerpo. **No piensa.**

Quien piensa es la capa de aplicación: el `FichaService`. Ahí vive la lógica de armar la
ficha, decidir qué es público, buscar en el repositorio. El controlador depende de un
**contrato** (`FichaService`, una interfaz), no de una implementación.

¿Por qué una interfaz? Porque así puedes probar el controlador con un doble de test, sin
base de datos, y puedes cambiar cómo se arma la ficha sin recompilar la web. Un controlador
que llama directo al repositorio es un controlador que, tarde o temprano, mete un `if` de
negocio entre medio — y ya nadie sabe dónde vive la lógica.

---

## 4. Inyección por constructor vs por campo

```java
@Autowired private FichaService fichas;              // ❌ por campo
public FichaController(FichaService fichas) { ... }  // ✅ por constructor
```

Por campo parece más corto. Su costo: el objeto no se puede construir sin Spring. En un
test unitario tendrías que levantar el contenedor entero para tener una instancia. Por
constructor, `new FichaController(mockDelServicio)` y listo.

Hay un guardián que lo vigila: `AU-06` falla si encuentra un `@Autowired` en un campo. No
es dogma — es que el diseño testeable no debería depender de tu disciplina.

---

## 5. Mapeo: a mano vs MapStruct (criterio, no dogma)

Convertir entidad → DTO se puede hacer a mano (`new FichaDTO(c.getRut(), c.getRazonSocial())`)
o con una librería como MapStruct que genera el mapeo.

**Cuándo a mano:** cuando el mapeo es una lista blanca corta y deliberada, como aquí. Verlo
escrito es la documentación de qué sale. Es contenido, no ruido (D-003: lo que se esconde
no se aprende).

**Cuándo MapStruct:** cuando tienes veinte DTOs con treinta campos cada uno y el mapeo es
mecánico. Ahí escribirlo a mano es error humano esperando ocurrir.

No hay respuesta única. Hay criterio. En este curso mapeamos a mano porque cada campo que
cruza la frontera es una decisión, y queremos verla.

---

## 6. Guardianes: quién vigila que no vuelva a pasar

Arreglar el crimen no basta. Carolina lo dijo: *"haz que sea imposible repetirlo"*.

Un guardián es un test de arquitectura (ArchUnit) que **falla** si alguien reintroduce el
patrón prohibido. `AU-02` dice: ninguna clase anotada con `@RestController` puede depender
de una clase anotada con `@Entity`. Si mañana alguien devuelve la entidad, el build se pone
rojo antes de llegar a producción.

**La trampa que vas a evitar** (medida en el spike técnico del curso): NO escribas la regla
sobre el *tipo de retorno crudo*. Un controlador que devuelve `ResponseEntity<Contribuyente>`
tiene tipo crudo `ResponseEntity` — la entidad viaja escondida en el genérico. Una regla
sobre `haveRawReturnType` pasa en verde mientras el puntaje se filtra. La regla correcta usa
`dependOnClassesThat()`, que lee el atributo `Signature` del bytecode, donde el genérico sí
aparece.

Y la regla de la casa sobre las reglas: **un guardián sin prueba de que muerde es un
adorno.** Cada guardián que instales viene con un *fixture*: una clase que lo viola a
propósito, y un test que verifica que el guardián la caza. Si tu regla no muerde a nadie, no
protege a nadie.

---

## 7. OpenAPI y el versionado nativo

Tu API tiene un contrato. OpenAPI lo hace **visible**: a partir de tus anotaciones
(`@Operation`, `@ApiResponse`, `@Schema`), springdoc genera una especificación navegable en
`/swagger-ui`. Quien consume tu API la lee sin adivinar.

El versionado nativo (`/api/v1/`) es una promesa: el día que cambie la forma de la ficha,
nace `/api/v2/` y el `v1` sigue respondiendo a quien no migró. Romper un contrato publicado
sin avisar es cómo se pierde la confianza de quien construyó sobre ti.

---

## 8. Jackson 3

Spring Boot 4 trae Jackson 3 (paquete `tools.jackson`, no `com.fasterxml` para el core). Para
lo que haces hoy no cambia casi nada — pero si copias un tutorial de Boot 3 y ves imports que
no resuelven, es esto. Las anotaciones (`@JsonIgnore`, `@JsonProperty`) siguen en
`com.fasterxml.jackson.annotation`.

---

## 9. Tabla DO / DON'T

| ✅ DO | ❌ DON'T |
|---|---|
| Devolver un DTO (lista blanca) | Devolver la entidad |
| `@JsonIgnore`... nunca como solución de contrato | `@JsonIgnore` para "tapar" un campo sensible |
| Lógica en el servicio | Lógica en el controlador |
| Inyección por constructor | `@Autowired` en el campo |
| Guardián + fixture que prueba que muerde | Un guardián que nunca cazó a nadie |
| `dependOnClassesThat()` para AU-02 | `haveRawReturnType()` (el genérico se escapa) |
| Versionar la API (`/api/v1/`) | Cambiar el contrato sin avisar |

---

## 10. Glosario

- **DTO** — Data Transfer Object. Lo que tu API dice, no lo que tu base guarda.
- **Lista blanca / negra** — enumerar lo permitido / lo prohibido. La blanca falla segura.
- **Capa de aplicación** — donde vive la lógica del caso de uso. Entre la web y el dominio.
- **ArchUnit** — librería para escribir reglas de arquitectura como tests.
- **Fixture (negativo)** — clase que viola una regla a propósito, para probar que la caza.
- **OpenAPI** — estándar para describir una API HTTP. Swagger UI lo muestra.
- **Versionado nativo** — `/api/v1/`: el número en la ruta es parte del contrato.

---

## 11. Conclusiones y siembra del Módulo 3

Hoy tapaste una filtración con un DTO, sacaste la lógica a su capa, y — lo más importante —
instalaste guardianes que hacen el crimen **estructuralmente imposible de repetir**.

Fíjate en lo que acabas de construir: tests que vigilan la **estructura** de tu código. Que
la web no toque la entidad. Que el controlador no salte la capa. Cosas que se pueden ver sin
ejecutar nada.

🌱 **Siembra del Módulo 3 — "Red de seguridad: los tests como enunciado".**

Pero la estructura es solo la mitad. Un código con las capas perfectas puede, aun así,
calcular mal un folio, aceptar una transición de estado ilegal, o cobrar un IVA que no
cuadra. Los guardianes de arquitectura no ven el **comportamiento**.

¿Quién vigila que el código haga lo correcto, no solo que esté bien ordenado?

**La próxima semana, la suite de la DGT te va a llegar en rojo. Catorce tests fallando. Y
esos tests no son un examen: son el enunciado.** Tu trabajo será ponerlos en verde — y al
hacerlo, vas a descubrir qué se supone que hace este sistema.

El Módulo 3 se llama *«Red de seguridad»*. Trae puesto el arnés.
