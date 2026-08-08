package cl.dgt.tramites.infrastructure.repository;

import cl.dgt.tramites.domain.entity.Folio;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface FolioRepository extends JpaRepository<Folio, Long> {

    /** Para la idempotencia (RN-05): un trámite tiene a lo más un folio. */
    Optional<Folio> findByTramiteId(Long tramiteId);
}
