package cl.dgt.errores.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;

// Las anotaciones de validación son el contrato: sin @Valid en el controller no hacen nada.
public record ProductoNuevoDto(
        @NotBlank(message = "el nombre es obligatorio") String nombre,
        @Positive(message = "el precio debe ser mayor que cero") int precio) {
}
