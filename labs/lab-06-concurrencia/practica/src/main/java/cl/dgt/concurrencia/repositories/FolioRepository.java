package cl.dgt.concurrencia.repositories;

import cl.dgt.concurrencia.entities.Folio;
import org.springframework.data.jpa.repository.JpaRepository;
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
    //  Aquí va un método que pida la fila de apertura del año «for update», es
    //  decir, anotado con @Lock(LockModeType.PESSIMISTIC_WRITE). El primero que
    //  llegue se la lleva y los demás esperan a que la suelte.
    //  Ojo: un candado pesimista bloquea FILAS. Por eso se agarra del folio
    //  número 1, que siempre existe — si la fila no existe, no protege nada.
    //  En el SQL, PostgreSQL lo escribe «for no key update», no «for update».
    //  Para pensar: ¿qué pasaría si cada hilo bloqueara una fila distinta?
    // =========================================================================
    // escribe aquí — paso 4
}
