package cl.dgt.relaciones.repositories;

import cl.dgt.relaciones.entities.Contribuyente;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ContribuyenteRepository extends JpaRepository<Contribuyente, Long> {

    Optional<Contribuyente> findByRut(String rut);
}
