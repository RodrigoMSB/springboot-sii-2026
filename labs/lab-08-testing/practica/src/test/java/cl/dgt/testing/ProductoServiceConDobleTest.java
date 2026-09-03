package cl.dgt.testing;

import cl.dgt.testing.repositories.ProductoRepository;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

// Paso 2 · el mismo método, con el repositorio sustituido por un doble.
@ExtendWith(MockitoExtension.class)
class ProductoServiceConDobleTest {

    @Mock
    private ProductoRepository repositorio;

    // escribe aquí
}
