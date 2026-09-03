package cl.dgt.testing;

import cl.dgt.testing.repositories.ProductoRepositoryLista;
import cl.dgt.testing.services.ProductoService;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

// Sin Spring: el servicio se construye a mano y el test corre en milisegundos.
class ProductoServiceTest {

    private final ProductoService servicio = new ProductoService(new ProductoRepositoryLista());

    // El producto 1 vale 4990 neto, o sea 5938 con IVA. Las tres franjas del descuento y el borde.
    @ParameterizedTest(name = "{0} unidades -> {1}")
    @CsvSource({
            " 1,  5938",     // sin descuento
            " 3, 16033",     // 10 %
            "10, 47504",     // 20 %
            " 0,     0"      // cantidad inválida: lanza
    })
    void elTotalAplicaElDescuentoPorVolumen(int cantidad, int esperado) {
        if (cantidad <= 0) {
            assertThrows(IllegalArgumentException.class, () -> servicio.totalConDescuento(1L, cantidad));
            return;
        }

        assertEquals(esperado, servicio.totalConDescuento(1L, cantidad));
    }
}
