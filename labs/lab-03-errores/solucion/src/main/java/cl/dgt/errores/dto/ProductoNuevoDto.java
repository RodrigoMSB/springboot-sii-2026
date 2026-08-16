package cl.dgt.errores.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;

/**
 * Lo que entra por el POST.
 *
 * <p>Las dos anotaciones llegaron en el paso 4. No son documentación: con
 * {@code starter-validation} en el pom y un {@code @Valid} en el controller, la petición que no
 * las cumpla ni siquiera llega al cuerpo del método.
 *
 * @param nombre no puede venir vacío ni en blanco
 * @param precio tiene que ser mayor que cero
 */
public record ProductoNuevoDto(
        @NotBlank(message = "el nombre es obligatorio") String nombre,
        @Positive(message = "el precio debe ser mayor que cero") int precio) {
}
