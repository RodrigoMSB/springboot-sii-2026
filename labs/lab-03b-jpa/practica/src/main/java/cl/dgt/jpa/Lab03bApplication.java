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

@SpringBootApplication
public class Lab03bApplication {

    public static void main(String[] args) {
        SpringApplication.run(Lab03bApplication.class, args);
    }

    @Bean
    CommandLineRunner run(DemosJpa demos) {
        return args -> {
        };
    }

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
