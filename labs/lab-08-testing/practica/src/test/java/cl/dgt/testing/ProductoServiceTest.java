package cl.dgt.testing;

import cl.dgt.testing.repositories.ProductoRepositoryLista;
import cl.dgt.testing.services.ProductoService;

// Paso 1 · el primer test: las tres franjas del descuento y la cantidad inválida.
// Un @ParameterizedTest con @CsvSource, sobre `totalConDescuento`.
class ProductoServiceTest {

    private final ProductoService servicio = new ProductoService(new ProductoRepositoryLista());

    // escribe aquí
}
