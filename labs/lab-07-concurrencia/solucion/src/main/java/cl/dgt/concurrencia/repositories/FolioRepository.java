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

public interface FolioRepository extends JpaRepository<Folio, Long> {

    @Query("select max(f.numero) from Folio f where f.anio = :anio")
    Optional<Integer> maxNumeroDe(@Param("anio") int anio);

    List<Folio> findByAnioOrderByNumero(int anio);

    @Transactional
    void deleteByAnio(int anio);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select f from Folio f where f.anio = :anio and f.numero = 1")
    Optional<Folio> bloquearLaApertura(@Param("anio") int anio);
}
