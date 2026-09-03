package cl.dgt.testing.controllers;

import cl.dgt.testing.models.Producto;
import cl.dgt.testing.services.ProductoService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/productos")
public class ProductoController {

    private final ProductoService servicio;

    public ProductoController(ProductoService servicio) {
        this.servicio = servicio;
    }

    @GetMapping
    public List<Producto> listar() {
        return servicio.todos();
    }

    @GetMapping("/{id}")
    public Producto porId(@PathVariable Long id) {
        return servicio.porId(id);
    }

    // La cotización con descuento por volumen. Es el método que se prueba en el paso 1.
    @GetMapping("/{id}/total")
    public Map<String, Integer> total(@PathVariable Long id,
                                      @RequestParam(defaultValue = "1") int cantidad) {
        return Map.of("total", servicio.totalConDescuento(id, cantidad));
    }
}
