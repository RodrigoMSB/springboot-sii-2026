package cl.dgt.di.repositories;

import cl.dgt.di.models.Producto;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * La segunda implementación del mismo contrato, con datos que se reconocen a simple vista.
 *
 * <p>Existe para probar una idea: el resto de la aplicación —el servicio, el controller— no
 * cambia ni una línea según cuál de las dos esté puesta. Por eso los datos son ridículos: si
 * aparecen en la respuesta, se sabe al instante cuál se inyectó.
 *
 * <p>En un proyecto real esta sería la implementación para pruebas, o la que responde mientras el
 * sistema de verdad todavía no existe.
 */
@Repository
public class ProductoRepositoryFalso implements ProductoRepository {

    private static final List<Producto> DATOS = List.of(
            new Producto(1L, "PRODUCTO DE PRUEBA UNO", 1),
            new Producto(2L, "PRODUCTO DE PRUEBA DOS", 2));

    @Override
    public List<Producto> todos() {
        return DATOS;
    }

    @Override
    public Optional<Producto> porId(Long id) {
        return DATOS.stream().filter(p -> p.id().equals(id)).findFirst();
    }
}
