package cl.dgt.contribuyentes.controllers;

import cl.dgt.contribuyentes.entities.Contribuyente;
import cl.dgt.contribuyentes.repositories.ContribuyenteRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/contribuyentes")
public class ContribuyenteController {

    private static final Logger log = LoggerFactory.getLogger(ContribuyenteController.class);

    private final ContribuyenteRepository repositorio;

    public ContribuyenteController(ContribuyenteRepository repositorio) {
        this.repositorio = repositorio;
    }

    public record FichaDto(String rut, String nombre, String segmento) {
    }

    @GetMapping
    public List<FichaDto> listar() {
        return repositorio.findAll().stream().map(ContribuyenteController::aFicha).toList();
    }

    @GetMapping("/{rut}")
    public ResponseEntity<FichaDto> porRut(@PathVariable String rut) {
        // Esta línea es la que se mira en el paso 3: aquí ENTRA la petición que salió de trámites.
        log.info("[CONTRIBUYENTES] me piden la ficha de {}", rut);
        return repositorio.findByRut(rut)
                .map(c -> ResponseEntity.ok(aFicha(c)))
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    private static FichaDto aFicha(Contribuyente c) {
        return new FichaDto(c.getRut(), c.getNombre(), c.getSegmento());
    }
}
