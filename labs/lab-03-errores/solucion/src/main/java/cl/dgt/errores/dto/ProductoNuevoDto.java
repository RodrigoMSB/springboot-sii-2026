package cl.dgt.errores.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;

/**
 * Lo que entra por el POST.
 *
 * <p>Las dos anotaciones llegaron en el paso 4. No son documentación: con
 * {@code starter-validation} en el pom y un {@code @Valid} en el controller, la petición que no
 * las cumpla ni siquiera llega al cuerpo del método. Hacen falta las tres piezas —la dependencia,
 * la anotación aquí y el {@code @Valid} allí—; con dos de las tres no pasa nada y tampoco avisa.
 *
 * <p>{@code @NotBlank} es más estricto que {@code @NotNull}: rechaza también la cadena vacía y la
 * que solo tiene espacios. {@code @Positive} exige mayor que cero — {@code @PositiveOrZero} sería
 * la variante que admite el 0.
 *
 * <p>El {@code message} de cada una es lo que verá quien llame a la API. Sin él saldría el texto
 * por defecto de la librería, en inglés y genérico.
 *
 * @param nombre no puede venir vacío ni en blanco
 * @param precio tiene que ser mayor que cero
 */
public record ProductoNuevoDto(
        @NotBlank(message = "el nombre es obligatorio") String nombre,
        @Positive(message = "el precio debe ser mayor que cero") int precio) {
}
