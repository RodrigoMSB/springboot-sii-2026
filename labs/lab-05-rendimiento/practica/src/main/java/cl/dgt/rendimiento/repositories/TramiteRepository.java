package cl.dgt.rendimiento.repositories;

import cl.dgt.rendimiento.entities.Tramite;
import org.springframework.data.jpa.repository.JpaRepository;

/** Lo heredado y nada más: hoy todo lo interesante pasa por el otro repositorio. */
public interface TramiteRepository extends JpaRepository<Tramite, Long> {
}
