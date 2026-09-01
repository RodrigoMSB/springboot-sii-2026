package cl.dgt.muchosamuchos.repositories;

import cl.dgt.muchosamuchos.entities.Documento;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface DocumentoRepository extends JpaRepository<Documento, Long> {

    Optional<Documento> findByCodigo(String codigo);
}
