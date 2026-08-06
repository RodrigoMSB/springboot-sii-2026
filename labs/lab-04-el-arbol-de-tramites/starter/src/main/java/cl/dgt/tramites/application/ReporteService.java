package cl.dgt.tramites.application;

import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Reportes agregados.
 *
 * <p><strong>TODO_4 — impleméntalo con {@link JdbcClient} (≈15 min).</strong> Un total por
 * período es una pregunta de columnas y sumas. Cargar entidades para esto sería pagar el
 * peaje del ORM por objetos que nadie va a usar. {@code JdbcClient} lee filas y las mapea a
 * un {@code record}, sin tocar una sola entidad.
 *
 * <p><em>Qué escribir:</em> un SQL que sume {@code linea_f29.monto} agrupado por
 * {@code formulario29.periodo} (un JOIN entre las dos tablas), ordenado por período, y que
 * mapee cada fila a {@link TotalPorPeriodo}.
 *
 * <p>Pista: {@code jdbc.sql("...").query(TotalPorPeriodo.class).list()}. Los alias de las
 * columnas SQL deben coincidir con los componentes del record ({@code periodo}, {@code total}).
 */
@Service
public class ReporteService {

    private final JdbcClient jdbc;

    public ReporteService(JdbcClient jdbc) {
        this.jdbc = jdbc;
    }

    public List<TotalPorPeriodo> totalDeclaradoPorPeriodo() {
        throw new UnsupportedOperationException("{{TODO_4}}");
    }

    public record TotalPorPeriodo(String periodo, long total) {}
}
