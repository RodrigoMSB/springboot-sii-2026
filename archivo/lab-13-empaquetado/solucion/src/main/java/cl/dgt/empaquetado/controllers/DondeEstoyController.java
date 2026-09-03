package cl.dgt.empaquetado.controllers;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Arrays;
import java.util.Map;

// El endpoint del paso 4: el mismo jar dice cosas distintas según el entorno.
@RestController
public class DondeEstoyController {

    private final Environment entorno;
    private final String saludo;
    private final String urlTesoreria;

    public DondeEstoyController(Environment entorno,
                                @Value("${lab13.saludo}") String saludo,
                                @Value("${lab13.tesoreria-url}") String urlTesoreria) {
        this.entorno = entorno;
        this.saludo = saludo;
        this.urlTesoreria = urlTesoreria;
    }

    @GetMapping("/donde-estoy")
    public Map<String, Object> dondeEstoy() {
        return Map.of(
                "perfilesActivos", Arrays.asList(entorno.getActiveProfiles()),
                "saludo", saludo,
                "tesoreriaUrl", urlTesoreria,
                "javaVersion", System.getProperty("java.version"));
    }
}
