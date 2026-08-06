package cl.dgt.tramites.application;

import cl.dgt.tramites.domain.entity.Contribuyente;
import cl.dgt.tramites.domain.exception.ContribuyenteNoEncontradoException;
import cl.dgt.tramites.infrastructure.repository.ContribuyenteRepository;
import cl.dgt.tramites.web.dto.FichaContribuyenteDTO;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Arma la ficha. Aquí vive Spring; el controlador solo traduce HTTP.
 *
 * <p>El mapeo entidad → DTO ocurre en esta capa (application), no en la web: AU-01 prohíbe
 * que {@code ..web..} dependa de {@code ..domain.entity..}. La aduana ve la entidad; el
 * controlador solo recibe lo despachado.
 *
 * <p>Inyección por constructor (AU-06): así este servicio se construye en un test con un
 * repositorio de mentira, sin levantar el contenedor entero.
 */
@Service
public class FichaServiceImpl implements FichaService {

    private final ContribuyenteRepository repositorio;

    public FichaServiceImpl(ContribuyenteRepository repositorio) {
        this.repositorio = repositorio;
    }

    @Override
    @Transactional(readOnly = true)
    public FichaContribuyenteDTO fichaDe(String rut) {
        Contribuyente c = repositorio.findByRut(rut)
                .orElseThrow(() -> new ContribuyenteNoEncontradoException(rut));
        // Lista blanca, a mano: solo estos dos campos cruzan la frontera.
        return new FichaContribuyenteDTO(c.getRut(), c.getRazonSocial());
    }
}
