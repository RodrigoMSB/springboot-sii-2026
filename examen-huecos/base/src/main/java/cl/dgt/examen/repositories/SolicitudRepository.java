package cl.dgt.examen.repositories;

import cl.dgt.examen.entities.Solicitud;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SolicitudRepository extends JpaRepository<Solicitud, Long> {

    /** Viene resuelta: la usa el resumen. */
    long countByOficinaCodigo(String codigo);

    /** Viene resuelta: la usa la suma por estado. */
    List<Solicitud> findByEstado(String estado);

    // =========================================================================
    //  HUECO 03 · Cuantas solicitudes hay en un estado
    // -------------------------------------------------------------------------
    //  Falta la consulta que CUENTA las solicitudes de un estado.
    //  No devuelve la lista: devuelve el numero, y lo cuenta la base de datos.
    //
    //  ESTA LISTO CUANDO · pasa el test H-03
    // =========================================================================

    // =========================================================================
    //  HUECO 04 · Las solicitudes de un estado, de la mas reciente a la mas antigua
    // -------------------------------------------------------------------------
    //  Falta la consulta que devuelve las solicitudes de un estado ORDENADAS
    //  por fecha, de la mas reciente a la mas antigua. El orden lo pone la
    //  consulta, no el codigo Java que la llama.
    //
    //  ESTA LISTO CUANDO · pasa el test H-04
    // =========================================================================

    List<Solicitud> findAllByOrderByFechaDesc();
}
