package cl.dgt.concurrencia.repositories;

import cl.dgt.concurrencia.entities.Contribuyente;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ContribuyenteRepository extends JpaRepository<Contribuyente, Long> {
}
