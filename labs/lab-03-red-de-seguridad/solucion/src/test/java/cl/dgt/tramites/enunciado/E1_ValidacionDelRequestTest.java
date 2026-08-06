package cl.dgt.tramites.enunciado;

import cl.dgt.tramites.application.TramiteService;
import cl.dgt.tramites.web.controller.ManejadorDeErrores;
import cl.dgt.tramites.web.controller.TramiteController;
import cl.dgt.tramites.web.dto.TramiteDto;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/** POST /api/v1/tramites · la validación declarativa rechaza en la frontera. */
@WebMvcTest(TramiteController.class)
@Import(ManejadorDeErrores.class)
class E1_ValidacionDelRequestTest {

    @Autowired MockMvc mvc;
    @MockitoBean TramiteService servicio;

    @Test
    @DisplayName("un request válido crea el trámite y responde 201")
    void requestValidoCrea() throws Exception {
        given(servicio.crear(anyString(), anyString()))
                .willReturn(new TramiteDto(1L, "DECLARACION_F29", "BORRADOR", "11111111-1"));
        mvc.perform(post("/api/v1/tramites").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"rutContribuyente\":\"11111111-1\",\"tipo\":\"DECLARACION_F29\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    @DisplayName("un tipo en blanco se rechaza con 400 y nombra el campo 'tipo'")
    void tipoEnBlancoEsRechazado() throws Exception {
        mvc.perform(post("/api/v1/tramites").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"rutContribuyente\":\"11111111-1\",\"tipo\":\"\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentTypeCompatibleWith("application/problem+json"))
                .andExpect(jsonPath("$.campos.tipo").exists());
    }

    @Test
    @DisplayName("un tipo no reconocido se rechaza con 400 nombrando el campo")
    void tipoDesconocidoEsRechazado() throws Exception {
        mvc.perform(post("/api/v1/tramites").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"rutContribuyente\":\"11111111-1\",\"tipo\":\"HACKEO\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.campos.tipo").exists());
    }

    @Test
    @DisplayName("el 400 es un ProblemDetail con el título 'Datos inválidos'")
    void elErrorEsProblemDetail() throws Exception {
        mvc.perform(post("/api/v1/tramites").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"rutContribuyente\":\"11111111-1\",\"tipo\":\"\"}"))
                .andExpect(jsonPath("$.title").value("Datos inválidos"))
                .andExpect(jsonPath("$.status").value(400));
    }
}
