package cl.dgt.rendimiento;

import cl.dgt.rendimiento.demos.DemosRendimiento;
import cl.dgt.rendimiento.soporte.CargadorDeDatos;
import io.zonky.test.db.postgres.embedded.EmbeddedPostgres;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import javax.sql.DataSource;
import java.io.File;
import java.io.IOException;

/**
 * El programa del laboratorio.
 *
 * <p>Primero siembra la base si hace falta —200 contribuyentes con 5 trámites cada uno— y después
 * corre las cinco demos. Cada una imprime <strong>cuántas consultas</strong> costó y cuántos
 * milisegundos tardó.
 *
 * <p>Después se queda corriendo, para poder mirar la base con un cliente SQL. Se apaga con Ctrl+C.
 */
@SpringBootApplication
public class Lab05Application {

    public static void main(String[] args) {
        SpringApplication.run(Lab05Application.class, args);
    }

    /** Las demos, en el orden del guion. Cada línea es un paso de la sesión. */
    @Bean
    CommandLineRunner run(CargadorDeDatos cargador, DemosRendimiento demos) {
        return args -> {
            cargador.sembrarSiHaceFalta();

            demos.elCrimen();
            demos.conJoinFetch();
            demos.conEntityGraph();
            demos.conProyeccion();
            demos.laPantallaQueNoNecesitaTramites();
        };
    }

    // -------------------------------------------------------------------------
    //  La base de datos. No hace falta leer esto para el laboratorio.
    //  Igual que en los labs 3.5 y 04: PostgreSQL de verdad como proceso hijo,
    //  con directorio de datos fijo para que lo sembrado sobreviva al apagado, y
    //  puerto propio para que varios labs puedan estar arriba a la vez.
    // -------------------------------------------------------------------------

    /** El puerto de la base. `practica` usa el 55436. */
    static final int PUERTO_BASE = 55437;

    @Bean(destroyMethod = "close")
    EmbeddedPostgres postgresEmbebido() throws IOException {
        return EmbeddedPostgres.builder()
                .setPort(PUERTO_BASE)
                .setDataDirectory(new File(".datos-pg"))
                .setCleanDataDirectory(false)
                .start();
    }

    @Bean
    DataSource dataSource(EmbeddedPostgres postgresEmbebido) {
        return postgresEmbebido.getPostgresDatabase();
    }
}
