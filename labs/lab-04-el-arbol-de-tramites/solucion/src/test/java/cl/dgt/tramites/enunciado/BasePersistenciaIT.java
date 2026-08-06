package cl.dgt.tramites.enunciado;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Bean;
import org.testcontainers.postgresql.PostgreSQLContainer;

/** Config compartida: un PostgreSQL real (Testcontainers) con las migraciones de Flyway. */
@TestConfiguration(proxyBeanMethods = false)
class BasePersistenciaIT {
    @Bean
    @ServiceConnection
    PostgreSQLContainer postgres() {
        return new PostgreSQLContainer("postgres:16-alpine3.24");
    }
}
