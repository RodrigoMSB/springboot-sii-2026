package cl.dgt.tramites.web.controller;

import cl.dgt.tramites.application.TramiteService;
import cl.dgt.tramites.web.dto.TramiteDto;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** El primer endpoint que escribe el alumno. Devuelve DTO; jamás la entidad. */
@RestController
@RequestMapping("/api/tramites")
public class TramiteController {

    private final TramiteService servicio;

    public TramiteController(TramiteService servicio) {
        this.servicio = servicio;
    }

    @GetMapping("/{id}")
    public ResponseEntity<TramiteDto> porId(@PathVariable Long id) {
        return ResponseEntity.ok(servicio.buscarPorId(id));
    }
}
