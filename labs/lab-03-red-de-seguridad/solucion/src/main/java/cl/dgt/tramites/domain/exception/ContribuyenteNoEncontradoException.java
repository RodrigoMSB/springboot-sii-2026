package cl.dgt.tramites.domain.exception;

/** No existe contribuyente con ese RUT. */
public class ContribuyenteNoEncontradoException extends RuntimeException {

    private final String rut;

    public ContribuyenteNoEncontradoException(String rut) {
        super("No existe un contribuyente con RUT " + rut);
        this.rut = rut;
    }

    public String getRut() { return rut; }
}
