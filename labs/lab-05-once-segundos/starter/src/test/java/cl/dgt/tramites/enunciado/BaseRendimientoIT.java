package cl.dgt.tramites.enunciado;

import jakarta.persistence.EntityManagerFactory;
import org.hibernate.stat.Statistics;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Bean;
import org.testcontainers.postgresql.PostgreSQLContainer;

/**
 * Config compartida: PostgreSQL real (Testcontainers) + el CONTADOR DE CONSULTAS ya construido.
 *
 * <p>El contador usa las {@link Statistics} de Hibernate: cuenta las sentencias JDBC preparadas
 * alrededor de un bloque de código. Es la herramienta que convierte "va lento" en un número.
 * Tú no la construyes — la usas.
 */
@TestConfiguration(proxyBeanMethods = false)
class BaseRendimientoIT {

    @Bean
    @ServiceConnection
    PostgreSQLContainer postgres() {
        return new PostgreSQLContainer("postgres:16-alpine3.24");
    }

    /** Cuenta las consultas SQL que dispara `bloque`. Determinista: mismo código, mismo número. */
    static long consultasDe(EntityManagerFactory emf, Runnable bloque) {
        Statistics stats = emf.unwrap(org.hibernate.SessionFactory.class).getStatistics();
        stats.setStatisticsEnabled(true);
        stats.clear();
        bloque.run();
        return stats.getPrepareStatementCount();
    }
}
