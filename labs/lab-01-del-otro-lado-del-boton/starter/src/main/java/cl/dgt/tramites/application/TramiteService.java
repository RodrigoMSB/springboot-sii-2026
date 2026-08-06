package cl.dgt.tramites.application;

import cl.dgt.tramites.application.mapper.TramiteMapper;
import cl.dgt.tramites.domain.exception.TramiteNoEncontradoException;
import cl.dgt.tramites.infrastructure.repository.TramiteRepository;
import cl.dgt.tramites.web.dto.TramiteDto;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Devuelve DTO, nunca la entidad: así el controlador no puede filtrarla (AU-02). */
@Service
public class TramiteService {

    private final TramiteRepository repositorio;

    public TramiteService(TramiteRepository repositorio) {
        this.repositorio = repositorio;
    }

    @Transactional(readOnly = true)
    public TramiteDto buscarPorId(Long id) {
        return repositorio.findById(id)
                .map(TramiteMapper::aDto)
                .orElseThrow(() -> new TramiteNoEncontradoException(id));
    }
}
