package cl.dgt.tramites.web.controller;

import cl.dgt.tramites.application.ObservacionInternaService;
import cl.dgt.tramites.application.ObservacionInternaVista;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Guardar y recuperar observaciones internas. Existe SOLO en el perfil {@code dev}: es la
 * ventana para ver JPA trabajando, no una API pública.
 *
 * <p>Este archivo YA ESTÁ HECHO y no se toca en el laboratorio. Está aquí para que puedas
 * guardar y recuperar con dos {@code curl} —lo que hace {@code ./bin/91-demo-jpa.sh}— mientras
 * el trabajo de verdad ocurre en la entidad, el repositorio y el servicio.
 *
 * <p>No conoce las entidades: habla en {@code ObservacionInternaVista}, que es lo que AU-01 y
 * AU-02 exigen.
 */
@RestController
@RequestMapping("/api/internal/observaciones")
@Profile("dev")
public class ObservacionInternaController {

    private final ObservacionInternaService servicio;

    public ObservacionInternaController(ObservacionInternaService servicio) {
        this.servicio = servicio;
    }

    /** Lo que llega al guardar. Un record de entrada, no la entidad. */
    public record NuevaObservacion(String rut, String texto, String autor) {
    }

    @PostMapping
    public ResponseEntity<ObservacionInternaVista> guardar(@RequestBody NuevaObservacion nueva) {
        ObservacionInternaVista guardada =
                servicio.guardar(nueva.rut(), nueva.texto(), nueva.autor());
        return ResponseEntity.status(HttpStatus.CREATED).body(guardada);
    }

    @GetMapping
    public ResponseEntity<List<ObservacionInternaVista>> porRut(@RequestParam String rut) {
        return ResponseEntity.ok(servicio.porRut(rut));
    }
}
