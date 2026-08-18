package cl.dgt.di.repositories;

import cl.dgt.di.models.Producto;

import java.util.List;
import java.util.Optional;

public interface ProductoRepository {

    List<Producto> todos();

    Optional<Producto> porId(Long id);
}
