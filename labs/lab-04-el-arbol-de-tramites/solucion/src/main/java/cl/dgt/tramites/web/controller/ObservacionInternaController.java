package cl.dgt.tramites.web.controller;

import cl.dgt.tramites.application.ObservacionInternaService;
import cl.dgt.tramites.application.ObservacionInternaVista;
import org.springframework.context.annotation.Profile;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Consulta interna de observaciones. Existe SOLO en el perfil {@code dev}: es una herramienta
 * de laboratorio, no una API pública.
 *
 * <p>El controlador no sabe de dónde salen las observaciones, y ese es el punto: durante el
 * Lab 3.5 cambia el mecanismo por completo —de JDBC a mano a un repositorio JPA— y este
 * archivo no se toca. Tampoco conoce las entidades: recibe {@code ObservacionInternaVista},
 * que es lo que AU-01 y AU-02 exigen.
 */
@RestController
@RequestMapping("/api/internal/observaciones")
@Profile("dev")
public class ObservacionInternaController {

    private final ObservacionInternaService servicio;

    public ObservacionInternaController(ObservacionInternaService servicio) {
        this.servicio = servicio;
    }

    @GetMapping
    public ResponseEntity<List<ObservacionInternaVista>> porRut(@RequestParam String rut) {
        return ResponseEntity.ok(servicio.porRut(rut));
    }
}
