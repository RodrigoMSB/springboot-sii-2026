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

/**
 * Este controller VIENE DADO y funciona. En este laboratorio no se escriben endpoints: se les da
 * forma a sus errores.
 *
 * <p>Solo cambian dos líneas en toda la sesión —el {@code orElseThrow()} del paso 2 y un
 * {@code @Valid} en el paso 4—. Todo lo demás que se escribe hoy vive fuera de este archivo, y que
 * viva fuera es justamente la idea.
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
    //  del sistema. Y sin embargo mira lo que devuelve esta línea tal como está:
    //  `.orElseThrow()` a secas lanza NoSuchElementException, que nadie tradujo.
    //  Ese es el paso 1, y el paso 2 cambia esta línea.
    //  Qué se espera ver: hoy, un 500 contando de más.
    //  Para pensar: ¿de quién es la culpa de este error, del sistema o de quien pregunta?
    // =========================================================================
    @GetMapping("/{id}")
    public Producto porId(@PathVariable Long id) {
        return base.stream()
                .filter(p -> p.id().equals(id))
                .findFirst()
                .orElseThrow();
    }

    // =========================================================================
    //  EL ERROR NO PREVISTO
    // -------------------------------------------------------------------------
    //  El precio dividido en cuotas. Con `cuotas=0` esto lanza una
    //  ArithmeticException que nadie escribió ni pensó: es el error de verdad,
    //  el que no está en ninguna lista. Se usa en el paso 5.
    //  Qué se espera ver: sin manejador, la respuesta cuenta media arquitectura.
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
    //  Tal como está, este POST acepta cualquier cosa: nombre vacío, precio
    //  negativo. En el paso 4 se le pone un @Valid delante del @RequestBody y las
    //  anotaciones del DTO empiezan a significar algo.
    //  Qué se espera ver: hoy, un 201 con basura dentro.
    //  Para pensar: ¿dónde habría que comprobar esto si no existiera @Valid, y en
    //  cuántos sitios se repetiría?
    // =========================================================================
    @PostMapping
    public ResponseEntity<Producto> crear(@RequestBody ProductoNuevoDto nuevo) {
        Producto producto = new Producto(siguienteId.getAndIncrement(), nuevo.nombre(), nuevo.precio());
        base.add(producto);
        return ResponseEntity.status(HttpStatus.CREATED).body(producto);
    }
}
