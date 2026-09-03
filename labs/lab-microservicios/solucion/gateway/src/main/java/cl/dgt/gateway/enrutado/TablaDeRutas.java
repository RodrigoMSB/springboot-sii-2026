package cl.dgt.gateway.enrutado;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

// LA TABLA DE RUTAS: qué prefijo va a qué servicio. Tres líneas, y son todo el
// "enrutado dinámico" que este laboratorio necesita.
@Component
public class TablaDeRutas {

    public record Ruta(String prefijo, String destino, String servicio) {
    }

    private final List<Ruta> rutas;

    public TablaDeRutas(@Value("${microservicios.contribuyentes.url}") String contribuyentes,
                        @Value("${microservicios.tramites.url}") String tramites,
                        @Value("${microservicios.auditoria.url}") String auditoria) {
        this.rutas = List.of(
                new Ruta("/contribuyentes", contribuyentes, "contribuyentes"),
                new Ruta("/tramites", tramites, "tramites"),
                new Ruta("/auditoria", auditoria, "auditoria"));
    }

    public Optional<Ruta> para(String ruta) {
        return rutas.stream().filter(r -> ruta.startsWith(r.prefijo())).findFirst();
    }

    public List<Ruta> todas() {
        return rutas;
    }
}
