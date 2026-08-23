package cl.dgt.testing.services;

import cl.dgt.testing.exceptions.ProductoNoEncontradoException;
import cl.dgt.testing.models.Producto;
import cl.dgt.testing.repositories.ProductoRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ProductoService {

    private static final double TASA_IVA = 0.19;

    private final ProductoRepository repositorio;

    public ProductoService(ProductoRepository repositorio) {
        this.repositorio = repositorio;
    }

    public int precioConIva(int precioNeto) {
        return (int) Math.round(precioNeto * (1 + TASA_IVA));
    }

    public List<Producto> todos() {
        return repositorio.todos();
    }

    public Producto porId(Long id) {
        return repositorio.porId(id)
                .orElseThrow(() -> new ProductoNoEncontradoException(id));
    }

    public int valorDelCatalogo() {
        return repositorio.todos().stream()
                .mapToInt(p -> precioConIva(p.precioNeto()))
                .sum();
    }
}
