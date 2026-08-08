package cl.dgt.tramites.seguridad;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.client.RestTestClient;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * TODO_2 · El login real, contra la tabla usuario (BCrypt de la semilla). Credenciales buenas →
 * un JWT bien formado (tres partes). Credenciales malas → 401, SIN distinguir si falló el usuario
 * o la clave: distinguir le regalaría a un atacante qué RUT existen.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        properties = "spring.docker.compose.enabled=false")
@Import(BaseSeguridadIT.class)
class E2_LoginIT {

    @LocalServerPort int puerto;

    private RestTestClient cliente() {
        return RestTestClient.bindToServer().baseUrl("http://localhost:" + puerto).build();
    }

    @Test
    @DisplayName("credenciales buenas devuelven un JWT de tres partes")
    void loginBuenoDevuelveToken() {
        String token = BaseSeguridadIT.login(cliente(), BaseSeguridadIT.CAROLINA, BaseSeguridadIT.CLAVE);
        assertThat(token)
                .as("un JWT es header.payload.signature")
                .isNotBlank()
                .matches("^[A-Za-z0-9_-]+\\.[A-Za-z0-9_-]+\\.[A-Za-z0-9_-]+$");
    }

    @Test
    @DisplayName("clave incorrecta responde 401 genérico")
    void claveMalaEsRechazada() {
        cliente().post().uri("/api/v1/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .body(Map.of("rut", BaseSeguridadIT.CAROLINA, "clave", "no-es-la-clave"))
                .exchange()
                .expectStatus().isUnauthorized();
    }

    @Test
    @DisplayName("usuario inexistente responde 401 idéntico (no revela qué RUT existen)")
    void usuarioInexistenteEsRechazadoIgual() {
        cliente().post().uri("/api/v1/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .body(Map.of("rut", "00000000-0", "clave", BaseSeguridadIT.CLAVE))
                .exchange()
                .expectStatus().isUnauthorized();
    }
}
