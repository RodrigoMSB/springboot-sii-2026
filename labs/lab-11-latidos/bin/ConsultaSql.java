import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;

/**
 * Le hace UNA consulta a la base del laboratorio y escribe el primer valor de la primera fila.
 *
 * <pre>
 *   java --class-path &lt;postgresql.jar&gt; ConsultaSql.java &lt;url-jdbc&gt; &lt;usuario&gt; &lt;clave&gt; "SELECT …"
 * </pre>
 *
 * <p><strong>Por qué existe este archivo.</strong> El guion de sala del Lab 11 necesita preguntarle
 * a la base cuántos cierres se ejecutaron: es la evidencia del crimen —dos servidores, dos cierres—
 * y sin ella el {@code --instancias 2} sería una afirmación sin medida. Antes esa pregunta se hacía
 * con {@code docker compose exec postgres psql}. Al salir Docker (SPEC-025), PostgreSQL llega
 * embebido por Zonky… y <strong>Zonky no empaqueta {@code psql}</strong>: su carpeta {@code bin/}
 * trae {@code initdb}, {@code pg_ctl} y {@code postgres}, y nada más. Comprobado, no supuesto.
 *
 * <p>Así que la consulta se hace por JDBC, con las dos únicas cosas que ya viajan en el
 * repositorio: el JDK embebido y el driver de PostgreSQL que el propio laboratorio usa. Cero
 * dependencias nuevas, cero instalaciones y nada que el alumno tenga que tener.
 *
 * <p>Se ejecuta como <em>programa de un solo archivo</em> (JEP 330): no hay que compilarlo ni
 * generar un jar. Es andamiaje del guion, no material de estudio — el alumno no lo abre nunca.
 */
public class ConsultaSql {

    public static void main(String[] args) throws Exception {
        if (args.length != 4) {
            System.err.println("uso: ConsultaSql <url> <usuario> <clave> <sql>");
            System.exit(2);
        }
        try (Connection cx = DriverManager.getConnection(args[0], args[1], args[2]);
             Statement st = cx.createStatement();
             ResultSet rs = st.executeQuery(args[3])) {
            System.out.println(rs.next() ? String.valueOf(rs.getObject(1)) : "");
        }
    }
}
