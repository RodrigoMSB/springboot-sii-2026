package cl.dgt.tramites.infrastructure.repository;

import cl.dgt.tramites.domain.entity.ObservacionInterna;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/**
 * Las cuarenta líneas del DAO heredado, en una.
 *
 * <p>El nombre del método ES la consulta. Spring Data lo lee —{@code findBy} + {@code
 * Contribuyente} + {@code Rut}— y genera el SELECT con su JOIN y su parámetro. No hay que
 * escribir SQL, y por eso no hay dónde concatenarlo.
 *
 * <p><strong>Y ahí muere el apóstrofe.</strong> El RUT viaja como PARÁMETRO, no como texto
 * pegado a la consulta: si alguien manda {@code 11111111-1' OR '1'='1}, el motor busca un
 * contribuyente cuyo RUT sea exactamente esa cadena rara. No lo encuentra, y devuelve vacío.
 * El dato dejó de poder convertirse en código.
 *
 * <p>Vive en {@code infrastructure} y no en {@code domain} por lo que AU-03 hace cumplir:
 * extiende Spring Data, y el dominio no conoce a Spring.
 */
public interface ObservacionInternaRepository extends JpaRepository<ObservacionInterna, Long> {

    List<ObservacionInterna> findByContribuyenteRut(String rut);
}
