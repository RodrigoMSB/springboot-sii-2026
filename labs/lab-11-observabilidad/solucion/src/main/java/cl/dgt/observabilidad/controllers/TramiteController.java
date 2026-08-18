package cl.dgt.observabilidad.controllers;

import cl.dgt.observabilidad.entities.Tramite;
import cl.dgt.observabilidad.repositories.TramiteRepository;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/tramites")
public class TramiteController {

    private static final Logger log = LoggerFactory.getLogger(TramiteController.class);

    private final TramiteRepository repositorio;
    private final Counter emitidos;

    public TramiteController(TramiteRepository repositorio, MeterRegistry registro) {
        this.repositorio = repositorio;
        // La métrica de negocio del paso 3. Se declara una vez, aquí.
        this.emitidos = Counter.builder("dgt.tramites.emitidos")
                .description("Trámites emitidos desde que arrancó la aplicación")
                .register(registro);
    }

    public record NuevoTramite(String tipo, String rut) {
    }

    @PostMapping
    public Map<String, Object> emitir(@RequestBody NuevoTramite nuevo) {
        log.info("Emitiendo trámite tipo={} rut={}", nuevo.tipo(), nuevo.rut());
        Tramite guardado = repositorio.save(new Tramite(nuevo.tipo(), nuevo.rut()));
        emitidos.increment();
        log.info("Trámite {} emitido", guardado.getId());
        return Map.of("id", guardado.getId(), "tipo", guardado.getTipo());
    }

    @GetMapping
    public List<Tramite> listar() {
        log.info("Listando trámites");
        return repositorio.findAll();
    }
}
