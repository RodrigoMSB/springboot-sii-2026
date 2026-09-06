package cl.dgt.tramites.controllers;

import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * El instrumento del BLOQUE 4, y el que convierte una afirmación en una medición.
 *
 * <p>Devuelve lo que ESTE servicio cree que hay en el sistema. La palabra «cree»
 * es la importante: no pregunta al registro en el momento de contestar. Lee la
 * COPIA LOCAL que su cliente de Eureka se baja cada 30 segundos y guarda en
 * memoria.
 *
 * <p>Por eso sirve para medir qué pasa cuando el registro se apaga. Con Eureka
 * muerto, este endpoint sigue contestando la lista completa —porque la tiene en
 * casa— y así se ve, en pantalla y con reloj, que el descubrimiento no es un
 * punto único de fallo. Cuando la lista empiece a vaciarse, será porque la copia
 * caducó, y ese instante también se ve aquí.
 *
 * <p>No existe en el laboratorio: es una diferencia declarada, y está para poder
 * demostrar en vez de afirmar.
 */
@RestController
public class RegistroController {

    private final DiscoveryClient registro;

    public RegistroController(DiscoveryClient registro) {
        this.registro = registro;
    }

    @GetMapping("/a-quien-veo")
    public Map<String, Object> aQuienVeo() {
        Map<String, Object> respuesta = new LinkedHashMap<>();
        respuesta.put("momento", Instant.now().toString());
        respuesta.put("fuente", "la copia local del registro que guarda este proceso");

        Map<String, List<String>> servicios = new LinkedHashMap<>();
        for (String nombre : registro.getServices()) {
            servicios.put(nombre, registro.getInstances(nombre).stream()
                    .map(RegistroController::direccion)
                    .toList());
        }
        respuesta.put("servicios", servicios);
        respuesta.put("cuantos", servicios.size());
        return respuesta;
    }

    private static String direccion(ServiceInstance i) {
        return i.getHost() + ":" + i.getPort();
    }
}
