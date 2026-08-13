package cl.dgt.tramites.enunciado;

import cl.dgt.tramites.PostgresEmbebido;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import javax.sql.DataSource;

/** Config compartida: un PostgreSQL real (embebido) con las migraciones de Flyway. */
@TestConfiguration(proxyBeanMethods = false)
class BasePersistenciaIT {
    /**
     * El {@code DataSource} del PostgreSQL embebido, arrancado una sola vez por JVM. Antes esto
     * era un contenedor con {@code @ServiceConnection}; el motor es el mismo, lo que cambia es
     * que llega como dependencia Maven y no como imagen Docker (SPEC-022).
     */
    @Bean
    DataSource dataSource() {
        return PostgresEmbebido.nuevoDataSource();
    }
}
