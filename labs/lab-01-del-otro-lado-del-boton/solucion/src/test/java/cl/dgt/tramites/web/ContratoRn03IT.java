package cl.dgt.tramites.web;

import cl.dgt.tramites.PostgresEmbebido;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.client.RestTestClient;

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
 * <p>La base es PostgreSQL de verdad, no un H2 que se le parece: los tipos, las restricciones y
 * el SQL de Flyway son los del motor que corre en producción. Lo que cambió (SPEC-022) es cómo
 * llega — ya no en un contenedor, sino como dependencia Maven que se extrae y corre como proceso
 * hijo. La diferencia le importa a la máquina del alumno, no al test.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        properties = "dgt.base-embebida.enabled=false")
class ContratoRn03IT {

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
