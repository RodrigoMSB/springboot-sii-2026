package cl.dgt.tramites.enunciado;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.web.servlet.client.RestTestClient;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * TODO_3 · El endpoint, por dentro, ya no habla JDBC.
 *
 * <p>Este es el test que mide el laboratorio de punta a punta: el mismo endpoint, los mismos dos
 * RUT del guion, y la diferencia entre antes y después. En el {@code starter} el segundo caso
 * devuelve TODAS las observaciones de la base; cuando el TODO_3 esté hecho, devuelve ninguna.
 */
@DisplayName("TODO_3 · el endpoint usa el repositorio, y el apóstrofe no filtra nada")
class E3_EndpointMigradoIT extends BaseObservacionesIT {

    @LocalServerPort
    private int puerto;

    private RestTestClient cliente() {
        return RestTestClient.bindToServer().baseUrl("http://localhost:" + puerto).build();
    }

    /**
     * El RUT viaja como variable de plantilla ({@code {rut}}) y no pegado a la cadena: así lo
     * codifica el cliente UNA vez, igual que lo haría un navegador. Pegarlo ya codificado lo
     * codifica dos veces, el {@code %} se convierte en {@code %25} y al servidor le llega otra
     * cosa — con lo cual la inyección no se reproduce y el test pasaría por el motivo
     * equivocado. Costó descubrirlo: el {@code curl} de la demo sí filtraba.
     */
    @SuppressWarnings("unchecked")
    private List<Map<String, Object>> observaciones(String rut) {
        return cliente().get().uri("/api/internal/observaciones?rut={rut}", rut)
                .exchange().expectStatus().isOk()
                .expectBody(List.class).returnResult().getResponseBody();
    }

    @Test
    @DisplayName("El camino honesto sigue funcionando igual que antes")
    void elCaminoHonestoNoCambia() {
        assertThat(observaciones(RUT_INOCENTE))
                .as("migrar el mecanismo no puede cambiar lo que el endpoint entrega")
                .hasSize(OBSERVACIONES_DE_VALENTINA);
    }

    @Test
    @DisplayName("El apóstrofe ya no abre la tabla entera")
    void elApostrofeNoFiltraNada() {
        assertThat(observaciones(RUT_MALICIOSO))
                .as("con el DAO heredado esto devolvía las %s observaciones de la base, incluidas "
                    + "las de otro contribuyente. Con el repositorio, el RUT es un dato.",
                    OBSERVACIONES_TOTALES)
                .isEmpty();
    }
}
