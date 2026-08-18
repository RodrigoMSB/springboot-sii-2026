package cl.dgt.jpa.repositories;

import cl.dgt.jpa.entities.Observacion;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

// Sin implementación: Spring Data la fabrica leyendo el nombre de cada método.
public interface ObservacionRepository extends JpaRepository<Observacion, Long> {

    List<Observacion> findByAutor(String autor);

    List<Observacion> findByAutorAndFechaAfter(String autor, LocalDate fecha);

    long countByAutor(String autor);
}
