package cl.dgt.testing;

import cl.dgt.testing.controllers.ProductoController;
import cl.dgt.testing.services.ProductoService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;

import static org.junit.jupiter.api.Assertions.assertNotNull;

// Levanta el contexto entero. Es el más caro del proyecto y por eso hay uno solo.
@SpringBootTest
class ContextoDeSpringTest {

    @Autowired
    private ApplicationContext contexto;

    @Test
    void elCableadoDeSpringEsCorrecto() {
        assertNotNull(contexto.getBean(ProductoService.class));
        assertNotNull(contexto.getBean(ProductoController.class));
    }
}
