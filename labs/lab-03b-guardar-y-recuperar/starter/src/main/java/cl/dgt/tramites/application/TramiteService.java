package cl.dgt.tramites.application;

import cl.dgt.tramites.application.mapper.TramiteMapper;
import cl.dgt.tramites.domain.entity.Contribuyente;
import cl.dgt.tramites.domain.tipo.EstadoTramite;
import cl.dgt.tramites.domain.entity.Tramite;
import cl.dgt.tramites.domain.exception.ContribuyenteNoEncontradoException;
import cl.dgt.tramites.domain.exception.TramiteNoEncontradoException;
import cl.dgt.tramites.infrastructure.repository.ContribuyenteRepository;
import cl.dgt.tramites.infrastructure.repository.TramiteRepository;
import cl.dgt.tramites.web.dto.TramiteDto;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Devuelve DTO, nunca la entidad: así el controlador no puede filtrarla (AU-02). */
@Service
public class TramiteService {

    private final TramiteRepository repositorio;
    private final ContribuyenteRepository contribuyentes;

    public TramiteService(TramiteRepository repositorio, ContribuyenteRepository contribuyentes) {
        this.repositorio = repositorio;
        this.contribuyentes = contribuyentes;
    }

    /** Crea un trámite en BORRADOR para el contribuyente del RUT dado. */
    @org.springframework.transaction.annotation.Transactional
    public TramiteDto crear(String rutContribuyente, String tipo) {
        Contribuyente c = contribuyentes.findByRut(rutContribuyente)
                .orElseThrow(() -> new ContribuyenteNoEncontradoException(rutContribuyente));
        Tramite tramite = repositorio.save(new Tramite(c, tipo));
        return TramiteMapper.aDto(tramite);
    }

    /** Avanza el trámite al estado destino. Lanza TransicionIlegalException si no procede. */
    @org.springframework.transaction.annotation.Transactional
    public TramiteDto avanzar(Long id, EstadoTramite destino) {
        Tramite tramite = repositorio.findById(id)
                .orElseThrow(() -> new TramiteNoEncontradoException(id));
        tramite.transicionarA(destino);
        return TramiteMapper.aDto(repositorio.save(tramite));
    }

    @Transactional(readOnly = true)
    public TramiteDto buscarPorId(Long id) {
        return repositorio.findById(id)
                .map(TramiteMapper::aDto)
                .orElseThrow(() -> new TramiteNoEncontradoException(id));
    }
}
