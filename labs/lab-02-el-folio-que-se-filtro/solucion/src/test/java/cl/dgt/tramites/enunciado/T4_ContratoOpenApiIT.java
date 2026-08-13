package cl.dgt.tramites.enunciado;

import cl.dgt.tramites.PostgresEmbebido;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.client.RestTestClient;

/**
 * TODO_4 · El contrato es visible y está versionado.
 *
 * <p>springdoc genera la especificación OpenAPI a partir de tus anotaciones. Este test la
 * pide y comprueba dos cosas: que el endpoint de la ficha está documentado bajo su ruta
 * versionada {@code /api/v1/}, y que su descripción menciona la promesa de RN-03.
 *
 * <p>El versionado nativo no es un prefijo cualquiera: {@code /api/v1/} es un contrato con
 * quien consume la API. El día que cambie la forma de la ficha, nace {@code /api/v2/} y el
 * v1 sigue respondiendo a quien no migró.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        properties = "dgt.base-embebida.enabled=false")
class T4_ContratoOpenApiIT {

    @DynamicPropertySource
    static void baseDeDatos(DynamicPropertyRegistry registro) {
        // Una base recién creada para ESTE contexto: se pide una sola vez y se guarda.
        String url = PostgresEmbebido.nuevaBase();
        registro.add("spring.datasource.url", () -> url);
        registro.add("spring.datasource.username", PostgresEmbebido::usuario);
        registro.add("spring.datasource.password", PostgresEmbebido::clave);
    }

    @LocalServerPort
    private int puerto;

    private RestTestClient cliente() {
        return RestTestClient.bindToServer().baseUrl("http://localhost:" + puerto).build();
    }

    @Test
    @DisplayName("La spec OpenAPI documenta el endpoint de la ficha, versionado en /api/v1/")
    void laSpecDocumentaElEndpointVersionado() {
        cliente().get().uri("/v3/api-docs")
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.paths['/api/v1/contribuyentes/{rut}/ficha']").exists()
                .jsonPath("$.paths['/api/v1/contribuyentes/{rut}/ficha'].get.summary").exists();
    }

    @Test
    @DisplayName("Swagger UI responde")
    void swaggerUiResponde() {
        cliente().get().uri("/swagger-ui/index.html")
                .exchange()
                .expectStatus().isOk();
    }
}
