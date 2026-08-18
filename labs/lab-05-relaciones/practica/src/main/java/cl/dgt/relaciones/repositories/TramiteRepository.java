package cl.dgt.relaciones.repositories;

import cl.dgt.relaciones.entities.Tramite;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TramiteRepository extends JpaRepository<Tramite, Long> {

    // Paso 6 · declara un método de consulta cuyo nombre cruce la relación.
    // escribe aquí
}
