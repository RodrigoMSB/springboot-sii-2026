package cl.dgt.rendimiento.repositories;

import cl.dgt.rendimiento.dto.ResumenContribuyente;
import cl.dgt.rendimiento.entities.Contribuyente;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

/**
 * El repositorio del laboratorio. Los tres métodos de abajo devuelven <em>lo mismo</em> que
 * {@code findAll()}: lo que cambia es cuántas consultas cuesta traerlo.
 */
public interface ContribuyenteRepository extends JpaRepository<Contribuyente, Long> {

    // =========================================================================
    //  JOIN FETCH — paso 2
    // -------------------------------------------------------------------------
    //  Se escribe la consulta a mano y se le dice a Hibernate que traiga también
    //  los trámites, en la MISMA consulta. `distinct` porque el join repite el
    //  contribuyente una vez por trámite.
    //  Qué se espera ver: el contador baja de tres cifras a una.
    //  Para pensar: ¿qué pasa si esta consulta hiciera fetch de dos colecciones?
    // =========================================================================
    @Query("select distinct c from Contribuyente c left join fetch c.tramites")
    List<Contribuyente> conJoinFetch();

    // =========================================================================
    //  @EntityGraph — paso 3
    // -------------------------------------------------------------------------
    //  Lo mismo que arriba sin escribir JPQL: se nombra la relación que hay que
    //  traer y Spring Data arma el fetch. Se prefiere cuando la consulta no tiene
    //  nada especial y solo se quiere cambiar QUÉ se trae.
    //  Qué se espera ver: el mismo número de consultas que el JOIN FETCH.
    //  Para pensar: ¿en cuál de los dos se ve antes lo que va a costar?
    // =========================================================================
    @EntityGraph(attributePaths = "tramites")
    List<Contribuyente> findAllBy();

    // =========================================================================
    //  PROYECCIÓN — paso 4
    // -------------------------------------------------------------------------
    //  Cuando la pantalla solo necesita tres datos, traer entidades enteras es
    //  pagar de más: viajan todas las columnas y Hibernate se queda vigilando
    //  cada objeto. Esto trae un record con lo justo, y la cuenta la hace la base.
    //  Qué se espera ver: una sola consulta, y muchísimos menos objetos en memoria.
    //  Para pensar: ¿qué se pierde al no tener entidades? (No se pueden modificar.)
    // =========================================================================
    @Query("""
            select new cl.dgt.rendimiento.dto.ResumenContribuyente(c.rut, c.razonSocial, count(t))
            from Contribuyente c
            left join c.tramites t
            group by c.id, c.rut, c.razonSocial
            order by c.id
            """)
    List<ResumenContribuyente> resumen();
}
