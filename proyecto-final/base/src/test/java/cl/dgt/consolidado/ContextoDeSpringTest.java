package cl.dgt.consolidado;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;

import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * Llega resuelto, y pasa desde el primer minuto. Comprueba que la aplicación ARRANCA:
 * que los beans se cablean, que Flyway migra y que la base embebida levanta.
 *
 * <p>No es tu entrega — es la red que avisa si algo que escribiste rompe el arranque.
 */
@SpringBootTest
class ContextoDeSpringTest {

    @Autowired
    private ApplicationContext contexto;

    @Test
    void laAplicacionArranca() {
        assertNotNull(contexto);
    }
}
