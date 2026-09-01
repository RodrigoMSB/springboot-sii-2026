package cl.dgt.muchosamuchos.soporte;

import jakarta.persistence.EntityManagerFactory;
import org.hibernate.SessionFactory;
import org.hibernate.stat.Statistics;
import org.springframework.stereotype.Component;

@Component
// El instrumento del paso 4: sin contar, «Set es mejor que List» es una opinión.
public class ContadorDeSentencias {

    private final Statistics estadisticas;

    public ContadorDeSentencias(EntityManagerFactory fabrica) {
        this.estadisticas = fabrica.unwrap(SessionFactory.class).getStatistics();
    }

    public void reiniciar() {
        estadisticas.clear();
    }

    public long sentencias() {
        return estadisticas.getPrepareStatementCount();
    }
}
