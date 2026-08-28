package cl.dgt.examen.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

/** Viene resuelto: es el cuerpo del PUT que cambia el estado. */
public record CambioDeEstado(
        @NotBlank
        @Pattern(regexp = "PAGADO|PENDIENTE|RECHAZADO",
                 message = "tiene que ser PAGADO, PENDIENTE o RECHAZADO")
        String estado) {
}
