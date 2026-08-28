package cl.dgt.examen.controllers;

import cl.dgt.examen.dto.CambioDeEstado;
import cl.dgt.examen.dto.NuevaSolicitud;
import cl.dgt.examen.dto.SolicitudBreve;
import cl.dgt.examen.services.ServicioDeSolicitudes;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
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
        // =========================================================================
        //  HUECO 09 · Crear una solicitud
        // -------------------------------------------------------------------------
        //  El servicio ya sabe crearla: `servicio.crear(nueva)` la guarda y la devuelve.
        //
        //  Lo que falta es la respuesta. Crear algo no se contesta con un 200 pelado:
        //  se contesta diciendo que se creo y DONDE quedo, con la cabecera que lo dice.
        //  La solicitud creada va en el cuerpo.
        //
        //  ESTA LISTO CUANDO · pasa el test H-09
        // =========================================================================
        throw new UnsupportedOperationException("HUECO 09");
    }

    /** Viene resuelto: es el endpoint sobre el que se prueba el 400. */
    @PutMapping("/{id}/estado")
    public ResponseEntity<SolicitudBreve> cambiarEstado(@PathVariable Long id,
                                                        @Valid @RequestBody CambioDeEstado cambio) {
        return ResponseEntity.ok(servicio.cambiarEstado(id, cambio));
    }
}
