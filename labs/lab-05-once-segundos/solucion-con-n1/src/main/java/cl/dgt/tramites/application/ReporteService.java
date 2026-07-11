package cl.dgt.tramites.application;

import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Reportes agregados. Aquí NO hay entidades: {@link JdbcClient} lee filas y las suma en
 * SQL, sin cargar un solo {@code Tramite} ni sus árboles.
 *
 * <p>La lección: no todo lo que lee la base merece el peaje del ORM. Un total por período
 * es una pregunta de columnas y sumas — el ORM cargaría objetos que nadie va a usar. Para
 * escribir y navegar el dominio, JPA. Para reportar, a veces, SQL directo.
 */
@Service
public class ReporteService {

    private final JdbcClient jdbc;

    public ReporteService(JdbcClient jdbc) {
        this.jdbc = jdbc;
    }

    /** Total declarado (suma de líneas del F29) por período, ordenado por período. */
    @Transactional(readOnly = true)
    public List<TotalPorPeriodo> totalDeclaradoPorPeriodo() {
        return jdbc.sql("""
                SELECT f.periodo AS periodo, SUM(l.monto) AS total
                  FROM formulario29 f
                  JOIN linea_f29 l ON l.formulario29_id = f.id
                 GROUP BY f.periodo
                 ORDER BY f.periodo
                """)
                .query(TotalPorPeriodo.class)
                .list();
    }

    /** Un dato, no una entidad. */
    public record TotalPorPeriodo(String periodo, long total) {}
}
