package cl.dgt.examen.exceptions;

/** Viene resuelta: la lanza el servicio cuando el código no existe. */
public class OficinaNoEncontrada extends RuntimeException {

    private final String codigo;

    public OficinaNoEncontrada(String codigo) {
        super("No existe la oficina " + codigo);
        this.codigo = codigo;
    }

    public String getCodigo() {
        return codigo;
    }
}
