package cl.dgt.testing.repositories;

import cl.dgt.testing.models.Producto;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

// El almacén de hoy es una lista en memoria: este lab es sobre testear, no sobre persistir.
@Repository
public class ProductoRepositoryLista implements ProductoRepository {

    private static final List<Producto> DATOS = List.of(
            new Producto(1L, "Resma de papel carta", 4990),
            new Producto(2L, "Tóner negro", 68900),
            new Producto(3L, "Silla ergonómica", 129900),
            new Producto(4L, "Monitor 24 pulgadas", 149900));

    @Override
    public List<Producto> todos() {
        return DATOS;
    }

    @Override
    public Optional<Producto> porId(Long id) {
        return DATOS.stream().filter(p -> p.id().equals(id)).findFirst();
    }
}
