// Test de controller con MockMvc: comprueba el 404 con cuerpo, sin levantar el servidor.
// Tu equivalente: el mismo test sobre `/consolidados/{rut}`.
package cl.dgt.consolidado;

import cl.dgt.consolidado.controllers.ManejadorDeErrores;
import cl.dgt.consolidado.controllers.ResumenController;
import cl.dgt.consolidado.services.OficinaNoEncontradaException;
import cl.dgt.consolidado.services.ResumenService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = ResumenController.class)
@Import(ManejadorDeErrores.class)
@AutoConfigureMockMvc(addFilters = false)
@WithMockUser(authorities = "ROLE_FISCALIZADOR")
class ResumenControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ResumenService servicio;

    @Test
    void unCodigoQueNoExisteDevuelve404ConCuerpo() throws Exception {
        when(servicio.delPeriodo(eq("NO-EXISTE"), any(LocalDate.class), any(LocalDate.class)))
                .thenThrow(new OficinaNoEncontradaException("NO-EXISTE"));

        mockMvc.perform(get("/resumenes/NO-EXISTE")
                        .param("desde", "2026-01-01")
                        .param("hasta", "2026-12-31"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.mensaje").value("No existe la oficina NO-EXISTE"));
    }
    // ^ LAS DOS COMPROBACIONES SON EL TEST:
    //
    //     status().isNotFound()  →  que la excepción no se escapó como un 500
    //     jsonPath("$.mensaje")  →  que el error trae un cuerpo legible
    //
    //   Ninguna la escribió el controller: las produce `ManejadorDeErrores`, y por eso hay que
    //   importarlo con `@Import` — `@WebMvcTest` sólo carga el controller que se le nombra.
    //
    //   `addFilters = false` apaga la cadena de filtros de seguridad EN ESTE TEST, y hay que
    //   saber por qué: lo que se prueba aquí es el 404 y su cuerpo, no la seguridad. Montar la
    //   cadena entera obligaría a construir un `JwtDecoder` y a firmar un token de mentira para
    //   llegar a comprobar... el manejador de errores.
    //
    //   La seguridad se comprueba aparte, con los curl de la rúbrica (401 sin token, 403 con el
    //   token equivocado). Cada test al nivel más barato que responda su pregunta — Lab 08.
}
