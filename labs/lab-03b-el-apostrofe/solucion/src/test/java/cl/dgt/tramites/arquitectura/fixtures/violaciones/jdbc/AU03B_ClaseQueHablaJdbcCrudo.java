package cl.dgt.tramites.arquitectura.fixtures.violaciones.jdbc;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;

/** Viola AU-03b: abre la conexión a mano y arma la consulta con un {@code Statement}. */
public class AU03B_ClaseQueHablaJdbcCrudo {

    public String primerRut(String url) throws Exception {
        Connection cx = DriverManager.getConnection(url);
        Statement st = cx.createStatement();
        var rs = st.executeQuery("SELECT rut FROM contribuyente LIMIT 1");
        return rs.next() ? rs.getString(1) : null;
    }
}
