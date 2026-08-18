package cl.dgt.errores.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.time.Instant;
import java.util.Map;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record ErrorRespuesta(String mensaje, int codigo, Instant timestamp, Map<String, String> campos) {

    public static ErrorRespuesta de(String mensaje, int codigo) {
        return new ErrorRespuesta(mensaje, codigo, Instant.now(), null);
    }
}
