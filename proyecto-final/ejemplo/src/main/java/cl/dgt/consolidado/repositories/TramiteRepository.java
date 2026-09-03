// La consulta del encargo: los trámites de una oficina entre dos fechas.
// Tu equivalente: la misma consulta filtrando por el RUT del contribuyente.
package cl.dgt.consolidado.repositories;

import cl.dgt.consolidado.entities.Tramite;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

public interface TramiteRepository extends JpaRepository<Tramite, Long> {

    @Query("""
            select t from Tramite t
            where t.oficinaCodigo = :codigo
              and t.fecha between :desde and :hasta
            order by t.fecha
            """)
    List<Tramite> delPeriodo(@Param("codigo") String codigo,
                             @Param("desde") LocalDate desde,
                             @Param("hasta") LocalDate hasta);
}
// ^ UNA consulta, no una por trámite, y ése es el punto que la rúbrica mira. La alternativa
//   ingenua —traer la oficina y recorrer sus trámites— dispara un SELECT por cada uno: es el
//   N+1 del Lab 06, con otro disfraz.
//
//   Se comprueba encendiendo `spring.jpa.show-sql` y contando: tiene que salir UNA línea de
//   `select` por petición, no una por fila.
//
//   `between` incluye los dos extremos, que es lo que espera quien pide «del 1 al 30».
//   Y el `order by` está porque una lista sin orden explícito sale en el orden que quiera la
//   base — y eso hace que un test pase hoy y falle mañana.
//
//   OJO CON TU CASO: el tuyo filtra por el RUT, que vive en `Contribuyente`, no en `Tramite`.
//   Eso significa que tu `where` navega la relación (`t.contribuyente.rut = :rut`), y ahí SÍ
//   conviene un `join fetch` si además necesitas datos del contribuyente. Aquí no hace falta
//   porque `oficinaCodigo` es una columna del propio trámite.
