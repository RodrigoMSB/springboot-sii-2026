package cl.dgt.empaquetado.controllers;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Arrays;
import java.util.Map;

// El endpoint del paso 5: la misma imagen dice cosas distintas según el entorno.
@RestController
public class DondeEstoyController {

    private final Environment entorno;
    private final String saludo;
    private final String urlTesoreria;

    public DondeEstoyController(Environment entorno,
                                @Value("${lab12.saludo}") String saludo,
                                @Value("${lab12.tesoreria-url}") String urlTesoreria) {
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
                "javaVersion", System.getProperty("java.version"),
                "enContenedor", System.getenv("LAB12_EN_CONTENEDOR") != null);
    }
}
