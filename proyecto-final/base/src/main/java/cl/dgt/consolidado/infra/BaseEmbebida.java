package cl.dgt.consolidado.infra;

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
 * también en los slices de test (`@WebMvcTest`), que la cargan como configuración raíz. Eso
 * levantaba un SEGUNDO PostgreSQL en el mismo puerto y la suite fallaba **según el orden** —
 * verde al correr un test solo, roja al correrlos juntos.
 *
 * <p>Aquí, al ser una `@Configuration` aparte, los slices no la escanean y sólo la levanta quien
 * arranca el contexto completo.
 */
@Configuration
public class BaseEmbebida {

    static final int PUERTO = 55444;

    @Bean(destroyMethod = "close")
    EmbeddedPostgres postgresEmbebido() throws IOException {
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
