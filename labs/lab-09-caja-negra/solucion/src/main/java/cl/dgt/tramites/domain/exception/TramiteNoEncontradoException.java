package cl.dgt.tramites.domain.exception;

/** No existe trámite con ese identificador. */
public class TramiteNoEncontradoException extends RuntimeException {

    private final Long id;

    public TramiteNoEncontradoException(Long id) {
        super("No existe un trámite con id " + id);
        this.id = id;
    }

    public Long getId() { return id; }
}
