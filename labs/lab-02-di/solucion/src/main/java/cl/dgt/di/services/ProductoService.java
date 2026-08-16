package cl.dgt.di.services;

import cl.dgt.di.models.Producto;
import cl.dgt.di.repositories.ProductoRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

/**
 * La capa del medio: controller → <strong>service</strong> → repository.
 *
 * <p>Llegó en el paso 6 y hoy casi no hace nada, y eso está bien: el sitio donde vivirán las
 * reglas del negocio se reserva antes de tener reglas. Cuando aparezca la primera —un descuento,
 * un permiso, un cálculo— tendrá dónde ir sin tocar ni el controller ni el repositorio.
 *
 * <p>Fíjate en lo que este servicio NO sabe: cuál de las dos implementaciones de
 * {@link ProductoRepository} le tocó. Pide el contrato y usa lo que le den.
 */
@Service
public class ProductoService {

    private final ProductoRepository repositorio;

    // =========================================================================
    //  EL CONSTRUCTOR ES LA DECLARACIÓN DE NECESIDADES
    // -------------------------------------------------------------------------
    //  Aquí no hay ningún `new`. Este constructor dice "para funcionar necesito
    //  un ProductoRepository", y Spring lo lee al arrancar: busca quién cumple
    //  ese contrato, y lo pasa por este parámetro.
    //  Qué se espera ver: /productos/quien responde el nombre de la clase que
    //  Spring eligió, sin que este archivo la mencione nunca.
    //  Para pensar: ¿en qué otro lugar del proyecto aparece el nombre de la
    //  implementación? (En ninguno, salvo la propia clase.)
    // =========================================================================
    public ProductoService(ProductoRepository repositorio) {
        this.repositorio = repositorio;
    }

    /** El catálogo completo. */
    public List<Producto> catalogo() {
        return repositorio.todos();
    }

    /** Uno, si existe. */
    public Optional<Producto> porId(Long id) {
        return repositorio.porId(id);
    }

    /** Qué implementación acabó inyectada. Responde la pregunta del paso 3 con un dato. */
    public String quienMeAtiende() {
        return repositorio.getClass().getSimpleName();
    }
}
