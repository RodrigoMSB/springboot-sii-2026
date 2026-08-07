package cl.dgt.tramites.infrastructure.repository;

import cl.dgt.tramites.domain.entity.Tramite;
import cl.dgt.tramites.domain.tipo.EstadoTramite;
import cl.dgt.tramites.application.TramiteResumenDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

/**
 * Repositorio de trámites. Muestra las tres formas de preguntarle a la base:
 * métodos derivados (Spring Data escribe el JPQL por ti), y {@code @Query} explícito.
 */
public interface TramiteRepository extends JpaRepository<Tramite, Long> {

    // --- TODO_2: consultas derivadas -----------------------------------------
    // El NOMBRE del método es la consulta. Spring Data lo traduce a JPQL.
    List<Tramite> findByContribuyenteRut(String rut);

    Page<Tramite> findByContribuyenteRut(String rut, Pageable pagina);

    boolean existsByContribuyenteRutAndEstado(String rut, EstadoTramite estado);

    long countByEstado(EstadoTramite estado);

    // --- TODO_3: JPQL multi-entidad ------------------------------------------
    /**
     * Trámites de un período cuyo F29 ya fue presentado (estado PRESENTADO o posterior).
     *
     * <p>Nota deliberada: NO usa {@code JOIN FETCH}. Ese existe, y es la respuesta a una
     * pregunta que todavía no te has hecho — la próxima semana te la vas a hacer. Aquí
     * solo navegamos la relación para filtrar, no para traerla cargada.
     */
    @Query("""
            SELECT t FROM Tramite t
            JOIN t.formulario29 f
            WHERE f.periodo = :periodo
              AND t.estado <> cl.dgt.tramites.domain.tipo.EstadoTramite.BORRADOR
            """)
    List<Tramite> presentadosDelPeriodo(@Param("periodo") String periodo);

    /**
     * El listado, como PROYECCIÓN. Trae SOLO las columnas que la tabla pinta —id, tipo,
     * estado, y el rut del contribuyente vía un JOIN— en UNA consulta. No carga la entidad
     * Tramite ni su árbol: no hay N+1 posible, porque no hay relaciones que iterar.
     */
    @Query("""
            SELECT new cl.dgt.tramites.application.TramiteResumenDto(
                       t.id, t.tipo, cast(t.estado as string), c.rut)
            FROM Tramite t
            JOIN t.contribuyente c
            """)
    org.springframework.data.domain.Page<TramiteResumenDto> resumenPaginado(
            org.springframework.data.domain.Pageable pagina);
}
