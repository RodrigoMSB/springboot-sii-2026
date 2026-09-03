package cl.dgt.tramites.repositories;

import cl.dgt.tramites.entities.Tramite;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TramiteRepository extends JpaRepository<Tramite, Long> {
}
