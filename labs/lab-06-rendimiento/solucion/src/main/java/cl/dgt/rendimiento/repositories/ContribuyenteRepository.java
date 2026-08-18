package cl.dgt.rendimiento.repositories;

import cl.dgt.rendimiento.dto.ResumenContribuyente;
import cl.dgt.rendimiento.entities.Contribuyente;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface ContribuyenteRepository extends JpaRepository<Contribuyente, Long> {

    @Query("select distinct c from Contribuyente c left join fetch c.tramites")
    List<Contribuyente> conJoinFetch();

    @EntityGraph(attributePaths = "tramites")
    List<Contribuyente> findAllBy();

    @Query("""
            select new cl.dgt.rendimiento.dto.ResumenContribuyente(c.rut, c.razonSocial, count(t))
            from Contribuyente c
            left join c.tramites t
            group by c.id, c.rut, c.razonSocial
            order by c.id
            """)
    List<ResumenContribuyente> resumen();
}
