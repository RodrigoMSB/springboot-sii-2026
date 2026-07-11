package cl.dgt.tramites.infrastructure.repository;

import cl.dgt.tramites.domain.entity.Tramite;
import cl.dgt.tramites.domain.tipo.EstadoTramite;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/**
 * Repositorio de trámites.
 *
 * <p><strong>TODO_2 y TODO_3 — completa las consultas.</strong> Ahora mismo cada método está
 * "stubeado" con un cuerpo {@code default} que lanza: por eso compilan y los tests los pueden
 * llamar, pero fallan. Tu trabajo es reemplazar cada uno.
 *
 * <p><em>TODO_2 (consultas derivadas):</em> borra el cuerpo {@code default} y deja SOLO la
 * firma. Spring Data lee el nombre del método y escribe el JPQL por ti:
 * {@code List<Tramite> findByContribuyenteRut(String rut);} y ya funciona. Lo mismo con los
 * demás. El nombre ES la consulta.
 *
 * <p><em>TODO_3 (JPQL multi-entidad):</em> reemplaza {@code presentadosDelPeriodo} por una
 * declaración con {@code @Query(...)} que traiga los trámites de un período cuyo F29 no está
 * en BORRADOR, con parámetro nombrado {@code :periodo}.
 *
 * <p>Pista sobre {@code JOIN FETCH}: existe, y es la respuesta a una pregunta que todavía no
 * te has hecho. La próxima semana te la vas a hacer. Hoy NO lo uses — hay un test que lo
 * verifica.
 */
public interface TramiteRepository extends JpaRepository<Tramite, Long> {

    // TODO_2 — reemplaza cada default por la firma derivada (borra el cuerpo).
    default List<Tramite> findByContribuyenteRut(String rut) {
        throw new UnsupportedOperationException("{{TODO_2}}");
    }

    default Page<Tramite> findByContribuyenteRut(String rut, Pageable pagina) {
        throw new UnsupportedOperationException("{{TODO_2}}");
    }

    default boolean existsByContribuyenteRutAndEstado(String rut, EstadoTramite estado) {
        throw new UnsupportedOperationException("{{TODO_2}}");
    }

    default long countByEstado(EstadoTramite estado) {
        throw new UnsupportedOperationException("{{TODO_2}}");
    }

    // TODO_3 — reemplaza por una declaración con @Query (parámetro nombrado, SIN join fetch).
    default List<Tramite> presentadosDelPeriodo(String periodo) {
        throw new UnsupportedOperationException("{{TODO_3}}");
    }
}
