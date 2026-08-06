package cl.dgt.tramites.enunciado;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.context.annotation.Bean;
import org.springframework.test.web.servlet.client.RestTestClient;
import org.testcontainers.postgresql.PostgreSQLContainer;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * TODO_1 · La ficha expone SOLO la lista blanca. Contra base real.
 *
 * <p>Comercial Andina (`12345678-5`) tiene `puntajeRiesgoInterno = 67` en la semilla. Si la
 * ficha devuelve la entidad, ese 67 aparece en el JSON y este test lo ve.
 *
 * <p>Se exige lo PERMITIDO (`containsOnlyKeys`), no se enumera lo prohibido. Un campo nuevo
 * y sensible en la entidad no puede colarse por descuido: tendría que agregarse aquí a mano.
 * Es la lección A-01 heredada del tronco: no confundir "oculté los campos que recuerdo" con
 * "solo dejo salir los que decidí".
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        properties = "spring.docker.compose.enabled=false")
class T1_FichaSinDatosSensiblesIT {

    @TestConfiguration(proxyBeanMethods = false)
    static class BaseDeDatos {
        @Bean
        @ServiceConnection
        PostgreSQLContainer postgres() {
            return new PostgreSQLContainer("postgres:16-alpine3.24");
        }
    }

    @LocalServerPort
    private int puerto;

    private RestTestClient cliente() {
        return RestTestClient.bindToServer().baseUrl("http://localhost:" + puerto).build();
    }

    @Test
    @DisplayName("La ficha de un contribuyente NO lleva el puntaje de riesgo interno")
    @SuppressWarnings("unchecked")
    void laFichaSoloLlevaLaListaBlanca() {
        Map<String, Object> ficha = cliente().get().uri("/api/v1/contribuyentes/12345678-5/ficha")
                .exchange()
                .expectStatus().isOk()
                .expectBody(Map.class)
                .returnResult()
                .getResponseBody();

        assertThat(ficha)
                .as("RN-03: la ficha expone exactamente rut y razonSocial, nada más")
                .containsOnlyKeys("rut", "razonSocial")
                .containsEntry("rut", "12345678-5");
    }

    @Test
    @DisplayName("Un RUT inexistente responde 404 con ProblemDetail")
    void rutInexistenteDevuelve404() {
        cliente().get().uri("/api/v1/contribuyentes/99999999-9/ficha")
                .exchange()
                .expectStatus().isNotFound()
                .expectBody()
                .jsonPath("$.title").isEqualTo("Contribuyente no encontrado");
    }
}
