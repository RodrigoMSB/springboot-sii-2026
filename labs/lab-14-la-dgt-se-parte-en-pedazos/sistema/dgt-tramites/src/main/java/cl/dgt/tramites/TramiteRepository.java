package cl.dgt.tramites;

import org.springframework.data.jpa.repository.JpaRepository;

/** La base propia de este servicio: trámites, y nada más. */
public interface TramiteRepository extends JpaRepository<Tramite, Long> {
}
