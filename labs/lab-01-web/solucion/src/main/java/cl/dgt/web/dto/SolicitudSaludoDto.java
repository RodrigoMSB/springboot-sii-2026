package cl.dgt.web.dto;

/**
 * Lo que ENTRA por la API en el POST del paso 5.
 *
 * <p>El cliente manda un JSON con estos dos campos y Spring lo convierte en este objeto antes de
 * llamar al método. Es el camino de vuelta de {@link SaludoDto}: uno entra, el otro sale.
 *
 * @param nombre a quién saludar
 * @param formal si el saludo va de usted
 */
public record SolicitudSaludoDto(String nombre, boolean formal) {
}
