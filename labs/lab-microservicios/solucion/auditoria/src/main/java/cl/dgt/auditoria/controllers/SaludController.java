package cl.dgt.auditoria.controllers;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

// El "sigo vivo" del paso 1. Uno por servicio, en su propio puerto: cuatro procesos,
// cuatro respuestas.
@RestController
public class SaludController {

    @GetMapping("/salud")
    public Map<String, String> salud() {
        return Map.of("servicio", "auditoria", "estado", "vivo");
    }
}
