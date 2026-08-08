package cl.dgt.tramites.infrastructure.repository;

import cl.dgt.tramites.domain.entity.ContadorFolio;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface ContadorFolioRepository extends JpaRepository<ContadorFolio, Short> {

    /**
     * Toma el contador con BLOQUEO PESIMISTA (SELECT ... FOR UPDATE). El primer hilo que
     * llega bloquea la fila; los demás ESPERAN a que confirme, y recién entonces leen el
     * valor ya actualizado. El candado vive en el DATO, no en el código: funciona aunque
     * corran dos instancias de la app (a diferencia de `synchronized`).
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT c FROM ContadorFolio c WHERE c.id = 1")
    Optional<ContadorFolio> tomarConBloqueo();

    /** Lectura SIN bloqueo (la usa la emisión ingenua del starter — la carrera). */
    @Query("SELECT c FROM ContadorFolio c WHERE c.id = 1")
    Optional<ContadorFolio> leerSinBloqueo();
}
