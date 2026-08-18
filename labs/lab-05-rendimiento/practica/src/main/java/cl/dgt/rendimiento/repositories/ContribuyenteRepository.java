package cl.dgt.rendimiento.repositories;

import cl.dgt.rendimiento.entities.Contribuyente;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ContribuyenteRepository extends JpaRepository<Contribuyente, Long> {

    // Paso 2 · declara la consulta que trae contribuyentes y trámites de una vez.
    // escribe aquí
}
