package cl.dgt.tramites.web.dto;

import jakarta.validation.constraints.NotBlank;

/** Lo que llega al login: un RUT y una clave. */
public record CredencialesRequest(
        @NotBlank String rut,
        @NotBlank String clave) {
}
