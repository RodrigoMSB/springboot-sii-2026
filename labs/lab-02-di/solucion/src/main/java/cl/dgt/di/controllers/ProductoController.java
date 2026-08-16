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
 */
@RestController
@RequestMapping("/productos")
public class ProductoController {

    private final ProductoService servicio;

    public ProductoController(ProductoService servicio) {
        this.servicio = servicio;
    }

    /** El catálogo. */
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

    /** Uno, o 404. Lo mismo que se aprendió en el Lab 01. */
    @GetMapping("/{id}")
    public ResponseEntity<Producto> porId(@PathVariable Long id) {
        return servicio.porId(id)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }
}
