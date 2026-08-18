package cl.dgt.resiliencia.controllers;

import cl.dgt.resiliencia.tesoreria.ClienteTesoreria;
import cl.dgt.resiliencia.tesoreria.TesoreriaSimulada;
import com.github.tomakehurst.wiremock.WireMockServer;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

// El mando a distancia de Tesorería: así se provoca cada escenario del guion sin tocar código.
@RestController
@RequestMapping("/simulador")
public class SimuladorController {

    private final WireMockServer tesoreria;
    private final ClienteTesoreria cliente;

    public SimuladorController(WireMockServer tesoreria, ClienteTesoreria cliente) {
        this.tesoreria = tesoreria;
        this.cliente = cliente;
    }

    @PostMapping("/sana")
    public Map<String, String> sana() {
        TesoreriaSimulada.sana(tesoreria);
        cliente.reiniciarContador();
        return Map.of("tesoreria", "responde normal");
    }

    @PostMapping("/lenta")
    public Map<String, String> lenta(@RequestParam(defaultValue = "30") int segundos) {
        TesoreriaSimulada.lenta(tesoreria, segundos);
        cliente.reiniciarContador();
        return Map.of("tesoreria", "tarda " + segundos + " s en responder");
    }

    @PostMapping("/caida")
    public Map<String, String> caida() {
        TesoreriaSimulada.caida(tesoreria);
        cliente.reiniciarContador();
        return Map.of("tesoreria", "devuelve 500");
    }
}
