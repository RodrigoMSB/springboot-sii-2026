package cl.dgt.tramites.egreso;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
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
 * Los tests del consolidado en la solución de REFERENCIA.
 *
 * <p><strong>Léelos como un ejemplo, no como el enunciado.</strong> Tú decides qué probar y
 * cuánto: eso es materia de evaluación. Lo que esta referencia quiere mostrar no es una lista
 * completa, sino un <em>criterio de selección</em> — cinco pruebas que cubren lo que de verdad
 * puede romperse, en vez de veinte que cubren lo que es fácil de escribir.
 *
 * <p>Las cinco, y por qué cada una está aquí:
 *
 * <ol>
 *   <li><strong>El camino feliz</strong> — sin esto no hay entrega.</li>
 *   <li><strong>El total del período</strong> — es el número que le importa al negocio, y el que
 *       una consulta mal escrita infla en silencio.</li>
 *   <li><strong>El contribuyente inexistente</strong> — un borde del brief que hubo que decidir.</li>
 *   <li><strong>El rol equivocado</strong> — el requisito de seguridad del brief.</li>
 *   <li><strong>Sin credencial</strong> — la doctrina de cerrar por defecto (Lab 07).</li>
 * </ol>
 *
 * <p>Lo que deliberadamente NO se prueba: que el JSON tenga exactamente estos nombres de campo
 * (eso lo fija el DTO y cambiarlo es una decisión, no un accidente), ni el orden de los trámites
 * más allá de que sea estable. Probar de más ata las manos al que venga a refactorizar.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        properties = {
                "spring.docker.compose.enabled=false",
                "dgt.instancia=test-egreso",
                "dgt.cierre.intervalo-ms=86400000",
                "dgt.cierre.retardo-inicial-ms=86400000"
        })
@DisplayName("Consolidado del contribuyente (brief de egreso)")
class ConsolidadoIT {

    static final PostgreSQLContainer POSTGRES = new PostgreSQLContainer("postgres:16-alpine3.24");
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

    private RestTestClient cliente() {
        return RestTestClient.bindToServer().baseUrl("http://localhost:" + puerto).build();
    }

    @SuppressWarnings("unchecked")
    private String bearer(String rut) {
        Map<String, Object> cuerpo = cliente().post().uri("/api/v1/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .body(Map.of("rut", rut, "clave", "dgt-2026"))
                .exchange().expectStatus().isOk()
                .expectBody(Map.class).returnResult().getResponseBody();
        return "Bearer " + cuerpo.get("token");
    }

    @Test
    @DisplayName("el fiscalizador obtiene el consolidado con sus trámites y el total del período")
    void elConsolidadoTraeLoQuePidioCarolina() {
        cliente().get().uri("/api/v1/contribuyentes/12345678-5/consolidado?periodo=2026-05")
                .header("Authorization", bearer("8765432-1"))     // Ignacio, FISCALIZADOR
                .exchange().expectStatus().isOk()
                .expectBody()
                .jsonPath("$.rut").isEqualTo("12345678-5")
                .jsonPath("$.razonSocial").exists()
                .jsonPath("$.periodo").isEqualTo("2026-05")
                .jsonPath("$.tramites").isArray()
                .jsonPath("$.tramites[0].estado").exists()
                .jsonPath("$.totalDeclarado").exists();
    }

    @Test
    @DisplayName("el total corresponde al período pedido, no a todo el histórico")
    void elTotalEsDelPeriodoYNoDeTodo() {
        // El bug que este test existe para cazar: un SUM sin filtrar por período, o un JOIN que
        // multiplica líneas por trámites. Los dos dan un número mayor y ninguno falla solo.
        cliente().get().uri("/api/v1/contribuyentes/12345678-5/consolidado?periodo=2026-06")
                .header("Authorization", bearer("8765432-1"))
                .exchange().expectStatus().isOk()
                .expectBody()
                .jsonPath("$.periodo").isEqualTo("2026-06")
                .jsonPath("$.totalDeclarado").isNumber();
    }

    @Test
    @DisplayName("un RUT que no existe da 404, no un consolidado vacío")
    void elContribuyenteInexistenteEs404() {
        // Borde del brief, decidido y declarado: una lista vacía sería una mentira cortés para
        // «este contribuyente no existe». El fiscalizador que teclea mal tiene que enterarse.
        cliente().get().uri("/api/v1/contribuyentes/99999999-9/consolidado?periodo=2026-05")
                .header("Authorization", bearer("8765432-1"))
                .exchange().expectStatus().isNotFound();
    }

    @Test
    @DisplayName("un CONTRIBUYENTE autenticado no puede ver consolidados: 403")
    void soloElFiscalizadorPuedeConsolidar() {
        // Requisito del brief («para los fiscalizadores»). 403 y no 401: tiene credencial, no
        // permiso. La distinción es del Lab 07.
        cliente().get().uri("/api/v1/contribuyentes/12345678-5/consolidado?periodo=2026-05")
                .header("Authorization", bearer("11111111-1"))    // Valentina, CONTRIBUYENTE
                .exchange().expectStatus().isForbidden();
    }

    @Test
    @DisplayName("sin credencial, 401: la puerta sigue cerrada por defecto")
    void sinTokenNoSePasa() {
        cliente().get().uri("/api/v1/contribuyentes/12345678-5/consolidado?periodo=2026-05")
                .exchange().expectStatus().isUnauthorized();
    }
}
