package cl.dgt.gateway.controllers;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
public class SaludController {

    @GetMapping("/salud")
    public Map<String, String> salud() {
        return Map.of("servicio", "gateway", "estado", "vivo");
    }
}
