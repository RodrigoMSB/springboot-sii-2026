package cl.dgt.tramites.integracion;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.client.RestTestClient;
import org.testcontainers.postgresql.PostgreSQLContainer;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * TODO_2 · Tu prueba de integración de punta a punta (≈15 min).
 *
 * <p>Este archivo es TUYO: fuera de {@code enunciado/}, el manifiesto no lo toca. El {@code 90}
 * comprueba que existe y pasa. Es tu primera IT completa: servidor real + PostgreSQL real
 * (Testcontainers) + RestTestClient pegándole por HTTP.
 *
 * <p>Prueba, como mínimo, que el listado pagina de verdad: pide dos páginas distintas y verifica
 * que el contenido cambia y que los totales son coherentes.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        properties = "spring.docker.compose.enabled=false")
@Import(ListadoIntegracionTest.BaseDatos.class)
class ListadoIntegracionTest {

    @TestConfiguration(proxyBeanMethods = false)
    static class BaseDatos {
        @Bean @ServiceConnection
        PostgreSQLContainer postgres() { return new PostgreSQLContainer("postgres:16-alpine3.24"); }
    }

    @LocalServerPort int puerto;

    private RestTestClient cliente() {
        return RestTestClient.bindToServer().baseUrl("http://localhost:" + puerto).build();
    }

    @Test
    @DisplayName("dos páginas distintas del listado traen contenido distinto")
    void paginaDeVerdad() {
        var p0 = cliente().get().uri("/api/v1/tramites?page=0&size=2").exchange()
                .expectStatus().isOk().expectBody()
                .jsonPath("$.pagina").isEqualTo(0)
                .jsonPath("$.tamano").isEqualTo(2)
                .returnResult();
        var p1 = cliente().get().uri("/api/v1/tramites?page=1&size=2").exchange()
                .expectStatus().isOk().expectBody()
                .jsonPath("$.pagina").isEqualTo(1)
                .returnResult();
        assertThat(new String(p0.getResponseBodyContent()))
                .as("la página 1 no debe ser idéntica a la 0")
                .isNotEqualTo(new String(p1.getResponseBodyContent()));
    }
}
