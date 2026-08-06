package cl.dgt.tramites.application.mapper;

import cl.dgt.tramites.domain.entity.Tramite;
import cl.dgt.tramites.web.dto.TramiteDto;

/**
 * Vive en {@code application}, no en {@code web}: AU-01 prohíbe que la capa web dependa
 * de {@code ..domain.entity..}, y un mapper entidad↔DTO no puede cumplirlo.
 */
public final class TramiteMapper {

    private TramiteMapper() {
    }

    public static TramiteDto aDto(Tramite tramite) {
        return new TramiteDto(
                tramite.getId(),
                tramite.getTipo(),
                tramite.getEstado().name(),
                tramite.getContribuyente().getRut());
    }
}
