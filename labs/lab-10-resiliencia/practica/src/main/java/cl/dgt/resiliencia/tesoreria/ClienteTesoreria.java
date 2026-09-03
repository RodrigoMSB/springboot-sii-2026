package cl.dgt.resiliencia.tesoreria;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

@Component
public class ClienteTesoreria {

    private final RestClient http;
    private final AtomicInteger llamadas = new AtomicInteger();

    public ClienteTesoreria(@Value("${lab10.tesoreria.puerto}") int puerto) {
        // Paso 2 · fija el transporte y ponle timeout de conexión y de lectura.
        // escribe aquí
        this.http = RestClient.builder()
                .baseUrl("http://localhost:" + puerto)
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
