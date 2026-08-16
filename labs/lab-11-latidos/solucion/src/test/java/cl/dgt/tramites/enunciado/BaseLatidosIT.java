package cl.dgt.tramites.enunciado;

import cl.dgt.tramites.PostgresEmbebido;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.MediaType;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.client.RestTestClient;

import java.util.Map;

/**
 * Base del enunciado del Lab 11: PostgreSQL real (embebido, ver Lab 08) + login.
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
                "dgt.base-embebida.enabled=false",
                "dgt.instancia=test-instancia",
                "dgt.cierre.intervalo-ms=86400000",
                "dgt.cierre.retardo-inicial-ms=86400000"
        })
abstract class BaseLatidosIT {

    static final String CLAVE = "dgt-2026";
    /** Carolina es FUNCIONARIO: emite folios. La semilla lo fija (V2). */
    static final String CAROLINA = "9876543-2";


    @DynamicPropertySource
    static void propiedades(DynamicPropertyRegistry registro) {
        // Una base recién creada para ESTE contexto: se pide una sola vez y se guarda.
        String url = PostgresEmbebido.nuevaBase();
        registro.add("spring.datasource.url", () -> url);
        registro.add("spring.datasource.username", PostgresEmbebido::usuario);
        registro.add("spring.datasource.password", PostgresEmbebido::clave);
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
