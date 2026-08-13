package cl.dgt.tramites.enunciado;

import cl.dgt.tramites.PostgresEmbebido;
import jakarta.persistence.EntityManagerFactory;
import org.hibernate.stat.Statistics;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import javax.sql.DataSource;

/**
 * Config compartida: PostgreSQL real (embebido) + el CONTADOR DE CONSULTAS ya construido.
 *
 * <p>El contador usa las {@link Statistics} de Hibernate: cuenta las sentencias JDBC preparadas
 * alrededor de un bloque de código. Es la herramienta que convierte "va lento" en un número.
 * Tú no la construyes — la usas.
 */
@TestConfiguration(proxyBeanMethods = false)
class BaseRendimientoIT {

    /**
     * El {@code DataSource} del PostgreSQL embebido, arrancado una sola vez por JVM. Antes esto
     * era un contenedor con {@code @ServiceConnection}; el motor es el mismo, lo que cambia es
     * que llega como dependencia Maven y no como imagen Docker (SPEC-022).
     */
    @Bean
    DataSource dataSource() {
        return PostgresEmbebido.nuevoDataSource();
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
