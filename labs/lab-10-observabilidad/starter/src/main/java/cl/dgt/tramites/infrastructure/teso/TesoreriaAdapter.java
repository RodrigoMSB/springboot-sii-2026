package cl.dgt.tramites.infrastructure.teso;

import cl.dgt.tramites.application.ConfirmacionPago;
import cl.dgt.tramites.application.TesoreriaPort;
import cl.dgt.tramites.domain.exception.TesoreriaNoDisponibleException;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;

/**
 * Adapta el cliente declarativo al puerto de la aplicación, y —clave— traduce cualquier fallo de
 * transporte (timeout, conexión rechazada, 5xx de TESO) a una excepción de DOMINIO. Así la
 * aplicación nunca ve una {@code RestClientException}: ve "Tesorería no está disponible", y decide.
 */
@Component
public class TesoreriaAdapter implements TesoreriaPort {

    private final TesoreriaClient cliente;

    public TesoreriaAdapter(TesoreriaClient cliente) {
        this.cliente = cliente;
    }

    @Override
    public ConfirmacionPago confirmarPago(String referencia) {
        try {
            return cliente.confirmar(referencia);
        } catch (RestClientException e) {
            throw new TesoreriaNoDisponibleException(
                    "Tesorería no confirmó el pago a tiempo (referencia " + referencia + ")", e);
        }
    }
}
