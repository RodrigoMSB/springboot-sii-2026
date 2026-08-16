package cl.dgt.web.dto;

/**
 * Lo que sale por la API cuando la respuesta es más que una frase.
 *
 * <p>Es un {@code record}: una clase de datos, inmutable, sin getters escritos a mano. Nadie lo
 * convierte a JSON a mano — de eso se encarga Spring, y el paso 4 mira cómo.
 *
 * @param mensaje el saludo ya armado
 * @param para    a quién va dirigido
 * @param formal  si se saludó de usted o de tú
 */
public record SaludoDto(String mensaje, String para, boolean formal) {
}
