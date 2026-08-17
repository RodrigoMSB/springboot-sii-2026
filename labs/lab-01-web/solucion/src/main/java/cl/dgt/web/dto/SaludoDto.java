package cl.dgt.web.dto;

/**
 * Lo que SALE por la API cuando la respuesta es más que una frase.
 *
 * <p>Es un {@code record}, no una clase normal, y eso trae tres cosas gratis: un constructor con
 * los tres valores, un método de lectura por cada uno ({@code mensaje()}, no {@code getMensaje()})
 * y {@code equals}/{@code hashCode}/{@code toString} ya escritos. Además es <strong>inmutable</strong>:
 * una vez creado no se puede cambiar, que es justo lo que quieres en algo que solo va de salida.
 *
 * <p>No lleva ni una anotación, y eso es lo que hay que notar: no hace falta marcarlo de ninguna
 * forma para que salga como JSON. Spring convierte lo que devuelva un {@code @RestController}
 * usando Jackson, que llegó dentro de {@code spring-boot-starter-web} sin pedirlo por su nombre.
 *
 * <p>Los nombres de los campos del JSON son <strong>estos mismos</strong>. Renombrar un componente
 * aquí cambia la API para todos los que la consumen: es una decisión pública, no interna.
 *
 * @param mensaje el saludo ya armado, tal como se quiere mostrar
 * @param para    a quién va dirigido — se devuelve para que quien llama no tenga que recordarlo
 * @param formal  si se saludó de usted o de tú; sale como `true`/`false` en el JSON
 */
public record SaludoDto(String mensaje, String para, boolean formal) {
}
