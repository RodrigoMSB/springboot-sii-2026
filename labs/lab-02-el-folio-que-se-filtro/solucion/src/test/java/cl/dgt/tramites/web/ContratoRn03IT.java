package cl.dgt.tramites.web;

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
 * RN-03, verificada de punta a punta contra una fila real.
 *
 * <p>El contribuyente `12345678-5` tiene `puntaje_riesgo_interno = 67` en la semilla. Si
 * algún día alguien devuelve la entidad "para no duplicar código", ese 67 aparecerá en el
 * JSON y este test lo verá. Es el guardián dinámico; AU-02 es el estático. Dos guardianes
 * porque el precio de fallar es una filtración, no un bug de formato.
 *
 * <p>Estilo de referencia para los labs: `RestTestClient` contra el servidor real
 * (Spring Framework 7). La rebanada rápida usa `@WebMvcTest` + MockMvc.
 *
 * <p>Testcontainers 2.x: el contenedor vive en `org.testcontainers.postgresql`. Las
 * coordenadas viejas (`org.testcontainers:postgresql`) ya no existen — la mitad del
 * material en línea todavía las muestra. Es contenido del Módulo 6.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        properties = "spring.docker.compose.enabled=false")
class ContratoRn03IT {

    @TestConfiguration(proxyBeanMethods = false)
    static class BaseDeDatosDePrueba {
        @Bean
        @ServiceConnection
        PostgreSQLContainer postgres() {
            // Testcontainers 2 dejó de ser genérico: ya no hay PostgreSQLContainer<?>
            // ni el `new ...<>()` autorreferente de la 1.x.
            return new PostgreSQLContainer("postgres:16-alpine3.24");
        }
    }

    @LocalServerPort
    private int puerto;

    private RestTestClient cliente() {
        return RestTestClient.bindToServer().baseUrl("http://localhost:" + puerto).build();
    }

    /**
     * RN-03 se verifica sobre las CLAVES del JSON, no sobre su texto.
     *
     * <p>Primera versión de este test: {@code assertThat(json).doesNotContain("67")},
     * porque 67 es el puntaje de Comercial Andina. Falló — y no por una filtración: el
     * RUT es `12345678-5`, que contiene "67". Buscar una subcadena confunde la forma del
     * texto con la propiedad que importa. Es el mismo defecto que el ADN llama A-01, y
     * cayó aquí el primer día.
     *
     * <p>La aserción correcta no enumera lo prohibido: exige exactamente lo permitido.
     * Un campo nuevo en la entidad no puede colarse por descuido.
     */
    @Test
    @DisplayName("El JSON expone SOLO rut y razonSocial: nada más puede filtrarse")
    @SuppressWarnings("unchecked")
    void elPuntajeDeRiesgoJamasSaleDeLaApi() {
        Map<String, Object> cuerpo = cliente().get().uri("/api/contribuyentes/12345678-5")
                .exchange()
                .expectStatus().isOk()
                .expectBody(Map.class)
                .returnResult()
                .getResponseBody();

        assertThat(cuerpo)
                .as("RN-03: ni puntajeRiesgoInterno ni claveHash ni ningún campo futuro")
                .containsOnlyKeys("rut", "razonSocial")
                .containsEntry("rut", "12345678-5")
                .containsEntry("razonSocial", "Comercial Andina SpA");
    }

    @Test
    @DisplayName("Flyway sembró a Valentina y la API la devuelve")
    void devuelveAValentinaDesdeLaBaseReal() {
        cliente().get().uri("/api/contribuyentes/11111111-1")
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.rut").isEqualTo("11111111-1")
                .jsonPath("$.razonSocial").isEqualTo("Valentina Rojas");
    }

    @Test
    @DisplayName("Un RUT inexistente responde ProblemDetail 404, no una traza")
    void rutInexistenteDevuelve404() {
        cliente().get().uri("/api/contribuyentes/99999999-9")
                .exchange()
                .expectStatus().isNotFound()
                .expectBody()
                .jsonPath("$.title").isEqualTo("Contribuyente no encontrado")
                .jsonPath("$.rut").isEqualTo("99999999-9");
    }
}
