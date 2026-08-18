package cl.dgt.resiliencia.tesoreria;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class TesoreriaSimulada {

    // WireMock como librería en este mismo proceso: sin Docker y sin un segundo terminal.
    @Bean(destroyMethod = "stop")
    WireMockServer tesoreria(@Value("${lab09.tesoreria.puerto}") int puerto) {
        WireMockServer servidor = new WireMockServer(WireMockConfiguration.options().port(puerto));
        servidor.start();
        sana(servidor);
        return servidor;
    }

    public static void sana(WireMockServer servidor) {
        servidor.resetAll();
        servidor.stubFor(WireMock.get(WireMock.urlPathMatching("/pagos/.*"))
                .willReturn(WireMock.okJson("{\"estado\":\"PAGADO\",\"monto\":45000}")));
    }

    public static void lenta(WireMockServer servidor, int segundos) {
        servidor.resetAll();
        servidor.stubFor(WireMock.get(WireMock.urlPathMatching("/pagos/.*"))
                .willReturn(WireMock.okJson("{\"estado\":\"PAGADO\",\"monto\":45000}")
                        .withFixedDelay(segundos * 1000)));
    }

    public static void caida(WireMockServer servidor) {
        servidor.resetAll();
        servidor.stubFor(WireMock.get(WireMock.urlPathMatching("/pagos/.*"))
                .willReturn(WireMock.aResponse().withStatus(500)));
    }
}
