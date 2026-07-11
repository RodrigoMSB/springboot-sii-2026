package cl.dgt.tramites.web.dto;

import cl.dgt.tramites.web.validacion.RutValido;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

/**
 * Lo que llega en {@code POST /api/v1/tramites}. La validación es DECLARATIVA: las reglas
 * viven en las anotaciones, no en un montón de {@code if} al principio del método.
 *
 * <p>Un request inválido nunca llega a la lógica: Spring lo rechaza en la frontera con un
 * 400 que nombra los campos. La lógica confía en que, si la ejecutan, los datos ya son
 * válidos.
 */
public record CrearTramiteRequest(

        @NotBlank(message = "El RUT del contribuyente es obligatorio")
        @RutValido
        String rutContribuyente,

        @NotBlank(message = "El tipo de trámite es obligatorio")
        @Pattern(regexp = "DECLARACION_F29|INICIO_ACTIVIDADES",
                message = "Tipo no reconocido: usa DECLARACION_F29 o INICIO_ACTIVIDADES")
        String tipo) {
}
