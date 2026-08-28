package cl.dgt.examen.exceptions;

/** Viene resuelta: la lanza el servicio cuando el id no existe. */
public class SolicitudNoEncontrada extends RuntimeException {

    private final Long id;

    public SolicitudNoEncontrada(Long id) {
        super("No existe la solicitud " + id);
        this.id = id;
    }

    public Long getId() {
        return id;
    }
}
