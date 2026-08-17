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
 *
 * <p>Quién la implementa: nadie, en el sentido de que no hay ningún archivo. Spring Data crea la
 * clase al arrancar y la mete en el contenedor, así que se puede pedir por constructor como
 * cualquier otra pieza — igual que en el Lab 02.
 *
 * <p>Los dos tipos entre {@code <>} son la entidad y el tipo de su clave: {@code Observacion} y
 * {@code Long}. De ahí saca Spring Data sobre qué tabla trabajar.
 *
 * <p>Un aviso que ahorra tiempo: el nombre se valida <strong>al arrancar</strong>, comprobando que
 * las propiedades existan en la entidad. Escribir {@code findByAutorr} no compila mal — hace que
 * la aplicación no arranque, y lo dice.
 */
public interface ObservacionRepository extends JpaRepository<Observacion, Long> {

    /**
     * {@code findBy} + {@code Autor} → {@code WHERE autor = ?}
     *
     * <p>«Autor» tiene que ser el nombre de una propiedad de la entidad, no el de la columna. El
     * parámetro se empareja por posición, no por nombre.
     */
    List<Observacion> findByAutor(String autor);

    /**
     * {@code findBy} + {@code Autor} + {@code And} + {@code Fecha} + {@code After}
     * → {@code WHERE autor = ? AND fecha > ?}
     *
     * <p>El vocabulario da para bastante más: {@code Or}, {@code Between}, {@code LessThan},
     * {@code Like}, {@code OrderBy…Desc}. El límite práctico es la legibilidad del nombre.
     */
    List<Observacion> findByAutorAndFechaAfter(String autor, LocalDate fecha);

    /**
     * {@code countBy} + {@code Autor} → {@code SELECT count(*) … WHERE autor = ?}
     *
     * <p>El prefijo cambia lo que se hace, no solo lo que se filtra: cuenta en la base y devuelve
     * un número, en vez de traerse las filas para contarlas en memoria. Es la demo 8.
     */
    long countByAutor(String autor);
}
