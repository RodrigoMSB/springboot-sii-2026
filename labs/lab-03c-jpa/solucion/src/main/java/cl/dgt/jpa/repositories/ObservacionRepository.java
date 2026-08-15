package cl.dgt.jpa.repositories;

import cl.dgt.jpa.entities.Observacion;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

/**
 * El repositorio: una interfaz, sin implementación.
 *
 * <p>De {@code JpaRepository} llegan gratis {@code save}, {@code findById}, {@code findAll},
 * {@code deleteById} y {@code count}. Los tres métodos de abajo los declaras tú, pero tampoco
 * escribes su consulta: <strong>el nombre del método ES la consulta</strong>, y Spring Data la
 * genera leyéndolo.
 */
public interface ObservacionRepository extends JpaRepository<Observacion, Long> {

    /** findBy + Autor → WHERE autor = ? */
    List<Observacion> findByAutor(String autor);

    /** findBy + Autor + And + Fecha + After → WHERE autor = ? AND fecha > ? */
    List<Observacion> findByAutorAndFechaAfter(String autor, LocalDate fecha);

    /** countBy + Autor → SELECT count(*) ... WHERE autor = ? */
    long countByAutor(String autor);
}
