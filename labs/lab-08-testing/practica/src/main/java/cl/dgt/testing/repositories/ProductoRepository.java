package cl.dgt.testing.repositories;

import cl.dgt.testing.models.Producto;

import java.util.List;
import java.util.Optional;

public interface ProductoRepository {

    List<Producto> todos();

    Optional<Producto> porId(Long id);
}
