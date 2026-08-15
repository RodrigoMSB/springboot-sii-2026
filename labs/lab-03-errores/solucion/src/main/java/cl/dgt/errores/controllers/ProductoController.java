package cl.dgt.errores.controllers;

import cl.dgt.errores.dto.ProductoNuevoDto;
import cl.dgt.errores.exceptions.ProductoNoEncontradoException;
import cl.dgt.errores.models.Producto;
import jakarta.validation.Valid;
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

/**
 * El controller viene dado: en este laboratorio no se escriben endpoints, se les da forma a sus
 * errores.
 *
 * <p>Solo cambian dos líneas en toda la sesión: el {@code orElseThrow()} del paso 2 y el
 * {@code @Valid} del paso 4. Lo demás que se escribe hoy vive fuera de este archivo — y que viva
 * fuera es justamente la idea.
 */
@RestController
@RequestMapping("/productos")
public class ProductoController {

    private final List<Producto> base = new ArrayList<>(List.of(
            new Producto(1L, "Resma de papel carta", 4990),
            new Producto(2L, "Tóner negro", 68900),
            new Producto(3L, "Silla ergonómica", 129900)));

    private final AtomicLong siguienteId = new AtomicLong(4);

    /** El catálogo. Nunca falla. */
    @GetMapping
    public List<Producto> listar() {
        return base;
    }

    // =========================================================================
    //  EL ERROR PREVISTO
    // -------------------------------------------------------------------------
    //  Pedir un id que no existe es normal: pasa todos los días y no es culpa
    //  del sistema. En el paso 1 esta línea termina en `.orElseThrow()` a secas
    //  —que lanza NoSuchElementException— y se ve lo que sale por la API.
    //  El paso 2 la cambia por la excepción propia de abajo.
    //  Qué se espera ver: 404 con cuerpo, en vez de 500 con traza.
    //  Para pensar: ¿por qué esta clase no menciona el número 404 en ningún sitio?
    // =========================================================================
    @GetMapping("/{id}")
    public Producto porId(@PathVariable Long id) {
        return base.stream()
                .filter(p -> p.id().equals(id))
                .findFirst()
                .orElseThrow(() -> new ProductoNoEncontradoException(id));
    }

    // =========================================================================
    //  EL ERROR NO PREVISTO
    // -------------------------------------------------------------------------
    //  El precio dividido en cuotas. Con `cuotas=0` esto lanza una
    //  ArithmeticException que nadie escribió ni pensó: es el error de verdad,
    //  el que no está en ninguna lista. Existe para el paso 5.
    //  Qué se espera ver: sin manejador general, la respuesta cuenta media
    //  arquitectura del sistema.
    //  Para pensar: ¿cuántos errores así hay en un sistema real? (Todos los que
    //  no se han encontrado todavía.)
    // =========================================================================
    @GetMapping("/{id}/cuota")
    public int cuota(@PathVariable Long id, @RequestParam int cuotas) {
        Producto producto = porId(id);
        return producto.precio() / cuotas;
    }

    // =========================================================================
    //  EL ERROR DE QUIEN LLAMA
    // -------------------------------------------------------------------------
    //  @Valid llegó en el paso 4. Es lo que hace que las anotaciones del DTO se
    //  comprueben ANTES de entrar aquí: si el cuerpo no cumple, este método no
    //  llega a ejecutarse nunca.
    //  Qué se espera ver: 400 con la lista de campos que fallaron, no un 500.
    //  Para pensar: ¿dónde habría que poner estas comprobaciones si no existiera
    //  @Valid, y en cuántos sitios se repetirían?
    // =========================================================================
    @PostMapping
    public ResponseEntity<Producto> crear(@Valid @RequestBody ProductoNuevoDto nuevo) {
        Producto producto = new Producto(siguienteId.getAndIncrement(), nuevo.nombre(), nuevo.precio());
        base.add(producto);
        return ResponseEntity.status(HttpStatus.CREATED).body(producto);
    }
}
