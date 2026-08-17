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
 *
 * <p>{@code @Service} es, técnicamente, lo mismo que {@code @Component}: hace que Spring encuentre
 * la clase, la construya una vez y la guarde. Lo que cambia es lo que le dice a quien lee el
 * código — «aquí viven las reglas del negocio»—, y que herramientas y convenciones del ecosistema
 * se apoyan en esa distinción.
 */
@Service
public class ProductoService {

    // `final` a propósito: se asigna en el constructor y ya no cambia nunca. El
    // compilador garantiza que no queda sin asignar, y deja claro que este
    // objeto no reemplaza su repositorio a mitad de vida.
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

    /**
     * El catálogo completo.
     *
     * <p>Hoy solo reenvía la llamada, y eso está bien: el sitio existe antes que la regla. Cuando
     * aparezca la primera —ocultar los productos sin stock, aplicar un descuento— irá aquí, y ni
     * el controller ni el repositorio se enterarán.
     */
    public List<Producto> catalogo() {
        return repositorio.todos();
    }

    /** Uno, si existe. El {@code Optional} viaja intacto hasta el controller, que decide el 404. */
    public Optional<Producto> porId(Long id) {
        return repositorio.porId(id);
    }

    // =========================================================================
    //  QUIÉN ME ATIENDE
    // -------------------------------------------------------------------------
    //  El método que convierte la pregunta del paso 3 en un dato. `getClass()`
    //  pregunta en tiempo de ejecución de qué clase es REALMENTE el objeto que
    //  llegó por el constructor —no el tipo declarado, que es la interfaz— y
    //  `getSimpleName()` se queda con el nombre sin el paquete.
    //  Qué se espera ver: "ProductoRepositoryLista", un nombre que este archivo
    //  no menciona en ninguna parte.
    //  Para pensar: si mañana hubiera una tercera implementación, ¿habría que
    //  tocar este método?
    // =========================================================================
    public String quienMeAtiende() {
        return repositorio.getClass().getSimpleName();
    }
}
