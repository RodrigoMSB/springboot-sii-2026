package cl.dgt.tramites.clientes;

import cl.dgt.tramites.infra.FiltroDeCorrelacion;
import io.github.resilience4j.circuitbreaker.CallNotPermittedException;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;

import java.net.http.HttpClient;
import java.time.Duration;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;

// Lo que en un monolito era `contribuyenteRepository.findByRut(rut)` — nanosegundos, siempre
// disponible, dentro de la misma transacción. Aquí es una llamada de red a otro proceso.
@Component
public class ClienteContribuyentes {

    private static final Logger log = LoggerFactory.getLogger(ClienteContribuyentes.class);

    private static final Duration TIMEOUT = Duration.ofSeconds(2);

    private final RestClient http;
    private final CircuitBreaker circuito;
    // Cuenta las llamadas que SALEN de verdad a la red. Con el circuito abierto no sube.
    private final AtomicInteger llamadasReales = new AtomicInteger();

    public record FichaDto(String rut, String nombre, String segmento) {
    }

    public ClienteContribuyentes(@Value("${microservicios.contribuyentes.url}") String url) {
        JdkClientHttpRequestFactory fabrica =
                new JdkClientHttpRequestFactory(HttpClient.newBuilder().connectTimeout(TIMEOUT).build());
        fabrica.setReadTimeout(TIMEOUT);
        this.http = RestClient.builder().baseUrl(url).requestFactory(fabrica).build();

        // Umbrales de SALA, no de producción: con los de fábrica hacen falta 100 llamadas
        // antes de que el circuito llegue a opinar, y aquí no hay cien llamadas.
        this.circuito = CircuitBreaker.of("contribuyentes", CircuitBreakerConfig.custom()
                .slidingWindowType(CircuitBreakerConfig.SlidingWindowType.COUNT_BASED)
                .slidingWindowSize(4)
                .minimumNumberOfCalls(3)
                .failureRateThreshold(50)
                .waitDurationInOpenState(Duration.ofSeconds(10))
                .permittedNumberOfCallsInHalfOpenState(1)
                .ignoreExceptions(HttpClientErrorException.class)
                .build());

        circuito.getEventPublisher().onStateTransition(e ->
                log.warn("[CIRCUITO] {} -> {}",
                        e.getStateTransition().getFromState(), e.getStateTransition().getToState()));
    }

    /**
     * La ficha del contribuyente, o vacío si el servicio no pudo contestar.
     * Devolver `Optional` y no lanzar es la decisión: quien llama tiene que elegir qué hacer.
     */
    public Optional<FichaDto> ficha(String rut) {
        try {
            return Optional.ofNullable(circuito.executeCallable(() -> pedir(rut)));
        } catch (CallNotPermittedException e) {
            // El circuito está ABIERTO: ni se intentó. Esto no cuesta una llamada de red.
            log.warn("[TRAMITES] circuito ABIERTO: no llamo a contribuyentes por {}", rut);
            return Optional.empty();
        } catch (Exception e) {
            log.warn("[TRAMITES] contribuyentes no contestó por {}: {}", rut, e.getClass().getSimpleName());
            return Optional.empty();
        }
    }

    private FichaDto pedir(String rut) {
        llamadasReales.incrementAndGet();
        log.info("[TRAMITES] pido la ficha de {} a contribuyentes", rut);
        return http.get()
                .uri("/contribuyentes/{rut}", rut)
                // El id de correlación viaja en la cabecera: sin esta línea, la petición
                // aparece en el log del otro servicio con un id distinto.
                .header(FiltroDeCorrelacion.CABECERA, MDC.get(FiltroDeCorrelacion.CLAVE))
                .retrieve()
                .body(FichaDto.class);
    }

    public Map<String, Object> estadoDelCircuito() {
        CircuitBreaker.Metrics m = circuito.getMetrics();
        return Map.of(
                "circuito", circuito.getState().name(),
                "llamadasHttpReales", llamadasReales.get(),
                "llamadasEnLaVentana", m.getNumberOfBufferedCalls(),
                "fallidas", m.getNumberOfFailedCalls(),
                "tasaDeFallo", m.getFailureRate());
    }
}
