# Guía 04 · Configuración con tipos, y tu primer endpoint

---

## TODO_3 · `DgtProperties` (≈15 min)

Abre `starter/src/main/java/cl/dgt/tramites/application/SelloService.java`. Mira el
constructor:

```java
public SelloService(
        @Value("${dgt.institucion}") String institucion,
        @Value("${dgt.folio.prefijo}") String prefijo,
        @Value("${dgt.folio.largo}") int largo) { ... }
```

Funciona. Y tiene tres agujeros:

1. **Cadenas mágicas.** Renombra `dgt.folio.largo` en el YAML y esto compila igual.
2. **Cero validación.** Pon `largo: 3` y la aplicación arranca feliz… y emite folios de
   tres dígitos a contribuyentes reales.
3. **Disgregación.** Tres propiedades, tres sitios donde equivocarse.

Convierte `config/DgtProperties.java` en configuración tipada y validada, y haz que
`SelloService` la reciba por constructor.

> **Pista 2.** `@ConfigurationProperties(prefix = "dgt")` + `@Validated` sobre el record.
> `@NotBlank` en los textos, `@Min(6)` en el largo, y `@Valid` sobre el record anidado —sin
> ese `@Valid`, la validación no entra en `Folio` y `largo: 3` pasa—. La aplicación ya
> tiene `@ConfigurationPropertiesScan`: no declares ningún bean a mano.

Compruébalo antes de continuar:

```bash
cd starter && ./mvnw test -Dtest='**/enunciado/T3_*.java'
```

---

## TODO_4 · `GET /api/tramites/{id}` (≈15 min)

Tu primer endpoint. Dos mitades, y la segunda es la que importa.

**Mitad A — el camino feliz.** En `TramiteController`, devuelve lo que el servicio te da.
El servicio ya existe y ya devuelve un `TramiteDto`. Borra el
`UnsupportedOperationException`.

**Mitad B — el camino triste.** Un `id` que no existe debe responder **404 con un
`ProblemDetail`**, no una traza de 300 líneas que le regala a un atacante los nombres de tus
clases. En `ManejadorDeErrores` hay un hueco marcado; el manejador de
`ContribuyenteNoEncontradoException`, tres líneas más arriba, hace exactamente lo que
necesitas. Cópiate a ti mismo, no a StackOverflow.

### Una advertencia

Vas a sentir la tentación de devolver `Tramite` en vez de `TramiteDto`. Son cuatro campos
menos que escribir. Hazlo, si quieres — y luego corre:

```bash
./mvnw test -Dtest='**/arquitectura/*Test.java'
```

`AU-02` te va a cazar. Y si crees que puedes esconder la entidad dentro de un
`ResponseEntity<Tramite>`, también. Lee el mensaje del test: nombra el crimen, no la regla.

### Compruébalo con tus manos

```bash
cd .. && ./bin/start-lab.sh
curl      http://localhost:8099/api/tramites/1
curl -i   http://localhost:8099/api/tramites/999
```

| Pregunta | Tu respuesta |
|---|---|
| ¿Qué campos viajan en el JSON del trámite? | |
| ¿Qué `Content-Type` devuelve el 404? | |
| ¿Por qué el dominio lanza una excepción en vez de devolver un 404? | |

---

## Cierre

```bash
./bin/90-validar.sh
```

Buscas `🏆 LAB 01 APROBADO`. Cuando lo tengas, completa
[`plantillas/reporte-entregable.md`](../plantillas/reporte-entregable.md).

➡️ ¿Te sobró tiempo? [`99-desafio-el-secreto-que-viaja.md`](99-desafio-el-secreto-que-viaja.md)
