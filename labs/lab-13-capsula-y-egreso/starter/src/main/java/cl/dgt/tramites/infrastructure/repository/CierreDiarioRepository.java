package cl.dgt.tramites.infrastructure.repository;

import cl.dgt.tramites.domain.entity.CierreDiario;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

/**
 * Los cierres diarios.
 *
 * <p>{@link #findByFecha(LocalDate)} devuelve una <strong>lista</strong>, no un {@code Optional},
 * y eso también es contenido: la tabla admite varias filas por fecha a propósito, y el tipo lo
 * dice. Un {@code Optional} aquí sería una promesa que la base no sostiene — y el test del crimen
 * necesita poder contar dos.
 */
public interface CierreDiarioRepository extends JpaRepository<CierreDiario, Long> {

    List<CierreDiario> findByFecha(LocalDate fecha);
}
