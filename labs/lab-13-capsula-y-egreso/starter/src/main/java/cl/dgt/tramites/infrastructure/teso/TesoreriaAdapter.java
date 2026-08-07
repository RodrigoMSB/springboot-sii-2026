package cl.dgt.tramites.infrastructure.teso;

import cl.dgt.tramites.application.ConfirmacionPago;
import cl.dgt.tramites.application.TesoreriaPort;
import cl.dgt.tramites.domain.exception.TesoreriaNoDisponibleException;
import io.github.resilience4j.circuitbreaker.CallNotPermittedException;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;

/**
 * Adapta el cliente declarativo al puerto de la aplicación, y —clave— traduce cualquier fallo de
 * transporte (timeout, conexión rechazada, 5xx de TESO) a una excepción de DOMINIO. Así la
 * aplicación nunca ve una {@code RestClientException}: ve "Tesorería no está disponible", y decide.
 *
 * <p><strong>Y desde el Lab 12, con interruptor automático.</strong> El timeout del Lab 08 impedía
 * que UNA llamada esperara para siempre. No impedía que mil llamadas siguieran golpeando a un TESO
 * que lleva veinte minutos caído — gastando 800 ms cada una y estorbando a quien intenta levantarse.
 * El circuit breaker cuenta los fallos y, pasado el umbral, deja de intentar: falla al instante y
 * sin tocar la red.
 *
 * <p><strong>El fallback es el mismo de siempre, y eso es lo bueno.</strong> Tanto si TESO falló
 * como si el circuito está abierto, la aplicación recibe {@link TesoreriaNoDisponibleException} —
 * la misma que el Lab 08 ya sabe degradar en un 503 con el trámite intacto. El circuito cambió
 * <em>cuánto tarda</em> la mala noticia, no <em>cuál</em> es. Ese es el diseño: la capa de arriba no
 * tiene que aprender un concepto nuevo para beneficiarse de uno.
 */
@Component
public class TesoreriaAdapter implements TesoreriaPort {

    private static final Logger log = LoggerFactory.getLogger(TesoreriaAdapter.class);

    private final TesoreriaClient cliente;
    private final CircuitBreaker circuito;

    public TesoreriaAdapter(TesoreriaClient cliente, CircuitBreaker circuitoTesoreria) {
        this.cliente = cliente;
        this.circuito = circuitoTesoreria;
    }

    @Override
    public ConfirmacionPago confirmarPago(String referencia) {
        try {
            // El circuito envuelve la llamada: cuenta sus fallos y, si está abierto, ni la intenta.
            return circuito.executeCallable(() -> cliente.confirmar(referencia));

        } catch (CallNotPermittedException circuitoAbierto) {
            // Aquí NO se tocó la red. El circuito está abierto y la llamada se rechazó al instante.
            // Es la diferencia que el test mide en milisegundos: fallar rápido en vez de esperar.
            log.warn("Circuito abierto: no llamo a Tesorería (referencia {})", referencia);
            throw new TesoreriaNoDisponibleException(
                    "Tesorería no está disponible: el circuito está abierto (referencia " + referencia + ")",
                    circuitoAbierto);

        } catch (RestClientException fallo) {
            throw new TesoreriaNoDisponibleException(
                    "Tesorería no confirmó el pago a tiempo (referencia " + referencia + ")", fallo);

        } catch (RuntimeException fallo) {
            throw fallo;

        } catch (Exception fallo) {
            // executeCallable declara Exception; la de dominio ya salió por las ramas de arriba.
            throw new TesoreriaNoDisponibleException(
                    "Tesorería falló de forma inesperada (referencia " + referencia + ")", fallo);
        }
    }
}
