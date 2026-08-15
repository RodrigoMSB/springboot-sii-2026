package cl.dgt.tramites.application.mapper;

import cl.dgt.tramites.domain.entity.Contribuyente;
import cl.dgt.tramites.web.dto.ContribuyenteDto;

/**
 * Mapeo a mano, sin librería (D-003: lo que se esconde no se aprende).
 *
 * <p><strong>Vive en {@code application}, no en {@code web}.</strong> La SPEC-005 §2 lo
 * dibujaba en {@code web/mapper}, pero AU-01 prohíbe que {@code ..web..} dependa de
 * {@code ..domain.entity..} y un mapper entidad↔DTO no puede cumplirlo. Manda la
 * SPEC-000 (jerarquía §1). Ver reporte de discrepancias de la SPEC-005.
 *
 * <p>La aduana ve lo que deja pasar; el controlador solo recibe lo despachado.
 */
public final class ContribuyenteMapper {

    private ContribuyenteMapper() {
    }

    public static ContribuyenteDto aDto(Contribuyente contribuyente) {
        return new ContribuyenteDto(contribuyente.getRut(), contribuyente.getRazonSocial());
    }
}
