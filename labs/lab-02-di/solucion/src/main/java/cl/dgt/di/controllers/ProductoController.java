package cl.dgt.di.controllers;

import cl.dgt.di.models.Producto;
import cl.dgt.di.services.ProductoService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Lo único que hace este controller es traducir HTTP a llamadas Java y devolver el resultado.
 *
 * <p>No sabe de dónde salen los productos. Ni siquiera sabe que existe un repositorio: le pide al
 * servicio, y el servicio se arregla. Esa ignorancia es la que permite cambiar la pieza de abajo
 * sin tocar este archivo — que es lo que se comprueba en el paso 5.
 *
 * <p>{@code @RequestMapping("/productos")} en la clase es un prefijo: se antepone a la ruta de
 * cada método. Así {@code @GetMapping("/quien")} atiende {@code /productos/quien}, y el
 * {@code @GetMapping} sin ruta atiende {@code /productos} a secas. Evita repetir el prefijo cinco
 * veces y hace que cambiarlo sea una sola línea.
 */
@RestController
@RequestMapping("/productos")
public class ProductoController {

    private final ProductoService servicio;

    // =========================================================================
    //  EL CONSTRUCTOR ES LA DECLARACIÓN DE NECESIDADES
    // -------------------------------------------------------------------------
    //  Aquí no hay ningún `new`. Este constructor dice "para funcionar necesito
    //  un ProductoService", y Spring lo lee al arrancar: busca quién cumple, lo
    //  construye si hace falta, y lo pasa por este parámetro.
    //  No lleva @Autowired y no le hace falta: cuando una clase tiene UN solo
    //  constructor, Spring usa ese sin que nadie se lo diga.
    //  Para pensar: ¿qué pasaría si hubiera dos constructores?
    // =========================================================================
    public ProductoController(ProductoService servicio) {
        this.servicio = servicio;
    }

    /**
     * El catálogo, en {@code GET /productos}.
     *
     * <p>Devolver una {@code List} de records basta: Jackson la convierte en un array JSON, y el
     * {@code Content-Type} pasa a {@code application/json} solo, por el tipo devuelto.
     */
    @GetMapping
    public List<Producto> listar() {
        return servicio.catalogo();
    }

    // =========================================================================
    //  QUIÉN ME ATIENDE
    // -------------------------------------------------------------------------
    //  El endpoint que contesta la pregunta del laboratorio con un dato en vez
    //  de con una explicación: dice el nombre de la clase que Spring eligió y
    //  construyó. Cambiando una anotación en los repositorios, esta respuesta
    //  cambia, y ni este archivo ni el servicio se tocan.
    //  Qué se espera ver: "ProductoRepositoryLista", porque es la @Primary.
    //  Para pensar: ¿por qué este archivo puede decir eso sin importar la clase?
    // =========================================================================
    @GetMapping("/quien")
    public String quien() {
        return servicio.quienMeAtiende();
    }

    // =========================================================================
    //  UNO, O 404
    // -------------------------------------------------------------------------
    //  El Optional del repositorio llega hasta aquí sin abrirse, y es este
    //  método el que decide qué significa en HTTP: `map` lo convierte en un 200
    //  con el producto dentro si venía lleno, y `orElseGet` produce el 404 si
    //  venía vacío. Ni un `if`, ni un `null`.
    //  Spring convierte el "7" de la URL al Long del parámetro por su cuenta; si
    //  se pidiera /productos/abc, respondería 400 sin llegar a este método.
    //  Ese 404 sale con el cuerpo VACÍO. Darle forma es el Lab 03.
    // =========================================================================
    @GetMapping("/{id}")
    public ResponseEntity<Producto> porId(@PathVariable Long id) {
        return servicio.porId(id)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }
}
