package cl.dgt.observabilidad.controllers;

import cl.dgt.observabilidad.infra.MotorDePostgres;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.util.Map;

// El mando a distancia del paso 5: tirar la base y volver a levantarla.
@RestController
@RequestMapping("/simulador")
public class SimuladorController {

    private final MotorDePostgres base;

    public SimuladorController(MotorDePostgres base) {
        this.base = base;
    }

    @PostMapping("/base-caida")
    public Map<String, Object> caer() throws IOException {
        base.caer();
        return Map.of("base", "apagada");
    }

    @PostMapping("/base-sana")
    public Map<String, Object> levantar() throws IOException {
        base.levantar();
        return Map.of("base", "arriba");
    }
}
