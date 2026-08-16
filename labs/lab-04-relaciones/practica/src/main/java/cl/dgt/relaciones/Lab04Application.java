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
 *
 * <p>Tal como llega, las seis llamadas están comentadas: arranca y no imprime demos. Cada paso
 * descomenta la suya.
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
            // Cada paso descomenta la suya, en este orden.
            // demos.guardarConRelacion();                 // paso 1
            // demos.navegarDeTramiteAContribuyente();     // paso 2
            // demos.listarTramitesDeUnContribuyente();    // paso 3
            // demos.lazyContraEager();                    // paso 4
            // demos.elErrorDeLaSesionCerrada();           // paso 5
            // demos.consultaQueCruzaLaRelacion();         // paso 6
        };
    }

    // -------------------------------------------------------------------------
    //  La base de datos. No hace falta leer esto para el laboratorio.
    // -------------------------------------------------------------------------
    //  PostgreSQL de verdad, arrancado como proceso hijo de este programa, igual
    //  que en el Lab 3.5. Directorio de datos fijo (.datos-pg/, aquí al lado) para
    //  que lo guardado sobreviva al apagado, y puerto fijo para poder conectarse
    //  con DBeaver o pgAdmin mientras corre. Cada lab tiene el suyo, así que
    //  varios pueden estar arriba a la vez sin pisarse.
    // -------------------------------------------------------------------------

    /** El puerto de la base. `solucion` usa el 55435. */
    static final int PUERTO_BASE = 55434;

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
