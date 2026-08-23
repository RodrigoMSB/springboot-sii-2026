package cl.dgt.relaciones;

import cl.dgt.relaciones.demos.DemosRelaciones;
import cl.dgt.relaciones.infra.CandadoLibre;
import cl.dgt.relaciones.infra.PuertoLibre;
import io.zonky.test.db.postgres.embedded.EmbeddedPostgres;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import javax.sql.DataSource;
import java.io.File;
import java.io.IOException;

@SpringBootApplication
public class Lab05Application {

    public static void main(String[] args) {
        SpringApplication.run(Lab05Application.class, args);
    }

    @Bean
    CommandLineRunner run(DemosRelaciones demos) {
        return args -> {
        };
    }

    static final int PUERTO_BASE = 55434;

    @Bean(destroyMethod = "close")
    EmbeddedPostgres postgresEmbebido() throws IOException {
        PuertoLibre.exigir(PUERTO_BASE);
        CandadoLibre.exigir(new File(".datos-pg"));

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
