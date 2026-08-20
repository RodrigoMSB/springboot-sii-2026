package cl.dgt.tramites.services;

import cl.dgt.tramites.entities.Tramite;
import cl.dgt.tramites.repositories.TramiteRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Service
public class TramiteService {

    private static final Logger log = LoggerFactory.getLogger(TramiteService.class);

    private final TramiteRepository repositorio;

    public TramiteService(TramiteRepository repositorio) {
        this.repositorio = repositorio;
    }

    public record TramiteDto(Long id, String tipo, String estado, String rutContribuyente,
                             String nombreContribuyente, String estadoDelNombre, Instant creadoEn) {
    }

    public Optional<TramiteDto> porId(Long id) {
        return repositorio.findById(id).map(this::conNombre);
    }

    public List<TramiteDto> listar() {
        return repositorio.findAll().stream().map(this::conNombre).toList();
    }

    public TramiteDto crear(String rutContribuyente, String tipo) {
        Tramite tramite = repositorio.save(new Tramite(rutContribuyente, tipo));
        log.info("[TRAMITES] trámite {} creado para {}", tramite.getId(), rutContribuyente);
        return conNombre(tramite);
    }

    private TramiteDto conNombre(Tramite tramite) {
        return new TramiteDto(tramite.getId(), tramite.getTipo(), tramite.getEstado(),
                tramite.getRutContribuyente(), null, "NO_CONSULTADO", tramite.getCreadoEn());
    }
}
