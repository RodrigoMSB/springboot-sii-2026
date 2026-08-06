package cl.dgt.tramites.enunciado;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.client.RestTestClient;

import java.util.Map;

/**
 * TODO_4 · Cada rol a su puerta (@PreAuthorize). Emitir folios es acto de FUNCIONARIO:
 *   · Carolina (FUNCIONARIO) emite  -> 201
 *   · Valentina (CONTRIBUYENTE) lo intenta -> 403 (tiene credencial, pero no para esta bóveda)
 *   · Ignacio (FISCALIZADOR) lee el listado -> 200
 *
 * <p>401 vs 403: Valentina NO recibe 401 (sí está autenticada); recibe 403 (autenticada, pero sin
 * el rol). La diferencia entre "no tienes credencial" y "tu credencial no abre esta puerta".
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        properties = "spring.docker.compose.enabled=false")
@Import(BaseSeguridadIT.class)
class E4_RolesIT {

    @LocalServerPort int puerto;

    private RestTestClient cliente() {
        return RestTestClient.bindToServer().baseUrl("http://localhost:" + puerto).build();
    }

    private String bearer(String rut) {
        return "Bearer " + BaseSeguridadIT.login(cliente(), rut, BaseSeguridadIT.CLAVE);
    }

    @SuppressWarnings("unchecked")
    private Long crearTramite(String tokenBearer) {
        Map<String, Object> creado = cliente().post().uri("/api/v1/tramites")
                .header("Authorization", tokenBearer)
                .contentType(MediaType.APPLICATION_JSON)
                .body(Map.of("rutContribuyente", BaseSeguridadIT.VALENTINA, "tipo", "DECLARACION_F29"))
                .exchange()
                .expectStatus().isCreated()
                .expectBody(Map.class)
                .returnResult()
                .getResponseBody();
        return ((Number) creado.get("id")).longValue();
    }

    @Test
    @DisplayName("Carolina (FUNCIONARIO) emite un folio: 201")
    void funcionarioEmite() {
        String carolina = bearer(BaseSeguridadIT.CAROLINA);
        Long tramiteId = crearTramite(carolina);
        cliente().post().uri("/api/v1/tramites/" + tramiteId + "/folio")
                .header("Authorization", carolina)
                .exchange()
                .expectStatus().isCreated();
    }

    @Test
    @DisplayName("Valentina (CONTRIBUYENTE) intenta emitir: 403, no 401")
    void contribuyenteNoEmite() {
        String carolina = bearer(BaseSeguridadIT.CAROLINA);
        Long tramiteId = crearTramite(carolina);   // el trámite existe; el rol es lo que falla
        cliente().post().uri("/api/v1/tramites/" + tramiteId + "/folio")
                .header("Authorization", bearer(BaseSeguridadIT.VALENTINA))
                .exchange()
                .expectStatus().isForbidden();
    }

    @Test
    @DisplayName("Ignacio (FISCALIZADOR) lee el listado: 200")
    void fiscalizadorLee() {
        cliente().get().uri("/api/v1/tramites")
                .header("Authorization", bearer(BaseSeguridadIT.IGNACIO))
                .exchange()
                .expectStatus().isOk();
    }
}
