package cl.dgt.seguridad.controllers;

import cl.dgt.seguridad.models.Producto;
import cl.dgt.seguridad.repositories.ProductoRepositoryLista;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/productos")
public class ProductoController {

    private final ProductoRepositoryLista repositorio;

    public ProductoController(ProductoRepositoryLista repositorio) {
        this.repositorio = repositorio;
    }

    @GetMapping
    public List<Producto> listar() {
        return repositorio.todos();
    }

    // Paso 5 · devuelve el usuario y los roles que vienen dentro del token.
    // escribe aquí

    // Paso 6 · devuelve un mensaje, y sólo para ADMIN.
    // escribe aquí

    @GetMapping("/{id}")
    public ResponseEntity<Producto> porId(@PathVariable Long id) {
        return repositorio.porId(id)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }
}
