package cl.dgt.di.repositories;

import cl.dgt.di.models.Producto;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

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
