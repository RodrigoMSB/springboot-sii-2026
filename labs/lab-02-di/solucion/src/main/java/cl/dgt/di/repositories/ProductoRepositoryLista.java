package cl.dgt.di.repositories;

import cl.dgt.di.models.Producto;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * La implementación de verdad: los datos viven en una lista en memoria.
 *
 * <p>Dos anotaciones y ninguna es decorativa:
 *
 * <ul>
 *   <li>{@code @Repository} — «Spring, esta clase te interesa». Al arrancar, {@code @ComponentScan}
 *       la encuentra, la construye <strong>una sola vez</strong> y se la guarda para dársela a
 *       quien la pida. Es un {@code @Component} con un nombre más preciso: funcionalmente hace lo
 *       mismo, pero dice para qué sirve la clase y, en proyectos con base de datos, además traduce
 *       las excepciones del driver a las de Spring.
 *   <li>{@code @Primary} — «cuando haya varias que cumplan el contrato, esta es la de por
 *       defecto». Llegó en el paso 5, para resolver el error del paso 4.
 * </ul>
 *
 * <p>El objeto que Spring construye se llama <strong>bean</strong>, y su nombre por defecto es el
 * de la clase con la primera letra en minúscula: {@code productoRepositoryLista}. Ese nombre es el
 * que aparece en el error del paso 4 y el que usa el {@code @Qualifier} del paso 5.
 */
@Repository
@Primary
public class ProductoRepositoryLista implements ProductoRepository {

    // =========================================================================
    //  LOS DATOS
    // -------------------------------------------------------------------------
    //  `static` porque son los mismos para todos: Spring construye esta clase
    //  una sola vez, así que en la práctica daría igual, pero deja dicho que no
    //  dependen de la instancia.
    //  `final` + `List.of(...)` los deja INMUTABLES. Importa: a un repositorio
    //  llegan varias peticiones a la vez, y una lista que nadie puede modificar
    //  no tiene problemas de concurrencia — es gratis y evita una clase entera
    //  de errores. En un sistema real esto sería una consulta a la base.
    // =========================================================================
    private static final List<Producto> DATOS = List.of(
            new Producto(1L, "Resma de papel carta", 4990),
            new Producto(2L, "Tóner negro", 68900),
            new Producto(3L, "Silla ergonómica", 129900),
            new Producto(4L, "Monitor 24 pulgadas", 149900));

    // =========================================================================
    //  @Override — la única anotación de aquí que no es de Spring
    // -------------------------------------------------------------------------
    //  Es de Java, y no cambia lo que hace el método: le pide al compilador que
    //  compruebe que de verdad está implementando algo del contrato. Si alguien
    //  renombra `todos()` en la interfaz y se olvida de esta clase, el error sale
    //  al compilar en vez de en tiempo de ejecución.
    //  Se puede quitar y todo seguiría funcionando. Es una red, no un requisito.
    // =========================================================================

    /** Devuelve la lista tal cual. Se puede porque es inmutable: nadie la va a estropear. */
    @Override
    public List<Producto> todos() {
        return DATOS;
    }

    // =========================================================================
    //  BUSCAR UNO
    // -------------------------------------------------------------------------
    //  `stream()` recorre la lista, `filter` se queda con los que cumplan, y
    //  `findFirst()` devuelve el primero envuelto en un Optional — vacío si no
    //  hubo ninguno. Justo lo que pide el contrato.
    //  `p.id().equals(id)` y no `==`: Long es un objeto, y `==` compararía si
    //  son el MISMO objeto en memoria, no si valen lo mismo. Con valores
    //  pequeños funcionaría por accidente (Java cachea los Long de -128 a 127) y
    //  fallaría con un id grande. Es una de las trampas clásicas del lenguaje.
    // =========================================================================
    @Override
    public Optional<Producto> porId(Long id) {
        return DATOS.stream().filter(p -> p.id().equals(id)).findFirst();
    }
}
