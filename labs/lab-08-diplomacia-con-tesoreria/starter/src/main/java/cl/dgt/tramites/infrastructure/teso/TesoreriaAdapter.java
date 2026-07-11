package cl.dgt.tramites.infrastructure.teso;

import cl.dgt.tramites.application.ConfirmacionPago;
import cl.dgt.tramites.application.TesoreriaPort;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

/**
 * Cliente de TESO INGENUO: un {@code RestClient} SIN TIMEOUT, imperativo, que no traduce el fallo.
 *
 * <p>Con un usuario y TESO sano, funciona. Cuando TESO se cuelga, este hilo se cuelga con él —para
 * siempre—, y con el pool lleno de hilos colgados, la API entera muere. Ese es el crimen:
 *   · TODO_1: falta el timeout (connect + read cortos).
 *   · TODO_2: falta traducir el fallo a una excepción de dominio (para dar un 503 elegante).
 *   · TODO_3: esto debería ser un cliente declarativo (@HttpExchange), no este código a mano.
 */
@Component
public class TesoreriaAdapter implements TesoreriaPort {

    private final RestClient restClient;

    public TesoreriaAdapter(@Value("${dgt.teso.base-url}") String baseUrl) {
        // Sin requestFactory con timeouts: la espera es infinita. Ahí vive el rehén.
        this.restClient = RestClient.builder().baseUrl(baseUrl).build();
    }

    @Override
    public ConfirmacionPago confirmarPago(String referencia) {
        // Sin try/catch: si TESO falla, la RestClientException sube cruda y termina en un 500.
        return restClient.get().uri("/pagos/{referencia}", referencia)
                .retrieve()
                .body(ConfirmacionPago.class);
    }
}
