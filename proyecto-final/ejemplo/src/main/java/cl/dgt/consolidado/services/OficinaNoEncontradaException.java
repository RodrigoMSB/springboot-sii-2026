// La excepción que el manejador convierte en 404.
// Tu equivalente: `ContribuyenteNoEncontradoException`, con el RUT en vez del código.
package cl.dgt.consolidado.services;

public class OficinaNoEncontradaException extends RuntimeException {

    public OficinaNoEncontradaException(String codigo) {
        super("No existe la oficina " + codigo);
    }
}
// ^ Excepción PROPIA y no una genérica: describe algo del dominio, y por eso el manejador puede
//   traducirla a un 404 sin adivinar. El mensaje viaja al cuerpo de la respuesta.
