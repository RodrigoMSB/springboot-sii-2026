package cl.dgt.errores.exceptions;

/**
 * «Ese producto no está», dicho por el código en vez de por un {@code Optional} vacío que revienta
 * más adelante.
 *
 * <p>Tener una excepción propia es lo que permite distinguir este caso —previsto, normal, culpa de
 * quien pregunta— de un fallo de verdad. Sin ella, todo es la misma clase de problema.
 *
 * <p>Fíjate en que no lleva ninguna anotación de HTTP: esta clase no sabe qué es un 404. De
 * traducirla a HTTP se encarga el manejador, y por eso se podría reutilizar fuera de una API web.
 */
public class ProductoNoEncontradoException extends RuntimeException {

    public ProductoNoEncontradoException(Long id) {
        super("No existe el producto con id " + id + ".");
    }
}
