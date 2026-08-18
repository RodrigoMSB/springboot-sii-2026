package cl.dgt.rendimiento.soporte;

import jakarta.persistence.EntityManagerFactory;
import org.hibernate.SessionFactory;
import org.hibernate.stat.Statistics;
import org.springframework.stereotype.Component;

@Component
// El instrumento del laboratorio: sin contar las consultas, el N+1 no se ve.
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
