package cl.dgt.tramites.controllers;

import cl.dgt.tramites.services.TramiteService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/tramites")
public class TramiteController {

    private final TramiteService servicio;

    public TramiteController(TramiteService servicio) {
        this.servicio = servicio;
    }

    public record SolicitudDto(String rutContribuyente, String tipo) {
    }

    @GetMapping
    public List<TramiteService.TramiteDto> listar() {
        return servicio.listar();
    }

    @GetMapping("/{id}")
    public ResponseEntity<TramiteService.TramiteDto> porId(@PathVariable Long id) {
        return servicio.porId(id)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<TramiteService.TramiteDto> crear(@RequestBody SolicitudDto solicitud) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(servicio.crear(solicitud.rutContribuyente(), solicitud.tipo()));
    }
}
