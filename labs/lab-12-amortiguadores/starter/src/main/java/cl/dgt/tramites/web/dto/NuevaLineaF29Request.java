package cl.dgt.tramites.web.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Una línea nueva del F29.
 *
 * <p>El monto NO lleva {@code @Positive}: los créditos son negativos y rechazarlos sería romper el
 * dominio para complacer a un validador. Lo único prohibido es el cero, y ese contrato vive donde
 * no se puede olvidar: el {@code CHECK (monto <> 0)} de la migración V3.
 */
public record NuevaLineaF29Request(

        @NotBlank(message = "El código del F29 es obligatorio")
        @Size(max = 10, message = "El código del F29 no puede exceder 10 caracteres")
        String codigo,

        long monto) {
}
