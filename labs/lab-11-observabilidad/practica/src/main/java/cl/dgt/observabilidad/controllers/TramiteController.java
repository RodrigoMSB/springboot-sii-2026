package cl.dgt.observabilidad.controllers;

import cl.dgt.observabilidad.entities.Tramite;
import cl.dgt.observabilidad.repositories.TramiteRepository;
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

    // Paso 3 · pide el MeterRegistry y declara un contador de trámites emitidos.
    public TramiteController(TramiteRepository repositorio) {
        this.repositorio = repositorio;
        // escribe aquí
    }

    public record NuevoTramite(String tipo, String rut) {
    }

    @PostMapping
    public Map<String, Object> emitir(@RequestBody NuevoTramite nuevo) {
        log.info("Emitiendo trámite tipo={} rut={}", nuevo.tipo(), nuevo.rut());
        Tramite guardado = repositorio.save(new Tramite(nuevo.tipo(), nuevo.rut()));
        // Paso 3 · incrementa el contador.
        // escribe aquí
        log.info("Trámite {} emitido", guardado.getId());
        return Map.of("id", guardado.getId(), "tipo", guardado.getTipo());
    }

    @GetMapping
    public List<Tramite> listar() {
        log.info("Listando trámites");
        return repositorio.findAll();
    }
}
