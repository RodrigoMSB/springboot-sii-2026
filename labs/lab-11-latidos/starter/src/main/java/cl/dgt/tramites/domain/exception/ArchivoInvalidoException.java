package cl.dgt.tramites.domain.exception;

/** El archivo subido no pasa la desconfianza: MIME no permitido, muy grande, o nombre peligroso. */
public class ArchivoInvalidoException extends RuntimeException {
    public ArchivoInvalidoException(String mensaje) {
        super(mensaje);
    }
}
