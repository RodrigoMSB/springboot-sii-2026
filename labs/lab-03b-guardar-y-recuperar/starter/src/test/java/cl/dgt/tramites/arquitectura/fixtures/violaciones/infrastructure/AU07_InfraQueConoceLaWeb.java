package cl.dgt.tramites.arquitectura.fixtures.violaciones.infrastructure;

import cl.dgt.tramites.web.dto.ContribuyenteDto;

/** Viola AU-07: reside en ..infrastructure.. y depende de ..web.. */
public class AU07_InfraQueConoceLaWeb {
    public ContribuyenteDto dto() {
        return new ContribuyenteDto("11111111-1", "Valentina Rojas");
    }
}
