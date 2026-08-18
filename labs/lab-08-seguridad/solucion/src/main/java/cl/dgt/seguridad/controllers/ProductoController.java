package cl.dgt.seguridad.controllers;

import cl.dgt.seguridad.models.Producto;
import cl.dgt.seguridad.repositories.ProductoRepositoryLista;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

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

    // Quién eres sale del token, no de un parámetro: el filtro ya lo verificó.
    @GetMapping("/quien-soy")
    public Map<String, Object> quienSoy(@AuthenticationPrincipal Jwt jwt) {
        return Map.of("usuario", jwt.getSubject(), "roles", jwt.getClaimAsString("scope"));
    }

    // La ruta del paso 6: sólo ADMIN. Un USUARIO autenticado recibe 403, no 401.
    @GetMapping("/administracion")
    public Map<String, String> administracion() {
        return Map.of("mensaje", "Sólo un ADMIN ve esto");
    }

    @GetMapping("/{id}")
    public ResponseEntity<Producto> porId(@PathVariable Long id) {
        return repositorio.porId(id)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }
}
