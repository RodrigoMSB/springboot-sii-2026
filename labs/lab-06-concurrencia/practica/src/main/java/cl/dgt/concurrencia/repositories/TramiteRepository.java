package cl.dgt.concurrencia.repositories;

import cl.dgt.concurrencia.entities.Tramite;
import org.springframework.data.jpa.repository.JpaRepository;

/** Viene del Lab 04. Hoy no se usa: está para que el modelo siga siendo el mismo. */
public interface TramiteRepository extends JpaRepository<Tramite, Long> {
}
