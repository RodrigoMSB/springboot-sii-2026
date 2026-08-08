package cl.dgt.tramites.config;

import cl.dgt.tramites.infrastructure.teso.TesoreriaClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.support.RestClientAdapter;
import org.springframework.web.service.invoker.HttpServiceProxyFactory;

import java.time.Duration;

/**
 * El cliente de TESO, con el candado que faltaba: los TIMEOUTS.
 *
 * <p><strong>Por qué dos timeouts, y por qué cortos.</strong> Un timeout es un presupuesto de
 * espera. Sin él, el hilo de la petición es un rehén: espera para siempre a un servicio ajeno, y
 * con el pool lleno de rehenes, la API entera se cae por culpa de TESO. Con timeouts:
 *
 * <ul>
 *   <li><strong>connect ({@value #CONNECT_MS} ms)</strong> — abrir la conexión es instantáneo en
 *       una red sana. Si TESO no acepta la conexión rápido, está caído: no tiene sentido esperar.</li>
 *   <li><strong>read ({@value #READ_MS} ms)</strong> — TESO confirma un pago en decenas de
 *       milisegundos. Si tarda casi un segundo, algo anda mal: fallamos rápido y damos la mala
 *       noticia (503), en vez de colgar el hilo 30 segundos.</li>
 * </ul>
 *
 * <p>Los números son cortos <em>a propósito</em>: es mejor un "no pude, reintenta" en un segundo
 * que un "espera…" de treinta. La mala noticia rápida es buen servicio.
 *
 * <p><strong>SSRF (lectura):</strong> la URL de TESO viene de configuración fija
 * ({@code dgt.teso.base-url}), no de datos del usuario, así que no hay superficie de Server-Side
 * Request Forgery aquí. Si algún día un endpoint aceptara una URL del cliente para llamarla, el
 * cliente HTTP de Boot 4.1 trae mitigaciones (validación de destino) que habría que activar.
 */
@Configuration
public class TesoreriaConfig {

    static final int CONNECT_MS = 500;
    static final int READ_MS = 800;

    private final String baseUrl;

    public TesoreriaConfig(@Value("${dgt.teso.base-url}") String baseUrl) {
        this.baseUrl = baseUrl;
    }

    @Bean
    TesoreriaClient tesoreriaClient() {
        // El candado que faltaba: los timeouts. connect y read por separado, cortos a propósito.
        SimpleClientHttpRequestFactory fabrica = new SimpleClientHttpRequestFactory();
        fabrica.setConnectTimeout(Duration.ofMillis(CONNECT_MS));
        fabrica.setReadTimeout(Duration.ofMillis(READ_MS));

        RestClient restClient = RestClient.builder()
                .baseUrl(baseUrl)
                .requestFactory(fabrica)
                .build();

        HttpServiceProxyFactory proxy = HttpServiceProxyFactory
                .builderFor(RestClientAdapter.create(restClient))
                .build();
        return proxy.createClient(TesoreriaClient.class);
    }
}
