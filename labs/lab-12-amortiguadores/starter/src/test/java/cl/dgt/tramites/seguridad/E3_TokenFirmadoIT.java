package cl.dgt.tramites.seguridad;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.client.RestTestClient;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

/**
 * TODO_3 · El validador que no cree. Un token bien firmado pasa; uno ADULTERADO —payload editado,
 * firma vieja— muere con 401. La firma detecta la mentira. Es el contraste directo con el base64
 * del acto 2: allí, editar el payload te ascendía; aquí, te deja fuera.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        properties = "spring.docker.compose.enabled=false")
@Import(BaseSeguridadIT.class)
class E3_TokenFirmadoIT {

    @LocalServerPort int puerto;

    private RestTestClient cliente() {
        return RestTestClient.bindToServer().baseUrl("http://localhost:" + puerto).build();
    }

    @Test
    @DisplayName("un token bien firmado deja ver el listado (200)")
    void tokenValidoPasa() {
        String token = BaseSeguridadIT.login(cliente(), BaseSeguridadIT.IGNACIO, BaseSeguridadIT.CLAVE);
        cliente().get().uri("/api/v1/tramites")
                .header("Authorization", "Bearer " + token)
                .exchange()
                .expectStatus().isOk();
    }

    @Test
    @DisplayName("un token con el payload adulterado (firma vieja) responde 401")
    void tokenAdulteradoEsRechazado() {
        String token = BaseSeguridadIT.login(cliente(), BaseSeguridadIT.IGNACIO, BaseSeguridadIT.CLAVE);
        String[] partes = token.split("\\.");

        // Reemplazamos el payload por uno FALSO —un ascenso a FUNCIONARIO— pero dejamos la firma
        // original. La firma se calculó sobre el payload viejo: ya no cuadra.
        String payloadFalso = Base64.getUrlEncoder().withoutPadding().encodeToString(
                "{\"sub\":\"8765432-1\",\"roles\":[\"FUNCIONARIO\"]}".getBytes(StandardCharsets.UTF_8));
        String adulterado = partes[0] + "." + payloadFalso + "." + partes[2];

        cliente().get().uri("/api/v1/tramites")
                .header("Authorization", "Bearer " + adulterado)
                .exchange()
                .expectStatus().isUnauthorized();
    }
}
