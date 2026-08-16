package cl.dgt.web.dto;

/**
 * Lo que ENTRA por la API en el POST del paso 5.
 *
 * <p>Es el camino de vuelta de {@link SaludoDto}: uno entra, el otro sale. Que sean dos clases
 * distintas no es burocracia — lo que se recibe y lo que se devuelve casi nunca coinciden, y
 * mezclarlos obliga a dejar campos a medias en uno de los dos sentidos.
 *
 * <p>Tampoco lleva anotaciones. Cuando llega un POST con {@code Content-Type: application/json},
 * Jackson lee el cuerpo y construye este record buscando campos con estos nombres. Los que no
 * vengan quedan en su valor por defecto: {@code null} para el String y {@code false} para el
 * boolean — <strong>sin avisar</strong>. Comprobar que vengan es el Lab 03.
 *
 * @param nombre a quién saludar
 * @param formal si el saludo va de usted
 */
public record SolicitudSaludoDto(String nombre, boolean formal) {
}
