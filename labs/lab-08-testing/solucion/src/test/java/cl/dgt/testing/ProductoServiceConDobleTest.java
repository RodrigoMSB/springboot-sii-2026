package cl.dgt.testing;

import cl.dgt.testing.models.Producto;
import cl.dgt.testing.repositories.ProductoRepository;
import cl.dgt.testing.services.ProductoNoEncontradoException;
import cl.dgt.testing.services.ProductoService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

// El repositorio real se sustituye por un doble: se prueba UNA pieza, no la cadena.
@ExtendWith(MockitoExtension.class)
class ProductoServiceConDobleTest {

    @Mock
    private ProductoRepository repositorio;

    @Test
    void elValorDelCatalogoSumaLosPreciosConIva() {
        when(repositorio.todos()).thenReturn(List.of(
                new Producto(1L, "Uno", 1000),
                new Producto(2L, "Dos", 2000)));

        ProductoService servicio = new ProductoService(repositorio);

        // 1190 + 2380
        assertEquals(3570, servicio.valorDelCatalogo());
        verify(repositorio).todos();
    }

    @Test
    void siElRepositorioNoTraeNadaSeLanzaLaExcepcion() {
        when(repositorio.porId(7L)).thenReturn(Optional.empty());

        ProductoService servicio = new ProductoService(repositorio);

        assertThrows(ProductoNoEncontradoException.class, () -> servicio.porId(7L));
        verify(repositorio).porId(7L);
    }
}
