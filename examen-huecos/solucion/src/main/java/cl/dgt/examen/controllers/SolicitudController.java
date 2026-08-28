package cl.dgt.examen.controllers;

import cl.dgt.examen.dto.CambioDeEstado;
import cl.dgt.examen.dto.NuevaSolicitud;
import cl.dgt.examen.dto.SolicitudBreve;
import cl.dgt.examen.services.ServicioDeSolicitudes;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.net.URI;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/solicitudes")
public class SolicitudController {

    private final ServicioDeSolicitudes servicio;

    public SolicitudController(ServicioDeSolicitudes servicio) {
        this.servicio = servicio;
    }

    @GetMapping("/conteo")
    public ResponseEntity<Map<String, Object>> conteo(@RequestParam String estado) {
        return ResponseEntity.ok(Map.of(
                "estado", estado,
                "solicitudes", servicio.conteoPorEstado(estado)));
    }

    @GetMapping("/recientes")
    public ResponseEntity<List<SolicitudBreve>> recientes(@RequestParam String estado) {
        return ResponseEntity.ok(servicio.recientesPorEstado(estado));
    }

    @GetMapping("/total")
    public ResponseEntity<Map<String, Object>> total(@RequestParam String estado) {
        BigDecimal total = servicio.totalPorEstado(estado);
        return ResponseEntity.ok(Map.of("estado", estado, "total", total));
    }

    @GetMapping("/ultimas")
    public ResponseEntity<List<SolicitudBreve>> ultimas() {
        return ResponseEntity.ok(servicio.ultimas());
    }

    @PostMapping
    public ResponseEntity<SolicitudBreve> crear(@Valid @RequestBody NuevaSolicitud nueva) {
        SolicitudBreve creada = servicio.crear(nueva);
        return ResponseEntity.created(URI.create("/solicitudes/" + creada.id())).body(creada);
    }

    /** Viene resuelto: es el endpoint sobre el que se prueba el 400. */
    @PutMapping("/{id}/estado")
    public ResponseEntity<SolicitudBreve> cambiarEstado(@PathVariable Long id,
                                                        @Valid @RequestBody CambioDeEstado cambio) {
        return ResponseEntity.ok(servicio.cambiarEstado(id, cambio));
    }
}
