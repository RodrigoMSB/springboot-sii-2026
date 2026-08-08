package cl.dgt.contribuyentes;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * El acceso a la base propia de este servicio. Nada que explicar aquí que no se
 * haya explicado en el Lab 04: lo interesante de este repositorio es <em>quién
 * no puede llamarlo</em>.
 */
public interface ContribuyenteRepository extends JpaRepository<Contribuyente, Long> {

    Optional<Contribuyente> findByRut(String rut);
}
