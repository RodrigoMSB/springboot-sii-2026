package cl.dgt.tramites.domain.exception;

import cl.dgt.tramites.domain.tipo.EstadoTramite;

/** Se intentó mover un trámite por un camino que la máquina de estados no permite. */
public class TransicionIlegalException extends RuntimeException {

    private final EstadoTramite origen;
    private final EstadoTramite destino;

    public TransicionIlegalException(EstadoTramite origen, EstadoTramite destino) {
        super("Transición ilegal: " + origen + " -> " + destino
              + ". Un trámite avanza, nunca retrocede ni salta etapas.");
        this.origen = origen;
        this.destino = destino;
    }

    public EstadoTramite getOrigen() { return origen; }
    public EstadoTramite getDestino() { return destino; }
}
