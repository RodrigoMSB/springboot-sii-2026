package cl.dgt.concurrencia.repositories;

import cl.dgt.concurrencia.entities.Tramite;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TramiteRepository extends JpaRepository<Tramite, Long> {
}
