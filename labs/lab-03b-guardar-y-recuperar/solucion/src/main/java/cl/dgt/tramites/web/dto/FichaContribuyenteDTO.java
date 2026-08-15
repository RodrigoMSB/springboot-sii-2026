package cl.dgt.tramites.web.dto;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * La ficha pública de un contribuyente. Una LISTA BLANCA: aquí está, explícito, todo lo
 * que la DGT le cuenta al mundo sobre una persona.
 *
 * <p>Fíjate en lo que NO está: {@code puntajeRiesgoInterno}, el número con que la DGT decide
 * a quién fiscaliza. No se oculta —eso sería una lista negra, y la próxima vez alguien olvida
 * un campo—. Simplemente no existe aquí. Un campo nuevo y sensible en la entidad
 * {@code Contribuyente} no puede filtrarse por esta ficha: tendría que agregarse a mano,
 * y ese acto se ve en el diff.
 */
@Schema(description = "Ficha pública de un contribuyente ante la DGT")
public record FichaContribuyenteDTO(

        @Schema(description = "RUT del contribuyente", example = "11111111-1")
        String rut,

        @Schema(description = "Razón social o nombre", example = "Valentina Rojas")
        String razonSocial) {
}
