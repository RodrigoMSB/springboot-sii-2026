package cl.dgt.examen.repositories;

import cl.dgt.examen.entities.Solicitud;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SolicitudRepository extends JpaRepository<Solicitud, Long> {

    /** Viene resuelta: la usa el resumen. */
    long countByOficinaCodigo(String codigo);

    /** Viene resuelta: la usa la suma por estado. */
    List<Solicitud> findByEstado(String estado);

    long countByEstado(String estado);

    List<Solicitud> findByEstadoOrderByFechaDesc(String estado);

    List<Solicitud> findAllByOrderByFechaDesc();
}
