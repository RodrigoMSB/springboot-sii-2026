package cl.dgt.testing;

import cl.dgt.testing.models.Producto;
import cl.dgt.testing.repositories.ProductoRepository;
import cl.dgt.testing.services.ProductoService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

// El repositorio real se sustituye por un doble: se prueba UNA pieza, no la cadena.
@ExtendWith(MockitoExtension.class)
class ProductoServiceConDobleTest {

    @Mock
    private ProductoRepository repositorio;

    @Test
    void elDescuentoSeCalculaSobreLoQueDevuelveElRepositorio() {
        when(repositorio.porId(1L)).thenReturn(Optional.of(new Producto(1L, "Inventado", 1000)));

        ProductoService servicio = new ProductoService(repositorio);

        // 1000 neto -> 1190 con IVA -> x3 = 3570 -> 10 % menos = 3213
        assertEquals(3213, servicio.totalConDescuento(1L, 3));
    }
}
