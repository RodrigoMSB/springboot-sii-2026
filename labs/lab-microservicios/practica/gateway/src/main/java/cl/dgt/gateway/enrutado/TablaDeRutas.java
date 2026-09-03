package cl.dgt.gateway.enrutado;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
public class TablaDeRutas {

    public record Ruta(String prefijo, String destino, String servicio) {
    }

    private final List<Ruta> rutas;

    public TablaDeRutas(@Value("${microservicios.contribuyentes.url}") String contribuyentes,
                        @Value("${microservicios.tramites.url}") String tramites,
                        @Value("${microservicios.auditoria.url}") String auditoria) {
        // TODO paso 6: la tabla de rutas. Hoy está vacía y el gateway no lleva a ninguna parte.
        this.rutas = List.of();
    }

    public Optional<Ruta> para(String ruta) {
        return rutas.stream().filter(r -> ruta.startsWith(r.prefijo())).findFirst();
    }

    public List<Ruta> todas() {
        return rutas;
    }
}
