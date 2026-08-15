package cl.dgt.tramites.infrastructure.repository;

import cl.dgt.tramites.domain.entity.ObservacionInterna;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/**
 * Guardar y buscar observaciones, sin escribir SQL.
 *
 * <p>Una interfaz, sin implementación. Spring Data la genera al arrancar, y con ella llegan
 * gratis {@code save}, {@code findById}, {@code findAll}, {@code delete}, paginación y orden.
 * {@code save} es el que convierte un objeto en una fila: le hace el {@code INSERT} y le
 * escribe de vuelta el {@code id} que generó el motor.
 *
 * <p><strong>Y el método de abajo lo escribes tú, pero no su consulta.</strong> El nombre ES la
 * consulta: Spring Data lo parte en {@code findBy} + {@code Contribuyente} + {@code Rut}, sigue
 * la relación declarada en la entidad y arma el {@code SELECT} con su {@code JOIN}. Si el
 * nombre no cuadra con las propiedades de la entidad, la aplicación no arranca y el error dice
 * cuál no encontró.
 *
 * <p>Vive en {@code infrastructure} y no en {@code domain} por lo que AU-03 hace cumplir:
 * extiende Spring Data, y el dominio no conoce a Spring.
 */
public interface ObservacionInternaRepository extends JpaRepository<ObservacionInterna, Long> {

    List<ObservacionInterna> findByContribuyenteRut(String rut);
}
