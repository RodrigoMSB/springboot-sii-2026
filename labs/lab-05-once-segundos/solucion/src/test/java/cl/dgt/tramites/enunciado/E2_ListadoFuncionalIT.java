package cl.dgt.tramites.enunciado;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.client.RestTestClient;

/**
 * El listado, funcionalmente. Este test NO mira el costo (eso es E1): mira que la respuesta
 * sea correcta y tenga NUESTRA forma de página, no la de Spring.
 *
 * <p>Pasa igual con el N+1 y con la proyección — porque ambos devuelven lo mismo. Esa es la
 * definición de refactorizar: cambias el costo, no el comportamiento. E1 y E2 juntos lo
 * prueban: E2 dice "hace lo mismo", E1 dice "cuesta menos".
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        properties = "spring.docker.compose.enabled=false")
@Import(BaseRendimientoIT.class)
class E2_ListadoFuncionalIT {

    @LocalServerPort int puerto;

    private RestTestClient cliente() {
        return RestTestClient.bindToServer().baseUrl("http://localhost:" + puerto).build();
    }

    @Test
    @DisplayName("GET /api/v1/tramites devuelve una página con nuestra forma (contenido, totalElementos)")
    void elListadoDevuelveNuestraPagina() {
        cliente().get().uri("/api/v1/tramites?page=0&size=3")
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.contenido").isArray()
                .jsonPath("$.contenido[0].rutContribuyente").exists()
                .jsonPath("$.totalElementos").exists()
                .jsonPath("$.totalPaginas").exists()
                .jsonPath("$.tamano").isEqualTo(3);
    }

    @Test
    @DisplayName("el resumen del listado NO trae el árbol del trámite, solo lo que la tabla pinta")
    void elResumenEsUnaProyeccion() {
        cliente().get().uri("/api/v1/tramites?page=0&size=1")
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                // un resumen tiene id/tipo/estado/rut; NO 'formulario29' ni 'adjuntos'.
                .jsonPath("$.contenido[0].formulario29").doesNotExist()
                .jsonPath("$.contenido[0].adjuntos").doesNotExist();
    }
}
