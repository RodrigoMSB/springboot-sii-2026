package cl.dgt.tramites.infrastructure.repository;

import cl.dgt.tramites.domain.entity.Tramite;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TramiteRepository extends JpaRepository<Tramite, Long> {
}
