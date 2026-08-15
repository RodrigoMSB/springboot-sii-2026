package cl.dgt.concurrencia;

import cl.dgt.concurrencia.demos.DemosConcurrencia;
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
 * <p>Tres demos: emitir folios de uno en uno, emitirlos veinte a la vez con el mismo código, y
 * emitirlos veinte a la vez con una línea más. Las tres imprimen cuántos folios distintos salieron.
 *
 * <p>Después se queda corriendo, para poder mirar la tabla con un cliente SQL. Se apaga con Ctrl+C.
 */
@SpringBootApplication
public class Lab06Application {

    public static void main(String[] args) {
        SpringApplication.run(Lab06Application.class, args);
    }

    /** Las demos, en el orden del guion. */
    @Bean
    CommandLineRunner run(DemosConcurrencia demos) {
        return args -> {
            // Cada paso descomenta la suya.
            // demos.deUnoEnUno();    // paso 1
            // demos.elCrimen();      // paso 2
            // demos.conCandado();    // paso 4
        };
    }

    // -------------------------------------------------------------------------
    //  La base de datos. No hace falta leer esto para el laboratorio.
    //  Igual que en los labs 3.5, 04 y 05: PostgreSQL de verdad como proceso
    //  hijo, con su directorio de datos y su puerto propios.
    // -------------------------------------------------------------------------

    /** El puerto de la base. `solucion` usa el 55439. */
    static final int PUERTO_BASE = 55438;

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
