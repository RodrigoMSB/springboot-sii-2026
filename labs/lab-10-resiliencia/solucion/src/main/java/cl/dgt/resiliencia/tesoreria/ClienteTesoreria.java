package cl.dgt.resiliencia.tesoreria;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.net.http.HttpClient;
import java.time.Duration;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

@Component
public class ClienteTesoreria {

    // Cuánto se espera por la conexión y por la respuesta. El paso 2 los baja de «infinito» a esto.
    private static final Duration TIMEOUT_CONEXION = Duration.ofSeconds(2);
    private static final Duration TIMEOUT_LECTURA = Duration.ofSeconds(2);

    private final RestClient http;
    private final AtomicInteger llamadas = new AtomicInteger();

    public ClienteTesoreria(@Value("${lab09.tesoreria.puerto}") int puerto) {
        // Transporte FIJADO a propósito: WireMock arrastra Apache HttpClient 5, Spring lo
        // preferiría por classpath, y Apache reintenta solo. Este lab mide llamadas y tiempos
        // (D-025-3).
        JdkClientHttpRequestFactory fabrica = new JdkClientHttpRequestFactory(
                HttpClient.newBuilder().connectTimeout(TIMEOUT_CONEXION).build());
        fabrica.setReadTimeout(TIMEOUT_LECTURA);

        this.http = RestClient.builder()
                .baseUrl("http://localhost:" + puerto)
                .requestFactory(fabrica)
                .build();
    }

    @SuppressWarnings("unchecked")
    public Map<String, Object> consultarPago(String id) {
        llamadas.incrementAndGet();
        return http.get().uri("/pagos/{id}", id).retrieve().body(Map.class);
    }

    public int llamadasHechas() {
        return llamadas.get();
    }

    public void reiniciarContador() {
        llamadas.set(0);
    }
}
