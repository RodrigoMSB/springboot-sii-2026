package cl.dgt.tramites.application;

import cl.dgt.tramites.application.mapper.ContribuyenteMapper;
import cl.dgt.tramites.domain.exception.ContribuyenteNoEncontradoException;
import cl.dgt.tramites.infrastructure.repository.ContribuyenteRepository;
import cl.dgt.tramites.web.dto.ContribuyenteDto;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Aquí vive Spring, no en {@code domain} (AU-03).
 *
 * <p>El servicio devuelve un DTO, no la entidad: así el controlador nunca la ve, y AU-01
 * y AU-02 se cumplen por construcción y no por disciplina.
 *
 * <p>Inyección por constructor, siempre (AU-06). Un bean inyectado por campo no se puede
 * construir en un test sin levantar un contexto, y eso empuja al alumno a probar con la
 * aplicación entera encendida.
 */
@Service
public class ContribuyenteService {

    private final ContribuyenteRepository repositorio;

    public ContribuyenteService(ContribuyenteRepository repositorio) {
        this.repositorio = repositorio;
    }

    @Transactional(readOnly = true)
    public ContribuyenteDto buscarPorRut(String rut) {
        return repositorio.findByRut(rut)
                .map(ContribuyenteMapper::aDto)
                .orElseThrow(() -> new ContribuyenteNoEncontradoException(rut));
    }
}
