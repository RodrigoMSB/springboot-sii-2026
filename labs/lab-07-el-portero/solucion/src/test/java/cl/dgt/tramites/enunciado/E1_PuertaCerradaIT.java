package cl.dgt.tramites.enunciado;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.client.RestTestClient;

/**
 * TODO_1 · Denegar por defecto. Sin token, la puerta está cerrada; lo público es SOLO la lista
 * blanca (health, login, la doc). Una ruta que nadie declaró —como /api/contribuyentes— nace
 * CERRADA: es la prueba de que la lista blanca es de puertas abiertas, no de cerradas.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        properties = "spring.docker.compose.enabled=false")
@Import(BaseSeguridadIT.class)
class E1_PuertaCerradaIT {

    @LocalServerPort int puerto;

    private RestTestClient cliente() {
        return RestTestClient.bindToServer().baseUrl("http://localhost:" + puerto).build();
    }

    @Test
    @DisplayName("sin token, el listado de trámites responde 401")
    void sinTokenElListadoEsRechazado() {
        cliente().get().uri("/api/v1/tramites")
                .exchange()
                .expectStatus().isUnauthorized();
    }

    @Test
    @DisplayName("health es público: responde 200 sin token")
    void healthEsPublico() {
        cliente().get().uri("/actuator/health")
                .exchange()
                .expectStatus().isOk();
    }

    @Test
    @DisplayName("una ruta sin regla explícita nace cerrada (deny by default)")
    void rutaSinReglaNaceCerrada() {
        // /api/contribuyentes no está en la lista blanca ni tiene @PreAuthorize: la atrapa
        // anyRequest().authenticated(). Sin token -> 401. Olvidar una regla CIERRA, no abre.
        cliente().get().uri("/api/contribuyentes/11111111-1")
                .exchange()
                .expectStatus().isUnauthorized();
    }
}
