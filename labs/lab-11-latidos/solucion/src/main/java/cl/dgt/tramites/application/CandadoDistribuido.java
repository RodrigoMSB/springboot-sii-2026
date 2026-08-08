package cl.dgt.tramites.application;

import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;

/**
 * El candado que sí funciona con varias instancias.
 *
 * <p><strong>Por qué no sirve nada de lo que ya sabes.</strong> {@code synchronized} y
 * {@code ReentrantLock} solo saben de SU JVM: dos instancias tienen dos candados distintos, y dos
 * candados distintos no son un candado. Una bandera en {@code application.yml} es peor todavía —
 * ver el acto 2 de la guía—. La respuesta es la misma que en el Lab 06 con el contador de folios:
 * <strong>el candado vive en el DATO</strong>, donde todas las instancias lo ven.
 *
 * <p><strong>Cómo se toma sin ventana de carrera.</strong> La tentación es leer, decidir y
 * escribir:
 *
 * <pre>{@code
 * if (noEstaTomado(tarea)) {   // <-- entre esta línea
 *     tomar(tarea);            // <-- y esta, cabe la otra instancia entera
 * }
 * }</pre>
 *
 * Entre las dos líneas hay una ventana, y con dos instancias esa ventana se abre lo justo para que
 * ambas crean que ganaron. Aquí no hay ventana porque <em>mirar y tomar son la misma sentencia</em>:
 * el {@code INSERT ... ON CONFLICT DO UPDATE ... WHERE} es una operación atómica del motor. Una
 * gana y recibe 1 fila afectada; la otra recibe 0. Sin empates.
 *
 * <p><strong>El reloj es el de la BASE, no el de la máquina.</strong> Los {@code now()} de este SQL
 * los evalúa PostgreSQL. Si compararamos contra la hora local de cada instancia, dos servidores con
 * dos minutos de desfase —cosa normalísima— discreparían sobre si el candado expiró. El único reloj
 * en el que las dos instancias están de acuerdo es el de la base.
 *
 * <p><strong>{@code REQUIRES_NEW}, y aquí sí corresponde.</strong> La toma del candado se
 * confirma en su propia transacción, corta, que termina antes de que empiece el trabajo. Si viajara
 * dentro de la transacción larga del cierre, ninguna otra instancia vería el candado tomado hasta
 * el commit final — que es justo cuando ya no importa. (En el Lab 06 {@code REQUIRES_NEW} era el
 * parche equivocado porque allí el número tenía que revertirse con el trabajo; aquí el candado debe
 * ser visible ANTES del trabajo. Misma herramienta, problema opuesto: por eso se aprende el
 * criterio y no la receta.)
 */
@Service
public class CandadoDistribuido {

    private final JdbcClient jdbc;

    public CandadoDistribuido(JdbcClient jdbc) {
        this.jdbc = jdbc;
    }

    /**
     * Intenta tomar el candado. Devuelve {@code true} solo si lo consiguió.
     *
     * <p>Lo consigue si el candado está libre, o si el anterior dueño ya expiró. Si otro lo tiene
     * vigente, devuelve {@code false} inmediatamente: <strong>no espera</strong>. En una tarea
     * programada esperar sería absurdo — el que no ganó no tiene nada que aportar; que se vaya a
     * dormir y lo intente en el siguiente latido.
     *
     * @param tarea    nombre del candado (uno por tarea programada)
     * @param duracion cuánto vale la toma antes de expirar sola
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public boolean intentarTomar(String tarea, String quien, Duration duracion) {
        int filas = jdbc.sql("""
                INSERT INTO candado_tarea (nombre, tomado_por, expira_en)
                VALUES (:nombre, :quien, now() + make_interval(secs => :segundos))
                ON CONFLICT (nombre) DO UPDATE
                   SET tomado_por = EXCLUDED.tomado_por,
                       expira_en  = EXCLUDED.expira_en
                 WHERE candado_tarea.expira_en < now()
                """)
                .param("nombre", tarea)
                .param("quien", quien)
                .param("segundos", (double) duracion.toSeconds())
                .update();

        // 1 = lo tomé (estaba libre, o el dueño anterior expiró). 0 = otro lo tiene vigente.
        return filas == 1;
    }

    /**
     * Suelta el candado al terminar.
     *
     * <p>No es obligatorio —la expiración lo soltaría igual— pero sin esto el siguiente latido
     * tendría que esperar el TTL completo aunque el trabajo haya durado dos segundos. Se suelta
     * en un {@code finally}: si el trabajo revienta, el candado igual se libera y el sistema no
     * queda bloqueado por una excepción.
     *
     * <p>Solo suelta lo que es SUYO ({@code tomado_por = :quien}): si el candado ya expiró y otra
     * instancia lo tomó, esta no tiene derecho a soltárselo. Sin esa condición, una instancia lenta
     * podría liberar el candado que otra acaba de ganar, y volverían a correr dos.
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void liberar(String tarea, String quien) {
        jdbc.sql("DELETE FROM candado_tarea WHERE nombre = :nombre AND tomado_por = :quien")
                .param("nombre", tarea)
                .param("quien", quien)
                .update();
    }
}
