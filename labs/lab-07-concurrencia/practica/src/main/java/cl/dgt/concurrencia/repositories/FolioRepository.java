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

    // Paso 4 · declara la búsqueda que toma el candado al leer.
    // escribe aquí
}
