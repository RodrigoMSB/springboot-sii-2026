package cl.dgt.tramites.enunciado;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;

/**
 * TODO_4 · El endurecimiento (M9). CORS nominal (no {@code *}): solo el front de Mi DGT puede
 * llamar desde un navegador; un origen intruso no recibe permiso. Y las cabeceras de seguridad
 * presentes en la respuesta.
 */
class E4_EndurecimientoIT extends BaseResilienciaIT {

    @Test
    @DisplayName("preflight CORS desde el origen nominal recibe permiso")
    void corsPermiteElOrigenNominal() {
        cliente().method(HttpMethod.OPTIONS).uri("/api/v1/tramites")
                .header("Origin", "https://mi.dgt.cl")
                .header("Access-Control-Request-Method", "GET")
                .exchange()
                .expectStatus().is2xxSuccessful()
                .expectHeader().valueEquals("Access-Control-Allow-Origin", "https://mi.dgt.cl");
    }

    @Test
    @DisplayName("preflight CORS desde un origen intruso NO recibe permiso")
    void corsRechazaElIntruso() {
        cliente().method(HttpMethod.OPTIONS).uri("/api/v1/tramites")
                .header("Origin", "https://intruso.cl")
                .header("Access-Control-Request-Method", "GET")
                .exchange()
                .expectHeader().doesNotExist("Access-Control-Allow-Origin");
    }

    @Test
    @DisplayName("las cabeceras de endurecimiento están presentes")
    void lasCabecerasDeEndurecimientoEstanPresentes() {
        cliente().get().uri("/api/v1/tramites")
                .header("Authorization", bearer(CAROLINA))
                .exchange()
                .expectStatus().isOk()
                .expectHeader().exists("Content-Security-Policy");
    }
}
