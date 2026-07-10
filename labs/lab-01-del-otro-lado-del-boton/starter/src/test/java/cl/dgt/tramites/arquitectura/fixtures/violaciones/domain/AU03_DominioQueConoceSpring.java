package cl.dgt.tramites.arquitectura.fixtures.violaciones.domain;

import org.springframework.util.StringUtils;

/**
 * Viola AU-03: reside en un paquete ..domain.. y depende de org.springframework..
 * No lleva @Service a propósito: no queremos que el component scan lo levante.
 */
public class AU03_DominioQueConoceSpring {
    public boolean tieneTexto(String s) {
        return StringUtils.hasText(s);
    }
}
