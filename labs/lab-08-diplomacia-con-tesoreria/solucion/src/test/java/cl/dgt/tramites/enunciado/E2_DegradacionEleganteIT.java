package cl.dgt.tramites.enunciado;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * TODO_2 · La mala noticia elegante. Cuando TESO no responde, el pago no explota con un 500 ni un
 * stacktrace: responde 503 `ProblemDetail` —tipo propio, sugerencia de reintento— y el trámite
 * queda ÍNTEGRO en PRESENTADO. La API viva, honesta, y rápida en su mala noticia.
 */
class E2_DegradacionEleganteIT extends BaseResilienciaIT {

    @Test
    @DisplayName("TESO caído -> 503 ProblemDetail limpio, y el trámite no cambia de estado")
    void tesoCaidoDa503YNoTocaElTramite() {
        tesoRespondeCon(3000);
        String carolina = bearer(CAROLINA);
        Long tramite = crearTramitePresentado(carolina);

        cliente().post().uri("/api/v1/tramites/" + tramite + "/pago")
                .header("Authorization", carolina)
                .exchange()
                .expectStatus().isEqualTo(503)
                .expectBody()
                .jsonPath("$.title").isEqualTo("Servicio de pagos no disponible")
                .jsonPath("$.status").isEqualTo(503)
                .jsonPath("$.type").isEqualTo("https://dgt.cl/errores/tesoreria-no-disponible")
                .jsonPath("$.reintentarEnSegundos").isEqualTo(5)
                .jsonPath("$.trace").doesNotExist();   // sin stacktrace: una mala noticia, no una autopsia

        cliente().get().uri("/api/v1/tramites/" + tramite)
                .header("Authorization", carolina)
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.estado").isEqualTo("PRESENTADO");   // el trámite no avanzó
    }
}
