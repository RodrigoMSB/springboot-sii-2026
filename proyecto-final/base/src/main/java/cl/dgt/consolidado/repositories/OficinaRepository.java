package cl.dgt.consolidado.repositories;

import cl.dgt.consolidado.entities.Oficina;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface OficinaRepository extends JpaRepository<Oficina, Long> {

    Optional<Oficina> findByCodigo(String codigo);
}
