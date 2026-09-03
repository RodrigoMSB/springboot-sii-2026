package cl.dgt.testing;

import cl.dgt.testing.controllers.ProductoController;
import cl.dgt.testing.services.ProductoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

// Paso 3 · la capa web sin servidor: pedir un producto que no existe da 404 con cuerpo.
@WebMvcTest(ProductoController.class)
class ProductoControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ProductoService servicio;

    // escribe aquí
}
