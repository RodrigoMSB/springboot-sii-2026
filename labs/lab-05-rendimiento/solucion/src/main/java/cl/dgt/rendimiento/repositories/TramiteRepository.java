package cl.dgt.rendimiento.repositories;

import cl.dgt.rendimiento.entities.Tramite;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TramiteRepository extends JpaRepository<Tramite, Long> {
}
