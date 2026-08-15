package cl.dgt.tramites.arquitectura.fixtures.violaciones.web;

import cl.dgt.tramites.domain.entity.Contribuyente;

/** Viola AU-01: reside en un paquete ..web.. y depende de ..domain.entity.. */
public class AU01_WebQueTocaLaEntidad {
    public String razonSocialDe(Contribuyente contribuyente) {
        return contribuyente.getRazonSocial();
    }
}
