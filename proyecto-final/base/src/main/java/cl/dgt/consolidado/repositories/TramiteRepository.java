package cl.dgt.consolidado.repositories;

import cl.dgt.consolidado.entities.Tramite;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TramiteRepository extends JpaRepository<Tramite, Long> {
}
