package cl.dgt.relaciones;

import cl.dgt.relaciones.demos.DemosRelaciones;
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
 * <p>Al arrancar corre las demos en orden: cada una es un método de {@link DemosRelaciones},
 * imprime lo que hace, y deja ver el SQL que Hibernate generó por debajo.
 *
 * <p>Después <strong>se queda corriendo</strong>, para poder mirar la base con un cliente SQL. Se
 * apaga con Ctrl+C.
 */
@SpringBootApplication
public class Lab04Application {

    public static void main(String[] args) {
        SpringApplication.run(Lab04Application.class, args);
    }

    /** Las demos, en el orden del guion. Cada línea es un paso de la sesión. */
    @Bean
    CommandLineRunner run(DemosRelaciones demos) {
        return args -> {
            demos.guardarConRelacion();
            demos.navegarDeTramiteAContribuyente();
            demos.listarTramitesDeUnContribuyente();
            demos.lazyContraEager();
            demos.elErrorDeLaSesionCerrada();
            demos.consultaQueCruzaLaRelacion();
        };
    }

    // -------------------------------------------------------------------------
    //  La base de datos. No hace falta leer esto para el laboratorio.
    // -------------------------------------------------------------------------
    //  PostgreSQL de verdad, arrancado como proceso hijo de este programa, igual
    //  que en el Lab 3b. Directorio de datos fijo (.datos-pg/, aquí al lado) para
    //  que lo guardado sobreviva al apagado, y puerto fijo para poder conectarse
    //  con DBeaver o pgAdmin mientras corre. Cada lab tiene el suyo, así que
    //  varios pueden estar arriba a la vez sin pisarse.
    // -------------------------------------------------------------------------

    /** El puerto de la base. `practica` usa el 55434. */
    static final int PUERTO_BASE = 55435;

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
