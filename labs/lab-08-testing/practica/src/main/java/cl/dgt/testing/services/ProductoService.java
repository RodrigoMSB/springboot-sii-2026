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

    // Sin dependencias: entra un número, sale otro. El método más fácil de testear del proyecto.
    public int precioConIva(int precioNeto) {
        return (int) Math.round(precioNeto * (1 + TASA_IVA));
    }

    public List<Producto> todos() {
        return repositorio.todos();
    }

    // El camino triste es parte del contrato: si no está, se avisa con una excepción propia.
    public Producto porId(Long id) {
        return repositorio.porId(id)
                .orElseThrow(() -> new ProductoNoEncontradoException(id));
    }

    /** Descuento por volumen. 3 o más unidades, 10 %. 10 o más, 20 %. */
    public int totalConDescuento(Long id, int cantidad) {
        if (cantidad <= 0) {
            throw new IllegalArgumentException("La cantidad tiene que ser mayor que cero: " + cantidad);
        }

        int bruto = precioConIva(porId(id).precioNeto()) * cantidad;

        double descuento = 0.0;
        if (cantidad >= 10) {
            descuento = 0.20;
        } else if (cantidad >= 3) {
            descuento = 0.10;
        }

        return (int) Math.round(bruto * (1 - descuento));
    }
}
