package cl.dgt.concurrencia.repositories;

import cl.dgt.concurrencia.entities.Folio;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/** El repositorio de folios. El método interesante es el último. */
public interface FolioRepository extends JpaRepository<Folio, Long> {

    /** El último número emitido de ese año, o vacío si no hay ninguno. */
    @Query("select max(f.numero) from Folio f where f.anio = :anio")
    Optional<Integer> maxNumeroDe(@Param("anio") int anio);

    List<Folio> findByAnioOrderByNumero(int anio);

    /**
     * Borra los folios de un año. Lleva su propio {@code @Transactional} porque un borrado necesita
     * una transacción y quien lo llama —el preparador de las demos— no la abre.
     */
    @Transactional
    void deleteByAnio(int anio);

    // =========================================================================
    //  EL CANDADO — paso 4
    // -------------------------------------------------------------------------
    //  PESSIMISTIC_WRITE bloquea la fila: el primero que llega se la lleva y los
    //  demás ESPERAN a que la suelte. Se agarra de la apertura del año —el folio
    //  número 1—, que siempre existe: un candado pesimista bloquea FILAS, y si la
    //  fila no existe no hay nada que bloquear.
    //  Qué se espera ver en el SQL: PostgreSQL lo escribe «for no key update»,
    //  no «for update». Es el mismo candado para lo que aquí importa.
    //  Para pensar: ¿qué pasaría si cada hilo bloqueara una fila distinta?
    // =========================================================================
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select f from Folio f where f.anio = :anio and f.numero = 1")
    Optional<Folio> bloquearLaApertura(@Param("anio") int anio);
}
