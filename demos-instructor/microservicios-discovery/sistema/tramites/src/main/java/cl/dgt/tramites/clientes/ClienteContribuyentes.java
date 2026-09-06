package cl.dgt.tramites.clientes;

import cl.dgt.tramites.infra.FiltroDeCorrelacion;
import io.github.resilience4j.circuitbreaker.CallNotPermittedException;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.client.loadbalancer.DeferringLoadBalancerInterceptor;
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

    // =========================================================================
    //  EL BLOQUE 2 DE LA DEMOSTRACIÓN CABE EN ESTE CONSTRUCTOR
    // =========================================================================
    //  En el laboratorio, `url` valía `http://localhost:8211`. Aquí vale
    //  `http://contribuyentes`. Lo único que cambió en el YAML es que
    //  desapareció el puerto — y con él, la máquina.
    //
    //  Pero un `RestClient` normal no sabe qué hacer con eso: intentaría
    //  resolver `contribuyentes` por DNS y fallaría. Lo que traduce el nombre a
    //  una dirección es la línea nueva, `.requestInterceptor(balanceador)`.
    //
    //  Ese interceptor hace tres cosas en cada llamada, y las tres importan:
    //    1. le pide al registro (a la COPIA LOCAL del registro, no al servidor)
    //       las instancias que se llaman `contribuyentes`;
    //    2. ELIGE UNA — si hubiera varias, iría rotando: de ahí lo de
    //       «balanceador», y es lo que una URL fija no puede hacer;
    //    3. reescribe la URI con el host y el puerto de la elegida.
    //
    //  ⚠️  Y hay una trampa que se pagó midiendo, así que queda escrita:
    //      la forma «de manual» de esto es declarar un bean
    //      `@LoadBalanced RestClient.Builder`. NO SE HAGA AQUÍ. Ese bean pasa a
    //      ser el `RestClient.Builder` de TODA la aplicación, y entonces el
    //      cliente de Eureka intenta resolver la dirección de su propio
    //      servidor —`http://localhost:8761`— como si `localhost` fuera un
    //      nombre de servicio:
    //
    //        ERROR ... Exception occurred while retrieving instances for service localhost
    //        WARN  ... registration failed Cannot execute request on any known server
    //
    //      El servicio arranca, y no se registra. Inyectando el interceptor en
    //      ESTE cliente y solo en éste, el resto de la aplicación sigue usando
    //      HTTP normal y el problema no existe.
    // =========================================================================
    public ClienteContribuyentes(@Value("${microservicios.contribuyentes.url}") String url,
                                 DeferringLoadBalancerInterceptor balanceador) {
        JdkClientHttpRequestFactory fabrica =
                new JdkClientHttpRequestFactory(HttpClient.newBuilder().connectTimeout(TIMEOUT).build());
        fabrica.setReadTimeout(TIMEOUT);
        this.http = RestClient.builder()
                .baseUrl(url)
                .requestFactory(fabrica)
                .requestInterceptor(balanceador)
                .build();

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
