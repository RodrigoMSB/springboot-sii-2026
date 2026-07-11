package cl.dgt.tramites.application;

import cl.dgt.tramites.domain.entity.Folio;

/**
 * La respuesta de una emisión: el número, y a qué trámite quedó pegado.
 *
 * <p>Vive en {@code application} (no en {@code web.dto}) por la misma razón que
 * {@code TramiteResumenDto}: es una forma de resultado de la aplicación, y el servicio
 * la produce. El controlador la expone tal cual; jamás sale la entidad {@code Folio}.
 */
public record FolioDto(Long numero, Long tramiteId) {

    public static FolioDto de(Folio folio) {
        return new FolioDto(folio.getNumero(), folio.getTramite().getId());
    }
}
