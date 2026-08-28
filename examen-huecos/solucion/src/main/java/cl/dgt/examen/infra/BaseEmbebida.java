package cl.dgt.examen.infra;

import io.zonky.test.db.postgres.embedded.EmbeddedPostgres;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.sql.DataSource;
import java.io.File;
import java.io.IOException;

/**
 * PostgreSQL embebido, en su propia clase de configuración y NO en la clase de arranque.
 *
 * <p>La diferencia importa: un `@Bean` declarado en la clase `@SpringBootApplication` se crea
 * también en los slices de test, que la cargan como configuración raíz. Eso levantaba un SEGUNDO
 * PostgreSQL en el mismo puerto y la suite fallaba **según el orden**.
 */
@Configuration
public class BaseEmbebida {

    static final int PUERTO = 55446;

    @Bean(destroyMethod = "close")
    EmbeddedPostgres postgresEmbebido() throws IOException {
        PuertoLibre.exigir(PUERTO);
        CandadoLibre.exigir(new File(".datos-pg"));

        return EmbeddedPostgres.builder()
                .setPort(PUERTO)
                .setDataDirectory(new File(".datos-pg"))
                .setCleanDataDirectory(false)
                .start();
    }

    @Bean
    DataSource dataSource(EmbeddedPostgres postgresEmbebido) {
        return postgresEmbebido.getPostgresDatabase();
    }
}
