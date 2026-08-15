package cl.dgt.rendimiento.repositories;

import cl.dgt.rendimiento.entities.Contribuyente;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * El repositorio del laboratorio. Tal como llega solo tiene lo heredado: {@code findAll},
 * {@code count}, {@code save}…
 *
 * <p>Los tres métodos que se agregan hoy devuelven <em>lo mismo</em> que {@code findAll()}. Lo
 * único que cambia es cuántas consultas cuesta traerlo.
 */
public interface ContribuyenteRepository extends JpaRepository<Contribuyente, Long> {

    // escribe aquí — paso 2: la consulta con JOIN FETCH
    // escribe aquí — paso 3: el mismo resultado con @EntityGraph
    // escribe aquí — paso 4: la proyección al record ResumenContribuyente
}
