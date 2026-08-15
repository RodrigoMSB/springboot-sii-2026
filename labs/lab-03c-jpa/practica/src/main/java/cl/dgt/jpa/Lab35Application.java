package cl.dgt.jpa;

import cl.dgt.jpa.demos.DemosJpa;
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
 * <p>Al arrancar corre las demos en orden: cada una es un método de {@link DemosJpa}, imprime lo
 * que hace, y deja ver el SQL que Hibernate generó por debajo.
 *
 * <p>Después <strong>se queda corriendo</strong>, y eso es a propósito: con el programa vivo
 * puedes mirar la base con un cliente SQL y llamar a los endpoints desde Postman. Se apaga con
 * Ctrl+C.
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
    //  Dos ajustes que sí importan para el laboratorio:
    //
    //   · DIRECTORIO DE DATOS FIJO (.datos-pg/, aquí al lado). Sin esto la base
    //     nace vacía en cada arranque, y un lab que se llama «guardar y
    //     recuperar» no puede perder lo guardado al apagar. Con esto, lo que
    //     guardaste ayer sigue ahí hoy. El paso 9 lo comprueba.
    //     `setCleanDataDirectory(false)` es lo que impide que lo borre al
    //     arrancar.
    //
    //   · PUERTO FIJO 55432, en vez de uno al azar. Así puedes conectarte con
    //     DBeaver o pgAdmin MIENTRAS el programa corre y mirar la tabla por
    //     fuera. Ver el objeto en consola es ver la memoria; ver la fila en la
    //     tabla es ver la persistencia. Los datos de conexión, en el README.
    //
    //  `destroyMethod = "close"` apaga el motor cuando el programa termina; sin
    //  eso quedaría un PostgreSQL huérfano en cada ejecución.
    // -------------------------------------------------------------------------

    /** El puerto de la base. Fijo a propósito: ver la nota de arriba. */
    static final int PUERTO_BASE = 55432;

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
