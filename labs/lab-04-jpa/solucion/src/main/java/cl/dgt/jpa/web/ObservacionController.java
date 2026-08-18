package cl.dgt.jpa.web;

import cl.dgt.jpa.entities.Observacion;
import cl.dgt.jpa.repositories.ObservacionRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/observaciones")
public class ObservacionController {

    private final ObservacionRepository repositorio;

    public ObservacionController(ObservacionRepository repositorio) {
        this.repositorio = repositorio;
    }

    @GetMapping
    public List<Observacion> listar(@RequestParam(required = false) String autor) {
        return (autor == null) ? repositorio.findAll() : repositorio.findByAutor(autor);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Observacion> porId(@PathVariable Long id) {
        return repositorio.findById(id)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<Observacion> crear(@RequestBody Observacion nueva) {
        return ResponseEntity.status(HttpStatus.CREATED).body(repositorio.save(nueva));
    }
}
