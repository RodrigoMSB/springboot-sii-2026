package cl.dgt.tramites.enunciado;

import cl.dgt.tramites.application.TramiteService;
import cl.dgt.tramites.domain.exception.TramiteNoEncontradoException;
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

import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * TODO_4 · El primer endpoint del alumno.
 *
 * <p>Dos exigencias, y la segunda separa a un profesional de un aficionado: que el camino
 * feliz devuelva un DTO, y que el camino triste devuelva un {@code ProblemDetail} en vez
 * de una traza de 300 líneas con el nombre de tus clases dentro.
 */
@WebMvcTest(TramiteController.class)
@Import(ManejadorDeErrores.class)
class T4_TramiteEndpointTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private TramiteService servicio;

    @Test
    @DisplayName("GET /api/tramites/{id} devuelve el DTO del trámite")
    void devuelveElTramite() throws Exception {
        given(servicio.buscarPorId(1L))
                .willReturn(new TramiteDto(1L, "DECLARACION_F29", "BORRADOR", "11111111-1"));

        mockMvc.perform(get("/api/tramites/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.tipo").value("DECLARACION_F29"))
                .andExpect(jsonPath("$.estado").value("BORRADOR"))
                .andExpect(jsonPath("$.rutContribuyente").value("11111111-1"));
    }

    @Test
    @DisplayName("Un id inexistente devuelve 404 con ProblemDetail, no una traza")
    void idInexistenteDevuelveProblemDetail() throws Exception {
        given(servicio.buscarPorId(anyLong())).willThrow(new TramiteNoEncontradoException(999L));

        mockMvc.perform(get("/api/tramites/999"))
                .andExpect(status().isNotFound())
                .andExpect(content().contentTypeCompatibleWith("application/problem+json"))
                .andExpect(jsonPath("$.title").value("Trámite no encontrado"))
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.id").value(999));
    }
}
