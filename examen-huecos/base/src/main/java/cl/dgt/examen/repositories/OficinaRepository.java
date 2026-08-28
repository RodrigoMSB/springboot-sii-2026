package cl.dgt.examen.repositories;

import cl.dgt.examen.entities.Oficina;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface OficinaRepository extends JpaRepository<Oficina, Long> {

    /** Viene resuelta: la usan la ficha y el resumen. */
    Optional<Oficina> findByCodigo(String codigo);

    // =========================================================================
    //  HUECO 02 · Las oficinas de una comuna
    // -------------------------------------------------------------------------
    //  Falta la consulta que devuelve todas las oficinas de una comuna dada.
    //  Spring Data la escribe sola si la declaras bien.
    //
    //  ESTA LISTO CUANDO · pasa el test H-02
    // =========================================================================
}
