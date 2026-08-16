package cl.dgt.relaciones.repositories;

import cl.dgt.relaciones.entities.Tramite;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/**
 * El repositorio de trámites.
 *
 * <p>El método de abajo llegó en el paso 6 y es el que enseña algo nuevo: el nombre
 * <strong>cruza la relación</strong>. No dice «por RUT» —el trámite no tiene RUT—, dice «por el
 * RUT de su contribuyente», y Spring Data escribe el JOIN.
 */
public interface TramiteRepository extends JpaRepository<Tramite, Long> {

    List<Tramite> findByContribuyenteRut(String rut);
}
