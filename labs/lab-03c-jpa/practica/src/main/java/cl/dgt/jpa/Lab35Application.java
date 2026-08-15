package cl.dgt.jpa;

import cl.dgt.jpa.demos.DemosJpa;
import io.zonky.test.db.postgres.embedded.EmbeddedPostgres;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import javax.sql.DataSource;
import java.io.IOException;

/**
 * El programa del laboratorio.
 *
 * <p>No es una aplicación web: arranca, corre las demos en orden, y termina. Cada demo es un
 * método de {@link DemosJpa}, y cada una imprime lo que hace y deja ver el SQL que Hibernate
 * generó por debajo.
 */
@SpringBootApplication
public class Lab35Application {

    public static void main(String[] args) {
        SpringApplication.run(Lab35Application.class, args);
    }

    /**
     * Las demos, en el orden del guion.
     *
     * <p>Están todas comentadas. Cada paso de la sesión descomenta la suya, y así el programa
     * va creciendo contigo: si algo falla, sabes exactamente qué línea lo rompió.
     */
    @Bean
    CommandLineRunner run(DemosJpa demos) {
        return args -> {
            // demos.guardar();
            // demos.buscarPorId();
            // demos.listarTodas();
            // demos.buscarPorAutor();
            // demos.buscarConDosCondiciones();
            // demos.actualizar();
            // demos.borrar();
            // demos.contar();
        };
    }

    // -------------------------------------------------------------------------
    //  La base de datos. No hace falta leer esto para el laboratorio.
    // -------------------------------------------------------------------------
    //  PostgreSQL de verdad, arrancado como proceso hijo de este programa. Llega
    //  como una dependencia Maven más: sin Docker y sin instalar nada.
    //
    //  `destroyMethod = "close"` es lo que lo apaga cuando el programa termina;
    //  sin eso quedaría un PostgreSQL huérfano en cada ejecución.
    // -------------------------------------------------------------------------

    @Bean(destroyMethod = "close")
    EmbeddedPostgres postgresEmbebido() throws IOException {
        return EmbeddedPostgres.builder().start();
    }

    @Bean
    DataSource dataSource(EmbeddedPostgres postgresEmbebido) {
        return postgresEmbebido.getPostgresDatabase();
    }
}
