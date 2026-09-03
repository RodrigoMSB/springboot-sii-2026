package cl.dgt.concurrencia.repositories;

import cl.dgt.concurrencia.entities.Folio;
import org.springframework.data.jpa.repository.JpaRepository;
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

    // Un lock CON NOMBRE, dentro de la transacción. PostgreSQL se lo entrega al primero que lo
    // pide y deja esperando a los demás hasta que esa transacción termina. No hay ninguna fila
    // que bloquear: el nombre del turno es el número del año.
    //
    // Devuelve Object y no void a propósito: es un `select`, así que no lleva @Modifying, y
    // Spring Data necesita un tipo de retorno para ejecutarlo como consulta. El valor se ignora.
    @Query(value = "select pg_advisory_xact_lock(:anio)", nativeQuery = true)
    Object tomarElTurnoDelAnio(@Param("anio") long anio);
}
