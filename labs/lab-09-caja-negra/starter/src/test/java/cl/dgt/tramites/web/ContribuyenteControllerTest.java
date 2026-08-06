package cl.dgt.tramites.web;

import cl.dgt.tramites.application.ContribuyenteService;
import cl.dgt.tramites.domain.exception.ContribuyenteNoEncontradoException;
import cl.dgt.tramites.web.controller.ContribuyenteController;
import cl.dgt.tramites.web.controller.ManejadorDeErrores;
import cl.dgt.tramites.web.dto.ContribuyenteDto;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Rebanada web: el controlador, su advice, y nada más.
 *
 * <p>Estilo de referencia para los labs: {@code @WebMvcTest} para la rebanada (rápido, sin
 * base de datos) y una prueba de integración aparte para el contrato de verdad. Aquí el
 * servicio está mockeado, así que un "no aparece claveHash" no probaría nada — probaría
 * que el mock devuelve lo que le dijimos. RN-03 se verifica de punta a punta en
 * {@code ContratoRn03IT}, contra una fila real de la base.
 *
 * <p>El {@code @Autowired} de abajo es de un test, no de un bean de producción: AU-06
 * vigila el {@code main}, y los tests no son beans de la aplicación.
 */
@WebMvcTest(ContribuyenteController.class)
@Import(ManejadorDeErrores.class)
// Lab 07: la rebanada web hereda la seguridad. Estos endpoints solo exigen estar autenticado
// (no un rol), así que un @WithMockUser basta. Divergencia declarada en el allowlist.
@WithMockUser
class ContribuyenteControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ContribuyenteService servicio;

    @Test
    @DisplayName("GET /api/contribuyentes/{rut} devuelve el DTO")
    void devuelveElDto() throws Exception {
        given(servicio.buscarPorRut("11111111-1"))
                .willReturn(new ContribuyenteDto("11111111-1", "Valentina Rojas"));

        mockMvc.perform(get("/api/contribuyentes/11111111-1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.rut").value("11111111-1"))
                .andExpect(jsonPath("$.razonSocial").value("Valentina Rojas"));
    }

    @Test
    @DisplayName("Un RUT inexistente responde 404 con ProblemDetail")
    void rutInexistenteDevuelveProblemDetail() throws Exception {
        given(servicio.buscarPorRut(anyString()))
                .willThrow(new ContribuyenteNoEncontradoException("99999999-9"));

        mockMvc.perform(get("/api/contribuyentes/99999999-9"))
                .andExpect(status().isNotFound())
                .andExpect(content().contentTypeCompatibleWith("application/problem+json"))
                .andExpect(jsonPath("$.title").value("Contribuyente no encontrado"))
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.rut").value("99999999-9"));
    }
}
