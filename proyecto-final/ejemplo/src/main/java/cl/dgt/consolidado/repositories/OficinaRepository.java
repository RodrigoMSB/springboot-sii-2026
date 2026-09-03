// Busca la oficina por su código, para saber si existe y sacar su nombre.
// Tu equivalente: `ContribuyenteRepository.findByRut(...)`, que YA EXISTE en `base/`.
package cl.dgt.consolidado.repositories;

import cl.dgt.consolidado.entities.Oficina;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface OficinaRepository extends JpaRepository<Oficina, Long> {

    Optional<Oficina> findByCodigo(String codigo);
}
// ^ Devuelve `Optional` y no la entidad pelada: que no exista es un resultado posible, no un
//   error del programa. Quien llama decide qué hacer con el vacío — aquí, un 404.
