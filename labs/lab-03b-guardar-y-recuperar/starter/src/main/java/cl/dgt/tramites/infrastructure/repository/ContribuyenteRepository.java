package cl.dgt.tramites.infrastructure.repository;

import cl.dgt.tramites.domain.entity.Contribuyente;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/**
 * Vive en {@code infrastructure} y no en {@code domain} por una razón que AU-03 hace
 * cumplir: extiende Spring Data, y el dominio no conoce a Spring.
 */
public interface ContribuyenteRepository extends JpaRepository<Contribuyente, Long> {

    Optional<Contribuyente> findByRut(String rut);
}
