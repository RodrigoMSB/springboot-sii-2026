package cl.dgt.relaciones.repositories;

import cl.dgt.relaciones.entities.Contribuyente;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/** Lo de siempre del Lab 3.5, más una búsqueda por RUT. */
public interface ContribuyenteRepository extends JpaRepository<Contribuyente, Long> {

    Optional<Contribuyente> findByRut(String rut);
}
