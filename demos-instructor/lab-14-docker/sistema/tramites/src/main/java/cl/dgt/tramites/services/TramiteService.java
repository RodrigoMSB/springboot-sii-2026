package cl.dgt.tramites.services;

import cl.dgt.tramites.clientes.ClienteAuditoria;
import cl.dgt.tramites.clientes.ClienteContribuyentes;
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
    private final ClienteContribuyentes contribuyentes;
    private final ClienteAuditoria auditoria;

    public TramiteService(TramiteRepository repositorio, ClienteContribuyentes contribuyentes,
                          ClienteAuditoria auditoria) {
        this.repositorio = repositorio;
        this.contribuyentes = contribuyentes;
        this.auditoria = auditoria;
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

        // Se avisa y NO se espera. El usuario ya tiene su respuesta.
        auditoria.avisarDeUnTramiteNuevo(tramite);
        return conNombre(tramite);
    }

    // El dato de este servicio y el dato del otro, cosidos aquí. En un monolito esto era
    // un JOIN; aquí, dos orígenes y uno de ellos puede faltar.
    private TramiteDto conNombre(Tramite tramite) {
        return contribuyentes.ficha(tramite.getRutContribuyente())
                .map(f -> nuevoDto(tramite, f.nombre(), "OK"))
                // Degradación MARCADA, no silenciosa: el que consume esto tiene que poder
                // distinguir "no tiene nombre" de "hoy no pude traerlo".
                .orElseGet(() -> nuevoDto(tramite, null, "NO_DISPONIBLE"));
    }

    private static TramiteDto nuevoDto(Tramite t, String nombre, String estadoDelNombre) {
        return new TramiteDto(t.getId(), t.getTipo(), t.getEstado(), t.getRutContribuyente(),
                nombre, estadoDelNombre, t.getCreadoEn());
    }
}
