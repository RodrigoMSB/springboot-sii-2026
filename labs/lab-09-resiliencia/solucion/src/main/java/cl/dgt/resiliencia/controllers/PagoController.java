package cl.dgt.resiliencia.controllers;

import cl.dgt.resiliencia.services.PagoService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/pagos")
public class PagoController {

    private final PagoService servicio;

    public PagoController(PagoService servicio) {
        this.servicio = servicio;
    }

    @GetMapping("/{id}")
    public Map<String, Object> consultar(@PathVariable String id) {
        return servicio.consultar(id);
    }

    @GetMapping("/estado-circuito")
    public Map<String, Object> estadoCircuito() {
        return servicio.metricas();
    }
}
