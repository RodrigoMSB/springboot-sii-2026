package cl.dgt.tramites.domain.tipo;

import java.util.Set;

/**
 * Los cuatro estados de un trámite, y las únicas transiciones legales entre ellos.
 *
 * <p>Vive en {@code domain.tipo} y no en {@code domain.entity} porque no es una entidad:
 * es un vocabulario. La distinción no es estética — AU-01 prohíbe que la web dependa de
 * {@code ..domain.entity..}, y el {@code ManejadorDeErrores} necesita nombrar estos
 * estados para explicar una transición ilegal. La regla cazó el paquete mal puesto el
 * primer día. En {@code domain.entity} viven las siete entidades, y nada más.
 *
 * <p>La máquina avanza y nunca retrocede: {@code BORRADOR → PRESENTADO → PAGADO →
 * FOLIADO}. No hay saltos (un trámite no se folia sin pagarse) ni vuelta atrás (un
 * folio emitido no se borra: se explica, ante un fiscalizador).
 *
 * <p>Las transiciones viven <em>aquí</em>, en el enum, y no en un {@code if} del
 * servicio: así el dominio no puede ser esquivado por un caso de uso apurado.
 */
public enum EstadoTramite {

    BORRADOR,
    PRESENTADO,
    PAGADO,
    FOLIADO;

    /** Sucesores legales de cada estado. FOLIADO es terminal. */
    private static Set<EstadoTramite> sucesoresDe(EstadoTramite estado) {
        return switch (estado) {
            case BORRADOR   -> Set.of(PRESENTADO);
            case PRESENTADO -> Set.of(PAGADO);
            case PAGADO     -> Set.of(FOLIADO);
            case FOLIADO    -> Set.of();
        };
    }

    /** ¿Puede este estado convertirse en {@code destino}? */
    public boolean puedeTransicionarA(EstadoTramite destino) {
        return sucesoresDe(this).contains(destino);
    }

    /** {@code true} si ya no admite más transiciones. */
    public boolean esTerminal() {
        return sucesoresDe(this).isEmpty();
    }
}
