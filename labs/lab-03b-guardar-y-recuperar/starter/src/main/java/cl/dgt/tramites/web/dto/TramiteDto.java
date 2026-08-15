package cl.dgt.tramites.web.dto;

/**
 * Lo que la DGT cuenta de un trámite.
 *
 * <p>Un record, no la entidad. Fíjate en lo que NO viaja: ni el contribuyente completo
 * (con su puntaje de riesgo), ni las líneas del F29. Solo el RUT, que es público para
 * quien ya conoce el trámite.
 */
public record TramiteDto(Long id, String tipo, String estado, String rutContribuyente) {
}
