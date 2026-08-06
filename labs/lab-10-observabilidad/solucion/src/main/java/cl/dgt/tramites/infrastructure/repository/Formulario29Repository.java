package cl.dgt.tramites.infrastructure.repository;

import cl.dgt.tramites.domain.entity.Formulario29;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/**
 * El F29 de un trámite. La relación es 1:1 —lo garantiza el {@code UNIQUE (tramite_id)} de la V1—,
 * así que la búsqueda por trámite devuelve un {@link Optional}, no una lista.
 */
public interface Formulario29Repository extends JpaRepository<Formulario29, Long> {

    Optional<Formulario29> findByTramiteId(Long tramiteId);
}
