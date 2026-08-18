package cl.dgt.observabilidad.repositories;

import cl.dgt.observabilidad.entities.Tramite;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TramiteRepository extends JpaRepository<Tramite, Long> {
}
