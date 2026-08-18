package cl.dgt.rendimiento.soporte;

import jakarta.persistence.EntityManagerFactory;
import org.hibernate.SessionFactory;
import org.hibernate.stat.Statistics;
import org.springframework.stereotype.Component;

@Component
public class ContadorDeConsultas {

    private final Statistics estadisticas;

    public ContadorDeConsultas(EntityManagerFactory fabrica) {
        this.estadisticas = fabrica.unwrap(SessionFactory.class).getStatistics();
    }

    public void reiniciar() {
        estadisticas.clear();
    }

    public long consultas() {
        return estadisticas.getPrepareStatementCount();
    }
}
