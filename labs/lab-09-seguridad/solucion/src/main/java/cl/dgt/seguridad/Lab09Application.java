package cl.dgt.seguridad;

import io.zonky.test.db.postgres.embedded.EmbeddedPostgres;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import javax.sql.DataSource;
import java.io.File;
import java.io.IOException;

@SpringBootApplication
public class Lab09Application {

    public static void main(String[] args) {
        SpringApplication.run(Lab09Application.class, args);
    }

    // Puerto fijo para poder mirar la tabla de usuarios con DBeaver mientras corre — el paso 3
    // se apoya en eso.
    static final int PUERTO_BASE = 55441;

    @Bean(destroyMethod = "close")
    EmbeddedPostgres postgresEmbebido() throws IOException {
        PuertoLibre.exigir(PUERTO_BASE);

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
