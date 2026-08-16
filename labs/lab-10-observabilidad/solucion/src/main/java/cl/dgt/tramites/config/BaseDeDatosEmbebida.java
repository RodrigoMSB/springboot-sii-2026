package cl.dgt.tramites.config;

import io.zonky.test.db.postgres.embedded.EmbeddedPostgres;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import javax.sql.DataSource;
import java.io.IOException;

/**
 * La base de datos del perfil {@code dev}: PostgreSQL de verdad, sin Docker.
 *
 * <p>Antes esto era un {@code compose.yaml} y Boot lo levantaba solo. Las máquinas del SII no
 * tienen Docker ni permisos para instalarlo, así que el motor llega por otro camino: Zonky
 * empaqueta los binarios oficiales de PostgreSQL como un artefacto Maven corriente. Al arrancar,
 * los extrae a una carpeta temporal del usuario y los corre como proceso hijo de este JVM. Sin
 * demonio, sin administrador, sin red.
 *
 * <p>Lo que NO cambia es lo único que el alumno tenía que aprender aquí: <strong>tú no escribes
 * una cadena de conexión</strong>. El {@code DataSource} lo publica este {@code @Bean}, Flyway
 * migra contra él y Hibernate valida contra él, exactamente igual que antes. Una cadena que no
 * existe es una cadena que no se puede filtrar.
 *
 * <p><strong>Solo en {@code dev}.</strong> En {@code prod} esta clase no se carga y la conexión
 * llega por variables de entorno, que es de lo que trata el laboratorio.
 */
@Configuration(proxyBeanMethods = false)
@Profile("dev")
@ConditionalOnProperty(name = "dgt.base-embebida.enabled", havingValue = "true", matchIfMissing = true)
class BaseDeDatosEmbebida {

    /**
     * El proceso de PostgreSQL. {@code destroyMethod = "close"} es lo que lo mata cuando el
     * contexto de Spring se cierra: sin eso quedaría un proceso huérfano por cada arranque.
     */
    @Bean(destroyMethod = "close")
    EmbeddedPostgres postgresEmbebido() throws IOException {
        return EmbeddedPostgres.builder().start();
    }

    /**
     * El {@code DataSource} que ve el resto de la aplicación. Al publicarlo como bean, la
     * autoconfiguración de Boot se aparta y nadie necesita {@code spring.datasource.url}.
     */
    @Bean
    DataSource dataSource(EmbeddedPostgres postgresEmbebido) {
        return postgresEmbebido.getPostgresDatabase();
    }
}
