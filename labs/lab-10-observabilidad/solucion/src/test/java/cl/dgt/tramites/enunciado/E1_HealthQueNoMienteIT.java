package cl.dgt.tramites.enunciado;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.client.RestTestClient;
import org.testcontainers.postgresql.PostgreSQLContainer;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * TODO_1 · <strong>El health que no miente.</strong>
 *
 * <p>Este es el test que mide el crimen del lab. Con la base caída:
 *
 * <ul>
 *   <li><strong>readiness</strong> debe decir {@code DOWN} <em>y nombrar el componente</em>: no
 *       basta un rojo anónimo. «Algo se cayó» no es información; «se cayó baseDeDatos» sí.</li>
 *   <li><strong>liveness</strong> debe seguir {@code UP}: la aplicación está perfectamente viva.
 *       Reiniciarla no levantaría PostgreSQL — solo tiraría el tráfico en curso y arrancaría un
 *       bucle de reinicios mientras el problema sigue en otra máquina.</li>
 * </ul>
 *
 * <p><strong>Por qué esta clase levanta su PROPIO contenedor</strong> y no usa el singleton de
 * {@code BaseTableroIT}: para probar «la base cayó» hay que <em>tumbar la base de verdad</em>. Con
 * el contenedor compartido, matarlo dejaría sin base a toda la suite. Aquí se levanta uno, se usa,
 * se mata, y el destrozo queda contenido en esta clase. Nada de simulacros con mocks: el crimen se
 * vive, también en los tests.
 *
 * <p>El orden de los métodos es fijo ({@code @Order}) por la misma razón: primero se comprueba el
 * mundo sano, y solo después se rompe. Al revés no habría con qué comparar.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        properties = "spring.docker.compose.enabled=false")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@DisplayName("TODO_1 · el tablero dice la verdad: readiness cae y nombra el componente; liveness aguanta")
class E1_HealthQueNoMienteIT {

    /** Contenedor PROPIO de esta clase: se tumba a media prueba y nadie más lo sufre. */
    static final PostgreSQLContainer POSTGRES = new PostgreSQLContainer("postgres:16-alpine3.24");

    static {
        POSTGRES.start();
    }

    @DynamicPropertySource
    static void propiedades(DynamicPropertyRegistry registro) {
        registro.add("spring.datasource.url", POSTGRES::getJdbcUrl);
        registro.add("spring.datasource.username", POSTGRES::getUsername);
        registro.add("spring.datasource.password", POSTGRES::getPassword);
    }

    @LocalServerPort
    int puerto;

    private RestTestClient cliente() {
        return RestTestClient.bindToServer().baseUrl("http://localhost:" + puerto).build();
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> salud(String ruta) {
        return cliente().get().uri(ruta).exchange()
                .expectBody(Map.class).returnResult().getResponseBody();
    }

    @Test
    @Order(1)
    @DisplayName("con la base viva, readiness está UP y el componente baseDeDatos aparece nombrado")
    void readinessNombraLaBaseCuandoTodoVaBien() {
        Map<String, Object> readiness = salud("/actuator/health/readiness");

        assertThat(readiness).containsEntry("status", "UP");

        // El componente tiene que APARECER. Un readiness que no mira la base no puede caerse con
        // ella, y ese es exactamente el tablero adorno que vinimos a arreglar.
        @SuppressWarnings("unchecked")
        Map<String, Object> componentes = (Map<String, Object>) readiness.get("components");
        assertThat(componentes)
                .as("readiness debe agregar el chequeo de la base (grupo readiness en application.yml)")
                .containsKey("baseDeDatos");
    }

    @Test
    @Order(2)
    @DisplayName("liveness NO incluye la base: una dependencia caída no es motivo para reiniciar")
    void livenessNoMiraLaBase() {
        Map<String, Object> liveness = salud("/actuator/health/liveness");

        assertThat(liveness).containsEntry("status", "UP");

        @SuppressWarnings("unchecked")
        Map<String, Object> componentes = (Map<String, Object>) liveness.get("components");
        assertThat(componentes == null ? Map.of() : componentes)
                .as("liveness jamás debe depender de la base: mezclarlas convierte una caída de "
                    + "PostgreSQL en una caída de PostgreSQL MÁS un bucle de reinicios")
                .doesNotContainKey("baseDeDatos");
    }

    @Test
    @Order(3)
    @DisplayName("con la base MUERTA: readiness cae a DOWN y dice qué se cayó; liveness sigue UP")
    @SuppressWarnings("unchecked")
    void conLaBaseCaidaReadinessCaeYLivenessAguanta() {
        // El crimen, en vivo. A partir de aquí la aplicación sigue en pie pero no puede trabajar.
        POSTGRES.stop();

        Map<String, Object> readiness = salud("/actuator/health/readiness");
        assertThat(readiness)
                .as("con la base caída, esta instancia NO puede atender: readiness debe decirlo")
                .containsEntry("status", "DOWN");

        Map<String, Object> componentes = (Map<String, Object>) readiness.get("components");
        assertThat(componentes).containsKey("baseDeDatos");

        Map<String, Object> base = (Map<String, Object>) componentes.get("baseDeDatos");
        assertThat(base).containsEntry("status", "DOWN");

        // Y el detalle: nombrar el componente sin decir qué le pasa deja al operador en la misma
        // duda. La causa raíz viaja en el detalle.
        Map<String, Object> detalles = (Map<String, Object>) base.get("details");
        assertThat(detalles)
                .as("el health debe adjuntar la causa: quien lo lee a las 3 AM necesita el porqué")
                .containsKey("causa");

        // Y lo más importante del TODO_1: la app está VIVA. Que la base se caiga no es razón para
        // reiniciarla — y este assert es lo que impide que alguien "arregle" el health metiendo
        // todo en el mismo saco.
        Map<String, Object> liveness = salud("/actuator/health/liveness");
        assertThat(liveness)
                .as("la aplicación no está rota: está sin base. Reiniciarla no arregla nada")
                .containsEntry("status", "UP");
    }
}
