package cl.dgt.tramites.web.dto;

/**
 * El total del F29 de un trámite después de declarar una línea.
 *
 * <p>RN-06: el total es DERIVADO. No existe columna que lo guarde, así que este número se calcula
 * al momento y viaja como dato, no como entidad.
 */
public record TotalF29Dto(Long tramiteId, long total) {
}
