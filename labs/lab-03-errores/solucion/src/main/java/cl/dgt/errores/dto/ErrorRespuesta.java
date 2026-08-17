package cl.dgt.errores.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.time.Instant;
import java.util.Map;

/**
 * La forma que tiene un error en esta API. Una sola, para todos.
 *
 * <p>Que sea siempre la misma es el punto: quien consume la API escribe el código que lee errores
 * <strong>una vez</strong>. Si cada fallo tuviera su propia forma, habría que programar contra
 * cada uno.
 *
 * <p>{@code campos} solo se llena en los errores de validación del paso 4. En los demás va
 * {@code null}, y {@code @JsonInclude(NON_NULL)} hace que entonces ni siquiera aparezca en el
 * JSON — sin esa anotación saldría {@code "campos":null} en todas las respuestas, que es ruido
 * que quien consume la API tendría que aprender a ignorar.
 *
 * <p>{@code Instant} y no {@code LocalDateTime}: un instante no tiene zona horaria, así que no hay
 * que preguntarse de quién es la hora que sale. Jackson lo escribe en formato ISO-8601 terminado
 * en {@code Z}.
 *
 * @param mensaje   qué pasó, en lenguaje de quien llama
 * @param codigo    el mismo código HTTP de la respuesta, repetido en el cuerpo
 * @param timestamp cuándo
 * @param campos    qué campo falló y por qué — solo en errores de validación
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record ErrorRespuesta(String mensaje, int codigo, Instant timestamp, Map<String, String> campos) {

    // =========================================================================
    //  EL ATAJO PARA EL CASO NORMAL
    // -------------------------------------------------------------------------
    //  Un método estático de fábrica: evita repetir `Instant.now(), null` en
    //  cada handler y deja el sitio único donde se decide la marca de tiempo.
    //  Los errores de validación no lo usan —ellos sí tienen campos— y llaman al
    //  constructor completo.
    //  Para pensar: si mañana hubiera que añadir un identificador de traza a
    //  todos los errores, ¿cuántos archivos habría que tocar?
    // =========================================================================
    public static ErrorRespuesta de(String mensaje, int codigo) {
        return new ErrorRespuesta(mensaje, codigo, Instant.now(), null);
    }
}
