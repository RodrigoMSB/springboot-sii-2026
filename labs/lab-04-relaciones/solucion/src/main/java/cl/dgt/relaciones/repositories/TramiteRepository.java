package cl.dgt.relaciones.repositories;

import cl.dgt.relaciones.entities.Tramite;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TramiteRepository extends JpaRepository<Tramite, Long> {

    List<Tramite> findByContribuyenteRut(String rut);
}
