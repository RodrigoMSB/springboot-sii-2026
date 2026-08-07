package cl.dgt.tramites.latidos;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.MediaType;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.client.RestTestClient;
import org.testcontainers.postgresql.PostgreSQLContainer;
import org.testcontainers.rabbitmq.RabbitMQContainer;

import java.util.Map;

/**
 * Base del enunciado del Lab 11: PostgreSQL real (contenedor singleton, ver Lab 08) + login.
 *
 * <p><strong>El latido automático se apaga en los tests</strong> ({@code intervalo-ms} enorme y
 * {@code retardo-inicial-ms} mayor que cualquier suite). No es por comodidad: un planificador
 * disparando el cierre por su cuenta mientras un test cuenta ejecuciones convierte la prueba en
 * una lotería. Aquí las tareas se invocan a mano, cuando el test decide, y así lo que se afirma es
 * el comportamiento y no la suerte.
 *
 * <p>El {@code cron} se deja declarado —el test del TODO_1 inspecciona su zona— pero apuntando a
 * las 3 AM: no va a saltar en mitad de una suite.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        properties = {
                "spring.docker.compose.enabled=false",
                "dgt.instancia=test-instancia",
                "dgt.cierre.intervalo-ms=86400000",
                "dgt.cierre.retardo-inicial-ms=86400000"
        })
abstract class BaseLatidosIT {

    static final String CLAVE = "dgt-2026";
    /** Carolina es FUNCIONARIO: emite folios. La semilla lo fija (V2). */
    static final String CAROLINA = "9876543-2";

    static final PostgreSQLContainer POSTGRES = new PostgreSQLContainer("postgres:16-alpine3.24");

    /**
     * Desde el Lab 12, estos tests necesitan también el broker.
     *
     * <p>No es que la lección del Lab 11 haya cambiado —el evento sigue siendo AFTER_COMMIT y el
     * envío sigue siendo asíncrono—: lo que cambió es POR DÓNDE sale el aviso. Antes se llamaba al
     * notificador en memoria; ahora se entrega a una cola, y sin broker esa entrega no ocurre. Los
     * tests heredados ganan lo que necesitan para seguir afirmando lo mismo, y la divergencia se
     * declara — el precedente de «romper hacia atrás» del Lab 07.
     */
    static final RabbitMQContainer RABBIT = new RabbitMQContainer("rabbitmq:4.2.4-management");

    static {
        POSTGRES.start();
        RABBIT.start();
    }

    @DynamicPropertySource
    static void propiedades(DynamicPropertyRegistry registro) {
        registro.add("spring.datasource.url", POSTGRES::getJdbcUrl);
        registro.add("spring.datasource.username", POSTGRES::getUsername);
        registro.add("spring.datasource.password", POSTGRES::getPassword);

        registro.add("spring.rabbitmq.host", RABBIT::getHost);
        registro.add("spring.rabbitmq.port", RABBIT::getAmqpPort);
        registro.add("spring.rabbitmq.username", RABBIT::getAdminUsername);
        registro.add("spring.rabbitmq.password", RABBIT::getAdminPassword);
    }

    @LocalServerPort
    int puerto;

    RestTestClient cliente() {
        return RestTestClient.bindToServer().baseUrl("http://localhost:" + puerto).build();
    }

    @SuppressWarnings("unchecked")
    String bearer(String rut) {
        Map<String, Object> cuerpo = cliente().post().uri("/api/v1/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .body(Map.of("rut", rut, "clave", CLAVE))
                .exchange().expectStatus().isOk()
                .expectBody(Map.class).returnResult().getResponseBody();
        return "Bearer " + cuerpo.get("token");
    }

    @SuppressWarnings("unchecked")
    Long crearTramite(String bearer) {
        Map<String, Object> creado = cliente().post().uri("/api/v1/tramites")
                .header("Authorization", bearer).contentType(MediaType.APPLICATION_JSON)
                .body(Map.of("rutContribuyente", "11111111-1", "tipo", "DECLARACION_F29"))
                .exchange().expectStatus().isCreated()
                .expectBody(Map.class).returnResult().getResponseBody();
        return ((Number) creado.get("id")).longValue();
    }
}
