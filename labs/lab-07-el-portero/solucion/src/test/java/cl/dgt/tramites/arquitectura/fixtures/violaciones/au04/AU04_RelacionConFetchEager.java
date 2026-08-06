package cl.dgt.tramites.arquitectura.fixtures.violaciones.au04;

import cl.dgt.tramites.domain.entity.Contribuyente;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;

/**
 * Viola AU-04: el @ManyToOne no declara fetch, y el default de JPA es EAGER.
 * Es exactamente el campo que hace tardar once segundos al listado del Lab 05.
 *
 * Deliberadamente NO lleva @Entity: si la llevara, Hibernate la escanearía y
 * `ddl-auto: validate` fallaría buscando su tabla. AU-04 juzga el CAMPO, no la clase.
 */
public class AU04_RelacionConFetchEager {

    @ManyToOne                                   // sin fetch = LAZY: EAGER por omisión
    @JoinColumn(name = "contribuyente_id")
    private Contribuyente contribuyente;

    public Contribuyente getContribuyente() { return contribuyente; }
}
