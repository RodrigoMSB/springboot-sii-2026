package cl.dgt.tramites.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.jdbc.core.JdbcTemplate;

/**
 * Siembra masiva para el escenario del N+1 (`./bin/start-lab.sh --lotes N`).
 *
 * <p>Antes esto lo hacía el script, entrando al contenedor con {@code psql}. Ya no hay
 * contenedor al que entrar, y el paquete de binarios embebidos trae el servidor pero NO el
 * cliente: solo {@code initdb}, {@code pg_ctl} y {@code postgres}. Así que la siembra se hace
 * desde dentro de la aplicación, que ya tiene una conexión abierta.
 *
 * <p>Sigue siendo SQL directo por un motivo pedagógico: si los trámites se crearan por la API,
 * estaríamos midiendo la API en vez de preparar el escenario. Dos {@code INSERT ... SELECT}
 * masivos son instantáneos; N llamadas HTTP no lo son.
 *
 * <p>Solo en el perfil {@code dev} y solo si te pasan {@code dgt.lotes}. En producción esta
 * clase no existe.
 */
@Configuration(proxyBeanMethods = false)
@Profile("dev")
@ConditionalOnProperty(name = "dgt.lotes")
class SembradorDeLotes {

    @Bean
    ApplicationRunner sembrarLotes(JdbcTemplate jdbc, @Value("${dgt.lotes}") int lotes) {
        return args -> {
            if (lotes <= 0) {
                return;
            }
            jdbc.update("""
                    INSERT INTO contribuyente (rut, razon_social, puntaje_riesgo_interno)
                      SELECT 'L' || g || '-0', 'Lote ' || g, 0 FROM generate_series(1, ?) g
                    """, lotes);
            jdbc.update("""
                    INSERT INTO tramite (contribuyente_id, tipo, estado)
                      SELECT c.id, 'DECLARACION_F29', 'BORRADOR'
                        FROM contribuyente c WHERE c.rut LIKE 'L%-0'
                    """);
        };
    }
}
