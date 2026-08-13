package cl.dgt.tramites;

import io.zonky.test.db.postgres.embedded.EmbeddedPostgres;

import java.io.IOException;
import java.io.UncheckedIOException;

/**
 * PostgreSQL embebido para la suite: <strong>una instancia por JVM</strong>.
 *
 * <p>Arranca en el inicializador estático, no por clase de test. Es a propósito, y es el mismo
 * motivo por el que los labs posteriores usan contenedores singleton: Spring cachea el contexto y
 * lo reutiliza entre clases de test, así que una base con ciclo de vida por-clase se apagaría
 * dejando al contexto cacheado apuntando a un puerto muerto — "connection refused" en el test
 * siguiente, y media hora buscando el bug donde no está. Una sola base, puertos estables.
 *
 * <p>No hay que cerrarla a mano: Zonky registra un <em>shutdown hook</em> y el proceso hijo muere
 * con el JVM de la suite.
 */
public final class PostgresEmbebido {

    private static final EmbeddedPostgres INSTANCIA = arrancar();

    private static final String USUARIO = "postgres";
    private static final String BASE = "postgres";

    private PostgresEmbebido() {
    }

    private static EmbeddedPostgres arrancar() {
        try {
            return EmbeddedPostgres.builder().start();
        } catch (IOException e) {
            throw new UncheckedIOException("No se pudo arrancar el PostgreSQL embebido", e);
        }
    }

    public static String jdbcUrl() {
        return INSTANCIA.getJdbcUrl(USUARIO, BASE);
    }

    public static String usuario() {
        return USUARIO;
    }

    /** Zonky arranca con autenticación `trust`: la contraseña no se verifica, pero el pool la pide. */
    public static String clave() {
        return USUARIO;
    }
}
