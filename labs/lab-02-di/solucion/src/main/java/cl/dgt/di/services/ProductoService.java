package cl.dgt.di.services;

import cl.dgt.di.models.Producto;
import cl.dgt.di.repositories.ProductoRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class ProductoService {

    private final ProductoRepository repositorio;

    public ProductoService(ProductoRepository repositorio) {
        this.repositorio = repositorio;
    }

    public List<Producto> catalogo() {
        return repositorio.todos();
    }

    public Optional<Producto> porId(Long id) {
        return repositorio.porId(id);
    }

    // Devuelve la clase que Spring inyectó de verdad: el endpoint del laboratorio.
    public String quienMeAtiende() {
        return repositorio.getClass().getSimpleName();
    }
}
