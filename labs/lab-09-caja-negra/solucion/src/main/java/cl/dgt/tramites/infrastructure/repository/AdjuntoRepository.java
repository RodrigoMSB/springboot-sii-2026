package cl.dgt.tramites.infrastructure.repository;

import cl.dgt.tramites.domain.entity.Adjunto;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AdjuntoRepository extends JpaRepository<Adjunto, Long> {
    List<Adjunto> findByTramiteId(Long tramiteId);
}
