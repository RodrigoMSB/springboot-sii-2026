package cl.dgt.tramites.domain.exception;

/**
 * Tesorería no contestó a tiempo (o contestó mal). Es una excepción de dominio: no sabe que existe
 * HTTP ni que se traducirá en un 503. Su sola existencia es la degradación elegante: en vez de
 * colgar el hilo esperando, la operación falla rápido y deja que la capa web dé la mala noticia.
 */
public class TesoreriaNoDisponibleException extends RuntimeException {

    public TesoreriaNoDisponibleException(String mensaje, Throwable causa) {
        super(mensaje, causa);
    }
}
