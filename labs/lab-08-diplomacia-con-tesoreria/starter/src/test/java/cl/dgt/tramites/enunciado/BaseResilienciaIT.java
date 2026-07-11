package cl.dgt.tramites.enunciado;

import org.junit.jupiter.api.BeforeEach;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.MediaType;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.client.RestTestClient;
import org.springframework.web.client.RestClient;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.postgresql.PostgreSQLContainer;
import org.testcontainers.utility.DockerImageName;

import java.util.Map;

/**
 * Base de los tests de resiliencia: PostgreSQL real + TESO (WireMock, tag fijado) en contenedores,
 * y los helpers para gobernar a TESO (ponerle un retraso, resetearlo) vía su API de administración.
 * TESO arranca sin mappings: cada test declara la respuesta que necesita. Cero {@code Thread.sleep}.
 *
 * <p><strong>Contenedores SINGLETON</strong> (arrancados en el bloque estático, no con
 * {@code @Container}/{@code @Testcontainers}): viven UNA vez para todo el JVM y Ryuk los limpia al
 * final. Es a propósito: el contexto de Spring se cachea y se reutiliza entre las clases de test,
 * y un contenedor con ciclo de vida por-clase se detendría dejando al contexto cacheado apuntando a
 * un puerto muerto —"connection refused"—. El singleton mantiene los puertos estables.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        properties = "spring.docker.compose.enabled=false")
abstract class BaseResilienciaIT {

    static final String CLAVE = "dgt-2026";
    static final String CAROLINA = "9876543-2";   // FUNCIONARIO: crea, paga, emite

    static final PostgreSQLContainer POSTGRES = new PostgreSQLContainer("postgres:16-alpine3.24");

    static final GenericContainer<?> TESO =
            new GenericContainer<>(DockerImageName.parse("wiremock/wiremock:3.9.1"))
                    .withExposedPorts(8080);

    static {
        POSTGRES.start();
        TESO.start();
    }

    @DynamicPropertySource
    static void propiedades(DynamicPropertyRegistry registro) {
        registro.add("spring.datasource.url", POSTGRES::getJdbcUrl);
        registro.add("spring.datasource.username", POSTGRES::getUsername);
        registro.add("spring.datasource.password", POSTGRES::getPassword);
        registro.add("dgt.teso.base-url", BaseResilienciaIT::tesoUrl);
    }

    static String tesoUrl() {
        return "http://" + TESO.getHost() + ":" + TESO.getMappedPort(8080);
    }

    @LocalServerPort
    int puerto;

    private final RestClient admin = RestClient.create();

    @BeforeEach
    void limpiarTeso() {
        admin.post().uri(tesoUrl() + "/__admin/mappings/reset").retrieve().toBodilessEntity();
    }

    RestTestClient cliente() {
        return RestTestClient.bindToServer().baseUrl("http://localhost:" + puerto).build();
    }

    /** Configura TESO para que confirme el pago con un retraso de {@code ms} milisegundos. */
    void tesoRespondeCon(int ms) {
        admin.post().uri(tesoUrl() + "/__admin/mappings")
                .contentType(MediaType.APPLICATION_JSON)
                .body(("""
                    {"request":{"method":"GET","urlPathPattern":"/pagos/.*"},
                     "response":{"status":200,"headers":{"Content-Type":"application/json"},
                                 "jsonBody":{"confirmado":true},"fixedDelayMilliseconds":%d}}
                    """).formatted(ms))
                .retrieve().toBodilessEntity();
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

    /** Crea un trámite y lo avanza a PRESENTADO (listo para pagarse). Devuelve su id. */
    @SuppressWarnings("unchecked")
    Long crearTramitePresentado(String bearer) {
        Map<String, Object> creado = cliente().post().uri("/api/v1/tramites")
                .header("Authorization", bearer).contentType(MediaType.APPLICATION_JSON)
                .body(Map.of("rutContribuyente", "11111111-1", "tipo", "DECLARACION_F29"))
                .exchange().expectStatus().isCreated()
                .expectBody(Map.class).returnResult().getResponseBody();
        Long id = ((Number) creado.get("id")).longValue();
        cliente().post().uri("/api/v1/tramites/" + id + "/avanzar?a=PRESENTADO")
                .header("Authorization", bearer)
                .exchange().expectStatus().isOk();
        return id;
    }
}
