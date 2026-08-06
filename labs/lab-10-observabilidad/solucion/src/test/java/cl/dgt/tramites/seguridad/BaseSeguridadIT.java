package cl.dgt.tramites.seguridad;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Bean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.client.RestTestClient;
import org.testcontainers.postgresql.PostgreSQLContainer;

import java.util.Map;

/**
 * Config compartida de los tests de seguridad: PostgreSQL real (Testcontainers) y el helper de
 * login. La clave de los tres usuarios semilla es {@code dgt-2026} (ver docs/clave-de-laboratorio).
 */
@TestConfiguration(proxyBeanMethods = false)
class BaseSeguridadIT {

    static final String CLAVE = "dgt-2026";
    static final String CAROLINA = "9876543-2";   // FUNCIONARIO
    static final String VALENTINA = "11111111-1";  // CONTRIBUYENTE
    static final String IGNACIO = "8765432-1";     // FISCALIZADOR

    @Bean
    @ServiceConnection
    PostgreSQLContainer postgres() {
        return new PostgreSQLContainer("postgres:16-alpine3.24");
    }

    /** Hace login y devuelve el JWT. Falla el test si el login no da 200. */
    @SuppressWarnings("unchecked")
    static String login(RestTestClient cliente, String rut, String clave) {
        Map<String, Object> cuerpo = cliente.post().uri("/api/v1/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .body(Map.of("rut", rut, "clave", clave))
                .exchange()
                .expectStatus().isOk()
                .expectBody(Map.class)
                .returnResult()
                .getResponseBody();
        return (String) cuerpo.get("token");
    }
}
