package cl.dgt.testing;

import cl.dgt.testing.repositories.ProductoRepositoryLista;
import cl.dgt.testing.exceptions.ProductoNoEncontradoException;
import cl.dgt.testing.services.ProductoService;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

// Sin Spring: el servicio se construye a mano y el test corre en milisegundos.
class ProductoServiceTest {

    private final ProductoService servicio = new ProductoService(new ProductoRepositoryLista());

    @Test
    void elPrecioConIvaSeRedondeaAlPesoMasCercano() {
        int conIva = servicio.precioConIva(4990);

        assertEquals(5938, conIva);
    }

    @Test
    void elCatalogoTraeLosCuatroProductos() {
        assertEquals(4, servicio.todos().size());
    }

    @Test
    void unIdQueExisteDevuelveElProducto() {
        assertEquals("Tóner negro", servicio.porId(2L).nombre());
    }

    @Test
    void unIdQueNoExisteLanzaProductoNoEncontrado() {
        ProductoNoEncontradoException e =
                assertThrows(ProductoNoEncontradoException.class, () -> servicio.porId(99L));

        assertEquals(99L, e.getId());
    }
}
