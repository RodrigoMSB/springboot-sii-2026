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
 * JSON.
 *
 * @param mensaje   qué pasó, en lenguaje de quien llama
 * @param codigo    el mismo código HTTP de la respuesta, repetido en el cuerpo
 * @param timestamp cuándo
 * @param campos    qué campo falló y por qué — solo en errores de validación
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record ErrorRespuesta(String mensaje, int codigo, Instant timestamp, Map<String, String> campos) {

    /** El caso normal: un error sin detalle por campo. */
    public static ErrorRespuesta de(String mensaje, int codigo) {
        return new ErrorRespuesta(mensaje, codigo, Instant.now(), null);
    }
}
