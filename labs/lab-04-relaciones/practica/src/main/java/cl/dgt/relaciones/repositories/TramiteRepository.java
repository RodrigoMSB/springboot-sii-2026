package cl.dgt.relaciones.repositories;

import cl.dgt.relaciones.entities.Tramite;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * El repositorio de trámites. Tal como llega solo trae lo heredado: {@code save}, {@code findById},
 * {@code findAll}, {@code deleteAll}…
 *
 * <p>En el paso 6 se le agrega un método cuyo nombre <strong>cruza la relación</strong>.
 */
public interface TramiteRepository extends JpaRepository<Tramite, Long> {

    // escribe aquí — paso 6
}
