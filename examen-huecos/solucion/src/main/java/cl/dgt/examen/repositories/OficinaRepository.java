package cl.dgt.examen.repositories;

import cl.dgt.examen.entities.Oficina;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface OficinaRepository extends JpaRepository<Oficina, Long> {

    /** Viene resuelta: la usan la ficha y el resumen. */
    Optional<Oficina> findByCodigo(String codigo);

    List<Oficina> findByComuna(String comuna);
}
