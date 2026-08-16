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
 * Spring ofrece un atajo —{@code @ResponseStatus(NOT_FOUND)} sobre la clase— que se ha evitado a
 * propósito: ataría el dominio al protocolo y dejaría el código repartido en dos sitios.
 *
 * <p>Extiende {@code RuntimeException} y no {@code Exception}: así no hay que declararla con
 * {@code throws} en cada método por el que pase. Para un error que se traduce en un solo sitio, no
 * aporta nada obligar a media aplicación a mencionarla.
 *
 * <p>El mensaje se arma en el constructor, con el id dentro. Ese texto es el que acabará saliendo
 * por la API, así que está escrito para quien llama —no para quien depura.
 */
public class ProductoNoEncontradoException extends RuntimeException {

    public ProductoNoEncontradoException(Long id) {
        super("No existe el producto con id " + id + ".");
    }
}
