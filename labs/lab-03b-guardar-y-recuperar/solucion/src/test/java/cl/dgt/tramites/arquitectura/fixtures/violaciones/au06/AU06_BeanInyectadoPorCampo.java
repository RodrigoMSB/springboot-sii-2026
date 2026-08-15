package cl.dgt.tramites.arquitectura.fixtures.violaciones.au06;

import cl.dgt.tramites.infrastructure.repository.ContribuyenteRepository;
import org.springframework.beans.factory.annotation.Autowired;

/** Viola AU-06: inyección por campo. No se construye sin levantar el contexto entero. */
public class AU06_BeanInyectadoPorCampo {

    @Autowired
    private ContribuyenteRepository repositorio;

    public ContribuyenteRepository getRepositorio() { return repositorio; }
}
