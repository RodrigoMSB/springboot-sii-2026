package cl.dgt.testing.exceptions;

public class ProductoNoEncontradoException extends RuntimeException {

    private final Long id;

    public ProductoNoEncontradoException(Long id) {
        super("No existe el producto " + id);
        this.id = id;
    }

    public Long getId() {
        return id;
    }
}
