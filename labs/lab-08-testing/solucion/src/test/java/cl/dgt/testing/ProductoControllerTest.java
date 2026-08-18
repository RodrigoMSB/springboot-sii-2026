package cl.dgt.testing;

import cl.dgt.testing.controllers.ProductoController;
import cl.dgt.testing.models.Producto;
import cl.dgt.testing.services.ProductoNoEncontradoException;
import cl.dgt.testing.services.ProductoService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

// Solo la capa web: ni puerto, ni Tomcat, ni el resto del contexto.
@WebMvcTest(ProductoController.class)
class ProductoControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ProductoService servicio;

    @Test
    void pedirUnProductoQueExisteDevuelve200YSuJson() throws Exception {
        when(servicio.porId(1L)).thenReturn(new Producto(1L, "Resma de papel carta", 4990));

        mockMvc.perform(get("/productos/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.nombre").value("Resma de papel carta"))
                .andExpect(jsonPath("$.precioNeto").value(4990));
    }

    @Test
    void pedirUnProductoQueNoExisteDevuelve404ConCuerpo() throws Exception {
        when(servicio.porId(99L)).thenThrow(new ProductoNoEncontradoException(99L));

        mockMvc.perform(get("/productos/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.mensaje").value("No existe el producto 99"));
    }
}
