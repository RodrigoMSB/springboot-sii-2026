package cl.dgt.tramites.enunciado;

import cl.dgt.tramites.application.TramiteService;
import cl.dgt.tramites.domain.tipo.EstadoTramite;
import cl.dgt.tramites.domain.exception.TransicionIlegalException;
import cl.dgt.tramites.web.controller.ManejadorDeErrores;
import cl.dgt.tramites.web.controller.TramiteController;
import cl.dgt.tramites.web.dto.TramiteDto;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/** Una transición de estado ilegal es un error CON CONTRATO: 409 con forma exacta. */
@WebMvcTest(TramiteController.class)
@Import(ManejadorDeErrores.class)
class E3_TransicionIlegalTest {

    @Autowired MockMvc mvc;
    @MockitoBean TramiteService servicio;

    @Test
    @DisplayName("avanzar un trámite por un camino legal responde 200")
    void transicionLegal() throws Exception {
        given(servicio.avanzar(eq(1L), eq(EstadoTramite.PRESENTADO)))
                .willReturn(new TramiteDto(1L, "DECLARACION_F29", "PRESENTADO", "11111111-1"));
        mvc.perform(post("/api/v1/tramites/1/avanzar").param("a", "PRESENTADO"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estado").value("PRESENTADO"));
    }

    @Test
    @DisplayName("una transición ilegal responde 409 con tipo, origen y destino")
    void transicionIlegal() throws Exception {
        given(servicio.avanzar(eq(1L), eq(EstadoTramite.FOLIADO)))
                .willThrow(new TransicionIlegalException(EstadoTramite.BORRADOR, EstadoTramite.FOLIADO));
        mvc.perform(post("/api/v1/tramites/1/avanzar").param("a", "FOLIADO"))
                .andExpect(status().isConflict())
                .andExpect(content().contentTypeCompatibleWith("application/problem+json"))
                .andExpect(jsonPath("$.type").value("https://dgt.cl/errores/transicion-ilegal"))
                .andExpect(jsonPath("$.origen").value("BORRADOR"))
                .andExpect(jsonPath("$.destino").value("FOLIADO"));
    }
}
