package cl.dgt.errores.controllers;

import cl.dgt.errores.dto.ProductoNuevoDto;
import cl.dgt.errores.models.Producto;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

@RestController
@RequestMapping("/productos")
public class ProductoController {

    private final List<Producto> base = new ArrayList<>(List.of(
            new Producto(1L, "Resma de papel carta", 4990),
            new Producto(2L, "Tóner negro", 68900),
            new Producto(3L, "Silla ergonómica", 129900)));

    private final AtomicLong siguienteId = new AtomicLong(4);

    @GetMapping
    public List<Producto> listar() {
        return base;
    }

    @GetMapping("/{id}")
    public Producto porId(@PathVariable Long id) {
        return base.stream()
                .filter(p -> p.id().equals(id))
                .findFirst()
                .orElseThrow();
    }

    @GetMapping("/{id}/cuota")
    public int cuota(@PathVariable Long id, @RequestParam int cuotas) {
        Producto producto = porId(id);
        return producto.precio() / cuotas;
    }

    @PostMapping
    public ResponseEntity<Producto> crear(@RequestBody ProductoNuevoDto nuevo) {
        Producto producto = new Producto(siguienteId.getAndIncrement(), nuevo.nombre(), nuevo.precio());
        base.add(producto);
        return ResponseEntity.status(HttpStatus.CREATED).body(producto);
    }
}
