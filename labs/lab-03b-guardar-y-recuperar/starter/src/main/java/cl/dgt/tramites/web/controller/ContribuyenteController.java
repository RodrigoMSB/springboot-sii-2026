package cl.dgt.tramites.web.controller;

import cl.dgt.tramites.application.ContribuyenteService;
import cl.dgt.tramites.web.dto.ContribuyenteDto;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * La única puerta abierta del tronco. Devuelve un DTO, jamás la entidad.
 *
 * <p>Nota deliberada: este controlador <em>no importa</em> {@code Contribuyente}. Ni
 * siquiera dentro de un genérico. AU-02 caza esa filtración incluso cuando vive solo en
 * el parámetro de tipo de {@code ResponseEntity<...>} — verificado por el spike S-1.
 */
@RestController
@RequestMapping("/api/contribuyentes")
public class ContribuyenteController {

    private final ContribuyenteService servicio;

    public ContribuyenteController(ContribuyenteService servicio) {
        this.servicio = servicio;
    }

    @GetMapping("/{rut}")
    public ResponseEntity<ContribuyenteDto> porRut(@PathVariable String rut) {
        return ResponseEntity.ok(servicio.buscarPorRut(rut));
    }
}
