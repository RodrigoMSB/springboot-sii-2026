package cl.dgt.gateway.enrutado;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

// LA TABLA DE RUTAS: qué prefijo va a qué servicio. Tres líneas, y son todo el
// "enrutado dinámico" que este laboratorio necesita.
//
// =============================================================================
//  @RefreshScope — el bloque 3 de la demostración, en una anotación
// =============================================================================
//  Sin ella, este bean se construye UNA VEZ al arrancar y se queda con las tres
//  URLs que había entonces. Cambiar `config-repo/gateway.yml` no tendría ningún
//  efecto hasta reiniciar el gateway.
//
//  Con ella, el bean que Spring inyecta en `Enrutador` no es este objeto sino un
//  PROXY. En cada llamada el proxy mira si hubo un refresco; si lo hubo, tira el
//  objeto viejo y construye uno nuevo leyendo las propiedades otra vez. Por eso
//  el cambio se ve en la siguiente petición y no hace falta reiniciar nada.
//
//  Y por eso mismo NO todo se puede refrescar: `server.port` no está en ningún
//  bean con `@RefreshScope` —lo usa el servidor web al arrancar—, así que
//  cambiarlo en el Config Server no mueve el puerto. Eso sí necesita reinicio, y
//  el README lo dice con esas palabras.
// =============================================================================
@Component
@RefreshScope
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
