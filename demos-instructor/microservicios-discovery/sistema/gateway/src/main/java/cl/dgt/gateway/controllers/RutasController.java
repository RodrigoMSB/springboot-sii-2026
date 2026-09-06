package cl.dgt.gateway.controllers;

import cl.dgt.gateway.enrutado.TablaDeRutas;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

/**
 * El instrumento del BLOQUE 3: enseña la tabla de rutas TAL COMO ESTÁ AHORA.
 *
 * <p>Existe por una razón muy concreta de sala. Sin él, «el gateway recogió el
 * cambio de configuración» hay que deducirlo de que una petición empezó a fallar
 * — y deducir no es ver. Con él, el antes y el después del `POST /actuator/refresh`
 * son dos `curl` que devuelven dos JSON distintos, proyectados uno debajo del otro.
 *
 * <p>No existe en el laboratorio y es una de las diferencias declaradas con él.
 */
@RestController
public class RutasController {

    private final TablaDeRutas tabla;

    // `TablaDeRutas` está en `@RefreshScope`, así que esto es un proxy: cada
    // llamada lee la tabla vigente, no la que había cuando se creó este bean.
    public RutasController(TablaDeRutas tabla) {
        this.tabla = tabla;
    }

    @GetMapping("/rutas")
    public Map<String, List<Map<String, String>>> rutas() {
        return Map.of("rutas", tabla.todas().stream()
                .map(r -> Map.of("prefijo", r.prefijo(),
                                 "servicio", r.servicio(),
                                 "destino", r.destino()))
                .toList());
    }
}
