package cl.dgt.examen.controllers;

import cl.dgt.examen.dto.FichaOficina;
import cl.dgt.examen.dto.OficinaBreve;
import cl.dgt.examen.dto.ResumenOficina;
import cl.dgt.examen.services.ServicioDeOficinas;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/oficinas")
public class OficinaController {

    private final ServicioDeOficinas servicio;

    public OficinaController(ServicioDeOficinas servicio) {
        this.servicio = servicio;
    }

    /** Viene resuelto. */
    @GetMapping("/{codigo}/ficha")
    public ResponseEntity<FichaOficina> ficha(@PathVariable String codigo) {
        return ResponseEntity.ok(servicio.ficha(codigo));
    }

    @GetMapping("/{codigo}/conteo")
    public ResponseEntity<Map<String, Object>> conteo(@PathVariable String codigo) {
        return ResponseEntity.ok(Map.of(
                "codigo", codigo,
                "solicitudes", servicio.conteoDeSolicitudes(codigo)));
    }

    @GetMapping("/comuna/{comuna}")
    public ResponseEntity<List<FichaOficina>> porComuna(@PathVariable String comuna) {
        return ResponseEntity.ok(servicio.porComuna(comuna));
    }

    @GetMapping("/{codigo}/resumen")
    public ResponseEntity<ResumenOficina> resumen(@PathVariable String codigo) {
        return ResponseEntity.ok(servicio.resumenDe(codigo));
    }

    @GetMapping
    public ResponseEntity<List<OficinaBreve>> todas() {
        return ResponseEntity.ok(servicio.todas());
    }
}
