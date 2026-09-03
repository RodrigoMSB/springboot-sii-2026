package cl.dgt.resiliencia.services;

import cl.dgt.resiliencia.tesoreria.ClienteTesoreria;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Map;
import java.util.function.Supplier;

@Service
public class PagoService {

    private static final Logger log = LoggerFactory.getLogger(PagoService.class);

    private final ClienteTesoreria cliente;
    private final CircuitBreaker circuito;

    public PagoService(ClienteTesoreria cliente) {
        this.cliente = cliente;

        CircuitBreakerConfig configuracion = CircuitBreakerConfig.custom()
                .slidingWindowType(CircuitBreakerConfig.SlidingWindowType.COUNT_BASED)
                .slidingWindowSize(5)
                .minimumNumberOfCalls(5)
                // Con 3 de 5 fallando, el circuito abre. Los valores por defecto piden 100
                // llamadas: en una sesión de tres horas nunca abriría.
                .failureRateThreshold(50)
                .waitDurationInOpenState(Duration.ofSeconds(10))
                .permittedNumberOfCallsInHalfOpenState(2)
                .build();

        this.circuito = CircuitBreaker.of("tesoreria", configuracion);

        // Los cambios de estado salen por consola: son el contenido del paso 3.
        circuito.getEventPublisher().onStateTransition(e ->
                log.info(">>> CIRCUITO {} -> {}",
                        e.getStateTransition().getFromState(), e.getStateTransition().getToState()));
    }

    /** Paso 4: si no hay respuesta, se responde igual — con lo que se sabe. */
    public Map<String, Object> consultar(String id) {
        Supplier<Map<String, Object>> protegida =
                CircuitBreaker.decorateSupplier(circuito, () -> cliente.consultarPago(id));
        try {
            return protegida.get();
        } catch (Exception e) {
            log.warn("Tesorería no respondió ({}). Se degrada.", e.getClass().getSimpleName());
            return Map.of("estado", "DESCONOCIDO", "id", id,
                    "aviso", "Tesorería no responde; el trámite sigue su curso");
        }
    }

    public String estadoDelCircuito() {
        return circuito.getState().name();
    }

    public Map<String, Object> metricas() {
        CircuitBreaker.Metrics m = circuito.getMetrics();
        return Map.of(
                "estado", circuito.getState().name(),
                "llamadasFallidas", m.getNumberOfFailedCalls(),
                "llamadasExitosas", m.getNumberOfSuccessfulCalls(),
                "tasaDeFallo", m.getFailureRate(),
                "llamadasHTTPReales", cliente.llamadasHechas());
    }
}
