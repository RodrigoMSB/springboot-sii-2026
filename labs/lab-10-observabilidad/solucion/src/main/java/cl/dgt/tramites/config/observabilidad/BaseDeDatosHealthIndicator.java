package cl.dgt.tramites.config.observabilidad;

import org.springframework.boot.health.contributor.Health;
import org.springframework.boot.health.contributor.HealthIndicator;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Component;

/**
 * ¿Puede esta instancia hacer su trabajo? Para la DGT, «hacer su trabajo» significa
 * <strong>hablar con su base de datos</strong>: sin ella no se lista un trámite, no se emite un
 * folio, no se guarda nada. Una API de trámites sin base no está degradada: está inútil.
 *
 * <p><strong>Por qué existe este archivo si Boot ya trae uno.</strong> Actuator registra solo un
 * {@code DataSourceHealthIndicator} cuando detecta un {@code DataSource}. Lo escribimos a mano una
 * vez por dos razones: (1) para ver el contrato por dentro —devolver {@link Health}, nombrar el
 * componente, adjuntar detalles—, que es lo que necesitarás el día que el chequeo sea de algo que
 * Boot no conoce (TESO, una carpeta de adjuntos, una licencia por vencer); y (2) porque el nombre
 * del bean se convierte en la CLAVE que aparece en la respuesta, y esa clave es lo que le dice al
 * operador <em>qué</em> se cayó. {@code baseDeDatosHealthIndicator} → {@code "baseDeDatos"}.
 *
 * <p><strong>La pregunta es SELECT 1, no «¿hay un objeto DataSource?».</strong> El objeto siempre
 * existe: lo creó Spring al arrancar y seguirá ahí aunque PostgreSQL lleve una hora muerto.
 * Preguntar por el objeto es preguntarle al enfermo si respira mirándole la fotografía. Hay que
 * ir a la base y volver.
 *
 * <p><strong>Nota de honestidad sobre el tiempo.</strong> Este chequeo tarda, como máximo, lo que
 * Hikari tarde en rendirse al pedir conexión ({@code spring.datasource.hikari.connection-timeout}).
 * Un health check que tarda 30 segundos en admitir que está caído es casi tan inútil como uno que
 * miente: para cuando responde, el balanceador ya te mandó tráfico. Por eso este lab baja ese
 * timeout a 5 s — ver el comentario en {@code application.yml}.
 */
@Component("baseDeDatos")
public class BaseDeDatosHealthIndicator implements HealthIndicator {

    private final JdbcClient jdbc;

    public BaseDeDatosHealthIndicator(JdbcClient jdbc) {
        this.jdbc = jdbc;
    }

    @Override
    public Health health() {
        long inicio = System.nanoTime();
        try {
            jdbc.sql("SELECT 1").query(Integer.class).single();
            return Health.up()
                    .withDetail("motor", "PostgreSQL")
                    .withDetail("sonda", "SELECT 1")
                    .withDetail("tardanzaMs", milisegundosDesde(inicio))
                    .build();
        } catch (Exception fallo) {
            // El detalle nombra el problema en el idioma del que va a leerlo a las 3 AM. No se
            // vuelca la traza entera: el health es un semáforo, no un log. El log ya existe (Lab 09).
            return Health.down()
                    .withDetail("motor", "PostgreSQL")
                    .withDetail("sonda", "SELECT 1")
                    .withDetail("tardanzaMs", milisegundosDesde(inicio))
                    .withDetail("causa", causaRaiz(fallo))
                    .build();
        }
    }

    private long milisegundosDesde(long inicioNanos) {
        return (System.nanoTime() - inicioNanos) / 1_000_000;
    }

    /** La excepción de JDBC llega envuelta en tres capas; al operador le sirve la de más adentro. */
    private String causaRaiz(Throwable fallo) {
        Throwable actual = fallo;
        while (actual.getCause() != null && actual.getCause() != actual) {
            actual = actual.getCause();
        }
        String mensaje = actual.getMessage();
        return actual.getClass().getSimpleName() + (mensaje == null ? "" : ": " + mensaje);
    }
}
