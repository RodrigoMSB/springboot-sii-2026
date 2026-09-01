package cl.dgt.muchosamuchos.repositories;

import cl.dgt.muchosamuchos.entities.Tramite;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TramiteRepository extends JpaRepository<Tramite, Long> {

    List<Tramite> findByDocumentosCodigo(String codigo);
}
