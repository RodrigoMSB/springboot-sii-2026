package cl.dgt.observabilidad.observabilidad;

import org.springframework.boot.health.contributor.Health;
import org.springframework.boot.health.contributor.HealthIndicator;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

// Health indicator propio: no dice «UP» porque sí, consulta la base y NOMBRA lo que se cayó.
@Component("baseDeDatos")
public class SaludDeLaBase implements HealthIndicator {

    private final DataSource dataSource;

    public SaludDeLaBase(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Override
    public Health health() {
        long inicio = System.currentTimeMillis();
        try (Connection conexion = dataSource.getConnection();
             Statement sentencia = conexion.createStatement()) {

            sentencia.execute("SELECT count(*) FROM tramite");

            return Health.up()
                    .withDetail("consulta", "SELECT count(*) FROM tramite")
                    .withDetail("milisegundos", System.currentTimeMillis() - inicio)
                    .build();

        } catch (SQLException e) {
            return Health.down()
                    .withDetail("causa", "la base de datos no responde")
                    .withDetail("detalle", e.getMessage().lines().findFirst().orElse(""))
                    .withDetail("milisegundos", System.currentTimeMillis() - inicio)
                    .build();
        }
    }
}
