package cl.dgt.muchosamuchos.soporte;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Component
public class MiradorDeLaIntermedia {

    private final JdbcTemplate jdbc;

    public MiradorDeLaIntermedia(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    public void imprimirTodo(String titulo) {
        imprimir(titulo, jdbc.queryForList(
                "select tramite_id, documento_id from tramite_documento "
                        + "order by tramite_id, documento_id"));
    }

    public void imprimirDelTramite(Long tramiteId, String titulo) {
        imprimir(titulo, jdbc.queryForList(
                "select tramite_id, documento_id from tramite_documento "
                        + "where tramite_id = ? order by documento_id", tramiteId));
    }

    public List<String> columnas() {
        return jdbc.queryForList(
                "select column_name from information_schema.columns "
                        + "where table_name = 'tramite_documento' order by ordinal_position",
                String.class);
    }

    private void imprimir(String titulo, List<Map<String, Object>> filas) {
        System.out.println("  tramite_documento · " + titulo + " -> " + filas.size() + " filas");
        for (Map<String, Object> fila : filas) {
            System.out.println("      tramite_id=" + fila.get("tramite_id")
                    + "  documento_id=" + fila.get("documento_id"));
        }
    }
}
