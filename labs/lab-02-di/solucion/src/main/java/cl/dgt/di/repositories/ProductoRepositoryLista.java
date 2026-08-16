package cl.dgt.di.repositories;

import cl.dgt.di.models.Producto;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * La implementación de verdad: los datos viven en una lista en memoria.
 *
 * <p>Dos anotaciones y ninguna es decorativa:
 *
 * <ul>
 *   <li>{@code @Repository} — «Spring, esta clase te interesa». Al arrancar, Spring la encuentra,
 *       la construye una sola vez, y se la guarda para dársela a quien la pida.
 *   <li>{@code @Primary} — «cuando haya varias que cumplan el contrato, esta es la de por
 *       defecto». Llegó en el paso 5, para resolver el error del paso 4.
 * </ul>
 */
@Repository
@Primary
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
