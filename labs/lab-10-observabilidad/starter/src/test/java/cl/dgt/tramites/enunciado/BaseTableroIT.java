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
 * Base del enunciado del Lab 10: PostgreSQL real (embebido, ver Lab 08) + login.
 *
 * <p>Los tres primeros tests comparten este contenedor. {@code E1_HealthQueNoMienteIT} NO lo usa:
 * necesita <strong>matar</strong> su base a media prueba, y matar la de todos sería sabotear la
 * suite. Levanta la suya y la tumba — ver el comentario en ese archivo.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        properties = "dgt.base-embebida.enabled=false")
abstract class BaseTableroIT {

    static final String CLAVE = "dgt-2026";
    /** Carolina es FUNCIONARIO: emite folios y declara líneas. La semilla lo fija (V2). */
    static final String CAROLINA = "9876543-2";
    /** Valentina es CONTRIBUYENTE: tiene credencial, no permisos de funcionario. */
    static final String VALENTINA = "11111111-1";


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

    /** Crea un trámite y devuelve su id. Sirve de sujeto para emitir folio o declarar líneas. */
    @SuppressWarnings("unchecked")
    Long crearTramite(String bearer) {
        Map<String, Object> creado = cliente().post().uri("/api/v1/tramites")
                .header("Authorization", bearer).contentType(MediaType.APPLICATION_JSON)
                .body(Map.of("rutContribuyente", "11111111-1", "tipo", "DECLARACION_F29"))
                .exchange().expectStatus().isCreated()
                .expectBody(Map.class).returnResult().getResponseBody();
        return ((Number) creado.get("id")).longValue();
    }

    /** El texto plano de {@code /actuator/prometheus}. Va autenticado: el pulso del negocio no es público. */
    String scrapePrometheus(String bearer) {
        return cliente().get().uri("/actuator/prometheus")
                .header("Authorization", bearer)
                .exchange().expectStatus().isOk()
                .expectBody(String.class).returnResult().getResponseBody();
    }

    /**
     * Lee el valor de una serie del scrape de Prometheus.
     *
     * <p>Se busca por PREFIJO de línea porque cada serie lleva sus etiquetas entre llaves
     * ({@code dgt_folios_emitidos_total{application="...",resultado="nuevo"} 3.0}) y las etiquetas
     * no vienen en un orden garantizado. Devuelve 0 si la serie no aparece.
     */
    static double serie(String scrape, String nombre, String etiqueta) {
        if (scrape == null) {
            return 0;
        }
        for (String linea : scrape.split("\n")) {
            if (linea.startsWith(nombre) && linea.contains(etiqueta)) {
                int corte = linea.lastIndexOf(' ');
                if (corte > 0) {
                    return Double.parseDouble(linea.substring(corte + 1).trim());
                }
            }
        }
        return 0;
    }
}
