package cl.dgt.seguridad.repositories;

import cl.dgt.seguridad.models.Producto;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class ProductoRepositoryLista {

    private static final List<Producto> DATOS = List.of(
            new Producto(1L, "Resma de papel carta", 4990),
            new Producto(2L, "Tóner negro", 68900),
            new Producto(3L, "Silla ergonómica", 129900),
            new Producto(4L, "Monitor 24 pulgadas", 149900));

    public List<Producto> todos() {
        return DATOS;
    }

    public Optional<Producto> porId(Long id) {
        return DATOS.stream().filter(p -> p.id().equals(id)).findFirst();
    }
}
