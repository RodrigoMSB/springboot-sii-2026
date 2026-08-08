package cl.dgt.tramites.trazabilidad;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClient;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * TODO_4 · Los adjuntos con desconfianza (M11). El tipo se juzga por el CONTENIDO (magic bytes), no
 * por la extensión ni el header: un ejecutable disfrazado de PDF se rechaza. Y el nombre se sanea:
 * un path traversal se neutraliza.
 */
class E4_AdjuntosIT extends BaseTrazabilidadIT {

    // MZ: cabecera de un ejecutable de Windows. %PDF-1.4: cabecera de un PDF de verdad.
    private static final byte[] EXE = {0x4D, 0x5A, (byte) 0x90, 0x00, 0x03, 0x00};
    private static final byte[] PDF = {0x25, 0x50, 0x44, 0x46, 0x2D, 0x31, 0x2E, 0x34};

    private ResponseEntity<String> subir(String bearer, Long tramite, String nombre, byte[] datos) {
        MultiValueMap<String, Object> cuerpo = new LinkedMultiValueMap<>();
        cuerpo.add("archivo", new ByteArrayResource(datos) {
            @Override
            public String getFilename() {
                return nombre;    // el cliente elige el nombre (aquí, uno malicioso)
            }
        });
        return RestClient.create().post()
                .uri("http://localhost:" + puerto + "/api/v1/tramites/" + tramite + "/adjuntos")
                .header("Authorization", bearer)
                .contentType(MediaType.MULTIPART_FORM_DATA)
                .body(cuerpo)
                .retrieve()
                .onStatus(status -> true, (req, res) -> { })   // no lanzar en 4xx: lo evaluamos nosotros
                .toEntity(String.class);
    }

    @Test
    @DisplayName("un .exe disfrazado de .pdf se rechaza (por su contenido)")
    void rechazaExeDisfrazado() {
        String carolina = bearer(CAROLINA);
        Long tramite = crearTramite(carolina);

        ResponseEntity<String> res = subir(carolina, tramite, "documento.pdf", EXE);
        assertThat(res.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    @DisplayName("un nombre con path traversal se neutraliza al último segmento")
    void neutralizaPathTraversal() throws Exception {
        String carolina = bearer(CAROLINA);
        Long tramite = crearTramite(carolina);

        ResponseEntity<String> res = subir(carolina, tramite, "../../../etc/passwd", PDF);
        assertThat(res.getStatusCode()).isEqualTo(HttpStatus.CREATED);

        JsonNode cuerpo = new ObjectMapper().readTree(res.getBody());
        assertThat(cuerpo.get("nombreArchivo").asText())
                .as("el path traversal se neutralizó al último segmento")
                .isEqualTo("passwd");
        assertThat(cuerpo.get("mimeReal").asText()).isEqualTo("application/pdf");
    }
}
