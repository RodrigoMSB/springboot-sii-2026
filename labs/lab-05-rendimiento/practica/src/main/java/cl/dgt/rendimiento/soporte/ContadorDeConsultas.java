package cl.dgt.rendimiento.soporte;

import jakarta.persistence.EntityManagerFactory;
import org.hibernate.SessionFactory;
import org.hibernate.stat.Statistics;
import org.springframework.stereotype.Component;

/**
 * El instrumento del laboratorio. <strong>Viene dado.</strong>
 *
 * <p>Hibernate lleva su propia contabilidad si se le enciende
 * ({@code hibernate.generate_statistics: true} en el {@code application.yml}), y aquí solo se le
 * pregunta. Sin este número, «la pantalla va lenta» es una opinión; con él, es un dato.
 *
 * <p>Se usa siempre igual: {@code reiniciar()} antes, {@code consultas()} después.
 */
@Component
public class ContadorDeConsultas {

    private final Statistics estadisticas;

    public ContadorDeConsultas(EntityManagerFactory fabrica) {
        this.estadisticas = fabrica.unwrap(SessionFactory.class).getStatistics();
    }

    /** Pone el contador a cero. Se llama justo antes de lo que se quiera medir. */
    public void reiniciar() {
        estadisticas.clear();
    }

    /** Cuántas sentencias se han preparado contra la base desde el último reinicio. */
    public long consultas() {
        return estadisticas.getPrepareStatementCount();
    }
}
