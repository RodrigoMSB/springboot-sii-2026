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

@SpringBootApplication
public class Lab06Application {

    public static void main(String[] args) {
        SpringApplication.run(Lab06Application.class, args);
    }

    @Bean
    CommandLineRunner run(DemosConcurrencia demos) {
        return args -> {
        };
    }

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
