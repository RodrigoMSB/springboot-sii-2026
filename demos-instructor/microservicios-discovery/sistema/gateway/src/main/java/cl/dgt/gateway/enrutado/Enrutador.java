package cl.dgt.gateway.enrutado;

import cl.dgt.gateway.infra.FiltroDeCorrelacion;
import io.github.resilience4j.circuitbreaker.CallNotPermittedException;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.cloud.client.loadbalancer.DeferringLoadBalancerInterceptor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestClient;

import java.net.http.HttpClient;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

// El gateway entero. Todo lo que hace cabe aquí: mira la ruta, elige el destino,
// reenvía y devuelve lo que le den. El cliente no sabe que hay tres servicios detrás.
@RestController
public class Enrutador {

    private static final Logger log = LoggerFactory.getLogger(Enrutador.class);

    private static final Duration TIMEOUT = Duration.ofSeconds(5);

    private final TablaDeRutas tabla;
    private final RestClient http;
    // Un circuito POR SERVICIO. Si trámites se cae, contribuyentes sigue pasando:
    // un circuito compartido castigaría a los tres por culpa de uno.
    private final Map<String, CircuitBreaker> circuitos = new HashMap<>();

    // El gateway reenvía a URIs absolutas que arma con la tabla de rutas, así que
    // aquí no hay `baseUrl` que cambiar: lo que cambió está en la tabla
    // (`http://localhost:8211` -> `http://contribuyentes`). Lo único que hay que
    // añadir es el mismo interceptor, para que ese host se resuelva contra el
    // registro en vez de contra el DNS.
    public Enrutador(TablaDeRutas tabla, DeferringLoadBalancerInterceptor balanceador) {
        this.tabla = tabla;

        JdkClientHttpRequestFactory fabrica =
                new JdkClientHttpRequestFactory(HttpClient.newBuilder().connectTimeout(TIMEOUT).build());
        fabrica.setReadTimeout(TIMEOUT);
        this.http = RestClient.builder()
                .requestFactory(fabrica)
                .requestInterceptor(balanceador)
                .build();

        CircuitBreakerConfig config = CircuitBreakerConfig.custom()
                .slidingWindowType(CircuitBreakerConfig.SlidingWindowType.COUNT_BASED)
                .slidingWindowSize(4)
                .minimumNumberOfCalls(3)
                .failureRateThreshold(50)
                .waitDurationInOpenState(Duration.ofSeconds(10))
                .build();
        tabla.todas().forEach(r -> circuitos.put(r.servicio(), CircuitBreaker.of(r.servicio(), config)));
    }

    @RequestMapping("/**")
    public ResponseEntity<byte[]> enrutar(HttpServletRequest peticion,
                                          @RequestBody(required = false) byte[] cuerpo) {
        String ruta = peticion.getRequestURI();
        TablaDeRutas.Ruta destino = tabla.para(ruta).orElse(null);
        if (destino == null) {
            return respuestaDeTexto(HttpStatus.NOT_FOUND,
                    "{\"error\":\"ninguna ruta del gateway atiende " + ruta + "\"}");
        }

        String consulta = peticion.getQueryString();
        String uri = destino.destino() + ruta + (consulta == null ? "" : "?" + consulta);
        log.info("[GATEWAY] {} {} -> {}", peticion.getMethod(), ruta, destino.servicio());

        try {
            return circuitos.get(destino.servicio()).executeCallable(() -> reenviar(peticion, uri, cuerpo));
        } catch (CallNotPermittedException e) {
            log.warn("[GATEWAY] circuito ABIERTO hacia {}: no reenvío", destino.servicio());
            return respuestaDeTexto(HttpStatus.SERVICE_UNAVAILABLE,
                    "{\"error\":\"el servicio " + destino.servicio() + " no está disponible\","
                            + "\"circuito\":\"ABIERTO\"}");
        } catch (Exception e) {
            log.warn("[GATEWAY] {} no contestó: {}", destino.servicio(), e.getClass().getSimpleName());
            return respuestaDeTexto(HttpStatus.SERVICE_UNAVAILABLE,
                    "{\"error\":\"el servicio " + destino.servicio() + " no contestó\","
                            + "\"causa\":\"" + e.getClass().getSimpleName() + "\"}");
        }
    }

    private ResponseEntity<byte[]> reenviar(HttpServletRequest peticion, String uri, byte[] cuerpo) {
        RestClient.RequestBodySpec salida = http
                .method(HttpMethod.valueOf(peticion.getMethod()))
                .uri(uri)
                // El id de correlación cruza la puerta con la petición. Sin esta línea,
                // el paso 7 no tiene nada que enseñar.
                .header(FiltroDeCorrelacion.CABECERA, MDC.get(FiltroDeCorrelacion.CLAVE));

        String tipo = peticion.getContentType();
        if (tipo != null) {
            salida.contentType(MediaType.parseMediaType(tipo));
        }
        if (cuerpo != null && cuerpo.length > 0) {
            salida.body(cuerpo);
        }

        // `exchange` y no `retrieve`: un 404 del servicio de abajo es una respuesta que hay
        // que devolver tal cual, no una excepción del gateway.
        return salida.exchange((solicitud, respuesta) -> ResponseEntity
                .status(respuesta.getStatusCode())
                .contentType(respuesta.getHeaders().getContentType() == null
                        ? MediaType.APPLICATION_JSON : respuesta.getHeaders().getContentType())
                .body(respuesta.getBody().readAllBytes()));
    }

    private static ResponseEntity<byte[]> respuestaDeTexto(HttpStatus estado, String cuerpo) {
        HttpHeaders cabeceras = new HttpHeaders();
        cabeceras.setContentType(MediaType.APPLICATION_JSON);
        return new ResponseEntity<>(cuerpo.getBytes(StandardCharsets.UTF_8), cabeceras, estado);
    }
}
