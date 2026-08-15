package cl.dgt.tramites;

import io.zonky.test.db.postgres.embedded.EmbeddedPostgres;

import javax.sql.DataSource;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * PostgreSQL embebido para la suite.
 *
 * <p><strong>Un solo motor por JVM, una base de datos por contexto de Spring.</strong> Las dos
 * mitades de esa frase importan, y por razones distintas.
 *
 * <p><em>Un motor.</em> Arranca en el inicializador estático, no por clase de test. Es el mismo
 * motivo por el que los labs posteriores usan contenedores singleton: Spring cachea el contexto
 * y lo reutiliza entre clases, así que un motor con ciclo de vida por-clase se apagaría dejando
 * al contexto cacheado apuntando a un puerto muerto — "connection refused" en el test siguiente,
 * y media hora buscando el bug donde no está.
 *
 * <p><em>Una base por contexto.</em> Cada contexto recibe una base RECIÉN CREADA y vacía, y
 * Flyway la migra desde cero. Sin esto todas las clases compartirían la misma base y se
 * pisarían: en el Lab 06 los tests de concurrencia emiten folios de verdad, y el guardián de la
 * semilla —que cuenta cuántos folios hay— encontraría catorce donde debe haber uno. Aislar por
 * base cuesta milisegundos; aislar por motor costaría un arranque de PostgreSQL por clase.
 *
 * <p>No hay que cerrar nada a mano: Zonky registra un <em>shutdown hook</em> y el proceso hijo
 * muere con el JVM de la suite.
 */
public final class PostgresEmbebido {

    private static final EmbeddedPostgres INSTANCIA = arrancar();
    private static final AtomicInteger CONTADOR = new AtomicInteger();

    private static final String USUARIO = "postgres";

    private PostgresEmbebido() {
    }

    private static EmbeddedPostgres arrancar() {
        try {
            return EmbeddedPostgres.builder().start();
        } catch (IOException e) {
            throw new UncheckedIOException("No se pudo arrancar el PostgreSQL embebido", e);
        }
    }

    /**
     * Crea una base vacía y devuelve su URL JDBC.
     *
     * <p>Llámala UNA vez por contexto, desde el {@code @DynamicPropertySource}, y guarda el
     * resultado: si se la pasaras a {@code registro.add} como proveedor, Spring podría invocarla
     * más de una vez y cada llamada crearía otra base.
     */
    public static String nuevaBase() {
        return INSTANCIA.getJdbcUrl(USUARIO, crear());
    }

    /**
     * Lo mismo, pero como {@code DataSource} ya construido: para los tests que comparten
     * configuración con {@code @Import} de una {@code @TestConfiguration}, donde
     * {@code @DynamicPropertySource} no llega.
     */
    public static DataSource nuevoDataSource() {
        return INSTANCIA.getDatabase(USUARIO, crear());
    }

    private static String crear() {
        String nombre = "dgt_" + CONTADOR.incrementAndGet();
        try (Connection cx = INSTANCIA.getPostgresDatabase().getConnection();
             Statement st = cx.createStatement()) {
            st.execute("CREATE DATABASE " + nombre);
        } catch (SQLException e) {
            throw new IllegalStateException("No se pudo crear la base de prueba " + nombre, e);
        }
        return nombre;
    }

    public static String usuario() {
        return USUARIO;
    }

    /** Zonky arranca con autenticación `trust`: la contraseña no se verifica, pero el pool la pide. */
    public static String clave() {
        return USUARIO;
    }
}
